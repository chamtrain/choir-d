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

import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.Sanitizer;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupAddon;
import org.gwtbootstrap3.client.ui.InputGroupButton;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SurveyResponseBuilderTextboxset extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

  private int startingValue = 0;

  public SurveyResponseBuilderTextboxset(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
    super(surveyBuilderFormResponse, forEdit);
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    return formResponse;
  }

  @Override
  public ArrayList<Row> showResponse() {

    ArrayList<Row> showResponses = new ArrayList<>();
    if (formResponse != null && formResponse.getValues() != null) {
      for (SurveyBuilderFormFieldValue field : formResponse.getValues()) {
        Row fieldRow = new Row();
        final TextBoxGroup viewTxtBoxGroup = new TextBoxGroup();
        final InputGroupAddon txtBoxIcon = new InputGroupAddon();
        txtBoxIcon.setIcon(IconType.PENCIL);
        viewTxtBoxGroup.add(txtBoxIcon);
        final InputGroupAddon txtBoxLabel = new InputGroupAddon();
        txtBoxLabel.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(field.getLabel()));
        txtBoxLabel.setWidth("70%");
        txtBoxLabel.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
        viewTxtBoxGroup.add(txtBoxLabel);
        final TextBox txtBoxRef = new TextBox();
        txtBoxRef.setValue(field.getRef());
        txtBoxRef.setWidth("30%");
        txtBoxRef.setEnabled(false);
        viewTxtBoxGroup.setReference(txtBoxRef);
        Column respColumn = new Column(ColumnSize.MD_9);
        respColumn.add(viewTxtBoxGroup);
        fieldRow.add(respColumn);
        showResponses.add(fieldRow);
      }
    }
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    startingValue = 9999999;
    //FormGroup container = new FormGroup();
    Div container = new Div();
    if (response != null && response.getValues() != null) {
      for (SurveyBuilderFormFieldValue field : response.getValues()) {
        TextBoxGroup txtBoxChoice = getTextBoxChoice(field, response.getRef());
        container.add(txtBoxChoice);
        inputGroups.add(txtBoxChoice);
        if (field.getId() != null) {
          try { // set it to the lowest value
            int id = Integer.parseInt(field.getId());
            if (id < startingValue) {
              startingValue = id;
            }
          } catch (NumberFormatException ignored) {

          }
        }
      }
    }
    if (startingValue == 9999999) {
      startingValue = 0;
    }
    return container;
  }

  @Override
  public InputGroup getNewResponse() {
    return getTextBoxChoice(null, null);
  }

  private TextBoxGroup getTextBoxChoice(final SurveyBuilderFormFieldValue field, String responseRef) {

    final TextBoxGroup txtBoxGroup = new TextBoxGroup();
    StyleHelper.addEnumStyleName(txtBoxGroup, ColumnSize.LG_10);

    final TextBox txtBoxLabel = new TextBox();
    if (field != null) {
      txtBoxLabel.setText(field.getLabel());
    }
    txtBoxLabel.setPlaceholder("Label");
    txtBoxLabel.setWidth("70%");
    SurveyBuilderValidatorHelper.addValueRequiredValidator(txtBoxLabel);
    txtBoxGroup.setLabel(txtBoxLabel);

    final TextBox txtBoxRef = new TextBox();
    txtBoxRef.setPlaceholder("reference");
    if (field != null) {
      if (field.getRef() != null && !field.getRef().isEmpty()) {
        txtBoxRef.setText(field.getRef());
      } else if (responseRef != null && !responseRef.equals("null")) {
        txtBoxRef.setText(responseRef + "_" + field.getId());
      }
    }
    SurveyBuilderValidatorHelper.addValueRequiredValidator(txtBoxRef);
    txtBoxRef.addValidator(new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(Editor<String> editor, String valueStr) {
        List<EditorError> result = new ArrayList<>();
        final String value = valueStr == null ? "" : valueStr.trim();
        if (!value.trim().matches("^[a-zA-Z0-9_]*$")) {
          result.add(new BasicEditorError(editor, value, SurveyBuilder.ALPHA_NUM_ERROR));
        }
        int cnt=0;
        if (getFormFieldValues() != null && getFormFieldValues().size() > 0) {
          for (SurveyBuilderFormFieldValue fieldValue : getFormFieldValues()) {
            if (value.equals(fieldValue.getRef())) {
              cnt++;
            }
          }
        }
        if (cnt > 1) {
          result.add(new BasicEditorError(editor, value, "Field reference is not unique!"));
        }
        if (result.size() == 0) {
          if (reference.getText().equals(value)) { // rasq
            result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
          } else {
            result.addAll(checkReferenceHandlers(editor, value));
          }
        }
        return result;
      }
    });
    txtBoxRef.setWidth("30%");
    txtBoxGroup.setReference(txtBoxRef);
    InputGroupButton groupButton = new InputGroupButton();
    groupButton.add(getValueButton(txtBoxGroup, field));
    groupButton.add(getUpButton(txtBoxGroup));
    groupButton.add(getDwButton(txtBoxGroup));
    groupButton.add(getDelButton(txtBoxGroup));
    groupButton.add(getAddButton(txtBoxGroup));
    txtBoxGroup.setGroupButton(groupButton);
    return txtBoxGroup;

  }

  @Override
  public InputGroup refreshResponse(int inx) {
    TextBoxGroup tbGroup = (TextBoxGroup) inputGroups.get(inx);
    InputGroupButton groupButton = tbGroup.getGroupButton();
    Button valueButton = (Button) groupButton.getWidget(0);
    valueButton.setType(ButtonType.DEFAULT);
    if (inx == 0) {
      valueButton.setType(ButtonType.INFO);
    }
    return tbGroup;
  }

  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    ArrayList<SurveyBuilderFormFieldValue> fieldValues = new ArrayList<>();
    for (InputGroup inputGroup : inputGroups) {
      TextBoxGroup textBoxGroup = (TextBoxGroup) inputGroup;
      SurveyBuilderFormFieldValue formFieldValue = factory.formFieldValue().as();
      InputGroupButton groupButton = textBoxGroup.getGroupButton();
      Button valueButton = (Button) groupButton.getWidget(0);
      formFieldValue.setId(valueButton.getText());
      formFieldValue.setLabel(textBoxGroup.getLabel().getText());
      formFieldValue.setRef(textBoxGroup.getReference().getText());
      fieldValues.add(formFieldValue);
    }
    return fieldValues;
  }


  private Button getValueButton(final TextBoxGroup group, SurveyBuilderFormFieldValue thisField) {
    final Button valueButton = new Button();
    if (thisField != null) {
      valueButton.setText(thisField.getId());
    } else {
      valueButton.setText(Integer.toString(startingValue + inputGroups.size()));
    }
    valueButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (inputGroups.size() > 0 && inputGroups.get(0).equals(group)) {

          final Modal modal = new Modal();
          modal.setClosable(true);
          //final ModalHeader header = new ModalHeader();

          final ModalBody modalBody = new ModalBody();
          modalBody.add(FormWidgets.formLabelFor("Change the starting value for this response ", "val"));

          final TextBox tbox = new TextBox();
          tbox.setValue(valueButton.getText());
          tbox.setId("val");
          modalBody.add(tbox);

          if (inputGroups.size() > 1) {
            FormLabel noteLabel = new FormLabel();
            noteLabel.setText("Note: this will change the value of all subsequent choices");
            modalBody.add(noteLabel);
          }
          modal.add(modalBody);
          modal.show();
          ModalFooter footer = new ModalFooter();
          Button changeButton = new Button("Save");
          changeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              try {
                startingValue = Integer.parseInt(tbox.getValue());
                valueButton.setText(tbox.getValue());
                modal.setVisible(false);
                for (int inx = 0; inx < inputGroups.size(); inx++) {
                  TextBoxGroup textBoxGroup = (TextBoxGroup) inputGroups.get(inx);
                  Button button = (Button) (textBoxGroup.getGroupButton()).getWidget(0);
                  button.setText(Integer.toString(startingValue + inx));
                }
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
      }
    });
    if ("0".equals(valueButton.getText())) {
      valueButton.setType(ButtonType.INFO);
    }
    valueButton.setTitle("This is the value for the response when this choice is selected");

    return valueButton;
  }

  static private class TextBoxGroup extends InputGroup {
    private TextBox label;
    private TextBox reference;
    private InputGroupButton groupButton;

    TextBox getLabel() {
      return label;
    }

    void setLabel(TextBox textBox) {
      label = textBox;
      add(textBox);
    }

    TextBox getReference() {
      return reference;
    }

    void setReference(TextBox textBox) {
      reference = textBox;
      Tooltip refTip = new Tooltip(SurveyBuilder.ALPHA_NUM_TIP);
      refTip.add(reference);
      add(reference);
    }

    InputGroupButton getGroupButton() {
      return groupButton;
    }

    void setGroupButton(InputGroupButton button) {
      groupButton = button;
      add(groupButton);
    }
  }
  @Override
  public boolean hasLabel() {
    return true;
  }
}