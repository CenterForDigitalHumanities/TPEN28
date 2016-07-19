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

import edu.slu.tpen.entity.transcription.Annotation;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Update trans-line to rerum,io. Please distinguish this to UpdateLineServlet.java. 
 * NOTE! This is not inherited from tpen. It utilizes rerum.io as its repository. 
 * @author hanyan
 */
public class UpdateTransLineServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Annotation anno = new Annotation();
        anno.setContent(req.getParameter("content"));
        try {
            URL postUrl = new URL(Constant.ANNOTATION_SERVER_ADDR + "annotationstore/annotation/updateAnnotation");
            HttpURLConnection connection = (HttpURLConnection) postUrl
                .openConnection();  
            // Output to the connection. Default is  
            // false, set to true because post  
            // method must write something to the  
            // connection  
            connection.setDoOutput(true);  
            // Read from the connection. Default is true.  
            connection.setDoInput(true);  
            // Set the post method. Default is GET  
            connection.setRequestMethod("POST");  
            // Post cannot use caches  
            connection.setUseCaches(false);  
            connection.setInstanceFollowRedirects(true);  
            connection.setRequestProperty("Content-Type",  
                    "application/x-www-form-urlencoded");
            connection.connect();  
            DataOutputStream out = new DataOutputStream(connection  
                    .getOutputStream());  
            String content = anno.toString();
            out.writeBytes(content);
            out.flush();  
            out.close(); // flush and close
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
            String line="";
            StringBuilder sb = new StringBuilder();
//            System.out.println("=============================");  
//            System.out.println("Contents of post request");  
//            System.out.println("=============================");  
            while ((line = reader.readLine()) != null){  
                //line = new String(line.getBytes(), "utf-8");  
//                System.out.println(line);
                sb.append(line);
            }  
//            System.out.println("=============================");  
//            System.out.println("Contents of post request ends");  
//            System.out.println("=============================");  
            reader.close();
            connection.disconnect();
            resp.getWriter().print(sb.toString());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SaveNewTransLineServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaveNewTransLineServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
    
}
