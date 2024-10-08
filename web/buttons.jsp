<%-- 
    Document   : buttons
    Created on : Oct 26, 2010, 1:38:08 PM
    Author     : jdeerin1
--%> 
<%@page import="textdisplay.Project"%>

<%
            int UID = 0;
            if (session.getAttribute("UID") == null) {
%>              <%@ include file="loginCheck.jsp" %><%
            } else {
                UID = Integer.parseInt(session.getAttribute("UID").toString());
            }%>
<%@page import ="textdisplay.Hotkey"%>
<%@page import ="textdisplay.TagButton"%>
<%@page import ="user.*"%>
<%@page contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Button Management</title>
        <link rel="stylesheet" href="css/tpen.css" type="text/css" media="screen, projection">
        <link rel="stylesheet" href="css/print.css" type="text/css" media="print">
        <!--[if lt IE 8]><link rel="stylesheet" href="css/ie.css" type="text/css" media="screen, projection"><![endif]-->
        <link type="text/css" href="css/custom-theme/jQuery.css" rel="Stylesheet" />
        <link type="text/css" href="css/jquery.simple-color-picker.css" rel="Stylesheet" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.js"></script> 
        <script type="text/javascript" src="js/tpen.js"></script>
        <link rel="shortcut icon" type="image/x-icon" href="favicon.ico">
<!--        <script src="js/jquery.simple-color-picker.js" type="text/javascript"></script>-->
        <style type="text/css" >
                input[type=text]{font-size: 14px;width: 200px;}
                #sortable1 .ui-sortable-helper{padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; width: 180px !important; height:44px !important;}
                #sortable2 .ui-sortable-helper{padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; width: 430px !important; height:36px !important;}
                #sortable1 .ui-state-highlight{min-height:44px;}
                #sortable2 .ui-state-highlight{height:20px;}
                #sortable { margin: 0; padding: 0; width: 30%; }
                #sortable2{
                   margin-bottom: 25px;
                }
                #sortable1 li, #sortable1b li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; width: 180px; position:relative;}
                #sortable1 input[type=text] {width: 80px;}
                #sortable2 li { margin: 0 3px 3px 3px; padding: 0.4em; padding-left: 1.5em; width:430px; position:relative;}
                span.tag {position:relative;}
                span.tag:before {
                    content:"\003c";
                    position: absolute;
                    left:5px;
                }
                span.tag:after {
                    content:"\003e";
                    position:absolute;
                    right:5px;
                }
                span.tag input {width: 160px;font-weight: bold;padding:1px 20px;}
                a.ui-icon-closethick:hover {background-image: url(css/custom-theme/images/ui-icons_cd0a0a_256x240.png);}
                .disabled { margin: 0 3px 3px 23px; padding: 0.4em; padding-left: 1.5em; font-size: 1.4em; height: 18px;width:650px;}
/*                input.colors {width:16px !important; height: 16px !important;background-color:transparent;background-image: url("images/color-picker.png");position: absolute;left: 55%;}
                input.colors:focus {color:black !important;background: white none !important;width:84px !important;}*/
                .lastRow,.secondRow {display: none;}
                .moreParameters,.ui-icon-closethick {cursor: pointer;margin:2px -5px;}
                .collapseXML{display: none;}
                .importButton {padding:0.4em 1em;position: relative;}
                .badInfo {border: thin solid red !important; box-shadow:inset 0 0 10px red;}
                .colors {display: none;} /* TODO show when colors are supported */
                #codeSearch{padding: .4em 1em;width:145px;}
                .tagWarning {position: absolute;top:0;left:0;}
                .tag .tagWarning {top:-28px;font-size: smaller;white-space: nowrap; width: 300px;}
        </style>
    <%
            user.User thisUser = null;
            if (session.getAttribute("UID") != null) {
                thisUser = new user.User(Integer.parseInt(session.getAttribute("UID").toString()));
                }
            Project thisProject = null;
            String projectAppend = "";
            int projectID = 0;
            if (request.getParameter("projectID") == null){
                textdisplay.Project[] p = thisUser.getUserProjects();
                if (p.length > 0) {
                    projectID = p[0].getProjectID();
                }
                if (projectID > 0) {
                    %>
                    <script type="text/javascript">
                        document.location.href = "buttons.jsp?projectID=<%out.print(projectID);%>";
                    </script>
        <%
                    //response.sendRedirect("buttons.jsp?projectID=" + projectID);
                } else {
                String errorMessage = "No project has been indicated.";
            %><%@include file="WEB-INF/includes/errorBang.jspf" %><%
                }
                return;
            }
            String p = "";
            if (request.getParameter("p") != null){
                p = request.getParameter("p");
            }
            projectID = Integer.parseInt(request.getParameter("projectID"));
            thisProject = new Project(projectID);
            projectAppend = (p.length()>0)? "&projectID=" + projectID + "&p=" + p : "&projectID=" + projectID;
            out.println("<script>");
            out.println("var projectID="+projectID+";");
            out.println("var projectAppend='"+projectAppend+"';");
            out.println("</script>");                               
