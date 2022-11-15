# TPEN Contributors Guide
![](http://t-pen.org/TPEN/images/tpen_logo_header.jpg)

### 1. Welcome

#### 1.1 Thank You!
Thank you for considering contributions to the TPEN source code.  TPEN relies on Open Source contributions to stay current with Linked Data best practices occurring all over the web.  Below you will find guides and guidelines for contributing to the code.

### 1.2 Code of Conduct
Use this as a model maybe https://iiif.io/event/conduct/, or perhaps just say TPEN adopts this code of conduct??

### 2. Tables of Contents
- 1. Wecome
    + Thank You
    + Code of Conduct
- 2. Table of Contents
- 3. Introduction
- 4. Code Free Contributions
    + Help!
    + Submit a (Good) Bug Report
    + Submit a (Good) Enhancement Suggestion
- 5. Source Code Contributions
    + Here be Dragons, Semicolons, and the Dreaded Brackets
- 6. TPEN User Interfaces Front End
    + HTML + JS + CSS Interfaces
    + Transcription Interface
    + Interface Diagram
- 7. TPEN Java Back End
    + Servlets and Classes
    + Example Servlets
    + Servlet Diagram
- 8. Branches
- 9. Issues and Pull Requests
- 10. Additioinal Information

### 3. Introduction

#### 3.1 Architecture From High Up
TPEN's greatest contribution to the internet is the Linked Open Data it produces, stores and makes available. The TPEN 2.8 distribution does this with Java Classes, a bulk of which are HTTPServlets mapped by a web.xml document Spring and Struts style. The data is stored in a MySQL database. The proper data is queried for by a Servlet or Class then reconfigured as JSON-LD by that class, then sent on in the body of a HTTPServletResponse to clients.

TPEN's greatest use case is Line by Line Transcription of Manuscripts. The response from the Java servlets is processed by Javascript middleware that builds it into a HTML User Interface. Users can set up projects based off of particular manuscripts then build teams around those projects for transcribing all pertinent lines on pertinent pages. Project Management, User Management, Admin Reports, and Transcription interfaces exist to give users the controls necessary to do all this. Those interfaces interact with existing Servlets to take data from a user in the interface to the MySQL database, and then back when necessary.

To follow are guides for where to begin based on how you would like to contribute and what you would like to contribute to.
-  Submit a Bug Report
-  Submit an Enhancement Suggestion
-  Contribute Code for the TPEN User Interfaces Front End
-  Contribute Code for the TPEN Servlets Back End

### 4. Code Free Contributions

#### 4.1 Help!

#### 4.2 Submit a (Good) Bug Report

#### 4.3 Submit a (Good) Enhacement Suggestion

### 5. Source Code Contributions

#### 5.1 Here Be Dragons, Semicolons, and the Dreaded Brackets

#### 6. TPEN User Interfaces Front End

#### 6.1 HTML + JS + CSS Interfaces
TPEN offers various interfaces which offers users the ability to set up projects built around manuscripts and perform line by line transcription of those manuscripts. 

#### 6.2 Transcription Interface
We will look specifically at the transcription interface located in `/web/transcription.html` which uses the script file `/web/js/transcribe.js` to `fetch()` data using the available servlets.  In this case, the TPEN interface acts as the client.

#### 6.3 Interface Diagram
<p align="middle">
    <img align="top" src="web/images/diagram2.jpg" width="420"/> <img align="top" src="web/images/diagram3.jpg" width="225"/>
</p>

#### 7. TPEN Java Back End

#### 7.1 Servlets and Classes
TPEN uses Java 8 through those most recent JDK.  The Java Classes which extend [HTTPServlet](https://javaee.github.io/javaee-spec/javadocs/javax/servlet/http/HttpServlet.html) are considered "Java Servlets".  There are also various Java Classes that back end duties which are not exposed to users, though some of them contain helper functions for the Java Servlets.

- The servlets are located in `/src/java/slu/edu/tpen/servlet`
- The classes are located throughout `/src/`
- The URL patterns for the servlet are recorded in `/web/WEB-INF/web.xml`

#### 7.2 Example Servlet
TPEN offers an endpoint for any user project that returns a IIIF Presentation API Manifest JSON-LD representation of the project.  The JSON-LD contains the image URIs, the segments of interest from those images, and text to go along with those segments.

Here is a diagram of what happens when a user asks for a particular Manifest like http://t-pen.org/TPEN/manifest/7006

#### 7.3 Servlet Diagram
- /src/java/slu/edu/tpen/servlet/ProjectServlet.java
- /src/java/slu/edu/tpen/transfer/JSONLDExporter.java
- web.xml maps the URL pattern to those files via the following entry
```
<servlet>
    <servlet-name>ProjectServlet</servlet-name>
    <servlet-class>edu.slu.tpen.servlet.ProjectServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>ProjectServlet</servlet-name>
    <url-pattern>/manifest/*</url-pattern>
</servlet-mapping>
```
<img align="top" src="web/images/diagram1.jpg" width="320"/>

### 8. Branches

### 9. Issues and Pull Requests

### 10. Additional Information
