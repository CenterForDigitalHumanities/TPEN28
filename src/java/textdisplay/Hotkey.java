/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */
package textdisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;

/**customizable hotkeys for transcribing non enlgish texts*/
public class Hotkey {

    int uid;
    int position;
    int key;
    int projectID = 0;

    public Hotkey() {
    }
    
    /**
     * Create a new project Hotkey and store it
     * @param code the integer keycode for the character
     * @param projectID
     * @param position position this button falls in, used to order the output of all buttons
     * @param isProject distinguishes this from a button intended for on-the-fly transcription
     * @throws SQLException
     */
    public Hotkey(int code, int projectID, int position, Boolean isProject) throws SQLException {
        String query = "insert into hotkeys(projectID,uid,position,`key`) values (?,0,?,?)";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(3, code);
            stmt.setInt(1, projectID);
            stmt.setInt(2, position);
            stmt.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }

    /**
     * Add a new Hotkey for use in on-the-fly transcription
     * @param code the integer keycode for the character
     * @param uid user unique id under which this should be stored
     * @param position position this button falls in, used to order the output of all buttons
     * @throws SQLException
     */
    public Hotkey(int code, int uid, int position) throws SQLException {
        String query = "insert into hotkeys(uid,position,`key`) values (?,?,?)";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(3, code);
            stmt.setInt(1, uid);
            stmt.setInt(2, position);
            stmt.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }

    }
    
    /**
     * Add a new Hotkey based on project
     * @param code the integer keycode for the character
     * @param uid user unique id under which this should be stored
     * @param position position this button falls in, used to order the output of all buttons
     * @throws SQLException
     */
    public Hotkey(int code, int projectID, int position, boolean hello, boolean isProject) throws SQLException {
        String query = "insert into hotkeys(projectID, position,`key`, uid) values (?,?,?,?)";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(3, code);
            stmt.setInt(1, projectID);
            stmt.setInt(2, position);
            stmt.setInt(4, 0);
            stmt.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }

    }

    /**Get an existing Hotkey based on the current user and the key position (1-10)*/
    public Hotkey(int uid, int position) throws SQLException {
        String query = "select * from hotkeys where uid=? and position=? and projectID=0";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();

            stmt = j.prepareStatement(query);
            stmt.setInt(1, uid);
            stmt.setInt(2, position);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.uid = rs.getInt("uid");
                this.position = rs.getInt("position");
                this.key = rs.getInt("key");
            } else {
                this.uid = 0;
                this.position = 0;
                this.key = 0;
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }

    /**Get an existing Hotkey based on the current project and the key position (1-10)*/
    public Hotkey(int projectID, int position, Boolean project) throws SQLException {
        String query = "select * from hotkeys where projectID=? and position=?";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();

            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            stmt.setInt(2, position);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.projectID = rs.getInt("projectID");
                this.uid = 0;
                this.position = rs.getInt("position");
                this.key = rs.getInt("key");
            } else {
                this.uid = 0;
                this.position = position;
                this.key = 0;
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }

    }
    
    /**Get an existing Hotkey based on the current project*/
    public List<Hotkey> getProjectHotkeyByProjectID(int projectID, int uid) throws SQLException {
        List<Hotkey> ls_hk = new ArrayList();
        String query = "select * from hotkeys where projectID=?";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Hotkey hk = new Hotkey();
                hk.projectID = rs.getInt("projectID");
                hk.uid = uid;
                hk.position = rs.getInt("position");
                hk.key = rs.getInt("key");
                ls_hk.add(hk);
            } else {
                Hotkey hk = new Hotkey();
                hk.uid = uid;
                hk.position = position;
                hk.key = 0;
                ls_hk.add(hk);
            }
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
        return ls_hk;
    }
    
    /**Check to see if this key was actually populated with useful data when instatiated*/
    public Boolean exists() {
        if (key == 0) {
            return false;
        }
        return true;
    }
    /**Set the value for this hotkey*/
    public void setKey(int newKey) throws SQLException {
        if (!this.exists()) {
            return;
        }
        String query = "update hotkeys set `key`=? where uid=? and position=? and projectID=0";
        if (projectID > 0) {
            query = "update hotkeys set `key`=? where projectID=? and position=?";
        }

        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, newKey);
            if (projectID == 0) {
                stmt.setInt(2, uid);
            } else {
                stmt.setInt(2, projectID);
            }
            stmt.setInt(3, position);
            this.key = newKey;
            stmt.execute();

        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }

    /**
    @deprecated use Hotkey(int projectID, Boolean isProject)
     */
    public Hotkey(int uid) throws SQLException {
        this.uid = uid;
    }
    /**Constructor for project based buttons. All buttons are project based, but there was a time when that was not the case.*/
    public Hotkey(int projectID, Boolean isProject) throws SQLException {
        this.projectID = projectID;
    }
    /**Return the value for this key. will be a decimal integer, but represented as a String*/
    public String getButton() {
        return "" + key;
    }
