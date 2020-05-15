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

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryEntryPoint;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.AssessmentConfigService;
import edu.stanford.registry.client.service.AssessmentConfigServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.AssessmentConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AssessmentConfigEditorWidget extends TabWidget {
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final AssessmentConfigServiceAsync assessmentConfigService = GWT.create(AssessmentConfigService.class);
  private final ClinicUtils clinicUtils;
  private final User user;
  private final Long siteId;
  private final DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
  private final HorizontalPanel wrapper = new HorizontalPanel();
  private final VerticalPanel clinicPanel = new VerticalPanel();
  private final VerticalPanel assessmentConfigPanel = new VerticalPanel();
  private final Container container = new Container();
  private final Heading clinicSelectionTitle = new Heading(HeadingSize.H4);
  private final Heading selectAssessmentTitle = new Heading(HeadingSize.H4);
  private final Heading instrumentTypeTitle = new Heading(HeadingSize.H4);
  private final Heading frequencyTitle = new Heading(HeadingSize.H4);
  private final FlexTable ftAssessments = new FlexTable();
  private final ListBox lbClinic = new ListBox();
  private final ListBox lbAssessmentType = new ListBox();
  private final Button btnSave = new Button("Save");
  private final Alert alert = new Alert();
  private final ErrorDialogWidget errorPopUp = new ErrorDialogWidget();
  private AssessmentConfig assessmentConfig;

  public AssessmentConfigEditorWidget(ClinicUtils clinicUtils, User user, Long siteId) {
    super(clinicUtils);
    this.clinicUtils = clinicUtils;
    this.user = user;
    this.siteId = siteId;
    new RegistryEntryPoint().setServiceEntryPoint(assessmentConfigService, "assessmentConfigService", null);

    container.setFluid(true);
    container.add(wrapper);
    ScrollPanel scrollPanel = new ScrollPanel();
    scrollPanel.add(container);
    dockPanel.add(scrollPanel);
    wrapper.add(clinicPanel);
    wrapper.add(assessmentConfigPanel);

    clinicSelectionTitle.setSubText("Select a Clinic to Customize");
    selectAssessmentTitle.setSubText("Survey Type");
    instrumentTypeTitle.setSubText("Instrument");
    frequencyTitle.setSubText("Frequency(in days)");

    clinicSelectionTitle.setStyleName(css.assessmentConfigFormTitle());
    selectAssessmentTitle.setStyleName(css.assessmentConfigFormTitle());
    instrumentTypeTitle.setStyleName(css.assessmentConfigFormTitle());
    frequencyTitle.setStyleName(css.assessmentConfigFormTitle());

    assessmentConfigPanel.setStylePrimaryName(css.assessmentConfig());
    assessmentConfigPanel.add(alert);
    assessmentConfigPanel.add(ftAssessments);
    assessmentConfigPanel.add(btnSave);

    initWidget(dockPanel);
  }

  @Override
  public void load() {
    if (isLoaded()) {
      return;
    }
    setServiceEntryPoint(assessmentConfigService, "assessmentConfigService");

    alert.setVisible(false);

    clinicPanel.add(clinicSelectionTitle);
    clinicPanel.add(lbClinic);
    clinicSelectionTitle.setVisible(false);
    lbClinic.setVisible(false);

    clinicPanel.add(selectAssessmentTitle);
    clinicPanel.add(lbAssessmentType);
    selectAssessmentTitle.setVisible(false);
    lbAssessmentType.setVisible(false);
    btnSave.setVisible(false);

    try {
      if (getClientConfig().isClinicFilterEnabled()) {
        initClinicSelection();
      } else {
        initAssessmentTypeSelection();
      }

      btnSave.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (assessmentConfig == null) {
            throw new ServiceUnavailableException("Service error occurred. Try refreshing your browser.");
          }

          int row = ftAssessments.getRowCount();
          for (int i = 1; i < row; i++) {
            CheckBox isActive = (CheckBox) ftAssessments.getWidget(i, 0);
            TextBox frequency = (TextBox) ftAssessments.getWidget(i, 1);
            // Checkbox column
            if (isActive.getValue()) {
              if (frequency.getText() != null && !frequency.getText().isEmpty()
                  && validateFrequency(frequency.getText()) > 0) {
                // Valid frequency
                if (assessmentConfig.getInstruments() != null) {
                  assessmentConfig.getInstruments().put(isActive.getText(), Integer.valueOf(frequency.getText()));
                } else {
                  Map<String, Integer> instruments = new LinkedHashMap<>();
                  assessmentConfig.setInstruments(instruments);
                  assessmentConfig.getInstruments().put(isActive.getText(), Integer.valueOf(frequency.getText()));
                }
              } else {
                displayAlert("Frequency accepts number only. Minimum value is 1", false);
                return;
              }
            } else {
              // Instrument is unchecked. Making sure it's not set in the config object
              if (assessmentConfig.getInstruments() != null) {
                if (assessmentConfig.getInstruments().get(isActive.getText()) != null) {
                  assessmentConfig.getInstruments().remove(isActive.getText());
                }
              }
            }
          }

          assessmentConfigService.updateCustomAssessmentConfig(assessmentConfig, new AsyncCallback<AssessmentConfig>() {
            @Override
            public void onFailure(Throwable caught) {
              errorPopUp.setText("Error occurred saving configuration.");
              errorPopUp.setError(caught.getMessage());
              //caught.printStackTrace();
            }

            @Override
            public void onSuccess(AssessmentConfig result) {
              if (result != null) {
                if (getClientConfig().isClinicFilterEnabled()) {
                  lbClinic.setSelectedIndex(0);
                  lbAssessmentType.clear();
                }
                lbAssessmentType.setSelectedIndex(0);
                ftAssessments.clear(true);
                btnSave.setEnabled(false);
                btnSave.setVisible(false);
                displayAlert("Custom assessment configuration saved.", false);
              }
            }
          });
        }
      });

      ftAssessments.setStyleName(css.assessmentListWrapper());

    } catch (Exception e) {
      errorPopUp.setText("Service error occurred. Try refreshing your browser.");
      errorPopUp.setError(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Displays the active process types from process.xml into a list box
   */
  private void createAssessmentTypeSelectBox() {
    lbAssessmentType.clear();
    int count = 0;
    ArrayList<String> activeAssessmentTypes = getUtils().getProcessXml().getActiveVisitProcessNames();
    lbAssessmentType.addItem("");
    if (activeAssessmentTypes != null && !activeAssessmentTypes.isEmpty()) {
      for (String at : activeAssessmentTypes) {
        count++;
        lbAssessmentType.addItem(at, at);
      }
    }
    // Make sure there exists at least one active process type
    if (count < 1) {
      throw new ServiceUnavailableException("Can not find active assessment type.");
    }
  }

  /**
   * Service call to get list of custom instrument configuration for the selected clinic and assessment/process type
   *
   * @param allAssessments set of set of
   */
  private void getClinicAssessmentConfig(final Set<String> allAssessments) {
    String clinicName;

    if (getClientConfig().isClinicFilterEnabled() && lbClinic.getSelectedIndex() > 0
        && lbClinic.getSelectedItemText() != null
        && !lbClinic.getSelectedItemText().isEmpty()) {
      //Site has multiple clinic
      clinicName = lbClinic.getSelectedItemText();
    } else {
      clinicName = null;
    }

    assessmentConfigService.getAssessmentConfig(clinicName, lbAssessmentType.getSelectedItemText(), new AsyncCallback<AssessmentConfig>() {
      @Override
      public void onFailure(Throwable caught) {
        errorPopUp.setText("Error occurred getting assessment configuration.");
        errorPopUp.setError(caught.getMessage());
        //caught.printStackTrace();
      }

      @Override
      public void onSuccess(AssessmentConfig result) {
        if (result != null && result.getInstruments() != null
            && result.getAssessmentType().equalsIgnoreCase(lbAssessmentType.getSelectedItemText())) {
          assessmentConfig = result;
          displayAssessmentGrid(result.getInstruments(), allAssessments);
        } else {
          displayAssessmentGrid(null, allAssessments);
        }
      }
    });
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_ASSESSMENT_CONFIG_EDITOR;
  }

  /**
   * Display the assessments list table: checkboxes to select and text boxes for the frequency value
   *
   * @param cAssessmentConfig current assessment config object if exists
   * @param allAssessments    list of all assessments under the selected assessment/process type
   */
  private void displayAssessmentGrid(Map<String, Integer> cAssessmentConfig, Set<String> allAssessments) {
    if (cAssessmentConfig == null) {
      // This should be a new entry to the config
      assessmentConfig = new AssessmentConfig();
      assessmentConfig.setSiteId(siteId);
      Map<String, Integer> nInstrument = new LinkedHashMap<>();
      assessmentConfig.setAssessmentType(lbAssessmentType.getSelectedItemText());
      assessmentConfig.setInstruments(nInstrument);

      // Set selected clinic if site has clinic filter enabled
      if (getClientConfig().isClinicFilterEnabled()) {
        assessmentConfig.setClinicName(lbClinic.getSelectedItemText());
      } else {
        assessmentConfig.setClinicName(null);
      }
    }

    ftAssessments.setWidget(0, 0, instrumentTypeTitle);
    Tooltip ttFrequency = new Tooltip("Number of days between last assessment and the next.");
    ttFrequency.setPlacement(Placement.RIGHT);
    ttFrequency.add(frequencyTitle);
    ftAssessments.setWidget(0, 1, ttFrequency);
    int row = 1;
    if (allAssessments != null && !allAssessments.isEmpty()) {
      //Assessments are in the AllInstruments process type
      for (String inst : allAssessments) {
        CheckBox isActive = new CheckBox(inst);
        // Add click even to the checkboxes
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
          frequency.setEnabled(false);
        }

        frequency.getElement().setAttribute("type", "number");
        frequency.getElement().setAttribute("min", "1");
        ftAssessments.setWidget(row, 0, isActive);

        ftAssessments.setWidget(row, 1, frequency);
        row++;
      }
    }
  }

  /**
   * Reads ALL assessments
   *
   * @param assessmentType Assessment/process type
   */
  private void getAllQuestionnaires(String assessmentType) {
    ftAssessments.clear();
    assessmentConfigService.getAllQuestionnaires(assessmentType, new AsyncCallback<Set<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        errorPopUp.setText("Error occurred. Can not get list of questionnaires.");
        errorPopUp.setError(caught.getMessage());
        //caught.printStackTrace();
      }

      @Override
      public void onSuccess(Set<String> allAssessments) {
        if (allAssessments != null && !allAssessments.isEmpty()) {
          getClinicAssessmentConfig(allAssessments);
        }
      }
    });
  }

  /**
   * Populates the clinic listbox with mappings configured from customizer class
   */
  private void createClinicSelectBox() {
    Set<String> clinics = clinicUtils.getClientConfig().getClinicFilterMapping().keySet();
    lbClinic.clear();
    lbClinic.addItem(" ");
    for (String clinic : clinics) {
      lbClinic.addItem(clinic, clinic);
    }
  }

  /**
   * Initializing the clinic listbox
   */
  private void initClinicSelection() {
    // Initialize clinic selection title and list box
    if (getClientConfig().isClinicFilterEnabled()) {
      clinicSelectionTitle.setVisible(true);
      lbClinic.setVisible(true);
      createClinicSelectBox();

      lbClinic.addChangeHandler(new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          ftAssessments.clear();
          alert.setVisible(false);
          if (lbClinic.getSelectedIndex() > 0) {
            initAssessmentTypeSelection();
          }
        }
      });
    }
  }

  /**
   * Initializing the assessment listbox
   */
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

  /**
   * Validates a frequency value
   *
   * @param frequency A number of days after which the patient will be assessed the instrument. Integer value > 0
   * @return -1 if invalid, or frequency value if valid.
   */
  private Integer validateFrequency(String frequency) {
    Integer f;
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

  /**
   * Handles the state of the save button depending on valid input
   *
   * @param valid
   */
  private void enableSave(boolean valid) {
    if (valid) {
      btnSave.setVisible(true);
      btnSave.setEnabled(true);
    } else {
      btnSave.setEnabled(false);
    }
  }

  /**
   * Displays message alert.
   *
   * @param message
   * @param error
   */
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
