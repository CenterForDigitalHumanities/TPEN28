/*
 * Copyright 2014- Saint Louis University. Licensed under the
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
 */

package edu.slu.tpen.servlet;

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
import textdisplay.Manuscript;

/**
 * Get manuscript by city and repository.
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class GetManuscriptsByCityAndRepository extends HttpServlet {

    /**
     * Get manuscript by city and repository or by either of them. 
     * @param city
     * @param repository
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject jo = new JSONObject();
        if (null != request.getParameter("city")) {
            if (null != request.getParameter("repository")) {
                try {
                    String city = request.getParameter("city");
                    String repo = request.getParameter("repository");
                    Manuscript[] mss = Manuscript.getManuscriptsByCityAndRepository(city, repo);
                    jo.element("ls_manu", mss);
                } catch (SQLException ex) {
                    Logger.getLogger(GetManuscriptsByCityAndRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                try {
                    String city = request.getParameter("city");
                    Manuscript[] mss = Manuscript.getManuscriptsByCity(city);
                    jo.element("ls_manu", mss);
                } catch (SQLException ex) {
                    Logger.getLogger(GetManuscriptsByCityAndRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            if (request.getParameter("repository") != null) {
                try {
                    String repo = request.getParameter("repository");
                    Manuscript[] mss = Manuscript.getManuscriptsByRepository(repo);
                    jo.element("ls_manu", mss);
                } catch (SQLException ex) {
                    Logger.getLogger(GetManuscriptsByCityAndRepository.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                jo.element("error", "no city or repository specified");
            }
        }
        PrintWriter out = response.getWriter();
        out.print(jo);
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }
    
}
