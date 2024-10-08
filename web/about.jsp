<%-- 
    Document   : about
    Created on : Jan 18, 2012, 1:07:43 PM
    Author     : cubap
--%>

<%@page import="user.User"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>About T&#8209;PEN</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script>
        <script type="text/javascript" src="js/tpen.js"></script>
        <style type="text/css">
            ul {padding: 0;margin: 0;}
            ul li {list-style:outside none;padding:2%;width:32%;margin:.666%;height: 375px;overflow: auto;}
            textarea {width:100%;height:auto;}
            form {padding-right: 5%}
            #contact:focus {height:200px;}
        </style>
    </head>
    <body>
        <div id="wrapper">
            <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
                <div id="main">
                    <ul id="about">
                        <li class="gui-tab-section">
                            <h3>T&#8209;PEN</h3>
                            <p>The Transcription for Paleographical and Editorial Notation (T&#8209;PEN) project was coordinated by the <a href="//www.slu.edu/x27122.xml" target="_blank">Center for Digital Theology</a> at <a href="//www.slu.edu" target="_blank">Saint Louis University</a> (SLU) and funded by the <a href="//www.mellon.org/" target="_blank">Andrew W. Mellon Foundation</a> and the <a title="National Endowment for the Humanities" target="_blank" href="//www.neh.gov/">NEH</a>. The <a target="_blank" href="ENAP/">Electronic Norman Anonymous Project</a> developed several abilities at the core of this project's functionality.</p>
                            <p>T&#8209;PEN is released under <a href="//www.opensource.org/licenses/ecl2.php" title="Educational Community License" target="_blank">ECL v.2.0</a> as free and open-source software (<a href="https://github.com/jginther/T-PEN/tree/master/trunk" target="_blank">git</a>), the primary instance of which is maintained by SLU at <a href="//www.t-pen.org" target="_blank">T&#8209;PEN.org</a>.
                            <p>T&#8209;PEN is currently (2022) maintained by the Research Computing Group at Saint Louis University.</p>    
                        </li>
                        <li class="gui-tab-section" style="background-image:url(./images/trexhead.png);">
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
                            <h3>Support/Maintenance Team <br> T-PEN 2.8-3.0</h3>
                            <dl>                                
                                <dt>Patrick Cuba, IT Architect</dt>
                                <dd>Research Computing Group, Saint Louis University</dd>
                                <dt>Bryan Haberberger, Full Stack Developer</dt>
                                <dd>Research Computing Group, Saint Louis University</dd>
                            </dl>
                        </li>
                        <li class="gui-tab-section">
                            <h3>Support/Maintenance Team <br> T-PEN 2.0-2.8</h3>
                            <dl>                                
                                <dt>Dr. Jim Ginther, Principal Investigator</dt>
                                <dd>Director, Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                <dt>Dr. Thomas Finan, Principal Investigator (2016-present)</dt>
                                <dd>Director, Walter J. Ong S.J. Center for Digital Humanities, Saint Louis University</dd>
                                <dt>Donal Hegarty, Project Manager/UX Designer</dt>
                                <dd>Walter J. Ong S.J. Center for Digital Humanities, Saint Louis University</dd>
                                <dt>Patrick Cuba, Lead Developer</dt>
                                <dd>Walter J. Ong S.J. Center for Digital Humanities, Saint Louis University</dd>
                                <dt>Bryan Haberberger, Web Developer</dt>
                                <dd>Walter J. Ong S.J. Center for Digital Humanities, Saint Louis University</dd>
                                <dt>Han Yan, Web Developer</dt>
                                <dd>Walter J. Ong S.J. Center for Digital Humanities, Saint Louis University</dd>
                            </dl>
                        </li>
                        <li class="gui-tab-section">
                            <h3>Development Team <br> T-PEN 2.0</h3>
                            <dl>
                                <dt>Dr. Jim Ginther, Principal Investigator</dt>
                                <dd>Director, Center&nbsp;for&nbsp;Digital&nbsp;Theology, Saint&nbsp;Louis&nbsp;University</dd>
                                <dt>Dr. Abigail Firey, co-Principal Investigator</dt>
                                <dd><a href="//ccl.rch.uky.edu" target="_blank" title="Carolingian Canon Law">CCL</a>&nbsp;Project&nbsp;Director, University&nbsp;of&nbsp;Kentucky</dd>
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
                                <dd>Research Computing Group, Saint Louis University</dd>
                            </dl>
                        </li>
                        <li class="gui-tab-section">
                            <h3>Contributors</h3>
                            <h5>Repositories</h5>
                            <dl>
                                <dt><a target="_blank" href="//parkerweb.stanford.edu/">Parker Library on the Web</a></dt>
                                <dt><a target="_blank" href="//www.e-codices.unifr.ch/">e-codices</a></dt>
                                <dt><a target="_blank" href="//www.ceec.uni-koeln.de/">Codices Electronici Ecclesiae Coloniensis</a></dt>
                                <dt><a target="_blank" href="//hcl.harvard.edu/libraries/houghton/collections/early.cfm">Harvard Houghton Library</a></dt>
                                <dt><a target="_blank" href="//www.sisf-assisi.it/" title="Società internazionale di Studi francescani">SISF - Assisi</a></dt>
                            </dl>
                            <h5>Institutions</h5>
                            <dl>
                                <dt><a target="_blank" href="//www-sul.stanford.edu/">Stanford University Libraries</a></dt>
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
                            <p>The T&#8209;PEN logo displayed on each page and the variant on the home page was assembled by committee and is an identifying mark. The tyrannosaurus is used with permission from Mineo Shiraishi at <a href="//www.dinosaurcentral.com/" target="_blank">Dinosaur Central.com</a>.</p>
                            <h5>Source Code</h5>
                            <p>All code generated by the T&#8209;PEN Development Team is covered by license as described in the User Agreement. This project makes use of several public libraries.</p>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </body>
</html>
