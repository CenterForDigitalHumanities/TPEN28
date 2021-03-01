/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import textdisplay.TagButton;

/**
 *
 * @author bhaberbe
 */
public class updateXMLEntries extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, SQLException {
        response.setContentType("application/json;xmlet=UTF-8");
        BufferedReader bodyReader = request.getReader();
        StringBuilder bodyString = new StringBuilder();
        JSONObject requestJSON = new JSONObject();
        PrintWriter out = response.getWriter();
        String requestString;
        String line;
        boolean moveOn = false;
        while ((line = bodyReader.readLine()) != null)
        {
          bodyString.append(line);
        }
        bodyReader.close();
        requestString = bodyString.toString();
        try{ 
            //JSONObject test
            requestJSON = JSONObject.fromObject(requestString);
            moveOn = true;
        }
        catch(Exception ex){
            response.setStatus(500);
            out.print(ex);
        }     
        if(moveOn){
            int projectID = requestJSON.getInt("projectID");
            TagButton.removeAllProjectXML(projectID);
            for (int i=0; i<requestJSON.getJSONArray("xml").size(); i++){
                String tagText = requestJSON.getJSONArray("xml").getJSONObject(i).getString("tag");
                String description = requestJSON.getJSONArray("xml").getJSONObject(i).getString("description");
                JSONArray jparams = requestJSON.getJSONArray("xml").getJSONObject(i).getJSONArray("params");
                String[] params = new String[jparams.size()];
                for(int j=0; j<jparams.size(); j++) {
                    params[j]=jparams.optString(j);
                }
                new TagButton(projectID, (i+1), tagText, params, description, true);
            }
            response.setStatus(200);
            out.print("Special Characters Updated!");
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(updateXMLEntries.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
