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

import edu.stanford.registry.client.api.SurveyBuilderFactory;
import edu.stanford.registry.client.api.SurveyBuilderForm;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Method;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Type;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormQuestion;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.survey.client.api.FieldType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.InlineHelpBlock;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupButton;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.HasValidators;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;

public class SurveyConditionBuilder extends Form {

  private final String METHOD_PLACE_HOLDER = "Select how to compare";
  private final String VALUE_PLACE_HOLDER = "Value to compare";
  private final String DATE_FORMAT = "yyyy-MM-dd"; // use same format survey system returns datepicker answers with
  private final ArrayList<ClickHandler> saveHandlers = new ArrayList<>();
  private final ArrayList<ClickHandler> discardHandlers = new ArrayList<>();
  private final SurveyBuilderFormCondition questionCondition;
  private final TextBox attNameBox = FormWidgets.forTextBox("", "attributeName");
  private final TextBox valBox = FormWidgets.forTextBox("", "compareValue");
  private final Button refAnchorButton = new Button();
  private TextBox refValidationBox = new TextBox();
  private final Button compAnchorButton = new Button();
  private final TextBox compValidationBox = new TextBox();
  private final SurveyBuilderForm form;
  private final FormGroup subTypeGroups = new FormGroup();
  private final ArrayList<CheckBox> choices = new ArrayList<>();
  private final SurveyBuilderFactory factory = GWT.create(SurveyBuilderFactory.class);

  SurveyConditionBuilder(SurveyBuilderForm surveyBuilderForm, SurveyBuilderFormCondition formCondition, String questionOrder) {
    form = surveyBuilderForm;
    questionCondition = formCondition;

    if (questionCondition.getAttributes() == null) {
      questionCondition.setAttributes(new HashMap<String, String>());
    }
    buildPage(questionOrder);
  }

  void addSaveClickHandler(ClickHandler handler) {
    saveHandlers.add(handler);
  }

  void addDiscardHandler(ClickHandler handler) {
    discardHandlers.add(handler);
  }

