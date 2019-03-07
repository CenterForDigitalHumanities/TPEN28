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
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
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
import textdisplay.Manuscript;
import textdisplay.Metadata;
import textdisplay.Project;
import user.Group;
/**
 *
 * @author bhaberbe
 */
public class CreateProjectFromMSIDServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        writer.print(creatManuscriptFolioProject(request, response)); //To change body of generated methods, choose Tools | Templates.
        writer.close();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        super.doGet(request, response); //To change body of generated methods, choose Tools | Templates.
        this.doPost(request, response);
    }

    /**
     * Create project from given MSID.  Return projectID if a project already exists for this user with this MSID.
     * @param msid
     */
    public String creatManuscriptFolioProject(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            //receive parameters.
            //create a project
            int UID = 0;
            /*if(null != request.getSession().getAttribute("UID")){
             UID = (Integer) request.getSession().getAttribute("UID");
             }*/
            UID = ServletUtils.getUID(request, response);
            String repository = "T-PEN 2.8";
            String archive = "unknown";
            String city = "unknown";
            String collection = "unknown";
            String label = "";

            Manuscript man = null;
            String msID_str = request.getParameter("msID");
            if(msID_str == null){
                return "500: No MSID";
            }
            Integer msID = Integer.parseInt(msID_str);
            man = new Manuscript(msID, true);
            Integer existing_projID = man.checkExistingProjects(msID, UID);
            if(existing_projID>1) { //-1 if no project existed for this user with this MSID.  
                return "existing project/" + existing_projID.toString();
            }
            Folio[] array_folios = null;
            
            city = man.getCity();
            collection = man.getCollection();
            repository = man.getRepository();
            //create a manuscript
            List<String> ls_pageNames = new LinkedList();
            array_folios = man.getFolios();
            String tmpProjName = man.getShelfMark() + " project";
            if (request.getParameter("title") != null) {
                tmpProjName = request.getParameter("title");
            }
            try (Connection conn = ServletUtils.getDBConnection()) {
                conn.setAutoCommit(false);
                Group newgroup = new Group(conn, tmpProjName, UID);
                Project newProject = new Project(conn, tmpProjName, newgroup.getGroupID());
                man.setArchive(Folio.getRbTok("SERVERURL")+"/project/"+newProject.getProjectID());
                if (array_folios.length > 0) {
                    for(int i = 0; i < array_folios.length; i++) {
                        Folio folio = array_folios[i];
                        //This needs to be the same one the JSON Exporter creates and needs to be unique and unchangeable.
                        String canvasID_check = folio.getCanvas();
                        String canvasID = "";
                        String str_folioNum = Folio.getRbTok("SERVERURL")+"canvas/"+folio.getFolioNumber();
                        if("".equals(canvasID_check)){
                            canvasID = str_folioNum;
                        }
                        else{
                            canvasID = canvasID_check;
                        }
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
                newProject.addLogEntry(conn, "<span class='log_manuscript'></span>Added manuscript " + man.getShelfMark(), UID);
                int projectID_return = newProject.getProjectID();
                newProject.importData(UID);
                Metadata metadata = new Metadata(projectID_return);
                label = tmpProjName;
                //Set default metadata values for this project.
                metadata.setTitle(label);
                metadata.setMsRepository(repository);
                metadata.setMsCollection(collection);
                metadata.setMsIdNumber(msID_str);
                conn.commit();
                //String propVal = Folio.getRbTok("CREATE_PROJECT_RETURN_DOMAIN");
                return "project/" + projectID_return; //TODO: Make this the resolvable project url?
            }
        } catch (SQLException ex) {
            Logger.getLogger(createManuscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "500";
    }

}
