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
import static textdisplay.Annotation.getAnnotationSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.tpen.entity.Image.Canvas.*;
import java.util.ArrayList;
import static utils.JsonHelper.buildNoneLanguageMap;     
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import static java.lang.System.out;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import net.sf.json.JSONArray;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import static textdisplay.Metadata.getMetadataAsJSON;
import textdisplay.Project;
import user.User;
import utils.*;
import static utils.JsonHelper.buildAnnotationForManifest;
import static utils.JsonHelper.buildPage;

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
   /*
   A seperate constructor that still takes Project and User, but with the includement of Profile
   we are able to distinguish which constructor to use by searching the url and if finding presi 3 would send it to them
   But if we are unable to determine what version the User request, we will send presi 2
   Values for profile: "v3, iiif/v3, version3, ... etc..."
   */
public JsonLDExporter(Project proj, User u, String profile) throws SQLException, IOException
{   
       Folio[] folios = proj.getFolios();

       int projID = proj.getProjectID();
       String projName = getRbTok("SERVERURL") + "manifest/"+projID;
       if (profile.contains("v3")){
           System.out.println("manifest v3");
           List<Map<String, Object>> pageList = new ArrayList<>();
//           for (Folio f : folios) {
//               pageList.add(JsonHelper.buildPage(proj.getProjectID(), projName, f, u,"A"));
//           }
           //System.out.println("Put all canvas together");
           manifestData = new LinkedHashMap<>();
           manifestData.put("@context", "http://iiif.io/api/presentation/3/context.json");
           // Fix value of context
           manifestData.put("id", projName + "/manifest.json");
           manifestData.put("type", "Manifest");
           // Not preferred value
           //Remember that this is a Metadata title, not project name...
           manifestData.put("label",buildNoneLanguageMap(proj.getProjectName()));
           manifestData.put("metadata", getMetadataAsJSON(projID, profile));
           List<Map<String, Object>> canvasList = new ArrayList<>();
           
//            for (Folio f : folios) {
//                System.out.println(f.folioNumber);
//                canvasList.add(JsonHelper.buildPage(proj.getProjectID(), projName, f, u, "v3"));
//		}
//            manifestData.put("items", canvasList );

            Map<String, Object> services = JsonHelper.buildServices();
            manifestData.put("services", new Object[] { services });
	   
	    JSONArray canvases = new JSONArray(); 
	    JSONArray annotations = new JSONArray();
            for (Folio f : folios) {
                canvases.add(buildPage(proj.getProjectID(), projName, f, u, services, "v3"));
                String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
//                annotationPage.add(        getAnnotationLinesForAnnotationPage(proj.getProjectID(),projName,f.getFolioNumber(),u.getUID(),"v3"));
                  annotations.add(getAnnotationSet(proj.getProjectID(),f.getFolioNumber()));
		
            }
//                System.out.println(pageList.toString());
            manifestData.put("items", canvases);
	    manifestData.put("annotations", new JSONArray());
	    
//            manifestData.put("annotations", annotationPage);
       }
         
}

   /**
    * Populate a map which will contain all the relevant project information.
    *
    * @param proj the project to be exported.
     * @param u  
    * @throws SQLException
     * @throws IOException <--ß
    */
   public JsonLDExporter(Project proj, User u) throws SQLException, IOException {
      Folio[] folios = proj.getFolios();
      int projID = proj.getProjectID();

      try {
          System.out.println("v2 manifest");
         String projName = getRbTok("SERVERURL") + "manifest/"+projID;
         manifestData = new LinkedHashMap<>();
         manifestData.put("@context", "http://iiif.io/api/presentation/2/context.json");
         manifestData.put("@id", projName + "/manifest.json");
         manifestData.put("@type", "sc:Manifest");
         //Remember that this is a Metadata title, not project name...
         manifestData.put("label", proj.getProjectName());
         manifestData.put("metadata", getMetadataAsJSON(projID));

           Map<String, Object> service = new LinkedHashMap<>();
         service.put("@context", "http://iiif.io/api/auth/1/context.json");
         service.put("@id","http://t-pen.org/TPEN/login.jsp");
         service.put("profile", "http://iiif.io/api/auth/1/login");
         service.put("label", "T-PEN Login");
         service.put("header", "Login for image access");
         service.put("description", "Agreement requires an open T-PEN session to view images");
         service.put("confirmLabel", "Login");
         service.put("failureHeader", "T-PEN Login Failed");
         service.put("failureDescription", "<a href=\"http://t-pen.org/TPEN/about.jsp\">Read Agreement</a>");
        Map<String, Object> logout = new LinkedHashMap<>();
         logout.put("@id", "http://t-pen.org/TPEN/login.jsp");
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
            pageList.add(JsonHelper.buildPage(proj.getProjectID(), projName, f, u));
         }
         //System.out.println("Put all canvas together");
         pages.put("canvases", pageList);
         manifestData.put("sequences", new Object[] { pages });
      }

      catch (UnsupportedEncodingException ignored) {
      //System.out.println("Put all canvas together");
   }
   }

   public String export() throws JsonProcessingException {
        out.println("Send out manifest");
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(manifestData);
   }
   
 
   private static final Logger LOG = getLogger(JsonLDExporter.class.getName());
}
