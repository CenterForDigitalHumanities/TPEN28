    var transcriptionCanvases = [];
    var focusItem = [null,null];
    var transcriptionFile = "";
    var transcriptionObject = {};
    var projectID = 0;
    var dragHelper = "<div id='dragHelper'></div>";
    var liveTool = "none";
    var zoomMultiplier = 2;
    var isMagnifying = false;
    var currentFolio = 0; //The current folio number.  It runs from 1 -> infinity, remember to subtract 1 when referring to index.  
    var isFullscreen = true;
    var line = false;
    var isAddingLines = false;
    var charactersForButton = "";
    var tagsForButton = "";
    var colorList = ["rgba(153,255,0,.4)", "rgba(0,255,204,.4)", "rgba(51,0,204,.4)", "rgba(204,255,0,.4)", "rgba(0,0,0,.4)", "rgba(255,255,255,.4)", "rgba(255,0,0,.4)"];
    var colorThisTime = "rgba(255,255,255,.4)";
    var annoLists = [];
    var loggedInUser = false;
    var userIsAdmin = false;
    var adjustRatio = 0;
    //var basePath = window.location.protocol + "//" + window.location.host;
    
    /* Load the interface to the first page of the manifest. */
    function firstFolio(parsing){
        currentFolio = parseInt(currentFolio);
        if(parseInt(currentFolio) !== 1){
            if(parsing === "parsing"){
                $(".pageTurnCover").show();
                fullPage();
                focusItem = [null,null];
                currentFolio = 1;
                loadTranscriptionCanvas(transcriptionFolios[0], parsing);
                setTimeout(function(){
                hideWorkspaceForParsing();
                    $(".pageTurnCover").fadeOut(1500);
                }, 800);
            }
            else{
                focusItem = [null,null];
                currentFolio = 1;
                loadTranscriptionCanvas(transcriptionFolios[0], "");
            }
            
            
        }
    }
    
    /* Load the interface to the last page of the manifest. */
    function lastFolio(parsing){
        currentFolio = parseInt(currentFolio);
        var lastFolio = transcriptionFolios.length;
        if(parseInt(currentFolio) !== parseInt(lastFolio)){
            if(parsing === "parsing"){
                $(".pageTurnCover").show();
                fullPage();
                focusItem = [null,null];
                currentFolio = lastFolio;
                loadTranscriptionCanvas(transcriptionFolios[lastFolio-1], parsing);
                setTimeout(function(){
                    hideWorkspaceForParsing();
                    $(".pageTurnCover").fadeOut(1500);
                }, 800);
            }
            else{
                focusItem = [null,null];
                currentFolio = lastFolio;
                loadTranscriptionCanvas(transcriptionFolios[lastFolio-1], "");
            }
        }
    }
    /* Load the interface to the previous page from the one you are on. */
    function previousFolio(parsing){
        currentFolio = parseInt(currentFolio);
        if(parseInt(currentFolio) > 1){
            if(parsing === "parsing"){
                $(".pageTurnCover").show();
                fullPage();
                focusItem = [null, null];
                currentFolio -= 1;
                loadTranscriptionCanvas(transcriptionFolios[currentFolio - 1], parsing);
                setTimeout(function(){
                    hideWorkspaceForParsing();
                    $(".pageTurnCover").fadeOut(1500);
                }, 800);
            }
            else{
                focusItem = [null, null];
                currentFolio -= 1;
                loadTranscriptionCanvas(transcriptionFolios[currentFolio - 1], "");
            }
        }
        else{
            //console.log("BUGGER");
        }
    }
    
    /* Load the interface to the next page from the one you are on. */
    function nextFolio(parsing){
        currentFolio = parseInt(currentFolio);
        if(parseInt(currentFolio) !== transcriptionFolios.length){
            if(parsing === "parsing"){
                $(".pageTurnCover").show();
                fullPage();
                focusItem = [null, null];  
                currentFolio += 1;
                loadTranscriptionCanvas(transcriptionFolios[currentFolio-1], parsing);
                setTimeout(function(){
                    hideWorkspaceForParsing();
                    $(".pageTurnCover").fadeOut(1500);
                }, 800);
            }
            else{
                focusItem = [null, null];  
                currentFolio += 1;
                loadTranscriptionCanvas(transcriptionFolios[currentFolio-1], "");
            }
            
        }
        else{
            //console.log("BOOGER");
        }
    }
    
    /* Test if a given string can be parsed into a valid JSON object.
     * @param str  A string
     * @return bool
     */
    function isJSON(str) {
        var r = true;
        if(typeof str === "object"){
            r = true;
        }
        else{
            try {
                JSON.parse(str);
                r=true;
            } 
            catch (e) {
               r = false;
            }
        }
        return r;
    };
    

    function resetTranscription(){
        window.location.reload();

    }
    /* The tools for newberry are hard set in the html page, no need for this function. */
    
