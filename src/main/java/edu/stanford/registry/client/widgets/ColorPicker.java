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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;

public class ColorPicker extends PopupPanel implements ClickHandler {
  RadioButton[] buttons;
  private FlexTable colorTable;
  Integer[][] colors;

  // Label[] labels;
  private int selected = 0;

  public ColorPicker(String title, String[] names, Integer[][] colors) {

    super(true); // autohide
    this.colors = colors;
    colorTable = new FlexTable();
    colorTable.setWidth("50px");
    colorTable.setCellPadding(0);
    colorTable.setCellSpacing(0);
    buttons = new RadioButton[colors.length];
    final Label[] labels = new Label[colors.length];
    // labels = new Label[colors.length];
    for (int i = 0; i < colors.length; i++) {
      String hexColor = RgbToHex(colors[i][0], colors[i][1], colors[i][2]);
      buttons[i] = new RadioButton(RgbToHex(colors[i][0], colors[i][1], colors[i][2]));
      buttons[i].getElement().getStyle().setBackgroundColor(hexColor);
      buttons[i].setSize("40px", "40px");
      buttons[i].setStylePrimaryName("surveyAnswerRadioButton");
      final int inx = i;
      buttons[i].addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setSelected(inx);
        }

      });
      colorTable.setWidget(i, 0, buttons[i]);
      colorTable.getCellFormatter().setAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER,
          HasVerticalAlignment.ALIGN_MIDDLE);

      labels[i] = new Label(names[i]);
      labels[i].setWidth("100px");
      labels[i].getElement().getStyle().setBackgroundColor(hexColor);

      colorTable.setWidget(i, 1, labels[i]);
      colorTable.getCellFormatter().setAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER,
          HasVerticalAlignment.ALIGN_MIDDLE);
      colorTable.setBorderWidth(0);

    }
    colorTable.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        //if (event.getSource() instanceof RadioButton) {
        //  RadioButton b = (RadioButton) event.getSource();
        //}
      }

    });
    setTitle("Colors");
    this.setModal(true);
    this.addDomHandler(this, ClickEvent.getType());
    setGlassEnabled(true);
    setWidget(colorTable);

  }

  @Override
  public void onClick(ClickEvent event)

  {

    ColorPicker.this.hide();

  }

  public static String RgbToHex(int r, int g, int b) {
    StringBuilder sb = new StringBuilder();
    sb.append('#').append(Integer.toHexString(r)).append(Integer.toHexString(g)).append(Integer.toHexString(b));
    return sb.toString();
  }

  public void setSelected(int i) {
    selected = i;
    if (buttons != null) {
      for (RadioButton button : buttons) {
        if (button.getValue()) {
          button.setValue(false);
        }
      }
    }
    buttons[i].setValue(true);
  }

  public int getSelected() {
    return selected;
  }
}
