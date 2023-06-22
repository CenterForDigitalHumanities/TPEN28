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
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import utils.JsonHelper;

/**
 *
 * @author markskroba
 */
public class PageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int folioID = 0, projID = 0;    
        try {
            String[] URLParts = req.getPathInfo().substring(1).split("/");
            folioID = parseInt(URLParts[0]);
            
            if (folioID > 0 && Folio.exists(folioID)) {
                Folio f = new Folio(folioID);
                resp.setContentType("application/json; charset=UTF-8");
                resp.setHeader("Access-Control-Allow-Headers", "*");
                resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
                resp.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                resp.setHeader("Expires", "0"); // Proxies.
                
                switch (req.getServletPath().substring(1)) {
                    case "annotations":
                        if (URLParts.length == 3 && URLParts[1].equals("project")) {
                            String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
                            String pageID = getRbTok("SERVERURL")+"annotations/"+f.getFolioNumber();
                            projID = parseInt(URLParts[2]);
                            resp.getWriter().write(export(JsonHelper.buildAnnotationPage(projID, f, pageID, canvasID)));
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
            } else {
                getLogger(PageServlet.class.getName()).log(SEVERE, null, "No ID provided for canvas");
                resp.sendError(SC_NOT_FOUND);
            }

        }
        catch(NumberFormatException ex){
             getLogger(PageServlet.class.getName()).log(SEVERE, null, "No ID provided for canvas");
             resp.sendError(SC_NOT_FOUND);
             System.out.println(ex);
        } 
        catch (SQLException ex) {
            Logger.getLogger(CanvasServlet.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        }
    }
 
    
    private String export(Map<String, Object> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(data);
    }
}
