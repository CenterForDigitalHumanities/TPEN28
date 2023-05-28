/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.entity.Image;

import static edu.slu.tpen.servlet.Constant.ANNOTATION_SERVER_ADDR;
import static edu.slu.util.LangUtils.buildQuickMap;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.String.format;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static java.net.URLEncoder.encode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import static net.sf.json.JSONObject.fromObject;
import static org.owasp.esapi.ESAPI.encoder;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.Transcription;
import static textdisplay.Transcription.getProjectTranscriptions;
import utils.JsonHelper;

/**
 *
 * @author hanyan
 */
public class Canvas {

    private String objectId;
    //for reference in the sc:AnnotationList
    private String id;
    private String type;
    private Integer height;
    private Integer width;
    private List<Image> ls_images;
    private List<OtherContent> ls_otherContent;

    public Canvas() {
    }

    public Canvas(String id, Integer height, Integer width, List<Image> ls_images, List<OtherContent> ls_otherContent) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.ls_images = ls_images;
        this.ls_otherContent = ls_otherContent;
    }

    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        jo.element("@id", this.id);
        jo.element("@type", this.type);
        jo.element("height", this.height);
        jo.element("width", this.width);
        JSONArray ja_images = new JSONArray();
        for (Image i : ls_images) {
            ja_images.add(i.toJSON());
        }
        jo.element("images", ja_images);
        JSONArray ja_otherContent = new JSONArray();
        for (OtherContent oc : ls_otherContent) {
            ja_otherContent.add(oc.toJSON());
        }
        jo.element("otherContent", ja_otherContent);
        return jo;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * @return the ls_images
     */
    public List<Image> getLs_images() {
        return ls_images;
    }

    /**
     * @param ls_images the ls_images to set
     */
    public void setLs_images(List<Image> ls_images) {
        this.ls_images = ls_images;
    }

    /**
     * @return the ls_otherContent
     */
    public List<OtherContent> getLs_otherContent() {
        return ls_otherContent;
    }

    /**
     * @param ls_otherContent the ls_otherContent to set
     */
    public void setLs_otherContent(List<OtherContent> ls_otherContent) {
        this.ls_otherContent = ls_otherContent;
    }

    /**
     * Load annotations on this canvas for this project.
     *
     * @param projectID : the projectID the canvas belongs to
     * @param canvasID: The canvas ID the annotation list is on
     * @param folioNumber : The folio ID on this canvas
     * @param UID: The current UID of the user in session.
     * @return : The annotation lists @id property, not the object. Meant to
     * look like an otherContent field.
     */
    public static JSONArray getLinesForProject(Integer projectID, String canvasID, Integer folioNumber, Integer UID) throws MalformedURLException, IOException, SQLException {
        JSONObject annotationList = new JSONObject();
        JSONArray resources_array = new JSONArray();
        String dateString = "";
        String annoListID = getRbTok("SERVERURL") + "project/" + projectID + "/annotations/" + folioNumber;
        annotationList.element("@id", annoListID);
        annotationList.element("@type", "sc:AnnotationList");
        annotationList.element("label", canvasID + " List");
        annotationList.element("proj", projectID);
        annotationList.element("on", canvasID);
        annotationList.element("@context", "http://iiif.io/api/presentation/2/context.json");
        //annotationList.element("testing", "msid_creation");
        Transcription[] lines;
        lines = getProjectTranscriptions(projectID, folioNumber); //Can return an empty array now.
        int numberOfLines = lines.length;
        List<Object> resources = new ArrayList<>();
        //System.out.println("How many lines?   "+numberOfLines);
        for (int i = 0; i < numberOfLines; i++) { //numberOfLines can be 0 now.
            if (lines[i] != null) {
                //System.out.println("On line "+i);
                dateString = "";
                //when it breaks, it doesn't get this far
                //System.out.println(lines[i].getLineID() + " " +lines[i].getDate().toString());
                int lineID = lines[i].getLineID();
                Map<String, Object> lineAnnot = new LinkedHashMap<>();
                String lineURI = "line/" + lineID;
                String annoLineID = getRbTok("SERVERURL") + "line/" + lineID;
                //lineAnnot.put("@id", lineURI);
                lineAnnot.put("@id", annoLineID);
                lineAnnot.put("_tpen_line_id", lineURI);
                lineAnnot.put("@type", "oa:Annotation");
                lineAnnot.put("motivation", "oad:transcribing");
                lineAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", encoder().decodeForHTML(lines[i].getText())));
                lineAnnot.put("on", format("%s#xywh=%d,%d,%d,%d", canvasID, lines[i].getX(), lines[i].getY(), lines[i].getWidth(), lines[i].getHeight()));
                if (null != lines[i].getComment() && !"null".equals(lines[i].getComment())) {
                    //System.out.println("comment was usable");
                    lineAnnot.put("_tpen_note", lines[i].getComment());
                } else {
                    //System.out.println("comment was null");
                    lineAnnot.put("_tpen_note", "");
                }
                lineAnnot.put("_tpen_creator", lines[i].getCreator());
                //This is throwing Null Pointer and bubbling up to JsonLDExporter and up to getProjectTPENServlet

                dateString = lines[i].getDate().toString();
                lineAnnot.put("modified", dateString);
                resources.add(lineAnnot);
            } else {
                getLogger(Canvas.class.getName()).log(INFO, null, "Lines for list was null");
                out.println("lines was null");
            }
        }
        resources_array = JSONArray.fromObject(resources); //This can be an empty array now.
//            String newListID = Annotation.saveNewAnnotationList(annotationList);
//            annotationList.element("@id", newListID);
        annotationList.element("resources", resources_array);
        JSONArray annotationLists = new JSONArray();
        annotationLists.add(annotationList); // Only one in this version.
        return annotationLists;
    }
    /**
     * Check the annotation store for the annotation page with textual annotations
     * on this canvas for this project.
     *
     * @param projectID : the projectID the canvas belongs to
     * @param canvasID: The canvas ID the annotation page is on
     * @param folioNumber: 
     * @return: The annotation page of textual annotations that belong to the 
     * specific canvas
     */
    public static JSONArray getAnnotationLinesForAnnotationPage(Integer projectID, String canvasID, Integer folioNumber) throws MalformedURLException, IOException, SQLException {
        JSONArray annotationsArray;
        String dateString;
        Transcription[] lines;
        lines = getProjectTranscriptions(projectID, folioNumber); //Can return an empty array now.
        int numberOfLines = lines.length;
        List<Object> resources = new ArrayList<>();
        for (int i = 0; i < numberOfLines; i++) { //numberOfLines can be 0 now.
            if (lines[i] != null) {
                dateString = "";
                int lineID = lines[i].getLineID();
                Map<String, Object> lineAnnot = new LinkedHashMap<>();
                String lineURI = "line/" + lineID;
                String annoLineID = getRbTok("SERVERURL") + "line/" + lineID;
                lineAnnot.put("id", annoLineID);
                lineAnnot.put("type", "Annotation");
                lineAnnot.put("motivation", "transcribing");
		
		// Annotation body
		Map<String, String> body = JsonHelper.buildAnnotationBody("cnt:ContentAsText", encoder().decodeForHTML(lines[i].getText()));
		lineAnnot.put("body", body);
                lineAnnot.put("target", format("%s#xywh=%d,%d,%d,%d", canvasID, lines[i].getX(), lines[i].getY(), lines[i].getWidth(), lines[i].getHeight()));
		// `target` replaces `on` from version 2 and it seems like they don't want the dimensions on the canvas url


		// All these properties below are from version 2 annotations, but it seems like they are project-specific and therefore should be here as well
		lineAnnot.put("_tpen_line_id", lineURI);
                if (null != lines[i].getComment() && !"null".equals(lines[i].getComment())) {   
                    lineAnnot.put("_tpen_note", lines[i].getComment());
                } else {
                    lineAnnot.put("_tpen_note", "");
                }
                lineAnnot.put("_tpen_creator", lines[i].getCreator());
                //This is throwing Null Pointer and bubbling up to JsonLDExporter and up to getProjectTPENServlet

                dateString = lines[i].getDate().toString();
                lineAnnot.put("modified", dateString);
                resources.add(lineAnnot);
            } else {
                getLogger(Canvas.class.getName()).log(INFO, null, "Lines for list was null");
                out.println("lines was null");
            }
        }
        annotationsArray = JSONArray.fromObject(resources); //This can be an empty array now.
	return annotationsArray;
    }

    public static JSONArray getPaintingAnnotations(Integer projectID, Folio f, Dimension storedDims) throws SQLException {
        try {      
            String canvasID = getRbTok("SERVERURL")+"canvas/"+f.getFolioNumber();
            String annoListID = getRbTok("SERVERURL") + "project/" + projectID + "/annotations/" + f.getFolioNumber();
            Map<String, Object> manifestServices = JsonHelper.buildServices();
            JSONArray paintingAnnotations = new JSONArray();
            JSONObject annotation = new JSONObject();
            String imageURL = f.getImageURL();
            if (imageURL.startsWith("/")) {
                imageURL = String.format("%spageImage?folio=%s",getRbTok("SERVERURL"), f.getFolioNumber());
            }

            annotation.put("id", annoListID);
            annotation.put("type", "Annotation");
            annotation.put("motivation", "painting");

            JSONObject body = new JSONObject();
            body.put("id", imageURL);
            body.put("type", "Image");
            body.put("format", "image/jpeg");
            if (storedDims.height > 0) { //We could ignore this and put the 0's into the image annotation
                //doing this check will return invalid images because we will not include height and width of 0.
               body.put("height", storedDims.height ); 
               body.put("width", storedDims.width ); 
            }

            JSONObject service = new JSONObject();
            service.put("id", manifestServices.get("id"));
            service.put("type", manifestServices.get("type"));
            //body.put("service", service);

            annotation.put("body", body);

            annotation.put("target", canvasID);
            paintingAnnotations.add(annotation);
            return paintingAnnotations;

            } catch (Exception e)
            {
                    System.out.println(e);
                    return new JSONArray();
            }
    }
    
    /* 
    @param resources: A JSON array of annotations that are all new (insert can be used).  
    @return A JSONArray of annotations with their @id included.

    The resources need to be saved and a JSON array of the objects with their @ids in them needs
    to be returned.
     */
    public static JSONArray bulkUpdateTranscriptlets(JSONArray resources) throws MalformedURLException, IOException {
        JSONArray new_resources = new JSONArray();
        URL postUrlCopyAnno = new URL(ANNOTATION_SERVER_ADDR + "/anno/batchSaveMetadataForm.action");
        HttpURLConnection ucCopyAnno = (HttpURLConnection) postUrlCopyAnno.openConnection();
        ucCopyAnno.setDoInput(true);
        ucCopyAnno.setDoOutput(true);
        ucCopyAnno.setRequestMethod("POST");
        ucCopyAnno.setUseCaches(false);
        ucCopyAnno.setInstanceFollowRedirects(true);
        ucCopyAnno.addRequestProperty("content-type", "application/x-www-form-urlencoded");
        ucCopyAnno.connect();
        try (DataOutputStream dataOutCopyAnno = new DataOutputStream(ucCopyAnno.getOutputStream())) {
            String str_resources = "";
            if (resources.size() > 0) {
                str_resources = resources.toString();
            } else {
                str_resources = "[]";
            }
            dataOutCopyAnno.writeBytes("content=" + encode(str_resources, "utf-8"));
            dataOutCopyAnno.flush();
        }
        StringBuilder sbAnnoLines;
        try (BufferedReader returnedAnnoList = new BufferedReader(new InputStreamReader(ucCopyAnno.getInputStream(), "utf-8"))) {
            String lines = "";
            sbAnnoLines = new StringBuilder();
            while ((lines = returnedAnnoList.readLine()) != null) {
                sbAnnoLines.append(lines);
            }
        }
        String parseThis = sbAnnoLines.toString();
        JSONObject batchSaveResponse = fromObject(parseThis);
        try {
            new_resources = (JSONArray) batchSaveResponse.get("reviewed_resources");
        } catch (JSONException e) {
            getLogger(Canvas.class.getName()).log(INFO, null, e);
            throw e;
        }
        return new_resources;
    }
}
