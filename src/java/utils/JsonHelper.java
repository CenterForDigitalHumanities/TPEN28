package utils;


import static edu.slu.tpen.entity.Image.Canvas.*;
import static edu.slu.util.LangUtils.buildQuickMap;
import static imageLines.ImageCache.getCachedImageDimensions;
import java.awt.Dimension;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.FolioDims;
import static textdisplay.FolioDims.createFolioDimsRecord;
import static textdisplay.FolioDims.updateFolioDimsRecord;
import user.User;


/**
 *
 * @author markskroba
 */
public class JsonHelper {
    private static final Logger LOG = getLogger(JsonHelper.class.getName());

    public static HashMap buildNoneLanguageMap(String label)
    {
        HashMap<String, String[]> languageMap = new LinkedHashMap();
        String[] noneMap = new String[] {label};
        languageMap.put("none", noneMap);
        return languageMap;
    }

    public static HashMap buildLanguageMap(String language, String label)
    {
	HashMap<String, String[]> languageMap = new LinkedHashMap();
        String[] noneMap = new String[] {label};
        languageMap.put(language, noneMap);
        return languageMap;
    }
    
        public static JSONObject buildLanguageMapOtherContent(String language, String label)
    {
	JSONObject languageMap = new JSONObject();
        languageMap.put(language,label+ " List");
        return languageMap;
    }
    
    /**
    *  Make a hashmap with old @type from v2 API being keys and new type and format fields being values
     *  to use in buildAnnotationResource
    * */
    private static HashMap formatTextualBody()
    {
        HashMap<String, HashMap> map = new HashMap();
        
        HashMap<String, String> textMap = new LinkedHashMap();
        textMap.put("type", "TextualBody");
        textMap.put("format", "text/plain");
        map.put("cnt:ContentAsText", textMap);
		
        return map;

    }

    /**
     * Make a body property for annotations according to IIIF v3 API using @type and value from v2
     * works based on map build by formatTextualBody() and throws NoSuchElementException if type was not found among keys
     * */
    public static HashMap buildAnnotationBody(String type, String value) throws NoSuchElementException
    {
        HashMap body = (HashMap) formatTextualBody().get(type);
        if (body == null)
        {
            throw new NoSuchElementException(type + " was not found among supported types: " + formatTextualBody().keySet().toString());
        }
        body.put("language", new String[] {"none"});
        body.put("value", value);
        return body;
    }

    public static HashMap buildAnnotationBody(String type, String value, String purpose) throws NoSuchElementException
    {
        HashMap body = buildAnnotationBody(type, value);
        body.put("purpose", purpose);
        return body;
    }
    
    /**
     * Build a JSON representation of a single AuthCookieService1 service,
     * used in version 3 manifests. Doing it here instead of in JSONLDExporter
     * makes it easier to access for version 3 canvases, that need for annotations.
     * 
     * @return services
     */
    public static Map<String, Object> buildServices() {
        Map<String, Object> services = new LinkedHashMap<>();
        services.put("@context","http://iiif.io/api/auth/1/context.json");
        services.put("id","https://t-pen.org/TPEN/login.jsp");
        services.put("type","AuthCookieService1");
        services.put("profile", "http://iiif.io/api/auth/1/login");
        services.put("label", "T-PEN Login");
        services.put("header", "Login for image access");
        services.put("description", "Agreement requires an open T-PEN session to view images");
        services.put("confirmLabel", "Login");
        services.put("failureHeader", "T-PEN Login Failed");
        services.put("failureDescription","<a href=\"https://t-pen.org/TPEN/about.jsp\">Read Agreement</a>");

        Map<String, Object> logout = new LinkedHashMap<>();
        logout.put("@id", "https://t-pen.org/TPEN/login.jsp");
        logout.put("profile", "http://iiif.io/api/auth/1/logout");
        logout.put("label", "End T-PEN Session");
        services.put("service",new Object[] { logout });
        return services;
    }
    
