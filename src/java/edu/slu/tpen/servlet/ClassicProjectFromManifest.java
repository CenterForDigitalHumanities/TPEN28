/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import edu.slu.util.ServletUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import textdisplay.Project;
import user.Group;
import user.User;

/**
 *
 * @author bhaberbe
 */
public class ClassicProjectFromManifest extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        try {
            writer.print(createProject(request, response));
        } catch (SQLException ex) {
            Logger.getLogger(ClassicProjectFromManifest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //response.setHeader("Access-Control-Allow-Origin", "*");
        this.doPost(request, response);
        
    }
    
    
       public String createProject(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException, SQLException {
            int UID = Integer.parseInt(request.getSession().getAttribute("UID").toString());
            int projectID = 0;
            textdisplay.Project thisProject = null;
            if(request.getParameter("manifest")==null){
                return "You must provide a manifest ID via ?manifest={ID}";
            }
            if (UID > 0 && request.getParameter("manifest")!=null) {
               JSONObject theManifest = resolveID(request.getParameter("manifest")); 
               String repository = "unknown";
                String archive = "unknown";
                String city = "unknown";
                String collection = "unknown";
                String label = "unknown"; 
                List<Integer> ls_folios_keys = new ArrayList();
                if(theManifest.has("@id")){
                    archive = theManifest.getString("@id");
                }
                else{
                    return "500: Malformed Manifest";
                }
                if(theManifest.has("label")){
                    label = theManifest.getString("label");
                }
                else{
                    label = "New T-PEN Manifest";
                }
                System.out.println("Make new manuscript");
                textdisplay.Manuscript mss=new textdisplay.Manuscript("TPEN 2.8", archive, city, city, -999);
               // int [] msIDs=new int[0];
                User u = new User(UID);
                JSONArray sequences = (JSONArray) theManifest.get("sequences");
                List<String> ls_pageNames = new LinkedList();
                System.out.println("Go over sequences");
                for (int i = 0; i < sequences.size(); i++) {
                    JSONObject inSequences = (JSONObject) sequences.get(i);
                    JSONArray canvases = inSequences.getJSONArray("canvases");
                    System.out.println("Go over canvases");
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
                                    int folioKey = textdisplay.Folio.createFolioRecordFromManifest(city, canvas.getString("label"), imageName.replace('_', '&'), archive, mss.getID(), 0);
                                    ls_folios_keys.add(folioKey);
                                }
                            }
                        }
                    }
                }
                //textdisplay.Project[] p = u.getUserProjects();
                //msIDs = new int[p.length];
                // firstPage() fails ALOT
                // Cambridge, Cologny
//                for (int i = 0; i < p.length; i++) {
//                    try {
//                        msIDs[i] = new textdisplay.Manuscript(p[i].firstPage()).getID();
//                    } catch (Exception e) {
//                        msIDs[i] = -1;
//                    }
//                }
//                for (int l = 0; l < msIDs.length; l++) {
//                    if (msIDs[l] == mss.getID()) {
//                        projectID=p[l].getProjectID();
//                        thisProject=p[l];
//                    }
//                }
                System.out.println("Create project");
                if(projectID<1 && theManifest.size() > 0) {
                    //create a project for them
                    String tmpProjName = mss.getShelfMark()+" project";
                    if (request.getParameter("title") != null) {
                        tmpProjName = request.getParameter("title");
                    }
                    try (Connection conn = ServletUtils.getDBConnection()) {
                       conn.setAutoCommit(false);
                       Group newgroup = new Group(conn, tmpProjName, UID);
                       Project newProject = new Project(conn, tmpProjName, newgroup.getGroupID());
                       newProject.setFolios(conn, mss.getFolios());
                       newProject.addLogEntry(conn, "<span class='log_manuscript'></span>Added manuscript " + mss.getShelfMark(), UID);
                       thisProject=newProject;
                       projectID=thisProject.getProjectID();
                       newProject.importData(UID);
                       conn.commit();
                    }
                }
                else{
                    return "The manifest provided was empty";
                }
            }
            else{
                return "Either you aren't logged in or you didn't provide a manifest";
            }
            return ""+projectID;
        }

        public JSONObject resolveID(String manifestID) throws MalformedURLException, IOException{
            System.out.println("Resolving ID "+manifestID);
            JSONObject manifest = new JSONObject();
            URL id = new URL(manifestID);
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            HttpURLConnection connection = (HttpURLConnection) id.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(15*1000);
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
              stringBuilder.append(line + "\n");
            }
            manifest = JSONObject.fromObject(stringBuilder.toString());
            System.out.println("resolved");
            System.out.println(manifest);
            return manifest;
        }
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
