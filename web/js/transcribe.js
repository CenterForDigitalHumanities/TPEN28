var tpen = {
    project: {
        id: 0,
        tools: [],
        leaders: [],
        buttons: [],
        hotkeys: [],
        xml: [],
        specialChars:[],
        permissions: [],
        metadata: [],
        folios: [],
        user_list: [],
        leader_list: [],
        projectName: "",
        groupID:0,
        linebreakSymbol:"",
        projectImageBounding:"",
        linebreakCharacterLimit:0
    },
    manifest: {
        
    },
    screen:{
        focusItem:[null, null],
        liveTool: "none",
        zoomMultiplier: 2,
        isMagnifying: false,
        isFullscreen: true,
        isAddingLines: false,
        colorList: [
            "rgba(153,255,0,.4)",
            "rgba(0,255,204,.4)",
            "rgba(51,0,204,.4)",
            "rgba(204,255,0,.4)",
            "rgba(0,0,0,.4)",
            "rgba(255,255,255,.4)",
            "rgba(255,0,0,.4)"],
        colorThisTime: "rgba(255,255,255,.4)",
        currentFolio: 0,
        currentAnnoListID: 0,
        nextColumnToRemove: null,
        dereferencedLists : [],
        parsing: false
    },
    user: {
        isAdmin: false,
        UID: 0,
        fname: "",
        lname: "",
        openID : 0
    }
};
var dragHelper = "<div id='dragHelper'></div>";

/**
 * Redraw the screen for use after updating the current line, folio, or
 * tools being used. Expects all screen variables to be set.
 *
 * @return {undefined}
*/
function redraw() {
    tpen.screen.focusItem = [null, null];
    var canvas = tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio];
    if (tpen.screen.currentFolio > - 1) {
        if (tpen.screen.liveTool === "parsing") {
            $(".pageTurnCover").show();
            fullPage();
            tpen.screen.currentFolio = parseInt(tpen.screen.currentFolio);
            if (!canvas) {
                canvas = tpen.manifest.sequences[0].canvases[0];
                console.warn("Folio was not found in Manifest. Loading first page...");
            }
            loadTranscriptionCanvas(canvas, "parsing");
            setTimeout(function () {
            hideWorkspaceForParsing();
                $(".pageTurnCover").fadeOut(1500);
            }, 800);
        }
        else {
            if (!canvas) {
                canvas = tpen.manifest.sequences[0].canvases[0];
                console.warn("Folio was not found in Manifest. Loading first page...");
            }
            loadTranscriptionCanvas(canvas, "");
        }
    } else {
    // failed to draw, no Canvas selected
    }
}

/* Load the interface to the first page of the manifest. */
function firstFolio () {
    tpen.screen.currentFolio = 0;
    redraw("");
}

/* Load the interface to the last page of the manifest. */
function lastFolio(){
    tpen.screen.currentFolio = tpen.manifest.sequences[0].canvases.length - 1;
    redraw("");
}
/* Load the interface to the previous page from the one you are on. */
function previousFolio (parsing) {
    if (tpen.screen.currentFolio === 0) {
        throw new Error("You are already on the first page.");
    }
    tpen.screen.currentFolio--;
    redraw("");
}

/* Load the interface to the next page from the one you are on. */
function nextFolio (parsing) {
    if (tpen.screen.currentFolio >= tpen.manifest.sequences[0].canvases.length - 1) {
        throw new Error("That page is beyond the last page.");
    }
    tpen.screen.currentFolio++;
    redraw("");
}

/** Test if a given string can be parsed into a valid JSON object.
 * @param str  A string
 * @return bool
*/
function isJSON(str) {
    if (typeof str === "object") {
        return true;
    }
    else {
        try {
            JSON.parse(str);
            return true;
        }
        catch (e) {
            return false;
        }
    }
    return false;
};

/* Populate the split page for Text Preview.  These are the transcription lines' text. */
function createPreviewPages(){
    $(".previewPage").remove();
        var pageLabel = "";
        var transcriptionFolios = tpen.manifest.sequences[0].canvases;
        for (var i = 0; i < transcriptionFolios.length; i++) {
            var currentFolioToUse = transcriptionFolios[i];
            pageLabel = currentFolioToUse.label;
            var currentPage = "";
            if (i === tpen.screen.currentFolio) {
                currentPage = "currentPage";
            }
            if (currentFolioToUse.otherContent && currentFolioToUse.otherContent.length > 0){
                var allAnnoLists = currentFolioToUse.otherContent;
                for(var j=0; j<allAnnoLists.length; j++){
                    var thisList = allAnnoLists[j];
                    makePreviewPage(thisList, pageLabel, currentPage, i, j, allAnnoLists.length);
                }
            }
            else{
                console.warn("otherContent was null or empty, passing an empty array of lines");
                populatePreview([], pageLabel, currentPage, 0);
            }
        }
}

function makePreviewPage(thisList, pageLabel, currentPage, i, j, l){
    $.get(thisList,function(data){
        if(data.proj == tpen.project.id){
            var linesForThisProject = data.resources;
            console.log("found the right one");
            populatePreview(linesForThisProject, pageLabel, currentPage, i);
        }
        else if(j == l){ //we did not find the proper annotation list, send this off to create an empty page
            console.log("Did not find lines for this project");
            populatePreview(linesForThisProject, pageLabel, currentPage, i);
        }
    });
}

/* Gather the annotations for a canvas and populate the preview interface with them. */
function gatherAndPopulate(currentOn, pageLabel, currentPage, i){
    var annosURL = "getAnno";
    var properties = {"@type": "sc:AnnotationList", "on" : currentOn};
    var paramOBJ = {"content": JSON.stringify(properties)};
    $.post(annosURL, paramOBJ, function(annoList){
        annoList = JSON.parse(annoList);
    });
}

/* Populate the line preview interface. */
function populatePreview(lines, pageLabel, currentPage, order){
    var letterIndex = 0;
    var letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var previewPage = $('<div order="' + order + '" class="previewPage"><span class="previewFolioNumber">' + pageLabel + '</span></div>');
    if (lines.length === 0) {
        previewPage = $('<div order="' + order + '" class="previewPage">'
        + '<span class="previewFolioNumber">'
        + pageLabel + '</span><br>No Lines</div>');
    }
    var num = 0;
    for (var j = 0; j < lines.length; j++){
        num++;
        var col = letters[letterIndex];
        var currentLine = lines[j].on;
        var currentLineXYWH = currentLine.slice(currentLine.indexOf("#xywh=") + 6);
        currentLineXYWH = currentLineXYWH.split(",");
        var currentLineX = currentLineXYWH[0];
        var line = lines[j];
        var lineID = line["@id"];
        var lineText = line.resource["cnt:chars"];
        if (j >= 1){
            var lastLine = lines[j - 1].on;
            var lastLineXYWH = lastLine.slice(lastLine.indexOf("#xywh=") + 6);
            lastLineXYWH = lastLineXYWH.split(",");
            var lastLineX = lastLineXYWH[0];
            var abs = Math.abs(parseInt(lastLineX) - parseInt(currentLineX));
            if (abs > 0){
                letterIndex++;
                num = 0;
            }
        }
        var previewLine = $('<div class="previewLine" data-lineNumber="' + j + '">'
            + '<span class="previewLineNumber" lineserverid="' + lineID + '" data-lineNumber="' + j + '"  data-column="' + col + '"  data-lineOfColumn="' + j + '">'
            + col + '' + num + '</span>'
            + '<span class="previewText ' + currentPage + '">' + lineText + '<span class="previewLinebreak"></span></span>'
            + '<span class="previewNotes" contentEditable="(permitModify||isMember)" ></span></div>');
        previewPage.append(previewLine);
    }
    $("#previewDiv").append(previewPage);
}

function populateSpecialCharacters(specialCharacters){
    var specialCharacters = tpen.project.specialChars;
    var speCharactersInOrder = new Array(specialCharacters.length);
    for (var char = 0; char < specialCharacters.length; char++){
        var thisChar = specialCharacters[char];
        if (thisChar == ""){ }
        else {
            var keyVal = thisChar.key;
            var position2 = parseInt(thisChar.position);
            var newCharacter = "<option class='character'>&#" + keyVal + ";</option>";
            if (position2 - 1 >= 0 && (position2 - 1) < specialCharacters.length) {
                speCharactersInOrder[position2 - 1] = newCharacter;
            }
        }
    }
    for (var char2 = 0; char2 < speCharactersInOrder.length; char2++){
        var textButton = speCharactersInOrder[char2];
        var button1 = $(textButton);
        $(".specialCharacters").append(button1);
    }
}

function populateXML(){
    var xmlTags = tpen.project.xml;
    var tagsInOrder = [];
    //TODO make sure this respects xmlTags.position order
    for (var tagIndex = 0; tagIndex < xmlTags.length; tagIndex++){
        var newTagBtn = "";
        if(xmlTags[tagIndex].tag && xmlTags[tagIndex].tag!== "" && xmlTags[tagIndex].tag !== " "){
            newTagBtn = "<option class='xmlTag'>"+xmlTags[tagIndex].tag+"</option>";
            var button = $(newTagBtn);
            $(".xmlTags").append(button);
        }
    }
}

function setTPENObjectData(data){
    if(data.project){
        if(data.projectTool){
            tpen.project.tools = JSON.parse(data.projectTool);
        }
        if(data.ls_u){
            tpen.project.user_list = JSON.parse(data.ls_u);
        }
        if(data.ls_leader){
            tpen.project.leaders = JSON.parse(data.ls_leader);
        }
        if(data.projectButtons){
            tpen.project.specialChars = JSON.parse(data.projectButtons);
        }
        if(data.ls_hk){
            tpen.project.hotkeys = JSON.parse(data.ls_hk);
        }
        if(data.xml){
            tpen.project.xml = JSON.parse(data.xml);
        }
        if(data.projper){
            tpen.project.permissions = JSON.parse(data.projper);
        }
        if(data.metadata){
            tpen.project.metadata = JSON.parse(data.metadata);
        }
        if(data.ls_fs){
            tpen.project.folios = JSON.parse(data.ls_fs);
        }
        if(data.projectName){
            tpen.project.projectName = data.projectName;
        }
        if(data.project.projectID){
            tpen.project.id = parseInt(data.project.projectID);
        }
        if(data.project.groupID){
            tpen.project.groupID = parseInt(data.project.groupID);
        }
        if(data.project.linebreakSymbol){
            tpen.project.linebreakSymbol = data.project.linebreakSymbol;
        }
        if(data.project.projectImageBounding){
            tpen.project.projectImageBounding = data.project.projectImageBounding;
        }
        if(data.project.linebreakCharacterLimit){
            tpen.project.linebreakCharacterLimit = parseInt(data.project.linebreakCharacterLimit);
        }
    }
    
    if(data.manifest){
        tpen.manifest = JSON.parse(data.manifest);
    }
    
    if(data.cuser){
        tpen.user.UID = parseInt(data.cuser);
    }
    
    var count = 0;
    var length = tpen.project.leaders.length;
    $.each(tpen.project.leaders, function(){
        count++;
        if (this.UID === parseInt(data.cuser)){
            if(this.fname){
                tpen.user.fname = this.fname;
            }
            if(this.lname){
                tpen.user.lname = this.lname;
            }
            if(this.openID){
                tpen.user.openID = this.openID;
            }
            tpen.user.isAdmin = true;
            //tpen.user.UID = parseInt(this.UID);
        }
        else if(count == length){ //we did not find this user in the list of leaders.
            console.warn("Not an admin");
        }
    });   
}

/*
* Load the transcription from the text in the text area.
* This is the first thing called when coming into 
* transcription.html when it recognizes a projectID in the URL.
*/
function loadTranscription(pid, tool){
    //Object validation here.
    var projectID = 0; 
    var userTranscription = $('#transcriptionText').val();
    var pageToLoad = getURLVariable("p");
    var canvasIndex = 0;
    
    $("#transTemplateLoading").show();
    if (pid || $.isNumeric(userTranscription)){
        //The user can put the project ID in directly and a call will be made to newberry proper to grab it.
        projectID = pid || userTranscription;
        tpen.project.id = projectID; //this must be set or the canvas won't draw
        var aBar = document.location.href;
        var toAddressBar = aBar+"?projectID=" + projectID;
        if(aBar.indexOf("projectID=") === -1){
            window.history.pushState("", "T-PEN Transcription", toAddressBar);
        }
        var url = "getProjectTPENServlet?projectID=" + projectID;
        $.ajax({
            url: url,
            type:"GET",
            success: function(activeProject){
                var url = "";
                setTPENObjectData(activeProject);
                var userToolsAvailable = activeProject.userTool;
                var projectPermissions = JSON.parse(activeProject.projper);
                activateUserTools(userToolsAvailable, projectPermissions);
                if (tpen.manifest.sequences[0] !== undefined
                    && tpen.manifest.sequences[0].canvases !== undefined
                    && tpen.manifest.sequences[0].canvases.length > 0)
                {
                    transcriptionFolios = tpen.manifest.sequences[0].canvases;
                    if(pageToLoad){
                        $.each(tpen.project.folios, function(i){
                            if(this.folioNumber === parseInt(pageToLoad)){
                                canvasIndex = i;
                                tpen.screen.currentFolio = i;
                                return true;
                            }
                        });
                    }
                    scrubFolios();
                    var count = 1;
                    $.each(transcriptionFolios, function(){
                        $("#pageJump").append("<option folioNum='" + count
                            + "' class='folioJump' val='" + this.label + "'>"
                            + this.label + "</option>");
                        $("#compareJump").append("<option class='compareJump' folioNum='"
                            + count + "' val='" + this.label + "'>"
                            + this.label + "</option>");
                        count++;
                        if (this.otherContent){
                            if (this.otherContent.length > 0){
                                // all's well
                            }
                            else {
                            //otherContent was empty (IIIF says otherContent should
                            //have URI's to AnnotationLists).  We will check the
                            //store for these lists still.
                            }
                        }
                        else {
                            // no list at all, let's create a spot
                            this.otherContent = [];
                        }
                    });
                    loadTranscriptionCanvas(transcriptionFolios[canvasIndex], "", tool);
                    var projectTitle = tpen.manifest.label;
                    $("#trimTitle").text(projectTitle);
                    $("#trimTitle").attr("title", projectTitle);
                    $('#transcriptionTemplate').css("display", "inline-block");
                    $('#setTranscriptionObjectArea').hide();
                    $(".instructions").hide();
                    $(".hideme").hide();
                    //load Iframes after user check and project information data call
                    loadIframes();
                }
                else {
                    throw new Error("This transcription object is malformed. No canvas sequence is defined.");
                }
                if(tpen.project.tools){
                    $.each(tpen.project.tools, function(){
                        var splitHeight = window.innerHeight + "px";
                        var toolLabel = this.name;
                        var toolSource = this.url;
                        var splitTool = $('<div toolName="' + toolLabel
                            + '" class="split iTool"><button class="fullScreenTrans">'
                            + 'Full Screen Transcription</button></div>');
                        var splitToolIframe = $('<iframe style="height:' + splitHeight
                            + ';" src="' + toolSource + '"></iframe>');
                        var splitToolSelector = $('<option splitter="' + toolLabel
                            + '" class="splitTool">' + toolLabel + '</option>');
                        splitTool.append(splitToolIframe);
                        $("#splitScreenTools")
                            .append(splitToolSelector);
                        $(".iTool:last")
                            .after(splitTool);
                    });
                    }
                    if (tpen.project.specialChars) {
                        populateSpecialCharacters();
                    }
                    if (tpen.project.xml) {
                        populateXML();
                    }
            },
            error: function(jqXHR, error, errorThrown) {
                if (jqXHR.status && jqXHR.status === 404){
                   $(".turnMsg").html("Could not find this project.  Check the project ID. Refresh the page to try again or contact your T-PEN admin.");
                   $(".transLoader").find("img").attr("src", "../TPEN28/images/missingImage.png");
                }
                else {
                    $(".turnMsg").html("This project appears to be broken.  Refresh the page to try to load it again or contact your T-PEN admin.");
                    $(".transLoader").find("img").attr("src", "../TPEN28/images/BrokenBook01.jpg");
                }
                
                //load Iframes after user check and project information data call
                loadIframes();
            }
        });
        
    }
    else if (isJSON(userTranscription)){
        tpen.manifest = userTranscription = JSON.parse(userTranscription);
        if (userTranscription.sequences[0] !== undefined
            && userTranscription.sequences[0].canvases !== undefined
            && userTranscription.sequences[0].canvases.length > 0)
        {
            var transcriptionFolios = userTranscription.sequences[0].canvases;
            scrubFolios();
            var count = 1;
            $.each(transcriptionFolios, function(){
                $("#pageJump").append("<option folioNum='" + count
                    + "' class='folioJump' val='" + this.label + "'>"
                    + this.label + "</option>");
                $("#compareJump").append("<option class='compareJump' folioNum='"
                    + count + "' val='" + this.label + "'>"
                    + this.label + "</option>");
                count++;
                if (this.otherContent){
                    if (this.otherContent.length > 0){
                        // all's well
                    }
                    else {
                        //otherContent was empty (IIIF says otherContent should
                        //have URI's to AnnotationLists).  We will check the
                        //store for these lists still.
                    }
                }
                else {
                    // no property at all, create one for storing new annotations
                    this.otherContent=[];
                }
            });
            loadTranscriptionCanvas(transcriptionFolios[canvasIndex], "", tool);
            var projectTitle = userTranscription.label;
            $("#trimTitle").html(projectTitle);
            $("#trimTitle").attr("title", projectTitle);
            $('#transcriptionTemplate').css("display", "inline-block");
            $('#setTranscriptionObjectArea').hide();
            $(".instructions").hide();
            $(".hideme").hide();
        }
        else {
            throw new Error("This is a valid JSON object, but it cannot be read as a transcription object.");
        }
        //load Iframes after user check and project information data call
        loadIframes();
    }
    else if (userTranscription.indexOf("http://") >= 0 || userTranscription.indexOf("https://") >= 0) {
        //TODO: allow users to include the p variable and load a page?
        var localProject = false;
        if (userTranscription.indexOf("/TPEN28/project") > - 1 || userTranscription.indexOf("/TPEN28/manifest") > - 1){
//            if (userTranscription.indexOf("t-pen.org") > - 1){
//                localProject = false;
//                projectID = 0; //This way, it will not grab the t-pen project id.
//            }
//            else {
            localProject = true; //Well, probably anyway.  I forsee this being an issue like with t-pen.
            //TODO: this URL exists in more ways now (/manifest/PID/manifest.json)
            if(userTranscription.indexOf("/TPEN28/project") > - 1){
                projectID = parseInt(userTranscription.substring(userTranscription.lastIndexOf('/project/') + 9));
            }
            else if(userTranscription.indexOf("/TPEN28/manifest") > - 1){
                projectID = parseInt(userTranscription.substring(userTranscription.lastIndexOf('/manifest/') + 10).replace("/manifest.json", ""));
            }
            
           // }
        }
        else {
            projectID = 0;
        }
        
        if (localProject){
            //get project info first, get manifest out of it, populate
            var aBar = document.location.href;
            var toAddressBar = aBar+"?projectID=" + projectID;
            if(aBar.indexOf("projectID=") === -1){
                window.history.pushState("", "T-PEN Transcription", toAddressBar);
            }
            var url = "getProjectTPENServlet?projectID=" + projectID;
            $.ajax({
                url: url,
                type:"GET",
                success: function(activeProject){
                    tpen.project.id = projectID; //this must be set or the canvas won't draw
                    setTPENObjectData(activeProject);
                    var userToolsAvailable = activeProject.userTool;
                    var projectPermissions = JSON.parse(activeProject.projper);
                    activateUserTools(userToolsAvailable, projectPermissions);
                    if (tpen.manifest.sequences[0] !== undefined
                        && tpen.manifest.sequences[0].canvases !== undefined
                        && tpen.manifest.sequences[0].canvases.length > 0)
                    {
                        transcriptionFolios = tpen.manifest.sequences[0].canvases;
                        //Could allow them to load a specific page with a local project URL.  
//                        if(pageToLoad){
//                            $.each(tpen.project.folios, function(i){
//                                if(this == pageToLoad){
//                                    canvasIndex = i;
//                                }
//                            });
//                        }
                        scrubFolios();
                        var count = 1;
                        $.each(transcriptionFolios, function(){
                            $("#pageJump").append("<option folioNum='" + count
                                + "' class='folioJump' val='" + this.label + "'>"
                                + this.label + "</option>");
                            $("#compareJump").append("<option class='compareJump' folioNum='"
                                + count + "' val='" + this.label + "'>"
                                + this.label + "</option>");
                            count++;
                            if (this.otherContent){
                                if (this.otherContent.length > 0){
                                    // all's well
                                }
                                else {
                                //otherContent was empty (IIIF says otherContent should
                                //have URI's to AnnotationLists).  We will check the
                                //store for these lists still.
                                }
                            }
                            else {
                                // no list at all, let's create a spot
                                this.otherContent = [];
                            }
                        });
                        loadTranscriptionCanvas(transcriptionFolios[canvasIndex], "", tool);
                        var projectTitle = tpen.manifest.label;
                        $("#trimTitle").text(projectTitle);
                        $("#trimTitle").attr("title", projectTitle);
                        $('#transcriptionTemplate').css("display", "inline-block");
                        $('#setTranscriptionObjectArea').hide();
                        $(".instructions").hide();
                        $(".hideme").hide();
                        //load Iframes after user check and project information data call
                        loadIframes();
                    }
                    else {
                        throw new Error("This transcription object is malformed. No canvas sequence is defined.");
                    }
                    if(tpen.project.tools){
                        $.each(tpen.project.tools, function(){
                            var splitHeight = window.innerHeight + "px";
                            var toolLabel = this.name;
                            var toolSource = this.url;
                            var splitTool = $('<div toolName="' + toolLabel
                                + '" class="split iTool"><button class="fullScreenTrans">'
                                + 'Full Screen Transcription</button></div>');
                            var splitToolIframe = $('<iframe style="height:' + splitHeight
                                + ';" src="' + toolSource + '"></iframe>');
                            var splitToolSelector = $('<option splitter="' + toolLabel
                                + '" class="splitTool">' + toolLabel + '</option>');
                            splitTool.append(splitToolIframe);
                            $("#splitScreenTools")
                                .append(splitToolSelector);
                            $(".iTool:last")
                                .after(splitTool);
                        });
                        }
                        if (tpen.project.specialChars) {
                            populateSpecialCharacters();
                        }
                        if (tpen.project.xml) {
                            populateXML();
                        }
                },
                error: function(jqXHR, error, errorThrown) {
                    if (jqXHR.status && jqXHR.status === 404){
                        $(".turnMsg").html("Could not find this project.  Check the project ID.  Refresh the page to try again or contact your T-PEN admin.");
                        $(".transLoader").find("img").attr("src", "..TPEN28/images/missingImage.png");
                     }
                     else {
                         $(".turnMsg").html("This project appears to be broken.  Refresh the page to try to load it again or contact your T-PEN admin.");
                         $(".transLoader").find("img").attr("src", "..TPEN28/images/BrokenBook01.jpg");
                     }
                    //load Iframes after user check and project information data call
                    loadIframes();
                }
            });
        }
        else {
        //it is not a local project, so just grab the url that was input and request the manifest.
        var url = userTranscription;
        tpen.project.id = -1; //This means it is not a T-PEN projec5t, but rather a manifest from another source.  
        $.ajax({
            url: url,
            success: function(projectData){
                if (projectData.sequences[0] !== undefined
                    && projectData.sequences[0].canvases !== undefined
                    && projectData.sequences[0].canvases.length > 0){
                    transcriptionFolios = projectData.sequences[0].canvases;
                    scrubFolios();
                    var count = 1;
                    $.each(transcriptionFolios, function(){
                        $("#pageJump").append("<option folioNum='" + count
                            + "' class='folioJump' val='" + this.label + "'>"
                            + this.label + "</option>");
                        $("#compareJump").append("<option class='compareJump' folioNum='"
                            + count + "' val='" + this.label + "'>"
                            + this.label + "</option>");
                        count++;
                        if (this.otherContent){
                            if (this.otherContent.length > 0){
                                // all's well
                            }
                            else {
                                //otherContent was empty (IIIF says otherContent
                                //should have URI's to AnnotationLists).
                                console.warn("`otherContent` exists, but has no content.");
                            }
                        }
                        else {
                            console.warn("`otherContent` does not exist in this Manifest.");
                        }
                    });
                    loadTranscriptionCanvas(transcriptionFolios[canvasIndex], "", tool);
                    var projectTitle = projectData.label;
                    $("#trimTitle").html(projectTitle);
                    $("#trimTitle").attr("title", projectTitle); $('#transcriptionTemplate').css("display", "inline-block");
                    $('#setTranscriptionObjectArea').hide();
                    $(".instructions").hide();
                    $(".hideme").hide();
                }
                else {
                    throw new Error("Malformed transcription object. There is no canvas sequence defined.");
                }
                //load Iframes after user check and project information data call
                loadIframes();
            },
            error: function(jqXHR, error, errorThrown) {
                if (jqXHR.status && jqXHR.status > 400){
                    alert(jqXHR.responseText);
                }
                else {
                    throw error;
                }
                //load Iframes after user check and project information data call
                loadIframes();
            }
        });
    }
    }
    else {
        throw new Error("The input was invalid.");
    }
}

