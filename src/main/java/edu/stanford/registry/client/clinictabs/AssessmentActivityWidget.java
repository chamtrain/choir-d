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

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.PatientButton;
import edu.stanford.registry.client.PatientRadioButton;
import edu.stanford.registry.client.PatientSearchResultsWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.utils.DisplayUtils;
import edu.stanford.registry.client.utils.HandleRegister;
import edu.stanford.registry.client.widgets.AssessmentActivityTree;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientActivity;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class AssessmentActivityWidget extends TabWidget implements ClickHandler {

  protected DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private FlowPanel activityPanel = new FlowPanel();
  private ArrayList<PatientActivity> activityList = new ArrayList<>();

  // Assessment list page components
  private HorizontalPanel searchPanel = new HorizontalPanel();
  private HorizontalPanel searchBar = new HorizontalPanel();
  private final Button searchButton = new Button("Go");
  private final Label showFromLabel = new Label("Activity ");
  private final Label showToLabel = new Label("To");
  private final ListBox searchTypeBox = new ListBox();

  private TextBoxDatePicker showFromPicker;
  private TextBoxDatePicker showToPicker;
  private Date fromDt;
  private Date toDt;

  private final Grid treeLabels = new Grid(1, 3);
  private final Widget[] listLabels = { new Label("MRN"), new Label("First name"), new Label("Last name"),
      new Label("Email address"), new Label("Appt"), treeLabels, new Image(RegistryResources.INSTANCE.accept()),
      new Image(RegistryResources.INSTANCE.detail()), new Image(RegistryResources.INSTANCE.edit()) };
  private FlexTable patientsTable = new FlexTable();

  private final ClinicServiceAsync clinicService;
  protected ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();

  private String searchString = null;
  private int searchType = 0;
  private static final String ODD = "x-odd";
  private static final String EVEN = "x-even";
  private static final Logger logger = Logger.getLogger(AssessmentActivityWidget.class.getName());
  private ShowPatientCallback patientCallback;
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private Long siteId;

  public interface ShowPatientCallback {
    void showPatient(Patient pat);
  }

  public AssessmentActivityWidget(ClinicUtils clinicUtils, ClinicServiceAsync clinicService, ShowPatientCallback patientCallback) {
    super(clinicUtils);
    this.clinicService = clinicService;
    this.patientCallback = patientCallback;
    // Assemble the main panel
    mainPanel.setStylePrimaryName("mainPanel");
    mainPanel.addNorth(messageBar, 2);
    initWidget(mainPanel);
    this.siteId = clinicUtils.getSiteId();
  }

  @Override
  public void load() {
    if (isLoaded()) {
      return;
    }

    // Add the search bar with start/end date & time to yesterday
    showFromPicker = new TextBoxDatePicker(getUtils().getDefaultDateFormat());
    showToPicker = new TextBoxDatePicker(getUtils().getDefaultDateFormat());
    showFromPicker.setStylePrimaryName(css.clTabPgHeadingBarText());
    showFromPicker.addStyleName("registrationSearchText");
    showToPicker.setStylePrimaryName(css.clTabPgHeadingBarText());
    showToPicker.addStyleName("registrationSearchText");
    showFromLabel.setStylePrimaryName(css.clTabPgHeadingBarLabel());
    showFromLabel.addStyleName("registrationSearchLabel");
    showToLabel.setStylePrimaryName(css.clTabPgHeadingBarLabel());
    showToLabel.addStyleName("registrationSearchLabel");

    searchButton.addStyleName("registrationSearchButton");
    searchTypeBox.addItem("Uncompleted");
    searchTypeBox.addItem("All assessments");
    searchTypeBox.setSelectedIndex(0);
    searchTypeBox.setStylePrimaryName(css.clTabPgHeadingBarList());
    searchTypeBox.addStyleName("registrationSearchList");

    // set from and to dates to start of today to now
    Date now = new Date();
    // Date yesterday = new
    // Date(getUtils().getStartOfDay(ClientUtils.addDays(now, -1)));
    // fromDt = new Date(yesterday.getTime() - ClientUtils.SECONDS_IN_A_DAY);
    fromDt = new Date(getUtils().getStartOfDay(now));
    toDt = now;
    showFromPicker.setValue(fromDt);
    showToPicker.setValue(toDt);

    searchBar.setStylePrimaryName(css.clTabPgHeadingSearchBar());
    searchBar.addStyleName("activityHeadingSearchBar");
    searchPanel.setWidth("100%");
    searchPanel.setStylePrimaryName(css.clTabPgHeadingBar());
    searchPanel.addStyleName("ActivityHeadingBar");
    searchPanel.add(searchBar);

    searchBar.add(showFromLabel);
    searchBar.add(showFromPicker);
    searchBar.add(showToLabel);
    searchBar.add(showToPicker);
    searchBar.add(searchTypeBox);
    searchBar.add(searchButton);
    mainPanel.addNorth(searchPanel, 4);

    // Add the list panel
    patientsTable.getRowFormatter().setStylePrimaryName(0, "tableDataHeader");
    patientsTable.setStylePrimaryName("patientDataList");
    patientsTable.addStyleName("activityTablelabel");
    patientsTable.setVisible(false);
    doShowListPage(); // populate the page with registrationz
    mainPanel.add(activityPanel);

    patientsTable.setStylePrimaryName("fixedList");
    patientsTable.addStyleName("dataList");

    // add click handlers
    searchButton.addClickHandler(this);
    setInitialFocus(showFromPicker);
    setLoaded(true);

  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();
    setEmptyMessage();

    logger.log(Level.INFO, "in onclick");

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
        setErrorMessage("Please enter a valid to Date");
        toDt = null;
      }
      doShowListPage();
    } else {
      logger.log(Level.INFO, "in onclick sender is unknown");
    }
  }

  private void doDetailsPage(final Patient pat) {
    patientCallback.showPatient(pat);
  }

  private void doShowListPage() {
    activityPanel.clear();
    activityPanel.setStylePrimaryName("activityPage");
    boolean includeCompletedAssessments = false;
    if (searchTypeBox.getSelectedIndex() > 0) {
      includeCompletedAssessments = true;
    }

    getUtils().showLoadingPopUp();
    clinicService.searchForActivity(fromDt, toDt, includeCompletedAssessments,
        new AsyncCallback<ArrayList<PatientActivity>>() {
          @Override
          public void onFailure(Throwable caught) {
            getUtils().hideLoadingPopUp();
            basicErrorPopUp.setText("Encountered the following error on getting assessment activity");
            basicErrorPopUp.setError(caught.getMessage());
          }

          @Override
          public void onSuccess(ArrayList<PatientActivity> result) {
            activityList = result;
            if (activityList != null && activityList.size() > 0) {
              logger.log(Level.INFO, "Succes! got " + result.size());
              makeListTable(true);
              activityPanel.add(patientsTable);
              patientsTable.setVisible(true);
              activityPanel.setVisible(true);
            } else {
              setErrorMessage("No activity found during that time frame");
            }
            getUtils().hideLoadingPopUp();
          }

        });
  }

  private void makeListTable(boolean includeEditLink) {

    String lineStyle = ODD;
    // patientsTable.addStyleName("dataList");
    patientsTable.removeAllRows();
    patientsTable.getCellFormatter().setWidth(0, 0, "62px"); // MRN
    patientsTable.getCellFormatter().setWidth(0, 1, "75px"); // First name
    patientsTable.getCellFormatter().setWidth(0, 2, "85px"); // Last name
    patientsTable.getCellFormatter().setWidth(0, 3, "150px"); // Email
    patientsTable.getCellFormatter().setWidth(0, 4, "40px"); // Appt
    patientsTable.getCellFormatter().setWidth(0, 5, "500px"); // Tree
    patientsTable.getCellFormatter().setWidth(0, 6, "24px"); // Image
    patientsTable.getCellFormatter().setWidth(0, 7, "24px"); // Image
    patientsTable.getCellFormatter().setWidth(0, 8, "24px"); // Image
    for (int i = 0; i < listLabels.length; i++) {
      listLabels[i].setStylePrimaryName(css.tableDataHeaderLabel());
      patientsTable.setWidget(0, i, listLabels[i]);
    }

    patientsTable.getRowFormatter().addStyleName(0, "tableDataHeader");

    for (int n = 0; n < activityList.size(); n++) {
      final PatientActivity act = activityList.get(n);
      final Patient detailPatient = act.getPatient();
      final ApptRegistration registration = act.getRegistration();
      int row = n + 1;
      int col = 0;
      // mrn, first & last name
      if (detailPatient != null) {
        Label mrn = new Label(detailPatient.getPatientId().toString());
        mrn.setStylePrimaryName("mrn");
        patientsTable.setWidget(row, col, mrn);
        // patientsTable.getCellFormatter().addStyleName(row, col, "mrn");
        col++;
        Label fnameLbl = new Label(detailPatient.getFirstName());
        // fnameLbl.setWidth("80px");
        patientsTable.setWidget(row, col, fnameLbl);
        col++;
        Label lnameLbl = new Label(detailPatient.getLastName());
        // lnameLbl.setWidth("100px");
        patientsTable.setWidget(row, col, lnameLbl);
        col++;
      }
      // email address
      if (registration != null) {
        patientsTable.setWidget(row, col, new Label(registration.getEmailAddr()));
        // -- makeEmailField(row, col, registration.getEmailAddr()));
        col++;

        // Indicates if its an appointment
        patientsTable.setWidget(row, col, DisplayUtils.makeRegistrationTypeLabel(registration));
        col++;

        // Displays the activity tree heading
        AssessmentActivityTree activityDetails = new AssessmentActivityTree(act, getUtils(), lineStyle);
        activityDetails.setStylePrimaryName("activity-tree-widget");
        patientsTable.setWidget(row, col, activityDetails);
        col++;
        boolean completed = false;
        try {
          /*
           * Show an image indicating whether or not they completed the surveys
           * for this registration.
           */
          if (activityList.get(n).getActivities() != null) {
            for (int i = 0; i < activityList.get(n).getActivities().size(); i++) {
              if (Constants.ACTIVITY_COMPLETED.equals(activityList.get(n).getActivity(i).getActivityType())) {
                completed = true;
              }
            }
          }
          patientsTable.setWidget(row, col, getUtils().getCompletedImage(completed));
          col++;
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        if (completed) {
          PatientButton historyButton = getUtils().getHistoryButton(activityList.get(n).getPatient(), registration);
          historyButton.setStylePrimaryName(css.imageButton());
          patientsTable.setWidget(row, col, historyButton);
        }
        col++;

        // Add a link to go to the edit page for this patient.
        if (includeEditLink) {
          PatientButton detailsButton = new PatientButton(new Image(RegistryResources.INSTANCE.edit()).toString());
          detailsButton.setPatient(detailPatient);
          detailsButton.setTitle("View patients activity details");
          detailsButton.setStylePrimaryName(css.imageButton());
          detailsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              PatientButton sender = (PatientButton) event.getSource();
              Patient listPatient = sender.getPatient();
              getUtils().showLoadingPopUp();
              clinicService.getPatient(listPatient.getPatientId(), new AsyncCallback<Patient>() {
                @Override
                public void onFailure(Throwable caught) {
                  getUtils().hideLoadingPopUp();
                  basicErrorPopUp.setText("Encountered the following error when searching ");
                  basicErrorPopUp.setError(caught.getMessage());
                }

                @Override
                public void onSuccess(Patient result) {
                  doDetailsPage(result);
                  getUtils().hideLoadingPopUp();
                }
              });

            }
          });
          patientsTable.setWidget(row, col, detailsButton);
          col++;
        }

        patientsTable.getRowFormatter().addStyleName(row, lineStyle);
        if (ODD.equals(lineStyle)) {
          lineStyle = EVEN;
        } else {
          lineStyle = ODD;
        }

        // for (int i = 0; i < 8; i++) {
        // patientsTable.getFlexCellFormatter().addStyleName(row, i,
        // "activityTableLabel");
        // }
      }
    }

  }

  public void searchForPatients(int searchType, String searchString) {
    if (!isLoaded()) {
      load();
    }
    this.searchString = searchString;
    this.searchType = searchType;

    getUtils().showLoadingPopUp();
    if (searchType == ClientUtils.PATIENT_SEARCH_BY_PATIENT_ID) {
      logger.log(Level.INFO, "Searching by patientId");
      clinicService.searchForPatientsByPatientId(siteId, searchString, searchCallback);
    } else { // search by name
      logger.log(Level.INFO, "Searching by name");
      clinicService.searchForPatientsByName(searchString.toLowerCase(), searchCallback);
    }
  }

  private Callback<ArrayList<Patient>> searchCallback = new Callback<ArrayList<Patient>>() {
    @Override
    protected void afterFailure() {
      getUtils().hideLoadingPopUp();
    }

    @Override
    public void handleSuccess(ArrayList<Patient> result) {
      getUtils().hideLoadingPopUp();
      setEmptyMessage();
      if (result != null && result.size() == 1) {
        logger.log(Level.INFO, "One patient found");
        load();
        doDetailsPage(result.get(0));
      } else {
        final PatientSearchResultsWidget searchResults = new PatientSearchResultsWidget();
        HandleRegister handleRegister = new HandleRegister(logger, getUtils(), clinicService, false) {
          @Override
          public void successOnSetRegisteredOrDeclined(Patient result) {
            doDetailsPage(result);
          }
        };
        searchResults.addChangeHandler(handleRegister.getConsentChangeHandler());

        if (result == null || result.size() < 1) {
          logger.log(Level.INFO, "No patients found for: " + searchString);
        } else {
          logger.log(Level.INFO, "More than 1 patient found");
          searchResults.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              if (event.getSource() instanceof PatientRadioButton) {
                Patient pat = ((PatientRadioButton) event.getSource()).getPatient();
                doDetailsPage(pat);
              }
            }
          });
        }
        searchResults.showPatients(result, searchString, searchType);
      }
    }
  };


  @Override
  public String serviceName() {
    return "ClinicServices";
  }
}
