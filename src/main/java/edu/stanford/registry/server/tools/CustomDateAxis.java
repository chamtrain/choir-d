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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.axis.Tick;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

public class CustomDateAxis extends DateAxis {

  private static final long serialVersionUID = 1L;
  private static Logger logger = Logger.getLogger(CustomDateAxis.class);
  String fmt = "d MMM yy";
  TimeSeriesCollection collection;
  Date firstDate;
  Date lastDate;

  public CustomDateAxis(TimeSeriesCollection collection) {
    super();
    init(collection);
  }

  public CustomDateAxis(String label, TimeSeriesCollection collection) {
    super(label);
    init(collection);
  }

  private void init(TimeSeriesCollection collection) {
    this.collection = collection;

    for (int s = 0; s < collection.getSeriesCount(); s++) {
      TimeSeries timeSeries = collection.getSeries(s);

      for (int t = 0; t < timeSeries.getItemCount(); t++) {
        if (firstDate == null || timeSeries.getDataItem(t).getPeriod().getStart().before(firstDate)) {
          firstDate = timeSeries.getDataItem(t).getPeriod().getStart();
        }
        if (lastDate == null || timeSeries.getDataItem(t).getPeriod().getEnd().after(lastDate)) {
          lastDate = timeSeries.getDataItem(t).getPeriod().getEnd();
        }
      }
    }
    if (firstDate == null) {
      firstDate = new Date();
    }
    if (lastDate == null) {
      lastDate = new Date();
    }

    this.setDateFormatOverride(new SimpleDateFormat(fmt));
    this.setTickLabelsVisible(true);

  }

  @Override
  public List<Tick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {

    List<Tick> result = null;
    // if (RectangleEdge.isTopOrBottom(edge)) {
    result = customRefreshTicksHorizontal(g2, dataArea, edge);
    // } else if (RectangleEdge.isLeftOrRight(edge)) {
    // result = refreshTicksVertical(g2, dataArea, edge);
    // }
    return result;
  }

  public List<Tick> customRefreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
    List<Tick> result = new java.util.ArrayList<>();

    Font tickLabelFont = getTickLabelFont();
    g2.setFont(tickLabelFont);
    if (isAutoTickUnitSelection()) {
      selectAutoTickUnit(g2, dataArea, edge);
    }

    // DateTickUnit unit = new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat(fmt));
    // Date tickDate = calculateLowestVisibleTickValue(unit);
    int mos = numberOfMonths();
    int remainder = mos % 5; // remainder will be between 0 & 4
    int gap = (mos - remainder) / 5 + 1;
    Calendar cal = Calendar.getInstance();
    cal.setTime(firstDate);
    cal.set(Calendar.DAY_OF_MONTH, 1);

    logger.debug(mos + " months with a remainder of " + remainder + " gives us a gap of " + gap);

    while (cal.getTime().before(lastDate) && result.size() < 6) {

      // work out the value, label and position
      String tickLabel;
      DateFormat formatter = getDateFormatOverride();
      if (formatter != null) {
        tickLabel = formatter.format(cal.getTime());
      } else {
        tickLabel = getTickUnit().dateToString(cal.getTime());
      }
      TextAnchor anchor = null;
      TextAnchor rotationAnchor = null;
      double angle = 0.0;
      if (isVerticalTickLabels()) {
        anchor = TextAnchor.CENTER_RIGHT;
        rotationAnchor = TextAnchor.CENTER_RIGHT;
        if (edge.equals(RectangleEdge.TOP)) {
          angle = Math.PI / 2.0;
        } else {
          angle = -Math.PI / 2.0;
        }
      } else {
        if (edge.equals(RectangleEdge.TOP)) {
          anchor = TextAnchor.BOTTOM_CENTER;
          rotationAnchor = TextAnchor.BOTTOM_CENTER;
        } else {
          anchor = TextAnchor.TOP_CENTER;
          rotationAnchor = TextAnchor.TOP_CENTER;
        }
      }

      Tick tick = new DateTick(cal.getTime(), tickLabel, anchor, rotationAnchor, angle);
      result.add(tick);

      cal.add(Calendar.MONTH, gap);

    }

    return result;

  }

  private int numberOfMonths() {

    Calendar fCal = Calendar.getInstance();
    fCal.setTime(firstDate);
    Calendar lCal = Calendar.getInstance();
    lCal.setTime(lastDate);
    int mos = (lCal.get(Calendar.YEAR) - fCal.get(Calendar.YEAR)) * 12;
    mos = mos + ((12 - (fCal.get(Calendar.MONTH) - 1) + (lCal.get(Calendar.MONTH) - 12)) - 1);
    logger.debug("First date = " + firstDate.toString() + " Last date = " + lastDate.toString() + " is " + mos
        + " months");
    return mos;
  }
}
