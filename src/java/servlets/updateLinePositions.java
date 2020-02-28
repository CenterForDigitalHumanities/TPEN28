/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
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
 *
 * @author Jon Deering
 */
package servlets;

import java.awt.Rectangle;
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
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import javax.servlet.http.HttpSession;
import textdisplay.ProjectPermissions;
import textdisplay.Transcription;
import user.Group;

/**
 *
 * Update the line position information for a single line of Transcription. Used when they stretch or shrink
 * a line, or add or delete one.
 */
public class updateLinePositions extends HttpServlet {

   /**
    * Processes requests for both HTTP
    * <code>GET</code> and
    * <code>POST</code> methods.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException, SQLException {
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();
      HttpSession session = request.getSession();

      try {
         int UID = 0;
         if (session.getAttribute("UID") == null) {
            response.sendError(SC_UNAUTHORIZED);
            return;
         } else {
            UID = parseInt(session.getAttribute("UID").toString());
            user.User thisUser = new user.User(UID);
            int projectID = 0;
            textdisplay.Project thisProject = null;
            ProjectPermissions pms = null;
            
            if (request.getParameter("projectID") != null) {
               projectID = parseInt(request.getParameter("projectID"));
               thisProject = new textdisplay.Project(projectID);
               pms = new ProjectPermissions(projectID);
            }
            String folioNum;

            if (request.getParameter("folio") != null) {
               folioNum = request.getParameter("folio");
            } else {
               folioNum = "" + thisProject.firstPage();

            }
            if(!(new Group(thisProject.getGroupID()).isMember(UID) || pms.getAllow_public_modify() || pms.getAllow_public_modify_line_parsing())){
                response.sendError(SC_FORBIDDEN);
                return;
            }
            if (request.getParameter("submitted") != null) {

               if (request.getParameter("remove") != null) {
                  textdisplay.Transcription t = new textdisplay.Transcription(request.getParameter("remove"));
                  //System.out.println("archive removed line");
                  t.archive();
                  t.remove();
               }

               if (request.getParameter("new") != null) {
                  if (request.getParameter("folio") == null) {
                     out.print("Failed, missing folio number!");
                     return;
                  }
                  String xStr = request.getParameter("newx");
                  if (xStr.contains(".")) {
                     xStr = xStr.substring(0, xStr.indexOf('.'));
                  }
                  int x = parseInt(xStr);
                  String yStr = request.getParameter("newy");
                  if (yStr.contains(".")) {
                     yStr = yStr.substring(0, yStr.indexOf('.'));
                  }
                  int y = parseInt(yStr);
                  String hStr = request.getParameter("newheight");
                  if (hStr.contains(".")) {
                     hStr = hStr.substring(0, hStr.indexOf('.'));
                  }
                  int height = parseInt(hStr);
                  String wStr = request.getParameter("newwidth");
                  if (wStr.contains(".")) {
                     wStr = wStr.substring(0, wStr.indexOf('.'));
                  }
                  int width = parseInt(wStr);

                  Transcription t = new Transcription(thisUser.getUID(), projectID, parseInt(folioNum), "", "", new Rectangle(x, y, width, height));
                  out.print(t.getLineID());
               }
               if (request.getParameter("update") != null) {
                   
                  String lineID = request.getParameter("update");
                  //System.out.println("The back end says we should update line "+lineID);
                  Transcription t = new Transcription(lineID);
                  String xStr = request.getParameter("updatex");
                  if (xStr.contains(".")) {
                     xStr = xStr.substring(0, xStr.indexOf('.'));
                  }
                  t.archive();
                  t.setX(parseInt(xStr));
                  String yStr = request.getParameter("updatey");
                  if (yStr.contains(".")) {
                     yStr = yStr.substring(0, yStr.indexOf('.'));
                  }
                  t.setY(parseInt(yStr));
                  String hStr = request.getParameter("updateheight");
                  if (hStr.contains(".")) {
                     hStr = hStr.substring(0, hStr.indexOf('.'));
                  }
                  t.setHeight(parseInt(hStr));
                  String wStr = request.getParameter("updatewidth");
                  if (wStr.contains(".")) {
                     wStr = wStr.substring(0, wStr.indexOf('.'));
                  }
                  t.setWidth(parseInt(wStr));
               }

               if (request.getParameter("new") == null) {
                  out.print("success");
               }
            }


         }
      } finally {
         out.close();
      }
   }

   /**
    * Handles the HTTP
    * <code>GET</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try {
         processRequest(request, response);
      } catch (SQLException ex) {
            getLogger(updateLinePositions.class.getName()).log(SEVERE, null, ex);
      }
   }

   /**
    * Handles the HTTP
    * <code>POST</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try {
         processRequest(request, response);
      } catch (SQLException ex) {
            getLogger(updateLinePositions.class.getName()).log(SEVERE, null, ex);
      }
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "Short description";
   }
}
