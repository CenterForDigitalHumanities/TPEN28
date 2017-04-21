/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

import com.mongodb.util.JSON;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONObject;
import user.UpgradeManager;

/**
 *
 * @author bhaberbe
 */
public class UpgradeManagement extends HttpServlet {

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
            throws ServletException, IOException, SQLException, ParseException {
        response.setContentType("text/html;charset=UTF-8");
        user.User thisUser = null;
        HttpSession session = request.getSession();
        UpgradeManager upgrader = null;
        PrintWriter out = response.getWriter();
        if (session.getAttribute("UID") != null) {
            thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
        }
        else{
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        if (null != thisUser && thisUser.isAdmin()){
            String uDate = request.getParameter("upgradeDate");
            //String uTime = (String)request.getAttribute("upgradeTime");
            String uMessage = (String)request.getParameter("upgradeMessage");
            String countdown = (String)request.getParameter("countdown");
            if(request.getParameter("cancelUpgrade").equals("true")){
                upgrader = new UpgradeManager();
                upgrader.deactivateUpgrade();
                out.println("Upgrade settings deactivated");
            }
            else if(request.getParameter("getSettings").equals("get")){
                upgrader = new UpgradeManager();
                JSONObject jo_manager = new JSONObject();
                jo_manager.element("active", upgrader.checkActive());
                jo_manager.element("upgradeMessage", upgrader.getUpgradeMessage());
                jo_manager.element("upgradeDate", upgrader.getUpgradeDate());
                jo_manager.element("countdown", upgrader.checkCountdown());
                out.print(jo_manager);
            }
            else{
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH.mm.ss");
                Date parsedDate = sdf.parse(uDate);
                Timestamp upgradeDate = new Timestamp(parsedDate.getTime());
                //Timestamp upgradeTime = new Timestamp(uTime);
                boolean ucountdown = false;
                if(countdown.equals("true")) ucountdown = true;
                upgrader = new UpgradeManager(upgradeDate, uMessage, ucountdown, true);
                out.println("Upgrade settings applied");
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(UpgradeManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(UpgradeManagement.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UpgradeManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(UpgradeManagement.class.getName()).log(Level.SEVERE, null, ex);
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
