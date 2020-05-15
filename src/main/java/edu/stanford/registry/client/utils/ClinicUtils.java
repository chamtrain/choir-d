/*
 * Copyright 2013-2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.client.utils;

import edu.stanford.registry.client.PatientButton;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.client.widgets.ProcessXml;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class ClinicUtils extends ClientUtils {

  private static final DateTimeFormat expireFmt = DateTimeFormat.getFormat("MM-dd-yyyy");
  private ProcessXml processXml;
  private final Image tickImage = new Image(RegistryResources.INSTANCE.tick());

  private final ClinicServiceAsync clinicService;

  public ClinicUtils(ClinicServiceAsync clinicService, ClientConfig clientConfig, User user) {//, String logo) {
    super(clientConfig, user);
    this.clinicService = clinicService;
    processXml = new ProcessXml(clinicService);
  }

  /**
   * Gets the button to display patients history report
   */
  public PatientButton getHistoryButton(Patient patIn, final ApptRegistration registration) {
    PatientButton histButton = new PatientButton(new Image(RegistryResources.INSTANCE.report()).toString());
    histButton.setPatient(patIn);
    histButton.setTitle("View patient's scores report");
    histButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        String urlString = getChartUrl(Constants.ASSESSMENT_ID, registration.getAssessmentRegId().toString(), "height=118&width=297&print=n");
        Window.open(urlString, "Pdf", "");
      }
    });
    return histButton;
  }

  public ProcessXml getProcessXml() {
    return processXml;
  }
  public void resetProcessXml() {
    processXml = new ProcessXml(clinicService);
  }

  public java.util.Date getProcessExpirationDate(String processName) {
    String expires = getProcessXml().getProcessAttribute(processName, "expiration_date");
    if (expires == null || expires.length() < 1) {
      return null;
    }

    try {

      return expireFmt.parse(expires);
    } catch (IllegalArgumentException iae) {
      //
    }
    return null;

  }

  public void showSentPopup() {
    final Popup emailPopup = makePopup(" ");
    emailPopup.setModal(false);
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
    List<Button> buttons = new ArrayList<>();
    Label response = new Label("Sent");
    response.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    response.addStyleName(RegistryResources.INSTANCE.css().leftButton());
    panel.add(response);
    FlowPanel imagePanel = new FlowPanel();
    imagePanel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    imagePanel.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
    imagePanel.add(tickImage);

    panel.add(imagePanel);
    Button closeButton = new Button("Ok");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        emailPopup.hide();
      }
    });
    buttons.add(closeButton);
    emailPopup.setCustomButtons(buttons);
    emailPopup.setGlassEnabled(true);
    emailPopup.showMessage(panel);
  }

  /*
   * Show dialog box with an appropriate message for why email was not sent.
   */
  public void showSentFailedPopup(EmailSendStatus status, ApptRegistration patReg) {
    final Popup errorPopup = makePopup("Attention: Could not send email. ");
    FlowPanel panel = new FlowPanel();
    Label problemLabel = new Label();
    switch (status) {
    case not_consented:
      problemLabel.setText("Patient has not consented");
      break;
    case invalid_email_addr:
      problemLabel.setText("Email address is not valid");
      break;
    case invalid_survey_dt:
      String daysStr = getParam("appointment.initialemail.daysout");
      String onString = "";
      if (daysStr != null) {
        try {
          Date emailDt = new Date(patReg.getSurveyDt().getTime());
          int days = Integer.parseInt(daysStr);
          CalendarUtil.addDaysToDate(emailDt, days * -1);
          onString = " on " + getDateString(getDefaultDateFormat(),emailDt);
        } catch (Exception ignored) {
        }
      }
      problemLabel.setText("Your email will be sent " + daysStr + " days prior to this appointment " + onString);
      break;
    case no_email_addr:
      problemLabel.setText("Email address is missing");
      break;
    case no_registration:
      problemLabel.setText("There is no survey for this token");
      break;
    case not_18:
      problemLabel.setText("Patient is not 18");
      break;
    default:
      break;
    }
    //problemLabel.setStylePrimaryName("popUpLabel");
    problemLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    panel.add(problemLabel);
    //if (getParam(EMAIL_TO_PARENT) == null || !"true".equals(getParam(EMAIL_TO_PARENT).toLowerCase()))  {
    //  Label ageLabel = new Label(" - is at least 18 years of age");
    //  ageLabel.setStylePrimaryName("popUpLabel");
    //  panel.add(ageLabel);
    //}
    Button closeButton = new Button("Close");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        errorPopup.hide();
      }
    });
    List<Button> buttons = new ArrayList<>();
    buttons.add(closeButton);
    errorPopup.setCustomButtons(buttons);
    errorPopup.setGlassEnabled(true);
    errorPopup.showMessage(panel);
  }

  public ListBox getProcessTypeListBox() {
    ListBox box = new ListBox();
    ArrayList<String> processNames = getProcessXml().getActiveVisitProcessNames();
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

  public int determineSearchType(String value) {
    if (value == null || value.trim().length() < 1)
    {
      throw new IllegalArgumentException("A search term cannot be blank.");
    } else if (isValidEmail(value)) {
      return PATIENT_SEARCH_BY_EMAIL;
    } else if (isValidPatientId(value)) {
      return PATIENT_SEARCH_BY_PATIENT_ID;
    }
    // default to searching by name
    return PATIENT_SEARCH_BY_PARTIAL_NAME;
  }

  /**
   * Update the email attribute of the patient object and call the server
   * to update the database.
   */
  public void updatePatientEmail(Patient patient, String emailAddr, final Runnable afterDone) {
    setEmail(patient, emailAddr);

    clinicService.updatePatientEmail(patient, new Callback<Boolean>() {
      @Override
      public void handleSuccess(Boolean result) {
        afterDone.run();
      }
    });
  }
}
