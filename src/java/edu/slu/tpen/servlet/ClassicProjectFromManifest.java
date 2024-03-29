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
     * The Manifest must be IIIF Presentation API 2.1.
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
            //TODO: @context validation too?
            if(!theManifest.has("@type") || !theManifest.getString("@type").equals("sc:Manifest")){
                response.sendError(SC_BAD_REQUEST, "The object provided is not a IIIF Presentation API 2.1 Manifest.");
                return -1;
            }
            String type = theManifest.getString("@type");
            //Should we be setting these to something strategic?
            //String repository = "unknown";
            //String collection = "unkown";
            String archive = "private";
            String city = "unknown";
            String label = "unknown"; 
            List<Integer> ls_folios_keys = new ArrayList();
            if(theManifest.has("@id")){
                archive = theManifest.getString("@id");
            }
            else{
                response.sendError(SC_INTERNAL_SERVER_ERROR, "Manifest does not contain '@id'");
                return -1;
            }
            if(theManifest.has("label")){
                label = theManifest.getString("label");
            }
            else{
                label = "New T-PEN Manifest";
            }
            textdisplay.Manuscript mss= new textdisplay.Manuscript("TPEN Manifest Ingester", "fromManifest", archive, city, -999);
            JSONArray sequences = (JSONArray) theManifest.get("sequences");
            out.println("Go over sequences");
            int folioscreated = 0;
            int canvasespresent = 0;
            for (int i = 0; i < sequences.size(); i++) {
                JSONObject inSequences = (JSONObject) sequences.get(i);
                JSONArray canvases = inSequences.getJSONArray("canvases");
                out.println("Go over "+canvases.size()+" canvases");
                canvasespresent = canvases.size();
                if (null != canvases && canvases.size() > 0) {
                    for (int j = 0; j < canvases.size(); j++) {
                        JSONObject canvas = canvases.getJSONObject(j);
                        JSONArray images = canvas.getJSONArray("images");
                        /**
                         * NOTE
                         * We make Folios from images and not Canvases.  1 image per Folio.
                         * We could make Folios from Canvases instead.  Canvases without images could just get a placeholder instead of being ignored.
                         */
                        if (null != images && images.size() > 0) {
                            for (int n = 0; n < images.size(); n++) {
                                JSONObject image = images.getJSONObject(n);
                                if(!image.has("resource")){
                                    // This image object does not have a resource.  Skip it.
                                    LOG.log(WARNING, "Image {0} on canvas {1} did not have resource.  It will be skipped.", new Object[]{n, j});
                                    continue;
                                }
                                JSONObject resource = image.getJSONObject("resource");
                                String imageName = resource.getString("@id");
                                String[] parts = imageName.split("/");
                                String part = parts[parts.length-1];
                                // If we think it is already a direct link to a resource at http://not.real/some.filetype, let's keep it and just use that.
                                if(!checkIfFileHasExtension(part)){
                                    // Well then it isn't a file link.  It might resolve, but we can do better if a service exists.
                                    if(resource.has("service")){
                                        // Then it is probably IIIF Image API compliant.  Let's build from the image service link
                                        JSONObject service = resource.getJSONObject("service");
                                        if(service.has("@id")){
                                            String serviceImageName = service.getString("@id");
                                            if(serviceImageName.endsWith("/")){
                                                serviceImageName += "full/full/0/default.jpg";
                                            }
                                            else{
                                                serviceImageName += "/full/full/0/default.jpg";
                                            }
                                            imageName = serviceImageName;
                                        }
                                        // If there wasn't a service @id, then we are stuck with whatever the original image URL was.  Let's hope it resolves to an image.
                                    }
                                    // If there wasn't a service, then we are stuck with whatever the original image URL was.  Let's hope it resolves to an image.
                                }
                                LOG.log(INFO, "Create Folio entry for image: {0}", imageName);
                                int folioKey = createFolioRecordFromManifest(city, canvas.getString("label"), imageName, archive, mss.getID(), 0);
                                ls_folios_keys.add(folioKey);
                                folioscreated++;
                            }
                        }
                    }
                }
            }
            System.out.println(folioscreated+" folios created from "+canvasespresent+" canvases");
            String tmpProjName = mss.getShelfMark()+" project";
            if (theManifest.has("label")) {
                tmpProjName = theManifest.getString("label");
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
    
    /**
     * See if this filename contains a filetypes we might come across in the IIIF Image API.
     * See https://iiif.io/api/image/3.0/#45-format
     * @param s
     * @return 
     */
    public static boolean checkIfFileHasExtension(String s) {
        String[] extn = {"png", "jpg", "JPG", "jpeg", "JPEG", "jp2", "gif", "tif", "webp"};
        return Arrays.stream(extn).anyMatch(entry -> s.endsWith(entry));
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
