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

package edu.stanford.registry.client.widgets;

import edu.stanford.registry.shared.survey.Constants;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CustomLabelList extends Composite {
  public int align = Constants.ALIGN_VERTICAL;

  HorizontalPanel horizontalPanel = new HorizontalPanel();
  VerticalPanel verticalPanel = new VerticalPanel();

  ArrayList<Label> labels = new ArrayList<>();

  int checkedCount = 0;
  boolean dataHasBeenSet = false;

  String style = null;
  FlexTable descriptionTable = new FlexTable();
  FlexTable labelTable = new FlexTable();
  Label descLabel;

  public CustomLabelList(int labelLocation, Label listLabel, int align) {
    this.descLabel = listLabel;
    this.descriptionTable.setWidget(0, 0, listLabel);

    switch (labelLocation) {
    case Constants.POSITION_ABOVE:
      verticalPanel.add(descriptionTable);
      verticalPanel.add(labelTable);
      initWidget(verticalPanel);
      break;
    case Constants.POSITION_BELOW:
      verticalPanel.add(labelTable);
      verticalPanel.add(descriptionTable);
      initWidget(verticalPanel);
      break;
    case Constants.POSITION_RIGHT:
      horizontalPanel.add(labelTable);
      horizontalPanel.add(descriptionTable);
      initWidget(horizontalPanel);
      break;
    default:
      horizontalPanel.add(descriptionTable);
      horizontalPanel.add(labelTable);
      initWidget(horizontalPanel);
      break;
    }
    this.align = align;
  }

  public void setList(ArrayList<Label> list) {
    labels.clear();
    if (list == null) {
      return;
    }
    for (Label aList : list) {
      addLabel(aList);
    }
  }

  public void addLabel(Label lbl) {

    int lInx = labels.size();
    labels.add(lbl);
    setHeadingStyle(lbl);
    if (align == Constants.ALIGN_HORIZONTAL) {
      labelTable.setWidget(0, lInx, labels.get(lInx));
    } else {
      labelTable.setWidget(lInx, 0, labels.get(lInx));
    }
  }

  public void addLabel(String value) {
    Label lbl = new Label(value);
    addLabel(lbl);
  }

  /**
   * Have any of the options been chosen
   */
  public boolean hasData() {
    return false;
  }

  // clears all selected items
  public void reset() {

  }

  @Override
  public void setStyleName(String style) {
    super.setStyleName(style);
    this.style = style;
    horizontalPanel.setStylePrimaryName(style + "-panel");
    verticalPanel.setStylePrimaryName(style + "-panel");
    descriptionTable.setStylePrimaryName(style + "-descriptionTable");
    setLabelStyle(descLabel);
    labelTable.setStylePrimaryName(style + "-labelTable");
    for (Label label : labels) {
      setLabelStyle(label);
    }
  }

  private void setLabelStyle(Label label) {
    if (this.style != null) {
      label.setStylePrimaryName(style + "-description");
    }
  }

  private void setHeadingStyle(Label label) {
    if (this.style != null) {
      label.setStylePrimaryName(this.style + "-label");
    }
  }

}
