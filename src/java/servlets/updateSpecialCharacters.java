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
import net.sf.json.JSONObject;
import textdisplay.Hotkey;

/**
 *
 * @author bhaberbe
 */
public class updateSpecialCharacters extends HttpServlet {

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
        response.setContentType("application/json;charset=UTF-8");
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
            Hotkey[] hks = new Hotkey[requestJSON.getJSONArray("chars").size()];
            Hotkey.removeAllProjectHotkeys(projectID);
            for (int i=0; i<requestJSON.getJSONArray("chars").size(); i++){
                int characterCode = requestJSON.getJSONArray("chars").getInt(i);
                new Hotkey(characterCode, projectID, (i+1), true);
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
            Logger.getLogger(updateSpecialCharacters.class.getName()).log(Level.SEVERE, null, ex);
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
