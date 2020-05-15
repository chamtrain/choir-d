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

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.survey.Child;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.StandardEntityCollection;
import org.w3c.dom.Element;

import com.github.susom.database.Database;

public class PDFUtils {
  private static final Logger logger = Logger.getLogger(PDFUtils.class);

  public static float pageWidth(PDPage page) {
    return page.getMediaBox().getWidth();
  }

  public static float pageHeight(PDPage page) {
    return page.getMediaBox().getHeight();
  }

  public static void writeHighlightBox(PDPageContentStream contentStream, float posX, float posY, float width,
      float height, Color color) throws IOException {

    contentStream.setNonStrokingColor(color);
    contentStream.fillRect(posX, posY, width, height);
  }

  public static void drawPolygon(PDPageContentStream contentStream, float[] x, float[] y, Color color)
      throws IOException {
    contentStream.setNonStrokingColor(color);
    contentStream.fillPolygon(x, y);

  }

  public static void drawBoxOutline(PDPageContentStream contentStream, float xBeg, float yBeg, float width,
      float height, Color color) throws IOException {

    contentStream.saveGraphicsState();
    contentStream.setNonStrokingColor(color);
    contentStream.setStrokingColor(color);
    float xEnd = xBeg + width;
    float yEnd = yBeg + height;

    // top
    contentStream.drawLine(xBeg, yBeg, xEnd, yBeg);
    // sides
    contentStream.drawLine(xBeg, yBeg, xBeg, yEnd);
    contentStream.drawLine(xEnd, yBeg, xEnd, yEnd);
    // bottom
    contentStream.drawLine(xBeg, yEnd, xEnd, yEnd);
    contentStream.restoreGraphicsState();
  }

  public static void drawLine(PDPageContentStream contentStream, float xBeg, float yBeg, float width, float lineSize,
      Color color) throws IOException {
    contentStream.setNonStrokingColor(color);
    contentStream.setLineWidth(lineSize);
    contentStream.drawLine(xBeg, yBeg, xBeg + width, yBeg);
  }

  public static void writePDFText(PDPageContentStream contentStream, String string, float x, float y)
      throws IOException {
    contentStream.beginText();
    contentStream.moveTextPositionByAmount(x, y);
    contentStream.drawString(string);
    contentStream.endText();
  }

  public static PDFArea writePDFHtml(PDPageContentStream contentStream, String string, float xStart, float yStart,
      float xEnd, PDFont[] fonts, float fontSize) throws IOException {
    HTMLParser writer = new HTMLParser(contentStream, yStart, xStart, xEnd);
    if (fonts != null && fonts.length == 4) {
      writer.setFonts(fonts[0], fonts[1], fonts[2], fonts[3]);
    }
    writer.setFontSize(fontSize);
    return writer.writeText("<body>" + string + "</body>");
  }

  public static PDFArea writePDFHtml(PDPageContentStream contentStream, String string, float xStart, float yStart,
      float xEnd, PDFont[] fonts, float fontSize, Color textColor, Color background) throws IOException {
    HTMLParser writer = new HTMLParser(contentStream, yStart, xStart, xEnd, textColor, background);
    if (fonts != null && fonts.length == 4) {
      writer.setFonts(fonts[0], fonts[1], fonts[2], fonts[3]);
    }
    writer.setFontSize(fontSize);
    return writer.writeText("<body>" + string + "</body>");
  }

  public static String removeWinAnsiChars(String test) {
   StringBuilder b = new StringBuilder();
    for (int i = 0; i < test.length(); i++) {
      if (WinAnsiEncoding.INSTANCE.contains(test.charAt(i))) {
        b.append(test.charAt(i));
      }
    }
    return b.toString();
  }

  public static PDFArea writePDFBackground(PDPageContentStream contentStream, String string, float xStart,
      float yStart, float xEnd, PDFont[] fonts, float fontSize, Color background) throws IOException {
    HTMLParser writer = new HTMLParser(contentStream, yStart, xStart, xEnd, background, background);
    if (fonts != null && fonts.length == 4) {
      writer.setFonts(fonts[0], fonts[1], fonts[2], fonts[3]);
    }
    writer.setFontSize(fontSize);
    return writer.writeText("<body>" + string + "</body>");
  }

