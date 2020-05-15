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

package edu.stanford.registry.shared;

import java.io.Serializable;
import java.util.HashMap;

public class ConfigurationOptions implements Serializable {
  private static final long serialVersionUID = 1L;

  public static int FOLLOWUP_CHART = 0;
  public static int INITIAL_CHART = 1;
  public static int CHART_TEST = 2;
  public static Boolean FALSE = false;
  public static Boolean TRUE = true;
  public static Integer OPTION_GRAY = 1;
  public static Integer OPTION_BANDING = 2;
  public static Integer OPTION_BANDING_COLOR_1 = 1;
  public static Integer OPTION_BANDING_COLOR_2 = 2;
  public static Integer OPTION_BACKGROUND_COLOR = 5;
  public static Integer OPTION_CENTERLINE_COLOR = 6;
  public static Integer OPTION_ODD_COLOR = 7;
  public static Integer OPTION_EVEN_COLOR = 8;
  public static Integer OPTION_SECTION_BACKGROUND_COLOR = 9;
  public static Integer OPTION_TABLE_BACKGROUND_COLOR = 10;
  public static Integer OPTION_CHART_FONT = 11;
  public static Integer OPTION_SCALE_IMAGES = 12;
  public static Integer OPTION_HEADING_BACKGROUND_COLOR = 13;
  public static Integer OPTION_HEADING_TEXT_COLOR = 14;
  public static Integer OPTION_BODYMAP_FILL_COLOR = 15;
  public static Integer OPTION_BODYMAP_STROKE_COLOR = 16;
  public static Integer OPTION_CHART_PERCENTILES = 17;
  public static Integer[] WHITE = { 255, 255, 255 };
  public static Integer[] GRAY2 = { 242, 241, 235 };
  public static Integer[] GRAY3 = { 233, 230, 223 };
  public static Integer[] GRAY4 = { 227, 223, 213 };
  public static Integer[] GRAY5 = { 213, 208, 192 };
  public static Integer[] GRAY6 = { 172, 166, 141 };
  public static Integer[] GRAY60 = { 138, 136, 125 };
  public static Integer[] GRAY90 = { 86, 83, 71 };
  public static Integer[] GRAY = { 63, 60, 48 };
  public static Integer[] BLACK80 = { 88, 87, 84 };
  public static Integer[] SANDSTONE20 = { 248, 246, 234 };
  public static Integer[] SANDSTONE25 = { 246, 243, 229 };
  public static Integer[] SANDSTONE35 = { 243, 239, 216 };
  public static Integer[] SANDSTONE50 = { 238, 230, 203 };
  public static Integer[] SANDSTONE = { 221, 207, 153 };
  public static Integer[] BEIGE25 = { 230, 228, 219 };
  public static Integer[] BEIGE60 = { 196, 191, 169 };
  public static Integer[] BEIGE = { 157, 149, 115 };
  public static Integer[] CARDINAL = { 140, 21, 21 };
  public static Integer[] BLACK = { 0, 0, 0 };
  public static String[] COLOR_NAMES = { "White", "Gray2", "Gray3", "Gray4", "Gray5", "Gray6", "Gray60%", "Gray90%",
      "Gray", "Black80", "Sandstone20", "Sandstone25", "Sandstone35", "Sandstone50", "Beige25", "Beige60", "Beige",
      "Cardinal", "Black" };
  public static Integer[][] COLORS = { WHITE, GRAY2, GRAY3, GRAY4, GRAY5, GRAY6, GRAY60, GRAY90, GRAY, BLACK80,
      SANDSTONE20, SANDSTONE25, SANDSTONE35, SANDSTONE50, BEIGE25, BEIGE60, BEIGE, CARDINAL, BLACK };
  private static String COMMA = ",";
  private String SEP = ":";
  private int configurationType = 0;
  private HashMap<Integer, String> attributes = new HashMap<>();

  public ConfigurationOptions() {

  }

  public ConfigurationOptions(int type) {
    this.configurationType = type;
  }

  public ConfigurationOptions(int type, String string) {
    this.configurationType = type;
    this.fromString(string);
  }

  public void setType(int type) {
    this.configurationType = type;
  }

  public int getType() {
    return configurationType;
  }

  public void setOption(Integer option, Boolean value) {
    attributes.put(option, value ? "1" : "0");
  }

  public void setOption(Integer option, Integer value) {
    attributes.put(option, value.toString());
  }

  public void setOption(Integer option, String value) {
    attributes.put(option, value);
  }

  public Boolean getBooleanOption(Integer option) {
    Boolean value = "1".equals(attributes.get(option));
    return value;
  }

  public String getStringOption(Integer option) {
    return attributes.get(option);
  }

  public Integer getIntegerOption(Integer option) {
    try {
      if (attributes.get(option) != null) {
        return Integer.valueOf(attributes.get(option));
      }
    } catch (Exception e) {
    }
    return 0;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    attributes.keySet();
    for (Integer integer : attributes.keySet()) {
      if (buf.length() > 0) {
        buf.append(COMMA);
      }
      Integer key = integer;
      buf.append(key.toString()).append(SEP).append(attributes.get(key));
    }
    return buf.toString();
  }

  public void fromString(String attributesString) throws NumberFormatException {
    if (attributesString == null) {
      return;
    }
    String[] valueSets = attributesString.split(COMMA);
    for (String valueSet : valueSets) {
      String[] attributeSet = valueSet.split(SEP, 2);
      if (attributeSet.length == 1) {
        attributes.put(Integer.valueOf(attributeSet[0]), "0");
      } else if (attributeSet.length > 1) {
        attributes.put(Integer.valueOf(attributeSet[0]), (attributeSet[1]));
      }
    }
  }
}