function activateTool(tool){
	// TODO: Include other tools here.
    if(tool === "parsing"){
        if(tpen.user.isAdmin){
            $("#parsingBtn").click();
            tpen.screen.liveTool = "parsing";
        }
    }
}

/*
 * 
 * @param {type} tools
 * Looks at the array of user tools available passed in as project data and makes those options visible.
 * 
 */
function activateUserTools(tools, permissions){
    if(tpen.user.isAdmin || $.inArray("parsing", tools) > -1 || permissions.allow_public_modify || permissions.allow_public_modify_line_parsing){
        $("#parsingBtn").show();
        tpen.user.isAdmin = true;
        var message = $('<span>This canvas has no lines. If you would like to create lines</span>'
            + '<span style="color: blue;" onclick="hideWorkspaceForParsing()">click here</span>.'
            + 'Otherwise, you can <span style="color: red;" onclick="$(\'#noLineWarning\').hide()">'
            + 'dismiss this message</span>.');
        $("#noLineConfirmation").empty();
        $("#noLineConfirmation").append(message);
    }
    if($.inArray("linebreak", tools) > -1){
        $(".splitTool[splitter='linebreak']").show();
    }
    if($.inArray("history", tools) > -1){
        $(".splitTool[splitter='history']").show();
    }
    if($.inArray("preview", tools) > -1){
        $(".splitTool[splitter='preview']").show();
    }
    if($.inArray("abbreviation", tools) > -1){
        $(".splitTool[splitter='abbreviation']").show();
    }
    if($.inArray("compare", tools) > -1){
        $(".splitTool[splitter='compare']").show();
    }
}

/*
 * Load a canvas from the manifest to the transcription interface.
 */
function loadTranscriptionCanvas(canvasObj, parsing, tool){
    var noLines = true;
    var canvasAnnoList = "";
    $("#imgTop, #imgBottom").css("height", "400px");
    $("#imgTop img, #imgBottom img").css("height", "400px");
    $("#imgTop img, #imgBottom img").css("width", "auto");
    $("#prevColLine").html("**");
    $("#currentColLine").html("**");
    $('.transcriptionImage').attr('src', "images/loading2.gif"); //background loader if there is a hang time waiting for image
    $('.lineColIndicator').remove();
    $(".transcriptlet").remove();
    var pageTitle = canvasObj.label;
    $("#trimPage").html(pageTitle);
    $("#trimPage").attr("title", pageTitle);
    $('#transcriptionTemplate').css("display", "inline-block");
    $("#parsingBtn").css("box-shadow", "none");
    $("#parsingButton").removeAttr('disabled');
    $(".lineColIndicator").css({
        "box-shadow": "rgba(255, 255, 255, 0.4)",
        "border": "1px solid rgb(255, 255, 255)"
    });
    $(".lineColOnLine").css({
        "border-left": "1px solid rgba(255, 255, 255, 0.2);",
        "color": "rgb(255, 255, 255)"
    });
    //Move up all image annos
    var cnt = - 1;
    if (canvasObj.images[0].resource['@id'] !== undefined
        && canvasObj.images[0].resource['@id'] !== ""){ //Only one image
        var image = new Image();
        $(image)
        .on("load", function() {
            $("#imgTop, #imgTop img, #imgBottom img, #imgBottom, #transcriptionCanvas").css("height", "auto");
            $("#imgTop img, #imgBottom img").css("width", "100%");
            $("#imgBottom").css("height", "inherit");
            $('.transcriptionImage').attr('src', canvasObj.images[0].resource['@id'].replace('amp;', ''));
            $("#fullPageImg").attr("src", canvasObj.images[0].resource['@id'].replace('amp;', ''));
            originalCanvasHeight2 = $("#imgTop img").height();
            originalCanvasWidth2 = $("#imgTop img").width();
            drawLinesToCanvas(canvasObj, parsing, tool);
            $("#transcriptionCanvas").attr("canvasid", canvasObj["@id"]);
            $("#transcriptionCanvas").attr("annoList", canvasAnnoList);
            $("#parseOptions").find(".tpenButton").removeAttr("disabled");
            $("#parsingBtn").removeAttr("disabled");
        })
        .on("error", function(){
            var image2 = new Image();
            $(image2)
            .on("load", function(){
                $("#noLineWarning").hide();
                $("#imgTop, #imgTop img, #imgBottom img, #imgBottom, #transcriptionCanvas").css("height", "auto");
                $("#imgTop img, #imgBottom img").css("width", "100%");
                $('.transcriptionImage').attr('src', "images/missingImage.png");
                $("#fullPageImg").attr("src", "images/missingImage.png");
                $('#transcriptionCanvas').css('height', $("#imgTop img").height() + "px");
                $('.lineColIndicatorArea').css('height', $("#imgTop img").height() + "px");
                $("#imgTop").css("height", "0%");
                $("#imgBottom img").css("top", "0px");
                $("#imgBottom").css("height", "inherit");
                $("#parsingButton").attr("disabled", "disabled");
                alert("No image for this canvas or it could not be resolved.  Not drawing lines.");
                $("#parseOptions").find(".tpenButton").attr("disabled", "disabled");
                $("#parsingBtn").attr("disabled", "disabled");
            })
            .attr("src", "images/missingImage.png");
        })
        .attr("src", canvasObj.images[0].resource['@id'].replace('amp;', ''));
    }
    else {
        $('.transcriptionImage').attr('src', "images/missingImage.png");
        throw Error("The canvas is malformed.  No 'images' field in canvas object or images:[0]['@id'] does not exist.  Cannot draw lines.");
    }
    $(".previewText").removeClass("currentPage");
    $.each($("#previewDiv").children(".previewPage:eq(" + (parseInt(tpen.screen.currentFolio) - 1) + ")").find(".previewLine"), function(){
        $(this).find('.previewText').addClass("currentPage");
    });
    createPreviewPages(); //each time you load a canvas to the screen with all of its updates, remake the preview pages.
}

/*
 * @paran canvasObj  A canvas object to extract transcription lines from and draw to the interface.
 * @param parsing boolean if parsing is live tool
 */
function drawLinesToCanvas(canvasObj, parsing, tool){
    var lines = [];
    var currentFolio = parseInt(tpen.screen.currentFolio);
    if (canvasObj.resources !== undefined
        && canvasObj.resources.length > 0){
        for (var i = 0; i < canvasObj.resources.length; i++){
            if (isJSON(canvasObj.resources[i])){   // it is directly an annotation
                lines.push(canvasObj.resources[i]);
            }
        }
        linesToScreen(lines, tool);
    }
    else {
        // we have the anno list for this canvas (potentially), so query for it.
        // If not found, then consider this an empty canvas.
        var annosURL = "getAnno";
        var onValue = canvasObj["@id"];
        var properties = {"@type": "sc:AnnotationList", "on" : onValue, proj:tpen.project.id};
        var paramOBJ = {"content": JSON.stringify(properties)};
        $.post(annosURL, paramOBJ, function(annoList){
            if (!tpen.manifest.sequences[0].canvases[currentFolio]){
                throw new Error("Missing canvas:" +currentFolio);
            }
            if(!tpen.manifest.sequences[0].canvases[currentFolio].otherContent){
                tpen.manifest.sequences[0].canvases[currentFolio].otherContent = new Array();
            }
            //FIXME: The line below throws a JSON error sometimes, especially on first load.  
            var annoList = tpen.manifest.sequences[0].canvases[currentFolio].otherContent = tpen.manifest.sequences[0].canvases[currentFolio].otherContent.concat(JSON.parse(annoList));
            var currentList = {};
            
            if (annoList.length > 0){
                // Scrub resolved lists that are already present.
                tpen.screen.currentAnnoListID = annoList[0]; //There should always just be one that matches because of proj, default to first in array if more 
                lines = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], true, parsing, tool);
//                $.each(annoList, function(index){
//                    if (typeof annoList[index] === "string"){
//                        // annoList is most likely an array of @ids.
//                        for (var i = annoList.length - 1; i >= 0; i--) {
//                            if (annoList[index] === annoList[i]["@id"]){
//                                // found the dereferenced object, wipe this
//                                delete annoList[index]; // above this scope
//                                break;
//                            }
//                        }
//                        lines = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio]);
//                    }
//                    else if (this.resources) {
//                        lines = this.resources;
//                        currentList = this;
//                    } else {
//                    	console.warn("Multiple AnnotationLists found, but '" + this + "' was not recognized.");
//                        delete annoList[index]; // above this scope
//                    }
//                });
//                annoList = tpen.manifest.sequences[0].canvases[currentFolio].otherContent
//                = annoList.filter(function(){ // clear out empty items
//                	return true;
//                });

                
            }
            else {
                // couldnt get list.  one should always exist, even if empty.
                // We will say no list and changes will be stored locally to the canvas.
                if (parsing !== "parsing") {
                    $("#noLineWarning").show();
                }
                $("#transTemplateLoading").hide();
                $("#transcriptionTemplate").show();
                $('#transcriptionCanvas').css('height', $("#imgTop img").height() + "px");
                $('.lineColIndicatorArea').css('height', $("#imgTop img").height() + "px");
                $("#imgTop").css("height", "0%");
                $("#imgBottom img").css("top", "0px");
                $("#imgBottom").css("height", "inherit");
                $("#parsingBtn").css("box-shadow", "0px 0px 6px 5px yellow");
            }
        });
    }
}

