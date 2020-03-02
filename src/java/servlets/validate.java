/*
 * @author Jon Deering
Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License.

You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
and limitations under the License.
 */

package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xml.sax.SAXException;
import textdisplay.Manuscript;
import static textdisplay.Manuscript.getFullDocument;
import textdisplay.Project;
import static textdisplay.TagFilter.noteStyles.remove;
import utils.XmlSchema;
import utils.XmlSchema.types;
import static utils.XmlSchema.types.RELAXNG;
import static utils.XmlSchema.types.RELAXNG_COMPACT;

/**
 *
 * Run validation of the Project against the associated xml schema
 */
public class validate extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if(request.getParameter("projectID")==null)
            {
                out.print("Validations skipped: No project specified");
                    return;
            }
            int projectID=parseInt(request.getParameter("projectID"));
            try {
                Project p = new Project(projectID);
                String schemaURL=p.getSchemaURL();
                if(schemaURL.length()<5)
                {
                    out.print("Validations skipped: No schema specified");
                    return;
                }
                types schemaType=RELAXNG;
                //if the schema file ending is rnc, assume its compact, otherwise go with non compact
                if(p.getSchemaURL().endsWith("rnc"))
                    schemaType=RELAXNG_COMPACT;
                XmlSchema s=new XmlSchema(p.getSchemaURL(),schemaType);
           
            Manuscript ms=new Manuscript(5);
            String content=getFullDocument(p, remove,false,false,false);
            if(!(s.validate(content)))
                out.print("validation failed: "+s.getMessages()+"\n");
            else
                out.print("valid");
            } catch (SAXException | SQLException ex) {
                getLogger(validate.class.getName()).log(SEVERE, null, ex);
            }

        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
