/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
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
 * @author Jon Deering
 */

package imageLines;

import static imageLines.ImageCache.getCachedImageDimensions;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import static java.lang.Integer.parseInt;
import java.sql.SQLException;
import static javax.imageio.ImageIO.write;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import textdisplay.Folio;
import textdisplay.FolioDims;

/**
 *
 * @author jdeerin1
 */
public class PageImage extends HttpServlet {
   
   /** 
    * Handles the HTTP <code>GET</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try (OutputStream os = response.getOutputStream()) {
         String folioParam = request.getParameter("folio");
         if (folioParam != null) {
            Folio f = new Folio(parseInt(folioParam));
            BufferedImage img = f.loadLocalImage();
            if (img != null) {
               response.setContentType("image/jpeg");
               response.setHeader("Access-Control-Allow-Origin", "*");
               response.setHeader("Access-Control-Allow-Headers", "*");
               response.setHeader("Access-Control-Allow-Methods", "GET");
               response.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
               response.setHeader("Cache-Control", "max-age=31536000, must-revalidate"); //This is immutable (or at least there is no plan for it to change).  Let it be fresh for a year.
               write(img, "jpg", os);
            } else {
               response.sendError(400, "Unknown image archive");
            }
         } else {
            response.sendError(400, "Missing \"folio=\" parameter");
         }
      } catch(SQLException e) {
          System.out.println(e);
         response.sendError(503, e.getMessage());
      } catch(Error err){
          System.out.println(err);
          response.sendError(500, err.getMessage());
      }
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
        response.setHeader("Access-Control-Max-Age", "600"); //Cache preflight responses for 10 minutes.
        response.setStatus(200);
    }
    
    /**
     * Handles the HTTP <code>HEAD</code> preflight method.
     * Pre-flight support.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
            try (OutputStream os = response.getOutputStream()) {
             String folioParam = request.getParameter("folio");
             if (folioParam != null) {
                Folio f = new Folio(parseInt(folioParam));
                FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
                Dimension storedDims = getCachedImageDimensions(f.getFolioNumber());
                if (pageDim.getImageHeight() <= 0) { //There was no foliodim entry
                    if(null == storedDims || storedDims.height <=0) { //There was no imagecache entry or a bad one we can't use
                        try{
                            storedDims = f.resolveImageForDimensions(); 
                        }
                        catch (java.net.SocketTimeoutException e) {
                            // There was a timeout on the Image URL.  We could not resolve the image for dimensions.
                            response.sendError(502, "Timeout.  Could not resolve imageURL "+f.getImageURL());
                        }
                    }
                }
                response.setContentType("image/jpeg");
                response.setHeader("Width", storedDims.getWidth()+"");
                response.setHeader("Height", storedDims.getHeight()+"");
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Allow-Methods", "HEAD");
                response.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
                response.setHeader("Cache-Control", "max-age=31536000, must-revalidate"); //This is immutable (or at least there is no plan for it to change).  Let it be fresh for a year.
                response.setStatus(200);
             } else {
                response.sendError(400, "Missing \"folio=\" parameter");
             }
          } catch(SQLException e) {
              System.out.println(e);
             response.sendError(503, "SQL ERRROR");
          } catch (Error err){
              System.out.println(err);
            response.sendError(500, err.getMessage());
          }
    }

   /** 
    * Returns a short description of the servlet.
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Page Image Servlet";
   }
}
