///*
// * Copyright 2014- Saint Louis University. Licensed under the
// *	Educational Community License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License. You may
// * obtain a copy of the License at
// *
// * http://www.osedu.org/licenses/ECL-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an "AS IS"
// * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// * or implied. See the License for the specific language governing
// * permissions and limitations under the License.
// */
//package edu.slu.tpen.servlet;
//
//import edu.slu.tpen.servlet.util.CreateAnnoListUtil;
//import edu.slu.util.ServletUtils;
//
//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.sql.Connection;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
//import textdisplay.Folio;
//import textdisplay.PartnerProject;
//import textdisplay.Project;
//
///**
// * Copy project from a template project(or called standard project) which is created by NewBerry. 
// * Clear all transcription data from project and connect the new project to the template project on switch board. 
// * Servlet will first go deep into annotation list and copy each annotation first, then goes out to update annotation list with newly copied annotation info then update annotation list. 
// * It keeps annotation list name but change project id to newly created project's. 
// * Please follow the comments to go through the process. If you want to know how it works step by step, please uncomment "System out". 
// * This function is not from tpen. It's a new function required by NewBerry. 
// * @author hanyan
// * 
// * THIS IS NOW DEPRICATED AND OVERWRITTEN BY COPYPROJECTANDANNOTATIONS.JACA
// */
////public class CopyProjWithAnnoServlet extends HttpServlet {
////    
////    @Override
////    /**
////     * @param projectID
////     * @param uID
////     */
////    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
////    	String result = "";
////    	int uID = ServletUtils.getUID(request, response);
////        if(null != request.getParameter("projectID") && uID != -1){
////            Integer projectID = Integer.parseInt(request.getParameter("projectID"));
////            
////            try {
////                //find original project and copy to a new project. 
////                System.out.println("begin copy project");
////                Project templateProject = new Project(projectID);
////                Connection conn = ServletUtils.getDBConnection();
////                conn.setAutoCommit(false);
////                //in this method, it copies everything about the project.
////                if(null != templateProject.getProjectName())
////                {
////                    System.out.println("new project from template projects...");
////                    Project thisProject = new Project(templateProject.copyProjectWithoutTranscription(conn, uID));
////                    System.out.println("done 1");
////                    //set partener project. It is to make a connection on switch board. 
////                    thisProject.setAssociatedPartnerProject(projectID);
////                    PartnerProject theTemplate = new PartnerProject(projectID);
////                    thisProject.copyButtonsFromProject(conn, theTemplate.getTemplateProject());
////                    thisProject.copyHotkeysFromProject(conn, theTemplate.getTemplateProject());
////                    conn.commit();
////                    Folio[] folios = thisProject.getFolios();
////                    if(null != folios && folios.length > 0)
////                    {
////                        System.out.println("start copying folios");
////                        for(int i = 0; i < folios.length; i++)
////                        {
////                            System.out.println("folio "+i);
////                            Folio folio = folios[i];
////                            //get annotation list for each canvas
////                            JSONObject annoLsQuery = new JSONObject();
////                            annoLsQuery.element("@type", "sc:AnnotationList");
////                            //For newberry, we cannot do this since its possible we want the master project, which does not have this field.
////                            //annoLsQuery.element("proj", templateProject.getProjectID());
////                            //Parse folio.getImageURL() to retrieve paleography pid, and then generate new canvas id
////                            String imageURL = folio.getImageURL();
////                            // use regex to extract paleography pid
////                            String canvasID = Constant.PALEO_CANVAS_ID_PREFIX + imageURL.replaceAll("^.*(paleography[^/]+).*$", "/$1");
////                            //Folio.getRbTok("SERVERURL") + templateProject.getProjectName() + "/canvas/" + URLEncoder.encode(folio.getPageName(), "UTF-8")
////                            annoLsQuery.element("on", canvasID);
////                           
////                            //System.out.println(annoLsQuery.toString());
////                            System.out.println("get annolist for folio "+i);
////                            URL postUrlannoLs = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/getAnnotationByProperties.action");
////                            HttpURLConnection ucAnnoLs = (HttpURLConnection) postUrlannoLs.openConnection();
////                            ucAnnoLs.setDoInput(true);
////                            ucAnnoLs.setDoOutput(true);
////                            ucAnnoLs.setRequestMethod("POST");
////                            ucAnnoLs.setUseCaches(false);
////                            ucAnnoLs.setInstanceFollowRedirects(true);
////                            ucAnnoLs.addRequestProperty("content-type", "application/x-www-form-urlencoded");
////                            ucAnnoLs.connect();
////                            DataOutputStream dataOutAnnoLs = new DataOutputStream(ucAnnoLs.getOutputStream());
////                            dataOutAnnoLs.writeBytes("content=" + URLEncoder.encode(annoLsQuery.toString(), "utf-8"));
////                            dataOutAnnoLs.flush();
////                            dataOutAnnoLs.close();
////                            BufferedReader readerAnnoLs = new BufferedReader(new InputStreamReader(ucAnnoLs.getInputStream(),"utf-8"));
////                            String lineAnnoLs = "";
////                            StringBuilder sbAnnoLs = new StringBuilder();
//////                                System.out.println("=============================");  
//////                                System.out.println("Contents of annotation list starts");  
//////                                System.out.println("=============================");  
////                            while ((lineAnnoLs = readerAnnoLs.readLine()) != null){
//////                                    System.out.println(lineAnnoLs);
////                                sbAnnoLs.append(lineAnnoLs);
////                            }
//////                                System.out.println("=============================");  
//////                                System.out.println("Contents of annotation list ends");  
//////                                System.out.println("=============================");
////                            readerAnnoLs.close();
////                            ucAnnoLs.disconnect();
////                            //transfer annotation list string to annotation list JSON Array. 
////                            JSONArray ja_allAnnoLists = JSONArray.fromObject(sbAnnoLs.toString()); //This is the list of all AnnotatationLists attached to this folio.
////                            JSONObject jo_annotationList = new JSONObject();
////                            JSONObject jo_masterList = new JSONObject();
////                            if(ja_allAnnoLists.size() > 0){
////                                System.out.println("go through all anno lists for folio "+i);
////                                //find the annotations list whose proj matches or use the master ([0])
////                                for(int x =0; x<ja_allAnnoLists.size(); x++){
////                                    JSONObject current_list = ja_allAnnoLists.getJSONObject(x);
////                                    System.out.println("on anno "+x+" for folio "+i);
//////                                    System.out.println("WHICH LIST ARE WE ON?");
//////                                    System.out.println(current_list.getString("@id"));
////                                    if(null!=current_list.get("proj")){ //make sure this list has proj field
////                                        String current_proj = current_list.getString("proj");
////                                        if(current_proj == "master"){
////                                            jo_masterList = current_list;
////                                            System.out.println("found master anno list for folio "+i);
////                                        }
////                                        if(current_proj.matches("^\\d+$") && Integer.parseInt(current_proj) == templateProject.getProjectID()){ //if its id equal to the id of the project we are copying
////                                            jo_annotationList = current_list; //if so, thats the list we want
////                                            System.out.println("found project anno list for folio "+i);
////                                            break;
////                                        }
////                                        else{ //it was not a match, are we done looking at all lists?
////                                            if(x == (ja_allAnnoLists.size()-1)){ //if none of them match, we want the first to be our list to copy (master list)
//////                                                System.out.println("USE MASTER!!!!!!!!!!!!!!!!!!!!");
////                                                jo_annotationList = jo_masterList; //assuming the first object is the master.  if not, we will have to do something
////                                                System.out.println("default to master anno list for folio "+i);
//////                                                System.out.println(jo_annotationList.getString("@id"));
////                                                break;
////                                            }
////                                        }
////                                    }
////                                    else{ //it was null, are we done looking at all lists?
////                                        if(x == (ja_allAnnoLists.size()-1)){ //if none of them match, we want the first to be our list to copy (master list)
////  //                                          System.out.println("USE MASTER!!!!!!!!!!!!!!!!!!!!");
////                                            jo_annotationList = jo_masterList; //assuming the first object is the master.  if not, we will have to do something
////                                            System.out.println("default to master anno list for folio "+i);
//// //                                           System.out.println(jo_annotationList.getString("@id"));
////                                            break;
////                                        }
////                                    }
////                                }
////                            }
////                            //JSONArray ja_annotationList = JSONArray.fromObject(sbAnnoLs.toString());
////                            //figure out if we are using master (elem [0]) or if we can match projID
////                            //loop through annotation list and make a copy of each list memeber. 
////                            //if(ja_annotationList.size() > 0)
////                            //{
////                               // for(int m = 0; m < ja_annotationList.size(); m++)
////                                //{
//////                                    System.out.println("What is the id of the annotationList chosen?");
//////                                    System.out.println(jo_annotationList.getString("@id"));
////                                    JSONArray resources = jo_annotationList.getJSONArray("resources");
////                                    //Get the annotations out of the AnnotaionList resources and go through them.  We need to make new annotaions for each of  them for the new list.
////                                    System.out.println("create copies of each anno in anno list for folio "+i);
////                                    for(int n = 0; n < resources.size(); n++)
////                                    {
////                                        System.out.println("create copies of anno "+n+" in anno list for folio "+i);
////                                        JSONObject resource = resources.getJSONObject(n);
////                                        //print json element of annotation list starts
//////                                        System.out.println(annoInList.keySet().size());
//////                                        System.out.println("content of template anno in anno list starts: ");
//////                                        System.out.println("content of template anno in anno list starts: ");
////                                        //print json element of annotation list ends
////                                        //get each annotation in annotation list by its @id
////                                        JSONObject annoQuery = new JSONObject();
//////                                            System.out.println("@id ==== " + resource.get("@id"));
////                                        annoQuery.element("@id", resource.get("@id"));
////                                        URL postGetAnnoByAID = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/getAnnotationByProperties.action");
////                                        HttpURLConnection ucGetAnnoByAID = (HttpURLConnection) postGetAnnoByAID.openConnection();
////                                        ucGetAnnoByAID.setDoInput(true);
////                                        ucGetAnnoByAID.setDoOutput(true);
////                                        ucGetAnnoByAID.setRequestMethod("POST");
////                                        ucGetAnnoByAID.setUseCaches(false);
////                                        ucGetAnnoByAID.setInstanceFollowRedirects(true);
////                                        ucGetAnnoByAID.addRequestProperty("content-type", "application/x-www-form-urlencoded");
////                                        ucGetAnnoByAID.connect();
////                                        DataOutputStream dataOutGetAnnoByAID = new DataOutputStream(ucGetAnnoByAID.getOutputStream());
////                                        dataOutGetAnnoByAID.writeBytes("content=" + URLEncoder.encode(annoQuery.toString(), "utf-8"));
////                                        dataOutGetAnnoByAID.flush();
////                                        dataOutGetAnnoByAID.close();
////                                        BufferedReader readerGetAnnoByAID = new BufferedReader(new InputStreamReader(ucGetAnnoByAID.getInputStream(), "utf-8"));
////                                        StringBuilder sbGetAnnoByAID = new StringBuilder();
////                                        String lineGetAnnoByAID = "";
//////                                        System.out.println("content of template anno starts: ");
////                                        while((lineGetAnnoByAID = readerGetAnnoByAID.readLine()) != null){
//////                                            System.out.println(lineGetAnnoByAID);
////                                            sbGetAnnoByAID.append(lineGetAnnoByAID);
////                                        }
//////                                        System.out.println("content of template anno ends: ");
////                                        readerGetAnnoByAID.close();
////                                        ucGetAnnoByAID.disconnect();
//////                                        System.out.println("first char of json object ===" + sbGetAnnoByAID.toString().charAt(0) + "===");
////                                        JSONObject anno = JSONArray.fromObject(sbGetAnnoByAID.toString()).getJSONObject(0);
////                                        anno.remove("_id");
////                                        anno.remove("@id");
////                                        //copy annotation
////                                        URL postUrlCopyAnno = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/saveNewAnnotation.action");
////                                        HttpURLConnection ucCopyAnno = (HttpURLConnection) postUrlCopyAnno.openConnection();
////                                        ucCopyAnno.setDoInput(true);
////                                        ucCopyAnno.setDoOutput(true);
////                                        ucCopyAnno.setRequestMethod("POST");
////                                        ucCopyAnno.setUseCaches(false);
////                                        ucCopyAnno.setInstanceFollowRedirects(true);
////                                        ucCopyAnno.addRequestProperty("content-type", "application/x-www-form-urlencoded");
////                                        ucCopyAnno.connect();
////                                        DataOutputStream dataOutCopyAnno = new DataOutputStream(ucCopyAnno.getOutputStream());
////                                        dataOutCopyAnno.writeBytes("content=" + URLEncoder.encode(anno.toString(), "utf-8"));
////                                        dataOutCopyAnno.flush();
////                                        dataOutCopyAnno.close();
////                                        BufferedReader readerCopyAnno = new BufferedReader(new InputStreamReader(ucCopyAnno.getInputStream(), "utf-8"));
////                                        StringBuilder sbCopyAnno = new StringBuilder();
////                                        String lineCopyAnno = "";
//////                                        System.out.println("content of copied anno starts: ");
////                                        while((lineCopyAnno = readerCopyAnno.readLine()) != null){
//////                                            System.out.println(lineCopyAnno);
////                                            sbCopyAnno.append(lineCopyAnno);
////                                        }
//////                                        System.out.println("content of copied anno endss: ");
////                                        readerCopyAnno.close();
////                                        ucCopyAnno.disconnect();
////                                        JSONObject copyAnnoReturnVal = JSONObject.fromObject(sbCopyAnno.toString());
////                                        String copyAnnoNewAID = copyAnnoReturnVal.getString("@id");
////                                        //result++;
////                                        resource.remove("@id");
////                                        resource.element("@id", copyAnnoNewAID);
////                                    }
////                                    //copy canvas list from original canvas(also known as folio in old tpen) list with newly copied annotation info.
////                                    System.out.println("store new anno list information folio "+i);
////                                    JSONObject canvasList = CreateAnnoListUtil.createEmptyAnnoList(thisProject.getProjectID(), canvasID, resources);
////                                    URL postUrl = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/saveNewAnnotation.action");
////                                    HttpURLConnection uc = (HttpURLConnection) postUrl.openConnection();
////                                    uc.setDoInput(true);
////                                    uc.setDoOutput(true);
////                                    uc.setRequestMethod("POST");
////                                    uc.setUseCaches(false);
////                                    uc.setInstanceFollowRedirects(true);
////                                    uc.addRequestProperty("content-type", "application/x-www-form-urlencoded");
////                                    uc.connect();
////                                    DataOutputStream dataOut = new DataOutputStream(uc.getOutputStream());
////                                    dataOut.writeBytes("content=" + URLEncoder.encode(canvasList.toString(), "utf-8"));
////                                    dataOut.flush();
////                                    dataOut.close();
////                                    BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(),"utf-8"));
////    //                                String line="";
////    //                                StringBuilder sb = new StringBuilder();
////    //                                System.out.println("=============================");  
////    //                                System.out.println("Contents of post request");  
////    //                                System.out.println("=============================");  
////    //                                while ((line = reader.readLine()) != null){  
////    //                                    //line = new String(line.getBytes(), "utf-8");  
////    //                                    System.out.println(line);
////    //                                    sb.append(line);
////    //                                }
////    //                                System.out.println("=============================");  
////    //                                System.out.println("Contents of post request ends");  
////    //                                System.out.println("=============================");  
////                                    reader.close();
////                                    uc.disconnect();
////                               // }
////                            //}
////                                    System.out.println("done with folio "+i);
////                        }
////                    }
////                    System.out.println("return project ID for copied project");
////                    String propVal = textdisplay.Folio.getRbTok("CREATE_PROJECT_RETURN_DOMAIN"); 
////                    result = propVal + "/project/" + thisProject.getProjectID();
////                }
////            } catch(Exception e){
////                e.printStackTrace();
////            }
////        }else{
////            response.setStatus(response.SC_FORBIDDEN);
////        	result = "Unauthorized or invalid project speficied.";
////        }
////        response.getWriter().print(result);
////    }
////    
////    @Override
////    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
////        doPost(request, response);
////    }
//}
