/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryEntryPoint;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.ConfigurePatientAssessmentService;
import edu.stanford.registry.client.service.ConfigurePatientAssessmentServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.AssessmentConfig;
import edu.stanford.registry.shared.AssignedPatientAssessment;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAssignedAssessmentEntry;
import edu.stanford.registry.shared.ServiceUnavailableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;

public class PatientAssessmentConfigWidget extends FlowPanel {

  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final ConfigurePatientAssessmentServiceAsync configurePatientAssessmentService = GWT
      .create(ConfigurePatientAssessmentService.class);
  private final ClinicUtils clinicUtils;

  private final ScrollPanel scrollPanel = new ScrollPanel();
  private final Container container = new Container();
  private final VerticalPanel wrapper = new VerticalPanel();

  // Two column containers for clinic/survey and instrument selection
  private final VerticalPanel clinicPanel = new VerticalPanel();
  private final VerticalPanel assessmentConfigPanel = new VerticalPanel();

  private final Heading selectAssessmentTitle = new Heading(HeadingSize.H4);
  private final Heading instrumentTypeTitle = new Heading(HeadingSize.H4);

  private final Heading frequencyTitle = new Heading(HeadingSize.H4);
  private final FlexTable ftAssessments = new FlexTable();
  private final ListBox lbAssessmentType = new ListBox();
  private final Button btnSave = new Button("Save");
  private final Alert alert = new Alert();

  private final ErrorDialogWidget errorPopUp = new ErrorDialogWidget();

  private Patient patient;

  private AssignedPatientAssessment currentAssignment;

  public PatientAssessmentConfigWidget(ClinicUtils clinicUtils, Patient patient) {
    this.clinicUtils = clinicUtils;
    this.patient = patient;
    new RegistryEntryPoint().setServiceEntryPoint(configurePatientAssessmentService, "configurePatientAssessmentService", null);
    layoutWidgets();
  }

  private void layoutWidgets() {
    this.add(scrollPanel);
    scrollPanel.setHeight("400px");
    clinicPanel.setWidth("435px");
    scrollPanel.add(container);
    container.add(wrapper);
    container.setFluid(true);
    wrapper.add(clinicPanel);
    wrapper.add(assessmentConfigPanel);

    selectAssessmentTitle.setSubText("Survey Type");
    instrumentTypeTitle.setSubText("Instrument");
    frequencyTitle.setSubText("Number of days since last assessment");

    selectAssessmentTitle.setStyleName(css.assessmentConfigFormTitle());
    instrumentTypeTitle.setStyleName(css.assessmentConfigFormTitle());
    frequencyTitle.setStyleName(css.assessmentConfigFormTitle());

    clinicPanel.add(selectAssessmentTitle);
    clinicPanel.add(lbAssessmentType);

    assessmentConfigPanel.add(alert);
    alert.setVisible(false);
    btnSave.setVisible(false);
    assessmentConfigPanel.add(ftAssessments);
    assessmentConfigPanel.add(btnSave);

    createAssessmentTypeSelectBox();

    savePatientAssignedAssessment();

    try {
      initAssessmentTypeSelection();
    } catch (Exception e) {
      errorPopUp.setText("Service error occurred. Try refreshing your browser.");
      errorPopUp.setError(e.getMessage());
      e.printStackTrace();
    }
  }

