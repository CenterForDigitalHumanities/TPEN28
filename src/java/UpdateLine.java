/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import javax.servlet.http.HttpSession;
import static org.owasp.esapi.ESAPI.encoder;
import textdisplay.Project;
import textdisplay.Transcription;
import user.Group;

/**
 *
 * @author jdeerin1
 */
public class UpdateLine extends HttpServlet {

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
            HttpSession session = request.getSession();
            if (session.getAttribute("UID") == null) {
                response.sendError(SC_FORBIDDEN);
            }
            else if (request.getParameter("text") == null) {
                getLogger(UpdateLine.class.getName()).log(SEVERE, null, "'text' was not provided.");
                response.sendError(SC_BAD_REQUEST);
            }
            else if (request.getParameter("projectID") == null) {
                getLogger(UpdateLine.class.getName()).log(SEVERE, null, "'projectID' was not provided.");
                response.sendError(SC_BAD_REQUEST);
            }
            else{
                String text = request.getParameter("text");
                String comment = "";
                int projectID = parseInt(request.getParameter("projectID"));
                int uid = parseInt(session.getAttribute("UID").toString());
                String line = request.getParameter("line");
                try{
                    Project thisProject = new Project(projectID);
                    if (request.getParameter("comment") != null) {
                        comment = request.getParameter("comment");
                    }
                    if (line == null) {
                        if (request.getParameter("projectID") != null) {
                            if (new Group(thisProject.getGroupID()).isMember(uid)) {
                                thisProject.setLinebreakText(text);
                            }
                        }
                    }
                    if (new Group(thisProject.getGroupID()).isMember(uid)) {
                        Transcription t = new Transcription(line);
                        t.archive(); //create an archived version before making changes
                        t.setText(text);
                        t.setComment(comment);
                        t.setCreator(uid);
                        out.print(encoder().decodeForHTML(new Transcription(line).getText()));
                    } 
                    else {
                        response.sendError(SC_FORBIDDEN);
                    }
                }
                catch(SQLException e){
                    System.out.println("UpdateLine SQL failure");
                    getLogger(UpdateLine.class.getName()).log(SEVERE, null, e);
                    response.sendError(SC_INTERNAL_SERVER_ERROR);
                }
            }
        }
        catch(Exception e){
            System.out.println("UpdateLine generic failure");
            getLogger(UpdateLine.class.getName()).log(SEVERE, null, e);
            response.sendError(SC_INTERNAL_SERVER_ERROR);
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
