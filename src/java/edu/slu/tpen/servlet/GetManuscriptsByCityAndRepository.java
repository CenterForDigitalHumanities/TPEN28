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
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import textdisplay.Manuscript;
import static textdisplay.Manuscript.getManuscriptsByCity;
import static textdisplay.Manuscript.getManuscriptsByCityAndRepository;
import static textdisplay.Manuscript.getManuscriptsByRepository;

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
        response.setContentType("application/json");
        JSONObject jo = new JSONObject();
        
        String city = sanitize(request.getParameter("city"));
        String repo = sanitize(request.getParameter("repository"));
        
        try {
            if (city != null && repo != null) {
                Manuscript[] mss = getManuscriptsByCityAndRepository(city, repo);
                jo.element("ls_manu", mss);
            } else if (city != null) {
                Manuscript[] mss = getManuscriptsByCity(city);
                jo.element("ls_manu", mss);
            } else if (repo != null) {
                Manuscript[] mss = getManuscriptsByRepository(repo);
                jo.element("ls_manu", mss);
            } else {
                jo.element("error", "no city or repository specified");
            }
        } catch (SQLException ex) {
            getLogger(GetManuscriptsByCityAndRepository.class.getName()).log(SEVERE, null, ex);
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.print(jo);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
    
    private String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>\"'%;()&+]", "");
    }
}
