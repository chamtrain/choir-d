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

import edu.stanford.registry.shared.PROMISScore;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

public class XYErrorRenderer extends org.jfree.chart.renderer.xy.XYErrorRenderer {
  /**
   *
   */
  private static final long serialVersionUID = 7801979753719741834L;
  private static Logger logger = Logger.getLogger(XYErrorRenderer.class);
  ArrayList<PROMISScore> stats;
  private boolean myDrawXError;
  private boolean myDrawYError;

  public XYErrorRenderer(ArrayList<PROMISScore> stats, boolean linesVisible, boolean shapesVisible) {
    super();
    super.setBaseLinesVisible(linesVisible);
    super.setBaseShapesVisible(shapesVisible);

    this.stats = stats;
    this.myDrawXError = false;
    this.myDrawYError = false;

  }

  public void setCustomDrawXError(boolean drawX) {
    super.setDrawXError(false);
    this.myDrawXError = drawX;
  }

  public void setCustomDrawYError(boolean drawY) {
    super.setDrawYError(false);
    this.myDrawYError = drawY;
  }

  /**
   * We're overriding drawItem to use the stdError values to determine the size of the error bars.
   */
  @Override
  public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info,
                       XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series, int item,
                       CrosshairState crosshairState, int pass) {

    if (series == 0) {
      return;
    }
    boolean printDetails = false;

    if (pass == 0) {
      PlotOrientation orientation = plot.getOrientation();
      if (myDrawXError && item < stats.size()) {
        double x0 = dataset.getXValue(series, item);
        double x1 = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        RectangleEdge edge = plot.getDomainAxisEdge();

        double xx0 = plot.getDomainAxis().valueToJava2D(x0, dataArea, edge);
        xx0 = xx0 - stats.get(item).getStdError();
        double xx1 = plot.getDomainAxis().valueToJava2D(x1, dataArea, edge);
        xx1 = xx1 + stats.get(item).getStdError();
        double yy = plot.getRangeAxis().valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
        Line2D line;
        Line2D cap1 = null;
        Line2D cap2 = null;
        if (printDetails) {
          logger.debug("errorbars will be drawn from " + xx0 + " to " + xx1 + " for a stderr of "
              + stats.get(item).getStdError());
        }
        double adj = this.getCapLength() / 2; // get 1/2 the cap length
        if (orientation.equals(PlotOrientation.VERTICAL)) {
          line = new Line2D.Double(xx0, yy, xx1, yy);
          cap1 = new Line2D.Double(xx0, yy - adj, xx0, yy + adj);
          cap2 = new Line2D.Double(xx1, yy - adj, xx1, yy + adj);
        } else { // PlotOrientation.HORIZONTAL
          line = new Line2D.Double(yy, xx0, yy, xx1);
          cap1 = new Line2D.Double(yy - adj, xx0, yy + adj, xx0);
          cap2 = new Line2D.Double(yy - adj, xx1, yy + adj, xx1);
        }
        g2.setStroke(new BasicStroke(1.0f));
        if (getErrorPaint() != null) {
          g2.setPaint(getErrorPaint());
        } else {
          g2.setPaint(getItemPaint(series, item));
        }
        g2.draw(line);
        g2.draw(cap1);
        g2.draw(cap2);
      }
      if (myDrawYError && item < stats.size()) {

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX1 = domainAxis.valueToJava2D(dataset.getXValue(series, item), dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(dataset.getYValue(series, item), dataArea, yAxisLocation);
        if (getItemShapeVisible(series, item)) {
          Shape point = getItemShape(series, item);
          Stroke stroke = getSeriesStroke(series);
          Shape shape = stroke.createStrokedShape(point);
          if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            shape = ShapeUtilities.createTranslatedShape(shape, transY1, transX1);
          } else if (orientation.equals(PlotOrientation.VERTICAL)) {
            shape = ShapeUtilities.createTranslatedShape(shape, transX1, transY1);
          }

          Rectangle rectangle = shape.getBounds();
          double pointX = rectangle.getCenterX();
          double pointY = rectangle.getCenterY();
          //

          double score = stats.get(item).getScore().doubleValue();
          double theta = stats.get(item).getTheta();
          double stdEr = stats.get(item).getStdError();
          double errBarFrom = 10 * (theta - stdEr) + 50;
          double errBarTo = 10 * (theta + stdEr) + 50;

          double xx = rectangle.getCenterX(); // dataset.getXValue(series, item);
          double yy0 = plot.getRangeAxis().valueToJava2D(errBarFrom, dataArea, yAxisLocation);
          double yy1 = plot.getRangeAxis().valueToJava2D(errBarTo, dataArea, yAxisLocation);

          if (printDetails) {
            logger.debug("theta=" + stats.get(item).getTheta() + " score=" + score + " stdErr=" + stdEr + " from="
                + errBarFrom + " to=" + errBarTo + " point (x,y)=(" + pointX + "," + pointY + ") height:"
                + rectangle.getHeight() + " translated score (y)="
                + Math.round(plot.getRangeAxis().valueToJava2D(score, dataArea, yAxisLocation)) + " errBarFrom (y)="
                + Math.round(yy0) + " to (y)=" + Math.round(yy1));
          }

          // double xx = plot.getDomainAxis().valueToJava2D(x, dataArea, plot.getDomainAxisEdge());
          Line2D line;
          Line2D cap1 = null;
          Line2D cap2 = null;
          double adj = getCapLength() / 2; // Get 1/2 the cap length
          long logXX = Math.round(xx - adj);
          if (orientation.equals(PlotOrientation.VERTICAL)) {
            line = new Line2D.Double(xx, yy0, xx, yy1);
            cap1 = new Line2D.Double(xx - adj, yy0, xx + adj, yy0);
            cap2 = new Line2D.Double(xx - adj, yy1, xx + adj, yy1);
            if (printDetails) {
              logger.debug("VERTICAL: eBar (x1, y1, x2, y2)=(" + logXX + "," + Math.round(yy0) + "," + logXX + ","
                  + Math.round(yy1) + ")");
              logger.debug("VERTICAL: cap1 (x1, y1, x2, y2)=(" + Math.round(xx - adj) + "," + Math.round(yy0)
                  + Math.round(xx + adj) + "," + Math.round(yy0) + ")");
              logger.debug("VERTICAL: cap2 (x1, y1, x2, y2)=(" + Math.round(xx - adj) + "," + Math.round(yy1)
                  + Math.round(xx + adj) + "," + Math.round(yy1) + ")");
            }
          } else { // PlotOrientation.HORIZONTAL
            line = new Line2D.Double(yy0, xx, yy1, xx);
            cap1 = new Line2D.Double(yy0, xx - adj, yy0, xx + adj);
            cap2 = new Line2D.Double(yy1, xx - adj, yy1, xx + adj);
            if (printDetails) {
              logger.debug("HORIZONTAL: eBar (x1, y1, x2, y2)=(" + Math.round(yy0) + "," + logXX + ","
                  + Math.round(yy1) + "," + logXX + ")");
              logger.debug("HORIZONTAL: cap1 (x1, y1, x2, y2)=(" + Math.round(yy0) + "," + Math.round(xx - adj) + ","
                  + Math.round(yy0) + "," + Math.round(xx + adj) + ")");
              logger.debug("HORIZONTAL: cap2 (x1, y1, x2, y2)=(" + Math.round(yy1) + "," + Math.round(xx - adj) + ","
                  + Math.round(yy1) + "," + Math.round(xx + adj) + ")");
            }
          }

          g2.setStroke(new BasicStroke(.7f));
          if (getErrorPaint() != null) {
            g2.setPaint(getErrorPaint());
          } else {
            g2.setPaint(getItemPaint(series, item));
          }
          g2.draw(line);
          g2.draw(cap1);
          g2.draw(cap2);
        }
      }
    }

    super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);

  }

}