/* Take line data, turn it into HTML elements and put them to the DOM */
function linesToScreen(lines, tool){
    $("#noLineWarning").hide();
    var letterIndex = 0;
    var letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    letters = letters.split("");
    var update = true;
    if ($("#parsingDiv").is(":visible")){
        update = false; // TODO: Is this just a tpen.screen.liveTool check?
    }
    var thisContent = "";
    var thisPlaceholder = "Enter a line transcription";
    var counter = -1;
    var colCounter = 0;
    var image = $('#imgTop img');
    var theHeight = image.height();
    var theWidth = image.width();
    $('#transcriptionCanvas').css('height', originalCanvasHeight2 + "px");
    $('.lineColIndicatorArea').css('height', originalCanvasHeight2 + "px");
    var ratio = 0;
    //should be the same as originalCanvasWidth2/originalCanvasHeight2
    ratio = theWidth / theHeight;
    for (var i = 0; i < lines.length; i++){
        var line = lines[i];
        var lastLine = {};
        var col = letters[letterIndex];
        if (i > 0)lastLine = lines[i - 1];
        var lastLineX = 10000;
        var lastLineWidth = - 1;
        var lastLineTop = - 2;
        var lastLineHeight = - 2;
        var x, y, w, h = 0;
        var XYWHarray = [x, y, w, h];
        var lineURL = "";
        var lineID = - 1;
        if (line.on !== undefined){
            lineURL = line.on;
        }
        else {
            //ERROR.  malformed line.
            update = false;
        }
        if (line["@id"] !== undefined && line["@id"] !== ""){
            lineID = line['@id'];
        }
        else {
            //ERROR.  Malformed line.
            update = false;
        }
        thisContent = "";
        if (lineURL.indexOf('#') > - 1){ //string must contain this to be valid
            var XYWHsubstring = lineURL.substring(lineURL.lastIndexOf('#' + 1)); //xywh = 'x,y,w,h'
            if (lastLine.on){ //won't be true for first line
                var xywh = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")
                lastLineX = xywh[0];
                lastLineWidth = xywh[2];
                lastLineTop = xywh[1];
                lastLineHeight = xywh[3];
            }
            else if (i === 0 && lines.length > 1){ // Check for the variance with the first line
                lastLine = lines[0];
                if (lastLine.on){
                    lastLineX = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[0];
                    lastLineWidth = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[2];
                    lastLineTop = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[1];
                    lastLineHeight = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[3];
                }
            }
            if (XYWHsubstring.indexOf('xywh=') > - 1){ //string must contain this to be valid
                var numberArray = XYWHsubstring.substring(lineURL.lastIndexOf('xywh=') + 5).split(',');
                if (parseInt(lastLineTop) + parseInt(lastLineHeight) !== numberArray[1]){
                //check for slight variance in top position.  Happens because of rounding percentage math that gets pixels to be an integer.
                    var num1 = parseInt(lastLineTop) + parseInt(lastLineHeight);
                    if (Math.abs(num1 - numberArray[1]) <= 4 && Math.abs(num1 - numberArray[1]) !== 0){
                        numberArray[1] = num1;
                        var newString = numberArray[0] + "," + num1 + "," + numberArray[2] + "," + numberArray[3];
                        if (i > 0){
                        //to make the change cascade to the rest of the lines,
                        // we actually have to update the #xywh of the current
                        // line with the new value for y.
                            var lineOn = lineURL;
                            var index = lineOn.indexOf("#xywh=") + 6;
                            var newLineOn = lineOn.substr(0, index) + newString + lineOn.substr(index + newString.length);
                            lines[i].on = newLineOn;
                        }
                    }
                }
                if (numberArray.length === 4){ // string must have all 4 to be valid
                    x = numberArray[0];
                    w = numberArray[2];
                    if (lastLineX !== x){
                        //check if the last line's x value is equal to this
                        // line's x value (means same column)
                        if (Math.abs(x - lastLineX) <= 3){
                            //allow a 3 pixel  variance and fix this variance when necessary...
                        //align them, call them the same Column.
                /*
                 * This is a consequence of #xywh for a resource needing to be an integer.  When I calculate its integer position off of
                 * percentages, it is often a float and I have to round to write back.  This can cause a 1 or 2 pixel discrenpency, which I account
                 * for here.  There may be better ways of handling this, but this is a good solution for now.
                 */
                            if (lastLineWidth !== w){ //within "same" column (based on 3px variance).  Check the width
                                if (Math.abs(w - lastLineWidth) <= 5){
                                    // If the width of the line is within five pixels,
                                    // automatically make the width equal to the last line's width.

                                    //align them, call them the same Column.
                            /*
                             * This is a consequence of #xywh for a resource needing to be an integer.  When I calculate its intger position off of
                             * percentages, it is often a float and I have to round to write back.  This can cause a 1 or 2 pixel discrenpency, which I account
                             * for here.  There may be better ways of handling this, but this is a good solution for now.
                             */
                                    w = lastLineWidth;
                                        numberArray[2] = w;
                                }
                            }
                            x = lastLineX;
                            numberArray[0] = x;
                        }
                        else { //we are in a new column, column indicator needs to increase.
                            letterIndex++;
                            col = letters[letterIndex];
                            colCounter = 0; //Reset line counter so that when the column changes the line# restarts
                        }
                    }
                    else {
                        // X value matches, we are in the same column and don't
                        // have to account for any variance or update the array.
                        // Still check for slight width variance..
                        if (lastLineWidth !== w){
                            if (Math.abs(w - lastLineWidth) <= 5){ //within 5 pixels...
                                //align them, call them the same Column.
                                /* This is a consequence of #xywh for a resource needing to be an integer.  When I calculate its intger position off of
* percentages, it is often a float and I have to round to write back.  This can cause a 1 or 2 pixel discrenpency, which I account
* for here.  There may be better ways of handling this, but this is a good solution for now. */
                                w = lastLineWidth;
                                numberArray[2] = w;
                            }
                        }
                    }
                    y = numberArray[1];
                    h = numberArray[3];
                    XYWHarray = [x, y, w, h];
                }
                else {
                    //ERROR! Malformed line
                    update = false;
                }
            }
            else {
                //ERROR! Malformed line
                update = false;
            }
        }
        else {
            //ERROR!  Malformed line.
            update = false;
        }
        if (line.resource['cnt:chars'] !== undefined
            && line.resource['cnt:chars'] !== "") {
            thisContent = line.resource['cnt:chars'];
        }
        counter++;
        var newAnno = $('<div id="transcriptlet_' + counter + '" col="' + col
            + '" colLineNum="' + colCounter + '" lineID="' + counter
            + '" lineserverid="' + lineID + '" class="transcriptlet" data-answer="'
            + thisContent + '"><textarea placeholder="' + thisPlaceholder + '">'
            + thisContent + '</textarea></div>');
        // 1000 is promised, 10 goes to %
        var left = parseFloat(XYWHarray[0]) / (10 * ratio);
        var top = parseFloat(XYWHarray[1]) / 10;
        var width = parseFloat(XYWHarray[2]) / (10 * ratio);
        var height = parseFloat(XYWHarray[3]) / 10;
        newAnno.attr({
            lineLeft: left,
                lineTop: top,
                lineWidth: width,
                lineHeight: height,
                counter: counter
        });
        colCounter++;
        $("#transcriptletArea").append(newAnno);
        var lineColumnIndicator = $("<div onclick='loadTranscriptlet(" + counter + ");' pair='" + col + "" + colCounter
            + "' lineserverid='" + lineID + "' lineID='" + counter + "' class='lineColIndicator' style='left:"
            + left + "%; top:" + top + "%; width:" + width + "%; height:" + height + "%;'><div class='lineColOnLine' >"
            + col + "" + colCounter + "</div></div>");
        var fullPageLineColumnIndicator = $("<div pair='" + col + "" + colCounter + "' lineserverid='" + lineID
            + "' lineID='" + counter + "' class='lineColIndicator fullP' onclick=\"updatePresentation($('#transcriptlet_" + counter + "'));\""
            + " style='left:" + left + "%; top:" + top + "%; width:" + width + "%; height:"
            + height + "%;'><div class='lineColOnLine' >" + col + "" + colCounter + "</div></div>");
        // TODO: add click event to update presentation
        // Make sure the col/line pair sits vertically in the middle of the outlined line.
        var lineHeight = theHeight * (height / 100) + "px";
        lineColumnIndicator.find('.lineColOnLine').attr("style", "line-height:" + lineHeight + ";");
        //Put to the DOM
        $(".lineColIndicatorArea").append(lineColumnIndicator);
        $("#fullPageSplitCanvas").append(fullPageLineColumnIndicator);
    }
    if (update && $(".transcriptlet").eq(0) !== undefined){
        updatePresentation($(".transcriptlet").eq(0));
        activateTool(tool);
    }
    // we want automatic updating for the lines these texareas correspond to.
    var typingTimer; //timer identifier
    $("textarea")
        .keydown(function(e){
        //user has begun typing, clear the wait for an update
        clearTimeout(typingTimer);
    })
        .keyup(function(e){
            var lineToUpdate = $(this).parent();
            clearTimeout(typingTimer);
            //when a user stops typing for 2 seconds, fire an update to get the new text.
            if(e.which !== 18){
                typingTimer = setTimeout(function(){
                    console.log("timer update");
                    updateLine(lineToUpdate, false, false);
                }, 2000);
            }
            
    });
}

/* Make the transcription interface focus to the transcriptlet passed in as the parameter. */
function updatePresentation(transcriptlet) {
    if (transcriptlet === undefined || transcriptlet === null){
        $("#imgTop").css("height", "0%");
        $("#imgBottom").css("height", "inherit");
        return false;
    }
    var nextCol = transcriptlet.attr("col");
    var nextLineNum = parseInt(transcriptlet.attr("collinenum")) + 1;
    var transcriptletBefore = $(transcriptlet.prev());
    var nextColLine = nextCol + "" + nextLineNum;
    $("#currentColLine").html(nextColLine);
    if (parseInt(nextLineNum) >= 1){
        if (transcriptletBefore.length > 0){
        var currentTranscriptletNum = parseInt(transcriptletBefore.attr("collinenum")) + 1;
            if (transcriptletBefore.length > 0){ }
            else{ }
            var prevLineCol = transcriptletBefore.attr("col");
            var prevLineText = transcriptletBefore.attr("data-answer");
            $("#prevColLine").html(prevLineCol + "" + currentTranscriptletNum);
            $("#captionsText").html((prevLineText.length && prevLineText) || "This line is not transcribed.");
        }
        else { //this is a problem
            $("#prevColLine").html("**");
            $("#captionsText").html("You are on the first line.");
        }
    }
    else { //there is no previous line
        $("#prevColLine").html("**");
        $("#captionsText").html("ERROR.  NUMBERS ARE OFF");
    }
    tpen.screen.focusItem[0] = tpen.screen.focusItem[1];
    tpen.screen.focusItem[1] = transcriptlet;
    if ((tpen.screen.focusItem[0] === null)
        || (tpen.screen.focusItem[0].attr("id") !== tpen.screen.focusItem[1].attr("id"))) {
        adjustImgs(setPositions());
        swapTranscriptlet();
        // show previous line transcription
        $('#captions').css({
            opacity: 1
        });
    }
    else {
        adjustImgs(setPositions());
        tpen.screen.focusItem[1].prevAll(".transcriptlet").addClass("transcriptletBefore").removeClass("transcriptletAfter");
        tpen.screen.focusItem[1].nextAll(".transcriptlet").addClass("transcriptletAfter").removeClass("transcriptletBefore");
    }
    // prevent textareas from going invisible and not moving out of the workspace
    tpen.screen.focusItem[1].removeClass("transcriptletBefore transcriptletAfter");
};

/* Helper for position focus onto a specific transcriptlet.  Makes sure workspace stays on screen. */
function setPositions() {
    // Determine size of section above workspace
    var bottomImageHeight = $("#imgBottom img").height();
    if (tpen.screen.focusItem[1].attr("lineHeight") !== null) {
        var pairForBookmarkCol = tpen.screen.focusItem[1].attr('col');
        var pairForBookmarkLine = parseInt(tpen.screen.focusItem[1].attr('collinenum'));
        pairForBookmarkLine++;
        var pairForBookmark = pairForBookmarkCol + pairForBookmarkLine;
        var currentLineHeight = parseFloat(tpen.screen.focusItem[1].attr("lineHeight"));
        var currentLineTop = parseFloat(tpen.screen.focusItem[1].attr("lineTop"));
        var previousLineTop = 0.0;
        var previousLineHeight = 0.0;
        var nextLineHeight = 0.0; //only used to gauge height of imgTop
        var imgTopHeight = 0.0; //value for the height of imgTop
        if(tpen.screen.focusItem[1].next().is('.transcriptlet')){
            nextLineHeight = parseFloat(tpen.screen.focusItem[1].next().attr("lineHeight"));
        }
        if(tpen.screen.focusItem[1].prev().is('.transcriptlet') && currentLineTop > parseFloat(tpen.screen.focusItem[1].prev().attr("lineTop"))){ 
            previousLineTop = parseFloat(tpen.screen.focusItem[1].prev().attr("lineTop"));
            previousLineHeight = parseFloat(tpen.screen.focusItem[1].prev().attr("lineHeight"));
        }
        else{ //there may not be a previous line so use the values of the line you are on
            previousLineTop = currentLineTop;
            previousLineHeight = currentLineHeight;
        }
        
        //We may be able to do this a bit better. 
        if(nextLineHeight > 0.0){
            imgTopHeight = (nextLineHeight + currentLineHeight); // obscure behind workspace.
        }
        else{ //there may not be a next line so use the value of the previous line...
            imgTopHeight = (previousLineHeight + currentLineHeight) + 1.5; // obscure behind workspace.  Do we need the 1.5?
        }
        var topImgPositionPercent = ((previousLineTop - currentLineTop) * 100) / imgTopHeight;
        var topImgPositionPx = ((-currentLineTop * bottomImageHeight) / 100);
        if(topImgPositionPx <= -12){
            topImgPositionPx += 12;
        }
        var bottomImgPositionPercent = -(currentLineTop + currentLineHeight);
        var bottomImgPositionPx = -((currentLineTop + currentLineHeight) * bottomImageHeight / 100);
        if(bottomImgPositionPx <= -12){
            bottomImgPositionPx += 12;
        }
        var imgTopSize = (((imgTopHeight/100)*bottomImageHeight) / Page.height())*100;
        var percentageFixed = 0;
        //use this to make sure workspace stays on screen!
        if (imgTopSize > 80){ //if #imgTop is 80% of the screen size then we need to fix that so the workspace stays.  
            var workspaceHeight = 170; //$("#transWorkspace").height();
            var origHeight = imgTopHeight;
            imgTopHeight = ((Page.height() - workspaceHeight - 80) / bottomImageHeight) *  100; //this needs to be a percentage
            percentageFixed = (100-(origHeight - imgTopHeight))/100; //what percentage of the original amount is left
            bottomImgPositionPercent *= percentageFixed; //do the same percentage change to this value
            bottomImgPositionPx *= percentageFixed; //and this one
            topImgPositionPx *= percentageFixed; // and this one

        }
    }
    var positions = {
        imgTopHeight: imgTopHeight,
        topImgPositionPercent: topImgPositionPercent,
        topImgPositionPx : topImgPositionPx,
        bottomImgPositionPercent: bottomImgPositionPercent,
        bottomImgPositionPx: bottomImgPositionPx,
        activeLine: pairForBookmark
    };
    return positions;
}

/**
* Removes previous textarea and slides in the new focus.
*
* @see updatePresentation()
*/
function swapTranscriptlet() {
    // slide in the new transcriptlet
    tpen.screen.focusItem[1].css({"width": "auto", "z-index": "5"});
    tpen.screen.focusItem[1].removeClass("transcriptletBefore transcriptletAfter");
    tpen.screen.focusItem[1].prevAll(".transcriptlet").addClass("transcriptletBefore").removeClass("transcriptletAfter");
    tpen.screen.focusItem[1].nextAll(".transcriptlet").addClass("transcriptletAfter").removeClass("transcriptletBefore");
    if ($('.transcriptletAfter').length === 0){
        $('#nextTranscriptlet').hide();
    }
    else {
        $('#nextTranscriptlet').show();
    }
    if ($('.transcriptletBefore').length === 0){
        $('#previousTranscriptlet').hide();
    }
    else {
        $('#previousTranscriptlet').show();
    }
}
var Page = {
    /** 
     *  Returns converted number to CSS consumable string rounded to n decimals.
     *  
     *  @param num float unprocessed number representing an object dimension
     *  @param n number of decimal places to include in returned string
     *  @returns float in ##.## format (example shows n=2)
     */
    convertPercent: function(num,n){
        return Math.round(num*Math.pow(10,(n+2)))/Math.pow(10,n);
    },
    /**
     * Sloppy hack so .focus functions work in FireFox
     * 
     * @param elem element to focus on
     */
    focusOn: function(elem){
        setTimeout("elem.focus()",0);
    },
    /**
     * Window dimensions.
     * 
     * @return Integer width of visible page
     */
    width: function() {
        return window.innerWidth !== null? window.innerWidth: document.body !== null? document.body.clientWidth:null;
    },
    /**
     * Window dimensions.
     * 
     * @return Integer height of visible page
     */
    height: function() {
        return window.innerHeight !== null? window.innerHeight: document.body !== null? document.body.clientHeight:null;
    }
};

/**
 * Keep workspace on the screen when displaying large lines.
 * Tests for need and then adjusts. Runs on change to
 * workspace size or line change.
 * 
 * **deprecated.  @see setPositions()
 */
function maintainWorkspace(){
    // keep top img within the set proportion of the screen
    var imgTopHeight = $("#imgTop").height();
    console.log(" is"+imgTopHeight + " > "+Page.height() + "?");
    
    if (imgTopHeight > Page.height()) {
        console.log("yes");
        imgTopHeight = Page.height();
        //Should I try to convert this to a percentage? 
        $("#imgTop").css("height", imgTopHeight);
       // adjustImgs(setPositions());
    }
    else{
        console.log("no");
    }
    
}
/**
 * Aligns images and workspace using defined dimensions.
 *
 * @see maintainWorkspace()
*/
function adjustImgs(positions) {
    //move background images above and below the workspace
    var lineToMakeActive = $(".lineColIndicator[pair='" + positions.activeLine + "']:first");
    var topImageHeight = $("#imgTop img").height();
    $("#imgTop")
        .css({
            "height": positions.imgTopHeight + "%"
            });
    $("#imgTop img").css({
            top: positions.topImgPositionPx + "px",
            left: "0px"
        });
    $("#imgTop .lineColIndicatorArea")
        .css({
            top: positions.topImgPositionPx + "px",
            left: "0px"
        });
    $("#imgBottom img")
        .css({
            top: positions.bottomImgPositionPx + "px",
            left: "0px"
        });
    $("#imgBottom .lineColIndicatorArea")
        .css({
            top: positions.bottomImgPositionPx + "px",
            left: "0px"
        });
    if ($('.activeLine').hasClass('linesHidden')){
        $('.activeLine').hide();
    }
    $(".lineColIndicator")
        .removeClass('activeLine')
        .css({
            "box-shadow": "none",
            "background-color":"transparent"
        });
    lineToMakeActive.addClass("activeLine");
    // use the active line color to give the active line a little background color
    // to make it stand out if the box shadow is not enough.
    var activeLineColor = tpen.screen.colorThisTime.replace(".4", ".2");
    $('.activeLine').css({
        'box-shadow': '0px 0px 15px 8px ' + tpen.screen.colorThisTime
    });
}

/* Update the line information of the line currently focused on, then load the focus to a line that was clicked on */
function loadTranscriptlet(lineid){
    var currentLineServerID = tpen.screen.focusItem[1].attr("lineserverid");
    if ($('#transcriptlet_' + lineid).length > 0){
        if (tpen.user.UID || tpen.user.isAdmin){
            var lineToUpdate = $(".transcriptlet[lineserverid='" + currentLineServerID + "']");
            updateLine(lineToUpdate, false, false);
            updatePresentation($('#transcriptlet_' + lineid));
        }
        else {
        var captionText1 = $("#captionsText").html();
            $("#captionsText").html("You are not logged in.");
            $('#captionsText').css("background-color", 'red');
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
            setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); $("#captionsText").html(captionText1); }, 1500);
        }
    }
    else { //blink a caption warning
        var captionText = $("#captionsText").html();
        $("#captionsText").html("Cannot load this line.");
        $('#captionsText').css("background-color", 'red');
        setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
        setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
        setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); $("#captionsText").html(captionText); }, 1500);
    }
}

/*
             * The UI control for going the the next transcriptlet in the transcription.
             */
