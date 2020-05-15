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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

public class SurveyResponseBuilderNumericScale extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

  public SurveyResponseBuilderNumericScale(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
    super(surveyBuilderFormResponse, forEdit);
  }

  @Override
  public ArrayList<Row> showResponse() {

    ArrayList<Row> showResponses = new ArrayList<>();
    Row buttonRow = new Row();
    buttonRow.getElement().setAttribute("Style", "align: left");
    Column buttonCol = new Column(ColumnSize.MD_9);
    int startingValue = getLowerBoundId();
    int endingValue = getUpperBoundId();
    if (endingValue == startingValue) {
      endingValue = endingValue + 10;
    }
    float pct  = 100 / (endingValue - startingValue + 1);
    BigDecimal percent = new BigDecimal(pct);
    for (int i = startingValue; i <= endingValue; i++) {
      Button button = new Button();
      button.setText(Integer.toString(i));
      button.setWidth(percent.intValue() + "%");
      buttonCol.add(button);
    }
    buttonRow.add(buttonCol);
    showResponses.add(buttonRow);
    Row labelRow = new Row();
    Column labelCol1 = new Column(ColumnSize.MD_3);
    Paragraph valueBox1 = new Paragraph(getLowerBoundLabel());
    valueBox1.setAlignment(Alignment.LEFT);
    labelCol1.add(valueBox1);
    labelRow.add(labelCol1);
    labelRow.add(new Column(ColumnSize.MD_3)); // spacing
    Column labelCol2 = new Column(ColumnSize.MD_3);
    Paragraph valueBox2 = new Paragraph(getUpperBoundLabel());
    //valueBox2.setText(getUpperBoundLabel());
    valueBox2.setAlignment(Alignment.RIGHT);
    labelCol2.add(valueBox2);
    labelRow.add(labelCol2);
    showResponses.add(labelRow);
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    Div container = new Div();
    formResponse.setValues(response.getValues());
    InputGroup newResponse = getNewResponse();
    container.add(newResponse);
    inputGroups.add(newResponse);
    return container;
  }

  @Override
  public InputGroup getNewResponse() {
    InputGroup thisGroup = new InputGroup();
    buildResponse(thisGroup);
    thisGroup.setWidth("100%");
    return thisGroup;
  }

  private void buildResponse(InputGroup thisGroup) {
    thisGroup.clear();
    final Button lowerBoundButton = new Button();
    final Button upperBoundButton = new Button();
    final TextBox lowerBoundLabel = new TextBox();
    final TextBox upperBoundLabel = new TextBox();
    Column buttonCol = new Column(ColumnSize.MD_10);
    lowerBoundButton.setText(Integer.toString(getLowerBoundId()));
    lowerBoundButton.setSize("50px", "50px");
    buttonCol.add(getValueButton(lowerBoundButton, true));

    int startingValue = getLowerBoundId();
    int endingValue = getUpperBoundId();
    if (endingValue == startingValue) {
      endingValue = endingValue + 10;
    }
    for (int i = (startingValue + 1); i < endingValue; i++) {
      Button button = new Button();
      button.setText(Integer.toString(i));
      button.setSize("50px", "50px");
      buttonCol.add(button);
    }
    upperBoundButton.setText(Integer.toString(endingValue));
    upperBoundButton.setSize("100%", "100%");
    buttonCol.add(getValueButton(upperBoundButton, false));
    Row buttonRow = new Row();
    buttonRow.add(buttonCol);
    thisGroup.add(buttonRow);

    lowerBoundLabel.setId("startlbl");
    lowerBoundLabel.setPlaceholder("(optional) label");
    lowerBoundLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        TextBox textBox = (TextBox) event.getSource();
        setLowerBoundLabel(textBox.getValue());
      }
    });
    if (!getLowerBoundLabel().isEmpty()) {
      lowerBoundLabel.setText(getLowerBoundLabel());
    }
    Column labelCol1 = (FormWidgets.textBoxColumn(lowerBoundLabel, getLowerBoundLabel(), ColumnSize.MD_5));
    ((TextBox) labelCol1.getWidget(0)).setAlignment(TextAlignment.LEFT);
    upperBoundLabel.setId("endval");
    upperBoundLabel.setPlaceholder("(optional) label");
    upperBoundLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        TextBox textBox = (TextBox) event.getSource();
        setUpperBoundLabel(textBox.getText());
      }
    });
    if (!getUpperBoundLabel().isEmpty()) {
      upperBoundLabel.setText(getUpperBoundLabel());
    }
    Column labelCol2 = FormWidgets.textBoxColumn(upperBoundLabel, getUpperBoundLabel(), ColumnSize.MD_5);
    ((TextBox) labelCol2.getWidget(0)).setAlignment(TextAlignment.RIGHT);

    Column labelsCol = new Column(ColumnSize.MD_12);
    labelsCol.add(labelCol1);
    labelsCol.add(labelCol2);
    Row labelsRow = new Row();
    labelsRow.add(labelsCol);
    thisGroup.add(labelsRow);
  }

  @Override
  public InputGroup refreshResponse(int inx) {
    if (inputGroups.size() > inx) {
      InputGroup inputGroup = inputGroups.get(inx);
      buildResponse(inputGroup);
      return inputGroup;
    }
    return getNewResponse();
  }



  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    ArrayList<SurveyBuilderFormFieldValue> fields = new ArrayList<>();

    for (int i = getLowerBoundId(); i <= getUpperBoundId(); i++) {
      SurveyBuilderFormFieldValue field = factory.formFieldValue().as();
      field.setId(Integer.toString(i));
      if (i == getLowerBoundId() && getLowerBoundLabel() != null) {
        field.setLabel(getLowerBoundLabel());
      }
      if (i == getUpperBoundId()
          && getUpperBoundLabel() != null) {
        field.setLabel(getUpperBoundLabel());
      }
      fields.add(field);
    }

    return fields;
  }

  private Button getValueButton(final Button valueButton, final boolean isStarting) {
    valueButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final Modal modal = new Modal();
        modal.setClosable(true);

        final ModalBody modalBody = new ModalBody();
        if (isStarting)
          modalBody.add(FormWidgets.formLabelFor("Change the starting value for this response ", "val"));
        else
          modalBody.add(FormWidgets.formLabelFor("Change the ending value for this response ", "val"));

        final TextBox tbox = new TextBox();
        tbox.setValue(valueButton.getText());
        tbox.setId("val");
        tbox.addValidator(new Validator<String>() {
          @Override
          public int getPriority() {
            return 0;
          }

          @Override
          public List<EditorError> validate(Editor<String> editor, String value) {
            List<EditorError> result = new ArrayList<>();
            String valueStr = value == null ? "" : value.trim();
            if (valueStr.isEmpty()) {
              result.add(new BasicEditorError(tbox, value, "Cannot be blank!"));
            } else if (!valueStr.matches("[0-9]*")) {
              result.add(new BasicEditorError(tbox, value, "Must be a valid number!"));
            } else if (isStarting
                && Integer.parseInt(valueStr) >= getUpperBoundId()) {
              result.add(new BasicEditorError(tbox, value, "Starting value must be less than the ending value"));
            } else if (!isStarting && Integer.parseInt(valueStr) <= getLowerBoundId()) {
              result.add(new BasicEditorError(tbox, value, "Ending value must be less thatn the starting value "));
            }
            return result;
          }
        });
        modalBody.add(tbox);
        modal.add(modalBody);
        modal.show();
        ModalFooter footer = new ModalFooter();
        Button changeButton = new Button("Save");
        changeButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            try {
              if (isStarting) {
                setLowerBoundId(Integer.parseInt(tbox.getValue()));
              } else {
                setUpperBoundId(Integer.parseInt(tbox.getValue()));
              }
              valueButton.setText(tbox.getValue());
              buildResponse(inputGroups.get(0));
              modal.hide();
            } catch (NumberFormatException ignored) {
            }
          }
        });
        Button exitButton = new Button("Exit");
        exitButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            modal.setVisible(false);
          }
        });
        footer.add(changeButton);
        modalBody.add(footer);
      }
    });
    if ("0".equals(valueButton.getText())) {
      valueButton.setType(ButtonType.INFO);
    }
    if (isStarting)
      valueButton.setTitle("Starting at number");
    else
      valueButton.setTitle("Ending at number");
    valueButton.setType(ButtonType.INFO);
    valueButton.setSize("50px", "50px");
    return valueButton;
  }

  private String getLowerBoundLabel() {
    if (formResponse != null && formResponse.getValues() != null && formResponse.getValues().size() > 0
        && formResponse.getValues().get(0) != null && formResponse.getValues().get(0).getLabel() != null) {
       return formResponse.getValues().get(0).getLabel();
    }
    return "0";
  }

  private int getLowerBoundId() {
    try {
      if (formResponse != null && formResponse.getValues() != null && formResponse.getValues().size() > 0
          && formResponse.getValues().get(0) != null && formResponse.getValues().get(0).getId() != null)
        return Integer.parseInt(formResponse.getValues().get(0).getId());
    } catch (Exception ignored) {}
    return 0;
  }

  private String getUpperBoundLabel() {
    if (formResponse != null && formResponse.getValues() != null && formResponse.getValues().size() > 0 &&
        formResponse.getValues().get(formResponse.getValues().size() - 1).getLabel() != null) {
      return formResponse.getValues().get(formResponse.getValues().size() - 1).getLabel();
    }
    return "10";
  }

  private int getUpperBoundId() {
    try {
      if (formResponse != null && formResponse.getValues() != null && formResponse.getValues().size() > 0 &&
        formResponse.getValues().get(formResponse.getValues().size() - 1).getId() != null)
        return Integer.parseInt(formResponse.getValues().get(formResponse.getValues().size() - 1).getId());
    } catch (Exception ignored) {}
    return 10;
  }

  private void setUpperBoundLabel(String text) {
    List<SurveyBuilderFormFieldValue> fields =
        formResponse.getValues();

    if (fields == null || fields.size() <= 0) {
      fields = getFields();
    }
      fields.get(fields.size() - 1).setLabel(text);

  }

  private void setLowerBoundLabel(String text) {
    List<SurveyBuilderFormFieldValue> fields =
        formResponse.getValues();

    if (fields == null || fields.size() <= 0) {
      fields = getFields();
    }
    fields.get(0).setLabel(text);
  }

  private void setUpperBoundId(int id) {
    checkFields(getLowerBoundId(), id);
  }

  private void setLowerBoundId(int id) {
    checkFields(id, getUpperBoundId());
  }

  private List<SurveyBuilderFormFieldValue> getFields() {
    // default range is 1 to 10
    return checkFields(1, 10);
  }

  private List<SurveyBuilderFormFieldValue> checkFields(int from, int to) {

    List<SurveyBuilderFormFieldValue> fields = new ArrayList<>();
    String lowerLabel = getLowerBoundLabel();
    String upperLabel = getUpperBoundLabel();


      for (int i=from; i<=to; i++) {
        SurveyBuilderFormFieldValue field = factory.formFieldValue().as();
        field.setId(Integer.toString(i));
        fields.add(field);
      }

    if (lowerLabel != null)
      fields.get(0).setLabel(lowerLabel);
    if (upperLabel != null)
      fields.get(fields.size() - 1).setLabel(upperLabel);

    formResponse.setValues(fields);
    return fields;
  }

  @Override
  public boolean hasLabel() {
    return false;
  }
}