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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.*;
import org.owasp.esapi.ESAPI;

/**
 * This class handles saving and retrieving portions of a single Line of a
 * Transcription in progress, as well as storing the coordinates that
 * Transcription is associated with.
 *
 * @author Jon Deering
 */
public class Transcription {

    private String text;
    private String comment;

    private int folio;
    private int line;
    private Timestamp date;
    private String timestamp;
    public int UID;//should use a user object rather than the user id?
    private int projectID = -1;
    //the coordinates this Transcription is attached to
    private int x;
    private int y;
    private int height;
    private int width;

    private int lineID;

    /**
     * Create a new Transcription specifying creator and project id
     *
     * @param uid user responsible for creating this transcriptions
     * @param projID project ID (possibly 0)
     * @param folioID ID of folio to which this transcription is being added
     * @param t initial text of transcription
     * @param c initial text of note/comment
     * @param r bounding box for transcription
     * @throws SQLException
     * @throws java.io.IOException
     */
    public Transcription(final int uid, final int projID, final int folioID, final String t, final String c, final Rectangle r) throws SQLException, IOException {
        try (Connection j = DatabaseWrapper.getConnection()) {
            try (PreparedStatement ps = j.prepareStatement("INSERT INTO transcription (creator, projectID, folio, x, y, width, height, comment, text, line, date) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, -1, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                UID = uid;
                projectID = projID;
                folio = folioID;
                x = r.x;
                y = r.y;
                width = r.width;
                height = r.height;
                text = t;
                comment = c;
                date = new java.sql.Timestamp(System.currentTimeMillis());
                ps.setInt(1, UID);
                ps.setInt(2, projectID);
                ps.setInt(3, folio);
                ps.setInt(4, x);
                ps.setInt(5, y);
                ps.setInt(6, width);
                ps.setInt(7, height);
                ps.setString(8, text);
                ps.setString(9, comment);
                ps.setTimestamp(10, date);
                ps.execute();
                final ResultSet rs = ps.getGeneratedKeys();
                //the Line id is an auto increment field, so get that and store it here.
                if (rs.next()) {
                    lineID = rs.getInt(1);
                }
                //         TranscriptionIndexer.add(this);
            }
        }
    }

    /**
     * Update a transcription based on values extracted from a JSON import.
     *
     * @param cont String new text content
     * @param bounds Rectangle new annotation boundary
     * @throws java.sql.SQLException
     */
    public void update(final String cont, final Rectangle bounds) throws SQLException {
        if (!cont.equals(text) || bounds.x != x || bounds.y != y || bounds.width != width || bounds.height != height) {
            text = cont;
            x = bounds.x;
            y = bounds.y;
            width = bounds.width;
            height = bounds.height;
            commit();
        }
    }

    public void setComment(final String comm) throws SQLException {
        comment = comm;
        commit();
    }

    public void setText(final String tt) throws SQLException {
        text = tt;
        commit();
    }

    public void setHeight(final int h) throws SQLException {
        height = h;
        commit();
    }

    public void setWidth(final int w) throws SQLException {
        width = w;
        commit();
    }

    public void setX(final int val) throws SQLException {
        x = val;
        commit();
    }

    public void setY(final int val) throws SQLException {
        y = val;
        commit();
    }

    public int getHeight() {
        return height;
    }

    public int getLineID() {
        return lineID;
    }

    public int getProjectID() {
        return projectID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Timestamp getDate() {
        return date;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getComment() {
        return comment;
    }

    public int getCreator() {
        return UID;
    }

    public void setCreator(final int uid) {
        this.UID = uid;
        try {
            this.commit();
        } catch (final SQLException ex) {
            Logger.getLogger(Transcription.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getFolio() {
        return folio;
    }

    public int getLine() {
        return line;
    }

    /**
     * Return a count of all lines transcribed
     *
     * @return int number of lines
     * @throws java.sql.SQLException
     */
    public static int getNumberOfTranscribedLines() throws SQLException {
        int toret = 0;
        final String query = "select count(id) from transcription where text!=''";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            final ResultSet rs = ps.executeQuery();
            rs.next();
            toret += rs.getInt(1);
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
        return toret;
    }

    /**
     * Delete both the Line position data and the transcribed text/comment.
     * Scary thing to do.
     *
     * @throws SQLException
     */
    public void remove() throws SQLException {
        final String query = "delete from transcription where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, this.lineID);
            ps.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**
     * Encode the Transcription text for use in html
     *
     * @return encoded text or "" if none available
     */
    public String getText() {
        if (text != null) //return ESAPI.encoder().encodeForHTML(text);
        {
            return text; // DEBUG all trouble. Where's ESAPI?
//         return ESAPI.encoder().encodeForHTML(ESAPI.encoder().decodeForHTML(text));
        } else {
            return "";
        }
    }

    /**
     * Only use this if you are very sure you want the unescaped version. This
     * could have unsafe html in it!
     *
     * @return text without special encoding
     */
    public String getTextUnencoded() {
        return ESAPI.encoder().decodeForHTML(text);
    }

    /**
     * Retrieve a random user's transcription of the requested page/Line
     * combination.Used only in a demo.
     *
     * @param uniqueID
     * @throws SQLException
     */
    public Transcription(final String uniqueID) throws SQLException {
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = DatabaseWrapper.getConnection();

            stmt = j.prepareStatement("Select * from transcription where id=?");
            stmt.setString(1, uniqueID);
            ResultSet rs;
            rs = stmt.executeQuery();
            if (rs.next()) {
                text = rs.getString("text");
                comment = rs.getString("comment");
                UID = rs.getInt("creator");
                lineID = rs.getInt("id");
                x = rs.getInt("x");
                y = rs.getInt("y");
                width = rs.getInt("width");
                height = rs.getInt("height");
                this.projectID = rs.getInt("projectID");
                this.folio = rs.getInt("folio");
                date = rs.getTimestamp("date");
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);
        }
    }

    /**
     * Build an ordered array of transcriptions for the specified Folio in the
     * specified Project
     *
     * @param projectID
     * @param folioNumber
     * @return
     * @throws SQLException
     * @throws java.io.IOException
     */
    public static Transcription[] getProjectTranscriptions(final int projectID, final int folioNumber) throws SQLException, IOException {
        final String query = "select id from transcription where projectID=? and folio=? order by x, y";
        /* 
         For different interfaces with a mixture of left to right, right to left, top to bottom, bottom to top, we cannot really rely on the order returned here.
         When IIIF, the annotationList holds the older we can rely on.  For LTR, this query returns the lines in the order I want, but makes it a strange order for RTL.
         Therefore, the front end will be assuming some responsibility for ordering these lines for their line# and column letter designation, until such time
         as the column is a real object that we can control and assign these types of properties to.  
         */
        Connection j = null;
        PreparedStatement ps = null;
        final Stack<Transcription> orderedTranscriptions = new Stack();
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ps.setInt(2, folioNumber);
            //System.out.println("Get transcription line ids");
            final ResultSet transcriptionIDs = ps.executeQuery();
            while (transcriptionIDs.next()) {
                //add a Transcription object built using the unique id
                orderedTranscriptions.add(new Transcription(transcriptionIDs.getString(1)));
            }
            final Transcription[] toret = new Transcription[orderedTranscriptions.size()]; //orderedTranscriptions can be empty;
            for (int i = 0; i < orderedTranscriptions.size(); i++) {
                toret[i] = orderedTranscriptions.get(i);
                LOG.log(Level.FINE, "Transcription {0} {1}->{2}", new Object[]{i, toret[i].getY(), toret[i].getY() + toret[i].getHeight()});
            }
            return toret; //This can be an empty array
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }

    }

    /**
     * was used to convert some old records. No longer used
     *
     * @param args
     * @throws SQLException
     */
    private static void main(final String[] args) throws SQLException {
        final String transcriptionSelect = "select * from transcription";
        final String projectImageQuery = "select * from projectimagepositions where folio=? and project=? and line=?";
        final String imageQuery = "select * from imagepositions where folio=? and line=?";
        final String updateQuery = "update transcription set x=?, y=?, height=?, width=? where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(transcriptionSelect);
            ps2 = j.prepareStatement(imageQuery);
            ps3 = j.prepareStatement(projectImageQuery);
            ps4 = j.prepareStatement(updateQuery);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("width") >= 0) {
                    final int id = rs.getInt("id");
                    final int folio = rs.getInt("folio");
                    final int projectID = rs.getInt("projectID");
                    final int line = rs.getInt("line");
                    if (projectID > 0) {
                        ps3.setInt(1, folio);
                        ps3.setInt(2, projectID);
                        ps3.setInt(3, line);
                        final ResultSet rs2 = ps3.executeQuery();
                        if (rs2.next()) {
                            //Folio 	Line 	bottom 	top 	id 	colstart 	width 	dummy
                            ps4.setInt(2, rs2.getInt("top"));
                            ps4.setInt(3, (rs2.getInt("bottom") - rs2.getInt("top")));
                            ps4.setInt(1, rs2.getInt("colstart"));
                            ps4.setInt(4, (rs2.getInt("colstart") + rs2.getInt("width")));
                            ps4.setInt(5, id);
                            ps4.execute();
                        }
                    } else {
                        ps2.setInt(1, folio);

                        ps2.setInt(2, line);
                        final ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) {
                            //Folio 	Line 	bottom 	top 	id 	colstart 	width 	dummy
                            ps4.setInt(2, rs2.getInt("top"));
                            ps4.setInt(3, (rs2.getInt("bottom") - rs2.getInt("top")));
                            ps4.setInt(1, rs2.getInt("colstart"));
                            ps4.setInt(4, (rs2.getInt("colstart") + rs2.getInt("width")));
                            ps4.setInt(5, id);
                            ps4.execute();
                        }
                    }
                }
            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
            DatabaseWrapper.closePreparedStatement(ps2);
            DatabaseWrapper.closePreparedStatement(ps3);
            DatabaseWrapper.closePreparedStatement(ps4);
        }
    }

    /**
     * Build an OAC message for this Transcription, currently generating a
     * plaintext message rather than serialized rdf for demo purposes
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getOAC() throws SQLException {
        String toret = "";
        final Folio f = new Folio(this.getFolio());
        toret += "        ex:Anno   a oac:Annotation ,<br>";
        toret += "                  oac:hasTarget ex:" + f.getImageName() + " ,<br>";
        toret += "                  oac:hasBody ex:uuid .<br>";
        toret += "        ex:uuid   a oac:Body ,<br>";
        toret += "                  a cnt:ContentAsText ,<br>";
        toret += "                  cnt:chars \"" + this.text + "\" ,<br>";
        toret += "                  cnt:characterEncoding \"utf-8\" .<br>";

        return toret;
    }

    public static boolean projectHasTranscriptions(final int projectID, final int folioNumber) throws SQLException {
        final String query = "select id from transcription where projectID=? and folio=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ps.setInt(2, folioNumber);
            final ResultSet transcriptionIDs = ps.executeQuery();
            return transcriptionIDs.next();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**
     * Archive the current Transcription.This is done before saving changes.
     *
     * @throws java.sql.SQLException
     */
    public void archive() throws SQLException {
        final String query = "insert into archivedtranscription (folio,line,comment,text,date,creator,projectID,id,x,y,width,height)  (SELECT * FROM `transcription` WHERE id=?)";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, this.lineID);
            ps.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**
     * Save the current data. Dont actually save it unless changes have been
     * made.
     *
     * @return true if an update occured. useful for future versioning
     * @throws SQLException
     */
    public Boolean commit() throws SQLException {
        //this.archive();//archive the old version before saving the changes.
        Connection j = null;
        PreparedStatement stmt = null;
        if (this.text == null) {
            final StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            String stackLog = "";
            for (StackTraceElement stacktrace1 : stacktrace) {
                stackLog += stacktrace1.toString();
            }
            LOG.log(Level.WARNING, "Transcription text was null {0}", stackLog);
            text = "";
        }
        try {
            j = DatabaseWrapper.getConnection();
            //if this is a group Transcription, update the current one if it exists and set the uid to this user to indicate who modified it most recently
            if (projectID > 0) {
                final Project p = new Project(projectID);
                final Manuscript m = new Manuscript(folio);
                //modified to now be a link
                p.addLogEntry(j, "<span class='log_transcription'></span>Saved <a href=\"transcription.jsp?projectID=" + projectID + "&folio=" + folio + "\">" + m.getShelfMark() + "</a> " + new Folio(folio).getPageName(), this.UID);// ,"Transcription"
                stmt = j.prepareStatement("Select text,comment from transcription where id=?");
                stmt.setInt(1, lineID);
                ResultSet rs;
                rs = stmt.executeQuery();
                if (rs.next()) {
                    stmt = j.prepareStatement("Update transcription set  text=?, comment=?, creator=?, x=?, y=?, height=?, width=? where id=?");

                    try {
                        /**
                         * @TODO is this reencoding crap needed, or was this a
                         * desperate attempt to solve a server encoding issue
                         */
                        text = new String(text.getBytes("UTF8"), "UTF8");
                    } catch (final UnsupportedEncodingException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                    stmt.setString(1, text);
                    stmt.setString(2, comment);
                    stmt.setInt(3, UID);
                    stmt.setInt(4, x);
                    stmt.setInt(5, y);
                    stmt.setInt(6, height);
                    stmt.setInt(7, width);
                    stmt.setInt(8, lineID);

                    stmt.execute();
//               try {
//       //           TranscriptionIndexer.update(this);
//               } catch (IOException ex) {
//                  LOG.log(Level.SEVERE, null, ex);
//               }
                    return true;
                }

                stmt = j.prepareStatement("Insert into transcription (text,comment,folio,line,creator,projectID,x,y,height,width) values(?,?,?,?,?,?,?,?,?,?)");
                stmt.setInt(3, folio);
                stmt.setInt(4, line);
                stmt.setString(1, text);
                stmt.setString(2, comment);
                stmt.setInt(5, UID);
                stmt.setInt(6, projectID);
                stmt.setInt(7, x);
                stmt.setInt(8, y);
                stmt.setInt(9, height);
                stmt.setInt(10, width);

                stmt.execute();
//            try {
// //              TranscriptionIndexer.update(this);
//            } catch (IOException ex) {
//               LOG.log(Level.SEVERE, null, ex);
//            }
                return true;

            } else {
                stmt = j.prepareStatement("Select text,comment from transcription where id=?");
                stmt.setInt(1, lineID);

                stmt = j.prepareStatement("Update transcription set  text=?, comment=?, creator=?, x=?, y=?, height=?, width=? where id=?");

                try {
                    text = new String(text.getBytes("UTF8"), "UTF8");
                } catch (final UnsupportedEncodingException ex) {
                    Logger.getLogger(Transcription.class.getName()).log(Level.SEVERE, null, ex);
                }
                stmt.setString(1, text);
                stmt.setString(2, comment);
                stmt.setInt(3, UID);
                stmt.setInt(4, x);
                stmt.setInt(5, y);
                stmt.setInt(6, height);
                stmt.setInt(7, width);
                stmt.setInt(8, lineID);
                stmt.execute();
//            try {
//  //             TranscriptionIndexer.update(this);
//            } catch (IOException ex) {
//               LOG.log(Level.SEVERE, null, ex);
//            }
                return true;

            }
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(stmt);

        }
    }

    /**
     * Make a copy of a Line of non Project Transcription and add it to a
     * Project. Used for converting old data.
     *
     * @param uid transcriber uid
     * @param project Project id
     * @throws SQLException
     */
    public void makeCopy(final int uid, final int project) throws SQLException {
        final String query = "insert into transcription (text,comment,folio,line,creator,projectID) values(?,?,?,?,?,?)";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = DatabaseWrapper.getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, text);
            ps.setString(2, comment);
            ps.setInt(3, this.folio);
            ps.setInt(4, line);
            ps.setInt(5, uid);
            ps.setInt(6, project);
            ps.execute();
        } finally {
            DatabaseWrapper.closeDBConnection(j);
            DatabaseWrapper.closePreparedStatement(ps);
        }
    }

    /**
     * Update all lines of Transcription with the same left parameter and update
     * them to the new width
     *
     * @param newWidth the new width for all lines in the column
     * @return
     * @throws SQLException
     * @throws java.io.IOException
     */
    public boolean updateColumnWidth(final int newWidth) throws SQLException, IOException {
        if (projectID > 0) {
            final Transcription[] theseTranscriptions = getProjectTranscriptions(projectID, folio);
            for (Transcription theseTranscription : theseTranscriptions) {
                if (theseTranscription.x == this.x) {
                    theseTranscription.setWidth(newWidth);
                }
            }
            return true;
        } else {
//         final Transcription[] theseTranscriptions = Transcription.getPersonalTranscriptions(UID, folio);
//         for (int i = 0; i < theseTranscriptions.length; i++) {
//            if (theseTranscriptions[i].x == this.x) {
//               theseTranscriptions[i].setWidth(newWidth);
//            }
//         }
            return true;
        }

    }

    /**
     * Update all lines of Transcription with the same left parameter and update
     * them to the new value
     *
     * @param newLeft
     * @return
     * @throws SQLException
     * @throws java.io.IOException
     */
    public boolean updateColumnLeft(final int newLeft) throws SQLException, IOException {
        if (projectID > 0) {
            final Transcription[] theseTranscriptions = getProjectTranscriptions(projectID, folio);
            for (Transcription theseTranscription : theseTranscriptions) {
                if (theseTranscription.x == this.x) {
                    theseTranscription.setX(newLeft);
                }
            }
            return true;
        } else {
//         final Transcription[] theseTranscriptions = Transcription.getPersonalTranscriptions(UID, folio);
//         for (int i = 0; i < theseTranscriptions.length; i++) {
//            if (theseTranscriptions[i].x == this.x) {
//               theseTranscriptions[i].setX(newLeft);
//            }
//         }
            return true;
        }
    }

    /**
     * Build this line of transcription as an OAC annotation and return the N3
     * serialization
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getAsOAC() throws SQLException {
        final Model model = ModelFactory.createDefaultModel();
        //model.setNsPrefix("dms", "http://dms.stanford.edu/ns/");
        model.setNsPrefix("oac", "http://www.openannotation.org/ns/");
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
        model.setNsPrefix("cnt", "http://www.w3.org/2008/content#");

        model.setNsPrefix("sc", "http://www.shared-canvas.org/ns/");
        //model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");

        final Property oacTarget = model.createProperty("http://www.openannotation.org/ns/", "hasTarget");
        final Property oacBody = model.createProperty("http://www.openannotation.org/ns/", "hasBody");
        // Property scContentAnnotation=model.createProperty("http://www.openannotation.org/ns/","Annotation");
        final Property scContentAnnotation = model.createProperty("http://www.shared-canvas.org/ns/", "ContentAnnotation");
        final Property contentChars = model.createProperty("http://www.w3.org/2008/content#", "rest");
        final Property encoding = model.createProperty("http://www.w3.org/2008/content#", "characterEncoding");
        Resource item;
        item = model.createResource("http://t-pen.org/transcriptions/" + this.lineID);
        final Property rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
        final Resource thisLine = model.createResource("urn:uuid:" + java.util.UUID.randomUUID().toString());
        final Property stringContent = model.createProperty("http://www.w3.org/2008/content#ContentAsText");
//        final Property parseType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
        final int folioNumber = this.folio;
        final Folio f = new Folio(folioNumber);
        final String xyhw = "#xywh=" + this.getX() + "," + this.getY() + "," + this.getWidth() + "," + this.getHeight();
        String image_canvas = f.getCanvas();
        if (image_canvas == null || image_canvas.length() < 10) {
            image_canvas = f.getImageURL();
        }
        final Resource target = model.createResource(image_canvas + xyhw);
        final Literal textLiteral = model.createLiteral(this.getText());
        /*      final Literal literal = model.createLiteral("Literal");*/
        final Literal encodingType = model.createLiteral("utf-8");

        item.addProperty(oacBody, thisLine);
        item.addProperty(oacTarget, target);

        item.addProperty(rdfType, scContentAnnotation);
        //contentChars.addProperty(parseType, literal);
        thisLine.addProperty(contentChars, textLiteral);
        //thisLine.addProperty(parseType, literal);
        thisLine.addProperty(encoding, encodingType);
        thisLine.addProperty(rdfType, stringContent);
        final StringWriter tmp = new StringWriter();
        model.write(tmp, "");
        return tmp.toString();
    }

    /**
     * Get the url of the image on which this Transcription is based. This is
     * the scaled version of the image! This abomination is used by search.jsp.
     *
     * @return the url, or "" on error
     */
    public String getImageURL() {
        try {
            Folio f = new Folio(folio);
            Manuscript m = new Manuscript(folio);
            if (m.isRestricted()) {
                return "restricted";
            } else {
                return f.getImageURLResize();
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static final Logger LOG = Logger.getLogger(Transcription.class.getName());
}
