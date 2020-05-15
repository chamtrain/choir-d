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

import edu.stanford.registry.shared.ConfigurationOptions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ChartConfigurationOptions {
  /**
   *
   */

  public static final Color CARDINAL_RED = new Color(140, 21, 21);
  public static final Color BLACK_80 = new Color(88, 87, 84);
  public static final Color[] LINE_COLORS = { new Color(39, 161, 49), new Color(12, 65, 209), new Color(78, 33, 131) };
  public static final BasicStroke[] LINE_STYLES = { new ChartLine(LINE_COLORS[1]).getSolidLine(),
      new ChartLine(LINE_COLORS[0]).getDottedLine(), new ChartLine(LINE_COLORS[2]).getLongDashedLine() };

  public static BufferedImage[] LINE_SYMBOLS = new BufferedImage[3];

  ConfigurationOptions options;
  private HashMap<Integer, Integer[]> colors = new HashMap<>();
  private Integer height = 118;
  private Integer width = 297;
  private Integer gap = 20;

  public ChartConfigurationOptions(ConfigurationOptions opts) {

    options = opts;
    init();

  }

  public ConfigurationOptions getOptions() {
    return options;
  }

  public Boolean getBooleanOption(int opt) {
    return options.getBooleanOption(opt);
  }

  public Integer getIntegerOption(int opt) {
    return options.getIntegerOption(opt);
  }

  public void setOption(Integer option, Integer value) {
    options.setOption(option, value);
  }

  public void setOption(Integer option, Boolean value) {
    options.setOption(option, value);
  }

  public void setOption(Integer option, String value) {
    options.setOption(option, value);
  }

  public String getStringOption(int opt) {
    return options.getStringOption(opt);
  }

  public static Color getColor(int colorOption) {
    return getColor(colorOption, 255);
  }

  public static Color getColor(int colorOption, int transparency) {
    if (colorOption < ConfigurationOptions.COLORS.length) {
      return new Color(ConfigurationOptions.COLORS[colorOption][0], ConfigurationOptions.COLORS[colorOption][1],
          ConfigurationOptions.COLORS[colorOption][2], 255);
    }
    return Color.WHITE;
  }

  public static Color getColor(Integer[] rgb) {
    return getColor(rgb, 255);
  }

  public static Color getColor(Integer[] rgb, int transparency) {
    return new Color(rgb[0], rgb[1], rgb[2], transparency);
  }

  public void setHeight(Integer h) {
    this.height = h;
  }

  public Integer getHeight() {
    return height;
  }

  public void setWidth(Integer w) {
    this.width = w;
  }

  public Integer getWidth() {
    return width;
  }

  public void setGap(Integer gap) {
    this.gap = gap;
  }

  public Integer getGap() {
    return gap;
  }

  public Integer[] getColorOption(Integer colorOpt) {
    Integer[] color = null;
    if (colorOpt != null) {
      color = colors.get(colorOpt);
    }
    if (color == null) {
      color = ConfigurationOptions.WHITE;
    }
    return color;

  }

  public void setColorOption(Integer option, Integer[] rgb) {
    if (rgb != null && rgb.length == 3) {
      colors.put(option, rgb);
    }
  }

  public Color getOptionColor(Integer colorOpt) {
    return getColor(getColorOption(colorOpt));
  }

  private void init() {
    colors.put(ConfigurationOptions.OPTION_BANDING_COLOR_1, ConfigurationOptions.SANDSTONE50);
    colors.put(ConfigurationOptions.OPTION_BANDING_COLOR_2, ConfigurationOptions.SANDSTONE20);
    colors.put(ConfigurationOptions.OPTION_BACKGROUND_COLOR, ConfigurationOptions.WHITE);
    colors.put(ConfigurationOptions.OPTION_CENTERLINE_COLOR, getColorArr(CARDINAL_RED));
    colors.put(ConfigurationOptions.OPTION_HEADING_TEXT_COLOR, ConfigurationOptions.WHITE);
    colors.put(ConfigurationOptions.OPTION_HEADING_BACKGROUND_COLOR, ConfigurationOptions.CARDINAL);
    colors.put(ConfigurationOptions.OPTION_ODD_COLOR, ConfigurationOptions.GRAY2);
    colors.put(ConfigurationOptions.OPTION_EVEN_COLOR, ConfigurationOptions.GRAY5);
    colors.put(ConfigurationOptions.OPTION_TABLE_BACKGROUND_COLOR, ConfigurationOptions.GRAY2);
    colors.put(ConfigurationOptions.OPTION_SECTION_BACKGROUND_COLOR, ConfigurationOptions.GRAY5);
    colors.put(ConfigurationOptions.OPTION_BODYMAP_FILL_COLOR, ConfigurationOptions.GRAY);
    colors.put(ConfigurationOptions.OPTION_BODYMAP_STROKE_COLOR, ConfigurationOptions.BLACK80);
    if (getStringOption(ConfigurationOptions.OPTION_CHART_FONT) == null) {
      setOption(ConfigurationOptions.OPTION_CHART_FONT, "Helvetica");
    }
    //if (getIntegerOption(ConfigurationOptions.OPTION_SCALE_IMAGES).intValue() == 0) {
    //  setOption(ConfigurationOptions.OPTION_SCALE_IMAGES, 80);
    //}
    setOption(ConfigurationOptions.OPTION_CHART_PERCENTILES, ConfigurationOptions.TRUE);
  }

  private Integer[] getColorArr(Color clr) {
    return new Integer[] { clr.getRed(), clr.getGreen(), clr.getBlue() };
  }

}
