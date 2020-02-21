/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Hotkey;

/**
 *
 * @author bhaberbe
 */
public class SaveNewProjectCharacter extends HttpServlet {


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
        int projectID = Integer.parseInt(request.getParameter("projectID"));
        int position = Integer.parseInt(request.getParameter("position"));
        int code = Integer.parseInt(request.getParameter("code"));
        String sendOut = "";
        PrintWriter out = response.getWriter();
        try {
            Hotkey newButton = new Hotkey(code, projectID, position, true, true);
            sendOut = "<li position='"+position+"' onclick=\"editcharcnfrm('"+position+"', event);\"  class='character'>&#"+code+"; <span class='removechar' onclick=\"removecharcnfrm("+position+");\">X</span></li>";
            out.println(sendOut);
            out.close();
        } catch (SQLException ex) {
            Logger.getLogger(UpdateProjectTag.class.getName()).log(Level.SEVERE, null, ex);
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
