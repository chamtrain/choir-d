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

package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.RegistryRpcRequestBuilder;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.api.SurveyReport;
import edu.stanford.registry.client.api.SurveyReportFactory;
import edu.stanford.registry.client.api.SurveyReportStudy;
import edu.stanford.registry.client.api.SurveyStudyObj;
import edu.stanford.registry.client.api.SurveyStudyStepObj;
import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.client.service.PhysicianService;
import edu.stanford.registry.client.service.PhysicianServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.utils.DeclinePopup;
import edu.stanford.registry.client.utils.DisplayUtils;
import edu.stanford.registry.client.utils.HandleRegister;
import edu.stanford.registry.client.widgets.AssessmentActivityTree;
import edu.stanford.registry.client.widgets.ChangeSurveyPopup;
import edu.stanford.registry.client.widgets.Menu;
import edu.stanford.registry.client.widgets.PatientHeader;
import edu.stanford.registry.client.widgets.PatientIdentification;
import edu.stanford.registry.client.widgets.PopdownButton;
import edu.stanford.registry.client.widgets.PopdownButton.Customizer;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.client.widgets.PopupRelative;
import edu.stanford.registry.client.widgets.PopupRelative.Align;
import edu.stanford.registry.client.widgets.PopupRelative.CloseCallback;
import edu.stanford.registry.client.widgets.tset.TreatmentSetUI;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomTab;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientActivity;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveyStart;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TabPanel;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.TabPosition;
import org.gwtbootstrap3.client.ui.html.UnorderedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * This is the Patient tab
 *
 * It contains:
 * <br>PatientIdentification (the horizontal red-boxed bar) --- ControlButtonsPanel
 * <br>Optional TreatmentSet bar
 * <br>PatientHeader (the 2-column table of label/value pairs)
 * <br>patientActivityTable
 * <br>BottomButtonPanel
 */
public class PatientDetailWidget extends TabWidget implements InvalidEmailHandler {
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private final ClinicServiceAsync clinicService;
  private final PhysicianServiceAsync physicianService;
  protected PatientRegistrationsWidget patientAppts;
  private SurveyReportFactory factory = GWT.create(SurveyReportFactory.class);

  private final Logger logger = Logger.getLogger(PatientDetailWidget.class.getName());
  private Patient patient;
  private Label patientLabel;
  //private final DockLayoutPanel patientsHeaderPanel = new DockLayoutPanel(Unit.PX);
  private final HorizontalPanel patientsHeaderPanel = new HorizontalPanel();
  private final FlexTable patientActivityTable = new FlexTable();
  private ArrayList<PatientActivity> activityList = new ArrayList<>();
  private static final String ODD = "x-odd";
  private static final String EVEN = "x-even";
  // Clinical data details page control buttons
  //private final Button saveDetailsButton = new Button(new Image(RegistryResources.INSTANCE.save()).toString());
  //private final PatientButton registrationsButton = new PatientButton(new Image(RegistryResources.INSTANCE.calendar()).toString());
  private final FlowPanel controlButtonsPanel = new FlowPanel();
  final ArrayList<Button> editDocumentTypeButtons = new ArrayList<>();
  private final Grid treeLabels = new Grid(1, 3);
  private final Label dtTreeLabel = new Label("Date / Time");
  private final Label tpTreeLabel = new Label("Type");
  private final Label tkTreeLabel = new Label("Pin");
  private final Label surveyLabel = new Label(" ");
  private final Label emptyLabel = new Label(" ");
  private HorizontalPanel buttonPanel;
  private String physicianSurveyPath = null;
  private PopupRelative surveyPopup;
  private Frame surveyFrame;
  private String patientIdentificationViewVs = "0";
  private final TabPanel tabPanel = new TabPanel();
  private final ArrayList<CustomHTMLWidget> customHTMLWidgets = new ArrayList<>();
  NavTabs navTabs = new NavTabs();
  private TreatmentSetUI treatmentSetUI;
  private final ScrollPanel scroller = new ScrollPanel();

  public PatientDetailWidget(ClinicUtils clinicUtils, ClinicServiceAsync clinicService) {
    super(clinicUtils);
    this.clinicService = clinicService;
    if (getUtils().isPhysician()) {
      this.physicianService = createPhysicianService();
    } else {
      this.physicianService = null;
    }
    initWidget(mainPanel);
  }

  @Override
  public String serviceName() {
    return "ClinicServices";
  }

  @Override
  public void load() {
    if (isLoaded()) {
      return;
    }

    final FlowPanel northPanel = new FlowPanel();
    northPanel.add(super.getMessageBar());
    patientLabel = new Label("No patient selected. Search or select one from the Schedule tab.");
    northPanel.add(patientLabel);
    setErrorMessage("");

    patientAppts = new PatientRegistrationsWidget(getUtils(), clinicService);

    dtTreeLabel.setWidth("150px");
    tpTreeLabel.setWidth("200px");
    tkTreeLabel.setWidth("150px");
    dtTreeLabel.setStylePrimaryName(css.tableDataHeaderLabel());
    tpTreeLabel.setStylePrimaryName(css.tableDataHeaderLabel());
    tkTreeLabel.setStylePrimaryName(css.tableDataHeaderLabel());
    treeLabels.setWidget(0, 0, dtTreeLabel);
    treeLabels.setWidget(0, 1, tpTreeLabel);
    treeLabels.setWidget(0, 2, tkTreeLabel);
    surveyLabel.setStylePrimaryName(css.tableDataHeaderLabel());
    patientActivityTable.setStylePrimaryName(css.fixedList());
    patientActivityTable.addStyleName(css.dataList());

    controlButtonsPanel.setStylePrimaryName(css.patientHeader());

    buttonPanel = new HorizontalPanel();
    buttonPanel.setStylePrimaryName(css.clTabPgFootingBar());
    buttonPanel.addStyleName(css.clTabPgFootingBar());

    patientsHeaderPanel.addStyleName(css.patientHeaderPanel());
    patientsHeaderPanel.setWidth("100%");

    treatmentSetUI = new TreatmentSetUI(this, clinicService, physicianService, getUtils());

    northPanel.add(patientsHeaderPanel);
    double headerSize = 17.0;
    // Increase the header size when there are custom attributes
    if (getUtils().getClientConfig().getCustomPatientAttributeNames() != null) {
      int numberAttributes = getUtils().getClientConfig().getCustomPatientAttributeNames().size();
      int lines = (int) Math.ceil(numberAttributes / 2.0);
      headerSize = headerSize + (lines * 2.5);
    }
    mainPanel.addNorth(northPanel, treatmentSetUI.enlarge(headerSize));
    mainPanel.addSouth(buttonPanel, 4);
    scroller.setSize("98%", "98%");
    if (getUtils().getClientConfig().getCustomPatientTabs(getUtils().getUser()) != null &&
        getUtils().getClientConfig().getCustomPatientTabs(getUtils().getUser()).size() > 0) {
      scroller.add(tabPanel);
      makeTabs();
    } else {
     scroller.add(patientActivityTable);
    }
    mainPanel.add(scroller);
    patientIdentificationViewVs = getUtils().getParam("patientIdentificationViewVs","0");

    refreshPatient();
    setLoaded(true);
  }

