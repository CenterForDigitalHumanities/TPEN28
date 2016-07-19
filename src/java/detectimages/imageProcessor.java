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
package detectimages;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import imageLines.ImageHelpers;
import static edu.slu.util.ImageUtils.cloneImage;
import static edu.slu.util.ImageUtils.writeDebugImage;

/**
 * @author Jon Deering
 */
public class imageProcessor {

   /** Portion of image width which is necessary to be considered as a column. */
   private static final int MINIMAL_COLUMN_FRACTION = 8;

   /** Smallest gap in pixels which is necessary to consider two separate columns. */
   private static final int MINIMAL_GAP = 20;

   static int thresholdMethod = 0;

   private int maxHeight;
   private BufferedImage orig;
   private BufferedImage untouchedImage;

   /**
    * Constructor for use in real-time line detection.
    *
    * @param img
    * @param h
    */
   public imageProcessor(BufferedImage img, int h) {
      untouchedImage = img;
      orig = img;
      maxHeight = h;
   }

   public static void setThesholdMethod(int meth) {
      thresholdMethod = meth;
   }

   /**
    * Perform column and line detection on the image
    *
    * @param doBlobExtract Use an additional preprocessing method that looks for likely characters, and only
    * uses them in the column/line detection to the exclusion of all other content in the image. Best for
    * microfilm images.
    * @return Vector containing all lines, null in case of unrecoverable error
    */
   public List<line> detectLines(boolean doBlobExtract) {

      orig = ImageHelpers.scale(untouchedImage, maxHeight);
      writeDebugImage(orig, "detectLines.orig");
      BufferedImage stored = ImageHelpers.scale(orig, 1000);
      BufferedImage bin = ImageHelpers.scale(ImageHelpers.binaryThreshold(untouchedImage, 4), maxHeight);
      writeDebugImage(bin, "detectLines.bin");
      for (int i = 0; i < bin.getWidth(); i++) {
         for (int j = 0; j < bin.getHeight(); j++) {
            orig.setRGB(i, j, -1);
         }
      }

      if (doBlobExtract) {
         for (int i = 0; i < bin.getWidth(); i++) {
            for (int j = 0; j < bin.getHeight(); j++) {
               orig.setRGB(i, j, -1);
            }
         }
         List<blob> blobs = new ArrayList<>();
         for (int i = 0; i < bin.getWidth(); i++) {
            for (int j = 0; j < bin.getHeight(); j++) {
               if (bin.getRGB(i, j) != -1) {
                  blob thisOne = new blob(i, j);
                  thisOne.copy = orig;
                  if (blobs.size() % 3 == 0) {
                     thisOne.color = 0xcc0000;
                  }
                  if (blobs.size() % 3 == 1) {
                     thisOne.color = 0x000099;
                  }
                  if (blobs.size() % 3 == 2) {
                     thisOne.color = 0x006600;
                  }
                  thisOne.count(bin, thisOne.getX(), thisOne.getY());
                  if (thisOne.size > 5) {
                     blobs.add(thisOne);
                     if ((thisOne.getSize() - (thisOne.getSize() % 10)) != 5000 && (thisOne.getSize() - (thisOne.getSize() % 10)) != 0) {

                        thisOne.calculateRelativeCoordinates();
                        thisOne.drawBlob(orig, thisOne.color);

                     }
                  }
               }
            }
         }

         for (int i = 0; i < blobs.size(); i++) {
            if ((blobs.get(i).size < 4000)) {
               blob.drawBlob(orig, blobs.get(i).x, blobs.get(i).y, blobs.get(i), 0x000000);
            }
         }
      }

      if (!doBlobExtract) {
         orig = bin;
      }

      List<line> lines = new ArrayList<>();
      createViewableVerticalProfile(orig, "", lines);

      orig = ImageHelpers.scale(orig, 1000);
      if (lines.size() > 3) {
         lines = new ArrayList<>();
         lines.add(new line());
         lines.get(0).setStartHorizontal(0);
         lines.get(0).setStartVertical(0);
         lines.get(0).setWidth(orig.getWidth());
         lines.get(0).setDistance(maxHeight);
      }
      //d.detect();
      List<line> allLines = new ArrayList<>();
      for (int i = 0; i < lines.size(); i++) {
         line col = lines.get(i);
         BufferedImage storedBin = ImageHelpers.binaryThreshold(stored, 4);
         BufferedImage colOnly = storedBin.getSubimage(col.getStartHorizontal(), col.getStartVertical(), col.getWidth(), col.getDistance());
         Detector d = new Detector(colOnly, colOnly);
         //d.debugLabel = imageFile.getName();
         d.detect(true);
         LOG.log(Level.INFO, "total lines in col is {0}", d.lines.size());
         for (int j = 0; j < d.lines.size(); j++) {
            line r = d.lines.get(j);
            allLines.add(r);
            r.setStartHorizontal(r.getStartHorizontal() + col.getStartHorizontal());
            r.setStartVertical(r.getStartVertical() + col.getStartVertical());
//                        r.commitQuery(writer, imageFile.getName());
            if (j % 2 == 1) {
               int color = 0x0000ff;
               stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
            } else {
               int color = 0xff0000;
               stored = highlightBlock(stored, r.getStartHorizontal(), r.getStartVertical() - r.getDistance(), r.getDistance(), r.getWidth(), color);
            }
         }
      }
      writeDebugImage(stored, "detectLines.stored");
      return allLines;
   }

