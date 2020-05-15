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

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Popup extends PopupPanel {

  private final Button closeButton = new Button("Close");

  private FocusWidget widgetToFocusOnClose = null;

  private final HorizontalPanel headerPanel = new HorizontalPanel();
  private final HorizontalPanel messagePanel = new HorizontalPanel();
  //private final HorizontalPanel footingPanel = new HorizontalPanel();
  private final HorizontalPanel buttonPanel = new HorizontalPanel();
  private final VerticalPanel mainPanel = new VerticalPanel();
  private final VerticalPanel dialogVPanel = new VerticalPanel();

  public void setMessage(String msg) {
    Label label = new Label(msg);
    label.setText(msg);
    label.addStyleName("titleLabel");
    showMessage(label);
  }

  public void setMessage(HTML msg) {
    showMessage(msg);
  }

  public void showMessage(Widget msg) {
    messagePanel.clear();
    messagePanel.add(msg);

    setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        center();
      }
    });
  }

  public Popup(String headerText) {
    super(false);
    headerPanel.addStyleName("head");
    headerPanel.add(new HTML(headerText));
    headerPanel.setWidth("100%");
    headerPanel.setHeight("20px");
    init(true);
  }

  public Popup(Label label) {
    super(false);
    headerPanel.addStyleName("heading");
    headerPanel.add(label);
    headerPanel.setWidth("100%");
    headerPanel.setHeight("20px");
    init(true);
  }

  public Popup() {
    super(false); // do not allow click outside to close
    init(false); // without header
  }

  private void init(boolean header) {
    addStyleName("dialog-popUp");
//    setAnimationEnabled(true);
//    mainPanel.setBorderWidth(6);
    dialogVPanel.addStyleName("popupPanel");
//    dialogVPanel.setBorderWidth(0);

    if (header) {
      dialogVPanel.add(headerPanel);
    }

    dialogVPanel.add(messagePanel);

    // footingPanel.setStylePrimaryName("clTabPgFootingBar");
    closeButton.setStylePrimaryName("sendButton");
    setButtonsClickHandler(closeButton);
    buttonPanel.add(closeButton);

    // footingPanel.add(buttonPanel);
    // dialogVPanel.add(footingPanel);
    dialogVPanel.add(buttonPanel);
    /* We're wrapping the dialogpanel to get an outer border */
    mainPanel.add(dialogVPanel);
    setWidget(mainPanel);
  }

  public void setCustomButtons(List<Button> buttons) {

    buttonPanel.clear();
    buttonPanel.addStyleName("buttonPanel");

    for (int i = 0; i < buttons.size(); i++) {
      if (i == 0) {
        buttons.get(0).setFocus(true);
      }
      buttons.get(i).addStyleName("sendButton");
      buttonPanel.add(buttons.get(i));
    }
  }

  public void setCustomButtonsGWT(List<org.gwtbootstrap3.client.ui.Button> buttons) {

    buttonPanel.clear();
    buttonPanel.addStyleName("buttonPanel");

    for (int i = 0; i < buttons.size(); i++) {
      if (i == 0) {
        buttons.get(0).setFocus(true);
      }
      buttons.get(i).addStyleName("sendButton");
      buttonPanel.add(buttons.get(i));
    }
  }

  private void setButtonsClickHandler(Button button) {
    // Add a handler to close the popup
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        if (widgetToFocusOnClose != null) {
          widgetToFocusOnClose.setFocus(true);
        }
      }
    });
  }

  @SuppressWarnings("unused")
  private void setButtonsClickHandler(org.gwtbootstrap3.client.ui.Button button) {
    // Add a handler to close the popup
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        if (widgetToFocusOnClose != null) {
          widgetToFocusOnClose.setFocus(true);
        }
      }
    });
  }

  public void addCloseButtonClickHandler(ClickHandler handler) {
    closeButton.addClickHandler(handler);
  }

  public HorizontalPanel getHeaderPanel() {
    return headerPanel;
  }
}
