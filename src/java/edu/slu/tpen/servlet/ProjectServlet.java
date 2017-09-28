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
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.slu.tpen.transfer.JsonImporter;
import edu.slu.tpen.transfer.JsonLDExporter;
import static edu.slu.util.ServletUtils.getBaseContentType;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import textdisplay.Project;
import user.Group;
import user.User;

/**
 * Servlet for transferring project information out of and into T-PEN.
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author tarkvara
 */
public class ProjectServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method, returning a JSON-LD
     * serialisation of the requested T-PEN project.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int uid = 0;
        int projID = 0;
        boolean skip = true;
        String url_piece = req.getRequestURI() + req.getPathInfo().substring(1).replace("/", "").replace("manifest.json","");
        String skip_uid_check = "manifest";
        //System.out.println(url_piece);
//        if(url_piece.contains(skip_uid_check)){
//            System.out.println("We wanna skip");
//            uid = 0;
//            skip = true;
//        }
//        else{
//            uid = getUID(req, resp);
//        }     
        
        if(!resp.containsHeader("Access-Control-Allow-Origin")){
            //System.out.println("Allow origin header in project servlet");
            resp.addHeader("Access-Control-Allow-Origin", "*");
            resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
            resp.addHeader("Access-Control-Allow-Methods", "GET");
        }
        if (uid >= 0) {
            try {
                //System.out.println("Project 1");
                String check = "transcribe";
                String redirect = req.getPathInfo().substring(1);
                if (redirect.contains(check)) {
                    projID = Integer.parseInt(redirect.replace("/" + check, ""));
                    String redirectURL = req.getContextPath() + "/transcription.html?projectID=" + projID;
                    resp.sendRedirect(redirectURL);
                } else {
                    //System.out.println("Project 2");
                    projID = Integer.parseInt(req.getPathInfo().substring(1).replace("/", "").replace("manifest.json",""));
                    Project proj = new Project(projID);
                    //System.out.println("Project 3");
                    if (proj.getProjectID() > 0) {
                        //System.out.println("Project 4");
                        if (new Group(proj.getGroupID()).isMember(uid) || skip) {
                           // System.out.println("export");
                            if (checkModified(req, proj)) {
                               // System.out.println("Project 5");
                                resp.setContentType("application/ld+json; charset=UTF-8");
                                resp.getWriter().write(new JsonLDExporter(proj, new User(uid)).export());
                                resp.setStatus(HttpServletResponse.SC_OK);
                            } else {
                                resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            }
                        } else {
                            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        }
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                throw new ServletException(ex);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Handles the HTTP <code>PUT</code> method, updating a project from a plain
     * JSON serialisation.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        receiveJsonLD(getUID(req, resp), req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletInfo() {
        return "T-PEN Project Import/Export Servlet";
    }

    private static void receiveJsonLD(int uid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (uid >= 0) {
            try {
                int projID = Integer.parseInt(req.getPathInfo().substring(1));
                Project proj = new Project(projID);
                if (proj.getProjectID() > 0) {
                    if (new Group(proj.getGroupID()).isMember(uid)) {
                        if (getBaseContentType(req).equals("application/json")) {
                            new JsonImporter(proj, uid).update(req.getInputStream());
                            resp.setStatus(HttpServletResponse.SC_OK);
                        } else {
                            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Expecting application/json");
                        }
                    } else {
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                reportInternalError(resp, ex);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean checkModified(HttpServletRequest req, Project proj) throws SQLException {
        boolean result = true;
        long modSince = req.getDateHeader("If-Modified-Since");
        if (modSince > 0) {
            Date projMod = proj.getModification();
            result = projMod.after(new Date(modSince));
        }
        return result;
    }
}
