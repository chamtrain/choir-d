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

package edu.stanford.registry.shared.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RegistryAnswer implements SurveyAnswerIntf, Serializable {

  ArrayList<Child> children = null;

  String ref = null;
  String label = null;
  String name = null;
  String tag = null;
  String value = null;
  int align = Constants.ALIGN_VERTICAL;
  int labelPosition = Constants.POSITION_LEFT;
  String style = null;

  public RegistryAnswer() {

  }

  /**
   *
   */
  private static final long serialVersionUID = -5834293387729848727L;
  private ArrayList<String> description = new ArrayList<>();
  private HashMap<String, String> attributes = new HashMap<>();
  private boolean isSelected = false;
  private int type = Constants.TYPE_RADIO;

  @Override
  public ArrayList<String> getText() {
    return description;
  }

  @Override
  public void addText(String answer) {
    description.add(answer);
  }

  @Override
  public int getType() {

    return type;
  }

  @Override
  public void setType(int type) {
    this.type = type;
  }

  public void setSelected(boolean sel) {
    isSelected = sel;
  }

  @Override
  public boolean getSelected() {
    return isSelected;
  }

  public String getDescription() {
    if (getText() != null && getText().size() > 0) {
      return getText().get(0);
    }
    return null;
  }

  public String getStringValue() {
    switch (getType()) {
    case Constants.TYPE_RADIO:
      return getAttribute("Order");
    case Constants.TYPE_LABEL_LIST:
    case Constants.TYPE_INPUT:
      return value;
    default:
      return getAttribute("Order");
    }
  }

  public void addChild(Child child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    if (child == null) {
      return;
    }
    children.add(child);
  }

  public int getNumberChildren() {
    if (children == null) {
      return 0;
    }
    return children.size();

  }

  public ArrayList<Child> getChildren() {
    return children;
  }

  public Child getChild(String name) {
    if (children != null && name != null) {
      for (Child aChildren : children) {
        if (name.equals(aChildren.getName())) {
          return aChildren;
        }
      }
    }
    return null;
  }

  public Child getChild(String attributeName, String attributeValue) {
    if (children != null) {
      for (Child child : children) {
        if (child.hasAttribute(attributeName) && child.getAttribute(attributeName).equals(attributeValue)) {
          return child;
        }
      }

    }
    return null;
  }

  public boolean hasChildren() {
    if (children != null || children.size() > 0) {
      return true;
    }
    return false;

  }

  public int getNumberAttributes() {

    return attributes.size();
  }

  @Override
  public Set<String> getAttributeKeys() {

    return attributes.keySet();
  }

  @Override
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public void setAttribute(String key, String value) {
    if (key == null || value == null) {
      return;
    }
    attributes.put(key, value);
    if (Constants.CLASS.equals(key)) {
      setStyle(value);
    }

  }

  public boolean hasAttribute(String key) {
    return attributes.containsKey(key);
  }

  public void setReference(String reference) {
    ref = reference;
  }

  public String getReference() {
    return ref;
  }

  public void setLabel(String lbl) {
    label = lbl;
  }

  public String getLabel() {
    return label;
  }

  public void setAlign(int align) {
    if (align == Constants.ALIGN_HORIZONTAL) {
      this.align = align;
    } else {
      this.align = Constants.ALIGN_VERTICAL;
    }
  }

  public void setAlign(String align) {
    if (align == null) {
      return;
    }
    if (align.toLowerCase().equals("horizontal")) {
      this.align = Constants.ALIGN_HORIZONTAL;
    } else if (align.toLowerCase().equals("vertical")) {
      this.align = Constants.ALIGN_VERTICAL;
    }
  }

  public int getAlign() {
    return align;
  }

  public void setLabelPosition(int position) {
    if (position < Constants.POSITION.length) {
      labelPosition = position;
    }
  }

  public void setLabelPosition(String position) {
    if (position == null) {
      return;
    }
    for (int inx = 0; inx < Constants.POSITION.length; inx++) {
      if (Constants.POSITION[inx].equals(position.toLowerCase())) {
        setLabelPosition(inx);
      }
    }

  }

  public int getLabelPosition() {
    return labelPosition;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setStyle(String value) {
    style = value;
  }

  public String getStyle() {
    return style;
  }

  public int getCharacterWidth() {
    return getFormatIntValue(Constants.XFORM_CHAR_WIDTH, 30);
  }

  public int getLines() {
    return getFormatIntValue(Constants.XFORM_LINES, 1);
  }

  protected int getFormatIntValue(String key, int defaultValue) {
    Child format = getChild(Constants.XFORM_FORMAT);
    int value = defaultValue;
    try {
      value = Integer.parseInt(format.getAttribute(key));
    } catch (Exception ex) {
    }
    return value;
  }

  @Override
  public ArrayList<String> getResponse() {
    ArrayList<String> response = new ArrayList<>();
    if (value != null) {
      response.add(value);
    }
    return response;
  }

  public void reset() {
    setValue("");
    setSelected(false); /*
                         * switch (getType()) { case Constants.TYPE_RADIO: setSelected(false); break; case Constants.TYPE_LABEL_LIST: break; case Constants.TYPE_INPUT:
                         * setSelected(false); setValue(""); break; default: setSelected(false); }
                         */
  }

  @Override
  public String getClientId() {
    return getAttribute(Constants.ORDER);
  }

  @Override
  public void setClientId(String id) {
    setAttribute(Constants.ORDER, id);
  }
}
