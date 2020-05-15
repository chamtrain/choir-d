/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.SecurityService;
import edu.stanford.registry.client.service.SecurityServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.UserDetail;
import edu.stanford.survey.client.ui.JavaScriptInjector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Client widget for including html content inside a tab in the application
 *
 * @author tpacht
 */
public class HTMLWidget extends TabWidget implements RegistryTabIntf {

  private final SecurityServiceAsync securityService = GWT.create(SecurityService.class);
  // containers
  private final DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private final DockLayoutPanel centerPanel = new DockLayoutPanel(Unit.EM);
  private final DockLayoutPanel subPanel = new DockLayoutPanel(Unit.EM);
  private final SingleSelectionModel<UserDetail> selectionModel = new SingleSelectionModel<>();
  private Frame contentFrame;
  private String contentPath = null;

  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public HTMLWidget(ClientUtils clientUtils) {
    super(clientUtils);
    setServiceEntryPoint(securityService, "securityService");
    initWidget(mainPanel);
  }

  public HTMLWidget(ClientUtils clientUtils, String path) {
    super(clientUtils);
    contentPath = path;
    SecurityServiceAsync securityService = GWT.create(SecurityService.class);
    setServiceEntryPoint(securityService, "securityService");
    initWidget(mainPanel);
  }

  @Override
  public void load() {
    setEmptyMessage();
    mainPanel.addNorth(getMessageBar(), 2);

    centerPanel.setStylePrimaryName(css.centerPanel());
    subPanel.add(centerPanel);
    mainPanel.add(subPanel);
    showContent();
  }


  private void showContent() {
    selectionModel.clear();
    centerPanel.clear();
    centerPanel.addStyleName(css.centerPanel());
    centerPanel.setWidth("100%");
    centerPanel.setHeight("100%");

    if (contentPath != null && !contentPath.isEmpty()) {
      if (contentFrame == null) {
        contentFrame = new Frame(GWT.getModuleBaseURL() + contentPath);
      } else {
        contentFrame.setUrl(contentPath);
      }
    }
    contentFrame.setWidth("100%");
    contentFrame.setHeight("100%");
    contentFrame.addLoadHandler(new LoadHandler() {

      @Override
      public void onLoad(LoadEvent event) {
        contentFrame.setVisible(true);
      }
    });
    centerPanel.add(contentFrame);
    setEmptyMessage();
    getClientUtils().hideLoadingPopUp();
  }

  public void refresh() {
    contentFrame.setUrl(contentFrame.getUrl());
  }

  void injectJavaScript(String script) {
    JavaScriptInjector.injectBodyScript(script);
  }

  @Override
  public String serviceName() {
    return null;
  }
}
