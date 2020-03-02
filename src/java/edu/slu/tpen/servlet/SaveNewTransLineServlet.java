/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import edu.slu.tpen.entity.transcription.Annotation;
import static edu.slu.tpen.servlet.Constant.ANNOTATION_SERVER_ADDR;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import static java.lang.Integer.parseInt;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.net.URLEncoder.encode;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import static net.sf.json.JSONObject.fromObject;

/**
 * This servlet is used for IIIF store (rerum.io). It saves new trans-line to rerum.io. 
 * This is not from tpen. It utilizes rerum.io as its annotation repository. 
 * @author hanyan
 */
public class SaveNewTransLineServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(null != req.getSession().getAttribute("UID")){
            int UID = parseInt(req.getSession().getAttribute("UID").toString());
            Annotation anno = new Annotation();
            anno.setContent(req.getParameter("content"));
            JSONObject jo = fromObject(anno.getContent());
            jo.element("oa:createdBY", req.getLocalName() + "/" + UID);
            anno.setContent(jo.toString());
            try {
                URL postUrl = new URL(ANNOTATION_SERVER_ADDR + "/anno/saveNewAnnotation.action");
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
                try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                    out.writeBytes("content=" + encode(anno.getContent(), "utf-8"));
                    out.flush();
                    // flush and close
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
                String line="";
                StringBuilder sb = new StringBuilder();
    //            System.out.println("=============================");  
    //            System.out.println("Contents of post request");  
    //            System.out.println("=============================");  
                while ((line = reader.readLine()) != null){  
                    //line = new String(line.getBytes(), "utf-8");  
                    //System.out.println(line);
                    sb.append(line);
                }
    //            System.out.println("=============================");  
    //            System.out.println("Contents of post request ends");  
    //            System.out.println("=============================");  
                reader.close();
                connection.disconnect();
                resp.getWriter().print(sb.toString());
            } catch (UnsupportedEncodingException ex) {
                getLogger(SaveNewTransLineServlet.class.getName()).log(SEVERE, null, ex);
            } catch (IOException ex) {
                getLogger(SaveNewTransLineServlet.class.getName()).log(SEVERE, null, ex);
            }
        }else{
            resp.getWriter().print("You didn't log in. ");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp); 
    }
    
}
