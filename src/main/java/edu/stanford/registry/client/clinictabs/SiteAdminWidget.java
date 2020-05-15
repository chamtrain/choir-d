/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.client.RegistryEntryPoint;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.AdminService;
import edu.stanford.registry.client.service.AdminServiceAsync;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.survey.SurveyBuilder;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.ProcessXml;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.api.ClientService;
import edu.stanford.registry.shared.api.ClientServiceAsync;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.shared.event.TabShowHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Legend;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TabPanel;
import org.gwtbootstrap3.client.ui.VerticalButtonGroup;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.TabPosition;
import org.gwtbootstrap3.client.ui.html.ClearFix;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * CHOIR Adminstrative functions
 */
public class SiteAdminWidget extends TabWidget implements ClickHandler {
  private final Container topPanel;
  private final Alert actionMessage = new Alert();
  /**
   * Create a remote service proxy to talk to the server-side Table service.
   */
  private final AdminServiceAsync adminService = GWT.create(AdminService.class);


  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private ProcessXml processXml = null;

  private final Logger logger = Logger.getLogger(SiteAdminWidget.class.getName());
  private final Button closeButton = new Button("Close " + (new Image(RegistryResources.INSTANCE.close()).toString()));

  private final User user;
  private final String site;
  private static final String TABWIDTH= "1272px";
  private static final String TABHEIGHT= "700px";
  public SiteAdminWidget(ClinicUtils clinicUtils, User user, String site) {
    super(clinicUtils);
    this.user = user;
    this.site = site;

    topPanel = new Container();
    topPanel.setFluid(true);

    ScrollPanel scrollPanel = new ScrollPanel();
    scrollPanel.add(topPanel);
    DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
    dockPanel.add(scrollPanel);
    initWidget(dockPanel);
  }