function nextTranscriptlet() {
    var thisLine = tpen.screen.focusItem[1].attr('lineID');
    thisLine++;
    var nextID = thisLine;
    var currentLineServerID = tpen.screen.focusItem[1].attr("lineserverid");
    if ($('#transcriptlet_' + nextID).length > 0){
        if (tpen.user.UID || tpen.user.isAdmin){
            var lineToUpdate = $(".transcriptlet[lineserverid='" + currentLineServerID + "']");
            updateLine(lineToUpdate, false, false);
            updatePresentation($('#transcriptlet_' + nextID));
        }
        else {
            var captionText1 = $("#captionsText").html();
            $("#captionsText").html("You are not logged in.");
            $('#captionsText').css("background-color", 'red');
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
            setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); $("#captionsText").html(captionText1); }, 1500);
        }
    }
    else { //blink a caption warning
        var captionText = $("#captionsText").html();
        $("#captionsText").html("You are on the last line! ");
        $('#captionsText').css("background-color", 'red');
        setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
        setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
        setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); $("#captionsText").html(captionText); }, 1500);
    }
}

/*
             * The UI control for going the the previous transcriptlet in the transcription.
             */
function previousTranscriptlet() {
    var prevID = parseFloat(tpen.screen.focusItem[1].attr('lineID')) - 1;
    var currentLineServerID = tpen.screen.focusItem[1].attr("lineServerID");
    if (prevID >= 0){
        if (tpen.user.UID || tpen.user.isAdmin){
            var lineToUpdate = $(".transcriptlet[lineserverid='" + currentLineServerID + "']");
            updateLine(lineToUpdate, false, false);
            updatePresentation($('#transcriptlet_' + prevID));
        }
        else {
            var captionText1 = $("#captionsText").html();
            $("#captionsText").html("You are not logged in.");
            $('#captionsText').css("background-color", 'red');
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
            setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); $("#captionsText").html(captionText1); }, 1500);
        }
    }
    else {
        //captions already say "You are on the first line"
    }
}

/**
 *
 * Allows workspace to be moved up and down on the screen.
 * Requires shift key to be held down.
 */
function moveWorkspace(evt){
    $("#imgTop,#imgBottom,#imgBottom img").addClass('noTransition');
    var startImgTop = $("#imgTop").height();
    var startImgBottom = $("#imgBottom img").position().top;
    var startImgBottomH = $("#imgBottom").height();
    var mousedownPosition = evt.pageY;
    evt.preventDefault();
    $(dragHelper).appendTo("body");
    $(document)
    .disableSelection()
    .mousemove(function(event){
        var imgBtmSpot = startImgBottom - (event.pageY - mousedownPosition);
        $("#imgTop").height(startImgTop + event.pageY - mousedownPosition);
        $("#imgBottom").css({
            "height": startImgBottomH - (event.pageY - mousedownPosition)
        })
        .find("img").css({
            "top"   : startImgBottom - (event.pageY - mousedownPosition)
        });
        $("#imgBottom .lineColIndicatorArea").css("top", startImgBottom - (event.pageY - mousedownPosition) + "px");
        $("#dragHelper").css({
            top :   event.pageY - 90,
            left:   event.pageX - 90
        });
    })
    .mouseup(function(){
        $("#dragHelper").remove();
        $("#imgTop,#imgBottom,#imgBottom img").removeClass('noTransition');
        $(document)
            .enableSelection()
            .unbind("mousemove");
        isUnadjusted = false;
    });
}

/* Start event listening to move the image in the transcirption interface */
function startMoveImg(){
    if ($(".transcriptlet:first").hasClass("moveImage")){
        $("#moveImage").removeClass("selected");
        $(".transcriptlet").removeClass("moveImage");
        $(".transcriptlet").children("textarea").removeAttr("disabled");
        $("#imgTop, #imgBottom").css("cursor", "default");
        $("#imgTop,#imgBottom").unbind("mousedown");
    }
    else {
        $("#moveImage").addClass("selected");
        $(".transcriptlet").addClass("moveImage");
        $(".transcriptlet").children("textarea").attr("disabled", "");
        $("#imgTop, #imgBottom").css("cursor", "url(" + "images/open_grab.png),auto");
        $("#imgTop,#imgBottom").mousedown(function(event){moveImg(event); });
    }
}

/**
* Allows manuscript image to be moved around.
* Requires shift key to be held down.
* Synchronizes movement of top and bottom images.
* Bookmark bounding box moves with top image.
* @param event Event
*/
function moveImg(event){
    var startImgPositionX = parseFloat($("#imgTop img").css("left"));
    var startImgPositionY = parseInt($("#imgTop img").css("top"));
    var startBottomImgPositionX = parseInt($("#imgBottom img").css("left"));
    var startBottomImgPositionY = parseInt($("#imgBottom img").css("top"));
    var mousedownPositionX = event.pageX;
    var mousedownPositionY = event.pageY;
    event.preventDefault();
    $("#imgTop img,#imgBottom img,#imgTop .lineColIndicatorArea, #imgBottom .lineColIndicatorArea, #bookmark").addClass('noTransition');
    $("#imgTop, #imgBottom").css("cursor", "url(images/close_grab.png),auto");
    $(document)
    .disableSelection()
    .mousemove(function(event){
        $("#imgTop img").css({
            top :   startImgPositionY + event.pageY - mousedownPositionY,
            left:   startImgPositionX + event.pageX - mousedownPositionX
        });
        $("#imgTop .lineColIndicatorArea").css({
            top :   startImgPositionY + event.pageY - mousedownPositionY,
            left:   startImgPositionX + event.pageX - mousedownPositionX
        });
        $("#imgBottom img").css({
            top :   startBottomImgPositionY + event.pageY - mousedownPositionY,
            left:   startBottomImgPositionX + event.pageX - mousedownPositionX
        });
        $("#imgBottom .lineColIndicatorArea").css({
            top :   startBottomImgPositionY + event.pageY - mousedownPositionY,
            left:   startBottomImgPositionX + event.pageX - mousedownPositionX
        });
        if (!event.altKey) unShiftInterface();
    })
    .mouseup(function(){
        $("#dragHelper").remove();
        $("#imgTop img,#imgBottom img,#imgTop .lineColIndicatorArea, #imgBottom .lineColIndicatorArea, #bookmark").removeClass('noTransition');
        if (!tpen.screen.isMagnifying)$("#imgTop, #imgBottom").css("cursor", "url(images/open_grab.png),auto");
        $(document)
        .enableSelection()
        .unbind("mousemove");
        isUnadjusted = false;
    });
}

function restoreWorkspace(){
    $("#imgBottom").show();
    $("#imgTop").show();
    $("#imgTop").removeClass("fixingParsing");
    $("#transWorkspace").show();
    $("#imgTop").css("width", "100%");
    $("#imgTop img").css({"height":"auto", "width":"100%"});
    updatePresentation(tpen.screen.focusItem[1]);
    $(".hideMe").show();
    $(".showMe").hide();
    var pageJumpIcons = $("#pageJump").parent().find("i");
    pageJumpIcons[0].setAttribute('onclick', 'firstFolio();');
    pageJumpIcons[1].setAttribute('onclick', 'previousFolio();');
    pageJumpIcons[2].setAttribute('onclick', 'nextFolio();');
    pageJumpIcons[3].setAttribute('onclick', 'lastFolio();');
    $("#prevCanvas").attr("onclick", "previousFolio();");
    $("#nextCanvas").attr("onclick", "nextFolio();");
    $("#pageJump").removeAttr("disabled");
}

function hideWorkspaceToSeeImage(){
    $("#transWorkspace").hide();
    $("#imgTop").hide();
    $("#imgBottom img").css({
        "top" :"0%",
        "left":"0%"
    });
    $("#imgBottom .lineColIndicatorArea").css({
        "top": "0%"
    });
    $(".hideMe").hide();
    $(".showMe").show();
}

function magnify(img, event){
    //For separating out different imgs on which to zoom.
    //Right now it is just the transcription canvas.
    if (img === "trans"){
        img = $("#transcriptionTemplate");
        $("#magnifyTools").fadeIn(800);
        $("button[magnifyimg='trans']").addClass("selected");
    }
    else if (img === "compare"){
        img = $("#compareSplit");
        $("#magnifyTools").fadeIn(800).css({
            "left":$("#compareSplit").css("left"),
            "top" : "100px"
        });
        $("button[magnifyimg='compare']").addClass("selected");
    }
    else if (img === "full"){
        img = $("#fullPageSplitCanvas");
        $("#magnifyTools").fadeIn(800).css({
            "left":$("#fullPageSplit").css("left"),
            "top" : "100px"
        });
        $("button[magnifyimg='full']").addClass("selected");
    }
    $("#zoomDiv").show();
    $(".magnifyHelp").show();
    hideWorkspaceToSeeImage();
    $(".lineColIndicatorArea").hide();
    tpen.screen.liveTool = "image";
    mouseZoom(img, event);
}

function stopMagnify(){
    tpen.screen.isMagnifying = false;
    tpen.screen.zoomMultiplier = 2;
    $(document).off("mousemove");
    $("#zoomDiv").removeClass("ui-state-active");
    $("#zoomDiv").hide();
    $(".magnifyBtn").removeClass("ui-state-active");
    $("#magnifyTools").fadeOut(800);
    $(".lineColIndicatorArea").show();
    $(".magnifyHelp").hide();
    $("button[magnifyimg='full']").removeClass("selected");
    $("button[magnifyimg='compare']").removeClass("selected");
    $("button[magnifyimg='trans']").removeClass("selected");
    restoreWorkspace();
}

/**
* Creates a zoom on the image beneath the mouse.
*
* @param $img jQuery img element to zoom on
* @param event Event
*/
function mouseZoom($img, event){
    tpen.screen.isMagnifying = true;
    var imgURL = $img.find("img:first").attr("src");
    var page = $("#transcriptionTemplate");
    //collect information about the img
    var imgDims = new Array($img.offset().left, $img.offset().top, $img.width(), $img.height());
    //build the zoomed div
    var zoomSize = (page.height() / 3 < 120) ? 120 : page.height() / 3;
    var zoomPos = new Array(event.pageX, event.pageY);
    $("#zoomDiv").css({
        "box-shadow"    : "2px 2px 5px black,15px 15px " + zoomSize / 3 + "px rgba(230,255,255,.8) inset,-15px -15px " + zoomSize / 3 + "px rgba(0,0,15,.4) inset",
        "width"         : zoomSize,
        "height"        : zoomSize,
        "left"          : zoomPos[0] + 3,
        "top"           : zoomPos[1] + 3 - $(document).scrollTop() - $(".magnifyBtn").offset().top,
        "background-position" : "0px 0px",
        "background-size"     : imgDims[2] * tpen.screen.zoomMultiplier + "px",
        "background-image"    : "url('" + imgURL + "')"
    });
    $(document).on({
        mousemove: function(event){
            if (tpen.screen.liveTool !== "image" && tpen.screen.liveTool !== "compare") {
                $(document).off("mousemove");
                $("#zoomDiv").hide();
            }
            var mouseAt = new Array(event.pageX, event.pageY);
            var zoomPos = new Array(mouseAt[0] - zoomSize / 2, mouseAt[1] - zoomSize / 2);
            var imgPos = new Array((imgDims[0] - mouseAt[0]) * tpen.screen.zoomMultiplier + zoomSize / 2 - 3, (imgDims[1] - mouseAt[1]) * tpen.screen.zoomMultiplier + zoomSize / 2 - 3); //3px border adjustment
            $("#zoomDiv").css({
                "left"  : zoomPos[0],
                "top"   : zoomPos[1] - $(document).scrollTop(),
                "background-size"     : imgDims[2] * tpen.screen.zoomMultiplier + "px",
                "background-position" : imgPos[0] + "px " + imgPos[1] + "px"
            });
        }
    }, $img);
}

function removeTransition(){
    // TODO: objectify this
    $("#imgTop img").css("-webkit-transition", "");
    $("#imgTop img").css("-moz-transition", "");
    $("#imgTop img").css("-o-transition", "");
    $("#imgTop img").css("transition", "");
    $("#imgBottom img").css("-webkit-transition", "");
    $("#imgBottom img").css("-moz-transition", "");
    $("#imgBottom img").css("-o-transition", "");
    $("#imgBottom img").css("transition", "");
    $("#imgTop").css("-webkit-transition", "");
    $("#imgTop").css("-moz-transition", "");
    $("#imgTop").css("-o-transition", "");
    $("#imgTop").css("transition", "");
    $("#imgBottom").css("-webkit-transition", "");
    $("#imgBottom").css("-moz-transition", "");
    $("#imgBottom").css("-o-transition", "");
    $("#imgBottom").css("transition", "");
};

function restoreTransition(){
    // TODO: objectify this
    $("#imgTop img").css("-webkit-transition", "left .5s, top .5s, width .5s");
    $("#imgTop img").css("-moz-transition", "left .5s, top .5s, width .5s");
    $("#imgTop img").css("-o-transition", "left .5s, top .5s, width .5s");
    $("#imgTop img").css("transition", "left .5s, top .5s, width .5s");
    $("#imgBottom img").css("-webkit-transition", "left .5s, top .5s, width .5s");
    $("#imgBottom img").css("-moz-transition", "left .5s, top .5s, width .5s");
    $("#imgBottom img").css("-o-transition", "left .5s, top .5s, width .5s");
    $("#imgBottom img").css("transition", "left .5s, top .5s, width .5s");
    $("#imgTop").css("-webkit-transition", "left .5s, top .5s, width .5s");
    $("#imgTop").css("-moz-transition", "left .5s, top .5s, width .5s");
    $("#imgTop").css("-o-transition", "left .5s, top .5s, width .5s");
    $("#imgTop").css("transition", "left .5s, top .5s, width .5s");
    $("#imgBottom").css("-webkit-transition", "left .5s, top .5s, width .5s");
    $("#imgBottom").css("-moz-transition", "left .5s, top .5s, width .5s");
    $("#imgBottom").css("-o-transition", "left .5s, top .5s, width .5s");
    $("#imgBottom").css("transition", "left .5s, top .5s, width .5s");
};

/**
* Sets screen for parsing tool use.
* Slides the workspace down and scales the top img
* to full height. From here, we need to load to interface
* for the selected tool.
*/
function hideWorkspaceForParsing(){
    $("#parsingBtn").css("box-shadow: none;");
    originalCanvasHeight = $("#transcriptionCanvas").height();
    originalCanvasWidth = $("#transcriptionCanvas").width();
    imgTopOriginalTop = $("#imgTop img").css("top");
    var pageJumpIcons = $("#pageJump").parent().children("i");
    pageJumpIcons[0].setAttribute('onclick', 'firstFolio("parsing");');
    pageJumpIcons[1].setAttribute('onclick', 'previousFolio("parsing");');
    pageJumpIcons[2].setAttribute('onclick', 'nextFolio("parsing");');
    pageJumpIcons[3].setAttribute('onclick', 'lastFolio("parsing");');
    $("#prevCanvas").attr("onclick", "");
    $("#nextCanvas").attr("onclick", "");
    $("#imgTop").addClass("fixingParsing");
    var topImg = $("#imgTop img");
    imgRatio = topImg.width() / topImg.height();
    var wrapWidth = imgRatio * $("#transcriptionTemplate").height();
    var PAGEWIDTH = $("#transcriptionTemplate").width();
    if (wrapWidth > PAGEWIDTH - 350){
        wrapWidth = PAGEWIDTH - 350;
    }
    $("#tools").children("[id$='Split']").hide();
    $("#parsingSplit")
    .css({
        "display": "inline-block",
        "height": window.innerHeight + "px"
    })
    .fadeIn();
    topImg.css({
        "top":"0px",
        "left":"0px",
        "height":"auto",
        "overflow":"auto"
    });
    $("#imgTop .lineColIndicatorArea").css({
        "top":"0px",
        "left":"0px"
    });
    $("#transcriptionTemplate").css("max-width", "57%");
    //the width and max-width here may need to be played with a bit.
    $("#transcriptionTemplate").resizable({
        disabled:false,
        minWidth: window.innerWidth / 2,
        maxWidth: window.innerWidth * .75,
        start: function(event, ui){
            originalRatio = $("#transcriptionCanvas").width() / $("#transcriptionCanvas").height();
        },
        resize: function(event, ui) {
            var width = ui.size.width;
            var height = 1 / originalRatio * width;
            $("#transcriptionCanvas").css("height", height + "px").css("width", width + "px");
            $(".lineColIndicatorArea").css("height", height + "px");
            var splitWidth = window.innerWidth - (width + 35) + "px";
            $(".split img").css("max-width", splitWidth);
            $(".split:visible").css("width", splitWidth);
        },
        stop: function(event, ui){
            //$(".lineColIndicator .lineColOnLine").css("line-height", $(this).height()+"px");
        }
    });
    $("#transWorkspace,#imgBottom").hide();
    $("#noLineWarning").hide();
    window.setTimeout(function(){
        $("#imgTop, #imgTop img").height($(window).innerHeight());
        $("#imgTop img").css("width", "auto");
        $("#imgTop").css("width", $("#imgTop img").width());
        $("#imgTop").css("height", $("#imgTop img").height());
        //At this point, transcription canvas is the original height and width
        //of the full page image.  We can use that for when we resume transcription.
        $("#transcriptionCanvas").css("height", $(window).innerHeight());
        $(".lineColIndicatorArea").css("height", $(window).innerHeight());
        $("#transcriptionCanvas").css("display", "block");
    }, 500);
    window.setTimeout(function(){
        //in here we can control what interface loads up.  writeLines
        //draws lines onto the new full size transcription image.
        $('.lineColIndicatorArea').hide();
        writeLines($("#imgTop img"));
    }, 1200);
}

/**
 * Overlays divs for each parsed line onto img indicated.
 * Divs receive different classes in different
 *
 * @param imgToParse img element lines will be represented over
 */
function writeLines(imgToParse){
    $(".line,.parsing,.adjustable,.parsingColumn").remove();
    //clear and old lines to put in updated ones
    var originalX = (imgToParse.width() / imgToParse.height()) * 1000;
    var setOfLines = [];
    var count = 0;
    $(".transcriptlet").each(function(index){
        count++;
        setOfLines[index] = makeOverlayDiv($(this), originalX, count);
    });
    imgToParse.parent().append($(setOfLines.join("")));
}

function makeOverlayDiv(thisLine, originalX, cnt){
    var Y = parseFloat(thisLine.attr("lineTop"));
    var X = parseFloat(thisLine.attr("lineLeft"));
    var H = parseFloat(thisLine.attr("lineHeight"));
    var W = parseFloat(thisLine.attr("lineWidth"));
    var newY = (Y);
    var newX = (X);
    var newH = (H);
    var newW = (W);
    var lineOverlay = "<div class='parsing' linenum='" + cnt + "' style='top:"
        + newY + "%;left:" + newX + "%;height:"
        + newH + "%;width:" + newW + "%;' lineserverid='"
        + thisLine.attr('lineserverid') + "'linetop='"
        + Y + "'lineleft='" + X + "'lineheight='"
        + H + "'linewidth='" + W + "'></div>";
    return lineOverlay;
}

