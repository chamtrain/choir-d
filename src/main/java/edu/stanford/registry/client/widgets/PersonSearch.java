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

package edu.stanford.registry.client.widgets;

import edu.stanford.registry.client.PatientRadioButton;
import edu.stanford.registry.client.PatientSearchResultsWidget;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.clinictabs.AssessmentActivityWidget.ShowPatientCallback;
import edu.stanford.registry.client.utils.HandleRegister;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PersonSearch extends Composite implements HasClickHandlers {
  private final Logger logger = Logger.getLogger(PersonSearch.class.getName());
  private final ClinicUtils clinicUtils;
  private final ClinicServiceAsync clinicService;
  private final ShowPatientCallback showPatientCallback;
  private TextBox patientSearchTextBox = new TextBox();
  private final Button patientSearchButton = new Button("Go");
  Widget[] searchComponents = null;
  private ArrayList<ClickHandler> handlers = new ArrayList<>();
  HorizontalPanel searchPanel = new HorizontalPanel();
  final Long siteId;

  public PersonSearch(Long siteId, ClinicUtils clinicUtils, ClinicServiceAsync clinicService, ShowPatientCallback showPatientCallback) {
    this.clinicUtils = clinicUtils;
    this.clinicService = clinicService;
    this.showPatientCallback = showPatientCallback;
    this.siteId = siteId;
    searchPanel.setSpacing(10);
    patientSearchTextBox.setWidth("200px");
    patientSearchTextBox.getElement().setPropertyString("placeholder", "email, mrn, or name");
    searchPanel.add(patientSearchTextBox);
    searchPanel.add(patientSearchButton);
    // Add the search handlers
    patientSearchTextBox.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
          doClick(event);
        }
      }
    });

    patientSearchButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doClick(event);
      }
    });
  }

  public HorizontalPanel getSearchPanel() {
    return searchPanel;
  }

  public void setSearchName(String name) {
    patientSearchTextBox.setValue(name);

  }

  public void setSearchMrn(String mrn) {
    patientSearchTextBox.setValue(mrn);
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {

    handlers.add(handler);
    return addHandler(handler, ClickEvent.getType());
  }

  @UiHandler("hyperlinkControlInComposite")
  void doClick(ClickEvent e) {
    sendSearchEvent();
  }

  private void sendSearchEvent() {
    try {
      final String searchString = patientSearchTextBox.getValue();
      final int searchType = clinicUtils.determineSearchType(searchString);
      Callback<ArrayList<Patient>> searchCallback = new Callback<ArrayList<Patient>>() {
        @Override
        protected void afterFailure() {
          clinicUtils.hideLoadingPopUp();
        }

        @Override
        public void handleSuccess(ArrayList<Patient> result) {
          clinicUtils.hideLoadingPopUp();
          if (result != null && result.size() == 1) {
            showPatientCallback.showPatient(result.get(0));
          } else {
            final PatientSearchResultsWidget searchResults = new PatientSearchResultsWidget();
            HandleRegister regHandler = new HandleRegister(logger, clinicUtils, clinicService, false) {
              @Override
              public void successOnSetRegisteredOrDeclined(Patient result) {
                showPatientCallback.showPatient(result);
              }
            };

            searchResults.addChangeHandler(regHandler.getConsentChangeHandler());

            if (result == null || result.size() < 1) {
              logger.log(Level.INFO, "No patients found for: " + searchString);
            } else {
              logger.log(Level.INFO, "More than 1 patient found");
              searchResults.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  if (event.getSource() instanceof PatientRadioButton) {
                    Patient pat = ((PatientRadioButton) event.getSource()).getPatient();
                    showPatientCallback.showPatient(pat);
                  }
                }
              });
            }
            searchResults.showPatients(result, searchString, searchType);
          }
        }

        @Override
        protected boolean handleCheckedExceptions(Throwable error) throws Throwable {
          if (error != null && (error instanceof ServiceUnavailableException) && error.getMessage() != null &&
              (error.getMessage().contains("is not a valid MRN format"))) {
            final Popup msgPopup = clinicUtils.makePopup("ERROR");

            msgPopup.setModal(false);
            Label titleLabel = new Label(error.getMessage());
            titleLabel.addStyleName(RegistryResources.INSTANCE.css().patientInfoVerify());
            final Button cancelButton = new Button("Cancel");
            cancelButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                msgPopup.hide();
              }
            });
            final Button searchButton = new Button("Search by name");
            searchButton.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                sendSearchEvent();
                msgPopup.hide();
              }
            });
            List<Button> buttons = new ArrayList<>();
            buttons.add(searchButton);
            buttons.add(cancelButton);
            msgPopup.setCustomButtonsGWT(buttons);
            FlowPanel panel = new FlowPanel();
            panel.add(titleLabel);
            msgPopup.setGlassEnabled(true);
            msgPopup.showMessage(panel);
            msgPopup.show();
            return true;
          }
          return false;
        }
      };
      clinicUtils.showLoadingPopUp();
      clinicService.searchForPatients(searchString, searchCallback);
    } catch (IllegalArgumentException e) {
      final Popup msgPopup = clinicUtils.makePopup("ERROR");
      msgPopup.setModal(false);
      Label titleLabel = new Label("A valid patient name, MRN, or a valid patient email must be provided.");
      final Button cancelButton = new Button("Close");
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          msgPopup.hide();
        }
      });
      List<Button> buttons = new ArrayList<>();
      buttons.add(cancelButton);
      msgPopup.setCustomButtonsGWT(buttons);
      FlowPanel panel = new FlowPanel();
      panel.add(titleLabel);
      msgPopup.setGlassEnabled(true);
      msgPopup.showMessage(panel);
      msgPopup.show();
    }
  }

  @UiHandler("hyperlinkControlInComposite")
  void doClick(KeyPressEvent e) {
    sendSearchEvent();
  }

  public String getSearchString() {
    return patientSearchTextBox.getValue();
  }
}
