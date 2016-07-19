/*
 * Copyright 2011-2014 Saint Louis University. Licensed under the
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
 * 
 * This servlet is modified from source code at http://www.javabeat.net/articles/262-asynchronous-file-upload-using-ajax-jquery-progress-ba-1.html
 */
package ImageUpload;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.sql.Connection;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import static edu.slu.util.ServletUtils.getDBConnection;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import textdisplay.Folio;
import textdisplay.Manuscript;
import textdisplay.Project;
import user.Group;
import user.User;

/**
 *
 * @author obi1one
 */
public class FileUpload extends HttpServlet implements Servlet {

   /**
    *
    * @param req
    * @param resp
    * @throws ServletException
    * @throws IOException
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try (PrintWriter out = resp.getWriter()) {
         HttpSession session = req.getSession();

         if (session != null) {
            FileUploadListener listener = (FileUploadListener)session.getAttribute("LISTENER");

            if (listener == null) {
               out.print("No active listener");
               return;
            }
            
            long bytesRead = listener.getBytesRead();
            long contentLength = listener.getContentLength();

            resp.setContentType("text/xml");
            StringBuilder buffy = new StringBuilder();
            buffy.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
            buffy.append("<response>\n");
            buffy.append("\t<bytes_read>").append(bytesRead).append("</bytes_read>\n");
            buffy.append("\t<content_length>").append(contentLength).append("</content_length>\n");

            if (bytesRead == contentLength) {
               buffy.append("\t<finished />\n");
               //session.setAttribute("LISTENER", null);
            } else {
               long percentComplete = ((100 * bytesRead) / contentLength);
               buffy.append("\t<percent_complete>").append(percentComplete).append("</percent_complete>\n");
            }
            buffy.append("</response>\n");
            out.println(buffy.toString());
            out.flush();
         }
      }
   }

   /**
    *
    * @param req
    * @param resp
    * @throws ServletException
    * @throws IOException
    */
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      HttpSession session = req.getSession();
      int uid = getUID(req, resp);
      if (uid > 0) {
         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
         FileUploadListener listener = new FileUploadListener();
         upload.setProgressListener(listener);

         session.setAttribute("LISTENER", listener);

         try (Connection conn = getDBConnection()) {
            conn.setAutoCommit(false);
            User thisUser = new User(uid);
            String city = req.getParameter("city");
            String collection = req.getParameter("collection");
            String repository = req.getParameter("repository");
            String archive = "private";

            long maxSize = Integer.parseInt(Folio.getRbTok("maxUploadSize")); //200 megs
            List uploadedItems = upload.parseRequest(req);
            Iterator i = uploadedItems.iterator();
            while (i.hasNext()) {
               FileItem fileItem = (FileItem)i.next();
               if (fileItem.isFormField() == false) {
                  if (fileItem.getSize() > 0 && fileItem.getSize() < maxSize && fileItem.getName().toLowerCase().endsWith("zip")) {
                     File f = new File(Folio.getRbTok("uploadLocation") + "/" + thisUser.getLname() + thisUser.getUID() + ".zip");
                     fileItem.write(f);
                     Manuscript ms = new Manuscript(repository, archive, collection, city, -999);
                     UserImageCollection.create(conn, f, thisUser, ms);
                     Group g = new Group(conn, ms.getShelfMark(), thisUser.getUID());
                     Project p = new Project(conn, ms.getShelfMark(), g.getGroupID());
                     p.setFolios(conn, ms.getFolios());
                  }
               }
            }
            conn.commit();
         } catch (Exception ex) {
            reportInternalError(resp, ex);
         } finally {
            session.setAttribute("LISTENER", null);
         }
      } else {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
   }
}