/* Reset the interface to the full screen transcription view. */
function fullPage(){
    if ($("#overlay").is(":visible")) {
        $("#overlay").click();
        return false;
    }
    $(".line, .parsing, .adjustable,.parsingColumn").remove();
    tpen.screen.isUnadjusted = tpen.screen.isFullscreen = true;
    if ($("#trascriptionTemplate").hasClass("ui-resizable")){
        $("#transcriptionTemplate").resizable('destroy');
    }
    $("#splitScreenTools").removeAttr("disabled");
    $("#splitScreenTools").find('option:eq(0)').prop("selected", true);
    $("#transcriptionCanvas").css("width", "100%");
    $("#transcriptionCanvas").css("height", "auto");
    $("#transcriptionTemplate").css("width", "100%");
    $("#transcriptionTemplate").css("max-width", "100%");
    $("#transcriptionTemplate").css("height", "auto");
    $("#transcriptionTemplate").css("display", "inline-block");
    $('.lineColIndicatorArea').show();
    $("#fullScreenBtn").fadeOut(250);
    tpen.screen.isZoomed = false;
    $(".split").hide();
    $(".split").css("width", "43%");
    restoreWorkspace();
    $("#splitScreenTools").show();
    var screenWidth = $(window).width();
    var adjustedHeightForFullscreen = (originalCanvasHeight2 / originalCanvasWidth2) * screenWidth;
    $("#transcriptionCanvas").css("height", adjustedHeightForFullscreen + "px");
    $(".lineColIndicatorArea").css("height", adjustedHeightForFullscreen + "px");
    $("#imgTop").hover(function(){
        var color = tpen.screen.colorThisTime.replace(".4", "1");
        $('.activeLine').css('box-shadow', '0px 0px 15px 8px ' + color);
    }, function(){
        $('.activeLine').css('box-shadow', '0px 0px 15px 8px ' + tpen.screen.colorThisTime);
    });
    $.each($(".lineColOnLine"), function(){
        $(this).css("line-height", $(this).height() + "px");
    });
    if (tpen.screen.focusItem[0] == null
        && tpen.screen.focusItem[1] == null){
        updatePresentation($("#transcriptlet_1"));
    }
}

function splitPage(event, tool) {
    tpen.screen.liveTool = tool;
    originalCanvasHeight = $("#transcriptionCanvas").height(); //make sure these are set correctly
    originalCanvasWidth = $("#transcriptionCanvas").width(); //make sure these are set correctly
    var ratio = originalCanvasWidth / originalCanvasHeight;
    $("#splitScreenTools").attr("disabled", "disabled");
    var imgBottomRatio = parseFloat($("#imgBottom img").css("top")) / originalCanvasHeight;
    var imgTopRatio = parseFloat($("#imgTop img").css("top")) / originalCanvasHeight;
    $("#transcriptionTemplate").css({
        "width"   :   "55%",
        "display" : "inline-table"
    });
    var newCanvasWidth = originalCanvasWidth2 * .55;
    var newCanvasHeight = 1 / ratio * newCanvasWidth;
    $("#transcriptionCanvas").css({
        "width"   :   newCanvasWidth + "px",
        "height"   :   newCanvasHeight + "px"
    });
    var newImgBtmTop = imgBottomRatio * newCanvasHeight;
    var newImgTopTop = imgTopRatio * newCanvasHeight;
    $(".lineColIndicatorArea").css("height", newCanvasHeight + "px");
    $("#imgBottom img").css("top", newImgBtmTop + "px");
    $("#imgBottom .lineColIndicatorArea").css("top", newImgBtmTop + "px");
    $("#imgTop img").css("top", newImgTopTop + "px");
    $("#imgTop .lineColIndicatorArea").css("top", newImgTopTop + "px");
    $.each($(".lineColOnLine"), function(){$(this).css("line-height", $(this).height() + "px"); });
    $("#transcriptionTemplate").resizable({
        disabled:false,
        minWidth: window.innerWidth / 2,
        maxWidth: window.innerWidth * .75,
        start: function(event, ui){
            originalRatio = $("#transcriptionCanvas").width() / $("#transcriptionCanvas").height();
        },
        resize: function(event, ui) {
            var width = ui.size.width;
            var height = 1 / originalRatio * width;
            $("#transcriptionCanvas").css("height", height + "px").css("width", width + "px");
            $(".lineColIndicatorArea").css("height", height + "px");
            var splitWidth = window.innerWidth - (width + 35) + "px";
            $(".split img").css("max-width", splitWidth);
            $(".split:visible").css("width", splitWidth);
            var newHeight1 = parseFloat($("#fullPageImg").height()) + parseFloat($("#fullPageSplit .toolLinks").height());
            var newHeight2 = parseFloat($(".compareImage").height()) + parseFloat($("#compareSplit .toolLinks").height());
            $('#fullPageSplit').css('height', newHeight1 + 'px');
            $('#compareSplit').css('height', newHeight2 + 'px');
        },
        stop: function(event, ui){
            $.each($(".lineColOnLine"), function(){
                var height = $(this).height() + "px";
                $(this).css("line-height", height);
            });
        }
    });
    $("#fullScreenBtn").fadeIn(250);
    //show/manipulate whichever split tool is activated.
    switch (tool){
        case "calligraphy":
            $("#calligraphySplit").css({
            "display": "inline-table"
        });
            break;
        case "scripts":
            $("#scriptsSplit").css({
                "display": "inline-table"
            });
            break;
        case "frenchdocs":
            $("#documentsSplit").css({
                "display": "inline-table",
            });
            break;
        case "conservation":
            $("#conservationSplit").css({
                "display": "inline-table"
            });
            break;
        case "conventions":
            $("#conventionsSplit").css({
                "display": "inline-table"
            });
            break;
        case "teachers":
            $("#teachersSplit").css({
                "display": "inline-table"
            });
            break;
        case "groupwork":
            $("#groupSplit").css({
                "display": "inline-table"
            });
            break;
        case "glossary":
            $("#glossarySplit").css({
                "display": "inline-table"
            });
            break;
        case "fInstitutions":
            $("#fInstitutionsSplit").css({
                "display": "inline-table"
            });
            break;
        case "other":
            $("#otherSplit").css({
                "display": "inline-table"
            });
            break;
        case "essay":
            $("#essaySplit").css({
                "display": "inline-table"
            });
            break;
        case "partialTrans":
            $("#partialTransSplit").css({
                "display": "inline-table"
            });
            break;
        case "abbreviations":
            $("#abbrevSplit").css({
                "display": "inline-table"
            });
            break;
        case "dictionary":
            $("#dictionarySplit").css({
                "display": "inline-table"
            });
            break;
        case "preview":
            forceOrderPreview();
            break;
        case "history":
            $("#historySplit").css({
                "display": "inline-table"
            });
            break;
        case "fullPage":
            $("#fullPageSplit").css({
                "display": "block"
            });
            break;
        case "compare":
            $("#compareSplit").css({
                "display": "block"
            });
            //When comparing, you need to be able to see the whole image, so I restrict it to window height.
            //To allow it to continue to grow, comment out the code below.
            $(".compareImage").css({
                "max-height":window.innerHeight + "px",
                "max-width":$("#compareSplit").width() + "px"
            });
            populateCompareSplit(1);
            break;
        case "facing":
            $("#facingSplit").css("display", "block");
            break;
        case "maps":
            $("#mapsSplit").css("display", "inline-table");
            break;
        case "start":
            $("#startSplit").css("display", "inline-table");
            default:
            //This is a user added iframe tool.  tool is toolID= attribute of the tool div to show.
            $('div[toolName="' + tool + '"]').css("display", "inline-table");
    }
    $(".split:visible").find('img').css({
        'max-height': window.innherHeight + 350 + "px",
        'max-width' : $(".split:visible").width() + "px"
    });
    var pageJumpIcons = $("#pageJump").parent().children("i");
    pageJumpIcons[0].setAttribute('onclick', 'firstFolio("parsing");');
    pageJumpIcons[1].setAttribute('onclick', 'previousFolio("parsing");');
    pageJumpIcons[2].setAttribute('onclick', 'nextFolio("parsing");');
    pageJumpIcons[3].setAttribute('onclick', 'lastFolio("parsing");');
    $("#prevCanvas").attr("onclick", "");
    $("#nextCanvas").attr("onclick", "");
}

function forceOrderPreview(){
    var ordered = [];
    var length = $(".previewPage").length;
    for (var i = 0; i < length; i++){
        var thisOne = $(".previewPage[order='" + i + "']");
        ordered.push(thisOne);
        if (i === length - 1){
            $("#previewDiv").empty();
            $("#previewDiv").append(ordered);
        }
    }
    $("#previewSplit").css({
        "display": "inline-table"
    });
}

function populateCompareSplit(folioIndex){
    var canvasIndex = folioIndex - 1;
    var compareSrc = tpen.manifest.sequences[0].canvases[canvasIndex].images[0].resource["@id"];
    var currentCompareSrc = $(".compareImage").attr("src");
    if (currentCompareSrc !== compareSrc) $(".compareImage").attr("src", compareSrc);
}

/*
 * Go through all of the parsing lines and put them into columns;
 * @see linesToColumns()
 * Global Arrray: gatheredColumns
 *
 */
function gatherColumns(startIndex){
    var colX, colY, colW, colH;
    var lastColumnLine = - 1;
    var linesInColumn = - 1;
    var hasTranscription = false;
    if ($(".parsing")[startIndex + 1]){
        var line = $(".parsing")[startIndex + 1];
        colX = parseFloat($(line).attr("lineleft"));
        colY = parseFloat($(line).attr("linetop"));
        colW = parseFloat($(line).attr("linewidth"));
        var $lastLine = $(".parsing[lineleft='" + colX + "']:last");
        colH = parseFloat($lastLine.attr("linetop")) - colY + parseFloat($lastLine.attr("lineheight"));
        var lastLineIndex = $(".parsing").index($lastLine);
        tpen.screen.gatheredColumns.push([colX, colY, colW, colH, $(line).attr("lineserverid"), $lastLine.attr("lineserverid"), true]);
        gatherColumns(lastLineIndex);
    }
}

function removeColumn(column, destroy){
    if (!destroy){
        if (column.attr("hastranscription") === "true"){
            var cfrm = confirm("This column contains transcription data that will be lost.\n\nContinue?");
            if (!cfrm) return false;
        }
    }
    var colX = column.attr("lineleft");
    // collect lines from column
    var lines = $(".parsing[lineleft='" + colX + "']");
    lines.addClass("deletable");
    tpen.screen.nextColumnToRemove = column;
    removeColumnTranscriptlets(lines, false);
    
                        
}

function destroyPage(){
    tpen.screen.nextColumnToRemove = $(".parsingColumn:first");
    var colX = tpen.screen.nextColumnToRemove.attr("lineleft");
    var lines = $(".parsing[lineleft='" + colX + "']");
    if (tpen.screen.nextColumnToRemove.length > 0) {
        removeColumnTranscriptlets(lines, true);
    }
    else {
        cleanupTranscriptlets(true);
        $("#parsingCover").hide();
    }
}

/* Make parsing interface turn the lines in the view into columns */
function linesToColumns(){
//update lines in case of changes
    var gatheredColumns = tpen.screen.gatheredColumns = []; //The array built by gatherColumns()
    $(".parsingColumn").remove();
    if ($(".parsing").size() === 0) return false;
    //loop through lines to find column dimensions
    var columnParameters = new Array(); // x,y,w,h,startID,endID
    var i = 0;
    var colX, colY, colW, colH;
    var lastColumnLine = - 1;
    var linesInColumn = - 1;
    gatherColumns( - 1); //Gets all columns into an array.
    //build columns
    var columns = [];
    for (j = 0; j < gatheredColumns.length; j++){
        var parseImg = document.getElementById("imgTop").getElementsByTagName("img");
        var scaledX = gatheredColumns[j][0];
        var scaledY = gatheredColumns[j][1];
        var scaledW = gatheredColumns[j][2];
        var scaledH = gatheredColumns[j][3];
    //            // recognize, alert, and adjust to out of bounds columns
        if (scaledX + scaledW > 100){
        // exceeded the right boundary of the image
            if (scaledX > 98){
                scaledX = 98;
                scaledW = 2;
            } else {
                scaledW = 100 - scaledX - 1;
            }
        }
        if (scaledX < 0){
            // exceeded the left boundary of the image
            scaledW += scaledX;
            scaledX = 0;
        }
        if (scaledY + scaledH > 100){
            // exceeded the bottom boundary of the image
            if (scaledY > 98){
                scaledY = 98;
                scaledH = 2;
            } else {
                scaledH = 100 - scaledY - 1;
            }
        }
        if (scaledY < 0){
            // exceeded the top boundary of the image
            scaledH += scaledY;
            scaledY = 0;
        }
        var startID = $(".parsing[lineleft='" + gatheredColumns[j][0] + "']:first").attr("lineserverid");
        var endID = $(".parsing[lineleft='" + gatheredColumns[j][0] + "']:last").attr("lineserverid");
        columns.push("<div class='parsingColumn' lineleft='", gatheredColumns[j][0], "'",
            " linetop='", gatheredColumns[j][1], "'",
            " linewidth='", gatheredColumns[j][2], "'",
            " lineheight='", gatheredColumns[j][3], "'",
            " hastranscription='", gatheredColumns[j][6] == true, "'",
            " startid='", startID, "'",
            " endid='", endID, "'",
            " style='top:", scaledY, "%;left:", scaledX, "%;width:", scaledW, "%;height:", scaledH, "%;'>",
            "</div>");
    }
    //attach columns
    $(parseImg).before(columns.join(""));
    // avoid events on .lines
    $('#imgTop').find('.parsing').css({
        'z-index': '-10'
    });
    $(".parsingColumn")
    .mouseenter(function(){
        var lineInfo;
        lineInfo = $("#transcription" + ($(this).index(".parsing") + 1)).val();
        $("#lineInfo").empty()
        .text(lineInfo)
        .append("<div>" + $("#t" + ($(this).index(".line") + 1)).find(".counter").text() + "</div>")
        .show();
        if (!tpen.screen.isMagnifying){
            $(this).addClass("jumpLine");
        }
    })
    .mouseleave(function(){
        $(".parsing").removeClass("jumpLine");
        $("#lineInfo").hide();
    })
    .click(function(event){
    });
}

/**
 * Allows for column adjustment in the parsing interface.
 */
function adjustColumn(event){
    var thisColumnID = new Array(2);
    var thisColumn;
    var originalX = 1;
    var originalY = 1;
    var originalW = 1;
    var originalH = 1;
    var adjustment = "";
    var column = undefined;
    var originalPercentW;
    var originalPercentX;
    $.each($(".parsingColumn"), function(){
        if ($(this).hasClass("ui-resizable")){
            $(this).resizable("destroy");
        }
    });
    $(".parsingColumn").resizable({
        handles     : "n,s,w,e",
        containment : 'parent',
        start       : function(event, ui){
            $("#progress").html("Adjusting Columns - unsaved").fadeIn();
            $("#columnResizing").show();
            $("#sidebar").fadeIn();
            thisColumn = $(".ui-resizable-resizing");
            thisColumnID = [thisColumn.attr("startid"), thisColumn.attr("endid")];
            adjustment = "new";
            originalPercentW = parseFloat($(this).attr("linewidth"));
            originalPercentX = parseFloat($(this).attr("lineleft"));
        },
        resize      : function(event, ui){
            if (adjustment === "new"){
                var originalX = ui.originalPosition.left;
                var originalY = ui.originalPosition.top;
                var originalW = ui.originalSize.width;
                var originalH = ui.originalSize.height;
                var newX = ui.position.left;
                var newY = ui.position.top;
                var newW = ui.size.width;
                var newH = ui.size.height;
                var offsetForBtm = $(event.target).position().top;
                if (Math.abs(originalW - newW) > 5) adjustment = "right";
                if (Math.abs(originalH - newH) > 5) adjustment = "bottom";
                if (Math.abs(originalX - newX) > 5) adjustment = "left"; // a left change would affect w and x, order matters
                if (Math.abs(originalY - newY) > 5) adjustment = "top"; // a top change would affect h and y, order matters
                offsetForBtm = (offsetForBtm / $("#imgTop img").height()) * 100;
                newH = (newH / $("#imgTop img").height()) * 100;
                var actualBottom = newH + offsetForBtm;
                $("#progress").html("Adjusting " + adjustment + " - unsaved");
            }
        },
        stop        : function(event, ui){
            $("#progress").html("Column Resized - Saving...");
            var parseRatio = $("#imgTop img").width() / $("#imgTop img").height();
            var originalX = ui.originalPosition.left;
            var originalY = ui.originalPosition.top;
            var originalW = ui.originalSize.width;
            var originalH = ui.originalSize.height;
            var newX = ui.position.left;
            var newY = ui.position.top;
            var newW = ui.size.width;
            var newH = ui.size.height;
            var oldHeight, oldTop, oldLeft, newWidth, newLeft;
            //THESE ORIGINAL AND NEW VALUES ARE EVALUATED AS PIXELS, NOT PERCENTAGES
            if (adjustment === "top") {
                newY = (newY / $("#imgTop img").height()) * 100;
                originalY = (originalY / $("#imgTop img").height()) * 100;
                //save a new height for the top line;
                var startLine = $(".parsing[lineserverid='" + thisColumnID[0] + "']");
                oldHeight = parseFloat(startLine.attr("lineheight"));
                oldTop = parseFloat(startLine.attr("linetop"));
                //This should be resized right now.  If it is a good resize, the lineheight will be > 0
                startLine.attr({
                    "linetop"    : newY,
                    "lineheight" : oldHeight + oldTop - newY
                });
                startLine.css({
                    "top"    : newY + "%",
                    "height" : oldHeight + oldTop - newY + "%"
                });
                if (parseFloat(startLine.attr("lineheight")) < 0){
                    // top of the column is below the bottom of its top line
                    var newTopLine = startLine;
                    do {
                        newTopLine = startLine.next('.parsing');
                        removeLine(startLine, true);
                        removeTranscriptlet(startLine.attr("lineserverid"), startLine.attr("lineserverid"), true);
                        startLine = newTopLine;
                        oldHeight = parseFloat(startLine.attr("lineheight"));
                        oldTop = parseFloat(startLine.attr("linetop"));
                    } while (parseFloat(startLine.attr("linetop")) + parseFloat(startLine.attr("lineheight")) < newY);
                    //Got through all the ones that needed removing, now I am on the one that needs resizing.
                    startLine.attr({
                        "linetop"    : newY,
                        "lineheight" : oldHeight + oldTop - newY
                    });
                    startLine.css({
                        "top"    : newY + "%",
                        "height" : oldHeight + oldTop - newY + "%"
                    });
                    thisColumn.attr("startid", startLine.attr("lineserverid"));
                }
                $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
            }
        else if (adjustment === "bottom"){
            //technically, we want to track the bottom.  The bottom if the height + top offset
            var offsetForBtm = $(event.target).position().top;
            offsetForBtm = (offsetForBtm / $("#imgTop img").height()) * 100;
            newH = (newH / $("#imgTop img").height()) * 100;
            var actualBottom = newH + offsetForBtm;
            //save a new height for the bottom line
            var endLine = $(".parsing[lineserverid='" + thisColumnID[1] + "']");
            oldHeight = parseFloat(endLine.attr("lineheight"));
            oldTop = parseFloat(endLine.attr("linetop"));
            endLine.attr({
                "lineheight" : oldHeight + (newH - originalH)
            });
            endLine.css({
                "height" : oldHeight + (newH - originalH) + "%"
            });
            if (parseFloat(endLine.attr("linetop")) > actualBottom){
                //the bottom line isnt large enough to account for the change,
                // delete lines until we get to a  line that,
                // when combined with the deleted lines
                //can account for the requested change.
                do {
                oldHeight = parseFloat(endLine.attr("lineheight"));
                    oldTop = parseFloat(endLine.attr("linetop"));
                    var nextline = endLine.prev(".parsing");
                    endLine.remove();
                    removeLine(endLine, true);
                    removeTranscriptlet(endLine.attr("lineserverid"), endLine.attr("lineserverid"), true);
                    endLine = nextline;
                } while (parseFloat(endLine.attr("linetop")) > actualBottom);
                var currentLineTop = parseFloat(endLine.attr("linetop"));
                endLine.attr({
                    "lineheight" : actualBottom - currentLineTop
                });
                endLine.css({
                    "height" : actualBottom - currentLineTop + "%"
                });
                thisColumn.attr("endid", endLine.attr("lineserverid"));
            }
            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
        }
        else if (adjustment === "left"){
            //save a new left,width for all these lines
            var leftGuide = $(".parsing[lineserverid='" + thisColumnID[0] + "']");
            oldLeft = parseFloat(leftGuide.attr("lineleft"));
            var ratio1 = originalPercentW / originalW;
            var ratio2 = originalPercentX / originalX;
            newWidth = newW * ratio1;
            newLeft = newX * ratio2;
            $(".parsing[lineleft='" + oldLeft + "']").each(function(){
                $(this).attr({
                    "lineleft" : newLeft,
                    "linewidth": newWidth
                });
                    $(this).css({
                    "left" : newLeft + "%",
                    "width": newWidth + "%"
                });
            });
            updateLinesInColumn(thisColumnID);
            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
            cleanupTranscriptlets(true);
        }
        else if (adjustment === "right"){
            //save a new width for all these lines
            var rightGuide = $(".parsing[lineserverid='" + thisColumnID[0] + "']");
            oldLeft = parseFloat(rightGuide.attr("lineleft"));
            var ratio = originalPercentW / originalW;
            newWidth = newW * ratio; //new percent width
            $(".parsing[lineleft='" + oldLeft + "']").each(function(){
                $(this).attr({
                    "linewidth": newWidth
                });
                $(this).css({
                    "width": newWidth + "%"
                });
            });
            updateLinesInColumn(thisColumnID);
            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
            cleanupTranscriptlets(true);
        } else {
            $("#progress").html("No changes made.").delay(3000).fadeOut(1000);
        }
        $("#lineResizing").delay(3000).fadeOut(1000);
        adjustment = "";
        }
    });
}