  public static void writeImage(PDDocument doc, PDPageContentStream contentStream, String imagePath, float xStart,
      float yStart) throws IOException {
    /** Load the image **/
    BufferedImage img = getImage(imagePath);
    if (img != null) {
      BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
      newImage.createGraphics().drawImage(img, 0, 0, Color.BLACK, null);
      PDImageXObject ximage = JPEGFactory.createFromImage(doc, img);
      contentStream.drawImage(ximage, 20, 20);
    }

  }

  public static BufferedImage getImage(String path) {
    BufferedImage img = null;
    InputStream stream = null;
    try {
      stream = PDFUtils.class.getClassLoader().getResourceAsStream(path);
      img = ImageIO.read(stream);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          logger.error("Problem while trying to close PDF stream", e);
        }
      }
    }
    return img;
  }

  public static PDImage getPDImage(PDDocument doc, String path) throws IOException {
    /** Load the image **/
    BufferedImage img = getImage(path);

    if (img != null) {
      BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
      newImage.createGraphics().drawImage(img, 0, 0, Color.BLACK, null);
      return JPEGFactory.createFromImage(doc, img);
    }
    return null;
  }

  public static PDPageContentStream startNewPage(PDPage page, PDPageContentStream contentStream, PDDocument pdf,
      PDFont font, int fontSize) throws IOException {

    pdf.addPage(page);
    if (contentStream != null) {
      contentStream.close();
    }
    logger.debug("starting page with font " + font.getName());
    contentStream = new PDPageContentStream(pdf, page, true, false);
    contentStream.setFont(font, fontSize);

    return contentStream;
  }

  public static byte[] getImage(JFreeChart chart, int width, int height) throws IOException {
    logger.debug("Creating buffered image for chart");
    ChartRenderingInfo rendInfo = new ChartRenderingInfo(new StandardEntityCollection());
    BufferedImage bi = chart.createBufferedImage(width, height, BufferedImage.TYPE_INT_RGB, rendInfo);
    logger.debug("Writing buffered image to string");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(bos);
    ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
    ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
    imageWriter.setOutput(ios);

    IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bi), null);
    //JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(bi);
    //jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
    //jpegEncodeParam.setXDensity(Constants.DPI_IMAGE_RESOLUTION);
    //jpegEncodeParam.setYDensity(Constants.DPI_IMAGE_RESOLUTION);
    //jpegEncodeParam.setQuality(1.00f, true);
    Element tree = (Element) imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");
    Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
    jfif.setAttribute("resUnits", "1"); // density is dots per inch
    jfif.setAttribute("Xdensity", Integer.toString(Constants.DPI_IMAGE_RESOLUTION));
    jfif.setAttribute("Ydensity", Integer.toString(Constants.DPI_IMAGE_RESOLUTION));
    // new Compression
    JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
    jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
    jpegParams.setCompressionQuality(1.00f);

    //jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);
    //jpegEncoder.encode(bi, jpegEncodeParam);
    imageWriter.write(imageMetaData, new IIOImage(bi, null, null), null);
    bi.flush();
    ios.close();
    imageWriter.dispose();
    return bos.toByteArray();
  }

  public static BufferedImage resizeImage(BufferedImage image, float factor) {
    int newWidth = Math.round(image.getWidth() * factor);
    int newHeight = Math.round(image.getHeight() * factor);
    Image resizedImage = image.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_DEFAULT);
    BufferedImage bimage = new BufferedImage(resizedImage.getWidth(null), resizedImage.getHeight(null),
        BufferedImage.TYPE_INT_ARGB);
    // Draw the image on to the buffered image
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(resizedImage, 0, 0, null);
    bGr.dispose();
    return bimage;
  }

  public static float getTextHeight(PDFont font, float d) {
    float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * d;
    return textHeight;
  }

  public static float getTextWidth(PDFont font, int fontSize, String text) throws IOException {
    return (font.getStringWidth(PDFUtils.removeWinAnsiChars(text)) / 1000) * (fontSize);
  }

  public static HashMap<String, Study> getStudyCodeHash(Database database, ArrayList<PatientStudyExtendedData> studies) {
    HashMap<String, Study> studyNames = new HashMap<>();
    SurveySystDao ssDao = new SurveySystDao(database);
    ArrayList<Study> allStudies = ssDao.getStudies();
    if (studies != null) {
      for (PatientStudyExtendedData study : studies) {
        if (study != null && study.getStudyDescription() != null
            && !studyNames.containsKey(study.getStudyDescription())) {
          for (Study allStudy : allStudies) {
            if (allStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
              studyNames.put(study.getStudyDescription(), allStudy);
            }
          }
        }
      }
    }
    return studyNames;
  }

  public static HashMap<String, Study> getStudyCodeHash(Database database) {
    HashMap<String, Study> studyNames = new HashMap<>();
    SurveySystDao ssDao = new SurveySystDao(database);
    ArrayList<Study> allStudies = ssDao.getStudies();
    if (allStudies != null) {
      for (Study allStudy : allStudies) {
        if (allStudy != null && allStudy.getStudyDescription() != null
            && !studyNames.containsKey(allStudy.getStudyDescription())) {
          studyNames.put(allStudy.getStudyDescription(), allStudy);
        }
      }
    }
    return studyNames;
  }

  public static HashMap<String, SurveySystem> getSurveySystemHash(Database database) {
    /* make a surveySystemId:surveySystem map of the survey systems */
    ArrayList<SurveySystem> surveySystemArr = new SurveySystDao(database).getSurveySystems();
    HashMap<String, SurveySystem> surveySystems = new HashMap<>();
    for (int s = 0; s < surveySystems.size(); s++) {
      surveySystems.put(surveySystemArr.get(s).getSurveySystemId().toString(), surveySystemArr.get(s));
    }
    return surveySystems;
  }

  public static HashMap<String, BufferedImage> getPaintedImages(SurveyQuestionIntf question, PDDocument pdf,
      Color strokeColor, Color fillColor) throws IOException {
    HashMap<String, BufferedImage> bufImages = new HashMap<>();
    ArrayList<SurveyAnswerIntf> answers = question.getAnswers();
    // First load the images from disk
    for (SurveyAnswerIntf ans : answers) {
      if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_IMAGE) {
        String imageName = ans.getAttribute("usemap");
        String path = ans.getAttribute("src");
        logger.trace("Getting image for usemap:" + imageName + " at " + path);
        BufferedImage bufImg = PDFUtils.getImage(path);
        if (imageName != null && bufImg != null) {
          bufImages.put(imageName, bufImg);
        }
      }
    }

    if (bufImages.size() < 1) {
      return bufImages;
    }

    // Then get the coordinates for the selected answers
    String ids[] = new String[0];
    if (question.getAttribute("ItemResponse") != null) {
      ids = question.getAttribute("ItemResponse").split(",");
    }
    if (ids.length > 0) { // get areas
      for (SurveyAnswerIntf answer : answers) {
        RegistryAnswer ans = (RegistryAnswer) answer;
        if (ans.getType() == edu.stanford.registry.shared.survey.Constants.TYPE_MAP) {
          ArrayList<Child> areas = ans.getChildren();
          for (Child child : areas) { // Areas
            if ("area".equals(child.getName())) {
              boolean selected = false;
              for (String id : ids) {
                try {
                  if (child.hasAttribute("id") && child.getAttribute("id").trim().equals(id.trim())) {
                    selected = true;
                  }
                } catch (Exception e) {
                }
              }
              if (selected) {
                String coordsString = child.getAttribute("coords");
                if (coordsString != null) {
                  String coords[] = coordsString.split(",");
                  String imageName = ans.getAttribute("id");
                  if (imageName != null) {
                    String useMap = "#" + imageName;
                    int points = Math.round(coords.length / 2);
                    int x1Points[] = new int[points];
                    int y1Points[] = new int[points];
                    int c = 0;
                    int p = 0;
                    while (c < coords.length) {
                      x1Points[p] = Integer.parseInt(coords[c]);
                      c++;
                      y1Points[p] = Integer.parseInt(coords[c]);
                      c++;
                      p++;
                    }
                    GeneralPath polygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
                    polygon.moveTo(x1Points[0], y1Points[0]);
                    for (int index = 1; index < x1Points.length; index++) {
                      polygon.lineTo(x1Points[index], y1Points[index]);
                    }
                    polygon.closePath();
                    BufferedImage image = bufImages.get(useMap);
                    if (image != null) {
                      Graphics2D g1 = image.createGraphics();
                      g1.setPaint(fillColor);
                      g1.fill(polygon);
                      g1.draw(polygon);
                      g1.dispose();
                      GeneralPath border = new GeneralPath(GeneralPath.WIND_EVEN_ODD, x1Points.length);
                      border.moveTo(x1Points[0], y1Points[0]);
                      for (int index = 1; index < x1Points.length; index++) {
                        border.lineTo(x1Points[index], y1Points[index]);
                        border.lineTo(x1Points[index], y1Points[index] + 1);
                        border.lineTo(x1Points[index] + 1, y1Points[index] + 1);
                        border.lineTo(x1Points[index] + 1, y1Points[index]);
                        border.lineTo(x1Points[index], y1Points[index]);
                      }
                      border.closePath();
                      Graphics2D g2 = image.createGraphics();
                      g2.setPaint(strokeColor);
                      g2.setStroke(new BasicStroke(2));
                      g2.draw(border);
                      g2.dispose();
                      bufImages.put(useMap, image);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return bufImages;
  }

  public static byte[] getImage(JFreeChart chart, int scale) throws IOException {
    ChartRenderingInfo rendInfo = new ChartRenderingInfo(new StandardEntityCollection());
    BufferedImage bi = chart.createBufferedImage(ChartUtilities.PAGE_IMAGE_WIDTH * scale,
        ChartUtilities.PAGE_IMAGE_HEIGHT * scale, BufferedImage.TYPE_INT_RGB, rendInfo);
    BufferedImage bi2 = sideways(bi);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
    ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
    imageWriter.setOutput(ios);

    IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bi2), null);
    Element tree = (Element) imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");
    Element jfif = (Element)tree.getElementsByTagName("app0JFIF").item(0);
    jfif.setAttribute("resUnits", "1"); // density is dots per inch
    jfif.setAttribute("Xdensity", Integer.toString(Constants.DPI_IMAGE_RESOLUTION));
    jfif.setAttribute("Ydensity", Integer.toString(Constants.DPI_IMAGE_RESOLUTION));
    JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
    jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
    jpegParams.setCompressionQuality(1.00f);
    imageWriter.write(imageMetaData, new IIOImage(bi2, null, null), null);

    bi2.flush();
    ios.close();
    imageWriter.dispose();
    return bos.toByteArray();
  }

  /**
   * Turns the image sideways on the page
   *
   * @param bi
   * @return
   */
  public static BufferedImage sideways(BufferedImage bi) {
    int w = bi.getWidth();
    int h = bi.getHeight();
    BufferedImage biNew = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
    double theta = -Math.PI / 2;
    AffineTransform xform = new AffineTransform();
    xform.translate(0.5 * h, 0.5 * w);
    xform.rotate(theta);
    xform.translate(-0.5 * w, -0.5 * h);
    Graphics2D g = biNew.createGraphics();
    g.drawImage(bi, xform, null);
    g.dispose();
    return biNew;
  }

  public static BufferedImage circleImg(int index, Color backColor, boolean fill) {
    if (index > ChartConfigurationOptions.LINE_COLORS.length - 1) {
      index = 0;
    }
    Color lineColor = ChartConfigurationOptions.LINE_COLORS[index];
    BufferedImage bufferedImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setBackground(backColor);
    g2d.setPaint(lineColor);
    g2d.setColor(lineColor);
    g2d.drawOval(76, 1, 48, 48);
    if (fill) {
      g2d.fillOval(76, 1, 48, 48);
    }
    drawLine(g2d, lineColor, index);
    return bufferedImage;
  }

  public static BufferedImage triangleImg(int index, Color backColor, boolean fill) {
    if (index > ChartConfigurationOptions.LINE_COLORS.length - 1) {
      index = 0;
    }
    Color lineColor = ChartConfigurationOptions.LINE_COLORS[index];
    BufferedImage bufferedImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setBackground(backColor);
    g2d.setColor(lineColor);
    g2d.setPaint(lineColor);
    int nPoints = 3;
    int xPoints[] = new int[nPoints];
    int yPoints[] = new int[nPoints];
    xPoints[0] = 76;
    yPoints[0] = 49;
    xPoints[1] = 100;
    yPoints[1] = 1;
    xPoints[2] = 124;
    yPoints[2] = 49;
    g2d.drawPolygon(xPoints, yPoints, nPoints);
    if (fill) {
      g2d.fillPolygon(xPoints, yPoints, nPoints);
    }
    drawLine(g2d, lineColor, index);
    g2d.dispose();
    return bufferedImage;
  }

  public static BufferedImage squareImg(int index, Color backColor, boolean fill) {
    if (index > ChartConfigurationOptions.LINE_COLORS.length - 1) {
      index = 0;
    }
    Color lineColor = ChartConfigurationOptions.LINE_COLORS[index];
    BufferedImage bufferedImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setBackground(backColor);
    g2d.setColor(lineColor);
    g2d.setPaint(lineColor);
    g2d.drawRect(76, 1, 48, 48);
    if (fill) {
      g2d.fillRect(76, 1, 48, 48);
    }
    drawLine(g2d, lineColor, index);
    g2d.dispose();
    return bufferedImage;
  }

  private static void drawLine(Graphics2D g2d, Color lineColor, int index) {
    BasicStroke stroke = ChartConfigurationOptions.LINE_STYLES[index];
    BasicStroke newStroke;
    float dashes[] = stroke.getDashArray();
    if (dashes != null) {
      for (int d = 0; d < dashes.length; d++) {
        dashes[d] = dashes[d] * 2;
      }
      newStroke = new BasicStroke(stroke.getLineWidth() * 2f, stroke.getEndCap(), stroke.getLineJoin(),
          stroke.getMiterLimit(), dashes, stroke.getDashPhase() * 2f);
    } else {
      newStroke = new BasicStroke(stroke.getLineWidth() * 2f, stroke.getEndCap(), stroke.getLineJoin(),
          stroke.getMiterLimit());
    }
    g2d.setStroke(newStroke);
    g2d.setColor(lineColor);
    g2d.setPaint(lineColor);
    g2d.drawLine(1, 25, 199, 25);
  }

  public static BufferedImage downArrow(Color imageColor, Color backColor) {
    BasicStroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f);
    BufferedImage bufferedImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = bufferedImage.createGraphics();
    g2d.setStroke(stroke);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setBackground(backColor);
    g2d.setColor(imageColor);
    g2d.setPaint(imageColor);
    int nPoints = 3;
    int xPoints[] = new int[nPoints];
    int yPoints[] = new int[nPoints];
    xPoints[0] = 0;
    yPoints[0] = 0;
    xPoints[1] = 50;
    yPoints[1] = 50;
    xPoints[2] = 100;
    yPoints[2] = 0;
    g2d.drawPolygon(xPoints, yPoints, nPoints);
    g2d.fillPolygon(xPoints, yPoints, nPoints);
    g2d.dispose();
    return bufferedImage;
  }

  public static BufferedImage upArrow(Color imageColor, Color backColor) {
    BufferedImage image = downArrow(imageColor, backColor);
    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    tx.translate(0, -image.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    image = op.filter(image, null);
    return image;
  }
}
