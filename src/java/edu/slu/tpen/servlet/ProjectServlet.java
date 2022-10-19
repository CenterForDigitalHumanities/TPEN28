/*
 * Copyright 2014- Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliances with the License. You may
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

import edu.slu.tpen.transfer.JsonImporter;
import edu.slu.tpen.transfer.JsonLDExporter;
import static edu.slu.util.ServletUtils.getBaseContentType;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
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
     * If <code>Accept</code> header in the request is specified
     * and contains <code>iiif/v3</code>, returns Presentation v3.0 serialisation.
     * Otherwise, returns Presentation v2.1 serialisation.
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
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Headers", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET");
        resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
        //System.out.println(url_piece);
//        if(url_piece.contains(skip_uid_check)){
//            System.out.println("We wanna skip");
//            uid = 0;
//            skip = true;
//        }
//        else{
//            uid = getUID(req, resp);
//        }     
        if (uid >= 0) {
            try {
                //System.out.println("Project 1");
                String check = "transcribe";
                String redirect = req.getPathInfo().substring(1);
                if (redirect.contains(check)) {
                    projID = parseInt(redirect.replace("/" + check, ""));
                    String redirectURL = req.getContextPath() + "/transcription.html?projectID=" + projID;
                    resp.sendRedirect(redirectURL);
                } else {
                    //System.out.println("Project 2");
                    projID = parseInt(req.getPathInfo().substring(1).replace("/", "").replace("manifest.json",""));
                    Project proj = new Project(projID);
                    //System.out.println("Project 3");
                    if (proj.getProjectID() > 0) {
                        //System.out.println("Project 4");
                        if (new Group(proj.getGroupID()).isMember(uid) || skip) {
                           // System.out.println("export");
                            if (checkModified(req, proj)) {
                               // System.out.println("Project 5");
                                resp.setContentType("application/ld+json; charset=UTF-8");
                                //resp.setHeader("Etag", req.getContextPath() + "/manifest/"+proj.getProjectID());
                                //We expect projects to change, often.  Let's just help quick page refresh.
                                try{
                                    String lastModifiedDate = proj.getModification().toString().replace(" ", "T");
                                    //Note that dates like 2021-05-26T10:39:19.328 have been rounded to 2021-05-26T10:39:19.328 in browser headers.  Account for that here.
                                    if(lastModifiedDate.contains(".")){
                                        //If-Modified-Since and Last-Modified headers are rounded.  Wed, 26 May 2021 10:39:19.629 GMT becomes Wed, 26 May 2021 10:39:19 GMT.
                                        lastModifiedDate = lastModifiedDate.split("\\.")[0];
                                    }
                                    LocalDateTime ldt = LocalDateTime.parse(lastModifiedDate, DateTimeFormatter.ISO_DATE_TIME);
                                    ZonedDateTime fromObject = ldt.atZone(ZoneId.of("GMT"));
                                    resp.setHeader("Last-Modified", fromObject.format(DateTimeFormatter.RFC_1123_DATE_TIME));
                                }
                                catch(DateTimeParseException ex){
                                    System.out.println("Last-Modified Header could not be formed.  Bad date value for project "+proj.getProjectID());
                                }
                                catch(Exception e){
                                
                                }
                                resp.setHeader("Access-Control-Allow-Headers", "*");
                                resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                                resp.setHeader("Cache-Control", "max-age=15, must-revalidate");

                                if (req.getHeader("Accept") != null && req.getHeader("Accept").contains("iiif/v3"))
                                {
//                                    System.out.println(req.getHeader("Accept"));
                                    resp.setHeader("Content-Type", "application/ld+json;profile=\"http://iiif.io/api/presentation/3/context.json\"");
                                    resp.getWriter().write(new JsonLDExporter(proj, new User(uid), "v3").export());

                                } else
                                {
                                    resp.getWriter().write(new JsonLDExporter(proj, new User(uid)).export());
                                }

                                resp.setStatus(SC_OK);
                            } else {
                                //FIXME we seem to make it here, but the response is still 200 with the object in the body...doesn't seem to save any time.
                                //No work necessary, send out the 304.
                                resp.setStatus(SC_NOT_MODIFIED);
                            }
                        } else {
                            resp.sendError(SC_UNAUTHORIZED);
                        }
                    } else {
                        resp.sendError(SC_NOT_FOUND);
                    }
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                throw new ServletException(ex);
            }
        } else {
            resp.sendError(SC_UNAUTHORIZED);
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
     * Handles the HTTP <code>OPTIONS</code> preflight method.
     * Pre-flight support.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //These headers must be present to pass browser preflight for CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Max-Age", "600"); //Cache preflight responses for 10 minutes.
        response.setStatus(200);
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
                int projID = parseInt(req.getPathInfo().substring(1));
                Project proj = new Project(projID);
                if (proj.getProjectID() > 0) {
                    if (new Group(proj.getGroupID()).isMember(uid)) {
                        if (getBaseContentType(req).equals("application/json")) {
                            new JsonImporter(proj, uid).update(req.getInputStream());
                            resp.setStatus(SC_OK);
                        } else {
                            resp.sendError(SC_UNSUPPORTED_MEDIA_TYPE, "Expecting application/json");
                        }
                    } else {
                        resp.sendError(SC_UNAUTHORIZED);
                    }
                } else {
                    resp.sendError(SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                reportInternalError(resp, ex);
            }
        } else {
            resp.sendError(SC_UNAUTHORIZED);
        }
    }

    private boolean checkModified(HttpServletRequest request, Project proj) throws SQLException {
        if(request.getHeader("If-Modified-Since") != null && !request.getHeader("If-Modified-Since").equals("")){
            String lastModifiedDateHeader = request.getHeader("If-Modified-Since");
            String lastModifiedDateProj = proj.getModification().toString().replace(" ", "T");
            //Note that dates like 2021-05-26T10:39:19.328 have been rounded to 2021-05-26T10:39:19.328 in browser headers.  Account for that here.
            if(lastModifiedDateProj.contains(".")){
                //If-Modified-Since and Last-Modified headers are rounded.  Wed, 26 May 2021 10:39:19.629 GMT becomes Wed, 26 May 2021 10:39:19 GMT.
                lastModifiedDateProj = lastModifiedDateProj.split("\\.")[0];
            }
            try{
                LocalDateTime ldt = LocalDateTime.parse(lastModifiedDateProj, DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime fromProject = ldt.atZone(ZoneId.of("GMT"));
                ZonedDateTime fromHeader = ZonedDateTime.parse(lastModifiedDateHeader, DateTimeFormatter.RFC_1123_DATE_TIME);
                if(fromHeader.isEqual(fromProject)){
                    return false;
                }
                else{
                    return fromHeader.isBefore(fromProject);
                }
            }
            catch(DateTimeParseException ex){
                System.out.println("Last-Modified Header could not be formed.  Bad date value for project "+proj.getProjectID());
                return true;
            }
        }
        else{
            //There was no header, so consider this modified.
            //System.out.println("No 'If-Modified-Since' Header present for project request, consider it modified");
            return true;
        }
    }
}
