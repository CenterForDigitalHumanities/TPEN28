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
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import javax.servlet.http.HttpSession;
import org.owasp.encoder.Encode;
import textdisplay.Project;
import user.Group;
import user.User;

/**
 * Add user to project and add this user to project group. This is a transformation of tpen function to web service.  
 * It's using tpen MySQL database. 
 * @author hanyan
 */
public class AddUserToProjectServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("UID") != null) {
            int UID = parseInt(session.getAttribute("UID").toString());
            try {
                User thisUser = new user.User(UID);
                if(null != request.getParameter("uname") && null != request.getParameter("projectID")){
                    String uname = Encode.forHtml(request.getParameter("uname"));
                    String fname = Encode.forHtml(request.getParameter("fname"));
                    String lname = Encode.forHtml(request.getParameter("lname"));
                    Project thisProject = new Project(parseInt(request.getParameter("projectID")));
                    int result = thisUser.invite(uname, fname, lname);
                    if (result == 0) {
                        //successfully send out email to user
                        Group g = new Group(thisProject.getGroupID());
                        if (g.isAdmin(thisUser.getUID())) {
                            User newUser = new User(uname);
                            g.addMember(newUser.getUID());
                            response.getWriter().print(newUser.getUID());
                        }else{
                            //if user is not admin, return unauthorized. 
                            response.getWriter().print(SC_UNAUTHORIZED);
                        }
                    }else if (result == 2) {
                        //account created but email issue occured, usually happens in dev environments with no email server.
                        user.Group g = new user.Group(thisProject.getGroupID());
                        if (g.isAdmin(thisUser.getUID())) {
                            User newUser = new User(uname);
                            g.addMember(newUser.getUID());
                            response.getWriter().print(newUser.getUID());
                        }else{
                            //if user is not admin, return unauthorized. 
                            response.getWriter().print(SC_UNAUTHORIZED);
                        }
                    }else if(result == 1){
                        //user exits
                        user.Group g = new user.Group(thisProject.getGroupID());
                        if (g.isAdmin(thisUser.getUID())) {
                            User newUser = new User(uname);
                            g.addMember(newUser.getUID());
                            response.getWriter().print(newUser.getUID());
                        }else{
                            //if user is not admin, return unauthorized. 
                            out.println("user not admin error");
                            response.getWriter().print(SC_UNAUTHORIZED);
                        }
                    }else{
                        //user doesn't exist
                        out.println("user doesnt exist error");
                        response.getWriter().print(SC_NOT_ACCEPTABLE);
                    }
                }else{
                    //if there is no uname
                    out.println("There is no uname error.");
                    response.getWriter().print(SC_NOT_ACCEPTABLE);
                }
            } catch (SQLException ex) {
                out.println("SQL exception");
                getLogger(AddUserToProjectServlet.class.getName()).log(SEVERE, null, ex);
            }
        }else{
            //if user doesn't log in, return unauthorized. 
            out.println("No user logged in.");
            response.getWriter().print(SC_UNAUTHORIZED);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
    
}
