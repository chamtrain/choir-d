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

package edu.stanford.registry.server.charts;

import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.shared.ConfigurationOptions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

public class ChartMaker {
  private static final Logger logger = Logger.getLogger(ChartMaker.class);
  public static final RectangleInsets CHART_INSETS = new RectangleInsets(10.0f, 4.0f, 4.0f, 20.0f);

  public static final int LABELFONTSIZE = 24;
  public static final int TITLEFONTSIZE = 28;
  public static final int NUMTICKFONTSIZE = 24;
  public static final int DTTICKFONTSIZE = 22;
  public static final int MARKERFONTSIZE = 11;
  public static final int LEGENDFONTSIZE = 7;
  public static final Color LEGENDBACKGROUNDCOLOR = new Color(240, 240, 240);

  public static String defaultFontFamily = initializeUiManagerDefaultFontFamily();
  private final Font labelFont;
  private final Font titleFont;
  private final Font numTickFont;
  private final Font dtTickFont;


  private final ChartInfo chartInfo;
  private ChartConfigurationOptions opts = null;

  public ChartMaker(ChartInfo chartInfo, ChartConfigurationOptions opts) throws IOException {
    this.chartInfo = chartInfo;
    this.opts = opts;

    Integer scaleBy = opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES);
    if (scaleBy == null || scaleBy < 1) {
      scaleBy = 100;
    }