  private void initAssessmentTypeSelection() {
    // Initialize assessment selection list box
    selectAssessmentTitle.setVisible(true);
    lbAssessmentType.setVisible(true);
    createAssessmentTypeSelectBox();

    lbAssessmentType.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (lbAssessmentType.getSelectedIndex() > 0) {
          getAllQuestionnaires(lbAssessmentType.getSelectedItemText());
        }
      }
    });
  }

  private void getAllQuestionnaires(String assessmentType) {
    ftAssessments.clear();
    configurePatientAssessmentService.getAllQuestionnaires(assessmentType, new AsyncCallback<Set<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        errorPopUp.setText("Error occurred. Can not get list of questionnaires.");
        errorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(final Set<String> allAssessments) {
        if (allAssessments != null && !allAssessments.isEmpty()) {
          getClinicAssessmentConfig(allAssessments);
          configurePatientAssessmentService
              .getAssignmentByClinic(patient.getPatientId(), clinicUtils.getSiteId(), getCurrentClinic(),
                  new AsyncCallback<AssignedPatientAssessment>() {
                    @Override
                    public void onFailure(Throwable caught) {
                      errorPopUp.setText("Error occurred. Can not get assigned assessments.");
                      errorPopUp.setError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(AssignedPatientAssessment result) {
                      if (result != null) {
                        if (result.hasAssignment()) {
                          displayPatientAssignedAssessments(result);
                        }
                      }
                    }
                  });
        }
      }
    });
  }

  private void displayPatientAssignedAssessments(AssignedPatientAssessment assignedPatientAssessment) {
    currentAssignment = assignedPatientAssessment;

    Map<String, Integer> instrumentLookUp = new HashMap<>();
    for (String instrument : assignedPatientAssessment.getAssignedInstrumentNames()) {
      instrumentLookUp.put(instrument, assignedPatientAssessment.getFrequency(instrument));
    }

    int row = ftAssessments.getRowCount();
    for (int i = 1; i < row; i++) {
      final CheckBox isActive = (CheckBox) this.ftAssessments.getWidget(i, 0);
      final TextBox frequency = (TextBox) this.ftAssessments.getWidget(i, 1);
      Integer curFrequency = instrumentLookUp.get(isActive.getText());

      if (curFrequency != null) {
        isActive.setValue(true);
        frequency.setText(curFrequency.toString());
        isActive.setEnabled(true);
        frequency.setEnabled(true);
      } else {
        setUIWithLowFrequency(isActive, frequency);
      }
    }
  }

  private void savePatientAssignedAssessment() {
    //patient assessment save event handler

    btnSave.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        AssignedPatientAssessment newAssignment = new AssignedPatientAssessment(patient.getPatientId(),
            clinicUtils.getSiteId(), getCurrentClinic());
        int row = ftAssessments.getRowCount();
        for (int i = 1; i < row; i++) {
          CheckBox isActive = (CheckBox) ftAssessments.getWidget(i, 0);
          TextBox frequency = (TextBox) ftAssessments.getWidget(i, 1);
          // Checkbox column
          if (isActive.getValue()) {
            if (frequency.getText() != null && !frequency.getText().isEmpty()
                && validateFrequency(frequency.getText()) > 0) {
              // Valid frequency
              newAssignment
                  .assignAssessment(isActive.getText(), Integer.valueOf(frequency.getText()));
            } else {
              displayAlert("Frequency accepts number only. Minimum value is 1", false);
            }
          }
        }

        configurePatientAssessmentService
            .updatePatientAssignedAssessments(newAssignment, new AsyncCallback<AssignedPatientAssessment>() {
              @Override
              public void onFailure(Throwable caught) {
                errorPopUp.setText("Error occurred saving configuration.");
                errorPopUp.setError(caught.getMessage());
              }

              @Override
              public void onSuccess(AssignedPatientAssessment result) {
                if (result != null) {
                  lbAssessmentType.clear();
                  lbAssessmentType.setSelectedIndex(0);
                  ftAssessments.clear(true);
                  btnSave.setEnabled(false);
                  btnSave.setVisible(false);
                  displayAlert("Assignment configuration saved.", false);
                }
              }
            });
      }
    });
  }

  private void getClinicAssessmentConfig(final Set<String> allAssessments) {
    final String clinicName = getCurrentClinic();
    configurePatientAssessmentService
        .getAssessmentConfig(clinicName, lbAssessmentType.getSelectedItemText(), new AsyncCallback<AssessmentConfig>() {
          @Override
          public void onFailure(Throwable caught) {
            errorPopUp.setText("Error occurred getting assessment configuration.");
            errorPopUp.setError(caught.getMessage());
          }

          @Override
          public void onSuccess(AssessmentConfig result) {
            if (result != null && result.getInstruments() != null && result.getAssessmentType()
                .equalsIgnoreCase(lbAssessmentType.getSelectedItemText())) {
              displayAssessmentGrid(result.getInstruments(), allAssessments);
            } else {
              displayAssessmentGrid(null, allAssessments);
            }
          }
        });
  }

  // Todo: get the current provider's clinic
  private String getCurrentClinic() {
    String clinicName;
    if (clinicUtils.getClientConfig().isClinicFilterEnabled()) {
      //Site has multiple clinic
      clinicName = "Psychosocial Treatment Clinic";
    } else {
      clinicName = null;
    }
    return clinicName;
  }

  private void displayAssessmentGrid(Map<String, Integer> cAssessmentConfig, Set<String> allAssessments) {
    ftAssessments.setWidget(0, 0, instrumentTypeTitle);
    ftAssessments.setWidget(0, 1, frequencyTitle);
    int row = 1;
    if (allAssessments != null && !allAssessments.isEmpty()) {
      for (String inst : allAssessments) {
        CheckBox isActive = new CheckBox(inst);
        // Add click event to the checkboxes
        isActive.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            alert.setVisible(false);
            int rowIndex = ftAssessments.getCellForEvent(event).getRowIndex();
            TextBox frequency = (TextBox) ftAssessments.getWidget(rowIndex, 1);
            if (((CheckBox) event.getSource()).getValue()) {
              frequency.setEnabled(true);
              frequency.setFocus(true);
            } else {
              frequency.setEnabled(false);
              frequency.setText("");
              if (currentAssignment != null) {
                btnSave.setVisible(true);
                btnSave.setEnabled(true);
              }
            }
          }
        });

        TextBox frequency = new TextBox();
        frequency.addChangeHandler(new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            alert.setVisible(false);
            enableSave(validateFrequency(((TextBox) (event.getSource())).getText()) > 0);
          }
        });

        if (cAssessmentConfig != null && cAssessmentConfig.containsKey(inst)) {
          isActive.setValue(true);
          frequency.setText(cAssessmentConfig.get(inst).toString());
        } else {
          setUIWithLowFrequency(isActive, frequency);
        }

        frequency.getElement().setAttribute("type", "number");
        frequency.getElement().setAttribute("min", "1");
        ftAssessments.setWidget(row, 0, isActive);
        ftAssessments.setWidget(row, 1, frequency);
        row++;
      }
    }
  }

  private void setUIWithLowFrequency(CheckBox inst, TextBox freq) {
    final TextBox frequency = freq;
    final CheckBox isActive = inst;
    if (isActive != null && frequency != null) {
      configurePatientAssessmentService.getLowFrequencyByInstrumentName(patient.getPatientId(), clinicUtils.getSiteId(),
          isActive.getText(), new AsyncCallback<PatientAssignedAssessmentEntry>() {
            @Override
            public void onFailure(Throwable caught) {
              //do nothing
            }

            @Override
            public void onSuccess(PatientAssignedAssessmentEntry result) {
              if (result != null && result.getFrequency() != null && result.getInstrumentName() != null) {
                if (isActive != null) {
                  if (currentAssignment != null && !currentAssignment.hasInstrument(isActive.getText())) {
                    isActive.setValue(false);
                  }
                }

                if (frequency != null) {
                  frequency.setPlaceholder(result.getFrequency().toString());
                }

                if (!Objects.requireNonNull(getCurrentClinic()).equals(result.getClinicName())) {
                  String enabledFrom = "Assigned in " + result.getClinicName();
                  isActive.setTitle(enabledFrom);
                  isActive.setEnabled(false);
                  frequency.getElement().setAttribute("max", result.getFrequency().toString());
                  frequency.setTitle(result.getClinicName());
                }
              }
            }
          });
    }
  }

  //Displays the active process types from process.xml into a list box
  private void createAssessmentTypeSelectBox() {
    lbAssessmentType.clear();
    int count = 0;
    ArrayList<String> activeAssessmentTypes = clinicUtils.getProcessXml().getActiveVisitProcessNames();
    lbAssessmentType.addItem("");
    if (activeAssessmentTypes != null && !activeAssessmentTypes.isEmpty()) {
      for (String at : activeAssessmentTypes) {
        count++;
        lbAssessmentType.addItem(at, at);
      }
    }

    if (count < 1) {
      throw new ServiceUnavailableException("Can not find active assessment type.");
    }
  }

  private Integer validateFrequency(String frequency) {
    int f;
    try {
      f = Integer.parseInt(frequency);
      if (f < 1) {
        return -1;
      }
    } catch (NumberFormatException | NullPointerException e) {
      return -1;
    }
    return f;
  }

  private void enableSave(boolean valid) {
    if (valid) {
      btnSave.setVisible(true);
      btnSave.setEnabled(true);
    } else {
      btnSave.setEnabled(false);
    }
  }

  private void displayAlert(String message, boolean error) {
    alert.setVisible(true);
    if (error) {
      alert.setType(AlertType.WARNING);
    } else {
      alert.setType(AlertType.SUCCESS);
    }
    alert.setText(message);
  }

}
