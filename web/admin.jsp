<%-- 
    Document   : admin
    Created on : Feb 25, 2011, 6:09:12 PM
    Author     : jdeerin1
--%>
<%@page import="textdisplay.WelcomeMessage"%>
<%@page import="textdisplay.Project"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.*"%>
<%@page import="org.owasp.esapi.ESAPI" %>
<%@page import="utils.Tool"%>
<%@page import="utils.UserTool"%>
<%@page import="textdisplay.Manuscript" %>
<%@page import="static textdisplay.Folio.getRbTok" %>
<%@page import="textdisplay.CityMap" %>
<%@page import ="user.*"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>User Account Management</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>  
        <script type="text/javascript" src="js/manuscriptFilters.js"></script>  
        <style type="text/css">
            #userAccount, #ms, #manageUsers, #options, #about, #reportsTab { margin: 0; padding: 0;}
            #updateManagement li, #reportsTab li,#userAccount li, #manageUsers li, #ms li, #options li, #about li { margin: 0 4px 3px 3px; padding: 0.4em; padding-left: 1.5em; height: 100%;overflow: hidden; float:left; width:29%; position: relative;}
            #updateManagement li{
                width: 87%;
            }
            #updateManagement input[type="text"]{
                width: 50%;
                margin: 3px 0px;
                padding: 3px;
            }
            #updateManagement .tpenButton{
                padding: 4px;
                margin-top: 8px;
            }
            #schedmaintenance, #countdown{
                color: green;
            }
            #upgradeMessage{
                color: blue;
            }
            #manageUsers li {max-height: 350px;overflow: auto;}
            #tabs-3 {padding-bottom: 120px;}
            #ms li {width:98% !important; height:auto !important;}
            #ms textarea {width:600px;height:8em;}
            #userAccount li ul,#ms li ul,#manageUsers li ul, #options li ul, #about li ul {padding: 0;margin: 0;}
            #userAccount li ul li,#ms li ul li,#manageUsers li ul li, #options li ul li, #about li ul li {list-style:outside none;padding:0;width:100%;}
            .approved, .deactivated {display: block;clear:both;}
            .approved:after, .deactivated:after {content:attr(title);}
            .deactivated {color:red;}
            #userSummary span {display: block;width: 100%;}
            label {float: none; font-weight: normal;padding: 0;width: auto;display: block;} /* reset formatting */
            #newUserApproval label:hover {color:green;}
            #userDeactivation label:hover,#denyUsers label:hover {color:red;}
            #manageUsers label {display: block;width:100%;position: relative;} /*Keep checkboxes lined up*/
            #newUserApproval .approved {color: darkgreen;}
            #manageUsersBtn, #emailAlert {width:50%;margin: 0 auto 10px; padding: 5px 0;display: block;text-align: center;text-decoration: none;}
            a.ui-corner-all {text-align: center;text-decoration: none;}
            .iprs,.archAlert {display: none;}
            #closePopup:hover {color: crimson;cursor: pointer;}
            #adminMS,#userEmailList {position: absolute; top:25%; left:25%; width:50%; display: none;z-index: 500;}
            #form {height: 425px; margin: 0 auto; width:515px;border-width: 1px; 
                   -webkit-box-shadow: -1px -1px 12px black;
                   -moz-box-shadow: -1px -1px 12px black;
                   box-shadow: -1px -1px 12px black;
            }
            #listings a {position: relative;float:left;padding: 0 4px;}
            #adminManuscript {max-width: 120px;padding: 15px;margin:0 15%;position: relative;}
            #about li {height: 400px;overflow: auto;}
            #contact,#FBextra {width:100%;height:6em;}
            .contactCOMMENT{padding:2px;text-align: center;position: relative;z-index: 2;cursor: pointer;}
            .contactDiv{margin:-4px 5px 2px;clear:left;background: #FAF0E6;padding:6px 2px 2px;
/*                        display:none;*/
                        overflow: hidden;z-index: 1; border: 1px solid #A68329;
                       -moz-box-shadow: -1px -1px 2px black;
                       -webkit-box-shadow: -1px -1px 2px black; 
                       box-shadow:-1px -1px 2px black;}
            #overlay {display: none;}
            #overlayNote{position: fixed;top:2%;right:2%;white-space: nowrap;font-size: large;font-weight: 700;font-family: monospace;text-shadow:1px 1px 0 white;}
            .mapCheck {display: none;}
            .mapCheck[checked],.mapCheck:checked {display: inline-block;}
            .msSelect {display: none;padding: 3px;background-color: rgba(66,66,00,.3);position: relative;cursor: pointer;white-space: nowrap;overflow: hidden;width: 100%;text-overflow:ellipsis;}
            .msSelect.newCity {display:block;}
            .msSelect:hover {background-color: rgba(166,166,90,.1)}
            .mapCheck[checked]:after,.mapCheck:checked:after{
                content: '';
                background-color: green;
                position: absolute;
                top:0;left:0;
                width:100%;height:100%;
                opacity:.2;
            }
            #cityMapContain {float: right;width:300px;height:200px;position: relative;}
            #cityMap,#cityMapZoom {position: absolute;}
            #cityMap {top:0;left:0;width:100%;}
            #cityMapZoom {bottom:-5px;right:-3px;width: 30%;height:30%;overflow: hidden;box-shadow:-1px -1px 3px black;}
            #cityMapZoom img {position:relative;top:-100%;left:0;}
            #mapCities {position: relative;float: left;clear:left;max-height: 200px;overflow: auto;max-width: 300px;}
            #updateCityMap button,#updateCityMap input {padding: .4em;float: left;
            }
            #updateCityMap p {max-width: 300px;}
            #cityString {width:70%;}
            #updateMap {width: 25%;float:right !important;}
            #clearMap {width:100%;clear:left;}
            #mapSearch {
                width:300px;
            }
            #welcomeMsg textarea {
                height:2em;
            }
            #welcomeMsg textarea, welcomeForm {
                -o-transition: height, .5s;
                -moz-transition: height .5s;
                -webkit-transition: height .5s;
                transition: height .5s;
            }
            #welcomeForm:hover textarea {
                height:18em;
            }
            #welcomeForm {
                position:absolute !important;
                width:96% !important;
                height:auto !important;
                bottom:0;
            }
        </style>
        <script>
            var selecTab<%if (request.getParameter("selecTab") != null) {
                out.print("=" + request.getParameter("selecTab"));
            }%>;
                var userList = '';
                function scrubListings (){
                    $("#listings").ajaxStop(function(){
                        $("#listings")
                        .find("a[href *= 'transcription']").remove().end()
                        .find("a").attr("href",function(){
                            var oldLink = $(this).attr("href");
                            $(this).attr('href',oldLink.replace("addMStoProject","manuscriptAdmin")+"&unrestricted=true");
                        }).html("<span class='ui-icon ui-icon-wrench left'></span>Modify");
                    });
                }
                function overlay (text) {
                    $("#userEmailList").val(text).add("#overlay").show('fade',500);
                }
                $(function() {
                    if (selecTab) $('#tabs').tabs('option','selected',selecTab);
                    /* Interface Feedback Handlers */
                    var awaitingApproval = $('#newUserApproval>input:checkbox').size()
                    if(awaitingApproval>0){
                        var tasklist = "Click on the 'Manage Users' tab to complete these pending tasks:<span class='approved'><span class='left ui-icon ui-icon-check'></span>" + awaitingApproval + " users waiting for approval.</span>";
                        if (awaitingApproval==1) tasklist.replace("users","user")
                        $('#taskList').prepend(tasklist)
                        .parent('li').fadeIn(2000);
                    };
                    $("#newUserApproval").find("input:checkbox").change(function(){
                        $("#manageUsersBtn,#userAlert").slideDown();
                        $(this).parent("label").toggleClass("approved");
                    });
                    $("#userDeactivation,#denyUsers").find("input:checkbox").on('change',function(){
                        $("#manageUsersBtn").slideDown();
                        $("#userAlert").show();
                        $(this).parent("label").toggleClass("deactivated");
                    });
                    $('#overlay').on("click",function(event){
                        $(this).hide(250);
                        $(".popover").hide();
                    });     
                    $("input:submit").addClass("ui-state-default ui-corner-all ui-button")
                    .add("#manageUsersBtn").hover(
                    function(){$(this).addClass     ("ui-state-hover");},
                    function(){$(this).removeClass  ("ui-state-hover");}
                );
                    $("#iprs").change(function(){
                        if ($(".iprs").is(":visible")){
                            $(".iprs:visible").hide("slide",{direction:"left"},"fast",function(){
                                $("#ipr"+$("#iprs").val()).show("slide",{direction:"right"},"fast");
                            });
                        } else {
                            $("#ipr"+$("#iprs").val()).show("slide",{direction:"right"});
                        }   
                    });
                    $("#archiveAlert").change(function(){
                        if ($(".archAlert").is(":visible")){
                            $(".archAlert:visible").hide("slide",{direction:"left"},"fast",function(){
                                $("#archAlert"+$("#archiveAlert").val()).show("slide",{direction:"right"},"fast");
                            });
                        } else {
                            $("#archAlert"+$("#archiveAlert").val()).show("slide",{direction:"right"});
                        }   
                    });
                    $("#adminManuscript").click(function(){
                        $("div#adminMS,#overlay").show('fade',500);
                    });
                    $("#closePopup").click(function(){
                        $("#overlay").click();
                    });
                    $(".contactDiv").addClass('ui-corner-all');
//                    $(".contact").click(function(){
//                        $(this).siblings(".contactDiv").slideToggle(500);
//                    });
                });
                function manageUsers(){
                    $("#manageUsersBtn").children("span").switchClass("ui-icon-alert","ui-icon-check");
                    var saveChangesURL = ["admin.jsp?selecTab=2"];
                    var $changes = $("input:checked");
                    var approve = new Array();
                    var deactivate = new Array();
                    var deny = new Array();
                    $changes.filter("[name^='approve']").each(function(index){
                        approve[index] = "approveUser[]="+this.value;
                    });
                    $changes.filter("[name^='deactivate']").each(function(index){
                        deactivate[index] = "deactivateUser[]="+this.value;
                    });
                    $changes.filter("[name^='eliminate']").each(function(index){
                        deny[index] = "denyUser[]="+this.value;
                    });
                    if(approve.length>0)    saveChangesURL.push(approve.join("&"));
                    if(deactivate.length>0) saveChangesURL.push(deactivate.join("&"));
                    if(deny.length>0)       saveChangesURL.push(deny.join("&"));
                    window.location.href = saveChangesURL.join("&");
                }
        </script>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <p>Use this page to manage your account, change your password, or manage manuscripts and user privileges.</p>
                <div id="outer-barG">
                    <div id="front-barG" class="bar-animationG">
                        <div id="barG_1" class="bar-lineG">
                        </div>
                        <div id="barG_2" class="bar-lineG">
                        </div>
                        <div id="barG_3" class="bar-lineG">
                        </div>
                    </div>
                </div>
                <div id="tabs">
                    <%
                        user.User thisUser = null;
                        if (session.getAttribute("UID") != null) {
                            thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
                    %>
                    <ul>
                        <li><a title="Reset or change your password; view account privileges" href="#tabs-1">User Account</a></li>
                        <li><a title="Alter IPR statements, restrict access, update global metadata" href="#tabs-2">Manuscripts</a></li>
                        <%if (thisUser.isAdmin()) { //hiding non-Admin tab%>
                        <li><a title="Manage Users" href="#tabs-3">Manage Users</a><div id="userAlert" class='ui-icon-alert ui-icon right' style="display:none;margin: 8px 8px 0 0;"></div></li>
                        <li><a title="Reports" href="#reportsTab">Reports</a></li>
                        <li><a title="Update Maintenance Message" href="#updateTab">Maintenance Message</a></li>
                        <%}%>
                        
                        <li><a title="About the T&#8209;PEN project" href="#aboutTab">About T&#8209;PEN</a></li>
                    </ul>
                    <div id="tabs-1">
                        <% } else {%>
                        <div id="userUnknown2">
                            <div class="left inline" style="width:300px;"> <form id="login" action="login.jsp" method="POST" >
                                    <fieldset>
                                        <legend>Login Here:</legend>
                                        <label for="uname">Email</label><input class="text" type="text" name="uname"/><br/>
                                        <label for="password">Password</label><input  class="text" type="password" name="password"/><br/>
                                        <input type="hidden" name="ref" value="admin.jsp"/>
                                        <span class='buttons right'><button type="submit" title="Log In" value="log in">Log In</button></span>
                                    </fieldset>
                                </form></div>
                        </div>
                        <%                        }
                            //process any submitted requests
                            user.User[] unapprovedUsers = user.User.getUnapprovedUsers();
                            user.User[] allUsers = user.User.getAllActiveUsers();
                            if (thisUser != null) {
                                //String to return completion information to user on Tabs-3
                                StringBuilder manageUserFeedback = new StringBuilder("");
                                //a request to deactivate a user so they can no longer log in. Does NOT delete any associated content
                                if (request.getParameterValues("deactivateUser[]") != null) {
                                    String[] deactivateUser = request.getParameterValues("deactivateUser[]");
                                    for (int i = 0; i < deactivateUser.length; i++) {
                                        if (thisUser.isAdmin()) {
                                            user.User newUser = new user.User(Integer.parseInt(deactivateUser[i]));
                                            newUser.deactivate();
                                            manageUserFeedback.append("<span title=' has been deactivated.' class='deactivated'>").append(newUser.getFname()).append(" ").append(newUser.getLname()).append("</span>");
                                        }
                                    }
                                }
                                if (request.getParameterValues("denyUser[]") != null) {
                                    String[] denyUser = request.getParameterValues("denyUser[]");
                                    for (int i = 0; i < denyUser.length; i++) {
                                        if (thisUser.isAdmin()) {
                                            user.User newUser = new user.User(Integer.parseInt(denyUser[i]));
                                            newUser.deactivate();
                                            manageUserFeedback.append("<span title=' has been removed.' class='deactivated'>").append(newUser.getFname()).append(" ").append(newUser.getLname()).append("</span>");
                                        }
                                    }
                                }
                                if (manageUserFeedback.length() > 3) {
                                    out.println("<script>");
                                    out.println("$(document).ready(function() {");
                                    out.println("$('#manageUserFeedback').html(\"" + manageUserFeedback.toString() + "\").fadeIn(2000);");
                                    out.println("});");
                                    out.println("</script>");
                                }


                                //a password update request
                                if (request.getParameter("newPassword") != null) {
                                    String pass = request.getParameter("newPassword");
                                    String conf = request.getParameter("confirmPassword");
                                    if (pass.compareTo(conf) == 0) {
                                        thisUser.updatePassword(pass);
                                        out.print("<br><br><h3>Password updated!</h3><br><br>");
                                    } else {
                                        out.print("<br><br><ul><h3>Passwords did not match; no change has been made.</h3></ul><br><br>");
                                    }
                                }
                                if (request.getParameter("welcome") != null) {
                                    if (!thisUser.isAdmin()) {
                String errorMessage = thisUser.getFname() + ", you have attempted something limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                                    }
                                    textdisplay.WelcomeMessage nwm = new WelcomeMessage();
                                    nwm.SetMessage(request.getParameter("welcome"));
                                }
                                if (request.getParameter("eula") != null) {
                                    if (!thisUser.isAdmin()) {
                String errorMessage = thisUser.getFname() + ", you have attempted something limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                                    }
                                    String archiveName = request.getParameter("name");
                                    textdisplay.Archive a = new textdisplay.Archive(archiveName);
                                    a.setIPRAgreement(request.getParameter("eula"));
                                }
                                if (request.getParameter("alert") != null) {
                                    if (!thisUser.isAdmin()) {
                String errorMessage = thisUser.getFname() + ", you have attempted something limited to administrators.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                return;
                                    }
                                    String archiveMsg = request.getParameter("alert");
                                    textdisplay.Archive a = new textdisplay.Archive(request.getParameter("name"));
                                    a.setMessage(archiveMsg);
                                }
                            }
                            //reinitiate the user lists for use on the rest of the page
                            unapprovedUsers = user.User.getUnapprovedUsers();
                            allUsers = user.User.getAllActiveUsers();
                            //if the person isnt logged in, only show them the 'reset my password via email' div
                            if (thisUser == null) { //reset a user's password based on their email address
                                if (request.getParameter("emailSubmitted") != null) {
                                    user.User toReset = new user.User(request.getParameter("email"));
                                    if (toReset.getUID() > 0) {
                                        if (!toReset.requiresApproval()) {
                                            toReset.resetPassword(true);
                                            out.print("<br><br><h3>Password reset!</h3><br>Please check your e-mail from "+getRbTok("NOTIFICATIONEMAIL")+" for a new password.  If your e-mail does not arrive, please verify that it has not been caught by a spam filter.<br>");
                                        } else {
                                            out.print("This user does not exist or needs administrator approval before they can log in!");
                                            return;
                                        }
                                    } else {
                                        out.print("This user does not exist or needs administrator approval before they can log in!");
                                        return;
                                    }
                                }

                        %>
                        <div class="right" id="resetPassword" style="width:45%;">
                            <h3>Reset your Password</h3>
                            To reset your password and have the new password sent to your email address, please enter the email address associated with your account.
                            <form action="admin.jsp" method="POST">
                                <input type="text" name="email">
                                <input type="submit" name="emailSubmitted" value="Reset Password"/>
                            </form>
                            <h3>Accept an Invitation</h3>
                            Please enter the email address associated with your invitation to receive a temporary password.
                            <form action="admin.jsp" method="POST">
                                <input type="text" name="email">
                                <input type="submit" name="emailSubmitted" value="Redeem Invitation"/>
                            </form>
                        </div>
                        <%
                                //if they arent logged in, dont bother with showing them any of the other stuff
                                out.print("</div></div>\n<a class='returnButton' href='index.jsp'>Return to TPEN Homepage</a>\n</div>"); //close up the tab                                   
                                return;
                            }
                        %>
                        <ul id="userAccount" class="ui-helper-reset"> 
                            <li class="gui-tab-section">
                                <%if (request.getParameter("pleaseReset") != null) {
                                        System.out.print("<h3>Due to a server migration, we ask you to change your password below:<br></h3>");
                                    }
                                %>
                                <h3>Change your password</h3>
                                <div>
                                    <form action="admin.jsp" method="POST">
                                        <label>New Password
                                            <input type="password" name="newPassword" /></label>
                                        <label>Confirm Password
                                            <input type="password" name="confirmPassword" /></label>
                                        <input class="tpenButton right" type="submit">
                                    </form></div>
                            </li>
                            <li class="gui-tab-section">
                                <a class="tpenButton" href="index.jsp"><span class="ui-icon ui-icon-home right"></span>TPEN Homepage</a>
                            </li>
                            <li class="gui-tab-section" style="display:none;">
                                <h3>Task List</h3>
                                <div id="taskList"></div>
                            </li>
                            <li class="gui-tab-section" id="userSummary">
                                <h3>Account Information</h3>
                                Name: <%out.print(thisUser.getFname() + " " + thisUser.getLname());%> <br />
                                E-mail Login: <%out.print(thisUser.getUname());%> <br />
                                Status:<%if (thisUser.isAdmin()) {
                                        out.print("Administrator, ");
                                    }%>Contributor<%if (thisUser.requiresApproval()) {
                                                out.print(" (pending approval)");
                                            }%><br />
                                <%
                                    Project[] userProjects = thisUser.getUserProjects();
                                    if (userProjects.length > 0) {
                                        out.print("You are a member of " + userProjects.length + " project");
                                        if (userProjects.length == 1) {
                                            out.print(", " + userProjects[0].getProjectName() + ".");
                                        } else {
                                            out.print("s:");
                                            for (int i = 0; i < userProjects.length; i++) {
                                                out.println("<span>" + (i + 1) + ". " + userProjects[i].getProjectName() + "</span>");
                                            }
                                        }
                                     }
                                %>

                            </li>
                        </ul>
                    </div>
                    <!--                end of tab-1, user accounts-->

                    <div id="tabs-2">
                        <ul id="ms" class="ui-helper-reset"> 

                            <%                           //this request seeks to remove access restrictions for this manuscript.
                                if (request.getParameter("unrestrict") != null) {
                                    int msID = Integer.parseInt(request.getParameter("ms"));
                                    if (thisUser.isAdmin()) {
                                        textdisplay.Manuscript ms = new textdisplay.Manuscript(msID, true);
                                        ms.makeUnresricted();
                                    } else {
                                        out.print("Only admins can do that!");
                                        return;
                                    }
                                }
                                if (request.getParameter("restrict") != null) {


                                    int msID = Integer.parseInt(request.getParameter("ms"));
                                    if (thisUser.isAdmin()) {
                                        textdisplay.Manuscript ms = new textdisplay.Manuscript(msID, true);
                                        int controllingUID = Integer.parseInt(request.getParameter("uid"));
                                        ms.makeRestricted(controllingUID);
                                        ms.authorizeUser(controllingUID);
                                    }
                                }

                                if (thisUser.isAdmin()) { //hide non-Admin items%>
                                <li class="gui-tab-section">
                                <h3>Update IPR agreements </h3>
                                <select id="iprs">
                                    <option value="-1" selected>Select an archive</option>
                                    <%
                                        //allow admins to edit archive IPR agreements
                                        String[] archives = textdisplay.Archive.getArchives();
                                        for (int i = 0; i < archives.length; i++) {
                                            out.print("<option value='" + i + "'>" + archives[i] + "</option>");
                                            //out.print(new textdisplay.archive(archives[i]).getIPRAgreement());
                                            //out.print("<a href=\"?archive="+archives[i]+"\">Edit IPR agreement</a>");
                                        }%>
                                </select>
                                <%  for (int i = 0; i < archives.length; i++) {%>
                                <form id="ipr<%out.print(i);%>" class="iprs" action="admin.jsp" method="post">
                                    <textarea name="eula"><% out.print(new textdisplay.Archive(archives[i]).getIPRAgreement());%></textarea>
                                    <input type="hidden" name="name" value="<% out.print(archives[i]);%>"><br />
                                    <input type="hidden" name="selecTab" value="1">
                                    <input type="submit" name="submitted" value="Update <% out.print(archives[i]);%>">
                                </form>
                                <%
                                    }
                                %>
                            </li>
                            <li class="gui-tab-section" id="modifyArchiveAlerts">
                                <h3>Modify Archive Alert</h3>
                                <div class="ui-state-error-text"><span class="ui-icon ui-icon-alert left"></span>
                                    This message is intrusive and should be left blank unless a disruption or important message is needed.
                                </div>
                                <script>
                                    $("#iprs").clone().attr("id","archiveAlert").appendTo($("#modifyArchiveAlerts"));
                                    function escapeTextarea(textareaToEscape){
                                        textareaToEscape.value = escape(textareaToEscape.value);
                                    }
                                </script>
                                <%  for (int i = 0; i < archives.length; i++) {%>
                                <form id="archAlert<%out.print(i);%>" class="archAlert" onsubmit="escapeTextarea(this.getElementsByTagName('textarea')[0]);" action="admin.jsp" method="post">
                                    <textarea id="msg<%out.print(i);%>" name="alert"></textarea>
                                    <script>
                                              var msg<%out.print(i);%> = <% out.print("unescape('" + new textdisplay.Archive(archives[i]).message() + "')");%>;
                                              $("#msg<%out.print(i);%>").val(msg<%out.print(i);%>);
                                    </script>
                                    <input type="hidden" name="name" value="<% out.print(archives[i]);%>"><br />
                                    <input type="hidden" name="selecTab" value="1">
                                    <input type="submit" name="submitted" value="Post <% out.print(archives[i]);%>">
                                </form>
                                <%
                                    }
                                %>
                            </li>
                            <li class="gui-tab-section" id="modifyCityMap">
                                <script>
                                    function mapFilter(){
                                        $("#updateCityMap").find("input[type='checkbox']").not(':checked').remove();
                                    }
                                    $(function(){
                                        $("#updateCityMap").find(".msSelect").on({
                                            click: function(event){
                                                if(event.target!=this)return true;
                                                var checkbox = $(this).find("input[type='checkbox']");
                                                //                        console.log(event.target);
                                                if (!checkbox.prop("checked")){
                                                    checkbox.prop("checked",true);
                                                }
                                                $(this).addClass('activeMap').siblings().removeClass("activeMap");
                                                var city = $(this).attr('data-map');
                                                if (city == "na") {
                                                    //No reliable map data in lookup table
                                                    city = $(this).attr('title'); 
                                                    $(this).attr('data-map',city);
                                                }
                                                $("#cityString").val(city);
                                                var src = [
                                                    "https://maps.googleapis.com/maps/api/staticmap?",
                                                    "center=",city,
                                                    "&markers=icon:http://www.t-pen.org/TPEN/images/quillpin.png|",city,
                                                    "&sensor=false&scale=1&zoom=3&visibility=simplified&maptype=terrain",
                                                    "&size=",$("#cityMap").width(),"x",$("#cityMap").height()
                                                ].join("");
                                                var src2 = [
                                                    "https://maps.googleapis.com/maps/api/staticmap?",
                                                    "center=",city,
                                                    "&sensor=false&scale=1&zoom=10&visibility=simplified&maptype=terrain",
                                                    "&size=",$("#cityMap").width()*.3,"x",$("#cityMap").height()*.9
                                                ].join("");
                                                $("#cityMap").attr("src",src).show();
                                                $("#cityMapZoom img").attr("src",src2).show();
                                            }
                                        });
                                        $("#updateMap").on({
                                            click: function(){
                                                $(".activeMap").attr("data-map",$("#cityString").val()).click()
                                                .children('input[type="checkbox"]').val($("#cityString").val()).prop('checked',true);
                                            }
                                        });
                                        $("#clearMap").on({
                                            click: function(){
                                                $(".activeMap").attr("data-map","").children('input[type="checkbox"]').add("#cityString").val("");
                                            }
                                        });
                                        $("#cityString").on({
                                            keydown: function(event){
                                                if(!event)event=window.event;
                                                if(event.which==13){
                                                    // Enter pressed
                                                    event.preventDefault();
                                                    $("#updateMap").click();
                                                }
                                            }
                                        });
                                    });

                                </script>
                                <h3>Display City Map</h3>
                                <form id="updateCityMap" class="cityMap" action="admin.jsp" method="post" onsubmit="mapFilter();">
                                    <div id="cityMapContain">
                                        <img id="cityMap" src="https://maps.googleapis.com/maps/api/staticmap?center=St.%20Louis&zoom=3&sensor=false&scale=1&size=300x200&maptype=terrain&visibility=simplified&markers=icon:http://www.t-pen.org/TPEN/images/quillpin.png%257St.%20Louis" />
                                        <div id="cityMapZoom">
                                            <img src="https://maps.googleapis.com/maps/api/staticmap?center=St.%20Louis&zoom=10&sensor=false&scale=1&size=100x140&maptype=terrain&visibility=simplified&markers=icon:http://www.t-pen.org/TPEN/images/quillpin.png%257St.%20Louis" />
                                        </div>
                                    </div>
                                    <div class="right" id="mapSearch">
                                        <input id="cityString" class="left" placeholder="test values here" />
                                        <button class="tpenButton ui-button" id="updateMap" type="button"><span class="ui-icon ui-icon-search left"></span>Search</button>
                                        <button class="tpenButton ui-button" id="clearMap" type="button"><span class="ui-icon ui-icon-close left"></span>Remove City Map</button>
                                    </div>
                                    <p class="left">Select a city to view the map. Update the search string to change the associated map. Any checked city will be updated.</p>
                                    <button class="tpenButton left clear-left" id="mapAll" onclick="$('.msSelect').show();return false;" type="button">Show All Cities</button>
                                    <div id="mapCities">
                                        <%
                                            if (request.getParameter("updateCityMap") != null) {
                                                // Process updates to citymap
                                                Map<String, String[]> mapCities = request.getParameterMap();
                                                for (Map.Entry<String, String[]> entry : mapCities.entrySet()) {
                                                    if (entry.getKey().startsWith("city")) {
                                                        CityMap updateCity = new CityMap(entry.getKey().substring(4));
                                                        updateCity.setValue(entry.getValue()[0]);
                                                    }
                                                }
                                            }
                                            String[] cities = textdisplay.Manuscript.getAllCities();
                                            for (int i = 0; i < cities.length; i++) {
                                                CityMap thisCity = new CityMap(cities[i]);
                                                String mapped = thisCity.getValue();
                                                if (mapped.length() < 2) { // New city listing
                                                    mapped = "na";
                                                    out.print("<span class='msSelect newCity left' title='" + cities[i] + "' data-map='" + mapped + "'><input type='checkbox' class='mapCheck' value='" + cities[i] + "' id='city" + i + "' name='city" + cities[i] + "' />" + cities[i] + "</span> ");
                                                } else {
                                                    // no newCity flag
                                                    out.print("<span class='msSelect left' title='" + cities[i] + "' data-map='" + mapped + "'><input type='checkbox' class='mapCheck' value='" + cities[i] + "' id='city" + i + "' name='city" + cities[i] + "' />" + cities[i] + "</span> ");
                                                }
                                        %>          <%
    }
                                        %></div>
                                    <input type="hidden" name="selecTab" value="1">
                                    <input type="submit" name="updateCityMap" value="Save All Map Updates" class="left clear">
                                </form>

                            </li>
                            <li class="gui-tab-section">
                                <h3>Restrict access to a manuscript</h3>
                                Select a manuscript to restrict access and the user who will be in charge of controlling access to it. As an administrator, you will
                                always be able to control access to it as well.
                                <form action="admin.jsp" method="POST">

                                    <input type="hidden" name="restrict" value="true">
                                    <select name="ms" class="combobox">
                                        <%
                                            for (int i = 0; i < cities.length; i++) {
                                                textdisplay.Manuscript[] cityMSS = textdisplay.Manuscript.getManuscriptsByCity(cities[i]);
                                                for (int j = 0; j < cityMSS.length; j++) {
                                                    if (!cityMSS[j].isRestricted()) {
                                                        out.print("<option value=" + cityMSS[j].getID() + ">" + cityMSS[j].getShelfMark() + "</option>");
                                                    }
                                                }
                                            }
                                        %>
                                    </select>
                                    <select name="uid" class="combobox">
                                        <%
                                            for (int i = 0; i < allUsers.length; i++) {
                                                out.print("<option value=" + allUsers[i].getUID() + ">" + allUsers[i].getFname() + " " + allUsers[i].getLname() + " (" + allUsers[i].getUname() + ")" + "</option>");
                                            }

                                        %>
                                    </select>
                                    <input type="submit" name="submitted" value="restrict">
                                </form>

                            </li>
                            <% } //end of hiding Admin items
                                textdisplay.Manuscript[] mss = thisUser.getUserControlledManuscripts();
                                if (mss.length > 0) {%>
                            <li class="ui-widget-content ui-corner-tr ui-corner-bl">
                                <h3>Manuscript Administration</h3>
                                <%if (thisUser.isAdmin()) {%>   <a class="ui-button tpenButton right" id="adminManuscript">Select an unrestricted manuscript to administer</a><%}%>
                                <h6>Restricted Manuscripts</h6>
                                <%
                                        out.print("Click a manuscript to administer access to it or modify the shelfmark<br>");
                                        for (int i = 0; i < mss.length; i++) {
                                            out.print("<a href=\"manuscriptAdmin.jsp?ms=" + mss[i].getID() + "\">" + ESAPI.encoder().decodeFromURL(mss[i].getShelfMark()) + "</a> <a href=\"admin.jsp?unrestrict=true&ms=" + mss[i].getID() + "&submitted=true\">remove access restrictions</a><br>");
                                        }
                                    }
                                %></li>
                        </ul>
                    </div>
                    <!--                end of tab-2, manuscripts-->
                    <%if (thisUser.isAdmin()) { //hiding non-Admin tab%>
                    <div id="tabs-3">
                        <ul id="manageUsers" class="ui-helper-reset"> 
                            <li class="left">
                                <a id="manageUsersBtn" class="tpenButton" href="#" onclick="manageUsers();return false;" style="display:none;">Save Changes</a>
                            </li>               
                            <li class="left">
                                <a id="emailAlert" class="tpenButton" href="#" onclick="overlay(userList);return false;">User Emails</a><br />
                            </li>
                            <li id="manageUserFeedback" class="gui-tab-section" style="display:none;"></li>
                            <%
                                //if this is an administrator, allow them to approve new users
                                if (thisUser.isAdmin()) {
                            %>
                            <li class="gui-tab-section clear-left">
                                <div id="newUserApproval">
                                    <h3>Activations</h3>
                                    <%
                                        for (int i = 0; i < unapprovedUsers.length; i++) {
                                    %><label for="approve<%out.print(i);%>"><input type="checkbox" name="approve<%out.print(i);%>" id="approve<%out.print(i);%>" value="<%out.print(unapprovedUsers[i].getUID());%>" /><%out.print(unapprovedUsers[i].getFname() + " " + unapprovedUsers[i].getLname() + " (" + unapprovedUsers[i].getUname() + ")");%></label>
                                        <%
                                            }
                                        %>
                                </div></li>
                            <li class="gui-tab-section">
                                <div id="denyUsers">
                                    <h3>Deny Requests</h3>
                                    <%
                                        for (int i = 0; i < unapprovedUsers.length; i++) {
                                    %><label for="eliminate<%out.print(i);%>"><input type="checkbox" name="eliminate<%out.print(i);%>" id="eliminate<%out.print(i);%>" value="<%out.print(unapprovedUsers[i].getUID());%>" /><%out.print(unapprovedUsers[i].getFname() + " " + unapprovedUsers[i].getLname() + " (" + unapprovedUsers[i].getUname() + ")");%></label>
                                        <%
                                            }
                                        %>
                                </div>
                            </li>
                            <li class="gui-tab-section">
                                <div id="userDeactivation">
                                    <h3>Deactivate User</h3>
                                    <%
                                        StringBuilder userEmails = new StringBuilder();
                                        for (int i = 0; i < allUsers.length; i++) {
                                            int lastActive = allUsers[i].getMinutesSinceLastActive();
                                            String ago = "Activity unknown";
                                            if (lastActive == -1) {
                                                //never
                                                ago = "This is an inactive user";
                                            } else if (lastActive < 60) {
                                                // minutes
                                                ago = (lastActive == 1) ? "Active 1 minute ago" : "Active " + lastActive + " minutes ago";
                                            } else if (lastActive < 1440) {
                                                // hours
                                                ago = (lastActive < 120) ? "Active in the last couple hours" : "Active " + Math.floor(lastActive / 60) + " hours ago";
                                            } else {
                                                //days
                                                ago = (lastActive < 2880) ? "Active yesterday" : "Active " + Math.floor(lastActive / 1440) + " days ago";
                                            }
                                            userEmails.append(", " + allUsers[i].getUname());
                                    %><label for="deactivate<%out.print(i);%>" title="<%out.print(ago);%>"><input type="checkbox" name="deactivate<%out.print(i);%>" id="deactivate<%out.print(i);%>" data-lastactive="<%out.print(lastActive);%>" value="<%out.print(allUsers[i].getUID());%>" /><%out.print(allUsers[i].getFname() + " " + allUsers[i].getLname() + " (" + allUsers[i].getUname() + ")");%></label>
                                        <%
                                            }
                                        %>
                                    <script>userList = '<%out.print(userEmails.toString().substring(2));%>';</script>
                                </div></li>
                                <%
                                textdisplay.WelcomeMessage welcome = new WelcomeMessage();
                                String wMsg = welcome.getMessagePlain();
                                %>
                                <li class="gui-tab-section" id="welcomeForm">
                                    <h3>Update Welcome Message </h3>
                                    <form id="welcomeMsg" class="ui-corner-all" action="admin.jsp" method="post">
                                        <textarea name="welcome" cols="78" rows="6"><%out.print(wMsg);%></textarea>
                                        <input type="hidden" name="selecTab" value="2">
                                        <input type="submit" name="submitted" style="display:block;" value="Update Welcome">
                                    </form>
                                </li>
                        </ul>
                    </div>
                                        <div id="reportsTab">
                                            <ul id="reports">
                                                <li class="gui-tab-section">
                                                    <h3>User Reports</h3>
                                                    <form action="reports.jsp" method="GET" target="_blank">
                      <select name="u" class="combobox">
                                        <%
                                            for (int i = 0; i < allUsers.length; i++) {
                                                out.print("<option value=" + allUsers[i].getUID() + ">" + allUsers[i].getFname() + " " + allUsers[i].getLname() + " (" + allUsers[i].getUname() + ")" + "</option>");
                                            }

                                        %>
                                    </select>
                                    <input class="tpenButton ui-button" type="submit" />
                                                    </form>

                                                </li>
                                                <li class="gui-tab-section">
                                                    <h3>Active Users and Projects</h3>
                                                    <a class="tpenButton ui-button" target="_blank" href="reports.jsp?active=true">Run Report</a>
                                                </li>
                                                <li class="gui-tab-section">
                                                    <h3>T&#8209;PEN Totals</h3>
                                                    <a class="tpenButton ui-button" target="_blank" href="reports.jsp?totals=true">Run Report</a>
                                                </li>
                                            </ul>
                                        </div>
                    <%} // end of isAdmin()
                                    }%>
                    <!--                end of tabs-3, manage users-->
                    <%if (thisUser.isAdmin()) {%>
                    <div id="updateTab">
                        <ul id="updateManagement">
                            <li class="gui-tab-section">
                                <h3>Create a warning about down time during web site updates and upgrades</h3>
                                <div></div>
                                <div id="upgradeSettings">
                                    <input type="hidden" name="cancelUpgrade" value="false">
                                    <input type="hidden" name="active" value="true">
                                    <span>Date and time (yyyy-mm-dd hh:mm:ss) : </span><input type="text" name="upgradeDate" placeholder="Date and time upgrade will take place"><br>
                                    <span>Message to display to the user: </span><input type="text" name="upgradeMessage" placeholder="Custom message for the user"><br>
                                    <span>Check to include a countdown: </span><input type="checkbox" name="updateTimer" ><br>
                                    <input class="tpenButton" type="button" value="Set Update Settings" onclick="setUpgrade();">
                                </div>
                            </li>
                            <li class="gui-tab-section">
                                <h3>Cancel an Upgrade</h3>
                                <div>
                                    If you already have an update message set, you can cancel it here and the default will be displayed instead. 
                                </div>
                                <span id="schedmaintenance">
                                    
                                </span>
                                <span id="countdown">
                                    
                                </span> <br>
                                <span id="upgradeMessage">
                                    
                                </span> 
                                <br>
                                <input class="tpenButton" type="button" value="Remove Update Settings" onclick="removeUpgrade();">
                            </li>
                        </ul>
                    </div>
                    <%}%>
                    <div id="aboutTab">
                        <ul id="about">
                            <li class="gui-tab-section">
                                <h3>T&#8209;PEN</h3>
                                <p>The Transcription for Paleographical and Editorial Notation (T&#8209;PEN) project is coordinated by the <a href="http://www.slu.edu/x27122.xml" target="_blank">Center for Digital Theology</a> at <a href="www.slu.edu" target="_blank">Saint Louis University</a> (SLU) and funded by the <a href="http://www.mellon.org/" target="_blank">Andrew W. Mellon Foundation</a> and the <a title="National Endowment for the Humanities" target="_blank" href="http://www.neh.gov/">NEH</a>. The <a target="_blank" href="ENAP/">Electronic Norman Anonymous Project</a> developed several abilities at the core of this project's functionality.</p>
                                <p>T&#8209;PEN is released under <a href="http://www.opensource.org/licenses/ecl2.php" title="Educational Community License" target="_blank">ECL v.2.0</a> as free and open-source software (<a href="https://github.com/jginther/T-PEN/tree/master/trunk" target="_blank">git</a>), the primary instance of which is maintained by SLU at <a href="www.T&#8209;PEN.org" target="_blank">T&#8209;PEN.org</a>.
                                </p>
                            </li>
                            <li id="contactForm" class="gui-tab-section">
                                <h3>Contact Us</h3>
                                <div>
<!--                                    <div class="tpenButton contact">Directed Communication</div>
                                    <div class="contactDiv" style="display:block;">
                                        <form id="bugForm" onsubmit="$('#FBextra').change();" method="POST" action="http://165.134.241.72/ScoutSubmit.asp" target="_blank">
                                            <input type="hidden" value="James Ginther" name="ScoutUserName" />
                                            <input type="hidden" value="T-PEN" name="ScoutProject" />
                                            <input type="hidden" value="Use Cases" name="ScoutArea" />
                                            <input type="hidden" value="Thank you. A new case has been submitted. You can close this tab to resume your work." name="ScoutDefaultMessage" />
                                            <input type="hidden" value="We are aware of this problem and are working to fix it. Thank you." name="ScoutMessage" />
                                            <input type="hidden" value="cubap@slu.edu" name="ScoutPersonAssignedTo" />
                                            <input type="hidden" value="1" name="Priority" />
                                            <input id="extraSubmit" type="hidden" value="" name="Extra" />
                                            <input id="FBemail" type="hidden" value="<%out.print(thisUser.getUname());%>" name="Email" />
                                            <input type="hidden" name="FriendlyResponse" value="1" />
                                                    Category is not supported in Scout at this time and will be added to the description.
                                            <select id="FBcategory" name="Category">
                                                <option value="Inquiry">Ask a Question</option>
                                                <option value="Feature">Request a Feature</option>
                                                <option value="Bug">Report a Bug</option>
                                            </select>
                                            <input type="text" value="Brief Description" name="Description" />
                                            <textarea id="FBextra" placeholder="Include any additional information" name="FBExtra"></textarea>
                                            <input type="submit" value="Submit" />
                                        </form>
                                    </div>-->
                                    <div>
                                        <%
                                            if (request.getParameter("contactTPEN") != null) {
                                                String msg = "Message was not successfully received.";
                                                if (request.getParameter("contact") != null) {
                                                    msg = request.getParameter("contact");
                                                }
                                                int msgSent = thisUser.contactTeam(msg);
                                                switch (msgSent) {
                                                    case 0:
                                                        out.print("<span class='loud'><span class='ui-icon ui-icon-check left'></span>Message sent</span>");
                                                        break;
                                                    case 1:
                                                        out.print("<span class='ui-state-error-text'><span class='ui-icon ui-icon-alert left'></span>You must log in to send a message</span>");
                                                        break;
                                                    case 2:
                                                        out.print("<span class='ui-state-error-text'><span class='ui-icon ui-icon-close left'></span>Server failed to send your message</span>");
                                                        break;
                                                }
                                            }
                                        %>
                                        <form action="admin.jsp" method="POST" onsubmit="return Message.isValid();">
                                            <script type="text/javascript">
                                                var Message = {
                                                    isValid:    function(){
                                                        var contact = $("#contact");
                                                        var msgLength = contact.val().length
                                                        var maxLength = 10000;
                                                        if (msgLength > maxLength) {
                                                            contact.addClass("ui-state-error-text")
                                                            .change(function(){
                                                                var maxLength = 10000;
                                                                var msgLength = $("#contact").val().length
                                                                if (msgLength < maxLength) contact.removeClass("ui-state-error-text");
                                                            });
                                                            alert ("Please limit your message to "+maxLength+" characters.");
                                                            return false;
                                                        }
                                                        if (msgLength === 0) {
                                                            alert ("No message to send");
                                                            return false;
                                                        }
                                                        return true;
                                                    }
                                                };
                                            </script>
                                            <input type="hidden" value="3" name="selecTab" />
                                            <textarea id="contact" name="contact" placeholder="User information will be included automatically with this message"></textarea>
                                            <input type="submit" name="contactTPEN" value="Send Message" />
                                        </form> </div>
                                </div>
                            </li>
                            <li class="gui-tab-section">
                                <h3>User Agreement</h3>
                                <p><b>Conditions of Use</b></p>
                                <p>As a T&#8209;PEN user, you agree 
                                    to use T&#8209;PEN, its tools and services, for their intended purpose.  
                                    You will not use T&#8209;PEN for illegal purposes.  You will not use 
                                    T&#8209;PEN to obtain digital images or transcription data without permission 
                                    or to void any Intellectual Property Rights (IPR) governing one or more 
                                    of the digital collections to which T&#8209;PEN provides access.  Furthermore, 
                                    you agree not to infringe on the rights of other T&#8209;PEN users through 
                                    your own use of T&#8209;PEN.  You also agree that any action that does 
                                    contravene these conditions of use may result in the suspension and 
                                    even deletion of your T&#8209;PEN account.  </p>
                                <p>You agree to abide by the 
                                    IPR conditions that govern access to, and use of, digital images in 
                                    each individual Digital Repository that have their manuscripts listed 
                                    and displayed in T&#8209;PEN. Those notices are displayed when you request 
                                    access to a manuscript of that repository for the first time. </p>
                                <p><b>Intellectual Property and 
                                        Permissions</b></p>
                                <p>You grant permission to 
                                    Saint Louis University (SLU) to store your transcription data on a SLU 
                                    server.  Even if you elect to keep your work completely private, 
                                    you give permission to SLU to use your work as an index for searching 
                                    the manuscripts that T&#8209;PEN has processed.  Your transcription data 
                                    will never be displayed without your express permission, but instead 
                                    will be used to search and display the image of the line of the manuscript 
                                    that matches the search query.  When search results are displayed, your 
                                    username will be cited as the transcription used in the search, but 
                                    no other personal data you have provided for your T&#8209;PEN account will 
                                    ever be displayed.  Your username is defined as the initial letter 
                                    of your first name and your surname.</p>
                                <p>You grant permission for 
                                    SLU to share your transcription data with the Digital Repository where 
                                    the digital manuscript resides.  The Digital Repository is prohibited 
                                    form using any transcription data for commercial purposes nor can they 
                                    distribute it without obtaining your permission. </p>
                                <p>You grant permission to 
                                    the Andrew W. Mellon Foundation to have access to, keep copies of, and 
                                    distribute your transcription data.  This IPR transfer is necessary 
                                    should SLU, for some unforeseen reason, be unable to provide access 
                                    to your transcription in the future; at which point the Mellon Foundation 
                                    would be have the permissions to provide access to your data stored 
                                    in T&#8209;PEN&#39;s server.  This IPR transfer, however, prohibits the Mellon 
                                    Foundation from using your transcription data for commercial purposes. </p>
                                <p><b>License to Use Transcription 
                                        Data</b></p>
                                <p>SLU grants you an unlimited 
                                    license to use any and all transcription data created under your username 
                                    for non-commercial purposes.  You may export your transcription 
                                    data using T&#8209;PEN&#39;s export functions and disseminate it in any electronic 
                                    or print format.</p>
                                <p>This unlimited license 
                                    cannot be interpreted as a license to gain access to repositories or 
                                    individual manuscript images that are protected by subscription or conditions 
                                    external to T&#8209;PEN functionality.     </p>
                                <p><b>Privacy</b></p>
                                <p>SLU will never share your 
                                    personal information that you provide to T&#8209;PEN without your express 
                                    written permission.  This includes your full name, complete email 
                                    address and list of projects and/or transcriptions.  Users who 
                                    elect to collaborate  with other users on projects agree to share 
                                    their full name and email address with their collaborators.  </p>
                                <p><b>Indemnification</b></p>
                                <p>As a T&#8209;PEN user, you indemnify  
                                    SLU, its affiliates and employees from any liability for damage to your 
                                    computer and/or any information stored therein because of your use of 
                                    T&#8209;PEN as a web-based application.</p>
                                <p>  </p>
                            </li>
                            <li class="gui-tab-section">
                                <h3>Development Team</h3>
                                <dl>
                                    <dt>Dr. Jim Ginther, Principal Investigator</dt>
                                    <dd>Director, Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Dr. Abigail Firey, co-Principal Investigator</dt>
                                    <dd><a href="http://ccl.rch.uky.edu" target="_blank" title="Carolingian Canon Law">CCL</a>&nbsp;Project&nbsp;Director, University&nbsp;of&nbsp;Kentucky</dd>
                                    <dt>Dr. Tomás O’Sullivan, Research Fellow (2010-11)</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Dr. Alison Walker, Research Fellow (2011-12)</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Michael Elliot, Research Assistant</dt>
                                    <dd>University&nbsp;of&nbsp;Toronto</dd>
                                    <dt>Meredith Gaffield, Research Assistant</dt>
                                    <dd>University&nbsp;of&nbsp;Kentucky</dd>
                                    <dt>Jon Deering, Senior Developer</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                    <dt>Patrick Cuba, Web Developer</dt>
                                    <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                </dl>
                                <!--
                                <dl>
                                        <dt>Dr. Jim Ginther, Principal Investigator</dt>
                                        <dd>Dean of the Faculty of Theology at University of St. Michael’s College </dd>
                                        <dt>Dr. Abigail Firey, co-Principal Investigator</dt>
                                        <dd><a href="http://ccl.rch.uky.edu" target="_blank" title="Carolingian Canon Law">CCL</a>&nbsp;Project&nbsp;Director, University&nbsp;of&nbsp;Kentucky</dd>
                                        <dt>Dr. Tomás O’Sullivan, Research Fellow (2010-11)</dt>
                                        <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                        <dt>Dr. Alison Walker, Research Fellow (2011-12)</dt>
                                        <dd>Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                        <dt>Michael Elliot, Research Assistant</dt>
                                        <dd>University&nbsp;of&nbsp;Toronto</dd>
                                        <dt>Meredith Gaffield, Research Assistant</dt>
                                        <dd>University&nbsp;of&nbsp;Kentucky</dd>
                                        <dt>Dr. Thomas Finan, Director</dt>
                                        <dd>The Walter J. Ong, SJ Center for Digital Humanities at Saint Louis University</dd>
                                        <dt>Jon Deering, Senior Developer</dt>
                                        <dd>The Walter J. Ong, SJ Center for Digital Humanities at Saint Louis University</dd>
                                        <dt>Patrick Cuba, Web Developer</dt>
                                        <dd>The Walter J. Ong, SJ Center for Digital Humanities at Saint Louis University</dd>
                                        <dt>Bryan Haberberger, Web Developer</dt>
                                        <dd>The Walter J. Ong, SJ Center for Digital Humanities at Saint Louis University</dd>
                                        <dt>Domhnall Ó h'Éigheartaigh, Project Manager</dt>
                                        <dd>The Walter J. Ong, SJ Center for Digital Humanities at Saint Louis University</dd>
                                        <dt>Sameersingh Gautam Deeljore, Server Administrator </dt>
                                        <dd>Pius XII Memorial Library at Saint Louis University</dd>
                                </dl>
                                -->
                            </li>
                            <li class="gui-tab-section">
                                <h3>Contributors</h3>
                                <h5>Repositories</h5>
                                <dl>
                                    <dt><a target="_blank" href="http://parkerweb.stanford.edu/">Parker Library on the Web</a></dt>
                                    <dt><a target="_blank" href="http://www.e-codices.unifr.ch/">e-codices</a></dt>
                                    <dt><a target="_blank" href="http://www.ceec.uni-koeln.de/">Codices Electronici Ecclesiae Coloniensis</a></dt>
                                    <dt><a target="_blank" href="http://hcl.harvard.edu/libraries/houghton/collections/early.cfm">Harvard Houghton Library</a></dt>
                                    <dt><a target="_blank" href="http://www.sisf-assisi.it/" title="Società internazionale di Studi francescani">SISF - Assisi</a></dt>
                                </dl>
                                <h5>Institutions</h5>
                                <dl>
                                    <dt><a target="_blank" href="http://www-sul.stanford.edu/">Stanford University Libraries</a></dt>
                                </dl>
                            </li>
                            <li class="gui-tab-section">
                                <h3>Site Elements</h3>
                                <h5>Tools</h5>
                                <p>T&#8209;PEN will continue to add tools for transcription as regular feature releases. Please contact us if you would like to see a particular tool integrated with the transcription interface.</p>
                                <h5>Images</h5>
                                <p>Manuscript images displayed on T&#8209;PEN are not the property of T&#8209;PEN, but are linked through agreement from hosting repositories. The User Agreement describes the users' rights to these images.</p>
                                <p>All images used or composited in the design of T&#8209;PEN originated in the public domain. Individuals who wish to use portions of this site's design are encouraged to seek out the original source file to adapt. Using the T&#8209;PEN logo or any of its design elements with the purposes of deceiving, defrauding, defaming, phishing, or otherwise misrepresenting the T&#8209;PEN project is prohibited.</p>
                                <h5>Logo</h5>
                                <p>The T&#8209;PEN logo displayed on each page and the variant on the home page was assembled by committee and is an identifying mark. The tyrannosaurus is used with permission from Mineo Shiraishi at <a href="http://www.dinosaurcentral.com/" target="_blank">Dinosaur Central.com</a>.</p>
                                <h5>Source Code</h5>
                                <p>All code generated by the T&#8209;PEN Development Team is covered by license as described in the User Agreement. This project makes use of several public libraries.</p>
                            </li>
                        </ul>
                    </div>

                </div>
                <!--                close up tabs panels-->
                <a class="returnButton" href="index.jsp">Return to TPEN Homepage</a>
            </div>
        </div>
        <div id="adminMS" class="popover"> <!-- container for managing unrestricted MSs -->
            <div class="callup" id="form"> <!-- add to project -->
                <span id="closePopup" class="right caps">close<a class="right ui-icon ui-icon-closethick" title="Close this window">cancel</a></span>
                <%    //Attach arrays of AllCities and AllRepositories represented on T&#8209;PEN
                    String[] cities = textdisplay.Manuscript.getAllCities();
                    String[] repositories = textdisplay.Manuscript.getAllRepositories();
                %>
                <label class="left" for="cities">City: </label>
                <select class="left" name="cities" onchange="Manuscript.filteredCity();scrubListings();" id="cities">
                    <option selected value="">Select a City</option>
                    <%  //Generate dropdown menus for available cities.
                        for (int i = 0; i < cities.length; i++) {
                            out.print("<option value=\"" + (cities[i]) + "\">" + cities[i] + "</option>");
                        }
                    %>
                </select>
                <label class="left clear" for="repositories">Repository: </label>
                <select class="left" name="repository" onchange="Manuscript.filteredRepository();scrubListings();" id="repositories">
                    <option selected value="">Select a Repository</option>
                    <%  //Generate dropdown menus for available repositories.
                        for (int i = 0; i < repositories.length; i++) {
                            out.print("<option class=\"" + (i + 1) + "\" value=\"" + (repositories[i]) + "\">" + repositories[i] + "</option>");
                        }
                    %>
                </select>
                <div id="listings" class="center clear"  style="height:355px;overflow: auto;">
                    <div class="ui-state-active ui-corner-all" align="center">
                        Select a city or repository above to view available manuscripts.
                    </div>

                </div>
            </div>
        </div>
        <div id="overlay" class="ui-widget-overlay">
            <div id="overlayNote">Click the page to return</div>
        </div>
        <textarea id="userEmailList" class="popover"></textarea>
        <script type="text/javascript">
            $(function() {
                maintenanceDate();  
            });
            if(!Date.prototype.format){Date.prototype.format=(function(){var a={d:function(){var b=this.getDate().toString();return b.length===1?"0"+b:b},D:function(){return a.l.call(this).slice(0,3)},j:function(){return this.getDate()},l:function(){switch(this.getDay()){case 0:return"Sunday";case 1:return"Monday";case 2:return"Tuesday";case 3:return"Wednesday";case 4:return"Thursday";case 5:return"Friday";case 6:return"Saturday"}},N:function(){return this.getDay()===0?7:this.getDay()},S:function(){if(this.getDate()>3&&this.getDate()<21){return"th"}switch(this.getDate().toString().slice(-1)){case"1":return"st";case"2":return"nd";case"3":return"rd";default:return"th"}},w:function(){return this.getDay()},z:function(){return Math.floor(((this-new Date(this.getFullYear(),0,1))/86400000),0)},W:function(){var b=new Date(this.getFullYear(),0,1);return Math.ceil((((this-b)/86400000)+b.getDay()+1)/7)},F:function(){switch(this.getMonth()){case 0:return"January";case 1:return"February";case 2:return"March";case 3:return"April";case 4:return"May";case 5:return"June";case 6:return"July";case 7:return"August";case 8:return"September";case 9:return"October";case 10:return"November";case 11:return"December"}},m:function(){var b=(this.getMonth()+1).toString();return b.length===1?"0"+b:b},M:function(){return a.F.call(this).slice(0,3)},n:function(){return this.getMonth()+1},t:function(){return 32-new Date(this.getFullYear(),this.getMonth(),32).getDate()},L:function(){return new Date(this.getFullYear(),1,29).getDate()===29?1:0},o:function(){return null},Y:function(){return this.getFullYear()},y:function(){return this.getFullYear().toString().slice(-2)},a:function(){return this.getHours()<12?"am":"pm"},A:function(){return this.getHours()<12?"AM":"PM"},B:function(){return null},g:function(){var b=this.getHours();return b>12?b-12:b},G:function(){return this.getHours()},h:function(){var b=a.g.call(this).toString();return b.length===1?"0"+b:b},H:function(){var b=a.G.call(this).toString();return b.length===1?"0"+b:b},i:function(){return this.getMinutes()<10?"0"+this.getMinutes():this.getMinutes()},s:function(){return this.getSeconds()<10?"0"+this.getSeconds():this.getSeconds()},u:function(){return this.getMilliseconds()},e:function(){return null},I:function(){return null},O:function(){var b=this.getTimezoneOffset()/60;return(b<0?"":"+")+(b<10?"0"+b.toString():b.toString())+"00"},P:function(){var b=a.O.call(this);return b.slice(0,3)+":"+b.slice(-2)},T:function(){return null},Z:function(){return parseInt(a.O.call(this),10)*60},c:function(){function c(d){return d<10?"0"+d.toString():d.toString()}var b="";b+=this.getUTCFullYear()+"-";b+=c(this.getUTCMonth()+1)+"-";b+=c(this.getUTCDate())+"T";b+=c(this.getUTCHours())+":";b+=c(this.getUTCMinutes())+":";b+=c(this.getUTCSeconds())+"Z";return b},r:function(){return this.toUTCString()},U:function(){return this.getTime()}};return function(b){var c="",e="",d;for(d=0;d<=b.length;d+=1){e=b.charAt(d);if(a.hasOwnProperty(e)){c+=a[e].call(this).toString()}else{c+=e}}return c}}())};

            function setUpgrade(){
                var url = "upgradeManagement";
                var upgradeDate = $("input[name='upgradeDate']").val();
                //var formattedDate = new Date(upgradeDate);
                var upgradeMessage = $("input[name='upgradeMessage']").val();
                var countdown = 0;
                if($("input[name='updateTimer']:checked").length){
                    countdown = 1;
                }
                else{
                    countdown = 0;
                }
                var params = {
                    "upgradeDate" : upgradeDate,
                    "upgradeMessage": upgradeMessage,
                    "countdown" : countdown,
                    "cancelUpgrade" : "false",
                    "getSettings" : "false",
                    "active" : 1
                };
                $.post(url, params)
                    .done(function(data){
                        if(data === "date parse error"){
                            alert("Bad date syntax.");
                        }
                        else{
                             maintenanceDate();  
                        }
                    }
                );
            }
            function removeUpgrade(){
                var url = "upgradeManagement";
                var params = {"cancelUpgrade":"true"};
                $.post(url, params)
                    .done(function(){
                        $("#schedmaintenance").html("The Default");
                        $("#countdown").html("");
                    }
                );
            }
            function maintenanceDate(){
                var url="upgradeManagement";
                var params = {"getSettings" : "get", "cancelUpgrade":"false"};
                $.post(url, params)
                    .done(function(data){
                        if(data){
                            var managerData = JSON.parse(data);
                            var mdate = managerData.upgradeDate;
                            var message = managerData.upgradeMessage;
                            var countdown = managerData.countdown;
                            var options = {  
                                weekday: "long", year: "numeric", month: "short",  
                                day: "numeric", hour: "2-digit", minute: "2-digit"  
                            };  
                            var dateForUser = new Date(mdate);
                            if(managerData.active > 0){
                                $("#upgradeMessage").html(message);
                                $("#schedmaintenance").html(dateForUser.toLocaleTimeString("en-us", options)); //.format('l, F jS, Y g:00a')
                                if(countdown > 0){
                                    setCountdown(mdate);
                                }
                                else{
                                    $("#countdown").html("");
                                }
                                //return(dateForUser.format('l, F jS, Y g:00a'));
                            }
                            else{
                                var today = new Date();
                                while ((today.getDay() !== 1) && (today.getDay() !== 3) && (today.getDay() !== 5)){
                                    today.setDate(today.getDate()+1);
                                }
                                // set to 8am Central Time
                                today.setHours(14 - today.getTimezoneOffset()/60);
                                //return(today.format('l, F jS, Y g:00a'));
                                $("#schedmaintenance").html(today.format('l, F jS, Y g:00a'));
                            }
                        }
                    });
            }
                                                                                                         
            function setCountdown(endtime){
                var end = new Date(endtime);

                var _second = 1000;
                var _minute = _second * 60;
                var _hour = _minute * 60;
                var _day = _hour * 24;
                var timer;

                function showRemaining() {
                    var now = new Date();
                    var distance = end - now;
                    if (distance < 0) {

                        clearInterval(timer);
                        document.getElementById('countdown').innerHTML = 'UPGRADING!';

                        return;
                    }
                    var days = Math.floor(distance / _day);
                    var hours = Math.floor((distance % _day) / _hour);
                    var minutes = Math.floor((distance % _hour) / _minute);
                    var seconds = Math.floor((distance % _minute) / _second);

                    document.getElementById('countdown').innerHTML = days + 'days ';
                    document.getElementById('countdown').innerHTML += hours + 'hrs ';
                    document.getElementById('countdown').innerHTML += minutes + 'mins ';
                    document.getElementById('countdown').innerHTML += seconds + 'secs';
                }

                timer = setInterval(showRemaining, 1000);
            }
            
    
            
            <%
                if (mss.length > 0) {%>
                    $("#taskList").append("<p title='Click on the \"Manuscript\" tab'>Update information or restrict access to manuscripts you control (<%out.print(mss.length);%> total).</p>")
            <%}%>
        </script>
    </body>
</html>