  private void buildPage(String qOrder) {
    final FormGroup mainGroup = new FormGroup();
    this.clear();
    mainGroup.clear();
    this.add(mainGroup);
    this.setType(FormType.HORIZONTAL);
    FieldSet fieldSet = new FieldSet();
    final FormGroup conditionGroup = new FormGroup();
    FormLabel typeLabel = FormWidgets.formLabelFor("Condition Type", "typeBox", ColumnSize.MD_2);
    conditionGroup.add(typeLabel);
    final TextBox typeBox = FormWidgets.forTextBox(questionCondition.getType().label(), "typeBox");
    typeBox.setEnabled(false);
    conditionGroup.add(wrapInFlowPanel(typeBox));
    fieldSet.add(conditionGroup);
    DropDownMenu compareMethodMenu = // new DropDownMenu();
    getMethodDropDown(   );
    mainGroup.add(addTypeFields(questionCondition.getType(), compareMethodMenu, qOrder));
    mainGroup.add(subTypeGroups);

    // create list for comparing the value
    FormGroup compGroup = new FormGroup();
    compGroup.getElement().getStyle().setPaddingLeft(25, Unit.PX);
    compValidationBox.setVisible(false);

    if (questionCondition.getMethod() != null) {
      if (questionCondition.getMethod().value().equals(Method.exists.value())
          || questionCondition.getMethod().value().equals(Method.notexists.value())) {
        valBox.setVisible(false);
      }
    }
    compAnchorButton.setDataToggle(Toggle.DROPDOWN);
    compAnchorButton.getElement().getStyle().setFloat(Float.RIGHT);
    StyleHelper.addEnumStyleName(compAnchorButton, ColumnSize.MD_10);

    compValidationBox.addValidator(new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(final Editor<String> editor, final String compValidationBoxValue) {
        final List<EditorError> result = new ArrayList<>();
        String compStr = compValidationBoxValue == null ? "" : compValidationBoxValue.trim();
        if (compStr.length() < 1) {
          result.add(new BasicEditorError(compValidationBox, compValidationBoxValue, METHOD_PLACE_HOLDER));
        }

        String valueStr = valBox.getValue() == null ? "" : valBox.getValue();
        String value = compValidationBox.getValue();
          if (Method.equal.label().equals(value) || Method.greaterthan.label().equals(value) ||
              Method.greaterequal.label().equals(value) || Method.lessequal.label().equals(value) ||
            Method.lessthan.label().equals(value) || Method.notequal.label().equals(value)) {
            if (valueStr.trim().length() < 1) {
              result.add(new BasicEditorError(valBox, value,
                  "Value to compare is required for " + compAnchorButton.getText()));
            } else if (valueStr.contains(" ")) {
              result.add(new BasicEditorError(valBox, value, "Value to compare '" + valueStr + "'  can not have spaces!"));
            } else if (!valueStr.trim().matches("^[a-zA-Z0-9.-]*$")) {
              result.add(new BasicEditorError(valBox, value, "Value to compare can not contain any special characters other than a dash '-', or period '.'"));
            } else {
              for (final SurveyBuilderFormQuestion formQuestion : form.getQuestions()) {
                for (final SurveyBuilderFormResponse response : formQuestion.getResponses()) {
                  if (response.getRef() != null && response.getRef().equals(refValidationBox.getValue())) {
                  if (response.getFieldType() == FieldType.number || response.getFieldType() == FieldType.numericScale ||
                      response.getFieldType() == FieldType.numericSlider) {
                    try {
                      int number = Integer.parseInt(valueStr);
                      if (response.getFieldType() == FieldType.numericScale || response.getFieldType() == FieldType.numericSlider ) {
                        if (response.getValues() != null && response.getValues().size() > 0
                            && response.getValues().get(0) != null && response.getValues().get(0).getId() != null) {
                          int lowerBound =  Integer.parseInt(response.getValues().get(0).getId());
                          int upperBound = Integer.parseInt(response.getValues().get(response.getValues().size() - 1).getId());
                          if (number > upperBound || number < lowerBound) {
                              result.add(new BasicEditorError(valBox, value,
                                "Value must be in the range " + lowerBound + " to " + upperBound));
                          }
                        }
                      }
                    } catch (NumberFormatException nfe) {
                      result.add(new BasicEditorError(valBox, value, "Value to compare must be a valid number"));
                    }
                  } else if (response.getFieldType() == FieldType.datePicker) {
                    try {
                      DateTimeFormat.getFormat(DATE_FORMAT).parse(valueStr);
                    } catch (Exception e) {
                      result.add(new BasicEditorError(valBox, value, "Value must be a valid date in the format " + DATE_FORMAT.toUpperCase()));
                    }
                  }
                }
              }
            }
          }
        }
        return result;
      }
    });
    final InlineHelpBlock valHelp = new InlineHelpBlock();
    valHelp.setIconType(IconType.EXCLAMATION_TRIANGLE);
    compGroup.add(valHelp);

    ButtonGroup compButtonGroup = new ButtonGroup();
    compButtonGroup.setId("compBox");
    StyleHelper.addEnumStyleName(compButtonGroup, ColumnSize.MD_4);
    compButtonGroup.add(compAnchorButton);
    compButtonGroup.add(compareMethodMenu);
    compGroup.add(compButtonGroup);
    compGroup.add(wrapInFlowPanel(valBox));
    compGroup.add(compValidationBox);
    mainGroup.add(compGroup);

    if (questionCondition.getDataValue() != null) {
      valBox.setValue(questionCondition.getDataValue());
    }

