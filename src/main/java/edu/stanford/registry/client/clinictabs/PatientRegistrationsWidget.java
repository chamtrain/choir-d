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
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.TimePicker;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.utils.DisplayUtils;
import edu.stanford.registry.client.widgets.PageWidget;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.SurveyRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PatientRegistrationsWidget extends PageWidget implements HasClickHandlers {
  private final ClinicServiceAsync clinicService;

  private final FlexTable patientsTable = new FlexTable();
  private ArrayList<PatientRegistration> patientRegistrations = new ArrayList<>();
  protected ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();

  private ArrayList<TextBoxDatePicker> listDateField;
  private ArrayList<TimePicker> listTimeField;
  private ArrayList<ValidEmailAddress> listEmailTextBox;
  private ArrayList<Button> listDeleteButton;
  private final Label[] listLabels = { new Label("Appt"), new Label("Date"), new Label("Time"), new Label("Email"),
      new Label("Type"), new Label("PIN"), new Label("Survey"), new Label("Delete") };
  // private final PopupPanel popUp = new PopupPanel();
  private final DialogBox popUp = new DialogBox();
  public Button closeButton = new Button("Close");
  public Button saveButton = new Button("Save");
  private String patientId = null;
  private boolean changed = false;
  private final VerticalPanel dialogVPanel = new VerticalPanel();
  private final Logger logger = Logger.getLogger(PatientRegistrationsWidget.class.getName());

  public PatientRegistrationsWidget(ClinicUtils clinicUtils, ClinicServiceAsync clinicService) {
    super(clinicUtils);
    this.clinicService = clinicService;
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    closeButton.addClickHandler(handler);
    return addDomHandler(handler, ClickEvent.getType());
  }

  /**
   * Display this patients registrations in a pop-up window.
   *
   * @param patientId
   */
  public void popUpPatientRegistrations(final String patientId) {

    this.patientId = patientId;
    this.changed = false;

    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        popUp.hide();
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSaveChanges();
      }
    });
    dialogVPanel.clear();
    final HorizontalPanel dialogHPanel = new HorizontalPanel();

    Label title = new Label("Registration details for MRN: " + patientId);
    title.setStylePrimaryName("popUpLabel");
    dialogHPanel.setWidth("100%");
    dialogHPanel.add(title);
    dialogHPanel.add(saveButton);
    dialogHPanel.setCellHorizontalAlignment(saveButton, HorizontalPanel.ALIGN_RIGHT);
    dialogHPanel.setCellVerticalAlignment(saveButton, HorizontalPanel.ALIGN_MIDDLE);
    dialogHPanel.add(closeButton);
    dialogHPanel.setCellHorizontalAlignment(closeButton, HorizontalPanel.ALIGN_RIGHT);
    dialogHPanel.setCellVerticalAlignment(closeButton, HorizontalPanel.ALIGN_MIDDLE);

    dialogVPanel.add(dialogHPanel);
    dialogVPanel.add(super.getMessageBar());

    RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
    int screenWidth = rootLayoutPanel.getOffsetWidth();
    int screenHeight = rootLayoutPanel.getOffsetHeight();
    ScrollPanel scroller = new ScrollPanel();
    scroller.setPixelSize((int) (screenWidth * 0.6), (int) ((screenHeight) * 0.6));

    scroller.add(dialogVPanel);

    popUp.setWidget(scroller);
    popUp.center();
    popUp.show();
    getUtils().showLoadingPopUp();
    getRegistrations();

  }

  private void getRegistrations() {
    // call the service to get the data
    PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
    clinicService.searchForPatientRegistration(patientId, searchOptions, new Callback<ArrayList<PatientRegistration>>() {
      @Override
      public void handleSuccess(ArrayList<PatientRegistration> result) {
        if (result != null && result.size() > 0) {
          patientRegistrations = result;
          makePatientsTable();
          dialogVPanel.add(patientsTable);
          patientsTable.setVisible(true);
        } else {
          HTML message = new HTML("<P><P>No data found");
          dialogVPanel.add(message);
        }
        getUtils().hideLoadingPopUp();
      }
    });
  }

  private void makePatientsTable() {

    initRegistrationList();
    patientsTable.removeAllRows();
    setEmptyMessage();

    for (int i = 0; i < listLabels.length; i++) {
      listLabels[i].setStylePrimaryName("tableDataHeaderLabel");
      patientsTable.setWidget(0, i, listLabels[i]);
    }

    patientsTable.getRowFormatter().addStyleName(0, "tableDataHeader");
    patientsTable.addStyleName("dataList");

    boolean oddRow = false;
    for (int n = 0; n < patientRegistrations.size(); n++) {
      final PatientRegistration pReg = patientRegistrations.get(n);
      final int row = n + 1;
      int col = 0;

      patientsTable.setWidget(row, col, DisplayUtils.makeRegistrationTypeLabel(pReg));

      col++;

      // Date (and time for appointments)
      TextBoxDatePicker listDate = DisplayUtils.makelistDateField(getUtils(), pReg.getSurveyDt());
      listDate.setWidth("70px");
      listDateField.add(listDate);
      patientsTable.setWidget(row, col, listDate);
      col++;

      TimePicker tp = new TimePicker();
      logger.log(Level.INFO, "pReg time is " + pReg.getSurveyDt());
      tp.setTime(pReg.getSurveyDt());
      listTimeField.add(tp);
      if (pReg.isAppointment()) {
        patientsTable.setWidget(row, col, tp);
      }
      col++;

      // email address
      String emailString = "";
      if (pReg.getEmailAddr() != null) {
        emailString = pReg.getEmailAddr();
      }
      final int emailColumn = col;
      listEmailTextBox.add(getUtils().makeEmailField(emailString, new InvalidEmailHandler() {

        @Override
        public void onFailedValidation(InvalidEmailEvent event) {
          setErrorMessage(event.getMessage() + "!");
          if (patientsTable.getWidget(row, emailColumn) instanceof ValidEmailAddress) {
            final ValidEmailAddress emailAddr = (ValidEmailAddress) patientsTable.getWidget(row, emailColumn);
            emailAddr.setFocus(true);
            emailAddr.addStyleName("dataListTextError");
            emailAddr.addBlurHandler(new BlurHandler() {
              @Override
              public void onBlur(BlurEvent event) {
                setEmptyMessage();
                if (emailAddr.isValid()) {
                  emailAddr.removeStyleName("dataListTextError");
                }
              }
            });
          }
        }
      }));

      patientsTable.setWidget(row, col, listEmailTextBox.get(n));
      col++;
      patientsTable.setText(row, col, pReg.getSurveyType());
      col++;
      List<SurveyRegistration> surveys = pReg.getSurveyRegList();
      if (surveys.size() == 1) {
        patientsTable.setText(row, col, surveys.get(0).getToken());
      } else {
        ListBox lb = new ListBox();
        for(SurveyRegistration survey : surveys) {
          lb.addItem(survey.getToken());
        }
        patientsTable.setWidget(row,  col, lb);
      }
      col++;
      try {
        // show if the survey was completed or not.
        patientsTable.setWidget(row, col, getUtils().getCompletedImage(pReg.getIsDone()));
        col++;

        // delete button
        Button dButton = new Button(new Image(RegistryResources.INSTANCE.delete()).toString());
        dButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            deleteRegistration((Button) event.getSource(), pReg, row - 1); // row
            // includes
            // header
          }
        });
        dButton.setStylePrimaryName("imageButton");
        dButton.addStyleName("patientRegistrationDeleteButton");
        dButton.setTitle("Delete registration");
        listDeleteButton.add(dButton);
        if (!pReg.getIsDone() && pReg.getNumberPrints() == 0 &&
            pReg.getNumberCompleted() == 0 ) {
          patientsTable.setWidget(row, col, dButton);
        }
        if (oddRow) {
          oddRow = false;
          patientsTable.getRowFormatter().addStyleName(row, "x-odd");
        } else {
          patientsTable.getRowFormatter().addStyleName(row, "x-even");
          oddRow = true;
        }

      } catch (Exception e) {
        logger.log(Level.SEVERE, "Ignoring client error", e);
      }
    }

  }

  private void initRegistrationList() {

    listDeleteButton = new ArrayList<>();
    listEmailTextBox = new ArrayList<>();
    listDateField = new ArrayList<>();
    listTimeField = new ArrayList<>();
  }

  public void deleteRegistration(Button button, final PatientRegistration pReg, final int row) {
    final Popup actionPopup = getUtils().makePopup("Delete Registration");
    actionPopup.setModal(false);
    // make sure this shows on top of the dialog box
    int zIndex = 200;
    try {
      zIndex = Integer.parseInt(popUp.getElement().getStyle().getZIndex()) + 100;
    } catch (NumberFormatException nfe) {
      // ignore
    }
    actionPopup.getElement().getStyle().setZIndex(zIndex);
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());

    Button deleteButton = new Button("Delete");
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getUtils().showLoadingPopUp();
        clinicService.deletePatientRegistration(pReg, new Callback<Void>() {
          @Override
          public void handleSuccess(Void result) {
            actionPopup.hide();
            patientRegistrations.remove(row);
            getRegistrations();
            changed = true;
          }
        });
      }
    });
    Button cancelButton = new Button("Cancel");

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        actionPopup.hide();
      }
    });
    ArrayList<Button> actionButtons = new ArrayList<>();
    actionButtons.add(deleteButton);
    actionButtons.add(cancelButton);
    actionPopup.setCustomButtons(actionButtons);

    panel.add(new Label("Registration for"));
    Label name = new Label(pReg.getFirstName() + " " + pReg.getLastName() + " (DOB: "
        + getUtils().getDefaultDateFormat().format(pReg.getDtBirth()) + ")");
    name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(name);
    panel.add(new Label("Scheduled on"));
    Label sched = new Label(getUtils().getDefaultDateTimeFormat().format(pReg.getSurveyDt()) + "? ");
    sched.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(sched);
    actionPopup.setGlassEnabled(true);
    actionPopup.showMessage(panel);
    actionPopup.show();
  }

  private void doSaveChanges() {

    final ArrayList<PatientRegistration> changedRegistrations = new ArrayList<>();
    final ArrayList<Date> changedRegistrationTimes = new ArrayList<>();
    // check for changes in each row
    for (int i = 0; i < patientRegistrations.size(); i++) {
      boolean listChanged = false;

      PatientRegistration patReg = patientRegistrations.get(i);
      java.util.Date newDate = listTimeField.get(i).getTime(listDateField.get(i).getValue());

      /* See if the date or time has changed */
      if (patReg.getSurveyDt().getTime() != newDate.getTime()) {
        listChanged = true;
      }
      /* Validate the email exists, format is correct, then check for changes */
      if (listEmailTextBox.get(i).getValue() != null &&
          !listEmailTextBox.get(i).isValid()) {
        setErrorMessage("Patient " + patReg.getPatientId() + " Email address " + listEmailTextBox.get(i).getValue()
            + " is not valid ");
        listEmailTextBox.get(i).setStyleName("dataListTextError");
        return;
      } else if (listEmailTextBox.get(i).getValue() != null
          && !listEmailTextBox.get(i).getValue().equals(patReg.getEmailAddr())) {
        listChanged = true;
        patReg.setEmailAddr(listEmailTextBox.get(i).getValue());
      }
      if (listChanged) {
        changedRegistrations.add(patReg);
        changedRegistrationTimes.add(new Date(newDate.getTime()));
      }
    }
    if (changedRegistrations.size() > 0) {
      logger.log(Level.INFO, "writing pending changes");
      disableButtons();
      clinicService.updatePatientRegistrations(changedRegistrations, changedRegistrationTimes,
          new Callback<Void>() {
        @Override
        public void handleSuccess(Void result) {
          setSuccessMessage("Changes have been saved");
          for (int inx = 0; inx < changedRegistrations.size(); inx++) {
            for (PatientRegistration patientRegistration : patientRegistrations) {
              if (patientRegistration.getPatientId()
                  .equals(changedRegistrations.get(inx).getPatientId())) {
                patientRegistration.setSurveyDt(changedRegistrationTimes.get(inx));
              }
            }
          }
        }
      });
      enableButtons();
    } else {
      setSuccessMessage("No changes were made");
    }
  }

  private void disableButtons() {
    saveButton.setEnabled(false);
    closeButton.setEnabled(false);
  }

  private void enableButtons() {
    saveButton.setEnabled(true);
    closeButton.setEnabled(true);
  }

  public String getPatientId() {
    return patientId;
  }

  public boolean getPatientDataChanged() {
    return changed;
  }

}
