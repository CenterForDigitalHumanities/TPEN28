import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import textdisplay.Project;
import textdisplay.Transcription;
import user.Group;

import static java.lang.Integer.parseInt;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.owasp.esapi.ESAPI.encoder;

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
                response.sendError(SC_FORBIDDEN, "User not logged in.");
                return;
            }
            if (request.getParameter("text") == null) {
                getLogger(UpdateLine.class.getName()).log(SEVERE, "'text' was not provided.");
                response.sendError(SC_BAD_REQUEST, "'text' parameter is missing.");
                return;
            }
            if (request.getParameter("projectID") == null) {
                getLogger(UpdateLine.class.getName()).log(SEVERE, "'projectID' was not provided.");
                response.sendError(SC_BAD_REQUEST, "'projectID' parameter is missing.");
                return;
            }

            String text = request.getParameter("text");
            String comment = "";
            int projectID = parseInt(request.getParameter("projectID"));
            int uid = parseInt(session.getAttribute("UID").toString());
            String line = request.getParameter("line");

            try {
                Project thisProject = new Project(projectID);
                if (request.getParameter("comment") != null) {
                    if (line == null) {
                        if (!new Group(thisProject.getGroupID()).isMember(uid)) {
                            response.sendError(SC_FORBIDDEN, "User is not a member of the project group.");
                            return;
                        }
                        thisProject.setLinebreakText(text);
                        out.print(encoder().decodeForHTML(thisProject.getLinebreakText()));
                    } else {
                        if (!new Group(thisProject.getGroupID()).isMember(uid)) {
                            response.sendError(SC_FORBIDDEN, "User is not a member of the project group.");
                            return;
                        }
                        Transcription t = new Transcription(line);
                        t.archive(); //create an archived version before making changes
                        t.setText(text);
                        t.setComment(comment);
                        t.setCreator(uid);
                        out.print(encoder().decodeForHTML(t.getText()));
                    }
                }
            } catch (SQLException e) {
                System.out.println("UpdateLine SQL failure");
                getLogger(UpdateLine.class.getName()).log(SEVERE, null, e);
                response.sendError(SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
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
     * @throws ServletException if an I/O error occurs
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
