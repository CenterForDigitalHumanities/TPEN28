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
import javax.servlet.http.HttpSession;
import textdisplay.Project;
import user.Group;
import user.User;

/**
 * Delete user from project. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class DelUserFromProjectServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("UID") != null) {
            try {
                int UID = Integer.parseInt(session.getAttribute("UID").toString());
                User thisUser = new user.User(UID);
                if(null != request.getParameter("uname") && null != request.getParameter("projectID")){
                    User newUser = new User(request.getParameter("uname"));
                    if(null != newUser){
                        Project thisProject = new Project(Integer.parseInt(request.getParameter("projectID")));
                        Group g = new Group(thisProject.getGroupID());
                        if (g.isAdmin(thisUser.getUID())) {
                            g.remove(newUser.getUID());
                        }else{
                            //if user is not admin, return unauthorized. 
                            response.getWriter().print(response.SC_UNAUTHORIZED);
                        }
                    }else{
                        //if there is no uname
                        response.getWriter().print(response.SC_NOT_ACCEPTABLE);
                    }
                }else{
                    //if there is no uname
                    response.getWriter().print(response.SC_NOT_ACCEPTABLE);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DelUserFromProjectServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            //if user doesn't log in, return unauthorized. 
            response.getWriter().print(response.SC_UNAUTHORIZED);
        }          
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }
    
}
