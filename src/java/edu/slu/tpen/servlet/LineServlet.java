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
import utils.*;

/**
 *
 * @author DrewSadler01
 */
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
    
    //A callable method that would have the framework to resolve URI and a header request to the page
    @Override
    protected void doGetUri(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            int folioID = 0;
            try {
                if (req.getHeader("Accept") != null && req.getHeader("Accept").contains("iiif/v3")) {
                folioID = parseInt(req.getPathInfo().substring(1).replace("/", ""));
                //System.out.println(req.getPathInfo().substring(1));
               // System.out.println(folioID);
                if (folioID > 0 && Folio.exists(folioID)) {
                    Folio f = new Folio(folioID);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.setHeader("Access-Control-Allow-Headers", "*");
                    resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                    resp.setHeader("Cache-Control", "max-age=15, must-revalidate");

                    if (req.getHeader("Accept") != null && req.getHeader("Accept").contains("iiif/v3")) {
                        
                        resp.setHeader("Content-Type", "application/ld+json;profile=\"http://iiif.io/api/presentation/3/context.json\"");

                        resp.getWriter().write(export(JsonHelper.buildPage(f, "v3")));
                    }
                    else {
                        resp.getWriter().write(export(JsonHelper.buildPage(f)));
                    }
                    resp.setStatus(SC_OK);
                } else {
                    getLogger(CanvasServlet.class.getName()).log(SEVERE, null, "No ID provided for canvas");
                    resp.sendError(SC_NOT_FOUND);
                }

                }
            }
     catch(NumberFormatException ex){
          getLogger(CanvasServlet.class.getName()).log(SEVERE, null, "No ID provided for canvas");
          resp.sendError(SC_NOT_FOUND);
     } 
     catch (SQLException ex) {
         Logger.getLogger(CanvasServlet.class.getName()).log(Level.SEVERE, null, ex);
     }
 }

    /**
     * Handles the HTTP <code>PUT</code> method, updating a project from a plain
     * JSON serialisation.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       doGet(req, resp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletInfo() {
        return "T-PEN Canvas Dereferencer";
    }
    

    private static final Logger LOG = getLogger(CanvasServlet.class.getName());
    
    private String export(JSONObject data) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(data);
   }
    
 
    private String export(Map<String, Object> data) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(data);
   }

}