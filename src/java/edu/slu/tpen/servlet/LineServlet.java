/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.slu.tpen.servlet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import net.sf.json.JSONObject;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import utils.*;

/**
 *
 * @author DrewSadler01
 */


///// use web.xml to make header areas
public class LineServlet extends HttpServlet{
    
        /**
     * Handles the HTTP <code>GET</code> method, returning a JSON-LD
     * serialisation of the requested T-PEN canvas.
     * If <code>Accept</code> header in the request is specified
     * and contains <code>iiif/v3</code>, returns Presentation v3.0 serialisation.
     * Otherwise, returns Presentation v2.1 serialisation.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    //Override HTTPServlet's doGet Function for line specific function
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            int lineID = 0;
            int projectID = 0;
            try {
                String[] URLreq = req.getPathInfo().substring(1).split("/");
                lineID = parseInt(URLreq[0]);
                JSONObject hello = new JSONObject();
                hello.accumulate("hello", "everyone");            
                resp.setContentType("application/json; charset=UTF-8");
                resp.setHeader("Access-Control-Allow-Headers", "*");
                resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                resp.setHeader("Cache-Control","no-cache, no-store, must-revalidate,max-age=15"); // HTTP 1.1.");
                resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                resp.setHeader("Expires", "0"); // Proxies.
                resp.getWriter().write(hello.toString());

            /*    
            if (req.getHeader("Accept") != null && req.getHeader("Accept").contains("iiif/v3")) {
                 // Mint a Presentation API 3 Annotation
                //Replace foloio with word line, create a line.Exist methods
                
                //System.out.println(req.getPathInfo().substring(1));
               // System.out.println(folioID);
                if (lineID > 0 && Folio.exists(lineID)) {
                    // Work on this
                    Folio f = new Folio(lineID);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.setHeader("Access-Control-Allow-Headers", "*");
                    resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                    resp.setHeader("Cache-Control","no-cache, no-store, must-revalidate,max-age=15"); // HTTP 1.1.");
                    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                    resp.setHeader("Expires", "0"); // Proxies.
                    switch (req.getServletPath().substring(1)) {
                        case "annotations":
                            if (URLreq.length == 3 && URLreq[1].equals("project")) {
                                String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
                                String pageID = getRbTok("SERVERURL")+"annotations/"+f.getFolioNumber();
                                projectID = parseInt(URLreq[2]);
                                resp.getWriter().write(export(JsonHelper.buildAnnotationPage(projectID, f, pageID, canvasID)));
                            } else {
                                resp.sendError(SC_NOT_FOUND);
                            }
                            break;
                        case "annotationpage":
                            resp.getWriter().write(export(JsonHelper.buildAnnotationPage(-1, f)));
                            break;
                        default:
                            resp.sendError(SC_NOT_FOUND);
                            break;
                    }
                    resp.setStatus(SC_OK);
                } 
                else {
                    getLogger(LineServlet.class.getName()).log(SEVERE, null, "No ID provided for line");
                    resp.sendError(SC_NOT_FOUND);
                }

            }
            */
            // build version 2 API here
        }
        catch(NumberFormatException ex){
             getLogger(LineServlet.class.getName()).log(SEVERE, null, "No ID provided for line");
             resp.sendError(SC_NOT_FOUND);
             System.out.println(ex);
        } 
//        catch (SQLException ex) {
//            Logger.getLogger(LineServlet.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println(ex);
//        }
    }
 
    
    private String export(Map<String, Object> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(data);
    }
}

