
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

import static java.lang.System.out;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;

/**
 * This class handles the storage, retrieval, and rendering of the welcome
 * message that should be sent to newly activated users.
 */
public class WelcomeMessage {

    private final String NamePlaceholder = "<name_here>";
    private final String PasswordPlaceholder = "<pass_here>";

    public void SetMessage(String newMessage) throws SQLException {
        Connection j = null;
        PreparedStatement ps = null;
        String query = "update welcomemessage set msg=?";

        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ps.setString(1, newMessage);
            ps.execute();
        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    public String getMessagePlain() throws SQLException, Exception {

        Connection j = null;
        PreparedStatement ps = null;
        String query = "select msg from welcomemessage";

        try {
            j = getConnection();
            ps = j.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return (rs.getString(1));
            }
            throw new Exception("no welcome message found!");

        } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
        }
    }

    public String getMessage(String userName, String pass) throws SQLException, Exception {

        Connection j = null;
        PreparedStatement ps = null;
        String query = "select msg from welcomemessage";
        {
            try {
                j = getConnection();
                ps = j.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String toret = rs.getString(1);
                    if (!toret.contains(this.PasswordPlaceholder)) // embed a password to prevent unexpected results
                    {
                        toret = "Your new password is: " + this.PasswordPlaceholder + "\n";
                    }
                    toret = toret.replace(this.NamePlaceholder, userName).replace(this.PasswordPlaceholder, pass);
                    return toret;
                }
                throw new Exception("no welcome message found!");

            } finally {
                closeDBConnection(j);
                closePreparedStatement(ps);
            }
        }
    }

    public static void main(String[] args) throws SQLException, Exception {
        WelcomeMessage w = new WelcomeMessage();
        String msg = w.getMessage("digitalhumanities@slu.edu", "newpass");
        out.println(msg);

    }
}
