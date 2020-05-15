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

import edu.stanford.registry.client.CustomCheckBox;
import edu.stanford.registry.client.CustomListBox;
import edu.stanford.registry.client.CustomTextBox;
import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.PatientButton;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.TimePicker;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.ValidPatientId;
import edu.stanford.registry.client.api.ClinicServicePreferences;
import edu.stanford.registry.client.api.ClinicServicesPreferencesFactory;
import edu.stanford.registry.client.api.FilteredProviders;
import edu.stanford.registry.client.clinictabs.AssessmentActivityWidget.ShowPatientCallback;
import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.event.InvalidPatientEvent;
import edu.stanford.registry.client.event.InvalidPatientHandler;
import edu.stanford.registry.client.event.ValidPatientEvent;
import edu.stanford.registry.client.event.ValidPatientHandler;
import edu.stanford.registry.client.service.AppointmentStatus;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.utils.DeclinePopup;
import edu.stanford.registry.client.utils.DisplayUtils;
import edu.stanford.registry.client.widgets.CancelPopup;
import edu.stanford.registry.client.widgets.ChangeSurveyPopup;
import edu.stanford.registry.client.widgets.Menu;
import edu.stanford.registry.client.widgets.PopdownAny;
import edu.stanford.registry.client.widgets.PopdownAny.Customizer;
import edu.stanford.registry.client.widgets.PopdownButton;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.client.widgets.PopupRelative;
import edu.stanford.registry.client.widgets.PopupRelative.Align;
import edu.stanford.registry.client.widgets.PopupRelative.CloseCallback;
import edu.stanford.registry.shared.ApptAction;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.MenuDef;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveyStart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class ScheduleWidget extends TabWidget implements ClickHandler {
  private static final DateTimeFormat fmtTime = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm a");
  private static final DateTimeFormat fmtMonthDay = DateTimeFormat.getFormat("MM/dd");

  private static final String ALL = "All";
  private static final String APPOINTMENT = "Appointment";
  private static final String SURVEY = "Assessment";

  /*
   * Images from resources
   */
  private static final Image printerImage = new Image(RegistryResources.INSTANCE.printer());
  private static final Image emailImage = new Image(RegistryResources.INSTANCE.email_go());
  private static final Image loadingImage = new Image(RegistryResources.INSTANCE.loadingImage());
  private static final String PRINT_BUTTON = "<img src='" + printerImage.getUrl() + "' />";

  private static final String PRINTING_BUTTON = "<img src='" +loadingImage.getUrl() + "' /> Printing";

  public static final String ACTIVITY_TYPE = "ActivityType";
  public static final String SURVEY_TYPE = "SurveyType";

  private final DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);

  private final HorizontalPanel searchPanel = new HorizontalPanel();
  private final HorizontalPanel searchBar = new HorizontalPanel();

  private final DockLayoutPanel subPanel = new DockLayoutPanel(Unit.EM);
  private final VerticalPanel addPanel = new VerticalPanel();

  // Scheduled surveys list page Buttons
  private final Button todaySearchButton = new Button("Today");
  private final Button weekSearchButton = new Button("Next 7 Days");
  private final Button searchButton = new Button("Refresh");
  private final Button newAppointmentButton = new Button("New Appointment");
  private final Button newSurveyButton = new Button("New Assessment");
  private final Button printSurveysButton = new Button(PRINT_BUTTON);

  // New appointment/registration page buttons
  private final Button addlistContinueButton = new Button(
      new Image(RegistryResources.INSTANCE.save()).toString() + " Save and continue");
  private final Button addlistCloseButton = new Button(
      new Image(RegistryResources.INSTANCE.save()).toString() + " Save and close");
  private final Button addlistCancelButton = new Button(
      new Image(RegistryResources.INSTANCE.close()).toString() + " Close");

  // New survey registration buttons
  private final Button addSurveyContinueButton = new Button(
      new Image(RegistryResources.INSTANCE.save()).toString() + " Save and continue");
  private final Button addSurveyCloseButton = new Button(
      new Image(RegistryResources.INSTANCE.save()).toString() + " Save and close");
  private final Button addSurveyCancelButton = new Button(
      new Image(RegistryResources.INSTANCE.close()).toString() + " Close");

  private final DialogBox addPatientDialogBox = new DialogBox();
  // search bar components
  private final Label showFromLabel = new Label("From");
  private final Label showToLabel = new Label("To");

  private TextBoxDatePicker showFromPicker;
  private TextBoxDatePicker showToPicker;
  private Date fromDt;
  private Date toDt;
  private final CheckBox registeredChkBox = new CheckBox();
  private final CheckBox cancelledChkBox = new CheckBox();
  private final CheckBox printedChkBox = new CheckBox();
  private final CheckBox notPrintedChkBox = new CheckBox();
  private String clinicFilter = null;
  private ClinicServicePreferences preferences = null;
  private String displayVersion = "0";

  private final HTML consentedLabel = new HTML("Registered");
  private final HTML cancelledLabel = new HTML("Cancelled");
  private final HTML printedLabel = new HTML("Printed");
  private final HTML notPrintedLabel = new HTML("Not printed");

  private final Image completedImage = new Image(RegistryResources.INSTANCE.accept());
  private final Image scoresImage = new Image(RegistryResources.INSTANCE.detail());
  private final Image deleteImage = new Image(RegistryResources.INSTANCE.delete());

  // display details page
  private final CustomListBox displayListTypes = new CustomListBox();
  private final DataGrid<PatientRegistration> patientsTable = new DataGrid<>();
  private List<PatientRegistration> registrationsFiltered;
  private ArrayList<PatientRegistration> registrationsUnfiltered;
  private final HashMap<String, Boolean> multipleRegistrations = new HashMap<>();
  private List<DisplayProvider> providers;
  private Map<Long, Boolean> providerStatus = new HashMap<>();

  // new schedule components
  private TextBoxDatePicker addDate;
  private TimePicker addlistTime;
  private ListBox addClinic;
  private CustomTextBox addFirstName;
  private CustomTextBox addLastName;
  private TextBoxDatePicker addDob;
  private ValidPatientId addMrnTextBox;
  private ValidEmailAddress addEmailTextBox;
  private ListBox addSurveyType;
  private CustomCheckBox addAgreesChkBox;
  private Label agreesLabel;
  private final PatientButton addDeclineButton = new PatientButton("Declined assessments");

  // Common components

  private final ClinicServiceAsync clinicService;
  private final ShowPatientCallback showPatientCallback;
  private final Logger logger = Logger.getLogger(ScheduleWidget.class.getName());
  private HorizontalPanel buttonPanel;
  private final HorizontalPanel pdfPanel = new HorizontalPanel();
  private Label totalPatients;
  private Label toEnroll;
  private Label toStart;
  private Label toFinish;
  private Label toPrint;
  private Label done;
  private PopdownButton filterButton;
  private PopdownButton providerFilterButton;

  @SuppressWarnings("unused")
  private PopdownButton clinicFilterButton;

  private boolean showAssessments;
  private boolean showAppointments = true;
  private boolean hideCompleted = true;
  private boolean hideNotCompleted;
  private boolean hideEnrolled;
  private boolean hideDeclined;
  private boolean hidePrinted;
  private ClinicServicesPreferencesFactory servicesPreferencesFactory = GWT.create(ClinicServicesPreferencesFactory.class);
  private MenuDefBeanFactory menuDefBeanFactory = GWT.create(MenuDefBeanFactory.class);
  private MenuDefIntfUtils menuDefIntfUtils = new MenuDefIntfUtils();
  public ScheduleWidget(ClinicUtils clinicUtils, ClinicServiceAsync clinicService, ShowPatientCallback showPatientCallback) {
    super(clinicUtils);
    this.clinicService = clinicService;
    this.showPatientCallback = showPatientCallback;
    initWidget(mainPanel);
  }

  @Override
  public void load() {
    if (isLoaded()) return;
    // Initialize the panels and other components.
    // Make the message panel
    setEmptyMessage();
    getPreferences();
    mainPanel.addNorth(getMessageBar(), 2);
    showFromPicker = new TextBoxDatePicker(getUtils().getDefaultDateFormat());
    showFromPicker.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (showFromPicker.getValue().after(showToPicker.getValue())) showToPicker.setValue(showFromPicker.getValue());
      }
    });
    showToPicker = new TextBoxDatePicker(getUtils().getDefaultDateFormat());

    searchButton.setTitle("Run the search");
    todaySearchButton.setTitle("Set the search to todays date");
    weekSearchButton.setTitle("Set the search dates to one week");

    showFromLabel.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarLabel());
    showToLabel.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarLabel());
    showFromPicker.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarText());
    showFromPicker.addStyleName(RegistryResources.INSTANCE.css().datePickerText());
    showToPicker.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarText());
    showToPicker.addStyleName(RegistryResources.INSTANCE.css().datePickerText());
    displayListTypes.setStylePrimaryName(RegistryResources.INSTANCE.css().clTabPgHeadingBarList());

    registeredChkBox.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarChkBox());
    cancelledChkBox.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarChkBox());
    printedChkBox.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarChkBox());
    notPrintedChkBox.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBarChkBox());

    registeredChkBox.setTitle("Show only patients who have registered");
    cancelledChkBox.setTitle("Include cancelled appointments");
    printedChkBox.setTitle("Include ones that have already been printed");
    notPrintedChkBox.setTitle("Include ones that have NOT been printed yet");

    setSearchBarStyles(consentedLabel);
    setSearchBarStyles(printedLabel);
    setSearchBarStyles(notPrintedLabel);
    setSearchBarStyles(cancelledLabel);

    registeredChkBox.setSize("24px", "24px");
    cancelledChkBox.setSize("24px", "24px");
    printedChkBox.setSize("24px", "24px");
    notPrintedChkBox.setSize("24px", "24px");

    createFilterButton();
    showFromLabel.setVisible(false);
    showFromPicker.setVisible(false);
    showToLabel.setVisible(false);
    showToPicker.setVisible(false);
    createproviderFilterButton();

    searchBar.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    searchBar.add(filterButton);
    searchBar.add(showFromLabel);
    searchBar.add(showFromPicker);
    searchBar.add(showToLabel);
    searchBar.add(showToPicker);
    searchBar.add(providerFilterButton);
    searchBar.add(searchButton);
    searchBar.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingSearchBar());
    searchBar.addStyleName(RegistryResources.INSTANCE.css().registrationSearchBar());

    searchPanel.setWidth("100%");
    searchPanel.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingBar());
    searchPanel.addStyleName(RegistryResources.INSTANCE.css().scheduleHeadingBar());
    searchPanel.add(searchBar);

    HorizontalPanel newBar = new HorizontalPanel();
    newBar.addStyleName(RegistryResources.INSTANCE.css().clTabPgHeadingButtonBar());
    newBar.addStyleName(RegistryResources.INSTANCE.css().registrationSearchBar());

    newAppointmentButton.setTitle("Create a new appointment");
    newSurveyButton.setTitle("Create a standalone assessment (no appointment)");
    newBar.add(newAppointmentButton);
    newBar.add(newSurveyButton);
    searchPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    searchPanel.add(newBar);
    subPanel.addNorth(searchPanel, 4.2);
    mainPanel.add(subPanel);

    Column<PatientRegistration, String> mrnCol = new Column<PatientRegistration, String>(new ClickableTextCell()) {
      @Override
      public String getValue(PatientRegistration registration) {
        return registration.getPatientId();
      }
    };
    mrnCol.setFieldUpdater(new FieldUpdater<PatientRegistration, String>() {
      @Override
      public void update(int index, PatientRegistration registration, String value) {
        showPatientCallback.showPatient(registration.getPatient());
      }
    });
    mrnCol.setSortable(true);
    mrnCol.setDataStoreName("mrn");
    patientsTable.addColumn(mrnCol, getPatientIdLabel());
    Column<PatientRegistration, String> firstNameCol = new Column<PatientRegistration, String>(new ClickableTextCell()) {
      @Override
      public String getValue(PatientRegistration registration) {
        return registration.getFirstName();
      }
    };
    firstNameCol.setFieldUpdater(new FieldUpdater<PatientRegistration, String>() {
      @Override
      public void update(int index, PatientRegistration registration, String value) {
        showPatientCallback.showPatient(registration.getPatient());
      }
    });
    firstNameCol.setSortable(true);
    firstNameCol.setDataStoreName("firstName");
    patientsTable.addColumn(firstNameCol, "First Name");

    Column<PatientRegistration, String> lastNameCol = new Column<PatientRegistration, String>(new ClickableTextCell()) {
      @Override
      public String getValue(PatientRegistration registration) {
        return registration.getLastName();
      }

      @Override
      public String getCellStyleNames(Context context, PatientRegistration object) {
        if ("1".equals(displayVersion)) {
          return super.getCellStyleNames(context, object) + " " + "patientIdColorLabel";
        }
        return super.getCellStyleNames(context, object);
      }
    };
    lastNameCol.setFieldUpdater(new FieldUpdater<PatientRegistration, String>() {
      @Override
      public void update(int index, PatientRegistration registration, String value) {
        showPatientCallback.showPatient(registration.getPatient());
      }
    });
    lastNameCol.setSortable(true);
    lastNameCol.setDataStoreName("lastName");
    patientsTable.addColumn(lastNameCol, "Last Name");


    TextColumn<PatientRegistration> apptTypeCol = new TextColumn<PatientRegistration>() {
      @Override
      public String getValue(PatientRegistration registration) {
        return registration.getVisitType();
      }
    };
    apptTypeCol.setSortable(true);
    apptTypeCol.setDataStoreName("apptType");
    patientsTable.addColumn(apptTypeCol, "Appt Type");

    final Column<PatientRegistration, String> apptTimeCol = new Column<PatientRegistration, String>(new ClickableTextCell()) {
      @Override
      public String getValue(PatientRegistration registration) {
        String time;
        if (registration.isAppointment()) {
          time = fmtTime.format(registration.getSurveyDt());
        } else {
          time = getUtils().getDefaultDateFormat().format(registration.getSurveyDt());
        }
        if (multipleRegistrations(registration)) {
          // \u25bc is the unicode character ▼ (down triangle)
          time += "  \u25bc ";
        }
        return time;
      }
    };
    apptTimeCol.setFieldUpdater(new FieldUpdater<PatientRegistration, String>() {
      private PopupRelative popup;

      @Override
      public void update(int index, final PatientRegistration patReg, final String value) {
        if (!multipleRegistrations(patReg)) {
          return;
        }

        if (popup == null) {
          Element cell = patientsTable.getRowElement(index).getCells().getItem(
              patientsTable.getColumnIndex(apptTimeCol)).getFirstChildElement();
          popup = new PopupRelative(cell, createAppointmentsTable(patReg), Align.BELOW_RIGHT, new CloseCallback() {
            @Override
            public void afterClose() {
              popup = null;
            }
          });
        } else {
          popup.close();
          popup = null;
        }
      }
    });
    apptTimeCol.setSortable(true);
    apptTimeCol.setDataStoreName("apptTime");
    patientsTable.addColumn(apptTimeCol, "Appt Time");

    TextColumn<PatientRegistration> surveyTypeCol = new TextColumn<PatientRegistration>() {
      @Override
      public String getValue(PatientRegistration registration) {
        String surveyType = registration.getSurveyType();
        if (surveyType != null && surveyType.contains(".")) {
          surveyType = surveyType.substring(0, surveyType.indexOf("."));
        }
        return surveyType;
      }
    };
    surveyTypeCol.setSortable(true);
    surveyTypeCol.setDataStoreName("surveyType");
    patientsTable.addColumn(surveyTypeCol, "Survey Type");

    final Column<PatientRegistration, String> apptStatusCol = createApptStatusColumn(patientsTable);
    patientsTable.addColumn(apptStatusCol, "Appt Status");

    Column<PatientRegistration, ImageResource> imageCol = createActionIconColumn();
    patientsTable.addColumn(imageCol, " ");

    final Column<PatientRegistration, String> actionCol = createActionColumn(patientsTable);
    actionCol.setSortable(true);
    actionCol.setDataStoreName("action");
    patientsTable.addColumn(actionCol, "Recommended Action");

    patientsTable.setColumnWidth(mrnCol, 90, Unit.PX);
    patientsTable.setColumnWidth(firstNameCol, 30, Unit.PCT);
    patientsTable.setColumnWidth(lastNameCol, 30, Unit.PCT);
    patientsTable.setColumnWidth(apptTypeCol, 100, Unit.PX);
    patientsTable.setColumnWidth(apptTimeCol, 140, Unit.PX);
    patientsTable.setColumnWidth(surveyTypeCol, 130, Unit.PX);
    patientsTable.setColumnWidth(imageCol, 21, Unit.PX);
    patientsTable.setColumnWidth(actionCol, 40, Unit.PCT);
    patientsTable.setColumnWidth(apptStatusCol, 140, Unit.PX);

    patientsTable.setStylePrimaryName(RegistryResources.INSTANCE.css().fixedList());
    patientsTable.addStyleName(RegistryResources.INSTANCE.css().dataList());
    patientsTable.addStyleName(RegistryResources.INSTANCE.css().scheduleList());
    patientsTable.setEmptyTableWidget(new Label("Nothing found. Adjust the search criteria above."));

    ListDataProvider<PatientRegistration> dataProvider = new ListDataProvider<>();
    dataProvider.addDataDisplay(patientsTable);
    registrationsFiltered = dataProvider.getList();
    final ListHandler<PatientRegistration> columnSortHandler = new ListHandler<PatientRegistration>(registrationsFiltered) {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        ColumnSortList sortList = patientsTable.getColumnSortList();
        if (sortList != null && sortList.size() > 0) {
          Column sortColumn = sortList.get(0).getColumn();
          GWT.log("col_sorta: " + event.isSortAscending() + " col_index: " + sortColumn.getDataStoreName() +
              " col in event is " + event.getColumn().getDataStoreName());
          saveSortChoice(sortColumn.getDataStoreName(), event.isSortAscending());
        }
        super.onColumnSort(event);
      }
    };
    columnSortHandler.setComparator(mrnCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getPatientId().compareTo(o2.getPatientId()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(firstNameCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getFirstName().compareTo(o2.getFirstName()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(lastNameCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getLastName().compareTo(o2.getLastName()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(apptTypeCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getVisitType().compareTo(o2.getVisitType()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(apptTimeCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getSurveyDt().compareTo(o2.getSurveyDt()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(surveyTypeCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getSurveyType().compareTo(o2.getSurveyType()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(actionCol,
        new Comparator<PatientRegistration>() {
      @Override
      public int compare(PatientRegistration o1, PatientRegistration o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          // May want to order by logical (from enroll to done similar to bottom summary bar)
          return (o2 != null) ? getActionName(o1).compareTo(getActionName(o2)) : 1;
        }
        return -1;
      }
    });
    patientsTable.addColumnSortHandler(columnSortHandler);

    //    SimplePager pager = new SimplePager();
    //    pager.setDisplay(patientsTable);
    String sortBy = preferences != null &&
        notNullorEmpty(preferences.getSchedSortColumn()) ? preferences.getSchedSortColumn()
        : getUtils().getParam(Constants.SCHED_SORT_PARAM);
    boolean isAscending =
        preferences != null && preferences.getSchedSortAsc() != null ? preferences.getSchedSortAsc() : true;
    GWT.log("sortBy is " + sortBy + " ascending: " + isAscending);
    for (int c = 0; c < patientsTable.getColumnCount(); c++) {
      String dataStoreName = patientsTable.getColumn(c).getDataStoreName();
      if (dataStoreName != null && dataStoreName.equals(sortBy)) {
        ColumnSortInfo columnSortInfo = new ColumnSortInfo(patientsTable.getColumn(c), isAscending);
        patientsTable.getColumnSortList().push(columnSortInfo);
      }
    }

    buttonPanel = new HorizontalPanel();
    buttonPanel.addStyleName(RegistryResources.INSTANCE.css().clTabPgFootingBar());
    buttonPanel.addStyleName(RegistryResources.INSTANCE.css().registrationListButtonBar());
    buttonPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    buttonPanel.add(new Label("Total Patients: "));
    totalPatients = new Label(" ");
    buttonPanel.add(totalPatients);
    buttonPanel.add(new Label("To Enroll: "));
    toEnroll = new Label(" ");
    buttonPanel.add(toEnroll);
    buttonPanel.add(new Label("To Start: "));
    toStart = new Label(" ");
    buttonPanel.add(toStart);
    buttonPanel.add(new Label("To Finish: "));
    toFinish = new Label(" ");
    buttonPanel.add(toFinish);
    buttonPanel.add(new Label("To Print: "));
    toPrint = new Label(" ");
    buttonPanel.add(toPrint);
    buttonPanel.add(printSurveysButton);
    buttonPanel.add(new Label("Done: "));
    done = new Label(" ");
    buttonPanel.add(done);
    buttonPanel.setVisible(false);
    buttonPanel.add(pdfPanel);
    printSurveysButton.setVisible(false);
    subPanel.addSouth(buttonPanel, 4);
    subPanel.add(patientsTable);

    searchButton.addClickHandler(this);
    todaySearchButton.addClickHandler(this);
    weekSearchButton.addClickHandler(this);
    newAppointmentButton.addClickHandler(this);
    newSurveyButton.addClickHandler(this);
    addlistContinueButton.addClickHandler(this);
    addlistCloseButton.addClickHandler(this);
    addlistCancelButton.addClickHandler(this);
    addSurveyContinueButton.addClickHandler(this);
    addSurveyCloseButton.addClickHandler(this);
    addSurveyCancelButton.addClickHandler(this);
    printSurveysButton.addClickHandler(this);
    addDeclineButton.addClickHandler(this);

    addlistContinueButton.addStyleName(RegistryResources.INSTANCE.css().leftButton());
    addlistCloseButton.addStyleName(RegistryResources.INSTANCE.css().leftButton());
    addlistCancelButton.addStyleName(RegistryResources.INSTANCE.css().leftButton());
    addSurveyContinueButton.addStyleName(RegistryResources.INSTANCE.css().leftButton());
    addSurveyCloseButton.addStyleName(RegistryResources.INSTANCE.css().leftButton());
    addSurveyCancelButton.addStyleName(RegistryResources.INSTANCE.css().leftButton());

    // init new list date fields
    addDate = DisplayUtils.makelistDateField(getUtils());
    addlistTime = new TimePicker();
    // The range is from the beginning of the from date through the end of to date so this gets all of today
    fromDt = new Date();
    toDt = new Date();
    showFromPicker.setValue(fromDt);
    showToPicker.setValue(toDt);

    displayListTypes.addItem(ALL, ALL);
    displayListTypes.addItem(APPOINTMENT + "s", APPOINTMENT);
    displayListTypes.addItem(SURVEY + "s", SURVEY);
    displayListTypes.setSelectedIndex(1);

    registeredChkBox.setValue(false);
    cancelledChkBox.setValue(false);
    printedChkBox.setValue(false);
    notPrintedChkBox.setValue(false);

    if (getClientConfig().isClinicFilterEnabled()) {
      clinicFilter = getClientConfig().getClinicFilterValue();
    }
    if (preferences != null && preferences.getClinicFilter() != null) {
      clinicFilter = preferences.getClinicFilter();
    }

    this.displayVersion = getUtils().getParam("patientIdentificationViewVs", "0");

    doShowListPage();

    subPanel.addStyleName(RegistryResources.INSTANCE.css().scheduleListsPanel());

    printSurveysButton.setTitle("Print pending");

    emailImage.setTitle("Send Email");
    completedImage.setTitle("Completed survey");
    scoresImage.setTitle("Show scores report");
    deleteImage.setTitle("Delete");

    // initialize the dialogbox for adding new appointments and survey schedules
    addPanel.addStyleName(RegistryResources.INSTANCE.css().popupBox());
    addPatientDialogBox.add(addPanel);
    // initialize the list of providers for the filter
    findProviders();
    setLoaded(true);

    setInitialFocus(showFromPicker);
  }

  private void createFilterButton() {
    filterButton = new PopdownButton().withText("Today").withMenu(new PopdownButton.Customizer() {
      @Override
      public void customizePopup(final PopdownButton button, Menu menu) {
        menu.addItem("Today", new Command() {
          @Override
          public void execute() {
            button.setText("Today");
            showFromLabel.setVisible(false);
            showFromPicker.setVisible(false);
            showToLabel.setVisible(false);
            showToPicker.setVisible(false);
            fromDt = new Date();
            toDt = new Date();
            showFromPicker.setValue(fromDt);
            showToPicker.setValue(toDt);
            doShowListPage();
          }
        });
        menu.addItem("Tomorrow", new Command() {
          @Override
          public void execute() {
            filterButton.setText("Tomorrow");
            showFromLabel.setVisible(false);
            showFromPicker.setVisible(false);
            showToLabel.setVisible(false);
            showToPicker.setVisible(false);
            Date tommorrow = new Date((new Date()).getTime() + (24 * 3600 * 1000));
            fromDt = new Date(getUtils().getStartOfDay(tommorrow));
            toDt = new Date(getUtils().getEndOfDay(tommorrow));
            showFromPicker.setValue(new Date(getUtils().getStartOfDay(toDt)));
            showToPicker.setValue(new Date(getUtils().getEndOfDay(toDt)));
            doShowListPage();
          }
        });
        menu.addItem("Custom date range", new Command() {
          @Override
          public void execute() {
            filterButton.setText("Custom Date Range");
            showFromLabel.setVisible(true);
            showFromPicker.setVisible(true);
            showToLabel.setVisible(true);
            showToPicker.setVisible(true);
          }
        });

        if (getClientConfig().isClinicFilterEnabled()) {
          menu.addSeparator();
          if (getClientConfig().isClinicFilterAllEnabled()) {
            menu.addItemChecked("All clinics", (clinicFilter == null), new Command() {
              @Override
              public void execute() {
                clinicFilter = null;
                saveClinicFilter();
                doShowListPage();
              }
            });
          }
          Map<String,List<String>> clinicFilterMapping = getClientConfig().getClinicFilterMapping();
          for(final String name : clinicFilterMapping.keySet()) {
            menu.addItemChecked(name, name.equals(clinicFilter), new Command() {
              @Override
              public void execute() {
                clinicFilter = name;
                saveClinicFilter();
                doShowListPage();
              }
            });
          }
        }
        menu.addSeparator();
        menu.addItemChecked("Show appointments", showAppointments, new Command() {
          @Override
          public void execute() {
            if (!showAppointments || showAssessments) {
              showAppointments = !showAppointments;
              doShowListPage();
            }
          }
        });
        menu.addItemChecked("Show assessments with no appointment", showAssessments, new Command() {
          @Override
          public void execute() {
            if (!showAssessments || showAppointments) {
              showAssessments = !showAssessments;
              doShowListPage();
            }
          }
        });
        menu.addSeparator();
        menu.addItemChecked("Hide items I dealt with", hideCompleted, new Command() {
          @Override
          public void execute() {
            hideCompleted = !hideCompleted;
            if (hideCompleted && hideNotCompleted) {
              hideNotCompleted = false;
            }
            refreshDisplay();
          }
        });
        menu.addItemChecked("Hide items I need to deal with", hideNotCompleted, new Command() {
          @Override
          public void execute() {
            hideNotCompleted = !hideNotCompleted;
            if (hideCompleted && hideNotCompleted) {
              hideCompleted = false;
            }
            refreshDisplay();
          }
        });
        menu.addSeparator();
        menu.addItemChecked("Hide registered patients", hideEnrolled, new Command() {
          @Override
          public void execute() {
            hideEnrolled = !hideEnrolled;
            refreshDisplay();
          }
        });
        menu.addItemChecked("Hide declined patients", hideDeclined, new Command() {
          @Override
          public void execute() {
            hideDeclined = !hideDeclined;
            refreshDisplay();
          }
        });
        menu.addItemChecked("Hide printed items", hidePrinted, new Command() {
          @Override
          public void execute() {
            hidePrinted = !hidePrinted;
            refreshDisplay();
          }
        });
      }
    });
  }

  private Column<PatientRegistration, ImageResource> createActionIconColumn() {
    return new Column<PatientRegistration, ImageResource>(new ImageResourceCell()) {
      @Override
      public ImageResource getValue(PatientRegistration registration) {
        ApptAction apptAction = registration.getAction();
        if (apptAction.isActionNeeded()) {
          return RegistryResources.INSTANCE.decline();
        } else {
          return RegistryResources.INSTANCE.accept();
        }
      }
    };
  }

  private Column<PatientRegistration, String> createApptStatusColumn(final DataGrid<PatientRegistration> dataGrid) {
    final Column<PatientRegistration, String> apptStatusCol = new Column<PatientRegistration, String>(new ButtonCell() {
      @Override
      public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        // Override the default behavior to avoid empty buttons being visible and clickable
        if (data == null) {
          sb.appendHtmlConstant("<button type=\"button\" tabindex=\"-1\" style=\"display:none;\"></button>");
        } else {
          sb.appendHtmlConstant("<button type=\"button\" tabindex=\"-1\">");
          sb.append(data);
          sb.appendHtmlConstant("</button>");
        }
      }
    }) {
      @Override
      public String getValue(PatientRegistration registration) {
        // \u25bc is the unicode character ▼ (down triangle)
        if (registration.isAppointment()) {
          if (registration.getAppointmentStatus() == AppointmentStatus.completed) {
            return "Completed | \u25bc";
          } else if (registration.getAppointmentStatus() == AppointmentStatus.notCompleted) {
            return "Not completed | \u25bc";
          }
          return "Select one... | \u25bc";
        }
        return null;
      }
    };
    apptStatusCol.setCellStyleNames("actionButton");
    apptStatusCol.setFieldUpdater(new FieldUpdater<PatientRegistration, String>() {
      private PopdownAny popdown;

      @Override
      public void update(int index, final PatientRegistration patReg, final String value) {
        if (!patReg.isAppointment()) {
          return;
        }

        if (popdown == null || !popdown.isVisible()) {
          Element button = dataGrid.getRowElement(index).getCells().getItem(
              dataGrid.getColumnIndex(apptStatusCol)).getFirstChildElement().getFirstChildElement();
          popdown = new PopdownAny(button, Align.BELOW_LEFT, new Customizer() {
            @Override
            public void customizePopup(Menu menu) {
              if (patReg.getAppointmentStatus() != AppointmentStatus.completed) {
                menu.addItem("Completed", new Command() {
                  @Override
                  public void execute() {
                    clinicService.setAppointmentStatus(patReg.getApptId(), AppointmentStatus.completed, new Callback<Void>() {
                      @Override
                      public void handleSuccess(Void result) {
                        patReg.setAppointmentStatus(AppointmentStatus.completed);
                        refreshDisplay();
                      }
                    });
                  }
                });
              }
              if (patReg.getAppointmentStatus() != AppointmentStatus.notCompleted) {
                menu.addItem("Not completed", new Command() {
                  @Override
                  public void execute() {
                    clinicService.setAppointmentStatus(patReg.getApptId(), AppointmentStatus.notCompleted, new Callback<Void>() {
                      @Override
                      public void handleSuccess(Void result) {
                        patReg.setAppointmentStatus(AppointmentStatus.notCompleted);
                        refreshDisplay();
                      }
                    });
                  }
                });
              }
            }
          });
          popdown.show();
        } else if (popdown != null) {
          popdown.hide();
          popdown = null;
        }
      }
    });
    return apptStatusCol;
  }

  private boolean multipleRegistrations(PatientRegistration registration) {
    return nullToFalse(multipleRegistrations.get(registration.getPatientId()));
  }

  private boolean nullToFalse(Boolean value) {
    if (value == null) {
      return false;
    }
    return value;
  }

  private Widget createAppointmentsTable(final PatientRegistration patReg) {
    final DataGrid<PatientRegistration> apptsTable = new DataGrid<>();
    TextColumn<PatientRegistration> apptTypeCol = new TextColumn<PatientRegistration>() {
      @Override
      public String getValue(PatientRegistration registration) {
        return registration.getVisitType();
      }
    };
    apptsTable.addColumn(apptTypeCol, "Appt Type");

    // Might want to make this clickable to dismiss and move the patientsTable view to make the clicked one visible/highlighted
    TextColumn<PatientRegistration> apptTimeCol = new TextColumn<PatientRegistration>() {
      @Override
      public String getValue(PatientRegistration registration) {
        if (registration.isAppointment()) {
          return fmtTime.format(registration.getSurveyDt());
        } else {
          return getUtils().getDefaultDateFormat().format(registration.getSurveyDt());
        }
      }
    };
    apptsTable.addColumn(apptTimeCol, "Appt Time");

    TextColumn<PatientRegistration> surveyTypeCol = new TextColumn<PatientRegistration>() {
      @Override
      public String getValue(PatientRegistration registration) {
        return registration.getSurveyType();
      }
    };
    apptsTable.addColumn(surveyTypeCol, "Survey Type");

    final Column<PatientRegistration, String> apptStatusCol = createApptStatusColumn(apptsTable);
    apptsTable.addColumn(apptStatusCol, "Appt Status");

    Column<PatientRegistration, ImageResource> imageCol = createActionIconColumn();
    apptsTable.addColumn(imageCol, " ");

    final Column<PatientRegistration, String> actionCol = createActionColumn(apptsTable);
    apptsTable.addColumn(actionCol, "Recommended Action");

    apptsTable.setColumnWidth(apptTypeCol, 80, Unit.PX);
    apptsTable.setColumnWidth(apptTimeCol, 140, Unit.PX);
    apptsTable.setColumnWidth(surveyTypeCol, 120, Unit.PX);
    apptsTable.setColumnWidth(imageCol, 21, Unit.PX);
    apptsTable.setColumnWidth(actionCol, 300, Unit.PX);
    apptsTable.setColumnWidth(apptStatusCol, 150, Unit.PX);

    apptsTable.setPixelSize(750, 300);
    apptsTable.setStylePrimaryName(RegistryResources.INSTANCE.css().fixedList());
    apptsTable.addStyleName(RegistryResources.INSTANCE.css().dataList());
    apptsTable.addStyleName(RegistryResources.INSTANCE.css().scheduleList());
    apptsTable.setEmptyTableWidget(new Label("Nothing found. Adjust the search criteria above."));

    ListDataProvider<PatientRegistration> dataProvider = new ListDataProvider<>();
    dataProvider.addDataDisplay(apptsTable);
    List<PatientRegistration> appts = dataProvider.getList();
    for (PatientRegistration registration : registrationsFiltered) {
      if (registration.getPatientId().equals(patReg.getPatientId())) {
        appts.add(registration);
      }
    }

    SimplePager pager = new SimplePager();
    pager.setDisplay(apptsTable);

    return apptsTable;
  }

  private Column<PatientRegistration, String> createActionColumn(final DataGrid<PatientRegistration> dataGrid) {
    final Column<PatientRegistration, String> actionCol = new Column<PatientRegistration, String>(new ButtonCell()) {
      @Override
      public String getValue(PatientRegistration registration) {
        return getActionName(registration);
      }
    };
    actionCol.setCellStyleNames("actionButton");
    actionCol.setFieldUpdater(new FieldUpdater<PatientRegistration, String>() {
      private PopdownAny popdown;

      @Override
      public void update(int index, final PatientRegistration patReg, final String value) {
        final List<MenuDef> menuDefs =  menuDefIntfUtils.getMenuDefs(patReg.getAction().getMenuDefs(), menuDefBeanFactory);
        if ((popdown == null || !popdown.isVisible()) && ((menuDefs != null) && !menuDefs.isEmpty())) {
          Element button = dataGrid.getRowElement(index).getCells().getItem(
              dataGrid.getColumnIndex(actionCol)).getFirstChildElement().getFirstChildElement();
          popdown = new PopdownAny(button, Align.BELOW_LEFT, new Customizer() {
            @Override
            public void customizePopup(Menu menu) {
              for(MenuDef menuDef : menuDefs) {
                Command command = getActionCommand(menuDef, patReg);
                menu.addItem(menuDef.getMenuLabel(), command);
              }
            }
          });
          popdown.show();
        } else if (popdown != null) {
          popdown.hide();
          popdown = null;
        }
      }
    });
    return actionCol;
  }

  private void showStartSurveyPopup(final PatientRegistration patReg) {
    clinicService.getSurveyStartStatus(patReg.getPatient(), patReg.getApptId(), new Callback<SurveyStart>() {
      @Override
      public void handleSuccess(SurveyStart result) {
        final Popup enrollPopup = makePopup("Start Assessment");
        enrollPopup.setModal(false);
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
        // make sure the name is set up properly
        if (ClientUtils.isEmpty(patReg.getFirstName()) || patReg.getFirstName().trim().equals("-") ||
            ClientUtils.isEmpty(patReg.getLastName()) || patReg.getLastName().trim().equals("-")) {
          Label warningLabel = new Label("This survey cannot be started! ");
          warningLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
          panel.add(warningLabel);
          panel.add(new Label("Please check that the patient's name and date of birth are entered correctly"));
        } else {
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
              boolean completed = (survey.getNumberCompleted() >0) && (survey.getNumberPending() == 0);
              Image statusIcon = getUtils().getCompletedImage(completed);
              boolean showLink = getClientConfig().paramEqualsIgnoreCase("survey.clinic.link.enabled", "false","true");
              String surveyLink = getClientConfig().getParam("survey.link");
              String surveyLabel = survey.getSurveyName() + ": " + survey.getToken();
              Widget code;
              if (showLink && !surveyLink.equals("")) {
                String siteName = getClientConfig().getSiteName();
                String surveyUrl = surveyLink + "/survey2?s=" + siteName + "&tk=" + survey.getToken();
                code = new Anchor(surveyLabel, surveyUrl, "_blank");
              } else {
                code = new Label(surveyLabel);
              }
              code.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
              HorizontalPanel block = new HorizontalPanel();
              block.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
              block.add(statusIcon);
              block.add(code);
              panel.add(block);
            }
          } else {
            SurveyRegistration survey = patReg.getSurveyRegList().get(0);
            boolean showLink = getClientConfig().paramEqualsIgnoreCase("survey.clinic.link.enabled", "false","true");
            String surveyLink = getClientConfig().getParam("survey.link");
            String surveyLabel = survey.getToken();
            if (!survey.getSurveyName().equals("Default")) {
              surveyLabel = survey.getSurveyName() + ": " + survey.getToken();
            }
            Widget code;
            if (showLink && !surveyLink.equals("")) {
              String siteName = getClientConfig().getSiteName();
              String surveyUrl = surveyLink + "/survey2?s=" + siteName + "&tk=" + survey.getToken();
              code = new Anchor(surveyLabel, surveyUrl, "_blank");
            } else {
              code = new Label(surveyLabel);
            }
            code.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
            panel.add(code);
          }
        }
        Button cancelButton = new Button("Ok");
        cancelButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            enrollPopup.hide();
          }
        });

        Button changeButton = new Button("Change Survey Type");
        changeButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            enrollPopup.hide();
            showChangeSurveyPopup(patReg);
          }
        });

        final List<Button> buttons = new ArrayList<>();
        buttons.add(cancelButton);
        if (patReg.getNumberCompleted() == 0) {
          buttons.add(changeButton);
        }
        enrollPopup.setCustomButtons(buttons);
        enrollPopup.setGlassEnabled(true);
        enrollPopup.showMessage(panel);
      }
    });

  }

  private void showEnrollPopup(final PatientRegistration patReg) {
    final Popup enrollPopup = makePopup("Enroll Patient");
    enrollPopup.setModal(false);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());

    panel.add(new Label("Verify patient:"));
    Label name = new Label(patReg.getFirstName() + " " + patReg.getLastName() + " (DOB: "
        + getUtils().getDefaultDateFormat().format(patReg.getDtBirth()) + ")");
    name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(name);
    panel.add(new Label("Email address (leave blank to assess in clinic only):"));
    String currentEmail = patReg.getEmailAddr();
    final ValidEmailAddress email = getClientUtils().makeEmailField(currentEmail,
        new InvalidEmailHandler() {
      @Override
      public void onFailedValidation(InvalidEmailEvent event) {
        // nothing to do here - validation is below
      }
    });
    email.setInvalidStyleName("emailTextError");
    email.setValidStyleName("emailTextValid");
    panel.add(email);

    List<Button> buttons = new ArrayList<>();
    final Button enrollButton = new Button("Enroll");
    enrollButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!email.isValid()) {
          return;
        }
        String emailValue = email.getValue();
        if (emailValue != null) {
          emailValue = emailValue.trim();
          if (emailValue.isEmpty()) {
            emailValue = null;
          }
        }

        //clinicService.setPatientAgreesToSurvey(patReg.getPatient(), new Callback<Patient>() {
        //  @Override
        //  public void handleSuccess(Patient result) {
        //    patReg.setPatient(result);
        clinicService.acceptEnrollment(patReg.getPatientId(), emailValue, new Callback<Void>() {
          @Override
          public void handleSuccess(Void result) {
            enrollPopup.hide();
            doShowListPage();
          }
        });
      }
    });
    enrollButton.addStyleName(RegistryResources.INSTANCE.css().defaultButton());
    final Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        enrollPopup.hide();
      }
    });

    email.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          enrollButton.click();
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          cancelButton.click();
        }
      }
    });
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        email.selectAll();
        email.setFocus(true);
      }
    });

    buttons.add(enrollButton);
    buttons.add(cancelButton);
    enrollPopup.setCustomButtons(buttons);
    enrollPopup.setGlassEnabled(true);

    enrollPopup.showMessage(panel);
  }

  private void showDeclinePopup(final PatientRegistration patReg) {
    final Popup declinePopup = makePopup("Decline assessments");
    declinePopup.setModal(false);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickDeclinePanel());
    panel.add(new Label("Verify patient:"));
    Label name = new Label(patReg.getFirstName() + " " + patReg.getLastName() + " (DOB: "
        + getUtils().getDefaultDateFormat().format(patReg.getDtBirth()) + ")");
    name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(name);

    new DeclinePopup(clinicService, RegistryResources.INSTANCE.css()) {
      @Override
      protected void declineSuccessHandler(Patient result) {
        patReg.setPatient(result);
        doShowListPage();
      }
    }.addRestOfReasonsForDecliningPopup(patReg.getPatient(), declinePopup, panel);
    declinePopup.showMessage(panel);
  };

  private void showCancelPopup(final PatientRegistration patReg) {
    final Popup cancelPopup = makePopup("Cancel assessment");
    cancelPopup.setModal(false);

    new CancelPopup(clinicService, RegistryResources.INSTANCE.css()) {
      @Override
      protected void cancelSuccessHandler(PatientRegistration result) {
        doShowListPage();
      }
    }.addContents(patReg, getUtils());
  };

  private void showChangeSurveyPopup(final PatientRegistration patReg) {
    new ChangeSurveyPopup(getUtils(), patReg, clinicService, new Callback<PatientRegistration>() {
      @Override
      public final void handleSuccess(PatientRegistration result) {
        showStartSurveyPopup(patReg);
      }
    });
  }

  private void sendPatientEmail(final PatientRegistration patReg, final emailRunnable afterDone) {
    patReg.setSendEmail(true);

    clinicService.sendEmail(patReg, new Callback<EmailSendStatus>() {
      @Override
      public void handleSuccess(EmailSendStatus result) {
        afterDone.run(result);
      }
    });

  }

  public String getActionName(PatientRegistration reg) {
    String label = reg.getAction().getLabel();
    String mmdd = "";
    if (reg.getSurveyLastCompleted() != null) {
      mmdd = fmtMonthDay.format(reg.getSurveyLastCompleted());
    }

    String completed = "";
    String total = "";
    if (reg.getNumberCompleted() != null) {
      completed = Integer.toString(reg.getNumberCompleted());
      if (reg.getNumberPending() != null) {
        total = Integer.toString(reg.getNumberCompleted() + reg.getNumberPending());
      }
    }

    // Replace the substitution strings in the label with the values
    String fmtLabel = label.replace("%mmdd%", mmdd).replace("%completed%", completed).replace("%total%", total);
    return fmtLabel;
  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();
    setEmptyMessage();

    if (sender == newAppointmentButton) {
      doAddPage(true, APPOINTMENT);
    } else if (sender == newSurveyButton) {
      doAddPage(true, SURVEY);
    } else if (sender == addlistContinueButton) {
      if (doSaveApptRegistration(APPOINTMENT)) {
        doAddPage(true, APPOINTMENT);
      } else {
        doAddPage(false, APPOINTMENT);
      }
    } else if (sender == addlistCloseButton) {
      if (doSaveApptRegistration(APPOINTMENT)) {
        addPatientDialogBox.hide();
      }
    } else if (sender == addlistCancelButton) {
      addMrnTextBox.setValue("");
      addPatientDialogBox.hide();
    } else if (sender == addSurveyContinueButton) {
      if (doSaveApptRegistration(SURVEY)) {
        doAddPage(true, SURVEY);
      } else {
        doAddPage(false, SURVEY);
      }
    } else if (sender == addSurveyCloseButton) {
      if (doSaveApptRegistration(SURVEY)) {
        addPatientDialogBox.hide();
      }
    } else if (sender == addSurveyCancelButton) {
      addMrnTextBox.setValue("");
      addPatientDialogBox.hide();
    }
    if (sender == searchButton) {
      try {
        fromDt = showFromPicker.getValue();
        if (fromDt == null) {
          setErrorMessage("You must enter a from date");
        }
      } catch (Exception e) {
        setErrorMessage("Please enter a valid from Date");
        fromDt = null;
      }
      try {
        toDt = showToPicker.getValue();
        if (toDt == null) {
          setErrorMessage("Please enter a valid to Date");
        }
      } catch (Exception e) {
        setErrorMessage("Please enter a valid tom Date");
        toDt = null;
      }
      doShowListPage();
    } else if (sender == todaySearchButton) {
      fromDt = new Date();
      toDt = new Date();
      showFromPicker.setValue(fromDt);
      showToPicker.setValue(toDt);
      doShowListPage();
    } else if (sender == weekSearchButton) {
      fromDt = new Date();
      long todayTime = fromDt.getTime();
      long oneWeek = 7 * 24 * 3600 * 1000;
      toDt = new Date(todayTime + oneWeek);
      showFromPicker.setValue(fromDt);
      showToPicker.setValue(toDt);
      doShowListPage();
    } else if (sender == addDeclineButton) {
      doDeclineSurveys(addDeclineButton.getPatient());
    } else if (sender == printSurveysButton) {
      doPrintSurveys();
    }
  }

  private void doDeclineSurveys(final Patient patient) {
    if (patient != null) {
      clinicService.declineEnrollment(patient, null, null, new Callback<Patient>() {
        @Override
        public void handleSuccess(Patient result) {
          addDeclineButton.setVisible(false);
          addAgreesChkBox.setValue(false);
          addAgreesChkBox.setEnabled(true);
        }
      });
    }
  }

  private void doPrintSurveys() {
    final ArrayList<AssessmentId> assessmentIds = new ArrayList<>();
    for (PatientRegistration registration : registrationsFiltered) {
      if (Constants.ACTION_TYPE_PRINT.equals(registration.getAction().getActionType())) {
        assessmentIds.add(registration.getAssessmentId());
      }
    }
    if (assessmentIds.size() < 1) {
      setErrorMessage("No patients waiting to be printed");
      return;
    }
    getUtils().showLoadingPopUp();
    printSurveysButton.setHTML(PRINTING_BUTTON);
    disableButtons();
    pdfPanel.clear();
    pdfPanel.setVisible(true);
    Timer timer = new Timer() {
      final int BATCH_SIZE = 3;
      private int numberProcessed = 0;

      @Override
      public void run() {
        if (numberProcessed == assessmentIds.size()) {
          cancel();
          return;
        }
        final ArrayList<AssessmentId> batchIds = new ArrayList<>();
        while (batchIds.size() < BATCH_SIZE && numberProcessed < assessmentIds.size()) {
          batchIds.add(assessmentIds.get(numberProcessed));
          numberProcessed++;
        }
        /* Put the patient ids in the session */
        clinicService.printScorePdfs(batchIds, 150, 400, 20, new Callback<Void>() {
          @Override
          protected void afterFailure() {
            setErrorMessage("Printing failed");
            cleanup();
          }

          @Override
          public void handleSuccess(Void result) {
            //pdfPanel.clear();
            final Frame frame = new Frame() {
              @Override
              public void onLoad() {
                this.setVisible(false);
              }
            };
            // frame.sinkEvents(Event.ONLOAD);
            /*
             * Now call the servlet to create the pdf for the patientids in the
             * session, sends to printer
             */
            String urlString = getUtils().getChartUrl(Constants.ASSESSMENT_ID, Constants.ASSESSMENT_ID_LIST, "height=118&width=297&print=y");
            frame.setTitle("[" + numberProcessed + "] done");
            frame.setUrl(urlString);
            pdfPanel.add(frame);
          }
        });
      }

      @Override
      public void cancel() {
        super.cancel();
        cleanup();
      }

      private void cleanup() {
        enableButtons();
        printSurveysButton.setHTML(PRINT_BUTTON);
        if (numberProcessed == assessmentIds.size()) {
          setSuccessMessage("Request is complete. " + numberProcessed + " PDF's have been printed.");
          getUtils().hideLoadingPopUp();
          doShowListPage();
        }
      }
    };
    timer.scheduleRepeating(10000);
  }

  private void doAddPage(boolean clearVars, String addType) {

    if (clearVars) {
      addMrnTextBox = makeMrnField();
      addFirstName = getUtils().makeRequiredField("");
      addLastName = getUtils().makeRequiredField("");
      addEmailTextBox = getUtils().makeEmailField(null, new InvalidEmailHandler() {

        @Override
        public void onFailedValidation(InvalidEmailEvent event) {
          setErrorMessage(event.getMessage());
          addEmailTextBox.addStyleName(RegistryResources.INSTANCE.css().dataListTextError());
        }
      });

      addDob = DisplayUtils.makelistDateField(getUtils());
      addSurveyType = getProcessTypeListBox();
      addAgreesChkBox = new CustomCheckBox();
      addAgreesChkBox.setValue(false);
      addDeclineButton.setEnabled(false);

      if (getClientConfig().isClinicFilterEnabled()) {
        addClinic = new ListBox();
        Map<String,List<String>> clinicFilterMapping = getClientConfig().getClinicFilterMapping();
        for(String clinic : clinicFilterMapping.keySet()) {
          addClinic.addItem(clinic);
        }
        addClinic.setSelectedIndex(0);
        for(int i=0; i<addClinic.getItemCount(); i++) {
          if (addClinic.getItemText(i).equals(clinicFilter)) {
            addClinic.setSelectedIndex(i);
          }
        }
        addClinic.setStyleName(RegistryResources.INSTANCE.css().gwtTextBox());
        addClinic.setWidth("200px");
      }

      addDate.setWidth("200px");
      addMrnTextBox.setWidth("200px");
      addFirstName.setWidth("200px");
      addLastName.setWidth("200px");
      addEmailTextBox.setWidth("200px");
      addDob.setWidth("200px");
      addSurveyType.setStyleName(RegistryResources.INSTANCE.css().gwtTextBox());
      addSurveyType.setWidth("200px");

      addSurveyType.addChangeHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          if (getUtils().getProcessXml().getProcessPatientAttributes() != null) {
            String selectedType = addSurveyType.getValue(addSurveyType.getSelectedIndex());
            // Get the attributes for the process type the user selected
            ArrayList<PatientAttribute> attribs = getUtils().getProcessXml().getProcessPatientAttributes()
                .get(selectedType);

            // Start by setting the consent checkbox value based on the patients
            // attribute
            if (addMrnTextBox.getPatient() != null
                && addMrnTextBox.getPatient().attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y")) {
              addAgreesChkBox.setValue(true);
            }
            boolean consentVisible = true;
            // now check if the type they've selected dictates setting & hiding
            // the checkbox
            if (attribs != null) {
              for (PatientAttribute attrib : attribs) {
                if (Constants.ATTRIBUTE_PARTICIPATES.equals(attrib.getDataName())) {
                  if ("y".equals(attrib.getDataValue())) {
                    addAgreesChkBox.setValue(true);
                    addAgreesChkBox.setValue(true);
                    consentVisible = false;
                  } else {
                    addAgreesChkBox.setValue(false);
                    addAgreesChkBox.setEnabled(false);
                    addAgreesChkBox.setVisible(false);
                    consentVisible = false;
                  }
                }
              }
            }

            if (consentVisible) {
              agreesLabel.setVisible(true);
              addAgreesChkBox.setVisible(true);
            } else {
              agreesLabel.setVisible(false);
              addAgreesChkBox.setVisible(false);
            }
          }
        }
      });

    }

    addPanel.clear();
    addPanel.setSpacing(5);

    HorizontalPanel headerPanel = new HorizontalPanel();
    Grid headingGrid = new Grid(1, 3);
    headingGrid.setStylePrimaryName(RegistryResources.INSTANCE.css().heading());
    // Image img = new Image(getUtils().getLogo());
    //headingGrid.setWidget(0, 0, new Image(RegistryResources.INSTANCE.logo()));
    Label titleLabel = new Label("New " + addType);
    //titleLabel.addStyleName("popUpBox");
    //titleLabel.addStyleName("heading");
    titleLabel.addStyleName(RegistryResources.INSTANCE.css().heading());
    // headerPanel.add(titleLabel);
    headingGrid.setWidget(0, 1, titleLabel);
    headerPanel.add(headingGrid);
    addPanel.add(headerPanel);

    FlexTable addPageTable = new FlexTable();
    Label label;
    int row = 0;

    if (addType.equals(APPOINTMENT)) {
      label = new Label("Appointment Date");
      label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
      addPageTable.setWidget(row, 0, label);
      addPageTable.setWidget(row, 1, addDate);
      addPageTable.setWidget(row, 2, new Label("Time"));
      addPageTable.setWidget(row, 3, addlistTime);
      row++;

      if (getClientConfig().isClinicFilterAllEnabled()) {
        label = new Label("Clinic");
        label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
        addPageTable.setWidget(row, 0, label);
        addPageTable.setWidget(row, 1, addClinic);
        row++;
      }
    } else {
      label = new Label("Survey Date");
      label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
      addPageTable.setWidget(row, 0, label);
      addPageTable.setWidget(row, 1, addDate);
      row++;
    }

    label = new Label(getPatientIdLabel());
    label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, label);
    addPageTable.setWidget(row, 1, addMrnTextBox);
    addPageTable.setWidget(row, 2, new Label("xxxxxx-x"));
    row++;

    label = new Label("First Name");
    label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, label);
    addPageTable.setWidget(row, 1, addFirstName);
    row++;

    label = new Label("Last Name");
    label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, label);
    addPageTable.setWidget(row, 1, addLastName);
    row++;

    label = new Label("Date of birth");
    label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, label);
    addPageTable.setWidget(row, 1, addDob);
    addPageTable.setWidget(row, 2, new Label("MM/DD/YYYY"));
    row++;

    label = new Label("Email");
    label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, label);
    addPageTable.setWidget(row, 1, addEmailTextBox);
    row++;

    label = new Label("Type");
    label.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, label);
    addPageTable.setWidget(row, 1, addSurveyType);
    row++;

    agreesLabel = new Label("Registered");
    agreesLabel.addStyleName(RegistryResources.INSTANCE.css().rightLabel());
    addPageTable.setWidget(row, 0, agreesLabel);
    addPageTable.setWidget(row, 1, addAgreesChkBox);
    addPageTable.setWidget(row, 2, addDeclineButton);
    addPageTable.getFlexCellFormatter().setColSpan(row, 2, 2);
    row++;

    FlowPanel dataPanel = new FlowPanel();
    dataPanel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
    dataPanel.add(addPageTable);
    addPanel.add(dataPanel);
    addPageTable.setVisible(true);
    addPatientDialogBox.show();

    FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.addStyleName(RegistryResources.INSTANCE.css().clTabPgFootingBar());
    buttonPanel.addStyleName(RegistryResources.INSTANCE.css().registrationButtonBar());

    if (addType.equals(APPOINTMENT)) {
      buttonPanel.add(addlistContinueButton);
      buttonPanel.add(addlistCloseButton);
      buttonPanel.add(addlistCancelButton);
    } else {
      buttonPanel.add(addSurveyContinueButton);
      buttonPanel.add(addSurveyCloseButton);
      buttonPanel.add(addSurveyCancelButton);
    }
    addPanel.add(buttonPanel);
    addPatientDialogBox.center();
    addPatientDialogBox.setVisible(true);
  }

  /**
   * Show the list of schedules that met the search criteria
   */
  private void doShowListPage() {
    // Turn the buttons off while the search runs
    disableButtons();
    registrationsFiltered.clear();
    /*
     * Delay running this to avoid the script processing message on IE
     */
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      // DeferredCommand.addCommand(new IncrementalCommand() {
      @Override
      public void execute() {
        getUtils().showLoadingPopUp();
        PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
        if (registeredChkBox.getValue()) {
          searchOptions.setOption(PatientRegistrationSearch.CONSENTED);
        }
        if (cancelledChkBox.getValue()) {
          searchOptions.setOption(PatientRegistrationSearch.CANCELLED);
        }
        if (printedChkBox.getValue()) {
          searchOptions.setOption(PatientRegistrationSearch.PRINTED);
        }
        if (notPrintedChkBox.getValue()) {
          searchOptions.setOption(PatientRegistrationSearch.NOTPRINTED);
        }
        if (clinicFilter != null) {
          Map<String, List<String>> clinicFilterMapping = getClientConfig().getClinicFilterMapping();
          for (String name : clinicFilterMapping.keySet()) {
            if (clinicFilter.equals(name)) {
              searchOptions.includeClinics(clinicFilterMapping.get(name));
            }
          }
        }
        if (preferences != null && notNullorEmpty(preferences.getSchedSortColumn())) {
          searchOptions.setSortBy(preferences.getSchedSortColumn());
        } else {
          String siteSort = getUtils().getParam(Constants.SCHED_SORT_PARAM);
          if (notNullorEmpty(siteSort)) {
            searchOptions.setSortBy(siteSort);
          } else {
            searchOptions.setSortBy(Constants.SCHED_SORT_DEFAULT);
          }
        }
        GWT.log("Sorting by " + searchOptions.getSortBy());
        if (preferences != null && preferences.getSchedSortAsc() != null) {
          searchOptions.setSortAscending(preferences.getSchedSortAsc());
        } else {
          searchOptions.setSortAscending(true);
        }
        if (showAppointments && showAssessments) { // search for all
          clinicService.searchForPatientRegistration(fromDt, toDt, searchOptions, searchCallback);
        } else if (showAppointments) {
          clinicService.searchForPatientRegistration(fromDt, toDt,
              Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT, searchOptions,
              searchCallback);
        } else {
          clinicService.searchForPatientRegistration(fromDt, toDt,
              Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, searchOptions,
              searchCallback);
        }
      }
    });
  }

  private boolean doSaveApptRegistration(String registerType) {
    boolean errors = false;
    StringBuilder errorMessages = new StringBuilder();
    String mrn = null;
    if (addMrnTextBox == null || addMrnTextBox.getText() == null || addMrnTextBox.getText().trim().length() < 1) {
      errors = true;
      errorMessages.append(getPatientIdLabel() + " is missing\n");
      addMrnTextBox.addStyleName(RegistryResources.INSTANCE.css().dataListTextError());
    } else {
      try {
        mrn = addMrnTextBox.getText();
        addMrnTextBox.addStyleName(RegistryResources.INSTANCE.css().dataListTextColumn());
      } catch (Exception e) {
        errors = true;
        errorMessages.append(getPatientIdLabel() + " is invalid. \n");
        addMrnTextBox.addStyleName(RegistryResources.INSTANCE.css().dataListTextError());
        logger.log(Level.INFO, "Invalid mrn saving survey registration");
      }

    }

    if (addEmailTextBox.getValue() != null && addEmailTextBox.getValue().trim().length() > 0) {
      if ((!getUtils().isValidEmail(addEmailTextBox.getValue()))) {
        errorMessages.append("Email address is not valid. \n");
        errors = true;
        addEmailTextBox.addStyleName(RegistryResources.INSTANCE.css().dataListTextError());
      }
    }
    /* CANNOT SAVE THE REGISTRATION UNLESS THEY AGREE */
    final boolean participates = addAgreesChkBox.getValue();
    if (!participates) {
      errorMessages.append("Can not save unless they agree to taking the assessments. \n");
      errors = true;
      addAgreesChkBox.addStyleName(RegistryResources.INSTANCE.css().dataListTextError());
    }

    // create the registration
    if (!errors) {
      Date tm = addDate.getValue();
      tm = addlistTime.getTime(tm);

      String aType = addSurveyType.getValue(addSurveyType.getSelectedIndex());

      final Patient patient;
      if (!addMrnTextBox.isNewPatient() && addMrnTextBox.getPatient() != null) {
        patient = addMrnTextBox.getPatient();
        /**
         * Check for changes to existing patients
         */
        if (!getCleanString(patient.getFirstName()).equals(getCleanString(addFirstName.getText()))) {
          patient.setFirstName(getCleanString(addFirstName.getText()));
        }
        if (!getCleanString(patient.getLastName()).equals(getCleanString(addLastName.getText()))) {
          patient.setLastName(getCleanString(addLastName.getText()));
        }
        if (!getCleanString(patient.getDtBirth().toString()).equals(getCleanString(addDob.getValue().toString()))) {
          if (addDob.getValue() != null) {
            String dobStr = getClientUtils().getDateString(addDob.getValue());
            Date newDate = new Date(getClientUtils().getMidDay(dobStr));
            patient.setDtBirth(new java.util.Date(newDate.getTime()));
          } else {
            patient.setDtBirth(null);
          }
        }
      } else {
        if (addFirstName.getText() == null || addFirstName.getText().length() < 1) {
          addFirstName.setText("-");
        }
        if (addLastName.getText() == null || addLastName.getText().length() < 1) {
          addLastName.setText("-");
        }

        String dobStr = getClientUtils().getDateString(addDob.getValue());
        Date newDate = new Date(getClientUtils().getMidDay(dobStr));
        patient = new Patient(mrn, addFirstName.getText(), addLastName.getText(), new java.util.Date(newDate.getTime()));
      }

      if (patient.getDtBirth() == null) {
        patient.setDtBirth(new java.util.Date((new Date()).getTime()));
      }

      String registrationType = Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT;
      String visitType = null;
      if (registerType.equals(SURVEY)) {
        registrationType = Constants.REGISTRATION_TYPE_STANDALONE_SURVEY;
        tm = new Date(getUtils().getEndOfDay(tm));
      } else {
        visitType = getUtils().getProcessXml().getProcessAttribute(aType, "visitType");
      }
      final ApptRegistration registration = new ApptRegistration(getUtils().getSiteId(), mrn,
          new Date(tm.getTime()), addEmailTextBox.getValue().trim(), aType, registrationType, visitType);
      registration.setSendEmail(false);

      if (getClientConfig().isClinicFilterEnabled()) {
        String clinic = addClinic.getValue(addClinic.getSelectedIndex());
        Map<String,List<String>> clinicFilterMapping = getClientConfig().getClinicFilterMapping();
        for(String name : clinicFilterMapping.keySet()) {
          if (name.equals(clinic)) {
            List<String> clinicValues = clinicFilterMapping.get(name);
            registration.setClinic(clinicValues.get(0));
          }
        }
      }

      final Date listDt = registration.getSurveyDt();
      Date start = new Date(getUtils().getStartOfDay(fromDt));
      Date end = new Date(getUtils().getEndOfDay(toDt));
      final boolean shouldRefresh;
      if ((listDt.after(start) || listDt.equals(start)) && (listDt.before(end) || listDt.equals(end))) {
        shouldRefresh = true;
      } else {
        shouldRefresh = false;
      }
      clinicService.addPatientRegistration(registration, patient, new Callback<PatientRegistration>() {
        @Override
        public void handleSuccess(PatientRegistration patReg) {
          Patient pat = patReg.getPatient();
          patient.setPatientId(pat.getPatientId()); // incase it was formatted
          patient.setDtChanged(pat.getDtChanged());
          patient.setDtCreated(pat.getDtCreated());
          patient.setAttributes(pat.getAttributes());
          boolean willRefreshLater = false;

          boolean nowAgrees = participates;
          boolean didAgree = patient.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y");

          if (nowAgrees && !didAgree) {
            // adds the attribute and also adds a consented activity, plus sets email
            willRefreshLater = true;
            callPatientAgreesToSurvey(patient);
          } else {
            // if the patient doesn't have the survey email address save it
            String regEmail = registration.getEmailAddr();
            if (!ClientUtils.isEmpty(regEmail) && !regEmail.equals(pat.getEmailAddress())) {
              willRefreshLater = true;
              callUpdatePatientEmail(pat);
            }

            if (!nowAgrees && didAgree) {
              willRefreshLater = true;
              callPatientDeclinesEnrollment(patient);
            }
          }
          if (shouldRefresh && !willRefreshLater) {
            doShowListPage();
          }
        }

        private void callUpdatePatientEmail(Patient pat) {
          getUtils().updatePatientEmail(pat, registration.getEmailAddr(), new Runnable() {
            @Override
            public void run() {
              if (shouldRefresh) {
                doShowListPage();
              }
            }
          });
        }

        private void callPatientAgreesToSurvey(final Patient patient) {
          clinicService.setPatientAgreesToSurvey(patient, new Callback<Patient>() { // xxx
            @Override
            public void handleSuccess(Patient result) {
              patient.setAttributes(result.getAttributes());
              doShowListPage();
            }
          });
        }

        protected void callPatientDeclinesEnrollment(final Patient patient) {
          clinicService.declineEnrollment(patient, null, null, new Callback<Patient>() {
            @Override
            public void handleSuccess(Patient result) {
              patient.setAttributes(result.getAttributes());
              doShowListPage();
            }
          });
        }

      });
    } else {
      if (errors) {
        setErrorMessage(errorMessages.toString());
      }
    }
    return !errors;
  }


  private ValidPatientId makeMrnField() {

    final ErrorDialogWidget yesNoErrorPopUp = new ErrorDialogWidget();
    yesNoErrorPopUp.setModal(false);
    final ValidPatientId mrn = new ValidPatientId(getUtils(), clinicService);

    mrn.addStyleName(RegistryResources.INSTANCE.css().dataListTextColumn());
    mrn.setInvalidStyleName("dataListTextError");
    mrn.setValidStyleName("dataListTextColumn");

    mrn.addValidPatientHandler(new ValidPatientHandler() {
      @Override
      public void onPassedValidation(ValidPatientEvent event) {
        Patient validatedPatient = event.getPatient();
        if (validatedPatient != null) {
          addMrnTextBox.setValue(validatedPatient.getPatientId());
          addFirstName.setValue(validatedPatient.getFirstName());
          addLastName.setValue(validatedPatient.getLastName());
          addDob.setValue(validatedPatient.getDtBirth());

          addAgreesChkBox.setValue(false);
          addAgreesChkBox.setEnabled(true);
          addDeclineButton.setEnabled(false);
          addDeclineButton.setPatient(validatedPatient);
          if (validatedPatient.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y")) {
            addAgreesChkBox.setValue(true);
            addAgreesChkBox.setEnabled(false);
            addDeclineButton.setEnabled(true);
          }

          if (validatedPatient.getEmailAddress() != null) {
            addEmailTextBox.setValue(validatedPatient.getEmailAddress());
          }

        } else {
          logger.log(Level.WARNING, "Passed validation but no patient on event");
        }
      }
    });

    mrn.addInvalidPatientHandler(new InvalidPatientHandler() {
      @Override
      public void onFailedValidation(InvalidPatientEvent event) {
        logger.log(Level.INFO, "InvalidPatientEvent formatError=" + event.formatError());
        ArrayList<Button> panelButtons = new ArrayList<>();
        if (event.formatError()) {
          Button closeButton = new Button("Close");
          closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              mrn.setFocus(true);
              yesNoErrorPopUp.hide();
            }
          });
          panelButtons.add(closeButton);
          HTML popUpHtml = new HTML("<p>" + event.getMessage());
          yesNoErrorPopUp.setError(popUpHtml);
        } else {
          Button yesButton = new Button("Yes");
          Button noButton = new Button("No");
          final String newPatientId = event.getFormattedId();
          yesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              mrn.setNewPatient(true);
              mrn.setValue(newPatientId);
              mrn.addStyleName(RegistryResources.INSTANCE.css().dataListTextColumn());
              yesNoErrorPopUp.hide();

              if (mrn.equals(addMrnTextBox)) {
                int options = addSurveyType.getItemCount();
                Date now = new Date();
                for (int o = 0; o < options; o++) {
                  if (addSurveyType.getItemText(o).contains("Init")) {
                    // make sure it's the one that is currently active
                    Date expires = getUtils().getProcessExpirationDate(addSurveyType.getItemText(o));
                    if (expires == null || expires.after(now)) {
                      addSurveyType.setSelectedIndex(o);
                    }
                  }
                }
              }
            }
          });

          noButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              mrn.setFocus(true);
              mrn.addStyleName(RegistryResources.INSTANCE.css().dataListTextColumn());
              yesNoErrorPopUp.hide();
            }
          });
          panelButtons.add(yesButton);
          panelButtons.add(noButton);
          HTML popUpHtml = new HTML("<p>" + event.getMessage() + "<p><p>" + " Are you adding a new patient ? <p> ");
          yesNoErrorPopUp.setError(popUpHtml);
        }
        yesNoErrorPopUp.setCustomButtons(panelButtons);
        ValidPatientId sender = (ValidPatientId) event.getSource();

        yesNoErrorPopUp.setPopupPosition(sender.getAbsoluteLeft(), sender.getAbsoluteTop());
        yesNoErrorPopUp.show();
      }
    });
    return mrn;
  }

  public ListBox getProcessTypeListBox(boolean consents) {
    ListBox box = getProcessTypeListBox();
    ArrayList<String> processNames = getUtils().getProcessXml().getProcessNames();
    int selectIndex = -1;
    if (processNames != null) {
      for (int i = 0; i < processNames.size(); i++) {
        ArrayList<PatientAttribute> attribs = getUtils().getProcessXml().getProcessPatientAttributes()
            .get(processNames.get(i));
        for (PatientAttribute attrib : attribs) {
          // if we're consenting find the process that sets the consents
          // attribute to 'y'
          if (Constants.ATTRIBUTE_PARTICIPATES.equals(attrib.getDataName())) {
            if ("y".equals(attrib.getDataValue()) && consents) {
              selectIndex = i;
            }
          }
          // if we're declining find the process that sets the consents
          // attribute to 'n'
          if (Constants.ATTRIBUTE_PARTICIPATES.equals(attrib.getDataName())) {
            if ("n".equals(attrib.getDataValue()) && !consents) {
              selectIndex = i;
            }
          }
        }
      } // end for each type of process
    }
    if (selectIndex > -1) {
      box.setSelectedIndex(selectIndex);
    }
    return box;
  }

  public ListBox getProcessTypeListBox() {
    ListBox box = new ListBox();
    ArrayList<String> processNames = getUtils().getProcessXml().getActiveVisitProcessNames();
    if (processNames != null) {
      for (String processName : processNames) {
        box.addItem(processName);
      }
    }

    if (box.getItemCount() > 0) {
      box.setSelectedIndex(0);
    }
    return box;
  }

  public void enableButtons() {
    todaySearchButton.setEnabled(true);
    weekSearchButton.setEnabled(true);
    searchButton.setEnabled(true);
    newAppointmentButton.setEnabled(true);
    newSurveyButton.setEnabled(true);
    printSurveysButton.setEnabled(true);
  }

  public void disableButtons() {
    todaySearchButton.setEnabled(false);
    weekSearchButton.setEnabled(false);
    searchButton.setEnabled(false);
    newAppointmentButton.setEnabled(false);
    newSurveyButton.setEnabled(false);
    printSurveysButton.setEnabled(false);
  }

  private final Callback<ArrayList<PatientRegistration>> searchCallback = new Callback<ArrayList<PatientRegistration>>() {
    @Override
    protected void afterFailure() {
      enableButtons();
      getUtils().hideLoadingPopUp();
    }

    @Override
    public void handleSuccess(ArrayList<PatientRegistration> result) {
      registrationsUnfiltered = result;
      // Get the user preferences
      if (getUtils().getUser().getUserPreferences("ScheduleTab") != null) {
        preferences = AutoBeanCodex.decode(servicesPreferencesFactory,
            ClinicServicePreferences.class, getUtils().getUser().getUserPreferences("ScheduleTab").toString()).as();
        FilteredProviders filteredProviders = preferences.getProviderFilter();
        if (filteredProviders != null && filteredProviders.getProviders() != null && filteredProviders.getProviders().size() > 0) {
          for (Long providerId : filteredProviders.getProviders()) {
            providerStatus.put(providerId, true);
          }
        }
        if (preferences.getClinicFilter() != null) {
          clinicFilter = preferences.getClinicFilter();
        }
      }
      refreshDisplay();

      getUtils().hideLoadingPopUp();
      enableButtons();
    }
  };

  private void refreshDisplay() {
    registrationsFiltered.clear();
    multipleRegistrations.clear();

    int total = 0, enroll = 0, start = 0, finish = 0, print = 0, done = 0, selectedProviders = 0;
    for(Long providerId : providerStatus.keySet()) {
      if (getProviderStatus(providerId)) {
        selectedProviders++;
      }
    }
    for (PatientRegistration registration : registrationsUnfiltered) {
      String actionType = registration.getAction().getActionType();

      total++;
      if (Constants.ACTION_TYPE_ENROLL.equals(actionType)) {
        enroll++;
      } else if (Constants.ACTION_TYPE_ASSESSMENT.equals(actionType)) {
        start++;
      } else if (Constants.ACTION_TYPE_IN_PROGRESS.equals(actionType)) {
        finish++;
      } else if (Constants.ACTION_TYPE_PRINT.equals(actionType)) {
        print++;
      } else if (Constants.ACTION_TYPE_PRINTED.equals(actionType)) {
        done++;
      } else { // ACTION_TYPE_OTHER
        // ACTION_ASSIGN_SURVEY
        // ACTION_NOTHING_DECLINED
        // ACTION_NOTHING_RECENTLY_COMPLETED
        // ACTION_NOTHING_INELIGIBLE
      }
      
      boolean completed = false;
      if (registration.getAppointmentStatus() == AppointmentStatus.completed ||
          registration.getAppointmentStatus() == AppointmentStatus.notCompleted) {
        completed = !registration.getAction().isActionNeeded();
      }

      boolean add = true;
      add = add && !(hideCompleted && completed);
      add = add && !(hideNotCompleted && !completed);
      add = add && !(hidePrinted && Constants.ACTION_TYPE_PRINTED.equals(actionType));
      add = add && !(hideEnrolled && registration.hasConsented());
      add = add && !(hideDeclined && registration.hasDeclined());
      add = add && (selectedProviders == 0  || getProviderStatus(registration.getProviderId()));
      if (add) {
        registrationsFiltered.add(registration);
      }

      if (multipleRegistrations.containsKey(registration.getPatientId())) {
        multipleRegistrations.put(registration.getPatientId(), true);
      } else {
        multipleRegistrations.put(registration.getPatientId(), false);
      }
    }
    patientsTable.setRowCount(registrationsFiltered.size());
    patientsTable.setPageSize(registrationsFiltered.size());
    totalPatients.setText(Integer.toString(total));
    toEnroll.setText(Integer.toString(enroll));
    toStart.setText(Integer.toString(start));
    toFinish.setText(Integer.toString(finish));
    toPrint.setText(Integer.toString(print));
    ScheduleWidget.this.done.setText(Integer.toString(done));
    printSurveysButton.setVisible(print > 0);
    buttonPanel.setVisible(total > 0);
  }

  private void showEmailPopup(final PatientRegistration patReg) {
    final Popup emailPopup = makePopup("Send Email");
    emailPopup.setModal(false);
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
    panel.add(new Label("Verify patient:"));
    Label name = new Label(patReg.getFirstName() + " " + patReg.getLastName() + " (DOB: "
        + getUtils().getDefaultDateFormat().format(patReg.getDtBirth()) + ")");
    name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(name);
    panel.add(new Label("Verify/correct email address:"));
    final String currentPatientEmail = patReg.getPatient().getEmailAddress();
    final ValidEmailAddress email = getClientUtils().makeEmailField(currentPatientEmail,
        new InvalidEmailHandler() {
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
          emailPopup.hide();
          getUtils().showSentFailedPopup(EmailSendStatus.invalid_email_addr, patReg);
          return;
        }
        String emailValue = email.getValue();
        if (emailValue == null || emailValue.trim().length() == 0) {
          getUtils().showSentFailedPopup(EmailSendStatus.no_email_addr, patReg);
          emailPopup.hide();
          return;
        }
        emailButton.setEnabled(false);
        if (!emailValue.equals(currentPatientEmail)) {
          getUtils().updatePatientEmail(patReg.getPatient(), emailValue, new Runnable() {
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

  private void printResults(final PatientRegistration patReg, final String appendParamString) {
    clinicService.searchForPatientStudyDataByPatientId(patReg.getPatientId(), patReg.getSurveyDt(), true,
        new Callback<ArrayList<PatientStudyExtendedData>>() {
          @Override
          public void handleSuccess(ArrayList<PatientStudyExtendedData> result) {
            pdfPanel.clear();
            String urlString = getUtils().getChartUrl(Constants.ASSESSMENT_ID, patReg.getAssessmentRegId().toString(), "height=118&width=297&print=y" + appendParamString);
            final Frame frame = new Frame(urlString) {
              @Override
              public void onLoad() {
                this.setVisible(false);
              }
            };
            frame.setUrl(urlString);
            pdfPanel.add(frame);
            setSuccessMessage("Printing request has been made.");
            for (PatientRegistration registration : registrationsUnfiltered) {
              if (registration.getApptId().equals(patReg.getApptId())) {
                registration.setNumberPrints(1);
                if (Constants.ACTION_TYPE_PRINT.equals(registration.getAction().getActionType())) {
                  registration.setAction(menuDefIntfUtils.getActionNothingPrinted(menuDefBeanFactory));
                }
              }
            }
            refreshDisplay();
          }
        });
  }

  protected Command getActionCommand(final MenuDef menuDef, final PatientRegistration patReg) {
    String commandName = menuDef.getCommandName();

    if (Constants.ACTION_CMD_ENROLL_POPUP.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          showEnrollPopup(patReg);
        }
      };
    } else if (Constants.ACTION_CMD_DECLINE_POPUP.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          showDeclinePopup(patReg);
        }
      };
    } else if (Constants.ACTION_CMD_START_SURVEY_POPUP.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          showStartSurveyPopup(patReg);
        }
      };
    } else if (Constants.ACTION_CMD_EMAIL_POPUP.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          showEmailPopup(patReg);
        }
      };
    } else if (Constants.ACTION_CMD_PRINT.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          printResults(patReg, "");
        }
      };
    } else if (Constants.ACTION_CMD_PRINT_RECENT.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          printResults(patReg, "&rrr=y");
        }
      };
    } else if (Constants.ACTION_CMD_ASSIGN_SURVEY_POPUP.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          new ChangeSurveyPopup(getUtils(), patReg, clinicService, new Callback<PatientRegistration>() {
            @Override
            public final void handleSuccess(PatientRegistration result) {
              doShowListPage();
            }
          });
        }
      };
    } else if (Constants.ACTION_CMD_CANCEL_POPUP.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          showCancelPopup(patReg);
        }
      };
    } else if (Constants.ACTION_CMD_SET_SURVEY_ATTR.equals(commandName)) {
      return new Command(){
        @Override
        public void execute() {
          Map<String,String> params = menuDef.getParameters();
          String surveyName = params.get(Constants.ACTION_CMD_PARAM_SURVEY_NAME);
          String name = params.get(Constants.ACTION_CMD_PARAM_NAME);
          if (name != null) {
            String value = params.get(Constants.ACTION_CMD_PARAM_VALUE);
            AssessmentId asmtId = patReg.getAssessmentId();
            clinicService.setSurveyRegAttribute(asmtId, surveyName, name, value, new Callback<Void>() {
              @Override
              public void handleSuccess(Void result) {
                doShowListPage();
              }
            });
          }
        }
      };
    } else if (Constants.ACTION_CMD_CUSTOM.equals(commandName)) {
      // Custom action menu command
      return new Command(){
        @Override
        public void execute() {
          final String action = menuDef.getAction();
          final String confirmMsg = menuDef.getConfirmMsg();
          final Map<String,String> menuDefParams = menuDef.getParameters();
          final Map<String,String> params = new HashMap<>(menuDefParams);
          final AssessmentId asmtId = patReg.getAssessmentId();
          if (confirmMsg != null) {
            // If there is a confirmation message the display the message in a confirm dialog box
            final Popup confirmPopup = makePopup("Confirm Action");
            confirmPopup.setModal(false);
            FlowPanel panel = new FlowPanel();
            panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
            Label msg = new Label(confirmMsg);
            msg.setWidth("300px");
            panel.add(msg);
            final Button cancelButton = new Button("Cancel");
            cancelButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                confirmPopup.hide();
              }
            });
            final Button okButton = new Button("OK");
            okButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                confirmPopup.hide();
                if (action != null) {
                  clinicService.customActionMenuCommand(action, asmtId, params, new Callback<Boolean>() {
                    @Override
                    public void handleSuccess(Boolean result) {
                      doShowListPage();
                    }
                  });
                }
              }
            });
            List<Button> buttons = new ArrayList<>();
            buttons.add(cancelButton);
            buttons.add(okButton);
            confirmPopup.setCustomButtons(buttons);
            confirmPopup.setGlassEnabled(true);
            confirmPopup.showMessage(panel);
          } else {
            // Call the custom action menu command
            if (action != null) {
              clinicService.customActionMenuCommand(action, asmtId, params, new Callback<Boolean>() {
                @Override
                public void handleSuccess(Boolean result) {
                  doShowListPage();
                }
              });
            }
          }
        }
      };
    }
    throw new IllegalArgumentException("Command \"" + commandName + "\" is not defined.");
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_CLINIC_STAFF;
  }

  public HorizontalPanel getButtonPanel() {
    return buttonPanel;
  }

  public ClinicServiceAsync getClinicService() {
    return clinicService;
  }

  public HorizontalPanel getSearchPanel() {
    return searchPanel;
  }

  public Button getSearchButton() {
    return searchButton;
  }

  private void setSearchBarStyles(Label lbl) {
    lbl.addStyleName(RegistryResources.INSTANCE.css().scheduleSearchBarLabel());
  }

  private String getCleanString(String str) {
    if (str == null) {
      return "";
    } else {
      return str.trim();
    }
  }

  static private class emailRunnable implements Runnable {

    @Override
    public void run() {
    }

    public void run(EmailSendStatus i) {
    }

  }

  private void findProviders() {
    clinicService.findDisplayProviders(new Callback<List<DisplayProvider>>() {
      @Override
      public void handleSuccess(List<DisplayProvider> result) {
        providers = result;
        for (DisplayProvider provider : providers) {
          if (providerStatus.get(provider.getProviderId()) == null) {
            providerStatus.put(provider.getProviderId(), Boolean.valueOf(false));
          } else if (providerStatus.get(provider.getProviderId())){
            providerStatus.put(provider.getProviderId(), !providerStatus.get(provider.getProviderId()));
          }
        }
        createproviderFilterButton();
      }
    });
  }

  private void createproviderFilterButton() {
    providerFilterButton = new PopdownButton().withText("All Providers").withMenu(new PopdownButton.Customizer() {
      @Override
      public void customizePopup(final PopdownButton button, Menu menu) {
        menu.addItem("All Providers", new Command() {
          @Override
          public void execute() {
            button.setText("All Providers");
            for (Long providerId : providerStatus.keySet()) {
              if (getProviderStatus(providerId)) {
                providerStatus.put(providerId, !providerStatus.get(providerId));
              }
            }
            FilteredProviders prefProviders = servicesPreferencesFactory.FilteredProviders().as();
            prefProviders.setProviders(new ArrayList<Long>());
            preferences.setProviderFilter(prefProviders);
            setPreferences();
            refreshDisplay();
          }
        });
        for (final DisplayProvider provider : providers) {
          final Long providerId = provider.getProviderId();
          menu.addItemChecked(provider.getDisplayName(), getProviderStatus(providerId), new Command() {
            @Override
            public void execute() {
              providerStatus.put(providerId, !providerStatus.get(providerId));
              button.setText(getProviderButtonText());

              // make call to update user preferences
              ArrayList<Long> preferredProviderList = new ArrayList<Long>();
              for(Long providerId : providerStatus.keySet()) {
                if (getProviderStatus(providerId)) {
                  preferredProviderList.add(providerId);
                }
              }
              FilteredProviders prefProviders = servicesPreferencesFactory.FilteredProviders().as();
              prefProviders.setProviders(preferredProviderList);
              preferences.setProviderFilter(prefProviders);
              setPreferences();
              refreshDisplay();
            }
          });
        }
        menu.addItem("Refresh List", new Command() {
          @Override
          public void execute() {
            findProviders();
          }
        });
      }
    });
  }

  private Boolean getProviderStatus(Long providerId) {
    Boolean status = providerStatus.get(providerId);
    if (status != null) {
      return status;
    }
    logger.log(Level.WARNING, "no status for providerId " + providerId);
    return false;
  }

  private String getProviderButtonText() {
    int selectedProviders=0;
    long selectedId=0;
    for(Long providerId : providerStatus.keySet()) {
      if (getProviderStatus(providerId)) {
        selectedProviders++;
        selectedId= providerId;
      }
    }
    if (selectedProviders == 1) {
      return getProviderName(selectedId);
    }
    if (selectedProviders > 1) {
      return "Selected providers";
    }
    return "All Providers";
  }

  private String getProviderName(Long providerId) {
    for (DisplayProvider provider : providers) {
      if (providerId.longValue() == provider.getProviderId().longValue()) {
        return provider.getDisplayName();
      }
    }
    return "";
  }
  private void saveClinicFilter() {
    preferences.setClinicFilter(clinicFilter);
    setPreferences();
  }

  private void saveSortChoice(String col, boolean asc) {
    preferences.setSchedSortColumn(col);
    preferences.setSchedSortAsc(asc);
    setPreferences(false);
  }

  private ClinicServicePreferences getPreferences() {
    preferences = null;
    if (getUtils().getUser().getUserPreferences("ScheduleTab") != null) {
      preferences = AutoBeanCodex.decode(servicesPreferencesFactory,
          ClinicServicePreferences.class, getUtils().getUser().getUserPreferences("ScheduleTab")).as();
    } else {
      preferences = servicesPreferencesFactory.ClinicServicesPreferences().as();
    }
    return preferences;
  }

  private void setPreferences(final boolean refresh) {
    AutoBean<ClinicServicePreferences> preferencesBean = AutoBeanUtils.getAutoBean(preferences);
    String preferencesJson = AutoBeanCodex.encode(preferencesBean).getPayload();
    getUtils().getUser().setUserPreferences("ScheduleTab", preferencesJson);
    clinicService.updateUserPreferences("ScheduleTab", preferencesJson, new Callback<Void>() {
      @Override
      public void handleSuccess(Void result) {
        if (refresh) {
          refreshDisplay();
        }
      }
    });
  }

  private void setPreferences() {
    setPreferences(true);
  }

  private String getPatientIdLabel() {
    return getUtils().getClientConfig().getParam(Constants.PATIENT_ID_LABEL);
  }

  private boolean notNullorEmpty(String value) {
    return (value != null && !value.isEmpty());
  }
}
