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

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.clinictabs.PatientAssessmentConfigWidget;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.PopdownAny.Customizer;
import edu.stanford.registry.client.widgets.PopupRelative.Align;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PatientHeader extends FlexTable {
  private final ClinicServiceAsync clinicService;
  protected ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();
  private DateTimeFormat fmt;
  private ClinicUtils utils;

  private static final Image editImage = new Image(RegistryResources.INSTANCE.pageEdit());
  private static final Image saveImage = new Image(RegistryResources.INSTANCE.save());
  private static final Image cnclImage = new Image(RegistryResources.INSTANCE.close());
  private static final Image validImage = new Image(RegistryResources.INSTANCE.accept());
  private static final Image invalidImage = new Image(RegistryResources.INSTANCE.decline());
  private static final String CELL_LABEL_WIDTH = "160px";
  private static final String CELL_DATA_WIDTH = "265px";
  private static final String EDIT_LABEL_WIDTH = "200px";
  private static final String IMAGE_WIDTH = "20px";
  private static final String IMAGE_HEIGHT = "25px";

  public final static String[] GENDER = { "Female", "Male" };
  public final static String[] YESNO = { "", "Y", "N" };

  private InvalidEmailHandler emailHandler;
  private Patient pat;
  private String patientIdentificationViewVs = "0";
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public PatientHeader(ClinicUtils clinicUtils, ClinicServiceAsync clinicService, Patient patient,
                       InvalidEmailHandler emailHandler) {
    this.clinicService = clinicService;
    this.emailHandler = emailHandler;
    setStylePrimaryName(css.fixedList());
    addStyleName(css.patientHeader());
    fmt = DateTimeFormat.getFormat(clinicUtils.getDateFormatString());
    utils = clinicUtils;
    this.pat = patient;
    if (clinicUtils.getClientConfig().getParams().containsKey("patientIdentificationViewVs")) {
      patientIdentificationViewVs = clinicUtils.getClientConfig().getParams().get("patientIdentificationViewVs");
    }
    fillPatientHeader();
  }

  public void refresh() {
    fillPatientHeader();
  }

  private void fillPatientHeader() {
    clear();
    setCellSpacing(0);
    setCellPadding(4);
    getColumnFormatter().setWidth(0, CELL_LABEL_WIDTH);
    getColumnFormatter().setWidth(1, CELL_DATA_WIDTH);
    getColumnFormatter().setWidth(2, CELL_LABEL_WIDTH);
    getColumnFormatter().setWidth(3, CELL_DATA_WIDTH);
    if (pat == null) {
      pat = new Patient();
    }

    // These are the labels+data pairs in 2 columns, even columns are on the left
    int position = 0;

    if ("0".equals(patientIdentificationViewVs)) {  // old style, all fields in the grid
      addDisplayField(position++, utils.getParam(Constants.PATIENT_ID_LABEL), pat.getPatientId());
      addDisplayField(position++, "First Name:", pat.getFirstName());
      String dob = (pat.getDtBirth() == null) ? null : fmt.format(pat.getDtBirth());
      addDisplayField(position++, "Date of Birth:", dob);
      addDisplayField(position++, "Last Name:", pat.getLastName());
      addField(position++, "Gender:", getEditableAttributePanel(Constants.ATTRIBUTE_GENDER, GENDER));
    } else {  // new style, omit fields that are in the PatientIdentification header
      Label genderLabel = new Label("Gender:");
      genderLabel.getElement().setAttribute("style", "padding-top: 6px;");
      genderLabel.addStyleName(css.patientIdColorLabel());
      HorizontalPanel genderWidget = getEditableAttributePanel(Constants.ATTRIBUTE_GENDER, GENDER);
      genderWidget.addStyleName(css.patientIdColorLabel());
      addField(position++, genderLabel, genderWidget);
    }

    addField(position++, "Email Address:", getEmailPanel());

    Set<String> attributeNames = utils.getClientConfig().getCustomPatientAttributeNames();
    if (attributeNames != null) {
      boolean even = true;
      for (String dataName : attributeNames) {
        addField(position++, utils.getClientConfig().getCustomPatientAttributeHeading(dataName),
            getEditableAttributePanel(dataName, utils.getClientConfig().getCustomPatientAttributeValues(dataName)));
        even = !even;
      }
      if (!even) {
        addField(position++, "", new HorizontalPanel()); // add a blank one in the last row
      }
    }

    final Label lastApptLabel = new Label(" - ");
    lastApptLabel.addStyleName(css.leftLabel());
    lastApptLabel.addStyleName(css.patientHeaderValue());
    addField(position++, "Last Appointment:", lastApptLabel);
    clinicService.getPatientsLastAppointmentDate(pat.getPatientId(), new AsyncCallback<Date>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error getting patients last appointment date.");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(Date result) {
        if (result != null) {
          lastApptLabel.setText(fmt.format(result));
        }
      }
    });

    final Label lastSurveyLabel = new Label(" - ");
    lastSurveyLabel.addStyleName(css.leftLabel());
    lastSurveyLabel.addStyleName(css.patientHeaderValue());
    addField(position++, "Last Survey Completed:", lastSurveyLabel);
    clinicService.getPatientsLastSurveyDate(pat.getPatientId(), new AsyncCallback<Date>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error getting patients last survey date.");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(Date result) {
        if (result != null) {
          lastSurveyLabel.setText(fmt.format(result));
        }
      }
    });

    final Label nextApptLabel = new Label(" - ");
    nextApptLabel.addStyleName(css.leftLabel());
    nextApptLabel.addStyleName(css.patientHeaderValue());
    addField(position++, "Next Appointment:", nextApptLabel);
    clinicService.getPatientsNextAppointmentDate(pat.getPatientId(), new AsyncCallback<Date>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error getting patients last appointment date.");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(Date result) {
        if (result != null) {
          nextApptLabel.setText(fmt.format(result));
        }
      }
    });

    final Label nextSurveyLabel = new Label(" - ");
    nextSurveyLabel.addStyleName(css.leftLabel());
    nextSurveyLabel.addStyleName(css.patientHeaderValue());
    addField(position++, "Next Survey due:", nextSurveyLabel);
    clinicService.getPatientsNextSurveyDueDate(pat.getPatientId(), new AsyncCallback<Date>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error getting patients next survey date.");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(Date result) {
        if (result != null) {
          nextSurveyLabel.setText(fmt.format(result));
        }
      }
    });

    String configValue = utils.getParam(Constants.ENABLE_CUSTOM_ASSESSMENT_CONFIG);
    if ((configValue != null && configValue.equalsIgnoreCase("y")) && utils.getUser()
        .hasRole(Constants.ROLE_ASSIGN_ASSESSMENT, utils.getClientConfig().getSiteName())) {
      final Button patientAssessmentConfig = new Button("Configure");
      patientAssessmentConfig.addStyleName(css.leftLabel());
      patientAssessmentConfig.addStyleName(css.patientHeaderValue());
      addField(position++, "Assigned Assessment:", patientAssessmentConfig);

      final Popup enrollPopup = makePopup("Configuring instruments for: " + pat.getLastName() + ", "+ pat.getLastName());
      enrollPopup.setModal(true);

      patientAssessmentConfig.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if(!enrollPopup.isShowing()){
            PatientAssessmentConfigWidget patientAssessmentConfigWidget = new PatientAssessmentConfigWidget(utils, pat);
            enrollPopup.showMessage(patientAssessmentConfigWidget);
          }
        }
      });
    }

    // this.setWidth("80%");
    FlexCellFormatter cellFormatter = getFlexCellFormatter();
    int lastRow = this.getRowCount() - 1;

    for (int rowIndex = 0; rowIndex < this.getRowCount(); rowIndex++) {
      cellFormatter.setStyleName(rowIndex, 0, css.phName());
      cellFormatter.setStyleName(rowIndex, 2, css.phName());
      cellFormatter.setStyleName(rowIndex, 1, css.phValue());
      cellFormatter.setStyleName(rowIndex, 3, css.phValue());
      if (rowIndex == 0) {
        if ("0".equals(patientIdentificationViewVs)) {
          cellFormatter.addStyleName(rowIndex, 0, css.phTopLeft());
        }
        cellFormatter.addStyleName(rowIndex, 1, css.phTopMid());
        cellFormatter.addStyleName(rowIndex, 2, css.phTopMid());
        if ("0".equals(patientIdentificationViewVs)) {
          cellFormatter.addStyleName(rowIndex, 3, css.phTopRight());
        }
      } else if (rowIndex == lastRow) {
        cellFormatter.addStyleName(rowIndex, 0, css.phBotLeft());
        cellFormatter.addStyleName(rowIndex, 1, css.phBotMid());
        cellFormatter.addStyleName(rowIndex, 2, css.phBotMid());
        cellFormatter.addStyleName(rowIndex, 3, css.phBotRight());
      }
    }
  }

  private void addDisplayField(int position, String label, String value) {
    Label field = new Label("");
    field.addStyleName(css.leftLabel());
    field.addStyleName(css.patientHeaderValue());
    value = (value == null) ? "" : value;
    field.setText(value);
    addField(position, label, field);
  }

  private void addField(int position, String label, Widget widget) {
    int row = position / 2;
    int col = (position % 2);
    setWidget(row, col*2, new Label(label));
    setWidget(row, (col*2)+1, widget);
  }

  private void addField(int position, Label label, Widget widget) {
    int row = position / 2;
    int col = (position % 2);
    setWidget(row, col*2, label);
    setWidget(row, (col*2)+1, widget);
  }

  private void updatePatientAttribute(String attributeName, String newValue) {
    PatientAttribute attribute = pat.getAttribute(attributeName);

    String oldValue = (attribute != null) ? attribute.getDataValue() : null;
    if (oldValue != null) {
      oldValue = oldValue.trim();
      if (oldValue.isEmpty()) {
        oldValue = null;
      }
    }

    if (newValue != null) {
      newValue = newValue.trim();
      if (newValue.isEmpty()) {
        newValue = null;
      }
    }

    if (newValue == null) {
      if (attribute != null) {
        pat.removeAttribute(attributeName);
        deletePatientAttribute(attribute);
      }
    } else if (attribute == null) {
      attribute = new PatientAttribute(pat.getPatientId(), attributeName, newValue, PatientAttribute.STRING);
      pat.addAttribute(attribute);
      addPatientAttribute(attribute);
    } else if (!newValue.equals(oldValue)) {
      attribute.setDataValue(newValue);
      addPatientAttribute(attribute);
    }
  }

  private void addPatientAttribute(PatientAttribute pAttribute) {
    clinicService.addPatientAttribute(pAttribute, new AsyncCallback<PatientAttribute>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error when updating patient");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(PatientAttribute result) { // do nothing
        if (result == null) {
          basicErrorPopUp.setText("Updated failed");
          basicErrorPopUp.setError("Contact support");
        }
      }
    });
  }

  private void deletePatientAttribute(PatientAttribute pAttribute) {
    clinicService.deletePatientAttribute(pAttribute, new AsyncCallback<Integer>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered the following error when updating patient");
        basicErrorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(Integer result) { // do nothing
        if (result == null) {
          basicErrorPopUp.setText("Updated failed");
          basicErrorPopUp.setError("Contact support");
        }
      }
    });
  }

  private HorizontalPanel getEditableAttributePanel(final String attributeName, String[] values) {
    final HorizontalPanel panel = new HorizontalPanel();
    final Button editButton = new Button(editImage.toString());
    final Button saveButton = new Button(saveImage.toString());
    final Button cnclButton = new Button(cnclImage.toString());

    panel.addStyleName(css.patientHeaderValue());
    panel.setWidth(CELL_DATA_WIDTH);

    final Label dataLabel = new Label(" ");
    PatientAttribute pattribute = pat.getAttribute(attributeName);
    if (pattribute == null) {
      pattribute = new PatientAttribute(pat.getPatientId(), attributeName, null, PatientAttribute.STRING);
    }
    dataLabel.setText(getDisplayFromValue(pattribute.getDataValue()));
    dataLabel.setWidth(EDIT_LABEL_WIDTH);
    dataLabel.addStyleName(css.leftLabel());
    dataLabel.addStyleName(css.patientHeaderValue());

    panel.add(dataLabel);
    //panel.addStyleName("registrationCurrentStatus");

    editButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    editButton.setTitle("Change");
    //editButton.addStyleName("paddedButton");
    panel.add(editButton);

    saveButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    saveButton.setTitle("Save changes");
    //saveButton.addStyleName("paddedButton");

    cnclButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    cnclButton.setTitle("Cancel changes");
    //.addStyleName("paddedButton");

    if (Arrays.equals(values, ClientConfig.PATIENT_ATTRIBUTE_TYPE_DATE)) {
      // Date picker field
      final DateTimeFormat dateFormat = utils.getDefaultDateFormat();
      final TextBoxDatePicker editBox = new TextBoxDatePicker(dateFormat, false);
      editButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          PatientAttribute attribute = pat.getAttribute(attributeName);
          if (attribute == null) {
            attribute = new PatientAttribute(pat.getPatientId(), attributeName, "", PatientAttribute.STRING);
          }
          Date value = null;
          if (attribute.getDataValue() != null) {
            try {
              value = dateFormat.parse(attribute.getDataValue());
            } catch(IllegalArgumentException e) {
              value = null;
            }
          }
          editBox.setValue(value);
          editBox.setWidth(EDIT_LABEL_WIDTH);
          panel.clear();
          panel.add(editBox);
          panel.add(saveButton);
          panel.add(cnclButton);
        }
      });
      saveButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String newValue = null;
          if (editBox.getValue() != null) {
            newValue = dateFormat.format(editBox.getValue());
          }
          updatePatientAttribute(attributeName, newValue);
          dataLabel.setText(newValue);

          panel.clear();
          panel.add(dataLabel);
          panel.add(editButton);
        }
      });
    } else if (values.length > 0) {
      // Drop down list field
      final ListBox listBox = new ListBox();
      for (int v = 0; v < values.length; v++) {
        listBox.addItem(getDisplayFromValue(values[v]));
        listBox.setValue(v, values[v]);
        listBox.setWidth(EDIT_LABEL_WIDTH);
        listBox.getElement().getStyle().setProperty("float", "left");
      }
      editButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          PatientAttribute attribute = pat.getAttribute(attributeName);
          if (attribute == null) {
            attribute = new PatientAttribute(pat.getPatientId(), attributeName, "", PatientAttribute.STRING);
          }
          for (int l = 0; l < listBox.getItemCount(); l++) {
            if (listBox.getValue(l).equals(attribute.getDataValue())) {
              listBox.setSelectedIndex(l);
            }
          }
          panel.clear();
          panel.add(listBox);
          panel.add(saveButton);
          panel.add(cnclButton);
        }
      });
      saveButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String newValue = "";
          if (listBox.getSelectedIndex() > -1) {
            newValue = listBox.getValue(listBox.getSelectedIndex());
          }
          updatePatientAttribute(attributeName, newValue);
          dataLabel.setText(getDisplayFromValue(newValue));

          panel.clear();
          panel.add(dataLabel);
          panel.add(editButton);
        }
      });
    } else {
      // Text field
      final TextBox editBox = new TextBox();
      editButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          PatientAttribute attribute = pat.getAttribute(attributeName);
          if (attribute == null) {
            attribute = new PatientAttribute(pat.getPatientId(), attributeName, "", PatientAttribute.STRING);
          }
          editBox.setValue(attribute.getDataValue());
          editBox.setWidth(EDIT_LABEL_WIDTH);
          panel.clear();
          panel.add(editBox);
          panel.add(saveButton);
          panel.add(cnclButton);
        }
      });
      saveButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          String newValue = editBox.getValue();
          updatePatientAttribute(attributeName, newValue);
          dataLabel.setText(newValue);

          panel.clear();
          panel.add(dataLabel);
          panel.add(editButton);
        }
      });
    }
    cnclButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        panel.clear();
        panel.add(dataLabel);
        panel.add(editButton);
      }
    });
    return panel;
  }

  private String getDisplayFromValue(String text) {
    if (text == null || "".equals(text)) {
      return " ";
    }

    if (text.equalsIgnoreCase("Y")) {
      return "Yes";
    } else if (text.equalsIgnoreCase("N")) {
      return "No";
    }

    return text;
  }

  private HorizontalPanel getEmailPanel() {
    final HorizontalPanel emailPanel = new HorizontalPanel();
    final Label emailValue = new Label();
    final ValidEmailAddress emailTextBox = utils.makeEmailField("", emailHandler);
    final Button editEmailButton = new Button(editImage.toString());
    final Button saveEmailButton = new Button(saveImage.toString());
    final Button cnclEmailButton = new Button(cnclImage.toString());
    final Button validEmailButton = new Button(validImage.toString());

    emailPanel.setWidth(CELL_DATA_WIDTH);
    emailPanel.addStyleName(css.patientHeaderValue());

    emailValue.setWidth(EDIT_LABEL_WIDTH);
    emailValue.addStyleName(css.leftLabel());
    emailValue.getElement().getStyle().setProperty("float", "left");

    emailTextBox.setWidth(EDIT_LABEL_WIDTH);
    emailTextBox.getElement().getStyle().setProperty("float", "left");

    editEmailButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    editEmailButton.setTitle("Edit email address");
    editEmailButton.addStyleName(css.scheduleTableImage());

    editEmailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String emailAddr = "";
        if ((pat != null) && (pat.getEmailAddress() != null)) {
          emailAddr = pat.getEmailAddress();
        }
        emailTextBox.setValue(emailAddr);
        emailPanel.clear();
        emailPanel.add(emailTextBox);
        emailPanel.add(saveEmailButton);
        emailPanel.add(cnclEmailButton);
      }
    });

    saveEmailButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    saveEmailButton.addStyleName(css.scheduleTableImage());
    saveEmailButton.setTitle("Save changes");

    saveEmailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // Get current email address value
        String emailAddr = "";
        if ((pat != null) && (pat.getEmailAddress() != null)) {
          emailAddr = pat.getEmailAddress();
        }
        // Get new email address value
        String newEmailAddr = "";
        if ((emailTextBox.getValue() != null)) {
          if (!emailTextBox.isValidString()) {
            return;
          }
          newEmailAddr = emailTextBox.getValue();
        }
        // If changed then update the patient
        if (!newEmailAddr.equals(emailAddr)) {
          utils.updatePatientEmail(pat, newEmailAddr, new Runnable() {
            @Override
            public void run() {
            }
          });
          // Updating the patient email also resets the survey email address valid attribute
          validEmailButton.setHTML(validImage.toString());
          validEmailButton.setEnabled(!ClientUtils.isEmpty(newEmailAddr));
          pat.removeAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID);
        }
        // Reset the panel
        emailValue.setText(newEmailAddr);
        emailPanel.clear();
        emailPanel.add(emailValue);
        emailPanel.add(validEmailButton);
        emailPanel.add(editEmailButton);
      }
    });

    cnclEmailButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    cnclEmailButton.addStyleName(css.scheduleTableImage());
    cnclEmailButton.setTitle("Cancel changes");

    cnclEmailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String emailAddr = "";
        if ((pat != null) && (pat.getEmailAddress() != null)) {
          emailAddr = pat.getEmailAddress();
        }
        emailValue.setText(emailAddr);
        emailPanel.clear();
        emailPanel.add(emailValue);
        emailPanel.add(validEmailButton);
        emailPanel.add(editEmailButton);
      }
    });

    validEmailButton.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    validEmailButton.addStyleName(css.scheduleTableImage());
    validEmailButton.setTitle("Set email address valid/invalid");

    validEmailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        PopdownAny popdown = new PopdownAny(validEmailButton.getElement(), Align.BELOW_LEFT, new Customizer() {
          @Override
          public void customizePopup(Menu menu) {
            menu.addItem("Email address is valid", new Command() {
              @Override
              public void execute() {
                validEmailButton.setHTML(validImage.toString());
                updatePatientAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID, "y");
              }
            });
            menu.addItem("Email address is invalid", new Command() {
              @Override
              public void execute() {
                validEmailButton.setHTML(invalidImage.toString());
                updatePatientAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID, "n");
              }
            });
          }
        });
        popdown.show();
      }
    });

    String emailAddr = "";
    if ((pat != null) && (pat.getEmailAddress() != null)) {
      emailAddr = pat.getEmailAddress();
    }
    emailValue.setText(emailAddr);

    PatientAttribute emailValidAttr = pat.getAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID);
    if ((emailValidAttr != null) && "n".equalsIgnoreCase(emailValidAttr.getDataValue())) {
      validEmailButton.setHTML(invalidImage.toString());
    }

    validEmailButton.setEnabled(!ClientUtils.isEmpty(emailAddr));

    emailPanel.clear();
    emailPanel.add(emailValue);
    emailPanel.add(validEmailButton);
    emailPanel.add(editEmailButton);

    return emailPanel;
  }

  public Popup makePopup(String headerText) {
    return utils.makePopup(headerText);
  }
}
