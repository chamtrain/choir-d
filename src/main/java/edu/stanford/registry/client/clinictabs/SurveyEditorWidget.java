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
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.survey.LocalSurveyProvider;
import edu.stanford.registry.client.survey.PROMISSurveyProvider;
import edu.stanford.registry.client.survey.SurveyProvider;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.SurveySystem;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import com.google.gwt.xml.client.impl.DOMParseException;

public class SurveyEditorWidget extends TabWidget implements ClickHandler {

  // service to get/put appointments
  private final ClinicServiceAsync clinicService;

  // display components
  private FlowPanel mainPanel = new FlowPanel();
  private DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
  private FlowPanel detailPanel = new FlowPanel();
  private FlowPanel reportPanel = new FlowPanel();

  private HorizontalPanel detailHeadingPanel = new HorizontalPanel();
  private HorizontalPanel detailHeadingBar = new HorizontalPanel();
  private HorizontalPanel surveyTextPanel = new HorizontalPanel();
  private TextArea surveyText = new TextArea();
  private PatientStudy patStudy = null;

  private ScrollPanel templateScroll = new ScrollPanel();
  private ScrollPanel reportScroll = new ScrollPanel();
  private HorizontalPanel detailCommandPanel = new HorizontalPanel();
  private HorizontalPanel detailButtonBar = new HorizontalPanel();
  private HorizontalPanel detailSubVarBar = new HorizontalPanel();
  private HorizontalPanel reportButtonBar = new HorizontalPanel();
  private HorizontalPanel reportCommandPanel = new HorizontalPanel();
  private final Label mrnLabel = new Label("MRN ");

  private final Label stdLabel = new Label("Study Code ");
  private final Label tokLabel = new Label("Token ");
  private final TextBox mrnTextBox = new TextBox();
  private final TextBox stdTextBox = new TextBox();
  private final TextBox tokTextBox = new TextBox();

  private final Button getSurveyButton = new Button("Lookup");
  private final Button updSurveyButton = new Button("Update");
  private final Button clearButton = new Button("Clear");
  private final Button showButton = new Button("View survey");
  private final Button showAll = new Button("Show all answers");
  private final Button closeButton = new Button("Return to edit"); // <img src='images/close.png' height='16' width='16' />");
  private boolean showAllResponses = false;
  private ArrayList<SurveySystem> surveySystems;

  protected ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();

  private final Logger logger = Logger.getLogger(SurveyEditorWidget.class.getName());

  public SurveyEditorWidget(ClinicUtils utils, ClinicServiceAsync clinicService) {
    super(utils);
    this.clinicService = clinicService;
    initWidget(dockPanel);
  }

