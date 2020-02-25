/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;
import textdisplay.Manuscript;
import textdisplay.Project;

/**
 *
 * Check the tpen database to identify any orphaned or incomplete data.
 */
public class DatastoreValidator {

    public static void main(String[] args) {
        try {
            DatastoreValidator dv = new DatastoreValidator();
            //System.out.print(dv.delete());
            getLogger(DatastoreValidator.class.getName()).log(SEVERE, dv.results);
        } catch (SQLException ex) {
            getLogger(DatastoreValidator.class.getName()).log(SEVERE, null, ex);
        }
    }
    public String results;

    public DatastoreValidator() throws SQLException {

    }

    private String delete() throws SQLException {
        String delresults = "";
        delresults += deleteEmptyManuscripts();
        delresults += deleteEmptyProjects();
        delresults += deleteOrphanedImages();
        delresults += deleteOrphanedProjectImages();
        delresults += deleteTranscriptionsOnOrphanedProjects();
        delresults += deleteTranscriptionsOnOrphanedImages();
        return delresults;

    }
    /**Check for manuscripts with no attached images, return a report listing any that were found*/

    public static String checkEmptyManuscripts() throws SQLException {
        String results = "Begin check for empty manuscripts\n";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            String query = "select * from manuscript where not exists(select * from folios where msID=manuscript.id)";
            j = getConnection();
            stmt = j.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Manuscript ms = new Manuscript(rs.getInt("id"), true);
                results += "MS " + ms.getID() + " " + ms.getShelfMark() + " has no images!\n";
                while (rs.next()) {
                    ms = new Manuscript(rs.getInt("id"), true);
                    results += "MS " + ms.getID() + " " + ms.getShelfMark() + " has no images!\n";
                }
            } else {
                results += "No problems\n";
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results;

    }

