/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static textdisplay.Folio.getBadFolioReport;

/**
 *
 * @author bhaberbe
 */
@WebServlet(name = "FolioReport", urlPatterns = {"/folioReport"})
public class FolioReport extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * 
     * This will use Folio.java and wait for the whole report to be generated before writing to the file.  Even if you try to write out line by line,
     * it waits for the servlet to finish and writes the whole file at once.
     * There are roughly 1.5 million entries to do a header request for to check, so it takes around 8 days to run.  
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        try{
            PrintWriter clearer = new PrintWriter("/home/hanyan/sql/folioOutput.txt");
            clearer.close();
        }
        catch (Exception e){
            out.println("File not yet created...");
        }
        try (PrintWriter out = new PrintWriter(new    
                FileOutputStream("/home/hanyan/sql/folioOutput.txt",true))) {
            String newline = "\n";
            //try (PrintWriter out = response.getWriter()) {
            //out.println("Below is a report of the folios whose URL's are broken: "+newline);
            out.println("Processing folio report request...");
            try {
                String foliosToReturn = getBadFolioReport();
                out.println("Writing to text file...");
                out.println(foliosToReturn + newline);
            } catch (SQLException ex) {
                getLogger(FolioReport.class.getName()).log(SEVERE, null, ex);
                out.println(ex + newline);
            }
            out.println("File created!");
            //out.println("End of the listt.  Hopefully it is short.");
            out.flush();
            //}
        }    }
    

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
        return "Generate a report of bad folios.";
    }// </editor-fold>

}
