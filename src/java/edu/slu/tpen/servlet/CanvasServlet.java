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
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.FolioDims;
import textdisplay.TagButton;


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
                    Logger.getLogger(CanvasServlet.class.getName()).log(Level.SEVERE, null, "No ID provided for canvas");
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException | SQLException | IOException ex) {
                Logger.getLogger(CanvasServlet.class.getName()).log(Level.SEVERE, null, ex);
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
      String[] otherContent;
      FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
      Dimension storedDims = null;
      int canvasWidth = 0;
      int canvasHeight = 0;
      if (pageDim.getImageHeight() <= 0) { //There was no foliodim entry
          storedDims = ImageCache.getImageDimension(f.getFolioNumber());
         if(null == storedDims || storedDims.height <=0){ //There was no imagecache entry, or there was a bad one we can't use
            storedDims = f.getImageDimension(); //Resolve the image headers and get the image dimensions
         }
      }
      //Logger.getLogger(CanvasServlet.class.getName()).log(Level.INFO, "pageDim={0}", pageDim);
      JSONObject result = new JSONObject();
      result.element("@id", canvasID);
      result.element("@type", "sc:Canvas");
      result.element("label", f.getPageName());
      
      JSONArray images = new JSONArray();
      JSONObject imageAnnot = new JSONObject();
      imageAnnot.element("@type", "oa:Annotation");
      imageAnnot.element("motivation", "sc:painting");
      Map<String, Object> imageResource_map = buildQuickMap("@id", String.format("%s%s", Folio.getRbTok("SERVERURL"), f.getImageURLResize()), "@type", "dctypes:Image", "format", "image/jpeg");
      JSONObject imageResource = JSONObject.fromObject(imageResource_map);
      imageResource.element("height",0 ); 
      imageResource.element("width",0 ); 
      
      if (storedDims != null) {//Then we were able to resolve image headers and we have good values to run this code block
            if(storedDims.height > 0){//The image header resolved to 0, so actually we have bad values.
                if(pageDim.getImageHeight() <= 0){ //There was no foliodim entry, so make one.
                    //generate canvas values for foliodim
                    canvasHeight = 1000;
                    canvasWidth = storedDims.width * canvasHeight / storedDims.height; 
                    FolioDims.createFolioDimsRecord(storedDims.width, storedDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
                }
            }
            else{ //We were unable to resolve the image or for some reason it is 0, we must continue forward with values of 0
                canvasHeight = 0;
                canvasWidth = 0;
            }
      }
      //We will return 0 for the values here no matter what so that the object returned is valid IIIF from this servlet.
      //We could do the same check that JsonLDExporter does and return the object without the height and width so it is invalid (on purpose).
      result.element("width", canvasWidth);
      result.element("height", canvasHeight);
      imageResource.element("height",0 ); 
      imageResource.element("width",0 ); 
      
      imageAnnot.element("resource", imageResource);
      imageAnnot.element("on", canvasID);
      images.add(imageAnnot);
      //FIXME this looks for data on anno store.  Cannot build list this way for canvas servlet while not looking to anno store.  
      otherContent = Canvas.getAnnotationListsForProject(-1, canvasID, 0);
      //otherContent = Canvas.getLinesForProject(projID, canvasID, f.getFolioNumber(), u.getUID());
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
