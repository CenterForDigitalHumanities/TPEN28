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

import com.google.gson.Gson;
import edu.slu.tpen.transfer.JsonImporter;
import edu.slu.tpen.transfer.JsonLDExporter;
import static edu.slu.util.ServletUtils.getBaseContentType;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import static java.lang.System.gc;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static net.sf.json.JSONObject.fromObject;
import textdisplay.Folio;
import textdisplay.Hotkey;
import textdisplay.Manuscript;
import textdisplay.Metadata;
import textdisplay.Project;
import textdisplay.ProjectPermissions;
import static textdisplay.TagButton.getAllProjectButtons;
import user.Group;
import user.User;
import utils.Tool;
import static utils.Tool.getTools;
import utils.UserTool;
import static utils.UserTool.getUserTools;

/**
 * Get tpen project. This is a transformation of tpen function to web service.
 * It's using tpen MySQL database.
 *
 * @author hanyan
 */
public class GetProjectTPENServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method, returning a JSON-LD
     * serialisation of the requested T-PEN project.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int uid = getUID(request, response);
        User user = null;
        try {
            user = new User(uid);
            getLogger(GetProjectTPENServlet.class.getName()).log(INFO, "Get project {0} as JSON for {1}", new Object[]{request.getParameter("projectID"), user.getUname()});
        } catch (SQLException ex) {
            getLogger(GetProjectTPENServlet.class.getName()).log(SEVERE, "Failed to GET project {0} as JSON for user({1}) \n{2}", new Object[]{request.getParameter("projectID"), uid, ex});
        }
        HttpSession session = request.getSession();
        boolean isTPENAdmin = false;
        try {
            isTPENAdmin = user.isAdmin();
        } catch (SQLException ex) {
            getLogger(GetProjectTPENServlet.class.getName()).log(SEVERE, "Failed to check admin status for user({0}).\n{1}", new Object[]{uid, ex});
        }
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
        String manifest_obj_str;
        Gson gson = new Gson();
        Map<String, String> jsonMap = new HashMap();
        JSONObject jo_error = new JSONObject();
        JSONArray mans_and_restrictions = new JSONArray();
        ArrayList manuscripts_in_project = new ArrayList();
        jo_error.element("error", "No Manifest URL");
        if (uid >= 0) {
            try {
                int projID = parseInt(request.getParameter("projectID"));
                Project proj = new Project(projID);
                if (proj.getProjectID() > 0) {
                    Group group = new Group(proj.getGroupID());
                    ProjectPermissions pms = new ProjectPermissions(proj.getProjectID());
                    jsonMap.put("projper", gson.toJson(pms));
                    if (group.isMember(uid) || isTPENAdmin || pms.getAllow_public_read_transcription()) { //check for public project here
                        if (checkModified(request, proj)) {
                            getLogger(GetProjectTPENServlet.class.getName()).log(INFO, "Approved for GET. isAdmin:{0}; isPublicProject:{1}", new Object[]{isTPENAdmin, pms.getAllow_public_read_transcription()});
                            jsonMap.put("project", gson.toJson(proj));
                            int projectID = proj.getProjectID();
                            Folio[] folios = proj.getFolios();
                            JSONArray folios_array = JSONArray.fromObject(gson.toJson(folios));
                            JSONObject folio_obj;
                            //TODO need to get the ipr agreement status for the archive of this folio for this user 
                            for (int x = 0; x < folios.length; x++) {
                                Integer folioNum = folios[x].getFolioNumber();
                                Manuscript forThisFolio = new Manuscript(folioNum);
                                int manID = forThisFolio.getID();
                                User controller = forThisFolio.getControllingUser();
                                String controllerName = "public project";
                                String archive = folios[x].getArchive();
                                String ipr_agreement = folios[x].getIPRAgreement();
                                if (null != controller) {
                                    controllerName = controller.getUname();
                                }
                                folio_obj = folios_array.getJSONObject(x);
                                folio_obj.element("manuscript", manID);
                                folio_obj.element("archive", archive);
                                folio_obj.element("ipr_agreement", ipr_agreement);
                                if (user.getUname().equals(controllerName) || !forThisFolio.isRestricted()) {
                                    //This user is the controlling user or this folio is not restricted
                                    folio_obj.element("ipr", true); //should be true in this case because the check needs to pass
                                } else {
                                    folio_obj.element("ipr", user.hasAcceptedIPR(folioNum));
                                }
                                folios_array.set(x, folio_obj);
                                JSONObject for_the_array = new JSONObject();
                                for_the_array.element("manID", Integer.toString(manID));
                                for_the_array.element("auth", "false");
                                for_the_array.element("controller", controllerName);
                                if (!manuscripts_in_project.contains(manID)) {
                                    //Then this manuscript is accounted for.
                                    manuscripts_in_project.add(manID);
                                    User currentUser = user;
                                    if (forThisFolio.isRestricted()) {
                                        if (forThisFolio.isAuthorized(currentUser)) {
                                            for_the_array.element("auth", "true");
                                        } else {
                                            getLogger(GetProjectTPENServlet.class.getName()).log(WARNING, "User {0} is not authorized to view folio {1}", new Object[]{currentUser.getUname(), folioNum});
                                        }
                                    } else {
                                        for_the_array.element("auth", "true");
                                    }
                                    mans_and_restrictions.add(for_the_array);
                                }
                            }
                            jsonMap.put("ls_fs", folios_array.toString());
                            jsonMap.put("mans_in_project", manuscripts_in_project.toString());
                            jsonMap.put("user_mans_auth", mans_and_restrictions.toString());
                            manifest_obj_str = new JsonLDExporter(proj, user).export();
                            jsonMap.put("manifest", manifest_obj_str);
                            String header = proj.getHeader();
                            jsonMap.put("ph", header);
                            String linebreakRemainingText = proj.getLinebreakText();
                            jsonMap.put("remainingText", linebreakRemainingText);
                            //get group members
                            User[] users = group.getMembers();
                            jsonMap.put("ls_u", gson.toJson(users));
                            //get group leader
                            User[] leaders = group.getLeader();
                            // if current user is admin AND not in leaders, add them to leaders array
                            boolean isLeader = false;
                            for (User u : leaders) {
                                if (u.getUID() == uid) {
                                    isLeader = true;
                                    break;
                                }
                            }
                            Object role = session.getAttribute("role");
                            if (!isLeader) {
                                if (role != null && role.toString().equals("1")) {
                                    User currentUser = user;
                                    ArrayList<User> leaderList = new ArrayList<>(asList(leaders));
                                    leaderList.add(currentUser);
                                    leaders = leaderList.toArray(new User[leaderList.size()]);
                                }
                            }
                            jsonMap.put("ls_leader", gson.toJson(leaders));
                            //get project permission
                            //get project buttons
                            Hotkey hk = new Hotkey();
                            List<Hotkey> ls_hk = hk.getProjectHotkeyByProjectID(projectID, uid);
                            jsonMap.put("ls_hk", gson.toJson(ls_hk));
                            //get project tools
                            UserTool[] projectTools = getUserTools(projectID);
                            jsonMap.put("projectTool", gson.toJson(projectTools));
                            jsonMap.put("cuser", uid + "");
                            //get user tools
                            Tool.tools[] userTools = getTools(uid);
                            jsonMap.put("userTool", gson.toJson(userTools));
                            //get project metadata
                            Metadata metadata = new Metadata(proj.getProjectID());
                            jsonMap.put("metadata", gson.toJson(metadata));
                            String allProjectButtons = getAllProjectButtons(projID);
                            jsonMap.put("xml", allProjectButtons);
                            //get special characters
                            jsonMap.put("projectButtons", hk.javascriptToAddProjectButtonsRawData(projectID));
                            response.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                            //response.setHeader("Etag", request.getContextPath() + "/getProjectTPENServlet/project/"+proj.getProjectID());
                            response.setHeader("Cache-Control", "max-age=8, must-revalidate");
                            ZonedDateTime z = ZonedDateTime.parse(proj.getModification().toString());
                            String formattedLastModifiedDate = z.format(DateTimeFormatter.RFC_1123_DATE_TIME); // Magic Make it an RFC date
                            response.setHeader("Last-Modified", formattedLastModifiedDate);
                            response.setStatus(SC_OK);
                            out.println(fromObject(jsonMap));
                            out.close();
                        } else {
                            response.setStatus(SC_NOT_MODIFIED);
                            out.close();
                        }
                        gc(); //Force garbage cleaning to remove null pointers, empty variables, and new Whatevers that were destroyed by return statements.
                    } else {
                        response.sendError(SC_FORBIDDEN);
                        out.close();
                        gc();
                    }
                } else {
                    response.sendError(SC_NOT_FOUND);
                    out.close();
                    gc();
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                gc();
                out.close();
                getLogger(GetProjectTPENServlet.class.getName()).log(SEVERE, null, ex);
                throw new ServletException(ex);
            }
        } else {
            gc();
            out.close();
            response.sendError(SC_UNAUTHORIZED);
        }
    }

    /**
     * Handles the HTTP <code>PUT</code> method, updating a project from a plain
     * JSON serialisation.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        receiveJsonLD(getUID(request, response), request, response);
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

    private static void receiveJsonLD(int uid, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (uid >= 0) {
            try {
                int projID = parseInt(request.getPathInfo().substring(1));
                Project proj = new Project(projID);
                if (proj.getProjectID() > 0) {
                    if (new Group(proj.getGroupID()).isMember(uid)) {
                        if (getBaseContentType(request).equals("application/json")) {
                            new JsonImporter(proj, uid).update(request.getInputStream());
                            response.setStatus(SC_OK);
                        } else {
                            response.sendError(SC_UNSUPPORTED_MEDIA_TYPE, "Expecting application/json");
                        }
                    } else {
                        response.sendError(SC_FORBIDDEN);
                    }
                } else {
                    response.sendError(SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                reportInternalError(response, ex);
            }
        } else {
            response.sendError(SC_UNAUTHORIZED);
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
            LocalDateTime ldt = LocalDateTime.parse(lastModifiedDateProj, DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime fromProject = ldt.atZone(ZoneId.of("GMT"));
            ZonedDateTime fromHeader = ZonedDateTime.parse(lastModifiedDateHeader, DateTimeFormatter.RFC_1123_DATE_TIME);
            if(fromHeader.isEqual(fromProject)){
                System.out.println("TPEN says not modified");
                return false;
            }
            else{
                return fromHeader.isBefore(fromProject);
            }
        }
        else{
            //There was no header, so consider this modified.
            System.out.println("No 'If-Modified-Since' Header present for project request, consider it modified");
            return true;
        }
    }
}
