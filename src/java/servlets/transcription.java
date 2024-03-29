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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import static org.owasp.esapi.ESAPI.encoder;
import org.owasp.esapi.errors.EncodingException;
import textdisplay.Folio;
import static textdisplay.Folio.getFolioFromCanvas;
import static textdisplay.Folio.getImageName;
import static textdisplay.Folio.getImageNameFolio;
import textdisplay.Project;
import static textdisplay.Project.getPublicProjects;
import user.User;

/**
 *
 * Handles transcription export in pdf, rtf, and plaintext/xml formats
 */
public class transcription extends HttpServlet {
   
    public static void main(String [] args)
    {
        for(int i=0;i<10000;i++)
        {
        try {
            String text="dicat si deus est qui iustificat quis est qui condempnet something else";
            text=encoder().encodeForURL(text);
            String url="https://t-pen.org/TPEN/updateLine?projectID=56&line=100002000&text="+text+"&comment=test";
            try {
                URL testURL=new URL(url);
                try {
                    URLConnection t=testURL.openConnection();
                    
                    BufferedReader b = new BufferedReader(new InputStreamReader(t.getInputStream()) );
                    while(!b.ready())
                    {
                        //sleep
                        try {
                                out.print("sleeping\n");
                                        sleep(500);//sleep for 1000 ms
                                        if(!b.ready())
                                        {
                                            out.print("retrying\n");
                                            t=testURL.openConnection();
                                             b = new BufferedReader(new InputStreamReader(t.getInputStream()) );
                                        }
                                    } catch (InterruptedException ie) {
    //If this thread was intrrupted by nother thread
                                    }
                    }
                    String buff="";
                    while(b.ready())
                        buff+=b.readLine();
                    text=encoder().decodeFromURL(text);
                    if(buff.compareTo(text)==0) 
                    {
                        //System.out.print(""+i+"\n"); 
                    }                    
else
                        out.print("bad:\n"+buff+"\n"+text+"\n");
                } catch (IOException ex) {
                        getLogger(transcription.class.getName()).log(SEVERE, null, ex);
                }
                
            } catch (MalformedURLException ex) {
                    getLogger(transcription.class.getName()).log(SEVERE, null, ex);
            }
        } catch (EncodingException ex) {
                getLogger(transcription.class.getName()).log(SEVERE, null, ex);
        }
        }
    }
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("application/N3;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            int uid=0;
            HttpSession session=request.getSession();
            try{
            uid=parseInt(session.getAttribute("UID").toString());
            }
            catch(Exception e)
            {
                uid=-1;
            }
            if(request.getParameter("p")!=null)
            {
                int page=parseInt(request.getParameter("p"));
                int projectID=0;
                
                if(request.getParameter("projectID")!=null)
                {
                    projectID=parseInt(request.getParameter("projectID"));
                }
               
                if(projectID>0)
                {
                    try
                        {
                        Project p = new Project(projectID);
                        out.print(p.getOAC(page));
                        } catch (SQLException ex)
                        {
                        getLogger(transcription.class.getName()).log(SEVERE, null, ex);
                        }
                }
                else{
                if(uid>0)
                {
                    Folio f=new Folio(page);
                    try
                        {
                        out.print(f.getOAC(uid));
                        } catch (SQLException ex)
                        {
                            getLogger(transcription.class.getName()).log(SEVERE, null, ex);
                        }
                }
                }
            }
            if(request.getParameter("image")!=null)
            {
                String pageName=request.getParameter("image").replace("_46.jp2", "");
        String imageName=getImageName(pageName)+".jpg";
        textdisplay.Folio f=getImageNameFolio(imageName);
        String toret=f.getOAC(uid);
        if(toret.length()>0)
        {
            out.print(toret);
            return;
        }
        else
        {
            User thisUser=new User(uid);
            Project [] userProjects=thisUser.getUserProjects();
                    for (Project userProject : userProjects) {
                        toret = userProject.getOAC(f.getFolioNumber());
                        if(toret.length()>0)
                        {
                            out.print(toret);
                            return;
                        }
                    }

        }
            }
            if(request.getParameter("canvas")!=null)
            {
                String canvas=request.getParameter("canvas");

        textdisplay.Folio f=new textdisplay.Folio(getFolioFromCanvas(canvas));
        Project [] publicProjects=getPublicProjects();
                for (Project publicProject : publicProjects) {
                    String toret = publicProject.getOAC(f.getFolioNumber());
                    if(toret!=null && toret.length()>1)
                    {
                        out.print(toret);
                        return;
                    }   }
            }
        } catch (SQLException ex)
            {
            getLogger(transcription.class.getName()).log(SEVERE, null, ex);
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
