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

package edu.stanford.registry.server;

import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;

import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.PublicCloneable;

public class CustomRenderer extends BarRenderer implements Cloneable,
    PublicCloneable, Serializable {
  static final long serialVersionUID = 6402943811500067531L;
  static Color primaryColor = Color.black;
  static Color secondaryColor = Color.white;
  CategoryDataset dataset = null;

  /**
   *
   **/
  public CustomRenderer(CategoryDataset dataset) {
    this.dataset = dataset;
  }

  private Color[] colors = { new Color(130, 0, 0), new Color(193, 128, 128),
      new Color(224, 192, 192), new Color(224, 192, 192),
      new Color(247, 239, 239), new Color(247, 239, 239),
      new Color(224, 192, 192), new Color(224, 192, 192),
      new Color(193, 128, 128), new Color(130, 0, 0) };

  float[] coloring = { 0.0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f,
      0.875f, 0.9f, 1.0f };

  @Override
  public Paint getItemPaint(final int row, final int column) {
    // returns color for each column
    Number dataValue = dataset.getValue(row, column);

    Integer intValue = dataValue.intValue();
    intValue = intValue / 10;
    if (intValue < 1) { // handle the zero's
      intValue = 1;
    }
    return (this.colors[intValue - 1]);
  }

  @Override
  public int getPassCount() {
    return 1;
  }
}
