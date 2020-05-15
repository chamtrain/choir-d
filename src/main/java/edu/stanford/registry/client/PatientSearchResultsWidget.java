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

package edu.stanford.registry.client;

import edu.stanford.registry.client.event.ConsentChangeEvent;
import edu.stanford.registry.client.event.ConsentChangeHandler;
import edu.stanford.registry.client.event.HasConsentChangeHandler;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PatientSearchResultsWidget extends Composite implements HasClickHandlers, HasConsentChangeHandler {
  final HandlerManager handlerManager = new HandlerManager(this);
  private Button closeButton = new Button(new Image(RegistryResources.INSTANCE.close()).toString());

  private final PopupPanel popUp = new PopupPanel();
  private FlexTable tbl = new FlexTable();
  private ArrayList<PatientRadioButton> patientButtons = new ArrayList<>();
  private Patient selectedPatient = null;

  private ClickHandler registeredClickHandler = null;
  private final Label[] patientTableLabels = { new Label("First Name"), new Label("Last Name"), new Label("Select") };

  /**
   * This is the admin mode widget for registering patients in a specific survey.
   */
  public PatientSearchResultsWidget() {

    closeButton.setText("Close");
    closeButton.setPixelSize(100, 25);
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        popUp.hide();
      }
    });

  }

  public void hidePatients() {
    popUp.hide();
  }

  public void showPatients(ArrayList<Patient> patients, final String searchString, final int searchType) {
    ScrollPanel scroller = new ScrollPanel();
    VerticalPanel panel = new VerticalPanel();
    panel.addStyleName("search-popUp");
    HorizontalPanel headerPanel = new HorizontalPanel();
    headerPanel.addStyleName("head");
    headerPanel.setWidth("350px");
    headerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    Label titleLabel = new Label("Search results");
    headerPanel.add(titleLabel);
    panel.add(headerPanel);
    panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

    if (patients != null && patients.size() > 1) {
      tbl.removeAllRows();

      // add header
      for (int i = 0; i < patientTableLabels.length; i++) {
        tbl.setWidget(0, i, patientTableLabels[i]);
      }
      tbl.getRowFormatter().addStyleName(0, "tableDataHeader");
      tbl.addStyleName(RegistryResources.INSTANCE.css().dataList());

      for (int i = 0; i < patients.size(); i++) {
        int row = i + 1;
        Patient pat = patients.get(i);
        PatientRadioButton button = new PatientRadioButton(pat);

        tbl.setWidget(row, 0, new Label(pat.getFirstName()));
        tbl.setWidget(row, 1, new Label(pat.getLastName()));
        tbl.setWidget(row, 2, button);

        patientButtons.add(button);
        button.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            PatientRadioButton sender = (PatientRadioButton) event.getSource();
            selectedPatient = sender.getPatient();
            if (registeredClickHandler != null) {
              registeredClickHandler.onClick(event);
            }
            popUp.hide();
          }
        });
      }

      titleLabel.setText("More than 1 patient was found for '" + searchString + "'");
      panel.add(tbl);
    } else {
      titleLabel.setText("No patients were found for '" + searchString + "'");
      Button addPatientDeclined = new Button("Add patient as declined");
      addPatientDeclined.addClickHandler(getConsentClickHandler(searchString, searchType,
          ConsentChangeEvent.ConsentChangeType.declined));
      Button addPatientConsented = new Button("Add patient as registered");
      addPatientConsented.addClickHandler(getConsentClickHandler(searchString, searchType,
          ConsentChangeEvent.ConsentChangeType.consented));
      panel.add(addPatientConsented);
      panel.add(addPatientDeclined);
    }
    panel.add(closeButton);
    panel.setHeight(("98%"));
    panel.setWidth("98%");
    scroller.add(panel);
    scroller.setWidth("100%");
    scroller.setHeight("100%");
    popUp.setWidget(scroller);
    popUp.center();
    popUp.show();
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    registeredClickHandler = handler;

    return addDomHandler(handler, ClickEvent.getType());
  }

  public Patient getSelectedPatient() {
    return selectedPatient;
  }


  private ClickHandler getConsentClickHandler(final String searchString, final int searchType,
                                              final ConsentChangeEvent.ConsentChangeType eventType) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Patient patient = new Patient();
        if (searchType == ClientUtils.PATIENT_SEARCH_BY_PATIENT_ID) {
          patient.setPatientId(searchString);
        } else if (searchType == ClientUtils.PATIENT_SEARCH_BY_PARTIAL_NAME) {
          patient.setLastName(searchString);
        } else if (searchType == ClientUtils.PATIENT_SEARCH_BY_EMAIL) {
          PatientAttribute attribute = new PatientAttribute(patient.getPatientId(),
              Constants.ATTRIBUTE_SURVEYEMAIL, searchString, PatientAttribute.STRING);
          patient.addAttribute(attribute);
        }
        ConsentChangeEvent consentEvent = new ConsentChangeEvent(patient, eventType);
        handlerManager.fireEvent(consentEvent);
        hidePatients();
      }
    };
  }

  @Override
  public void addChangeHandler(ConsentChangeHandler handler) {
    handlerManager.addHandler(ConsentChangeEvent.getType(), handler);

  }
}