%>
    <script type="text/javascript">
	$(function() {
//            $( ".returnButton" ).css("display","inline-block");
            $( ".sortable" ).disableSelection();
            $( "#sortable1, #sortable2" ).sortable({
                connectWith: ".connectedSortable",
                placeholder: "ui-state-highlight ui-state-disabled",
                axis:'y'
            })
                .css("cursor","move");
            var $tabs = $( "#tabs" ).tabs();
            $(".tag").children('input').keyup(function(){
                unsavedAlert('#tabs-2');
                isValidTag(this);
            });
            $(".moreParameters").click(function(){
                $(this)
                    .hide()
                    .parent().next().slideDown();
            });
            $(".xmlPanel").focusin(function() {
                $(".xmlPanel").not(this).each(function(){collapsePanel(this);});
                expandPanel(this);
            });
            $("#addT").click(function(){
                var addTagData = {projectID:projectID};
                $.post("addTag", $.param(addTagData),function(data){
                    var position = data;    //tag position from servlet
                    //$("#sortable2").children("li").eq(-1).clone(true).appendTo($("#sortable2"))
                    var buttonHTML = "";
                    buttonHTML += "<li class=\"ui-state-default xmlPanel\">";
                    buttonHTML += "<span class='ui-icon ui-icon-arrow-4 toggleXML left'></span>";
                    buttonHTML += "<a class=\"ui-icon ui-icon-closethick right\" onclick=\"deleteTag(" + position + ");\">delete</a>";
                    buttonHTML += "<input class=\"description\" onchange=\"unsavedAlert('#tabs-2');\" type=\"text\" placeholder=\"Button Name\" name=description"+(position)+" value=\"description\">";
                //    out.println("<input class=\"colors\" onchange=\"unsavedAlert('#tabs-2');\" type=\"text\" placeholder=\"black\" name=xmlColor"+(position)+" value=\""+"b.getXMLColor"+"\">");
                    buttonHTML += "<div class='xmlParams'>";
                    buttonHTML += "<span class=\"firstRow expandXML\"><span class=\"bold tag\"><input name=\"b"+position+"\" id=\"b"+position+"\" type=\"text\" class='collapseXML' value=\"New Tag\"></input></span>";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p1\" />";
                    buttonHTML += "<span class=\"right ui-icon moreParameters ui-icon-plus\" title=\"Add more parameters to this button\"></span></span>"; //close .firstRow%>
                    buttonHTML += "<span class='clear-left secondRow collapseXML'>";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p2\" />";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p3\" />";
                    buttonHTML += "<span class='right ui-icon moreParameters ui-icon-plus' title='Add more parameters to this button'></span>";
                    buttonHTML += "</span>";
                    buttonHTML += "<span class='clear-left lastRow expandXML'>";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+">p4\" />";
                    buttonHTML += "<input onchange=\"unsavedAlert('#tabs-2');\" placeholder=\"parameter\" type=\"text\" name=\"b"+position+"p5\" />";
                    buttonHTML += "</span>";
                    
                    var theButton = $(buttonHTML);
                    $("#sortable2").append(theButton); //Make the new button and put it in
//                    .children("input.colors").attr("name","xmlColor"+position).val("black").end()
                    $("#sortable2").children(".xmlParams").find("input").each(function(index,param){
                        var rename = (index > 0) ? "b"+position+"p"+index :  "b"+position;
                        $(param).attr("name", rename).val("");
                    });
                    $("[name='b"+position+"']").val("New Tag");
                    $(".tag").children('input').keyup(function(){
                        unsavedAlert('#tabs-2');
                        isValidTag(this);
                    });
                    $(".moreParameters").click(function(){
                        $(this)
                            .hide()
                            .parent().next().slideDown();
                    });
                    $(".xmlPanel").focusin(function() {
                        $(".xmlPanel").not(this).each(function(){collapsePanel(this);});
                        expandPanel(this);
                    });
                },"html");           
            });
            $("#addH").click(function(){
                var addHotkeyData = {projectID:projectID};
                $.post("addHotkey", $.param(addHotkeyData),function(data){
                    var position = data;    //tag position from servlet
                    var newCharHTML = $("<li class=\"ui-state-default\"><input readonly class=\"label hotkey\" value='42' tabindex=-5>\n\
                    <input class=\"shrink\" onkeyup=\"updatea(this);\" name=\"a"+position+"\" id=\"a"+position+"\" type=\"text\" value='42'></input>\n\
                    <a class=\"ui-icon ui-icon-closethick right\" onclick=\"deleteHotkey(" + position + ");\">delete</a></li>");
                        
                    newCharHTML.appendTo($("#sortable1"))
                    .children("input.label").attr("id", "a"+position+"a").val("-").end()
                    .children("input.shrink").attr({
                        "name":"a"+position,
                        "id":"a"+position
                    }).val("42").end()
                    .children("a").attr("onclick","deleteHotkey("+position+");");
                },"html");           
            });
            
            $("#updateChars").click(async function(){
                var listItems = $("#sortable1 li");
                var chars = [];
                listItems.each(function(i, li) {
                    chars[i] = parseInt($(li).children(".shrink")[0].value);
                });
                var allCharData = {"projectID":projectID, "chars":chars}
                await fetch("updateSpecialCharacters", {
                    method: "POST",
                    mode: "cors",
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    body: JSON.stringify(allCharData)
                })
                .then(response => {
                    if(response.ok){
                        $("#buttonForm").submit();
                    }
                })         
            });
            
            $("#updateXML").click(async function(){
                var listItems = $("#sortable2 li");
                var entries = [];
                listItems.each(function(i, li) {
                    var description = $(li).children(".description")[0].value
                    var $allParams = $(li).children(".xmlParams")
                    var tag = $allParams.children(".firstRow").children(".tag").children("input")[0].value
                    var $param1 = $allParams.children(".firstRow").children("input[placeholder='parameter']");
                    var $params23 = $allParams.children(".secondRow").children("input[placeholder='parameter']");
                    var $params45 = $allParams.children(".lastRow").children("input[placeholder='parameter']");
                    var $params123 = $.merge( $.merge( [], $param1 ), $params23 );
                    var $params = $.merge( $.merge( [], $params123 ), $params45 );
                    var params_arr = [];
                    for(var j=0; j<$params.length; j++){
                        params_arr[j] = $params[j].value
                    }
                    var entryObj = {
                        "description" : description,
                        "tag" : tag,
                        "params":params_arr
                    }
                    entries[i] = entryObj
                });
                var allXMLData = {"projectID":projectID, "xml":entries}
                await fetch("updateXMLEntries", {
                    method: "POST",
                    mode: "cors",
                    headers: {
                        'Content-Type': 'application/json;charset=utf-8'
                    },
                    body: JSON.stringify(allXMLData)
                })
                .then(response => {
                    if(response.ok){
                        document.getElementById('selecTab').value = 1;
                        $("#buttonForm").submit();
                    }
                })         
            });
            $('#tabs').tabs({
                show:equalWidth,
                selected:<%if (request.getParameter("selecTab") != null) {
                out.print(request.getParameter("selecTab"));
            } else {
                out.print('0');
            };%>
            });
            //$('input:text').not(".colors").focus(hideBox(box));
//            $('input.colors')
//                .focusin(function(){$(".color-picker").hide();$(this).select();})
//                .blur(function() {
//                    var newColor = $(this).val();
//                    var bgColor = "#444";
//                    var color = (colorNameToHex(newColor).length == 7) ? colorNameToHex(newColor).substr(1,1)+colorNameToHex(newColor).substr(3,1)+colorNameToHex(newColor).substr(5,1) : colorNameToHex(newColor).substr(1,3);
//                    if(parseInt(color)==NaN)color="FFF";
//                    $(this).css({'color':'black','background':newColor});
//                    if (color<"888"){
//                        //dark color selected
//                        bgColor = "white";
//                        $(this).css({'color':'white'});
//                    }
//                    $(this).prev().css({'color':newColor,'background':bgColor});
//                    $(".color-picker").hide("fade");
//                })
//                .simpleColorPicker({ showEffect: 'slide', hideEffect: 'fade' });
            $('.ui-icon-closethick').hover(function()
                {$(this).parent().addClass("ui-state-error");},
                function(){$(this).parent().removeClass("ui-state-error");}
            );
            $('.toggleXML').click(function(){
                if ($(this).hasClass("ui-icon-arrow-4")) {
                    expandPanel($(this).parent("li"));
                } else {
                    collapsePanel($(this).parent("li"));
                }
            });
            $(".lookLikeButtons").prop("onclick",null);
        });
