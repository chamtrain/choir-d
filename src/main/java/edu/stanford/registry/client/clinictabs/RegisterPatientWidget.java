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
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.ValidPatientId;
import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.event.InvalidPatientEvent;
import edu.stanford.registry.client.event.InvalidPatientHandler;
import edu.stanford.registry.client.event.ValidPatientEvent;
import edu.stanford.registry.client.event.ValidPatientHandler;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.client.service.RegistrationService;
import edu.stanford.registry.client.service.RegistrationServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.DeclinePopup;
import edu.stanford.registry.client.widgets.ImageLabel;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

public class RegisterPatientWidget extends TabWidget implements RegistryTabIntf, InvalidEmailHandler {

  /**
   * This widget is for Adding/Updating a patient as registered or declined without having to go through the search. It requires the REGISTRATION
   * role only.
   */
  private final RegistrationServiceAsync registrationService = GWT.create(RegistrationService.class);
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  // containers
  protected DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private FlowPanel centerPanel = new FlowPanel();
  private final int DT_ROWS = 6;
  private final int ST_ROWS = 2;
  private final int COLUMNS = 3;
  private final String CELL_WIDTH = "200px";
  private final String ELEMENT_WIDTH = "90%";
  private final Grid displayTable = new Grid(DT_ROWS, COLUMNS);
  private final Grid statusTable = new Grid(ST_ROWS, COLUMNS);
  // page components

  private final Label titleLabel = new Label("Register Patient");
  //private final Label mrnLabel;
  // private final Label mrnFormatLabel = new Label("xxxxxx-x");
  private final Label emailLabel = new Label("Email");
  private final Label firstNameLabel = new Label("First name");
  private final Label lastNameLabel = new Label("Last name");
  private final Label dobLabel = new Label("Date of birth");
  private final Label dobFormatLabel = new Label("MM/DD/YYYY");
  private final Label emptyLabel = new Label("");

  private ValidPatientId mrnTextBox;
  private ValidEmailAddress emailTextBox;
  private TextBox firstNameTextBox = new TextBox();
  private TextBox lastNameTextBox = new TextBox();
  private TextBox dobTextBox = new TextBox();
  private final Button registerButton = new Button(new Image(RegistryResources.INSTANCE.accept()).toString() +
      " Register this patient");
  private final Button declineButton = new Button(new Image(RegistryResources.INSTANCE.decline()).toString() +
      " Decline this patient");

  private final ImageLabel currentStatusLabel = new ImageLabel();
  private final Button lookupButton = new Button("Lookup");
  private final Button clearButton = new Button("Clear");
  private final Button registeredButton = new Button(new Image(RegistryResources.INSTANCE.accept()).toString() +
      " Registered");
  private final Button saveButton = new Button("Save");
  private final RadioButton registerRadio = new RadioButton("status", "Registered");
  private final RadioButton declineRadio = new RadioButton("status", "Declined");
  private Patient thisPatient = new Patient();
  private final Logger logger = Logger.getLogger(RegisterPatientWidget.class.getName());
  private Label declineReasonLabel=new Label ();
  private Label registerLabel = new Label("Assessments are enabled");

  public RegisterPatientWidget(ClientUtils utils) {
    super(utils);
    setServiceEntryPoint(registrationService, "registrationService");
    mainPanel.setStylePrimaryName("mainPanel");
    mainPanel.addNorth(messageBar, 4);
    initWidget(mainPanel);
  }