  @Override
  public void load() {
    setEmptyMessage();
    getSurveyButton.addClickHandler(this);
    updSurveyButton.addClickHandler(this);
    clearButton.addClickHandler(this);
    showButton.addClickHandler(this);
    showAll.addClickHandler(this);

    updSurveyButton.setEnabled(false);
    showButton.setEnabled(false);

    // Style the labels and text boxes
    mrnTextBox.setStylePrimaryName("clTabPgHeadingBarList");
    mrnTextBox.addStyleName("emailTemplateSelectList");
    stdTextBox.setStylePrimaryName("clTabPgHeadingBarList");
    stdTextBox.addStyleName("emailTemplateSelectList");
    tokTextBox.setStylePrimaryName("clTabPgHeadingBarList");
    tokTextBox.addStyleName("emailTemplateSelectList");
    mrnLabel.setStylePrimaryName("clTabPgHeadingBarLabel");
    mrnLabel.addStyleName("emailTemplateSelectLabel");
    stdLabel.setStylePrimaryName("clTabPgHeadingBarLabel");
    stdLabel.addStyleName("emailTemplateSelectLabel");
    tokLabel.setStylePrimaryName("clTabPgHeadingBarLabel");
    tokLabel.addStyleName("emailTemplateSelectLabel");

    // Build the heading bar for selecting which template to edit
    detailHeadingPanel.setStylePrimaryName("clTabPgHeadingBar");
    detailHeadingPanel.addStyleName("emailTemplateHeadingBar");
    detailHeadingBar.setStylePrimaryName("clTabPgHeadingSelectBar");
    detailHeadingBar.addStyleName("emailTemplateHeadingSelectBar");

    detailHeadingBar.add(mrnLabel);
    detailHeadingBar.add(mrnTextBox);
    detailHeadingBar.add(stdLabel);
    detailHeadingBar.add(stdTextBox);
    detailHeadingBar.add(tokLabel);
    detailHeadingBar.add(tokTextBox);

    detailHeadingBar.add(getSurveyButton);
    detailHeadingBar.add(updSurveyButton);
    detailHeadingBar.add(showButton);
    detailHeadingBar.add(clearButton);
    detailHeadingPanel.add(detailHeadingBar);

    // Create the scrollable center for the template contents
    surveyTextPanel.setSize("100%", "98%");
    surveyText.addStyleName("emailsurveyTextArea");
    surveyText.setSize("100%", "750px");
    surveyTextPanel.addStyleName("emailsurveyTextPanel");
    surveyTextPanel.add(surveyText);
    templateScroll.add(surveyTextPanel);
    templateScroll.addStyleName("emailsurveyTextPanel");

    // Build the command bar
    detailCommandPanel.setWidth("98%");
    detailCommandPanel.setStylePrimaryName("clTabPgFootingBar");
    detailCommandPanel.addStyleName("emailTemplateFootingBar");
    detailButtonBar.setStylePrimaryName("clTabPgFootingButtonBar");
    detailButtonBar.addStyleName("emailTemplateFootingButtonBar");
    getSurveyButton.addStyleName("mixedButton");
    updSurveyButton.addStyleName("mixedButton");
    showButton.addStyleName("mixedButton");
    clearButton.addStyleName("mixedButton");
    detailCommandPanel.add(detailButtonBar);

    detailSubVarBar.setStylePrimaryName("clTabPgFootingButtonBar");
    detailSubVarBar.addStyleName("emailTemplateFootingSubVarBar");
    detailCommandPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    detailCommandPanel.add(detailSubVarBar);

    // Assemble the detail panel
    detailPanel.addStyleName("emailTemplatePanel");
    detailPanel.add(detailHeadingPanel);
    detailPanel.add(templateScroll);
    detailPanel.add(detailCommandPanel);

    // Assemble the main page
    dockPanel.addNorth(getMessageBar(), 2);
    mainPanel.setStylePrimaryName("mainPanel");
    mainPanel.add(detailPanel);
    dockPanel.add(mainPanel);

    reportCommandPanel.setWidth("98%");
    reportCommandPanel.setStylePrimaryName("clTabPgHeadingBar");
    reportCommandPanel.addStyleName("emailTemplateHeadingBar");
    reportButtonBar.setStylePrimaryName("clTabPgHeadingSelectBar");
    reportButtonBar.addStyleName("emailTemplateHeadingSelectBar");
    reportButtonBar.add(showAll);
    closeButton.addStyleName("scoresCloseButton");
    closeButton.setTitle("Close");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        mainPanel.remove(reportPanel);
        mainPanel.add(detailPanel);
      }
    });
    reportButtonBar.add(closeButton);
    reportCommandPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    reportCommandPanel.add(reportButtonBar);
    // reportPanel.setSize("98%", "98%");

    clinicService.getSurveySystems(new AsyncCallback<ArrayList<SurveySystem>>() {

      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage("ERROR Getting studies: " + caught.getMessage());
      }

      @Override
      public void onSuccess(ArrayList<SurveySystem> result) {
        surveySystems = result;
      }

    });
  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();

    if (sender == updSurveyButton) {
      updateSurvey();
    } else if (sender == getSurveyButton) {
      lookupSurvey();
    } else if (sender == showButton) {
      showSurvey();
    } else if (sender == clearButton) {
      mrnTextBox.setValue("");
      stdTextBox.setValue("");
      tokTextBox.setValue("");
      surveyText.setValue("");
      updSurveyButton.setEnabled(false);
      showButton.setEnabled(false);
    } else if (sender == showAll) {
      showAllResponses = !showAllResponses;
      showSurvey();
    }
  }

  private void updateSurvey() {
    String mrn = mrnTextBox.getValue();
    String std = stdTextBox.getValue();
    String tok = tokTextBox.getValue();

    //PatientStudy patStudy = new PatientStudy();
    if (patStudy == null) {
      setErrorMessage("You must first look up a survey!");
      return;
    }
    patStudy.setPatientId(mrn);
    patStudy.setStudyCode(Integer.valueOf(std));
    patStudy.setToken(tok);
    patStudy.setContents(surveyText.getValue());

    clinicService.updatePatientStudy(patStudy, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        logger.log(Level.SEVERE, "Failure: caught ", caught);
        basicErrorPopUp.setText("Encountered the following error getting the study details");
        basicErrorPopUp.setError(caught.getMessage());
        caught.printStackTrace();
      }

      @Override
      public void onSuccess(String result) {
        setSuccessMessage("Database has been updated!");
      }
    });

  }

  private void lookupSurvey() {

    setEmptyMessage();
    String mrn = mrnTextBox.getValue();
    String std = stdTextBox.getValue();
    String tok = tokTextBox.getValue();

    StringBuffer errorMsg = null;
    if (isMissing(mrn)) {
      errorMsg = addError(errorMsg, "Missing data! You must enter a value for ", "MRN");
    }
    if (isMissing(std)) {
      errorMsg = addError(errorMsg, "Missing data! You must enter a value for ", "Study Code");
    }
    if (isMissing(tok)) {
      errorMsg = addError(errorMsg, "Missing data! You must enter a value for ", "Token");
    }
    if (errorMsg != null) {
      setErrorMessage(errorMsg.toString());
      return;
    }

    Integer stdInt = getInteger(std);
    
    if (stdInt == null) {
      errorMsg = addError(errorMsg, "Invalid data! You must enter an integer for ", "Study Code");
    }
    if (tok == null) {
      errorMsg = addError(errorMsg, "Invalid data! You must enter an integer for ", "Token");
    }
    if (errorMsg != null) {
      setErrorMessage(errorMsg.toString());
      return;
    }
    clinicService.getPatientStudy(mrn, stdInt, tok, new AsyncCallback<PatientStudy>() {
      @Override
      public void onFailure(Throwable caught) {
        logger.log(Level.SEVERE, "Failure: caught ", caught);
        basicErrorPopUp.setText("Encountered the following error getting the study details");
        basicErrorPopUp.setError(caught.getMessage());
        caught.printStackTrace();
      }

      @Override
      public void onSuccess(PatientStudy result) {
        if (result == null) {
          setErrorMessage("No Patient Study was found for these values");
        } else {
          patStudy = result;
          surveyText.setValue(result.getContents());
          updSurveyButton.setEnabled(true);
          showButton.setEnabled(true);
        }

      }
    });
  }

  private void showSurvey() {
    reportPanel.clear();
    reportScroll.clear();
    if (showAllResponses) {
      showAll.setText("Show answers only");
    } else {
      showAll.setText("Show all responses");
    }
    reportPanel.add(reportCommandPanel);
    FlexTable reportTable = new FlexTable();
    reportTable.setStylePrimaryName("dataList");

    if (surveyText == null || surveyText.getValue() == null || surveyText.getValue().isEmpty()) {
      setErrorMessage("This study has not been completed");
      return;
    }

    try {
      Document messageDom = XMLParser.parse(surveyText.getValue());

      if (messageDom == null) {
        logger.log(Level.INFO, "No promis score. xmlString is null");
        setErrorMessage("No data");
        return;
      }

      SurveyProvider surveyProv = getScoreProvider(messageDom);
      reportTable.setText(0, 0, " ");
      String[] scoreHeadings = surveyProv.getScoreDetailsHeading();
      for (int colInx = 0; colInx < scoreHeadings.length; colInx++) {
        Label rptHeading = new Label(scoreHeadings[colInx]);
        rptHeading.setStylePrimaryName("showSurveyDetails");
        rptHeading.addStyleName("surveyQuestionCompleted");
        reportTable.setWidget(0, colInx + 1, rptHeading);
      }
      int rowInx = 1;
      Label titleLabel = new Label(surveyProv.getAssessmentName());
      titleLabel.setStylePrimaryName("titleLabel");
      titleLabel.addStyleName("tableDataHeader");
      reportPanel.add(titleLabel);

      ArrayList<ArrayList<String>> questions = surveyProv.getQuestions();
      if (questions != null) {
        for (int q = 0; q < questions.size(); q++) {
          ArrayList<String> questionText = questions.get(q);

          if (questionText != null) {
            Grid questionGrid = new Grid(questionText.size(), 2);
            for (int qt = 0; qt < questionText.size(); qt++) {

              HTML question = new HTML(questionText.get(qt));
              question.setStylePrimaryName("showSurveyDetailsQuestion");
              question.addStyleName("surveyQuestionCompleted");

              if (qt == 0) {
                int num = q + 1;
                Label questionNum = new Label(num + ".");
                questionNum.setStylePrimaryName("showSurveyDetailsQuestion");
                questionNum.addStyleName("surveyQuestionCompleted");
                question.addStyleName("surveyQuestionPadding");
                questionNum.addStyleName("surveyQuestionPadding");
                questionGrid.setWidget(qt, 0, questionNum);
              }
              questionGrid.setWidget(qt, 1, question);
            }
            reportTable.setWidget(rowInx, 0, questionGrid);
            for (int i = 1; i < 4; i++) {
              reportTable.setText(rowInx, i, "");
            }
            reportTable.getRowFormatter().addStyleName(rowInx, "x-odd");
            rowInx++;

            ArrayList<String> answerTextArr = surveyProv.getAnswers(q);
            if (answerTextArr == null) {
              answerTextArr = new ArrayList<>();
            }
            ArrayList<Integer> selected = surveyProv.getAnswered(q);
            if (selected == null) {
              selected = new ArrayList<>();
            }

            Grid answerTable;
            if (showAllResponses) {
              answerTable = getAnswerTable(answerTextArr.size());
            } else {
              answerTable = getAnswerTable(selected.size());
            }

            for (int at = 0; at < answerTextArr.size(); at++) {
              boolean wasAnswerSelected = false;
              for (Integer aSelected : selected) {
                if (aSelected == at) {
                  wasAnswerSelected = true;
                }
              }
              if (wasAnswerSelected || showAllResponses) {

                final RadioButton answerButton = new RadioButton(q + "");
                Label answerText = new Label(answerTextArr.get(at));
                answerText.setStylePrimaryName("showSurveyDetailsAnswer");

                if (wasAnswerSelected) {
                  answerText.addStyleName("surveyAnswerCompleted");
                  answerButton.setValue(true);
                } else {
                  answerText.addStyleName("surveyAnswerNotSelected");
                  answerButton.setVisible(false);
                }
                answerText.addClickHandler(new ClickHandler() {
                  @Override
                  public void onClick(ClickEvent event) {
                    // don't let it change
                    answerButton.setValue(true);
                  }
                });
                answerButton.addStyleName("rightLabel");
                answerText.addStyleName("leftLabel");
                if (!showAllResponses) {
                  answerTable.setWidget(0, 0, answerButton);
                  answerTable.setWidget(0, 1, answerText);

                } else {
                  answerTable.setWidget(at, 0, answerButton);
                  answerTable.setWidget(at, 1, answerText);
                }
                reportTable.setWidget(rowInx, 0, answerTable);
                reportTable.getRowFormatter().addStyleName(rowInx, "x-even");
              }
            }

          }

          String scores[] = surveyProv.getItemScoreDetails(q);
          logger.log(Level.INFO, "scores has " + scores.length + " items");
          for (int colInx = 0; colInx < scores.length; colInx++) {
            Label scoreLabel = new Label(scores[colInx]);
            scoreLabel.setStylePrimaryName("showSurveyDetails");
            scoreLabel.addStyleName("surveyAnswerCompleted");
            reportTable.setWidget(rowInx, colInx + 1, scoreLabel);
          }
          rowInx++;

        }
      }
      Label footingLabel = new Label("Final score");
      footingLabel.setStylePrimaryName("showSurveyDetailsQuestion");
      footingLabel.addStyleName("surveyQuestionCompleted");
      footingLabel.addStyleName("surveyQuestionPadding");
      reportTable.setWidget(rowInx, 0, footingLabel);
      reportTable.getRowFormatter().addStyleName(rowInx, "x-odd");

      int cell = reportTable.getCellCount(rowInx - 1) - 1;
      String assessmentScore = surveyProv.getFinalScore();
      Label scoreLabel = new Label(assessmentScore);
      scoreLabel.setStylePrimaryName("showSurveyDetails");
      scoreLabel.addStyleName("surveyQuestionCompleted");
      reportTable.setWidget(rowInx, cell, scoreLabel);

      reportScroll.add(reportTable);
      reportPanel.add(reportScroll);

      mainPanel.remove(detailPanel);
      mainPanel.add(reportPanel);

    } catch (DOMParseException dpe) {
      setErrorMessage("Survey is not completed");
    } catch (Exception ex) {

      logger.log(Level.SEVERE, "Error " + ex.toString(), ex);
    }
  }

  private SurveyProvider getScoreProvider(Document doc) throws DOMParseException, InvalidDataElementException {
    String serviceName = "Local";

    if (surveySystems != null) {
      for (SurveySystem ssys : surveySystems) {
        if (ssys.getSurveySystemId().intValue() == patStudy.getSurveySystemId().intValue()) {
          serviceName = ssys.getSurveySystemName();
        }
      }
    }
    int version = 1;
    if (serviceName != null && serviceName.indexOf(".") > 0) {
      int indx = serviceName.indexOf(".");
      if (serviceName.length() > indx) {
        version = Integer.valueOf(serviceName.substring(indx + 1));
        serviceName = serviceName.substring(0, indx);
      }
    }
    if ("PROMIS".equals(serviceName)) {
      return new PROMISSurveyProvider(version, doc);
    }
    return new LocalSurveyProvider(version, doc);

  }

  private boolean isMissing(String value) {
    if (value == null) {
      return true;
    }
    if (value.trim().length() < 1) {
      return true;
    }
    return false;
  }

  private Integer getInteger(String value) {
    try {
      Integer intValue = Integer.valueOf(value);
      return intValue;
    } catch (Exception e) {
      return null;
    }
  }

  private StringBuffer addError(StringBuffer errorMsg, String startStr, String item) {
    if (errorMsg == null) {
      errorMsg = new StringBuffer();
      errorMsg.append(startStr);
    } else {
      errorMsg.append(", ");
    }
    errorMsg.append(item);
    return errorMsg;
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_DEVELOPER;
  }

  private Grid getAnswerTable(int rows) {
    Grid answerTable = new Grid(rows, 2);
    answerTable.setStylePrimaryName("fixedList");
    answerTable.setCellSpacing(5);

    // displayTable.setHeight("200px");
    for (int row = 0; row < rows; row++) {
      answerTable.setHTML(row, 0, "");
      answerTable.setHTML(row, 1, "");
      answerTable.getCellFormatter().setWidth(row, 0, "150px");
      answerTable.getCellFormatter().setWidth(row, 1, "400px");
    }
    return answerTable;
  }
}
