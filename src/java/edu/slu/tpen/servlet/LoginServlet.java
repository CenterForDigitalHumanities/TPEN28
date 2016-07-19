/*
 * Copyright 2013- Saint Louis University. Licensed under the
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
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static edu.slu.util.ServletUtils.getBaseContentType;
import static edu.slu.util.ServletUtils.reportInternalError;
import user.User;


/**
 * Servlet to log into and log out of T-PEN.
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author tarkvara
 */
public class LoginServlet extends HttpServlet {

   /**
    * Handles the HTTP <code>POST</code> method by logging in using the given credentials.  Credentials
    * should be specified as a JSON object in the request body.  There is also a deprecated way of passing
    * the credentials as query parameters.
    *
    * @param req servlet request
    * @param resp servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try {
         String mail = null, password = null;
         if (req.getContentLength() > 0) {
            String contentType = getBaseContentType(req);
            if (contentType.equals("application/json")) {
               ObjectMapper mapper = new ObjectMapper();
               Map<String, String> creds = mapper.readValue(req.getInputStream(), new TypeReference<Map<String, String>>() {});
               mail = creds.get("mail");
               password = creds.get("password");
            }else if(contentType.equals("application/x-www-form-urlencoded")){
                mail = req.getParameter("uname");
                password = req.getParameter("password");
            }
         } else {
            // Deprecated approach where user-name and password are passed on the query string.
            mail = req.getParameter("uname");
            password = req.getParameter("password");
         }
         if (mail != null && password != null) {
            User u = new User(mail, password);
            if (u.getUID() > 0) {
               HttpSession sess = req.getSession(true);
               sess.setAttribute("UID", u.getUID());
//               System.out.println("HAve UID!!!!!!!!!!");
//               System.out.println(u.getUID());
//               System.out.println(sess.getAttribute("UID"));
               PrintWriter writer = resp.getWriter();
               writer.print(sess.getId());
            } else {
               resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
         } else if (mail == null && password == null) {
            // Passing null data indicates a logout.
            // System.out.println("Email and pwd null   !!!!!!!!!!");
            HttpSession sess = req.getSession(true);
            sess.removeAttribute("UID");
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
         } else {
            // Only supplied one of user-id and password.
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
         }
      } catch (NoSuchAlgorithmException ex) {
         reportInternalError(resp, ex);
      }
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Login Servlet";
   }
}
