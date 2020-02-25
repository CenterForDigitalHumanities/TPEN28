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

import static edu.slu.util.ServletUtils.getUID;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import textdisplay.Project;
import user.Group;
import user.User;

/**
 * Delete project and project related attachments from tpen. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class DeleteProjectServlet extends HttpServlet {
    private int projectID;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int projectNumToDelete = parseInt(request.getParameter("projectID"));
        int UID = 0;
        UID = getUID(request, response);
        if (UID != -1) {
            //UID = Integer.parseInt(request.getSession().getAttribute("UID").toString());
            try {
                Project todel = new Project(projectNumToDelete);
                Group projectGroup = new Group(todel.getGroupID());
                boolean isTPENAdmin = (new User(UID)).isAdmin();

                if (isTPENAdmin || projectGroup.isAdmin(UID)) {
                    todel.delete();
                }else{
                    response.setStatus(SC_UNAUTHORIZED);
                    PrintWriter out = response.getWriter();
                    out.print(SC_UNAUTHORIZED);
                }
            } catch (SQLException ex) {
                getLogger(DeleteProjectServlet.class.getName()).log(SEVERE, null, ex);
            }
        }else{
            //user doesn't log in
            PrintWriter out = response.getWriter();
            out.print("User doesn't log in.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp); 
    }
    
    

    /**
     * @return the projectID
     */
    public int getProjectID() {
        return projectID;
    }

    /**
     * @param projectID the projectID to set
     */
    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }
}
