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
package edu.stanford.registry.client.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;


/**
 * Makes the DeclinePopup reusable.  See also the HandleRegister class which collects some logic.
 *
 * This is used by
 * PatientDetailWidget - on the right there's an accept/decline button
 *
 * Note there are 3 somewhat-redundant Server API calls:
 * ClinicServices.declineEnrollment(Patient, DeclineReason, String-other)
 * ClinitServices.setPatientAgreesToSurvey(patient)
 *
 * ScheduleWidget - the recommended action buttons on the right have register/decline
 * PatientDetailWidget - on the right is a Register/Declined/Registered button
 * RegisterPatientWidget - this is the RegisterPatient tab
 * PersonSearch - if the person isn't found, lets you add them, registered or declined (uses HandleRegister)
 * RegistrationPopUp.java - (used by HandleRegister)
 *
 * PatientDetailWidget and PersonSearch also use HandleRegister which uses RegistrationPopUp
 */
public class DeclinePopup {

  final ClinicServiceAsync clinicSvc;
  final RegistryCssResource css;

  public DeclinePopup(ClinicServiceAsync clinicSvc, RegistryCssResource registryCssResource) {
    this.clinicSvc = clinicSvc;
    css = registryCssResource;
  }


  /**
   * These are used in call-backs
   */
  TextBox otherText;
  HashMap<RadioButton,DeclineReason> buttonHash;


  /**
   * Creates a declined panel to put on your own pop-up.
   * You add the cancel and/or save buttons and their call-backs.
   *
   * User interaction sets 0, 1 or both of the passed non-null PatientAttributes.
   */
  public FlowPanel getDeclineReasonPanel(final PatientAttribute declinedAttribute,
                                         final PatientAttribute declinedOtherAttr) {
    FlowPanel panel = new FlowPanel();
    createRadioButtonsAndTextBox(panel, getAttrValue(declinedAttribute), getAttrValue(declinedOtherAttr));

    // add call-backs to set the attributes
    for (Entry<RadioButton, DeclineReason> e: buttonHash.entrySet()) {
      e.getKey().addClickHandler(new ReasonClickHandler(e.getValue().name(), declinedAttribute));
    }
    otherText.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        // do this even if value isn't other, else problem if click other, set this, click another, click other...
        declinedOtherAttr.setDataValue(otherText.getValue());
      }
    });
    return panel;
  }


  /**
   * For a radio button representing a reason, to set a PatientAttribute's value,
   * for the getDeclineReasonPanel(attributes) call above.
   */
  private static class ReasonClickHandler implements ClickHandler {
    final String reasonName;
    final PatientAttribute declinedAttribute;
    ReasonClickHandler(String name, PatientAttribute attr) {
      reasonName = name;
      declinedAttribute = attr;
    }
    @Override
    public void onClick(ClickEvent event) {
      declinedAttribute.setDataName(Constants.ATTRIBUTE_DECLINE_REASON_CODE);
      declinedAttribute.setDataValue(reasonName);
    }
  }


  /**
   * For overriding when calling addRestOfReasonsForDecliningPopup() which creates
   * a popup that will call a clinic service when activated.
   */
  protected void declineSuccessHandler(Patient result) { }


  /**
   * This adds to the passed panel a panel with the Decline reasons and otherText text-box.
   * Plus it adds Decline and Cancel buttons and calls the clinicSvc.declineEnrollment
   * when the Decline button is hit.
   *
   * Cancel closes the decline popup without any server interaction.
   *
   * If you use this method, override the declineSuccessHandler() to update the patient.
   *
   * The patient need have no REASON_CODE or REASON_OTHER attributes.
   */
  public void addRestOfReasonsForDecliningPopup(final Patient patient, final Popup declinePopup, final FlowPanel panel) {
    createRadioButtonsAndTextBox(panel, patient.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_CODE),
                                        patient.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_OTHER));

    final Button declineButton = new Button("Decline");
    declineButton.addStyleName(css.defaultButton());
    declineButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        DeclineReason reasonCode = null;
        for (Entry<RadioButton, DeclineReason> e: buttonHash.entrySet()) {
          if (e.getKey().getValue()) { // true if it's the one set
            reasonCode = e.getValue();
          }
        }
        if (reasonCode != null) {
          clinicSvc.declineEnrollment(patient, reasonCode, otherText.getText(), new Callback<Patient>() {
            @Override
            public void handleSuccess(Patient result) {
              declinePopup.hide();
              declineSuccessHandler(result);
            }
          });
        }
      }
    });

    final Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        declinePopup.hide();
      }
    });

    otherText.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          declineButton.click();
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          cancelButton.click();
        }
      }
    });

    List<Button> buttons = new ArrayList<>();
    buttons.add(declineButton);
    buttons.add(cancelButton);
    declinePopup.setCustomButtons(buttons);

    declinePopup.setGlassEnabled(true);
    declinePopup.showMessage(panel);
  }


  private String getAttrValue(PatientAttribute attr) {
    return (attr == null) ? "" : attr.getDataValue();
  }


  /**
   * Populates the buttonHash and otherText instance variables as it adds
   * the radio button panel, buttons and text-box to the passed panel, initializing their
   * values with the passed curReason and curOtherReason (for the text-box)
   */
  private void createRadioButtonsAndTextBox(FlowPanel panel, String curReason, String curOtherReason) {
    buttonHash = new HashMap<RadioButton,DeclineReason>(10);
    panel.add(new Label("Reason for declining:"));

    FlowPanel radioSet = new FlowPanel();
    radioSet.addStyleName(css.radioset());
    for (DeclineReason reason: DeclineReason.values()) { // add all the radio buttons
      RadioButton button = new RadioButton("reason", reason.getDisplayName());
      radioSet.add(button);
      if (reason.name().equals(curReason))
        button.setValue(true);
      buttonHash.put(button, reason);
    }
    otherText = new TextBox();
    if (curOtherReason != null) {
      otherText.setValue(curOtherReason);
    }
    panel.add(radioSet);
    panel.add(otherText);
  }
}