    /**
     * Build an AnnotationPage containing only textual annotations from a specific project and folio
     * @param projID
     * @param pageID
     * @param canvasID
     * @param f
     * @return
     * @throws IOException
     * @throws SQLException 
     */
    public static Map<String, Object> buildAnnotationPage(int projID, Folio f, String pageID, String canvasID) 
        throws IOException, SQLException {
        try {
            Map<String, Object> annotationPage = new LinkedHashMap<>();
            annotationPage.put("id", pageID + "/project/" + projID);
            annotationPage.put("type", "AnnotationPage");
            annotationPage.put("items", getAnnotationLinesForAnnotationPage(projID, canvasID, f.getFolioNumber()));
            return annotationPage;
        } catch (Exception e) {
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return new JSONObject();   
        }
    }
    
    /**
     * Build an AnnotationPage containing only painting annotation from a specific project and folio
     * 
     * @param projID
     * @param pageID
     * @param canvasID
     * @param f
     * @param imageDims
     * @return
     * @throws IOException
     * @throws SQLException 
     */
    public static Map<String, Object> buildAnnotationPage(int projID, Folio f) 
        throws IOException, SQLException {
        try {
            if (projID == -1) {
                ArrayList<Integer> projIDs = getProjIDFromFolio(f.getFolioNumber());
                projID = projIDs.get(0);
            }
            Dimension imageDims = null;
            String paintingPageID = getRbTok("SERVERURL")+"annotationpage/"+f.getFolioNumber();
            FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
            if (pageDim.getImageHeight() <= 0) { //There was no foliodim entry
            imageDims = getCachedImageDimensions(f.getFolioNumber());
                if(null == imageDims || imageDims.height <=0) { //There was no imagecache entry or a bad one we can't use
                    imageDims = f.resolveImageForDimensions(); //Resolve the image headers and get the image dimensions
                }
            }
            
            else{ //define a 0, 0 imageDims
                imageDims = new Dimension(0,0);
            }
            Map<String, Object> annotationPage = new LinkedHashMap<>();
            annotationPage.put("id", paintingPageID);
            annotationPage.put("type", "AnnotationPage");
            annotationPage.put("items", getPaintingAnnotations(projID, f, imageDims, pageDim));
            return annotationPage;
            
            
        } catch (Exception e) {
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return new JSONObject();   
        }
    }
    
