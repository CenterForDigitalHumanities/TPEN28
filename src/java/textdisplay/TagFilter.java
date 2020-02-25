/*
 * @author Jon Deering
 Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License.

 You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 and limitations under the License.
 */
package textdisplay;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import static com.lowagie.text.Font.BOLD;
import static com.lowagie.text.Font.ITALIC;
import static com.lowagie.text.Font.NORMAL;
import static com.lowagie.text.Font.UNDERLINE;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import static com.lowagie.text.pdf.BaseFont.EMBEDDED;
import static com.lowagie.text.pdf.BaseFont.IDENTITY_H;
import static com.lowagie.text.pdf.BaseFont.createFont;
import com.lowagie.text.pdf.PdfWriter;
import static com.lowagie.text.pdf.PdfWriter.getInstance;
import static com.tutego.jrtf.Rtf.rtf;
import com.tutego.jrtf.RtfPara;
import static com.tutego.jrtf.RtfPara.p;
import com.tutego.jrtf.RtfText;
import static com.tutego.jrtf.RtfText.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import static java.lang.System.out;
import java.util.Hashtable;
import java.util.Stack;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static textdisplay.TagFilter.styles.none;
import static textdisplay.TagFilter.styles.paragraph;
import static textdisplay.TagFilter.styles.remove;
import static textdisplay.TagFilter.styles.underlined;

/**
 * A class to provide output formatting based on xml tags in a document.
 */
public class TagFilter {

    private final String text;

    public TagFilter(String text) {
        this.text = text;
    }

    /**
     * Build an array of tag names that are present in the document.
     *
     * @return an array of tags with no brackets or parameters
     */
    public String[] getTags() {
        //this method does not use existing xml libraries because there is no presumption that the document is a well formed xml document
        //it can be a single page from a document, or just have some tags wrapping certain items that the user intends to style.
        String[] parts = text.split("<");
        Hashtable h = new Hashtable();
        Stack<String> inOrder = new Stack();
        for (String part : parts) {
            String[] tmp = part.split(">");
            //if there was a > tag, then this is an actual tag, not a random angle bracket, so add it to the list
            if (tmp.length > 1 || part.endsWith(">")) {
                String thisTag = tmp[0];
                if (thisTag.endsWith("/")) {
                    thisTag = thisTag.split(" ")[0];

                }
                thisTag = thisTag.split(" ")[0];
                if (h.contains(thisTag)) {
                } else {
                    //find the matching end tag before adding this
                    Boolean b = false;

                    inOrder.add(thisTag);
                    h.put(thisTag, thisTag);
                }
            }
        }
        String[] toret = new String[0];
        while (!inOrder.isEmpty()) {
            //only add the tag to the tag list if it is self closing or has a closing tag
            Boolean addThis = false;
            String theTag = inOrder.pop();
            if (theTag.endsWith("/")) {
                addThis = true;
            }
            if (h.contains("/" + theTag)) {
                addThis = true;
            }

            if (addThis) {
                String[] tmp = new String[toret.length + 1];
                System.arraycopy(toret, 0, tmp, 0, toret.length);
                tmp[tmp.length - 1] = theTag;
                toret = tmp;
            }
        }
        return toret;
    }

    /**
     * Remove tags in the along with any text or other tags inside these tags
     * **This may be deprecated.It does not work, I use stripTags() instead.
     * @param tagsToExclude
     * @return 
     */
    public String removeTagsAndContents(String[] tagsToExclude) {
        //this method does not use existing xml libraries because there is no presumption that the document is a well formed xml document
        //it can be a single page from a document, or just have some tags wrapping certain items that the user intends to style.
        if (tagsToExclude.length == 0) {
            return text;
        }
        String content = text;

        for (String tagsToExclude1 : tagsToExclude) {
            //LOG.log(Level.INFO, "Removing {0}", tagsToExclude[i]);
            if (tagsToExclude1 != null && tagsToExclude1.compareTo("") != 0) {
                String[] parts = text.split("<" + tagsToExclude1 + " .*?>");
                content = "";
                content += parts[0];
                for (int j = 1; j < parts.length; j++) {
                    String[] tmp = parts[j].split("</" + tagsToExclude1 + ">");
                    if (tmp.length == 2) {
                        content += tmp[1];
                    }
                }
            }
        }
        return content;

    }

