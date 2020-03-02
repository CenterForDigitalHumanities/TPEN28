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
import detectimages.blob;
import static detectimages.blob.drawBlob;
import static detectimages.blob.getBlob;
import static imageLines.ImageCache.getImage;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.io.IOException;
import java.io.OutputStream;
import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static javax.imageio.ImageIO.write;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import match.blobGetter;
import static textdisplay.Folio.getRbTok;

/**
 *
 * @author jdeerin1
 */
public class characterImage extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param milliSeconds
     * @return
     */
    public static String getGMTTimeString(long milliSeconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'");
        return sdf.format(new Date(milliSeconds));
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        response.addHeader("Cache-Control", "max-age=3600");
        long relExpiresInMillis = currentTimeMillis() + (1000 * 2600);
        response.addHeader("Expires", getGMTTimeString(relExpiresInMillis));
        response.setContentType("image/jpeg");
        if (true || request.getParameter("orig") != null) {
            int width, height, x, y;
            String pageIdentifier;
            int blobIdentifier;

            try {
                blobIdentifier = parseInt(request.getParameter("blob"));
                pageIdentifier = request.getParameter("page");
            } catch (NumberFormatException | NullPointerException e) {
                return;
            }
            blobGetter thisBlob = new blobGetter(pageIdentifier, blobIdentifier);
            String s = (getRbTok("SERVERCONTEXT") + "imageResize?folioNum=" + pageIdentifier + "&height=2000");
            out.print(s + "\n");
            BufferedImage originalImg = getImage(parseInt(pageIdentifier));//imageHelpers.readAsBufferedImage(new URL(Folio.getRbTok("SERVERCONTEXT")+"imageResize?folioNum="+pageIdentifier+"&height=2000&code="+Folio.getRbTok("imageCode")));
            width = thisBlob.getHeight();
            height = thisBlob.getWidth();
            x = thisBlob.getX();
            y = thisBlob.getY();
            //scale coordinates based on the fixed 1500 pixel size of the observations
            double factor = originalImg.getHeight() / (double) 2000;
            //factor=1.0;
            width = (int) (width * factor);
            height = (int) (height * factor);
            x = (int) (x * factor);
            y = (int) (y * factor);
            try (OutputStream os = response.getOutputStream()) {
                write(originalImg.getSubimage(x, y, width, height), "jpg", os);
                originalImg = null;
            }
        } else {
            String pageIdentifier;
            int blobIdentifier;

            try {
                blobIdentifier = parseInt(request.getParameter("blob"));
                pageIdentifier = request.getParameter("page");
            } catch (NumberFormatException | NullPointerException e) {
                return;
            }

            //call the blob constructor tha builds the requested blob based on the blob id and MS/page info
            blob thisBlob = getBlob(pageIdentifier + ".txt", blobIdentifier);
            //find the width and height of this blob, so the canvase can be sized properly.
            int height = thisBlob.getHeight();
            int width = thisBlob.getWidth();

            BufferedImage toRender = new BufferedImage(width, height, TYPE_INT_RGB);
            drawBlob(toRender, 0, 0, thisBlob, 0xffffff);
            try ( //blob.drawBlob(thisBlob, toRender);
                    OutputStream os = response.getOutputStream()) {
                write(toRender, "jpg", os);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            getLogger(characterImage.class.getName()).log(SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            getLogger(characterImage.class.getName()).log(SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
