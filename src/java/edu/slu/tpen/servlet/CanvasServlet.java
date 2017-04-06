/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static textdisplay.Transcription.LOG;
import java.awt.Dimension;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import edu.slu.tpen.entity.Image.Canvas;
import imageLines.ImageCache;
import textdisplay.Folio;
import static edu.slu.util.LangUtils.buildQuickMap;
import javax.servlet.http.HttpServlet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.FolioDims;


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
                folioID = Integer.parseInt(req.getPathInfo().substring(1).replace("/", ""));
                //System.out.println(req.getPathInfo().substring(1));
               // System.out.println(folioID);
                if (folioID > 0) {
                    Folio f = new Folio(folioID);
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write(export(buildPage(f)));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
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
    private JSONObject buildPage(Folio f) throws SQLException, IOException {
      Integer msID = f.getMSID();
      String msID_str = msID.toString();
      String canvasID = Folio.getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();  
      Dimension pageDim = ImageCache.getImageDimension(f.getFolioNumber());//Try to get image dimensions from the imagecache table
      String[] otherContent;
      FolioDims storedDims = new FolioDims(f.getFolioNumber());
      if (pageDim == null) {
         
         pageDim = storedDims.getNaturalImageDimensions(); //Try to get image dimensions from the foliodim table
         if(pageDim.height == 0){
            //LOG.log(Level.INFO, "Image for {0} not found in cache, loading image...", f.getFolioNumber());
            pageDim = f.getImageDimension(); //Resolve the image headers and get the image dimensions
         }
      }
      LOG.log(Level.INFO, "pageDim={0}", pageDim);

      JSONObject result = new JSONObject();
      result.element("@id", canvasID);
      result.element("@type", "sc:Canvas");
      result.element("label", f.getPageName());
      int canvasHeight = 1000;
      result.element("height", canvasHeight);
      if (pageDim != null) {
         int canvasWidth = pageDim.width * canvasHeight / pageDim.height;  // Convert to canvas coordinates.
         result.element("width", canvasWidth);
      }
      JSONArray images = new JSONArray();
      JSONObject imageAnnot = new JSONObject();
      imageAnnot.element("@type", "oa:Annotation");
      imageAnnot.element("motivation", "sc:painting");
      Map<String, Object> imageResource_map = buildQuickMap("@id", String.format("%s%s", Folio.getRbTok("SERVERURL"), f.getImageURLResize()), "@type", "dctypes:Image", "format", "image/jpeg");
      JSONObject imageResource = JSONObject.fromObject(imageResource_map);
      if (pageDim != null) {
         imageResource.element("height", pageDim.height ); 
         imageResource.element("width", pageDim.width ); 
      }
      imageAnnot.element("resource", imageResource);
      imageAnnot.element("on", canvasID);
      images.add(imageAnnot);
      otherContent = Canvas.getAnnotationListsForProject(-1, canvasID, 0);
      //it seems like it wants me to do Arrays.toString(otherContent), but then it is not formatted correctly.  
      result.element("otherContent", JSONArray.fromObject(otherContent));
      result.element("images", images);
      return result;
   }
    
    private String export(JSONObject data) throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(data);
   }

}