    /**
     * Remove these tags along with any text or other tags inside these tags
     * @param tagsToExclude
     * @return 
     */
    public String stripTags(String[] tagsToExclude) {
        //this method does not use existing xml libraries because there is no presumption that the document is a well formed xml document
        //it can be a single page from a document, or just have some tags wrapping certain items that the user intends to style.
        if (tagsToExclude.length == 0) {
            return text;
        }
        String content = text;
        for (String tagsToExclude1 : tagsToExclude) {
            LOG.log(INFO, "Stripping out {0}", tagsToExclude1);
            if (tagsToExclude1 != null && tagsToExclude1.compareTo("") != 0) {
                String[] parts = content.split("<" + tagsToExclude1 + ">|<" + tagsToExclude1 + " +.*?>");
                out.print("tag to strip is " + tagsToExclude1 + "\n");
                for (int j = 0; j < parts.length; j++) {
                    out.print("part " + j + " is: " + parts[j] + "\n");
                }
                content = "";
                content += parts[0];
                for (int j = 1; j < parts.length; j++) {
                    String[] tmp = parts[j].split("</" + tagsToExclude1 + ">");
                    if (tmp.length == 2) {
                        content += tmp[0] + tmp[1];
                    } else {
                        if (tmp.length < 2) {
                            out.print("Missed closing for " + tagsToExclude1 + "\n" + tmp[0] + "\n");
                            content += tmp[0];
                        }
                    }
                }
            }
        }
        return content;

    }

    public enum styles {

        italic, bold, underlined, superscript, none, remove, paragraph
    };

    public enum noteStyles {

        footnote, endnote, sidebyside, inline, remove
    };

    public void replaceTagsWithPDFEncoding(String[] tags, styles[] tagStyles, OutputStream os) throws DocumentException {
        //   FileWriter w = null;

        try {
            BaseFont bf = createFont("/usr/Junicode.ttf", IDENTITY_H, EMBEDDED);

            Document doc = new Document();
            PdfWriter p = getInstance(doc, os);
            doc.open();
            Paragraph para = new Paragraph();
            para.setFont(new Font(bf, 12, NORMAL));
            //doc.add(para);
            Font italic = new Font(bf, 12, ITALIC);
            Font bold = new Font(bf, 12, BOLD);
            Font underlined = new Font(bf, 12, UNDERLINE);

            StringBuilder chunkBuffer = new StringBuilder(""); //holds the next bit of content that will be added to the pdf as a chunk
            styles chunkStyle = null; //the style to be applied to chunkBuffer when it gets added to the document
            String chunkTag = "";
            Stack<String> wrappingTags = new Stack();
            Stack<styles> wrappingStyles = new Stack();
            String content = text;
            Boolean inTag = false; //is this inside a tag, meaning between the < and >
            String tagTextBuffer = ""; //the text of the tag, including name and any parameters
            Boolean beingTagged = false; //Is the parser currently reading character data that is surrounded by a tag that demands styling
            for (int charCounter = 0; charCounter < this.text.length(); charCounter++) {

                if (text.charAt(charCounter) == '>') {
                    inTag = false;
                    //if this was a self closing tag, dont do anything
                    if (tagTextBuffer.contains("/>")) {
                        tagTextBuffer = "";
                    } else {
                        //this is a closing tag, save the chunk and pop the tag and style off of the stack
                        if (tagTextBuffer.startsWith("/")) {
                            if (chunkStyle != null) {
                                LOG.log(INFO, " Closing tag {0} with style {1}", new Object[]{tagTextBuffer, chunkStyle.name()});
                            } else {
                                LOG.log(INFO, " Closing tag {0} with style null", tagTextBuffer);
                            }
                            if (chunkStyle == paragraph) {
                                chunkBuffer = new StringBuilder("\n" + chunkBuffer);
                            }
                            Chunk c = new Chunk(chunkBuffer.toString());
                            styleChunk(c, chunkStyle);

                            if (chunkStyle != remove) {
                                para.add(c);
                            }
                            chunkBuffer = new StringBuilder("");
                            chunkStyle = null;
                            chunkTag = "";
                            if (!wrappingStyles.empty()) {
                                chunkStyle = wrappingStyles.pop();
                                chunkTag = wrappingTags.pop();
                            }
                            tagTextBuffer = "";

                        } else {
                            //this is the closing bracket of an opening tag
                            String tagName = tagTextBuffer.split(" ")[0];
                            LOG.log(INFO, "Closing <{0}>", tagName);
                            for (int i = 0; i < tags.length; i++) {

                                if (tags[i].compareTo(tagName) == 0) {
                                    // This is a tag that is supposed to be styled in the pdf.
                                    if (chunkStyle != null) {
                                        //this tag is nested in a tag that was already applying styling. Add this chunk to the pdf and put the tag/style
                                        //for the previous tag on the stack, so when this new tag ends, the previous styling will resume.
                                        if (chunkStyle == paragraph) {
                                            chunkBuffer = new StringBuilder("\n" + chunkBuffer);
                                        }
                                        Chunk c = new Chunk(chunkBuffer.toString());
                                        styleChunk(c, chunkStyle);
                                        if (chunkStyle != remove) {
                                            para.add(c);
                                        }
                                        wrappingStyles.add(chunkStyle);
                                        wrappingTags.add(chunkTag);
                                        chunkTag = tagName;
                                        chunkStyle = tagStyles[i];
                                        chunkBuffer = new StringBuilder("");
                                    } else {
                                        Chunk c = new Chunk(chunkBuffer.toString());
                                        para.add(c);
                                        chunkTag = tagName;
                                        chunkStyle = tagStyles[i];
                                        chunkBuffer = new StringBuilder("");
                                    }
                                }
                            }
                            tagTextBuffer = "";
                        }
                    }
                }
                if (inTag) {
                    tagTextBuffer += text.charAt(charCounter);
                }
                if (text.charAt(charCounter) == '<') {
                    if (inTag) {
                        //if we hit another < before hitting a > this was not a tag, so add the tagTextBuffer to the chunk. It was simply conent.
                        chunkBuffer.append(tagTextBuffer);
                        tagTextBuffer = "";
                    }
                    inTag = true;
                }
                if (!inTag && text.charAt(charCounter) != '>') {
                    chunkBuffer.append(text.charAt(charCounter));
                }
            }
            Chunk c = new Chunk(chunkBuffer.toString());
            para.add(c);
            doc.newPage();
            doc.add(para);
            doc.newPage();
            doc.close();
        } catch (IOException ex) {
            getLogger(TagFilter.class.getName()).log(SEVERE, null, ex);
        } finally {
        }

    }

