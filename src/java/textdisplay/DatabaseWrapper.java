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
import static java.lang.Class.forName;
import static java.lang.Thread.currentThread;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.dbcp.BasicDataSource;
import static textdisplay.Folio.getRbTok;




/**Contains all of the database connection handling.*/
public class DatabaseWrapper
{
    
    static BasicDataSource d;
    //static BasicDataSource e;
    static
    {

            try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            forName("com.mysql.jdbc.Driver").newInstance();
            } catch (Exception ex) {
                // handle the error
            }
        

    }
    /**Get a connnection from the connection pool*/
    public synchronized static Connection getConnection()
    {
        try
        {
            if(d==null)
            {
                System.out.println("Defining BasicDataSource JDBC driver programmatically.");
                d=new BasicDataSource();
                d.setDriverClassName("com.mysql.jdbc.Driver");
                d.setUrl("jdbc:mysql://"+getRbTok("DATABASE")+"?useUnicode=true&amp;characterEncoding=utf8&amp;max_allowed_packet=16M");
                d.setUsername(getRbTok("DBUSER"));
                d.setPassword(getRbTok("DBPASSWORD"));
                d.setInitialSize(800);
                d.setMaxActive(700);
                d.setMaxIdle(100);
                d.setMaxWait(8000);
                d.setValidationQuery("select 1");
            }
            StackTraceElement [] stack=currentThread().getStackTrace();
            String stackdump="";
            return d.getConnection();
        } 
        catch (SQLException ex)
        {
            getLogger(DatabaseWrapper.class.getName()).log(SEVERE, null, ex);
            //d.close();
            return null;
        }
    }
    
    /**Close the db connection, catching a potentially useless error and ignoring nulls*/
    public static void closeDBConnection(Connection dbconn)
    {
        try{
            if(dbconn!=null)
            {
                dbconn.close();
            }
            
        }
        catch(SQLException e)
        {
            getLogger(DatabaseWrapper.class.getName()).log(INFO, "Failed to close db connection: null\n");
        }
    }
    /**Close a preparedstatement, ignoring the potential for a sql exception because there is nothing to be done about it*/
    public static void closePreparedStatement(PreparedStatement ps)
    {
        try{
            if(ps!=null)
        ps.close();
        }
        catch(SQLException e)
        {
            getLogger(DatabaseWrapper.class.getName()).log(INFO, "Failed to close prepared statement: null\n");
        }
    }
    /**Close a resultset, ignoring the potential for a sql exception because there is nothing to be done about it*/
    public static void closeResultSet(ResultSet rs)
    {
        try{
            if(rs!=null)
                rs.close();
        }
        catch(SQLException e)
        {
            getLogger(DatabaseWrapper.class.getName()).log(INFO, "Failed to close result set: null\n");
        }
        
    }
}
