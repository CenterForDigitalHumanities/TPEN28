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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Project;

/**
 * Copy project from a template project(or called standard project) which is created by NewBerry. 
 * Clear all transcription data from project and connect the new project to the template project on switch board. 
 * Servlet will first go deep into annotation list and copy each annotation first, then goes out to update annotation list with newly copied annotation info then update annotation list. 
 * It keeps annotation list name but change project id to newly created project's. 
 * Please follow the comments to go through the process. If you want to know how it works step by step, please uncomment "System out". 
 * This function is not from tpen. It's a new function required by NewBerry. 
 * @author hanyan
 * 
 * THIS IS NOW DEPRICATED AND OVERWRITTEN BY COPYPROJECTANDANNOTATIONS.JACA
 */
public class CopyProjAndAnnosClassic extends HttpServlet {
    
    @Override
    /**
     * @param projectID
     * @param uID
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String result = "";
        int uID = ServletUtils.getUID(request, response);
        int projectID = Integer.parseInt(request.getParameter("projectID"));
        if(null != request.getParameter("projectID") && uID != -1){
            try {
                Connection conn = ServletUtils.getDBConnection();
                Project thisProject = new Project(projectID);
                conn.setAutoCommit(false);
                thisProject.copyProject(conn, uID);
                conn.commit();
            } catch (SQLException ex) {
                Logger.getLogger(CopyProjAndAnnosClassic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(CopyProjAndAnnosClassic.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