  @Override
  public void load() {
    if (isLoaded()) {
      logger.log(Level.INFO, "already loaded");
      return;
    }
    setServiceEntryPoint(adminService, "admin");

    //final Label message = new Label("Loading...");

    processXml = getUtils().getProcessXml();
    showOpeningPanel();
    messageBar.setStylePrimaryName(css.messageBar());
    closeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          showOpeningPanel();
        }
      }
    );
    closeButton.setVisible(false);

  }

  private void showOpeningPanel() {
    TabPanel tabPanel = new TabPanel();
    tabPanel.setTabPosition(TabPosition.LEFT);
    NavTabs navTabs = new NavTabs();
    final TabContent tabContent = new TabContent();
    boolean hasActiveTab = false;
    tabPanel.add(navTabs);
    tabPanel.add(tabContent);
    topPanel.add(tabPanel);
    tabPanel.add(new ClearFix());
    setEmptyMessage();

    // build the individual tabs

    TabListItem adminItem = new TabListItem("System Admin");

    adminItem.setDataTarget("#tabIntro");
    final TabPane adminTab = new TabPane();
    adminTab.setId("tabIntro");

    if (user.hasRole(Constants.ROLE_DEVELOPER, site)) {
      navTabs.add(adminItem);
      adminTab.add(makeAdminPanel());
      adminTab.setActive(true);
      hasActiveTab = true;
      tabContent.add(adminTab);
    }
    adminItem.addShowHandler(new TabShowHandler() {
      @Override
      public void onShow(TabShowEvent event) {
        for (int w=0; w< tabContent.getWidgetCount(); w++) {
          ((TabPane)tabContent.getWidget(w)).setActive(false);
        }
        adminTab.setActive(true);
      }
    });
    TabListItem emailItem = new TabListItem("Email templates");
    emailItem.setDataTarget("#tabEmail");
    TabPane emailTab = new TabPane();
    emailTab.setId("tabEmail");

    if (user.hasRole(Constants.ROLE_EDITOR, site)) {
      navTabs.add(emailItem);
      emailTab.add(makeEmailWidget());
      if (!hasActiveTab) {
        emailTab.setActive(true);
        hasActiveTab = true;
      }
      tabContent.add(emailTab);
    } //else { // if we decide to show the tab but indicate no privilege
      // emailTab.add(noPermissionPanel());
    //}

    TabListItem surveyItem = new TabListItem("Survey builder");
    surveyItem.setDataTarget("#tabSurveyBuilder");
    TabPane surveyTab = new TabPane();
    surveyTab.setId("tabSurveyBuilder");

    if (user.hasRole(Constants.ROLE_BUILDER, site)) {
      navTabs.add(surveyItem);
      SurveyBuilder builder = new SurveyBuilder(getUtils());
      surveyTab.add(builder);
      if (!hasActiveTab) {
        surveyTab.setActive(true);
        hasActiveTab = true;
      }
      tabContent.add(surveyTab);
    }

    TabListItem configItem = new TabListItem("Application Configuration Editor");
    configItem.setDataTarget("#tabAppConfig");
    TabPane configTab = new TabPane();
    configTab.setId("tabAppConfig");

    if (user.hasRole(Constants.ROLE_DEVELOPER, site)) {
      navTabs.add(configItem);
      AppConfigEditorWidget configEditor = new AppConfigEditorWidget(getUtils());
      configEditor.setSize(TABWIDTH, TABHEIGHT);
      configTab.add(configEditor);
      if (!hasActiveTab) {
        configTab.setActive(true);
        hasActiveTab = true;
      }
      tabContent.add(configTab);
    }
    if (!hasActiveTab) {
      tabContent.add(noPermissionPanel());
    }

    closeButton.setVisible(false);
  }
  private void initTabWidget(final TabWidget tabWidget) {
    if (tabWidget.isLoaded()) {
      return;
    }

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override      public void execute() {
        if (!tabWidget.isLoaded()) {
          tabWidget.load();
          tabWidget.setLoaded(true);
        }
      }
    });
  }

  private Panel makeAdminPanel() {
    Panel adminPanel = new Panel();
    Legend heading = new Legend();
    heading.setText("Administrative functions");
    adminPanel.add(heading);
    adminPanel.setSize(TABWIDTH, TABHEIGHT);

    PanelHeader panelHeader = new PanelHeader();

    actionMessage.setVisible(false);
    panelHeader.add(actionMessage);
    adminPanel.add(panelHeader);

    final VerticalButtonGroup buttonGroup = new VerticalButtonGroup();

    Button emailButton = new Button("Send todays emails");
    emailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        sendEmails();
      }
    });
    Button xmlButton = new Button("Reload xml files");
    xmlButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        reloadXml();
      }
    });
    Button appButton = new Button("Reload application configuration");
    appButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        reloadConfig();
      }
    });
    Button usersButton = new Button("Reload users");
    usersButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        reloadUsers();
      }
    });

    buttonGroup.add(xmlButton);
    buttonGroup.add(appButton);
    buttonGroup.add(usersButton);
    buttonGroup.add(emailButton);
    PanelBody panelBody = new PanelBody();
    panelBody.add(buttonGroup);
    adminPanel.add(panelBody);
    return adminPanel;
  }

  private EmailTemplateWidget makeEmailWidget() {
    EmailTemplateWidget emailTemplateWidget = new EmailTemplateWidget(getUtils());
    initTabWidget(emailTemplateWidget);
    emailTemplateWidget.setSize(TABWIDTH, TABHEIGHT);
    return emailTemplateWidget;
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_EDITOR;
  }


  @Override
  public void onClick(ClickEvent event) {

  }

  private Panel noPermissionPanel() {
    Panel noPermissionPanel = new Panel();
    Legend msg = new Legend("You do not have permission for this area");
    noPermissionPanel.add(msg);
    noPermissionPanel.setSize("900px", "700px");
    return noPermissionPanel;
  }

  private void reloadXml() {
    getUtils().showLoadingPopUp();
    adminService.reloadXml(new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        setMessage("Reload xml failed with : " + caught.getMessage(), AlertType.DANGER);
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Boolean result) {
        int cnt = processXml.getProcessNames().size();
        setMessage("XML files reloaded for " + cnt + " Processes", AlertType.SUCCESS);
        getUtils().resetProcessXml();
        getUtils().hideLoadingPopUp();
      }
    });

  }
  private void reloadConfig() {
    getUtils().showLoadingPopUp();
    adminService.reloadConfig(new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable caught) {
        setMessage("Reload app config failed with : " + caught.getMessage(), AlertType.DANGER);
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Void result) {
        setMessage("Application configuration paramters reloaded", AlertType.SUCCESS);
        createClientService().getClientConfig(new Callback<ClientConfig>() {
          @Override
          public void handleSuccess(ClientConfig newClientConfig) {
            for (String key : newClientConfig.getParams().keySet()) {
              getClientConfig().getParams().put(key, newClientConfig.getParam(key));
            }
            ArrayList<String> pendingRemoval = new ArrayList<>();
            for (String key : getClientConfig().getParams().keySet()) {
              if (newClientConfig.getParams().get(key) == null) {
                //getClientConfig().getParams().remove(key); // remoe any old ones that don't exist
                pendingRemoval.add(key);
              }
            }
            newClientConfig.getParams().keySet().removeAll(pendingRemoval);
          }
        });
        getUtils().hideLoadingPopUp();
      }
    });
  }

  private void reloadUsers() {
    getUtils().showLoadingPopUp();
    adminService.reloadUsers(new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable caught) {
        setMessage("Reload users failed with : " + caught.getMessage(), AlertType.DANGER);
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Void result) {
        setMessage("Users reloaded", AlertType.SUCCESS);
        getUtils().hideLoadingPopUp();
      }
    });
  }
  private void sendEmails() {
    getUtils().showLoadingPopUp();
    adminService.doSurveyInvitations(new AsyncCallback<Integer>() {

      @Override
      public void onFailure(Throwable caught) {
        //setErrorMessage(caught.getMessage());
        setMessage("Send emails failed with : " + caught.getMessage(), AlertType.DANGER);
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Integer result) {
        setMessage(result + " emails sent", AlertType.SUCCESS);
        getUtils().hideLoadingPopUp();
      }
    });

  }

  private void setMessage(String message, AlertType type) {
    actionMessage.setText(message);
    actionMessage.setType(type);
    actionMessage.setVisible(true);
  }

  private ClientServiceAsync createClientService() {
    final ClientServiceAsync clientService = GWT.create(ClientService.class);
    new RegistryEntryPoint().setServiceEntryPoint(clientService, "clientService");
    return clientService;
  }
}
