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
 */
package imageLines;

import static edu.slu.util.LangUtils.getMessage;
import static edu.slu.util.ServletUtils.getUID;
import static edu.slu.util.ServletUtils.reportInternalError;
import static imageLines.ImageCache.getCachedImageDimensions;
import static imageLines.ImageCache.getImage;
import static imageLines.ImageCache.setImage;
import static imageLines.ImageHelpers.scale;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.imageio.IIOImage;
import static javax.imageio.ImageIO.createImageOutputStream;
import static javax.imageio.ImageIO.getImageWritersByFormatName;
import static javax.imageio.ImageIO.read;
import javax.imageio.ImageWriteParam;
import static javax.imageio.ImageWriteParam.MODE_EXPLICIT;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import textdisplay.Archive;
import static textdisplay.Archive.connectionType.local;
import textdisplay.Folio;
import static textdisplay.Folio.getRbTok;
import textdisplay.FolioDims;


/**
 * A servlet to resize an image from an Archive to a size requested.
 *
 * @author Jon Deering
 */
public class ImageResize extends HttpServlet {

   /**
    * Processes requests for both HTTP
    * <code>GET</code> and
    * <code>POST</code> methods.
    *
    * @param milliSeconds
    * @return
    */
   private static String getGMTTimeString(long milliSeconds) {
      SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'");
      return sdf.format(new Date(milliSeconds));
   }

   /**
    * This handles delivering properly sized images to both end users and parts of tpen that need to access
    * images, such as the line parser.
    *
    * @param request
    * @param response
    * @throws ServletException
    * @throws IOException
    * @throws SQLException
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      try {
         int uID = getUID(request, response);
         if (uID < 0) {
            if (request.getParameter("code") == null) {
               response.sendError(403);
               return;
            }
            if (request.getParameter("code").compareTo(getRbTok("imageCode")) != 0) {
               response.sendError(403);
               return;
            }
         }
         response.setHeader("Access-Control-Allow-Origin", "*");
         response.setHeader("Access-Control-Allow-Headers", "*");
         response.setHeader("Access-Control-Allow-Methods", "GET");
         response.setHeader("Access-Control-Expose-Headers", "*"); //Headers are restricted, unless you explicitly expose them.  Darn Browsers.
         response.setHeader("Cache-Control", "max-age=31536000, must-revalidate"); //This is immutable (or at least there is no plan for it to change).  Let it be fresh for a year.
         long relExpiresInMillis = currentTimeMillis() + (1000 * 2600);
         response.addHeader("Expires", getGMTTimeString(relExpiresInMillis));
         response.setContentType("image/jpeg");

         if (request.getParameter("folioNum") != null) {
            BufferedImage toResize;
            Iterator iter = getImageWritersByFormatName("jpeg");
            ImageWriter writer = (ImageWriter) iter.next();
   // instantiate an ImageWriteParam object with default compression options
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(MODE_EXPLICIT);
            float quality = 0.5f;
            if (request.getParameter("quality") != null) {
               try {
                  int qual = parseInt(request.getParameter("quality"));
                  quality = qual / 100.0f;
               } catch (NumberFormatException e) {
               }
            }
            iwp.setCompressionQuality(quality);   // an integer between 0 and 1
            int folioNum = parseInt(request.getParameter("folioNum"));

            ImageRequest ir = null;
            if (uID > 0) {
               try {
                  ir = new ImageRequest(folioNum, uID);
               } catch (NumberFormatException | SQLException e) {
               }
            }
            if (ir == null) {
               ir = new ImageRequest(folioNum, 0);
            }
            try {
               toResize = getImage(folioNum);
               if (toResize != null) {
                  LOG.log(INFO, "Loaded image {0} from cache", folioNum);
               } else {
                  LOG.log(INFO, "Cache load failed, loading from source.");
                  Folio f = new Folio(folioNum);
                try (InputStream stream = f.getUncachedImageStream(false)) { //getting a 500 connection refused here.  firewall problems.  
                     toResize = read(stream);
                     LOG.log(INFO, "Loaded {0}", toResize);
                     Archive a = new Archive(f.getArchive());
                     if (!a.getName().equals("private") && a.getConnectionMethod() != local) {
                        LOG.log(INFO, "Adding image {0} to cache", folioNum);
                                setImage(folioNum, toResize);
                     }
                     ir.completeSuccess();
						}
               }

               int height = parseInt(request.getParameter("height"));
               int width = (int) ((height / (double)toResize.getHeight()) * toResize.getWidth());
               toResize = scale(toResize, height, width);
               OutputStream os = response.getOutputStream();
               IIOImage image = new IIOImage(toResize, null, null);
               writer.setOutput(createImageOutputStream(os));
               writer.write(null, image, iwp);
            } 
            catch (SQLException | IOException | IllegalArgumentException ex) {
               ir.completeFail(getMessage(ex));
               reportInternalError(response, ex);
            }
         } else {
            // Folio not provided in URL, throw a 400.
            response.sendError(SC_BAD_REQUEST);
         }
      } catch (SQLException | IllegalArgumentException ex) {
         reportInternalError(response, ex);
      } catch (Error err){
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
             String folioParam = request.getParameter("folioNum");
             if (folioParam != null) {
                Folio f = new Folio(parseInt(folioParam));
                FolioDims pageDim = new FolioDims(f.getFolioNumber(), true);
                Dimension storedDims = getCachedImageDimensions(f.getFolioNumber());
                if(null == storedDims || storedDims.height <=0 || storedDims.width <=0) { //There was no imagecache entry or a bad one we can't use
                    // System.out.println("Need to resolve image headers for dimensions");
                    if (pageDim != null && pageDim.getImageHeight() > 0 && pageDim.getImageWidth() > 0) { //There was no foliodim entry
                        storedDims = new Dimension(pageDim.getImageWidth(), pageDim.getImageHeight());
                    }
                    else{
                        try{
                            storedDims = f.resolveImageForDimensions(); 
                        }
                        catch (java.net.SocketTimeoutException e) {
                            // There was a timeout on the Image URL.  We could not resolve the image for dimensions.
                            // There is no image to return.  Send an error.
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
                response.sendError(400, "Missing \"folioNum=\" parameter");
             }
          } catch(SQLException e) {
             response.sendError(503, e.getMessage());
          }  catch (Error err){
            response.sendError(500, err.getMessage());
          }
    }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "T-PEN Image Resize servlet";
   }
   
   private static final Logger LOG = getLogger(ImageResize.class.getName());
}
