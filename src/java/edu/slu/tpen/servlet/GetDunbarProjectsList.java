/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package edu.slu.tpen.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.Project;
import textdisplay.Folio;
import user.Group;
import user.User;

/**
 *
 * @author bhaberbe
 */
public class GetDunbarProjectsList extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
        response.setHeader("Cache-Control", "max-age=60, must-revalidate");
        response.setHeader("Etag", request.getContextPath() + "/getDunbarProjects/"+System.currentTimeMillis());
        int uid = getUID(request, response);
        response.setContentType("application/json; charset=utf-8");
        JSONArray result = new JSONArray();
        //if (uid > 0) {
           try {
              User u = new User(uid);
              Project[] projs = Project.getAllDunbarProjects();
              //image name/page name contains Fsomething that is folder number.  Add this change.
              for (Project p: projs) {
                    boolean skipProj = false;
                    JSONObject ro = new JSONObject();
                    JSONArray folios = new JSONArray();
                    Folio fp = new Folio(p.firstPage());
                    //String thumbnailURI = fp.getImageURL(); //Hmm this doesn't work
                    String thumbnailURI = "http://t-pen.org/TPEN/pageImage?folio="+fp.getFolioNumber();
                    Folio[] projectfolios = p.getFolios();
                    boolean finalized = true;
                    boolean extralog = false;
                    for(int i=0; i<projectfolios.length; i++){
                        Folio f = projectfolios[i];
                        JSONObject fo = new JSONObject();
                        int numParsedLines = p.getNumTranscriptionLines(f.folioNumber);
                        int numTranscribedLines = p.getNumTranscriptionLinesWithText(f.folioNumber);
                        if(!f.getPageName().contains("_F")){
                            System.out.println("Trouble processing folios for project.id ' "+p.getProjectID()+" ' project.name ' "+p.getProjectName()+" '");
                            System.out.println("Cannot get F-code from image name: ' "+fp.getPageName()+" '");
                            skipProj = true;
                            break;
                            //Or we can continue and just skip this folio, and not skip the project later.
                        }
                        fo.element("page_name", f.getPageName());
                        fo.element("numParsedLines", numParsedLines);
                        fo.element("numTranscribedLines", numTranscribedLines);
                        //Naive, just spitballing to give this a value.  This may end up being a flag that a human sets.  
                        if(finalized){
                            if(numTranscribedLines <= 5 || numParsedLines != numTranscribedLines){
                                finalized = false;
                            }
                        }
                        folios.add(fo);
                    }
                    if(skipProj){
                        //There was an image name that led us to believe this project is not a Dunbar project. skip it.
                        System.out.println("DO NOT add project "+p.getProjectID()+" to the result");
                        continue;
                    }
                    String pattern1 = "_F";
                    String pattern3 = ".jpg";
                    String delShort = "";
                    String longPiece = "";
                    String longNum = "";
                    String regexString2 = Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern3);
                    Pattern patternB = Pattern.compile(regexString2);
                    Matcher matcherB = patternB.matcher(fp.getPageName());
                    String longCode = "";
                    //image names like /MSS0113_S01_B02_F053_01_page_0001.jpg
                    if (matcherB.find()) {
                        longCode = matcherB.group(1); // Since (.*?) is capturing group 1
                    }
                    delShort = "F"+longCode.split("_")[0];
                    longPiece = longCode.split("_")[1];
                    //Just want the numbers
                    Pattern patterNum = Pattern.compile("\\d+");
                    Matcher m = patterNum.matcher(longPiece);
                    if(m.find()) {
                        longNum = m.group();
                    }
                    Group group = new Group(p.getGroupID());
                    User[] users = group.getMembers();
                    int[] userids = new int[users.length];
                    JSONArray userArr = new JSONArray();
                    for(int i=0; i<users.length; i++){
                        JSONObject tpenuser = new JSONObject();
                        tpenuser.element("userid", users[i].getUID());
                        tpenuser.element("username", users[i].getUname());
                        userArr.add(tpenuser);
                    }
                    ro.element("id", ""+p.getProjectID());
                    ro.element("project_name", p.getName());
                    ro.element("metadata_name", p.getProjectName());
                    ro.element("collection_code", delShort);
                    ro.element("entry_code", longNum);
                    ro.element("pages", folios);
                    ro.element("assignees", userArr);
                    ro.element("finalized", ""+finalized);
                    ro.element("thumbnail", thumbnailURI);
                    result.add(ro);
              }
              ObjectMapper mapper = new ObjectMapper();
              mapper.writeValue(response.getOutputStream(), result);
           } catch (SQLException ex) {
              reportInternalError(response, ex);
              response.sendError(SC_INTERNAL_SERVER_ERROR, ex.getMessage());
           }
        //} 
        //else {
        //    response.sendError(SC_UNAUTHORIZED);
        //}
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setStatus(200);
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

}
