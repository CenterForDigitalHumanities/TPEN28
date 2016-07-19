/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author hanyan
 */
public class createManuscript extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if(request.getParameter("city")!=null)
        {
            try {
                String repository="unknown";
                String archive="HMML";
                String city="unknown";
                String collection="unknown";

                city=request.getParameter("city");

                if(request.getParameter("collection")!=null)
                {
                    collection=request.getParameter("collection");
                }
                if(request.getParameter("repository")!=null)
                {
                    repository=request.getParameter("repository");
                }
                textdisplay.Manuscript m=new textdisplay.Manuscript(repository, archive, city, city, -999);
                System.out.println("msID ============= " + m.getID());
                String urls=request.getParameter("urls");
                String [] seperatedURLs=urls.split(";");
                String names=request.getParameter("names");
                String [] seperatedNames=names.split(",");
                for(int i=0;i<seperatedURLs.length;i++)
                {
                    int num = textdisplay.Folio.createFolioRecordFromVhmml(city, seperatedNames[i], seperatedURLs[i].replace('_', '&'), archive, m.getID());
                }
                PrintWriter writer = response.getWriter();
                writer.print(m.getID());
            } catch (SQLException ex) {
                Logger.getLogger(createManuscript.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
