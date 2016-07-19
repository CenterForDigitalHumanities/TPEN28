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
package textdisplay;

import detectimages.line;

/**
 * The Line object is used by folio and Detector to store the information about a parsed Line in an image.
 *
 * @author Jon Deering
 */
public class Line {

   int left;
   int right;
   int top;
   int bottom;

   public Line(int left, int right, int top, int bottom) {
      this.left = left;
      this.right = right;
      this.top = top;
      this.bottom = bottom;
   }

   public Line(line l) {
      this.left = l.getStartHorizontal();
      this.right = l.getWidth();
      this.top = l.getStartVertical();
      this.bottom = l.getDistance() + l.getStartVertical();
   }

   /**
    * Calculate the height of this Line as bottom - top.
    *
    * @return
    */
   public int getHeight() {
      return bottom - top;
   }

   /**
    * Calculate the width
    *
    * @return
    */
   public int getWidth() {
      return right - left;
   }

   /**
    * retrieve the topmost point, aka vertical start
    *
    * @return
    */
   public int getTop() {
      return top;
   }

   /**
    * retrieve the leftmost point, aka horizontal start
    *
    * @return
    */
   public int getLeft() {
      return left;
   }

   /**
    * retrieve the bottom of this Line, vertical end
    *
    * @return
    */
   public int getBottom() {
      return bottom;
   }
}
