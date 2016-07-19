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


/**
 * This class stores the information needed to draw a line. It also holds a method to draw this line on a
 * buffered image
 *
 * @author Jon Deering
 */
public class line {

   private int width;
   private int startHorizontal;
   private int startVertical;
   private int distance;

   public line() {
   }

   public line(int w, int hor, int ver) {
      width = w;
      startHorizontal = hor;
      startVertical = ver;
      distance = 0;
   }

   public line(int w, int hor, int ver, int dist) {
      width = w;
      startHorizontal = hor;
      startVertical = ver;
      distance = dist;

   }

   public BufferedImage drawLine(BufferedImage img, int color) {
      if (width > 0) {
         for (int i = 0; i < width; i++) {
            img.setRGB(i + startHorizontal, startVertical, color);
         }
      }
      return img;
   }

   public void setWidth(int w) {
      width = w;
   }

   public void setStartHorizontal(int hor) {
      startHorizontal = hor;
   }

   public void setStartVertical(int ver) {
      startVertical = ver;
   }

   public void setDistance(int d) {
      distance = d;
   }

   public int getStartHorizontal() {
      return startHorizontal;
   }

   public int getStartVertical() {
      return startVertical;
   }

   public int getEndHorizontal() {
      return startHorizontal + width;
   }

   public int getEndVertical() {
      return startVertical + distance;
   }

   public int getWidth() {
      return width;
   }

   public int getDistance() {
      return distance;
   }
   
   @Override
   public String toString() {
      return String.format("line(%d,%d,%d,%d)", startHorizontal, startVertical, width, distance);
   }
}
