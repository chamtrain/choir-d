/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.stanford.registry.server.utils;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

public class HTMLParser {
  private float fontSize = 8f;
  private PDPageContentStream contentStream;
  private float startY;
  float startX, endX;
  CustomFonts useFonts;
  private Color background = null;
  private Color textColor = null;

  /**
   * Constructor for writing highlighted text
   *
   * @param contentStream
   * @param startY
   * @param startX
   * @param endX
   * @param textColor
   * @param background
   * @throws IOException
   */
  public HTMLParser(PDPageContentStream contentStream, float startY, float startX, float endX, Color textColor,
                    Color background) throws IOException {
    this(contentStream, startY, startX, endX);
    this.background = background;
    this.textColor = textColor;
  }

  /**
   * Constructor for writing unhighlighted text
   *
   * @param contentStream
   * @param startY
   * @param startX
   * @param endX
   * @throws IOException
   */
  public HTMLParser(PDPageContentStream contentStream, float startY, float startX, float endX) throws IOException {
    this.contentStream = contentStream;
    this.startY = startY;
    this.startX = startX;
    this.endX = endX;
  }

  public PDFArea writeText(String text) throws IOException {
    CallBack callback = new CallBack(contentStream, fontSize, startY, startX, endX);
    if (useFonts != null) {
      callback.setFonts(useFonts);
    }
    if (background != null) {
      callback.setBackground(background);
    }
    if (textColor != null) {
      callback.setTextColor(textColor);
    }
    Reader reader = new StringReader(text);

    new ParserDelegator().parse(reader, callback, false);
    return callback.getArea();
  }

  public void setFontSize(float fontSize) {
    this.fontSize = fontSize;
  }

  public void setFonts(PDFont normalFont, PDFont strongFont, PDFont italicFont, PDFont strongItalicFont) {
    useFonts = new CustomFonts(normalFont, strongFont, italicFont, strongItalicFont);
  }

}

class CustomFonts {
  PDFont normalFont;
  PDFont strongFont;
  PDFont italicFont;
  PDFont strongItalicFont;

  public CustomFonts(PDFont normalFont, PDFont strongFont, PDFont italicFont, PDFont strongItalicFont) {
    this.normalFont = normalFont;
    this.strongFont = strongFont;
    this.italicFont = italicFont;
    this.strongItalicFont = strongItalicFont;
  }
}

class CallBack extends HTMLEditorKit.ParserCallback {
  private static Logger logger = Logger.getLogger(CallBack.class);
  private float fontSize;
  private float startX, startY, endX, height, textX, textY, startU, linesp;
  StringBuffer nextLineToDraw = new StringBuffer();
  private boolean isUnderLined = false;
  private boolean isBold = false;
  private boolean isItalic = false;

  PDPageContentStream contentStream;

  PDFont currentFont;
  private PDFont FONT_NORMAL = PDType1Font.TIMES_ROMAN;
  private PDFont FONT_STRONG = PDType1Font.TIMES_BOLD;
  private PDFont FONT_ITALIC = PDType1Font.TIMES_ITALIC;
  private PDFont FONT_STRONG_ITALIC = PDType1Font.TIMES_BOLD_ITALIC;

  PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
  public StringBuffer sb = new StringBuffer();
  float writtenOnLine = 0;
  Color textColor = Color.BLACK;
  Color background = null;

  private PDFArea area = new PDFArea();
  /*
   * This Implementation of the ParserCallback supports the following subset of html tags <BR> | <P> = Newline <B> | <STRONG> = Bold Font <U> = Underline
   */

  public CallBack(PDPageContentStream contentStream, float fontSize, float startY, float startX, float endX)
      throws IOException {
    this.contentStream = contentStream;
    this.fontSize = fontSize;
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    // logger.setLevel(Level.TRACE);  // for more details setLevel(Level.TRACE)


    textY = startY;
    textX = startX;
    startU = -1;

    initStuff();
  }

