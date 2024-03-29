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

import static edu.slu.tpen.servlet.Constant.ANNOTATION_SERVER_ADDR;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static java.net.URLEncoder.encode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import java.util.Stack;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import static net.sf.json.JSONObject.fromObject;
import static org.owasp.esapi.ESAPI.encoder;
import static textdisplay.DatabaseWrapper.closeDBConnection;
import static textdisplay.DatabaseWrapper.closePreparedStatement;
import static textdisplay.DatabaseWrapper.getConnection;

/**
 *
 * @author obi1one
 */
public class Annotation
{
   private String text;
   private int x;
   private int y;
   private int h;
   private int w;
   private int id;
   private int folio;
   private int projectID;


   public int getFolio() {
        return folio;
    }

   public int getH() {
        return h;
    }

   
   public int getId() {
        return id;
    }

    public int getProjectID() {
        return projectID;
    }

    public String getText() {
        return encoder().encodeForHTML(text);
    }

    public int getW() {
        return w;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
   
    /**
     * Create a new annotation
     * @param folio the folio the annotation is on
     * @param projectID the projectID for the project the annotation is part of
     * @param text text of the annotation
     * @param x
     * @param y
     * @param h
     * @param w
     * @throws SQLException
     */
    public Annotation(int folio, int projectID,String text, int x, int y, int h, int w) throws SQLException
   {
       String query="insert into annotation(folio,projectID,text,x,y,h,w) values(?,?,?,?,?,?,?)";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=getConnection();
           ps=j.prepareStatement(query, RETURN_GENERATED_KEYS);
           ps.setInt(1, folio);
           ps.setInt(2, projectID);
           ps.setString(3, text);
           ps.setInt(4, x);
           ps.setInt(5, y);
           ps.setInt(6, h);
           ps.setInt(7, w);
           ps.execute();
           ResultSet keys=ps.getGeneratedKeys();
           keys.next();
           this.id=keys.getInt(1);
           this.x=x;
           this.y=y;
           this.h=h;
           this.w=w;
           this.text=text;
           this.projectID=projectID;
           this.folio=folio;
       }
       finally{
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
   }
   /**
    * Retrieve an existing annotation by unique id.
    * @param id unique id for the annotation
    * @throws SQLException
    */
   public Annotation(int id) throws SQLException
   {
       String query="select * from annotation where id=?";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, id);
           ResultSet rs=ps.executeQuery();
           if(rs.next())
           {
               this.x=rs.getInt("x");
               this.y=rs.getInt("y");
               this.h=rs.getInt("h");
               this.w=rs.getInt("w");
               this.folio=rs.getInt("folio");
               this.projectID=rs.getInt("projectID");
               this.id=id;
               this.text=rs.getString("text");
           }
       }
       finally
       {
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
   }
   /**
    * Update the text of an annotation
    * @param text new text
    * @throws SQLException
    */
   public void updateAnnotationContent(String text) throws SQLException
   {
       createArchiveCopy();
          String query="update annotation set text=? where id=?";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=getConnection();
           ps=j.prepareStatement(query);
           ps.setString(1, text);
           ps.setInt(2, this.id);
           ps.execute();
           this.text=text;
       }
       finally{
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
   }
   /**
    * Update the coordinates and dimensions of the annotation
    * @param x
    * @param y
    * @param h
    * @param w
    * @throws SQLException
    */
   public void updateAnnoationPosition(int x, int y, int h, int w) throws SQLException
   {
       createArchiveCopy();
              String query="update annotation set x=?,y=?,h=?,w=? where id=?";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, x);
           ps.setInt(2, y);
           ps.setInt(3, h);
           ps.setInt(4, w);
           ps.setInt(5, this.id);
           ps.execute();
           this.x=x;
           this.y=y;
           this.h=h;
           this.w=w;
       }
       finally{
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
   }
   /**
    * Update the folio the annotation is on
    * @param folio
    * @throws SQLException
    */
   public void updateAnnotationFolio(int folio) throws SQLException
   {
       createArchiveCopy();
       String query="update annotation set folio=? where id=?";
       Connection j=null; 
PreparedStatement ps=null;
       
       try{ 
           j=getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, folio);
           ps.setInt(2, this.id);
           ps.execute();
           this.folio=folio;
       }
       finally{
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
   }
   /**
    * Delete the annotation entirely
    * @throws SQLException
    */
   public void delete() throws SQLException
   {
       String query="delete from annotation where id=?";
       PreparedStatement ps=null;
       Connection j=null;
       try{
           j=getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1, this.id);
           ps.execute();
       }
       finally
       {
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
   }
   /**Duplicate this annotation, creating something with the same x,y,h,w, folio, projectID and text, but a new id.
    * @return
    * @throws SQLException
    */
   public Annotation duplicate() throws SQLException
   {
       return new Annotation(this.folio,this.projectID,this.text,this.x,this.y,this.h,this.w);
   }
   private void createArchiveCopy() throws SQLException
   {
       String query="insert into archivedannotation(folio,projectID,text,x,y,h,w,id) (select folio,projectID,text,x,y,h,w,id from annotation where id=?)";
       Connection j=null;
PreparedStatement ps=null;
       try{
           j=getConnection();
           ps=j.prepareStatement(query);
           ps.setInt(1,this.id);
           ps.execute();
       }
       finally
       {
            closeDBConnection(j);
            closePreparedStatement(ps);
       }
       
   }
   /**
    * Get all annotations on a particular folio that are part of a particular project
    * @param projectID
    * @param folio
    * @return
    * @throws SQLException
    */
   public static Annotation[] getAnnotationSet(int projectID,int folio) throws SQLException
{
    Annotation [] toret=null;
    Stack <Annotation> tmp =new Stack();
    String query="select id from annotation where projectID=? and folio=?";
    Connection j=null;
PreparedStatement ps=null;
    try{
        j=  getConnection();
        ps=j.prepareStatement(query);
        ps.setInt(1, projectID);
        ps.setInt(2, folio);
        
        ResultSet rs=ps.executeQuery();
        while(rs.next())
        {
            Annotation a=new Annotation(rs.getInt("id"));
            System.out.println(a);
            tmp.push(a);
        }
        toret=new Annotation[tmp.size()];
        for(int i=0;i<toret.length;i++)
            toret[i]=tmp.pop();
    }
    finally{
            closeDBConnection(j);
            closePreparedStatement(ps);
    }
    return toret;
}
   
   /*
   * Used in the JSONLDExporter, specifically in a situation to help support old annotations and transfer them over
     to the annotation store.  This essentially functions like a servlet.  
     
     @param annotationListObject: A well formed JSONObject that is an annotationList to be saved to the store.
     @return: The @id of the newly saved anntoationList
   */
   public static String saveNewAnnotationList(JSONObject annotationListObject) throws MalformedURLException, IOException{
        String newListID = "";
        URL postUrl = new URL(ANNOTATION_SERVER_ADDR + "/anno/saveNewAnnotation.action");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.connect();
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.writeBytes("content=" + encode(annotationListObject.toString(), "utf-8"));
            out.flush();
            // flush and close
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        String line="";
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null){  
            sb.append(line);
        } 
        reader.close();
        connection.disconnect();
        JSONObject server_response = fromObject(sb.toString());
        try{
           newListID = server_response.getString("@id");
        }
        catch(JSONException e){
           newListID = "/id/gather/error";
        }
        return newListID;
   }
}
