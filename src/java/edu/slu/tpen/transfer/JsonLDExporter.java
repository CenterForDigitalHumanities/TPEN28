/*
 * Copyright 2013-2014 Saint Louis University. Licensed under the
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.tpen.entity.Image.Canvas.getLinesForProject;
import static edu.slu.util.LangUtils.buildQuickMap;
import static imageLines.ImageCache.getImageDimension;
import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.String.format;
import static java.lang.System.out;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import net.sf.json.JSONArray;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.FolioDims;
import static textdisplay.FolioDims.createFolioDimsRecord;
import static textdisplay.Metadata.getMetadataAsJSON;
import textdisplay.Project;
import user.User;

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
      int projID = proj.getProjectID();
      try {
          //System.out.println("Export project "+projID);
         String projName = getRbTok("SERVERURL") + "manifest/"+projID;
         manifestData = new LinkedHashMap<>();
         manifestData.put("@context", "http://www.shared-canvas.org/ns/context.json");
         manifestData.put("@id", projName + "/manifest.json");
         manifestData.put("@type", "sc:Manifest");
         //Remember that this is a Metadata title, not project name...
         manifestData.put("label", proj.getProjectName());
         manifestData.put("metadata", getMetadataAsJSON(projID));

           Map<String, Object> service = new LinkedHashMap<>();
         service.put("@context", "http://iiif.io/api/auth/1/context.json");
         service.put("@id","https://t-pen.org/TPEN/login.jsp");
         service.put("profile", "http://iiif.io/api/auth/1/login");
         service.put("label", "T-PEN Login");
         service.put("header", "Login for image access");
         service.put("description", "Agreement requires an open T-PEN session to view images");
         service.put("confirmLabel", "Login");
         service.put("failureHeader", "T-PEN Login Failed");
         service.put("failureDescription", "<a href=\"https://t-pen.org/TPEN/about.jsp\">Read Agreement</a>");
        Map<String, Object> logout = new LinkedHashMap<>();
         logout.put("@id", "https://t-pen.org/TPEN/login.jsp");
         logout.put("profile", "http://iiif.io/api/auth/1/logout");
         logout.put("label", "End T-PEN Session");
        service.put("service",new Object[] { logout });

         manifestData.put("service",new Object[] { service });
      
         
         Map<String, Object> pages = new LinkedHashMap<>();
         pages.put("@id", getRbTok("SERVERURL")+"manifest/"+projID + "/sequence/normal");
         pages.put("@type", "sc:Sequence");
         pages.put("label", "Current Page Order");

         List<Map<String, Object>> pageList = new ArrayList<>();
         //System.out.println("I found "+folios.length+" pages");
         int index = 0;
         for (Folio f : folios) {
             index++;
             //System.out.println("Build page "+index);
            Map<String, Object> page = buildPage(proj.getProjectID(), projName, f, u);
            if(!page.isEmpty()){
                pageList.add(page);
            }
            else{
                System.out.println("Omitting canvas from folio "+f.getFolioNumber());
                System.out.println("Check folio URL "+f.getImageURL());
            }
         }
         //System.out.println("Put all canvas together");
         pages.put("canvases", pageList);
         manifestData.put("sequences", new Object[] { pages });
      } 
      catch (UnsupportedEncodingException ignored) {
      }
   }

   public String export() throws JsonProcessingException {
        out.println("Send out manifest");
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
      
        try{
            String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
            // See if we have an entry cached for all the Canvas and Image size data
            FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
            // Initial check the image cache table for this image.
            Dimension storedDims = getImageDimension(f.getFolioNumber());
            JSONArray otherContent;
            LOG.log(INFO, "pageDim={0}", pageDim);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("@id", canvasID);
            result.put("@type", "sc:Canvas");
            result.put("label", f.getPageName());
            int canvasHeight = 0;
            int canvasWidth = 0;
            
            // Do all the work necessary to set storedDims up front.  Only resolve the image if you have to.
            if(null == pageDim || pageDim.getImageWidth() <= 0 || pageDim.getImageHeight() <= 0){
                if(storedDims == null || storedDims.width <=0 || storedDims.height <=0){
                    // The Image dimensions are not cached.  Try to resolve the image and get them.
                    System.out.println("Need to resolve image headers for dimensions because there was no canvas dimension entry for this Folio: "+f.getFolioNumber());
                    storedDims = f.getImageDimension(); 
                    if(null == storedDims || storedDims.height <=0 || storedDims.width <= 0){
                        // Upstream when this empty object is detected, it is omitted from the Canvas' Array
                        System.out.println("No way to get Image dimensions.  Image height and/or width was 0.  See "+f.getImageURL());
                        return new LinkedHashMap<>();
                    }
                }
            }
            else{
                // pageDim has the image dimensions, let's trust them so we don't have to resolve the image.
                storedDims = new Dimension(pageDim.getImageWidth(), pageDim.getImageHeight());
            }
            
            if(null == pageDim || pageDim.getCanvasHeight() <= 0 || pageDim.getCanvasWidth() <= 0){
                // The Canvas dimensions are not cached.  We need to generate them using the image's width and height.
                canvasHeight = 1000;
                canvasWidth = storedDims.width * canvasHeight / storedDims.height; 
            }
            else{
                // The Canvas Dimensions are cached.
                canvasHeight = pageDim.getCanvasHeight();
                canvasWidth = pageDim.getCanvasWidth(); 
            }
            //Generate the FolioDims entry so we don't have to do this work again.  Note we can only create it, not update an existing one that may have a 0.
            if(null == pageDim){
                System.out.println("Create folio dim record for "+f.getFolioNumber());
                System.out.println(storedDims.width+","+ storedDims.height);
                System.out.println(canvasWidth+","+ canvasHeight);
                createFolioDimsRecord(storedDims.width, storedDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
            }
            result.put("width", canvasWidth);
            result.put("height", canvasHeight);
            List<Object> images = new ArrayList<>();
            Map<String, Object> imageAnnot = new LinkedHashMap<>();
            imageAnnot.put("@type", "oa:Annotation");
            imageAnnot.put("motivation", "sc:painting");
            String imageURL = f.getImageURL();
            if (imageURL.startsWith("/")) {
                imageURL = String.format("%spageImage?folio=%s",getRbTok("SERVERURL"), f.getFolioNumber());
            }
            Map<String, Object> imageResource = buildQuickMap("@id", imageURL, "@type", "dctypes:Image", "format", "image/jpeg");

            if (storedDims.height > 0 && storedDims.width > 0) { //We could ignore this and put the 0's into the image annotation
                //doing this check will return invalid images because we will not include height and width of 0.
                imageResource.put("width", storedDims.width ); 
                imageResource.put("height", storedDims.height ); 
            }
            imageAnnot.put("resource", imageResource);
            imageAnnot.put("on", canvasID);
            images.add(imageAnnot);
            //If this list was somehow stored in the SQL DB, we could skip calling to the store every time.
            //System.out.println("Get otherContent");
            //System.out.println(projID + "  " + canvasID + "  " + f.getFolioNumber() + "  " + u.getUID());
            otherContent = getLinesForProject(projID, canvasID, f.getFolioNumber(), u.getUID()); //Can be an empty array now.
            //System.out.println("Finalize result");
            result.put("otherContent", otherContent);
            result.put("images", images);
            return result;
        }
        catch(Exception e){
            Map<String, Object> empty = new LinkedHashMap<>();
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            System.out.println("buidPage Error.  See stack trace below.");
            System.out.println(e);
            System.out.println(Arrays.toString(e.getStackTrace()));
            return empty;
        }
   }
   private static final Logger LOG = getLogger(JsonLDExporter.class.getName());
}
