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

package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegisterAssessmentButton;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.Study;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class ListAssessmentWidget extends TabWidget implements ClickHandler, HasChangeHandlers, HasClickHandlers {
  private final String USER_TABLE_STYLE = "tableDataHeader";
  private final String USER_DATA_LIST_TEXT_STYLE = "dataListTextColumn";
  private final String USER_DATA_LIST_STYLE = "dataList";

  private DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
  private final FlowPanel centerPanel = new FlowPanel();
  private final FlowPanel buttonPanel = new FlowPanel();
  private final FlowPanel listPanel = new FlowPanel();
  private final ScrollPanel scrollPanel = new ScrollPanel();

  protected Button getPromis1Button = new Button("List Promis version 1 Assessments");
  protected Button getPromis2Button = new Button("List Promis version 2 Assessments");
  protected Button getLocalButton = new Button("List Local Assessments");
  protected Button getLocalPromisButton = new Button("List Local Promis Assessments");
  protected Button closeButton = new Button("return");

  protected ArrayList<RegisterAssessmentButton> regAssessmentsButton = new ArrayList<>();

  //private Label messageLabel = new Label("");
  protected ErrorDialogWidget errorPopUp = new ErrorDialogWidget();
  protected PatientStudy registrationResponse = null;
  protected int patientId = 0;

  /**
   * Create the remote service
   */
  private final ClinicServiceAsync clinicService;
  private final Logger logger = Logger.getLogger(ListAssessmentWidget.class.getName());

  public ListAssessmentWidget(ClinicUtils utils, ClinicServiceAsync clinicService) {
    super(utils);
    this.clinicService = clinicService;
    initWidget(dockPanel);
  }

  @Override
  public void load() {
    //messageLabel.setText("");
    //messageLabel.addStyleName("messageLabel");
    //mainPanel.add(messageLabel);
    dockPanel.addNorth(getMessageBar(), 2);
    dockPanel.add(centerPanel);
    getPromis1Button.setStylePrimaryName("sendButton");
    getPromis1Button.addClickHandler(this);

    getPromis2Button.setStylePrimaryName("sendButton");
    getPromis2Button.addClickHandler(this);

    getLocalButton.setStylePrimaryName("sendButton");
    getLocalButton.addClickHandler(this);
    
    getLocalPromisButton.setStylePrimaryName("sendButton");
    getLocalPromisButton.addClickHandler(this);

    closeButton.setStylePrimaryName("sendButton");
    closeButton.addClickHandler(this);

    buttonPanel.addStyleName("buttonPanel");
    buttonPanel.add(getLocalButton);
    buttonPanel.add(getLocalPromisButton);
    buttonPanel.add(getPromis1Button);
    buttonPanel.add(getPromis2Button);

    centerPanel.add(buttonPanel);
    scrollPanel.add(listPanel);
  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();

    if (sender == getPromis1Button) {
      logger.log(Level.INFO, "getting promis assessments (v1)");
      fireAdd("PROMIS", 1);
    } else if (sender == getPromis2Button) {
      logger.log(Level.INFO, "getting promis assessments (v2)");
      fireAdd("PROMIS", 2);
    } else if (sender == getLocalButton) {
      logger.log(Level.INFO, "getting Local assessments (v2)");
      fireAdd("Local", 2);
    } else if (sender == getLocalPromisButton) {
      fireAdd("LocalPromis",1);
    } else if (sender == closeButton) {
      centerPanel.remove(scrollPanel);
      centerPanel.add(buttonPanel);
    } else {
      logger.log(Level.INFO, "checking regAssessments");
      if (regAssessmentsButton != null) {
        if (sender instanceof RegisterAssessmentButton) {
          RegisterAssessmentButton thisButton = (RegisterAssessmentButton) sender;
          logger.log(Level.INFO, "name:" + thisButton.getFormName());
          if (regAssessmentsButton.contains(sender)) {
            fireRegister((RegisterAssessmentButton) sender);
          } else {
          }
        }
      }
    }
  }

  /**
   * Add the details panel, removing the button to ask to get the details
   */
  private void fireAdd(String surveySystemName, int version) {
    NativeEvent nativeEvent = Document.get().createChangeEvent();
    ChangeEvent.fireNativeEvent(nativeEvent, this);
    centerPanel.remove(buttonPanel);
    listPanel.clear();
    scrollPanel.setSize("98%", "90%");
    listPanel.setSize("100%", "100%");
    listPanel.add(getAssessmentTable(surveySystemName, version));
    listPanel.add(closeButton);
    centerPanel.add(scrollPanel);
  }

  private void fireRegister(RegisterAssessmentButton button) {
    fireRegister(button.getSurveySystemName(), button.getFormName(), button.getTitle(), button.getExplanation(),
        button.getVersion());
  }

  private void fireRegister(String surveySystemName, String name, String title, String explanation, int version
  ) {
    listPanel.clear();
    FlowPanel registeredPanel = registerAssessment(surveySystemName, name, title, explanation, version);

    if (registrationResponse == null) {
      setErrorMessage("Problem occurred registering assessment");
    } else {
      setSuccessMessage("Registration completed.");

    }
    listPanel.add(registeredPanel);
    listPanel.add(closeButton);
  }

  @Override
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  /**
   * allows for observing click events on the entire widget
   */
  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  public FlexTable getAssessmentTable(String surveySystemName, int version) {
    return getAssessmentTable(surveySystemName, version, false);
  }

  private FlexTable getAssessmentTable(final String surveySystemName, final int version,
                                       final boolean includeRegisterButtons) {
    final FlexTable outputTable = createAssessmentsTable();
    clinicService.getAssessments(surveySystemName, version, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        // Show the RPC error message to the user
        errorPopUp.setText("Searching Patients");
        errorPopUp.setError(caught.getMessage());
      }

      @Override
      public void onSuccess(String result) {
        logger.log(Level.INFO, "Got result: " + result);
        com.google.gwt.xml.client.Document messageDom = XMLParser.parse(result);
        Element docElement = messageDom.getDocumentElement();

        // handle list of assessment forms
        if (docElement.getTagName().equals("forms")) {
          // populate the table with data
          NodeList forms = messageDom.getElementsByTagName("form");
          for (int i = 0; i < forms.getLength(); i++) {
            Element form = (Element) forms.item(i);
            final String oid = form.getAttribute("OID");
            String testTitle = form.getAttribute("name");
            if (testTitle == null || testTitle.length() < 1) {
              testTitle = form.getAttribute("Name");
            }
            final String title = testTitle;
            outputTable.setText(i + 1, 0, oid);
            outputTable.setText(i + 1, 1,
                title);
            outputTable.getRowFormatter().setStyleName(i + 1, USER_DATA_LIST_TEXT_STYLE);
            if (includeRegisterButtons) {
              RegisterAssessmentButton rab = null;
              if (regAssessmentsButton.size() >= (i + 1)) {
                rab = regAssessmentsButton.get(i);
              }
              if (rab == null) {

                rab = new RegisterAssessmentButton("Register this assessment");
                rab.addClickHandler(new ClickHandler() {

                  @Override
                  public void onClick(ClickEvent event) {
                    fireRegister(surveySystemName, oid, title, null, version);
                  }

                });
                rab.setFormName(form.getAttribute("OID"));
                rab.setTitle(form.getAttribute("name"));
                rab.addStyleDependentName("sendButton");
                regAssessmentsButton.add(rab);
              }
              outputTable.setWidget(i + 1, 2, rab);
            }

          }
        }

      }
    });
    return outputTable;
  }

  private FlexTable createAssessmentsTable() {
    FlexTable outputTable = new FlexTable();
    outputTable.setStyleName(USER_TABLE_STYLE);
    outputTable.setBorderWidth(3);
    outputTable.getRowFormatter().setStyleName(0, USER_TABLE_STYLE);
    outputTable.addStyleName(USER_DATA_LIST_STYLE);
    outputTable.setText(0, 0, "Study ID");
    outputTable.setText(0, 1, "Name");
    return outputTable;
  }

  public FlowPanel registerAssessment(String surveySystemName, String oid, final String title, String explanation,
                                      int version) {

    final FlowPanel registeredPanel = new FlowPanel();
    registeredPanel.add(new HTML("<h2>Registration</h2>"));
    clinicService.registerAssessment(surveySystemName, oid, title, explanation, version, new AsyncCallback<Study>() {
      @Override
      public void onFailure(Throwable
                                caught) {
        setErrorMessage("Failed to register assessment with " + caught.getMessage());
        //messageLabel.addStyleName("messageLabel");

      }

      @Override
      public void onSuccess(Study patStudy) {
        setSuccessMessage("Registration complete for study " + title);
        //messageLabel.addStyleName("serverResponseLabelError");
      }
    });

    return registeredPanel;
  }

  public void setPatientId(int id) {
    patientId = id;
  }

  @Override
  public String serviceName() {
    return "ClinicServices";
  }

}
