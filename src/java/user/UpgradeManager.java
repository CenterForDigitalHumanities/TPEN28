/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import textdisplay.DatabaseWrapper;

/**
 *
 * @author bhaberbe
 */
public class UpgradeManager {
    public String upgradeDate = null;
    public int countdown = 0;
    //public Timestamp upgradeTime = null;
    public String upgradeMessage = "";
    public int active = 0;
    
    /* Get the current upgrade settings */
    public UpgradeManager() throws SQLException{
        Connection j = DatabaseWrapper.getConnection();
        PreparedStatement stmt = null;
        stmt = j.prepareStatement("Select * from upgrademanager");
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
           upgradeDate = rs.getString("upgradeDate");
           //upgradeTime = rs.getTimestamp("upgradeTime");
           upgradeMessage = rs.getString("upgradeMessage");
           countdown = rs.getInt("countdown");
           active = rs.getInt("active");
        }
    }
    
    /* Set new upgrade settings */
    public UpgradeManager(String d, String m, int c, int a) throws SQLException{
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        //System.out.println("NEW UPGRADE SETTINGS!");
        try {
           String query = "update upgrademanager set upgradeDate=?, upgradeMessage=?, countdown=?, active=? where managerID=1";
           j = DatabaseWrapper.getConnection();
           stmt = j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           stmt.setString(1, d);
           stmt.setString(2, m);
           stmt.setInt(3, c);
           stmt.setInt(4, 1);
           //System.out.println("SQL statement...");
           stmt.execute();
          // System.out.println("SQL statement executed!");
           upgradeDate = d;
           //upgradeTime = t;
           upgradeMessage = m;
           countdown = c;
           active = 1;
        } finally {
           DatabaseWrapper.closeDBConnection(j);
           DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    
    public void deactivateUpgrade() throws SQLException{
        Connection j = null;
        PreparedStatement stmt = null;
        String query = "update upgrademanager set active=0 where managerID=1";
        j = DatabaseWrapper.getConnection();
        stmt = j.prepareStatement(query);
        stmt.execute();
        active = 0;
    }
    
    public String getUpgradeDate(){
        return upgradeDate;
    }
//    public Timestamp getUpgradeTime(){
//        return upgradeTime;
//    }
    public String getUpgradeMessage(){
        return upgradeMessage;
    }
    public int checkCountdown(){
        return countdown;
    }
    public int checkActive(){
        return active;
    }
    
    public void setUpgradeDate(String d){
        upgradeDate = d;
    }
//    public void setUpgradeTime(Timestamp t){
//        upgradeTime = t;
//    }
    public void setUpgradeMessage(String m){
        upgradeMessage = m;
    }
    public void setCountdown(int b){
        countdown = b;
    }
    public void setActive(int b){
        active = b;
    }
}
