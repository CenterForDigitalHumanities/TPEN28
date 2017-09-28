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
import textdisplay.ProjectPermissions;
import user.Group.roles;

/**
 * Change user permission in a project (group). This is a transformation of tpen function to web service. 
 * It's using tpen MySQL database. 
 * @author hanyan
 */
public class ChangeUserPermissionServlet extends HttpServlet {
    private PrintWriter out;
    private JSONObject jo;

    @Override
    /**
     * The parameter format: projectid=12&uid=123&act=changerole&role=leader
     * @param projectid
     * @param uid
     * @param act
     * @param role
     * @param allowOACRead
     * @param allowOACWrite
     * @param allowExport
     * @param allowPulicCopy
     * @param allowPublicModify
     * @param allowPublicModifyAnnotation
     * @param allowPublicModifyButtons
     * @param allowPublicModifyLineParsing
     * @param allowPublicModifyMetadata
     * @param allowPublicModifyNotess
     * @param allowPublicReadTranscription
     * @return 10000: projectid is null
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int result = 0;
        if (request.getParameter("projectID") != null && null != request.getSession().getAttribute("UID")) {
            //System.out.println("UID !!!!!!!!!!!!!!!!!!!!");
            //System.out.println(request.getSession().getAttribute("UID"));
            int currentUserId = Integer.parseInt(request.getSession().getAttribute("UID") + "");
            int projectId = new Integer(request.getParameter("projectID")).intValue();
            if(null != request.getParameter("uid")){
                //change user permission for other user
                int uid = new Integer(request.getParameter("uid")).intValue();
                if("add".equals(request.getParameter("act"))){
                    
                    result = addUserToProject(projectId, uid);
                    
                }else if("rm".equals(request.getParameter("act"))){
                    
                    result = removeUserFromProject(projectId, uid, currentUserId);
                    
                }else if("changerole".equals(request.getParameter("act"))){
                    if("leader".equals(request.getParameter("role"))){
                    
                        result = changeUserRole(projectId, uid, currentUserId, roles.Leader);

                    }else if("editor".equals(request.getParameter("role"))){

                        result = changeUserRole(projectId, uid, currentUserId, roles.Editor);

                    }else if("contributor".equals(request.getParameter("role"))){

                        result = changeUserRole(projectId, uid, currentUserId, roles.Contributor);

                    }else if("suspended".equals(request.getParameter("role"))){

                        result = changeUserRole(projectId, uid, currentUserId, roles.Suspended);

                    }else if("none".equals(request.getParameter("role"))){

                        result = changeUserRole(projectId, uid, currentUserId, roles.None);

                    }else{
                        //if non of above, send back not accepttable. The role name is not acceptable. 
                        result = response.SC_NOT_ACCEPTABLE;
                    }
                }
            }
            if(null != request.getParameter("act") && "cpm".equals(request.getParameter("act"))){
                //change current user (in session) permission
                try {
                    //change permission, cpm=change permission, p=permission
                    ProjectPermissions pp = new ProjectPermissions(projectId);
                    if(null != request.getParameter("allowOACRead") 
                            && (request.getParameter("allowOACRead").equals("true") || request.getParameter("allowOACRead").equals("false"))){
                        pp.setAllow_OAC_read(new Boolean(request.getParameter("allowOACRead")));
                        result = 1;
                    }else if(null != request.getParameter("allowOACWrite") 
                            && (request.getParameter("allowOACWrite").equals("true") || request.getParameter("allowOACWrite").equals("false"))){
                        pp.setAllow_OAC_write(new Boolean(request.getParameter("allowOACWrite")));
                        result = 1;
                    }else if(null != request.getParameter("allowExport") 
                            && (request.getParameter("allowExport").equals("true") || request.getParameter("allowExport").equals("false"))){
                        pp.setAllow_export(new Boolean(request.getParameter("allowExport")));
                        result = 1;
                    }else if(null != request.getParameter("allowPulicCopy") 
                            && (request.getParameter("allowPulicCopy").equals("true") || request.getParameter("allowPulicCopy").equals("false"))){
                        pp.setAllow_public_copy(new Boolean(request.getParameter("allowPulicCopy")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicModify") 
                            && (request.getParameter("allowPublicModify").equals("true") || request.getParameter("allowPublicModify").equals("false"))){
                        pp.setAllow_public_modify(new Boolean(request.getParameter("allowPublicModify")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicModifyAnnotation") 
                            && (request.getParameter("allowPublicModifyAnnotation").equals("true") || request.getParameter("allowPublicModifyAnnotation").equals("false"))){
                        pp.setAllow_public_modify_annotation(new Boolean(request.getParameter("allowPublicModifyAnnotation")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicModifyButtons") 
                            && (request.getParameter("allowPublicModifyButtons").equals("true") || request.getParameter("allowPublicModifyButtons").equals("false"))){ 
                        pp.setAllow_public_modify_buttons(new Boolean(request.getParameter("allowPublicModifyButtons")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicModifyLineParsing") 
                            && (request.getParameter("allowPublicModifyLineParsing").equals("true") || request.getParameter("allowPublicModifyLineParsing").equals("false"))){
                        pp.setAllow_public_modify_line_parsing(new Boolean(request.getParameter("allowPublicModifyLineParsing")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicModifyMetadata") 
                            && (request.getParameter("allowPublicModifyMetadata").equals("true") || request.getParameter("allowPublicModifyMetadata").equals("false"))){
                        pp.setAllow_public_modify_metadata(new Boolean(request.getParameter("allowPublicModifyMetadata")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicModifyNotess") 
                            && (request.getParameter("allowPublicModifyNotess").equals("true") || request.getParameter("allowPublicModifyNotess").equals("false"))){
                        pp.setAllow_public_modify_notes(new Boolean(request.getParameter("allowPublicModifyNotess")));
                        result = 1;
                    }else if(null != request.getParameter("allowPublicReadTranscription") 
                            && (request.getParameter("allowPublicReadTranscription").equals("true") || request.getParameter("allowPublicReadTranscription").equals("false"))){
                        pp.setAllow_public_read_transcription(new Boolean(request.getParameter("allowPublicReadTranscription")));
                        result = 1;
                    }else{
                        //if non of above, send back not accepttable. The permission is not acceptable. 
                        result = response.SC_NOT_ACCEPTABLE;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ChangeUserPermissionServlet.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassCastException cce){
                    System.out.println("Type error of project user permission. ");
                    Logger.getLogger(ChangeUserPermissionServlet.class.getName()).log(Level.INFO, null, cce);
                }
            }
        }else{
            result = response.SC_FORBIDDEN;
        }
        out = response.getWriter();
        out.print(result);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    /**
     * Add user to a project by saving user to project group.
     * @param projectId
     * @param userId
     * @return 1: user added, 11: user is already in project
     */
    private int addUserToProject(int projectdId, int uid){
        int result = 1;
        try {
            //get project by project id
            textdisplay.Project thisProject = new textdisplay.Project(projectdId);
            user.Group thisGroup = new user.Group(thisProject.getGroupID());
            //get user by user id
            user.User thisUser = new user.User(uid);
            //test if the user has aleady in the group
            if (!thisGroup.isMember(uid)) {
                //if the user is not a member, add user to the group
                thisGroup.addMember(uid);
                result = 1;
            }else{
                //user has already in the group
                result = 11;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ChangeUserPermissionServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * Remove a user from project by deleting user from project group.
     * @param projectId
     * @param userId
     * @param currentUid (uid from session)
     * @return 1: user removed, 0: user isn't in project, 
     */
    private int removeUserFromProject(int projectdId, int uid, int currentUid){
        int result = 1;
        try {
            //get project by project id
            textdisplay.Project thisProject = new textdisplay.Project(projectdId);
            user.Group thisGroup = new user.Group(thisProject.getGroupID());
            //get user by user id
            user.User thisUser = new user.User(uid);
            //test if the user has aleady in the group
            if (!thisGroup.isMember(uid)) {
                //if the user is not a member, return 0
                result = 0;
            }else{
                //user has already in the group, remove user
                //Do they have permission to remove this person? That would be either isAdmin==true or current user=requestuested user
                if (thisGroup.isAdmin(currentUid) || uid == currentUid) {
                   thisGroup.remove(uid);
                }
                result = 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ChangeUserPermissionServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * Update user role. 
     * @param projectId
     * @param userId
     * @param currentUid (uid from session)
     * @param role
     * @return 1: user removed, 0: user isn't in project
     */
    private int changeUserRole(int projectdId, int uid, int currentUid, roles role){
        int result = 1;
        try {
            //get project by project id
            textdisplay.Project thisProject = new textdisplay.Project(projectdId);
            user.Group thisGroup = new user.Group(thisProject.getGroupID());
            //get user by user id
            user.User thisUser = new user.User(uid);
            //test if the user has aleady in the group
            if (!thisGroup.isMember(uid)) {
                //if the user is not a member, return 0
                result = 0;
            }else{
                //user has already in the group, change user role
                //Do they have permission to promote this person? That would be either isAdmin==true or current user=group leader
                if (thisGroup.isAdmin(currentUid)) {
                   thisGroup.setUserRole(currentUid, uid, role);
                }
                result = 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ChangeUserPermissionServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
}
