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
package ImageUpload;

import static imageLines.ImageHelpers.readAsBufferedImage;
import static imageLines.ImageHelpers.scale;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.out;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Stack;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import static javax.imageio.ImageIO.write;
import textdisplay.Folio;
import static textdisplay.Folio.createFolioRecords;
import static textdisplay.Folio.getRbTok;
import textdisplay.Manuscript;
import user.User;

/**
 * This class handles the process of creating a manuscript and project from a user uploaded zip file full of
 * JPEG images.
 */
public class UserImageCollection {

   /**
    * Delete a manuscript created on a set of private images
    *
    * @param m the manuscript to delete. Manuscript.getArchive must be "private"
    * @throws SQLException
    */
   public static void delete(Manuscript m) throws SQLException {
      if (m.getArchive().compareTo("private") != 0) {
         return;
      }

      Folio[] fols = m.getFolios();
        for (Folio fol : fols) {
            //delete the image
            File tmp = new File(fol.getImageName());
            if (tmp.exists()) {
                tmp.delete();
            }
            /*@TODO consider implementing deeper deletion of the other data*/
            //remove the Folio record
            
            //remove any project references to this
            
            //remove any transcriptions that reference this
        }
        //now delete the Manuscript record
   }

   private static File[] removeItem(File[] old, int item) {
      File[] res = new File[old.length - 1];
      int pos = 0;
      for (int i = 0; i < old.length; i++) {
         if (i != item) {
            res[pos] = old[i];
            pos++;
         }
      }
      return res;
   }

   /**
    * Unzip the images in the zip file, put them in the proper location, and create folios records for them
    *
    * @param zippedFile name of the zip file without path
    * @param uploader uploading user
    * @param ms manuscript created to contain these images
    * @throws Exception
    */
   public static void create(Connection conn, File zippedFile, User uploader, Manuscript ms) throws Exception {
      String directory = getRbTok("uploadLocation");
      File dir = new File(directory + "/" + uploader.getLname() + "/" + ms.getID());
      if (!dir.exists()) {
         dir.mkdirs();

      }

      File newZippedFile = new File(dir.getAbsoluteFile() + "/" + zippedFile.getName());
      zippedFile.renameTo(newZippedFile);
      zippedFile = newZippedFile;
      extractFolder(zippedFile.getAbsolutePath());
      File[] images = getAllJPGsRecursive(dir);
      for (int i = 0; i < images.length; i++) {
         if (!validateImage(images[i])) {
                out.print("bad image "+images[i].getName()+", would do something\n");
            images = removeItem(images, i);
            i--;
         }

      }
        createFolioRecords(conn, ms.getCollection(), images, "private", ms.getID(), "");
   }

   /**
    * Recurse through this directory finding all images in whatever folders are in it
    *
    * @param dir the directory to look in
    * @return
    */
   public static File[] getAllJPGsRecursive(File dir) {
      Stack<File> ret = getJPGSInFolder(dir);
      File[] toret = new File[ret.size()];
      for (int i = 0; i < toret.length; i++) {
         toret[i] = ret.pop();
      }
      return toret;
   }

   /**
    * Find all jpgs in this folder and all subfolders
    *
    * @param dir dir the directory to look in
    * @return
    */
   public static Stack<File> getJPGSInFolder(File dir) {
      Stack<File> res = new Stack();
      File[] allFiles = dir.listFiles();
        for (File allFile : allFiles) {
            if (allFile.getName().toLowerCase().contains(".jpg")) {
                res.add(allFile);
            } else {
                if (allFile.isDirectory()) {
                    res = merge(res, getJPGSInFolder(allFile));
                }
            }
        }
      return res;
   }

   /**
    * Helper method to merge 2 stacks
    */
   private static Stack merge(Stack a, Stack b) {
      Stack c = new Stack();
      while (!a.empty()) {
         c.push(a.pop());
      }
      while (!b.empty()) {
         c.push(b.pop());
      }
      return c;

   }

   /**
    * This function was created as an answer to the stack overflow question
    * http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java I decided not to
    * reinvent the wheel
    *
    * @param zipFile The location of the zip file
    * @throws ZipException on incomplete uploads etc
    * @throws IOException if the file doesnt exist..
    */
   static public void extractFolder(String zipFile) throws ZipException, IOException {
        out.println(zipFile);
      int BUFFER = 2048;
      File file = new File(zipFile);

      ZipFile zip = new ZipFile(file);
      String newPath = zipFile.substring(0, zipFile.length() - 4);

      new File(newPath).mkdir();
      Enumeration zipFileEntries = zip.entries();

      // Process each entry
      while (zipFileEntries.hasMoreElements()) {
         // grab a zip file entry
         ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
         String currentEntry = entry.getName();
         File destFile = new File(newPath, currentEntry);
         //destFile = new File(newPath, destFile.getName());
         File destinationParent = destFile.getParentFile();

         // create the parent directory structure if needed
         destinationParent.mkdirs();

         if (!entry.isDirectory()) {
            try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry))) {
               int currentByte;
               // establish buffer for writing file
               byte data[] = new byte[BUFFER];

               // write the current file to disk
               FileOutputStream fos = new FileOutputStream(destFile);
               try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                  while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                     dest.write(data, 0, currentByte);
                  }
                  dest.flush();
               }
            }
         }

         if (currentEntry.endsWith(".zip")) {
            // found a zip file, try to open
            extractFolder(destFile.getAbsolutePath());
         }
      }
   }

   /**
    * Check that the image is actually a valid jpg image by loading it as a BufferedImage
    * BH TODO strip invalid characters!
    */
   private static Boolean validateImage(File f) {
      try {
         BufferedImage img = readAsBufferedImage(f.getAbsolutePath());
         img = scale(img, 2000);
            write(img, "jpg", f);
      } catch (Exception e) {
         LOG.log(SEVERE, e.getMessage());
         return false;
      }
      return true;
   }
   
   private static final Logger LOG = getLogger(UserImageCollection.class.getName());
}
