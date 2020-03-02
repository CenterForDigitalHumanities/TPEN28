/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import static edu.slu.tpen.servlet.Constant.ANNOTATION_SERVER_ADDR;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.net.URLEncoder.encode;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Update annotation list from rerum.io. 
 * This is not tpen transformation. It utilizes rerum.io as its repository. 
 * @author hanyan
 */
public class UpdateAnnoListServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            URL postUrl = new URL(ANNOTATION_SERVER_ADDR + "/anno/updateAnnotation.action");
            HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.connect();
            //value to save
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                //value to save
                out.writeBytes("content=" + encode(request.getParameter("content"), "utf-8"));
                out.flush();
                // flush and close
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
            String line="";
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null){
                //line = new String(line.getBytes(), "utf-8");  
//                System.out.println(line);
                sb.append(line);
            }
            reader.close();
            connection.disconnect();
            response.setHeader("Content-Location", "absoluteURI");
            response.getWriter().print(sb.toString());
        } catch (UnsupportedEncodingException ex) {
            getLogger(UpdateAnnoListServlet.class.getName()).log(SEVERE, null, ex);
        } catch (IOException ex) {
            getLogger(UpdateAnnoListServlet.class.getName()).log(SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
