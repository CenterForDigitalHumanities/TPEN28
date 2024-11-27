package edu.slu.tpen.servlet;

import static edu.slu.util.ServletUtils.getDBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.sql.Connection;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Project;
import user.Group;
import user.User;

public class CreateProjectClassic extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        try {
            writer.print(createProject(request, response));
            writer.close();
        } catch (SQLException ex) {
            getLogger(CreateProjectClassic.class.getName()).log(SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public String createProject(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, SQLException {
        int UID = 0;
        try {
            UID = parseInt(request.getSession().getAttribute("UID").toString());
            if (UID < 1) {
                throw new ServletException("User Session is not valid.");
            }
        } catch (NumberFormatException e) {
            throw new ServletException("User Session is unrecognizable.");
        }
        int projectID = 0;
        textdisplay.Project thisProject = null;
        if (request.getParameter("ms") != null) {
            textdisplay.Manuscript mss = new textdisplay.Manuscript(parseInt(request.getParameter("ms")), true);
            int[] msIDs = new int[0];
            User u = new User(UID);
            textdisplay.Project[] p = u.getUserProjects();
            msIDs = new int[p.length];
            for (int i = 0; i < p.length; i++) {
                try {
                    msIDs[i] = new textdisplay.Manuscript(p[i].firstPage()).getID();
                } catch (Exception e) {
                    msIDs[i] = -1;
                }
            }
            for (int l = 0; l < msIDs.length; l++) {
                if (msIDs[l] == mss.getID()) {
                    projectID = p[l].getProjectID();
                    thisProject = p[l];
                }
            }
            if (projectID < 1) {
                String tmpProjName = mss.getShelfMark() + " project";
                if (request.getParameter("title") != null) {
                    tmpProjName = org.owasp.encoder.Encode.forHtml(request.getParameter("title"));
                }
                try (Connection conn = getDBConnection()) {
                    conn.setAutoCommit(false);
                    Group newgroup = new Group(conn, tmpProjName, UID);
                    Project newProject = new Project(conn, tmpProjName, newgroup.getGroupID());
                    newProject.setFolios(conn, mss.getFolios());
                    newProject.addLogEntry(conn, "<span class='log_manuscript'></span>Added manuscript " + mss.getShelfMark(), UID);
                    thisProject = newProject;
                    projectID = thisProject.getProjectID();
                    newProject.importData(UID);
                    conn.commit();
                }
            }
        }
        return "" + projectID;
    }
}
