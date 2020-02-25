/*
 * Copyright 2014- Saint Louis University. Licensed under the
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
package edu.slu.tpen.servlet;

import static edu.slu.tpen.servlet.Constant.ANNOTATION_SERVER_ADDR;
import static edu.slu.tpen.servlet.util.CreateAnnoListUtil.createEmptyAnnoList;
import static edu.slu.util.ServletUtils.getDBConnection;
import static edu.slu.util.ServletUtils.getUID;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.net.URLEncoder.encode;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.PartnerProject;
import textdisplay.Project;

/**
 * Copy project from a template project(or called standard project) which is created by NewBerry. 
 * Clear all transcription data from project and connect the new project 
 * to the template project on switch board. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class CopyProjectForPracticerServlet extends HttpServlet {
    
    @Override
    /**
     * @param projectID
     * @param uID
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String result = "";
        int uID = getUID(request, response);
        if(null != request.getParameter("projectID") && uID != -1){
            Integer projectID = parseInt(request.getParameter("projectID"));

            try {
                //find original project and copy to a new project. 
                Project templateProject = new Project(projectID);
                Connection conn = getDBConnection();
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
                    if(null != folios && folios.length > 0)
                    {
                        for (Folio folio : folios) {
                            Integer msID = folio.getMSID();
                            String msID_str = msID.toString();
                            //This needs to be the same one the JSON Exporter creates and needs to be unique and unchangeable.
                            String canvasID_check = folio.getCanvas();
                            String canvasID = "";
                            String str_folioNum = getRbTok("SERVERURL") + "canvas/" + folio.getFolioNumber();
                            if(canvasID_check == ""){
                                canvasID = str_folioNum;
                            }
                            else{
                                canvasID = canvasID_check;
                            }
                            //String canvasID = Folio.getRbTok("SERVERURL") + templateProject.getProjectName() + "/canvas/" + URLEncoder.encode(folio.getPageName(), "UTF-8"); // for slu testing
                            //create canvas list for original canvas
                            JSONObject annoList = createEmptyAnnoList(thisProject.getProjectID(), canvasID, new JSONArray());
                            URL postUrl = new URL(ANNOTATION_SERVER_ADDR + "/anno/getAnnotationByProperties.action");
                            HttpURLConnection uc = (HttpURLConnection) postUrl.openConnection();
                            uc.setDoInput(true);
                            uc.setDoOutput(true);
                            uc.setRequestMethod("POST");
                            uc.setUseCaches(false);
                            uc.setInstanceFollowRedirects(true);
                            uc.addRequestProperty("content-type", "application/x-www-form-urlencoded");
                            uc.connect();
                            DataOutputStream dataOut = new DataOutputStream(uc.getOutputStream());
                            dataOut.writeBytes("content=" + encode(annoList.toString(), "utf-8"));
                            dataOut.flush();
                            dataOut.close();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(),"utf-8"));
                            //                                String line="";
//                                StringBuilder sb = new StringBuilder();
//                                System.out.println("=============================");
//                                System.out.println("Contents of post request");
//                                System.out.println("=============================");
//                                while ((line = reader.readLine()) != null){
//                                    //line = new String(line.getBytes(), "utf-8");  
//                                    System.out.println(line);
//                                    sb.append(line);
//                                }
//                                System.out.println("=============================");
//                                System.out.println("Contents of post request ends");
//                                System.out.println("=============================");
reader.close();
uc.disconnect();
                        }
                    }
                    //String propVal = textdisplay.Folio.getRbTok("CREATE_PROJECT_RETURN_DOMAIN"); 
                    result = "/project/" + thisProject.getProjectID();
                }
            } catch(Exception e){
            }
        }else{
            result = "" + SC_FORBIDDEN;
        }
        response.getWriter().print(result);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
