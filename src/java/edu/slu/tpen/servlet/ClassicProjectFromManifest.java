/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import edu.slu.tpen.transfer.JsonLDExporter;
import static edu.slu.util.ServletUtils.getDBConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static net.sf.json.JSONObject.fromObject;
import static textdisplay.Folio.createFolioRecordFromManifest;
import textdisplay.Project;
import user.Group;

/**
 *
 * @author bhaberbe
 */
public class ClassicProjectFromManifest extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        //response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        writer.print(createProject(request, response));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
    
    /**
     * Handles the HTTP <code>OPTIONS</code> preflight method.
     * Pre-flight support.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //These headers must be present to pass browser preflight for CORS
        //response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "600"); //Cache preflight responses for 10 minutes.
        response.setStatus(200);
    }
    
    /**
     * Make a project from a user provided Manifest URI.
     * The Manifest should be IIIF Presentation API 2.1 or 3.0.
     */
    public Integer createProject(HttpServletRequest request, HttpServletResponse response) throws IOException{
        LOG.log(INFO, "Create classic TPEN project from manifest URL");
        try{
            int UID = -1;
            if(null != request.getSession().getAttribute("UID")){
                UID = parseInt(request.getSession().getAttribute("UID").toString());
            }
            int projectID = 0;
            textdisplay.Project thisProject = null;
            if(request.getParameter("manifest")==null){
                response.sendError(SC_BAD_REQUEST, "You must provide a manifest ID via ?manifest={ID}");
                return -1;
            }
            if (UID <= 0) {
                response.sendError(SC_UNAUTHORIZED, "You must log in first.");
                return -1; 
            }
            JSONObject theManifest = resolveID(request.getParameter("manifest"));
            if(!isSupportedManifest(theManifest)){
                response.sendError(SC_BAD_REQUEST, "The object provided is not a recognized IIIF Presentation Manifest.");
                return -1;
            }
            //Should we be setting these to something strategic?
            //String repository = "unknown";
            //String collection = "unkown";
            String archive = "private";
            String city = "unknown";
            List<Integer> ls_folios_keys = new ArrayList();
            String manifestId = getStringValue(theManifest, "id", "@id");
            if(manifestId != null && !manifestId.trim().isEmpty()){
                archive = manifestId;
            }
            else{
                archive = request.getParameter("manifest");
            }
            textdisplay.Manuscript mss= new textdisplay.Manuscript("TPEN Manifest Ingester", "fromManifest", archive, city, -999);
            List<JSONObject> canvases = extractCanvases(theManifest);
            if(canvases.isEmpty()){
                response.sendError(SC_BAD_REQUEST, "Manifest did not contain any canvases to import.");
                return -1;
            }
            out.println("Go over " + canvases.size() + " canvases");
            int folioscreated = 0;
            int canvasespresent = canvases.size();
            for (int j = 0; j < canvases.size(); j++) {
                JSONObject canvas = canvases.get(j);
                String canvasLabel = readLabel(canvas, "Canvas " + (j + 1));
                List<String> imageUrls = extractImageUrlsFromCanvas(canvas);
                /**
                 * NOTE
                 * We make Folios from images and not Canvases.  1 image per Folio.
                 * Canvases without images are ignored.
                 */
                if(imageUrls.isEmpty()){
                    LOG.log(WARNING, "Canvas {0} had no importable image resources. It will be skipped.", j);
                    continue;
                }
                for (String imageName : imageUrls) {
                    LOG.log(INFO, "Create Folio entry for image: {0}", imageName);
                    int folioKey = createFolioRecordFromManifest(city, canvasLabel, imageName, archive, mss.getID(), 0);
                    ls_folios_keys.add(folioKey);
                    folioscreated++;
                }
            }
            System.out.println(folioscreated+" folios created from "+canvasespresent+" canvases");
            String tmpProjName = mss.getShelfMark()+" project";
            if (theManifest.has("label")) {
                tmpProjName = readLabel(theManifest, "New T-PEN Manifest");
            }
            Connection conn = getDBConnection();
            conn.setAutoCommit(false);
            Group newgroup = new Group(conn, tmpProjName, UID);
            Project newProject = new Project(conn, tmpProjName, newgroup.getGroupID());
            
            newProject.setFolios(conn, mss.getFolios());
            newProject.addLogEntry(conn, "<span class='log_manuscript'></span>Added manuscript " + mss.getShelfMark(), UID);
            thisProject=newProject;
            projectID=thisProject.getProjectID();
            System.out.println("Set "+mss.getFolios().length+" folios for newProject "+projectID);
            newProject.importData(UID);
            conn.commit();
            return projectID;
        }
        catch(IOException | NumberFormatException | SQLException e){
            response.sendError(500, e.getMessage());
            return -1;
        }
    }

    /**
     * Resolve a URI into JSON.
     * 
     * @param manifestID {String} URL to resolve into JSON.
     * @return The JSON representing the resolved resource.
     * @throws MalformedURLException
     * @throws IOException 
     */
    public JSONObject resolveID(String manifestID) throws MalformedURLException, IOException{
        JSONObject manifest = new JSONObject();
        URL id = new URL(manifestID);
        BufferedReader reader = null;
        StringBuilder stringBuilder;
        try{
            HttpURLConnection connection = (HttpURLConnection) id.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(15*1000);
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
              stringBuilder.append(line + "\n");
            }
            if(!stringBuilder.toString().trim().equals("")){
                manifest = fromObject(stringBuilder.toString());
            }
            LOG.log(INFO, "URL ' {0} ' Resolved successfully.", manifestID);
            return manifest;
        }
        catch(Exception e){
            return new JSONObject();
        }
        
    }

    private static boolean isSupportedManifest(JSONObject manifest) {
        String type = getStringValue(manifest, "type", "@type");
        if ("Manifest".equals(type) || "sc:Manifest".equals(type)) {
            return true;
        }
        // Be tolerant of real-world manifests with weak or missing type metadata.
        return manifest.has("items") || manifest.has("sequences");
    }

    private static List<JSONObject> extractCanvases(JSONObject manifest) {
        List<JSONObject> canvases = new ArrayList<>();
        if (manifest.has("sequences")) {
            JSONArray sequences = safeArray(manifest.get("sequences"));
            for (int i = 0; i < sequences.size(); i++) {
                JSONObject sequence = safeObject(sequences.get(i));
                if (sequence == null || !sequence.has("canvases")) {
                    continue;
                }
                JSONArray seqCanvases = safeArray(sequence.get("canvases"));
                for (int j = 0; j < seqCanvases.size(); j++) {
                    JSONObject canvas = safeObject(seqCanvases.get(j));
                    if (canvas != null) {
                        canvases.add(canvas);
                    }
                }
            }
        }

        if (manifest.has("items")) {
            JSONArray items = safeArray(manifest.get("items"));
            for (int i = 0; i < items.size(); i++) {
                JSONObject item = safeObject(items.get(i));
                if (item == null) {
                    continue;
                }
                String type = getStringValue(item, "type", "@type");
                if (type == null || type.trim().isEmpty() || "Canvas".equals(type) || "sc:Canvas".equals(type)) {
                    canvases.add(item);
                }
            }
        }
        return canvases;
    }

    private static List<String> extractImageUrlsFromCanvas(JSONObject canvas) {
        List<String> imageUrls = new ArrayList<>();

        // IIIF Presentation 2.x: canvas.images[*].resource
        if (canvas.has("images")) {
            JSONArray images = safeArray(canvas.get("images"));
            for (int i = 0; i < images.size(); i++) {
                JSONObject image = safeObject(images.get(i));
                if (image == null || !image.has("resource")) {
                    continue;
                }
                JSONObject resource = safeObject(image.get("resource"));
                String imageUrl = resolveImageUrl(resource);
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    imageUrls.add(imageUrl);
                }
            }
        }

        // IIIF Presentation 3.x: canvas.items[*].items[*].body
        if (canvas.has("items")) {
            JSONArray annotationPages = safeArray(canvas.get("items"));
            for (int i = 0; i < annotationPages.size(); i++) {
                JSONObject annotationPage = safeObject(annotationPages.get(i));
                if (annotationPage == null || !annotationPage.has("items")) {
                    continue;
                }
                JSONArray annotations = safeArray(annotationPage.get("items"));
                for (int j = 0; j < annotations.size(); j++) {
                    JSONObject annotation = safeObject(annotations.get(j));
                    if (annotation == null || !annotation.has("body")) {
                        continue;
                    }
                    Object body = annotation.get("body");
                    if (body instanceof JSONObject) {
                        String imageUrl = resolveImageUrl((JSONObject) body);
                        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                            imageUrls.add(imageUrl);
                        }
                        continue;
                    }
                    if (body instanceof JSONArray) {
                        JSONArray bodyArray = (JSONArray) body;
                        for (int k = 0; k < bodyArray.size(); k++) {
                            JSONObject bodyObj = safeObject(bodyArray.get(k));
                            String imageUrl = resolveImageUrl(bodyObj);
                            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                                imageUrls.add(imageUrl);
                            }
                        }
                    }
                }
            }
        }

        return imageUrls;
    }

    private static String resolveImageUrl(JSONObject resource) {
        if (resource == null) {
            return null;
        }
        String imageName = getStringValue(resource, "id", "@id");
        if (imageName == null || imageName.trim().isEmpty()) {
            return null;
        }

        String[] parts = imageName.split("/");
        String part = parts[parts.length - 1];
        if (checkIfFileHasExtension(part)) {
            return imageName;
        }

        String serviceId = getServiceId(resource);
        if (serviceId == null || serviceId.trim().isEmpty()) {
            return imageName;
        }
        if (serviceId.endsWith("/")) {
            return serviceId + "full/!4000,5000/0/default.jpg";
        }
        return serviceId + "/full/!4000,5000/0/default.jpg";
    }

    private static String getServiceId(JSONObject resource) {
        if (!resource.has("service")) {
            return null;
        }
        Object service = resource.get("service");
        if (service instanceof JSONObject) {
            return getStringValue((JSONObject) service, "id", "@id");
        }
        if (service instanceof JSONArray) {
            JSONArray serviceArray = (JSONArray) service;
            if (serviceArray.isEmpty()) {
                return null;
            }
            JSONObject serviceObj = safeObject(serviceArray.get(0));
            return getStringValue(serviceObj, "id", "@id");
        }
        return null;
    }

    private static JSONArray safeArray(Object value) {
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        return new JSONArray();
    }

    private static JSONObject safeObject(Object value) {
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        return null;
    }

    private static String getStringValue(JSONObject obj, String... keys) {
        if (obj == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (!obj.has(key)) {
                continue;
            }
            Object value = obj.get(key);
            if (value == null) {
                continue;
            }
            String asString = String.valueOf(value);
            if (!asString.trim().isEmpty() && !"null".equalsIgnoreCase(asString.trim())) {
                return asString;
            }
        }
        return null;
    }

    private static String readLabel(JSONObject obj, String fallback) {
        if (obj == null || !obj.has("label")) {
            return fallback;
        }
        Object label = obj.get("label");
        if (label instanceof String) {
            String text = ((String) label).trim();
            return text.isEmpty() ? fallback : text;
        }
        if (label instanceof JSONObject) {
            JSONObject labelObj = (JSONObject) label;
            if (labelObj.has("@value")) {
                String value = getStringValue(labelObj, "@value");
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            }
            if (labelObj.has("none")) {
                JSONArray none = safeArray(labelObj.get("none"));
                if (!none.isEmpty()) {
                    String value = String.valueOf(none.get(0));
                    if (!value.trim().isEmpty()) {
                        return value;
                    }
                }
            }
            for (Object keyObj : labelObj.keySet()) {
                String key = String.valueOf(keyObj);
                JSONArray localized = safeArray(labelObj.get(key));
                if (!localized.isEmpty()) {
                    String value = String.valueOf(localized.get(0));
                    if (!value.trim().isEmpty()) {
                        return value;
                    }
                }
            }
        }
        if (label instanceof JSONArray) {
            JSONArray labelArray = (JSONArray) label;
            if (!labelArray.isEmpty()) {
                String value = String.valueOf(labelArray.get(0));
                if (!value.trim().isEmpty()) {
                    return value;
                }
            }
        }
        return fallback;
    }
    
    /**
     * See if this filename contains a filetypes we might come across in the IIIF Image API.
     * See https://iiif.io/api/image/3.0/#45-format
     * @param s
     * @return 
     */
    public static boolean checkIfFileHasExtension(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        String normalized = s.trim();
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int fragmentIndex = normalized.indexOf('#');
        if (fragmentIndex >= 0) {
            normalized = normalized.substring(0, fragmentIndex);
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        String[] extn = {"png", "jpg", "jpeg", "gif", "webp", "avif", "apng", "bmp", "svg", "ico", "tif", "tiff", "jp2"};
        return Arrays.stream(extn).anyMatch(entry -> lower.endsWith(entry));
    }
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private static final Logger LOG = getLogger(JsonLDExporter.class.getName());

}
