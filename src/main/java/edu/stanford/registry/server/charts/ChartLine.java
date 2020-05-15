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

import java.awt.BasicStroke;
import java.awt.Color;

public class ChartLine {
  public static final float LONG_DASH = 12.0f;
  public static final float SHORT_DASH = 6.0f;
  public static final float DOTTED = 2.0f;
  public static final float NORMAL_WEIGHT = 2.0f;
  Color lineColor = Color.BLACK;
  float lineWeight = NORMAL_WEIGHT;
  float dashLength = SHORT_DASH;

  public ChartLine() {

  }

  public ChartLine(float lWeight) {
    setLineWeight(lWeight);
  }

  public ChartLine(float lWeight, Color colr) {
    setLineWeight(lWeight);
    setLineColor(colr);
  }

  public ChartLine(Color colr, float dLength) {
    setLineColor(colr);
    setDashLength(dLength);
  }

  public ChartLine(Color colr) {
    setLineColor(colr);
  }

  public ChartLine(float lWeight, Color colr, float dLength) {
    setLineWeight(lWeight);
    setLineColor(colr);
    setDashLength(dLength);
  }

  public void setDashLength(float dLength) {
    dashLength = dLength;
  }

  public void setLineWeight(float lWeight) {
    lineWeight = lWeight;
  }

  public BasicStroke getSolidLine() {
    return getSolidLine(lineWeight);
    // new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f }, 0.0f);

  }

  public BasicStroke getSolidLine(float lWeight) {
    return new BasicStroke(lWeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f);
  }

  public BasicStroke getDashedLine() {
    return getDashedLine(lineWeight, dashLength);
  }

  public BasicStroke getShortDashedLine() {
    return getShortDashedLine(lineWeight);
  }

  public BasicStroke getShortDashedLine(float lWeight) {
    return getDashedLine(lWeight, SHORT_DASH);
    // new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f }, 0.0f);

  }

  public BasicStroke getLongDashedLine() {
    return getLongDashedLine(lineWeight);
  }

  public BasicStroke getLongDashedLine(float lWeight) {
    return getDashedLine(lWeight, LONG_DASH);
  }

  public BasicStroke getDashedLine(float lWeight, float dLength) {
    return new BasicStroke(lWeight, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
        new float[] { dLength, dLength }, 0.0f);
  }

  public BasicStroke getDottedLine() {
    // return getDashedLine(lineWeight, DOTTED);

    return new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 4.0f, 4.0f, 4.0f },
        0.4f);

  }

  public BasicStroke getDottedLine(float lWeight) {
    return getDashedLine(lWeight, DOTTED);
  }

  public Color getLineColor() {
    return lineColor;
  }

  public void setLineColor(Color colr) {
    lineColor = colr;
  }
}
