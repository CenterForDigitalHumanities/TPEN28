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
import static edu.slu.util.ServletUtils.getBaseContentType;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import textdisplay.Folio;
import textdisplay.Hotkey;
import textdisplay.Manuscript;
import textdisplay.Metadata;
import textdisplay.Project;
import textdisplay.ProjectPermissions;
import user.Group;
import user.User;
import utils.Tool;
import utils.UserTool;

/**
 *
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
        PrintWriter out = response.getWriter();
        response.setContentType("application/ld+json; charset=UTF-8");
        Gson gson = new Gson();
        Map<String, String> jsonMap = new HashMap();
        if (uid >= 0) {
            try {
//                System.out.println("project id =============== " + request.getParameter("projectID"));
                int projID = Integer.parseInt(request.getParameter("projectID"));
                Project proj = new Project(projID);
                if (proj.getProjectID() > 0) {
                    Group group = new Group(proj.getGroupID());
                    if (group.isMember(uid)) {
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
                            
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(JSONObject.fromObject(jsonMap));
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
