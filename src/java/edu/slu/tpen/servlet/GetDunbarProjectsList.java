/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package edu.slu.tpen.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.util.LangUtils.buildQuickMap;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import textdisplay.Project;
import textdisplay.Folio;
import user.User;

/**
 *
 * @author bhaberbe
 */
public class GetDunbarProjectsList extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        int uid = getUID(request, response);
        if (uid > 0) {
           response.setContentType("application/json");
           try {
              User u = new User(uid);
              Project[] projs = Project.getAllDunbarProjects();
              List<Map<String, Object>> result = new ArrayList<>();
              //image name/page name contains Fsomething that is folder number.  Add this change.
              for (Project p: projs) {
                    Folio fp = new Folio(p.firstPage());
                    String pattern1 = "_F";
                    String pattern2 = "_";
                    String pattern3 = ".jpg";
                    String regexString1 = Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2);
                    Pattern patternA = Pattern.compile(regexString1);
                    Matcher matcherA = patternA.matcher(fp.getPageName());
                    String shortCode = "";
                    String longCode = "";
                    if (matcherA.find()) {
                        shortCode = matcherA.group(1); // Since (.*?) is capturing group 1
                    }
                    String delShort = "F"+shortCode;
                    result.add(buildQuickMap("id", ""+p.getProjectID(), "project_name", p.getName(), "metadata_name", p.getProjectName(), "code", delShort));
              }
              ObjectMapper mapper = new ObjectMapper();
              mapper.writeValue(response.getOutputStream(), result);
           } catch (SQLException ex) {
              reportInternalError(response, ex);
              response.sendError(SC_INTERNAL_SERVER_ERROR, ex.getMessage());
           }
        } 
        else {
            response.sendError(SC_UNAUTHORIZED);
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
