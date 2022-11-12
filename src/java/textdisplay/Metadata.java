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
package textdisplay;

import static java.lang.System.out;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;
import user.Group;
import user.User;
import static utils.JsonHelper.buildNoneLanguageMap; 

/**
 *
 * A class for storing Project Metadata.
 */
public class Metadata {

   int projectID = -1;
   String title;
   String subtitle;

   public String getSubject() {
      return subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }
   String msIdentifier;
   String msSettlement;
   String msRepository;
   String msCollection = "";
   String msIdNumber;
   String author;
   String date;
   String language;
   String description;
   String location;
   String subject;

   public String getAuthor() {
      return author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public String getDate() {
      return date;
   }

   public void setDate(String date) {
      this.date = date;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getLanguage() {
      return language;
   }

   public void setLanguage(String language) {
      this.language = language;
   }

   public String getLocation() {
      return location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public void setMsCollection(String msCollection) {
      this.msCollection = msCollection;
   }

   public void setMsIdNumber(String msIdNumber) {
      this.msIdNumber = msIdNumber;
   }

   public void setMsIdentifier(String msIdentifier) {
      this.msIdentifier = msIdentifier;
   }

   public void setMsRepository(String msRepository) {
      this.msRepository = msRepository;
   }

   public void setMsSettlement(String msSettlement) {
      this.msSettlement = msSettlement;
   }

   public void setProjectID(int projectID) {
      this.projectID = projectID;
   }

   public void setSubtitle(String subtitle) {
      this.subtitle = subtitle;
   }

   public void setTitle(String title) throws SQLException {
      this.title = title;
      this.commit();
   }

   public String getMsCollection() {
      return msCollection;
   }

   public String getMsIdNumber() {
      return msIdNumber;
   }

   public String getMsIdentifier() {
      return msIdentifier;
   }

   public String getMsRepository() {
      return msRepository;
   }

   public String getMsSettlement() {
      return msSettlement;
   }

   public int getProjectID() {
      return projectID;
   }

   public String getSubtitle() {
      return subtitle;
   }

   public String getTitle() {
      return title;
   }

   /**
    * Creates a new metadata record for a project.
    */
   public Metadata(Connection conn, int projID, String t) throws SQLException {
      try (PreparedStatement ps = conn.prepareStatement("INSERT INTO metadata (title, subtitle, "
              + "msIdentifier, msSettlement, msRepository, msIdNumber, msCollection, projectID, subject, "
              + "description, `date`, language, location, author) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
         ps.setString(1, title = t);
         ps.setString(2, subtitle = " ");
         ps.setString(3, msIdentifier = " ");
         ps.setString(4, msSettlement = " ");
         ps.setString(5, msRepository = " ");
         ps.setString(6, msIdNumber = " ");
         ps.setString(7, msCollection = " ");
         ps.setInt(8, projectID = projID);
         ps.setString(9, subject = " ");
         ps.setString(10, description = " ");
         ps.setString(11, date = "");
         ps.setString(12, language = "");
         ps.setString(13, location = "");
         ps.setString(14, author = "");
         ps.execute();
      }
   }

   /**
    * Fetch the metadata for the given project.
    *
    * @param projectID
    * @throws SQLException
    */
   public Metadata(int projectID) throws SQLException {
      String query = "select * from metadata where projectID=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            this.projectID = projectID;
            title = rs.getString("title");
            subtitle = rs.getString("subtitle");
            //we could decode here.
          //  System.out.println("title from metadata table for project "+projectID+" put into Metadata object: "+title);
            msIdentifier = rs.getString("msIdentifier");
            msSettlement = rs.getString("msSettlement");
            msRepository = rs.getString("msRepository");
            msIdNumber = rs.getString("msIdNumber");
            subject = rs.getString("subject");
            date = rs.getString("date");
            language = rs.getString("language");
            author = rs.getString("author");
            description = rs.getString("description");
            location = rs.getString("location");
            msCollection = rs.getString("msCollection");
         }
      } finally {
         if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(ps);
         }
      }
   }
   
    /**
    * Fetch the metadata for the given project, returning it in JSON format.
    *
    * @param projectID
    * @throws SQLException
    */
   public Metadata(int projectID, boolean jaysohn) throws SQLException {
      
   }

   /**
    * netbean constructor, can be used to populate values and commit or set projid and build
    */
   public Metadata() {
   }

   public void commit() throws SQLException {
      String query = "update metadata set title=?, subtitle=?, msIdentifier=?, msSettlement=?, msRepository=?, msIdNumber=?, subject=?, author=?, `date`=?, location=?, description=?, language=?, msCollection=? where projectID=? ";
      Connection j = null;
      PreparedStatement updater = null;
      try {
         j = getConnection();
         updater = j.prepareStatement(query);
         //what if we did an ecode here for the specified fields and decoded them on the way out?  Or leave them encoded and let the front end decode?
         //System.out.println("commit new title "+this.title);
         updater.setString(1, this.title);
         updater.setString(2, this.subtitle);
         updater.setString(3, this.msIdentifier);
         updater.setString(4, this.msSettlement);
         updater.setString(5, this.msRepository);
         updater.setString(6, this.msIdNumber);
         updater.setString(7, this.subject);
         updater.setString(8, this.author);
         updater.setString(9, this.date);
         updater.setString(10, this.location);
         updater.setString(12, this.language);
         updater.setString(11, this.description);
         updater.setString(13, this.msCollection);
         updater.setInt(14, projectID);
         updater.execute();
      } catch (Exception e) {
      } finally {
            closeDBConnection(j);
            closePreparedStatement(updater);
      }

   }
   
   public static JSONArray getMetadataAsJSON(int projectID) throws SQLException{
        String query = "select * from metadata where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        j = getConnection();
        ps = j.prepareStatement(query);
        ps.setInt(1, projectID);
        ResultSet rs = ps.executeQuery();
        JSONArray metadata = new JSONArray();
        JSONObject metadata_entry = new JSONObject();
        if (rs.next()) {            
           String title = rs.getString("title");
           metadata_entry.element("label", "title");
           metadata_entry.element("value", title);
           metadata.add(metadata_entry);

           String subtitle = rs.getString("subtitle");
           metadata_entry.element("label", "subtitle");
           metadata_entry.element("value", subtitle);
           metadata.add(metadata_entry);

           String msIdentifier = rs.getString("msIdentifier");
           metadata_entry.element("label", "msIdentifier");
           metadata_entry.element("value", msIdentifier);
           metadata.add(metadata_entry);

           String msSettlement = rs.getString("msSettlement");
           metadata_entry.element("label", "msSettlement");
           metadata_entry.element("value", msSettlement);
           metadata.add(metadata_entry);

           String msRepository = rs.getString("msRepository");
           metadata_entry.element("label", "msRepository");
           metadata_entry.element("value", msRepository);
           metadata.add(metadata_entry);

           String msIdNumber = rs.getString("msIdNumber");
           metadata_entry.element("label","msIdNumber");
           metadata_entry.element("value", msIdNumber);
           metadata.add(metadata_entry);

           String subject = rs.getString("subject");
           metadata_entry.element("label", "subject");
           metadata_entry.element("value", subject);
           metadata.add(metadata_entry);

           String date = rs.getString("date");
           metadata_entry.element("label", "date");
           metadata_entry.element("value", date);
           metadata.add(metadata_entry);

           String language = rs.getString("language");
           metadata_entry.element("label", "language");
           metadata_entry.element("value", language);
           metadata.add(metadata_entry);

           String author = rs.getString("author");
           metadata_entry.element("label", "author");
           metadata_entry.element("value", author);
           metadata.add(metadata_entry);

           String description = rs.getString("description");
           metadata_entry.element("label", "description");
           metadata_entry.element("value", description);
           metadata.add(metadata_entry);

           String location = rs.getString("location");
           metadata_entry.element("label", "location");
           metadata_entry.element("value", location);
           metadata.add(metadata_entry);

           String msCollection = rs.getString("msCollection");
           metadata_entry.element("label", "msCollection");
           metadata_entry.element("value", msCollection);
           metadata.add(metadata_entry);

        }
        closeDBConnection(j);
        closePreparedStatement(ps);
        return metadata;
  }
   
   
   public static JSONArray getMetadataAsJSON(int projectID, String profile) throws SQLException{
	String query = "select * from metadata where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        j = getConnection();
        ps = j.prepareStatement(query);
        ps.setInt(1, projectID);
        ResultSet rs = ps.executeQuery();
        JSONArray metadata = new JSONArray();
        JSONObject metadata_entry = new JSONObject();
        if (rs.next()) {            
           String title = rs.getString("title");
           metadata_entry.element("label", buildNoneLanguageMap("title"));
           metadata_entry.element("value", buildNoneLanguageMap(title));
           metadata.add(metadata_entry);

           String subtitle = rs.getString("subtitle");
           metadata_entry.element("label", buildNoneLanguageMap("subtitle"));
           metadata_entry.element("value", buildNoneLanguageMap(subtitle));
           metadata.add(metadata_entry);

           String msIdentifier = rs.getString("msIdentifier");
           metadata_entry.element("label", buildNoneLanguageMap("msIdentifier"));
           metadata_entry.element("value", buildNoneLanguageMap(msIdentifier));
           metadata.add(metadata_entry);

           String msSettlement = rs.getString("msSettlement");
           metadata_entry.element("label", buildNoneLanguageMap("msSettlement"));
           metadata_entry.element("value", buildNoneLanguageMap(msSettlement));
           metadata.add(metadata_entry);

           String msRepository = rs.getString("msRepository");
           metadata_entry.element("label", buildNoneLanguageMap("msRepository"));
           metadata_entry.element("value", buildNoneLanguageMap(msRepository));
           metadata.add(metadata_entry);

           String msIdNumber = rs.getString("msIdNumber");
           metadata_entry.element("label", buildNoneLanguageMap("msIdNumber"));
           metadata_entry.element("value", buildNoneLanguageMap(msIdNumber));
           metadata.add(metadata_entry);

           String subject = rs.getString("subject");
           metadata_entry.element("label", buildNoneLanguageMap("subject"));
           metadata_entry.element("value", buildNoneLanguageMap(subject));
           metadata.add(metadata_entry);

           String date = rs.getString("date");
           metadata_entry.element("label", buildNoneLanguageMap("date"));
           metadata_entry.element("value", buildNoneLanguageMap(date));
           metadata.add(metadata_entry);

           String language = rs.getString("language");
           metadata_entry.element("label", buildNoneLanguageMap("language"));
           metadata_entry.element("value", buildNoneLanguageMap(language));
           metadata.add(metadata_entry);

           String author = rs.getString("author");
           metadata_entry.element("label", buildNoneLanguageMap("author"));
           metadata_entry.element("value", buildNoneLanguageMap(author));
           metadata.add(metadata_entry);

           String description = rs.getString("description");
           metadata_entry.element("label", buildNoneLanguageMap("description"));
           metadata_entry.element("value", buildNoneLanguageMap(description));
           metadata.add(metadata_entry);

           String location = rs.getString("location");
           metadata_entry.element("label", buildNoneLanguageMap("location"));
           metadata_entry.element("value", buildNoneLanguageMap(location));
           metadata.add(metadata_entry);

           String msCollection = rs.getString("msCollection");
           metadata_entry.element("label", buildNoneLanguageMap("msCollection"));
           metadata_entry.element("value", buildNoneLanguageMap(msCollection));
           metadata.add(metadata_entry);

        }
        closeDBConnection(j);
        closePreparedStatement(ps);
        return metadata;
   }

   /**
    * Generate a TEI header from the Metadata TPEN stores
    */
   public String getTEI() throws SQLException {
      String toret = "";
      toret += "<teiHeader>\n<fileDesc>\n<titleStmt>\n<title type=\"main\">" + this.title + "</title>\n";
      toret += "<title type=\"sub\">" + this.subtitle + "</title>\n";
      Project thisProject = new Project(this.projectID);
      Group g = new Group(thisProject.groupID);
      User[] members = g.getMembers();

      toret += "<respStmt><resp>Contributor</resp>\n";
        for (User member : members) {
            toret += "<name>" + member.getFname() + " " + member.getLname() + "</name>\n";
        }
      toret += "\n</respStmt>\n";

      toret += "</titleStmt>\n";

      toret += "<sourceDesc>\n";
      toret += "<msDesc>\n";
      toret += "<msIdentifier>\n";
      if (this.msSettlement.length() > 2) {
         toret += "<settlement>" + this.msSettlement + "</settlement>\n";
         toret += "<repository>" + this.msRepository + "</repository>\n";
         toret += "<collection>" + this.msCollection + "</collection>\n";
         toret += "<idno>" + this.msIdNumber + "</idno>\n";
         toret += "<altIdentifier>" + this.msIdentifier + "</altIdentifier>\n";
      } else {
         Manuscript f = new Manuscript(thisProject.firstPage());
         toret += "<settlement>" + f.getCity() + "</settlement>\n";
         toret += "<repository>" + f.getRepository() + "</repository>\n";
         toret += "<collection>" + f.getCollection() + "</collection>\n";
         toret += "<idno>" + f.getShelfMark() + "</idno>\n";

      }

      toret += "</msIdentifier>\n";
      toret += "</msDesc>\n";
      toret += "</sourceDesc>\n";
      toret += "</fileDesc>\n";
      toret += "</teiHeader>";
      return toret;
   }

   public static void main(String[] args) throws SQLException {
        out.print(new Metadata(611).getTEI());
   }

   public String getDublinCore() throws SQLException {
      Project thisProject = new Project(this.projectID);
      Group g = new Group(thisProject.groupID);
      Manuscript f = new Manuscript(thisProject.firstPage());
      String toret = "";
      toret += "<metadata>";
      toret += "<dc:title>" + this.title;
      toret += "</dc:title>\n";
      toret += "<dc:identifier>" + f.getShelfMark();
      toret += "</dc:identifier>\n";
      toret += "</metadata>";
      return toret;
   }
}
