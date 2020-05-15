package edu.stanford.registry.client.widgets;

import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.PatientRegistration;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class ChangeSurveyPopup {

  public ChangeSurveyPopup(final ClinicUtils utils, final PatientRegistration patReg,
      final ClinicServiceAsync clinicService, final Callback<PatientRegistration> callback) {
    final Popup chgSurveyPopup = utils.makePopup("Change survey");

    chgSurveyPopup.setModal(false);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().quickRegisterPanel());
    List<Button> buttons = new ArrayList<>();
    final Label msgLabel = new Label("");
    msgLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
    if (patReg.getNumberCompleted() > 0) {
      Label warningLabel = new Label("This survey can not be changed. It has already been started.");
      warningLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
      panel.add(warningLabel);
    } else {

      Label name = new Label(patReg.getFirstName() + " " + patReg.getLastName() + " (DOB: "
          + utils.getDefaultDateFormat().format(patReg.getDtBirth()) + ")");
      name.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
      panel.add(name);
      final ListBox surveyType = utils.getProcessTypeListBox();
      for (int s = 0; s < surveyType.getItemCount(); s++) {
        if (patReg.getSurveyType().equals(surveyType.getItemText(s))) {
          surveyType.setSelectedIndex(s);
        }
      }
      panel.add(msgLabel);
      panel.add(surveyType);


      final Button changeButton = new Button("Change");
      changeButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (surveyType.getSelectedIndex() < 0) {
            msgLabel.setText("Please select a survey type");
            return;
          }
          final String newType = surveyType.getItemText(surveyType.getSelectedIndex());
          if (patReg.getSurveyType().equals(newType)) {
            msgLabel.setText("This survey is already type " + newType);
            return;
          }

          clinicService.changeSurveyType(patReg.getPatientId(), patReg.getApptId(), newType, new Callback<ApptRegistration>() {
            @Override
            protected boolean handleCheckedExceptions(Throwable caught) throws Throwable {
              msgLabel.setText(caught.getMessage());
              return true;
            }
            @Override
            public void handleSuccess(ApptRegistration result) {
              chgSurveyPopup.hide();
              patReg.setSurveyType(result.getSurveyType());
              patReg.setSurveyRegList(result.getSurveyRegList());
              callback.handleSuccess(patReg);
            }
          });
        }
      });
      changeButton.addStyleName(RegistryResources.INSTANCE.css().defaultButton());
      buttons.add(changeButton);
    }
    final Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        chgSurveyPopup.hide();
      }
    });
    buttons.add(cancelButton);

    chgSurveyPopup.setCustomButtons(buttons);
    chgSurveyPopup.setGlassEnabled(true);

    chgSurveyPopup.showMessage(panel);
  }

}
