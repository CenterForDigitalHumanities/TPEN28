/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.slu.tpen.servlet;

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
        //System.out.println("Upgarde serverlet");
        int ucountdown = 0;
        if (session.getAttribute("UID") != null) {
            thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
        }
        else{
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        if (null != thisUser && thisUser.isAdmin()){
            //System.out.println("U1");
            String uDate = request.getParameter("upgradeDate");
            //String uTime = (String)request.getAttribute("upgradeTime");
            String uMessage = request.getParameter("upgradeMessage");
            String countdown = request.getParameter("countdown");
            //System.out.println("U2");
            if(request.getParameter("cancelUpgrade").equals("true")){
                //System.out.println("U3");
                upgrader = new UpgradeManager();
               // System.out.println("U4");
                upgrader.deactivateUpgrade();
                //System.out.println("U5");
                out.println("Upgrade settings deactivated");
            }
            else if(request.getParameter("getSettings").equals("get")){
                upgrader = new UpgradeManager();
                //System.out.println("U6");
                JSONObject jo_manager = new JSONObject();
                jo_manager.element("active", upgrader.checkActive());
                //System.out.println("U7");
                jo_manager.element("upgradeMessage", upgrader.getUpgradeMessage());
               // System.out.println("U8");
                jo_manager.element("upgradeDate", upgrader.getUpgradeDate());
                //System.out.println("U9");
                jo_manager.element("countdown", upgrader.checkCountdown());
               // System.out.println("U10");
                out.print(jo_manager);
            }
            else{
               // System.out.println("U11");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
               // System.out.println("Date to parse...");
               // System.out.println(uDate);
                try{
                    Date parsedDate = sdf.parse(uDate);
                  //  System.out.println("New date is ");
                    //Timestamp upgradeTime = new Timestamp(uTime);
                 //   System.out.println("countdown flag is "+countdown);
                 //   System.out.println("U13");
                    if(countdown.equals("1")){
                        ucountdown = 1;
                    }
                    upgrader = new UpgradeManager(uDate, uMessage, ucountdown, 1);
                  //  System.out.println("U14");
                    out.println("Upgrade settings applied");
                }
                catch (Exception e){
                    out.println("date parse error");
                    out.println(e);
                }
               
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
