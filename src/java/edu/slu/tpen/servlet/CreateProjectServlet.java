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
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import servlets.createManuscript;
import textdisplay.Folio;
import textdisplay.Project;
import user.Group;

/**
 * Create a manuscript, folio and project for New Berry. This part is a transformation of tpen function to web service. 
 * Servlet also adds annotation list to each canvas (also known as foliio in old tpen) using rerum.io. 
 * @author hanyan
 */
public class CreateProjectServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        writer.print(creatManuscriptFolioProject(request, response)); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        super.doGet(request, response); //To change body of generated methods, choose Tools | Templates.
        this.doPost(request, response);
    }

    /**
     * Create manuscript, folio and project using given json data.
     *
     * @param repository (optional)
     * @param archive (optional)
     * @param city (optional)
     * @param collection (optional)
     * @param title (optional)
     * @param urls
     * @param names
     */
    public String creatManuscriptFolioProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*if(null != request.getSession().getAttribute("UID")){
         UID = (Integer) request.getSession().getAttribute("UID");
         }*/
        try {
            //receive parameters.
            String repository = "newBerry";
            String archive = "";
            String city = "unknown";
            String collection = "unknown";

            city = request.getParameter("city");
            if (null == city) {
                city = "fromWebService";
            }
            textdisplay.Manuscript m = null;
//            System.out.println("msID ============= " + m.getID());
//            String urls = request.getParameter("urls");
//            String [] seperatedURLs = urls.split(";");
//            String names = request.getParameter("names");
//            String [] seperatedNames = names.split(",");

            String str_manifest = request.getParameter("scmanifest");
            List<Integer> ls_folios_keys = new ArrayList();
            if (null != str_manifest) {
                JSONObject jo = JSONObject.fromObject(str_manifest);
                archive = jo.getString("@id");
                //create a manuscript
                m = new textdisplay.Manuscript("newBerry", archive, city, city, -999);
                JSONArray sequences = (JSONArray) jo.get("sequences");
                List<String> ls_pageNames = new LinkedList();
                for (int i = 0; i < sequences.size(); i++) {
                    JSONObject inSequences = (JSONObject) sequences.get(i);
                    JSONArray canvases = inSequences.getJSONArray("canvases");
                    if (null != canvases && canvases.size() > 0) {
                        for (int j = 0; j < canvases.size(); j++) {
                            JSONObject canvas = canvases.getJSONObject(j);
                            ls_pageNames.add(canvas.getString("label"));
                            JSONArray images = canvas.getJSONArray("images");
                            if (null != images && images.size() > 0) {
                                for (int n = 0; n < images.size(); n++) {
                                    JSONObject image = images.getJSONObject(n);
                                    JSONObject resource = image.getJSONObject("resource");
                                    String imageName = resource.getString("@id");
                                    int folioKey = textdisplay.Folio.createFolioRecordFromNewBerry(city, canvas.getString("label"), imageName.replace('_', '&'), archive, m.getID(), 0);
                                    ls_folios_keys.add(folioKey);
                                }
                            }
                        }
                    }
                }

            } else {
                return "You need a manifest to create a project.";
            }

            //create a project
            int UID = 0;
            /*if(null != request.getSession().getAttribute("UID")){
             UID = (Integer) request.getSession().getAttribute("UID");
             }*/
            UID = ServletUtils.getUID(request, response);
            String tmpProjName = m.getShelfMark() + " project";
            if (request.getParameter("title") != null) {
                tmpProjName = request.getParameter("title");
            }
            try (Connection conn = ServletUtils.getDBConnection()) {
                conn.setAutoCommit(false);
                Group newgroup = new Group(conn, tmpProjName, UID);
                Project newProject = new Project(conn, tmpProjName, newgroup.getGroupID());
                Folio[] array_folios = new Folio[ls_folios_keys.size()];
                if (ls_folios_keys.size() > 0) {
                    for (int i = 0; i < ls_folios_keys.size(); i++) {
                        Folio folio = new Folio(ls_folios_keys.get(i));
                        array_folios[i] = folio;
                        //Parse folio.getImageURL() to retrieve paleography pid, and then generate new canvas id
                        String imageURL = folio.getImageURL();
                        // use regex to extract paleography pid
                        String canvasID = Constant.PALEO_CANVAS_ID_PREFIX + imageURL.replaceAll("^.*(paleography[^/]+).*$", "/$1");
                        //create anno list for original canvas
                        JSONObject annoList = CreateAnnoListUtil.createEmptyAnnoList(newProject.getProjectID(), canvasID, new JSONArray());
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
                        dataOut.writeBytes("content=" + URLEncoder.encode(annoList.toString(), "utf-8"));
                        dataOut.flush();
                        dataOut.close();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), "utf-8"));
//                      String line="";
//                      StringBuilder sb = new StringBuilder();
//                      System.out.println("=============================");  
//                      System.out.println("Contents of post request");  
//                      System.out.println("=============================");  
//                      while ((line = reader.readLine()) != null){  
//                      line = new String(line.getBytes(), "utf-8");  
//                           System.out.println(line);
//                           sb.append(line);
//                      }
//                      System.out.println("=============================");  
//                      System.out.println("Contents of post request ends");  
//                      System.out.println("=============================");  
                        reader.close();
                        uc.disconnect();
                    }
                }
                newProject.setFolios(conn, array_folios);
                newProject.addLogEntry(conn, "<span class='log_manuscript'></span>Added manuscript " + m.getShelfMark(), UID);
                int projectID = newProject.getProjectID();
                newProject.importData(UID);
                conn.commit();
                String propVal = Folio.getRbTok("CREATE_PROJECT_RETURN_DOMAIN");
                //return trimed project url
                return propVal + "/project/" + projectID;
            }
        } catch (SQLException ex) {
            Logger.getLogger(createManuscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "500";
    }

}
