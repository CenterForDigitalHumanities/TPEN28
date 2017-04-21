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
    public Timestamp upgradeDate = null;
    public boolean countdown = false;
    //public Timestamp upgradeTime = null;
    public String upgradeMessage = "";
    public boolean active = false;
    
    /* Get the current upgrade settings */
    public UpgradeManager() throws SQLException{
        Connection j = null;
        try (PreparedStatement stmt = j.prepareStatement("Select * from upgrademanager")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
               upgradeDate = rs.getTimestamp("upgradeDate");
               //upgradeTime = rs.getTimestamp("upgradeTime");
               upgradeMessage = rs.getString("upgradeMessage");
               countdown = rs.getBoolean("countdown");
               active = rs.getBoolean("active");
            }
        } 
    }
    
    /* Set new upgrade settings */
    public UpgradeManager(Timestamp d, String m, Boolean c, boolean a) throws SQLException{
        Connection j = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        try {
           String query = "update upgrademanager set upgradeDate=?, upgradeMessage=?, countdown=?, active=true where managerID=1";
           String query2 = "insert into upgrademanager (upgradeDate, upgradeTime, upgradeMessage, countdown) values(?,?,?) WHERE changes() = 0";
           j = DatabaseWrapper.getConnection();
           stmt = j.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
           stmt.setTimestamp(1, d);
           stmt.setString(2, m);
           stmt.setBoolean(3, c);
           stmt.execute();
           stmt2 = j.prepareStatement(query2, PreparedStatement.RETURN_GENERATED_KEYS);
           stmt2.setTimestamp(1, d);
           stmt2.setString(2, m);
           stmt2.setBoolean(3, c);
           stmt2.execute();
           upgradeDate = d;
           //upgradeTime = t;
           upgradeMessage = m;
           countdown = c;
           active = true;
        } finally {
           DatabaseWrapper.closeDBConnection(j);
           DatabaseWrapper.closePreparedStatement(stmt);
        }
    }
    
    public void deactivateUpgrade() throws SQLException{
        Connection j = null;
        PreparedStatement stmt = null;
        String query = "update upgrademanager set active=false where managerID=1";
        stmt = j.prepareStatement(query);
        stmt.execute();
        active = false;
    }
    
    public Date getUpgradeDate(){
        return upgradeDate;
    }
//    public Timestamp getUpgradeTime(){
//        return upgradeTime;
//    }
    public String getUpgradeMessage(){
        return upgradeMessage;
    }
    public boolean checkCountdown(){
        return countdown;
    }
    public boolean checkActive(){
        return active;
    }
    
    public void setUpgradeDate(Timestamp d){
        upgradeDate = d;
    }
//    public void setUpgradeTime(Timestamp t){
//        upgradeTime = t;
//    }
    public void setUpgradeMessage(String m){
        upgradeMessage = m;
    }
    public void setCountdown(Boolean b){
        countdown = b;
    }
    public void setActive(Boolean b){
        active = b;
    }
}