    if (valBox.getValue() == null || valBox.getValue().isEmpty()) {
      valBox.setPlaceholder(VALUE_PLACE_HOLDER);
    }
    mainGroup.add(getControlRow(mainGroup));
  }

  private DropDownMenu getMethodDropDown( ) {
    DropDownMenu compareMethodMenu = new DropDownMenu();
    return getMethodDropDown(compareMethodMenu);
  }

  private DropDownMenu getMethodDropDown(DropDownMenu compareMethodMenu) {
    compareMethodMenu.clear();
    compAnchorButton.setText(METHOD_PLACE_HOLDER);
    compValidationBox.clear();

    if (questionCondition.getMethod() != null) {
      compAnchorButton.setText(getLabelForType(questionCondition.getType(), questionCondition.getMethod()));
      compValidationBox.setValue(questionCondition.getMethod().value());
    }
    for (final Method compareMethod : SurveyBuilderFormCondition.Method.values()) {
      if (!questionCondition.getType().value().equals(Type.item.value()) || compareMethod == Method.exists
          || compareMethod == Method.notexists) {
        AnchorListItem listItem = new AnchorListItem( getLabelForType(questionCondition.getType(), compareMethod));
        listItem.addClickHandler(methodListHandler(compareMethod));
        compareMethodMenu.add(listItem);
      }
    }
    return compareMethodMenu;
  }

  private ClickHandler methodListHandler(final Method compareMethod) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        compAnchorButton.setText(   getLabelForType(questionCondition.getType(), compareMethod));
        compValidationBox.setValue(compareMethod.value());
        questionCondition.setMethod(compareMethod);
        if (compareMethod.value().equals(Method.exists.value())
            || compareMethod.value().equals(Method.notexists.value())) {
          valBox.setVisible(false);
          valBox.setValue("");
        } else {
          valBox.setVisible(true);
        }
      }
    };
  }

  private static String getLabelForType(Type type, Method method) {
    if (type == Type.item) {
      if (method == Method.exists) {
        return "Selected";
      }
      if (method == Method.notexists) {
        return "Not selected";
      }
    }
    return method.label();
  }

  private void addSaveHandler(Button saveButton, final FormGroup formGroup) {
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (validate()) {
          save(event);
        } else {
          for (final HasValidators<?> child : getChildrenWithValidators(formGroup)) {
            if (!child.validate() && child instanceof TextBox) {
              final TextBox childTextBox = (TextBox) child;
              StyleHelper.addEnumStyleName(childTextBox, AlertType.DANGER);
              childTextBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                  StyleHelper.removeEnumStyleName(childTextBox, AlertType.DANGER);

                }
              });
            }
          }
        }
      }
    });
  }

  private void save(ClickEvent event) {
    if (questionCondition.getType().value().equals(Type.patientAttribute.value())) {
      questionCondition.getAttributes().put(Type.patientAttribute.value(), attNameBox.getValue());
    } else {
      SurveyBuilderFormFieldValue surveyBuilderFormFieldValue = factory.formFieldValue().as();
      if (questionCondition.getType() == null) {
        questionCondition.setType(Type.response);
      }
      surveyBuilderFormFieldValue.setRef(refValidationBox.getValue());

      for (final SurveyBuilderFormQuestion formQuestion : form.getQuestions()) {
        for (SurveyBuilderFormResponse response : formQuestion.getResponses()) {
          if (response.getRef().equals(refValidationBox.getText())) {
            if (response.getFieldType() == FieldType.checkboxes || response.getFieldType() == FieldType.radios) {
              ArrayList<SurveyBuilderFormResponse> responses = new ArrayList<>();
              questionCondition.setType(Type.item);
              for (CheckBox choice : choices) {
                if (choice.getValue()) {
                  for (SurveyBuilderFormFieldValue value : response.getValues()) {
                    if (choice.getId().equals(value.getId())) {
                      SurveyBuilderFormResponse chosenResponse = factory.formResponse().as();
                      chosenResponse.setFieldType(response.getFieldType());
                      chosenResponse.setRef(value.getRef());
                      chosenResponse.setLabel(value.getLabel());
                      responses.add(chosenResponse);
                    }
                  }
                }
                surveyBuilderFormFieldValue.setResponses(responses);
              }
              surveyBuilderFormFieldValue.setResponses(responses);
            }
          }
        }
      }
      questionCondition.setValue(surveyBuilderFormFieldValue);
    }
    questionCondition.setDataValue(valBox.getValue());

    if (!saveHandlers.isEmpty()) {
      for (ClickHandler clickHandler : saveHandlers) {
        clickHandler.onClick(event);
      }
    }
  }

  private FormGroup addTypeFields(Type type, final DropDownMenu compareMethodMenu, String qOrder) {
    attNameBox.clear();
    refAnchorButton.clear();

    choices.clear();
    FormGroup typeGroup = new FormGroup();
    if (type.value().equals(Type.patientAttribute.value())) {
      FormLabel attrLabel = FormWidgets.formLabelFor("Ask only when the attribute named", "attributeName", ColumnSize.MD_4);
      typeGroup.add(attrLabel);
      attNameBox.setAllowBlank(false);
      attNameBox.addValidator(new Validator<String>() {
        @Override
        public int getPriority() {
          return 0;
        }

        @Override
        public List<EditorError> validate(final Editor<String> editor, final String value) {
          List<EditorError> result = new ArrayList<>();
          String valueStr = value == null ? "" : value.trim();
          if (valueStr.length() < 1) {
            result.add(new BasicEditorError(attNameBox, value, "Attribute name is required"));
          } else if (!valueStr.trim().matches("^[a-zA-Z0-9_]*$")) {
            result.add(new BasicEditorError(attNameBox, value, "Attribute name cannot contain spaces or any special characters other than an underscore '_'"));
          }
          return result;
        }
      });

      if (questionCondition.getAttributes().get(Type.patientAttribute.value()) != null) {
        attNameBox.setText(questionCondition.getAttributes().get(Type.patientAttribute.value()));
      }
      if (attNameBox.getValue() == null || attNameBox.getValue().isEmpty()) {
        attNameBox.setPlaceholder("Name");
      }
      typeGroup.add(wrapInFlowPanel(attNameBox));
    } else {
      FormLabel refLabel = FormWidgets.formLabelFor("Ask only when the response with reference ", "refName", ColumnSize.MD_4);
      typeGroup.add(refLabel);
      DropDownMenu referenceMenu = getReferenceDropDown(compareMethodMenu, qOrder);
      final InlineHelpBlock refHelp = new InlineHelpBlock();
      refHelp.setIconType(IconType.EXCLAMATION_TRIANGLE);
      refValidationBox = new TextBox();
      refValidationBox.setVisible(false);
      refValidationBox.addValidator(new Validator<String>() {
        @Override
        public int getPriority() {
          return 0;
        }

        @Override
        public List<EditorError> validate(final Editor<String> editor, final String value) {
          List<EditorError> result = new ArrayList<>();
          String valueStr = value == null ? "" : value.trim();
          if (valueStr.length() < 1) {
            result.add(new BasicEditorError(refValidationBox, value, "Select a column reference !"));
          }
          return result;
        }
      });

      if (questionCondition.getValue() != null) {
        SurveyBuilderFormFieldValue value = questionCondition.getValue();
        refAnchorButton.setText(value.getRef());
        refAnchorButton.setId(value.getId());
        refValidationBox.setText(value.getRef());
        refValidationBox.setId(value.getId());
        for (final SurveyBuilderFormQuestion formQuestion : form.getQuestions()) {
            for (SurveyBuilderFormResponse formResponse : formQuestion.getResponses() ) {
              if (formResponse.getRef().equals(value.getRef()) &&
                  (formResponse.getFieldType() == FieldType.radios || formResponse.getFieldType() == FieldType.checkboxes)) {
                for (SurveyBuilderFormFieldValue formFieldValue : formResponse.getValues() ) {
                  boolean chosen = false;
                  for (SurveyBuilderFormResponse condResp : value.getResponses()) {
                    if (condResp.getLabel().equals(formFieldValue.getLabel()))
                      chosen = true;
                  }
                  addChkBoxChoice(formFieldValue, chosen);
                }
              }
            }
        }
      }
      ButtonGroup refButtonGroup = new ButtonGroup();
      refButtonGroup.setId("refName");
      StyleHelper.addEnumStyleName(refButtonGroup, ColumnSize.MD_4);
      refButtonGroup.add(refAnchorButton);
      refButtonGroup.add(referenceMenu);
      typeGroup.add(wrapInFlowPanel(refButtonGroup));
      typeGroup.add(refValidationBox);
      typeGroup.add(refHelp);
    }
    return typeGroup;
  }

  static Row showCondition(SurveyBuilderFormCondition condition) {
    Row row = new Row();
    Row rowTop = new Row();
    Row rowBottom = new Row();
    row.add(rowTop);
    if (condition.getType() != null) {
      if (condition.getType().value().equals(Type.patientAttribute.value())) {
        if (condition.getAttributes() != null && condition.getAttributes().get(Type.patientAttribute.value()) != null) {
          rowTop.add(FormWidgets.valueColumn(condition.getAttributes().get(Type.patientAttribute.value()), ColumnSize.MD_3));
        }
        if (condition.getMethod() != null) {
          rowTop.add(FormWidgets.valueColumn(condition.getMethod().label(), ColumnSize.MD_2));
        }
        if (condition.getDataValue() != null) {
          rowTop.add(FormWidgets.valueColumn(condition.getDataValue(), ColumnSize.MD_4));
        }
      } else {
        FormLabel rlabel = new FormLabel();
        rlabel.setText("Ask only when the response with reference  ");
        rowTop.add(FormWidgets.formLabelColumn(rlabel, ColumnSize.MD_4));
        if (condition.getValue() != null && condition.getValue().getRef() != null) {
          rowTop.add(FormWidgets.valueColumn(condition.getValue().getRef()));

        }
        if (condition.getValue() != null && condition.getValue().getResponses() != null) {
         for (SurveyBuilderFormResponse resp : condition.getValue().getResponses()) {
            Row midRow = new Row();
            Column spacerColumn = new Column(ColumnSize.MD_2);
            final InputGroup chkBoxGroup = new InputGroup();
            StyleHelper.addEnumStyleName(chkBoxGroup, ColumnSize.MD_8);
            chkBoxGroup.add(spacerColumn);
            CheckBox chkBox = new CheckBox(resp.getLabel());
            chkBox.setEnabled(false);
            chkBox.setPull(Pull.LEFT);
            chkBoxGroup.add(FormWidgets.checkBoxColumn(chkBox,true));
            InputGroupButton groupButton = new InputGroupButton();
            chkBoxGroup.add(groupButton);
            midRow.add(chkBoxGroup);
            row.add(midRow);
           }
        }
        rowBottom.add(FormWidgets.formLabelColumn(new FormLabel(), ColumnSize.MD_4));
        if (condition.getAttributes() != null && condition.getAttributes().get(Type.response.value()) != null) {
          rowBottom.add(FormWidgets.valueColumn(condition.getAttributes().get(Type.response.value())));
        }

        if (condition.getMethod() != null) {
          rowBottom.add(FormWidgets.valueColumn(getLabelForType(condition.getType(), condition.getMethod()), ColumnSize.MD_2));
        }

        if (condition.getDataValue() != null && !condition.getDataValue().isEmpty()) {
          rowBottom.add(FormWidgets.valueColumn(condition.getDataValue(), ColumnSize.MD_2));
        }
      }
    }
    row.add(rowBottom);
    return row;
  }

  public SurveyBuilderFormCondition getCondition() {
    return questionCondition;
  }

  private FlowPanel wrapInFlowPanel(TextBox tb) {
    FlowPanel fp = new FlowPanel();
    StyleHelper.addEnumStyleName(fp, ColumnSize.MD_4);
    fp.add(tb);
    return fp;
  }

  private FlowPanel wrapInFlowPanel(ButtonGroup selection) {
    FlowPanel fp = new FlowPanel();
    StyleHelper.addEnumStyleName(fp, ColumnSize.MD_4);
    fp.add(selection);
    return fp;
  }

  private DropDownMenu getReferenceDropDown(final DropDownMenu compareMethodMenu, String qOrder) {

    refAnchorButton.setText("Select a column reference ");

    if (questionCondition.getValue() != null && questionCondition.getValue().getRef() != null) {
      refAnchorButton.setText(questionCondition.getValue().getRef());
      refValidationBox.setText(questionCondition.getValue().getRef());
    }
    refAnchorButton.setDataToggle(Toggle.DROPDOWN);

    int numReferences = 0;

    if (questionCondition.getType() == null) {
      questionCondition.setType(Type.response);
    }
    DropDownMenu referenceMenu = new DropDownMenu();
    for (final SurveyBuilderFormQuestion formQuestion : form.getQuestions()) {
      try {
        if (Integer.parseInt(qOrder) > Integer.parseInt(formQuestion.getOrder())) {
          for (final SurveyBuilderFormResponse response : formQuestion.getResponses()) {
            if (response.getFieldType() != FieldType.collapsibleContentField &&
                response.getFieldType() != FieldType.heading) {
              final AnchorListItem listItem = new AnchorListItem(response.getRef());
              listItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  refAnchorButton.setText(listItem.getText());
                  refAnchorButton.setId(listItem.getId());
                  refValidationBox.setText(listItem.getText());
                  refValidationBox.setId(listItem.getId());
                  choices.clear();
                  subTypeGroups.clear();
                  if ((response.getFieldType() == FieldType.checkboxes ||
                      response.getFieldType() == FieldType.radios)) {
                    questionCondition.setType(Type.item);
                    valBox.setVisible(false);
                    for (SurveyBuilderFormFieldValue choice : response.getValues()) {
                      addChkBoxChoice(choice, false);
                    }
                  } else {
                    if (valBox.getText().isEmpty()) {
                      if (response.getFieldType() == FieldType.datePicker) {
                        valBox.setPlaceholder(DATE_FORMAT);
                      } else {
                        valBox.setPlaceholder(VALUE_PLACE_HOLDER);
                      }
                    }
                    questionCondition.setType(Type.response);
                    valBox.setVisible(true);
                  }
                  getMethodDropDown(compareMethodMenu );

                }
              });
              referenceMenu.add(listItem);
              numReferences++;
            }
          }
        }
      } catch (NumberFormatException nfe) {
        // nothing
      }
    }

    if (numReferences == 0) {
      refAnchorButton.setText("No previous responses!");
    }
    return referenceMenu;
  }

  private Row getControlRow(FormGroup formGroup) {
    final Row controlRow = new Row();
    final Button saveButton = new Button("Save condition");
    addSaveHandler(saveButton, formGroup);
    final Button exitButton = new Button("Discard changes");
    exitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        choices.clear();
        attNameBox.clear();
        refAnchorButton.clear();
        //refValidationBox.clear();
        valBox.clear();
        compValidationBox.clear();
        //compAnchorButton.clear();
        subTypeGroups.clear();
        if (!discardHandlers.isEmpty()) {
          for (ClickHandler clickHandler : discardHandlers) {
            clickHandler.onClick(event);
          }
        }
      }
    });
    controlRow.add(FormWidgets.formLabelColumn(new FormLabel(), ColumnSize.MD_5));
    ButtonGroup controlButtonGroup = new ButtonGroup();
    StyleHelper.addEnumStyleName(controlButtonGroup, ColumnSize.MD_4);
    saveButton.setSize(ButtonSize.DEFAULT);
    saveButton.setType(ButtonType.PRIMARY);
    controlButtonGroup.add(saveButton);
    exitButton.setSize(ButtonSize.DEFAULT);
    exitButton.setType(ButtonType.DEFAULT);
    controlButtonGroup.add(exitButton);
    controlRow.add(controlButtonGroup);
    return controlRow;
  }

  private void addChkBoxChoice(final SurveyBuilderFormFieldValue field, boolean chosen) {

    // Create a chkBox button choice
    Column spacerColumn = new Column(ColumnSize.MD_2);
    final InputGroup chkBoxGroup = new InputGroup();
    StyleHelper.addEnumStyleName(chkBoxGroup, ColumnSize.MD_8);
    chkBoxGroup.add(spacerColumn);
    CheckBox chkBox = new CheckBox(field.getLabel());
    chkBox.setId(field.getId());
    chkBox.setPull(Pull.LEFT);
    chkBoxGroup.add(FormWidgets.checkBoxColumn(chkBox, chosen));
    choices.add(chkBox);
    InputGroupButton groupButton = new InputGroupButton();
    chkBoxGroup.add(groupButton);

    subTypeGroups.add(chkBoxGroup);
  }

}
