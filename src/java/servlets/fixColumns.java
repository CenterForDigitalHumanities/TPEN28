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
 */
package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static edu.slu.util.ServletUtils.getDBConnection;
import static edu.slu.util.ServletUtils.reportInternalError;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.Project;
import textdisplay.Transcription;

/**
 * @author Jon Deering
 */
public class fixColumns extends HttpServlet {

   /**
    * Handle the HTTP <code>GET</code> method.
    *
    * @param req servlet request
    * @param resp servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      resp.setContentType("text/html;charset=UTF-8");
      HttpSession session = req.getSession();
      if (session.getAttribute("UID") == null) {
         resp.sendError(403);
      } else {
         PrintWriter out = resp.getWriter();
         int UID = Integer.parseInt(session.getAttribute("UID").toString());
         int projectID = Integer.parseInt(req.getParameter("projectID"));
         int folioNum = Integer.parseInt(req.getParameter("folioNum"));

         try (Connection conn = getDBConnection()) {
            Project thisProject = new Project(projectID);
         
            int[] lines = new int[999];
            int[] lines2 = new int[999];
            int[] lines3 = new int[999];
            int[] lines4 = new int[999];
            int ctr = 0;//how many actual values are in here
            for (int i = 0; i < 999; i++) {
               if (req.getParameter("t" + i) != null) {
                  try {
                     int decimalPoint = req.getParameter("t" + i).indexOf('.');
                     int tmp;
                     if (decimalPoint > 0) {
                        tmp = Integer.parseInt(req.getParameter("t" + i).substring(0, decimalPoint));
                     } else {
                        tmp = Integer.parseInt(req.getParameter("t" + i));
                     }
                     int tmp2;
                     decimalPoint = req.getParameter("l" + i).indexOf('.');
                     if (decimalPoint > 0) {
                        tmp2 = Integer.parseInt(req.getParameter("l" + i).substring(0, decimalPoint));
                     } else {
                        tmp2 = Integer.parseInt(req.getParameter("l" + i));
                     }
                     int tmp3;
                     decimalPoint = req.getParameter("w" + i).indexOf('.');
                     if (decimalPoint > 0) {
                        tmp3 = Integer.parseInt(req.getParameter("w" + i).substring(0, decimalPoint));
                     } else {
                        tmp3 = Integer.parseInt(req.getParameter("w" + i));
                     }
                     int tmp4;
                     decimalPoint = req.getParameter("b" + i).indexOf('.');
                     if (decimalPoint > 0) {
                        tmp4 = Integer.parseInt(req.getParameter("b" + i).substring(0, decimalPoint));
                     } else {
                        tmp4 = Integer.parseInt(req.getParameter("b" + i));
                     }
                     lines[i] = tmp;
                     lines2[i] = tmp2;
                     lines3[i] = tmp3;
                     lines4[i] = tmp4;
                     ctr++;
                  } catch (NumberFormatException e) {
                  }
               }
            }

            int[] linePositions = new int[ctr + 1];
            int[] linePositions2 = new int[ctr + 1];
            int[] linePositions3 = new int[ctr + 1];
            int[] linePositions4 = new int[ctr + 1];
            int nonSequentialCounter = 0;
            for (int i = 0; i < linePositions.length && nonSequentialCounter < 998; i++) {
               while (lines[nonSequentialCounter] == 0 && lines2[nonSequentialCounter] == 0 & lines3[nonSequentialCounter] == 0 && nonSequentialCounter < 998) {
                  nonSequentialCounter++;
               }
               linePositions[i] = lines[nonSequentialCounter];
               linePositions2[i] = lines2[nonSequentialCounter];
               linePositions3[i] = lines3[nonSequentialCounter];
               linePositions4[i] = lines4[nonSequentialCounter];
               nonSequentialCounter++;
               if (nonSequentialCounter == 998) {
                  break;
               }
            }
            Transcription[] t = Transcription.getProjectTranscriptions(projectID, folioNum);
            for (int i = 0; i < t.length; i++) {
               t[i].remove();
            }
            Folio thisFolio = new Folio(folioNum, true);

            //Folio thisFolio=new Folio(folioNum,true);
            if (projectID > 0) {
               thisProject.update(linePositions, linePositions2, linePositions3, linePositions4, folioNum);
               Manuscript ms = new Manuscript(folioNum);
               thisProject.addLogEntry(conn, "<span class='log_parsing'></span>Corrected line parsing of page  " + ms.getShelfMark() + " " + thisFolio.getPageName(), UID); // ,"parsing"
            }
            thisProject.detectInColumns(thisFolio.getFolioNumber());
            out.print("success");
         } catch (SQLException ex) {
            reportInternalError(resp, ex);
         } finally {
            out.close();
         }
      }
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Fix Columns servlet";
   }
}
