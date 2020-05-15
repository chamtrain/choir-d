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

import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.ClientConfig;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;

public abstract class PageWidget extends ResizeComposite {
  private final Label messageLabel = new Label();
  public final HorizontalPanel messageBar = new HorizontalPanel();

  private ClinicUtils clinicUtils;
  private ClientUtils clientUtils;
  private boolean isLoadedFlag = false;

  public PageWidget(ClinicUtils clinicUtils) {
    setClinicUtils(clinicUtils);
    init();
  }

  public PageWidget(ClientUtils clientUtils) {
    setClientUtils(clientUtils);
    init();
  }

  private void init() {
    messageBar.setWidth("100%");
    messageBar.setStylePrimaryName("messageBar");
    messageBar.addStyleName("registrationMessageBar");
    messageBar.add(messageLabel);
    setEmptyMessage();
  }

  public void setErrorMessage(String message) {
    messageLabel.setText(message);
    messageLabel.setStylePrimaryName("serverResponseLabelError");
  }

  public void setEmptyMessage() {
    messageLabel.setText(" ");
    messageLabel.setStylePrimaryName("serverResponseLabelEmpty");
  }

  public void setSuccessMessage(String message) {
    messageLabel.setText(message);
    messageLabel.setStylePrimaryName("serverResponseLabelSuccess");
  }

  public HorizontalPanel getMessageBar() {
    return messageBar;
  }

  public ClinicUtils getUtils() {
    return clinicUtils;
  }

  public ClientUtils getClientUtils() {
    if (clientUtils == null) {
      return clinicUtils;
    }
    return clientUtils;
  }

  public Popup makePopup(String headerText) {
    return getUtils().makePopup(headerText);
  }

  public void setClinicUtils(ClinicUtils utils) {
    this.clinicUtils = utils;
  }

  public void setClientUtils(ClientUtils utils) {
    this.clientUtils = utils;
  }

  public ClientConfig getClientConfig() {
    return getClientUtils().getClientConfig();
  }

  public void setLoaded(boolean is) {
    isLoadedFlag = is;
  }

  public boolean isLoaded() {
    return isLoadedFlag;
  }

  /*
   * This is to set the focus when a page loads.
   */
  public void setInitialFocus(final Focusable widget) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        widget.setFocus(true);
      }
    });
  }

  /*
   * Workaround for an IE Focusing issue.
   */
  public void setFocus(Focusable widget) {

    String userAgent = Navigator.getUserAgent().toLowerCase();
    if (userAgent.contains("msie")) {
      TextBox dumbBox = new TextBox();
      RootLayoutPanel.get().add(dumbBox);
      dumbBox.setFocus(true);
      widget.setFocus(true);
      dumbBox.removeFromParent();
    } else {
      widget.setFocus(true);
    }
  }
}
