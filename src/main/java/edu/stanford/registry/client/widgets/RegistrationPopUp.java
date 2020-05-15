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

import edu.stanford.registry.client.CustomTextBox;
import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.ValidPatientId;
import edu.stanford.registry.client.utils.DeclinePopup;
import edu.stanford.registry.client.event.ConsentChangeEvent;
import edu.stanford.registry.client.event.ConsentChangeEvent.ConsentChangeType;
import edu.stanford.registry.client.event.ConsentChangeHandler;
import edu.stanford.registry.client.event.HasConsentChangeHandler;
import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.event.InvalidPatientEvent;
import edu.stanford.registry.client.event.InvalidPatientHandler;
import edu.stanford.registry.client.event.ValidPatientEvent;
import edu.stanford.registry.client.event.ValidPatientHandler;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RegistrationPopUp extends PageWidget implements HasConsentChangeHandler, InvalidEmailHandler {

  private final ClinicServiceAsync clinicService;
  private final HandlerManager handlerManager = new HandlerManager(this);
  private ClinicUtils utils;

  private final PopupPanel popUp = new PopupPanel();
  private final VerticalPanel addPanel = new VerticalPanel();
  private final ErrorDialogWidget yesNoErrorPopUp = new ErrorDialogWidget();
  private final Label[] addPageLabels = { null, new Label("MRN:"), new Label("First Name:"), new Label("Last Name:"),
      new Label("Date of birth:"), new Label("Email:") };
  private final Button addSubmitButton = new Button(
      new Image(RegistryResources.INSTANCE.save()).toString() + " Submit");
  private final Button addSurveyCancelButton = new Button(
      new Image(RegistryResources.INSTANCE.cancel()).toString() + " Cancel");

  private CustomTextBox addFirstName;
  private CustomTextBox addLastName;
  private TextBoxDatePicker addDob;
  private ValidPatientId addMrnTextBox;
  private ValidEmailAddress addEmailTextBox;

  public RegistrationPopUp(ClinicUtils clinicUtils, ClinicServiceAsync clinicService) {
    super(clinicUtils);
    utils = clinicUtils;
    this.clinicService = clinicService;
    //processXml = clinicUtils.getProcessXml();
  }

  /**
   * Displays box to register/decline pre-populating all that it can.
   */
  public void doMiniRegistrationPage(final Patient patient, String title, final boolean registering) {
    final RegistryCssResource css = RegistryResources.INSTANCE.css();

    ScrollPanel scroller = new ScrollPanel();
    addPanel.clear();
    addPanel.setSpacing(5);

    HorizontalPanel headerPanel = new HorizontalPanel();
    Grid headingGrid = new Grid(1, 3);

    //Image img = new Image(RegistryResources.INSTANCE.logo());
    //headingGrid.setWidget(0, 0, img);
    Label titleLabel = new Label(title);
    titleLabel.setStylePrimaryName(RegistryResources.INSTANCE.css().titleLabel());
    headingGrid.setWidget(0, 1, titleLabel);
    headerPanel.add(headingGrid);

    addPanel.add(headerPanel);
    setEmptyMessage();
    addPanel.add(getMessageBar());
    FlexTable addPageTable = new FlexTable();
    for (int i = 1; i < addPageLabels.length; i++) {
      addPageLabels[i].setStylePrimaryName(RegistryResources.INSTANCE.css().rightLabel());
      addPageTable.setWidget(i, 0, addPageLabels[i]);
    }
    addPageTable.addStyleName(css.datalist());

    /**
     * Create an mrn field and validate
     */
    addMrnTextBox = makeMrnField(patient.getPatientId());
    addFirstName = utils.makeRequiredField(patient.getFirstName());
    addLastName = utils.makeRequiredField(patient.getLastName());
    if (patient.getEmailAddress() != null && registering) {
      addEmailTextBox = utils.makeEmailField(patient.getEmailAddress(), this);
    } else {
      addEmailTextBox = utils.makeEmailField(null, this);
    }
    addEmailTextBox.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        setEmptyMessage();
        if (addEmailTextBox.isValid()) {
          addEmailTextBox.removeStyleName(css.dataListTextError());
              setEmptyMessage();
        }
      }
    });
    addDob = makelistDateField();
    if (patient.getDtBirth() != null) {
      addDob.setValue(patient.getDtBirth());
    }

    addMrnTextBox.setWidth("200px");
    addFirstName.setWidth("200px");
    addLastName.setWidth("200px");
    addEmailTextBox.setWidth("200px");
    addDob.setWidth("200px");

    Label mrnLabel = new Label("xxxxxx-x");
    Label dobLabel = new Label("MM/DD/YYYY");

    addPageTable.setWidget(1, 1, addMrnTextBox);
    addPageTable.setWidget(1, 2, mrnLabel);
    addPageTable.setWidget(2, 1, addFirstName);
    addPageTable.setWidget(3, 1, addLastName);
    addPageTable.setWidget(4, 1, addDob);
    addPageTable.setWidget(4, 2, dobLabel);

    final PatientAttribute declinedAttribute = patient.setAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE, null);
    final PatientAttribute otherAttribute = patient.setAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER, null);
    if (registering) {
      addPageTable.setWidget(5, 1, addEmailTextBox);
    } else {
      FlowPanel panel = new DeclinePopup(null, css).getDeclineReasonPanel(declinedAttribute, otherAttribute);
      panel.setWidth("200px");
      addPageTable.setWidget(5, 1, panel);
    }
    addPageTable.getFlexCellFormatter().setColSpan(7, 2, 2);

    addPanel.add(addPageTable);
    addPageTable.setVisible(true);

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setStylePrimaryName(css.clTabPgFootingBar());
    buttonPanel.addStyleName(css.registrationButtonBar());
        buttonPanel.add(addSubmitButton);
    buttonPanel.add(addSurveyCancelButton);
    addPanel.add(buttonPanel);

    addSubmitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (addMrnTextBox.getValue() == null || addMrnTextBox.getValue().isEmpty()) {
          return;
        }
        // first update the patient object
        patient.setPatientId(addMrnTextBox.getValue());
        patient.setFirstName(addFirstName.getValue());
        patient.setLastName(addLastName.getValue());
        String dobStr = getClientUtils().getDateString(addDob.getValue());
        Date newDate = new Date(getClientUtils().getMidDay(dobStr));
        patient.setDtBirth(new java.util.Date(newDate.getTime()));
        if (registering) {
          patient.removeAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE);
          patient.removeAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER);
          if ((addEmailTextBox.getValue() != null) &&
              (addEmailTextBox.getValue().trim().length() > 1) &&
              (!addEmailTextBox.isValidString())) {
            return;
          }
          utils.setEmail(patient, addEmailTextBox.getValue());
        }
        if (registering) {
          handlerManager.fireEvent(new ConsentChangeEvent(patient, ConsentChangeType.completedConsentForm));
        } else {
          handlerManager.fireEvent(new ConsentChangeEvent(patient, ConsentChangeType.completedDeclineForm));
        }
        popUp.hide();
      }
    });
    addSurveyCancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        yesNoErrorPopUp.hide();
        popUp.hide();
      }
    });

    scroller.add(addPanel);
    popUp.setWidget(scroller);
    popUp.center();
    addMrnTextBox.setFocus(true);
    popUp.show();
  }

  private ValidPatientId makeMrnField(String mrnString) {

    yesNoErrorPopUp.setModal(false);
    final ValidPatientId mrn = new ValidPatientId(utils, clinicService);
    mrn.setStylePrimaryName(RegistryResources.INSTANCE.css().dataListTextColumn());
        mrn.setInvalidStyleName("dataListTextError");
    mrn.setValidStyleName("dataListTextColumn");
    mrn.setValue(mrnString);
    mrn.addValidPatientHandler(new ValidPatientHandler() {
      @Override
      public void onPassedValidation(ValidPatientEvent event) {
        Patient validatedPatient = event.getPatient();
        if (validatedPatient != null) {
          mrn.setValue(validatedPatient.getPatientId());
          addFirstName.setValue(validatedPatient.getFirstName());
          addLastName.setValue(validatedPatient.getLastName());
          addDob.setValue(validatedPatient.getDtBirth());
          if (validatedPatient.getEmailAddress() != null) {
            addEmailTextBox.setValue(validatedPatient.getEmailAddress());
          }
        }
      }
    });

    mrn.addInvalidPatientHandler(new InvalidPatientHandler() {
      @Override
      public void onFailedValidation(InvalidPatientEvent event) {
        ArrayList<Button> panelButtons = new ArrayList<>();
        mrn.setValue(event.getFormattedId());
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
          yesNoErrorPopUp.setCustomButtons(panelButtons);
          ValidPatientId sender = (ValidPatientId) event.getSource();
          yesNoErrorPopUp.setPopupPosition(sender.getAbsoluteLeft(), sender.getAbsoluteTop());
          yesNoErrorPopUp.show();
        } else {
          /** failed for patient not found **/
          mrn.setNewPatient(true);
          mrn.setStylePrimaryName(RegistryResources.INSTANCE.css().dataListTextColumn());
        }
      }
    });
    return mrn;
  }

  /**
  public ListBox getProcessTypeListBox(boolean registering) {
    ListBox box = getProcessTypeListBox();
    ArrayList<String> processNames = processXml.getProcessNames();
    int selectIndex = -1;
    if (processNames != null) {
      for (int i = 0; i < processNames.size(); i++) {
        ArrayList<PatientAttribute> attribs = processXml.getProcessPatientAttributes().get(processNames.get(i));
        for (int a = 0; a < attribs.size(); a++) {
          // if we're registering find the process that sets the registered
          // attribute to 'y'
          if (Constants.ATTRIBUTE_PARTICIPATES.equals(attribs.get(a).getDataName())) {
            if ("y".equals(attribs.get(a).getDataValue()) && registering) {
              selectIndex = i;
            }
          }
          // if we're declining find the process that sets the registered
          // attribute to 'n'
          if (Constants.ATTRIBUTE_PARTICIPATES.equals(attribs.get(a).getDataName())) {
            if ("n".equals(attribs.get(a).getDataValue()) && !registering) {
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
    if (processXml.getProcessNames() != null) {
      for (int i = 0; i < processXml.getProcessNames().size(); i++) {
        box.addItem(processXml.getProcessNames().get(i));
      }
    }

    if (box.getItemCount() > 0) {
      box.setSelectedIndex(0);
    }
    return box;
  }
**/
  private TextBoxDatePicker makelistDateField() {
    return makelistDateField(new Date());
  }

  private TextBoxDatePicker makelistDateField(Date dt) {
    TextBoxDatePicker listDt = new TextBoxDatePicker(utils.getDefaultDateFormat());
    listDt.setValue(dt);
    return listDt;
  }

  @Override
  public void addChangeHandler(ConsentChangeHandler handler) {
    handlerManager.addHandler(ConsentChangeEvent.getType(), handler);
  }

  @Override
  public void onFailedValidation(InvalidEmailEvent event) {
    setErrorMessage(event.getMessage());
    addEmailTextBox.setStylePrimaryName(RegistryResources.INSTANCE.css().dataListTextError());

  }

}