/**Return the value for this key as an integer*/
    public int getButtonInteger() {
        return key;
    }

    /**change the position of the button from its current*/
    public void changePosition(int newPos) throws SQLException {
        String query = "update hotkeys set position=? where uid=? and position=? and projectID=0";
        if (projectID > 0) {
            query = "update hotkeys set position=? where projectID=? and position=?";
        }
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, newPos);
            if (projectID > 0) {
                stmt.setInt(2, projectID);
            } else {
                stmt.setInt(2, uid);
            }
            stmt.setInt(3, position);

            stmt.execute();
            this.position = newPos;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }
    /**@deprecated  use public String javascriptToAddProjectButtons(int projectID)*/
    public String javascriptToAddButtons(int uid) throws SQLException {
        String toret = "";
        String vars = "<script>";
        String query = "select * from hotkeys where uid=? order by position";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, uid);
            ResultSet rs = stmt.executeQuery();
            int buttonOffset = 48;
            int ctr = 0;
            while (rs.next()) {
                ctr++;
                char chara = (char) (rs.getInt("key"));
                int button = rs.getInt("position") + buttonOffset;
                //toret+="<script>if(pressedkey=="+(buttonOffset+rs.getInt("position"))+"){addchar('&#"+rs.getInt("key")+";');  return false;}</script>";
                vars += "var char" + button + "=\"" + rs.getInt("key") + "\";\n";
                toret += "<span class=\"lookLikeButtons\"  onclick=\"Interaction.addchar('&#" + rs.getInt("key") + ";');\">&#" + rs.getInt("key") + ";<sup>" + rs.getInt("position") + "</sup></span>";
            }
            if (ctr == 0) {
//                Hotkey ha;
//                ha = new Hotkey(222, uid, 1);
//                ha = new Hotkey(254, uid, 2);
//                ha = new Hotkey(208, uid, 3);
//                ha = new Hotkey(240, uid, 4);
//                ha = new Hotkey(503, uid, 5);
//                ha = new Hotkey(447, uid, 6);
//                ha = new Hotkey(198, uid, 7);
//                ha = new Hotkey(230, uid, 8);
//                ha = new Hotkey(540, uid, 9);
//                return this.javascriptToAddButtons(uid);
            }
            vars += "</script>";
            return vars + toret;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }
    
    public static String javascriptToBuildEditableButtons(int projectID) throws SQLException{
        String toret = "";
        String query = "select * from hotkeys where uid=0 and projectID=? and not position=0 order by position";
        Connection j = null;
        PreparedStatement stmt = null;
        //System.out.println("build editable buttons");
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            //System.out.println("DO sql...");
            ResultSet rs = stmt.executeQuery();
            //System.out.println("OK");
            int buttonOffset = 48;
            String ctr = "";
            while (rs.next()) {
                int position = rs.getInt("position");
                int key = rs.getInt("key");
                String btn = ""+key;
                ctr = ""+position;
                //System.out.println("got one");
                toret += "<li class=\"ui-state-default\"><input readonly class=\"label hotkey\" name=\"a"+ctr+"a\" id=\"a"+ctr+"a\" value=\""+(char)key+"\" tabindex=-5>";
                toret += "<input class=\"shrink\" onkeyup=\"updatea(this);\" name=\"a"+ctr+"\" id=\"a"+ctr+"\" type=\"text\" value=\""+btn+"\"></input>";
                toret += "<a class=\"ui-icon ui-icon-closethick right\" onclick=\"deleteHotkey(" + ctr + ");\">delete</a></li>";
            }
            return toret;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }
    /**Build the javascript used to drive all hotkeys that are part of this project*/
    public static String javascriptToAddProjectButtons(int projectID) throws SQLException {
        String toret = "";
        String vars = "<script>";
        String query = "select * from hotkeys where uid=0 and projectID=? and not position=0 order by position";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            ResultSet rs = stmt.executeQuery();
            int buttonOffset = 48;
            int ctr = 0;
            while (rs.next()) {
                ctr++;
                char chara = (char) (rs.getInt("key"));
                int button = rs.getInt("position") + buttonOffset;
                vars += "var char" + button + "=\"" + rs.getInt("key") + "\";\n";
                toret += "<span class=\"lookLikeButtons\"  onclick=\"Interaction.addchar('&#" + rs.getInt("key") + ";');\">&#" + rs.getInt("key") + ";<sup>" + rs.getInt("position") + "</sup></span>";
                //toret += "&#" + rs.getInt("key")+";<br>";
            }
            if (ctr == 0) {

//                new Hotkey(222, projectID, 1, true);
//                new Hotkey(254, projectID, 2, true);
//                new Hotkey(208, projectID, 3, true);
//                new Hotkey(240, projectID, 4, true);
//                new Hotkey(503, projectID, 5, true);
//                new Hotkey(447, projectID, 6, true);
//                new Hotkey(198, projectID, 7, true);
//                new Hotkey(230, projectID, 8, true);
//                new Hotkey(540, projectID, 9, true);
//                return this.javascriptToAddButtons(uid);
            }
            vars += "</script>";
            return toret;
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }
    
    /**Build the javascript used to drive all hotkeys that are part of this project*/
    public String javascriptToAddProjectButtonsRawData(int projectID) throws SQLException {
        String query = "select * from hotkeys where uid=0 and projectID=? and not position=0 order by position";
        Connection j = null;
        PreparedStatement stmt = null;
        try {
            j = getConnection();
            stmt = j.prepareStatement(query);
            stmt.setInt(1, projectID);
            ResultSet rs = stmt.executeQuery();
            int ctr = 0;
            JSONArray ja = new JSONArray();
            while (rs.next()) {
                ctr++;
                JSONObject jo = new JSONObject();
                jo.element("key", rs.getInt("key"));
                jo.element("position", rs.getInt("position"));
                jo.element("uid", rs.getInt("uid"));
                ja.add(jo);
            }
            if (ctr == 0) {
                //These are default ones.  Do we still want them?
//                new Hotkey(222, projectID, 1, true);
//                new Hotkey(254, projectID, 2, true);
//                new Hotkey(208, projectID, 3, true);
//                new Hotkey(240, projectID, 4, true);
//                new Hotkey(503, projectID, 5, true);
//                new Hotkey(447, projectID, 6, true);
//                new Hotkey(198, projectID, 7, true);
//                new Hotkey(230, projectID, 8, true);
//                new Hotkey(540, projectID, 9, true);
//                return this.javascriptToAddButtons(uid);
            }
            return ja.toString();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
        }
    }

    /**
     * @deprecated 
     * @param uid
     * @return 
     */
    public String keyhandler(int uid) {
        String toret = "";

        return toret;
    }

    /**Remove this key*/
    public void delete() throws SQLException {
        if (this.projectID > 0) {
            String query = "delete from hotkeys where projectID=? and position=?";
            Connection j = null;
            PreparedStatement ps = null;
            PreparedStatement update=null;
            try {
                j = getConnection();
                ps = j.prepareStatement(query);
                ps.setInt(1, projectID);
                ps.setInt(2, position);
                ps.execute();
                update = j.prepareStatement("update hotkeys set position=? where position=? and projectID=?");
                //Adjust the position of all of the buttons with positions greater than this to be 1 less than they were
                while (true) {
                    Hotkey k = new Hotkey(projectID, position + 1, true);
                    if (k.exists()) {
                        update.setInt(1, position);
                        update.setInt(2, position + 1);
                        update.setInt(3, projectID);
                        update.execute();
                        position++;
                    } else {
                        break;
                    }
                }

            } finally {
                closeDBConnection(j);
                closePreparedStatement(ps);
                closePreparedStatement(update);
                
            }
        } else {
            String query = "delete from hotkeys where uid=? and position=?";
            Connection j = null;
            PreparedStatement ps = null;
            PreparedStatement update=null;
            try {
                j = getConnection();
                ps = j.prepareStatement(query);
                ps.setInt(1, uid);
                ps.setInt(2, position);
                ps.execute();
                update = j.prepareStatement("update hotkeys set position=? where position=? and uid=?");
                //Adjust the position of all of the buttons with positions greater than this to be 1 less than they were
                while (true) {
                    Hotkey k = new Hotkey(uid, position + 1);
                    if (k.exists()) {
                        update.setInt(1, position);
                        update.setInt(2, position + 1);
                        update.setInt(3, uid);
                        update.execute();
                        position++;
                    } else {
                        break;
                    }
                }
            } finally {
                closeDBConnection(j);
                closePreparedStatement(ps);
                closePreparedStatement(update);
            }
        }
    }
}
