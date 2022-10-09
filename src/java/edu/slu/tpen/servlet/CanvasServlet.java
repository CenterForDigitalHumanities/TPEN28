/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.tpen.entity.Image.Canvas.getAnnotationListsForProject;
import static edu.slu.tpen.entity.Image.Canvas.getLinesForProject;
import static edu.slu.util.LangUtils.buildQuickMap;
import static imageLines.ImageCache.getImageDimension;
import java.awt.Dimension;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static net.sf.json.JSONObject.fromObject;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.FolioDims;
import static textdisplay.FolioDims.createFolioDimsRecord;
import user.User;


/**
 *
 * @author bhaberbe
 */
public class CanvasServlet extends HttpServlet{
    /**
     * Handles the HTTP <code>GET</code> method, returning a JSON-LD
     * serialisation of the requested T-PEN project.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //System.out.println("Get a canvas");
            int folioID = 0;
            try {
                folioID = parseInt(req.getPathInfo().substring(1).replace("/", ""));
                //System.out.println(req.getPathInfo().substring(1));
               // System.out.println(folioID);
                if (folioID > 0) {
                    Folio f = new Folio(folioID);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.setHeader("Access-Control-Allow-Headers", "*");
                    resp.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                    resp.setHeader("Cache-Control", "max-age=15, must-revalidate");
                    User u = new User("asd");
                    resp.getWriter().write(export(buildPage(folioID,"canvas", f, u)));
                    resp.setStatus(SC_OK);
                } else {
                    getLogger(CanvasServlet.class.getName()).log(SEVERE, null, "No ID provided for canvas");
                    resp.sendError(SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                getLogger(CanvasServlet.class.getName()).log(SEVERE, null, ex);
                throw new ServletException(ex);
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
    
    /*
        build the JSON representation of a canvas and return it.  It will not know about the project, so otherContent will contains all annotation lists this canvas
        has across all projects.  It will ignore all user checks so as to be open.  
    */
     private JSONObject buildPage(int projID, String projName, Folio f, User u) throws SQLException, IOException {

     try{
            String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
            FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
            Dimension storedDims = null;

            JSONArray otherContent;
            if (pageDim.getImageHeight() <= 0) { //There was no foliodim entry
               storedDims = getImageDimension(f.getFolioNumber());
               if(null == storedDims || storedDims.height <=0){ //There was no imagecache entry or a bad one we can't use
                  // System.out.println("Need to resolve image headers for dimensions");
                  storedDims = f.getImageDimension(); //Resolve the image headers and get the image dimensions
               }
            }

            LOG.log(INFO, "pageDim={0}", pageDim);
            //Map<String, Object> result = new LinkedHashMap<>();
            JSONObject result  = new JSONObject();
            result.put("@id", canvasID);
            result.put("@type", "sc:Canvas");
            result.put("label", f.getPageName());
            int canvasHeight = pageDim.getCanvasHeight();
            int canvasWidth = pageDim.getCanvasWidth();
            if (storedDims != null) {//Then we were able to resolve image headers and we have good values to run this code block
                  if(storedDims.height > 0){//The image header resolved to 0, so actually we have bad values.
                      if(pageDim.getImageHeight() <= 0){ //There was no foliodim entry, so make one.
                          //generate canvas values for foliodim
                          canvasHeight = 1000;
                          canvasWidth = storedDims.width * canvasHeight / storedDims.height; 
                          //System.out.println("Need to make folio dims record");
                          createFolioDimsRecord(storedDims.width, storedDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
                      }
                  }
                  else{ //We were unable to resolve the image or for some reason it is 0, we must continue forward with values of 0
                      canvasHeight = 0;
                      canvasWidth = 0;
                  }
            }
            else{ //define a 0, 0 storedDims
                storedDims = new Dimension(0,0);
            }
            result.put("width", canvasWidth);
            result.put("height", canvasHeight);
            List<Object> images = new ArrayList<>();
            Map<String, Object> imageAnnot = new LinkedHashMap<>();
            imageAnnot.put("@type", "oa:Annotation");
            imageAnnot.put("motivation", "sc:painting");
            String imageURL = f.getImageURL();
            if (imageURL.startsWith("/")) {
                imageURL = String.format("%spageImage?folio=%s",getRbTok("SERVERURL"), f.getFolioNumber());
            }
            Map<String, Object> imageResource = buildQuickMap("@id", imageURL, "@type", "dctypes:Image", "format", "image/jpeg");

            if (storedDims.height > 0) { //We could ignore this and put the 0's into the image annotation
                //doing this check will return invalid images because we will not include height and width of 0.
               imageResource.put("height", storedDims.height ); 
               imageResource.put("width", storedDims.width ); 
            }
            imageAnnot.put("resource", imageResource);
            imageAnnot.put("on", canvasID);
            images.add(imageAnnot);
            //If this list was somehow stored in the SQL DB, we could skip calling to the store every time.
            //System.out.println("Get otherContent");
            //System.out.println(projID + "  " + canvasID + "  " + f.getFolioNumber() + "  " + u.getUID());
            otherContent = getLinesForProject(projID, canvasID, f.getFolioNumber(), u.getUID()); //Can be an empty array now.
            //System.out.println("Finalize result");
            result.put("otherContent", otherContent);
            result.put("images", images);
            //System.out.println("Return");
            return result;
        }
        catch(Exception e){
            //Map<String, Object> empty = new LinkedHashMap<>();
            JSONObject empty = new JSONObject();
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return empty;
        }
   }
    
    private String export(JSONObject data) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(data);
   }

}
