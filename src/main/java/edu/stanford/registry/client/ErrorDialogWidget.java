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

package edu.stanford.registry.client;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ErrorDialogWidget extends DialogBox {

  private final Button closeButton = new Button("Close");

  private FocusWidget widgetToFocusOnClose = null;

  // private ArrayList<Button> buttonList = new ArrayList<Button>();
  private final HorizontalPanel headerPanel = new HorizontalPanel();
  private final HorizontalPanel messagePanel = new HorizontalPanel();
  private final HorizontalPanel footingPanel = new HorizontalPanel();
  private final HorizontalPanel buttonPanel = new HorizontalPanel();
  private final VerticalPanel dialogVPanel = new VerticalPanel();

  Button cancelButton = new Button(new Image(RegistryResources.INSTANCE.cancel()).toString());

  public void setError(String errorMsg) {
    HTML serverResponseLabel = new HTML();
    serverResponseLabel.setText(errorMsg);
    serverResponseLabel.addStyleName("serverResponseLabelError");
    showError(serverResponseLabel);
  }

  public void setError(HTML error) {
    showError(error);
  }

  private void showError(HTML error) {
    messagePanel.clear();
    messagePanel.add(error);

    setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {

        center();
      }
    });
  }

  public ErrorDialogWidget() {
    super();

    // Create the popup dialog box
//    setAnimationEnabled(true);
    dialogVPanel.addStyleName("dialogVPanel");

    headerPanel.addStyleName("head");
    headerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    // Hide the window when cancel button is clicked
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });
    cancelButton.setStylePrimaryName("imageButton");
    cancelButton.addStyleName("popUpDialogCloseButton");
    cancelButton.setTitle("Close window");
    headerPanel.add(cancelButton);
    headerPanel.setWidth("100%");
    headerPanel.setHeight("20px");

    dialogVPanel.add(headerPanel);
    dialogVPanel.add(messagePanel);

    footingPanel.setStylePrimaryName("clTabPgFootingBar");
    closeButton.setStylePrimaryName("sendButton");
    setButtonsClickHandler(closeButton);
    buttonPanel.add(closeButton);
    footingPanel.add(buttonPanel);

    dialogVPanel.add(footingPanel);
    setWidget(dialogVPanel);
  }

  /**
   * public void setFocusOnClose(FocusWidget w) { widgetToFocusOnClose = w;
   */

  public void setCustomButtons(ArrayList<Button> buttons) {

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
    // Add a handler to close the DialogBox
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
    cancelButton.addClickHandler(handler);
  }

}
