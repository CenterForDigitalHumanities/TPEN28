/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import edu.slu.util.ServletUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.Tool;
import utils.UserTool;

/**
 * Remove user tools from user by user id and tool name. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class RemoveUserToolServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Tool.removeTool(Tool.tools.valueOf(request.getParameter("toolName")), Integer.parseInt(request.getParameter("uid")));
            response.getWriter().print("1");
        } catch (SQLException ex) {
            Logger.getLogger(AddProjectToolServlet.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp); 
    }
    
}
