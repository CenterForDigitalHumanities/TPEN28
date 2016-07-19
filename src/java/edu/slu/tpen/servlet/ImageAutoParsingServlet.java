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

import detectimages.imageProcessor;
import detectimages.line;
import edu.slu.tpen.entity.Image.Image;
import edu.slu.tpen.entity.Image.OtherContent;
import edu.slu.tpen.entity.Image.Resource;
import edu.slu.tpen.entity.Image.TransLine;
import imageLines.ImageHelpers;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Auto parse image from image url. Parsing function is inherited from tpen. Annotation saving function is new and using rerum.io. 
 * @author hanyan
 */
public class ImageAutoParsingServlet extends HttpServlet {
    
    private static final String ENCODING_TYPE = "utf-8";

    /**
     * Image auto parsing
     * @param imgurl
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String imgurl = (String) request.getParameter("imgurl");
//        System.out.println("url ========== " + imgurl);
        URL url = new URL(imgurl);
//        String imgurl = "https://cdm.csbsju.edu/cgi-bin/getimage.exe?CISOROOT=/ArcaArt&CISOPTR=6918";
//        URL url = new URL(imgurl);
        HttpsURLConnection httpConn = (HttpsURLConnection)url.openConnection();
        InputStream in = httpConn.getInputStream();
        BufferedImage img = ImageIO.read(in);
        int height = 1000;
//        System.out.println("img ======= " + img);
        BufferedImage scaledImg = ImageHelpers.scale(img, 1000);
        imageProcessor proc = new imageProcessor(img, height);
        List<line> ls_detectedLines = proc.detectLines(false);
        List<TransLine> ls_transLine = new ArrayList();
        if(null != ls_detectedLines && ls_detectedLines.size() > 0){
            for(line l : ls_detectedLines){
                TransLine tl = new TransLine();
                tl.setX(l.getStartHorizontal());
                tl.setY(l.getStartVertical());
                tl.setW(l.getWidth());
                tl.setH(l.getDistance());
                ls_transLine.add(tl);
                //System.out.println(l.toString());
            }
        }
        JSONObject jo_canvas = new JSONObject();
        jo_canvas.element("@id", "tpen_imageParse");
        jo_canvas.element("@type", "sc:Canvas");
        jo_canvas.element("height", 1000);
        jo_canvas.element("width", scaledImg.getWidth());
        //generate image array
        JSONArray arr_img = new JSONArray();
        ////generate image entity
        Image image = new Image();
        image.setId("");
        image.setMotivation("sc:painting");
        image.setType("oa:Annotation");
        ////generate resource json
        Resource rs = new Resource();
        rs.setId(imgurl);
        rs.setType("dctypes:Image");
        rs.setFormat("image/jpeg");
        rs.setHeight(img.getHeight());
        rs.setWidth(img.getWidth());
        ////add resource json to image and gnerate image json
        image.setJo_resource(rs.toJSON());
        JSONObject jo_image = image.toJSON();
        arr_img.add(jo_image);
        //add image array to cavans
        jo_canvas.element("images", arr_img);
        //generate otherContent array
        OtherContent oc = new OtherContent();
        oc.setCanvasObjectId("");
        oc.setType("sc:AnnotationList");
        oc.setContext("");
        JSONObject jo_otherContent = oc.toJSON();
        ////generate resources in otherContent which is trans line
        JSONArray ja_line = new JSONArray();
         if(null != ls_detectedLines && ls_detectedLines.size() > 0){
            for(line l : ls_detectedLines){
                TransLine tl = new TransLine();
                tl.setX(l.getStartHorizontal());
                tl.setY(l.getStartVertical());
                tl.setW(l.getWidth());
                tl.setH(l.getDistance());
                ja_line.add(tl.toJSON());
                //System.out.println(l.toString());
            }
        }
        jo_otherContent.element("resources", ja_line);
        jo_canvas.element("otherContent", jo_otherContent);
        response.getWriter().print(jo_canvas);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
