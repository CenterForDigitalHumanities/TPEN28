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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.DatabaseWrapper;

/**
 * Upload image to tpen. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class ImageUploadServlet extends HttpServlet {

    @Override
    /**
     * @param msID (optional)
     * @param repository (optional)
     * @param archive (optional)
     * @param city (optional)
     * @param collection (optional)
     * @param pageName (optional)
     * @param imageURL
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String imageURL = request.getParameter("imageURL");
        String repository = "unknown";
        if(null != request.getParameter("repository")){
            repository = request.getParameter("repository");
        }else{
            //take the first part of image url to be the repository
        }
        String archive = "unknown";
        if(null != request.getParameter("archive")){
            archive = request.getParameter("archive");
        }
        String city = "unknown";
        if(null != request.getParameter("city")){
            city = request.getParameter("city");
        }
        String collection = "unknown";
        if(null != request.getParameter("collection")){
            collection = request.getParameter("collection");
        }
        String pageName = "unknown";
        if(null != request.getParameter("pageName")){
            pageName = request.getParameter("pageName");
        }
        if(null != imageURL){
            if(null != request.getParameter("msID")){
                //if user choose an existing manuscript, add the image into the manuscript.
                Connection conn = DatabaseWrapper.getConnection();
                String sql = "select max(sequence) from folio where msID=?";
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, Integer.parseInt(request.getParameter("msID")));
                    ResultSet rs = ps.executeQuery();
                    int maxSequence = 0;
                    while(rs.next()){
                        maxSequence = rs.getInt("sequence");
                    }
                    int num = textdisplay.Folio.createFolioRecordFromNewBerry(collection, pageName, 
                            imageURL, archive, Integer.parseInt(request.getParameter("msID")), maxSequence+1);
                    rs.close();
                    ps.close();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                //if user didn't choose a manuscript, create a new manuscript first. 
                try {
                    //create a manuscript
                    textdisplay.Manuscript m = new textdisplay.Manuscript(repository, archive, city, city, -999);
                    int num = textdisplay.Folio.createFolioRecordFromNewBerry(collection, pageName, 
                            imageURL, archive, m.getID(), 0);
                } catch (SQLException ex) {
                    Logger.getLogger(ImageUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }
    
}
