/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

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
import textdisplay.TagButton;

/**
 *
 * @author bhaberbe
 * FIXME : This is for buttons.html, which has been abandoned.  Flagged for deletion.
 */
public class SaveNewProjectXML extends HttpServlet {


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
            this.doPost(request, response); 
        }   

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int projectID = parseInt(request.getParameter("projectID"));
        int position = parseInt(request.getParameter("position"));
        String tag = request.getParameter("tag");
        String description = request.getParameter("description");
        PrintWriter out = response.getWriter();
        String buttonToSendOut = "";
        try {
            TagButton newButton = new TagButton(projectID, position, tag, description, true);
            buttonToSendOut = "<li class='projectTag' position='"+position+"' onclick=\"editxmlcnfrm("+position+", event);\">"+tag+"<span class='removechar' onclick=\"removetagcnfrm("+position+");\">X</span></li>";
            out.println(buttonToSendOut);
            out.close();
        } catch (SQLException ex) {
            getLogger(UpdateProjectTag.class.getName()).log(SEVERE, null, ex);
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
    }// </editor-fold>

}
