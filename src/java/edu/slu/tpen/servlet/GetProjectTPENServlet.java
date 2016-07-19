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

import static edu.slu.util.ServletUtils.getBaseContentType;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;
import textdisplay.Folio;
import textdisplay.Hotkey;
import textdisplay.Manuscript;
import textdisplay.Metadata;
import textdisplay.Project;
import textdisplay.ProjectPermissions;
import textdisplay.TagButton;
import user.Group;
import user.User;
import utils.Tool;
import utils.UserTool;

import com.google.gson.Gson;

import edu.slu.tpen.transfer.JsonImporter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Get tpen project. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class GetProjectTPENServlet extends HttpServlet {
    /**
    * Handles the HTTP <code>GET</code> method, returning a JSON-LD serialisation of the
    * requested T-PEN project.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int uid = getUID(request, response);
        HttpSession session = request.getSession();
        boolean isTPENAdmin = false;
        try {
        	isTPENAdmin = (new User(uid)).isAdmin();
		} catch (SQLException e) {
			e.printStackTrace();
		}
// Force PrintWriter to use UTF-8 encoded strings.
        response.setContentType("application/json; charset=UTF-8");
        //PrintWriter out = response.getWriter();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
        
        Gson gson = new Gson();
        Map<String, String> jsonMap = new HashMap();
//        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        if (uid >= 0) {
//            System.out.println("UID ================= "+uid);
            try {
//                System.out.println("project id =============== " + request.getParameter("projectID"));
                int projID = Integer.parseInt(request.getParameter("projectID"));
                Project proj = new Project(projID);
                if (proj.getProjectID() > 0) {
                    Group group = new Group(proj.getGroupID());
//                    System.out.println("group Id ===== " + proj.getGroupID() + " is member " + group.isMember(uid));
                    if (group.isMember(uid) || isTPENAdmin) {
                        if (checkModified(request, proj)) {
                            
                            jsonMap.put("project", gson.toJson(proj));
//                            System.out.println("project json ====== " + gson.toJson(proj));
                            int projectID = proj.getProjectID();
                            //get folio
                            Folio[] folios = proj.getFolios();
                            jsonMap.put("ls_fs", gson.toJson(folios));
//                            System.out.println("folios json ========== " + gson.toJson(folios));
                            //get manuscripts
                            List<Manuscript> ls_ms = new ArrayList();
                            for(Folio f : folios){
                                ls_ms.add(new Manuscript(f.folioNumber));
                            }
                            jsonMap.put("ls_ms", gson.toJson(ls_ms));
//                            System.out.println("manuscript json ======= " + gson.toJson(ls_ms));
                            //get project header
                            String header = proj.getHeader();
                            jsonMap.put("ph", header);
//                            System.out.println("header json ======= " + gson.toJson(header));
                            //get group members
                            User[] users = group.getMembers();
                            jsonMap.put("ls_u", gson.toJson(users));
//                            System.out.println("users json ========= " + gson.toJson(users));
                            //get group leader
                            User[] leaders = group.getLeader();
                            
                            // if current user is admin AND not in leaders, add them to leaders array
                            boolean isLeader = false;
                            for (User u: leaders) {
                                if (u.getUID() == uid) {
                                    isLeader = true;
                                    break;
                                }
                            }
                            Object role = session.getAttribute("role");
                            if (!isLeader) {
	                            if (role != null && role.toString().equals("1")) {
	                                User currentUser = new User(uid);
	                                ArrayList<User> leaderList = new ArrayList<User>(Arrays.asList(leaders));
	                                leaderList.add(currentUser);
	                                leaders = leaderList.toArray(new User[leaderList.size()]);
	                            }
                            }
//                            System.out.println("project leaders json ========= " + gson.toJson(leaders));
                            jsonMap.put("ls_leader", gson.toJson(leaders));
                            //get project permission
                            ProjectPermissions pms = new ProjectPermissions(proj.getProjectID());
                            jsonMap.put("projper", gson.toJson(pms));
//                            System.out.println("project permission json ========= " + gson.toJson(pms));
                            //get project buttons
                            Hotkey hk = new Hotkey();
                            List<Hotkey> ls_hk = hk.getProjectHotkeyByProjectID(projectID, uid);
                            jsonMap.put("ls_hk", gson.toJson(ls_hk));
//                            System.out.println("hotkey json ======= " + gson.toJson(ls_hk));
                            //get project tools
                            UserTool[] projectTools = UserTool.getUserTools(projectID);
                            jsonMap.put("projectTool", gson.toJson(projectTools));
                            jsonMap.put("cuser", uid + "");
//                            System.out.println("usertools json ========= " + gson.toJson(projectTools));
                            //get user tools
                            Tool.tools[] userTools = Tool.getTools(uid);
                            jsonMap.put("userTool", gson.toJson(userTools));
                            //get project metadata
                            Metadata metadata = new Metadata(proj.getProjectID());
                            jsonMap.put("metadata", gson.toJson(metadata));
                            
                            String allProjectButtons = TagButton.getAllProjectButtons(projID);
                            jsonMap.put("xml", allProjectButtons);
                            //get special characters
                            jsonMap.put("projectButtons", hk.javascriptToAddProjectButtonsRawData(projectID));
                            
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.println(JSONObject.fromObject(jsonMap));
                        } else {
                           response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        }
                    } else {
                       response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                } else {
                   response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
               throw new ServletException(ex);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
   }

   
   /**
    * Handles the HTTP <code>PUT</code> method, updating a project from a plain JSON serialisation.
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
    * {@inheritDoc}
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Project Import/Export Servlet";
   }

   private static void receiveJsonLD(int uid, HttpServletRequest request, HttpServletResponse response) throws IOException {
      if (uid >= 0) {
         try {
            int projID = Integer.parseInt(request.getPathInfo().substring(1));
            Project proj = new Project(projID);
            if (proj.getProjectID() > 0) {
               if (new Group(proj.getGroupID()).isMember(uid)) {
                  if (getBaseContentType(request).equals("application/json")) {
                     new JsonImporter(proj, uid).update(request.getInputStream());
                     response.setStatus(HttpServletResponse.SC_OK);
                  } else {
                     response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Expecting application/json");
                  }
               } else {
                  response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
               }
            } else {
               response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
         } catch (NumberFormatException | SQLException | IOException ex) {
            reportInternalError(response, ex);
         }
      } else {
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
   }
   

   private boolean checkModified(HttpServletRequest request, Project proj) throws SQLException {
      boolean result = true;
      long modSince = request.getDateHeader("If-Modified-Since");
      if (modSince > 0) {
         Date projMod = proj.getModification();
         result = projMod.after(new Date(modSince));
      }
      return result;
   }
}
