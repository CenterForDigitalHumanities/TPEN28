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

import com.hp.hpl.jena.rdf.model.Model;
import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import static edu.slu.util.ServletUtils.getDBConnection;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import static java.lang.Boolean.FALSE;
import static java.lang.Integer.parseInt;
import static java.lang.System.err;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static org.owasp.esapi.ESAPI.encoder;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;
import static textdisplay.Project.imageBounding.columns;
import static textdisplay.Project.imageBounding.fullimage;
import static textdisplay.Project.imageBounding.lines;
import static textdisplay.Project.imageBounding.none;
import static textdisplay.TagButton.getTagsFromSchema;
import static textdisplay.Transcription.getProjectTranscriptions;
import user.Group;
import user.User;
import utils.UserTool;

/**
 * @author Jon Deering
 */
public class Project {

    /**
     * Defines the level of parsing TPEN will attempt to provide for a project.
     */
    public enum imageBounding {
        lines, columns, fullimage, none
    };

    int groupID;
    int projectID;
    String projectName;
    String linebreakSymbol = "-";
    private imageBounding projectImageBounding;

    /**
     * What is the preferred level of parsing for this project.
     *
     * @return
     */
    public imageBounding getProjectImageBounding() {
        return this.projectImageBounding;
    }

    /**
     * include one of the built in tools in TPEN in the display for this project
     * (ex.preview, correct parsing)
     *
     * @param name String label for the tool
     * @param url location for iFrame
     * @throws java.sql.SQLException
     */
    public void addTool(String name, String url) throws SQLException {
        Connection j = null;
        PreparedStatement ps = null;
        String query = "insert into tools(name,url) values(?,?)";
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, url);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * exclude one of the built in tools in TPEN in the display for this project
     * (ex.preview, correct parsing)
     *
     * @param name String label for the tool
     * @param url location for iFrame
     * @throws java.sql.SQLException
     */
    public void removeTool(String name, String url) throws SQLException {
        Connection j = null;
        PreparedStatement ps = null;
        String query = "delete from tools where name=? and url=?";
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, url);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Not Implemented
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String[] getTools() throws SQLException {
        String[] toret = null;
        Connection j = null;
        PreparedStatement ps = null;
        String query = "select * from tools";
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
        return toret;
    }

    /**
     * Add a tool by url which can be displayed in an iframe
     */
    private void addUserTool(Connection conn, String name, String url) throws SQLException {
        UserTool userTool = new UserTool(conn, name, url, projectID);
    }

    /**
     * Add the standard Latin tools to a project This is called from project
     * instantiation in project.java
     */
    private void initializeTools(Connection conn) throws SQLException {
        addUserTool(conn, "Latin Vulgate", "http://vulsearch.sourceforge.net/cgi-bin/vulsearch");
        addUserTool(conn, "Latin Dictionary", "http://www.perseus.tufts.edu/hopper/resolveform?lang=latin");
        addUserTool(conn, "Enigma", "http://ciham-digital.huma-num.fr/enigma/");
        addUserTool(conn, "Cappelli's Abbreviation", "https://centerfordigitalhumanities.github.io/cappelli/");

    }

    /**
     * Sets the preferred level of parsing TPEN should attempt.Default is line.
     *
     * @param projectImageBounding String "line" or "column"
     * @throws java.sql.SQLException
     */
    public void setProjectImageBounding(imageBounding projectImageBounding) throws SQLException {
        this.projectImageBounding = projectImageBounding;
        String updateQuery = "update project set imageBounding=? where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(updateQuery);
            ps.setString(1, projectImageBounding.name());
            ps.setInt(2, projectID);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    public String getLinebreakSymbol() {
        return linebreakSymbol;
    }

    public void setLinebreakSymbol(String linebreakSymbol) {
        this.linebreakSymbol = linebreakSymbol;
    }
    int linebreakCharacterLimit = 5000;

    public int getLinebreakCharacterLimit() {
        return linebreakCharacterLimit;
    }

    /**
     * Update the number of characters from the uploaded text the linebreak
     * feature will give per page
     *
     * @param newLimit int new upper limit
     * @throws java.sql.SQLException
     */
    public void setLinebreakCharacterLimit(int newLimit) throws SQLException {
        String query = "update project set linebreakCharacterLimit=? where projectID=?";
        Connection j = null;
        PreparedStatement updateQuery = null;
        try {
            j = getConnection();
            updateQuery = j.prepareStatement(query);
            updateQuery.setInt(1, newLimit);
            updateQuery.setInt(1, projectID);
            updateQuery.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(updateQuery);
        }
    }

    /**
     * Set the priority number for this project, which is used to sort the
     * project listing. Higher is displayed first
     *
     * @param priority
     * @throws java.sql.SQLException
     */
    public void setProjectPriorty(int priority) throws SQLException {
        String query = "update project set priority=? where id=?";
        Connection j = null;
        PreparedStatement updateQuery = null;
        try {
            j = getConnection();
            updateQuery = j.prepareStatement(query);
            updateQuery.setInt(1, priority);
            updateQuery.setInt(2, projectID);
            updateQuery.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(updateQuery);
        }
    }

    public Project() {
    }

    /**
     * Create a new Project
     *
     * @return
     */
    public int getProjectID() {
        return projectID;
    }

    /**
     * Update the Project image sequence
     *
     * @param orderedFolios
     * @throws java.sql.SQLException
     */
    public void buildSequence(Folio[] orderedFolios) throws SQLException {
        Connection j = null;
        PreparedStatement qry = null;
        try {
            String query = "update projectfolios set position=? where project=? and folio=?";
            j = getConnection();
            qry = j.prepareStatement(query);
            //qry.execute("delete from projectsequence where Project="+this.projectID);
            for (int i = 0; i < orderedFolios.length; i++) {
                qry.setInt(1, i + 1);
                qry.setInt(2, this.projectID);
                qry.setInt(3, orderedFolios[i].folioNumber);
                qry.execute();

            }
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(qry);
            }
        }

    }