   /**
    * Detect the columns in a binarized image
    *
    * @param bin Binarized version of the image
    * @param fileName Filename to be used for saving debug information
    * @param v Vector to store detected columns
    * @return
    */
   private static BufferedImage createViewableVerticalProfile(BufferedImage bin, String fileName, List<line> v) {
      int white = 0xff000000;
      int black = 0xffffffff;
      BufferedImage toret = cloneImage(bin);
      writeDebugImage(bin, "createViewableVerticalProfile.bin");
      int[] vals = new int[toret.getWidth()];
      for (int i = 0; i < vals.length; i++) {
         vals[i] = 0;
      }
      for (int i = 0; i < toret.getWidth(); i++) {
         for (int j = 0; j < toret.getHeight(); j++) {
            if (toret.getRGB(i, j) == white) {
               vals[i]++;
            }
         }
      }
      int[] ordered = Arrays.copyOf(vals, vals.length);
      Arrays.sort(ordered);
      int x = 0;
      List<line> lines = new ArrayList<>();
      int median = ordered[ordered.length / 6];
      median = median + median / 5;
      for (int i = 0; i < vals.length; i++) {
         if (vals[i] > median) {
            for (int j = 0; j < vals[i]; j++) {
               toret.setRGB(i, j, black);
            }
            for (int j = vals[i]; j < toret.getHeight(); j++) {
               toret.setRGB(i, j, white);
            }
         } else {
            for (int j = 0; j < toret.getHeight(); j++) {
               toret.setRGB(i, j, white);
            }
         }
      }
      writeDebugImage(toret, "createViewableVerticalProfile.toret");
      for (int i = 0; i < vals.length; i++) {
         if (vals[i] <= median) {
            // w.append("0\n");
            if (x > 0) {
               line l = new line();
               l.setStartHorizontal(x);
               l.setWidth(i - x);
               LOG.log(Level.INFO, "I: New line {5}/{0}: {1},{2},{3},{4}", new Object[] { i, l.getStartHorizontal(), l.getStartVertical(), l.getWidth(), l.getDistance(), lines.size() });
               lines.add(l);
               x = 0;
            }
         } else {
            // w.append(""+vals[i]+"\n");
            if (x == 0) {
               x = i;
            }
            if (i == vals.length - 1) {
               line l = new line();
               l.setStartHorizontal(x);
               l.setWidth(i - x);
               lines.add(l);
               LOG.log(Level.INFO, "II: New line {5}/{0}: {1},{2},{3},{4}", new Object[] { i, l.getStartHorizontal(), l.getStartVertical(), l.getWidth(), l.getDistance(), lines.size() });
               x = 0;
            }
         }
      }

      int minColWidth = bin.getWidth() / MINIMAL_COLUMN_FRACTION;
      for (int j = 0; j < lines.size(); j++) {
         line l = lines.get(j);
         //merging needs to consider the magnitude of the difference from median
         if (j > 0) {
            line prev = lines.get(j - 1);
            if (l.getStartHorizontal() - prev.getStartHorizontal() - prev.getWidth() < MINIMAL_GAP && (l.getWidth() < minColWidth || prev.getWidth() < minColWidth || l.getStartHorizontal() - prev.getStartHorizontal() - prev.getWidth() < 3)) {
               prev.setWidth(l.getStartHorizontal() + l.getWidth() - prev.getStartHorizontal());
               lines.remove(l);
               j--;
            } else {
               // System.out.print(" gap is "+l.getStartHorizontal()+" "+ prev.getStartHorizontal()+" "+prev.getWidth()+"\n");
            }
         }

      }
      for (int j = 0; j < lines.size(); j++) {
         line l = lines.get(j);
         //if this is a small column try to merge it
         if (l.getWidth() < minColWidth) {
            /*if(l.getWidth()!=0)
             {
             finalizedLines.add(l);
             l.setStartVertical(0);
             l.setDistance(bin.getHeight()/2);
             imageHelpers.highlightBlock(bin,l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0x0000ff);
             }*/
         } else {
            v.add(l);
            l.setStartVertical(0);
            l.setDistance(bin.getHeight() - 1);

            writeDebugImage(highlightBlock(bin, l.getStartHorizontal(), l.getStartVertical(), l.getDistance(), l.getWidth(), 0xff0000), "createViewableVerticalProfile.column" + (v.size() - 1));
         }
      }

      return toret;
   }

   /**
    * Highlight a portion of the image im the requested color
    *
    * @param img
    * @param x
    * @param y
    * @param height
    * @param width
    * @param color
    * @return
    */
   private static BufferedImage highlightBlock(BufferedImage img, int x, int y, int height, int width, int color) {

      for (int i = (int) (x); i <= x + width; i++) {
         for (int j = (int) (y); j <= y + height; j++) {
            try {
               if (i == x + width || j == y + height || j == y || i == x) {
                  img.setRGB(i, j, 0x000000);
               } else {
                  img.setRGB(i, j, (int) (color ^ img.getRGB(i, j)));
               }

            } //if there was an out of bounds its because of the size doubling, ignore it
            catch (ArrayIndexOutOfBoundsException e) {
            }
         }
      }
      return img;
   }
   
   private static final Logger LOG = Logger.getLogger(imageProcessor.class.getName());
}