    public static String deleteEmptyManuscripts() throws SQLException {
        String results = "Begin delete for empty manuscripts\n";
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteStmt = null;

        try {
            String query = "select * from manuscript where not exists(select * from folios where msID=manuscript.id)";
            String deleteQuery = "delete from manuscript where id=?";
            j = getConnection();
            stmt = j.prepareStatement(query);
            deleteStmt = j.prepareStatement(deleteQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Manuscript ms = new Manuscript(rs.getInt("id"), true);
                deleteStmt.setInt(1, ms.getID());
                deleteStmt.execute();
                results += "Deleted MS " + ms.getID() + " " + ms.getShelfMark() + "!\n";
                while (rs.next()) {
                    ms = new Manuscript(rs.getInt("id"), true);
                    deleteStmt.setInt(1, ms.getID());
                    deleteStmt.execute();
                    results += "Deleted MS " + ms.getID() + " " + ms.getShelfMark() + "!\n";
                }
            } else {
                results += "Nothing to delete\n";
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results;

    }
    /**look for image with no matching manuscript, return a report listing any that were found*/

    public static String checkOrphanedImages() throws SQLException {
        StringBuffer results = new StringBuffer("Begin check for orphaned images\n");
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            String query = "select * from folios where not exists(select * from manuscript where id=folios.msID)";
            j = getConnection();
            stmt = j.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                results.append("folio ").append(rs.getInt("pageNumber")).append(" ").append(rs.getString("uri")).append(" belongs to a non existant manuscript!\n");
                while (rs.next()) {
                    results.append("folio ").append(rs.getInt("pageNumber")).append(" ").append(rs.getString("uri")).append(" belongs to a non existant manuscript!\n");
                }
            } else {
                results.append("No problems\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }

    public static String deleteOrphanedImages() throws SQLException {
        StringBuffer results = new StringBuffer("Begin delete for orphaned images\n");
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteStmt = null;
        try {
            String query = "select * from folios where not exists(select * from manuscript where id=folios.msID)";
            String deleteQuery = "delete from folios where pageNumber=?";
            j = getConnection();
            stmt = j.prepareStatement(query);
            deleteStmt = j.prepareStatement(deleteQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                deleteStmt.setInt(1, rs.getInt("pageNumber"));
                deleteStmt.execute();
                results.append("deleted folio ").append(rs.getInt("pageNumber")).append(" ").append(rs.getString("uri")).append("!\n");
                while (rs.next()) {
                    deleteStmt.setInt(1, rs.getInt("pageNumber"));
                    deleteStmt.execute();
                    results.append("deleted folio ").append(rs.getInt("pageNumber")).append(" ").append(rs.getString("uri")).append("!\n");
                }
            } else {
                results.append("Nothing to delete\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }

    /**look for images in projects that do not exist, return a report listing any that were found*/
    public static String checkOrphanedProjectImages() throws SQLException {
        StringBuilder results = new StringBuilder("Begin check for images in non existant projects\n");
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            String query = "select * from projectfolios where not exists(select * from folios where pageNumber=projectfolios.folio)";
            j = getConnection();
            stmt = j.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                results.append("folio ").append(rs.getInt("folio")).append(" in project ").append(rs.getInt("project")).append(" belongs to a non existant project!\n");
                while (rs.next()) {
                    results.append("folio ").append(rs.getInt("folio")).append(" in project ").append(rs.getInt("project")).append(" belongs to a non existant project!\n");
                }
            } else {
                results.append("No problems\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }

    public static String deleteOrphanedProjectImages() throws SQLException {
        StringBuilder results = new StringBuilder("Begin delete for images in non existant projects\n");
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteStmt = null;
        try {
            String query = "select * from projectfolios where not exists(select * from folios where pageNumber=projectfolios.folio)";
            String deleteQuery = "delete from projectfolios where project=? and folio=?";
            j = getConnection();
            stmt = j.prepareStatement(query);
            deleteStmt = j.prepareStatement(deleteQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                deleteStmt.setInt(1, rs.getInt("project"));
                deleteStmt.setInt(2, rs.getInt("folio"));
                deleteStmt.execute();
                results.append("Deleted folio ").append(rs.getInt("folio")).append(" in project ").append(rs.getInt("project")).append("!\n");
                while (rs.next()) {
                    deleteStmt.setInt(1, rs.getInt("project"));
                    deleteStmt.setInt(2, rs.getInt("folio"));
                    deleteStmt.execute();
                    results.append("Deleted folio ").append(rs.getInt("folio")).append(" in project ").append(rs.getInt("project")).append("!\n");
                }
            } else {
                results.append("Nothing to delete\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }
/**Check for projects with no images in them, return a report listing any that were found*/
    public static String checkEmptyProjects() throws SQLException {
        String results = "Begin check for empty projects\n";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            String query = "select * from project where not exists(select * from projectfolios where project=project.id)";
            j = getConnection();
            stmt = j.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Project p = new Project(rs.getInt("id"));
                results += "Project " + p.getProjectID() + " " + p.getProjectName() + " has no images!\n";
                while (rs.next()) {
                    p = new Project(rs.getInt("id"));
                    results += "Project " + p.getProjectID() + " " + p.getProjectName() + " has no images!\n";
                }
            } else {
                results += "No problems\n";
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results;

    }

    public static String deleteEmptyProjects() throws SQLException {
        String results = "Begin delete for empty projects\n";
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteStmt = null;
        try {
            String query = "select * from project where not exists(select * from projectfolios where project=project.id)";
            String deleteQuery = "delete from project where id=?";
            j = getConnection();
            stmt = j.prepareStatement(query);
            deleteStmt = j.prepareStatement(deleteQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Project p = new Project(rs.getInt("id"));
                deleteStmt.setInt(1, p.getProjectID());
                deleteStmt.execute();
                results += "Deleted project " + p.getProjectID() + " " + p.getProjectName() + "!\n";
                while (rs.next()) {
                    p = new Project(rs.getInt("id"));
                    deleteStmt.setInt(1, p.getProjectID());
                    deleteStmt.execute();
                    results += "Deleted project " + p.getProjectID() + " " + p.getProjectName() + "!\n";
                }
            } else {
                results += "Nothing to delete\n";
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results;

    }
    /**look for transcriptions attached to non existant images, return a report listing any that were found*/

    public static String checkTranscriptionsOnOrphanedImages() throws SQLException {
        StringBuilder results = new StringBuilder("Begin check for transcriptions on non existant images\n");
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            String query = "select * from transcription where not exists(select * from folios where pageNumber=transcription.folio)";
            j = getConnection();
            stmt = j.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                results.append("transcription ").append(rs.getInt("id")).append(" in project ").append(rs.getInt("projectID")).append(" is based on non existant folio ").append(rs.getInt("folio")).append("!\n");
                while (rs.next()) {
                    results.append("transcription ").append(rs.getInt("id")).append(" in project ").append(rs.getInt("projectID")).append(" is based on non existant folio ").append(rs.getInt("folio")).append("!\n");
                }
            } else {
                results.append("No problems\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }

    public static String deleteTranscriptionsOnOrphanedImages() throws SQLException {
        StringBuilder results = new StringBuilder("Begin delete for transcriptions on non existant images\n");
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteStmt = null;
        try {
            String query = "select * from transcription where not exists(select * from folios where pageNumber=transcription.folio)";
            String deleteQuery = "delete from transcription where id=?";
            j = getConnection();
            stmt = j.prepareStatement(query);
            deleteStmt = j.prepareStatement(deleteQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                deleteStmt.setInt(1, rs.getInt("id"));
                deleteStmt.execute();
                results.append("Deleted transcription ").append(rs.getInt("id")).append(" in project ").append(rs.getInt("projectID")).append(" based on non existant folio ").append(rs.getInt("folio")).append("!\n");
                while (rs.next()) {
                    deleteStmt.setInt(1, rs.getInt("id"));
                    deleteStmt.execute();
                    results.append("Deleted transcription ").append(rs.getInt("id")).append(" in project ").append(rs.getInt("projectID")).append(" based on non existant folio ").append(rs.getInt("folio")).append("!\n");
                }
            } else {
                results.append("Nothing to delete\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }
/**Loook for transcriptions which are part of projects that do not exist*/
    public static String checkTranscriptionsOnOrphanedProjects() throws SQLException {
        StringBuilder results = new StringBuilder("Begin check for transcriptions on non existant projects\n");
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            String query = "select * from transcription where not exists(select * from project where id=transcription.projectID)";
            j = getConnection();
            stmt = j.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                results.append("transcription ").append(rs.getInt("id")).append(" in non existant project ").append(rs.getInt("projectID")).append("!\n");
                while (rs.next()) {
                    results.append("transcription ").append(rs.getInt("id")).append(" in non existant project ").append(rs.getInt("projectID")).append("!\n");
                }
            } else {
                results.append("No problems\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }

    public static String deleteTranscriptionsOnOrphanedProjects() throws SQLException {
        StringBuilder results = new StringBuilder("Begin delete for transcriptions on non existant projects\n");
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement deleteStmt = null;
        try {
            String query = "select * from transcription where not exists(select * from project where id=transcription.projectID)";
            String deleteQuery = "delete from transcription where id=?";
            j = getConnection();
            stmt = j.prepareStatement(query);
            deleteStmt = j.prepareStatement(deleteQuery);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                deleteStmt.setInt(1, rs.getInt("id"));
                deleteStmt.execute();
                results.append("Deleted transcription ").append(rs.getInt("id")).append(" in project ").append(rs.getInt("projectID")).append("!\n");
                while (rs.next()) {
                    deleteStmt.setInt(1, rs.getInt("id"));
                    deleteStmt.execute();
                    results.append("Deleted transcription ").append(rs.getInt("id")).append(" in project ").append(rs.getInt("projectID")).append("!\n");
                }
            } else {
                results.append("Nothing to delete\n");
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return results.toString();
    }
}