    /**
     * Get the full Transcription as a single string
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getFullDocument() throws SQLException {

        String toret = "";
        String query = "select transcription.text from transcription join folios on folios.pageNumber=transcription.folio where projectID=? order by folio,line";
        Connection j = null;
        PreparedStatement ps = null;

        try {
            j = getConnection();
            ps = j.prepareStatement(query);

            ps.setInt(1, this.projectID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                toret += rs.getString(1);
            }
            return toret;
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(ps);
            }
        }
    }

    /**
     * Copy the buttons from Project p to this Project, destroying an existing
     * buttons in this Project
     *
     * @param conn Connection
     * @param p Project
     * @throws java.sql.SQLException
     */
    public synchronized void copyButtonsFromProject(Connection conn, Project p) throws SQLException {

        try (PreparedStatement table = conn.prepareStatement("CREATE TEMPORARY TABLE tmpbuttons LIKE projectbuttons")) {
            table.executeUpdate();

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM projectbuttons WHERE project=?")) {
                del.setInt(1, projectID);
                del.executeUpdate();

                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO tmpbuttons (SELECT * FROM projectbuttons where project=?)")) {
                    ins.setInt(1, p.projectID);
                    ins.executeUpdate();

                    // Now update the Project ids.
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE tmpbuttons SET project=?")) {
                        upd.setInt(1, projectID);
                        upd.executeUpdate();

                        try (PreparedStatement reins = conn.prepareStatement("INSERT INTO projectbuttons (SELECT * FROM tmpbuttons)")) {
                            reins.executeUpdate();
                        }
                    }
                }
            }
        } finally {
            try (PreparedStatement drop = conn.prepareStatement("DROP TABLE tmpbuttons")) {
                drop.executeUpdate();
            }
        }
    }

    /**
     * Copy the character hotkeys from another project.Destroys anything in this
     * project.
     *
     * @param conn Connection
     * @param p Project
     * @throws java.sql.SQLException
     */
    public synchronized void copyHotkeysFromProject(Connection conn, Project p) throws SQLException {
        try (PreparedStatement table = conn.prepareStatement("CREATE TEMPORARY TABLE tmphotkeys LIKE hotkeys")) {
            table.executeUpdate();

            try (PreparedStatement del = conn.prepareStatement("DELETE FROM hotkeys WHERE projectID=?")) {
                del.setInt(1, projectID);
                del.executeUpdate();

                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO tmphotkeys (SELECT * FROM hotkeys WHERE projectID=?)")) {
                    ins.setInt(1, p.projectID);
                    ins.executeUpdate();

                    // Now update the Project ids.
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE tmphotkeys SET projectID=?")) {
                        upd.setInt(1, projectID);
                        upd.executeUpdate();

                        try (PreparedStatement reins = conn.prepareStatement("INSERT INTO hotkeys (SELECT * FROM tmphotkeys)")) {
                            reins.executeUpdate();
                        }
                    }
                }
            }
        } finally {
            try (PreparedStatement drop = conn.prepareStatement("DROP TABLE tmphotkeys")) {
                drop.executeUpdate();
            }
        }
    }

    /**
     * Modify existing transcriptions belonging to the specified user to be part
     * of this Project
     *
     * @param uid
     * @throws java.sql.SQLException
     */
    public void importData(int uid) throws SQLException {
        Connection j = null;
        PreparedStatement ps = null;
        String query = "update transcription set projectID=? where creator=? and folio=? and projectID=0";
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            Folio[] theseFolios = this.getFolios();
            for (Folio theseFolio : theseFolios) {
                ps.setInt(1, projectID);
                ps.setInt(2, uid);
                ps.setInt(3, theseFolio.getFolioNumber());
                ps.execute();
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }

    }

    /**
     * Create a new Project, with the specified name and group.
     *
     * @param conn Connection
     * @param name String project name
     * @param group Int group id
     * @throws java.sql.SQLException
     */
    public Project(Connection conn, String name, int group) throws SQLException {
        if (name == null || name.length() == 0) {
            name = "new project";
        }
        try (PreparedStatement stmt = conn.prepareStatement("insert into project (name, grp, schemaURL,linebreakCharacterLimit ) values(?,?,'',5000)", RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, group);
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                projectID = rs.getInt(1);
                Metadata metadata = new Metadata(conn, projectID, name);
                initializeTools(conn);
                groupID = group;
            }
        }
    }

    /**
     * Get the existing Project based on the Project ID
     *
     * @param projectID int Project id
     * @throws java.sql.SQLException
     */
    public Project(int projectID) throws SQLException {
        Connection j = null;
        PreparedStatement qry = null;
        try {
            j = getConnection();
            qry = j.prepareStatement("select * from project where id=?");
            qry.setInt(1, projectID);
            ResultSet rs = qry.executeQuery();
            if (rs.next()) {
                projectName = rs.getString("name");
                groupID = rs.getInt("grp");
                this.projectID = projectID;
                this.linebreakCharacterLimit = rs.getInt("linebreakCharacterLimit");
                String imageBoundingStr = rs.getString("imageBounding");
                if (imageBoundingStr.compareTo("columns") == 0) {
                    this.projectImageBounding = columns;
                }
                if (imageBoundingStr.compareTo("fullimage") == 0) {
                    this.projectImageBounding = fullimage;
                }
                if (imageBoundingStr.compareTo("none") == 0) {
                    this.projectImageBounding = none;
                }
                if (imageBoundingStr.compareTo("lines") == 0) {
                    this.projectImageBounding = lines;
                }

            }
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(qry);
            } else {
                err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
        }
    }

    /**
     * Return a listing of all tasks associated with this Project
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String listTasks() throws SQLException {
        String query = "select id from tasks where project=?";
        String toret = "";
        Connection j = null;
        PreparedStatement qry = null;
        try {
            j = getConnection();
            qry = j.prepareStatement(query);
            qry.setInt(1, projectID);
            ResultSet rs = qry.executeQuery();
            while (rs.next()) {
                toret += rs.getInt(1);
            }
            return toret;
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(qry);
            } else {
                err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
        }
    }

    /**
     * Retrieve the Project name from the Project Metadata
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getProjectName() throws SQLException {
        String toret = new Metadata(this.projectID).getTitle();
        if (toret.compareTo("") == 0) {
            toret = this.projectName;
            if (toret == null || toret.compareTo("") == 0) {
                toret = "unknown project";
            }
            new Metadata(this.projectID).setTitle(toret);
        }
        return toret;
    }

    /**
     * Return Metadata in TEI p5 format for CCL
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Metadata getMetadata() throws SQLException {
        return new Metadata(this.projectID);
    }

    /**
     * Use the log to figure out when this project was last modified.
     *
     * @return time of the last modification
     * @throws SQLException
     */
    public Date getModification() throws SQLException {
        Date result = null;
        try (Connection conn = getDBConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT MAX(date) FROM transcription JOIN projectfolios ON transcription.folio = projectfolios.folio WHERE project = ?")) {
                stmt.setInt(1, projectID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    result = rs.getTimestamp(1);
                }
            }
        }
        return result != null ? result : new Date(0);
    }

    /**
     * Get all projects with public read access ordered by project name
     *
     * @return
     * @throws java.sql.SQLException
     */
    public static Project[] getPublicProjects() throws SQLException {
        String query = "select distinct(project.id) from project join projectpermissions on project.id=projectpermissions.projectID where projectpermissions.allow_public_read_transcription=true order by project.name desc";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            Stack<Integer> projectIDs = new Stack();
            while (rs.next()) {
                projectIDs.push(rs.getInt("project.id"));
            }
            Project[] toret = new Project[projectIDs.size()];
            int ctr = 0;
            while (!projectIDs.empty()) {
                toret[ctr] = new Project(projectIDs.pop());
                ctr++;
            }
            return toret;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Get all projects ordered by project name
     *
     * @return
     * @throws java.sql.SQLException
     */
    public static Project[] getAllProjects() throws SQLException {
        String query = "select distinct(project.id) from project order by project.name desc";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            Stack<Integer> projectIDs = new Stack();
            while (rs.next()) {
                projectIDs.push(rs.getInt("project.id"));
            }
            Project[] toret = new Project[projectIDs.size()];
            int ctr = 0;
            while (!projectIDs.empty()) {
                toret[ctr] = new Project(projectIDs.pop());
                ctr++;
            }
            return toret;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Check to see if this project contains user uploaded manuscript images.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Boolean containsUserUploadedManuscript() throws SQLException {
        String query = "select * from projectfolios join folios on projectfolios.folio=folios.pageNumber join manuscript on folios.msID=manuscript.id where projectfolios.project=? and manuscript.restricted=-999";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Create a new Project with the specified leader and copes of the
     * transcriptions, annotations, buttons, and schema
     *
     * @param conn Connection
     * @param leaderUID Leader
     * @return
     * @throws java.sql.SQLException
     */
    public int copyProject(Connection conn, int leaderUID) throws SQLException, Exception {
        //if (containsUserUploadedManuscript()) {
        //   throw new Exception("Cannot copy a project with user uploaded images!");
        //}
        System.out.println("Hey, classic copy here.");
        Group g = new Group(conn, projectName, leaderUID);
        System.out.println("Got a group "+g.getGroupID());
        Project p = new Project(conn, projectName, g.getGroupID());
        System.out.println("Got a project "+p.getProjectID());
        p.setFolios(conn, getFolios());
        System.out.println("Set the folios");
        p.copyButtonsFromProject(conn, this);
        System.out.println("Copy buttons "+p.getProjectID());
        p.copyHotkeysFromProject(conn, this);
        System.out.println("opy hotkeys "+p.getProjectID());
        p.setSchemaURL(conn, getSchemaURL());
        System.out.println("Set the schema URL");
        try (PreparedStatement selectStmt = conn.prepareStatement("select * from transcription where projectID=?")) {
            selectStmt.setInt(1, projectID);
            ResultSet rs = selectStmt.executeQuery();
            System.out.println("Move over all transcription info.");
            try (PreparedStatement insertStmt = conn.prepareStatement("insert into transcription(folio, line, comment, text, creator, projectID,x,y,height,width) values(?,?,?,?,?,?,?,?,?,?)")) {
                while (rs.next()) {
                    System.out.println("...");
                    insertStmt.setInt(1, rs.getInt("folio"));
                    insertStmt.setInt(2, rs.getInt("line"));
                    insertStmt.setString(3, rs.getString("comment"));
                    insertStmt.setString(4, rs.getString("text"));
                    insertStmt.setInt(5, rs.getInt("creator"));
                    insertStmt.setInt(6, p.projectID);
                    insertStmt.setInt(7, rs.getInt("x"));
                    insertStmt.setInt(8, rs.getInt("y"));
                    insertStmt.setInt(9, rs.getInt("height"));
                    insertStmt.setInt(10, rs.getInt("width"));
                    insertStmt.execute();
                }
            }
        }
        System.out.println("Great we did it.  Return this projectID: "+p.projectID);
        return p.getProjectID();
    }

    /**
     * Create a new Project with the specified leader and copes of the
     * transcriptions, annotations, buttons, and schema
     *
     * @param conn Connection
     * @param leaderUID
     * @return
     * @throws java.sql.SQLException
     * @throws java.lang.Exception
     * @since 2014
     */
    public int copyProjectWithoutTranscription(Connection conn, int leaderUID) throws SQLException, Exception {
        //if (containsUserUploadedManuscript()) {
        //   throw new Exception("Cannot copy a project with user uploaded images!");
        //}
        //System.out.println("Creating a project template from "+projectName);
        Group g = new Group(conn, projectName, leaderUID);
        //find group leaders from template group, and if they are not in the new group, add them into new group. 
        Group templateGroup = new Group(this.getGroupID());
        User[] array_users = templateGroup.getLeader();
        for (User u : array_users) {
            if (!g.isMember(u.getUID())) {
                g.addMember(u.getUID());
            }
        }

        Project p = new Project(conn, projectName, g.getGroupID());
        p.setFolios(conn, getFolios());
        p.copyButtonsFromProject(conn, this);
        p.copyHotkeysFromProject(conn, this);
        p.setSchemaURL(conn, getSchemaURL());

        return p.projectID;
    }

    /**
     * Runs line detection on predefined columns.The columns are defined as
     * lines when this is called, so you might have 2 large lines, each being a
     * column.
     *
     * @param folioNumber
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public void detectInColumns(int folioNumber) throws SQLException, IOException {
        Folio f = new Folio(folioNumber);
        Line[] t = getLines(folioNumber);
        LOG.log(INFO, "User specified column count {0}", t.length);
        for (Line t1 : t) {
            LOG.log(INFO, "Column {0},{1},{2},{3}", new Object[]{t1.left, t1.right, t1.top, t1.bottom});
        }

        Line[] linePositions = f.detectInColumns(t);

        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement("Delete from projectimagepositions where folio=?");
            stmt.setInt(1, folioNumber);
            stmt.execute();
            Transcription[] oldTranscriptions = getProjectTranscriptions(projectID, folioNumber);
            for (Transcription oldTranscription : oldTranscriptions) {
                oldTranscription.remove();
            }
            stmt = j.prepareStatement("Insert into projectimagepositions (folio,line,top,bottom,colstart,width,project) values (?,?,?,?,?,?,?)");
            if (linePositions.length == 0) {
                linePositions = t;
            }
            for (int i = 0; i < linePositions.length; i++) {
                stmt.setInt(1, folioNumber);
                stmt.setInt(2, i + 1);
                stmt.setInt(3, linePositions[i].top);
                stmt.setInt(4, linePositions[i].bottom);
                stmt.setInt(5, linePositions[i].left);
                stmt.setInt(6, linePositions[i].right);
                stmt.setInt(7, projectID);
                stmt.execute();
                Rectangle r = new Rectangle(linePositions[i].left, linePositions[i].top, linePositions[i].getHeight(), linePositions[i].right);
                // TODO: create an agent for the application here, loaded from version.properties.
                Transcription tr = new Transcription(0, projectID, folioNumber, "", "", r);
            }
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(stmt);
            } else {
                err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
        }
    }

    /**
     * Set the linebreak text to this value, should only be used by the upload
     * process
     *
     * @param txt
     * @throws java.sql.SQLException
     */
    public void setLinebreakText(String txt) throws SQLException {
        String query = "insert into linebreakingtext (projectID,remainingText) values (?,?)";
        String deleteQuery = "delete from linebreakingtext where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        PreparedStatement del = null;
        try {
            j = getConnection();
            del = j.prepareStatement(deleteQuery);
            del.setInt(1, projectID);
            del.execute();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ps.setString(2, txt);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
            closePreparedStatement(del);

        }
    }

    /**
     * Set the linebreak text to this value, should only be used by the upload
     * process
     *
     * @param txt
     * @throws java.sql.SQLException
     */
    public void setHeaderText(String txt) throws SQLException {
        String query = "insert into projectheader (projectID,header) values (?,?)";
        String deleteQuery = "delete from projectheader where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        PreparedStatement del = null;
        try {
            j = getConnection();
            del = j.prepareStatement(deleteQuery);
            del.setInt(1, projectID);
            del.execute();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ps.setString(2, txt);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
            closePreparedStatement(del);

        }
    }

    /**
     * Update the linebreak text by removing the number of characters specified
     * by linebreakCharacterLimit and replacing those with returnedText
     *
     * @param returnedText
     * @throws java.sql.SQLException
     */
    public void setLinebreakTextWithReturnedText(String returnedText) throws SQLException {
        String query = "update linebreakingtext set remainingText=? where projectID=?";
        String selectQuery = "select remainingText  from linebreakingtext where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        PreparedStatement selectStatement = null;
        try {

            j = getConnection();
            ps = j.prepareStatement(query);
            selectStatement = j.prepareStatement(selectQuery);
            selectStatement.setInt(1, projectID);
            ResultSet rs = selectStatement.executeQuery();
            if (rs.next()) {

                String oldValue = "";
                if (rs.getString(1).length() >= linebreakCharacterLimit) {
                    oldValue = rs.getString(1).substring(this.linebreakCharacterLimit);
                }
                oldValue = returnedText + oldValue;
                ps.setInt(2, projectID);
                ps.setString(1, oldValue);
                ps.execute();
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
            closePreparedStatement(selectStatement);

        }
    }

    /**
     * Retrieve Project.linebreakCharacterLimit characters of from the uploaded
     * text file
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getLinebreakText() throws SQLException {
        String query = "select remainingText  from linebreakingtext where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String toret = rs.getString(1);
                if (toret.length() > this.linebreakCharacterLimit) {
                    return encoder().encodeForHTML(toret.substring(0, this.linebreakCharacterLimit));
                }
                return toret;
            } else {
                return "";
            }
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(ps);
            }
        }
    }

    /**
     * Retrieve a stored header that was uploaded by the user.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getHeader() throws SQLException {
        String query = "select header from projectheader where projectID=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String toret = rs.getString(1);
                return encoder().encodeForHTML(toret);
            } else {
                return "";
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Returns the Folio number of the most recently modified Folio, -1 if non
     * exists
     *
     * @return
     * @throws java.sql.SQLException
     */
    public int getLastModifiedFolio() throws SQLException {
        String query = "select folio from transcription where projectID=? order by date desc limit 1";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(ps);
            }
        }
    }

    /**
     * Get the parsed lines for this Folio that are specific to this Project
     *
     * @param folio
     * @return
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public Line[] getLines(int folio) throws SQLException, IOException {
        Connection j = null;
        PreparedStatement qry = null;
        try {
            String query = "Select * from projectimagepositions where folio=? and project=? and width>0 order by colstart,top ";
            j = getConnection();
            qry = j.prepareStatement(query);
            qry.setInt(1, folio);
            qry.setInt(2, this.projectID);
            ResultSet rs = qry.executeQuery();
            Stack<Line> lines = new Stack<>();
            while (rs.next()) {
                Line tmp = new Line(0, 0, 0, 0);
                tmp.bottom = rs.getInt("bottom");
                tmp.top = rs.getInt("top");
                tmp.left = rs.getInt("colstart");
                tmp.right = tmp.left + rs.getInt("width");
                lines.add(tmp);
            }
            Line[] toret = new Line[lines.size()];
            for (int i = 0; i < toret.length; i++) {
                toret[i] = lines.get(i);
            }
            //if there wasnt anything specific to this Project, get whatever is public, if that doesnt exist either, itll parse the image, store it public, and pass it here
            if (toret.length == 0) {
                return new Folio(folio, true).getlines();
            }
            return toret;
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(qry);
            } else {
                err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
        }
    }

    /**
     * Build a string of option elements, one for each Folio element in the
     * Project
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getFolioDropdown() throws SQLException {
        String toret = "";
        Folio[] allFolios = getFolios();
        for (Folio allFolio : allFolios) {
            toret += "<option value=\"" + allFolio.getFolioNumber() + "&projectID=" + this.projectID + "\">" + allFolio.getCollectionName() + " " + allFolio.getPageName() + "</option>";
        }
        return toret;
    }

    /**
     * Get the list of folios for this Transcription Proj
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Folio[] getFolios() throws SQLException {
        Connection j = null;
        PreparedStatement qry = null;
        try {
            Stack<Folio> t = new Stack();
            String query = "select * from projectfolios where project=? order by position";
            j = getConnection();
            qry = j.prepareStatement(query);
            qry.setInt(1, this.projectID);
            ResultSet rs = qry.executeQuery();
            while (rs.next()) {
                t.add(new Folio(false, rs.getInt("folio")));
            }
            Folio[] toret = new Folio[t.size()];
            for (int i = 0; i < t.size(); i++) {
                toret[i] = t.get(i);
            }
            return toret;
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(qry);
            } else {
                err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
        }
    }

    /**
     * Update the parsing of an image all at once.
     *
     * @param linePositions
     * @param linePositions2
     * @param linePositions3
     * @param pagenumber
     * @param linePositions4
     */
    public void update(int[] linePositions, int[] linePositions2, int[] linePositions3, int[] linePositions4, int pagenumber) {
        Line[] newLines = new Line[linePositions.length];
        for (int i = 0; i < linePositions.length; i++) {
            Line tmp = new Line(0, 0, 0, 0);
            tmp.top = linePositions[i];
            tmp.bottom = linePositions4[i];
            tmp.left = linePositions2[i];
            tmp.right = linePositions3[i];
            newLines[i] = tmp;
        }
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement("Delete from projectimagepositions where folio=? and project=?");
            stmt.setInt(1, pagenumber);
            stmt.setInt(2, projectID);
            stmt.execute();
            stmt = j.prepareStatement("Insert into projectimagepositions (folio,line,top,bottom,colstart,width,project, linebreakSymbol) values (?,?,?,?,?,?,?,?)");
            for (int i = 0; i < linePositions.length; i++) {
                stmt.setInt(1, pagenumber);
                stmt.setInt(2, i + 1);
                stmt.setInt(3, newLines[i].top);
                stmt.setInt(4, newLines[i].bottom);
                stmt.setInt(5, newLines[i].left);
                stmt.setInt(6, newLines[i].right);
                stmt.setInt(7, this.projectID);
                stmt.setString(8, this.linebreakSymbol);
                stmt.execute();
            }
        } catch (SQLException ex) {
            getLogger(Project.class.getName()).log(SEVERE, null, ex);
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }

    /**
     * Create an array of folios for the Folio numbers in the map
     *
     * @param m
     * @return 
     * @throws java.sql.SQLException
     */
    public Folio[] makeFolioArray(Map<String, String[]> m) throws SQLException {
        Set<String> e = m.keySet();
        Iterator<String[]> items = m.values().iterator();
        String[] names = new String[e.size()];
        int ctr = 0;
        while (items.hasNext()) {
            names[ctr] = items.next()[0];
            ctr++;
        }
        Stack<Folio> allFolios = new Stack();
        for (int i = 0; i < m.size(); i++) {
            try {
                Folio f = new Folio(parseInt(names[i]));
                allFolios.add(f);
            } catch (NumberFormatException er) {
            }
        }
        Folio[] toret = new Folio[allFolios.size()];
        for (int i = 0; i < toret.length; i++) {
            toret[i] = allFolios.get(i);
        }
        return toret;
    }

    /**
     * Get a list of images as html checkboxes.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String checkBoxes() throws SQLException {
        String toret = "";
        Folio[] folios = this.getFolios();
        for (Folio folio : folios) {
            toret += "<input type=\"checkbox\" name=\"" + folio.getFolioNumber() + "\" id=\"" + folio.getFolioNumber() + "\" checked value=\"" + folio.getFolioNumber() + "\"/>" + folio.getImageName() + "<br>\n";
        }
        return toret;
    }

    /**
     * Store the list of folios for this Project
     *
     * @param conn
     * @param f
     * @throws java.sql.SQLException
     */
    public void setFolios(Connection conn, Folio[] f) throws SQLException {

        try (PreparedStatement deletionStmt = conn.prepareStatement("Delete from projectfolios where project=?")) {
            deletionStmt.setInt(1, projectID);
            deletionStmt.executeUpdate();

            try (PreparedStatement insertionStmt = conn.prepareStatement("insert into projectfolios (project, folio, position) values(?,?,?)")) {
                insertionStmt.setInt(1, projectID);
                for (int i = 0; i < f.length; i++) {
                    if (f[i] != null) {
                        insertionStmt.setInt(2, f[i].getFolioNumber());
                        insertionStmt.setInt(3, i + 1);
                        insertionStmt.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Number of images in this project
     */
    int folioCount() throws SQLException {
        Connection j = null;
        PreparedStatement qry = null;
        try {
            String query = "select count(folio) from projectfolios where project=?";
            j = getConnection();
            qry = j.prepareStatement(query);

            return 0;
        } finally {
            if (j != null) {
                closeDBConnection(j);
                closePreparedStatement(qry);
            } else {
                err.print("Attempt to close DB connection failed, connection was null" + this.getClass().getName() + "\n");
            }
        }
    }

    /**
     * Return a list of Project members with the number of lines transcribed by
     * each
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getProgress() throws SQLException {
        String toret = "";
        User[] users = new Group(this.groupID).getMembers();
        String query = "select count(id) from transcription where projectID=? and creator=? and text!=''";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            for (User user : users) {
                ps.setInt(1, projectID);
                ps.setInt(2, user.getUID());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    toret += "<span class=\"progress\">" + user.getFname() + " " + user.getLname() + " " + rs.getInt(1) + "<br></span>";
                }
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }

        return toret;
    }

    /**
     * Return a count of lines transcribed
     *
     * @return
     * @throws java.sql.SQLException
     */
    public int getNumberOfTranscribedLines() throws SQLException {
        int toret = 0;
        String query = "select count(id) from transcription where projectID=? and text!=''";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                toret++;
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
        return toret;
    }

    public void setGroupID(int id) {
        groupID = id;
    }

    public int getGroupID() {
        return groupID;
    }

    /**
     * Check for tags used in the transcription that arent part of the
     * schema.This is can be used to warn users during pipeline export that they
     * are including unsupported tags without expecting the document to be fully
     * schema compliant.
     *
     * @return
     * @throws java.sql.SQLException
     * @throws java.net.MalformedURLException
     */
    public String[] hasTagsOutsideSchema() throws SQLException, MalformedURLException, IOException {
        Stack<String> badTags = new Stack();
        String[][] res = getTagsFromSchema(this, new ArrayList());
        String[] usedTags = new TagFilter(Manuscript.getFullDocument(this, FALSE)).getTags();
        for (String usedTag : usedTags) {
            Boolean found = false;
            for (String[] re : res) {
                if (usedTag.compareTo(re[0]) == 0) {
                    found = true;
                }
            }
            if (!found) {
                badTags.push(usedTag);
            }
        }
        String[] toret = new String[badTags.size()];
        for (int i = 0; i < toret.length; i++) {
            toret[i] = badTags.pop();
        }
        return toret;
    }

    /**
     * Return the partner Project, or null if the record couldn't be located
     *
     * @return
     * @throws java.sql.SQLException
     */
    public PartnerProject getAssociatedPartnerProject() throws SQLException {
        String query = "select partner from project where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, projectID);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return new PartnerProject(rs.getInt(1));
            }
            return null;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Associate this project with a project pipeline.This changes the character
     * hotkeys, buttons, and schema and adds a user to this project.
     *
     * @param id
     * @throws java.sql.SQLException
     */
    public void setAssociatedPartnerProject(int id) throws SQLException {
        String query = "update project set partner=? where id=?";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ps.setInt(2, projectID);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    /**
     * Returns all projects connected to a partner Project, or null if none
     *
     * @param id
     * @return
     * @throws java.sql.SQLException
     */
    public static Project[] getAllAssociatedProjects(int id) throws SQLException {
        Project[] all;
        String query = "select id from project where partner=? ";
        Connection j = null;
        PreparedStatement ps = null;
        Stack<Project> tmp = new Stack();
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmp.push(new Project(rs.getInt(1)));
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
        all = new Project[tmp.size()];
        //odd looking way of doing this copy, I know, but it was convenient
        while (!tmp.empty()) {
            all[tmp.size() - 1] = tmp.pop();
        }
        return all;
    }

    /**
     * Returns all projects updated in the last 2 months, or null if none
     *
     * @return
     * @throws java.sql.SQLException
     */
    public static Project[] getAllActiveProjects() throws SQLException {
        Project[] active;
        String query = "SELECT DISTINCT projectid FROM transcription WHERE DATE > ( NOW( ) + INTERVAL -2 MONTH )";
        Connection j = null;
        PreparedStatement ps = null;
        Stack<Project> tmp = new Stack();
        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tmp.push(new Project(rs.getInt(1)));
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
        active = new Project[tmp.size()];
        //odd looking way of doing this copy, I know, but it was convenient
        while (!tmp.empty()) {
            active[tmp.size() - 1] = tmp.pop();
        }
        return active;
    }

    /**
     * Build a new Project with the values stored herein, useful when using this
     * class as a bean
     *
     * @return
     */
    public int build() {
        try (Connection conn = getDBConnection()) {
            int toret = new Project(conn, projectName, groupID).projectID;
            return toret;
        } catch (SQLException ex) {
            return 0;
        }
    }

    /**
     * Delete the Project information for this Project.Does not delete the
     * underlying data, as Im sure the next request Ill get is to undelete
     * something...returns false if it failed to delete for some reason.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Boolean delete() throws SQLException {
        if (this.projectID > 0) {
            String query = "delete from project where id=?";
            Connection j = null;
            PreparedStatement qry = null;
            try {
                j = getConnection();
                qry = j.prepareStatement(query);
                qry.setInt(1, projectID);

                qry.execute();
                return true;
            } finally {
                if (j != null) {
                    closeDBConnection(j);
                }
                closePreparedStatement(qry);
            }
        }
        return false;
    }

    /**
     * returns -1 if an error occurs, probably due to deletion or the lack of
     * folios.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public int firstPage() throws SQLException {
        // @TODO:  If there're no folios, this throws an ArrayIndexOutOfBoundsException.
        try {
            return getFolios()[0].getFolioNumber();
        } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
            LOG.log(SEVERE, "Error loading first page for project " + projectID, ex);
            throw ex;
        }
    }

    /**
     * Get the unique identifier of the next page image to be displayed
     * according to the sequence being used by this Project
     *
     * @param current
     * @return
     * @throws java.sql.SQLException
     */
    public int getFollowingPage(int current) throws SQLException {
        Folio[] fols = this.getFolios();
        for (int i = 0; i < fols.length; i++) {
            if (fols[i].getFolioNumber() == current && i + 1 < fols.length) {
                return (fols[i + 1].folioNumber);
            }
        }
        return 0;
    }

    /**
     * Get the unique identifier of the previous page image according to the
     * sequence being used by this Project
     *
     * @param current
     * @return
     * @throws java.sql.SQLException
     */
    public int getPreceedingPage(int current) throws SQLException {
        Folio[] fols = this.getFolios();
        for (int i = 0; i < fols.length; i++) {
            if (fols[i].getFolioNumber() == current && i != 0) {
                return (fols[i - 1].folioNumber);
            }
        }
        return 0;
    }

    /**
     * Build OAC annotations our of the lines transcription
     *
     * @param folioNumber
     * @return
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public String getOAC(int folioNumber) throws SQLException, IOException {
        Model model = createDefaultModel();
        Transcription[] transcriptions = getProjectTranscriptions(projectID, folioNumber);
        for (Transcription transcription : transcriptions) {
            StringReader r = new StringReader(transcription.getAsOAC());
            model.read(r, "");
        }
        StringWriter w = new StringWriter();
        model.write(w, "N3");

        return w.toString();
    }

    /**
     * @return @throws java.sql.SQLException
     * @Depricated this code needs to be updated to build a sequence after the
     * changes to OAC
     */
    public String getOACSequence() throws SQLException {
        Model model = createDefaultModel();
        model.setNsPrefix("dms", "http://dms.stanford.edu/ns/");
        model.setNsPrefix("oac", "http://www.openannotation.org/ns/");
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("ore", "http://www.openarchives.org/ore/terms/");
        model.setNsPrefix("cnt", "http://www.w3.org/2008/content#");
        model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
        model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");

        Folio[] fols = this.getFolios();
        Resource sequence = model.createResource("http://t-pen.org/sequences/");
        Property rdfType = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
        RDFList l = model.createList(new RDFNode[]{sequence});
        Property aggregates = model.createProperty("http://www.openarchives.org/ore/terms/", "aggregates");
        for (Folio fol : fols) {
            Resource view = model.createResource("http://t-pen.org/views/" + fol.folioNumber);
            view.addProperty(rdfType, view);
            sequence.addProperty(aggregates, view);
            l.add(view);
        }
        StringWriter tmp = new StringWriter();
        model.write(tmp);
        return tmp.getBuffer().toString();
    }

    /**
     * Add a new Project log comment
     *
     * @param conn Connection
     * @param text
     * @param uid
     * @throws java.sql.SQLException
     */
    public void addLogEntry(Connection conn, String text, int uid) throws SQLException {
        try (PreparedStatement deleteStmt = conn.prepareStatement("delete from projectlog where content=? and uid=? and projectID=?")) {
            deleteStmt.setInt(3, projectID);
            deleteStmt.setInt(2, uid);
            deleteStmt.setString(1, text);
            deleteStmt.executeUpdate();

            try (PreparedStatement insertStmt = conn.prepareStatement("insert into projectlog(projectID, uid, content) values(?,?,?)")) {
                insertStmt.setInt(1, projectID);
                insertStmt.setInt(2, uid);
                insertStmt.setString(3, text);
                insertStmt.executeUpdate();
            }
        }
    }

    /**
     * Retrieve the log of Project comments in order by date.
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getProjectLog() throws SQLException {
        String query = "select content,creationDate,uid from projectlog where projectID=? order by creationDate desc limit 1000";
        StringBuilder toret = new StringBuilder("");
        Connection j = null;
        PreparedStatement qry = null;
        try {
            j = getConnection();
            qry = j.prepareStatement(query);
            qry.setInt(1, projectID);
            ResultSet rs = qry.executeQuery();
            while (rs.next()) {
                User u = new User(rs.getInt("uid"));
                SimpleDateFormat d = new SimpleDateFormat();
                toret.append("<div class=\"logEntry\"><div class=\"logDate\">").append(d.format(rs.getTimestamp("creationDate"))).append("</div>");
                toret.append("<div class=\"logAuthor\">").append(encoder().encodeForHTML(u.getFname() + " " + u.getLname())).append("</div>");
                //removed esapi encoding 
                toret.append("<div class=\"logContent\">").append(rs.getString("content")).append("</div></div>");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(qry);
        }
        return toret.toString();
    }

    /**
     * Retrieve the requested number of Project comments in order by date, from
     * date.
     *
     * @param recordCount
     * @param firstRecord
     * @return
     * @throws java.sql.SQLException
     */
    public String getProjectLog(int recordCount, int firstRecord) throws SQLException {
        String query = "select content,creationDate,uid from projectlog where projectID=? order by creationDate desc limit ?,?";
        StringBuilder toret = new StringBuilder("");
        Connection j = null;
        PreparedStatement qry = null;
        try {
            j = getConnection();
            qry = j.prepareStatement(query);
            qry.setInt(1, projectID);
            qry.setInt(2, firstRecord);
            qry.setInt(3, recordCount);
            ResultSet rs = qry.executeQuery();
            while (rs.next()) {
                User u = new User(rs.getInt("uid"));
                SimpleDateFormat d = new SimpleDateFormat();
                toret.append("<div class=\"logEntry\"><div class=\"logDate\">").append(d.format(rs.getTimestamp("creationDate"))).append("</div>");
                toret.append("<div class=\"logAuthor\">").append(encoder().encodeForHTML(u.getFname() + " " + u.getLname())).append("</div>");
                //removed esapi encoding to allow links in entries
                toret.append("<div class=\"logContent\">").append(rs.getString("content")).append("</div></div>");
            }
        } finally {
            if (j != null) {
                closeDBConnection(j);
            }
            closePreparedStatement(qry);
        }
        return toret.toString();
    }

    /**
     * Retrieve the requested number of Project comments in order by date.
     *
     * @param recordCount
     * @return
     * @throws java.sql.SQLException
     */
    public String getProjectLog(int recordCount) throws SQLException {
        return getProjectLog(recordCount, 0);
    }

    public String getSchemaURL() throws SQLException {
        String toret = "";
        Connection j = null;
        PreparedStatement ps = null;
        try {
            j = getConnection();
            ps = j.prepareStatement("select schemaURL from project where id=?");
            ps.setInt(1, this.projectID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("schemaURL");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
        return toret;
    }

    /**
     * Update the project URL.If it's not a valid URL (typically because it's an
     * empty string), we just smother the exception.I
     *
     * @param conn
     * @param newURL
     * @throws java.sql.SQLException
     */
    public void setSchemaURL(Connection conn, String newURL) throws SQLException {
        try {
            URL schemaURL = new URL(newURL);
            try (PreparedStatement ps = conn.prepareStatement("update project set schemaURL=? where id=?")) {
                ps.setInt(2, this.projectID);
                ps.setString(1, newURL);
                ps.executeUpdate();
            }
        } catch (MalformedURLException ignored) {
        }
    }

    private static final Logger LOG = getLogger(Project.class.getName());
}