  private void makeTabs() {
    tabPanel.clear();
    navTabs.clear();
    customHTMLWidgets.clear();;
    tabPanel.setTabPosition(TabPosition.TOP);
    TabContent tabContent = new TabContent();
    tabPanel.add(navTabs);
    tabPanel.add(tabContent);
    setEmptyMessage();
    TabListItem activityItem = new TabListItem("Activity");
    activityItem.setDataTarget("#tabIntro");
    TabPane activityTab = new TabPane();
    activityTab.setId("tabIntro");
    navTabs.add(activityItem);
    activityTab.add(patientActivityTable);
    activityTab.setActive(true);
    tabContent.add(activityTab);
    int tabInx = 0;
    if (patient != null) {
      for (CustomTab customTab : getUtils().getClientConfig().getCustomPatientTabs(getUtils().getUser())) {
        tabInx += 1;
        GWT.log("adding " + customTab.getPath());
        TabListItem tabItem = new TabListItem(customTab.getTitle());
        tabItem.setDataTarget("#" + tabInx);
        TabPane tabPane = new TabPane();
        tabPane.setId(String.valueOf(tabInx));
        navTabs.add(tabItem);
        final CustomHTMLWidget widget = new CustomHTMLWidget(getClientUtils(), customTab.getPath(), patient);
        initTabWidget(widget);
        customHTMLWidgets.add(widget);
        widget.setVisible(true);
        autofillCustomTab(widget);
        Window.addResizeHandler(new ResizeHandler() {
          @Override
          public void onResize(ResizeEvent event) {
            autofillCustomTab(widget);
          }
        });
        tabPane.add(widget);
        tabContent.add(tabPane);
      }
    }
  }

