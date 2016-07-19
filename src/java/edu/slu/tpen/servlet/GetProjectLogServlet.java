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

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Project;

/**
 * Get project log by project id. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class GetProjectLogServlet extends HttpServlet {

    /**
     * Get project log by project id. 
     * @param projectID
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(null != request.getParameter("projectID")){
            try {
                String projectID = request.getParameter("projectID");
                Project p = new Project(Integer.parseInt(projectID));
                if(null != request.getSession().getAttribute("uid")){
                    response.getWriter().print(p.getProjectLog());
                }else{
                    response.sendError(response.SC_UNAUTHORIZED);
                }
            } catch (SQLException ex) {
                Logger.getLogger(GetProjectLogServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            response.sendError(response.SC_NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(req, response);
    }
    
}
