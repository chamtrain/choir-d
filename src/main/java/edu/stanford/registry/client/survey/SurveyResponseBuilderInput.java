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

import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.survey.client.api.FieldType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class SurveyResponseBuilderInput extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {


  public SurveyResponseBuilderInput(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean build) {
    super(surveyBuilderFormResponse, build);
  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    if (formResponse == null || formResponse.getFieldType() == null) {
      return showResponses;
    }
    Row fieldRow = new Row();
    TextBox input = new TextBox();
    if (formResponse.getFieldType() == FieldType.number) {
      input.setPlaceholder("input number");
    } else if (formResponse.getFieldType() == FieldType.textArea) {
      input.setPlaceholder("input text area");
      input.setHeight("100px");
    } else {
      input.setPlaceholder("input text box");
    }
    input.setEnabled(false);
    Column inputColumn = new Column(ColumnSize.MD_6, input);
    fieldRow.add(inputColumn);
    showResponses.add(fieldRow);
    if (formResponse.getFieldType() == FieldType.number && formResponse.getAttributes() != null) {
      Row rangeRow = new Row();
      rangeRow.add(getRangeLabel(ColumnSize.MD_4));
      Map<String, String> attributes = formResponse.getAttributes();
      String rangeFromValue = attributes != null && attributes.containsKey("min") ? attributes.get("min") : "";
      String rangeToValue = attributes != null && attributes.containsKey("max") ? attributes.get("max") : "";
      String decimalsValue = attributes != null && attributes.containsKey("step") ?
          Integer.toString(stepToDecimal(attributes.get("step"))) : "";

      Form rangeForm = new Form();
      rangeForm.setType(FormType.INLINE);
      FormGroup rangeGroup = new FormGroup();

      final TextBox rangeFrom = FormWidgets.forTextBox(rangeFromValue, "frRange");
      rangeFrom.setEnabled(false);
      rangeFrom.setWidth("30px");
      if (rangeFrom.getValue().isEmpty()) {
        rangeFrom.setPlaceholder("-");
      }
      rangeGroup.add(rangeFrom);

      FormLabel toLabel = FormWidgets.formLabelFor("to", "toRange");
      toLabel.getElement().setAttribute("style", "margin: 7px;");
      rangeGroup.add(toLabel);

      final TextBox rangeTo = FormWidgets.forTextBox(rangeToValue, "toRange");
      rangeTo.setEnabled(false);
      rangeTo.setWidth("30px");
      if (rangeTo.getValue().isEmpty()) {
        rangeTo.setPlaceholder("-");
      }
      rangeGroup.add(rangeTo);

      if (!decimalsValue.isEmpty()) {
        FormLabel decLabel = FormWidgets.formLabelFor("with", "decimals");
        decLabel.getElement().setAttribute("style", "margin: 7px;");
        rangeGroup.add(decLabel);
        final TextBox decimals = FormWidgets.forTextBox(decimalsValue, "decimals");
        decimals.setEnabled(false);
        decimals.setWidth("30px");
        rangeGroup.add(decimals);
        rangeGroup.add(getDecimalPlacePostLabel());
      }
      rangeForm.add(rangeGroup);
      Column rangeColumn = new Column(ColumnSize.MD_4);
      rangeColumn.add(rangeForm);

      rangeRow.add(rangeColumn);
      showResponses.add(rangeRow);
    }
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {

    Div thisForm = new Div();
    if (response != null) {
      formResponse.setValues(response.getValues());
      formResponse.setFieldType(response.getFieldType());
      formResponse.setAttributes(response.getAttributes());
      if (response.getRequired() != null) {
        formResponse.setRequired(response.getRequired());
      }
      formResponse.setOrder(response.getOrder());
      formResponse.setLabel(response.getLabel());
      formResponse.setRef(response.getRef());
      CustomInputGroup inputGroup = getNewResponse(response.getFieldType());
      inputGroup.getRangeFrom().setText(
          formResponse.getAttributes() != null && formResponse.getAttributes().containsKey("min") ?
              formResponse.getAttributes().get("min") : "");
      inputGroup.getRangeTo().setText(
          formResponse.getAttributes() != null && formResponse.getAttributes().containsKey("max") ?
              formResponse.getAttributes().get("max") : "");
      inputGroup.getInputTextBox().setText("");
      inputGroup.setStep(
          formResponse.getAttributes() != null && formResponse.getAttributes().containsKey("step") ?
              formResponse.getAttributes().get("step") : "1");

      ((Button) inputGroup.getDecimalsGroup().getWidget(0)).setText(Integer.toString(inputGroup.getDecimalPlaces()));
      inputGroups.add(inputGroup);
      thisForm.add(inputGroup);
    }
    return thisForm;
  }

  @Override
  public InputGroup refreshResponse(int index) {
    return null; // up & down buttons n/a to input
  }

  @Override
  public CustomInputGroup getNewResponse() {
    return getNewResponse(FieldType.text);
  }

  private CustomInputGroup getNewResponse(FieldType fieldType) {
    final CustomInputGroup inputGroup = new CustomInputGroup();
    inputGroup.setWidth("100%");

    Row inputTypeRow = new Row();

    final TextBox inputBox = new TextBox();
    inputTypeRow.add(new Column(ColumnSize.MD_4, inputBox));
    Column inputTypeCol = new Column(ColumnSize.MD_4);
    inputTypeCol.getElement().setAttribute("align", "left");

    final InlineRadio radioText = new InlineRadio("inputType", "Text");
    radioText.getElement().setAttribute("align", "left");
    final InlineRadio radioTextArea = new InlineRadio("inputType", "Area");
    radioTextArea.getElement().setAttribute("align", "left");
    final InlineRadio radioNumber = new InlineRadio("inputType", "Numeric");
    radioNumber.getElement().setAttribute("align", "left");
    if (fieldType == FieldType.text) {
      radioText.setValue(true);
      inputGroup.setType(FieldType.text);
    } else if (fieldType == FieldType.textArea) {
      radioTextArea.setValue(true);
      inputBox.setHeight("100px");
      inputGroup.setType(FieldType.textArea);
    } else if (fieldType == FieldType.number) {
      radioNumber.setValue(true);
      inputGroup.setType(FieldType.number);
    }
    inputBox.setWidth("200px");
    radioText.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (radioText.getValue()) {
          setRangeVisible(inputGroup, false);
          formResponse.setFieldType(FieldType.text);
          inputGroup.setType(FieldType.text);
          inputBox.setHeight("30px");
        }
      }
    });
    inputTypeCol.add(radioText);
    radioTextArea.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (radioTextArea.getValue()) {
          setRangeVisible(inputGroup, false);

          inputGroup.setType(FieldType.textArea);
          inputBox.setHeight("100px");
        }
      }
    });
    inputTypeCol.add(radioTextArea);
    radioNumber.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        if (radioNumber.getValue()) {
          setRangeVisible(inputGroup, true);
          formResponse.setFieldType(FieldType.number);
          inputGroup.setType(FieldType.number);
          inputBox.setHeight("30px");
        }
      }
    });

    inputTypeCol.add(radioNumber);
    inputBox.setEnabled(false);
    inputBox.setHeight("30px");
    inputTypeRow.add(inputTypeCol);
    inputGroup.add(inputTypeRow);

    final TextBox rangeFrom = FormWidgets.forTextBox("", "frRange");
    rangeFrom.setWidth("30px");
    FormLabel toLabel = FormWidgets.formLabelFor("to", "toRange");
    toLabel.getElement().setAttribute("style", "margin-left: 7px; margin-right: 7px; float: left;");
    final TextBox rangeTo = FormWidgets.forTextBox("", "toRange");
    rangeTo.setWidth("30px");

    Row rangeRow = new Row();
    Form rangeLabelForm = new Form();
    rangeLabelForm.setType(FormType.INLINE);
    FormLabel rangeLabel = FormWidgets.formLabelFor("(Optional) numbers must be within the range", "rangeFrom");
    rangeLabel.getElement().setAttribute("float", "right");
    rangeLabelForm.add(rangeLabel);
    rangeRow.add(getRangeLabel(ColumnSize.MD_5));

    Column rangeColumn = new Column(ColumnSize.MD_5);
    rangeColumn.add(rangeFrom);
    rangeColumn.add(toLabel);
    rangeColumn.add(rangeTo);
    rangeRow.add(rangeColumn);

    FormLabel preLabel = FormWidgets.formLabelFor("Allow up to ", "decPlaces");
    preLabel.getElement().setAttribute("style", "margin-left: 7px; margin-right: 7px;");
    rangeColumn.add(preLabel);

    ButtonGroup decimalsGroup = new ButtonGroup();
    final Button anchorButton = new Button();
    anchorButton.setText("0");
    anchorButton.setDataToggle(Toggle.DROPDOWN);
    decimalsGroup.add(anchorButton);
    decimalsGroup.add(getDecimalPlaceDropDown(anchorButton));
    rangeColumn.add(decimalsGroup);
    rangeColumn.add(getDecimalPlacePostLabel());

    inputGroup.setRangeFrom(rangeFrom);
    inputGroup.setRangeTo(rangeTo);
    inputGroup.setDecimalsGroup(decimalsGroup);
    inputGroup.setDecimalPlaces(Integer.parseInt(anchorButton.getText()));
    inputGroup.add(rangeRow);

    inputGroup.setInputTextBox(inputBox);

    inputGroup.setRangeRow(rangeRow);

    setRangeVisible(inputGroup, fieldType == FieldType.number);
    return inputGroup;
  }


  private Column getRangeLabel(ColumnSize sz) {
    FormLabel rangeLabel = FormWidgets.formLabelFor("(Optional) numbers must be within the range","rangeFrom");
    rangeLabel.getElement().setAttribute("float", "right");
    Column rangeLabelCol = new Column(sz);
    rangeLabelCol.getElement().setAttribute("align","right");
    rangeLabelCol.add(rangeLabel);
    return rangeLabelCol;
  }

  private FormLabel getDecimalPlacePostLabel() {
    FormLabel postLabel = new FormLabel();
    postLabel.setText("decimal places");
    postLabel.getElement().setAttribute("style", "margin-left: 7px;");
    return postLabel;
  }

  private DropDownMenu getDecimalPlaceDropDown(final Button anchorButton) {
    DropDownMenu decimalsDropDownMenu = new DropDownMenu();
    decimalsDropDownMenu.clear();
    decimalsDropDownMenu.setPaddingLeft(5.0);
    decimalsDropDownMenu.setPaddingRight(5.0);
    for (int i = 0; i < 4; i++) {
      final int decimals = i;
      AnchorListItem listItem = new AnchorListItem();
      listItem.setText(Integer.toString(i));
      listItem.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          ((CustomInputGroup) inputGroups.get(0)).setDecimalPlaces(decimals);
          anchorButton.setText(Integer.toString(decimals));
        }
      });
      decimalsDropDownMenu.add(listItem);
    }
    return decimalsDropDownMenu;
  }

  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {

    return new ArrayList<>();
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    Map<String, String> attributes = formResponse.getAttributes();
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    CustomInputGroup inputGroup = (CustomInputGroup) inputGroups.get(0);
    if (inputGroup.getRangeFrom() != null && !inputGroup.getRangeFrom().getValue().isEmpty()) {
      attributes.put("min",inputGroup.getRangeFrom().getValue() );
    }
    if (inputGroup.getRangeTo() != null && !inputGroup.getRangeTo().getValue().isEmpty()) {
      attributes.put("max",inputGroup.getRangeTo().getValue() );
    }
    if (attributes.containsKey("step") || inputGroup.getDecimalPlaces() > 0) {
      attributes.put("step", inputGroup.getStep());
    }
    formResponse.setAttributes(attributes);
    return formResponse;
  }

  private void setRangeVisible(CustomInputGroup inputGroup, boolean isVisible) {
    inputGroup.getRangeRow().setVisible(isVisible);
  }

  @Override
  public String getRequiredText() {
    return "Please enter a response";
  }

  private class CustomInputGroup extends  InputGroup {
    TextBox inputTextBox;
    TextBox rangeFrom;
    TextBox rangeTo;
    Row  rangeRow;
    ButtonGroup decimalsGroup;
    int decimalPlaces = 0;
    private void setInputTextBox(TextBox textBox) {
      inputTextBox = textBox;
    }

    private TextBox getInputTextBox() {
      return inputTextBox;
    }

    private void setType(FieldType type) {
      formResponse.setFieldType(type);
    }

    private TextBox getRangeFrom() {
      return rangeFrom;
    }

    private void setRangeRow(Row row) {
      rangeRow = row;
    }
    private Row getRangeRow() {
      return rangeRow;
    }

    private void setRangeFrom(TextBox textBox) {
      rangeFrom = textBox;
    }
    private TextBox getRangeTo() {
      return rangeTo;
    }
    private void setRangeTo(TextBox textBox) {
      rangeTo = textBox;
    }

    private void setStep(String step) {
      setDecimalPlaces(stepToDecimal(step));
      switch (step) {
      case "0.1":
        setDecimalPlaces(1);
        break;
      case "0.01":
        setDecimalPlaces(2);
        break;
      case "0.001":
        setDecimalPlaces(3);
      default:
        setDecimalPlaces(0);
      }
    }

    private String getStep() {
      switch (decimalPlaces) {
      case 3:
        return "0.001";
      case 2:
        return "0.01";
      case 1:
        return "0.1";
      default:
        return "1";
      }
    }

    private void setDecimalPlaces(int places) {
      decimalPlaces = places;
    }

    private int getDecimalPlaces() {
      return decimalPlaces;
    }

    private void setDecimalsGroup(ButtonGroup group) {
      decimalsGroup = group;
    }

    private ButtonGroup getDecimalsGroup() {
      return decimalsGroup;
    }
  }

  @Override
  public boolean hasLabel() {
    return true;
  }

  private int stepToDecimal(String step) {
    switch (step) {
    case "0.1":
      return 1;
    case "0.01":
      return 2;
    case "0.001":
      return 3;
    default:
      return 0;
    }
  }
}