var minWidth = 70;
function equalWidth(){
    $("#xmlReview").children("span").each(function(){
        minWidth = ($(this).width()>minWidth) ? $(this).width() : minWidth;
    }).css({"min-width":minWidth+"px"});
}
    function expandPanel(btn) {
        $(btn)
            .find(".collapseXML").switchClass("collapseXML","expandXML").end()
            .find(".ui-icon-arrow-4").switchClass("ui-icon-arrow-4","ui-icon-arrowstop-1-n").end()
            .find(".xmlParams").show(); //we want some kind of animation here, fadeIn() and slideDown() break this.  
    }
    function collapsePanel(btn) {
        $(btn)
            .find(".xmlParams").hide().end() //we want some animation instead of hide(), but it seems to complicate stuff.  
            .find(".expandXML").switchClass("expandXML","collapseXML").end()
            .find(".ui-icon-arrowstop-1-n").switchClass("ui-icon-arrowstop-1-n","ui-icon-arrow-4");
    }
    function updatea(obj) {
        var objValue = obj.value;
        var decimalTest =/^[0-9]+(\.[0-9]+)+$/;
        if (isNaN(objValue) || objValue < 32 || objValue > 65518){
            $(obj).addClass('badInfo').prev("input").val(" ").parent().attr("title","Please use a valid unicode decimal (1-65518)\nUse the link on this page to find a complete list.");
        } else {
            if (!objValue.match(decimalTest)){
                obj.value = parseInt(objValue);
            }
            $(obj).removeClass('badInfo').prev("input").val(String.fromCharCode(objValue)).parent().attr("title","");
    //        var idnum=obj.id;
    //        idnum+="a";
    //        document.getElementById(idnum).value=String.fromCharCode(obj.value);
            unsavedAlert("#tabs-1");
        }
    }
    /* Check for self-closing */
    function checkType ($tag) {
        var tagName = $tag.find(".tag").val();
        if (tagName.lastIndexOf("/") == (tagName.length-1)){
            $tag.addClass("selfClosing");
        }
    }
    /* Swap ids to reorder the buttons when saved */
    function rebuildOrder() {
        //interrupt saving if bad data is included
        if($(".badInfo").length > 0){
            var thisValue = $(".badInfo").val();
            alert("\""+thisValue+"\" is not valid unicode.\n\nPlease enter a number from 32-65518.");
            $(".badInfo").show('pulsate',500);
            return false;
        }
        var hotkeyLoop = $("#tabs-1").find("input:text").not(".label");
        var xmlLoop = $("#tabs-2").find("input:text");
        for (var i=0;i<hotkeyLoop.length;i++){
            hotkeyLoop[i].name = "a"+(i+1);
        }
        for (var k=0;k<xmlLoop.length;k=k+7) {
            xmlLoop[k].name = "description"+(k/7+1);
            xmlLoop[k+1].name = "b"+(k/7+1);
            for (var j=0;j<5;j++){
                if(k+2+j < xmlLoop.length){
                    //k+2+j ends up == xmlLoop.length, we need to make sure that we don't try to access the array in this case.
                    xmlLoop[k+2+j].name = "b"+(k/7+1)+"p"+(j+1); 
                    if (xmlLoop[k+2+j].value == 'null') xmlLoop[k+2+j].value = '';
                }
                else{
                    //Stop all together?  Not sure what to do here
                }
            }
        }
        return true;
    }
    function unsavedAlert(idRef) {
        $("#tabs").find("a[href='"+idRef+"']").next().stop(true,true).show("pulsate","fast");
    }
    function isValidTag(tag){
        var tagVal = $(tag).val();
        var isIllegalChar  = /[^\w:\.\-]/;
        var hasSpace = /\s/;
        var validStart = /^\w|:/;
        var notDigitStart = /^\D/;
        var xmlStart = /^xml/i;
        var closingTag = /\s\//; // &nbsp;/  = self closing tag for this field.  This holds up logically.
        var msg = '';
        if(closingTag.test(tagVal)){
            msg = ['info','highlight','Using the / will make this a self closing tag.'];
        } else if (hasSpace.test(tagVal)){
            msg = ['alert','error','XML does not allow spaces in tag names'];
        } else if (isIllegalChar.test(tagVal)){
            msg = ['alert','error','XML allows only letters, digits, .,-,_, and : in tag names'];
        } else if (!validStart.test(tagVal) || !notDigitStart.test(tagVal)){
            msg = ['alert','error','XML tags must begin with a letter, _, or :'];
        } else if (xmlStart.test(tagVal)){
            msg = ['info','highlight','Beginning tag names with "XML" should be avoided'];
        }
        $(tag).parents('.xmlPanel').find('.tagWarning').remove();
        if (msg.length == 3){
            var warning     = "<div class='tagWarning ui-corner-all ui-state-"+msg[1]+"'><span class='ui-icon left ui-icon-"+msg[0]+"'></span>"+msg[2]+"</div>";
            var warningFlag = "<div class='tagWarning ui-corner-all ui-state-"+msg[1]+"'><span class='ui-icon left ui-icon-"+msg[0]+"' title='"+msg[2]+"'></span></div>";
            $(tag).after(warning)
            .parents('.xmlPanel').find('.description').after(warningFlag);
            if(msg[1] === "error"){
                $("#updateXML").attr("disabled", "disabled");
            }
            else{
                $("#updateXML").removeAttr("disabled");
            }
        }
        else{
            $("#updateXML").removeAttr("disabled");
        }
            
    }
    function deleteHotkey(position){
        $("#buttonForm")
            .append("<input type='hidden' value=true name='deletehotkey'/><input type='hidden' value="+position+" name='position'/>")
            .submit();
    }
    function deleteTag(position){
        document.getElementById('selecTab').value = 1;
        $("#buttonForm")
            .append("<input type='hidden' value=true name='deletetag'/><input type='hidden' value="+position+" name='position'/>")
            .submit();
    }
