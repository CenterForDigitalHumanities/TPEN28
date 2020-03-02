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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import javax.servlet.http.HttpSession;
import static org.owasp.esapi.ESAPI.encoder;
import textdisplay.Project;
import textdisplay.ProjectPermissions;
import textdisplay.Transcription;
import user.Group;

/**
 * Update trans-line servlet. This is a transformation of tpen function to web
 * service. It's using tpen MySQL database.
 *
 * @author Han Yan
 */
public class UpdateLineServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        //System.out.println("The back says we should update a line's content");
        try (PrintWriter out = response.getWriter()) {
            if (request.getParameter("text") == null) {
                response.sendError(SC_BAD_REQUEST);
                response.setHeader("Access-Control-Allow-Origin", "*");
                return;
            }
            String text = request.getParameter("text");
            String comment = "";
            if (request.getParameter("comment") != null) {
                comment = request.getParameter("comment");
            }
            HttpSession session = request.getSession();
            if (session.getAttribute("UID") == null || request.getParameter("projectID") == null) {
                response.sendError(SC_UNAUTHORIZED);
                response.setHeader("Access-Control-Allow-Origin", "*");
                return;
            }
            int uid = parseInt(session.getAttribute("UID").toString());
            if (request.getParameter("line") == null) {
                if (request.getParameter("projectID") != null) {
                    int projectID = parseInt(request.getParameter("projectID"));
                    try {
                        Project thisProject = new Project(projectID);
                        if (new Group(thisProject.getGroupID()).isMember(uid)) {
                            thisProject.setLinebreakText(text);
                        }
                    } catch (SQLException e) {
                    }
                }
            }
            if (request.getParameter("projectID") != null) {
                int projectID = parseInt(request.getParameter("projectID"));
                String line = request.getParameter("line");
                try {
                    Project thisProject = new Project(projectID);
                    ProjectPermissions pms = new ProjectPermissions(projectID);
                    if (new Group(thisProject.getGroupID()).isMember(uid) || (pms.getAllow_public_modify() || pms.getAllow_public_modify_annotation())) {
                        Transcription t = new Transcription(line);
                        t.archive(); //create an archived version before making changes
                        t.setText(text);
                        t.setComment(comment);
                        t.setCreator(uid);
                        out.print(encoder().decodeForHTML(new Transcription(line).getText() + "," + new Transcription(line).getComment()));
                        return;
                    } else {
                        response.sendError(SC_FORBIDDEN);
                        response.setHeader("Access-Control-Allow-Origin", "*");
                        return;
                    }
                } catch (SQLException ex) {
                    getLogger(UpdateLineServlet.class.getName()).log(SEVERE, null, ex);
                    response.sendError(SC_INTERNAL_SERVER_ERROR);
                    response.setHeader("Access-Control-Allow-Origin", "*");
                }
            } else {
                String line = request.getParameter("line");
                Transcription t;
                try {
                    t = new Transcription(line);

                    t.setText(text);
                    t.setComment(comment);
                    out.print("success");
                    return;
                } catch (SQLException ex) {
                    getLogger(UpdateLineServlet.class.getName()).log(SEVERE, null, ex);
                }
            }
            out.print("failure");
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
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
