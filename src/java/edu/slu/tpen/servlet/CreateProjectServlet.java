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

import edu.slu.util.ServletUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import servlets.createManuscript;
import textdisplay.Project;
import user.Group;

/**
 * Create a manuscript, folio and project for New Berry. 
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
        super.doGet(request, response); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Create manuscript, folio and project using given json data. 
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
        try {
            //receive parameters.
            String repository="unknown";
            String archive="New Berry";
            String city="unknown";
            String collection="unknown";

            city = request.getParameter("city");
            //create a manuscript
            textdisplay.Manuscript m=new textdisplay.Manuscript(repository, archive, city, city, -999);
//            System.out.println("msID ============= " + m.getID());
            String urls = request.getParameter("urls");
            String [] seperatedURLs = urls.split(";");
            String names = request.getParameter("names");
            String [] seperatedNames = names.split(",");
            for(int i=0;i<seperatedURLs.length;i++)
            {
                int num = textdisplay.Folio.createFolioRecordFromNewBerry(city, seperatedNames[i], seperatedURLs[i].replace('_', '&'), archive, m.getID(), 0);
            }
            //create a project
            int UID = 0;
            String tmpProjName = m.getShelfMark()+" project";
            if (request.getParameter("title") != null) {
                tmpProjName = request.getParameter("title");
            }
            try (Connection conn = ServletUtils.getDBConnection()) {
                conn.setAutoCommit(false);
                Group newgroup = new Group(conn, tmpProjName, UID);
                Project newProject = new Project(conn, tmpProjName, newgroup.getGroupID());
                newProject.setFolios(conn, m.getFolios());
                newProject.addLogEntry(conn, "<span class='log_manuscript'></span>Added manuscript " + m.getShelfMark(), UID);
                int projectID = newProject.getProjectID();
                newProject.importData(UID);
                conn.commit();
                return "http://t-pen.org/project/" + projectID;
            }
        } catch (SQLException ex) {
            Logger.getLogger(createManuscript.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "500";
    }
    
}
