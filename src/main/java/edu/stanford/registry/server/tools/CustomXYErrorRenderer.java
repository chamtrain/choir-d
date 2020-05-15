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

package edu.stanford.registry.server.tools;

import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.shared.ChartScore;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ShapeUtilities;

public class CustomXYErrorRenderer extends org.jfree.chart.renderer.xy.XYErrorRenderer {
  /**
   *
   */
  private static final long serialVersionUID = 7801979753719741834L;
  // private static Logger logger = Logger.getLogger(CustomXYErrorRenderer.class);
  private ChartInfo chartInfo = null;


  public CustomXYErrorRenderer(ChartInfo chartInfo, boolean linesVisible,
                               boolean shapesVisible) {
    super();
    super.setBaseLinesVisible(linesVisible);
    super.setBaseShapesVisible(shapesVisible);
    this.chartInfo = chartInfo;
  }

  public void setCustomDrawXError(boolean drawX) {
    super.setDrawXError(drawX);
  }

  public void setCustomDrawYError(boolean drawY) {
    super.setDrawYError(drawY);
  }

  /**
   * Overriding drawItem to put Arrows above scores for assisted surveys.
   */
  @Override
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
                       XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
                       CrosshairState crosshairState, int pass) {

    if (info == null || dataset.getSeriesCount() < 1 || series == 0) {
      return;
    }
    if (this.getSeriesShapesVisible(series)) {
      ChartScore score = chartInfo.getSeriesScore(series, item);
      if (score != null && score.getAssisted()) {
        drawAssistArrow(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState,
            pass);
      }
      if (score != null && score.wasReplaced()) {
        drawVersionMarker(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState,
            pass);
      }
    }
    super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);
  }

  private void drawAssistArrow(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
                               XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
                               CrosshairState crosshairState, int pass) {
    if (pass == 0 && getItemShapeVisible(series, item)) {
      double transX1 = domainAxis.valueToJava2D(dataset.getXValue(series, item), dataArea, plot.getDomainAxisEdge());
      double transY1 = rangeAxis.valueToJava2D(dataset.getYValue(series, item), dataArea, plot.getRangeAxisEdge());
      Shape point = getItemShape(series, item);
      point = ShapeUtilities.createTranslatedShape(point, transX1, transY1);
      double xx = point.getBounds().getCenterX();
      Double yy = dataArea.getMinY();
      Polygon poly = new Polygon();
      poly.addPoint(0, 0);
      poly.addPoint(-3, -9);
      poly.addPoint(3, -9);
      AffineTransform transformer = new AffineTransform();
      transformer.setToTranslation(xx, yy + 8);
      Shape arrow = transformer.createTransformedShape(poly);
      g2.setPaint(Color.RED);
      g2.setStroke(new BasicStroke(2.7f));
      g2.fill(arrow);
      g2.draw(arrow);
    }
  }
  private void drawVersionMarker(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
                               XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
                               CrosshairState crosshairState, int pass) {

    if (pass == 0 && getItemShapeVisible(series, item)) {
      double transX1 = domainAxis.valueToJava2D(dataset.getXValue(series, item), dataArea, plot.getDomainAxisEdge());
      double transY1 = rangeAxis.valueToJava2D(dataset.getYValue(series, item), dataArea, plot.getRangeAxisEdge());
      Shape point = getItemShape(series, item);
      point = ShapeUtilities.createTranslatedShape(point, transX1, transY1);
      Double xx = point.getBounds().getCenterX();
      Double yy = dataArea.getMaxY();
      Polygon poly = new Polygon();
      poly.addPoint(0, 0);
      poly.addPoint(-3,8);
      poly.addPoint(3, 8);
      AffineTransform transformer = new AffineTransform();
      transformer.setToTranslation(xx, yy - 10);
      Shape arrow = transformer.createTransformedShape(poly);
      g2.setPaint(Color.gray);
      g2.setStroke(new BasicStroke(2.7f));
      g2.fill(arrow);
      g2.draw(arrow);
    }
  }
}