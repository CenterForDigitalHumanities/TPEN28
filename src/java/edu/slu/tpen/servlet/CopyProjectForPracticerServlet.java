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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.Folio;
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
        int uID = ServletUtils.getUID(request, response);
        if(null != request.getParameter("projectID") && uID != -1){
            Integer projectID = Integer.parseInt(request.getParameter("projectID"));

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
                    if(null != folios && folios.length > 0)
                    {
                        for(int i = 0; i < folios.length; i++)
                        {
                            Folio folio = folios[i];
                            //Parse folio.getImageURL() to retrieve paleography pid, and then generate new canvas id
                            String imageURL = folio.getImageURL();
                            // use regex to extract paleography pid
                            String canvasID = Constant.PALEO_CANVAS_ID_PREFIX + imageURL.replaceAll("^.*(paleography[^/]+).*$", "/$1");
                            //create canvas list for original canvas
                            JSONObject annoList = CreateAnnoListUtil.createEmptyAnnoList(thisProject.getProjectID(), canvasID, new JSONArray());
                            URL postUrl = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/getAnnotationByProperties.action");
                            HttpURLConnection uc = (HttpURLConnection) postUrl.openConnection();
                            uc.setDoInput(true);
                            uc.setDoOutput(true);
                            uc.setRequestMethod("POST");
                            uc.setUseCaches(false);
                            uc.setInstanceFollowRedirects(true);
                            uc.addRequestProperty("content-type", "application/x-www-form-urlencoded");
                            uc.connect();
                            DataOutputStream dataOut = new DataOutputStream(uc.getOutputStream());
                            dataOut.writeBytes("content=" + URLEncoder.encode(annoList.toString(), "utf-8"));
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
                    String propVal = textdisplay.Folio.getRbTok("CREATE_PROJECT_RETURN_DOMAIN"); 
                    result = propVal + "/project/" + thisProject.getProjectID();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }else{
            result = "" + response.SC_FORBIDDEN;
        }
        response.getWriter().print(result);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
