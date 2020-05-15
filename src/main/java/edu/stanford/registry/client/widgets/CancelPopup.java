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
package edu.stanford.registry.client.widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientRegistration;


public class CancelPopup extends Popup {

  final ClinicServiceAsync clinicSvc;
  final RegistryCssResource css;

  public CancelPopup(ClinicServiceAsync clinicSvc, RegistryCssResource registryCssResource) {
    this.clinicSvc = clinicSvc;
    css = registryCssResource;

  }

  public void addContents(final PatientRegistration patReg, ClientUtils utils) {
    final CancelPopup cancelPopup = this;

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(css.quickDeclinePanel());
    panel.add(new Label("Verify assessment:"));
    Label name = new Label(
        patReg.getFirstName() + " " + patReg.getLastName()
        + " (DOB: " + utils.getDefaultDateFormat().format(patReg.getDtBirth()) + ")"
    );
    name.addStyleName(css.patientInfoVerify());
    panel.add(name);
    Label assessmentInfo = new Label(
        patReg.getSurveyType() + " " + utils.getDefaultDateTimeFormat().format(patReg.getVisitDt())
    );
    assessmentInfo.addStyleName(css.patientInfoVerify());
    panel.add(assessmentInfo);

    final Button confirmButton = new Button("Confirm cancellation");
    confirmButton.addStyleName(css.defaultButton());
    confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        patReg.setRegistrationType(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT);
        clinicSvc.updatePatientRegistration(patReg, patReg.getVisitDt(), new Callback<PatientRegistration>() {
          @Override
          public void handleSuccess(PatientRegistration result) {
            cancelPopup.hide();
            cancelSuccessHandler(result);
          }
        });
      }
    });

    final Button cancelButton = new Button("Close");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        cancelPopup.hide();
      }
    });

    List<Button> buttons = new ArrayList<>();
    buttons.add(confirmButton);
    buttons.add(cancelButton);
    this.setCustomButtons(buttons);

    this.setGlassEnabled(true);
    this.showMessage(panel);

  }


  /**
   * For overriding when calling addRestOfReasonsForDecliningPopup() which creates
   * a popup that will call a clinic service when activated.
   */
  protected void cancelSuccessHandler(PatientRegistration result) { }
}