/**
 * Determines action based on transcription line clicked and tool in use.
 * Alerts 'unknown click' if all fails. Calls lineChange(e,event) for
 * parsing tool. Jumps to transcriptlet for full page tool.
 */
function clickedLine(e, event) {
    //Stop ability to make a new line until the update from this process is complete.
    if ($(e).hasClass("parsing")){
        if ($("#addLines").hasClass('active') || $("#removeLines").hasClass('active')){
        $("#parsingCover").show();
            lineChange(e, event);
        }
    }
    else {
    }
}

function reparseColumns(){
    $.each($('.parsingColumn'), function(){
        var colX = $(this).attr("lineleft");
        // collect lines from column
        var lines = $(".parsing[lineleft='" + colX + "']");
        lines.addClass("deletable");
        var linesSize = lines.size();
        // delete from the end, alerting for any deleted data
        for (var i = linesSize; i > 0; i--){
            removeLine(lines[i], true);
        }
    });
}

function insertTag(tagName, fullTag){
    if (tagName.lastIndexOf("/") === (tagName.length - 1)) {
        //transform self-closing tags
        var slashIndex = tagName.length;
        fullTag = fullTag.slice(0, slashIndex) + fullTag.slice(slashIndex + 1, - 1) + " />";
    }
    // Check for wrapped tag
    if (!addchar(escape(fullTag), escape(tagName))) {
        closeTag(escape(tagName), escape(fullTag));
    }
}

function closeTag(tagName, fullTag){
    // Do not create for self-closing tags
    if (tagName.lastIndexOf("/") === (tagName.length - 1)) return false;
    var tagLineID = tpen.screen.focusItem[1].attr("lineserverid");
    var closeTag = document.createElement("div");
    var tagID;
    $.get("tagTracker", {
        addTag      : true,
        tag         : tagName,
        projectID   : tpen.project.id,
        line        : tagLineID
        }, function(data){
            tagID = data;
            $(closeTag).attr({
                "class"     :   "tags ui-corner-all right ui-state-error",
                "title"     :   unescape(fullTag),
                "data-line" :   tagLineID,
                //"data-folio":   folio,
                "data-tagID":   tagID
            }).text("/" + tagName);
            tpen.screen.focusItem[1].children(".xmlClosingTags").append(closeTag);
        }
    );
}

function addchar(theChar, closingTag) {
    var closeTag = (closingTag === undefined) ? "" : closingTag;
    var e = tpen.screen.focusItem[1].find('textarea')[0];
    if (e !== null) {
        return setCursorPosition(e, insertAtCursor(e, theChar, closeTag));
    }
    return false;
}

function setCursorPosition(e, position) {
    var pos = position;
    var wrapped = false;
    if (pos.toString().indexOf("wrapped") === 0) {
        pos = parseInt(pos.substr(7));
        wrapped = true;
    }
    e.focus();
    if (e.setSelectionRange) {
        e.setSelectionRange(pos, pos);
    }
    else if (e.createTextRange) {
        e = e.createTextRange();
        e.collapse(true);
        e.moveEnd('character', pos);
        e.moveStart('character', pos);
        e.select();
    }
    return wrapped;
}

function insertAtCursor (myField, myValue, closingTag) {
    var closeTag = (closingTag === undefined) ? "" : unescape(closingTag);
    //IE support
    if (document.selection) {
        myField.focus();
        sel = document.selection.createRange();
        sel.text = unescape(myValue);
        return sel + unescape(myValue).length;
    }
    //MOZILLA/NETSCAPE support
    else if (myField.selectionStart || myField.selectionStart == '0') {
        var startPos = myField.selectionStart;
        var endPos = myField.selectionEnd;
        if (startPos != endPos) {
            // something is selected, wrap it instead
            var toWrap = myField.value.substring(startPos, endPos);
            myField.value = myField.value.substring(0, startPos)
            + unescape(myValue)
            + toWrap
            + "</" + closeTag + ">"
            + myField.value.substring(endPos, myField.value.length);
            myField.focus();
            var insertLength = startPos + unescape(myValue).length +
            toWrap.length + 3 + closeTag.length;
            return "wrapped" + insertLength;
        } else {
            myField.value = myField.value.substring(0, startPos)
            + unescape(myValue)
            + myField.value.substring(startPos, myField.value.length);
            myField.focus();
            return startPos + unescape(myValue).length;
        }
    } else {
        myField.value += unescape(myValue);
        myField.focus();
        return myField.length;
    }
}

function toggleCharacters(){
    if ($("#charactersPopin .character:first").is(":visible")){
        $("#charactersPopin .character").fadeOut(400);
    }
    else{
        $("#charactersPopin .character").fadeIn(400).css("display", "block");
    }
}

function toggleTags(){
    if ($("#xmlTagPopin .lookLikeButtons:first").is(":visible")){
        $("#xmlTagPopin .lookLikeButtons").fadeOut(400);
    }
    else{
        $("#xmlTagPopin .lookLikeButtons").fadeIn(400).css("display", "block");
    }
}

function togglePageJump(){
    if ($("#pageJump .folioJump:first").is(":visible")){
        $("#pageJump .folioJump").fadeOut(400);
    }
    else{
        $("#pageJump .folioJump").fadeIn(400).css("display", "block");
    }
}

/* Change the page to the specified page from the drop down selection. */
function pageJump(page, parsing){
    var folioNum = parseInt(page); //1,2,3...
    var canvasToJumpTo = folioNum - 1; //0,1,2...
    if (tpen.screen.currentFolio !== canvasToJumpTo && canvasToJumpTo >= 0){ //make sure the default option was not selected and that we are not jumping to the current folio
        tpen.screen.currentFolio = canvasToJumpTo;
        if (parsing === "parsing"){
            $(".pageTurnCover").show();
            fullPage();
            tpen.screen.focusItem = [null, null];
            redraw(parsing);
            //loadTranscriptionCanvas(tpen.manifest.sequences[0].canvases[canvasToJumpTo], parsing);
            setTimeout(function(){
                hideWorkspaceForParsing();
                $(".pageTurnCover").fadeOut(1500);
            }, 800);
        }
        else {
            tpen.screen.currentFolio = canvasToJumpTo;
            tpen.screen.focusItem = [null, null];
            redraw("");
            //loadTranscriptionCanvas(tpen.manifest.sequences[0].canvases[canvasToJumpTo], "");
        }
    }
    else{
    }
}

function compareJump(folio){
    populateCompareSplit(folio);
}

/* Change color of lines on screen */
function markerColors(){
/*
 * This function allows the user to go through annotation colors and decide what color the outlined lines are.
 * colorThisTime
 */
    var tempColorList = ["rgba(153,255,0,.4)", "rgba(0,255,204,.4)", "rgba(51,0,204,.4)", "rgba(204,255,0,.4)", "rgba(0,0,0,.4)", "rgba(255,255,255,.4)", "rgba(255,0,0,.4)"];
    if (tpen.screen.colorList.length == 0){
        tpen.screen.colorList = tempColorList;
    }
    tpen.screen.colorThisTime = tpen.screen.colorList[Math.floor(Math.random() * tpen.screen.colorList.length)];
    tpen.screen.colorList.splice(tpen.screen.colorList.indexOf(tpen.screen.colorThisTime), 1);
    var oneToChange = tpen.screen.colorThisTime.lastIndexOf(")") - 2;
    var borderColor = tpen.screen.colorThisTime.substr(0, oneToChange) + '.2' + tpen.screen.colorThisTime.substr(oneToChange + 1);
    var lineColor = tpen.screen.colorThisTime.replace(".4", "1"); //make this color opacity 100
    $('.lineColIndicator').css('border', '1px solid ' + lineColor);
    $('.lineColOnLine').css({'border-left':'1px solid ' + borderColor, 'color':lineColor});
    $('.activeLine').css('box-shadow', '0px 0px 15px 8px ' + tpen.screen.colorThisTime); //keep this color opacity .4 until imgTop is hovered.
}

/* Toggle the line/column indicators in the transcription interface. (A1, A2...) */
function toggleLineMarkers(){
    if ($('.lineColIndicator:first').is(":visible")
        && $('.lineColIndicator:eq(1)').is(":visible")){ //see if a pair of lines are visible just in case you checked the active line first.
        $('.lineColIndicator').hide();
        $(".activeLine").show().addClass("linesHidden");
    }
    else {
        $('.lineColIndicator').show();
        $(".lineColIndicator").removeClass("linesHidden");
        $.each($(".lineColOnLine"), function(){$(this).css("line-height", $(this).height() + "px"); });
    }
}

/* Toggle the drawn lines in the transcription interface. */
function toggleLineCol(){
    if ($('.lineColOnLine:first').is(":visible")){
        $('.lineColOnLine').hide();
    }
    else{
        $('.lineColOnLine').show();
        $.each($(".lineColOnLine"), function(){$(this).css("line-height", $(this).height() + "px"); });
    }
}

function updateLinesInColumn(column){
    var startLineID = column[0];
    var endLineID = column[1];
    var startLine = $(".parsing[lineserverid='" + startLineID + "']"); //Get the start line
    var nextLine = startLine.next(".parsing"); //Get the next line (potentially)
    var linesToUpdate = [];
    linesToUpdate.push(startLine); //push first line
    while (nextLine.length > 0 && nextLine.attr("lineserverid") !== endLineID){ //if there is a next line and its not the last line in the column...
        linesToUpdate.push(nextLine);
        nextLine = nextLine.next(".parsing");
    }
    if (startLineID !== endLineID){ //push the last line, so long as it was also not the first line
        linesToUpdate.push($(".parsing[lineserverid='" + endLineID + "']")); //push last line
    }
    columnUpdate(linesToUpdate);
}

/* Bulk update for lines in a column. */
function columnUpdate(linesInColumn){
    var onCanvas = $("#transcriptionCanvas").attr("canvasid");
    var currentFolio = parseInt(tpen.screen.currentFolio);
    var currentAnnoListID = tpen.screen.currentAnnoListID;
    var currentAnnoListResources = [];
    var lineTop, lineLeft, lineWidth, lineHeight = 0;
    var ratio = originalCanvasWidth2 / originalCanvasHeight2;
    var currentAnnoList = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], false, false);
        //Go over each line from the column resize.
    $.each(linesInColumn, function(){
        var line = $(this);
        lineTop = parseFloat(line.attr("linetop")) * 10;
        lineLeft = parseFloat(line.attr("lineleft")) * (10 * ratio);
        lineWidth = parseFloat(line.attr("linewidth")) * (10 * ratio);
        lineHeight = parseFloat(line.attr("lineheight")) * 10;
        //round up.
        lineTop = Math.round(lineTop, 0);
        lineLeft = Math.round(lineLeft, 0);
        lineWidth = Math.round(lineWidth, 0);
        lineHeight = Math.round(lineHeight, 0);
        line.css("width", line.attr("linewidth") + "%");
        var lineString = lineLeft + "," + lineTop + "," + lineWidth + "," + lineHeight;
        var currentLineServerID = line.attr('lineserverid');
        var currentLineText = $(".transcriptlet[lineserverid='" + currentLineServerID + "']").find("textarea").val();
        var dbLine = {
            "@id" : currentLineServerID,
            "@type" : "oa:Annotation",
            "motivation" : "sc:painting",
            "resource" : {
                "@type" : "cnt:ContentAsText",
                "cnt:chars" : currentLineText
            },
            "on" : onCanvas + "#xywh=" + lineString,
            "otherContent" : [],
            "forProject": tpen.manifest['@id'],
            "testing":"TPEN28"
        };
        var index = - 1;
        //find the line in the anno list resources and replace its position with the new line resource.
        $.each(currentAnnoList, function(){
            index++;
            if (this["@id"] == currentLineServerID){
                currentAnnoList[index] = dbLine;
                return false;
            }
        });
    });
    //Now that all the resources are edited, update the list.
    var url = "updateAnnoList";
    var paramObj = {
        "@id":currentAnnoListID,
        "resources": currentAnnoListResources
    };
    var params = {"content":JSON.stringify(paramObj)};
    $.post(url, params, function(data){
        //currentFolio = parseInt(currentFolio);
        //annoLists[currentFolio - 1] = currentAnnoListID;
    });
    
}
    function drawLinesOnCanvas(lines, parsing, tool){
        if (lines.length > 0){
            $("#transTemplateLoading").hide();
            $("#transcriptionTemplate").show();
            linesToScreen(lines, tool);
        }
        else { //list has no lines
            if (parsing !== "parsing") {
                $("#noLineWarning").show();
            }
            $("#transTemplateLoading").hide();
            $("#transcriptionTemplate").show();
            $('#transcriptionCanvas').css('height', $("#imgTop img").height() + "px");
            $('.lineColIndicatorArea').css('height', $("#imgTop img").height() + "px");
            $("#imgTop").css("height", $("#imgTop img").height() + "px");
            $("#imgTop img").css("top", "0px");
            $("#imgBottom").css("height", "inherit");
            $("#parsingBtn").css("box-shadow", "0px 0px 6px 5px yellow");
        }
    }

    function getList(canvas, drawFlag, parsing, tool){ //this could be the @id of the annoList or the canvas that we need to find the @id of the list for.
        var lists = [];
        var annos = [];
        if(tpen.screen.dereferencedLists[tpen.screen.currentFolio]){
            annos = tpen.screen.dereferencedLists[tpen.screen.currentFolio].resources;
            tpen.screen.currentAnnoListID = tpen.screen.dereferencedLists[tpen.screen.currentFolio]["@id"];
            if(drawFlag){
                drawLinesOnCanvas(annos, parsing, tool);
            }
            return annos;
        }
        else{
            if(canvas.otherContent){
                lists = canvas.otherContent;
            }
            else{
                console.warn("canvas to get anno list for does not have otherContent");
                lists = "noList";
            }
            for(var i=0; i<lists.length; i++){
                var list = lists[i];
                $.get(list, function(annoListData){
                    if(annoListData.proj === parseInt(tpen.project.id)){
                        tpen.screen.currentAnnoListID = list;
                        tpen.screen.dereferencedLists[tpen.screen.currentFolio] = annoListData;
                        if (annoListData.resources) {
                            var resources = annoListData.resources;
                            //Here is when we would set empty, but its best just to return the empty array.  Maybe get rid of "empty" check in this file. 
                            for(var l=0; l<resources.length; l++){
                                var currentResource = resources[l];
                                if(currentResource.on.startsWith(canvas['@id'])){
                                     annos.push(currentResource);
                                 } 
                            }
                        }
                        if(drawFlag){
                            drawLinesOnCanvas(annos, parsing, tool);
                        }
                        return annos;
                    }
                });
            }
        }
        
    };

