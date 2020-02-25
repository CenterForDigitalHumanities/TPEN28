/*
 * Copyright 2011-2013 Saint Louis University. Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
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
package textdisplay;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import static java.util.Arrays.asList;
import java.util.Date;
import java.util.List;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.xml.transform.stream.StreamSource;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.owasp.esapi.ESAPI.encoder;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;
import static textdisplay.Folio.getRbTok;


/**
 * A button for inserting an XML tag into a transcription
 */
public class TagButton {

   int projectID = -1;
   int uid = -1;
   int position;
   String tag;
   String[] parameters;

   /**
    * Get the description for the tag, used to label the tag button
    *
    * @return the tag description, or the tag text if the description hasn't been set
    */
   public String getDescription() {
      return escapeHtml(description.length() > 0 ? description : tag);
   }
   String description;

   public String getXMLColor() {
      if (xmlColor.length() > 0) {
         return xmlColor;
      } else {
         return "";
      }
   }

   public void updateXMLColor(String color) throws SQLException {
      String query = "update buttons set color=? where project=? and position=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setString(1, color);
         ps.setInt(1, projectID);
         ps.setInt(2, position);
         ps.execute();
         this.xmlColor = color;
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }

   }
   String xmlColor;

   /**
    * Add a new button, tag is the tag name only, no brackets
    */
   public TagButton(int uid, int position, String tag, String description) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "insert into buttons(uid,position,text,description) values (?,?,?,?)";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, uid);
         stmt.setInt(2, position);
         stmt.setString(3, tag);
         stmt.setString(4, description);
         stmt.execute();

         this.tag = tag;
         this.position = position;
         this.uid = uid;
         this.xmlColor = "";
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * Add a new button, tag is the tag name only, no brackets
    */
   public TagButton(int projectID, int position, String tag, Boolean project, String description) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         TagButton t = new TagButton(projectID, position, true);
         t.deleteTag();
      } catch (Exception e) {
            getLogger(TagButton.class.getName()).log(SEVERE, null, e);
      }
      try {
         String query = "insert into projectbuttons(project,position,text,description) values (?,?,?,?)";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, projectID);
         stmt.setInt(2, position);
         stmt.setString(3, tag);
         stmt.setString(4, description);
         stmt.execute();

         this.tag = tag;
         this.position = position;
         this.uid = -1;
         this.projectID = projectID;
         this.xmlColor = "";
      } catch (Exception e) {
            getLogger(TagButton.class.getName()).log(SEVERE, null, e);
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * Add a new button, tag is the tag name only, no brackets. params needs to have a length of 5
    */
   public TagButton(int uid, int position, String tag, String[] params) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "insert into buttons(uid,position,text,param1, param2, param3, param4, param5) values (?,?,?,?,?,?,?,?)";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, uid);
         stmt.setInt(2, position);
         stmt.setString(3, params[0]);
         stmt.setString(4, params[1]);
         stmt.setString(5, params[2]);
         stmt.setString(6, params[3]);
         stmt.setString(7, params[4]);
         stmt.execute();
         for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].contains("null")) {
               params[i] = "";
            }
         }
         this.tag = tag;
         this.position = position;
         this.uid = uid;
         this.xmlColor = "";
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * Add a new button, tag is the tag name only, no brackets. params needs to have a length of 5
    */
   public TagButton(int projectID, int position, String tag, String[] params, Boolean isProject, String description) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         //delete any existing tag in that position
         try {
            TagButton t = new TagButton(projectID, position, true);
            t.deleteTag();
         } catch (Exception e) {
                getLogger(TagButton.class.getName()).log(SEVERE, null, e);
         }
         for (int i = 0; i < params.length; i++) {
            if (params[i] == null || params[i].contains("null")) {
               params[i] = "";
            }
         }
         String query = "insert into projectbuttons(project,position,text,param1, param2, param3, param4, param5, description) values (?,?,?,?,?,?,?,?,?)";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(3, tag);
         stmt.setInt(1, projectID);
         stmt.setInt(2, position);
         stmt.setString(3, tag);
         stmt.setString(4, params[0]);
         stmt.setString(5, params[1]);
         stmt.setString(6, params[2]);
         stmt.setString(7, params[3]);
         stmt.setString(8, params[4]);
         stmt.setString(9, description);
         stmt.execute();

         this.tag = tag;
         this.position = position;
         this.uid = -1;
         this.projectID = projectID;
         this.xmlColor = "";
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * Retrieve and existing button
    */
   public TagButton(int uid, int position) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "select * from buttons where uid=? and position=?";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, uid);
         stmt.setInt(2, position);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
            this.uid = uid;
            this.position = position;
            this.tag = rs.getString("text");
            this.description = rs.getString("description");
            this.xmlColor = rs.getString("color");
            if (rs.getString("param1").length() > 0) {
               parameters = new String[5];

               parameters[0] = encoder().encodeForHTML(rs.getString("param1"));
               if (!parameters[0].contains("&quot;")) {
                  parameters[0] += "=&quot;&quot;";
               }
               if (rs.getString("param2").length() > 0) {
                  parameters[1] = encoder().encodeForHTML(rs.getString("param2"));
                  if (!parameters[1].contains("&quot;")) {
                     parameters[1] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param3").length() > 0) {
                  parameters[2] = encoder().encodeForHTML(rs.getString("param3"));
                  if (!parameters[2].contains("&quot;")) {
                     parameters[2] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param4").length() > 0) {
                  parameters[3] = encoder().encodeForHTML(rs.getString("param4"));
                  if (!parameters[3].contains("&quot;")) {
                     parameters[3] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param5").length() > 0) {
                  parameters[4] = encoder().encodeForHTML(rs.getString("param5"));
                  if (!parameters[4].contains("&quot;")) {
                     parameters[4] += "=&quot;&quot;";
                  }
               }
            }
         } else {
            this.uid = 0;
            this.position = 0;
            this.tag = "";

         }
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * Retrieve and existing button
    */
   public TagButton(int projectID, int position, Boolean isProject) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "select * from projectbuttons where project=? and position=?";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, projectID);
         stmt.setInt(2, position);
         ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
            this.uid = -1;
            this.projectID = projectID;
            this.position = position;
            this.tag = rs.getString("text");
            this.xmlColor = rs.getString("color");
            this.description = rs.getString("description");
            if (rs.getString("param1").length() > 0) {
               parameters = new String[5];

               parameters[0] = encoder().encodeForHTML(rs.getString("param1"));
               if (!parameters[0].contains("&quot;")) {
                  parameters[0] += "=&quot;&quot;";
               }
               if (rs.getString("param2").length() > 0) {
                  parameters[1] = encoder().encodeForHTML(rs.getString("param2"));
                  if (!parameters[1].contains("&quot;")) {
                     parameters[1] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param3").length() > 0) {
                  parameters[2] = encoder().encodeForHTML(rs.getString("param3"));
                  if (!parameters[2].contains("&quot;")) {
                     parameters[2] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param4").length() > 0) {
                  parameters[3] = encoder().encodeForHTML(rs.getString("param4"));
                  if (!parameters[3].contains("&quot;")) {
                     parameters[3] += "=&quot;&quot;";
                  }
               }
               if (rs.getString("param5").length() > 0) {
                  parameters[4] = encoder().encodeForHTML(rs.getString("param5"));
                  if (!parameters[4].contains("&quot;")) {
                     parameters[4] += "=&quot;&quot;";
                  }
               }
            }
         } else {
            this.uid = 0;
            this.position = 0;
            this.tag = "";
         }
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * Set the parameters for this button.
    */
   public void updateParameters(String[] parameters) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         this.parameters = parameters;
         String query;
         if (this.projectID > 0) {
            query = "update projectbuttons set param1=?, param2=?, param3=?, param4=?, param5=? where project=? and position=?";
         } else {
            query = "update buttons set param1=?, param2=?, param3=?, param4=?, param5=? where uid=? and position=?";
         }
         for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null || parameters[i].contains("null")) {
               parameters[i] = "";
            }
         }
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(1, parameters[0]);
         stmt.setString(2, parameters[1]);
         stmt.setString(3, parameters[2]);
         stmt.setString(4, parameters[3]);
         stmt.setString(5, parameters[4]);
         if (projectID > 0) {
            stmt.setInt(6, projectID);
         } else {
            stmt.setInt(6, uid);
         }
         stmt.setInt(7, position);
         stmt.execute();
         //if none of the parameters had content, make sure hasParameters will return false...
         Boolean noParams = true;
         for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].length() > 0) {
               parameters[i] = parameters[i] + "=&quot;&quot;";
               noParams = false;
            }
         }
         if (noParams) {
            this.parameters = null;
         }
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }

   }

   /**
    * Return the parameters as strings. They will contain quotes and an equal sign
    *
    * @return array of 5 parameters
    */
   public String[] getparameters() {
      return parameters;
   }

   /**
    * Are there stored parameters for this button? Useful weh deciding the editing layout
    */
   public Boolean hasParameters() {
      if (parameters != null) {
         return true;
      }
      return false;

   }

   /**
    * Check whether the object populated properly
    *
    * @return false if there was a problem in populating the object previously
    */
   public boolean exists() {
      if (uid == 0 && projectID <= 0) {
         return false;
      }
      return true;
   }

   /**
    * Alter the position of an existing tag.
    */
   public void updatePosition(int newPos) throws SQLException {
      Connection j = null;
      PreparedStatement ps = null;
      try {
         String query = "UPDATE buttons set position=? where uid=? and position=?";
         if (projectID > 0) {
            query = "UPDATE projectbuttons set position=? where project=? and position=?";
         }

         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, newPos);
         ps.setInt(3, position);
         if (projectID > 0) {
            ps.setInt(2, projectID);
         } else {
            ps.setInt(2, uid);
         }
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }

   /**
    * Update the tag label of an existing button.
    */
   public void updateTag(String newTag) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query;
         if (projectID > 0) {
            query = "update projectbuttons set text=? where project=? and position=?";
         } else {
            query = "update buttons set text=? where uid=? and position=?";
         }

         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(1, newTag);
         if (projectID > 0) {
            stmt.setInt(2, projectID);
         } else {
            stmt.setInt(2, uid);
         }
         stmt.setInt(3, position);
         stmt.execute();
         tag = newTag;
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * The actual button markup
    */
   public String getButton() {
      Date date = new Date(currentTimeMillis());
      StackTraceElement[] t = currentThread().getStackTrace();
      DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd H:M:S");
      formatter.format(date);
      String stackTrace = "";
      Boolean caller = false;
        for (StackTraceElement t1 : t) {
            stackTrace += t1.toString() + "\n";
            if (t1.toString().contains("getAllProjectButtonsClassic")) {
                caller = true;
            }
        }
      if (!caller) {
         //LOG.log(Level.SEVERE, "{0} Running tagButton.getAllProjectButtonsClassic\n{1}", new Object[]{formatter.format(date), stackTrace});
      }
      return "<span class=\"lookLikeButtons\" title=\"" + getFullTag() + "\" onclick=\"Interaction.insertTag('" + tag + "', '" + getFullTag() + "');\">" + getDescription() + "</span>";
   }

   /**
    * return oepning and closing tag including brackets
    */
   public String getFullTag() {
        String tagCpy = tag;
        if(tagCpy.contains("&nbsp;/") || tagCpy.contains(" /")){
            tagCpy = tagCpy.replace("&nbsp;/", "");
            tagCpy = tagCpy.replace(" /", "");
        }
        //System.out.println("Now that tag is "+tagCpy);
        String toret = "<" + tagCpy;
        if (parameters != null && parameters.length == 5) {
            if (parameters[0] != null) {
               toret += " " + parameters[0];
            }
            if (parameters[1] != null) {
               toret += " " + parameters[1];
            }
            if (parameters[2] != null) {
               toret += " " + parameters[2];
            }
            if (parameters[3] != null) {
               toret += " " + parameters[3];
            }
            if (parameters[4] != null) {
               toret += " " + parameters[4];
            }
            if(tagCpy.equals(tag)){
               toret += ">"; 
            }
            else{
               toret += " />"; 
            }
            //System.out.println("Return "+toret);
            return toret;
        } 
        else {
            if(tagCpy.equals(tag)){
                return "<" + tagCpy + ">";
            }
            else{
               return "<" + tagCpy + " />";
            }
      }
   }

   /**
    * get the tag text without brackets or parameters
    *
    * @return tag text
    */
   public String getTag() {
      if (tag != null) {
         return tag;
      } else {
         return "";
      }
   }

 public static String getAllProjectButtonsClassic(int projectID) throws SQLException {
      Date date = new Date(currentTimeMillis());
      StackTraceElement[] t = currentThread().getStackTrace();
      DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd H:M:S");
      formatter.format(date);
      String stackTrace = "";
        for (StackTraceElement t1 : t) {
            stackTrace += t1.toString() + "\n";
        }

      //LOG.log(Level.SEVERE, "{0} Running tagButton.getAllProjectButtonsClassic\n{1}", new Object[]{formatter.format(date), stackTrace});
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String toret = "";
         String query = "select distinct(position) from projectbuttons where project=? order by position";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, projectID);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         while (rs.next()) {
            int position = rs.getInt("position");
            try {
               TagButton b = new TagButton(projectID, position, true);
               //System.out.println("Get all project buttons classic.  What is button "+b.getButton());
               toret += b.getButton();
            } catch (NullPointerException e) {
            }
            ctr++;
         }
         if (ctr == 0) {
//            TagButton b = new TagButton(projectID, 1, "temp", true, "button description");
//            b = new TagButton(projectID, 2, "temp", true, "button description");
//            b = new TagButton(projectID, 3, "temp", true, "button description");
//            b = new TagButton(projectID, 4, "temp", true, "button description");
         }
         return toret;
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }
 
  /**
    * get all of the buttons for this Project and build them.  Specifically for use in buttons.jsp
    */
   public static String buildAllProjectXML(int projectID) throws SQLException {
      Date date = new Date(currentTimeMillis());
      StackTraceElement[] t = currentThread().getStackTrace();
      DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd H:M:S");
      formatter.format(date);
      String stackTrace = "";
      String toret = "";
        for (StackTraceElement t1 : t) {
            stackTrace += t1.toString() + "\n";
        }
      //LOG.log(Level.SEVERE, "{0} Running tagButton.getAllProjectButtons\n{1}", new Object[]{formatter.format(date), stackTrace});
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "select distinct(position) from projectbuttons where project=? order by position";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, projectID);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         JSONArray ja = new JSONArray();
         String[] blank_case = new String[5];
         blank_case[0] = null;
         blank_case[1] = null;
         blank_case[2] = null;
         blank_case[3] = null;
         blank_case[4] = null;
         String buttonHTML = "";
         while (rs.next()) {
                buttonHTML = "";
                int position = rs.getInt("position");
                TagButton b = new TagButton(projectID, position, true);
                //System.out.println("Build all project XML.  What is b.getTag()..."+b.getTag());
                buttonHTML += "<li class=\"ui-state-default xmlPanel\">";
                buttonHTML += "<span class='ui-icon ui-icon-arrow-4 toggleXML left'></span>";
                buttonHTML += "<a class=\"ui-icon ui-icon-closethick right\" onclick=\"deleteTag(" + position + ");\">delete</a>";
                buttonHTML += "<input class=\"description\" onchange=\"unsavedAlert('#tabs-2');\" type=\"text\" placeholder=\"Button Name\" name=description"+(position)+" value=\""+b.getDescription()+"\">";
            //    out.println("<input class=\"colors\" onchange=\"unsavedAlert('#tabs-2');\" type=\"text\" placeholder=\"black\" name=xmlColor"+(position)+" value=\""+"b.getXMLColor"+"\">");
                buttonHTML += "<div class='xmlParams'>";
                //THIS may be where the slash is being put in
                buttonHTML += "<span class=\"firstRow collapseXML\"><span class=\"bold tag\"><input name=\"b"+position+"\" id=\"b"+position+"\" type=\"text\" class='collapseXML' value=\""+b.getTag()+"\"></input></span>";
                if (b.hasParameters()) {
                    String[] params = b.getparameters();
                    String parameters = new String();
                    //out.print("<script type='text/javascript'>var parameters='';");
                    switch (params.length) {
                        case 5:     parameters = "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p5' value='" + params[4] + "'/>\n</span>" + parameters;
                        case 4:     parameters = "<span class='clear-left lastRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p4' value='" + params[3] + "'/>"+parameters;
                        case 3:     parameters = "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' class='collapseXML' type='text' name='b" + ctr + "p3' value='" + params[2] + "'/><span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'>\n</span>\n</span>"+parameters;
                        case 2:     parameters = "<span class='clear-left secondRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p2' value='" + params[1] + "'/>"+parameters;
                        case 1:     parameters = "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b" + ctr + "p1' value='" + params[0] + "'/><span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'>\n</span>\n</span>"+parameters;
                        default:    break;
                    }
                    switch (params.length) {
                        case 1:     parameters += "<span class='clear-left secondRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p2' value=''/>";
                        case 2:     parameters += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p3' value=''/><span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'>\n</span>\n</span>";
                        case 3:     parameters += "<span class='clear-left lastRow collapseXML'>\n<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p4' value=''/>";
                        case 4:     parameters += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder='parameter' type='text' name='b"+ ctr + "p5' value=''/>\n</span>";
                        default:     break;
                    }
                buttonHTML += parameters;
                } 
                else{
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p1\" />";
                    buttonHTML += "<span class=\"right ui-icon moreParameters ui-icon-plus\" title=\"Add more parameters to this button\"></span></span>"; //close .firstRow%>
                    buttonHTML += "<span class='clear-left secondRow collapseXML'>";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p2\" />";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p3\" />";
                    buttonHTML += "<span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'></span>";
                    buttonHTML += "</span>";
                    buttonHTML += "<span class='clear-left lastRow collapseXML'>";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+">p4\" />";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p5\" />";
                    buttonHTML += "</span>";
                }
                buttonHTML += "</div></li>";
                toret += buttonHTML;
                ctr++;           
         }
         if (ctr == 0) {
//            TagButton b = new TagButton(projectID, 1, "temp", true, "button description");
//            b = new TagButton(projectID, 2, "temp", true, "button description");
//            b = new TagButton(projectID, 3, "temp", true, "button description");
//            b = new TagButton(projectID, 4, "temp", true, "button description");
         }
         
         return toret;
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * get all of the buttons for this Project
    */
   public static String getAllProjectButtons(int projectID) throws SQLException {
       //System.out.println("Get all project buttons");
      Date date = new Date(currentTimeMillis());
      StackTraceElement[] t = currentThread().getStackTrace();
      DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd H:M:S");
      formatter.format(date);
      String stackTrace = "";
        for (StackTraceElement t1 : t) {
            stackTrace += t1.toString() + "\n";
        }
      //LOG.log(Level.SEVERE, "{0} Running tagButton.getAllProjectButtons\n{1}", new Object[]{formatter.format(date), stackTrace});
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String toret = "";
         String query = "select distinct(position) from projectbuttons where project=? order by position";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, projectID);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         JSONArray ja = new JSONArray();
         String[] blank_case = new String[5];
         blank_case[0] = null;
         blank_case[1] = null;
         blank_case[2] = null;
         blank_case[3] = null;
         blank_case[4] = null;
         int count=0;
         while (rs.next()) {
             count++;
            int position = rs.getInt("position");
            try {
               TagButton b = new TagButton(projectID, position, true);
                ctr++;
                JSONObject jo = new JSONObject();
                if(null != b.getTag() && !"".equals(b.getTag())){
                    //System.out.println("New get all project buttons.  What is tag "+b.getTag());
                    jo.element("tag", b.getTag()); //b.getButton()
                }
                else{
                    jo.element("tag", ""); //b.getButton()
                }
                if(null != b.getparameters() && b.getparameters().length != 0){
                    jo.element("parameters", b.getparameters());
                }
                else{
                    jo.element("parameters",blank_case);
                }
                if(null != b.getDescription()&& !"".equals(b.getDescription())){
                    jo.element("description", b.getDescription());
                }
                else{
                    jo.element("description","");
                }
                //System.out.println("New get project buttons.  What is full tag?   "+b.getFullTag());
                if(null != b.getFullTag()&& !"".equals(b.getFullTag())){
                    jo.element("fullTag", b.getFullTag());
                }
                else{
                    jo.element("fullTag", "");
                }
                //jo.element("color", b.getXMLColor());
                ja.add(jo);
            } catch (NullPointerException e) {
            }
            ctr++;
         }
         if (ctr == 0) {
//            TagButton b = new TagButton(projectID, 1, "temp", true, "button description");
//            b = new TagButton(projectID, 2, "temp", true, "button description");
//            b = new TagButton(projectID, 3, "temp", true, "button description");
//            b = new TagButton(projectID, 4, "temp", true, "button description");
         }
         return ja.toString();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * empty a Project of tags to load new tags
    */
   public void removeButtonsByProject(int projectID) throws SQLException {
      String query = "DELETE FROM projectbuttons WHERE project=?";
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         ps.setInt(1, projectID);
         ps.execute();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }

   /**
    * Change the tag description
    *
    * @param desc new description. Should be short because this is the button label.
    * @throws SQLException
    */
   public void updateDescription(String desc) throws SQLException {
      String query = "update buttons set description=? where uid=? and position=?";
      if (this.projectID > 0) {
         query = "update projectbuttons set description=? where project=? and position=?";
      }
      Connection j = null;
      PreparedStatement ps = null;
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         if (this.projectID > 0) {
            ps.setInt(2, projectID);
         } else {
            ps.setInt(2, uid);
         }
         ps.setInt(3, position);
         ps.setString(1, desc);
         ps.execute();
      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
      }
   }

   /**
    * Deletes the tag button
    *
    * @throws SQLException
    */
   public void deleteTag() throws SQLException {
      String query = "delete from buttons where uid=? and position=?";
      if (this.projectID > 0) {
         query = "delete from projectbuttons where project=? and position=?";
      }
      Connection j = null;
      PreparedStatement ps = null;
      PreparedStatement update = null;
      String updateQuery = "update buttons set position=? where uid=? and position=?";
      if (this.projectID > 0) {
         updateQuery = "update projectbuttons set position=? where project=? and position=?";
      }
      try {
         j = getConnection();
         ps = j.prepareStatement(query);
         if (this.projectID > 0) {
            ps.setInt(1, projectID);
         } else {
            ps.setInt(1, uid);
         }
         ps.setInt(2, position);
         ps.execute();
         //now reorder them
         update = j.prepareStatement(updateQuery);

         TagButton t;
         while (true) {
            if (this.projectID > 0) {
               t = new TagButton(projectID, position + 1, true);
            } else {
               t = new TagButton(uid, position + 1);
            }
            if (t.uid == 0 && t.projectID <= 0) {
               break;
            }
            update.setInt(1, position);
            if (projectID > 0) {
               update.setInt(2, projectID);
            } else {
               update.setInt(2, uid);
            }
            update.setInt(3, position + 1);
            update.execute();
            position++;
         }

      } finally {
            closeDBConnection(j);
            closePreparedStatement(ps);
            closePreparedStatement(update);
      }


   }

   /**
    * Update the text of the xml tag
    *
    * @param newTag new tag text without brackets
    * @throws SQLException
    */
   public void setTag(String newTag) throws SQLException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String query = "update buttons set text=? where uid=? and position=?";
         if (projectID > 0) {
            query = "update projectbuttons set text=? where project=? and position=?";
         }
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setString(1, newTag);
         if (projectID > 0) {
            stmt.setInt(2, projectID);
         } else {
            stmt.setInt(2, uid);
         }
         stmt.setInt(3, position);
         stmt.execute();
         tag = newTag;
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }

   /**
    * run the schema tag extraction xsl on the schema the stream points to
    */
   public static String xslRunner(StreamSource xml) throws SaxonApiException {
      Processor proc = new Processor(false);
      XsltCompiler comp = proc.newXsltCompiler();
      XsltExecutable exp = comp.compile(new StreamSource(new File(getRbTok("XSLTLOCATION") + "schema2.xsl")));
      XdmNode source = proc.newDocumentBuilder().build(xml);
      Serializer out = new Serializer();
      StringWriter w = new StringWriter();
      out.setOutputWriter(w);
      XsltTransformer trans = exp.load();
      trans.setInitialContextNode(source);
      trans.setDestination(out);
      trans.transform();
      return w.toString();
   }

   /**
    * Returns a list of tags defined in the Project schema
    */
   public static String[][] getTagsFromSchema(Project p, List<parameterWithValueList> v) throws SQLException, MalformedURLException, IOException {
      try {

         URL schemaurl = new URL(p.getSchemaURL());
         StreamSource schemaStream = new StreamSource(schemaurl.openStream());
         String tagList = xslRunner(schemaStream);
         tagList = tagList.replaceAll("<.*?>", "").replaceAll(" ", "");
         String[] tagsAndParamData = tagList.split("\n");
         String[][] toret = new String[tagsAndParamData.length][];
         for (int i = 0; i < tagsAndParamData.length; i++) {

            String[] tmp = tagsAndParamData[i].split("/");
            toret[i] = tmp;

            for (int j = 0; j < tmp.length; j++) {
               //if there are any default parameter lists, remove that list and build the needed parameterWithValueList objects and put them into v
               if (tmp[j].contains("{")) {
                  String[] tmp2 = tmp[j].split("\\{");
                  tmp[j] = tmp2[0];
                  //tmp2[1] contaions the value list and a terminating }
                  String valueList = tmp2[1].replace("\\}", "");
                  //now split the value list, they are delimited by a single space
                  String[] values = valueList.split(",");
                  parameterWithValueList param = new parameterWithValueList(tmp[j]);
                  param.values.addAll(asList(values));
                  v.add(param);
               }
            }
         }
         return toret;
      } catch (SaxonApiException ex) {
            getLogger(TagButton.class.getName()).log(SEVERE, null, ex);
      }
      return null;
   }

   /**
    * Returns user tags
    */
   public static String[] getTagsFromUser(int uid) throws SQLException, IOException {
      Connection j = null;
      PreparedStatement stmt = null;
      try {
         String toret = "";
         String query = "select * from buttons where uid=?";
         j = getConnection();
         stmt = j.prepareStatement(query);
         stmt.setInt(1, uid);
         ResultSet rs = stmt.executeQuery();
         int ctr = 0;
         while (rs.next()) {
            int position = rs.getInt("position");
            TagButton b = new TagButton(uid, position);
            toret += b.getTag() + "\n";
            ctr++;
         }
         return toret.split("\n");
      } finally {
            closeDBConnection(j);
            closePreparedStatement(stmt);
      }
   }
   
   private static final Logger LOG = getLogger(TagButton.class.getName());
}