  @Override
  public void load() {

    setEmptyMessage();

    centerPanel.setWidth("600px");
    centerPanel.setStylePrimaryName(css.centerPanel());
    mainPanel.add(centerPanel);
    titleLabel.setStylePrimaryName(css.titleLabel());

    registerButton.addStyleName("mixedButton");
    declineButton.addStyleName("mixedButton");
    registerButton.setWidth("100%");
    declineButton.setWidth("100%");
    currentStatusLabel.setWidth("100%");
    registeredButton.setEnabled(false);
    registerButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (isValid()) {
          setPatientParticipation("y");
        }
      }
    });
    declineButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!isValid()) {
          return;
        }
        showDeclinePopup();
      }
    });

    declineRadio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!isValid()) {
          return;
        }
        registerLabel.setVisible(false);
        showDeclinePopup();
      }
    });
    registerRadio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        registerLabel.setVisible(true);
        declineReasonLabel.setText("");
      }
    });


    lookupButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        logger.log(Level.INFO, "in lookup click");
        setEmptyMessage();
        mrnTextBox.isValidPatient();
      }
    });

    clearButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        logger.log(Level.INFO, "in clearbutton click");
        setEmptyMessage();
        clear();
      }
    });
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setEmptyMessage();
        savePatient();
      }
    });
    emailTextBox = getClientUtils().makeEmailField(null, this);

    dobTextBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (dobTextBox.getText() != null) {
          if (!getClientUtils().isValidDate(dobTextBox.getValue())) {
            setErrorMessage(dobTextBox.getText() + " is not a valid date");
          }
        }
      }
    });

    doDetailsPage(null);

    /*
     * This is needed to set the focus when the page first loads.
     */
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        mrnTextBox.setFocus(true);
      }
    });

    declineReasonLabel.setWidth(ELEMENT_WIDTH);
    registerButton.setWidth(ELEMENT_WIDTH);
    declineButton.setWidth(ELEMENT_WIDTH);
  }

  private void doDetailsPage(Patient pat) {
    logger.log(Level.INFO, "dodetails starting");
    thisPatient = pat;
    resetPage();

    /*
     * Create the mrn field on startup.
     */
    if (mrnTextBox == null) {
      if (pat != null) {
        mrnTextBox = makeMrnField(pat.getPatientId());
      } else {
        mrnTextBox = makeMrnField("");
      }
    }
    Label mrnLabel = new Label(getPatientIdLabel());
    addRow(0, mrnLabel, mrnTextBox, lookupButton);
    mrnTextBox.setTabIndex(0);
    lookupButton.setTabIndex(1);

    /*
     * Only show email field if they've entered an mrn
     */
    if (pat != null || mrnTextBox.isNewPatient()) {
      if (pat != null && pat.getEmailAddress() != null) {
        emailTextBox.setValue(pat.getEmailAddress());
      }
      addRow(1, emailLabel, emailTextBox, clearButton);
    }
    emailTextBox.setTabIndex(2);
    clearButton.setTabIndex(3);
    int tabIndex = 4;

    /*
     * Populate the fields if looking at an existing patient
     */
    if (pat != null) { // && !isNew) {

      firstNameTextBox.setValue(pat.getFirstName());
      firstNameTextBox.setTabIndex(tabIndex);
      tabIndex++;

      lastNameTextBox.setValue(pat.getLastName());
      lastNameTextBox.setTabIndex(tabIndex);
      tabIndex++;
      try {
        if (getClientUtils() != null && pat.getDtBirth() != null) {
          String dobString = getClientUtils().getDateString(pat.getDtBirth());
          if (dobString != null) {
            dobTextBox.setValue(dobString);
          }

          if (pat.getDtCreated() != null) {
            String createString = getClientUtils().getDateString(new Date(pat.getDtCreated().getTime()));
            if (dobString.equals(createString)) {
              dobTextBox.setValue("-");
            }
          }
        }
      } catch (Exception caught) {
        logger.log(Level.SEVERE, "ERROR checking patients dob", caught);
      }
      dobTextBox.setTabIndex(tabIndex);
      tabIndex++;
      if (pat.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
        addRow(2, firstNameLabel, firstNameTextBox, saveButton);
      } else {
        addRow(2, firstNameLabel, firstNameTextBox);
      }
      addRow(3, lastNameLabel, lastNameTextBox);
      addRow(4, dobLabel, dobTextBox, dobFormatLabel);
    }

    centerPanel.add(displayTable);
    displayTable.setVisible(true);

    if (pat != null && pat.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
      /*
       * Show the radio buttons reflecting the patients current status
       */
      if (pat.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y")) {
        registerRadio.setValue(true);
        declineRadio.setValue(false);
        registerLabel.setVisible(true);
      } else {
        registerRadio.setValue(false);
        declineRadio.setValue(true);
        registerLabel.setVisible(false);
        String declinedOtherString = pat.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_OTHER);
        if (declinedOtherString != null) {
          declineReasonLabel.setText(declinedOtherString);
        } else {
          String declinedString = pat.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_CODE);
          if (declinedString != null) {
            declineReasonLabel.setText(declinedString);
            DeclineReason[] possibleReasons = DeclineReason.values();
            if (possibleReasons == null) {
              declineReasonLabel.setText("possibleReasons is null");
            } else {
              for (DeclineReason possibleReason : possibleReasons) {
                if (declinedString.equals(possibleReason.name())) {
                  declineReasonLabel.setText(possibleReason.getDisplayName());
                }
              }
            }
          }
        }
      }
      statusTable.setWidget(0, 1, registerRadio);
      statusTable.setWidget(0, 2, registerLabel);
      registerButton.setTabIndex(tabIndex);
      tabIndex++;
      statusTable.setWidget(1, 1, declineRadio);
      statusTable.setWidget(1, 2, declineReasonLabel);
    } else if (pat != null || mrnTextBox.isNewPatient()) {
      /*
       * Show buttons if existing patient or an mrn has been entered (not when cleared)
       */
      statusTable.setWidget(0, 1, registerButton);
      registerButton.setTabIndex(tabIndex);
      tabIndex++;
      statusTable.setWidget(1, 1, declineButton);
      declineButton.setTabIndex(tabIndex);
    }

    centerPanel.add(statusTable);
    if (pat != null || mrnTextBox.isNewPatient()) {
      setFocus(emailTextBox);
    } else {
      setFocus(mrnTextBox);
    }


  }

  private void clear() {
    mrnTextBox.setValue("", false);
    mrnTextBox.setNewPatient(false);
    doDetailsPage(null);
  }

  private void resetPage() {
    centerPanel.clear();
    centerPanel.add(titleLabel);
    displayTable.clear();
    displayTable.setStylePrimaryName(css.fixedList());
    displayTable.setCellSpacing(5);

    for (int row = 0; row < DT_ROWS; row++) {
      for (int column = 0; column < COLUMNS; column++) {
        displayTable.setHTML(row, column, "");
        displayTable.getCellFormatter().setWidth(row, column, CELL_WIDTH);
      }
    }

    statusTable.setStylePrimaryName(css.fixedList());
    statusTable.setCellSpacing(5);

    for (int row = 0; row < ST_ROWS; row++) {
      for (int column = 0; column < COLUMNS; column++) {
        statusTable.setHTML(row, column, "");
        statusTable.getCellFormatter().setWidth(row, column, CELL_WIDTH);
        statusTable.getCellFormatter().addStyleName(row, column, css.leftLabel());
      }
    }
    emailTextBox.setValue("");
    firstNameTextBox.setValue("");
    lastNameTextBox.setValue("");
    dobTextBox.setValue("");
    declineReasonLabel.setText("");

    centerPanel.add(displayTable);
    displayTable.setVisible(true);
    centerPanel.setWidth("600px");
  }

  private ValidPatientId makeMrnField(String mrnString) {

    final ValidPatientId mrn = new ValidPatientId(getClientUtils(), registrationService);
    mrn.setStylePrimaryName(css.dataListTextColumn());
    mrn.setInvalidStyleName(css.dataListTextError());
    mrn.setValidStyleName(css.dataListTextColumn());
    mrn.setValue(mrnString);
    mrn.addValidPatientHandler(new ValidPatientHandler() {
      @Override
      public void onPassedValidation(ValidPatientEvent event) {
        Patient validatedPatient = event.getPatient();
        if (validatedPatient != null) {
          setEmptyMessage();
          mrn.setValue(validatedPatient.getPatientId());
          mrn.setNewPatient(false);
          doDetailsPage(validatedPatient);
        }
      }
    });

    mrn.addInvalidPatientHandler(new InvalidPatientHandler() {
      @Override
      public void onFailedValidation(InvalidPatientEvent event) {
        if (event.formatError()) {
          setErrorMessage("Invalid " + getPatientIdLabel() + " format");
          mrn.setFocus(true);
        } else {
          setEmptyMessage();
          /** failed for patient not found **/
          if (event.getFormattedId() != null && event.getFormattedId().length() > 1) {
            mrn.setNewPatient(true);
            mrn.setStylePrimaryName(css.dataListTextColumn());
            mrn.setValue(event.getFormattedId());
            emailTextBox.setValue("");
            doDetailsPage(null);
          } else {
            logger.log(Level.INFO, "mrn:event error " + event.getMessage());
            setErrorMessage(event.getMessage());
          }
        }
      }
    });
    mrn.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {

        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
          logger.log(Level.INFO, "mrn:KEY_ENTER");

          mrn.isValidPatient();
        }
      }
    });
    return mrn;
  }

  private boolean isValid() {

    setEmptyMessage();
    if (mrnTextBox != null && mrnTextBox.getValue() != null && mrnTextBox.getValue().length() > 1
        && (mrnTextBox.isValid() || mrnTextBox.isNewPatient())) {
      if (thisPatient == null) {
        thisPatient = new Patient();
      }

      thisPatient.setPatientId(mrnTextBox.getValue());
      if ((emailTextBox != null) &&
          (emailTextBox.getValue() != null) &&
          (emailTextBox.getValue().length() > 1) &&
          emailTextBox.isValid()) {
        getClientUtils().setEmail(thisPatient, emailTextBox.getValue());
      }

      thisPatient.setFirstName("-");
      if (firstNameTextBox.getValue() != null && firstNameTextBox.getValue().length() > 0) {
        thisPatient.setFirstName(firstNameTextBox.getValue());
      }
      thisPatient.setLastName("-");
      if (lastNameTextBox.getValue() != null && lastNameTextBox.getValue().length() > 0) {
        thisPatient.setLastName(lastNameTextBox.getValue());
      }
      thisPatient.setDtBirth(new java.util.Date((new Date()).getTime()));
      if (dobTextBox.getValue() != null && dobTextBox.getValue().length() > 0
          && getClientUtils().isValidDate(dobTextBox.getValue())) {
        Date newDate = new Date(getClientUtils().getMidDay(dobTextBox.getText()));
        thisPatient.setDtBirth(new java.util.Date(newDate.getTime()));
      }
      return true;
    }
    setErrorMessage("Please enter a valid " + getPatientIdLabel());
    return false;
  }

  private void setPatientParticipation(String participatesValue) {
    if ("y".equals(participatesValue)) {
      if ((emailTextBox.getValue() != null) &&
          (emailTextBox.getValue().trim().length() > 1) &&
          (!emailTextBox.isValidString())) {
        setErrorMessage("Invalid email address");
        return;
      }
      getClientUtils().setEmail(thisPatient, emailTextBox.getValue());
    }

    getClientUtils().showLoadingPopUp();

    registrationService.setPatientParticipation(thisPatient, participatesValue,
        new edu.stanford.registry.client.service.Callback<Patient>() {
      @Override
      public void handleSuccess(Patient patient) {
        doDetailsPage(patient);
        getClientUtils().hideLoadingPopUp();
      }

      @Override
      protected void afterFailure() {
        getClientUtils().hideLoadingPopUp();
      }
    });

  }


  private void savePatient() {
    if ((emailTextBox.getValue() != null) &&
        (emailTextBox.getValue().trim().length() > 1) &&
        (!emailTextBox.isValidString())) {
      setErrorMessage("Invalid email address");
      return;
    }
    getClientUtils().setEmail(thisPatient, emailTextBox.getValue());

    // Set the first, last name, date of birth
    if (firstNameTextBox.getValue() != null && firstNameTextBox.getValue().length() > 0) {
      thisPatient.setFirstName(firstNameTextBox.getValue());
    }
    if (lastNameTextBox.getValue() != null && lastNameTextBox.getValue().length() > 0) {
      thisPatient.setLastName(lastNameTextBox.getValue());
    }
    if (dobTextBox.getValue() != null && dobTextBox.getValue().length() > 0 && !dobTextBox.getValue().equals("-")) {
      if (!getClientUtils().isValidDate(dobTextBox.getValue())) {
        setErrorMessage("Date of birth is invalid. Date must be in the form mm/dd/yyyy");
        return;
      }
      Date newDate = new Date(getClientUtils().getMidDay(dobTextBox.getText()));
      thisPatient.setDtBirth(new java.util.Date(newDate.getTime()));
    }
    // did they change participation option?
    String participateStr = thisPatient.getAttributeString(Constants.ATTRIBUTE_PARTICIPATES);
    if (participateStr != null) {
      if ("y".equals(participateStr) && declineRadio.getValue()) {
        setPatientParticipation("n");
        return; // the patient was updated
      } else if ("n".equals(participateStr) && registerRadio.getValue()) {
        setPatientParticipation("y");
        return; // the patient was updated
      }
    }

    getClientUtils().showLoadingPopUp();
    registrationService.updatePatient(thisPatient, new edu.stanford.registry.client.service.Callback<Patient>() {
      @Override
      public void handleSuccess(Patient patient) {
        doDetailsPage(patient);
        setSuccessMessage("Changes have been saved");
        getClientUtils().hideLoadingPopUp();
      }

      @Override
      protected void afterFailure() {
        getClientUtils().hideLoadingPopUp();
      }
    });

  }

  @Override
  public String serviceName() {
    return Constants.ROLE_DEVELOPER;
  }

  private void addRow(int row, Label fieldLabel, TextBox textBox, Label formatLabel) {
    addRow(row, fieldLabel, textBox);
    formatLabel.setStylePrimaryName(css.leftLabel());
    formatLabel.setWidth(ELEMENT_WIDTH); // "188px");

    displayTable.setWidget(row, 2, formatLabel);
  }

  private void addRow(int row, Label fieldLabel, TextBox textBox, Button button) {
    addRow(row, fieldLabel, textBox);

    emptyLabel.setStylePrimaryName(css.leftLabel());
    emptyLabel.setWidth(ELEMENT_WIDTH); // "188px");
    displayTable.setWidget(row, 2, emptyLabel);

    button.setWidth(ELEMENT_WIDTH);
    displayTable.setWidget(row, 2, button);

    emptyLabel.setStylePrimaryName(css.leftLabel());
    emptyLabel.setWidth(ELEMENT_WIDTH);
  }

  private void addRow(int row, Label fieldLabel, TextBox textBox) {
    fieldLabel.setStylePrimaryName(css.rightLabel());
    fieldLabel.setWidth(ELEMENT_WIDTH);
    displayTable.setWidget(row, 0, fieldLabel);
    textBox.setWidth(ELEMENT_WIDTH);
    displayTable.setWidget(row, 1, textBox);
  }

  @Override
  public void onFailedValidation(InvalidEmailEvent event) {
    if (emailTextBox != null && emailTextBox.getValue() != null) {
      setErrorMessage(emailTextBox.getValue() + " is not a valid email address");
    }
  }

  private void showDeclinePopup() {
    if (getClientUtils() == null) {
      Window.alert("NO UTILS!");
    }
    final Popup declinePopup = getClientUtils().makePopup("Decline assessments");
    declinePopup.setModal(false);

    final PatientAttribute declinedAttribute = thisPatient.setAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE, null);
    final PatientAttribute otherAttribute = thisPatient.setAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER, null);

    FlowPanel panel = new DeclinePopup(null, css).getDeclineReasonPanel(declinedAttribute, otherAttribute);
    List<Button> buttons = new ArrayList<>();
    final Button cancelButton = new Button("Done");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        declinePopup.hide();
        if (declinedAttribute.getDataValue() == null) {
          thisPatient.removeAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE);
          thisPatient.removeAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER);
        } else if (thisPatient.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_OTHER) != null
               && !thisPatient.attributeEquals(Constants.ATTRIBUTE_DECLINE_REASON_CODE, DeclineReason.other.name())) {
          thisPatient.setAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER, DeclineReason.other.name());
        }
        if (isValid()) {
          setPatientParticipation("n");
        }
      }
    });
    buttons.add(cancelButton);
    declinePopup.setCustomButtons(buttons);
    declinePopup.setGlassEnabled(true);
    declinePopup.showMessage(panel);
  }

  private String getPatientIdLabel() {
    return getClientUtils().getParam(Constants.PATIENT_ID_LABEL);
  }
      
}
