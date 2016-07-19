/*
 * Copyright 2013-2015 Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.slu.tpen.transfer;

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import imageLines.ImageCache;
import textdisplay.Folio;
import textdisplay.Project;
import user.User;
import static edu.slu.util.LangUtils.buildQuickMap;
import static edu.slu.util.ServletUtils.getDBConnection;

/**
 * Class which manages serialisation to JSON-LD. Builds a Map containing the
 * Project's data, and then uses Jackson to serialise it as JSON.
 *
 * @author tarkvara
 */
public class JsonLDExporter {

   /**
    * Holds data which will be serialised to JSON.
    */
   Map<String, Object> manifestData;

   /**
    * Populate a map which will contain all the relevant project information.
    *
    * @param proj the project to be exported.
    * @throws SQLException
    */
   public JsonLDExporter(Project proj, User u) throws SQLException, IOException {
      Folio[] folios = proj.getFolios();

      try {
         String projName = "http://t-pen.org/" + URLEncoder.encode(proj.getProjectName(), "UTF-8");
         manifestData = new LinkedHashMap<>();
         manifestData.put("@context", "http://iiif.io/api/presentation/1/context.json");
//         manifestData.put("@context", "http://www.shared-canvas.org/ns/context.json");
         manifestData.put("@id", "http://t-pen.org/TPEN/project/"+ proj.getProjectID());
//         manifestData.put("@id", projName + "/manifest.json");
         manifestData.put("@type", "sc:Manifest");
         textdisplay.Metadata mdata = proj.getMetadata();
         String label = (mdata.getTitle()!=null)
                 ? mdata.getTitle()
                 : proj.getProjectName();
         if(mdata.getSubtitle()!=null){
             label = label + ": " + mdata.getSubtitle();
         }
         String description = (mdata.getDescription()!=null)
                 ? mdata.getDescription()
                 : proj.getProjectName()+" created by t-pen.org for JSON-LD export.";
         manifestData.put("label", label);
         manifestData.put("description", description);
         // List<Map<String, Object>> metadata = new ArrayList<>();
         // TODO: iterate through mdata to add to "metadata" on manifest.
         Map<String, Object> pages = new LinkedHashMap<>();
//         pages.put("@id", projName + "/sequence/normal");
         pages.put("@id","http://t-pen.org/TPEN/project/"+ proj.getProjectID() + "/sequence/normal");
         pages.put("@type", "sc:Sequence");
         pages.put("label", "Current Page Order");

         List<Map<String, Object>> pageList = new ArrayList<>();
         for (Folio f : folios) {
            pageList.add(buildPage(proj.getProjectID(), projName, f, u));
         }
         pages.put("canvases", pageList);
         manifestData.put("sequences", new Object[] { pages });
      } catch (UnsupportedEncodingException ignored) {
         LOG.info("Never happens, because UTF-8 support is built in.");
      }
   }

   public String export() throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(manifestData);
   }

   /**
    * Get the map which contains the serialisable information for the given
    * page.
    *
    * @param f the folio to be exported
    * @return a map containing the relevant info, suitable for Jackson
    * serialisation
    */
   private Map<String, Object> buildPage(int projID, String projName, Folio f, User u) throws SQLException, IOException {

      String pageName = URLEncoder.encode(f.getPageName(), "UTF-8");
      String canvasID = projName + "/canvas/" + pageName;

      Dimension pageDim = ImageCache.getImageDimension(f.getFolioNumber());
      if (pageDim == null) {
         LOG.log(Level.INFO, "Image for {0} not found in cache, loading image...", f.getFolioNumber());
         pageDim = f.getImageDimension();
      }
      LOG.log(Level.INFO, "pageDim={0}", pageDim);

      Map<String, Object> result = new LinkedHashMap<>();
      result.put("@id", canvasID);
      result.put("@type", "sc:Canvas");
      result.put("label", f.getPageName());

      int canvasHeight = 1000;
      result.put("height", canvasHeight);
      
      if (pageDim != null) {
         int canvasWidth = pageDim.width * canvasHeight / pageDim.height;  // Convert to canvas coordinates.
         result.put("width", canvasWidth);
      }
      List<Object> images = new ArrayList<>();
      Map<String, Object> imageAnnot = new LinkedHashMap<>();
      imageAnnot.put("@id", projName + "/image/" + pageName);
      imageAnnot.put("@type", "oa:Annotation");
      imageAnnot.put("motivation", "sc:painting");
      
      Map<String, Object> imageResource = buildQuickMap("@id", String.format("%s%s&user=%s", Folio.getRbTok("SERVERURL"), f.getImageURLResize(), u.getUname()), "@type", "dctypes:Image", "format", "image/jpeg");
      if (pageDim != null) {
         imageResource.put("height", pageDim.height);
         imageResource.put("width", pageDim.width);
      }
      imageAnnot.put("resource", imageResource);
      imageAnnot.put("on", canvasID);
      images.add(imageAnnot);
// cubap: This looks like it should already be working!
      result.put("images", images);

      List<Object> transcription = null;
      try (Connection conn = getDBConnection()) {
         try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transcription WHERE projectID = ? AND folio = ? ORDER BY x, y")) {
            stmt.setInt(1, projID);
            stmt.setInt(2, f.getFolioNumber());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
               if (transcription == null) {
                  transcription = new ArrayList<>();
               }
               // Body of the annotation.  Contains the actual text.
               int lineID = rs.getInt("id");
               Map<String, Object> lineAnnot = new LinkedHashMap<>();
               String lineURI = projName + "/line/" + lineID;
               lineAnnot.put("@id", lineURI);
               lineAnnot.put("@type", "oa:Annotation");
               lineAnnot.put("motivation", "http://filteredpush.org/ontologies/oa/oad#transcribing");
               String lineText = rs.getString("text");
               if (StringUtils.isNotBlank(lineText)) {
                  lineText = ESAPI.encoder().decodeForHTML(lineText);
               }
               lineAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "chars", lineText));
               lineAnnot.put("on", String.format("%s#xywh=%d,%d,%d,%d", canvasID, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height")));
               transcription.add(lineAnnot);

               String note = rs.getString("comment");
               if (StringUtils.isNotBlank(note)) {
                  Map<String, Object> noteAnnot = new LinkedHashMap<>();
                  noteAnnot.put("@id", projName + "/note/" + lineID);
                  noteAnnot.put("@type", "oa:Annotation");
                  noteAnnot.put("motivation", "oa:commenting");
                  noteAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "chars", note));
                  noteAnnot.put("on", lineURI);
                  transcription.add(noteAnnot);
               }
            }
         }
      }
      if (transcription != null) {
         Map<String, Object> otherContent = buildQuickMap("@id", projName + "/transcription/" + pageName, "@type", "sc:AnnotationList");
         otherContent.put("resources", transcription);
         result.put("otherContent", new Object[] { otherContent });
      }
      // FIXME: technically, the standard does not want the AnnotationList embedded, but that makes this more difficult.
      return result;
   }

   private static final Logger LOG = Logger.getLogger(JsonLDExporter.class.getName());
}
