/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package servlets;

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
import textdisplay.Annotation;

public class annotation extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if(request.getParameter("create")!=null)
            {
                //a create should have xyhw folio text and projectID
                try{
                    int x=parseInt(request.getParameter("x"));
                    int y=parseInt(request.getParameter("y"));
                    int h=parseInt(request.getParameter("h"));
                    int w=parseInt(request.getParameter("w"));
                    int folio=parseInt(request.getParameter("folio"));
                    int projectID=parseInt(request.getParameter("projectID"));
                    String text=request.getParameter("text");
                    try {
                        Annotation a=new Annotation(folio,projectID,text,x,y,h,w);
                        out.print(a.getId());
                    } catch (SQLException ex) {
                        getLogger(annotation.class.getName()).log(SEVERE, null, ex);
                        response.sendError(500);
                    }
                }
                catch(NumberFormatException e)
                {
                    response.sendError(500);
                }
                return;
            }
            if(request.getParameter("update")!=null)
            {
                int id=parseInt(request.getParameter("id"));
                Annotation a=new Annotation(id);
                if(request.getParameter("folio")!=null)
                {
                    a.updateAnnotationFolio(parseInt(request.getParameter("folio")));
                }
                if(request.getParameter("text")!=null)
                {
                    a.updateAnnotationContent(request.getParameter("text"));
                }
                if(request.getParameter("x")!=null)
                {
                    a.updateAnnoationPosition(parseInt(request.getParameter("x")), parseInt(request.getParameter("y")), parseInt(request.getParameter("h")),parseInt(request.getParameter("w")));
                }
                return;
            }
            if(request.getParameter("delete")!=null)
            {
                int id=parseInt(request.getParameter("id"));
                Annotation a=new Annotation(id);
                a.delete();
                return;
            }
        } catch (SQLException ex) {
            getLogger(annotation.class.getName()).log(SEVERE, null, ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
