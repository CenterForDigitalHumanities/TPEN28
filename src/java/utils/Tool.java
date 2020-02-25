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
package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;
import user.User;
import static utils.Tool.tools.characters;
import static utils.Tool.tools.compare;
import static utils.Tool.tools.fullpage;
import static utils.Tool.tools.history;
import static utils.Tool.tools.inspector;
import static utils.Tool.tools.linebreak;
import static utils.Tool.tools.ltr;
import static utils.Tool.tools.page;
import static utils.Tool.tools.parsing;
import static utils.Tool.tools.preview;
import static utils.Tool.tools.rtl;
import static utils.Tool.tools.unknown;
import static utils.Tool.tools.values;
import static utils.Tool.tools.xml;


/**
 * This is a mechanism for controlling the display of tools internal to TPEN in the transcription UI. You
 * can do things like hide the parsing correction tool if you have already finished checking parsing for the
 * whole project.
 */
public class Tool {

   public enum tools {
      preview, compare, parsing, history, linebreak, fullpage, paleography, sciat, unknown, page, xml, characters, inspector, rtl, ltr
   };

   public Tool(tools toolName, int UID) throws SQLException {
      User u = new User(UID);//make sure the user exists
      String query = "insert into tools(uid,tool) values(?,?)";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ps.setString(2, toolName.toString());
         ps.execute();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }
   
   public Tool(){}
   
   public static void saveTool(String toolName, int UID) throws SQLException {
      User u = new User(UID);//make sure the user exists
      String query = "insert into tools(uid,tool) values(?,?)";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ps.setString(2, toolName);
         ps.execute();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }

   public static void removeTool(tools toolName, int UID) throws SQLException {
      String query = "delete from tools where UID=? and tool=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ps.setString(2, toolName.name());
         ps.execute();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }

   public static void initializeTools(int uid) throws SQLException {
        //These are the tools that get initialized when
      Tool t;
      for (Tool.tools iter : values()) {
          switch(iter){
                case preview:
                  t = new Tool(iter, uid);
                break;
                case compare:
                  t = new Tool(iter, uid);
                break;
                case parsing:
                  t = new Tool(iter, uid);
                break;
//                case abbreviation:
//                  t = new Tool(iter, uid);
//                break;
                case history:
                  t = new Tool(iter, uid);
                break;
                case linebreak:
                  t = new Tool(iter, uid);
                break;
                case fullpage:
                  t = new Tool(iter, uid);
                break;
                case xml:
                  t = new Tool(iter, uid);
                break;
                case characters:
                  t = new Tool(iter, uid);
                break;
                case inspector:
                  t = new Tool(iter, uid);
                break;
                default:
                  //Not a default tool, do nothing.
                
          }
      }
   }

   public static Boolean isToolActive(tools t, int uid) throws SQLException {

      String query = "select * from tools where uid=? and tool=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, uid);
         ps.setString(2, t.toString());
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
            return true;
         }
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
      return false;
   }

   public static tools[] getTools(int UID) throws SQLException {
      tools[] toret = null;
      String query = "select tool from tools where UID=?";
      Stack<String> res = new Stack();
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, UID);
         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
            res.push(rs.getString(1));
         }
         toret = new tools[res.size()];
         int ctr = 0;
         while (!res.empty()) {
            String tmp = res.pop().toLowerCase();
            //We do not want to return an array with null in it, so we added an unknown enum and made this switch.
            switch(tmp){
                case "preview":
                    toret[ctr] = preview;
                break;
                case "compare":
                    toret[ctr] = compare;        
                break;
                case "parsing":
                    toret[ctr] = parsing;        
                break;
                case "history":
                    toret[ctr] = history;        
                break;
                case "linebreak":
                    toret[ctr] = linebreak;        
                break;
                case "fullpage":
                    toret[ctr] = fullpage;        
                break;
                case "xml":
                    toret[ctr] = xml;
                break;
                case "characters":
                    toret[ctr] = characters;
                break;
                case "page":
                    toret[ctr] = page;
                break;
                case "inspector":
                    toret[ctr] = inspector;
                break;
                case "rtl":
                    toret[ctr] = rtl;
                break;
                case "ltr":
                    toret[ctr] = ltr;
                break;
                default:
                    toret[ctr] = unknown;
            }
            ctr++;
         }
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
      return toret;
   }

   public static void removeAll(int uid) throws SQLException {
      String query = "delete from tools where uid=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, uid);
         ps.execute();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }
}
