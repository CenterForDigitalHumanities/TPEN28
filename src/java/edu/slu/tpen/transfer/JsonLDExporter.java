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
import static utils.JsonHelper.buildNoneLanguageMap;     
import java.io.IOException;
import static java.lang.System.out;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import static textdisplay.Metadata.getMetadataAsJSON;
import textdisplay.Project;
import user.User;
import static utils.JsonHelper.*;

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
       System.out.println("Project "+projID+" has "+folios.length+" folios.");
       String projName = getRbTok("SERVERURL") + "manifest/"+projID;
       if (profile.contains("v3")){
            manifestData = new LinkedHashMap<>();
            manifestData.put("@context", "http://iiif.io/api/presentation/3/context.json");
            // Fix value of context
            manifestData.put("id", projName + "/manifest.json");
            manifestData.put("type", "Manifest");
            // Not preferred value
            //Remember that this is a Metadata title, not project name...
            manifestData.put("label",buildNoneLanguageMap(proj.getProjectName()));
            manifestData.put("metadata", getMetadataAsJSON(projID, profile));
            //Map<String, Object> services = JsonHelper.buildServices();
            //manifestData.put("services", new Object[] { services });
            JSONArray canvases = new JSONArray(); 
            JSONArray thumbnail = new JSONArray();
            JSONObject thumbObj = new JSONObject();
            
            /**
             * Default the thumbnail for this Manifest to the first folios image
             */
            Folio thumbFolio = folios[0];
            String imageURL = thumbFolio.getImageURL();
            if (imageURL.startsWith("/")) {
                imageURL = String.format("%spageImage?folio=%s",getRbTok("SERVERURL"), thumbFolio.getFolioNumber());
            }
            thumbObj.accumulate("id", imageURL);
            thumbObj.accumulate("type", "Image");
            thumbObj.accumulate("format", "image/jpeg");
            //thumbObj.accumulate("width", 300);
            //thumbObj.accumulate("height", 200);
            thumbnail.add(thumbObj);
            manifestData.put("thumbnail", thumbnail);            
            for (Folio f : folios) {
                //Map<String, Object> page = buildPage(proj.getProjectID(), projName, f, u);
                JSONObject page = buildPage(proj.getProjectID(), f, u, "v3");
                if(!page.isEmpty()){
                    canvases.add(page);
                }
                else{
                    LOG.log(WARNING, "Omitting canvas from folio "+f.getFolioNumber()+".  Check folio URL " + f.getImageURL());
                }
            }
            manifestData.put("items", canvases);
            manifestData.put("annotations", new JSONArray());
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
       Folio[] folios = proj.getFolios(); //System.out.println("Put all canvas together");
       System.out.println("Project "+proj+" has "+folios.length+" folios.");
       int projID = proj.getProjectID();
       String projName = getRbTok("SERVERURL") + "manifest/"+projID;
       manifestData = new LinkedHashMap<>();
       manifestData.put("@context", "http://iiif.io/api/presentation/2/context.json");
       manifestData.put("@id", projName + "/manifest.json");
       manifestData.put("@type", "sc:Manifest");
       //Remember that this is a Metadata title, not project name...
       manifestData.put("label", proj.getProjectName());
       manifestData.put("metadata", getMetadataAsJSON(projID));
       /**
        * Default the thumbnail for this Manifest to the first folios image
        */
       Folio thumbFolio = folios[0];
       JSONObject thumbObj = new JSONObject();
       String imageURL = thumbFolio.getImageURL();
       if (imageURL.startsWith("/")) {
           imageURL = String.format("%spageImage?folio=%s",getRbTok("SERVERURL"), thumbFolio.getFolioNumber());
       }
       thumbObj.accumulate("@id", imageURL);
       thumbObj.accumulate("@type", "dctypes:Image");
       thumbObj.accumulate("format", "image/jpeg");
       //thumbObj.accumulate("width", 300);
       //thumbObj.accumulate("height", 200);
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
       //manifestData.put("service",new Object[] { service });
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
           Map<String, Object> page = buildPage(proj.getProjectID(), f, u);
           if(!page.isEmpty()){
               pageList.add(page);
           }
           else{
               LOG.log(WARNING, "Omitting canvas from folio "+f.getFolioNumber()+".  Check folio URL "+ f.getImageURL());
           }
       }
       //System.out.println("Put all canvas together");
       pages.put("canvases", pageList);
       manifestData.put("sequences", new Object[] { pages });
   }

   public String export() throws JsonProcessingException {
      if(manifestData.containsKey("@id")){
          LOG.log(INFO, "Send out manifest {0}", manifestData.get("@id"));
      }
      else if (manifestData.containsKey("id")){
          LOG.log(INFO, "Send out manifest {0}", manifestData.get("id"));
      }
      else{
          LOG.log(INFO, "Send out manifest");
      }
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
    
   private static final Logger LOG = getLogger(JsonLDExporter.class.getName());
}
