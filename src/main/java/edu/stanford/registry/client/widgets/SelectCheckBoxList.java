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

import edu.stanford.registry.client.xform.XFormIntf;
import edu.stanford.registry.client.xform.XMLWidget;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SelectCheckBoxList extends XMLWidget implements XFormIntf {
  FlowPanel outerPanel = new FlowPanel();

  VerticalPanel boxes = new VerticalPanel();

  Label lbl = new Label();
  int checkedCount = 0;
  boolean dataHasBeenSet = false;

  public SelectCheckBoxList() {
  }

  public SelectCheckBoxList(String elementName, String elementTag, String elementLabel, String reference,
                            CheckBox[] cboxes) {

    setName(elementName);
    setTag(elementTag);
    setLabel(elementLabel);
    setReference(reference);

    if (getLabel() != null) {
      lbl.setText(getLabel());
    } else {
      lbl.setText(getReference());
    }
    outerPanel.add(lbl);

    for (CheckBox cboxe : cboxes) {
      cboxe.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          CheckBox cbox = (CheckBox) event.getSource();
          if (cbox.getValue()) {
            checkedCount++;
          } else {
            checkedCount--;
          }
        }
      });
      boxes.add(cboxe);
    }
    outerPanel.add(boxes);
    initWidget(outerPanel);

  }

  public int getCheckedCount() {
    return checkedCount;
  }

  public ArrayList<CheckBox> getBoxes() {
    ArrayList<CheckBox> boxList = new ArrayList<>();
    for (int i = 0; i < boxes.getWidgetCount(); i++) {
      boxList.add((CheckBox) boxes.getWidget(i));
    }
    return boxList;
  }

  /**
   * Have any of the options been chosen
   */
  @Override
  public boolean hasData() {
    for (int i = 0; i < boxes.getWidgetCount(); i++) {
      if (((CheckBox) boxes.getWidget(i)).getValue()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ArrayList<String> getResponse() {
    ArrayList<String> responses = new ArrayList<>();
    for (int i = 0; i < boxes.getWidgetCount(); i++) {
      CheckBox cb = ((CheckBox) boxes.getWidget(i));
      if (cb.getValue()) {
        responses.add(cb.getText());
      }
    }
    return responses;
  }

  @Override
  public boolean getSelected() {
    return hasData();
  }

  // clears all selected items
  @Override
  public void reset() {
    for (int i = 0; i < boxes.getWidgetCount(); i++) {
      ((CheckBox) boxes.getWidget(i)).setValue(false);
    }
  }
}
