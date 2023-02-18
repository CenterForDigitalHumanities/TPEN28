package utils;


import static edu.slu.tpen.entity.Image.Canvas.*;
import edu.slu.tpen.transfer.JsonLDExporter;
import static edu.slu.util.LangUtils.buildQuickMap;
import static imageLines.ImageCache.getImageDimension;
import java.awt.Dimension;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.FolioDims;
import static textdisplay.FolioDims.createFolioDimsRecord;
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
    * Get the map which contains the serialisable information for the given
    * page.
    *
    * @param f the folio to be exported
    * @return a map containing the relevant info, suitable for Jackson
    * serialisation
    */
    public static Map<String, Object> buildPage(int projID, String projName, Folio f, User u) throws SQLException, IOException {
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
            Map<String, Object> result = new LinkedHashMap<>();
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
            Map<String, Object> empty = new LinkedHashMap<>();
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return empty;
        }
   }
    
	
    
    /*
        build the JSON representation of a canvas and return it.  It will not know about the project, so otherContent will contains all annotation lists this canvas
        has across all projects.  It will ignore all user checks so as to be open.  
    */

     public static Map<String, Object> buildPage(Folio f) throws SQLException, IOException {
         
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

            Map<String, Object> result  = new LinkedHashMap();
            //Map<String, Object> result = new LinkedHashMap<>();
            result.put("@context","http://iiif.io/api/presentation/2/context.json");
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
            //System.out.println(projID + "  " + canvasID + "  " + f.getFolioNumber() + "  " + u.getUID());;
            //otherContent = getAnnotationListsForProject(-, canvasID, 0);
//            otherContent = getLinesForProject(projID, canvasID, f.getFolioNumber(), 0); //Can be an empty array now.

            //System.out.println("Finalize result");
            //result.put("otherContent", otherContent);
            result.put("images", images);
            //System.out.println("Return");
            return result;
        }
        catch(Exception e){
            //Map<String, Object> empty = new LinkedHashMap<>();

            Map<String, Object> empty = new LinkedHashMap();
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return empty;
        }
   }

   /**
    * Builds the JSON representation of canvas according to presentation 3 standard and returns it
    * */
    
    
    /**
    * Get the map which contains the serialisable information for the given
    * page.
    *
    * @param f the folio to be exported
    * @return a map containing the relevant info, suitable for Jackson
    * serialisation
    */
     
     
    public static Map<String, Object> buildPage(int projID, String projName, Folio f, User u, String profile) throws SQLException
	{
	try 	{
                System.out.println("v3 buildpage");
		Map<String, Object> result = new LinkedHashMap<>();
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

		result.put("id", canvasID);
		result.put("type", "Canvas");
		result.put("label", buildNoneLanguageMap(f.getPageName()));
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
		String pageID = getRbTok("SERVERURL")+"annotations/"+f.getFolioNumber();
		//AnnotationPage that contains painting annotations - should be under `items`
		Map<String, Object> itemsPage = new LinkedHashMap<>();
		itemsPage.put("id", pageID);
		itemsPage.put("type", "AnnotationPage");
                itemsPage.put("items", getPaintingAnnotations(storedDims, f));
		itemsPage.put("label", buildNoneLanguageMap(canvasID + " List"));
		itemsPage.put("target", canvasID);
		result.put("items", itemsPage);
		//AnnotationPage that contains external annotations - should be under `annotations`
		Map<String, Object> annotationsPage = new LinkedHashMap<>();
		annotationsPage.put("id", pageID);
		annotationsPage.put("type", "AnnotationPage");
                annotationsPage.put("items", getAnnotationLinesForAnnotationPage(projID,canvasID,f.getFolioNumber(),u.getUID(),profile));
		annotationsPage.put("label", buildNoneLanguageMap(canvasID + " List"));
		annotationsPage.put("target", canvasID);
		result.put("annotations", annotationsPage);
                return result;
	
        }
	catch (Exception e)
	{
		Map<String, Object> empty = new LinkedHashMap<>();
		LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
		return empty;
	}
   }
     
   /**
    * Builds the JSON representation of canvas according to presentation 3 standard and returns it
    * */
    public static Map<String, Object> buildPage(Folio f, String profile) throws SQLException, IOException {
         try {
             System.out.println("v3 canvas buildpage");
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
             Map<String, Object> result  = new LinkedHashMap();
             result.put("id", canvasID);
             result.put("type", "Canvas");
             result.put("label", buildNoneLanguageMap(f.getPageName()));
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
             result.put("height", canvasHeight);
             result.put("width", canvasWidth);
             result.put("items", new HashMap());
             Map<String, Object> resultAnnotation  = new LinkedHashMap();
             result.put("annotations", new HashMap());
             System.out.println(result);
             return result;
         }
         catch (Exception e) {
             HashMap<String, Object> empty = new HashMap();
             LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
             return empty;
         }

    }
    public static Map<String, Object> buildAnnotationForManifest(int projID, String projName, Folio f, User u) throws SQLException, IOException {
        try{
            String manifestID = getRbTok("SERVERURL")+"manifest/"+f.getFolioNumber();
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
            Map<String, Object> result = new LinkedHashMap<>();
//            result.put("@context", "http://iiif.io/api/presentation/3/context.json");
            result.put("id", manifestID);
            result.put("type", "AnnotationPage");
//            result.put("label", f.getPageName());
            int canvasHeight = pageDim.getCanvasHeight();
            int canvasWidth = pageDim.getCanvasWidth();
            result.put("items", getAnnotationLinesForAnnotationPage(projID,manifestID,f.getFolioNumber(),u.getUID(),"v3"));
            if (storedDims != null) {//Then we were able to resolve image headers and we have good values to run this code block
                  if(storedDims.height > 0){//The image header resolved to 0, so actually we have bad values.
                      if(pageDim.getImageHeight() <= 0){ //There was no foliodim entry, so make one.
                          //generate canvas values for foliodim
                          canvasHeight = 1000;
                          canvasWidth = storedDims.width * canvasHeight / storedDims.height; 
                          //System.out.println("Need to make folio dims record");
                          createFolioDimsRecord(storedDims.width,  storedDims.height, canvasWidth, canvasHeight, f.getFolioNumber());
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
//            result.put("width", canvasWidth);
//            result.put("height", canvasHeight);
            List<Object> images = new ArrayList<>();
            Map<String, Object> imageAnnot = new LinkedHashMap<>();
            imageAnnot.put("type", "Annotation");
            imageAnnot.put("motivation", "commenting");
            Map<String, Object>bodyAnnot = new LinkedHashMap<>();
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
            imageAnnot.put("on", manifestID);
            images.add(imageAnnot);
            //If this list was somehow stored in the SQL DB, we could skip calling to the store every time.
            //System.out.println("Get otherContent");
            //System.out.println(projID + "  " + canvasID + "  " + f.getFolioNumber() + "  " + u.getUID());
//            otherContent = getLinesForProject(projID, manifestID, f.getFolioNumber(), u.getUID()); //Can be an empty array now.
            //System.out.println("Finalize result");
//            result.put("otherContent", otherContent);
//            result.put("images", images);
            System.out.println("Return");
            return result;
        }
        catch(Exception e){
            Map<String, Object> empty = new LinkedHashMap<>();
            LOG.log(SEVERE, null, "Could not build page for canvas/"+f.getFolioNumber());
            return empty;
        }
   }

}

