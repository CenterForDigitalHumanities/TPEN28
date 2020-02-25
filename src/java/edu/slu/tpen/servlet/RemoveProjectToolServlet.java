/*
 * Copyright 2015- Saint Louis University. Licensed under the
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

import static edu.slu.util.ServletUtils.getDBConnection;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.sql.Connection;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static utils.UserTool.removeUserTool;

/**
 * Remove project relatd tools by project id. 
 * This is a transformation of tpen function to web service. It's using tpen MySQL database. 
 * @author hanyan
 */
public class RemoveProjectToolServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Connection conn = getDBConnection();
            removeUserTool(request.getParameter("url"), parseInt(request.getParameter("projectID")));
            response.getWriter().print("1");
        } catch (SQLException ex) {
            getLogger(AddProjectToolServlet.class.getName()).log(SEVERE, null, ex);
        } 
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp); 
    }
}
