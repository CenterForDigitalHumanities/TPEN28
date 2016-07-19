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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Methods for retrieving information about a single Archive (IPR for example) along with lots of old,
 * deprecated image url code.
 *
 * @author Jon Deering
 */
public class Archive {

   String name;
   Boolean permitBatchProcessing;
   private connectionType connectionMethod = connectionType.none;
   private String uname = "";
   private String pass = "";
   private String cookieURL = "";

   public enum connectionType {

      httpAuth, cookie, none, local
   };

   public URL getCookieURL() throws MalformedURLException {
      return new URL(cookieURL);
   }

   public String getName() {
      return name;
   }

   public String getPass() {
      return pass;
   }

   public String getUname() {
      return uname;
   }

   public connectionType getConnectionMethod() {
      return connectionMethod;
   }

   public Archive() {
   }

   public Archive(String archiveName) throws SQLException {
      name = archiveName;
      String query = "select * from archives where name=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, name);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            pass = rs.getString("pass");
            uname = rs.getString("uname");
            if (pass != null && uname != null && pass.length() > 0 && uname.length() > 0) {
               connectionMethod = connectionType.httpAuth;
            }
            cookieURL = rs.getString("cookieURL");
            if (connectionMethod == connectionType.none && cookieURL != null && cookieURL.length() > 0) {
               connectionMethod = connectionType.cookie;
            }
            if (rs.getBoolean("local")) {
               connectionMethod = connectionType.none;
            }
         }
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   public Boolean permitsBatchProcessing() {
      return false;
   }

   /**
    * Set a message for the Archive.
    *
    * @param msg
    * @throws SQLException
    */
   public void setMessage(String msg) throws SQLException {
      String query = "update archives set message=? where name=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, msg);
         ps.setString(2, this.name);
         ps.execute();
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Get a status message about the repository. Could be something like 'performance is lousy' or 'server
    * is down'
    *
    * @return String
    * @throws SQLException
    */
   public String message() throws SQLException {
      String query = "select message from archives where name=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, name);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            return rs.getString("message");
         }
         return "";
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Retrieve the IPR agreement text that users must agree to in order to view images from this hosting
    * Archive.
    *
    * @return String
    * @throws SQLException
    */
   public String getIPRAgreement() throws SQLException {
      String query = "select eula from archives where name=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, this.name);
         ResultSet rs = ps.executeQuery();
         if (rs.next() && rs.getString("eula").length() > 0) //return ESAPI.encoder().encodeForHTML(rs.getString("eula"));
         {
            return rs.getString("eula");
         }
         //return ESAPI.encoder().encodeForHTML("The IPR agreement for this repository hasn't been added yet. If you click agree, you will have no idea what you are agreeing to.");
         return "The IPR agreement for this repository hasn't been added yet. If you click agree, you will have no idea what you are agreeing to.";
      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * Get the names of all of the hosting archives TPEN is aware of.
    *
    * @return
    * @throws SQLException
    */
   public static String[] getArchives() throws SQLException {
      String[] toret = new String[0];

      String query = "select name from archives";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            String[] tmp = new String[toret.length + 1];
            System.arraycopy(toret, 0, tmp, 0, toret.length);
            tmp[tmp.length - 1] = rs.getString("name");
            toret = tmp;
         }


      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
      return toret;
   }

   /**
    * Set the IPR agreeement text that users must agree to in order to view images from the particular
    * hosting Archive
    *
    * @param agreement
    * @throws SQLException
    */
   public void setIPRAgreement(String agreement) throws SQLException {
      String query = "update archives set eula=? where name=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = DatabaseWrapper.getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, agreement);
         ps.setString(2, this.name);
         ps.execute();

      } finally {
         DatabaseWrapper.closeDBConnection(j);
         DatabaseWrapper.closePreparedStatement(ps);
      }
   }

   /**
    * List all manuscripts TPEN is aware of
    *
    * @return
    * @throws SQLException
    */
   public static String listAllManuscripts() throws SQLException {
      String toret = "";
      String[] cities = Manuscript.getAllCities();
      for (int i = 0; i < cities.length; i++) {
         Manuscript[] mss = Manuscript.getManuscriptsByCity(cities[i]);
         for (int k = 0; k < mss.length; k++) {
            toret += (mss[k].getShelfMark() + " (hosted by " + mss[k].getArchive() + ")\n");
            toret += ("<a href=transcription.jsp?ms=" + mss[k].getID() + ">Start transcribing</a>" + "\n");
            toret += ("<a href=addMStoProject.jsp?ms=" + mss[k].getID() + ">Add to project</a>" + "\n<br>");
         }
      }
      return toret;
   }

   /**
    * @deprecated use Manuscript object instead Build a proper shelfmark for this image
    */
   public static String getShelfMark(String archive) {
      String toret = "";
      if (null != archive && archive.compareTo("ecodices") == 0) {
         toret = "St. Gallen, Stiftsbibliothek, ";
      }
      if (null != archive && archive.compareTo("parkerweb") == 0) {
         toret = "Cambridge, CCC ";
      }
      if (null != archive && archive.compareTo("ENAP") == 0) {
         toret = "Cambridge, CCC ";
      }
      if (null != archive && archive.compareTo("BAV") == 0) {
         toret += "Vatican City, BAV, vat. Pal. lat. ";
      }
      if (null != archive && archive.compareTo("CCL") == 0) {
         toret += "CCL";
      }
      if (null != archive && archive.compareTo("TPEN") == 0) {
         toret += "TPEN";
      }

      return toret;
   }

   /**
    * This function is used to add all manuscripts from an Archive to TPEN. It is not generalized, so
    * modification is required for each new repository.
    *
    * @param archive
    * @param id
    * @param cid
    * @return
    * @throws MalformedURLException
    * @throws IOException
    * @throws SQLException
    */
   public String getAvailableCollectionsFromSite(String archive, int id, int cid) throws MalformedURLException, IOException, SQLException {
      String toret = "junk";
      String urls = "";
      Boolean added = false;








      return urls;
   }

   public static void main(String[] args) throws MalformedURLException, IOException, SQLException {
   }

   public static BufferedReader fetchAndPrepare(String url) throws MalformedURLException, IOException {
      try {
         Thread.sleep(5000);//sleep for 1000 ms
      } catch (InterruptedException ie) {
         //If this thread was intrrupted by nother thread
      }

      try {
         BufferedReader dLR = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));

         if (!dLR.ready()) {
            System.out.print("1Dcoument reader not ready, sleeping .5 seconds!\n");
            try {
               Thread.sleep(500);//sleep for 1000 ms
            } catch (InterruptedException ie) {
               //If this thread was intrrupted by nother thread
            }
         }
         StringBuilder dLRBuilder = new StringBuilder("");
         while (dLR.ready()) {
            dLRBuilder.append(dLR.readLine() + "\n");
         }

         BufferedReader toret = new BufferedReader(new StringReader(dLRBuilder.toString()));
         return toret;
      } catch (Exception e) {
         e.printStackTrace();
      }

      BufferedReader toret = new BufferedReader(new StringReader(""));
      return toret;
   }
}