//    function colorNameToHex(color)
//{
//    var colors = {"aliceblue":"#f0f8ff","antiquewhite":"#faebd7","aqua":"#00ffff","aquamarine":"#7fffd4","azure":"#f0ffff",
//    "beige":"#f5f5dc","bisque":"#ffe4c4","black":"#000000","blanchedalmond":"#ffebcd","blue":"#0000ff","blueviolet":"#8a2be2","brown":"#a52a2a","burlywood":"#deb887",
//    "cadetblue":"#5f9ea0","chartreuse":"#7fff00","chocolate":"#d2691e","coral":"#ff7f50","cornflowerblue":"#6495ed","cornsilk":"#fff8dc","crimson":"#dc143c","cyan":"#00ffff",
//    "darkblue":"#00008b","darkcyan":"#008b8b","darkgoldenrod":"#b8860b","darkgray":"#a9a9a9","darkgreen":"#006400","darkkhaki":"#bdb76b","darkmagenta":"#8b008b","darkolivegreen":"#556b2f",
//    "darkorange":"#ff8c00","darkorchid":"#9932cc","darkred":"#8b0000","darksalmon":"#e9967a","darkseagreen":"#8fbc8f","darkslateblue":"#483d8b","darkslategray":"#2f4f4f","darkturquoise":"#00ced1",
//    "darkviolet":"#9400d3","deeppink":"#ff1493","deepskyblue":"#00bfff","dimgray":"#696969","dodgerblue":"#1e90ff",
//    "firebrick":"#b22222","floralwhite":"#fffaf0","forestgreen":"#228b22","fuchsia":"#ff00ff",
//    "gainsboro":"#dcdcdc","ghostwhite":"#f8f8ff","gold":"#ffd700","goldenrod":"#daa520","gray":"#808080","green":"#008000","greenyellow":"#adff2f",
//    "honeydew":"#f0fff0","hotpink":"#ff69b4",
//    "indianred ":"#cd5c5c","indigo ":"#4b0082","ivory":"#fffff0","khaki":"#f0e68c",
//    "lavender":"#e6e6fa","lavenderblush":"#fff0f5","lawngreen":"#7cfc00","lemonchiffon":"#fffacd","lightblue":"#add8e6","lightcoral":"#f08080","lightcyan":"#e0ffff","lightgoldenrodyellow":"#fafad2",
//    "lightgrey":"#d3d3d3","lightgreen":"#90ee90","lightpink":"#ffb6c1","lightsalmon":"#ffa07a","lightseagreen":"#20b2aa","lightskyblue":"#87cefa","lightslategray":"#778899","lightsteelblue":"#b0c4de",
//    "lightyellow":"#ffffe0","lime":"#00ff00","limegreen":"#32cd32","linen":"#faf0e6",
//    "magenta":"#ff00ff","maroon":"#800000","mediumaquamarine":"#66cdaa","mediumblue":"#0000cd","mediumorchid":"#ba55d3","mediumpurple":"#9370d8","mediumseagreen":"#3cb371","mediumslateblue":"#7b68ee",
//    "mediumspringgreen":"#00fa9a","mediumturquoise":"#48d1cc","mediumvioletred":"#c71585","midnightblue":"#191970","mintcream":"#f5fffa","mistyrose":"#ffe4e1","moccasin":"#ffe4b5",
//    "navajowhite":"#ffdead","navy":"#000080",
//    "oldlace":"#fdf5e6","olive":"#808000","olivedrab":"#6b8e23","orange":"#ffa500","orangered":"#ff4500","orchid":"#da70d6",
//    "palegoldenrod":"#eee8aa","palegreen":"#98fb98","paleturquoise":"#afeeee","palevioletred":"#d87093","papayawhip":"#ffefd5","peachpuff":"#ffdab9","peru":"#cd853f","pink":"#ffc0cb","plum":"#dda0dd","powderblue":"#b0e0e6","purple":"#800080",
//    "red":"#ff0000","rosybrown":"#bc8f8f","royalblue":"#4169e1",
//    "saddlebrown":"#8b4513","salmon":"#fa8072","sandybrown":"#f4a460","seagreen":"#2e8b57","seashell":"#fff5ee","sienna":"#a0522d","silver":"#c0c0c0","skyblue":"#87ceeb","slateblue":"#6a5acd","slategray":"#708090","snow":"#fffafa","springgreen":"#00ff7f","steelblue":"#4682b4",
//    "tan":"#d2b48c","teal":"#008080","thistle":"#d8bfd8","tomato":"#ff6347","turquoise":"#40e0d0",
//    "violet":"#ee82ee",
//    "wheat":"#f5deb3","white":"#ffffff","whitesmoke":"#f5f5f5",
//    "yellow":"#ffff00","yellowgreen":"#9acd32"};
//
//    if (typeof colors[color.toLowerCase()] != 'undefined')
//        return colors[color.toLowerCase()];
//
//    return color;
//}
</script>
    </head>
    <%
                if (request.getParameter("deletetag") != null) {
                    int pos = Integer.parseInt(request.getParameter("position"));
                    TagButton b = new TagButton(projectID, pos, true);
                    b.deleteTag();
                }
                if (request.getParameter("deletehotkey") != null) {
                        int pos = Integer.parseInt(request.getParameter("position"));
                        Hotkey b = new Hotkey(projectID, pos, true);
                        b.delete();
                }
    %>
    <body id="buttonPage">
        <div id="wrapper">
        
        <div id="header"><p align="center" class="tagline">transcription for paleographical and editorial notation</p></div>
            <div id="content">
                <h1><script>document.write(document.title); </script></h1>
        <div class="widget">
            <form id="buttonForm" action="buttons.jsp" onsubmit="return rebuildOrder();" onkeypress="return event.keyCode!=13;" method="POST">
                <input type="hidden" value="<%out.print(projectID);%>" name="projectID" />
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
	<ul>
            <li><a href="#tabs-1">Special Characters</a><span title="There may be unsaved changes on this tab" class="right ui-icon ui-icon-alert" style="display:none;"></span></li>
		<li><a href="#tabs-2">Custom XML</a><span title="There may be unsaved changes on this tab" class="right ui-icon ui-icon-alert" style="display:none;"></span></li>
	</ul>
                <div id="tabs-1">
                    <div class="right" style="width:50%;">
                        <h2>Hotkeys</h2>
                        <p>Enter the unicode value for each character.</p>
                        <a id="codeSearch" target="_blank" class="tpenButton ui-button ui-button-text-only" href="//www.ssec.wisc.edu/~tomw/java/unicode.html"><span class="left ui-icon ui-icon-search"></span>Search for Codes</a>
                        <p>Buttons are mapped to the digits 1-9 on your keyboard. <span class="loud">Hold CTRL and press the corresponding number key</span> to insert one into your transcription.</p>
                        <p>Rearrange by clicking on the blue box and dragging to a new position. Click the X to remove a button.</p>
                        <p>You may add more than 9, but only the first 9 will be mapped to shortcuts.</p>
                    </div>
                        <ul id="sortable1" class="connectedSortable ui-helper-reset">
            <%
                    /* This should use the same get all hotkeys as the rest of the site... */
                    //Hotkey ha;
                    int ctr = 1; //There are lots of position 0 buttons, they are ignored by all the getters now since they should never happen.  That way the same buttons are displayed consitently. 
                    //In the future, we need to figure out how 0s got in there and stop it from happening. 
                    try {
                        String ref = request.getHeader("referer");
                        if (ref.contains("transcription")) {
                            session.setAttribute("ref", request.getHeader("referer"));
                        }
                    } catch (NullPointerException e) {
                        //They didnt get here from another page, maybe a bookmark. Not a big deal
                    }                   
                    out.print(Hotkey.javascriptToBuildEditableButtons(projectID));
            %>
                    </ul>
            <input type="button" id="addH" name="addH" class="tpenButton ui-button" value="Add a Button"/>
            <input type="button" id="updateChars" name="update" value="Save Changes" class="tpenButton ui-button"/>
            <input type="button" id="return" name="return" value="Return to Management" onclick="document.location.href='project.jsp?projectID=<%out.print(projectID);%>';" class="tpenButton ui-button"/><br><br>
                </div>
                <div id="tabs-2">
                    <div class="right" style="width:440px;">
                        <h2>Custom XML Tags</h2>
                        <p>Add each tag without &lsaquo;angle brackets&rsaquo;, any parameters including &quot;quotes&quot; in the next 5 fields, and a description in the final field. Text in the <span class="loud">"description"</span> field will become the title of your button. Seek conciseness.</p>
                        <p>Rearrange by clicking on the blue box and dragging. Click the "X" icon to remove a button from the list. Add as many buttons as is useful. You will access them through the footer menu.</p>
                    </div>
                    <ul id="sortable2" class="connectedSortable ui-helper-reset">
                    <%
                    String appendProject = "&projectID="+projectID;
                    out.print(TagButton.buildAllProjectXML(projectID)); //prints out all the XML tags.
                    %>
                    </ul>
                    <input type="button" id="addT" name="addT" value="Add a Tag" class="tpenButton ui-button" onclick="document.getElementById('selecTab').value = 1;" />
                    <input type="button" id="updateXML" name="update" value="Save Changes" class="tpenButton ui-button"/><br><br>
                </div>
                <div class="right">
                    <a href="buttonProjectImport.jsp?a=1<%out.print(appendProject);%>" class="importButton tpenButton ui-button">Copy Buttons from Another Project</a>
                    <%
                    if (thisProject.getSchemaURL().length() > 5){%>
                    <a href="buttonSchemaImport.jsp?<%out.print(appendProject);%>" class="importButton tpenButton ui-button">Import Buttons from Schema</a>
                    <%}%>
                </div>
            </div>
            <input type="hidden" name="selecTab" id="selecTab" value="0"/>
        
        <%if (p.length() > 0){
            p = request.getParameter("p");
            out.print("<input type='hidden' name='p' value='"+p+"'/>");
        }%>
            </form>
     </div>
        <%
        if (projectID > 0){
            String pPiece = "";
            if(p != ""){
                if(Integer.parseInt(p) > 0){
                    pPiece = "p="+p;
                }
            }
            out.print("<a class=\"returnButton\" href=\"transcription.html?" + pPiece + appendProject + "\">Return to Transcribing</a>");
        %><a class="returnButton" href="project.jsp?<%out.print(projectAppend);%>">Project Management</a><%
        }
        else {%>
        <a class="returnButton" href="project.jsp?<%out.print(projectAppend);%>">Return to Project Management</a>
        <%}%>        
        <a class="returnButton" href="index.jsp">T&#8209;PEN Home</a>
    </div>
    <div id="space"></div>
    <%@include file="WEB-INF/includes/projectTitle.jspf" %>
    </div>            
    </body>
</html>
