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

import edu.stanford.registry.client.clinictabs.AssessmentActivityWidget;
import edu.stanford.registry.client.clinictabs.AssessmentActivityWidget.ShowPatientCallback;
import edu.stanford.registry.client.clinictabs.AssessmentConfigEditorWidget;
import edu.stanford.registry.client.clinictabs.ClinicReportsWidget;
import edu.stanford.registry.client.clinictabs.CustomHTMLWidget;
import edu.stanford.registry.client.clinictabs.ImportExport;
import edu.stanford.registry.client.clinictabs.ListAssessmentWidget;
import edu.stanford.registry.client.clinictabs.PatientDetailWidget;
import edu.stanford.registry.client.clinictabs.RegisterPatientWidget;
import edu.stanford.registry.client.clinictabs.ScheduleWidget;
import edu.stanford.registry.client.clinictabs.SiteAdminWidget;
import edu.stanford.registry.client.clinictabs.SurveyEditorWidget;
import edu.stanford.registry.client.clinictabs.SystemAdmin;
import edu.stanford.registry.client.clinictabs.TabWidget;
import edu.stanford.registry.client.clinictabs.UserAdminWidget;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicService;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.utils.ErrorHandler;
import edu.stanford.registry.client.widgets.PersonSearch;
import edu.stanford.registry.client.widgets.WidgetResources;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomTab;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.api.ClientService;
import edu.stanford.registry.shared.api.ClientServiceAsync;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RegistryApp implements EntryPoint {
  /**
   * This needs to hold all possible tabs so if you add one increase this array size.
   */
  private TabWidget[] tabScrollPanels;
  private TabLayoutPanel tpanel;
  private final RegistryUi registryUi = new RegistryUi();
  private User user;
  private boolean isAdmin = false;
  private String appTitle = "Not set"; // this must be set correctly
  private DockLayoutPanel mainPanel;
  private final Label messageLabel = new Label();

  @SuppressWarnings("unused")
  private boolean isFuture = false;

  /* maps the users tabPanel index to the tabScrollPanels[] entry */
  private final Map<Integer, Integer> userTabMap = new HashMap<>();

  private final Logger logger = Logger.getLogger(RegistryApp.class.getName());
  private ClientUtils clientUtils;
  private ClinicServiceAsync clinicService;

  /**
   * This is the entry point method for the application.
   */
  @Override
  public void onModuleLoad() {
    // Set up custom logging to send log entries back to the server
    final ClientLogHandler logHandler = new ClientLogHandler();
    Logger.getLogger("").addHandler(logHandler);

    try {
      // Order of injection matters - make registry specific style override general widget styles
      RegistryResources.INSTANCE.cssGwt().ensureInjected();
      WidgetResources.INSTANCE.css().ensureInjected();
      RegistryResources.INSTANCE.css().ensureInjected();

      // Make sure any unhandled exceptions get displayed and hopefully reported back to the server
      GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void onUncaughtException(Throwable e) {
          new ErrorHandler().displayError("Client Error", "An unexpected problem occurred.", e);
        }
      });

      // Defer here so the uncaught exception handler will take effect
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          launch(logHandler);
        }
      });
    } catch (Throwable t) {
      new ErrorHandler().displayError("Client Error", "An unexpected problem occurred.", t);
    }

    // Disable the back button, adds "#x" to the URL
    History.newItem("x");
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        String historyToken = event.getValue();
        if (!historyToken.equals("x")) {
          History.newItem("x");
        }
      }
    });
  }

  private void launch(final ClientLogHandler logHandler) {
    mainPanel = new DockLayoutPanel(Unit.PX);

    final ClientServiceAsync clientService = createClientService();

    logHandler.setService(clientService);
    logHandler.setRemoteEnabled(true);

    // check for parameter on request
    String modeInput = com.google.gwt.user.client.Window.Location.getParameter("mode");
    if (modeInput != null && modeInput.equals("admin")) { // new
      isAdmin = true;
    } else if (modeInput != null && modeInput.equals("future")) {
      isFuture = true;
    }

    RootLayoutPanel rp = RootLayoutPanel.get();
    clientService.getClientConfig(new Callback<ClientConfig>() {
      @Override
      public void handleSuccess(ClientConfig clientConfig) {
        initClinicInterface(clientService, clientConfig);
      }
    });

    rp.add(registryUi);
  }

  private void initClinicInterface(ClientServiceAsync clientSvceAsync, final ClientConfig clientConfig) {
    clientSvceAsync.getUser(new Callback<User>() {
      @Override
      public void handleSuccess(User result) {
        user = result;
        registryUi.setMainWidget(mainPanel);

        appTitle = clientConfig.getParam("registry.name", "Not set");
        if (clientConfig.getSiteName() != null) { // should always be set
          RegistryRpcRequestBuilder.setSiteName(clientConfig.getParam("siteName"));
        }

        Window.setTitle(appTitle);
        if (user != null) {
          clientUtils = new ClientUtils(clientConfig, user);
          String about = clientUtils.getParam("aboutus.link");
          String terms = clientUtils.getParam("terms.link");
          String contact = clientUtils.getParam("contact.link");
          registryUi.assembleFooter(about, terms, contact);
          registryUi.setLoggedInUser(user);
        }
        registryUi.assembleHeader(appTitle, clientConfig.getSiteName());
        if (user != null) {
          makeTabbedPage(clientConfig);
        } else {
          setErrorMessage("No authority to use this application");
        }
      }
    });
  }

  private void makeTabbedPage(final ClientConfig clientConfig) {
    tpanel = new TabLayoutPanel(40, Unit.PX);
    mainPanel.add(tpanel);
    int numberTabs = 13;
    if (clientConfig.getCustomTabs() != null && clientConfig.getCustomTabs().size() > 0) {
      numberTabs = numberTabs + clientConfig.getCustomTabs().size();
    }

    tabScrollPanels = new TabWidget[numberTabs];
    tpanel.getElement().setAttribute("id", "tab-menu");
    tpanel.setStylePrimaryName("nav"); // TODO not valid, but has an effect by removing the default style...
    //tpanel.addStyleName("gwt-TabPanel");
    tpanel.addStyleName(RegistryResources.INSTANCE.css().registryTabPanel());
    final String site = clientUtils.getClientConfig().getSiteName();
    boolean haveScheduleTab = false;
    boolean haveRegistrationTab = false;
    boolean haveUserAdminTab = false;

    if (user.hasRole(Constants.ROLE_CLINIC_STAFF, site)) {
      haveScheduleTab = true;

      clinicService = createClinicService();
      final ClinicUtils clinicUtils = new ClinicUtils(clinicService, clientConfig, user);

      GWT.runAsync(new RunAsyncCallback() {
        @Override
        public void onSuccess() {
          try {
            final PatientDetailWidget patientDetailWidget = new PatientDetailWidget(clinicUtils, clinicService);
            final ShowPatientCallback showPatientCallback = new ShowPatientCallback() {
              @Override
              public void showPatient(final Patient pat) {
                // Isn't this lovely? All to prevent an error in IE8:
                // Can't move focus to the control because it is invisible, not enabled, or of a type that does not accept the focus.
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                  @Override
                  public void execute() {
                    tpanel.selectTab(1);
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                      @Override
                      public void execute() {
                        patientDetailWidget.setPatient(pat);
                        // push to custom tabs
                        for (TabWidget widget : tabScrollPanels) {
                          if (widget instanceof CustomHTMLWidget) {
                            CustomHTMLWidget customHTMLWidget = (CustomHTMLWidget) widget;
                            customHTMLWidget.setPatient(pat);
                          }
                        }
                      }
                    });
                  }
                });
              }
            };
            addTab(0, "Schedule", new ScheduleWidget(clinicUtils, clinicService, showPatientCallback));
            addTab(12, "Patient", patientDetailWidget);
            if (isAdmin && user.hasRole(Constants.ROLE_DEVELOPER, site)) {
              addTab(1, "Assessment Activity", new AssessmentActivityWidget(clinicUtils, clinicService, showPatientCallback));
            }
            addTab(2, "Reports", new ClinicReportsWidget(clinicUtils, clinicService));

            initTabWidget(0);
            initTabWidget(12);
            registryUi.setSearchPanel(new PersonSearch(clientConfig.getSiteId(),
                                                       clinicUtils, clinicService, showPatientCallback));

            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
              @Override
              public void execute() {
                tpanel.selectTab(0);
              }
            });
          } catch (Exception e) {
            new ErrorHandler().displayError("Unable to Launch Application", "", e);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          new ErrorHandler().displayError("Unable to Launch Application",
              "A portion of this application could not be loaded.", t);
        }
      });

      if (user.hasRole(Constants.ROLE_EDITOR, site) || user.hasRole(Constants.ROLE_BUILDER, site) || user.hasRole(Constants.ROLE_DEVELOPER, site)) {
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onSuccess() {
            addTab(3, "Site Administrator", new SiteAdminWidget(clinicUtils, user, site));
          }

          @Override
          public void onFailure(Throwable t) {
            new ErrorHandler().displayError("Unable to Launch Application",
                "A portion of this application could not be loaded.", t);
          }
        });
      }
      if (user.hasRole(Constants.ROLE_DATA_EXCHANGE, site)) {
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onSuccess() {
            addTab(4, "Import/Export", new ImportExport(clinicUtils));
          }

          @Override
          public void onFailure(Throwable t) {
            new ErrorHandler().displayError("Unable to Launch Application",
                "A portion of this application could not be loaded.", t);
          }
        });
      }
      if (isAdmin && user.hasRole(Constants.ROLE_DEVELOPER, site)) {
        /**
         * Add the developer only stuff
         */
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onSuccess() {
            addTab(7, "Assessments", new ListAssessmentWidget(clinicUtils, clinicService));
            addTab(8, "System Admin", new SystemAdmin(clinicUtils));
            addTab(10, "Update Survey", new SurveyEditorWidget(clinicUtils, clinicService));
          }

          @Override
          public void onFailure(Throwable t) {
            new ErrorHandler().displayError("Unable to Launch Application",
                "A portion of this application could not be loaded.", t);
          }
        });
      }
    }
    /*
     * Add the registration tab
     */
    if (user.hasRole(Constants.ROLE_REGISTRATION, site)) {
      haveRegistrationTab = true;
      final boolean finalHaveScheduleTab = haveScheduleTab;
      GWT.runAsync(new RunAsyncCallback() {
        @Override
        public void onSuccess() {
          addTab(6, "Register Patient", new RegisterPatientWidget(clientUtils));
          if (!finalHaveScheduleTab) {
            initTabWidget(6);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          new ErrorHandler().displayError("Unable to Launch Application",
              "A portion of this application could not be loaded.", t);
        }
      });
    }
    if (user.hasRole(Constants.ROLE_SECURTY, site)) {
      haveUserAdminTab = true;
      final boolean finalHaveScheduleTab1 = haveScheduleTab;
      final boolean finalHaveRegistrationTab = haveRegistrationTab;
      GWT.runAsync(new RunAsyncCallback() {
        @Override
        public void onSuccess() {
          addTab(11, "User Administration", new UserAdminWidget(clientUtils));
          if (!finalHaveScheduleTab1 && !finalHaveRegistrationTab) {
            initTabWidget(11);
          }
        }

        @Override
        public void onFailure(Throwable t) {
          new ErrorHandler().displayError("Unable to Launch Application",
              "A portion of this application could not be loaded.", t);
        }
      });
    }
    int nextTab = 14;
    // Add call to get customtabs from service
    for (final CustomTab tab : clientConfig.getCustomTabs()) {
      GWT.log("checking custom tab " + tab.getTitle());
      if (clientConfig.qualifiesForTab(tab, user)) {
        final boolean finalHaveScheduleTab = haveScheduleTab;
        final int customTabInx = nextTab;
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onSuccess() {
            addTab(customTabInx, tab.getTitle(),
            new CustomHTMLWidget(clientUtils, tab.getPath(), null));
               // new RegisterPatientWidget(clientUtils));
            if (!finalHaveScheduleTab) {
              initTabWidget(customTabInx);
            }
          }

          @Override
          public void onFailure(Throwable t) {
            new ErrorHandler().displayError("Unable to Launch Application",
                "A portion of this application could not be loaded.", t);
          }
        });
        nextTab++;
      }
    }

    if (user.hasRole(Constants.ROLE_ASSESSMENT_CONFIG_EDITOR, site)) {
      clinicService = createClinicService();
      final ClinicUtils clinicUtils = new ClinicUtils(clinicService, clientConfig, user);
      // Only display the custom assessment config tab when the feature is enabled
      String configValue = clinicUtils.getParam(Constants.ENABLE_CUSTOM_ASSESSMENT_CONFIG);
      if (configValue != null && configValue.equalsIgnoreCase("y")) {
        GWT.runAsync(new RunAsyncCallback() {
          @Override
          public void onSuccess() {
            addTab(15, "Assessment Configuration", new AssessmentConfigEditorWidget(clinicUtils, user, clientUtils.getSiteId()));
          }

          @Override
          public void onFailure(Throwable reason) {
            new ErrorHandler().displayError("Unable to Launch Application",
                "A portion of this application could not be loaded.", reason);
          }
        });
      }
    }

    if (!haveScheduleTab && !haveRegistrationTab && !haveUserAdminTab) {
      new ErrorHandler().displayError("Permission Denied",
          "You are not authorized to access this application.", null);
      setErrorMessage("You are not authorized to access this application");
      tpanel.setVisible(false);
    }
    tpanel.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      @Override
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        if (event == null) {
          return;
        }
        Integer tabNumber = event.getItem();
        if (tabNumber == null) {
          return;
        }

        Integer tabScrollPanelIndex = userTabMap.get(tabNumber);
        if (tabScrollPanelIndex == null) {
          logger.log(Level.WARNING, "Received tabNumber " + tabNumber + " but user does not have an index for it");
          return;
        }
        initTabWidget(tabScrollPanelIndex);

        // This is a work-around for an apparent bug in DataGrid in GWT 2.4.0 which collapses
        // the content of the table if a resize occurs when the DataGrid is not visible (e.g.
        // you are on a different tab)
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
          @Override
          public void execute() {
            tpanel.onResize();
          }
        });
      }
    });
  }

  private void addTab(int index, String title, TabWidget tab) {
    GWT.log("adding tab " + tpanel.getWidgetCount() + " index " + index + " name " + title);
    tabScrollPanels[index] = tab;
    userTabMap.put(tpanel.getWidgetCount(), index);
    tpanel.add(tabScrollPanels[index], title);
  }

  private void initTabWidget(final int tabNumber) {
    if (tabScrollPanels[tabNumber].isLoaded()) {
      return;
    }

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        if (!tabScrollPanels[tabNumber].isLoaded()) {
          tabScrollPanels[tabNumber].load();
          tabScrollPanels[tabNumber].setLoaded(true);
          GWT.log("tab " + tabNumber + " loaded");
        } else {
          GWT.log("tab " + tabNumber + " already loaded");
        }
      }
    });
    GWT.log("tab " + tabNumber + " scheduled for load");
  }

  private void setErrorMessage(String msg) {
    messageLabel.setText(msg);
    messageLabel.setStylePrimaryName(RegistryResources.INSTANCE.css().serverResponseLabelError());
    mainPanel.add(messageLabel);
  }

  private ClientServiceAsync createClientService() {
    final ClientServiceAsync clientService = GWT.create(ClientService.class);
    new RegistryEntryPoint().setServiceEntryPoint(clientService, "clientService");
    return clientService;
  }

  private ClinicServiceAsync createClinicService() {
    ClinicServiceAsync clinicService = GWT.create(ClinicService.class);
    new RegistryEntryPoint().setServiceEntryPoint(clinicService, "clinicService", new RegistryRpcRequestBuilder() {
      @Override
      protected void doSetCallback(RequestBuilder rb, final RequestCallback callback) {
        rb.setCallback(new RequestCallback() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == 302) {
              Location.reload();
            }
            callback.onResponseReceived(request, response);
          }

          @Override
          public void onError(Request request, Throwable exception) {
            callback.onError(request, exception);
          }
        });
      }
    });
    return clinicService;
  }
}