  public void setFonts(CustomFonts fonts) throws IOException {
    FONT_NORMAL = fonts.normalFont;
    FONT_STRONG = fonts.strongFont;
    FONT_ITALIC = fonts.italicFont;
    FONT_STRONG_ITALIC = fonts.strongItalicFont;
    initStuff();
  }

  public float getEndY() {
    if (logger.isTraceEnabled()) {
      logger.trace("returning: " + Math.round(textY - (height + (linesp * 2))) + " (started at " + Math.round(startY));
    }
    return textY - (height + (linesp * 2));
  }

  public void setTextColor(Color tc) throws IOException {
    textColor = tc;
    initStuff();
  }

  public void setBackground(Color bg) throws IOException {
    background = bg;
    initStuff();
  }

  @Override
  public void flush() throws BadLocationException {
    try {
      writePDFText(nextLineToDraw.toString());
      newLine();
    } catch (IOException e) {
      logger.error("error in method flush() writing " + nextLineToDraw.toString() + " to pdf", e);
    }
  }

  @Override
  public void handleComment(char[] data, int pos) {
  }

  @Override
  public void handleStartTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
    if ((t == HTML.Tag.DIV || t == HTML.Tag.BR || t == HTML.Tag.P)) {
      if (logger.isTraceEnabled()) {
        logger.trace("handleStartTab starting newLine");
      }
      newLine();
    }
    if (t == HTML.Tag.B || t == HTML.Tag.STRONG) { // Turn on Bold
      isBold = true;
    } else if (t == HTML.Tag.U) { // Turn on underlining
      startU = textX;
      isUnderLined = true;
    } else if (t == HTML.Tag.I) { // Turn on italic
      isItalic = true;
    }
  }

  @Override
  public void handleEndTag(HTML.Tag t, int pos) {
    if (t == HTML.Tag.B || t == HTML.Tag.STRONG) {
      isBold = false;
    } else if (t == HTML.Tag.U) {
      /*
       * Turn off underlining
       */
      isUnderLined = false;
    } else if (t == HTML.Tag.I) {
      isItalic = false;
    }
  }

  @Override
  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    if ((t).toString().equals("span")) {
      return;
    }
  }

  @Override
  public void handleError(String errorMsg, int pos) {
    logger.error("HTMLParser.handleError:" + errorMsg);
  }

  @Override
  public void handleEndOfLineString(String eol) {
    if (logger.isTraceEnabled()) {
      logger.trace("handleEndOfLine");
    }
    textX = startX;
  }

  @Override
  public void handleText(char[] data, int pos) {
    StringBuffer strData = new StringBuffer();
    for (char ch : data) {
      strData = strData.append(ch);
    }

    String[] words = strData.toString().trim().split(" ");
    int lineIndex = 0;
    try {
      while (lineIndex < words.length) {
        // Set the font so the length calculation is correct
        PDFont useFont = currentFont;
        if (isBold && isItalic) {
          useFont = FONT_STRONG_ITALIC;
        } else if (isBold) {
          useFont = FONT_STRONG;
        } else if (isItalic) {
          useFont = FONT_ITALIC;
        }
        String lineWithNextWord = nextLineToDraw.toString() + words[lineIndex];
        float lineLength = (useFont.getStringWidth(lineWithNextWord) / 1000) * (fontSize);

        if (lineTooLong(lineLength, textX, endX)) {
          //if (logger.isTraceEnabled()) {
          logger.debug("writing '" + nextLineToDraw.toString() + "' with length = " + lineLength + " at (" + textX + ","
              + textY + ") endX=" + endX + " plus newLine");
          //}
          writePDFText(nextLineToDraw.toString());
          newLine();
        }
        nextLineToDraw.append(words[lineIndex]);
        nextLineToDraw.append(" ");
        lineIndex++;
      }
      if (nextLineToDraw.toString().trim().length() > 0) {
        if (logger.isTraceEnabled()) {
          logger.trace("writing '" + nextLineToDraw.toString() + " at (" + textX + "," + textY + ") endX=" + endX);
        }
        writePDFText(nextLineToDraw.toString());
      }
    } catch (IOException e) {
      logger.error("Error in handleText writing " + nextLineToDraw.toString() + " to pdf ", e);
    }
  }

  public void writePDFText(String text) throws IOException {
    if (logger.isTraceEnabled()) {
      logger.trace("writePDFText: " + text);
    }

    // Write the highlight first, include the space between lines in the box
    if (background != null) {
      // move down a touch so we can add linespace around it
      PDFUtils.writeHighlightBox(contentStream, textX, textY - (linesp * 2), endX - textX, height + (linesp * 2),
          background);
      if (endX > area.getXto()) {
        area.setXto(endX);
      }
    }

    contentStream.setNonStrokingColor(textColor);
    contentStream.setStrokingColor(textColor);

    try {
      if (isBold && isItalic) {
        contentStream.setFont(FONT_STRONG_ITALIC, fontSize);
        currentFont = FONT_STRONG_ITALIC;
      } else if (isBold) {
        contentStream.setFont(FONT_STRONG, fontSize);
        currentFont = FONT_STRONG;
      } else if (isItalic) {
        contentStream.setFont(FONT_ITALIC, fontSize);
        currentFont = FONT_ITALIC;
      } else {
        contentStream.setFont(FONT_NORMAL, (fontSize));
        currentFont = FONT_NORMAL;
      }
    } catch (IOException ie) {
      logger.error("error in handleStartTag setting font ", ie);
    }

    contentStream.beginText();
    contentStream.setStrokingColor(textColor);
    contentStream.setNonStrokingColor(textColor);
    contentStream.moveTextPositionByAmount(textX, textY + linesp);
    contentStream.drawString(nextLineToDraw.toString());
    contentStream.endText();

    /*
     * Move the drawing position along the X-axis the width of the text we just wrote
     */
    textX += (currentFont.getStringWidth(nextLineToDraw.toString()) / 1000) * (fontSize);

    if (isUnderLined) {
      if (logger.isTraceEnabled()) {
        logger.trace("drawing line");
      }
      float y = textY - 1; // move it down just past the size
      contentStream.setLineWidth(0.6f);
      contentStream.drawLine(startU, y, textX - 5, y);
    }
    nextLineToDraw = new StringBuffer();
    if (textX > area.getXto()) {
      area.setXto(textX);
    }
  }

  /*
   * For new lines move the X-axis back to the beginning and drop down the y-axis 1 line height
   */
  private void newLine() {
    if (textX == startX) {
      return;
    }
    textX = startX;
    textY -= height;
    textY -= (linesp * 2);
    if (logger.isTraceEnabled()) {
      logger.trace("newLine: moveto: " + Math.round(textY - (height + (linesp * 2))) + " (started at "
          + Math.round(startY));

    }
  }

  boolean lineTooLong(float lineLength, float startAt, float endAt) {

    Float endOfLine = lineLength + startAt;
    Float endPoint = endAt;
    if (logger.isTraceEnabled()) {
      if ((endOfLine > endPoint)) {
        logger
            .trace("lineTooLong! " + Math.round(startAt) + " + " + Math.round(lineLength) + " > " + Math.round(endAt));
      }
    }
    return (endOfLine > endPoint);

  }

  private void initStuff() throws IOException {
    currentFont = FONT_NORMAL;
    contentStream.setFont(currentFont, (fontSize));
    height = currentFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000;
    height = height * (fontSize);
    linesp = height * .10f; // 10% for line spacing
    area.setXfrom(textX);
    if (background != null) {
      area.setYfrom(textY + height);
    } else {
      area.setYfrom(textY + (linesp * 2));
    }
  }

  public PDFArea getArea() {
    area.setYto(textY);
    if (background != null) {
      area.setYto(textY - (linesp * 2));
    }
    area.setNextY(getEndY());
    return area;
  }
}