/* Update line information for a particular line. */
function updateLine(line, cleanup, updateList){
    console.log("update line function");
    var onCanvas = $("#transcriptionCanvas").attr("canvasid");
    var currentAnnoList = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], false, false);
    var lineTop, lineLeft, lineWidth, lineHeight = 0;
    var ratio = originalCanvasWidth2 / originalCanvasHeight2;
    lineTop = parseFloat(line.attr("linetop")) * 10;
    lineLeft = parseFloat(line.attr("lineleft")) * (10 * ratio);
    lineWidth = parseFloat(line.attr("linewidth")) * (10 * ratio);
    lineHeight = parseFloat(line.attr("lineheight")) * 10;
    //round up.
    lineTop = Math.round(lineTop, 0);
    lineLeft = Math.round(lineLeft, 0);
    lineWidth = Math.round(lineWidth, 0);
    lineHeight = Math.round(lineHeight, 0);
    var lineString = lineLeft + "," + lineTop + "," + lineWidth + "," + lineHeight;
    var currentLineServerID = line.attr('lineserverid');
    var currentLineText = $(".transcriptlet[lineserverid='" + currentLineServerID + "']").find("textarea").val();
    var currentAnnoListID = tpen.screen.currentAnnoListID;
    var dbLine = {
        "@id" : currentLineServerID,
        "@type" : "oa:Annotation",
        "motivation" : "oad:transcribing",
        "resource" : {
            "@type" : "cnt:ContentAsText",
            "cnt:chars" : currentLineText
        },
        "on" : onCanvas + "#xywh=" + lineString,
        "otherContent" : [],
        "forProject": tpen.manifest['@id'],
        "testing":"TPEN28"
    };
    if (!currentAnnoListID){
        if(!currentAnnoList){
            throw new Error("No annotation list found.");
        } else if (typeof currentAnnoList==="string"){
            // unlikely, but just in case
            $.getJSON(currentAnnoList,function(list){
                tpen.screen.currentAnnoList = tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio].otherContent[tpen.screen.currentAnnoList] = list;
                return updateLine(line, cleanup, updateList);
            }).fail(function(err){
                throw err;
            });
        } else if ($.isArray(currentAnnoList.resources)){
            throw new Error("Everything looks good, but it didn't work.");
        } else {
            throw new Error("Annotation List was not recognized.");
        }
    }
    else if (currentAnnoListID){
        if(currentLineServerID.startsWith("http")){
            //var url = "http://165.134.241.141/annotationstore/anno/updateAnnotation.action"; //This gets a 403 Forbidden....
            var url = "http://165.134.241.141/TPEN28/updateAnnoList";
            var payload = { // Just send what we expect to update
                    content : JSON.stringify({
                    "@id" : dbLine['@id'],			// URI to find it in the repo
                    "resource" : dbLine.resource,	// the transcription content
                    "on" : dbLine.on 				// parsing update of xywh=
            	})
            };
            if(updateList){
                var url1 = "updateAnnoList";
                console.log("update list");
                for(var i=0  ;i < currentAnnoList.length; i++){
                    if(currentAnnoList[i]["@id"] === dbLine['@id']){
                        currentAnnoList[i].on = dbLine.on;
                    }
                    if(i===currentAnnoList.length -1){
                        var paramObj1 = {"@id":tpen.screen.currentAnnoListID, "resources": currentAnnoList};
                        var params1 = {"content":JSON.stringify(paramObj1)};
                        $.post(url1, params1, function(data){
                        });
                    }
                }
                
            }
            console.log("update line");
            $.post(url,payload,function(){
            	line.attr("hasError",null);
                $("#parsingCover").hide();
            	// success
            }).fail(function(err){
            	line.attr("hasError","Saving Failed "+err.status);
            	throw err;
            });
        } else {
            throw new Error("No good. The ID could not be dereferenced. Maybe this is a new annotation?");
        }
    }
    //I am not sure if cleanup is ever true
    if (cleanup) cleanupTranscriptlets(true);
}

function saveNewLine(lineBefore, newLine){
    var theURL = window.location.href;
    var projID = - 1;
    if (theURL.indexOf("projectID") === - 1){
        projID = tpen.project.id;
    }
    else{
        projID = getURLVariable("projectID");
    }
    var beforeIndex = - 1;
    if (lineBefore !== undefined && lineBefore !== null){
        beforeIndex = parseInt(lineBefore.attr("linenum"));
    }
    var onCanvas = $("#transcriptionCanvas").attr("canvasid");
    var newLineTop, newLineLeft, newLineWidth, newLineHeight = 0;
    var oldLineTop, oldLineLeft, oldLineWidth, oldLineHeight = 0;
    var ratio = originalCanvasWidth2 / originalCanvasHeight2;
    newLineTop = parseFloat(newLine.attr("linetop"));
    newLineLeft = parseFloat(newLine.attr("lineleft"));
    newLineWidth = parseFloat(newLine.attr("linewidth"));
    newLineHeight = parseFloat(newLine.attr("lineheight"));
    newLineTop = newLineTop * 10;
    newLineLeft = newLineLeft * (10 * ratio);
    newLineWidth = newLineWidth * (10 * ratio);
    newLineHeight = newLineHeight * 10;
    //round up.
    newLineTop = Math.round(newLineTop, 0);
    newLineLeft = Math.round(newLineLeft, 0);
    newLineWidth = Math.round(newLineWidth, 0);
    newLineHeight = Math.round(newLineHeight, 0);
    
    if(lineBefore){
        oldLineTop = parseFloat(lineBefore.attr("linetop"));
        oldLineLeft = parseFloat(lineBefore.attr("lineleft"));
        oldLineWidth = parseFloat(lineBefore.attr("linewidth"));
        oldLineHeight = parseFloat(lineBefore.attr("lineheight"));
        oldLineTop = oldLineTop * 10;
        oldLineLeft = oldLineLeft * (10 * ratio);
        oldLineWidth = oldLineWidth * (10 * ratio);
        oldLineHeight = oldLineHeight * 10;
        //round up.
        oldLineTop = Math.round(oldLineTop, 0);
        oldLineLeft = Math.round(oldLineLeft, 0);
        oldLineWidth = Math.round(oldLineWidth, 0);
        oldLineHeight = Math.round(oldLineHeight, 0);
    }
    var lineString = onCanvas + "#xywh=" + newLineLeft + "," + newLineTop + "," + newLineWidth + "," + newLineHeight;
    var updateLineString =  onCanvas + "#xywh=" + oldLineLeft + "," + oldLineTop + "," + oldLineWidth + "," + oldLineHeight;
    var currentLineText = "";
    var dbLine = {
        "@id" : "",
        "@type" : "oa:Annotation",
        "motivation" : "oad:transcribing",
        "resource" : {
            "@type" : "cnt:ContentAsText",
            "cnt:chars" : currentLineText
        },
        "on" : lineString,
        "otherContent":[],
        "forProject": tpen.manifest['@id'],
        "testing":"TPEN28"
    };
    var url = "saveNewTransLineServlet";
    var paramOBJ = dbLine;
    var params = {"content" : JSON.stringify(paramOBJ)};
    if (onCanvas !== undefined && onCanvas !== ""){
        $.post(url, params, function(data){
            data = JSON.parse(data);
            dbLine["@id"] = data["@id"];
            newLine.attr("lineserverid", data["@id"]);
            $("div[newcol='" + true + "']").attr({
                "startid" : dbLine["@id"],
                "endid" : dbLine["@id"],
                "newcol":false
            });
            var currentFolio = tpen.screen.currentFolio;
            var currentAnnoList = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], false, false);
            if (currentAnnoList !== "noList" && currentAnnoList !== "empty"){
                // if it IIIF, we need to update the list
                if (beforeIndex == - 1){
                    $(".newColumn").attr({
                        "lineserverid" : dbLine["@id"],
                        "linenum" : $(".parsing").length
                    }).removeClass("newColumn");
                    currentAnnoList.push(dbLine);
                }
                else {
                    currentAnnoList.splice(beforeIndex + 1, 0, dbLine);
                    currentAnnoList[beforeIndex].on = updateLineString;
                }
                currentFolio = parseInt(currentFolio);
                //Write back to db to update list
                var url1 = "updateAnnoList";
                var paramObj1 = {"@id":tpen.screen.currentAnnoListID, "resources": currentAnnoList};
                var params1 = {"content":JSON.stringify(paramObj1)};
                $.post(url1, params1, function(data){
                    if (lineBefore !== undefined && lineBefore !== null){
                        //This is the good case.  We called split line and saved
                        //the new line, now we need to update the other one.
                        updateLine(lineBefore, false, false); //This will update the line on the server.
                    }
                    else{
                    }
                        $("#parsingCover").hide();
                });

            }
            else if (currentAnnoList == "empty"){
                //This means we know no AnnotationList was on the store for this canvas,
                //and otherContent stored with the canvas object did not have the list.
                // Make a new one in this case.
                var newAnnoList = {
                    "@type" : "sc:AnnotationList",
                    "on" : onCanvas,
                    "originalAnnoID" : "",
                    "version" : 1,
                    "permission" : 0,
                    "forkFromID" : "",
                    "resources" : [],
                    "proj" : projID,
                    "testing":"TPEN28"
                };
                var url2 = "saveNewTransLineServlet";
                var params2 = {"content": JSON.stringify(newAnnoList)};
                $.post(url2, params2, function(data){ //save new list
                    data = JSON.parse(data);
                    var newAnnoListCopy = newAnnoList;
                    newAnnoListCopy["@id"] = data["@id"];
                    currentFolio = parseInt(currentFolio);
                    tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio].otherContent.push(newAnnoListCopy);
                    var url3 = "updateAnnoList";
                    var paramObj3 = {"@id":newAnnoListCopy["@id"], "resources": [dbLine]};
                    var params3 = {"content":JSON.stringify(paramObj3)};
                    $.post(url3, params3, function(data){
                        $(".newColumn").attr({
                            "lineserverid" : dbLine["@id"],
                            "startid" : dbLine["@id"],
                            "endid" : dbLine["@id"],
                            "linenum" : $(".parsing").length
                        }).removeClass("newColumn");
                        newLine.attr("lineserverid", dbLine["@id"]);
                        $("#parsingCover").hide();
                    });
                });
            }
            else if (currentAnnoList == "noList"){
                //noList is a special scenario for handling classic T-PEN objects.
                if (beforeIndex == - 1){ //New line vs new column
                    $(".newColumn").attr({
                        "lineserverid" : dbLine["@id"],
                        "startid" : dbLine["@id"],
                        "endid" : dbLine["@id"],
                        "linenum" : $(".parsing").length
                    }).removeClass("newColumn");
                    currentFolio = parseInt(currentFolio);
                    tpen.manifest.sequences[0].canvases[currentFolio - 1].resources.push(dbLine);
                }
                else {
                    currentFolio = parseInt(currentFolio);
                    tpen.manifest.sequences[0].canvases[currentFolio - 1].resources.splice(beforeIndex + 1, 0, dbLine);
                }
                $("#parsingCover").hide();
                // QUERY: should we write to the DB here?  This would be in support of old data.
            }
            cleanupTranscriptlets(true);
        });
    }
    else{
        throw new Error("Cannot save line.  Canvas id is not present.");
    }
}

/**
 * Inserts new transcriptlet when line is added.
 * Cleans up inter-transcriptlet relationships afterwards.
 *
 * @param e line element to build transcriptlet from
 * @param afterThisID lineid of line before new transcriptlet
 * @param newLineID lineid of new line
 */
function buildTranscriptlet(e, afterThisID, newServerID){
    var newLineID = $(".transcriptlet").length + 1;
    var isNotColumn = true;
    var newW = e.attr("linewidth");
    var newX = e.attr("lineleft");
    var newY = e.attr("linetop");
    var newH = e.attr("lineheight");
    if (afterThisID === - 1) {
        // new column, find its placement
        afterThisID = $(".transcriptlet").eq( - 1).attr("lineserverid") || - 1;
        $(".transcriptlet").each(function(index) {
            if ($(this).find('lineLeft') > newX) {
                afterThisID = (index > 0) ? $(this).prev('.transcriptlet').attr("lineserverid") : - 1;
                return false;
            }
        });
        isNotColumn = false;
    }
    var $afterThis = $(".transcriptlet[lineserverid='" + afterThisID + "']");
    var newTranscriptlet = [
        "<div class='transcriptlet transcriptletBefore' id='transciptlet_", newLineID,
        "' lineserverid='", newServerID, // took out style DEBUG
        "lineheight= ", newH,
        "linewidth= ", newW,
        "linetop= ", newY,
        "lineleft= ", newX,
        "lineid= ", ,
        "col= ", ,
        "collinenum= ", ,
        "'>\n",
        "<span class='counter wLeft ui-corner-all ui-state-active ui-button'>Inserted Line</span>\n",
        "<textarea></textarea>\n",
        "</div>"].join("");
    if (isNotColumn){
        //update transcriptlet that was split
        $afterThis.after(newTranscriptlet).find(".lineHeight")
            .val($(".parsing[lineserverid='" + afterThisID + "']")
            .attr("lineheight"));
    }
    else {
        if (afterThisID === - 1) {
            $("#entry").prepend(newTranscriptlet.join(""));
        }
        else {
            $afterThis.after(newTranscriptlet.join(""));
        }
    }
    $(e).attr("lineserverid", newServerID);
}

/**
 * Adds a line by splitting the current line where it was clicked.
 *
 * @param e clicked line element
 * @see organizePage(e)
 */
function splitLine(e, event){
    //e is the line that was clicked in
    //This is where the click happened relative to img top.  In other words, where the click happened among the lines.
    var originalLineHeight = $(e).height(); //-1 take one px off for the border
    $(".parsing").attr("newline", "false");
    var originalLineTop = $(e).offset().top - $("#imgTop").offset().top; // +1 Move down one px for the border.
    var clickInLines = event.pageY - $("#imgTop").offset().top;
    var lineOffset = $(e).offset().top - $("#imgTop").offset().top;
    var oldLineHeight = (clickInLines - lineOffset) / $("#imgTop").height() * 100;
    var newLineHeight = (originalLineHeight - (clickInLines - originalLineTop)) / $("#imgTop").height() * 100;
    var newLineTop = (clickInLines / $("#imgTop").height()) * 100;
    var newLine = $(e).clone(true);
    $(e).css({
        "height"    :   oldLineHeight + "%"
    }).attr({
        "newline"   :   true,
        "lineheight" :  oldLineHeight
    });
    $(newLine).css({
        "height"    :   newLineHeight + "%",
        "top"       :   newLineTop + "%"
    }).attr({
        "newline"   :   true,
        "linetop"   :   newLineTop,
        "lineheight" : newLineHeight
    });
    $(e).after(newLine);
    var newNum = - 1;
    $.each($(".parsing"), function(){
        newNum++;
        $(this).attr("linenum", newNum);
    });
    saveNewLine($(e), newLine);
    $("#progress").html("Line Added").fadeIn(1000).delay(3000).fadeOut(1000);
}

/**
 * Removes clicked line, merges if possible with the following line.
 * updateLine(e,additionalParameters) handles the original, resized line.
 *
 * @param e clicked line element from lineChange(e) via saveNewLine(e)
 * @see lineChange(e)
 * @see saveNewLine(e)
 */
function removeLine(e, columnDelete){
    $("#imageTip").hide();
    var removedLine = $(e);
    if (columnDelete){
        var lineID = "";
        removedLine.remove();
        return false;
    }
    else {
        if ($(e).attr("lineleft") == $(e).next(".parsing").attr("lineleft")) {
            removedLine = $(e).next();
            var removedLineHeight = removedLine.height();
            var currentLineHeight = $(e).height();
            var newLineHeight = removedLineHeight + currentLineHeight;
            var convertedNewLineHeight = newLineHeight / $("#imgTop").height() * 100;
            $(e).css({
                "height" :  convertedNewLineHeight + "%",
                "top" :     $(e).css("top")
            }).addClass("newDiv").attr({
                "lineheight":   convertedNewLineHeight
            });
        } else if ($(e).hasClass("deletable")){
            var cfrm = confirm("Removing this line will remove any data contained as well.\n\nContinue?");
            if (!cfrm){
                $("#parsingCover").hide();
                return false;
            }
            tpen.screen.isDestroyingLine = true;
        }
        var params = new Array({name:"remove", value:removedLine.attr("lineserverid")});
        removedLine.remove();
        removeTranscriptlet(removedLine.attr("lineserverid"), $(e).attr("lineserverid"), true, "cover");
        return params;
    }
}

/**
 * Removes transcriptlet when line is removed. Updates transcriplet
 * if line has been merged with previous.
 *
 * @param lineid lineid to remove
 * @param updatedLineID lineid to be updated
 */
function removeTranscriptlet(lineid, updatedLineID, draw, cover){
    //update remaining line, if needed
    $("#parsingCover").show();
    var updateText = "";
    var removeNextLine = false;
    if (lineid !== updatedLineID){
        removeNextLine = true;
        var updatedLine = $(".parsing[lineserverid='" + updatedLineID + "']");
        var removedLine1 = $(".parsing[lineserverid='" + lineid + "']");
        var removedLine2 = $(".transcriptlet[lineserverid='" + lineid + "']");
        var toUpdate = $(".transcriptlet[lineserverid='" + updatedLineID + "']");
        var removedText = $(".transcriptlet[lineserverid='" + lineid + "']").find("textarea").val();
        toUpdate.find("textarea").val(function(){
            var thisValue = $(this).val();
            if (removedText !== undefined){
                if (removedText !== "") thisValue += (" " + removedText);
                updateText = thisValue;
            }
            return thisValue;
        });
        var lineHeightForUpdate = parseFloat(toUpdate.attr("lineheight")) + parseFloat(removedLine2.attr("lineheight"));
        toUpdate.attr("lineheight", lineHeightForUpdate);
    }
    else {
    }
    var currentFolio = parseInt(tpen.screen.currentFolio);
    var currentAnnoList = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], false, false);
    if (currentAnnoList !== "noList" && currentAnnoList !== "empty"){ 
        $.each(currentAnnoList, function(index){
            var lineIDToCheck = "";
            if (removeNextLine){
                lineIDToCheck = lineid;
                removedLine2.remove(); //remove the transcriptlet from UI
            }
            else{
                lineIDToCheck = updatedLineID;
            }
            if (this["@id"] === lineIDToCheck){
                currentAnnoList.splice(index, 1);
                var url = "updateAnnoList";
                tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio] = currentAnnoList;
                var paramObj = {"@id":tpen.screen.currentAnnoListID, "resources": currentAnnoList};
                var params = {"content":JSON.stringify(paramObj)};
                $.post(url, params, function(data){
                    if (!removeNextLine){
                        $("#parsingCover").hide();
                    }
                    else {
                        updateLine(toUpdate, false, false); //put $(#parsingCover).hide() in updateLine(), but may just need it here for this scenario. 
                    }
                });
            }
        });
    }
    else if (currentAnnoList == "empty"){
        throw new Error("There is no anno list assosiated with this anno.  This is an error.");
    }
    else { // If it is classic T-PEN, we need to update canvas resources
        currentFolio = parseInt(currentFolio);
        $.each(tpen.manifest.sequences[0].canvases[currentFolio - 1].resources, function(){
            index++;
            if (this["@id"] == lineid){
                tpen.manifest.sequences[0].canvases[currentFolio - 1].resources.splice(index, 1);
                //update for real
            }
        });
    }
    //When it is just one line being removed, we need to redraw.  When its the whole column, we just delete.
    cleanupTranscriptlets(draw);
}

