/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import edu.slu.tpen.servlet.util.CreateAnnoListUtil;
import edu.slu.util.ServletUtils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.*;
import textdisplay.Folio;
import textdisplay.PartnerProject;
import textdisplay.Project;

/**
 *
 * @author bhaberbe
 * 
 * Copy all project metadata and the annotation list with its annotations for each canvas (here named folio).  Makes use of Mongo Bulk operation capabilities
 * to limit the amount of necessary http connections which greatly improved speed.
 */
public class CopyProjectAndAnnos extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String result = "";
    	int uID = ServletUtils.getUID(request, response);
        if(null != request.getParameter("projectID") && uID != -1){
            Integer projectID = Integer.parseInt(request.getParameter("projectID"));      
            //System.out.println("Copy project and annos for "+projectID);
            try {
                //find original project and copy to a new project. 
                Project templateProject = new Project(projectID);
                Connection conn = ServletUtils.getDBConnection();
                conn.setAutoCommit(false);
                //in this method, it copies everything about the project.
                if(null != templateProject.getProjectName())
                {
                    Project thisProject = new Project(templateProject.copyProjectWithoutTranscription(conn, uID));
                    //set partener project. It is to make a connection on switch board. 
                    thisProject.setAssociatedPartnerProject(projectID);
                    PartnerProject theTemplate = new PartnerProject(projectID);
                    thisProject.copyButtonsFromProject(conn, theTemplate.getTemplateProject());
                    thisProject.copyHotkeysFromProject(conn, theTemplate.getTemplateProject());
                    conn.commit();
                    Folio[] folios = thisProject.getFolios();
                    //System.out.println("Created a new project template.  What was the ID assigned to it: "+thisProject.getProjectID());
                    if(null != folios && folios.length > 0)
                    {
                        for(int i = 0; i < folios.length; i++)
                        {
                            //System.out.println("Starting copy for canvas");
                            Folio folio = folios[i];
                            //get annotation list for each canvas
                            JSONObject annoLsQuery = new JSONObject();
                            annoLsQuery.element("@type", "sc:AnnotationList");
                            //Parse folio.getImageURL() to retrieve paleography pid, and then generate new canvas id
                            String imageURL = folio.getImageURL();
                            // use regex to extract paleography pid
                            String canvasID = Constant.PALEO_CANVAS_ID_PREFIX + imageURL.replaceAll("^.*(paleography[^/]+).*$", "/$1"); //for paleo
                            //String canvasID = Folio.getRbTok("SERVERURL") + templateProject.getProjectName() + "/canvas/" + URLEncoder.encode(folio.getPageName(), "UTF-8"); // for slu testing
                            annoLsQuery.element("on", canvasID);
                            //System.out.println(annoLsQuery.toString());
                            URL postUrlannoLs = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/getAnnotationByProperties.action");
                            HttpURLConnection ucAnnoLs = (HttpURLConnection) postUrlannoLs.openConnection();
                            ucAnnoLs.setDoInput(true);
                            ucAnnoLs.setDoOutput(true);
                            ucAnnoLs.setRequestMethod("POST");
                            ucAnnoLs.setUseCaches(false);
                            ucAnnoLs.setInstanceFollowRedirects(true);
                            ucAnnoLs.addRequestProperty("content-type", "application/x-www-form-urlencoded");
                            ucAnnoLs.connect();
                            DataOutputStream dataOutAnnoLs = new DataOutputStream(ucAnnoLs.getOutputStream());
                            dataOutAnnoLs.writeBytes("content=" + URLEncoder.encode(annoLsQuery.toString(), "utf-8"));
                            dataOutAnnoLs.flush();
                            dataOutAnnoLs.close();
                            BufferedReader readerAnnoLs = new BufferedReader(new InputStreamReader(ucAnnoLs.getInputStream(),"utf-8"));
                            String lineAnnoLs = "";
                            StringBuilder sbAnnoLs = new StringBuilder();
//                                System.out.println("=============================");  
//                                System.out.println("Contents of annotation list starts");  
//                                System.out.println("=============================");  
                            while ((lineAnnoLs = readerAnnoLs.readLine()) != null){
//                                    System.out.println(lineAnnoLs);
                                sbAnnoLs.append(lineAnnoLs);
                            }
//                                System.out.println("=============================");  
//                                System.out.println("Contents of annotation list ends");  
//                                System.out.println("=============================");
                            readerAnnoLs.close();
                            ucAnnoLs.disconnect();
                            //transfer annotation list string to annotation list JSON Array. 
                            JSONArray ja_allAnnoLists = new JSONArray();
                            String getAnnoResponse ="";
                            try{
                               getAnnoResponse = sbAnnoLs.toString();
                            }catch(Exception e){
                               getAnnoResponse = "[]";
                            }
                            ja_allAnnoLists = JSONArray.fromObject(getAnnoResponse); //This is the list of all AnnotatationLists attached to this folio.
                            JSONObject jo_annotationList = new JSONObject();
                            JSONObject jo_masterList = new JSONObject();
                           // System.out.println("SIZE OF ANNO LIST LIST    "+ja_allAnnoLists.size());
                            if(ja_allAnnoLists.size() > 0){
                                //System.out.println(ja_allAnnoLists);
                                //find the annotations list whose proj matches or use the master
                                for(int x =0; x<ja_allAnnoLists.size(); x++){
                                    JSONObject current_list = ja_allAnnoLists.getJSONObject(x);
//                                    System.out.println(current_list.getString("@id"));
                                    if(null!=current_list.get("proj")){ //make sure this list has proj field
                                        String current_proj = current_list.getString("proj");
                                     //   System.out.println("CURRENT PROJ: "+current_proj+" == "+templateProject.getProjectID()+"?");
                                     //   System.out.println(current_proj.equals("master"));
                                        if(current_proj.equals("master")){
                                            jo_masterList = current_list;
                                        }
                                        if(current_proj.matches("^\\d+$") && Integer.parseInt(current_proj) == templateProject.getProjectID()){ //if its id equal to the id of the project we are copying
                                            jo_annotationList = current_list; //if so, thats the list we want
                                            break;
                                        }
                                        else{ //it was not a match, are we done looking at all lists?
                                            if(x == (ja_allAnnoLists.size()-1)){ //if none of them match, we want the first to be our list to copy (master list)
                                                jo_annotationList = jo_masterList; //assuming the first object is the master.  if not, we will have to do something
//                                                System.out.println(jo_annotationList.getString("@id"));
                                                break;
                                            }
                                        }
                                    }
                                    else{ //it was null, are we done looking at all lists?
                                        if(x == (ja_allAnnoLists.size()-1)){ //if none of them match, we want the first to be our list to copy (master list)
                                            jo_annotationList = jo_masterList; //assuming the first object is the master.  if not, we will have to do something
 //                                           System.out.println(jo_annotationList.getString("@id"));
                                            break;
                                        }
                                    }
                                }
                            }
                            JSONArray new_resources = new JSONArray();
                            JSONArray resources = new JSONArray();
                            if(jo_annotationList.size() > 0 || (null != jo_annotationList.get("resources") && !jo_annotationList.get("resources").toString().equals("[]"))){
                                try{
                                    resources = (JSONArray) jo_annotationList.get("resources");
                                }
                                catch(JSONException e){
                                    //If this list can't be parsed, the copied list will have errors.  Just define it as empty as the fail.  
                                }
                                URL postUrlCopyAnno = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/batchSaveFromCopy.action");
                                HttpURLConnection ucCopyAnno = (HttpURLConnection) postUrlCopyAnno.openConnection();
                                ucCopyAnno.setDoInput(true);
                                ucCopyAnno.setDoOutput(true);
                                ucCopyAnno.setRequestMethod("POST");
                                ucCopyAnno.setUseCaches(false);
                                ucCopyAnno.setInstanceFollowRedirects(true);
                                ucCopyAnno.addRequestProperty("content-type", "application/x-www-form-urlencoded");
                                ucCopyAnno.connect();
                                DataOutputStream dataOutCopyAnno = new DataOutputStream(ucCopyAnno.getOutputStream());
                                String str_resources = "";
                                if(resources.size() > 0){
                                    str_resources = resources.toString();
                                }
                                else{
                                    str_resources = "[]";
                                }
                                dataOutCopyAnno.writeBytes("content=" + URLEncoder.encode(str_resources, "utf-8"));
                                dataOutCopyAnno.flush();
                                dataOutCopyAnno.close();
                                BufferedReader returnedAnnoList = new BufferedReader(new InputStreamReader(ucCopyAnno.getInputStream(),"utf-8"));
                                String lines = "";
                                StringBuilder sbAnnoLines = new StringBuilder();
                                while ((lines = returnedAnnoList.readLine()) != null){
    //                                    System.out.println(lineAnnoLs);
                                    sbAnnoLines.append(lines);
                                }
                                returnedAnnoList.close();
                                String parseThis = sbAnnoLines.toString();
                                JSONObject batchSaveResponse = JSONObject.fromObject(parseThis);

                                try{
                                    new_resources = (JSONArray) batchSaveResponse.get("new_resources");
                                }
                                catch(JSONException e){
                                   // System.out.println("Batch save response does not contain JSONARRAY in new_resouces.");
                                }

                            }
                            else{
                                //System.out.println("No annotation list for this canvas.  do not call batch save.  just save empty list.");
                            }
                                       
                            //Send the annotation resources in to be bulk saved.  The response will be the resources with updated @id fields as a BSONObject        
                            //System.out.println("bulk save in new annotations.  What proj has been assigned to this project: "+thisProject.getProjectID());
                            JSONObject canvasList = CreateAnnoListUtil.createEmptyAnnoList(thisProject.getProjectID(), canvasID, new_resources);
                            canvasList.element("copiedFrom", request.getParameter("projectID"));
                            URL postUrl = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/saveNewAnnotation.action");
                            HttpURLConnection uc = (HttpURLConnection) postUrl.openConnection();
                            uc.setDoInput(true);
                            uc.setDoOutput(true);
                            uc.setRequestMethod("POST");
                            uc.setUseCaches(false);
                            uc.setInstanceFollowRedirects(true);
                            uc.addRequestProperty("content-type", "application/x-www-form-urlencoded");
                            uc.connect();
                            DataOutputStream dataOut = new DataOutputStream(uc.getOutputStream());
                            dataOut.writeBytes("content=" + URLEncoder.encode(canvasList.toString(), "utf-8"));
                            dataOut.flush();
                            dataOut.close();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(),"utf-8")); 
                            reader.close();
                            uc.disconnect();
                            //System.out.println("Finished this canvas.");
                        }
                    }
                    //System.out.println("Copy proj and annos finished.  Whats the ID to return: "+thisProject.getProjectID());
                    String propVal = textdisplay.Folio.getRbTok("CREATE_PROJECT_RETURN_DOMAIN"); 
                    result = propVal + "/project/" + thisProject.getProjectID();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }else{
            response.setStatus(response.SC_FORBIDDEN);
            result = "Unauthorized or invalid project speficied.";
        }
        response.getWriter().print(result);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}

