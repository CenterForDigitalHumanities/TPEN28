/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import edu.slu.tpen.entity.Image.Canvas;
import static edu.slu.util.LangUtils.buildQuickMap;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.owasp.esapi.ESAPI;
import textdisplay.Folio;
import textdisplay.Line;
import textdisplay.Project;
import textdisplay.Transcription;

/**
 *
 * @author bhaberbe
 */
public class autoParse extends HttpServlet {
    /**
     * Fire the auto parser on a given folio.  Return a stringified JSON sc:AnnotationList of the lines created.
     *
     * @param request servlet request
     * @param response servlet response
     * @respond with array of Transcription lines created from auto parsing.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        int projectID = Integer.parseInt(request.getParameter("projectID"));
        int folioNumber = Integer.parseInt(request.getParameter("folioNumber"));
        Stack<Transcription> orderedTranscriptions = new Stack();
        JSONObject annotationList = new JSONObject();
        JSONArray resources_array = new JSONArray();
        List<Object> resources = new ArrayList<>();
        String dateString = "";
        String annoListID = Folio.getRbTok("SERVERURL")+"project/"+projectID+"/annotations/"+folioNumber;  
        String canvasID = Folio.getRbTok("SERVERURL")+"canvas/"+folioNumber;
        //create Transcription(s) based on Project settings and Line parsing
        Project p = new Project(projectID);
        Project.imageBounding preferedBounding = p.getProjectImageBounding();
        if (preferedBounding == Project.imageBounding.none) {
           //do nothing
        }
        else if (preferedBounding == Project.imageBounding.fullimage) {
           //find the image size, add 1 Transcription to cover the entirety, and done
           int height = 1000;
           Folio f = new Folio(folioNumber, true);
           int width = f.getImageDimension().width;
           Transcription t = new Transcription(projectID, folioNumber, 0, 0, height, width, true);
           orderedTranscriptions.add(t);
        }
        else if (preferedBounding == Project.imageBounding.columns) {
           //run the image parsing and make a Transcription for each column
           Folio f = new Folio(folioNumber, true);
           Line[] lines = f.getlines();
           int x = 0;
           int y = 0;
           int w = 0;
           for (int i = 0; i < lines.length; i++) {
              if (lines[i].getWidth() != w) {
                 if (w != 0 && i != 0) {
                    Transcription t = new Transcription(projectID, folioNumber, x, y, lines[i].getBottom(), w, true);
                    orderedTranscriptions.add(t);
                 }
                 w = lines[i].getWidth();
                 x = lines[i].getLeft();
                 y = lines[i].getTop();
              }
           }
        }
        else if (preferedBounding == Project.imageBounding.lines) {
           Folio f = new Folio(folioNumber, true);
           Line[] lines = f.getlines();
           //make a Transcription for each Line
           for (int i = 0; i < lines.length; i++) {
              Transcription t = new Transcription(projectID, folioNumber, lines[i].getLeft(), lines[i].getTop(), lines[i].getHeight(), lines[i].getWidth(), true);
              orderedTranscriptions.add(t);
           }
           if (orderedTranscriptions.size() == 0) {
              int height = 1000;
              int width = f.getImageDimension().width;
              Transcription fullPage = new Transcription(projectID, folioNumber, 0, 0, height, width, true);
              orderedTranscriptions.add(fullPage);
           }
        }
        for (int i = 0; i < orderedTranscriptions.size(); i++) {
           if (orderedTranscriptions.get(i) != null) {  
               dateString = "";
               int lineID = orderedTranscriptions.get(i).getLineID();
               Map<String, Object> lineAnnot = new LinkedHashMap<>();
               String lineURI = "line/" + lineID;
               String annoLineID = Folio.getRbTok("SERVERURL")+"line/"+lineID;  
               lineAnnot.put("@id", annoLineID);
               lineAnnot.put("_tpen_line_id", lineURI);
               lineAnnot.put("@type", "oa:Annotation");
               lineAnnot.put("motivation", "oad:transcribing"); 
               lineAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", ESAPI.encoder().decodeForHTML(orderedTranscriptions.get(i).getText())));
               lineAnnot.put("on", String.format("%s#xywh=%d,%d,%d,%d", canvasID, orderedTranscriptions.get(i).getX(), orderedTranscriptions.get(i).getY(), orderedTranscriptions.get(i).getWidth(), orderedTranscriptions.get(i).getHeight())); 
               if(null != orderedTranscriptions.get(i).getComment() && !"null".equals(orderedTranscriptions.get(i).getComment())){
                   lineAnnot.put("_tpen_note", orderedTranscriptions.get(i).getComment());
               }
               else{
                   lineAnnot.put("_tpen_note", "");
               }
               lineAnnot.put("_tpen_creator",orderedTranscriptions.get(i).getCreator());             
               dateString = orderedTranscriptions.get(i).getDate().toString();
               lineAnnot.put("modified", dateString);
               resources.add(lineAnnot);
           }
           else{
               Logger.getLogger(Canvas.class.getName()).log(Level.INFO, null, "Lines for list was null");
           }
        }
        resources_array = JSONArray.fromObject(resources);
        annotationList.element("resources", resources_array);
        annotationList.element("@id", annoListID);
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(annotationList.toString());
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } 
        catch (SQLException ex) {
            Logger.getLogger(autoParse.class.getName()).log(Level.SEVERE, null, ex);
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
        } 
        catch (SQLException ex) {
            Logger.getLogger(autoParse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Supply a projectID and a folioNumber to fire the auto parser on.  Return the lines created by the process as a stringified JSON sc:AnnotationList";
    }

}