    /**
     * Apply a style (font) to a chunk of pdf text
     */
    private void styleChunk(Chunk c, styles s) {
        try {

            BaseFont bf = createFont("/usr/Junicode.ttf", IDENTITY_H, EMBEDDED);
            BaseFont ita = createFont("/usr/Junicode-Italic.ttf", IDENTITY_H, EMBEDDED);
            BaseFont bol = createFont("/usr/Junicode-Italic.ttf", IDENTITY_H, EMBEDDED);
            if (bf.charExists(540)) {
                out.print("font cant do 540\n");
            }
            Font italic = new Font(ita, 12, ITALIC);
            Font bold = new Font(bol, 12, BOLD);
            Font underlined = new Font(bf, 12, UNDERLINE);
            Font superscript = new Font(bf, 9, NORMAL);

            if (s == styles.bold) {
                c.setFont(bold);
            }
            if (s == styles.italic) {
                c.setFont(italic);
            }
            if (s == styles.underlined) {
                c.setFont(underlined);
            }
            if (s == styles.superscript) {
                c.setTextRise(7.0f);
                c.setFont(superscript);

            }

            //wipe out that content
        } catch (DocumentException | IOException ex) {
            getLogger(TagFilter.class.getName()).log(SEVERE, null, ex);
        }
    }

