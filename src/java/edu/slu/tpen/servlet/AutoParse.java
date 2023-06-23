/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import static edu.slu.util.LangUtils.buildQuickMap;
import static imageLines.ImageCache.getCachedImageDimensions;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import net.sf.json.JSONArray;
import static net.sf.json.JSONArray.fromObject;
import net.sf.json.JSONObject;
import static org.owasp.esapi.ESAPI.encoder;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.FolioDims;
import textdisplay.Line;
import textdisplay.Project;
import textdisplay.Transcription;

/**
 *
 * @author bhaberbe
 */
public class AutoParse extends HttpServlet {

    /**
     * Fire the auto parser on a given folio. Return a stringified JSON
     * sc:AnnotationList of the lines created.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @throws java.sql.SQLException
     * @respond with array of Transcription lines created from auto parsing.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
        int projectID = parseInt(request.getParameter("projectID"));
        int folioNumber = parseInt(request.getParameter("folioNumber"));
        Stack<Transcription> orderedTranscriptions = new Stack();
        JSONObject annotationList = new JSONObject();
        JSONArray resources_array;
        List<Object> resources = new ArrayList<>();
        String dateString;
        String annoListID = getRbTok("SERVERURL") + "project/" + projectID + "/annotations/" + folioNumber;
        String canvasID = getRbTok("SERVERURL") + "canvas/" + folioNumber;
        //create Transcription(s) based on Project settings and Line parsing
        Project p = new Project(projectID);
        Project.imageBounding preferedBounding = p.getProjectImageBounding();
        FolioDims pageDim = null;
        Dimension imageDims = null;       
        if (null != preferedBounding) {
            switch (preferedBounding) {
                case fullimage: {
                    //find the image size, add 1 Transcription to cover the entirety, and done
                    int height = 1000;
                    Folio f = new Folio(folioNumber, true);
                    pageDim = new FolioDims(f.getFolioNumber(), true);
                    imageDims = getCachedImageDimensions(f.getFolioNumber());
                    if(null == imageDims || imageDims.height <=0 || imageDims.width <=0) { //There was no imagecache entry or a bad one we can't use
                        // System.out.println("Need to resolve image headers for dimensions");
                        if (pageDim != null && pageDim.getImageHeight() > 1 && pageDim.getImageWidth() > 1) { //There was no foliodim entry
                            imageDims = new Dimension(pageDim.getImageWidth(), pageDim.getImageHeight());
                        }
                        else{
                            try{
                                imageDims = f.resolveImageForDimensions(); 
                            }
                            catch (java.net.SocketTimeoutException e) {
                                // There was a timeout on the Image URL.  We could not resolve the image for dimensions.
                                // We can't parse it, the canvas will be 1000, 1000.  Lets make a column that is 1000, 1000.
                                //response.sendError(502, "Timeout.  Could not resolve imageURL to AutoParse "+f.getImageURL());
                                LOG.log(WARNING, "AutoParser could not load image {0}.  A column of 1000, 1000 will be used.", f.getImageURL());
                                imageDims = new Dimension(1000, 1000);
                            }
                            catch (Exception e) {
                                // There was an unexpected issue resolving the image
                                LOG.log(WARNING, "AutoParser could not load image {0}.  A column of 1000, 1000 will be used.", f.getImageURL());
                                imageDims = new Dimension(1000, 1000);
                            }
                        }
                    }
                    int width = imageDims.width;
                    Rectangle r = new Rectangle(0, 0, height, width);
                    Transcription t = new Transcription(0, projectID, folioNumber, "", "", r);
                    orderedTranscriptions.add(t);
                    break;
                }
                case columns: {
                    //run the image parsing and make a Transcription for each column
                    Folio f = new Folio(folioNumber, true);
                    Line[] lines = f.getlines();
                    int x = 0;
                    int y = 0;
                    int w = 0;
                    for (int i = 0; i < lines.length; i++) {
                        if (lines[i].getWidth() != w) {
                            if (w != 0 && i != 0) {
                                Rectangle r = new Rectangle(x, y, lines[i].getBottom(), w);
                                // TODO: create an agent for the application here, loaded from version.properties.
                                Transcription t = new Transcription(0, projectID, folioNumber, "", "", r);
                                orderedTranscriptions.add(t);
                            }
                            w = lines[i].getWidth();
                            x = lines[i].getLeft();
                            y = lines[i].getTop();
                        }
                    }
                    break;
                }
                case lines: {
                    Folio f = new Folio(folioNumber, true);
                    pageDim = new FolioDims(f.getFolioNumber(), true);
                    imageDims = getCachedImageDimensions(f.getFolioNumber());
                    int height = 1000;
                    int width = 1000;
                    Line[] lines = {};
                    Rectangle r = null;
                    Transcription fullPage = null;
                    if(null == imageDims || imageDims.height <=0 || imageDims.width<=0) { //There was no imagecache entry or a bad one we can't use
                        // System.out.println("Need to resolve image headers for dimensions");
                        if (pageDim != null && pageDim.getImageHeight() > 1 && pageDim.getImageWidth() > 1) { //There was no foliodim entry
                            imageDims = new Dimension(pageDim.getImageWidth(), pageDim.getImageHeight());
                        }
                        else{
                            try{
                                imageDims = f.resolveImageForDimensions(); 
                                lines = f.getlines();
                            }
                            catch (java.net.SocketTimeoutException e) {
                                // There was a timeout on the Image URL.  We could not resolve the image for dimensions.
                                // We can't parse it, the canvas will be 1000, 1000.  Lets make a column that is 1000, 1000.
                                //response.sendError(502, "Timeout.  Could not resolve imageURL to AutoParse "+f.getImageURL());
                                LOG.log(WARNING, "AutoParser could not load image {0}.  A single column of 1000, 1000 will be used.", f.getImageURL());
                                imageDims = new Dimension(1000, 1000);
                            }
                            catch (Exception e) {
                                // There was an unexpected issue resolving the image
                                LOG.log(WARNING, "AutoParser could not load image {0}.  A column of 1000, 1000 will be used.", f.getImageURL());
                                imageDims = new Dimension(1000, 1000);
                            }
                        }
                    }
                    //make a Transcription for each Line
                    for (Line line : lines) {
                        r = new Rectangle(line.getLeft(), line.getTop(), line.getHeight(), line.getWidth());
                        // TODO: create an agent for the application here, loaded from version.properties.
                        Transcription t = new Transcription(0, projectID, folioNumber, "", "", r);
                        orderedTranscriptions.add(t);
                    }
                    if (orderedTranscriptions.isEmpty()) {
                        width = imageDims.width;
                        r = new Rectangle(0, 0, height, width);
                        fullPage = new Transcription(0, projectID, folioNumber, "", "", r);
                        orderedTranscriptions.add(fullPage);
                    }
                    break;
                }
                case none:
                default:
                    break;
            }
        }
        for (int i = 0; i < orderedTranscriptions.size(); i++) {
            if (orderedTranscriptions.get(i) != null) {
                int lineID = orderedTranscriptions.get(i).getLineID();
                Map<String, Object> lineAnnot = new LinkedHashMap<>();
                String lineURI = "line/" + lineID;
                String annoLineID = getRbTok("SERVERURL") + "line/" + lineID;
                lineAnnot.put("@id", annoLineID);
                lineAnnot.put("_tpen_line_id", lineURI);
                lineAnnot.put("@type", "oa:Annotation");
                lineAnnot.put("motivation", "oad:transcribing");
                lineAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", encoder().decodeForHTML(orderedTranscriptions.get(i).getText())));
                lineAnnot.put("on", format("%s#xywh=%d,%d,%d,%d", canvasID, orderedTranscriptions.get(i).getX(), orderedTranscriptions.get(i).getY(), orderedTranscriptions.get(i).getWidth(), orderedTranscriptions.get(i).getHeight()));
                if (null != orderedTranscriptions.get(i).getComment() && !"null".equals(orderedTranscriptions.get(i).getComment())) {
                    lineAnnot.put("_tpen_note", orderedTranscriptions.get(i).getComment());
                } else {
                    lineAnnot.put("_tpen_note", "");
                }
                lineAnnot.put("_tpen_creator", orderedTranscriptions.get(i).getCreator());
                dateString = orderedTranscriptions.get(i).getDate().toString();
                lineAnnot.put("modified", dateString);
                resources.add(lineAnnot);
            } else {
                getLogger(AutoParse.class.getName()).log(WARNING, "Lines for list was null in project {0}, folio {1}", new Object[]{projectID, folioNumber});
            }
        }
        resources_array = fromObject(resources);
        annotationList.element("resources", resources_array);
        annotationList.element("@id", annoListID);

        getLogger(AutoParse.class.getName()).log(INFO, "Autoparse succeeded for project {0}, folio {1} with {2} lines.", new Object[]{projectID, folioNumber, resources.size()});

        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(SC_CREATED);
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
        } catch (SQLException ex) {
            getLogger(AutoParse.class.getName()).log(SEVERE, null, ex);
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
            getLogger(AutoParse.class.getName()).log(SEVERE, null, ex);
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
    
    private static final Logger LOG = getLogger(Folio.class.getName());

}
