/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.client.survey;

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryEntryPoint;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.Sanitizer;
import edu.stanford.registry.client.api.SurveyBuilderFactory;
import edu.stanford.registry.client.api.SurveyBuilderForm;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormQuestion;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.registry.client.service.BuilderService;
import edu.stanford.registry.client.service.BuilderServiceAsync;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.PopupRelative;
import edu.stanford.registry.client.widgets.PopupRelative.Align;
import edu.stanford.registry.client.widgets.PopupRelative.CloseCallback;
import edu.stanford.registry.shared.StudyContent;
import edu.stanford.registry.shared.survey.SurveyException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.InlineHelpBlock;
import org.gwtbootstrap3.client.ui.Legend;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarButton;
import org.gwtbootstrap3.client.ui.NavbarText;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Pre;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class SurveyBuilder extends FlowPanel {
  /**
   * Build survey questionnaires
   */
  private final ClinicUtils utils;

  private final BuilderServiceAsync builderService = GWT.create(BuilderService.class);
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final Form pagePanel;
  private final Panel studiesPanel;
  private ArrayList<StudyContent> studies = null;
  private final SingleSelectionModel<StudyContent> selectionModel = new SingleSelectionModel<>();
  private final SingleSelectionModel<SurveyBuilderFormQuestion> questionSelectionModel = new SingleSelectionModel<>();
  private final SingleSelectionModel<UserDoc> userDocSelectionModel = new SingleSelectionModel<>();
  private final SurveyBuilderFactory factory = GWT.create(SurveyBuilderFactory.class);
  private final ProvidesKey<StudyContent> KEY_PROVIDER = new ProvidesKey<StudyContent>() {
    @Override
    public Object getKey(StudyContent item) {
      return item.getAppConfigId();
    }
  };
  private final Legend studiesPanelLegend = new Legend("Surveys");

  // For testing
  private Frame surveyFrame;
  private PopupRelative surveyPopup;
  private String builderSurveyPath = null;

  private final Tooltip prefixToolTip = new Tooltip();
  private final String PREFIX_TOOL_TIP_TEXT = "Max length of 12. Use letters, numbers and underscores only";
  static final String ALPHA_NUM_ERROR = "INVALID! Only alphanumeric characters and underscore's (_) are allowed";
  static final String ALPHA_NUM_TIP = "Letters, numbers and underscores (\"_\") only!";

  public SurveyBuilder(ClinicUtils utils) {
    super();
    this.utils = utils;
    new RegistryEntryPoint().setServiceEntryPoint(builderService, "builderService", null);

    builderService.getTestSurveyPath(new Callback<String>() {
      @Override
      public void handleSuccess(String result) {
        builderSurveyPath = result;
      }
    });
    Container topPanel = new Container();
    topPanel.setFluid(true);

    pagePanel = new Form();
    studiesPanel = new Panel();

    createEditPage();
    topPanel.add(pagePanel);
    this.add(topPanel);
  }

  private void createEditPage() {
    populateStudies();
    pagePanel.clear();
    pagePanel.add(studiesPanel);
    Button createButton = new Button("Create new Survey");
    createButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createSurvey();
      }
    });
    pagePanel.add(createButton);
  }

  private void createSurvey() {
    createNewSurveyPage();
  }

  private void createNewSurveyPage() {
    pagePanel.clear();
    final Form newSurveyForm = new Form();
    newSurveyForm.setType(FormType.HORIZONTAL);
    final FieldSet newSurveyFieldSet = new FieldSet();

    final Legend newSurveyLabel = new Legend("Create a new survey");
    newSurveyFieldSet.add(newSurveyLabel);

    final FormGroup nameForm = new FormGroup();
    nameForm.add(FormWidgets.formLabelFor("Survey name:", "surveyName", ColumnSize.LG_2));
    final FlowPanel namePanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    StyleHelper.addEnumStyleName(namePanel, ColumnSize.LG_3);
    final TextBox nameValue = new TextBox();
    nameValue.setPlaceholder("Abbreviated name with NO SPACES");
    nameValue.setId("surveyName");
    nameValue.setTitle("Short name that uniquely identifies the survey");
    nameValue.addKeyUpHandler(noSpaceHandler(nameValue));
    nameValue.setAllowBlank(false);
    namePanel.add(nameValue);
    nameForm.add(namePanel);
    final InlineHelpBlock nameHelp = new InlineHelpBlock();
    nameHelp.setIconType(IconType.EXCLAMATION_TRIANGLE);
    nameForm.add(nameHelp);
    nameValue.addValidator(new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(Editor<String> editor, String value) {
        List<EditorError> result = new ArrayList<>();
        String valueStr = value == null ? "" : value.trim();
        if (valueStr.length() < 1) {
          result.add(new BasicEditorError(nameValue, value, "Survey name is required"));
        } else if (valueStr.contains(" ")) {
          result.add(new BasicEditorError(nameValue, value, "Can not have spaces!"));
        } else if (!valueStr.trim().matches("^[a-zA-Z0-9:]*$")) {
          result.add(new BasicEditorError(nameValue, value, "Survey name cannot contain any special characters. Only upper and lowercase letters (A-Z) and numbers (0-9)."));
        }
        return result;
      }
    });
    newSurveyFieldSet.add(nameForm);

    final FormGroup titleForm = new FormGroup();
    titleForm.add(FormWidgets.formLabelFor("Survey title:", "formTitle", ColumnSize.LG_2));
    final FlowPanel titlePanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    StyleHelper.addEnumStyleName(titlePanel, ColumnSize.LG_10);
    final TextBox titleValue = new TextBox();
    titleValue.setPlaceholder("This is the descriptive name of the survey that displays on reports");
    titleValue.setId("formTitle");
    titlePanel.add(titleValue);
    titleForm.add(titlePanel);
    newSurveyFieldSet.add(titleForm);

    final FormGroup prefixForm = new FormGroup();
    prefixForm.add(FormWidgets.formLabelFor("Column prefix:", "formPrefix", ColumnSize.LG_2));
    final FlowPanel prefixPanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    StyleHelper.addEnumStyleName(prefixPanel, ColumnSize.LG_2);
    final TextBox prefixValue = new TextBox();
    final InlineHelpBlock prefixHelp = new InlineHelpBlock();
    prefixHelp.setIconType(IconType.EXCLAMATION_TRIANGLE);
    Validator<String> prefixValidator = getPrefixValidator(prefixValue);
    prefixValue.addValidator(prefixValidator);
    prefixValue.setPlaceholder("For square table columns");
    prefixValue.setId("formPrefix");
    prefixValue.setTitle("This is combined with the reference value to create the square table column names");
    prefixValue.addKeyUpHandler(noSpaceHandler(prefixValue));
    prefixValue.addBlurHandler(getAlphaNumBlurHandler(prefixForm, prefixValue, prefixHelp));
    prefixValue.setAllowBlank(false);

    prefixToolTip.setPlacement(Placement.BOTTOM);
    prefixToolTip.setTitle(PREFIX_TOOL_TIP_TEXT);
    prefixToolTip.setWidget(prefixValue);
    prefixPanel.add(prefixToolTip);
    prefixForm.add(prefixPanel);
    prefixForm.add(prefixHelp);
    newSurveyFieldSet.add(prefixForm);
    final ButtonGroup buttons = new ButtonGroup();
    final Button saveButton = new Button("Continue");
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean formValid = newSurveyForm.validate(true);
        if (prefixHelp.isError() || nameHelp.isError()) {
          return;
        }
        if (formValid) {
          StudyContent studyContent = new StudyContent();
          studyContent.setSurveySiteId(utils.getSiteId());
          studyContent.setStudyName(nameValue.getValue());
          studyContent.setTitle(titleValue.getValue());
          AutoBean<SurveyBuilderForm> surveyForm = factory.studyContent();
          SurveyBuilderForm survey = surveyForm.as();
          survey.setPrefix(prefixValue.getValue());
          studyContent.setConfigValue(AutoBeanCodex.encode(surveyForm).getPayload());
          builderService.addStudyContent(studyContent, new AsyncCallback<StudyContent>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(StudyContent result) {
              editStudy(result);
            }
          });
        }
      }

    });
    saveButton.addStyleName(RegistryResources.INSTANCE.css().defaultButton());
    final Button cancelButton = new Button("Return");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createEditPage();
      }
    });

    buttons.add(saveButton);
    buttons.add(cancelButton);
    newSurveyFieldSet.add(buttons);
    newSurveyForm.add(newSurveyFieldSet);
    pagePanel.add(newSurveyForm);
  }

  private void populateStudies() {
    studiesPanel.clear();
    studiesPanel.add(studiesPanelLegend);
    builderService.getStudies(new AsyncCallback<ArrayList<StudyContent>>() {
      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failed " + caught.getMessage());
      }

      @Override
      public void onSuccess(ArrayList<StudyContent> result) {
        if (result != null) {
          studies = result;
          studiesPanel.add(createStudiesTable(studies));

        }
        utils.hideLoadingPopUp();
      }
    });
  }

  private void editStudy(final StudyContent studyContentIn) {
    final StudyContent studyContent;
    if (studyContentIn == null) {
      studyContent = new StudyContent();
      studyContent.setConfigValue("");
      studyContent.setSurveySiteId(utils.getSiteId());
    } else {
      studyContent = studyContentIn;
    }
    final StudyContent study = studyContentIn != null ? studyContentIn : new StudyContent();
    pagePanel.clear();
    pagePanel.setWidth("1200px");
    pagePanel.setHeight("700px");
    final Form titleForm = new Form(FormType.INLINE);
    titleForm.getElement().setAttribute("style","text-align:left;");
    final TextBox titleTextBox = new TextBox();
    FormLabel studyNameLabel = FormWidgets.formLabelFor(" [" + study.getStudyName() + "]", "title");
    studyNameLabel.getElement().setAttribute("style","text-align:left; padding: 8px; width:120px");
    titleForm.add(studyNameLabel);
    titleTextBox.setText(study.getTitle() != null ? study.getTitle() : "");
    titleTextBox.setId("title");
    titleTextBox.setEnabled(false);
    titleTextBox.setWidth("608px");
    titleForm.add(titleTextBox);
    final Button editButton = new Button("Edit title");
    final Button cancelButton = new Button("Cancel");
    editButton.setType(ButtonType.PRIMARY);
    cancelButton.setType(ButtonType.PRIMARY);
    editButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (editButton.getText().equals("Edit title")) {
          editButton.setText("Save");
          titleTextBox.setEnabled(true);
          cancelButton.setVisible(true);
        } else {
          study.setTitle(titleTextBox.getValue());
          saveStudyTitle(study);
          editButton.setText("Edit title");
          titleTextBox.setEnabled(false);
          cancelButton.setVisible(false);
        }

      }
    });
    editButton.setWidth("80px");
    titleForm.add(editButton);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        cancelButton.setVisible(false);
        titleTextBox.setValue(study.getTitle() != null ? study.getTitle() : "");
        editButton.setText("Edit title");
        titleTextBox.setEnabled(false);
      }
    });
    cancelButton.setVisible(false);
    cancelButton.setWidth("80px");
    titleForm.add(cancelButton);
    pagePanel.add(titleForm);

    if (!studyContent.getConfigValue().isEmpty()) {
      final SurveyBuilderForm form = AutoBeanCodex.decode(factory, SurveyBuilderForm.class, studyContent.getConfigValue()).as();

      // prefix
      FormGroup prefixFormGroup = new FormGroup();

      final TextBox preTextBox = new TextBox();
      final InlineHelpBlock prefixHelp = new InlineHelpBlock();
      prefixHelp.setIconType(IconType.EXCLAMATION_TRIANGLE);
      preTextBox.setText(form.getPrefix() != null ? form.getPrefix() : "");
      preTextBox.setId("prefix");
      preTextBox.setEnabled(false);
      preTextBox.setWidth("90px");
      preTextBox.addKeyUpHandler(noSpaceHandler(preTextBox));
      final Validator<String> prefixValidator = getPrefixValidator(preTextBox);
      preTextBox.addValidator(prefixValidator);
      final Tooltip prefixToolTip = new Tooltip();
      prefixToolTip.setPlacement(Placement.BOTTOM);
      prefixToolTip.setTitle(PREFIX_TOOL_TIP_TEXT);
      prefixToolTip.setWidget(preTextBox);
      prefixFormGroup.add(prefixToolTip);
      final Button editPreButton = new Button("Edit prefix");
      final Button cancelPreButton = new Button("Cancel");
      editPreButton.setType(ButtonType.PRIMARY);
      cancelPreButton.setType(ButtonType.DEFAULT);

      prefixHelp.setIconType(IconType.EXCLAMATION_TRIANGLE);
      prefixFormGroup.add(prefixHelp);
      final Alert myPrefixColumnAlert = getAlert(
          "Can not save. Generated column names would be longer than the maximum allowed with the existing reference values.");
      final Alert myPrefixSizeAlert = getAlert("The prefix cannot be longer than 12 characters. It should begin with a letter and contain only letters, numbers and underscores (\"_\").");

      editPreButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (editPreButton.getText().equals("Edit prefix")) {
            editPreButton.setText("Save");
            preTextBox.setEnabled(true);
            cancelPreButton.setVisible(true);
          } else if (preTextBox.validate()) {
            // Check new prefix won't make column names too large
            if (prefixTooLong(preTextBox.getValue(), form)) {
              titleForm.add(myPrefixColumnAlert);
              return;
            }
            form.setPrefix(preTextBox.getValue());
            studyContent.setConfigValue(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(form)).getPayload());
            saveStudyContents(studyContent);
            editPreButton.setText("Edit prefix");
            preTextBox.setEnabled(false);
            cancelPreButton.setVisible(false);
            preTextBox.setEnabled(false);
            editPreButton.setType(ButtonType.PRIMARY);
            removeAlert(titleForm, myPrefixColumnAlert);
            removeAlert(titleForm, myPrefixSizeAlert);
          } else {
            titleForm.add(myPrefixSizeAlert);
          }
        }
      });
      editPreButton.setWidth("90px");
      //titleForm.add(editPreButton);
      prefixFormGroup.add(editPreButton);

      cancelPreButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          cancelPreButton.setVisible(false);
          preTextBox.setValue(form.getPrefix() != null ? form.getPrefix() : "");
          editPreButton.setText("Edit prefix");

          preTextBox.setEnabled(false);
          editPreButton.setType(ButtonType.PRIMARY);
          StyleHelper.removeEnumStyleName(editPreButton, ValidationState.ERROR);
          removeAlert(titleForm, myPrefixColumnAlert);
          removeAlert(titleForm, myPrefixSizeAlert);
        }
      });
      cancelPreButton.setVisible(false);
      cancelPreButton.setWidth("80px");
      prefixFormGroup.add(cancelPreButton);

      titleForm.add(prefixFormGroup);
      CellTable<SurveyBuilderFormQuestion> questionTable = new CellTable<>();
      questionTable.setAutoHeaderRefreshDisabled(true);
      questionTable.setAutoFooterRefreshDisabled(true);

      SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
      SimplePager pager = new SimplePager(TextLocation.LEFT, pagerResources, false, 0, true);
      pager.setDisplay(questionTable);

      OrderColumn<SurveyBuilderFormQuestion> orderColumn = new OrderColumn<SurveyBuilderFormQuestion>() {
        @Override
        public SurveyBuilderFormQuestion getValue(SurveyBuilderFormQuestion object) {
          return object;
        }
      };
      questionTable.addColumn(orderColumn);
      questionTable.setColumnWidth(orderColumn, 35, Unit.PX);

      QuestionColumn<SurveyBuilderFormQuestion> questionColumn = new QuestionColumn<SurveyBuilderFormQuestion>() {
        @Override
        public SurveyBuilderFormQuestion getValue(SurveyBuilderFormQuestion object) {
          return object;
        }
      };
      questionTable.addColumn(questionColumn);
      questionTable.setColumnWidth(questionColumn, 540, Unit.PX);
      ButtonCell editButtonCell = new ButtonCell(ButtonType.PRIMARY, ButtonSize.DEFAULT);
      Column<SurveyBuilderFormQuestion, String> editButtonColumn = new Column<SurveyBuilderFormQuestion, String>(editButtonCell) {
        @Override
        public String getValue(SurveyBuilderFormQuestion question) {
          return "Edit";
        }
      };
      editButtonColumn.setFieldUpdater(new FieldUpdater<SurveyBuilderFormQuestion, String>() {
        @Override
        public void update(int index, SurveyBuilderFormQuestion question, String value) {
          editQuestionPage(form, study, question);
        }

      });
      questionTable.addColumn(editButtonColumn);
      questionTable.setColumnWidth(editButtonColumn, 60, Unit.PX);
      ButtonCell deleteButtonCell = new ButtonCell(ButtonType.DEFAULT, ButtonSize.DEFAULT);
      Column<SurveyBuilderFormQuestion, String> deleteButtonColumn = new Column<SurveyBuilderFormQuestion, String>(deleteButtonCell) {
        @Override
        public String getValue(SurveyBuilderFormQuestion question) {
          return "Delete";
        }
      };
      deleteButtonColumn.setFieldUpdater(new FieldUpdater<SurveyBuilderFormQuestion, String>() {
        @Override
        public void update(int index, SurveyBuilderFormQuestion question, String value) {
          deleteQuestionPage(form, study, question);
        }

      });

      questionTable.addColumn(deleteButtonColumn);
      questionTable.setColumnWidth(deleteButtonColumn, 60, Unit.PX);
      ButtonCell copyButtonCell = new ButtonCell(ButtonType.DEFAULT);
      Column<SurveyBuilderFormQuestion, String> copyButtonColumn = new Column<SurveyBuilderFormQuestion, String>(copyButtonCell) {
        @Override
        public String getValue(SurveyBuilderFormQuestion question) {
          return "Copy";
        }
      };
      copyButtonColumn.setFieldUpdater(new FieldUpdater<SurveyBuilderFormQuestion, String>() {
        @Override
        public void update(int index, SurveyBuilderFormQuestion question, String value) {
          copyQuestionPage(study, form, question);
        }
      });
      questionTable.addColumn(copyButtonColumn);
      questionTable.setColumnWidth(copyButtonColumn, 60, Unit.PX);
      ButtonCell upButtonCell = new ButtonCell(IconType.ARROW_UP, ButtonType.DEFAULT, ButtonSize.DEFAULT);
      Column<SurveyBuilderFormQuestion, String> upButtonColumn = new Column<SurveyBuilderFormQuestion, String>(upButtonCell) {
        @Override
        public String getValue(SurveyBuilderFormQuestion question) {
          return " ";
        }

        @Override
        public void render(Cell.Context context, SurveyBuilderFormQuestion question, SafeHtmlBuilder sb) {
          if (form.getQuestions() == null || form.getQuestions().size() < 2 ||
              question.equals(form.getQuestions().get(0))) {
            sb.appendHtmlConstant("&nbsp;");
          } else {
            super.render(context, question, sb);
          }
        }
      };
      upButtonColumn.setFieldUpdater(new FieldUpdater<SurveyBuilderFormQuestion, String>() {
        @Override
        public void update(int index, SurveyBuilderFormQuestion question, String value) {
          if (form.getQuestions() == null || form.getQuestions().size() < 2 ||
              question.equals(form.getQuestions().get(0))) {
            return;
          }
          SurveyBuilderFormQuestion prevQuestion = form.getQuestions().get(index - 1);
          form.getQuestions().set(index, prevQuestion);
          form.getQuestions().set(index - 1, question);
          studyContent.setConfigValue(reOrderQuestions(form));
          saveStudyContents(studyContent);
          editStudy(studyContent);
        }

      });
      questionTable.addColumn(upButtonColumn);
      questionTable.setColumnWidth(upButtonColumn, 40, Unit.PX);
      ButtonCell downButtonCell = new ButtonCell(IconType.ARROW_DOWN, ButtonType.DEFAULT, ButtonSize.DEFAULT);
      Column<SurveyBuilderFormQuestion, String> downButtonColumn = new Column<SurveyBuilderFormQuestion, String>(downButtonCell) {
        @Override
        public String getValue(SurveyBuilderFormQuestion question) {
          return " ";
        }

        @Override
        public void render(Cell.Context context, SurveyBuilderFormQuestion question, SafeHtmlBuilder sb) {
          if (form.getQuestions() == null || form.getQuestions().size() < 2 ||
              question.equals(form.getQuestions().get(form.getQuestions().size() - 1))) {
            //sb.appendHtmlConstant("<button type=\"button\" class=\"btn btn-default\" tabindex=\"-1\" disabled=\"\">");
            //sb.appendHtmlConstant("<i class=\"fa fa-ban\" /></button>");
            sb.appendHtmlConstant("&nbsp;");
          } else {
            super.render(context, question, sb);
          }
        }
      };
      downButtonColumn.setFieldUpdater(new FieldUpdater<SurveyBuilderFormQuestion, String>() {
        @Override
        public void update(int index, SurveyBuilderFormQuestion question, String value) {
          if (index >= form.getQuestions().size()) {
            return;
          }

          SurveyBuilderFormQuestion nextQuestion = form.getQuestions().get(index + 1);
          form.getQuestions().set(index, nextQuestion);
          form.getQuestions().set(index + 1, question);
          studyContent.setConfigValue(reOrderQuestions(form));
          saveStudyContents(studyContent);
          editStudy(studyContent);
        }
      });

      questionTable.addColumn(downButtonColumn);
      questionTable.setColumnWidth(downButtonColumn, 40, Unit.PX);
      questionTable.addStyleName(css.dataList());
      questionTable.addStyleName(css.scheduleList());
      questionTable.setEmptyTableWidget(new Label("None found. "));
      ListDataProvider<SurveyBuilderFormQuestion> dataProvider = new ListDataProvider<>();
      dataProvider.addDataDisplay(questionTable);
      List<SurveyBuilderFormQuestion> sList = dataProvider.getList();
      if (form.getQuestions() != null) {
        sList.addAll(form.getQuestions());
      }

      questionTable.setSelectionModel(questionSelectionModel);
      questionTable.setRowCount(sList.size());
      questionTable.setPageSize(sList.size());

      pagePanel.add(questionTable);


    }

    final ButtonGroup pageButtons = new ButtonGroup();
    Button addButton = new Button("Add question");
    addButton.addStyleName(css.actionButton());
    addButton.setWidth("200px");
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final SurveyBuilderForm form = AutoBeanCodex.decode(factory, SurveyBuilderForm.class, studyContent.getConfigValue()).as();
        createNewQuestionPage(studyContent, form);
      }
    });
    Button exitButton = new Button("Return");
    exitButton.addStyleName(css.actionButton());
    exitButton.setWidth("200px");
    exitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createEditPage();
      }
    });

    pageButtons.add(addButton);
    pageButtons.add(exitButton);
    pagePanel.add(pageButtons);
  }
  private void testStudy(final StudyContent studyContent) {
    utils.showLoadingPopUp();
    openSurvey(studyContent.getStudyName(), studyContent.getAppConfigId());
  }
  private void openSurvey(final String surveyName, final Long configId) {
    // Since token and URL allow few chars, just use numbers:
    //   1 digit for the number of digits, then the siteId digits, then the name
    String siteIdString = "" + utils.getSiteId();
    String surveySitePlusName = siteIdString.length() + siteIdString + surveyName;
    String url;
    if (builderSurveyPath.contains("?s=")) {
      url = builderSurveyPath + "&tk=" + surveySitePlusName;
    } else {
      url = builderSurveyPath + "?s=bldr&tk=" + surveySitePlusName;
    }
    if (surveyFrame == null) {
      surveyFrame = new Frame(url);
    } else {
      surveyFrame.setUrl(url);
    }
    surveyFrame.getElement().setAttribute("style", "width: 900px; height: 600px; padding: 0px; margin: 0px; border: none;");
    surveyFrame.addLoadHandler(new LoadHandler() {

      @Override
      public void onLoad(LoadEvent event) {
        surveyFrame.setVisible(true);
      }
    });

    final SurveyTimer surveyTimer = new SurveyTimer(configId);

    FlowPanel framePanel = new FlowPanel();
    framePanel.addStyleName(css.borderedVPanel());
    framePanel.add(getTestNavBar(surveyName, surveyTimer));
    framePanel.add(surveyFrame);
    surveyPopup = new PopupRelative(RootLayoutPanel.get().getElement(), framePanel, Align.COVER_CENTER, false,
        new CloseCallback() {
          @Override
          public void afterClose() {
            if (surveyTimer.isRunning()) {
              surveyTimer.cancel();
            }
          }
        });
    surveyTimer.scheduleRepeating(3000);
  }

  private void deleteStudy(final StudyContent studyContent) {
    final Modal confirmDelete = new Modal();
    confirmDelete.setClosable(true);

    final ModalBody deleteBody = new ModalBody();
    deleteBody.add(new Legend("Are you sure you want to delete the study '" + studyContent.getStudyName() + "'?"));

    Button yesButton = new Button("Yes");
    yesButton.setType(ButtonType.DANGER);
    yesButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        builderService.disableStudyContent(studyContent.getAppConfigId(), studyContent.getStudyName(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {

          }

          @Override
          public void onSuccess(Void result) {
            confirmDelete.hide();
            createEditPage();
          }
        });
      }
    });
    Button noButton = new Button("No");
    noButton.setType(ButtonType.DEFAULT);
    noButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        confirmDelete.hide();
      }
    });
    final ModalFooter deleteFooter = new ModalFooter();
    deleteFooter.add(yesButton);
    deleteFooter.add(noButton);

    confirmDelete.add(deleteBody);
    confirmDelete.add(deleteFooter);

    confirmDelete.show();

  }

  private void viewXml(final StudyContent studyContent) {
    pagePanel.clear();
    pagePanel.setWidth("1200px");
    pagePanel.setHeight("700px");
    String title = studyContent.getTitle() == null ? "(no title)" : studyContent.getTitle();
    Label studyLabel = new Label(title + " [" + studyContent.getStudyName() + "]");
    studyLabel.addStyleName(css.titleLabel());
    pagePanel.add(studyLabel);
    final Form xmlForm = new Form();
    pagePanel.add(xmlForm);
    if (!studyContent.getConfigValue().isEmpty()) {
      builderService.getStudyAsXml(studyContent.getAppConfigId(), new Callback<String>() {
        @Override
        public final void handleSuccess(String xmlString) {
          Pre xmlLabel = new Pre();
          xmlLabel.setHTML(xmlString);
          xmlLabel.setScrollable(true);
          StyleHelper.addEnumStyleName(xmlLabel, Alignment.LEFT);
          xmlForm.add(xmlLabel);
          Button cancelButton = new Button("Return");
          cancelButton.addStyleName(css.actionButton());
          cancelButton.setWidth("200px");
          cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              createEditPage();
            }
          });
          xmlForm.add(cancelButton);
        }
      });
    }
  }

  private void exportXml(final StudyContent studyContent) {
    if (!studyContent.getConfigValue().isEmpty()) {
      builderService.getStudyAsXml(studyContent.getAppConfigId(), new Callback<String>() {
        @Override
        public final void handleSuccess(String xmlString) {
          exportXmlJs(xmlString, studyContent.getStudyName());
        }
      });
    }
  }
  private void viewDoc(final StudyContent studyContent) {
    final SurveyBuilderForm builderForm = AutoBeanCodex.decode(factory, SurveyBuilderForm.class, studyContent.getConfigValue()).as();
    if (builderForm.getPrefix() == null || builderForm.getPrefix().isEmpty()) {
      // first ask for the prefix if there isn't one
      final Form form = new Form();
      final Modal viewModal = new Modal();
      viewModal.setClosable(true);
      final ModalBody modalBody = new ModalBody();
      modalBody.add(form);
      final TextBox prefix = new TextBox();
      prefix.setId("pre");
      prefix.setAllowBlank(false);
      final FormGroup inputGroup = new FormGroup();
      inputGroup.add(FormWidgets.formLabelFor("Enter a short column prefix", "pre"));
      InlineHelpBlock help = new InlineHelpBlock();
      help.setIconType(IconType.EXCLAMATION_TRIANGLE);
      inputGroup.add(help);
      inputGroup.add(prefix);
      form.add(inputGroup);

      prefix.addValidator(getPrefixValidator(prefix));
      prefix.setPlaceholder("no spaces");
      prefix.setWidth("200px");
      form.add(new Label("This will be used along with the response \"Reference\" names as the column names for the study data"));

      ModalFooter footer = new ModalFooter();
      Button saveButton = new Button("Continue");
      saveButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (form.validate(true)) {
            viewModal.hide();
            getDoc(studyContent, prefix.getValue().trim());
          }
        }
      });
      footer.add(saveButton);
      final Button quitButton = new Button("Quit");
      quitButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          viewModal.hide();
        }
      });

      footer.add(quitButton);
      viewModal.add(modalBody);
      viewModal.add(footer);
      viewModal.show();
    } else { // there is a prefix so just build the doc
      getDoc(studyContent, builderForm.getPrefix());
    }

  }

  private void getDoc(StudyContent studyContent, String prefix) {
    pagePanel.clear();
    pagePanel.setWidth("1200px");
    pagePanel.setHeight("700px");
    Label studyLabel = new Label(studyContent.getTitle() + " [" + studyContent.getStudyName() + "]");
    studyLabel.addStyleName(css.titleLabel());
    pagePanel.add(studyLabel);
    final Form xmlForm = new Form();
    pagePanel.add(xmlForm);
    if (!studyContent.getConfigValue().isEmpty()) {
      builderService.getStudyDocumentation(studyContent, prefix, new AsyncCallback<ArrayList<String>>() {
        @Override
        public final void onSuccess(ArrayList<String> docString) {
          CellTable<UserDoc> docTable = new CellTable<>();
          docTable.setAutoHeaderRefreshDisabled(true);
          docTable.setAutoFooterRefreshDisabled(true);

          docTable.addStyleName(css.dataList());
          SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
          SimplePager pager = new SimplePager(TextLocation.LEFT, pagerResources, false, 0, true);
          pager.setDisplay(docTable);

          Column<UserDoc, String> question = new Column<UserDoc, String>(new ClickableTextCell()) {
            @Override
            public String getValue(UserDoc object) {
              return object.getQuestion();
            }
          };
          docTable.addColumn(question, "Question");

          Column<UserDoc, String> column = new Column<UserDoc, String>(new ClickableTextCell()) {
            @Override
            public String getValue(UserDoc object) {
              return object.getColumn();
            }
          };
          docTable.addColumn(column, "Column");
          Column<UserDoc, String> type = new Column<UserDoc, String>(new ClickableTextCell()) {
            @Override
            public String getValue(UserDoc object) {
              return object.getType();
            }
          };
          docTable.addColumn(type, "Type");
          Column<UserDoc, String> response = new Column<UserDoc, String>(new ClickableTextCell()) {
            @Override
            public String getValue(UserDoc object) {
              return object.getResponse();
            }
          };
          docTable.addColumn(response, "Response");
          Column<UserDoc, String> value = new Column<UserDoc, String>(new ClickableTextCell()) {
            @Override
            public String getValue(UserDoc object) {
              return object.getValue();
            }
          };
          docTable.addColumn(value, "Value");
          docTable.setEmptyTableWidget(new Label("None found. "));
          ListDataProvider<UserDoc> dataProvider = new ListDataProvider<>();
          dataProvider.addDataDisplay(docTable);
          List<UserDoc> sList = dataProvider.getList();
          if (docString != null && docString.size() > 0) {
            for (String str : docString) {
              sList.add(new UserDoc(str));
            }
          }
          docTable.setSelectionModel(userDocSelectionModel);
          docTable.setRowCount(sList.size());
          docTable.setPageSize(sList.size());
          docTable.setWidth("96%");

          xmlForm.add(docTable);
          Button cancelButton = new Button("Return");
          cancelButton.addStyleName(css.actionButton());
          cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              createEditPage();
            }
          });
          xmlForm.add(cancelButton);
        }

        @Override
        public final void onFailure(Throwable error) {
          Pre docLabel = new Pre();
          docLabel.setHTML(error.getMessage());
          docLabel.setScrollable(true);
          StyleHelper.addEnumStyleName(docLabel, Alignment.LEFT);
          xmlForm.add(docLabel);
          Button cancelButton = new Button("Return");
          cancelButton.addStyleName(css.actionButton());
          cancelButton.setWidth("200px");
          cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              createEditPage();
            }
          });
          xmlForm.add(cancelButton);
        }
      });
    }
  }

  private String reOrderQuestions(SurveyBuilderForm form) {
    for (int inx = 0; inx < form.getQuestions().size(); inx++) {
      form.getQuestions().get(inx).setOrder(Integer.toString(inx));
      form.getQuestions().get(inx).setId("Q" + Integer.toString(inx));
    }
    return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(form)).getPayload();
  }

  private Widget createStudiesTable(final List<StudyContent> studiesList) {
    ListHandler<StudyContent> sortDataHandler = new ListHandler<>(new ArrayList<StudyContent>());
    CellTable<StudyContent> studiesTable = new CellTable<>(15, KEY_PROVIDER);
    studiesTable.setAutoHeaderRefreshDisabled(true);
    studiesTable.setAutoFooterRefreshDisabled(true);

    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
    SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
    pager.setDisplay(studiesTable);

    Column<StudyContent, String> studyName = new Column<StudyContent, String>(new ClickableTextCell()) {
      @Override
      public String getValue(StudyContent object) {
        return object.getStudyName();
      }
    };
    //noinspection unchecked
    sortDataHandler.setComparator (studyName, new Comparator<StudyContent>() {
      @Override
      public int compare(StudyContent o1, StudyContent o2) {
        return o1.getStudyName().compareTo(o2.getStudyName());
      }
    });
    studyName.setSortable(true);
    studyName.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        editStudy(study);
      }
    });
    studiesTable.addColumn(studyName, "Name");
    studiesTable.setColumnWidth(studyName, "200px");
    Column<StudyContent, String> titleCol = new Column<StudyContent, String>(new ClickableTextCell()) {
      @Override
      public String getValue(StudyContent study) {
        return study.getTitle();
      }
    };
    titleCol.setSortable(true);
    //noinspection unchecked
    sortDataHandler.setComparator(titleCol, new Comparator<StudyContent>() {
      @Override
      public int compare(StudyContent o1, StudyContent o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
    titleCol.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        editStudy(study);
      }
    });
    titleCol.setSortable(true);
    studiesTable.addColumn(titleCol, "Title");
    Column<StudyContent, String> createdCol = new Column<StudyContent, String>(new ClickableTextCell()) {
      @Override
      public String getValue(StudyContent study) {
        return utils.getDateString(study.getDtCreated());
      }
    };
    createdCol.setSortable(true);
    studiesTable.addColumn(createdCol, "Date created");
    ButtonCell editButtonCell = new ButtonCell(ButtonType.PRIMARY);
    Column<StudyContent, String> editButtonColumn = new Column<StudyContent, String>(editButtonCell) {
      @Override
      public String getValue(StudyContent question) {
        return "Edit";
      }
    };
    editButtonColumn.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        editStudy(study);
      }
    });
    studiesTable.addColumn(editButtonColumn);
    studiesTable.setColumnWidth(editButtonColumn, "60px");


    ButtonCell testButtonCell = new ButtonCell(ButtonType.DEFAULT);
    Column<StudyContent, String> testButtonColumn = new Column<StudyContent, String>(testButtonCell) {
      @Override
      public String getValue(StudyContent question) {
        return "Test";
      }
    };

    testButtonColumn.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        testStudy(study);
      }
    });
    studiesTable.addColumn(testButtonColumn);
    studiesTable.setColumnWidth(testButtonColumn, "60px");
    ButtonCell viewXmlButtonCell = new ButtonCell(ButtonType.DEFAULT);

    Column<StudyContent, String> viewXmlButtonColumn = new Column<StudyContent, String>(viewXmlButtonCell) {
      @Override
      public String getValue(StudyContent question) {
        return "View Xml";
      }
    };
    viewXmlButtonColumn.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        viewXml(study);
      }

    });
    studiesTable.addColumn(viewXmlButtonColumn);
    studiesTable.setColumnWidth(viewXmlButtonColumn, "100px");
    ButtonCell viewDocButtonCell = new ButtonCell(ButtonType.DEFAULT);
    Column<StudyContent, String> viewDocButtonColumn = new Column<StudyContent, String>(viewDocButtonCell) {
      @Override
      public String getValue(StudyContent question) {
        return "View Documentation";
      }
    };
    viewDocButtonColumn.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        viewDoc(study);
      }

    });
    studiesTable.addColumn(viewDocButtonColumn);
    studiesTable.setColumnWidth(viewDocButtonColumn, "165px");
    ButtonCell exportXmlButtonCell = new ButtonCell(ButtonType.DEFAULT);
    Column<StudyContent, String> exportXmlButtonColumn = new Column<StudyContent, String>(exportXmlButtonCell) {
      @Override
      public String getValue(StudyContent object) {
        return "Export Xml";
      }
    };
    exportXmlButtonColumn.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        exportXml(study);
      }
    });
    studiesTable.addColumn(exportXmlButtonColumn);
    studiesTable.setColumnWidth(exportXmlButtonColumn, "100px");

    ButtonCell deleteButtonCell = new ButtonCell(ButtonType.DEFAULT);
    Column<StudyContent, String> deleteButtonColumn = new Column<StudyContent, String>(deleteButtonCell) {
      @Override
      public String getValue(StudyContent question) {
        return "Delete Study";
      }
    };
    deleteButtonColumn.setFieldUpdater(new FieldUpdater<StudyContent, String>() {
      @Override
      public void update(int index, StudyContent study, String value) {
        deleteStudy(study);
      }

    });
    studiesTable.addColumn(deleteButtonColumn);
    studiesTable.setColumnWidth(deleteButtonColumn, "120px");


    studiesTable.addStyleName(css.dataList());
    studiesTable.setEmptyTableWidget(new Label("None found. "));
    ListDataProvider<StudyContent> dataProvider = new ListDataProvider<>();
    dataProvider.addDataDisplay(studiesTable);
    List<StudyContent> sList = dataProvider.getList();
    ListHandler<StudyContent> columnSortHandler = new ListHandler<>(sList);
    columnSortHandler.setComparator(studyName,
        new Comparator<StudyContent>() {
          @Override
          public int compare(StudyContent o1, StudyContent o2) {
            if (o1 == o2) {
              return 0;
            }

            if (o1 != null) {
              return (o2 != null) ? o1.getStudyName().compareTo(o2.getStudyName()) : 1;
            }
            return -1;
          }
        });
    columnSortHandler.setComparator(titleCol,
        new Comparator<StudyContent>() {
          @Override
          public int compare(StudyContent o1, StudyContent o2) {
            if (o1 == o2) {
              return 0;
            }

            if (o1 != null) {
              return (o2 != null) ? o1.getTitle().compareTo(o2.getTitle()) : 1;
            }
            return -1;
          }
        });
    columnSortHandler.setComparator(createdCol, new Comparator<StudyContent>() {
      @Override
      public int compare(StudyContent o1, StudyContent o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getDtCreated().compareTo(o2.getDtCreated()) : 1;
        }
        return -1;
      }
    });
    studiesTable.addColumnSortHandler(columnSortHandler);
    studiesTable.getColumnSortList().push(studyName);
    sList.addAll(studiesList);
    studiesTable.setSelectionModel(selectionModel);
    studiesTable.setRowCount(sList.size());
    studiesTable.setPageSize(sList.size());
    studiesTable.setWidth("95%", false);
    return studiesTable;
  }

  private void questionPage(final SurveyBuilderFormQuestion surveyBuilderFormQuestion, final StudyContent study, final SurveyBuilderForm form) {
    final SurveyQuestionBuilder questionBuilder = new SurveyQuestionBuilder(form, surveyBuilderFormQuestion);
    questionBuilder.addSaveClickHandler(getQuestionClickHandler            (questionBuilder, form, study));
    questionBuilder.addDiscardClickHandlers(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editStudy(study);
      }
    });
    questionBuilder.addValidateHandler(new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(Editor<String> editor, String value) {
        List<EditorError> result = new ArrayList<>();
        if (form != null) {
          List<SurveyBuilderFormQuestion> questions;
          if (form.getQuestions() != null) {
            questions = form.getQuestions();
            HashMap<String, String> refTags = new HashMap<>();
            SurveyBuilderFormQuestion formQuestion = questionBuilder.getFormQuestion();
            for (SurveyBuilderFormQuestion question : questions) {
              if (!question.equals(formQuestion)) {
                for (SurveyBuilderFormResponse response : question.getResponses()) {
                  refTags.put(response.getRef(), response.getRef());
                  for (SurveyBuilderFormFieldValue field : response.getValues()) {
                    refTags.put(field.getRef(), field.getRef());
                  }
                }
              }
            }
            //noinspection SuspiciousMethodCalls
            if (refTags.get(value) != null) {
              result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
            }
          }
        }
        return result;
      }
    });
    pagePanel.add(questionBuilder);
  }

  private void editQuestionPage(SurveyBuilderForm form, final StudyContent study, SurveyBuilderFormQuestion surveyBuilderFormQuestion) {
    pagePanel.clear();
    pagePanel.setWidth("1200px");
    questionPage(surveyBuilderFormQuestion, study, form);
  }

  private void createQuestionPage(final StudyContent study, SurveyBuilderForm form, int asItem) {
    pagePanel.clear();
    pagePanel.setWidth("1200px");// Create a new question
    SurveyBuilderFormQuestion formQuestion = factory.formQuestion().as();
    formQuestion.setOrder(String.valueOf(asItem));
    formQuestion.setId("Q" + formQuestion.getOrder());
    ArrayList<SurveyBuilderFormResponse> responses = new ArrayList<>();
    formQuestion.setResponses(responses);
    questionPage(formQuestion, study, form);

  }

  private void copyQuestionPage(final StudyContent study, SurveyBuilderForm form, SurveyBuilderFormQuestion fromQuestion) {
    pagePanel.clear();
    pagePanel.setWidth("1200px");
    // Create a new question and copy the values from the existing question
    SurveyBuilderFormQuestion formQuestion = factory.formQuestion().as();
    formQuestion.setOrder(Integer.toString(form.getQuestions().size()));
    formQuestion.setId("Q" + form.getQuestions().size());
    formQuestion.setTitle1(fromQuestion.getTitle1());
    formQuestion.setTitle2(fromQuestion.getTitle2());
    if (fromQuestion.getResponses() != null) {
      formQuestion.setResponses(copyResponses(fromQuestion.getResponses(), formQuestion.getOrder()));
    }

    questionPage(formQuestion, study, form);
  }

  private ArrayList<SurveyBuilderFormResponse> copyResponses(List<SurveyBuilderFormResponse> fromResponses, String newQuestionOrder) {

    ArrayList<SurveyBuilderFormResponse> newResponses = new ArrayList<>();

    for (SurveyBuilderFormResponse response : fromResponses) {
      SurveyBuilderFormResponse newResponse = factory.formResponse().as();
      newResponse.setFieldType(response.getFieldType());
      newResponse.setLabel(response.getLabel());
      newResponse.setOrder(response.getOrder());
      if (response.getRequired() != null) {
        newResponse.setRequired(response.getRequired());
      }
      newResponse.setRef(response.getRef() + "_" + newQuestionOrder);
      if (response.getAttributes() != null) {
        HashMap<String, String> attributes = new HashMap<>();
        for (String key : response.getAttributes().keySet()) {
          String value = response.getAttributes().get(key);
          attributes.put(key, value);
        }
        newResponse.setAttributes(attributes);
      }
      if (response.getValues() != null) {
        ArrayList<SurveyBuilderFormFieldValue> newValues = new ArrayList<>();
        for (SurveyBuilderFormFieldValue value : response.getValues()) {
          SurveyBuilderFormFieldValue newValue = factory.formFieldValue().as();
          newValue.setId(value.getId());
          newValue.setLabel(value.getLabel());
          newValue.setRef(value.getRef());
          newValues.add(newValue);
        }
        newResponse.setValues(newValues);
      }
      newResponses.add(newResponse);
    }
    return newResponses;
  }

  private void createNewQuestionPage(final StudyContent study, SurveyBuilderForm form) {
    Integer numQuestions;
    if (form != null && form.getQuestions() != null) {
      numQuestions = form.getQuestions().size();
    } else {
      numQuestions = 0;
    }
    createQuestionPage(study, form, numQuestions);
  }

  private ClickHandler getQuestionClickHandler(final SurveyQuestionBuilder questionBuilder, final SurveyBuilderForm form, final StudyContent studyContent) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        SurveyBuilderFormQuestion formQuestion = questionBuilder.getFormQuestion();
        if (formQuestion == null || formQuestion.getTitle1() == null) {
          ErrorDialogWidget errorDialogWidget = new ErrorDialogWidget();
          errorDialogWidget.setError("Missing question!");
          errorDialogWidget.show();
        } else {
          if (form != null) {
            List<SurveyBuilderFormQuestion> questions;
            if (form.getQuestions() == null) {
              questions = new ArrayList<>();
              questions.add(formQuestion);
            } else {
              questions = form.getQuestions();
              boolean found = false;
              for (SurveyBuilderFormQuestion question : questions) {
                if (question.equals(formQuestion)) {
                  found = true;
                }
              }
              if (!found) {
                questions.add(formQuestion);
              }
            }
            for (SurveyBuilderFormQuestion q : questions) {
              if (q.getId() == null || q.getId().isEmpty()) {
                q.setId("Q" + q.getOrder());
              }
            }
            form.setQuestions(questions);
          }

          studyContent.setConfigValue(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(form)).getPayload());
          saveStudyContents(studyContent);
          if (event.getSource() instanceof  Button) {
            Button fromButton = (Button) event.getSource();
            if ("Save question".equals(fromButton.getText())) {
              editStudy(studyContent);
            }
          }
        }
      }
    };
  }

  private Alert getAlert(String alertMessage) {
    final Alert alert = new Alert();
    alert.add(new Heading(HeadingSize.H5, alertMessage));
    alert.setType(AlertType.DANGER);
    alert.setDismissable(true);
    return alert;
  }

  private void removeAlert(Form form, Alert alert) {
    if (form.getWidgetIndex(alert) >= 0) {
      form.remove(alert);
    }
  }

  private Validator<String> getPrefixValidator(final TextBox prefix) {
    return new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(Editor<String> editor, String value) {
        final List<EditorError> result = new ArrayList<>();
        final String valueStr = value == null ? "" : value.trim();
        // Todo: Add server check for unicode alphanumeric with regex "^[\p{L} \p{Nd}_]+$"
        if (valueStr.length() < 1) {
          result.add(new BasicEditorError(prefix, value, "Prefix is required!"));
        } else if (valueStr.contains(" ")) {
          result.add(new BasicEditorError(prefix, value, "Contains spaces!"));
        } else if (valueStr.length() > 12) {
          result.add(new BasicEditorError(prefix, value, "Prefix is too long!"));
        } else if (!valueStr.matches("^[a-zA-Z0-9_]*$")) {
          result.add(new BasicEditorError(prefix, value, ALPHA_NUM_ERROR));
        }
        return result;
      }
    };
  }

  private boolean prefixTooLong(String prefix, SurveyBuilderForm form) {
    for (SurveyBuilderFormQuestion question : form.getQuestions()) {
      for (SurveyBuilderFormResponse response : question.getResponses()) {
        if ((prefix + "_" + response.getRef()).length() > 30) {
          return true;
        }
      }
    }
    return false;
  }

  private void deleteQuestionPage(SurveyBuilderForm form, final StudyContent studyContent, SurveyBuilderFormQuestion question) {
    form.getQuestions().remove(question);
    studyContent.setConfigValue(reOrderQuestions(form));
    saveStudyContents(studyContent);
    editStudy(studyContent);

  }

  private void saveStudyContents(StudyContent studyContent) {
    if (studyContent.getSurveySiteId() == 0L) {
      studyContent.setSurveySiteId(utils.getSiteId());
      studyContent.setAppConfigId(null);
    }
    builderService.saveStudyContent(studyContent, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
      }

      @Override
      public void onSuccess(Void result)  {
      }
    });
  }

  private BlurHandler getAlphaNumBlurHandler(final FormGroup form, final TextBox value, final InlineHelpBlock help) {
    return new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        if (!value.getValue().trim().matches("^[a-zA-Z0-9:]*$")) {
          form.setValidationState(ValidationState.ERROR);
          help.setError(ALPHA_NUM_ERROR);
        } else {
          help.clearError();
          form.setValidationState(ValidationState.NONE);
        }
      }
    };
  }

  private void saveStudyTitle(StudyContent studyContent) {
    try {
      builderService.updateStudyTitle(studyContent, new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          showFailure(caught);
        }

        @Override
        public void onSuccess(Void result) {

        }
      });
    } catch (SurveyException e) {
      showFailure(e);
    }
  }


  private void showFailure(Throwable error) {
    final Modal showerror = new Modal();
    showerror.setClosable(true);

    final ModalBody errorBody = new ModalBody();
    errorBody.add(new Legend("Update failed'" + error.getMessage()));

    Button closeButton = new Button("Close");
    closeButton.setType(ButtonType.DANGER);
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showerror.hide();
      }
    });
  }

  private static class OrderCell extends AbstractCell<SurveyBuilderFormQuestion> implements Cell<SurveyBuilderFormQuestion> {
    OrderCell() {
      super("click", "keydown");
    }
    @Override
    public void render(Context context, SurveyBuilderFormQuestion question, SafeHtmlBuilder sb) {
      if (question == null) {
        return;
      }
      sb.appendHtmlConstant("<h5>");
      try {
        int order = Integer.parseInt(question.getOrder()) + 1;
        sb.append(order);
      } catch (NumberFormatException nfe) {
        sb.appendEscaped(question.getOrder());
      }
      sb.appendHtmlConstant(") </h5>");
    }
  }

  private static abstract class OrderColumn<T> extends Column<T, SurveyBuilderFormQuestion> {
    OrderColumn() {
      super(new OrderCell());
    }
    @Override
    public SurveyBuilderFormQuestion getValue(T object) {
      return null;
    }
  }

  private static class QuestionCell extends AbstractCell<SurveyBuilderFormQuestion> implements Cell<SurveyBuilderFormQuestion> {
    QuestionCell() {
      super("click", "keydown");
    }

    @Override
    public void render(Context context, SurveyBuilderFormQuestion question, SafeHtmlBuilder sb) {
      if (question == null) {
        return;
      }
      sb.appendHtmlConstant("<h5>");
      sb.append(Sanitizer.sanitizeHtml(question.getTitle1()));
      sb.appendHtmlConstant("</h5>");
      sb.appendHtmlConstant("<h5>");
      if (question.getTitle2() != null)
        sb.append(Sanitizer.sanitizeHtml(question.getTitle2()));
      sb.appendHtmlConstant("</h5>");
    }
  }

  private static abstract class QuestionColumn<T> extends Column<T, SurveyBuilderFormQuestion> {
    QuestionColumn() {
      super(new QuestionCell());
    }
    @Override
    public SurveyBuilderFormQuestion getValue(T object) {
      return null;
    }
  }


  private static class UserDoc {
    private String question = null;
    private String column = null;
    private String type = null;
    private String response = null;
    private String value = null;

    UserDoc(String line) {
      String[] tokens = split(line);

      if (tokens.length > 0) {
        setQuestion(withoutQuotes(tokens[0]));
      }
      if (tokens.length > 1) {
        setColumn(withoutQuotes(tokens[1]));
      }
      if (tokens.length > 2) {
        setType(withoutQuotes(tokens[2]));
      }
      if (tokens.length > 3) {
        setResponse(withoutQuotes(tokens[3]));
      }
      if (tokens.length > 4) {
        setValue(withoutQuotes(tokens[4]));
      }

    }

    String getQuestion() {
      return question;
    }

    void setQuestion(String q) {
      question = q;
    }

    String getColumn() {
      return column;
    }

    void setColumn(String c) {
      column = c;
    }

    String getType() {
      return type;
    }

    void setType(String t) {
      type = t;
    }

    String getResponse() {
      return response;
    }

    void setResponse(String r) {
      response = r;
    }

    String getValue() {
      return value;
    }

    void setValue(String v) {
      value = v;
    }

    private String withoutQuotes(String str) {
      if (str == null ||
          (str.length() == 2 && str.startsWith("\"") && str.endsWith("\""))) {
        return "";
      }
      if (str.startsWith("\"")) {
        str = str.substring(1);
      }
      if (str.endsWith("\"")) {
        return str.substring(0, str.length() - 1);
      }
      return str;
    }

    private String[] split(String line) {
      ArrayList<StringBuilder> fields = new ArrayList<>();
      String[] tokens = line.split(",");
      boolean inQuote = false;
      for (String token : tokens) {
        if (inQuote) {
          fields.get(fields.size() - 1).append(token);
        } else {
          fields.add(new StringBuilder(token));
        }
        if (token.startsWith("\"")) {
          inQuote = true;
        }
        if (token.endsWith("\"")) {
          inQuote = false;
        }
      }
      String[] result = new String[fields.size()];
      int inx = 0;
      for (StringBuilder field : fields) {
        result[inx] = field.toString();
        inx++;
      }
      return result;
    }
  }

  private KeyUpHandler noSpaceHandler(final TextBox textBox) {
     return new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        textBox.setText(textBox.getText().trim());
      }
    };
  }

  private Navbar getTestNavBar(final String surveyName, final SurveyTimer surveyTimer) {
    Navbar testNavBar = new Navbar();
    NavbarText text = new NavbarText();
    text.add(new Span("Testing survey " + surveyName));
    text.setPull(Pull.LEFT);
    testNavBar.add(text);

    NavbarButton button = new NavbarButton();
    button.setText("Close");
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        surveyTimer.stop();
      }
    });
    button.setPull(Pull.RIGHT);
    testNavBar.add(button);
    return testNavBar;
  }

  private class SurveyTimer extends Timer {
    int runttimes = 0;
    boolean inRunCall; // true until callback returns

    final Long configId;

    SurveyTimer(Long configId) {
      this.configId = configId;
    }

    /**
     * This is used to make the polling timer call bounce (not send the callback)
     * if the previous callback has not returned (which happens during debugging.)
     */
    private synchronized boolean inRunCallAlready(boolean entering) {
      if (!entering) {  // a callback occurred, so leaving the call
        inRunCall = false;
        return false;  // return value isn't used
      }

      // someone wants to get in
      if (inRunCall) {
        return true; // tell them we're not done yet
      } else {
        inRunCall = true;  // we're going in, so set this
        return false;      // let the caller in
      }
    }

    @Override
    public void run() {
      if (inRunCallAlready(true)) {
        return;
      }
      runttimes++;
      if (runttimes > 100) { // auto close if still open after 5 minutes // TODO: at timer to header
        stop();
      } else {
        builderService.isTestFinished(String.valueOf(configId), new Callback<Boolean>() {
          @Override
          protected void afterFailure() {
            ErrorDialogWidget errorDialogWidget = new ErrorDialogWidget();
            errorDialogWidget.setError("Testing failed!");
            errorDialogWidget.show();
            stop();
            inRunCallAlready(false);
          }

          @Override
          public void handleSuccess(Boolean finished) {
            if (finished) {
              stop();
            }
            inRunCallAlready(false);
          }
        });
      }
    }

    private void cleanup() {
      if (surveyPopup != null) {
        surveyPopup.close();
        utils.hideLoadingPopUp();
      }
    }

    void stop() {
      cancel();
      cleanup();
    }
  }
  public native void exportXmlJs(String downloadXMLString, String filename) /*-{
    var blob = new Blob([downloadXMLString], {type: 'text/xml'}),
            e    = document.createEvent('MouseEvents'),
            a    = document.createElement('a');
    a.download = filename + ".xml";
    a.href = window.URL.createObjectURL(blob);
    a.dataset.downloadurl =  ['text/csv', a.download, a.href].join(':');
    e.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    a.dispatchEvent(e);
  }-*/;
}

