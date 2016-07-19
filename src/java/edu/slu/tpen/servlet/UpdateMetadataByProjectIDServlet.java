/*
 * Copyright 2014- Saint Louis University. Licensed under the
 *	Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.slu.tpen.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Metadata;

/**
 * Update metadata by project id. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class UpdateMetadataByProjectIDServlet extends HttpServlet {
    //All parameters of metadata must be passed in. Null or empty properties will be set to null or 0. 
    private Integer projectID;
    private String title;
    private String subtitle;
    private String msIdentifier;
    private String msSettlement;
    private String msRepository;
    private String msCollection = "";
    private String msIdNumber;
    private String author;
    private String date;
    private String language;
    private String description;
    private String location;
    private String subject;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        System.out.println("project id =========== " + request.getParameter("projectID"));
        this.projectID = Integer.parseInt(request.getParameter("projectID"));
        try {
            if(null != projectID && -1 != projectID && projectID != 0){
                //update metadata if project ID is not 0. It is int type, so it's never null. 
                Metadata metadata = new Metadata(projectID);
                metadata.setTitle(request.getParameter("title"));
                metadata.setSubtitle(request.getParameter("subtitle"));
                metadata.setMsIdentifier(request.getParameter("msIdentifier"));
                metadata.setMsSettlement(request.getParameter("msSettlement"));
                metadata.setMsRepository(request.getParameter("msRepository"));
                metadata.setMsCollection(request.getParameter("msCollection"));
                metadata.setMsIdNumber(request.getParameter("msIdNumber"));
                metadata.setAuthor(request.getParameter("author"));
                metadata.setDate(request.getParameter("date"));
                metadata.setLanguage(request.getParameter("language"));
                metadata.setDescription(request.getParameter("description"));
                metadata.setLocation(request.getParameter("location"));
                metadata.setSubject(request.getParameter("subject"));
                metadata.commit();
                PrintWriter out = response.getWriter();
                out.print(HttpServletResponse.SC_OK);
            }else{
                //send error code if the project ID is 0. 
                PrintWriter out = response.getWriter();
                out.print(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException ex) {
            Logger.getLogger(UpdateMetadataByProjectIDServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the projectID
     */
    public int getProjectID() {
        return projectID;
    }

    /**
     * @param projectID the projectID to set
     */
    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * @param subtitle the subtitle to set
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return the msIdentifier
     */
    public String getMsIdentifier() {
        return msIdentifier;
    }

    /**
     * @param msIdentifier the msIdentifier to set
     */
    public void setMsIdentifier(String msIdentifier) {
        this.msIdentifier = msIdentifier;
    }

    /**
     * @return the msSettlement
     */
    public String getMsSettlement() {
        return msSettlement;
    }

    /**
     * @param msSettlement the msSettlement to set
     */
    public void setMsSettlement(String msSettlement) {
        this.msSettlement = msSettlement;
    }

    /**
     * @return the msRepository
     */
    public String getMsRepository() {
        return msRepository;
    }

    /**
     * @param msRepository the msRepository to set
     */
    public void setMsRepository(String msRepository) {
        this.msRepository = msRepository;
    }

    /**
     * @return the msCollection
     */
    public String getMsCollection() {
        return msCollection;
    }

    /**
     * @param msCollection the msCollection to set
     */
    public void setMsCollection(String msCollection) {
        this.msCollection = msCollection;
    }

    /**
     * @return the msIdNumber
     */
    public String getMsIdNumber() {
        return msIdNumber;
    }

    /**
     * @param msIdNumber the msIdNumber to set
     */
    public void setMsIdNumber(String msIdNumber) {
        this.msIdNumber = msIdNumber;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

}
