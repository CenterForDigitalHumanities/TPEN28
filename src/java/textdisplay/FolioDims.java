/**
 *
 * @author Bryan Haberberger at the Walter J. Ong, SJ Center for Digital Humanities at Saint Louis University
 */
package textdisplay;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class FolioDims {
    public int folioID = -1;
    public int projectfolioID = -1;
    private Dimension naturalImageSize = new Dimension(0,0); //The other classes expect a Dimension object sometimes, so let's be responsible for creating it here.
    private int folioDimID= -1;
    public int height = 0;
    public int width = 0;
    
    /* 
        Construct a FolioDim object from a given projectfolio ID
        @params folioID: a folio id
    */
    public FolioDims(int folioDimsID) throws SQLException{
        try (Connection j = DatabaseWrapper.getConnection()) {
            folioDimID = folioDimsID;
            try (PreparedStatement stmt = j.prepareStatement("Select * from foliodim where projectfolioID=?")) {
                stmt.setInt(1, projectfolioID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                   folioID = rs.getInt("folioID");
                   height = rs.getInt("height");
                   width = rs.getInt("width");
                   projectfolioID = rs.getInt("projectfolioID");
                   generateDimension(width, height);
                }
            }
        }
    }

    /* 
        Construct a FolioDim object from a given folio ID
        @params folioID: a folio id
    */
    public FolioDims(int folID, boolean folioFlag) throws SQLException{
        try (Connection j = DatabaseWrapper.getConnection()) {
            folioID = folID;
            try (PreparedStatement stmt = j.prepareStatement("Select * from foliodim where folioID=?")) {
                stmt.setInt(1, folioID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                   projectfolioID = rs.getInt("projectfolioID");
                   height = rs.getInt("height");
                   width = rs.getInt("width");
                   folioDimID = rs.getInt("folioDimID");
                   generateDimension(width, height);
                }
            }
        }
    }
    
    
    /* 
        Construct a FolioDim object from a given projectfolioID
        @params folioID: a folio id
    */
    public FolioDims(int projectFolioID, boolean flag, boolean projectfolioFlag) throws SQLException{
        try (Connection j = DatabaseWrapper.getConnection()) {
            projectfolioID = projectFolioID;
            try (PreparedStatement stmt = j.prepareStatement("Select * from foliodim where folioDimID=?")) {
                stmt.setInt(1, projectfolioID);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                   folioID = rs.getInt("folioID");
                   height = rs.getInt("height");
                   width = rs.getInt("width");
                   folioDimID = rs.getInt("folioDimID");
                   generateDimension(width, height);
                }
            }
        }
    }
    
    /*
      Create a new FolioDims record in SQL with a given width, height, folioID and projectFolioID.  Return the ID generated.  
    */
    public static int createFolioDimsRecord(int w, int h, int folioID, int projectFolioID) throws SQLException{
        Connection j = null;
        PreparedStatement stmt = null;
        try {
           String query = "insert into foliodim (folioID, projectfolioID, width, height) values(?,?,?,?)";
           j = DatabaseWrapper.getConnection();
           stmt = j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           stmt.setInt(1, folioID);
           stmt.setInt(2, projectFolioID);
           stmt.setInt(3, w);
           stmt.setInt(4, h);
           stmt.execute();
           ResultSet rs = stmt.getGeneratedKeys();
           if (rs.next()) {
              int toret = rs.getInt(1); //primary key when the table is created, which is going to be folioDimID
              return (toret);
           } else {
              return 0;
           }
        } finally {
           DatabaseWrapper.closeDBConnection(j);
           DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    
    /*
      Create a new FolioDims record in SQL with a given width, height and folioID.  Return the ID generated.  
    */
    public static int createFolioDimsRecord(int w, int h, int folioID) throws SQLException{
        Connection j = null;
        PreparedStatement stmt = null;
        try {
           String query = "insert into foliodim (folioID, projectfolioID, width, height) values(?,?,?,?)";
           j = DatabaseWrapper.getConnection();
           stmt = j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           stmt.setInt(1, folioID);
           stmt.setInt(2, -1);
           stmt.setInt(3, w);
           stmt.setInt(4, h);
           stmt.execute();
           ResultSet rs = stmt.getGeneratedKeys();
           if (rs.next()) {
              int toret = rs.getInt(1); //primary key when the table is created, which is going to be folioDimID
              return (toret);
           } else {
              return 0;
           }
        } finally {
           DatabaseWrapper.closeDBConnection(j);
           DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    
    /*
        From a given width and height, generate the Dimension object stored with this FolioDim
    */
    private void generateDimension(int w, int h){
        width = w;
        height = h;
        naturalImageSize = new Dimension(w,h);   
    }
    
    public int getFolioID(){
        return folioID;
    }
    public int getProjectfolioID(){
        return projectfolioID;
    }
    public Dimension getNaturalImageDimensions(){
        return naturalImageSize;
    }
    public int getFolioDimsID(){
        return folioDimID;
    }
    public int getDimHeight(){
        return height;
    }
    public int getDimWidth(){
        return width;
    }
    
    public void setFolioID(int fID){
        folioID = fID;
    }
    public void setProjectfolioID(int pfID){
        projectfolioID = pfID;
    }
    public void setNaturalImageSize(Dimension size){
        naturalImageSize = size;
    }
    public void setDimheight(int dimheight){
        height = dimheight;
    }
    public void setDimwidth(int dimwidth){
        width = dimwidth;
    }
    
       
}