  private void autofillCustomTab(CustomHTMLWidget widget){
    widget.setSize(scroller.getOffsetWidth() + "px",  scroller.getOffsetHeight() - 40 + "px");
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
          tabWidget.setVisible(true);
        }
      }
    });
  }

  // The PatientIdentification bar above the PatientHeader and the ControlButtons to the right
  private void fillPatientHeader(Patient pat) {
    patientsHeaderPanel.clear();
    if (pat == null) {
      return;
    }

    setPatientControlButtonsPanel(pat);  // the buttons on the right side of the header
    FlowPanel patientsHeaderInnerPanel = new FlowPanel();
    if (!"0".equals(patientIdentificationViewVs)) {
      PatientIdentification patientIdentification = new PatientIdentification(pat, getUtils());
      patientIdentification.removeColumn(3);
      patientIdentification.addStyleName(css.patientHeader());
      patientsHeaderInnerPanel.add(patientIdentification);
    }
    if (treatmentSetUI.isEnabled()) {
      treatmentSetUI.addTreatmentBar(pat, patientsHeaderInnerPanel);
    }
    PatientHeader patientHeader = new PatientHeader(getUtils(), clinicService, pat, this);
    patientsHeaderInnerPanel.add(patientHeader);
    patientsHeaderPanel.add(patientsHeaderInnerPanel);
    patientsHeaderPanel.add(controlButtonsPanel);
  }

  // The list of activities below
  private void fillPatientActivityTable(final Patient pat, final String msg) {
    patientActivityTable.removeAllRows();
    if (pat == null) {
      return;
    }

    getUtils().showLoadingPopUp();
    clinicService.searchForActivity(pat.getPatientId(), true, new Callback<ArrayList<PatientActivity>>() {
      @Override
      protected void afterFailure() {
        getUtils().hideLoadingPopUp();
      }


      @Override
      public void handleSuccess(ArrayList<PatientActivity> result) {
        getUtils().hideLoadingPopUp();
        setEmptyMessage();
        if (msg != null) {
          setSuccessMessage(msg);
        }

        activityList = result;
        if (activityList != null && activityList.size() > 0) {
          patientActivityTable.setWidget(0, 0, treeLabels);
          patientActivityTable.setWidget(0, 1, surveyLabel);
          patientActivityTable.getRowFormatter().addStyleName(0, css.tableDataHeader());
          String lineStyle = ODD;
          for (int n = 0; n < activityList.size(); n++) {
            int row = n + 1;
            PatientActivity act = activityList.get(n);
            AssessmentActivityTree actTree = new AssessmentActivityTree(act, getUtils(), lineStyle);
            actTree.setStylePrimaryName(css.activityTree());
            patientActivityTable.setWidget(row, 0, actTree);

            if (getUtils().getProcessXml().isSurveyProcess(act.getRegistration().getSurveyType())) {
              // Show an image indicating whether they completed the
              // surveys for this registration.
              final boolean completed = act.getRegistration().getIsDone();
              Grid buttonGrid = new Grid(1, 4);
              buttonGrid.setWidget(0, 0, getUtils().getCompletedImage(completed));
              buttonGrid.getColumnFormatter().setWidth(0, "50px");
              final ApptRegistration registration = act.getRegistration();

              @SuppressWarnings("deprecation")
              SurveyRegistration surveyReg = registration.getSurveyReg();

              String category = getUtils().getProcessXml().getProcessAttribute(registration.getSurveyType(), Constants.ATTRIBUTE_CATEGORY);

              if ("physician".equals(category)) {
                if (getUtils().isPhysician()) {
                  if (completed) {
                    addPhysicianDataReport(buttonGrid, registration.getApptId());
                  } else {
                    if (surveyReg != null)
                      addOpenPhysicianSurvey(buttonGrid, surveyReg.getToken());
                  }
                }
              } else {
                if (!completed) {
                  int buttonPos = 0;
                  Long todayL = getUtils().getStartOfDay(new Date());

                  Date today = new Date(todayL);
                  if (registration.getSurveyDt().after(today)) {
                    registration.setSendEmail(true);
                    final Button emailButton = new Button(new Image(RegistryResources.INSTANCE.email_go()).toString());
                    emailButton.addClickHandler(new ClickHandler() {
                      @Override
                      public void onClick(ClickEvent event) {
                        showEmailPopup(patient, registration);
                      }
                    });
                    emailButton.setTitle("Send Email");
                    buttonPos += 1;
                    buttonGrid.setWidget(0, buttonPos, emailButton);
                    buttonGrid.getColumnFormatter().setWidth(buttonPos, "50px");
                  }
                  //if surveyDate is 30 days before today
//                  days * 24 * 60 * 60 * 1000
                  Date thirtyDaysBefore = new Date(todayL - (30 *24 * 3600 * 1000L));

                  if (registration.getSurveyDt().after(thirtyDaysBefore)) {
                    final Button extendButton = new Button(
                        new Image(RegistryResources.INSTANCE.calendarEdit()).toString());
                    extendButton.addClickHandler(new ClickHandler() {
                      @Override
                      public void onClick(ClickEvent event) {
                        showExtendPopup(patient, registration);

                      }
                    });
                    extendButton.setTitle("Change Survey Date");
                    buttonPos += 1;
                    buttonGrid.setWidget(0, buttonPos, extendButton);
                    buttonGrid.getColumnFormatter().setWidth(buttonPos, "50px");
                  }
                  if (registration.getNumberCompleted() > 0) {
                    final Button reportButton = new Button(
                        new Image(RegistryResources.INSTANCE.report()).toString());
                    reportButton.addClickHandler(new ClickHandler() {
                      @Override
                      public void onClick(ClickEvent event) {
                        String urlString = getUtils().getChartUrl(Constants.ASSESSMENT_ID, registration.getAssessmentRegId().toString(),
                            "height=118&width=297&print=n");
                        logger.log(Level.INFO, "Calling url: " + urlString);
                        Window.open(urlString, "Pdf", "");
                      }
                    });
                    reportButton.setTitle("Open partial pdf report");
                    buttonPos += 1;
                    buttonGrid.setWidget(0, buttonPos, reportButton);
                    buttonGrid.getColumnFormatter().setWidth(buttonPos, "50px");
                    if (buttonPos > 0) {
                      buttonGrid.getColumnFormatter().setWidth(buttonPos, "150px");
                    }
                  }
                } else if (surveyReg != null) {
                  final Button reportButton = new Button(
                      new Image(RegistryResources.INSTANCE.report()).toString());
                  reportButton.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                      String urlString = getUtils().getChartUrl(Constants.ASSESSMENT_ID, registration.getAssessmentRegId().toString(), "height=118&width=297&print=n");
                      logger.log(Level.INFO, "Calling url: " + urlString);
                      Window.open(urlString, "Pdf", "");
                    }
                  });
                  reportButton.setTitle("Open patient pdf report");
                  buttonGrid.setWidget(0, 1, reportButton);
                  buttonGrid.getColumnFormatter().setWidth(1, "50px");
                  // Text report
                  final Button textButton = new Button(
                      new Image(RegistryResources.INSTANCE.page()).toString());
                  textButton.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                      String urlString = getUtils().getChartUrl(Constants.ASSESSMENT_ID, registration.getAssessmentRegId().toString(), "print=n&txt=y");
                      Window.open(urlString, "Text", "");
                    }
                  });
                  textButton.setTitle("Open patient text report");
                  buttonGrid.setWidget(0, 2, textButton);
                  buttonGrid.getColumnFormatter().setWidth(2, "150px");
                }
              }

              patientActivityTable.setWidget(row, 1, buttonGrid);
            } else {
              patientActivityTable.setWidget(row, 1, emptyLabel);
            }
            patientActivityTable.getRowFormatter().addStyleName(row, lineStyle);
            if (lineStyle.equals(ODD)) {
              lineStyle = EVEN;
            } else {
              lineStyle = ODD;
            }
          }

          patientActivityTable.setVisible(true);
        }
      }
    });
  }

  private void refreshPatient() {
    refreshPatient(null);
  }

  private void refreshPatient(String msg) {
    if (patient == null) {
      patientLabel.setVisible(true);
    } else {
      if (getUtils().getClientConfig().getCustomPatientTabs(getUtils().getUser()) != null
          && getUtils().getClientConfig().getCustomPatientTabs(getUtils().getUser()).size() > 0) {
        makeTabs();
      }
      patientLabel.setVisible(false);
    }

    fillPatientHeader(patient);
    fillPatientActivityTable(patient, msg);
    fillBottomButtonPanel(patient);
    if (msg != null) {
      setSuccessMessage(msg);
    }
    enablePatientData();
  }

  private void fillBottomButtonPanel(final Patient pat) {
    buttonPanel.clear();
    if (pat == null) {
      return;
    }

    final Button registrationsButton = new Button(
        new Image(RegistryResources.INSTANCE.calendar()).toString() + "Survey List");
    registrationsButton.addStyleName(css.iconTextButton());
    registrationsButton.setTitle("View patient's survey registrations");
    registrationsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        patientAppts.popUpPatientRegistrations(pat.getPatientId());
      }
    });

    buttonPanel.add(registrationsButton);

    /**
     * Add button to open the pdf to view/save/print with the adobe controls
     */
    final Button openButton = new Button(new Image(RegistryResources.INSTANCE.report()).toString() + " Patient Report");
    openButton.addStyleName(css.iconTextButton());
    openButton.setTitle("Open Patient Report pdf");
    openButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        PatientRegistrationSearch patientRegistrationSearch = new PatientRegistrationSearch();
        clinicService.searchForPatientRegistration(pat.getPatientId(), patientRegistrationSearch,
            new AsyncCallback<ArrayList<PatientRegistration>>() {
          @Override
          public void onFailure(Throwable caught) {
            logger.log(Level.SEVERE,
                "Failure calling searchForPatientRegistration. Caught " + caught.getMessage(), caught);
            HTML serverResponseLabel = new HTML("The following error occurred when getting patient registration: <P> "
                + caught.getMessage());
            serverResponseLabel.addStyleName(RegistryResources.INSTANCE.css().serverResponseLabelError());
          }

          @Override
          public void onSuccess(ArrayList<PatientRegistration> result) {
            // Get the AssessmentId for the last appointment which has a completed study
            AssessmentId assessmentId = null;
            if (result != null) {
              Date lastSurveyDt = null;
              for (PatientRegistration patReg : result) {
                if (patReg.getNumberCompleted() > 0) {
                  Date surveyDt = patReg.getSurveyDt();
                  if ((lastSurveyDt == null) || surveyDt.after(lastSurveyDt)) {
                    lastSurveyDt = surveyDt;
                    assessmentId = patReg.getAssessmentId();
                  }
                }
              }
            }
            // then request printing the last report
            if (assessmentId != null) {
              String urlString = getUtils().getChartUrl(Constants.ASSESSMENT_ID, assessmentId.toString(), "height=118&width=297&print=n");
              logger.log(Level.INFO, "Calling url: " + urlString);
              Window.open(urlString, "Open", "");
            } else {
              setErrorMessage("No data available for the report");
            }
          }
        });
      }
    });

    buttonPanel.add(openButton);

    final Button refreshButton = new Button("Refresh");

    refreshButton.addClickHandler(new ClickHandler() { // make it get the server's copy of the patient
      @Override
      public void onClick(ClickEvent event) {
        clinicService.getPatient(patient.getPatientId(), new AsyncCallback<Patient>() {
          @Override
          public void onFailure(Throwable caught) {
            setErrorMessage("Encountered the following error refreshing patient information.");
          }

          @Override
          public void onSuccess(Patient result) {
            updatePatient(result);
          };
        });
      }
    });
    buttonPanel.add(refreshButton);
  }


  // Similar to clientUtils.getDeclineReasonPanel, but with just 1 button
  // Not sure why we can't show HandleRegister's mini-registration popup...
  private void showDeclinePopup() {
    final Popup declinePopup = new Popup("Decline assessments");
    declinePopup.setModal(false);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(css.quickDeclinePanel());

    new DeclinePopup(clinicService, RegistryResources.INSTANCE.css()) {
      @Override
      protected void declineSuccessHandler(Patient result) {
        updatePatient(result);
      }
    }.addRestOfReasonsForDecliningPopup(patient, declinePopup, panel);
  }


  private void setPatientControlButtonsPanel(final Patient pat) {
    controlButtonsPanel.clear();
    Grid buttonGrid = new Grid(4,1);
    buttonGrid.setCellPadding(20);
    int gridInx = 1;
    if (pat.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
      if (pat.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y")) {
        buttonGrid.setWidget(0, 0, registrationButton("Registered", RegistryResources.INSTANCE.accept(), false, true));
        formatHeaderButtonWidget(buttonGrid, 0, 0);
        addAssessmentsButtons(buttonGrid, patient);
        gridInx = 3;
      } else { //  answered "n"
        DeclineReason reason = HandleRegister.getDeclineReasonFromCode(pat);
        String declineReason = HandleRegister.getDeclinedReason(pat, reason, true);
        buttonGrid.setWidget(0, 0, registrationButton("Declined" + declineReason,
            RegistryResources.INSTANCE.decline(), true, false));
        formatHeaderButtonWidget(buttonGrid, 0, 0);
      }
    } else {
      buttonGrid.setWidget(0, 0, registrationButton("Register or decline...", null, true, true));
      formatHeaderButtonWidget(buttonGrid, 0, 0);
    }
    if (getUtils().isPhysician() && physicianService != null) {
      buttonGrid.setWidget(gridInx,0,physSurveyButton("Enter Patient Data", RegistryResources.INSTANCE.accept(), pat.getPatientId()));
      formatHeaderButtonWidget(buttonGrid, gridInx, 0);
    }
    controlButtonsPanel.add(buttonGrid);
  }


  private PopdownButton registrationButton(String text, ImageResource icon, final boolean showRegister,
      final boolean showDecline) {

    final HandleRegister regHandler = new HandleRegister(logger, getUtils(), clinicService, false) {
      @Override
      public void successOnSetRegisteredOrDeclined(Patient result) {
        updatePatient(result);
      }
    };

    return new PopdownButton().withText(text).withIcon(icon).align(Align.BELOW_RIGHT).withMenu(new Customizer() {
      @Override
      public void customizePopup(PopdownButton button, Menu menu) {
        if (showRegister) {
          menu.addItem("Register this patient", new Command() {
            @Override
            public void execute() {
              regHandler.doRegister(patient);
            }
          });
        }
        if (showDecline) {
          menu.addItem("Decline this patient", new Command() {
            @Override
            public void execute() {
              showDeclinePopup();
            }
          });
        }
      }
    })./*withStyle(css.registerDeclineButton()).*/addMenuStyle(css.registerDeclineMenu());
  }


  private PhysicianServiceAsync createPhysicianService() {
    PhysicianServiceAsync physService = GWT.create(PhysicianService.class);
    setServiceEntryPoint(physService, "physicianService", new RegistryRpcRequestBuilder() {
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

    physService.getPhysicianSurveyPath(new Callback<String>() {
      @Override
      public void handleSuccess(String result) {
        physicianSurveyPath = result;
      }
    });
    return physService;
  }

  public String getPatientId() {
    return patient.getPatientId();
  }

  /**
   * Called from the ScheduleWidget. Internal calls should use updatePatient.
   */
  public void setPatient(Patient patient) {
    this.patient = patient;
    refreshPatient();
  }

  private void updatePatient(Patient patient) {
    if (this.patient == null) {
      this.patient = patient;
    } else {
      this.patient.updateFrom(patient);
    }
    refreshPatient();
  }

  public Patient getPatient() {
    return patient;
  }

  @Override
  public void onFailedValidation(InvalidEmailEvent event) {
    if (event != null && event.getValue() != null) {
      //setErrorMessage(event.getValue() + " is not a valid email address");
    }
  }

  private void showEmailPopup(final Patient patient, final ApptRegistration patReg) {
    final Popup emailPopup = getUtils().makePopup("Send Email");
    emailPopup.setModal(false);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());

    panel.add(new Label("Verify patient:"));
    Label name = new Label(patient.getFirstName() + " " + patient.getLastName() + " (DOB: "
        + getUtils().getDefaultDateFormat().format(patient.getDtBirth()) + ")");
    name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(name);
    panel.add(new Label("Verify/correct email address:"));
    final String currentPatientEmail = patient.getEmailAddress();
    final ValidEmailAddress email = getClientUtils().makeEmailField(currentPatientEmail, new InvalidEmailHandler() {
      @Override
      public void onFailedValidation(InvalidEmailEvent event) {
        // nothing to do here - validation is below
      }
    });
    email.setInvalidStyleName("emailTextError");
    email.setValidStyleName("emailTextValid");
    panel.add(email);

    List<Button> buttons = new ArrayList<>();
    final Button emailButton = new Button("Send Email");
    emailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!email.isValid()) {
          return;
        }
        String emailValue = email.getValue();
        if (emailValue == null || emailValue.length() == 0) {
          return;
        }

        emailButton.setEnabled(false);
        if (!emailValue.equals(currentPatientEmail)) {
          getUtils().updatePatientEmail(patient, emailValue, new emailRunnable() {
            @Override
            public void run() {
              sendPatientEmail(patReg, new emailRunnable() {
                @Override
                public void run(EmailSendStatus result) {
                  emailPopup.hide();
                  if (EmailSendStatus.sent.equals(result)) {
                    getUtils().showSentPopup();
                  } else {
                    getUtils().showSentFailedPopup(result, patReg);
                  }
                }
              });
            }
          });
        } else {
          sendPatientEmail(patReg, new emailRunnable() {
            @Override
            public void run(EmailSendStatus result) {
              emailPopup.hide();
              if (EmailSendStatus.sent.equals(result)) {
                getUtils().showSentPopup();
              } else {
                getUtils().showSentFailedPopup(result, patReg);
              }

            }
          });
        }
      }
    });
    emailButton.addStyleName(RegistryResources.INSTANCE.css().defaultButton());
    final Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        emailPopup.hide();
      }
    });

    buttons.add(emailButton);
    buttons.add(cancelButton);

    emailPopup.setCustomButtons(buttons);
    emailPopup.setGlassEnabled(true);
    emailPopup.showMessage(panel);

  }


  private void showExtendPopup(final Patient patient, final ApptRegistration patReg) {
    final Popup extendPopup = getUtils().makePopup("Change Survey Date");
    extendPopup.setModal(false);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
    panel.add(new Label("New Date:"));
    final TextBoxDatePicker extend = DisplayUtils.makelistDateField(getUtils());
    // 7 days in MILISECONDS
    String dayString = getClientConfig().getParam("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    Date daysAfter = new Date(new Date().getTime() + days * 24 * 60 * 60 * 1000);
    extend.setValue(daysAfter);
    panel.add(extend);

    List<Button> buttons = new ArrayList<>();
    final Button extendButton = new Button("Change Survey Date");
    extendButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Date newValue = extend.getValue();
        if (newValue == null || !newValue.after(new Date())) {
          return;
        }
        extendButton.setEnabled(false);
        clinicService.extendRegistration(patReg, newValue, new Callback<Void>() {
          @Override
          public void handleSuccess(Void result) {
            extendPopup.hide();
            refreshPatient();
          }

      });
        }
    });
    extendButton.addStyleName(RegistryResources.INSTANCE.css().defaultButton());
    final Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        extendPopup.hide();
      }
    });

    buttons.add(extendButton);
    buttons.add(cancelButton);

    extendPopup.setCustomButtons(buttons);
    extendPopup.setGlassEnabled(true);
    extendPopup.showMessage(panel);

  }


  private void sendPatientEmail(final ApptRegistration patReg, final emailRunnable afterDone) {
    patReg.setSendEmail(true);
    clinicService.sendEmail(patReg, new Callback<EmailSendStatus>() {
      @Override
      public void handleSuccess(EmailSendStatus result) {
        afterDone.run(result);
      }
    });
  }

  private void showStartSurveyPopup(final Patient pat, String heading, String selectedType, final boolean sendEmail,
                                    final boolean showPopup) {
    final Popup startPopup = getUtils().makePopup(heading);
    startPopup.setModal(false);
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
    panel.add(new Label("Verify patient:"));
    Label name = new Label(pat.getFirstName() + " " + pat.getLastName() + " (DOB: "
        + getUtils().getDefaultDateFormat().format(pat.getDtBirth()) + ")");
    name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(name);
    ///
    FlexTable choiceTable = new FlexTable();
    Label typeLabel = new Label("Assessment type:");
    typeLabel.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    choiceTable.setWidget(0, 0, typeLabel);
    Label dateLabel = new Label("Survey Date:");
    dateLabel.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    choiceTable.setWidget(0, 1, dateLabel);
    choiceTable.getCellFormatter().setWidth(0, 0, "150px");
    choiceTable.getCellFormatter().setWidth(0, 1, "150px");

    final ListBox surveyType = getUtils().getProcessTypeListBox();
    if (selectedType != null) {
      for (int s=0; s<surveyType.getItemCount(); s++) {
        if (selectedType.equals(surveyType.getItemText(s))) {
          surveyType.setSelectedIndex(s);
        }
      }
    }
    surveyType.setWidth("140px");
    choiceTable.setWidget(1, 0, surveyType);

    final TextBoxDatePicker addDate = DisplayUtils.makelistDateField(getUtils());
    addDate.setStyleName(RegistryResources.INSTANCE.css().leftLabel());
    addDate.getElement().setAttribute("style", "width: 130px; padding: 6px");

    choiceTable.setWidget(1, 1, addDate);
    int row = 2;
    String warning = getCurrentAssessmentWarning();
    if (warning != null) {
      Label warningLabel = new Label(warning);
      warningLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
      choiceTable.setWidget(row, 0, warningLabel);
      choiceTable.getFlexCellFormatter().setColSpan(row, 0, 2);
      row++;
    }
    final String currentPatientEmail = patient.getEmailAddress();
    final CheckBox emailChkBox = new CheckBox();
    if (sendEmail && currentPatientEmail != null && currentPatientEmail.trim().length() > 0) {
      FlowPanel emailLinkPanel = new FlowPanel();

      warning = null;
      ArrayList<String> warnings = getRecentEmails();
      if (warnings.size() > 0) {
          for (String warningMsg : warnings) {
            warning = "NOTE: This patient was emailed on " + warningMsg;
          }
      }
      if (warning != null) {
        Label warningLabel = new Label(warning);
        warningLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
        choiceTable.setWidget(row, 0, warningLabel);
        choiceTable.getFlexCellFormatter().setColSpan(row, 0, 2);
        row++;
      }
      final Label emailLabel = new Label("Send email now");
      emailLabel.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
      emailLabel.getElement().getStyle().setProperty("float", "left");
      emailLinkPanel.add(emailLabel);
      emailChkBox.setTitle("Send Email Now");
      emailChkBox.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
      emailChkBox.setSize("24px", "24px");
      emailChkBox.setValue(sendEmail);
      emailChkBox.getElement().getStyle().setProperty("float", "left");
      emailLinkPanel.add(emailChkBox);

      choiceTable.setWidget(row, 0, emailLinkPanel);

    }

    panel.add(choiceTable);


    Button createButton = new Button("Ok");
    createButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        startPopup.hide();
        getUtils().showLoadingPopUp();
        doSaveApptRegistration(pat, surveyType.getValue(surveyType.getSelectedIndex()), addDate.getValue(), showPopup,
            emailChkBox.getValue());
      }
    });

    Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        startPopup.hide();
      }
    });

    final List<Button> buttons = new ArrayList<>();
    buttons.add(createButton);
    buttons.add(cancelButton);

    startPopup.setCustomButtons(buttons);
    startPopup.setGlassEnabled(true);
    startPopup.showMessage(panel);
  }

  private boolean doSaveApptRegistration(Patient pat, String registerType, Date surveyDt, final Boolean showPopup,
                                         final Boolean sendEmail) {
    StringBuilder errorMessages = new StringBuilder();
    String mrn = pat.getPatientId();

    boolean participates = pat.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y");
    if (!participates) {
      errorMessages.append("Can not save unless they agree to taking the assessments. \n");
      setErrorMessage(errorMessages.toString());
      return false;
    }

    // create the registration
      Date tm = new Date(getUtils().getEndOfDay(surveyDt));
      String registrationType = Constants.REGISTRATION_TYPE_STANDALONE_SURVEY;

      String visitType = getUtils().getProcessXml().getProcessAttribute(registerType, "visitType");
      String emailAddr = "";
      if (pat.getEmailAddress() != null) {
        emailAddr = pat.getEmailAddress();
      }

      final ApptRegistration registration = new ApptRegistration(getUtils().getSiteId(), mrn, tm,
          emailAddr, registerType, registrationType, visitType);
      registration.setSendEmail(sendEmail);
      clinicService.addPatientRegistration(registration, patient, new Callback<PatientRegistration>() {
        @Override
        public void handleSuccess(final PatientRegistration patReg) {
          patient.updateFrom(patReg.getPatient());     // update the current patient object
          patient.setPatientId(patReg.getPatientId()); // incase it was formatted
          patient.setDtChanged(patReg.getDtChanged());
          patient.setDtCreated(patReg.getDtCreated());
          if (showPopup) {
            showStartSurveyPopup(patReg);
          } else if (sendEmail) {
            sendPatientEmail(patReg, new emailRunnable() {
              @Override
              public void run(EmailSendStatus result) {
                if (EmailSendStatus.sent.equals(result)) {
                  refreshPatient("Email sent");
                } else {
                  getUtils().showSentFailedPopup(result, patReg);
                }
              }
            });
          } else {
            refreshPatient("Created");
          }
          getUtils().hideLoadingPopUp();
        }
      });
    return true;
  }


  private void showStartSurveyPopup(final PatientRegistration patReg) {

    clinicService.getSurveyStartStatus(patReg.getPatient(), patReg.getApptId(), new Callback<SurveyStart>() {
      @Override
      public void handleSuccess(SurveyStart result) {
        final Popup registerPopup = getUtils().makePopup("Start Assessment");

        registerPopup.setModal(false);

        FlowPanel panel = new FlowPanel();
        panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());


        panel.add(new Label("Verify patient:"));
        Label name = new Label(patReg.getFirstName() + " " + patReg.getLastName() + " (DOB: "
            + getUtils().getDefaultDateFormat().format(patReg.getDtBirth()) + ")");
        name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
        panel.add(name);
        panel.add(new Label("Assessment type:"));
        Label type = new Label(patReg.getSurveyType());
        type.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
        panel.add(type);

        if (result.getLevel() == SurveyStart.LEVEL_ERROR || result.getLevel() == SurveyStart.LEVEL_WARN) {
          Label warningLabel = new Label("Warning! " + result.getMessage());
          warningLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
          panel.add(warningLabel);
          panel.add(new Label("To continue on to this survey type this code into the tablet: "));
        } else {
          panel.add(new Label("Type this code into the tablet:"));
        }
        List<SurveyRegistration> surveys = patReg.getSurveyRegList();
        if (surveys.size() > 1) {
          for(SurveyRegistration survey : surveys) {
            Label code = new Label(survey.getSurveyName() + ": " + survey.getToken());
            code.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
            panel.add(code);
          }
        } else {
          SurveyRegistration survey = patReg.getSurveyRegList().get(0);
          Label code = new Label(survey.getToken());
          code.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
          panel.add(code);
        }

        Button cancelButton = new Button("Ok");
        cancelButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            registerPopup.hide();
            refreshPatient();
          }
        });

        Button changeButton = new Button("Change Survey Type");
        changeButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            registerPopup.hide();
            showChangeSurveyPopup(patReg);
          }
        });
        ArrayList<String> warnings = getRecentEmails();
        if (warnings.size() > 0) {
            panel.add(new Label("NOTE: This patient was emailed on"));
            for (String warningMsg : warnings) {
              Label warningLabel = new Label(warningMsg);
              warningLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
              panel.add(warningLabel);
            }
        }
        final List<Button> buttons = new ArrayList<>();
        buttons.add(cancelButton);
        if (patReg.getNumberCompleted() == 0) {
          buttons.add(changeButton);
        }
        final String currentPatientEmail = patReg.getPatient().getEmailAddress();
        if (currentPatientEmail != null && currentPatientEmail.trim().length() > 0) {
          Button emailButton = new Button("Send Email");
          emailButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              sendPatientEmail(patReg, new emailRunnable() {
                @Override
                public void run(EmailSendStatus result) {
                  registerPopup.hide();
                  refreshPatient();
                  if (EmailSendStatus.sent.equals(result)) {
                    getUtils().showSentPopup();
                  } else {
                    getUtils().showSentFailedPopup(result, patReg);
                  }

                }
              });
            }
          });

          buttons.add(emailButton);
        }
        registerPopup.setCustomButtons(buttons);
        registerPopup.setGlassEnabled(true);
        registerPopup.showMessage(panel);
      }
    });
  }

  private void showChangeSurveyPopup(final PatientRegistration patReg) {
    new ChangeSurveyPopup(getUtils(), patReg, clinicService, new Callback<PatientRegistration>() {

      @Override
      public final void handleSuccess(PatientRegistration result) {
        showStartSurveyPopup(patReg);
      }
    });
  }

  private void formatHeaderButtonWidget(Grid grid, int row, int column) {
    grid.getCellFormatter().setHeight(row, column, "32px");
    grid.getCellFormatter().setWidth(row, column, "240px");
    grid.getCellFormatter().setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_LEFT);
    grid.getWidget(row, column).setWidth("100%");
  }
  private ArrayList<String> getRecentEmails() {
    ArrayList<String> messages = new ArrayList<>();
    Long today = getUtils().getStartOfDay(new Date());
    Date twoDaysAgo = new Date(today - (24 * 3600 * 1000 * 2));
    for (PatientActivity patientActivity : activityList) {
      for (Activity activity : patientActivity.getActivities()) {
        if ("Email sent".equals(activity.getActivityType()) && activity.getActivityDt().after(twoDaysAgo)) {
          messages.add(new String(ClientUtils.getDateString(getUtils().getDefaultDateTimeFormat(), activity.getActivityDt())));
        }
      }
    }
    return messages;
  }


  private String getCurrentAssessmentWarning() {
    Long today = getUtils().getStartOfDay(new Date());
    Date twoDaysOut = new Date(today + (24 * 3600 * 1000 * 3));
    for (PatientActivity patientActivity : activityList) {
      if (patientActivity.getRegistration().getSurveyDt().getTime() > today
          && patientActivity.getRegistration().getSurveyDt().before(twoDaysOut)
          && !"".equals(patientActivity.getRegistration().getRegistrationType())) {
        boolean registered = false;
        boolean completed = false;

        for (Activity activity : patientActivity.getActivities()) {
          if ("Registered".equals(activity.getActivityType())) {
            registered = true;
          }
          if ("Completed".equals(activity.getActivityType())) {
            completed = true;
          }

          if (registered) {
            if (!completed) {
              return ("Note: This patient has another assessment due "
                  + ClientUtils.getDateString(getUtils().getDefaultDateFormat(), patientActivity.getRegistration().getSurveyDt()));
            } else {
              return ("Note: This patient has a recent assessment completed on "
                  + ClientUtils.getDateString(getUtils().getDefaultDateFormat(), patientActivity.getRegistration().getSurveyDt()));
            }
          }
        }
      }
    }
    return null;
  }



  static private class emailRunnable implements Runnable {

    @Override
    public void run() {
    }

    public void run(EmailSendStatus i) {
    }

  }

  /*
   * Add create new assessments buttons
   */
  private void addAssessmentsButtons(final Grid buttonGrid, final Patient patient) {
    clinicService.getPatientsLastSurveyDate(patient.getPatientId(), new AsyncCallback<Date>() {
      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage("Encountered the following error getting patient information.");
      }

      @Override
      public void onSuccess(Date result) {
        String visits = "1";
        if (result != null) {
          visits = "2";
        }
        ArrayList<String> processNames = getUtils().getProcessXml().getActiveVisitProcessNames();
        for (final String text : processNames) {
          if (visits.equals(getUtils().getProcessXml().getProcessAttribute(text, "visit"))) {
            Button startAssessButton = new Button("New Assessment in Clinic");
            startAssessButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                showStartSurveyPopup(patient, "New Assessment in Clinic", text, false, true);
              }
            });
            //startAssessButton.addStyleName("mixedButton");
            buttonGrid.setWidget(1, 0, startAssessButton);
            formatHeaderButtonWidget(buttonGrid, 1, 0);

            final String currentPatientEmail = patient.getEmailAddress();
            if (currentPatientEmail != null && currentPatientEmail.trim().length() > 0) {
              Button emailAssessButton = new Button("Email New Assessment");
              emailAssessButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  showStartSurveyPopup(patient, "Email New Assessment", text, true, false);
                }
              });
              //emailAssessButton.addStyleName("mixedButton");
              buttonGrid.setWidget(2, 0, emailAssessButton);
              formatHeaderButtonWidget(buttonGrid, 2, 0);
            }
          }
        }
      }
    });
  }

  PopdownButton enterPatientDataButton;

  /**
   * If this button exists (for physicians), disables it if the patient isn't registered
   */
  private void enablePatientData() {
    if (enterPatientDataButton != null) {
      boolean enable = patient != null && patient.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y");
      enterPatientDataButton.asButton().setEnabled(enable);
      enterPatientDataButton.asButton().setTitle(enable ? "" : "Patient declined or has not registered");
    }
  }

  // The Enter Patient Data button, for physicians
  private PopdownButton physSurveyButton(String text, ImageResource icon, final String patientId) {
    final PopdownButton pb = new PopdownButton().withText(text).withIcon(icon).align(Align.BELOW_LEFT);
    Customizer customizer = new Customizer() {
      @Override
      public void customizePopup(PopdownButton button, final Menu menu) {
        if (getUtils().isPhysician() && physicianService != null) {
          physicianService.getProcessNames(patientId, new Callback<ArrayList<String>>()  {

            @Override
            public void handleSuccess(ArrayList<String> result) {
              for (final String process: result) {
                menu.addItem(process, new Command() {
                  @Override
                  public void execute() {
                    startPhysicianSurvey(patientId, process);
                  }
                });
              }
              // UI TreatmentSet items handled by UI instead of surveys
              if (treatmentSetUI == null)
                return;
              ArrayList<RandomSetParticipant> sets = treatmentSetUI.getNonSurveyTreatmentSetOptions();
              for (RandomSetParticipant item: sets) {
                final String label = item.getName();
                Command cmd = new Command() {
                  @Override
                  public void execute() {
                    treatmentSetUI.assignTreatmentSet(menu, label);
                  }
                };
                menu.addItem(item.getName(), null, item.isEnabled(), cmd).setTitle(item.getDisabledString());
              }
            }
          });

        }
      }
    };
    pb.withMenu(customizer).withStyle(css.registerDeclineButton());
    enterPatientDataButton = pb;
    enablePatientData();
    return pb;
  }

  private void startPhysicianSurvey(String patientId, String surveyType) {
    if (getUtils().isPhysician() && physicianService != null) {
      physicianService.createSurvey(patientId, surveyType, new Callback<String> () {
        @Override
        public void handleSuccess(final String token) {
          openSurvey(token);
        }
      });
    }
  }

  private void openSurvey(final String token) {
    String params = "?s=" + getUtils().getParam("siteName") + "&tk=" + token;
    if (surveyFrame == null) {
      surveyFrame = new Frame(physicianSurveyPath + params);
    } else {
      surveyFrame.setUrl(physicianSurveyPath + params);
    }
    surveyFrame.getElement().setAttribute("style", "width: 600px; height: 500px; padding: 0px; margin: 0px; border: none;");
    surveyFrame.addLoadHandler(new LoadHandler() {

      @Override
      public void onLoad(LoadEvent event) {
        surveyFrame.setVisible(true);
      }
    });
    final Timer surveyTimer = new Timer() {
      int runttimes = 0;

      @Override
      public void run() {
        runttimes++;
        if (runttimes > 100) {  // 5 minutes
          stop();
        } else {
          physicianService.isFinished(token, new Callback<Boolean>() {
            @Override
            protected void afterFailure() {
              setErrorMessage("Adding data failed");
              stop();
            }

            @Override
            public void handleSuccess(Boolean finished) {
              if (finished == null || finished) {
                stop();
                refreshPatient();
              }
            }
          });
        }
      }

      @Override
      public void cancel() {
        super.cancel();
      }

      private void cleanup() {
        if (surveyPopup != null) {
          surveyPopup.close();
          getUtils().hideLoadingPopUp();
        }
      }

      private void stop() {
        cancel();
        cleanup();
      }
    };

    FlowPanel framePanel = new FlowPanel();
    framePanel.addStyleName(css.borderedVPanel());
    framePanel.add(surveyFrame);
    surveyPopup = new PopupRelative(patientsHeaderPanel.getElement(), framePanel, Align.BELOW_CENTER, false,
        new CloseCallback() {
          @Override
          public void afterClose() {
            if (surveyTimer.isRunning()) {
              surveyTimer.cancel();
              refreshPatient();
            }
          }
        });
    surveyTimer.scheduleRepeating(3000);
  }

  private void addOpenPhysicianSurvey(final Grid buttonGrid, final String token) {
    if (!getUtils().isPhysician() || physicianService == null) {
      return;
    }
    final Button showButton = new Button(new Image(RegistryResources.INSTANCE.pageEdit()).toString());
    showButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getUtils().showLoadingPopUp();
        openSurvey(token);
      }
    });
    showButton.setTitle("Enter values for this patient");
    buttonGrid.setWidget(0, 1, showButton);
    buttonGrid.getColumnFormatter().setWidth(1, "50px");
  }

  private void addPhysicianDataReport(final Grid buttonGrid, final ApptId apptId) {
    if (!getUtils().isPhysician() || physicianService == null) {
      return;
    }

    final Button showButton = new Button(new Image(RegistryResources.INSTANCE.page()).toString());
    showButton.setTitle("Show the values that were entered for this patient");
    showButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getUtils().showLoadingPopUp();
        physicianService.getSurveyJson(apptId, new Callback<String> () {
          @Override
          protected void afterFailure() {
            setErrorMessage("The call failed on the server");
            getUtils().hideLoadingPopUp();
          }

          @Override
          public void handleSuccess(String json) {
            if (json == null || json.trim().isEmpty()) {
              setErrorMessage("The entry has no data");
              return;
            }
            JSONValue jsonValue = JSONParser.parseStrict(json);
            // make the display window
            HorizontalPanel header = new HorizontalPanel();
            header.setWidth("100%");
            final SurveyReport surveyReport = AutoBeanCodex.decode(factory, SurveyReport.class, jsonValue.toString()).as();
            int age = 0;
            try {
              age = Integer.parseInt(surveyReport.getAge());
            } catch (NumberFormatException nfe) {
              // leave it as 0
            }
            Panel patientIdentification = new PatientIdentification(getUtils(), surveyReport.getLastNameFirst(),
                              surveyReport.getMRN(), age, surveyReport.getGender().toString(), surveyReport.getDob());
            patientIdentification.setWidth("700px");
            header.add(patientIdentification);

            final Button closeButton = new Button(new Image(RegistryResources.INSTANCE.close()).toString());
            //closeButton.addStyleName("mixedButton");
            closeButton.setStyleName(css.rightButton());
            closeButton.setTitle("Close");

            FlowPanel reportPanel = new FlowPanel();
            reportPanel.addStyleName(css.patientHeaderPanel());
            reportPanel.add(header);
            List<SurveyReportStudy> studies = surveyReport.getStudies();

            if (studies != null && studies.size() > 0) {
              doReport(reportPanel, studies);
            } else {
              Label headerLabel = new Label("ERROR");
              headerLabel.addStyleName(RegistryResources.INSTANCE.css().popupBox());
              headerLabel.addStyleName(RegistryResources.INSTANCE.css().heading());
              header.add(headerLabel);
              reportPanel.add(new Label("no data"));
            }
            reportPanel.addStyleName(css.borderedVPanel());
            final PopupRelative physicianDataReportPopup = new PopupRelative(mainPanel.getWidget(2).getElement(), reportPanel, Align.COVER_CENTER, new CloseCallback() {
              @Override
              public void afterClose() {
              }
            });

            closeButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                physicianDataReportPopup.close();
              }
            });
            header.add(closeButton);
            reportPanel.setWidth("100%");
            reportPanel.addStyleName(css.datalist());
            getUtils().hideLoadingPopUp();
          }
        });
      }
    });
    buttonGrid.setWidget(0, 1, showButton);
    buttonGrid.getColumnFormatter().setWidth(1, "50px");
  }

  private void doReport(FlowPanel reportPanel, List<SurveyReportStudy> studies) {
    for (SurveyReportStudy study : studies) {
      if (study != null && study.getSurveyStudyObj() != null) {
        VerticalPanel studyPanel = new VerticalPanel();
        studyPanel.addStyleName(css.datalist());
        studyPanel.setWidth("100%");
        Heading studyTitle  = new Heading(HeadingSize.H3, study.getSectionHeading());
        studyTitle.addStyleName(css.patientReportHeader());
        studyTitle.setWidth("100%");
        studyPanel.add(studyTitle);
        Heading blankLine = new Heading(HeadingSize.H2, "");
        studyPanel.add(blankLine);
        SurveyStudyObj studyObj = study.getSurveyStudyObj();

        for (SurveyStudyStepObj step : studyObj.getSteps()) {
          PanelHeader questionPanelHeader = new PanelHeader();
          org.gwtbootstrap3.client.ui.Panel questionPanel = new org.gwtbootstrap3.client.ui.Panel();
          PanelBody questionPanelBody = new PanelBody();
          questionPanel.add(questionPanelHeader);
          questionPanel.add(questionPanelBody);
          studyPanel.add(questionPanel);
          if (step.getQuestion() != null) {
            addQuestionLabel(questionPanelHeader, step.getQuestion().getTitle1());
            addQuestionLabel(questionPanelHeader, step.getQuestion().getTitle2());
            for (FormField questionField : step.getQuestion().getFields()) { // Each question
              if (FieldType.heading.equals(questionField.getType())) {
                questionPanel = new org.gwtbootstrap3.client.ui.Panel();
                questionPanelHeader = new PanelHeader();
                addQuestionLabel(questionPanelHeader, questionField.getLabel());
                questionPanel.add(questionPanelHeader);
                questionPanelBody = new PanelBody();
                questionPanel.add(questionPanelBody);
                studyPanel.add(questionPanel);
              }
              String id = questionField.getFieldId(); // question FieldId
              if (questionField.getType().equals(FieldType.radios) || questionField.getType().equals(FieldType.checkboxes)) {
                UnorderedList unorderedList = new UnorderedList();
                for (FormFieldAnswer fieldAnswer : step.getAnswer().getFieldAnswers()) {
                  if (id != null && id.equals(fieldAnswer.getFieldId())) {
                    for (String choice : fieldAnswer.getChoice()) {
                      if (choice != null) {
                        for (FormFieldValue formFieldValue : questionField.getValues()) {
                          if (formFieldValue.getId().equals(choice)) {
                            addListItem(unorderedList, formFieldValue.getLabel());
                          }
                        }
                      }
                    }
                  }
                  questionPanelBody.add(unorderedList);
                }
              } else if (questionField.getType().equals(FieldType.text) ||
                  questionField.getType().equals(FieldType.textArea) ||
                  questionField.getType().equals(FieldType.numericScale) ||
                  questionField.getType().equals(FieldType.number) ||
                  questionField.getType().equals(FieldType.numericSlider)) {
                Row row = new Row();
                ColumnSize useSize = ColumnSize.LG_12;
                if (questionField.getLabel() != null && !questionField.getLabel().isEmpty()) {
                  row.add(addTextToColumn(questionField.getLabel(), ColumnSize.LG_3));
                  useSize = ColumnSize.LG_9;
                }
                for (FormFieldAnswer fieldAnswer : step.getAnswer().getFieldAnswers()) {
                  if (id != null && id.equals(fieldAnswer.getFieldId())) {
                    for (String choice : fieldAnswer.getChoice()) {
                      Column ansColumn = new Column(useSize);
                      ansColumn.add(addListItem(null, choice));
                      row.add(ansColumn);
                    }
                  }
                }
                questionPanelBody.add(row);
              } else if (questionField.getType().equals(FieldType.datePicker)) {
                for (FormFieldAnswer fieldAnswer : step.getAnswer().getFieldAnswers()) {
                  if (id != null && id.equals(fieldAnswer.getFieldId()) && fieldAnswer.getChoice() != null
                      && fieldAnswer.getChoice().size() > 0) {
                    questionPanelBody.add(addListItem(null, fieldAnswer.getChoice().get(0)));
                  }
                }
              } else if (questionField.getType().equals(FieldType.textBoxSet)) {
                Form form = new Form(FormType.HORIZONTAL);
                for (FormFieldAnswer fieldAnswer : step.getAnswer().getFieldAnswers()) {
                  if (id != null && id.equals(fieldAnswer.getFieldId())) {
                    int choiceIndex = 0;
                    for (String choice : fieldAnswer.getChoice()) {
                      if (choice != null) {
                        Row row = new Row();
                        ColumnSize useSize = ColumnSize.LG_12;
                        if (questionField.getValues().size() > choiceIndex) {
                          row.add(addTextToColumn(questionField.getValues().get(choiceIndex).getLabel(), ColumnSize.LG_4));
                          useSize = ColumnSize.LG_6;
                        }
                        Column ansColumn = new Column(useSize);
                        ansColumn.add(addListItem(null, choice));
                        row.add(ansColumn);
                        questionPanelBody.add(row);
                      }
                      choiceIndex++;
                    }
                  }
                }
                questionPanelBody.add(form);
              } else if (questionField.getType().equals(FieldType.radioSetGrid)){
                // Get the answers
                for (FormFieldAnswer fieldAnswer : step.getAnswer().getFieldAnswers()) {
                  if (id != null && id.equals(fieldAnswer.getFieldId())) {
                    int choiceIndex = 0;
                    for (String choice : fieldAnswer.getChoice()) {
                      if (choice != null) {
                        Row row = new Row();
                        ColumnSize useSize = ColumnSize.LG_12;
                        if (questionField.getValues().size() > choiceIndex) {
                          row.add(addTextToColumn(questionField.getValues().get(choiceIndex).getLabel(), ColumnSize.LG_4));
                          useSize = ColumnSize.LG_6;
                        }
                        Column ansColumn = new Column(useSize);
                        ansColumn.add(addListItem(null, choice));
                        row.add(ansColumn);
                        questionPanelBody.add(row);
                      }
                      choiceIndex++;
                    }
                  }
                }

              }
            }
          }
          if (step.getBodyMapQuestion() != null) {
            addQuestionLabel(questionPanelHeader, step.getBodyMapQuestion().getTitle1());
            addQuestionLabel(questionPanelHeader, step.getBodyMapQuestion().getTitle2());
            if (step.getBodyMapAnswer() != null) {
              Row row = new Row();
              row.add(addTextToColumn(" Body map areas selected", ColumnSize.LG_4));
              Column ansColumn = new Column(ColumnSize.LG_6);
              ansColumn.add(addListItem(null, step.getBodyMapAnswer().getRegionsCsv()));
              row.add(ansColumn);
              questionPanelBody.add(row);
            }
          }
          if (step.getRadiosetQuestion() != null) {
            addQuestionLabel(questionPanelHeader, step.getRadiosetQuestion().getTitle1());
            addQuestionLabel(questionPanelHeader, step.getRadiosetQuestion().getTitle2());
            if (step.getRadiosetAnswer() != null) {
              int selection = Integer.parseInt(step.getRadiosetAnswer().getChoice());
              questionPanelBody.add(addListItem(null, step.getQuestion().getFields().get( selection).getLabel()));
            }
          }
          if (step.getSliderQuestion() != null) {
            addQuestionLabel(questionPanelHeader, step.getSliderQuestion().getTitle1());
            addQuestionLabel(questionPanelHeader, step.getSliderQuestion().getTitle2());
            if (step.getNumericAnswer() != null) {
              questionPanelBody.add(addListItem(null, String.valueOf(step.getNumericAnswer().getChoice())));
            }
          }
          if (step.getTextInputQuestion() != null) {
            addQuestionLabel(questionPanelHeader, step.getTextInputQuestion().getTitle1());
            addQuestionLabel(questionPanelHeader, step.getTextInputQuestion().getTitle2());
            if (step.getTextInputAnswer() != null) {
              questionPanelBody.add(addListItem(null, step.getTextInputAnswer().toString()));
            }
          }

        }
        reportPanel.add(studyPanel);
      }
    }
  }

  private void addQuestionLabel(Panel panel, String string) {
    if (string != null) {
      HTML html = new HTML(string);
      panel.add(html);
    }
  }

  private UnorderedList addListItem(UnorderedList list, String itemLabel) {
    AnchorListItem item = new AnchorListItem(itemLabel);
    if (list == null) {
      list = new UnorderedList();
    }
    list.add(item);
    return list;
  }

  private Column addTextToColumn(String text, ColumnSize size) {
    Column column = new Column(size);
    column.add(new HTML(text));
    return column;
  }
}

