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
import java.util.regex.Pattern;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Level.INFO;
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
 * This class handles the process of creating a manuscript and project from a
 * user uploaded zip file full of JPEG images.
 */
public class UserImageCollection {
    
    // Compiled regex patterns for filename sanitization (performance optimization)
    private static final Pattern SPACES_AND_DOTS_PATTERN = Pattern.compile("[\\s.]+");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9_-]+");
    private static final Pattern MULTIPLE_DASHES_PATTERN = Pattern.compile("-+");
    private static final Pattern LEADING_TRAILING_DASHES_PATTERN = Pattern.compile("^-|-$");

    /**
     * Delete a manuscript created on a set of private images
     *
     * @param m the manuscript to delete. Manuscript.getArchive must be
     * "private"
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
     * Unzip the images in the zip file, put them in the proper location, and
     * create folios records for them
     *
     * @param conn MySQL connection
     * @param zippedFile name of the zip file without path
     * @param uploader uploading user
     * @param ms manuscript created to contain these images
     * @throws Exception
     */
    public static void create(Connection conn, File zippedFile, User uploader, Manuscript ms) throws Exception {
        String directory = getRbTok("uploadLocation");
        File dir = new File(directory + "images/userimages/" + uploader.getLname() + "/" + ms.getID());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File newZippedFile = new File(dir.getAbsoluteFile() + "/" + zippedFile.getName());
        
        // Check if the zip file already exists to prevent overwriting
        // Use canonical paths for reliable comparison
        try {
            if (newZippedFile.exists() && !newZippedFile.getCanonicalPath().equals(zippedFile.getCanonicalPath())) {
                LOG.log(WARNING, "Zip file already exists at destination: {0}", newZippedFile.getAbsolutePath());
                throw new IOException("Cannot overwrite existing zip file: " + newZippedFile.getName());
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot overwrite")) {
                throw e;
            }
            LOG.log(WARNING, "Unable to get canonical path for file comparison, proceeding with caution", e);
        }
        
        zippedFile.renameTo(newZippedFile);
        zippedFile = newZippedFile;
        
        try {
            extractFolder(zippedFile.getAbsolutePath());
        } catch (Exception e) {
            LOG.log(SEVERE, "Failed to extract zip file: " + zippedFile.getAbsolutePath(), e);
            throw new Exception("Failed to extract uploaded zip file", e);
        }
        
        File[] images = getAllJPGsRecursive(dir);
        
        if (images == null || images.length == 0) {
            LOG.log(WARNING, "No valid JPG images found in uploaded zip file for manuscript {0}", ms.getID());
            throw new Exception("No valid JPG images found in the uploaded zip file");
        }
        
        // Validate all images before creating folio records
        int validImageCount = 0;
        for (int i = 0; i < images.length; i++) {
            if (!validateImage(images[i])) {
                LOG.log(WARNING, "Invalid image removed: {0}", images[i].getName());
                // Delete the invalid image file
                if (images[i].exists()) {
                    images[i].delete();
                }
                images = removeItem(images, i);
                i--;
            } else {
                validImageCount++;
            }
        }
        
        if (validImageCount == 0) {
            LOG.log(SEVERE, "No valid images after validation for manuscript {0}", ms.getID());
            throw new Exception("All images failed validation. No valid images to process.");
        }
        
        LOG.log(INFO, "Successfully validated {0} images for manuscript {1}", new Object[]{validImageCount, ms.getID()});
        createFolioRecords(conn, ms.getCollection(), images, "private", ms.getID(), "");
    }

    /**
     * Recurse through this directory finding all images in whatever folders are
     * in it
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
            if (allFile.getName().toLowerCase().endsWith(".jpg")) {
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
     * http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
     * I decided not to reinvent the wheel
     *
     * @param zipFile The location of the zip file
     * @throws ZipException on incomplete uploads etc
     * @throws IOException if the file doesnt exist..
     */
    static public void extractFolder(String zipFile) throws ZipException, IOException {
        LOG.log(INFO, "Extracting folder: {0}", zipFile);
        int BUFFER = 2048;
        File file = new File(zipFile);

        try (ZipFile zip = new ZipFile(file)) {
            String newPath = zipFile.substring(0, zipFile.length() - 4);
            // scrub dirname
            newPath = newPath.trim().replaceAll("\\s|\\.", "_");

            new File(newPath).mkdir();
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();

            // lets not get fooled by a true jpg
            currentEntry = currentEntry.replaceAll("(?i)\\.jpe?g\\b", ".jpg");

            if (currentEntry.endsWith(".jpg") && !entry.isDirectory() && (entry.getSize() > 2000)) {

                // scrub filenames - replace spaces and dots (except the final .jpg) with dashes
                currentEntry = sanitizeFilename(currentEntry);

                File destFile = new File(newPath, currentEntry);
                
                // Check if file already exists to prevent overwriting
                if (destFile.exists()) {
                    LOG.log(WARNING, "File already exists, skipping: {0}", destFile.getAbsolutePath());
                    continue;
                }
                
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

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
                } catch (IOException e) {
                    LOG.log(SEVERE, "Failed to extract file: " + destFile.getAbsolutePath(), e);
                    // Clean up partial file if extraction failed
                    if (destFile.exists()) {
                        destFile.delete();
                    }
                    throw e;
                }
            }
        }
        } catch (IOException e) {
            LOG.log(SEVERE, "Failed to process zip file: " + zipFile, e);
            throw e;
        }
    }

    /**
     * Sanitize filename by replacing problematic characters.
     * Removes or replaces spaces, dots (except the final .jpg extension), 
     * and other special characters that could cause issues.
     * 
     * @param filename The original filename
     * @return The sanitized filename, or the original if null/empty
     */
    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        
        // Split filename and extension
        String name = filename;
        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            String potentialExtension = filename.substring(lastDotIndex).toLowerCase();
            // Validate extension is a known image type (only .jpg supported currently)
            if (potentialExtension.equals(".jpg") || potentialExtension.equals(".jpeg")) {
                name = filename.substring(0, lastDotIndex);
                extension = filename.substring(lastDotIndex);
            } else {
                // If not a valid image extension, treat the whole filename as name
                // This prevents files like "document.pdf" from being treated as images
                name = filename;
                extension = "";
            }
        }
        
        // Handle directory separators (keep them)
        String[] pathParts = name.split("[/\\\\]");
        for (int i = 0; i < pathParts.length; i++) {
            // Replace spaces, dots, and other problematic characters with dashes
            // Keep alphanumeric, hyphens, and underscores
            String part = pathParts[i].trim();
            part = SPACES_AND_DOTS_PATTERN.matcher(part).replaceAll("-");
            part = SPECIAL_CHARS_PATTERN.matcher(part).replaceAll("-");
            part = MULTIPLE_DASHES_PATTERN.matcher(part).replaceAll("-");
            pathParts[i] = LEADING_TRAILING_DASHES_PATTERN.matcher(part).replaceAll("");
        }
        
        // Rejoin path parts
        name = String.join("/", pathParts);
        
        return name + extension;
    }

    /**
     * Check that the image is actually a valid jpg image by loading it as a
     * BufferedImage and re-saving it as JPG.
     * 
     * NOTE: This system only supports JPG format for private uploads as documented
     * in the upload UI. Images are always scaled to max 2000px height and saved as JPG.
     */
    private static Boolean validateImage(File f) {
        try {
            BufferedImage img = readAsBufferedImage(f.getAbsolutePath());
            if (img == null) {
                LOG.log(WARNING, "Failed to read image as BufferedImage: {0}", f.getAbsolutePath());
                return false;
            }
            
            // Validate image dimensions are reasonable
            if (img.getWidth() <= 0 || img.getHeight() <= 0) {
                LOG.log(WARNING, "Image has invalid dimensions: {0}x{1} for file: {2}", 
                    new Object[]{img.getWidth(), img.getHeight(), f.getAbsolutePath()});
                return false;
            }
            
            // Scale image to max 2000px height and save as JPG (system only supports JPG)
            img = scale(img, 2000);
            write(img, "jpg", f);
        } catch (Exception e) {
            LOG.log(SEVERE, "Failed to validate image: " + f.getAbsolutePath(), e);
            return false;
        }
        return true;
    }

    private static final Logger LOG = getLogger(UserImageCollection.class.getName());
}
