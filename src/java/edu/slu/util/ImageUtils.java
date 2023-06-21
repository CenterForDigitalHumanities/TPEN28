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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.IOUtils;


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

        System.out.println("GET JPG DIMENSIONS FROM INPUT STREAM");
        if (input.read() != 255 || input.read() != 216) {
            //If we throw this exception, it breaks through to the front end.  Instead, we would rather the manifest contain a flagged bad canvas
           //throw new IOException("Missing JPEG SOI (Start Of Image) marker.");
           System.out.println("!!!!! MISSING JPEG SOI MARKER !!!!!!");
           return new Dimension(0,0);
        }
        System.out.println("I.S. size: "+input.available());
        System.out.println("I.S. first byte: "+input.read());
        System.out.println("Initialize B.I.S.");
        
//        String suffix = "jpg";
//        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
//        if (iter.hasNext()) {
//            ImageReader reader = iter.next();
//            try {
//                ImageInputStream stream = ImageIO.createImageInputStream(input);
//                System.out.println("set reader input");
//                reader.setInput(stream);
//                int width = reader.getWidth(reader.getMinIndex());
//                int height = reader.getHeight(reader.getMinIndex());
//                System.out.println("Result: "+width+", "+height);
//                return new Dimension(width, height);
//            } catch (Exception e) {
//                System.out.println("New function could not process stream");
//                System.out.println(e);
//            } finally {
//                reader.dispose();
//            }
//        } else {
//            System.out.println("No reader found for given format: " + suffix);
//        }
//        System.out.println("Unable to process image input stream");
//        return new Dimension(0,0);
        
        //InputStream is =  new BufferedInputStream ( input );  
//        ByteArrayInputStream in = new ByteArrayInputStream(IOUtils.toByteArray(input));
//        BufferedImage image = ImageIO.read(in); 
//        System.out.println("Read B.I.S. image info");
//        System.out.println(image);
//        try{
//            if(image.getHeight() > 0){
//                System.out.println("Found a height for this jpg");
//                return new Dimension(image.getWidth(), image.getHeight());
//            }
//            else{
//                System.out.println("jpg height could not be determined.  It will be 0,0");
//                return new Dimension(0, 0);
//            }
//        }
//        catch(Exception e){
//            System.out.println("getJPEGDimensions could not process the image stream.  NullPointer means the stream resulted in a null image.  Dimensions will be 0,0");
//            System.out.println(e);
//            return new Dimension(0, 0);
//        }


        while (input.read() == 255) {
           int marker = input.read();
           int len = input.read() << 8 | input.read();
           if (marker == 192) {
              input.skip(1);
              int h = input.read() << 8 | input.read();
              int w = input.read() << 8 | input.read();
              System.out.println("GOT DIMENSIONS HOORAY!@!!");
              return new Dimension(w, h);
           }
           input.skip(len - 2);
        }
        System.out.println("getJPEGDimensions never found marker 192");
        return new Dimension(0, 0);
   }
   
   private static final Logger LOG = getLogger(ImageUtils.class.getName());
}
