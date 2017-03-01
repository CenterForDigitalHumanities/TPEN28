/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.ArchivedTranscription;
import textdisplay.Project;
import textdisplay.Transcription;
import user.User;

/**
 *
 * @author bhaberbe
 */
public class GetHistory extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * 
     * This servlet creates the transcription and parsing history HTML for transcription.html to use in #historySplit #historyListing.
     * Please note this servlet returns the entirety of the HTML.  The request must contain projectID and p, where p is the folio number.
     * @throws java.sql.SQLException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder historyHTML = new StringBuilder("");
        StringBuilder historyLine = new StringBuilder("");
        StringBuilder historyEntry = new StringBuilder("");
        StringBuilder historyEntryCollection = new StringBuilder("");
        Calendar m = Calendar.getInstance(); //midnight
        //             m.set(Calendar.HOUR_OF_DAY, 0);
        //             m.set(Calendar.MINUTE, 0);
        //             m.set(Calendar.SECOND, 0);
        //             m.set(Calendar.MILLISECOND, 0);
        int DOY = m.get(Calendar.DAY_OF_YEAR);
        int YEAR= m.get(Calendar.YEAR);
        Transcription[] thisText;
        int projectID = 0;
        int pageno = 0;
        projectID = Integer.parseInt(request.getParameter("projectID"));
        pageno = Integer.parseInt(request.getParameter("p"));
        Map<Integer, List<ArchivedTranscription>> pageHistory = ArchivedTranscription.getAllVersionsForPage(projectID, pageno);
        Project thisProject = new Project(projectID);
        if (pageno < 0) pageno = thisProject.firstPage();
        thisText = Transcription.getProjectTranscriptions(projectID, pageno);
        int i = -1;
        for (Transcription t: thisText) {
            i++;
            historyLine.setLength(0);
            historyEntry.setLength(0);
            historyEntryCollection.setLength(0);
            List<ArchivedTranscription> history = pageHistory.get(t.getLineID());
            if (history == null){ //This will be the first entry, so the entry is the line
                historyLine.append("<div class='historyLine' id='history").append(t.getLineID()).append("' linewidth='").append(t.getWidth()).append("' lineheight='").append(t.getHeight()).append("' lineleft='").append(t.getX()).append("' linetop='").append(t.getY()).append("'>No previous versions</div>");
                historyHTML.append(historyLine);
            } 
            else {
                historyLine.append("<div class='historyLine' id='history").append(t.getLineID()).append("'>");
                for (ArchivedTranscription h: history){
                    historyEntry.setLength(0);
                    historyEntry.append("<div class='historyEntry ui-corner-all' linewidth='").append(h.getWidth()).append("' lineheight='").append(h.getHeight()).append("' lineleft='").append(h.getX()).append("' linetop='").append(h.getY()).append("'>");
                    String dateString = "-";
                    DateFormat dfm;
                    Calendar historyDate = Calendar.getInstance();
                    historyDate.setTimeInMillis(h.getDate().getTime());
                    if ((YEAR == historyDate.get(Calendar.YEAR)) && (DOY == historyDate.get(Calendar.DAY_OF_YEAR))){ // not perfect, but other date comparisons were frustrating
                        dfm = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                        dateString = "today";//DateFormat.getTimeInstance(DateFormat.SHORT).format(historyDate);
                    } else {
                        dfm = DateFormat.getDateInstance(DateFormat.MEDIUM);
                        dateString = dfm.format(h.getDate());//DateFormat.getDateInstance(DateFormat.MEDIUM).format(historyDate);
                    }
                    dfm.setCalendar(historyDate);
                    historyEntry.append("<div class='historyDate'>").append(dateString).append("</div>");
                    
                    if (h.getCreator() > 0){
                        User creatorUser = new User(h.getCreator());
                        String creatorName = creatorUser.getFname()+" "+creatorUser.getLname();
                        historyEntry.append("<div class='historyCreator'>").append(creatorName).append("</div>");
                    
                    }
                    String lineText = h.getText();
                    String lineComment = h.getComment();
                    if(null == lineText || lineText.equals("")){
                        lineText = "<span style='color:silver;'>&#45; empty &#45;</span>";
                    }
                    if(null == lineComment || lineComment.equals("")){
                        lineComment = "<span style='color:silver;'>&#45; empty &#45;</span>";
                    }
                    historyEntry.append("<div class='right historyRevert'></div><div class='right loud historyDims'></div><div class='historyText'>")
                            .append(lineText)
                            .append("</div><div class='historyNote'>")
                            .append(lineComment)
                            .append("</div>");
                   
                    //if(isMember || permitModify){
                    historyEntry.append("<div class='historyOptions'><span title='Revert image parsing only' class='ui-icon ui-icon-image right'></span>")
                        .append("<span title='Revert text only' class='ui-icon ui-icon-pencil right'></span>")
                        .append("<span title='Revert to this version' class='ui-icon ui-icon-arrowreturnthick-1-n right'></span></div>");
                    //}
                    historyEntry.append("</div>");
                    historyEntryCollection.insert(0, historyEntry);
                    //historyEntryCollection.append(historyEntry); 
                }
                //historyEntryCollection.append(historyEntry); 
                
                historyLine.append(historyEntryCollection);
                historyLine.append("</div>");
                historyHTML.append(historyLine); //Order lines from newest to oldest.  You can change this affect by using append() instead. 
            }
        }
        out.print(historyHTML.toString());
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(GetHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(GetHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Get the transcription and parsing history for #historySplit #historyListing.  This will return the HTML for the area.  Should be called on each folio load.";
    }// </editor-fold>

}