    labelFont = new Font(defaultFontFamily, Font.PLAIN, scaleFont(scaleBy, LABELFONTSIZE));
    titleFont = new Font(defaultFontFamily, Font.PLAIN, scaleFont(scaleBy, TITLEFONTSIZE));
    numTickFont = new Font(defaultFontFamily, Font.PLAIN, scaleFont(scaleBy, NUMTICKFONTSIZE));
    dtTickFont = new Font(defaultFontFamily, Font.PLAIN, scaleFont(scaleBy, DTTICKFONTSIZE));
  }

  public JFreeChart getTestChart(String title, boolean includeLegend, boolean isPromis, XYErrorRenderer renderer) {

    if (title != null && title.contains("PROMIS")) {
      isPromis = true;
    }
    XYPlot plot = getTestPlot(isPromis, renderer);

    return getChart(title, includeLegend, plot);
  }

  public JFreeChart getChart(String title, boolean includeLegend, XYPlot plot) {

    JFreeChart chart = new JFreeChart(title, titleFont, plot, true);
    chart.removeLegend();
    if (includeLegend && chartInfo.getDataSet() != null && chartInfo.getDataSet().getSeriesCount() > 0) {
      LegendTitle legend = new LegendTitle(plot, new FlowArrangement(), new ColumnArrangement());
      legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));

      legend.setPosition(RectangleEdge.TOP);
      legend.setItemFont(titleFont);
      legend.setBackgroundPaint(LEGENDBACKGROUNDCOLOR);
      legend.setFrame(new BlockBorder(LEGENDBACKGROUNDCOLOR));
      chart.addLegend(legend);
    }
    chart.setBackgroundPaint(LEGENDBACKGROUNDCOLOR);
    plot.setShadowGenerator(null);

    return chart;
  }

  private XYPlot getTestPlot(boolean isPromis, XYErrorRenderer renderer) {
    int startRange = 0;
    int endRange = 10;
    NumberTickUnit tickUnit = new NumberTickUnit(1);
    if (isPromis) {
      startRange = 10;
      endRange = 90;
      tickUnit = new NumberTickUnit(10);
    }
    DateAxis domainAxis;

      domainAxis = new DateAxis("");

    domainAxis.setLabelFont(labelFont);
    domainAxis.setTickLabelFont(dtTickFont);
    domainAxis.setDateFormatOverride(new SimpleDateFormat("dd MMM yy"));
    NumberAxis rangeAxis;
    rangeAxis = new NumberAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setTickUnit(tickUnit);
    rangeAxis.setRange(startRange, endRange);
    rangeAxis.setLabelFont(labelFont);
    rangeAxis.setTickLabelFont(numTickFont);
    final XYPlot plot = new XYPlot(chartInfo.getDataSet(), domainAxis, rangeAxis, renderer);
    plot.setDomainGridlinePaint(Color.black);
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinePaint(Color.black);
    plot.setBackgroundPaint(ChartConfigurationOptions.getColor(opts
        .getIntegerOption(ConfigurationOptions.OPTION_BACKGROUND_COLOR)));
    plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
    plot.setOutlineStroke(new BasicStroke(0.0f));

    Color bckgrnd = ChartConfigurationOptions.getColor(opts
        .getIntegerOption(ConfigurationOptions.OPTION_BACKGROUND_COLOR));

    IntervalMarker targetHigh = new IntervalMarker(8, 10, bckgrnd);
    IntervalMarker targetLow = new IntervalMarker(10, 20, bckgrnd);
    if (isPromis) {
      targetHigh = new IntervalMarker(80, 90, bckgrnd);
      targetLow = new IntervalMarker(10, 20, bckgrnd);
      targetLow.setLabel("Better");
      targetLow.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
      targetLow.setLabelAnchor(RectangleAnchor.LEFT);
      targetLow.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
    }
    targetHigh.setLabel("Worse");
    targetHigh.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
    targetHigh.setLabelAnchor(RectangleAnchor.LEFT);
    targetHigh.setLabelTextAnchor(TextAnchor.CENTER_LEFT);

    Color color60, color40, color30, color10;
    color60 = ChartConfigurationOptions.getColor(ConfigurationOptions.OPTION_BANDING_COLOR_2);
    color40 = ChartConfigurationOptions.getColor(ConfigurationOptions.OPTION_BANDING_COLOR_1);
    color30 = ChartConfigurationOptions.getColor(ConfigurationOptions.OPTION_BANDING_COLOR_1);
    color10 = ChartConfigurationOptions.getColor(ConfigurationOptions.OPTION_BANDING_COLOR_2);
    /*
     * Add shading & markers to the chart
     */
    if (opts.getBooleanOption(ConfigurationOptions.OPTION_BANDING)) {
      targetHigh.setPaint(new Color(255, 255, 255, 100));
      targetLow.setPaint(new Color(255, 255, 255, 255));
      IntervalMarker marker1;
      IntervalMarker marker2;
      IntervalMarker marker3 = new IntervalMarker(6, 8, color40);
      IntervalMarker marker4 = new IntervalMarker(8, 10, color60);
      if (isPromis) {
        marker1 = new IntervalMarker(10, 30, color10);
        marker2 = new IntervalMarker(30, 40, color30);
        marker3 = new IntervalMarker(40, 60, color40);
        marker4 = new IntervalMarker(60, 80, color60);
        plot.addRangeMarker(marker1, Layer.BACKGROUND);
        plot.addRangeMarker(marker2, Layer.BACKGROUND);
        plot.addRangeMarker(targetLow, Layer.BACKGROUND);
      }

      plot.addRangeMarker(marker3, Layer.BACKGROUND);
      plot.addRangeMarker(marker4, Layer.BACKGROUND);
      plot.addRangeMarker(targetHigh, Layer.BACKGROUND);

    }
    final ValueMarker centerLine = new ValueMarker(50);
    centerLine.setStroke(new BasicStroke(2.2f));
    if (opts.getBooleanOption(ConfigurationOptions.OPTION_GRAY)) {
      centerLine.setPaint(Color.BLACK);
    } else {
      centerLine.setPaint(opts.getOptionColor(ConfigurationOptions.OPTION_CENTERLINE_COLOR));
    }
    plot.addRangeMarker(centerLine);

    final ValueMarker topLine = new ValueMarker(90);
    topLine.setStroke(new BasicStroke(2.4f));
    topLine.setPaint(ChartConfigurationOptions.BLACK_80);
    plot.addRangeMarker(topLine);

    final ValueMarker botLine = new ValueMarker(10);
    botLine.setStroke(new BasicStroke(2.4f));
    botLine.setPaint(ChartConfigurationOptions.BLACK_80);
    plot.addRangeMarker(botLine);

    return plot;
  }

  public static Font getFont(ChartConfigurationOptions opts, int style, int size) {
    Integer scaleBy = opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES);
    if (scaleBy != null && scaleBy > 0 && scaleBy != 100) {
      return new Font(opts.getStringOption(ConfigurationOptions.OPTION_CHART_FONT), style, scaleFont(scaleBy, size));
    }
    return new Font(opts.getStringOption(ConfigurationOptions.OPTION_CHART_FONT), style, size);
  }

  public static int scaleFont(Integer scaleBy, int fontsize) {
    if (scaleBy == null || scaleBy == 100 || scaleBy == 0) {
      return fontsize;
    }
    return Long.valueOf(Math.round((double) fontsize * (double) scaleBy / 100.0)).intValue();
  }

  /*
   * Sets the common options used for rendering the charts across all providers
   */
  public static void setRendererOptions(XYLineAndShapeRenderer renderer2, ChartConfigurationOptions opts,
                                        int seriesCount) {

    renderer2.setSeriesShapesVisible(0, false);
    renderer2.setSeriesShapesFilled(0, false);
    renderer2.setSeriesPaint(0, Color.BLACK);
    renderer2.setSeriesItemLabelsVisible(0, false);

    Shape[] shape = new Shape[3];
    shape[0] = createSquare(5.0f);
    shape[1] = ShapeUtilities.createUpTriangle(6.0f);
    ShapeUtilities.rotateShape(shape[1], 90, 0, 0);
    shape[2] = createCircle(6.0f);
    renderer2.setBaseShape(shape[0]);

    int line = 1;
    int colorInx = 0;

    while (line < seriesCount) {
      if (colorInx >= ChartConfigurationOptions.LINE_COLORS.length) {
        colorInx = 0;
      }
      renderer2.setSeriesShape(line, shape[colorInx], true);
      renderer2.setSeriesShapesFilled(line, true);
      renderer2.setSeriesShapesVisible(line, true);
      renderer2.setSeriesLinesVisible(line, true);

      if (opts.getBooleanOption(ConfigurationOptions.OPTION_GRAY)) {
        renderer2.setSeriesPaint(line, Color.black);
      } else {
        renderer2.setSeriesPaint(line, ChartConfigurationOptions.LINE_COLORS[colorInx]);
      }
      renderer2.setSeriesStroke(line, ChartConfigurationOptions.LINE_STYLES[colorInx]);
      renderer2.setSeriesItemLabelsVisible(line, true);
      renderer2.setBasePaint(Color.BLACK);
      int titleFontSize = ChartMaker.scaleFont(opts.getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES),
          ChartMaker.TITLEFONTSIZE);
      Font titleFont = ChartMaker.getFont(opts, Font.PLAIN, titleFontSize);

      renderer2.setLegendTextFont(line, titleFont);
      renderer2.setLegendTextPaint(line, ChartConfigurationOptions.BLACK_80);

      line++;
      colorInx++;
    }

  }

  private static Shape createCircle(final float r) {
    // final GeneralPath p0 = new GeneralPath();
    return new Ellipse2D.Float(-r, -r, r * 2.0f, r * 2.0f);

  }

  private static Shape createSquare(final float s) {
    final GeneralPath gp = new GeneralPath();
    gp.moveTo(-s, -s);
    gp.lineTo(s, -s);
    gp.lineTo(s, s);
    gp.lineTo(-s, s);
    gp.closePath();
    return gp;
  }


  public Font getTitleFont() {
    return titleFont;
  }

  /**
   * Initializes the UIManager to get the default font family.
   * This is a public method because we had some trouble in this area with some OpenJDK 8 docker versions
   * so we call it during initialization to surface any problems early.
   */
  static public String initializeUiManagerDefaultFontFamily() {
    if (defaultFontFamily == null) { // called multiple times, during init and when each instance is made
      defaultFontFamily = "Dialog";  // if all else fails, "Dialog" is one of Java's 5 standard logical fonts
      String lookFeelClassName = "?";
      try {
        lookFeelClassName = UIManager.getCrossPlatformLookAndFeelClassName();
        UIManager.setLookAndFeel(lookFeelClassName);
      } catch (Throwable e) {
        String excClass = e.getClass().getSimpleName();
        logger.error(excClass + ", could not initialize UIManager with look+feel: " + lookFeelClassName);
      }
      try {
        Font font = UIManager.getFont("Label.font");
        if (font == null) {
          logger.warn("UIManager.getFont('Label.font)' not found, using Helvetica");
          defaultFontFamily = PDType1Font.HELVETICA.getName();
        }
        defaultFontFamily = font.getFamily(); // can throw an NPE if the LOCALE isn't set
        logger.info("Initialized ChartMaker font to "+defaultFontFamily);
      } catch (Throwable e) {
        logger.error(e.getClass().getSimpleName()+", error getting font family- using "+defaultFontFamily);
      }
    }
    return defaultFontFamily;
  }
}
