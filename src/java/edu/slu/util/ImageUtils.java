/*
 * Copyright 2013-2014 Saint Louis University. Licensed under the
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
package edu.slu.util;

import java.awt.Dimension;
import java.awt.Graphics;
import static java.awt.color.ColorSpace.CS_GRAY;
import static java.awt.color.ColorSpace.getInstance;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;


/**
 * Various image-related utility methods.
 * 
 * @author tarkvara
 */
public class ImageUtils {
   public static final String DEBUG_DIR = "/usr/debugImages";

   /**
    * Clone a BufferedImage.  The more efficient method, using BufferedImage.copyData, fails
	 * for certain ecodices images which apparently have a negative offset.
    * @param img image to be cloned
    * @return a deep copy of the image.
    */
   public static BufferedImage cloneImage(BufferedImage img) {
      int w = img.getWidth();
      int h = img.getHeight();
      return getSubImage(img, 0, 0, w, h);
   }

   /**
    * For some reason, BufferedImage.getSubimage returns something which is incompatible with
    * JAI.  This is a hack to work around that problem.
    */
   public static BufferedImage getSubImage(BufferedImage img, int x, int y, int w, int h) {
      BufferedImage result = new BufferedImage(w, h, img.getType());
      Graphics g = result.getGraphics();
      g.drawImage(img, 0, 0, w, h, x, y, w, h, null);
      g.dispose();
      return result;
   }

   public static BufferedImage convertToGreyscale(BufferedImage src) {
      return new ColorConvertOp(getInstance(CS_GRAY), null).filter(src, null);
   }


   /**
    * Retrieve the dimensions of a JPEG by reading the header bytes.
    * @param input stream containing JPEG data
    * @return dimensions of the JPEG, or <code>null</code> if header bytes not found
    */
    public static Dimension getJPEGDimension(InputStream input) throws IOException {
        // Check for SOI marker.
        if (input.read() != 255 || input.read() != 216) {
            //If we throw this exception, it breaks through to the front end.  Instead, we would rather the manifest contain a flagged bad canvas
           //throw new IOException("Missing JPEG SOI (Start Of Image) marker.");
           LOG.log(SEVERE, "_!_    MISSING JPEG SOI MARKER    _!_");
           return new Dimension(0,0);
        }
        //LOG.log(INFO, "Getting image dimensions.  File size: {0}", input.available());
        while (input.read() == 255) {
           int marker = input.read();
           int len = input.read() << 8 | input.read();
           if (marker == 192) {
              input.skip(1);
              int h = input.read() << 8 | input.read();
              int w = input.read() << 8 | input.read();
              //LOG.log(INFO, "Dimensions are "+w+","+h);
              return new Dimension(w, h);
           }
           input.skip(len - 2);
        }
        LOG.log(WARNING, "getJPEGDimensions never found marker 192.  Dimensions are unknown and will be 0,0");
        return new Dimension(0, 0);
   }
   
   private static final Logger LOG = getLogger(ImageUtils.class.getName());
}