    /**
    * Get the map which contains the serialisable information for the given
    * page using version 2 presentation standard.
    *   
    * @param projID the project whose folio is to be exported, -1 for folios from all projects
    * @param f the folio to be exported
    * @param u the current user in session
    * @return a map containing the relevant info, suitable for Jackson
    * serialisation
    */
    public static Map<String, Object> buildPage(int projID, Folio f, User u) throws SQLException, IOException {
      
        try{
            String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
                    
            // Wondering if foliodim alone means we don't need to access imagecache here??
            // See if we have an entry cached for all the Canvas and Image size data
            FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
            // Initial check the image cache table for this image.
            Dimension imageDims = getCachedImageDimensions(f.getFolioNumber());
            JSONArray otherContent;
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("@id", canvasID);
            result.put("@type", "sc:Canvas");
            result.put("label", f.getPageName());
            int canvasHeight = 0;
            int canvasWidth = 0;
            boolean updateCachedDimensions = false;
            
            // Do all the work necessary to set imageDims up front.  Only resolve the image if you have to.
            if(null == pageDim || pageDim.getImageWidth() <= 0 || pageDim.getImageHeight() <= 0){
                if(imageDims == null || imageDims.width <=0 || imageDims.height <=0){
                    // The Image dimensions are not cached.  Try to resolve the image and get them.
                    imageDims = f.resolveImageForDimensions(); 
                    if(null == imageDims || imageDims.height <=0 || imageDims.width <= 0){
                        // Upstream when this empty object is detected, it is omitted from the Canvas' Array
                        LOG.log(WARNING, "Image height and/or width was 0.  The Canvas for Folio {0} will be omitted.  See image at {1}", new Object[]{f.getFolioNumber(), f.getImageURL()});
                        return new LinkedHashMap<>();
                    }
                }
                updateCachedDimensions = true;
            }
            else{
                // pageDim has the image dimensions, let's trust them so we don't have to resolve the image.
                imageDims = new Dimension(pageDim.getImageWidth(), pageDim.getImageHeight());
            }
            
            if(null == pageDim || pageDim.getCanvasHeight() <= 0 || pageDim.getCanvasWidth() <= 0){
                // The Canvas dimensions are not cached.  We need to generate them using the image's width and height.
                canvasHeight = 1000;
                canvasWidth = imageDims.width * canvasHeight / imageDims.height; 
                updateCachedDimensions = true;
            }
            else{
                // The Canvas Dimensions are cached.
                canvasHeight = pageDim.getCanvasHeight();
                canvasWidth = pageDim.getCanvasWidth(); 
            }
            //Generate the FolioDims entry so we don't have to do this work again.  Note we can only create it, not update an existing one that may have a 0.
            if(null == pageDim || pageDim.getFolioDimsID() == -1){
                createFolioDimsRecord(imageDims.width, imageDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
            }
            else{
                if(updateCachedDimensions){
                    updateFolioDimsRecord(imageDims.width, imageDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
                }
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

            if (imageDims.height > 0 && imageDims.width > 0) { //We could ignore this and put the 0's into the image annotation
                //doing this check will return invalid images because we will not include height and width of 0.
                imageResource.put("width", imageDims.width ); 
                imageResource.put("height", imageDims.height ); 
            }
            imageAnnot.put("resource", imageResource);
            imageAnnot.put("on", canvasID);
            images.add(imageAnnot);
            // -1 is a hard-coded value from CanvasServlet call of this method
            if (projID == -1) {
                // method was called from CanvasServlet - find all projects that have a version of this folio 
                ArrayList<Integer> projIDs = getProjIDFromFolio(f.getFolioNumber());
                otherContent = new JSONArray();
                for (int i : projIDs) {
                    otherContent.add(getLinesForProject(i, canvasID, f.getFolioNumber(), u.getUID()).get(0));
                }
            } else {
                // method was called from JSONLDExporter - find the version of this folio that is used for this project
                otherContent = getLinesForProject(projID, canvasID, f.getFolioNumber(), u.getUID()); //Can be an empty array now.
            }
            result.put("otherContent", otherContent);
            result.put("images", images);
            return result;
        }
        catch(Exception e){
            Map<String, Object> empty = new LinkedHashMap<>();
            LOG.log(SEVERE, "Could not build page for canvas/{0}", f.getFolioNumber());
            LOG.log(SEVERE, Arrays.toString(e.getStackTrace()));
            return empty;
        }
   }
    
    /**
    * Get the map which contains the serialisable information for the given
    * page using version 3 presentation standard.
    *
    * @param projID the project whose folio is to be exported, -1 for folios from all projects
    * @param f the folio to be exported
    * @param u the current user in session
    * @param profile specifies that this returns a version 3 presentation canvas
    * @return a map containing the relevant info, suitable for Jackson
    * serialisation
    */
     
    public static JSONObject buildPage(int projID, Folio f, User u, String profile) throws SQLException {
        try {
            JSONObject result = new JSONObject();
            String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
            FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
            Dimension imageDims = getCachedImageDimensions(f.getFolioNumber());
            int canvasHeight = 0;
            int canvasWidth = 0;     
            boolean updateCachedDimensions = false;
            // Do all the work necessary to set imageDims up front.  Only resolve the image if you have to.
            if(null == pageDim || pageDim.getImageWidth() <= 0 || pageDim.getImageHeight() <= 0){
                if(imageDims == null || imageDims.width <=0 || imageDims.height <=0){
                    // The Image dimensions are not cached.  Try to resolve the image and get them.
                    imageDims = f.resolveImageForDimensions(); 
                    if(null == imageDims || imageDims.height <=0 || imageDims.width <= 0){
                        // Upstream when this empty object is detected, it is omitted from the Canvas' Array
                        LOG.log(WARNING, "Image height and/or width was 0.  The Canvas for Folio {0} will be omitted.  See image at {1}", new Object[]{f.getFolioNumber(), f.getImageURL()});
                        return new JSONObject();
                    }
                }
                updateCachedDimensions = true;
            }
            else{
                // pageDim has the image dimensions, let's trust them so we don't have to resolve the image.
                imageDims = new Dimension(pageDim.getImageWidth(), pageDim.getImageHeight());
            }
            
            if(null == pageDim || pageDim.getCanvasHeight() <= 0 || pageDim.getCanvasWidth() <= 0){
                // The Canvas dimensions are not cached.  We need to generate them using the image's width and height.
                canvasHeight = 1000;
                canvasWidth = imageDims.width * canvasHeight / imageDims.height; 
                updateCachedDimensions = true;
            }
            else{
                // The Canvas Dimensions are cached.
                canvasHeight = pageDim.getCanvasHeight();
                canvasWidth = pageDim.getCanvasWidth(); 
            }
            //Generate the FolioDims entry so we don't have to do this work again.  Note we can only create it, not update an existing one that may have a 0.
            if(null == pageDim || pageDim.getFolioDimsID() == -1){
                createFolioDimsRecord(imageDims.width, imageDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
            }
            else{
                if(updateCachedDimensions){
                    updateFolioDimsRecord(imageDims.width, imageDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
                }
            }
            result.put("id", canvasID);
            result.put("type", "Canvas");
            result.put("label", buildNoneLanguageMap(f.getPageName()));
            JSONArray thumbnail = new JSONArray();
            JSONObject thumbObj = new JSONObject();
            String imageURL = f.getImageURL();
            if (imageURL.startsWith("/")) {
                imageURL = String.format("%spageImage?folio=%s",getRbTok("SERVERURL"), f.getFolioNumber());
            }
            thumbObj.accumulate("id", imageURL);
            thumbObj.accumulate("type", "Image");
            thumbObj.accumulate("format", "image/jpeg");
            //thumbObj.accumulate("width", 300);
            //thumbObj.accumulate("height", 200);
            thumbnail.add(thumbObj);
            result.put("thumbnail", thumbnail);
            result.put("width", canvasWidth);
            result.put("height", canvasHeight);
            String pageID = getRbTok("SERVERURL")+"annotations/"+f.getFolioNumber();
            String paintingPageID = getRbTok("SERVERURL")+"annotationpage/"+f.getFolioNumber();
            ArrayList<Integer> projIDs = getProjIDFromFolio(f.getFolioNumber());
            
            //AnnotationPage that contains painting annotations - should be under `items`
            Map<String, Object> itemsPage;
            if (projID == -1) {
                itemsPage = buildAnnotationPage(projIDs.get(0), f);
            } else {
                itemsPage = buildAnnotationPage(projID, f);
            } 
            result.put("items", Arrays.asList(itemsPage));
            
            //AnnotationPage that contains external annotations - should be under `annotations`	
            JSONArray annotations = new JSONArray();
            
            // -1 is a hard-coded value from CanvasServlet call of this method
            if (projID == -1) {
                // method was called from CanvasServlet - find all projects that have a version of this folio  
                for (int id : projIDs) {
                    Map<String, Object> annotationPage = buildAnnotationPage(id, f, pageID, canvasID);
                    annotationPage.put("projectId", id);
                    annotations.add(annotationPage);
                }
            } else {
                // method was called from JSONLDExporter - find the version of this folio that is used for this project
                Map<String, Object> annotationPage = buildAnnotationPage(projID, f, pageID, canvasID);
                annotations.add(annotationPage);
            }
           
            result.put("annotations", annotations);
            return result;
        }
        catch (Exception e)
        {
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return new JSONObject();
        }
    }
    
    public static ArrayList getProjIDFromFolio(final int folioNumber) throws SQLException, IOException {
        final String query = "select project from projectfolios where folio=?";
        int projID = 0, size = 0;
        ArrayList<Integer> projIDs = new ArrayList<>();
        Connection j = null;
        PreparedStatement ps = null;
        
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, folioNumber);
            final ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                projID = rs.getInt("project");
				projIDs.add(projID);
                size++;
            }
            
			return projIDs;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

}