/* Remove all transcriptlets in a column */
function removeColumnTranscriptlets(lines, recurse){
    var currentAnnoList = getList(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], false, false);
    $("#parsingCover").show();
    if (currentAnnoList !== "noList" && currentAnnoList !== "empty"){
        // if it IIIF, we need to update the list
            for (var l = lines.length - 1; l >= 0; l--){
                var theLine = $(lines[l]);
                for(var k=0; k<currentAnnoList.length; k++){
                    var currentResource = currentAnnoList[k];
                    if (currentResource["@id"] == theLine.attr("lineserverid")){
                        currentAnnoList.splice(k, 1);
                        theLine.remove();
                    }
                }
                if (l === 0){
                    var url = "updateAnnoList";
                    var paramObj = {"@id":tpen.screen.currentAnnoListID, "resources": currentAnnoList};
                    var params = {"content":JSON.stringify(paramObj)};
                    tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio] = currentAnnoList;
                    $.post(url, params, function(data){
                        if (recurse){
                            tpen.screen.nextColumnToRemove.remove();
                            // FIXME: I cannot find this object always?
                            destroyPage();
                        }
                        else{
                            cleanupTranscriptlets(true);
                            tpen.screen.nextColumnToRemove.remove();
                            $("#parsingCover").hide();
                        }
                    });
                }
            }
    }
    else {
        //It was not a part of the list, but we can still cleanup the transcriptlets from the interface. This could happen when a object is fed to the
        //transcription textarea who instead of using an annotation list used the resources[] field to store anno objects directly with the canvas.
        //These changes will not save, they are purely UI manipulation.  An improper, view only object has been fed to the interface at this point, so this is intentional.
        for (var l = lines.length - 1; l >= 0; l--){
            var theLine = $(lines[l]);
            theLine.remove();
            var lineID = theLine.attr("lineserverid");
            $(".transcriptlet[lineserverid='" + lineID + "']").remove(); //remove the transcriptlet
            $(".lineColIndicator[lineserverid='" + lineID + "']").remove(); //Remove the line representing the transcriptlet
            $(".previewLineNumber[lineserverid='" + lineID + "']").parent().remove(); //Remove the line in text preview of transcription.
            $("#parsingCover").hide()
        }
    }
}

/* Re draw transcriptlets from the Annotation List information. */
function cleanupTranscriptlets(draw) {
    var transcriptlets = $(".transcriptlet");
    if (draw){
        transcriptlets.remove();
        $(".lineColIndicatorArea").children(".lineColIndicator").remove();
        $("#parsingSplit").find('.fullScreenTrans').unbind();
        $("#parsingSplit").find('.fullScreenTrans').bind("click", function(){
            fullPage();
            drawLinesToCanvas(tpen.manifest.sequences[0].canvases[tpen.screen.currentFolio], "");
        });
    }
}

/* Make some invalid information inside of folios valid empties */
function scrubFolios(){
    //you could even force create anno lists off of the existing resource here if you would like.
    var cnt1 = - 1;
    $.each(tpen.manifest.sequences[0].canvases, function(){
        cnt1++;
        var canvasObj = this;
        if (canvasObj.resources && canvasObj.resources.length > 0){
            if (canvasObj.images === undefined || canvasObj.images === null){
                canvasObj.images = [];
            }
            var cnt2 = - 1;
            $.each(canvasObj.resources, function(){
                cnt2 += 1;
                if (this.resource && this.resource["@type"] && this.resource["@type"] === "dctypes:Image"){
                    canvasObj.images.push(this);
                    canvasObj.resources.splice(cnt2, 1);
                    tpen.manifest.sequences[0].canvases[cnt1] = canvasObj;
                }
            });
        }
        if (canvasObj.otherContent === undefined){
            tpen.manifest.sequences[0].canvases[cnt1].otherContent = [];
        }
    });
}



/*
 * Load all included Iframes on the page.  This function should be strategically placed so that the Iframes load after user and project information
 * are gathered.  This should help avoid timeouts caused by embedded Iframes wait times mixed with many calls to the annotation store and calls for images.
 * See the Network console in the Browser deveoper tools for problems with wait times on embedded content.
 *
 * @see newberryTrans.html to find the iframe elements.
 */
function loadIframes(){
    $.each($("iframe"), function(){
        var src = $(this).attr("data_src");
        $(this).attr("src", src);
    });
}

function toggleImgTools(event){
    var locationX = event.pageX;
    var locationY = event.pageY;
    $("#imageTools").css({
        "display":  "block",
        "left" : locationX + "px",
        "top" : locationY + 15 + "px"
    });  
    $("#imageTools").draggable();
}

/* Control the hiding and showing of the image tools in the transcription interface. Depricated
function toggleImgTools(){
    if ($("#imageTools").attr("class") !== undefined && $("#imageTools").attr("class").indexOf("activeTools") > - 1){
        $('.toolWrap').hide();
        $("#imageTools").removeClass("activeTools");
        $("#activeImageTool").children("i").css("transform", "rotate(180deg)");
    }
    else{
        $("#imageTools").addClass("activeTools");
        $('.toolWrap').show();
        $("#activeImageTool").children("i").css("transform", "rotate(0deg)");
    }
}
*/
function toggleLineControls(event){
    var locationX = event.pageX;
    var locationY = event.pageY;
    $("#lineColControls").css({
        "display":  "block",
        "left" : locationX + "px",
        "top" : locationY + 15 + "px"
    });
    $("#lineColControls").draggable();
}

/* Allows users to slightly adjust a line within a column while within the transcription interface. */
function bumpLine(direction, activeLine){
    //values are in pixel, not percentage.
    var id = activeLine.attr("lineserverid");
    if(direction === "left"){
        var currentLineLeft = activeLine.css("left");
        var currentLineLeftPerc = (parseFloat(currentLineLeft) / $("#imgTop").width()) * 100;
        if (currentLineLeftPerc > .3){
            currentLineLeftPerc -= .3;
        }
        else{ //no negative left value
            currentLineLeftPerc = 0.0;
        }
       currentLineLeft = "%"+currentLineLeftPerc;
    }
    else if (direction === "right"){
        var currentLineLeft = activeLine.css("left");
        var currentLineLeftPerc = (parseFloat(currentLineLeft) / $("#imgTop").width()) * 100;
        if (currentLineLeftPerc < 99.7){ //no left values greater than 100
            currentLineLeftPerc += .3;
        }
        else{
            currentLineLeftPerc = 100.0;
        }
       currentLineLeft = "%"+currentLineLeftPerc;
    }
    var currentLineLeftPX = parseFloat(currentLineLeftPerc/100) * $("#imgTop").width() + "px";
    $(".transcriptlet[lineserverid='"+id+"']").attr("lineleft", currentLineLeftPerc);
    activeLine.css("left", currentLineLeftPX);
    //updateLine($(".transcriptlet[lineserverid='"+id+"']"), false, true);      
}


    //UI effects for when a user decides to edit a line within the preview split tool.
    function edit(line){
        var focusLineID = $(line).siblings(".previewLineNumber").attr("lineserverid");
        var transcriptionText = ($(line).hasClass("previewText")) ? ".theText" : ".notes";
        var pair = $(".transcriptlet[lineserverid='"+focusLineID+"']").find(transcriptionText);
        var transcriptlet = $(".transcriptlet[lineserverid='"+focusLineID+"']");
        if ($(line).hasClass("currentPage")){
          if (pair.parent().attr('id') !== tpen.screen.focusItem[1].attr('id')){
              updatePresentation(transcriptlet);
          }
            line.focus();
            $(line).keyup(function(){
                //Data.makeUnsaved();
                pair.val($(this).text());
            });
        }
        else {
            //console.log("NOt the current page.")
        }
    }

    function scrollToCurrentPage(){
        var pageOffset = $(".previewPage").filter(":first").height() + 20;
        $("#previewDiv").animate({
            scrollTop:pageOffset
        },500);
        $("#previewNotes").filter(".ui-state-active").each( // pulled out #previewAnnotations
            function(){
                if ($("."+this.id).is(":hidden")){
                    $("."+this.id).show();
                    scrollToCurrentPage();
                }
            });
    }

    /*
     * Get the inline 'style' attribute of the transcription image containers and attach brightness or contrast filters to it.
     * The entire function is string manipulation and runs quickly.
     */
    function imageSlider(){
        var newFilter = "";
        if(navigator.userAgent.indexOf("Chrome") !== -1 ) { //also works with filter
          newFilter = "-webkit-filter:";
        }
        else if(navigator.userAgent.indexOf("Opera") !== -1 ) {
          newFilter = "-o-filter:";
        }
        else if(navigator.userAgent.indexOf("MSIE") !== -1 ) { //also works with filter
          newFilter= "-ms-filter:";
        }
        else if(navigator.userAgent.indexOf("Firefox") !== -1 ) { //-moz-filter does not work in more recent versions.  The newest version works with regular filter.
          newFilter = "filter:";
        }
        else {
            //Uncomment this code to alert the user that their browser does not support these CSS3 Filter tools.

//          alert("This tool is not supported by your browser.  The supported browsers are:\n\n\
//                Google Chrome\n Microsoft Internet Explorer\nOpera\nLatest version of Firefox");
//          return false;
        }
        var imgTop = document.getElementById("imgTop");
        var imgBtm = document.getElementById("imgBottom");
        var currentStyleTop = imgTop.getAttribute("style");
        var currentStyleBtm = imgBtm.getAttribute("style"); //null if there is no inline style (this happens)
        if (currentStyleTop === "null" || currentStyleTop === null) currentStyleTop = ""; //in case there is no inline style becase that returns null
        if (currentStyleBtm === "null" || currentStyleBtm === null) currentStyleBtm = ""; //in case there is no inline style because that returns null
        var alteredStyleTop = "";
        var alteredStyleBottom = "";
        var pieceToRemoveTop = "";
        var pieceToRemoveBtm = "";
        //Account for the different ways filter can be represented and alter it accordingly
        if(currentStyleTop.indexOf("-webkit-filter") >= 0){ //Chrome
          //Remove current filter string from the style attribute
          pieceToRemoveTop = currentStyleTop.substring(currentStyleTop.indexOf("-webkit-filter"), currentStyleTop.lastIndexOf(";") + 1);
          pieceToRemoveBtm = currentStyleBtm.substring(currentStyleBtm.indexOf("-webkit-filter"), currentStyleBtm.lastIndexOf(";") + 1);

          //Put the pieces of the filter together
          newFilter += " brightness("+$("#brightnessSlider").slider("value")+"%) contrast("+$("#contrastSlider").slider("value")+"%);";
          alteredStyleTop = currentStyleTop.replace(pieceToRemoveTop, "") + newFilter;
          alteredStyleBottom = currentStyleBtm.replace(pieceToRemoveBtm, "") + newFilter;
        }
        else if(currentStyleTop.indexOf("-ms-filter") >= 0){ //microsoft browsers
          pieceToRemoveTop = currentStyleTop.substring(currentStyleTop.indexOf("-ms-filter"), currentStyleTop.lastIndexOf(";") + 1);
          pieceToRemoveBtm = currentStyleBtm.substring(currentStyleBtm.indexOf("-ms-filter"), currentStyleBtm.lastIndexOf(";") + 1);
          //Put the pieces of the filter together
          newFilter += " brightness("+$("#brightnessSlider").slider("value")+"%) contrast("+$("#contrastSlider").slider("value")+"%);";
          alteredStyleTop = currentStyleTop.replace(pieceToRemoveTop, "") + newFilter;
          alteredStyleBottom = currentStyleBtm.replace(pieceToRemoveBtm, "") + newFilter;
        }
        else if(currentStyleTop.indexOf("-o-filter") >= 0){ //Opera
          //Remove current filter string from the style attribute
          pieceToRemoveTop = currentStyleTop.substring(currentStyleTop.indexOf("-o-filter"), currentStyleTop.lastIndexOf(";") + 1);
          pieceToRemoveBtm = currentStyleBtm.substring(currentStyleBtm.indexOf("-o-filter"), currentStyleBtm.lastIndexOf(";") + 1);
          //Put the pieces of the filter together
          newFilter += " brightness("+$("#brightnessSlider").slider("value")+"%) contrast("+$("#contrastSlider").slider("value")+"%);";
          alteredStyleTop = currentStyleTop.replace(pieceToRemoveTop, "") + newFilter;
          alteredStyleBottom = currentStyleBtm.replace(pieceToRemoveBtm, "") + newFilter;
        }
        else if(currentStyleTop.indexOf("filter") >= 0){ //Works with firefox, IE and Chrome, but we specifically set prefixes at the beginning of the function.
          //Remove current filter string from the style attribute
          pieceToRemoveTop = currentStyleTop.substring(currentStyleTop.indexOf("filter"), currentStyleTop.lastIndexOf(";") + 1);
          pieceToRemoveBtm = currentStyleBtm.substring(currentStyleBtm.indexOf("filter"), currentStyleBtm.lastIndexOf(";") + 1);
          //Put the pieces of the filter together
          newFilter += " brightness("+$("#brightnessSlider").slider("value")+"%) contrast("+$("#contrastSlider").slider("value")+"%);";
          alteredStyleTop = currentStyleTop.replace(pieceToRemoveTop, "") + newFilter;
          alteredStyleBottom = currentStyleBtm.replace(pieceToRemoveBtm, "") + newFilter;
        }
        else{ //First time the filter is being added.  The prefix is already a part of newFilter, so we just need to add the rest of the string.
          newFilter += " brightness("+$("#brightnessSlider").slider("value")+"%) contrast("+$("#contrastSlider").slider("value")+"%);";
          alteredStyleTop = currentStyleTop + " "+newFilter;
          alteredStyleBottom = currentStyleBtm +" "+newFilter;
        }
        imgTop.setAttribute("style",alteredStyleTop); //set the style attribute with the appropriate filter string attached to it.
        imgBtm.setAttribute("style",alteredStyleBottom); //set the style attribute with the appropriate filter string attached to it.
    }

    /* Toggle grayscale and invert image CSS filters.v   */
    function toggleFilter(which){
        /*
         * The grayscale/invert toggle classes CANNOT BE DEFINED as the same object being given the brightness/contrast filters or they ALL BREAK
         */
        var filterObjectTop = document.getElementsByClassName("transcriptionImage")[0];
        var filterObjectBottom = document.getElementsByClassName("transcriptionImage")[1];
        var thisFilter = which+"Filter";
        if(filterObjectTop.className.indexOf(thisFilter) >= 0){
          filterObjectTop.className = filterObjectTop.className.replace(thisFilter, '');
          filterObjectBottom.className = filterObjectBottom.className.replace(thisFilter, '');
          $("button[which='"+which+"']").removeClass("selected");
        }
        else{
          filterObjectTop.className = filterObjectTop.className+=" "+thisFilter;
          filterObjectBottom.className = filterObjectBottom.className+=" "+thisFilter;
          $("button[which='"+which+"']").addClass("selected");
        }
    }

    //Make sure the value entered into the area that allows a user to define a custom ruler color is a valida color string.
    function validTextColour(stringToTest) {
        //Alter the following conditions according to your need.
        if (stringToTest === "") { return false; }
        if (stringToTest === "inherit") { return false; }
        if (stringToTest === "transparent") { return true; }

        var image = document.createElement("img");
        image.style.color = "rgb(0, 0, 0)";
        image.style.color = stringToTest;
        if (image.style.color !== "rgb(0, 0, 0)") { return true; }
        image.style.color = "rgb(255, 255, 255)";
        image.style.color = stringToTest;
        return image.style.color !== "rgb(255, 255, 255)";
    }

    //Change the ruler color.
    function rulerColor(color){
        if(color==="custom"){
            color=$("#customRuler").val();
            if(validTextColor(color)){

            }
            else{
                color = "red";
            }

        }
        $("#ruler1").css("color", color).css("background", color);
        $("#ruler2").css("color", color).css("background", color);
        $("#sampleRuler").css("color", color).css("background", color);
    }

    //Turn the ruler on
    function applyRuler(line){
            //var sRCbkp = selectRulerColor; //select Ruler Color backup
            $("#imageTip").html("Add a Line");
            if(!tpen.screen.isAddingLines){
                if (line.attr("lineleft") == line.next("div").attr("lineleft")) {
                    line.next("div").addClass('deletable');
                }
                line.addClass('deletable');
                if($(".deletable").size()>1){
                    $(".deletable").addClass("mergeable");
                    $("#imageTip").html("Merge Line");
                } else {
                    $("#imageTip").html("Delete Line");
                }
               //sRCbkp = 'transparent';
            }
            line.css('cursor','crosshair').bind('mousemove', function(e){
                var myLeft = line.position().left;
                var myWidth = line.width();
                $('#imageTip').show().css({
                    left:e.pageX,
                    top:e.pageY+20
                });
                $('#ruler1').show().css({
                    left: myLeft,
                    top: e.pageY,
                    height:'1px',
                    width:e.pageX-myLeft-7,
                    //background:"red"
                });
                $('#ruler2').show().css({
                    left: e.pageX+7,
                    top: e.pageY,
                    width:myWidth+myLeft-e.pageX-7,
                    height:'1px',
                   // background:"red"
                });
            });

    }
    /**
     * Hides ruler within parsing tool. Called on mouseleave .parsing.
     */
    function removeRuler(line){
        if(!tpen.screen.isAddingLines){$(".deletable").removeClass('deletable mergeable');}
        //line.unbind('mousemove');
        $('#imageTip').hide();
        $('#ruler1').hide();
        $('#ruler2').hide();
    }

    //Triggered when a user alters a line to either create a new one or destroy one.
    function lineChange(e,event){
        $("#parsingCover").show();
        if(tpen.screen.isAddingLines){
            splitLine(e,event);
        }
        else{
            //merge the line you clicked with the line below.  Delete the line below and grow this line by that lines height.
            removeLine(e);
        }

    }
    
function getURLVariable(variable)
{
       var query = window.location.search.substring(1);
       var vars = query.split("&");
       for (var i=0;i<vars.length;i++) {
               var pair = vars[i].split("=");
               if(pair[0] == variable){return pair[1];}
       }
       return(false);
}

// Shim console.log to avoid blowing up browsers without it - daQuoi?
if (!window.console) window.console = {};
    if (!window.console.log) window.console.log = function () { };