//    function getProjectTools(projectID){
//        var url = "http://localhost:8080/getProjectTPENServlet?projectID="+projectID;
//        $.ajax({
//            url: url,
//            type:"GET",
//            success: function(activeProject){
//                var toolArea = $("#iTools");
//                var projectTools = activeProject.projectTool; //These are all iframe tools
////                $.each(projectTools, function(){
////                    var toolLabel = this.label;
////                    var toolSource = this.source;
////                    var projectTool = $('<div id="userTool_'+toolLabel+'" class="split iTool">\n\
////                        <div class="fullScreenTrans">Full Screen Transcription</div>\n\
////                        <iframe id="tool_'+toolLabel+'" src="'+toolSource+'">\n\
////                        </iframe>\n\
////                    </div>');
////                    toolArea.append(projectTool);
////                });
//                var userTools = activeProject.userTool; //These are tools chosen by the project creator for users to have access to.  They may not be iframe tools.
////                $.each(userTools, function(){
////                    
////                });
//            }
//        });
//    }
    
    /* Populate the split page for Text Preview.  These are the transcription lines' text. */
    function createPreviewPages(){  
       // console.log("Creating preview pages");
        $(".previewPage").remove();
        var noLines = true;
        var pageLabel = "";
        for(var i = 0; i<transcriptionFolios.length; i++){
            var currentFolioToUse = transcriptionFolios[i];
            pageLabel = currentFolioToUse.label;
            var currentOn = currentFolioToUse["@id"];
            var currentPage = "";
            if(i===0){
                currentPage = "currentPage";
            }
           var lines = [];
           if(currentFolioToUse.resources && currentFolioToUse.resources.length > 0){
               lines = currentFolioToUse.resources;
                populatePreview(lines, pageLabel, currentPage);    
           }
//           else{
//               if(currentFolioToUse.otherContent && currentFolioToUse.otherContent.length>0){
////                //console.log("this is the tester")
//                    lines = annoListTester.resources;
//                    pageLabel = annoListTester.label;
//                    populatePreview(lines, pageLabel, currentPage);    
//                }
//                else{
//                    //console.log("Gotta get annos on " + currentOn);
//                    gatherAndPopulate(currentOn, pageLabel, currentPage, i);                   
//                }
//           }

        }
    }
    
    /* Gather the annotations for a canvas and populate the preview interface with them. */
    function gatherAndPopulate(currentOn, pageLabel, currentPage, i){
        //console.log("get annos on "+currentOn);
        var annosURL = "getAnno";
        var properties = {"@type": "sc:AnnotationList", "on" : currentOn};
        var paramOBJ = {"content": JSON.stringify(properties)};
         $.post(annosURL, paramOBJ, function(annoList){
             annoList = JSON.parse(annoList);
             if(annoList.length > 0){
                 checkForMaster(annoList, pageLabel, currentPage, i);
             }

         });
    }
    
    /* Check for which annotation list to use either by project ID or if its the master */
    function checkForMaster(annoList, pageLabel, currentPage, j){
        var lines = [];
        var masterList = undefined;
        for(var i=0; i<annoList.length; i++){
            var thisList = annoList[i];
            if(thisList.proj === "master"){
                //console.log("master");
                masterList = thisList; //The last list happens to be the master list, so set it.
            }
            if(thisList.proj !== undefined && thisList.proj == theProjectID){
               //console.log("proj == "+theProjectID);
               if(thisList.resources !== undefined){
                   if(thisList.resources.length > 0){ //can be an empty list.
                       lines = thisList.resources;
                   }
                   populatePreview(lines, pageLabel, currentPage, j);
                   return false;
               }
            }
            else if(lines.length===0 && i===annoList.length-1){
                //console.log("must default to master");
                if(masterList !== undefined){
                    lines = masterList.resources;
                    //TODO we do not want a user who is not an admin to alter this list.  A general user could end up being able to edit the master list.
                    populatePreview(lines, pageLabel, currentPage, j);
                }
                else{
                 //   console.log("No matching list by projectID and no master found for "+pageLabel);
                }
                
                return false;
            }
        }
    }
    
    /* Populate the line preview interface. */
    function populatePreview(lines, pageLabel, currentPage, order){
        var letterIndex = 0;
        var letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var previewPage = $('<div order="'+order+'" class="previewPage"><span class="previewFolioNumber">'+pageLabel+'</span></div>');
        if(lines.length === 0) previewPage = $('<div order="'+order+'" class="previewPage"><span class="previewFolioNumber">'+pageLabel+'</span><br>No Lines</div>');
        var num = 0;
        for(var j=0; j<lines.length; j++){
            num++;
            var col = letters[letterIndex];
            var currentLine = lines[j].on;
            var currentLineXYWH = currentLine.slice(currentLine.indexOf("#xywh=")+6);
            currentLineXYWH = currentLineXYWH.split(",");
            var currentLineX = currentLineXYWH[0];
            var line = lines[j];
            var lineURL = line["@id"];
            var lineID = lineURL; //lineURL.slice(lineURL.indexOf("/line/")+6)
            var lineText = line.resource["cnt:chars"];
            if(j>=1){
                var lastLine = lines[j-1].on;
                var lastLineXYWH = lastLine.slice(lastLine.indexOf("#xywh=")+6);
                lastLineXYWH = lastLineXYWH.split(",");
                var lastLineX = lastLineXYWH[0];
                var abs = Math.abs(parseInt(lastLineX) - parseInt(currentLineX));
                if(abs > 0){
                    letterIndex++;
                    num = 0;
                }
            }
            
            var previewLine = $('<div class="previewLine" data-lineNumber="'+j+'">\n\
                         <span class="previewLineNumber" lineserverid="'+lineID+'" data-lineNumber="'+j+'"  data-column="'+col+'"  data-lineOfColumn="'+j+'">\n\
                            '+col+''+num+'\n\
                          </span>\n\
                         <span class="previewText '+currentPage+'">'+lineText+'<span class="previewLinebreak"></span></span>\n\
                         <span class="previewNotes" contentEditable="(permitModify||isMember)" ></span>\n\
                     </div>');
             previewPage.append(previewLine);
         }
         $("#previewDiv").append(previewPage);
    }
    
    function populateSpecialCharacters(specialCharacters){
        specialCharacters = JSON.parse(specialCharacters);
        var speCharactersInOrder = new Array(specialCharacters.length);
        for (var char = 0; char < specialCharacters.length; char++){
            var thisChar = specialCharacters[char];
            if(thisChar == ""){

            }
            else{
                var keyVal = thisChar.key;
                var position2 = parseInt(thisChar.position);
                var newCharacter = "<option class='character'>&#"+keyVal+";</option>";  // onclick=\"addchar('&#"+keyVal+";');\" 
                if(position2-1 >= 0 && (position2-1) < specialCharacters.length){
                    speCharactersInOrder[position2-1] = newCharacter; 
                }
            }

        }
        $.each(speCharactersInOrder, function(){
            var button1 = $(''+this);
            $(".specialCharacters").append(button1);
        });

    }
    
    function populateXML(xmlTags){
        xmlTags = xmlTags.split(","); 
        var tagsInOrder = [];
        for (var tag = 0; tag < xmlTags.length; tag++){
            var newTagBtn = xmlTags[tag];
            if(newTagBtn!=="" && newTagBtn!==" "){
                 tagsInOrder.push("<option>"+newTagBtn + "</option>");
            }
            //tagsInOrder[position1] = newTagBtn;
        }
        $.each(tagsInOrder, function(){
            var button2 = $(''+this);
            $(".xmlTags").append(button2);
        }); 
    }
    /*
     * Load the trnascription from the text in the text area. 
     */
    function loadTranscription(){
        //Object validation here.
            projectID = 4080;
            var userTranscription = $('#transcriptionText').val();
            currentFolio = 1;
            
            if($.isNumeric(userTranscription)){ //The user can put the project ID in directly and a call will be made to newberry proper to grab it.
                projectID = userTranscription;
                theProjectID = projectID;
                var url = "getProjectTPENServlet?projectID="+projectID;
                $.ajax({
                    url: url,
                    type:"GET",
                    success: function(activeProject){
                        var projectTools = activeProject.projectTool;
                        projectTools = JSON.parse(projectTools);
                        var count = 0;
                        var url  = ""; 
                        var currentUser = activeProject.cuser;
                        var leaders = activeProject.ls_leader;
                        leaders = JSON.parse(leaders);
                        $.each(leaders, function(){
                            if(this.UID === parseInt(currentUser)){
                                //console.log("This user is a leader.");
                                userIsAdmin = true;
                                $("#parsingBtn").show();
                                var message = $('<span>This canvas has no lines.  If you would like to create lines</span> <span style="color: blue;" onclick="hideWorkspaceForParsing()">click here</span>.\n\
                                Otherwise, you can <span style="color: red;" onclick="$(\'#noLineWarning\').hide()">dismiss this message</span>.');
                                $("#noLineConfirmation").empty();
                                $("#noLineConfirmation").append(message);
                            }
                        });
                        if(activeProject.ls_ms[0] !== undefined){
                            var getURLfromThis = activeProject.ls_ms;
                            getURLfromThis = JSON.parse(getURLfromThis);
                            url  = getURLfromThis[0].archive; //This is the manifest inside the project data
                            if(url.indexOf("http") < 0){
                                //create the newberry url
                                url = "project/"+projectID;
                            }
                            $.ajax({ /* Causes CORS */
                                url: url,
                                success: function(projectData){
//                                    //console.log("Manifest data: ");
//                                    //console.log(projectData);
                                    if(projectData.sequences[0] !== undefined && projectData.sequences[0].canvases !== undefined
                                    && projectData.sequences[0].canvases.length > 0){
                                        transcriptionFolios = projectData.sequences[0].canvases;
                                        scrubFolios();
                                        var count = 1;
                                        $.each(transcriptionFolios, function(){
                                            $("#pageJump").append("<option folioNum='"+count+"' class='folioJump' val='"+this.label+"'>"+this.label+"</option>");
                                            $("#compareJump").append("<option class='compareJump' folioNum='"+count+"' val='"+this.label+"'>"+this.label+"</option>");
                                            count++;
                                            if(this.otherContent){
                                                if(this.otherContent.length > 0){
                                                    annoLists.push(this.otherContent[0]);
                                                }
                                                else{
                                                    //console.log("push empty 1");
                                                    //otherContent was empty (IIIF says otherContent should have URI's to AnnotationLists).  We will check the store for these lists still.
                                                    annoLists.push("empty");
                                                }
                                            }
                                            else{
                                                annoLists.push("noList");
                                            }
                                        });
                                        loadTranscriptionCanvas(transcriptionFolios[0],"");
                                        var projectTitle = projectData.label;
                                        $("#trimTitle").html(projectTitle);
                                        $("#trimTitle").attr("title", projectTitle);
                                        $('#transcriptionTemplate').css("display", "inline-block");
                                        $('#setTranscriptionObjectArea').hide();
                                        $(".instructions").hide();
                                        $(".hideme").hide();
                                        //load Iframes after user check and project information data call    
                                        loadIframes();
                                        //getProjectTools(projectID);
                                    }
                                    else{
                                        //ERROR! It is a malformed transcription object.  There is no canvas sequence defined.  
                                    }
                                },
                                error: function(jqXHR,error, errorThrown) {  
                                    if(jqXHR.status && jqXHR.status==400){
                                         alert(jqXHR.responseText); 
                                    }
                                    else{
                                        alert("Something went wrong. Could not get the project. 1");
                                    }
                                    //load Iframes after user check and project information data call    
                                    loadIframes();
                               }
                            });
                        }
                        else{
                            alert("No Manifest Found");
                        }
                        $.each(projectTools, function(){
                            if(count < 4){ //allows 5 tools.  
                                var splitHeight = window.innerHeight + "px";
                                var toolLabel = this.name;
                                var toolSource = this.url;
                                var splitTool = $('<div toolName="'+toolLabel+'" class="split iTool"><button class="fullScreenTrans">Full Screen Transcription</button></div>');
                                var splitToolIframe = $('<iframe style="height:'+splitHeight+';" src="'+toolSource+'"></iframe>');
                                var splitToolSelector = $('<option splitter="'+toolLabel+'" class="splitTool">'+toolLabel+'</option>');
                                splitTool.append(splitToolIframe);
                                $("#splitScreenTools").append(splitToolSelector);
                                $(".iTool:last").after(splitTool);
                            }
                            count++;
                        });
                        populateSpecialCharacters(activeProject.projectButtons);
                        populateXML(activeProject.xml);
                    },
                    error: function(jqXHR,error, errorThrown) {  
                            if(jqXHR.status && jqXHR.status==400){
                                 alert(jqXHR.responseText); 
                            }
                            else{
                                alert("Something went wrong. Could not get the project. 2");
                            }
                       }
                });
                }
                else if(isJSON(userTranscription)){
                        userTranscription = JSON.parse(userTranscription);
                        if(userTranscription.sequences[0] !== undefined && userTranscription.sequences[0].canvases !== undefined
                        && userTranscription.sequences[0].canvases.length > 0){
                            transcriptionFolios = userTranscription.sequences[0].canvases;
                            scrubFolios();
                            var count = 1;
                            $.each(transcriptionFolios, function(){
                               $("#pageJump").append("<option folioNum='"+count+"' class='folioJump' val='"+this.label+"'>"+this.label+"</option>");
                               $("#compareJump").append("<option class='compareJump' folioNum='"+count+"' val='"+this.label+"'>"+this.label+"</option>");
                               count++;
                                if(this.otherContent){
                                    if(this.otherContent.length > 0){
                                        annoLists.push(this.otherContent[0]);
                                    }
                                    else{
                                        //console.log("push empty 2");
                                        //otherContent was empty (IIIF says otherContent should have URI's to AnnotationLists).  We will check the store for these lists still.
                                        annoLists.push("empty");
                                    }
                                }
                                else{
                                    annoLists.push("noList");
                                }
                            });
                            loadTranscriptionCanvas(transcriptionFolios[0],"");
                            var projectTitle = userTranscription.label;
                            $("#trimTitle").html(projectTitle);
                            $("#trimTitle").attr("title", projectTitle);
                            $('#transcriptionTemplate').css("display", "inline-block");
                            $('#setTranscriptionObjectArea').hide();
                            $(".instructions").hide();
                            $(".hideme").hide();
                            //load Iframes after user check and project information data call    
                            loadIframes();
                        }
                        else{
                            //ERROR!  It is a valid JSON object, but it is malformed and cannot be read as a transcription object.
                            //load Iframes after user check and project information data call    
                            loadIframes();
                        }
                        
                }
                else if (userTranscription.indexOf("http://") >= 0 || userTranscription.indexOf("https://") >= 0){
                    var localProject = false;
                    if(userTranscription.indexOf("/project/") > -1){
                        if(userTranscription.indexOf("t-pen.org") > -1){
                            localProject = false;
                            projectID = 0;  //This way, it will not grab the t-pen project id.  
                        }
                        else{
                            localProject = true; //Well, probably anyway.  I forsee this being an issue like with t-pen.
                            projectID = parseInt(userTranscription.substring(userTranscription.lastIndexOf('/project/')+9));
                            theProjectID = projectID;
                        }
                    }
                    else{
                        projectID = 0;
                    }
                    if(localProject){
                        //get project info first, get manifest out of it, populate
                        var url = "getProjectTPENServlet?projectID="+projectID;
                        $.ajax({
                            url: url,
                            type:"GET",
                            success: function(activeProject){
                                var projectTools = activeProject.projectTool;
                                projectTools = JSON.parse(projectTools);
                                var count = 0;
                                var url  = "";
                                if(activeProject.ls_ms[0] !== undefined){
                                    var getURLfromThis = activeProject.ls_ms;
                                    getURLfromThis = JSON.parse(getURLfromThis);
                                    url  = getURLfromThis[0].archive;
                                    $.ajax({
                                        url: url,
                                        success: function(projectData){
                                            if(projectData.sequences[0] !== undefined && projectData.sequences[0].canvases !== undefined
                                            && projectData.sequences[0].canvases.length > 0){
                                                transcriptionFolios = projectData.sequences[0].canvases;
                                                scrubFolios();
                                                var count = 1;

                                                $.each(transcriptionFolios, function(){
                                                    $("#pageJump").append("<option folioNum='"+count+"' class='folioJump' val='"+this.label+"'>"+this.label+"</option>");
                                                    $("#compareJump").append("<option class='compareJump' folioNum='"+count+"' val='"+this.label+"'>"+this.label+"</option>");
                                                    count++;
                                                    if(this.otherContent){
                                                        if(this.otherContent.length > 0){
                                                            annoLists.push(this.otherContent[0]);
                                                        }
                                                        else{
                                                            //console.log("push empty 3");
                                                            //otherContent was empty (IIIF says otherContent should have URI's to AnnotationLists).  We will check the store for these lists still.
                                                            annoLists.push("empty");
                                                        }
                                                    }
                                                    else{
                                                        annoLists.push("noList");
                                                    }
                                                });
                                                loadTranscriptionCanvas(transcriptionFolios[0],"");
                                                var projectTitle = projectData.label;
                                                $("#trimTitle").html(projectTitle);
                                                $("#trimTitle").attr("title", projectTitle);$('#transcriptionTemplate').css("display", "inline-block");
                                                $('#setTranscriptionObjectArea').hide();
                                                $(".instructions").hide();
                                                $(".hideme").hide(); 
                                                //getProjectTools(projectID);
                                                //load Iframes after user check and project information data call    
                                                loadIframes();
                                            }
                                            else{
                                                //ERROR! It is a malformed transcription object.  There is no canvas sequence defined.  
                                                //load Iframes after user check and project information data call    
                                                loadIframes();
                                            }
                                        },
                                        error: function(jqXHR,error, errorThrown) {  
                                            if(jqXHR.status && jqXHR.status==400){
                                                 alert(jqXHR.responseText); 
                                            }
                                            else{
                                                alert("Something went wrong 2");
                                            }
                                            //load Iframes after user check and project information data call    
                                            loadIframes();
                                       }
                                });
                            }
                            else{
                                alert("No Manifest Found");
                                //load Iframes after user check and project information data call    
                                loadIframes();
                            }
                            $.each(projectTools, function(){
                                if(count < 4){ //allows 5 tools.  
                                    var splitHeight = window.innerHeight + "px";
                                    var toolLabel = this.name;
                                    var toolSource = this.url;
                                    var splitTool = $('<div toolName="'+toolLabel+'" class="split iTool"><button class="fullScreenTrans">Full Screen Transcription</button></div>');
                                    var splitToolIframe = $('<iframe style="height:'+splitHeight+';" src="'+toolSource+'"></iframe>');
                                    var splitToolSelector = $('<option splitter="'+toolLabel+'" class="splitTool">'+toolLabel+'</option>');
                                    splitTool.append(splitToolIframe);
                                    $("#splitScreenTools").append(splitToolSelector);
                                    $(".iTool:last").after(splitTool);
                                }
                                count++;
                            });
                            populateSpecialCharacters(activeProject.projectButtons);
                            populateXML(activeProject.xml);
                        },
                        error: function(jqXHR,error, errorThrown) {  
                                    if(jqXHR.status && jqXHR.status==400){
                                         alert(jqXHR.responseText); 
                                    }
                                    else{
                                        alert("Something went wrong. Could not get the project. 4");
                                    }
                               }
                    });
                    }
                    else{ //it is not a local project, so just grab the url that was input and request the manifst. 
                        var url  = userTranscription;
                        $.ajax({
                            url: url,
                            success: function(projectData){
                                if(projectData.sequences[0] !== undefined && projectData.sequences[0].canvases !== undefined
                                && projectData.sequences[0].canvases.length > 0){
                                    transcriptionFolios = projectData.sequences[0].canvases;
                                    scrubFolios();
                                    var count = 1;

                                    $.each(transcriptionFolios, function(){
                                        $("#pageJump").append("<option folioNum='"+count+"' class='folioJump' val='"+this.label+"'>"+this.label+"</option>");
                                        $("#compareJump").append("<option class='compareJump' folioNum='"+count+"' val='"+this.label+"'>"+this.label+"</option>");
                                        count++;
                                        if(this.otherContent){
                                            if(this.otherContent.length > 0){
                                                annoLists.push(this.otherContent[0]);
                                            }
                                            else{
                                                //console.log("push empty 4");
                                                //otherContent was empty (IIIF says otherContent should have URI's to AnnotationLists).  We will check the store for these lists still.
                                                annoLists.push("empty");
                                            }
                                        }
                                        else{
                                            annoLists.push("noList");
                                        }
                                    });
                                    loadTranscriptionCanvas(transcriptionFolios[0],"");
                                    
                                    var projectTitle = projectData.label;
                                    $("#trimTitle").html(projectTitle);
                                    $("#trimTitle").attr("title", projectTitle);$('#transcriptionTemplate').css("display", "inline-block");
                                    $('#setTranscriptionObjectArea').hide();
                                    $(".instructions").hide();
                                    $(".hideme").hide(); 
                                    //getProjectTools(projectID);
                                }
                                else{
                                    //ERROR! It is a malformed transcription object.  There is no canvas sequence defined.  
                                }
                                //load Iframes after user check and project information data call    
                                loadIframes();
                            },
                            error: function(jqXHR,error, errorThrown) {  
                                if(jqXHR.status && jqXHR.status==400){
                                     alert(jqXHR.responseText); 
                                }
                                else{
                                    alert("Something went wrong 5");
                                }
                                //load Iframes after user check and project information data call    
                                loadIframes();
                           }
                        });
                    }
                }
                else{
                    alert("The input was invalid.");
                    //load Iframes after user check and project information data call.  Maybe only after valid page load parameters.  uncomment this line if necessary.    
                    //loadIframes();
                }
            
    } 
    
    /*
     * Load a canvas from the manifest to the transcription interface. 
     */
    function loadTranscriptionCanvas(canvasObj, parsing){
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
        var cnt = -1;

        if(canvasObj.images[0].resource['@id'] !== undefined && canvasObj.images[0].resource['@id'] !== ""){ //Only one image
            var image = new Image();
            $(image)
                    .on("load",function() {
                        $("#imgTop, #imgTop img, #imgBottom img, #imgBottom, #transcriptionCanvas").css("height", "auto");
                        $("#imgTop img, #imgBottom img").css("width", "100%");
                        $("#imgBottom").css("height", "inherit");
                        $('.transcriptionImage').attr('src', canvasObj.images[0].resource['@id'].replace('amp;',''));
                        $("#fullPageImg").attr("src", canvasObj.images[0].resource['@id'].replace('amp;',''));
                        originalCanvasHeight2 = $("#imgTop img").height();
                        originalCanvasWidth2 = $("#imgTop img").width();
                        drawLinesToCanvas(canvasObj, parsing);
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
                    .attr("src", canvasObj.images[0].resource['@id'].replace('amp;',''));
        }
        else{
             $('.transcriptionImage').attr('src',"images/missingImage.png");
             alert("The canvas is malformed.  No 'images' field in canvas object or images:[0]['@id'] does not exist.  Cannot draw lines.");
            //ERROR!  Malformed canvas object.  
        }
        $(".previewText").removeClass("currentPage");
        $.each($("#previewDiv").children(".previewPage:eq("+(parseInt(currentFolio)-1)+")").find(".previewLine"),function(){
            $(this).find('.previewText').addClass("currentPage");
        });
        createPreviewPages(); //each time you load a canvas to the screen with all of its updates, remake the preview pages.
    }
    
      /*
     * @paran canvasObj  A canvas object to extrac transcription lines from and draw to the interface. 
     */
    function drawLinesToCanvas(canvasObj, parsing){
        var lines = [];
        currentFolio = parseInt(currentFolio);
        //console.log("Draw lines");
//        //console.log(canvasObj);
        if(canvasObj.resources !== undefined && canvasObj.resources.length > 0){
//            //console.log("Lines are resource annos");
            for(var i=0; i<canvasObj.resources.length; i++){
                if(isJSON(canvasObj.resources[i])){   // it is directly an annotation
                    lines.push(canvasObj.resources[i]);
                }
            }
            linesToScreen(lines);
        }
        else{ //we have the anno list for this canvas (potentially), so query for it.  If not found, then consider this an empty canvas.
                var annosURL = "getAnno";
                var onValue = canvasObj["@id"];
                //console.log("get annos for draw for canvas "+onValue);
                var properties = {"@type": "sc:AnnotationList", "on" : onValue};
                var paramOBJ = {"content": JSON.stringify(properties)};
                $.post(annosURL, paramOBJ, function(annoList){
                    //console.log("found annoLists");
                    annoList = JSON.parse(annoList);
                    //console.log(annoList);
                    var found = false;
                    var currentList = {};
                    if(annoList.length > 0){
                        //Always default to the master list, which was the first list created for the canvas.  That way, the annotation lists associated with the master are still supported.
                        var masterList = {};
                        //lines = masterList.resources;
                        //currentList = masterList;
                        //annoLists[currentFolio -1] = masterList["@id"];
                        $.each(annoList, function(){
                            //if we find the master list, make that the default
                            if(this.proj === "master"){
                                //console.log("master set to default");
                                masterList = this;
                                lines = this.resources;
                                currentList = this;
                                //TODO we do not want someone who is not an admin to be able to edit this list.  Do a check here and make annoLists[currentFolio -1] = "master" so it cannot be written to.
                                annoLists[currentFolio -1] = this["@id"];
                            }
                            if(this.proj !== undefined && this.proj!=="" && this.proj == theProjectID){
                                //These are the lines we want to draw because the projectID matches.  Overwrite master if necessary.
                                //console.log("Lines we wanna draw");
                                lines = this.resources;
                                currentList = this;
                                annoLists[currentFolio -1] = this["@id"];
                                return false;
                            }
                            else{
                                //It is an annotation list for this canvas in a different project.  We have defaulted to master already.
                                //console.log("Anno list for this canvas but different project.  ");
                            }
                        });
                        if(lines.length > 0){
                            //console.log("Got lines to draw");
                            $("#transTemplateLoading").hide();
                            $("#transcriptionTemplate").show();
                            linesToScreen(lines);
                        }
                        else{ //list has no lines
                            //console.log("no lines in what we got");
                            if(parsing !== "parsing"){
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
                    else{ // couldnt get list.  one should always exist, even if empty.  We will say no list and changes will be stored locally to the canvas.
                        annoLists[currentFolio -1 ] = "empty";
                        if(parsing !== "parsing"){
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
    function linesToScreen(lines){
        $("#noLineWarning").hide();
        var letterIndex = 0;
        var letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        letters = letters.split("");
        var update = true;
        if($("#parsingDiv").is(":visible")){
            update = false;
        }
        var thisContent = "";
        var thisPlaceholder = "Enter a line transcription";
        var counter = 0;
        var colCounter = 0;
        var image = $('#imgTop img');
        var theHeight = image.height();
        var theWidth = image.width();
        $('#transcriptionCanvas').css('height', originalCanvasHeight2 +"px");
        $('.lineColIndicatorArea').css('height',originalCanvasHeight2 + "px");
        var ratio = 0;
        //should be the same as originalCanvasWidth2/originalCanvasBeight2
        ratio = theWidth / theHeight;
        adjustRatio = ratio;
//        console.log("ratio for lines to screen");
//        console.log(theWidth + "/" +theHeight);
//        console.log(ratio);
        for(var i=0; i<lines.length;i++){
            //("line "+i);
            var line = lines[i];
            var lastLine = {};
            var col = letters[letterIndex];
            if(i>0)lastLine=lines[i-1];
            var lastLineX = 10000;
            var lastLineWidth = -1;
            var lastLineTop = -2;
            var lastLineHeight = -2;
            var x,y,w,h = 0;
            var XYWHarray = [x,y,w,h];
            var lineURL = "";
            var lineID = -1;
            if(line.on !== undefined){
                lineURL = line.on;
            }
            else{
                //ERROR.  malformed line.
                update = false;
            }
            if(line["@id"] !== undefined && line["@id"]!=="" ){ //&& line['@id'].indexOf('annotationstore/annotation') >=0
                lineID = line['@id']; //.slice(line['@id'].lastIndexOf('line/') + 5)
            }
            else{
                //ERROR.  Malformed line. 
                update = false;
            }
            thisContent = "";
            if(lineURL.indexOf('#') > -1){ //string must contain this to be valid
                var XYWHsubstring = lineURL.substring(lineURL.lastIndexOf('#' + 1)); //xywh = 'x,y,w,h'
                if(lastLine.on){ //won't be true for first line
                    lastLineX = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[0];
                    lastLineWidth = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[2];
                    lastLineTop = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[1];
                    lastLineHeight = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[3];
                }                
                else if(i===0 && lines.length > 1){ /* Check for the variance with the first line */
                    lastLine = lines[0];
                     if(lastLine.on){
                         lastLineX = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[0];
                         lastLineWidth = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[2];
                         lastLineTop = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[1];
                         lastLineHeight = lastLine.on.slice(lastLine.on.indexOf("#xywh=") + 6).split(",")[3];
                     }
                }
                if(XYWHsubstring.indexOf('=') > -1){ //string must contain this to be valid
                    var numberArray = XYWHsubstring.substring(lineURL.lastIndexOf('xywh=') + 5).split(',');
                    if(parseInt(lastLineTop) + parseInt(lastLineHeight) !== numberArray[1]){
                        //check for slight variance in top position.  Happens because of rounding percentage math that gets pixels to be an integer.
                        var num1 = parseInt(lastLineTop) + parseInt(lastLineHeight);
                        if(Math.abs(num1 - numberArray[1]) <= 4 && Math.abs(num1 - numberArray[1])!==0){
                            numberArray[1] = num1;
                            var newString = numberArray[0]+","+num1+","+numberArray[2]+","+numberArray[3];
                            if(i>0){
                                //to make the change cascade to the rest of the lines, we actually have to update the #xywh of the current line with the new value for y.
                                var lineOn = lineURL;
                                var index = lineOn.indexOf("#xywh=") + 6;
                                var newLineOn = lineOn.substr(0, index) + newString + lineOn.substr(index + newString.length);
                                lines[i].on = newLineOn;
                            }
                            
                        }
                        else{
                            //console.log("no difference");
                        }
                    }
                    if(numberArray.length === 4){ // string must have all 4 to be valid
                        x = numberArray[0];
                        w = numberArray[2];
                        if(lastLineX !== x){ //check if the last line's x value is equal to this line's x value (means same column)
                            if(Math.abs(x - lastLineX) <= 3){ //allow a 3 pixel  variance and fix this variance when necessary...
                                //align them, call them the same Column. 
                                /*
                                 * This is a consequence of #xywh for a resource needing to be an integer.  When I calculate its intger position off of
                                 * percentages, it is often a float and I have to round to write back.  This can cause a 1 or 2 pixel discrenpency, which I account
                                 * for here.  There may be better ways of handling this, but this is a good solution for now. 
                                 */
                                if(lastLineWidth !== w){ //within "same" column (based on 3px variance).  Check the width
                                    if(Math.abs(w - lastLineWidth) <= 5){ //If the width of the line is within five pixels, automatically make the width equal to the last line's width.

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
                            else{ //we are in a new column, column indicator needs to increase. 
                                if(lines.length > 1){
                                    letterIndex++;
                                    col = letters[letterIndex];
                                    colCounter = 0; //Reset line counter so that when the column changes the line# restarts?
                                }
                            }
                        }
                        else{ //If the X value matches, we are in the same column and don't have to account for any variance or update the array.  Still check for slight width variance.. 
                            if(lastLineWidth !== w){
                                if(Math.abs(w - lastLineWidth) <= 5){ //within 5 pixels...

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
                        }
                        y = numberArray[1];
                        h = numberArray[3];
                        XYWHarray = [x,y,w,h];
                    }
                    else{
                        //ERROR! Malformed line
                        update = false;
                    }
                }
                else{
                    //ERROR! Malformed line
                    update = false;
                }
            }
            else{
                //ERROR!  Malformed line.
                update = false;
            }
            
            if(line.resource['cnt:chars'] !== undefined && line.resource['cnt:chars'] !== "" && line.resource['cnt:chars'] != "Enter a line transcription"){
                thisContent = line.resource['cnt:chars'];
            }

                counter=parseInt(counter);
                counter += 1;
                var newAnno = $('<div id="transcriptlet_'+counter+'" col="'+col+'" colLineNum="'+colCounter+'" lineID="'+counter+'" lineserverid="'+lineID+'" class="transcriptlet" data-answer="' + thisContent + '"><textarea placeholder="' + thisPlaceholder + '">'+thisContent+'</textarea></div>');
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
                
                colCounter+=1;
                $("#transcriptletArea").append(newAnno);
                
                var lineColumnIndicator = $("<div onclick='loadTranscriptlet("+counter+");' pair='"+col+""+colCounter+"' lineserverid='"+lineID+"' lineID='"+counter+"' class='lineColIndicator' style='left:"+left+"%; top:"+top+"%; width:"+width+"%; height:"+height+"%;'><div class\n\
                ='lineColOnLine' >"+col+""+colCounter+"</div></div>");
                var fullPageLineColumnIndicator = $("<div pair='"+col+""+colCounter+"' lineserverid='"+lineID+"' lineID='"+counter+"' class='lineColIndicator fullP'\n\
                onclick=\"updatePresentation($('#transcriptlet_"+counter+"'));\" style='left:"+left+"%; top:"+top+"%; width:"+width+"%; height:"+height+"%;'><div class\n\
                ='lineColOnLine' >"+col+""+colCounter+"</div></div>"); //TODO add click event to update presentation
                //Make sure the col/line pair sits vertically in the middle of the outlined line.  
                var lineHeight = theHeight * (height/100) + "px";
                lineColumnIndicator.find('.lineColOnLine').attr("style", "line-height:"+lineHeight+";");
                //Put to the DOM
                $(".lineColIndicatorArea").append(lineColumnIndicator);
                $("#fullPageSplitCanvas").append(fullPageLineColumnIndicator);
                
            
        }
        if(update && $(".transcriptlet").eq(0) !== undefined){
            updatePresentation($(".transcriptlet").eq(0));
        }
        //we want automatic updating for the lines these texareas correspond to.
        var typingTimer;                //timer identifier
        $("textarea").keydown(function(e){
            //user has begun typing, clear the wait for an update
            clearTimeout(typingTimer);
        });
        $("textarea").keyup(function(e){
            var lineToUpdate = $(this).parent();
            clearTimeout(typingTimer);
            //when a user stops typing for 2 seconds, fire an update to get the new text.
            typingTimer = setTimeout(function(){
                updateLine(lineToUpdate, "no");
            }, 2000);
        });
    }
    
    /* Make the transcription interface focus to the transcriptlet passed in as the parameter. */
    function updatePresentation(transcriptlet) {
        if(transcriptlet === undefined || transcriptlet === null){
            $("#imgTop").css("height", "0%");
            $("#imgBottom").css("height", "inherit");
            return false;
        }
        var nextCol = transcriptlet.attr("col");
        var nextLineNum = parseInt(transcriptlet.attr("collinenum"))+1;
        var transcriptletBefore = $(transcriptlet.prev());
        var nextColLine = nextCol+""+nextLineNum;
        $("#currentColLine").html(nextColLine);
        if(parseInt(nextLineNum) >= 1){
            if(transcriptletBefore.length>0){
                var currentTranscriptletNum = parseInt(transcriptletBefore.attr("collinenum")) + 1;
                //var prevLine = $("#transcriptlet_"+previousTranscriptletNum);
                var preLine = "";
                if(transcriptletBefore.length > 0){

                }
                else{

                }
                var prevLineCol = transcriptletBefore.attr("col");
                var prevLineText = transcriptletBefore.attr("data-answer");
                $("#prevColLine").html(prevLineCol+""+currentTranscriptletNum);
                if(prevLineText === ""){
                    $("#captionsText").html("This line is not transcribed.");
                }
                else{
                    $("#captionsText").html(prevLineText);
                }
            }
            else{ //this is a probelm
                $("#prevColLine").html("**");
                $("#captionsText").html("You are on the first line.");
            }
            
        }
        else{ //there is no previous line
            $("#prevColLine").html("**");
            $("#captionsText").html("ERROR.  NUMBERS ARE OFF");
        }
        focusItem[0] = focusItem[1];
        focusItem[1] = transcriptlet;
        if ((focusItem[0] === null) || (focusItem[0].attr("id") !== focusItem[1].attr("id"))) {
          this.adjustImgs(this.setPositions());
          this.swapTranscriptlet();
          //show previous line transcription
          $('#captions').animate({
            opacity: 1
          }, 100);
        } 
        else {
          this.adjustImgs(this.setPositions());
          focusItem[1].prevAll(".transcriptlet").addClass("transcriptletBefore").removeClass("transcriptletAfter");
          focusItem[1].nextAll(".transcriptlet").addClass("transcriptletAfter").removeClass("transcriptletBefore");
          //this.maintainWorkspace();
        }
        //prevent textareas from going invisible and not moving out of the workspace
        focusItem[1].removeClass("transcriptletBefore transcriptletAfter");
      };
     
    /* Helper for position focus onto a specific transcriptlet */
    function setPositions() {
    //Determine size of section above workspace
        var bottomImageHeight = $("#imgBottom img").height();
        if (focusItem[1].attr("lineHeight") !== null) {
          var pairForBookmarkCol = focusItem[1].attr('col');
          var pairForBookmarkLine = parseInt(focusItem[1].attr('collinenum'));
          pairForBookmarkLine += 1;
          var pairForBookmark = pairForBookmarkCol + pairForBookmarkLine;
          var currentLineHeight = parseFloat(focusItem[1].attr("lineHeight"));
          var currentLineTop = parseFloat(focusItem[1].attr("lineTop"));
          // top of column
          var previousLine = (focusItem[1].prev().is('.transcriptlet') && (currentLineTop > parseFloat(focusItem[1].prev().attr("lineTop")))) ? parseFloat(focusItem[1].prev().attr("lineHeight")) : parseFloat(focusItem[1].attr("lineTop"));
          // oversized for screen
          var imgTopHeight = (previousLine + currentLineHeight)+1.5; // obscure behind workspace.
          var topImgPositionPercent = ((previousLine - currentLineTop)*100)/imgTopHeight;
          var topImgPositionPx = (previousLine - currentLineTop)*bottomImageHeight/100;
//          var bookmarkTop = (currentLineTop + ((imgTopHeight/100)*topImgPositionPercent));
          var bottomImgPositionPercent = -(currentLineTop + currentLineHeight);
          var bottomImgPositionPx = -(currentLineTop+currentLineHeight)*bottomImageHeight / 100;
        }
        var positions = {
          imgTopHeight: imgTopHeight,
          topImgPositionPercent: topImgPositionPercent,
          topImgPositionPx : topImgPositionPx,
          bottomImgPositionPercent: bottomImgPositionPercent,
          bottomImgPositionPx: bottomImgPositionPx,
          activeLine: pairForBookmark
//          bookmarkTop: (parseFloat(locationForBookmark.css("top")) / $(".lineColIndicatorArea:first").height()) * 100 + "%",
//          bookmarkHeight: currentLineHeight
        };
        return positions;
    };
  
  /**
   * Removes previous textarea and slides in the new focus.
   *
   * @see updatePresentation()
   */
    function swapTranscriptlet() {
      //focusItem[0].addClass("transcriptletBefore").removeClass('noTransition');
      // slide in the new transcriptlet
      focusItem[1].css({"width": "auto", "z-index": "5"});
      focusItem[1].removeClass("transcriptletBefore transcriptletAfter");
      focusItem[1].prevAll(".transcriptlet").addClass("transcriptletBefore").removeClass("transcriptletAfter");
      focusItem[1].nextAll(".transcriptlet").addClass("transcriptletAfter").removeClass("transcriptletBefore");
      if($('.transcriptletAfter').length == 0){
          $('#nextTranscriptlet').hide();
      }
      else{
          $('#nextTranscriptlet').show();
      }
      if($('.transcriptletBefore').length == 0){
          $('#previousTranscriptlet').hide();
      }
      else{
           $('#previousTranscriptlet').show();
      }
    };
    
  /**
   * Aligns images and workspace using defined dimensions.
   *
   * @see maintainWorkspace()
   */
    function adjustImgs(positions) {
      //move background images above and below the workspace
         var lineToMakeActive = $(".lineColIndicator[pair='"+positions.activeLine+"']:first");
         var topImageHeight = $("#imgTop img").height();
          $("#imgTop").animate({
            "height": positions.imgTopHeight + "%"
          },250)
          .find("img").animate({
            top: positions.topImgPositionPx + "px",
            left: "0px"
          },250);
         $("#imgTop .lineColIndicatorArea").animate({
            top: positions.topImgPositionPx + "px",
            left: "0px"
          },250);
          $("#imgBottom").find("img").animate({
            top: positions.bottomImgPositionPx  + "px",
            left: "0px"
          },250)
          $("#imgBottom .lineColIndicatorArea").animate({
            top: positions.bottomImgPositionPx  + "px",
            left: "0px"
          },250);
          if($('.activeLine').hasClass('linesHidden')){
              $('.activeLine').hide();
          }
          $(".lineColIndicator").removeClass('activeLine').css({
              "box-shadow": "none",
              "background-color":"transparent"
            });
          lineToMakeActive.addClass("activeLine");
          //use the active line color to give the active line a little background color to make it stand out if the box shadow is not enough.
          var activeLineColor = colorThisTime.replace(".4", ".2");
          $('.activeLine').css({
              'box-shadow': '0px 0px 15px 8px '+colorThisTime
              //'background-color':activeLineColor
          });
          
    }  
   
   /* Update the line information of the line currently focused on, then load the focus to a line that was clicked on */
   function loadTranscriptlet(lineid){
        var currentLineServerID = focusItem[1].attr("lineserverid");
        if($('#transcriptlet_'+lineid).length > 0){
            if(loggedInUser){
                var lineToUpdate = $(".transcriptlet[lineserverid='"+currentLineServerID+"']");
                updateLine(lineToUpdate, "no");
                updatePresentation($('#transcriptlet_'+lineid));
            }
            else{
              var captionText1 = $("#captionsText").html();
              $("#captionsText").html("You are not logged in.");
              $('#captionsText').css("background-color", 'red');
              setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
              setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
              setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8');  $("#captionsText").html(captionText1); }, 1500);
            }

        }
        else{ //blink a caption warning
            var captionText = $("#captionsText").html();
            $("#captionsText").html("Cannot load this line.");
            $('#captionsText').css("background-color", 'red');
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
            setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
            setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8');  $("#captionsText").html(captionText); }, 1500);
        }
   }
  
    /*
     * The UI control for going the the next transcriptlet in the transcription. 
     */
    function nextTranscriptlet() {
          var nextID = parseInt(focusItem[1].attr('lineID')) + 1;
          var currentLineServerID = focusItem[1].attr("lineserverid");
          if($('#transcriptlet_'+nextID).length > 0){
              if(loggedInUser){
                  var lineToUpdate = $(".transcriptlet[lineserverid='"+currentLineServerID+"']")
                  updateLine(lineToUpdate, "no");
                  updatePresentation($('#transcriptlet_'+nextID));
              }
              else{
                var captionText1 = $("#captionsText").html();
                $("#captionsText").html("You are not logged in.");
                $('#captionsText').css("background-color", 'red');
                setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
                setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
                setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8');  $("#captionsText").html(captionText1); }, 1500);
              }
              
          }
          else{ //blink a caption warning
              var captionText = $("#captionsText").html();
              $("#captionsText").html("You are on the last line! ");
              $('#captionsText').css("background-color", 'red');
              setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
              setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
              setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8');  $("#captionsText").html(captionText); }, 1500);
          }
    }
    
    /*
     * The UI control for going the the previous transcriptlet in the transcription. 
     */
    function previousTranscriptlet() {
          var prevID = parseFloat(focusItem[1].attr('lineID')) - 1;
          var currentLineServerID = focusItem[1].attr("lineServerID");
          //var currentLineText = focusItem[1].find('textarea').val();
          if(prevID >= 0){
              if(loggedInUser){
                var lineToUpdate = $(".transcriptlet[lineserverid='"+currentLineServerID+"']");
                updateLine(lineToUpdate, "no");
                updatePresentation($('#transcriptlet_'+prevID));
              }
              else{
                var captionText1 = $("#captionsText").html();
                $("#captionsText").html("You are not logged in.");
                $('#captionsText').css("background-color", 'red');
                setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8'); }, 500);
                setTimeout(function(){ $('#captionsText').css("background-color", 'red'); }, 1000);
                setTimeout(function(){ $('#captionsText').css("background-color", '#E6E7E8');  $("#captionsText").html(captionText1); }, 1500);
              }
              
          }
          else{
              //captions already say "You are on the first line"
          }
    }

    
    function scrub(thisText){
        var workingText = $("<div/>").text(thisText).html();
        var encodedText = [workingText];
        if (workingText.indexOf("&gt;")>-1){
            var open = workingText.indexOf("&lt;");
            var beginTags = new Array();
            var endTags = new Array();
            var i = 0;
            while (open > -1){
                beginTags[i] = open;
                var close = workingText.indexOf("&gt;",beginTags[i]);
                if (close > -1){
                    endTags[i] = (close+4);
                } else {
                    beginTags[0] = null;
                    break;}
                open = workingText.indexOf("&lt;",endTags[i]);
                i++;
            }
            //use endTags because it might be 1 shorter than beginTags
            var oeLen = endTags.length; 
            encodedText = [workingText.substring(0, beginTags[0])];
            for (i=0;i<oeLen;i++){
                encodedText.push("<span class='previewTag'>",
                    workingText.substring(beginTags[i], endTags[i]),
                    "</span>");
                if (i!=oeLen-1){
                    encodedText.push(workingText.substring(endTags[i], beginTags[i+1]));
            }
            }
        if(oeLen>0)encodedText.push(workingText.substring(endTags[oeLen-1]));
        }
        return encodedText.join("");
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
            }).find("img").css({
                "top"   : startImgBottom - (event.pageY - mousedownPosition)
            });
            $("#imgBottom .lineColIndicatorArea").css("top", startImgBottom - (event.pageY - mousedownPosition)+"px");
            $("#dragHelper").css({
                top :   event.pageY - 90,
                left:   event.pageX - 90
            });
//            if(!event.altKey) unShiftInterface();
        })
        .mouseup(function(){
            $("#dragHelper").remove();
            $("#imgTop,#imgBottom,#imgBottom img").removeClass('noTransition');
            $(document)
            .enableSelection()
            .unbind("mousemove");
            isUnadjusted = false;
        });
    };
    
    /* Start event listening to move the image in the transcirption interface */
     function startMoveImg(){
       if($(".transcriptlet:first").hasClass("moveImage")){
           $("#moveImage").removeClass("selected");
           $(".transcriptlet").removeClass("moveImage");
           $(".transcriptlet").children("textarea").removeAttr("disabled");
           $("#imgTop, #imgBottom").css("cursor", "default");
           $("#imgTop,#imgBottom").unbind("mousedown");
       }
       else{
            $("#moveImage").addClass("selected");
            $(".transcriptlet").addClass("moveImage");
            $(".transcriptlet").children("textarea").attr("disabled", "");
            $("#imgTop, #imgBottom").css("cursor", "url("+"images/open_grab.png),auto");
            $("#imgTop,#imgBottom").mousedown(function(event){moveImg(event);});
       }
        
    }
    
    /** 
     * Allows manuscript image to be moved around.
     * Requires shift key to be held down.
     * Synchronizes movement of top and bottom images.
     * Bookmark bounding box moves with top image.
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
        $("#imgTop, #imgBottom").css("cursor", "url(images/close_grab.png),auto" );
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
            if(!event.altKey) unShiftInterface();
        })
        .mouseup(function(){
            $("#dragHelper").remove();
            $("#imgTop img,#imgBottom img,#imgTop .lineColIndicatorArea, #imgBottom .lineColIndicatorArea, #bookmark").removeClass('noTransition');
            if(!isMagnifying)$("#imgTop, #imgBottom").css("cursor", "url(images/open_grab.png),auto");
            $(document)
            .enableSelection()
            .unbind("mousemove");
            isUnadjusted = false;
        });
    };
    
    function restoreWorkspace(){
            $("#imgBottom").show();
            $("#imgTop").show();
            $("#imgTop").removeClass("fixingParsing");
            $("#transWorkspace").show();
            $("#imgTop").css("width", "100%");
            $("#imgTop img").css({"height":"auto", "width":"100%"});
            updatePresentation(focusItem[1]);
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
//For separating out different imgs on which to zoom.  Right now it is just the transcription canvas.
        if(img === "trans"){
            img = $("#transcriptionTemplate");
            $("#magnifyTools").fadeIn(800);
            $("button[magnifyimg='trans']").addClass("selected");
        }
        else if(img === "compare"){
            img= $("#compareSplit");
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
        liveTool = "image";
        mouseZoom(img,event);
//        });
    };
    /** 
     * Creates a zoom on the image beneath the mouse.
     *  
     * @param img jQuery img element to zoom on
     */
    function mouseZoom($img, event){
        isMagnifying = true;
        var imgURL = $img.find("img:first").attr("src");
        var page = $("#transcriptionTemplate");
        //var page = $(document);
        //collect information about the img
        var imgDims = new Array($img.offset().left,$img.offset().top,$img.width(),$img.height());
        //build the zoomed div
        var zoomSize = (page.height()/3 < 120) ? 120 : page.height()/3;
       
        var zoomPos = new Array(event.pageX, event.pageY );
        $("#zoomDiv").css({
            "box-shadow"    : "2px 2px 5px black,15px 15px "+zoomSize/3+"px rgba(230,255,255,.8) inset,-15px -15px "+zoomSize/3+"px rgba(0,0,15,.4) inset",
            "width"         : zoomSize,
            "height"        : zoomSize,
            "left"          : zoomPos[0] + 3,
            "top"           : zoomPos[1] + 3 - $(document).scrollTop() - $(".magnifyBtn").offset().top,
            "background-position" : "0px 0px",
            "background-size"     : imgDims[2] * zoomMultiplier+"px",
            "background-image"    : "url('"+imgURL+"')"
        });
        $(document).on({
                mousemove: function(event){
                  if (liveTool !== "image" && liveTool !== "compare") {
                    $(document).off("mousemove");
                    $("#zoomDiv").hide();
                  }
                var mouseAt = new Array(event.pageX,event.pageY);
                var zoomPos = new Array(mouseAt[0]-zoomSize/2,mouseAt[1]-zoomSize/2);
                var imgPos = new Array((imgDims[0]-mouseAt[0])*zoomMultiplier+zoomSize/2-3,(imgDims[1]-mouseAt[1])*zoomMultiplier+zoomSize/2-3); //3px border adjustment
                $("#zoomDiv").css({
                    "left"  : zoomPos[0],
                    "top"   : zoomPos[1] - $(document).scrollTop(),
                    "background-size"     : imgDims[2] * zoomMultiplier+"px",
                    "background-position" : imgPos[0]+"px " + imgPos[1]+"px"
                });
            }
          }, $img
        );
    };
    
    function removeTransition(){
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
        //$("#pageJump").attr("disabled", "disabled");
         var pageJumpIcons = $("#pageJump").parent().children("i");
            pageJumpIcons[0].setAttribute('onclick', 'firstFolio("parsing");');
            pageJumpIcons[1].setAttribute('onclick', 'previousFolio("parsing");');
            pageJumpIcons[2].setAttribute('onclick', 'nextFolio("parsing");');
            pageJumpIcons[3].setAttribute('onclick', 'lastFolio("parsing");');
        $("#prevCanvas").attr("onclick", "");
        $("#nextCanvas").attr("onclick", "");
        
        //$("#pageJump").siblings().removeAttr("onclick").css("color", "red");
//        bookmarkInfo = {"top": $("#bookmark").position().top, "left":$("#bookmark").position().left,
//            "height": $("#bookmark").height()+"px", "width":$("#bookmark").width+"px"};

        $("#imgTop").addClass("fixingParsing");
        var topImg = $("#imgTop img");
        imgRatio = topImg.width() / topImg.height();
        var wrapWidth = imgRatio*$("#transcriptionTemplate").height();
        var PAGEWIDTH = $("#transcriptionTemplate").width();
        
        if (wrapWidth > PAGEWIDTH-350)wrapWidth = PAGEWIDTH-350;
        $("#tools").children("[id$='Split']").hide();
        $("#parsingSplit").css({
            "display": "inline-block",
            "height": window.innerHeight+"px"
        }).fadeIn();
        
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
                  var height = 1/originalRatio * width;

                  $("#transcriptionCanvas").css("height", height+"px").css("width", width+"px");
                  $(".lineColIndicatorArea").css("height", height+"px");
                  var splitWidth = window.innerWidth - (width+35) + "px";
                  $(".split img").css("max-width", splitWidth);
                  $(".split:visible").css("width", splitWidth);
              },
              stop: function(event, ui){;
                  //$(".lineColIndicator .lineColOnLine").css("line-height", $(this).height()+"px");
              }
            });
        
        $("#transWorkspace,#imgBottom").hide();
        $("#noLineWarning").hide();
            window.setTimeout(function(){
                $("#imgTop, #imgTop img").height($(window).innerHeight()); 
                $("#imgTop img").css("width" , "auto");
                $("#imgTop").css("width" , $("#imgTop img").width());
                $("#imgTop").css("height" , $("#imgTop img").height());
                //At this point, transcription canvas is the original height and width of the full page image.  We can use that for when we resume transcription. 
                $("#transcriptionCanvas").css("height" , $(window).innerHeight());
                $(".lineColIndicatorArea").css("height", $(window).innerHeight());
                $("#transcriptionCanvas").css("display" , "block");
            }, 500);
            window.setTimeout(function(){
                //in here we can control what interface loads up.  writeLines draws lines onto the new full size transcription image.
                $('.lineColIndicatorArea').hide();
                writeLines($("#imgTop img"));
            },1200);
         
    };
    
    /** 
     * Overlays divs for each parsed line onto img indicated.
     * Divs receive different classes in different 
     *  
     * @param imgToParse img element lines will be represented over
     */
    function writeLines(imgToParse){
        $(".line,.parsing,.adjustable,.parsingColumn").remove(); //clear and old lines to put in updated ones
        var originalX = (imgToParse.width()/imgToParse.height())*1000;
        var setOfLines = [];
        var count = 0;
        $(".transcriptlet").each(function(index){
            count++;
            setOfLines[index] = makeOverlayDiv($(this),originalX, count);
        });
        imgToParse.parent().append($(setOfLines.join("")));
    }
    
    function makeOverlayDiv(thisLine,originalX, cnt){
        var Y = parseFloat(thisLine.attr("lineTop"));
        var X = parseFloat(thisLine.attr("lineLeft"));
        var H = parseFloat(thisLine.attr("lineHeight"));
        var W = parseFloat(thisLine.attr("lineWidth"));
        var newY = (Y);
        var newX = (X);
        var newH = (H);
        var newW = (W);
        
        var lineOverlay = "<div class='parsing' linenum='"+cnt+"' style='\n\
        top:"+newY+"%; \n\
        left:"+newX+"%; \n\
        height:"+newH+"%; \n\
        width:"+newW+"%; \n\
        ' lineserverid='"+thisLine.attr('lineserverid')+"'\n\
        linetop='"+Y+"'\n\
        lineleft='"+X+"'\n\
        lineheight='"+H+"'\n\
        linewidth='"+W+"'>\n\
        </div>";
        return lineOverlay;
    }

    /* Reset the interface to the full screen transcription view. */
    function fullPage(){;
        if ($("#overlay").is(":visible")) {
            $("#overlay").click();
            return false;
        }
        $(".line, .parsing, .adjustable,.parsingColumn").remove();
        isUnadjusted = isFullscreen = true;
        //currentFocus = "transcription" + focusItem[1].attr('id').substring(1);
        if($("#trascriptionTemplate").hasClass("ui-resizable")){
            $("#transcriptionTemplate").resizable('destroy');
        }
        $("#splitScreenTools").removeAttr("disabled");
        $("#splitScreenTools").find('option:eq(0)').prop("selected",true);
        $("#transcriptionCanvas").css("width", "100%");
        $("#transcriptionCanvas").css("height", "auto");
        $("#transcriptionTemplate").css("width", "100%");
        $("#transcriptionTemplate").css("max-width", "100%");
        $("#transcriptionTemplate").css("height", "auto");
        $("#transcriptionTemplate").css("display", "inline-block");
        $('.lineColIndicatorArea').show();
        $("#fullScreenBtn").fadeOut(250);
        isZoomed = false;
        $(".split").hide();
        $(".split").css("width", "43%");
//        //console.log("RESTORE WORKSPACE");
        
        restoreWorkspace();
        $("#splitScreenTools").show();
        var screenWidth = $(window).width();
        var adjustedHeightForFullscreen = (originalCanvasHeight2 / originalCanvasWidth2) * screenWidth;
        $("#transcriptionCanvas").css("height", adjustedHeightForFullscreen+"px");
        $(".lineColIndicatorArea").css("height", adjustedHeightForFullscreen+"px");
        $("#imgTop").hover(function(){
            var color = colorThisTime.replace(".4", "1");
            $('.activeLine').css('box-shadow', '0px 0px 15px 8px '+color);
        }, function(){
            $('.activeLine').css('box-shadow', '0px 0px 15px 8px '+colorThisTime);
        });
        $.each($(".lineColOnLine"),function(){
              $(this).css("line-height", $(this).height()+"px");
          });
          if(focusItem[0] == null && focusItem[1] == null){
              updatePresentation($("#transcriptlet_1"));
        }
        
    }

    function splitPage(event, tool) {
//        //console.log("SPLIT PAGWE!");
        liveTool = tool;
        originalCanvasHeight = $("#transcriptionCanvas").height(); //make sure these are set correctly
        originalCanvasWidth = $("#transcriptionCanvas").width(); //make sure these are set correctly
        var ratio = originalCanvasWidth/originalCanvasHeight;
        $("#splitScreenTools").attr("disabled", "disabled");
        //$("#pageJump").attr("disabled", "disabled");
        var imgBottomRatio = parseFloat($("#imgBottom img").css("top")) / originalCanvasHeight;
        var imgTopRatio = parseFloat($("#imgTop img").css("top")) / originalCanvasHeight;
        $("#transcriptionTemplate").css({
           "width"   :   "55%",
           "display" : "inline-table"
        });
        var newCanvasWidth = originalCanvasWidth2 * .55;
        var newCanvasHeight = 1/ratio * newCanvasWidth;
//        //console.log("New canvas width: "+newCanvasWidth);
//        //console.log("New canvas height: "+newCanvasHeight);
        $("#transcriptionCanvas").css({
           "width"   :   newCanvasWidth + "px",
           "height"   :   newCanvasHeight + "px"
        });
        var newImgBtmTop = imgBottomRatio * newCanvasHeight;
        var newImgTopTop = imgTopRatio * newCanvasHeight;
        $(".lineColIndicatorArea").css("height", newCanvasHeight+"px");
//        //console.log("New Position: " + imgBottomRatio + " X " +newCanvasHeight+" = "+ imgBottomRatio * newCanvasHeight);
        $("#imgBottom img").css("top", newImgBtmTop + "px");
        $("#imgBottom .lineColIndicatorArea").css("top", newImgBtmTop + "px");
        $("#imgTop img").css("top", newImgTopTop + "px");
        $("#imgTop .lineColIndicatorArea").css("top", newImgTopTop + "px");
        $.each($(".lineColOnLine"),function(){$(this).css("line-height", $(this).height()+"px");});
        $("#transcriptionTemplate").resizable({
              disabled:false,
              minWidth: window.innerWidth / 2,
              maxWidth: window.innerWidth * .75,
              start: function(event, ui){
                  originalRatio = $("#transcriptionCanvas").width() / $("#transcriptionCanvas").height();
                  //originalRatio = $("#imgTop img").width() / $("#imgTop img").height();
              },
              resize: function(event, ui) {
                  var width = ui.size.width;
                  var height = 1/originalRatio * width;
                  $("#transcriptionCanvas").css("height", height+"px").css("width", width+"px");
                  $(".lineColIndicatorArea").css("height", height+"px");
                  
                  var splitWidth = window.innerWidth - (width+35) + "px";
                  $(".split img").css("max-width", splitWidth);
                  $(".split:visible").css("width", splitWidth);
//                   //console.log("Full page split should have height "+$("#fullPageImg").height());
                  var newHeight1 = parseFloat($("#fullPageImg").height()) + parseFloat($("#fullPageSplit .toolLinks").height());
                  var newHeight2 = parseFloat($(".compareImage").height()) + parseFloat($("#compareSplit .toolLinks").height());
                  $('#fullPageSplit').css('height', newHeight1 + 'px');
                  //console.log("compare height "+newHeight2);
                  $('#compareSplit').css('height', newHeight2 + 'px');
              },
              stop: function(event, ui){
                  $.each($(".lineColOnLine"),function(){
                      var height = $(this).height() + "px";
                      $(this).css("line-height", height);
                  });
              }
            });
        $("#fullScreenBtn").fadeIn(250);
        //show/manipulate whichever split tool is activated.
        switch(tool){
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
            //When comparing, you need to be able to see the whole image, so I restrict it to window height.  To allow it to continue to grow, comment out the code below.  
            $(".compareImage").css({
                "max-height":window.innerHeight+"px",
                "max-width":$("#compareSplit").width()+"px"
            });
            populateCompareSplit(1);
            //$("#toolLinks").show();
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
              $('div[toolName="'+tool+'"]').css("display", "inline-table");

        }
        $(".split:visible").find('img').css({
            'max-height': window.innherHeight + 350 +"px",
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
        for(var i=0; i<length; i++){
            //console.log("find order = "+i)
            var thisOne = $(".previewPage[order='"+i+"']");
            ordered.push(thisOne);
            if(i===length - 1){
                //console.log("append");
                $("#previewDiv").empty();
                $("#previewDiv").append(ordered);
            }
        }
        $("#previewSplit").css({
              "display": "inline-table"
//              "height" : splitHeight+"px",
//              "width" : splitWidth
            });
    }

    function populateCompareSplit(folioIndex){
        var canvasIndex = folioIndex - 1;
        var compareSrc = transcriptionFolios[canvasIndex].images[0].resource["@id"];
        var currentCompareSrc = $(".compareImage").attr("src");
        if(currentCompareSrc !== compareSrc) $(".compareImage").attr("src", compareSrc);
    }
    /*
     * Go through all of the parsing lines and put them into columns;
     * @see linesToColumns()
     * Global Arrray: gatheredColumns
     * 
     */
    function gatherColumns(startIndex){
        var colX,colY,colW,colH;
        var lastColumnLine = -1;
        var linesInColumn = -1;
        var hasTranscription = false;
        if($(".parsing")[startIndex + 1]){
            var line = $(".parsing")[startIndex + 1];
//            //console.log("START");
//            //console.log(line);
            colX = parseFloat($(line).attr("lineleft"));
            colY = parseFloat($(line).attr("linetop"));
            colW = parseFloat($(line).attr("linewidth"));  
            var $lastLine = $(".parsing[lineleft='"+colX+"']:last");
//            //console.log("END");
//            //console.log($lastLine);
            colH = parseFloat($lastLine.attr("linetop"))-colY+parseFloat($lastLine.attr("lineheight"));

            var lastLineIndex = $(".parsing").index($lastLine);
//            //console.log("PUSH TO GATHERED COLUMNS");
            gatheredColumns.push([colX,colY,colW,colH,$(line).attr("lineserverid"),$lastLine.attr("lineserverid"),true]);
//            //console.log("RECURSIVE!");
            gatherColumns(lastLineIndex);
        }
        
        
    }
    function removeColumn(column, destroy){
        ////console.log("Called removed column for this column");
        ////console.log(column);
        if(!destroy){
            if(column.attr("hastranscription")==="true"){
                var cfrm = confirm("This column contains transcription data that will be lost.\n\nContinue?");
                if (!cfrm) return false;
            }
        }
        var colX = column.attr("lineleft");
        // collect lines from column
        var lines = $(".parsing[lineleft='"+colX+"']");
        var lineLen = lines.length;
        var lineCnt = 0;
        lines.addClass("deletable");
        removeColumnTranscriptlets(lines);
        column.remove();
     
    }
    
       
    function destroyPage(){
        nextColumnToRemove = $(".parsingColumn:first");
        var colX = nextColumnToRemove.attr("lineleft");
        var lines = $(".parsing[lineleft='"+colX+"']");
        if(nextColumnToRemove.length > 0){
            removeColumnTranscriptlets(lines, true);
        }
        else{
            cleanupTranscriptlets(true);
        }
    }
    
    /* Make parsing interface turn the lines in the view into columns */
    function linesToColumns(){
        //update lines in case of changes
        gatheredColumns = []; //The array built by gatherColumns()
        $(".parsingColumn").remove();
        if ($(".parsing").size() == 0) return false;
        //writeLines($("#imgTop img"));
        //loop through lines to find column dimensions
        var columnParameters = new Array(); // x,y,w,h,startID,endID
        var i = 0;
        var colX,colY,colW,colH;
        var lastColumnLine = -1;
        var linesInColumn = -1;
        gatherColumns(-1); //Gets all columns into an array.
        //build columns
        var columns = [];
        for (j = 0;j<gatheredColumns.length;j++){
            var parseImg = document.getElementById("imgTop").getElementsByTagName("img");
            var scaledX = gatheredColumns[j][0];
            var scaledY = gatheredColumns[j][1];
            var scaledW = gatheredColumns[j][2];
            var scaledH = gatheredColumns[j][3];
//            // recognize, alert, and adjust to out of bounds columns
            if (scaledX+scaledW > 100){
                // exceeded the right boundary of the image
                if (scaledX > 98){
                    scaledX = 98;
                    scaledW = 2;
                } else {
                    scaledW = 100-scaledX-1;
                };
            }
            if (scaledX < 0){
                // exceeded the left boundary of the image
                scaledW += scaledX;
                scaledX = 0;
            }
            if (scaledY+scaledH > 100){
                // exceeded the bottom boundary of the image
                if (scaledY > 98){
                    scaledY = 98;
                    scaledH = 2;
                } else {
                    scaledH = 100-scaledY-1;
                };
            }
            if (scaledY < 0){
                // exceeded the top boundary of the image
                scaledH += scaledY;
                scaledY = 0;
            }
            var startID = $(".parsing[lineleft='"+gatheredColumns[j][0]+"']:first").attr("lineserverid");
            var endID = $(".parsing[lineleft='"+gatheredColumns[j][0]+"']:last").attr("lineserverid");
            columns.push("<div class='parsingColumn' lineleft='",gatheredColumns[j][0],"'",
            " linetop='",gatheredColumns[j][1],"'",
            " linewidth='",gatheredColumns[j][2],"'",
            " lineheight='",gatheredColumns[j][3],"'",
            " hastranscription='",gatheredColumns[j][6]==true,"'",
            " startid='",startID,"'",
            " endid='",endID,"'",
            " style='top:",scaledY,"%;left:",scaledX,"%;width:",scaledW,"%;height:",scaledH,"%;'>",
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
//                //console.log("mouse enter column");
                var lineInfo;
                lineInfo = $("#transcription"+($(this).index(".parsing")+1)).val();
                $("#lineInfo").empty().text(lineInfo).append("<div>" + $("#t"+($(this).index(".line")+1)).find(".counter").text() +"</div>").show();
                if (!isMagnifying){
                $(this).addClass("jumpLine");
                }
            })
            .mouseleave(function(){
//                //console.log("mouse leave coumn")
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
        // if(!isMember && !permitParsing)return false;
        //prep for column adjustment
//        //console.log("adjustColumn");
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
        $.each($(".parsingColumn"),function(){
            if($(this).hasClass("ui-resizable")){
                $(this).resizable("destroy");
            }
        });
        $(".parsingColumn").resizable({
            handles     : "n,s,w,e",
            containment : 'parent',
            start       : function(event,ui){
                $("#progress").html("Adjusting Columns - unsaved").fadeIn();
                $("#columnResizing").show();
                $("#sidebar").fadeIn();
                thisColumn = $(".ui-resizable-resizing");
                thisColumnID = [thisColumn.attr("startid"),thisColumn.attr("endid")];
                adjustment = "new";
                originalPercentW = parseFloat($(this).attr("linewidth"));
                originalPercentX = parseFloat($(this).attr("lineleft"));

            },
            resize      : function(event,ui){
                if(adjustment=="new"){
                    var originalX = ui.originalPosition.left;
                    var originalY = ui.originalPosition.top;
                    var originalW = ui.originalSize.width;
                    var originalH = ui.originalSize.height;
                    var newX = ui.position.left;
                    var newY = ui.position.top;
                    var newW = ui.size.width;
                    var newH = ui.size.height;
                    var offsetForBtm = $(event.target).position().top;
                    if (Math.abs(originalW-newW)>5) adjustment = "right";
                    if (Math.abs(originalH-newH)>5) adjustment = "bottom";
                    if (Math.abs(originalX-newX)>5) adjustment = "left";    // a left change would affect w and x, order matters
                    if (Math.abs(originalY-newY)>5) adjustment = "top";     // a top change would affect h and y, order matters
                    offsetForBtm = (offsetForBtm / $("#imgTop img").height()) * 100;
                    newH = (newH / $("#imgTop img").height()) * 100;
                    var actualBottom = newH + offsetForBtm;                   
                    $("#progress").html("Adjusting "+adjustment+" - unsaved");
                }
            },
            stop        : function(event,ui){
                $("#progress").html("Column Resized - Saving...");
               
                var parseRatio = $("#imgTop img").width() / $("#imgTop img").height();
//                console.log("ratio for adjust");
//                console.log($("#imgTop img").width()+"/"+$("#imgTop img").height());
//                console.log(parseRatio);
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
                    //console.log("top");
                    //save a new height for the top line;
                    var startLine = $(".parsing[lineserverid='"+thisColumnID[0]+"']");
                    oldHeight = parseFloat(startLine.attr("lineheight"));
                    oldTop = parseFloat(startLine.attr("linetop"));

                    //This should be resized right now.  If it is a good resize, the lineheight will be > 0
                    startLine.attr({
                        "linetop"    : newY,
                        "lineheight" : oldHeight + oldTop - newY
                    });
                    startLine.css({
                        "top"    : newY +"%",
                        "height" : oldHeight + oldTop - newY +"%"
                    });
                    
                    if (parseFloat(startLine.attr("lineheight"))<0){
                            // top of the column is below the bottom of its top line
                            var newTopLine = startLine;
                            do {
                                newTopLine = startLine.next('.parsing');
                                removeLine(startLine, true);
                                removeTranscriptlet(startLine.attr("lineserverid"),startLine.attr("lineserverid"), true);
                                startLine = newTopLine;
                                oldHeight = parseFloat(startLine.attr("lineheight"));
                                oldTop = parseFloat(startLine.attr("linetop"));
                                
                            } while (parseFloat(startLine.attr("linetop")) + parseFloat(startLine.attr("lineheight")) < newY );
                            //Got through all the ones that needed removing, now I am on the one that needs resizing.
                            startLine.attr({
                                "linetop"    : newY,
                                "lineheight" : oldHeight + oldTop - newY
                            });
                            startLine.css({
                                "top"    : newY +"%",
                                "height" : oldHeight + oldTop - newY +"%"
                            });
                            thisColumn.attr("startid", startLine.attr("lineserverid"));
                        };
                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
                    } 
                    else if(adjustment=="bottom"){
                        //console.log("bottom");
                        //technically, we want to track the bottom.  The bottom if the height + top offset
                        var offsetForBtm = $(event.target).position().top;
                        offsetForBtm = (offsetForBtm / $("#imgTop img").height()) * 100;
                        newH = (newH / $("#imgTop img").height()) * 100;
                        
                        var actualBottom = newH + offsetForBtm;
                        //save a new height for the bottom line
                        var endLine = $(".parsing[lineserverid='"+thisColumnID[1]+"']");
                        
                        oldHeight = parseFloat(endLine.attr("lineheight"));
                        oldTop = parseFloat(endLine.attr("linetop"));

                        endLine.attr({
                            "lineheight" : oldHeight + (newH - originalH)
                        });
                        endLine.css({
                            "height" : oldHeight + (newH - originalH) + "%"
                        });
                        if (parseFloat(endLine.attr("linetop")) > actualBottom){
                            //the bottom line isnt large enough to account for the change, delete lines until we get to a  line that, wehn combined with the deleted lines
                            //can account for the requested change.
                            do {
                                oldHeight = parseFloat(endLine.attr("lineheight"));
                                oldTop = parseFloat(endLine.attr("linetop"));
                                var nextline = endLine.prev(".parsing");
                                endLine.remove();
                                removeLine(endLine, true);
                                removeTranscriptlet(endLine.attr("lineserverid"),endLine.attr("lineserverid"), true);                           
                                endLine=nextline;
                            } while (parseFloat(endLine.attr("linetop"))>actualBottom);
                            
                                var currentLineTop = parseFloat(endLine.attr("linetop"));
                                endLine.attr({
                                    "lineheight" : actualBottom - currentLineTop
                                });
                                endLine.css({
                                    "height" : actualBottom - currentLineTop + "%"
                                });
                                thisColumn.attr("endid", endLine.attr("lineserverid"));
                        };
                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
                    }
                    else if(adjustment=="left"){
                            //save a new left,width for all these lines
                            var leftGuide = $(".parsing[lineserverid='"+thisColumnID[0]+"']");
                            oldLeft = parseFloat(leftGuide.attr("lineleft"));
                            var ratio1 = originalPercentW / originalW;
                            var ratio2 = originalPercentX/ originalX;
                            newWidth = newW * ratio1;
                            newLeft = newX * ratio2;
                            $(".parsing[lineleft='"+oldLeft+"']").each(function(){
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
                        else if (adjustment=="right"){
                            //save a new width for all these lines
                            var rightGuide = $(".parsing[lineserverid='"+thisColumnID[0]+"']");
                            
                            oldLeft = parseFloat(rightGuide.attr("lineleft"));
                            var ratio = originalPercentW / originalW;
                            newWidth = newW * ratio; //new percent width
                            $(".parsing[lineleft='"+oldLeft+"']").each(function(){
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
    function clickedLine(e,event) {
        //Stop ability to make a new line until the update from this process is complete.
        if ($(e).hasClass("parsing")){
            if ($("#addLines").hasClass('active')||$("#removeLines").hasClass('active')){
                //console.log("show parsing cover");
                $("#parsingCover").show();
                lineChange(e,event);
            }
        }
        else{
            
        }
    }
    
    function reparseColumns(){
        $.each($('.parsingColumn'),function(){
            var colX = $(this).attr("lineleft");
            // collect lines from column
            var lines = $(".parsing[lineleft='"+colX+"']");
            lines.addClass("deletable");
            var linesSize = lines.size();
            // delete from the end, alerting for any deleted data
            for (var i=linesSize; i>0;i--){
                removeLine(lines[i], true);
            }
        });
    }
     
      
   function insertTag(tagName,fullTag){
        if (tagName.lastIndexOf("/") == (tagName.length-1)) {
            //transform self-closing tags
            var slashIndex = tagName.length;
            fullTag = fullTag.slice(0,slashIndex)+fullTag.slice(slashIndex+1,-1)+" />";
        }
        // Check for wrapped tag
        if (!addchar(escape(fullTag),escape(tagName))) {
            closeTag(escape(tagName), escape(fullTag));
        }
        
    }
    
    function closeTag(tagName,fullTag){
            // Do not create for self-closing tags
            if (tagName.lastIndexOf("/") == (tagName.length-1)) return false;
            var tagLineID = focusItem[1].attr("lineserverid");
            var closeTag = document.createElement("div");
            var tagID;
            $.get("tagTracker",{
                addTag      : true,
                tag         : tagName,
                projectID   : projectID,
                //folio       : folio,
                line        : tagLineID
            }, function(data){
                tagID = data;
                $(closeTag).attr({
                    "class"     :   "tags ui-corner-all right ui-state-error",
                    "title"     :   unescape(fullTag),
                    "data-line" :   tagLineID,
                    //"data-folio":   folio,
                    "data-tagID":   tagID
                }).text("/"+tagName);
                focusItem[1].children(".xmlClosingTags").append(closeTag);
            });
        
    }
    
    function addchar(theChar, closingTag)
    {
        //console.log("Add Char Called");
        var closeTag = (closingTag == undefined) ? "" : closingTag;
        var e = focusItem[1].find('textarea')[0];
        if(e!=null) {
            //Data.makeUnsaved();
            return setCursorPosition(e,insertAtCursor(e,theChar,closeTag));
        }
        return false;
    }
    
    function setCursorPosition(e, position)
    {
        //console.log("set cursor pos.");
        var pos = position;
        var wrapped = false;
        if (pos.toString().indexOf("wrapped") == 0) {
            pos = parseInt(pos.substr(7));
            wrapped = true;
        }
        e.focus();
        if(e.setSelectionRange) {
            e.setSelectionRange(pos,pos);
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
        //console.log("insert at cursor");
        var closeTag = (closingTag == undefined) ? "" : unescape(closingTag);
        //IE support
        if (document.selection) {
            myField.focus();
            sel = document.selection.createRange();
            sel.text = unescape(myValue);
            //Preview.updateLine(myField);
            return sel+unescape(myValue).length;
        }
        //MOZILLA/NETSCAPE support
        else if (myField.selectionStart || myField.selectionStart == '0') {
            var startPos = myField.selectionStart;
            var endPos = myField.selectionEnd;
            if (startPos != endPos) {
                // something is selected, wrap it instead
                var toWrap = myField.value.substring(startPos,endPos);
                myField.value = myField.value.substring(0, startPos)
                    + unescape(myValue)
                    + toWrap
                    + "</" + closeTag +">"
                    + myField.value.substring(endPos, myField.value.length);
                myField.focus();
               // Preview.updateLine(myField);
                var insertLength = startPos + unescape(myValue).length +
                    toWrap.length + 3 + closeTag.length;
                return "wrapped" + insertLength;              
            } else {
                myField.value = myField.value.substring(0, startPos)
                    + unescape(myValue)
                    + myField.value.substring(startPos, myField.value.length);
                myField.focus();
                return startPos+unescape(myValue).length;
            }
        } else {
            myField.value += unescape(myValue);
            myField.focus();
            return myField.length;
        }
    }

function toggleCharacters(){
    if($("#charactersPopin .character:first").is(":visible")){
        $("#charactersPopin .character").fadeOut(400);
    }
    else{
       $("#charactersPopin .character").fadeIn(400).css("display", "block"); 
    }
}
function toggleTags(){
    if($("#xmlTagPopin .lookLikeButtons:first").is(":visible")){
        $("#xmlTagPopin .lookLikeButtons").fadeOut(400);
    }
    else{
       $("#xmlTagPopin .lookLikeButtons").fadeIn(400).css("display", "block"); 
    }

}
function togglePageJump(){
    if($("#pageJump .folioJump:first").is(":visible")){
        $("#pageJump .folioJump").fadeOut(400);
    }
    else{
       $("#pageJump .folioJump").fadeIn(400).css("display", "block"); 
    }
}

/* Change the page to the specified page from the drop down selection. */
function pageJump(page,parsing){
    var folioNum = parseInt(page); //1,2,3...
    var canvasToJumpTo = folioNum - 1; //0,1,2...
    if(currentFolio !== folioNum && canvasToJumpTo >= 0){ //make sure the default option was not selected and that we are not jumping to the current folio 
        currentFolio = folioNum;
        if(parsing == "parsing"){
            $(".pageTurnCover").show();
            fullPage();
            focusItem = [null, null];  
            loadTranscriptionCanvas(transcriptionFolios[canvasToJumpTo], parsing);
            setTimeout(function(){
                hideWorkspaceForParsing();
                $(".pageTurnCover").fadeOut(1500);
            }, 800);
        }
        else{
            currentFolio = folioNum;
            focusItem = [null, null];  
            loadTranscriptionCanvas(transcriptionFolios[canvasToJumpTo], "");
        } 
    }
    else{
        //console.log("Loaded current or invalid page");
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
    if (colorList.length == 0){
        colorList = tempColorList;
    }
    colorThisTime = colorList[Math.floor(Math.random()*colorList.length)];
    colorList.splice(colorList.indexOf(colorThisTime),1);
    var oneToChange = colorThisTime.lastIndexOf(")") - 2;
    var borderColor = colorThisTime.substr(0, oneToChange) + '.2' + colorThisTime.substr(oneToChange + 1);
    var lineColor = colorThisTime.replace(".4", "1"); //make this color opacity 100
    $('.lineColIndicator').css('border', '1px solid '+lineColor);
    $('.lineColOnLine').css({'border-left':'1px solid '+borderColor, 'color':lineColor});
    $('.activeLine').css('box-shadow', '0px 0px 15px 8px '+colorThisTime); //keep this color opacity .4 until imgTop is hovered.
}

/* Toggle the line/column indicators in the transcription interface. (A1, A2...) */
function toggleLineMarkers(){
    if($('.lineColIndicator:first').is(":visible") && $('.lineColIndicator:eq(1)').is(":visible")){ //see if a pair of lines are visible just in case you checked the active line first. 
        $('.lineColIndicator').hide();
        $(".activeLine").show().addClass("linesHidden");
    }
    else{
        $('.lineColIndicator').show();
        $(".lineColIndicator").removeClass("linesHidden");
        $.each($(".lineColOnLine"),function(){$(this).css("line-height", $(this).height()+"px");});
    }
}  

/* Toggle the drawn lines in the transcription interface. */
function toggleLineCol(){
    if($('.lineColOnLine:first').is(":visible")){ 
        $('.lineColOnLine').hide();
    }
    else{
        $('.lineColOnLine').show();
        $.each($(".lineColOnLine"),function(){$(this).css("line-height", $(this).height()+"px");});
    }
}

    //updates lines
    function updateLinesInColumn(column){
        var startLineID = column[0];
        var endLineID = column[1];
        var startLine = $(".parsing[lineserverid='"+startLineID+"']"); //Get the start line
        var nextLine = startLine.next(".parsing"); //Get the next line (potentially)
        var linesToUpdate = [];
        
        linesToUpdate.push(startLine); //push first line
        
        while(nextLine.length >0 && nextLine.attr("lineserverid") !== endLineID){ //if there is a next line and its not the last line in the column...
            linesToUpdate.push(nextLine);
            nextLine = nextLine.next(".parsing");
        }
        
        if(startLineID !== endLineID){ //push the last line, so long as it was also not the first line
            linesToUpdate.push($(".parsing[lineserverid='"+endLineID+"']")); //push last line
        }
        columnUpdate(linesToUpdate);  
    }
    
    /* Bulk update for lines in a column. */
    function columnUpdate(linesInColumn){
        //console.log("Doing batch update from column resize")
        var onCanvas = $("#transcriptionCanvas").attr("canvasid");
        currentFolio = parseInt(currentFolio);
        var currentAnnoListID = annoLists[currentFolio - 1];
        var currentAnnoListResources = [];
        var lineTop, lineLeft, lineWidth, lineHeight = 0;
        var ratio = originalCanvasWidth2 / originalCanvasHeight2; 
        var annosURL = "getAnno";
        var properties = {"@id": currentAnnoListID};
        var paramOBJ = {"content": JSON.stringify(properties)};
        $.post(annosURL, paramOBJ, function(annoLists){
            //console.log("got anno list.  Here are the current resources");
            annoLists = JSON.parse(annoLists);
            var currentAnnoList; 
            $.each(annoLists, function(){
                if(this.proj === "master"){
                     currentAnnoListResources =this.resources;
                }
                if(this.proj !== undefined && this.proj!=="" && this.proj == theProjectID){
                    //These are the lines we want to draw because the projectID matches.  Overwrite master if necessary.
                    //console.log("Lines we wanna draw");
                    currentAnnoListResources =this.resources;
                    return false;
                }
            });
            //console.log(currentAnnoListResources);
            //Go over each line from the column resize.
            $.each(linesInColumn, function(){
                //console.log("line from column...");
                var line = $(this);
                lineTop = parseFloat(line.attr("linetop")) * 10 ;
                lineLeft = parseFloat(line.attr("lineleft")) * (10*ratio);
                lineWidth = parseFloat(line.attr("linewidth")) * (10*ratio);
                lineHeight = parseFloat(line.attr("lineheight")) * 10;

                //round up.
                lineTop = Math.round(lineTop,0);
                lineLeft = Math.round(lineLeft,0);
                lineWidth = Math.round(lineWidth,0);
                lineHeight = Math.round(lineHeight,0);

                line.css("width", line.attr("linewidth") + "%");

                var lineString = lineLeft+","+lineTop+","+lineWidth+","+lineHeight;
                var currentLineServerID = line.attr('lineserverid');
                var currentLineText = $(".transcriptlet[lineserverid='"+currentLineServerID+"']").find("textarea").val();

                var dbLine = 
                {
                    "@id" : currentLineServerID,
                    "@type" : "oa:Annotation",
                    "motivation" : "sc:painting",
                    "resource" : {
                      "@type" : "cnt:ContentAsText",
                      "cnt:chars" : currentLineText
                    },
                    "on" : onCanvas+"#xywh="+lineString,
                    "otherContent" : [],
                    "forProject": "TPEN_NL"
                };
                
                var index = - 1;
                //find the line in the anno list resources and replace its position with the new line resource.
                //console.log("Need to find line in anno list resources and update the array...");
                $.each(currentAnnoListResources, function(){
                    index++;
                    if(this["@id"] == currentLineServerID){
                        //console.log("found, updating index "+index);
                        currentAnnoListResources[index] = dbLine;
                        return false;
                    }
                });
            
            });
            //Now that all the resources are edited, update the list.
            var url = "updateAnnoList";
            var paramObj = {"@id":currentAnnoListID, "resources": currentAnnoListResources};
            var params = {"content":JSON.stringify(paramObj)};
            //console.log("All resources updated in array.  Write array to DB.");
            //console.log(currentAnnoListResources);
            $.post(url, params, function(data){
                //console.log("list updated with new resources array");
                currentFolio = parseInt(currentFolio);
                annoLists[currentFolio - 1]= currentAnnoListID;
            });
            
        });
        
        
    }
    
    /* Update line information for a particular line. */
    function updateLine(line, cleanup){
        var onCanvas = $("#transcriptionCanvas").attr("canvasid");
        currentFolio = parseInt(currentFolio);
        var currentAnnoListID = annoLists[currentFolio - 1];
        var currentAnnoList = "";
        var lineTop, lineLeft, lineWidth, lineHeight = 0;
        var ratio = originalCanvasWidth2 / originalCanvasHeight2; 

        lineTop = parseFloat(line.attr("linetop")) * 10 ;
        lineLeft = parseFloat(line.attr("lineleft")) * (10*ratio);
        lineWidth = parseFloat(line.attr("linewidth")) * (10*ratio);
        lineHeight = parseFloat(line.attr("lineheight")) * 10;
        
        //round up.
        lineTop = Math.round(lineTop,0);
        lineLeft = Math.round(lineLeft,0);
        lineWidth = Math.round(lineWidth,0);
        lineHeight = Math.round(lineHeight,0);
        
        //line.css("width", line.attr("linewidth") + "%");
        var lineString = lineLeft+","+lineTop+","+lineWidth+","+lineHeight;
        var currentLineServerID = line.attr('lineserverid');
        var currentLineText = $(".transcriptlet[lineserverid='"+currentLineServerID+"']").find("textarea").val();
        var dbLine = 
        {
            "@id" : currentLineServerID,
            "@type" : "oa:Annotation",
            "motivation" : "sc:painting",
            "resource" : {
              "@type" : "cnt:ContentAsText",
              "cnt:chars" : currentLineText
            },
            "on" : onCanvas+"#xywh="+lineString,
            "otherContent" : [],
            "forProject": "TPEN_NL"
        };

        var index = -1;
        
        if(currentAnnoListID !== "noList" && currentAnnoListID !== "empty"){ // if its IIIF, we need to update the list
            var annosURL = "getAnno";
            var properties = {"@id": currentAnnoListID};
            var paramOBJ = {"content": JSON.stringify(properties)};
            //console.log("Query for list...")
            $.post(annosURL, paramOBJ, function(annoList){
                //console.log("got list");
                annoList = JSON.parse(annoList);
                var annoListID = currentAnnoListID;
                currentAnnoList = annoList[0];
                //console.log(currentAnnoList);
                //console.log("Check list resources...");
               $.each(currentAnnoList.resources, function(){
                    index++;
                    if(this["@id"] == currentLineServerID){
                        //console.log("update current anno list "+annoListID+" index " + index);
                        currentAnnoList.resources[index] = dbLine;
                        var url = "updateAnnoList";
                        var paramObj = {"@id":annoListID, "resources": currentAnnoList.resources};
                        var params = {"content":JSON.stringify(paramObj)};
                        $.post(url, params, function(data){
                            //console.log("list updated");
                            //console.log(currentAnnoList.resources)
                            currentFolio = parseInt(currentFolio);
                            annoLists[currentFolio - 1]= annoListID;
                            console.log("hide cover");
                            $("#parsingCover").hide();
                        });
                    }
                });                    
            });
        }
        else if(currentAnnoList == "empty"){
           //cannot update an empty list
        }
        else if(currentAnnoList == "noList"){ //If it is classic T-PEN, we need to update canvas resources
            currentFolio = parseInt(currentFolio);
            $.each(transcriptionFolios[currentFolio - 1].resources, function(){
                index++;
                if(this["@id"] == currentLineServerID){
                    transcriptionFolios[currentFolio - 1].resources[index] = dbLine;
                }
            });
            //Should we do an update here to support old data? 
        }
       if(cleanup !== "no") cleanupTranscriptlets(true);
        //$(".previewLineNumber[lineserverid='"+currentLineServerID+"']").siblings(previewText).html(scrub(line.val()));
    }
    
    
    function saveNewLine(lineBefore, newLine){
        var theURL = window.location.href;
        var projID = -1;
        if(theURL.indexOf("projectID") === -1){
            projID = theProjectID;
        }
        else{
            projID = theURL.substring(theURL.indexOf("projectID=")+10);
        }
        
        var beforeIndex = -1;
        if(lineBefore !== undefined && lineBefore !== null){
            beforeIndex = parseInt(lineBefore.attr("linenum"));
        }
        var onCanvas = $("#transcriptionCanvas").attr("canvasid");
        var newLineTop, newLineLeft, newLineWidth, newLineHeight = 0;
        var ratio = originalCanvasWidth2 / originalCanvasHeight2; 
        newLineTop = parseFloat(newLine.attr("linetop"));
        newLineLeft = parseFloat(newLine.attr("lineleft"));
        newLineWidth = parseFloat(newLine.attr("linewidth"));
        newLineHeight = parseFloat(newLine.attr("lineheight"));
        
        newLineTop = newLineTop * 10 ;
        newLineLeft = newLineLeft * (10*ratio);
        newLineWidth = newLineWidth * (10*ratio);
        newLineHeight = newLineHeight * 10;
        
        //round up.
        newLineTop = Math.round(newLineTop , 0);
        newLineLeft = Math.round(newLineLeft,0);
        newLineWidth = Math.round(newLineWidth,0);
        newLineHeight = Math.round(newLineHeight,0);
                       
        var lineString = onCanvas + "#xywh=" +newLineLeft+","+newLineTop+","+newLineWidth+","+newLineHeight;
        var currentLineText = "";
        var dbLine = 
            {
                "@id" : "",
                "@type" : "oa:Annotation",
                "motivation" : "sc:painting",
                "resource" : {
                  "@type" : "cnt:ContentAsText",
                  "cnt:chars" : currentLineText
                },
                "on" : lineString,
                "otherContent":[],
                "forProject": "TPEN_NL"
            }
        ;
        var url = "saveNewTransLineServlet";
        var paramOBJ = dbLine;
        var params = {"content" : JSON.stringify(paramOBJ)};
//        //console.log("saving new line...");
        if(onCanvas !== undefined && onCanvas !== ""){
            $.post(url, params, function(data){
                   //console.log("saved new line");
                   //console.log(data);
                    data=JSON.parse(data);
                    dbLine["@id"] = data["@id"];
                    newLine.attr("lineserverid", data["@id"]);
                    $("div[newcol='"+true+"']").attr({
                        "startid" : dbLine["@id"],
                        "endid" : dbLine["@id"],
                        "newcol":false
                    });
                    currentFolio = parseInt(currentFolio);
                    var currentAnnoList = annoLists[currentFolio - 1];
                    if(currentAnnoList !== "noList" && currentAnnoList !== "empty"){ // if it IIIF, we need to update the list
                        var annosURL = "getAnno";
                        var properties = {"@id": currentAnnoList};
                        var paramOBJ = {"content": JSON.stringify(properties)};

                        $.post(annosURL, paramOBJ, function(annoList){
    //                        //console.log("got list");
                            var annoListID = currentAnnoList;
                            annoList = JSON.parse(annoList);
                            currentAnnoList = annoList[0];
                            if(beforeIndex == -1){
                                $(".newColumn").attr({
                                    "lineserverid" : dbLine["@id"],
                                    "linenum" : $(".parsing").length
                                }).removeClass("newColumn");
                                currentAnnoList.resources.push(dbLine);
                            }
                            else{
                                currentAnnoList.resources.splice(beforeIndex + 1, 0, dbLine);
                            }
                            currentFolio = parseInt(currentFolio);
                            transcriptionFolios[currentFolio - 1].otherContent[0] = annoListID;
                            annoLists[currentFolio - 1] = annoListID;
                            //Write back to db to update list
                            var url1 = "updateAnnoList";
                            var paramObj1 = {"@id":annoListID, "resources": currentAnnoList.resources};
                            var params1 = {"content":JSON.stringify(paramObj1)};
                            $.post(url1, params1, function(data){
    //                            //console.log("Updated list on anno store");
                                if(lineBefore !== undefined && lineBefore !== null){
                                    //This is the good case.  We called split line and saved the new line, now we need to update the other one. 
                                    updateLine(lineBefore);
                                    $("#parsingCover").hide(); //doesnt always fire, so this is to be sure
                                }
                                else{
                                    $("#parsingCover").hide();
                                }
                            });
                        });
                    
                }
                else if(currentAnnoList == "empty"){ 
                    //This means we know no AnnotationList was on the store for this canvas, and otherContent stored with the canvas object did not have the list.  Make a new one in this case. 
                      var newAnnoList = 
                        {
                            "@type" : "sc:AnnotationList",
                            "on" : onCanvas,
                            "originalAnnoID" : "",
                            "version" : 1,
                            "permission" : 0,
                            "forkFromID" : "",
                            "resources" : [],
                            "proj" : projID
                        };
                    var url2 = "saveNewTransLineServlet";
                    var params2 = {"content": JSON.stringify(newAnnoList)};
                    $.post(url2, params2, function(data){ //save new list
    //                    //console.log("new list made");
                        data=JSON.parse(data);
                        var newAnnoListCopy = newAnnoList;
                        newAnnoListCopy["@id"] = data["@id"];
                        currentFolio = parseInt(currentFolio);
                        annoLists[currentFolio - 1] = newAnnoListCopy["@id"];
                        transcriptionFolios[currentFolio - 1].otherContent[0] = newAnnoListCopy["@id"];
                        var url3 = "updateAnnoList";
                        var paramObj3 = {"@id":newAnnoListCopy["@id"], "resources": [dbLine]};
//                            //console.log(paramObj3);
                        var params3 = {"content":JSON.stringify(paramObj3)};
                        $.post(url3, params3, function(data){
//                                //console.log("New list updated with new anno");
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
                else if(currentAnnoList == "noList"){ //noList is a special scenario for handling classic T-PEN objects.
                    if(beforeIndex == -1){ //New line vs new column
                        $(".newColumn").attr({
                            "lineserverid" : dbLine["@id"],
                            "startid" : dbLine["@id"],
                            "endid" : dbLine["@id"],
                            "linenum" : $(".parsing").length
                        }).removeClass("newColumn");
                        currentFolio = parseInt(currentFolio);
                        transcriptionFolios[currentFolio - 1].resources.push(dbLine);
                    }
                    else{
                        currentFolio = parseInt(currentFolio);
                        transcriptionFolios[currentFolio - 1].resources.splice(beforeIndex + 1, 0, dbLine);
                    }
                    $("#parsingCover").hide();
                    //should we write to the DB here?  This would be in support of old data.  
                }
                console.log("call cleanup from save line");
                cleanupTranscriptlets(true);
                 
            });
        }
        else{
            alert("Cannot save line.  Canvas id is not present.");
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
        if (afterThisID === -1) {
          // new column, find its placement
          afterThisID = $(".transcriptlet").eq(-1).attr("lineserverid") || -1;
          $(".transcriptlet").each(function(index) {
            if ($(this).find('lineLeft') > newX) {
              afterThisID = (index > 0) ? $(this).prev('.transcriptlet').attr("lineserverid") : -1;
              return false;
            }
          });
            isNotColumn = false;
        } 
        var $afterThis = $(".transcriptlet[lineserverid='"+afterThisID+"']");
        var newTranscriptlet = [
            "<div class='transcriptlet transcriptletBefore' id='transciptlet_",newLineID,
            "' lineserverid='",newServerID, // took out style DEBUG
            "lineheight= ",newH,
            "linewidth= ",newW,
            "linetop= ",newY,
            "lineleft= ",newX,
            "lineid= ", ,
            "col= ", ,
            "collinenum= ", ,
            "'>\n",
            "<span class='counter wLeft ui-corner-all ui-state-active ui-button'>Inserted Line</span>\n",
            "<textarea></textarea>\n",
            "</div>"];
        if (isNotColumn){
            //update transcriptlet that was split
            $afterThis.after(newTranscriptlet.join("")).find(".lineHeight").val($(".parsing[lineserverid='"+afterThisID+"']").attr("lineheight"));                    
        } 
        else {
            if (afterThisID === -1) {
            $   ("#entry").prepend(newTranscriptlet.join(""));
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
     function splitLine(e,event){        
        //e is the line that was clicked in
        //This is where the click happened relative to img top.  In other words, where the click happened among the lines. 
        var originalLineHeight = $(e).height(); //-1 take one px off for the border
        $(".parsing").attr("newline", "false");
        var originalLineTop = $(e).offset().top - $("#imgTop").offset().top; // +1 Move down one px for the border.  
        //var originalLineTop = parseFloat($(e).css("top"));
        var clickInLines = event.pageY - $("#imgTop").offset().top;
        var lineOffset = $(e).offset().top - $("#imgTop").offset().top;
        var oldLineHeight = (clickInLines - lineOffset)/$("#imgTop").height() * 100;
        //var oldLineHeight = parseFloat($(e).css("height"));
        var newLineHeight = (originalLineHeight - (clickInLines - originalLineTop))/$("#imgTop").height() * 100;
        var newLineTop = (clickInLines/$("#imgTop").height()) * 100;
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
        var newNum = -1;
        $.each($(".parsing"), function(){
            newNum++;
            $(this).attr("linenum", newNum);
        });        
         saveNewLine($(e),newLine); 
        $("#progress").html("Line Added").fadeIn(1000).delay(3000).fadeOut(1000);
    }
    
    function removeLine(e, columnDelete){
        /**
     * Removes clicked line, merges if possible with the following line.
     * updateLine(e,additionalParameters) handles the original, resized line.
     * 
     * @param e clicked line element from lineChange(e) via saveNewLine(e)
     * @see lineChange(e)
     * @see saveNewLine(e)
     */
    console.log("remove line");
        $("#imageTip").hide();
        var removedLine = $(e);
        if(columnDelete){
            var lineID = "";
            removedLine.remove();
            return false;
        }
        else{
            if ($(e).attr("lineleft") == $(e).next(".parsing").attr("lineleft")) {
                console.log("this will be a merge....");
                removedLine = $(e).next();
                var removedLineHeight = removedLine.height();
                var currentLineHeight = $(e).height();
                var newLineHeight = removedLineHeight + currentLineHeight;
                var convertedNewLineHeight = newLineHeight / $("#imgTop").height() * 100;
                $(e).css({
                    "height" :  convertedNewLineHeight+"%",
                    "top" :     $(e).css("top")
                }).addClass("newDiv").attr({
                    "lineheight":   convertedNewLineHeight
                });
            } else if ($(e).hasClass("deletable")){ //&& $(".transcriptlet[lineserverid='"+$(e).attr("lineserverid")+"']").find("textarea").val().length > 0
                console.log("this will be a delete...");
                var cfrm = confirm("Removing this line will remove any data contained as well.\n\nContinue?");
                if(!cfrm){
                    $("#parsingCover").hide();
                    return false;
                }
                isDestroyingLine = true;
            } 
            var params = new Array({name:"remove",value:removedLine.attr("lineserverid")});
            removedLine.remove(); 
            removeTranscriptlet(removedLine.attr("lineserverid"),$(e).attr("lineserverid"), true, "cover");
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
        // if(!isMember && !permitParsing)return false;
        //update remaining line, if needed
        $("#parsingCover").show();
        var updateText = "";
        console.log("is the id of the line clicked "+updatedLineID+" == the next line "+lineid);
        var removeNextLine = false;
        if (lineid !== updatedLineID){
            console.log("No it isn't. merge");
            removeNextLine = true;
            var updatedLine =   $(".parsing[lineserverid='"+updatedLineID+"']");
            var removedLine1 = $(".parsing[lineserverid='"+lineid+"']");
            var removedLine2 = $(".transcriptlet[lineserverid='"+lineid+"']");
            var toUpdate =      $(".transcriptlet[lineserverid='"+updatedLineID+"']");
            var removedText =   $(".transcriptlet[lineserverid='"+lineid+"']").find("textarea").val();
            toUpdate.find("textarea").val(function(){
                var thisValue = $(this).val();
                if (removedText !== undefined){
                   if(removedText !== "") thisValue += (" "+removedText);
                    updateText = thisValue;
                }
                return thisValue;
            });
            console.log("line height for update is line clicked height "+ toUpdate.attr("lineheight") + " + line being removed height " + parseFloat(removedLine2.attr("lineheight")));
            var lineHeightForUpdate = parseFloat(toUpdate.attr("lineheight")) + parseFloat(removedLine2.attr("lineheight"));
            console.log(lineHeightForUpdate);
            toUpdate.attr("lineheight", lineHeightForUpdate);   
        }
        else{
            console.log("yes it is. delete!");
        }

        var index = -1;
        currentFolio = parseInt(currentFolio);
        var currentAnnoList = annoLists[currentFolio -1];
        
         if(currentAnnoList !== "noList" && currentAnnoList !== "empty"){ // if it IIIF, we need to update the list
            console.log("Get anno list");
            var annosURL = "getAnno";
                var properties = {"@id": currentAnnoList};
                var paramOBJ = {"content": JSON.stringify(properties)};
                $.post(annosURL, paramOBJ, function(annoList){
                    annoList = JSON.parse(annoList);
                    var annoListID = currentAnnoList;
                    currentAnnoList = annoList[0];
                    console.log("got it");
                    //console.log(currentAnnoList.resources);
                    $.each(currentAnnoList.resources, function(){
                        index++;
                        var lineIDToCheck = "";
                        if(removeNextLine){
                            lineIDToCheck = lineid;
                            removedLine2.remove(); //remove the transcriptlet from UI
                        }
                        else{
                            lineIDToCheck = updatedLineID;
                        }
                        console.log(this["@id"]+" == "+lineIDToCheck+"?  Index = "+index);
                        if(this["@id"] === lineIDToCheck){
                            currentAnnoList.resources.splice(index, 1);
                            console.log("Delete from list " + lineIDToCheck+" at index "+index+".  Then update with line removed.");
                            console.log(currentAnnoList.resources);
                            var url = "updateAnnoList";
                            var paramObj = {"@id":annoListID, "resources": currentAnnoList.resources};
                            var params = {"content":JSON.stringify(paramObj)};
                            $.post(url, params, function(data){
                                //console.log("update from delete finished");
                                currentFolio = parseInt(currentFolio);
                                annoLists[currentFolio - 1] = annoListID;
                                if(!removeNextLine){
                                    $("#parsingCover").hide();
                                    console.log("hide cover.");
                                }
                                else{
                                    console.log("now we have to update the line that was clicked with the new line height from the one we removed.")
                                    updateLine(toUpdate);
                                }
                                
                            });
                        }
                    });                       
                });
        }
        else if(currentAnnoList == "empty"){
            //There is no anno list assosiated with this anno.  This is an error.
        }
        else{ //If it is classic T-PEN, we need to update canvas resources
            currentFolio = parseInt(currentFolio);
            $.each(transcriptionFolios[currentFolio - 1].resources, function(){
                index++;
                if(this["@id"] == lineid){
                    transcriptionFolios[currentFolio - 1].resources.splice(index, 1);
                    //update forreal
                }
            });
        } 
        //When it is just one line being removed, we need to redraw.  When its the whole column, we just delete. 
        console.log("call cleanup from remove.  Draw: "+draw);
        cleanupTranscriptlets(draw);
    
     }
     
     /* Remove all transcriptlets in a column */
     function removeColumnTranscriptlets(lines, recurse){
        var index = -1;
        currentFolio = parseInt(currentFolio);
        var currentAnnoList = annoLists[currentFolio -1];
        //console.log("removing transcriptlets from this list");
        //console.log(currentAnnoList);
         if(currentAnnoList !== "noList" && currentAnnoList !== "empty"){ // if it IIIF, we need to update the list
        //console.log("Get annos for column removal");
            var annosURL = "getAnno";
            var properties = {"@id": currentAnnoList};
            var paramOBJ = {"content": JSON.stringify(properties)};
            $.post(annosURL, paramOBJ, function(annoList){
                annoList = JSON.parse(annoList);
                var annoListID = currentAnnoList;
                currentAnnoList = annoList[0];
                //console.log("got them");
                //console.log(currentAnnoList.resources);
                for(var l=lines.length-1; l>=0; l--){
                    var theLine = $(lines[l]);
                    var index2 = -1;
                     $.each(currentAnnoList.resources, function(){
                        var currentResource = this;
                        index2++;
                        //console.log(currentResource["@id"] +" == "+ theLine.attr("lineserverid")+"?")
                        if(currentResource["@id"] == theLine.attr("lineserverid")){
                            currentAnnoList.resources.splice(index2, 1);
                            //console.log(theLine);
                            //console.log("Delete from list " + theLine.attr("lineserverid")+" at index "+index2+".");
                            theLine.remove();
                        }
                     });

                    if(l===0){
                        //console.log("last line in column, update list");
                        //console.log(currentAnnoList.resources);
                        var url = "updateAnnoList";
                        var paramObj = {"@id":annoListID, "resources": currentAnnoList.resources};
                        var params = {"content":JSON.stringify(paramObj)};
                        $.post(url, params, function(data){
                            //console.log("update from delete finished");
                            annoLists[currentFolio - 1] = annoListID;
                            if(recurse){
                                nextColumnToRemove.remove();
                                destroyPage();
                            }
                            else{
                                console.log("call cleanup from update");
                                cleanupTranscriptlets(true);
                            }

                        });
                    }
                }
            });
                
         }
         else{
             //It was not a part of the list, but we can still cleanup the transcriptlets from the interface.  This could happen when a object is fed to the 
             //transcription textarea who instead of using an annotation list used the resources[] field to store anno objects directly with the canvas.  
             //These changes will not save, they are purely UI manipulation.  An improper, view only object has been fed to the interface at this point, so this is intentional.
             for(var l=lines.length-1; l>=0; l--){
                  var theLine = $(lines[l]);
                  theLine.remove();
                  var lineID = theLine.attr("lineserverid");
                  //console.log("remove this line: "+lineID);
                  //console.log("remove tramscriptlets");
                  $(".transcriptlet[lineserverid='"+lineID+"']").remove(); //remove the transcriptlet
                  //console.log("remove trans drawn lines");
                  $(".lineColIndicator[lineserverid='"+lineID+"']").remove(); //Remove the line representing the transcriptlet
                  //console.log("remov preview line");
                  $(".previewLineNumber[lineserverid='"+lineID+"']").parent().remove(); //Remove the line in text preview of transcription.
                }
         }
         
     }
    
    /* Re draw transcriptlets from the Annotation List information. */
    function cleanupTranscriptlets(draw) {
        console.log("cleanup.  draw:"+draw);
        var transcriptlets = $(".transcriptlet");
          if(draw){
              transcriptlets.remove();
              $(".lineColIndicatorArea").children(".lineColIndicator").remove();
              $("#parsingSplit").find('.fullScreenTrans').unbind();
              $("#parsingSplit").find('.fullScreenTrans').bind("click", function(){
                fullPage(); 
                currentFolio = parseInt(currentFolio);
                drawLinesToCanvas(transcriptionFolios[currentFolio-1], "");
              });
          }

    }
 
 /* Make some invalid information inside of folios valid empties */
function scrubFolios(){
    //you could even force create anno lists off of the existing resource here if you would like.  
    var cnt1 = -1;
    $.each(transcriptionFolios, function(){
        cnt1++;
        var canvasObj = this;
        if(canvasObj.resources && canvasObj.resources.length > 0){
            //alert("Canvas "+canvasObj["@id"]+" does not contain any transcription lines.");
            if(canvasObj.images === undefined || canvasObj.images === null){
                canvasObj.images = [];
            }
            var cnt2 = -1;
            $.each(canvasObj.resources, function(){
                    cnt2 += 1;
                    if(this.resource && this.resource["@type"] && this.resource["@type"] === "dctypes:Image"){
                        canvasObj.images.push(this);
                        canvasObj.resources.splice(cnt2,1);
                        transcriptionFolios[cnt1] = canvasObj;
                    }
                });
        }
        if(canvasObj.otherContent === undefined){
            transcriptionFolios[cnt1].otherContent = [];
        }
        });
}

/* Control the hiding and showing of the image tools in the transcription interface. */
function toggleImgTools(){
    if($("#imageTools").attr("class")!==undefined && $("#imageTools").attr("class").indexOf("activeTools") > -1){
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

function stopMagnify(){
    isMagnifying = false;
    zoomMultiplier = 2;
    $(document).off("mousemove");
    $("#zoomDiv").removeClass("ui-state-active");
    $("#zoomDiv").hide();
    $(".magnifyBtn").removeClass("ui-state-active");
    $("#magnifyTools").fadeOut(800);
//                    $("#imgBottom img").css("top", imgBottomOriginal);
//                    $("#imgBottom .lineColIndicatorArea").css("top", imgBottomOriginal);
    $(".lineColIndicatorArea").show();
    $(".magnifyHelp").hide();
    $("button[magnifyimg='full']").removeClass("selected");
    $("button[magnifyimg='compare']").removeClass("selected");
    $("button[magnifyimg='trans']").removeClass("selected");
    restoreWorkspace();
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
        $(this).attr("src",src);
    });
}

// Shim console.log to avoid blowing up browsers without it
if (!window.console) window.console = {};
if (!window.console.log) window.console.log = function () { };
