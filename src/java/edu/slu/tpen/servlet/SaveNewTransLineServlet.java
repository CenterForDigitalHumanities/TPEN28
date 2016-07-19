/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is used for IIIF store. 
 * @author hanyan
 */
public class SaveNewTransLineServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Annotation anno = new Annotation();
        anno.setNamespace(req.getParameter("namespace"));
        anno.setContent(req.getParameter("content"));
        anno.setSelector(req.getParameter("selector"));
        anno.setTitle(req.getParameter("title"));
        anno.setResource(URLEncoder.encode(req.getParameter("resource"), "utf-8"));
        anno.setResourceType(req.getParameter("resourceType"));
        anno.setOutterRelative(req.getParameter("outterRelative"));
        anno.setAddedTime(System.currentTimeMillis()); //This value is set in annotationStore.
        anno.setFontColor(req.getParameter("fontColor"));
        anno.setFontType(req.getParameter("fontType"));
        if(null != req.getParameter("permission")){
            anno.setPermission(Integer.parseInt(req.getParameter("permission")));
        }else{
            anno.setPermission(0);
        }
        anno.setOriginalAnnoID("");
        anno.setVersionNum(1); // this value is set in annotationStore. 
        anno.setForkFromID(req.getParameter("forkFromID"));
//        System.out.println("fork from if ============== " + req.getParameter("forkFromID"));
        try {
            URL postUrl = new URL(Constant.ANNOTATION_SERVER_ADDR + "/anno/saveNewAnnotation");
            HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
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
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream()); 
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
                System.out.println(line);
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