    /**
     * Style the tags that have been passed in as requested and write the rtf
     * document to the writer w
     * @param tags
     * @param tagStyles
     * @param w
     */
    public void replaceTagsWithRTFEncoding(String[] tags, styles[] tagStyles, Writer w) {

        for (int i = 0; i < tagStyles.length; i++) {
            if (tagStyles[i] == null) {
                tagStyles[i] = none;
            }
        }
        Stack<RtfText> paragraphs = new Stack();
        String content = text;

        StringBuilder chunkBuffer = new StringBuilder(""); //holds the next bit of content that will be added to the pdf as a chunk
        styles chunkStyle = null; //the style to be applied to chunkBuffer when it gets added to the document
        String chunkTag = "";
        Stack<String> wrappingTags = new Stack();
        Stack<styles> wrappingStyles = new Stack();
        Boolean inTag = false; //is this inside a tag, meaning between the < and >
        String tagTextBuffer = ""; //the text of the tag, including name and any parameters
        Boolean beingTagged = false; //Is the parser currently reading character data that is surrounded by a tag that demands styling
        for (int charCounter = 0; charCounter < this.text.length(); charCounter++) {

            if (text.charAt(charCounter) == '>') {
                inTag = false;
                //if this was a self closing tag, dont do anything
                if (tagTextBuffer.contains("/>") || tagTextBuffer.contains("-->")) {
                    out.print("Skipping auto closing or comment tag " + tagTextBuffer + "\n");
                    tagTextBuffer = "";
                } else {
                    //this is a closing tag, save the chunk and pop the tag and style off of the stack
                    if (tagTextBuffer.startsWith("/")) {
                        if (chunkStyle != null) {
                            out.print(" closing tag " + tagTextBuffer.replace("/", "") + " with style " + chunkStyle.name() + "\n");
                        } else {
                            out.print(" closing tag " + tagTextBuffer + " with style null and content " + chunkBuffer + "\n");
                        }
                        if (chunkStyle != remove) {
                            paragraphs.add(applyRTFStyle(chunkStyle, chunkBuffer.toString()));
                        }
                        chunkBuffer = new StringBuilder("");

                        chunkTag = "";
                        if (!wrappingStyles.empty()) {
                            chunkStyle = wrappingStyles.pop();
                            chunkTag = wrappingTags.pop();
                        } else {
                            out.print("Forcing style italic because style is unknown\n");
                            chunkStyle = none;
                        }
                        tagTextBuffer = "";

                    } else {
                        //this is the closing bracket of an opening tag
                        String tagName = tagTextBuffer.split(" ")[0];
                        out.print("tag is " + tagName + "\n");
                        for (int i = 0; i < tags.length; i++) {

                            if (tags[i].compareTo(tagName) == 0) {
                                // this is a tag that is suposed to be styled in the pdf
                                if (chunkStyle != null) {
                                    //this tag is nested in a tag that was already applying styling. Add this chunk to the pdf and put the tag/style
                                    //for the previous tag on the stack, so when this new tag ends, the previous styling will resume.
                                    //if(chunkStyle != styles.remove)
                                    paragraphs.add(applyRTFStyle(chunkStyle, chunkBuffer.toString()));
                                    wrappingStyles.add(chunkStyle);
                                    wrappingTags.add(chunkTag);
                                    out.print("Stack add " + chunkTag + " with style " + chunkStyle + "\n");
                                    chunkTag = tagName;
                                    chunkStyle = tagStyles[i];
                                    chunkBuffer = new StringBuilder("");
                                } else {
                                    paragraphs.add(text(chunkBuffer));
                                    chunkTag = tagName;
                                    chunkStyle = tagStyles[i];
                                    chunkBuffer = new StringBuilder("");
                                }
                            }
                        }
                        tagTextBuffer = "";
                    }
                }
            }
            if (inTag) {
                tagTextBuffer += text.charAt(charCounter);
            }
            if (text.charAt(charCounter) == '<') {
                if (inTag) {
                    //if we hit another < before hitting a > this was not a tag, so add the tagTextBuffer to the chunk. It was simply conent.
                    chunkBuffer.append(tagTextBuffer);
                    tagTextBuffer = "";
                }
                inTag = true;
            }
            if (!inTag && text.charAt(charCounter) != '>') {
                chunkBuffer.append(text.charAt(charCounter));

            }
        }
        if (chunkBuffer.length() > 0) {
            paragraphs.add(applyRTFStyle(none, chunkBuffer.toString()));
        }
        Stack<RtfPara> textParas = new Stack();
        RtfText[] textarray = new RtfText[paragraphs.size()];
        for (int i = 0; i < paragraphs.size(); i++) {
            textarray[i] = paragraphs.get(i);
        }
        RtfPara p = p(textarray, true);

        textParas.add(p);
        rtf().section(textParas).out(w);
    }

    public RtfText applyRTFStyle(styles tagStyle, String text) {
        RtfText styledPortion = text();
        Boolean styled = false;
        if (tagStyle == null) {
            return styledPortion;
        }
        if (tagStyle == styles.italic) {
            styledPortion = italic(text);
            styled = true;
        }
        if (tagStyle == styles.bold) {
            styledPortion = bold(text);
            styled = true;
        }
        if (tagStyle == underlined) {
            styledPortion = underline(text);

            styled = true;
        }
        if (tagStyle == remove) {
            styledPortion = text("");
            styled = true;
        }
        if (tagStyle == none) {
            styledPortion = text(text);
            styled = true;
        }
        if (tagStyle == paragraph) {
            styledPortion = text("\n" + text);
            styled = true;
        }
        if (tagStyle == styles.superscript) {
            styledPortion = superscript(text);
            styled = true;
        }

        if (!styled) {

            out.print("Unknown style, using default non styled text!\n");
            styledPortion = text(text);
        }
        return styledPortion;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, DocumentException {
        BufferedReader f = new BufferedReader(new FileReader(new File("/usr/web/test.xml")));
        FileWriter w = new FileWriter(new File("/usr/web/filtered2.txt"));
        String txt = "";
        while (f.ready()) {
            txt += f.readLine();

        }
        txt = txt.replaceAll(" +", " ");
        // System.out.print(txt);
        TagFilter filter = new TagFilter(txt);
        String[] tags = filter.getTags();
        for (int i = 0; i < tags.length; i++) {
            out.print(tags[i] + "\n");
            if (tags[i].compareTo("p") == 0) {
                tags[i] = "";
            }
        }

        //String tmp=filter.stripTags(tags);
        //w.append(tmp);
        //filter=new TagFilter(tmp);
        String[] o = new String[]{"p"};
        filter.replaceTagsWithPDFEncoding(new String[]{"p", "note"}, new styles[]{styles.italic, styles.bold}, new FileOutputStream(new File("/usr/test.pdf")));

    }
    private static final Logger LOG = getLogger(TagFilter.class.getName());
}
