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

package edu.stanford.registry.shared.xform;

import edu.stanford.registry.client.widgets.SelectCheckBoxList;
import edu.stanford.registry.client.widgets.SelectListBox;
import edu.stanford.registry.client.xform.XFormIntf;
import edu.stanford.registry.client.xform.XFormSurveyIntf;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;

public class SelectElement extends RegistryAnswer implements SurveyAnswerIntf, XFormSurveyIntf, IsSerializable {
  private static final long serialVersionUID = 1L;

  public static final int APPEARANCE_MINIMAL = 0;
  public static final int APPEARANCE_COMPACT = 1;
  public static final int APPEARANCE_FULL = 2;
  public static final String[] APPEARANCE = { "minimal", "compact", "full" };

  XFormIntf thisWidget = null;

  int appearanceOption = APPEARANCE_MINIMAL;
  int sizeOption = 1;
  String width = null;
  String height = null;

  ArrayList<SelectItem> items = new ArrayList<>();

  public SelectElement() {
  }

  public void setAppearance(int option) {
    if (option < APPEARANCE.length) appearanceOption = option;
  }

  public void setAppearance(String option) {
    for (int a = 0; a < APPEARANCE.length; a++) {
      if (APPEARANCE[a].equals(option)) setAppearance(a);
    }
  }

  public int getAppearance() {
    return appearanceOption;
  }

  public String getAppearanceString() {
    return APPEARANCE[appearanceOption];
  }

  public void setSize(int sz) {
    sizeOption = sz;
  }

  public int getSize() {
    return sizeOption;
  }

  public void setWidth(String w) {
    width = w;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String h) {
    height = h;
  }

  public String getWidth() {
    return width;
  }

  public void addItem(SelectItem item) {
    items.add(item);
  }

  public ArrayList<SelectItem> getItems() {
    return items;
  }

  public XFormIntf getWidget(String name, String tag) {
    if (SelectElement.APPEARANCE_FULL == getAppearance()) {
      // display check boxes
      thisWidget = getCheckBox(this, name, tag);
    } else {
      // display list box
      thisWidget = getListBox(true, name, tag);
    }
    return thisWidget;
  }

  public SelectListBox getListBox(boolean multiple, String name, String tag) {
    SelectListBox listBox = new SelectListBox(multiple, name, tag, getLabel(), getReference());
    if (APPEARANCE_MINIMAL == getAppearance()) {
      listBox.getListBox().setVisibleItemCount(1);
    } else {
      listBox.getListBox().setVisibleItemCount(getSize());
    }
    for (SelectItem itm : items) {
      listBox.getListBox().addItem(itm.getLabel(), itm.getValue());
    }
    if (getLabel() != null) {
      listBox.setName(getLabel());
    } else {
      listBox.setName(getReference());
    }
    return listBox;
  }

  public SelectCheckBoxList getCheckBox(SelectElement el, String name, String tag) {
    SelectElement element = el;
    CheckBox[] boxes = new CheckBox[element.getItems().size()];
    for (int inx = 0; inx < element.getItems().size(); inx++) {
      SelectItem itm = element.getItems().get(inx);
      CheckBox cbox = new CheckBox(itm.getLabel());
      cbox.setFormValue(itm.getValue());
      boxes[inx] = cbox;
    }
    return new SelectCheckBoxList(name, tag, getLabel(), getReference(), boxes);
  }

  public void setSelected(int inx, boolean value) {
    items.get(inx).setSelected(value);
  }

  @Override
  public boolean getSelected() {
    for (SelectItem item : items) {
      if (item.getSelected()) {
        // Make sure the selected item has a value -- to support blanks at the
        // top to force selection
        if (item.getValue() != null && item.getValue().trim().length() > 0) return true;
      }
    }
    return false;
  }

  public ArrayList<SelectItem> getSelectedItems() {
    ArrayList<SelectItem> selectedItems = new ArrayList<>();
    for (SelectItem item : items) {
      if (item.getSelected()) selectedItems.add(item);
    }
    return selectedItems;
  }

  public boolean getSelected(int inx) {
    return items.get(inx).getSelected();
  }

  @Override
  public void reset() {
    for (SelectItem item : items) {
      item.setSelected(false);
    }
  }

  @Override
  public boolean hasData() {
    if (getSelectedItems().size() > 0) return true;
    return false;
  }

  @Override
  public void setAttribute(String key, String value) throws NumberFormatException {
    super.setAttribute(key, value);
    if (Constants.XFORM_WIDTH.equals(key)) setWidth(value);
    if (Constants.XFORM_HEIGHT.equals(key)) setHeight(value);
  }

  @Override
  public ArrayList<String> getResponse() {
    ArrayList<String> responses = new ArrayList<>();
    ArrayList<SelectItem> selectedItems = getSelectedItems();
    for (SelectItem selectedItem : selectedItems) {
      if (getLabel() != null && getLabel().trim().length() > 0 && responses.size() == 0) {
        responses.add(getLabel() + ": " + selectedItem.getLabel());
      } else {
        responses.add(selectedItem.getLabel());
      }
    }
    return responses;
  }
}
