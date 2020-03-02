/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import static edu.slu.tpen.entity.Image.Canvas.bulkSaveAnnotations;
import static edu.slu.tpen.servlet.Constant.ANNOTATION_SERVER_ADDR;
import static edu.slu.tpen.servlet.util.CreateAnnoListUtil.createEmptyAnnoList;
import static edu.slu.util.LangUtils.buildQuickMap;
import static edu.slu.util.ServletUtils.getDBConnection;
import static edu.slu.util.ServletUtils.getUID;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.out;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.net.URLEncoder.encode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import net.sf.json.*;
import static net.sf.json.JSONObject.fromObject;
import static org.owasp.esapi.ESAPI.encoder;
import static textdisplay.Annotation.saveNewAnnotationList;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.PartnerProject;
import textdisplay.Project;

/**
 *
 * @author bhaberbe
 *
 * Copy all project metadata and the annotation list with its annotations for
 * each canvas (here named folio). Makes use of Mongo Bulk operation
 * capabilities to limit the amount of necessary http connections which greatly
 * improved speed.
 */
public class CopyProjectAndAnnos extends HttpServlet {

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
        String result = "";
        int uID = getUID(request, response);
        if (null != request.getParameter("projectID") && uID != -1) {
            Integer projectID = parseInt(request.getParameter("projectID"));
            //System.out.println("Copy project and annos for "+projectID);
            try {
                //find original project and copy to a new project. 
                Project templateProject = new Project(projectID);
                Connection conn = getDBConnection();
                conn.setAutoCommit(false);
                //in this method, it copies everything about the project.
                if (null != templateProject.getProjectName()) {
                    Project thisProject = new Project(templateProject.copyProjectWithoutTranscription(conn, uID));
                    //set partener project. It is to make a connection on switch board. 
                    thisProject.setAssociatedPartnerProject(projectID);
                    PartnerProject theTemplate = new PartnerProject(projectID);
                    thisProject.copyButtonsFromProject(conn, theTemplate.getTemplateProject());
                    thisProject.copyHotkeysFromProject(conn, theTemplate.getTemplateProject());
                    conn.commit();
                    Folio[] folios = thisProject.getFolios();
                    //System.out.println("Created a new project template.  What was the ID assigned to it: "+thisProject.getProjectID());
                    if (null != folios && folios.length > 0) {
                        for (Folio folio : folios) {
                            //System.out.println("Starting copy for canvas");
                            //get annotation list for each canvas
                            JSONObject annoLsQuery = new JSONObject();
                            annoLsQuery.element("@type", "sc:AnnotationList");
                            Integer msID = folio.getMSID();
                            String msID_str = msID.toString();
                            //This needs to be the same one the JSON Exporter creates and needs to be unique and unchangeable.
                            String canvasID = "";
                            // if you want to include the manuscript it is involved in, add this before canvas/.... "/MS"+msID_str+"
                            canvasID = getRbTok("SERVERURL") + "canvas/" + folio.getFolioNumber();
                            //String canvasID = Folio.getRbTok("SERVERURL") + templateProject.getProjectName() + "/canvas/" + URLEncoder.encode(folio.getPageName(), "UTF-8"); // for slu testing
                            annoLsQuery.element("on", canvasID);
                            annoLsQuery.element("proj", projectID);
                            //System.out.println(annoLsQuery.toString());
                            URL postUrlannoLs = new URL(ANNOTATION_SERVER_ADDR + "/anno/getAnnotationByProperties.action");
                            HttpURLConnection ucAnnoLs = (HttpURLConnection) postUrlannoLs.openConnection();
                            ucAnnoLs.setDoInput(true);
                            ucAnnoLs.setDoOutput(true);
                            ucAnnoLs.setRequestMethod("POST");
                            ucAnnoLs.setUseCaches(false);
                            ucAnnoLs.setInstanceFollowRedirects(true);
                            ucAnnoLs.addRequestProperty("content-type", "application/x-www-form-urlencoded");
                            ucAnnoLs.connect();
                            try (DataOutputStream dataOutAnnoLs = new DataOutputStream(ucAnnoLs.getOutputStream())) {
                                dataOutAnnoLs.writeBytes("content=" + encode(annoLsQuery.toString(), "utf-8"));
                                dataOutAnnoLs.flush();
                            }
                            StringBuilder sbAnnoLs;
                            try (BufferedReader readerAnnoLs = new BufferedReader(new InputStreamReader(ucAnnoLs.getInputStream(), "utf-8"))) {
                                String lineAnnoLs = "";
                                sbAnnoLs = new StringBuilder();
                                while ((lineAnnoLs = readerAnnoLs.readLine()) != null) {
                                    sbAnnoLs.append(lineAnnoLs);
                                }
                            }
                            ucAnnoLs.disconnect();
//transfer annotation list string to annotation list JSON Array.
                            String getAnnoResponse = "";
                            try {
                                getAnnoResponse = sbAnnoLs.toString();
                            } catch (Exception e) {
                                getAnnoResponse = "[]";
                            }
                            JSONArray ja_allAnnoLists = JSONArray.fromObject(getAnnoResponse);
                            //This is the list of all AnnotatationLists attached to this folio.
                            JSONObject jo_annotationList = new JSONObject();
                            JSONArray new_resources = new JSONArray();
                            JSONArray resources = new JSONArray();
                            //For the original project...
                            if (ja_allAnnoLists.size() > 0) {
                                //Should only be one.  If not, that is a bit strange.
                                jo_annotationList = ja_allAnnoLists.getJSONObject(0);
                            } else {
                                //We should see if this is an old project.  Check the TPEN SQL for lines and transfer.
                                List<Object> resources_list = new ArrayList<>();
                                try (final PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transcription WHERE projectID = ? AND folio = ? ORDER BY x, y")) {
                                    stmt.setInt(1, projectID);
                                    stmt.setInt(2, folio.getFolioNumber());
                                    ResultSet rs = stmt.executeQuery();
                                    while (rs.next()) {
                                        int lineID = rs.getInt("id");
                                        Map<String, Object> lineAnnot = new LinkedHashMap<>();
                                        String lineURI = templateProject.getProjectName() + "/line/" + lineID;
                                        //lineAnnot.put("@id", lineURI);
                                        lineAnnot.put("_tpen_line_id", lineURI);
                                        lineAnnot.put("@type", "oa:Annotation");
                                        lineAnnot.put("motivation", "oad:transcribing");
                                        lineAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", encoder().decodeForHTML(rs.getString("text"))));
                                        lineAnnot.put("on", format("%s#xywh=%d,%d,%d,%d", canvasID, rs.getInt("x"), rs.getInt("y"), rs.getInt("width"), rs.getInt("height")));
                                        resources_list.add(lineAnnot);
                                        String note = rs.getString("comment");
                                        lineAnnot.put("_tpen_note", note);
                                        int creatorID = rs.getInt("creator");
                                        lineAnnot.put("_tpen_creator", creatorID);
//                                      if (StringUtils.isNotBlank(note)) { //could make the note an annotation...
//                                         Map<String, Object> noteAnnot = new LinkedHashMap<>();
//                                         //noteAnnot.put("@id", projName + "/note/" + lineID);
//                                         noteAnnot.put("@type", "oa:Annotation");
//                                         noteAnnot.put("motivation", "oa:commenting");
//                                         noteAnnot.put("resource", buildQuickMap("@type", "cnt:ContentAsText", "cnt:chars", note));
//                                         noteAnnot.put("on", lineURI); //TODO: should this be on an @id of an annotation? If so, that complicates how i want to do the bulk.
//                                         noteAnnot.put("testing", "msid_creation");
//                                         resources_list.add(noteAnnot);
//                                      }
                                    }
                                }
                                JSONArray resources_array = JSONArray.fromObject(resources_list);
                                resources = bulkSaveAnnotations(resources_array);
                                jo_annotationList = createEmptyAnnoList(projectID, canvasID, resources_array);
                                saveNewAnnotationList(jo_annotationList);
                                //This will have pulled the data over for the original project
                            }
                            //...Now for the new project
                            if (jo_annotationList.size() > 0 || (null != jo_annotationList.get("resources") && !jo_annotationList.get("resources").toString().equals("[]"))) {
                                try {
                                    resources = (JSONArray) jo_annotationList.get("resources");
                                } catch (JSONException e) {
                                    out.println("Could not parse resources.  Could not get annotations for copy.");
                                    //If this list can't be parsed, the copied list will have errors.  Just define it as empty as the fail.
                                }
                                URL postUrlCopyAnno = new URL(ANNOTATION_SERVER_ADDR + "/anno/batchSaveFromCopy.action");
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
                                ucCopyAnno.disconnect();
                                String parseThis = sbAnnoLines.toString();
                                JSONObject batchSaveResponse = fromObject(parseThis);
                                try {
                                    new_resources = (JSONArray) batchSaveResponse.get("new_resources");
                                } catch (JSONException e) {
                                    out.println("Batch save response does not contain JSONARRAY in new_resouces.");
                                }
                            } else {
                            }
                            //Create annotationList for new project and save into store.
                            JSONObject canvasList = createEmptyAnnoList(thisProject.getProjectID(), canvasID, new_resources);
                            canvasList.element("copiedFrom", request.getParameter("projectID"));
                            saveNewAnnotationList(canvasList);
                        }
                    }
                    result = "/project/" + thisProject.getProjectID();
                }
            } catch (Exception e) {
            }
        } else {
            response.setStatus(SC_FORBIDDEN);
            result = "Unauthorized or invalid project speficied.";
        }
        response.getWriter().print(result);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
