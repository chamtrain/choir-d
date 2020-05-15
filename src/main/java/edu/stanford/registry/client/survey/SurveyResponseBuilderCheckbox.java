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

public class SurveyResponseBuilderCheckbox extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

  private int startingValue = 0;

  public SurveyResponseBuilderCheckbox(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
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
        final InputGroup viewChkBoxGroup = new InputGroup();
        final InputGroupAddon chkBoxIcon = new InputGroupAddon();
        chkBoxIcon.setIcon(IconType.SQUARE_O);
        viewChkBoxGroup.add(chkBoxIcon);
        final InputGroupAddon chkBoxText = new InputGroupAddon();
        chkBoxText.setWidth("70%");
        chkBoxText.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(field.getLabel()));
        chkBoxText.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
        viewChkBoxGroup.add(chkBoxText);
        final TextBox chkBoxRef = new TextBox();
        chkBoxRef.setValue(field.getRef());
        chkBoxRef.setWidth("30%");
        chkBoxRef.setEnabled(false);
        viewChkBoxGroup.add(chkBoxRef);

        Column respColumn = new Column(ColumnSize.MD_9);
        respColumn.add(viewChkBoxGroup);
        fieldRow.add(respColumn);
        showResponses.add(fieldRow);
      }
    }
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    startingValue = 9999999;
    Div container = new Div();
    if (response != null && response.getValues() != null) {
      for (SurveyBuilderFormFieldValue field : response.getValues()) {
        InputGroup chkBoxChoice = getChkBoxChoice(field, response.getRef());
        container.add(chkBoxChoice);
        inputGroups.add(chkBoxChoice);
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
    return getChkBoxChoice(null, null);
  }

  private InputGroup getChkBoxChoice(final SurveyBuilderFormFieldValue field, String responseRef) {

    // Create a chkBox button choice
    final InputGroup chkBoxGroup = new InputGroup();
    StyleHelper.addEnumStyleName(chkBoxGroup, ColumnSize.LG_10);
    final InputGroupAddon chkBoxIcon = new InputGroupAddon();
    chkBoxIcon.setIcon(IconType.SQUARE_O);
    chkBoxGroup.add(chkBoxIcon);
    final TextBox chkBoxText = new TextBox();
    if (field != null) {
      chkBoxText.setText(field.getLabel());
    }
    chkBoxText.setPlaceholder("Label");
    chkBoxText.setWidth("70%");
    SurveyBuilderValidatorHelper.addValueRequiredValidator(chkBoxText);
    chkBoxGroup.add(chkBoxText);

    final TextBox chkBoxRef = new TextBox();
    chkBoxRef.setPlaceholder("reference");
    if (field != null) {
      if (field.getRef() != null && !field.getRef().isEmpty()) {
        chkBoxRef.setText(field.getRef());
      } else if (responseRef != null && !responseRef.equals("null")) {
        chkBoxRef.setText(responseRef + "_" + field.getId());
      }
    }
    SurveyBuilderValidatorHelper.addValueRequiredValidator(chkBoxRef);
    chkBoxRef.addValidator(new Validator<String>() {
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
        int cnt = 0;
        if (getFormFieldValues() != null && getFormFieldValues().size() > 0) {
          for (SurveyBuilderFormFieldValue fieldValue : getFormFieldValues()) {
            if (value.equals(fieldValue.getRef())) {
              cnt ++;
            }
          }
          if (cnt > 1) {
            result.add(new BasicEditorError(editor, value, "value " + value + " is not unique!"));
          }
        }
        if (result.size() == 0) {
          if (reference.getText().equals(value)) {  // rasq 
            result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
          } else {
            result.addAll(checkReferenceHandlers(editor, value));
          }
        }
        return result;
      }
    });

    chkBoxRef.setWidth("30%");
    Tooltip chkBoxRefTip = new Tooltip(SurveyBuilder.ALPHA_NUM_TIP);
    chkBoxRefTip.add(chkBoxRef);
    chkBoxGroup.add(chkBoxRefTip);
    InputGroupButton groupButton = new InputGroupButton();
    groupButton.add(getValueButton(chkBoxGroup, field));
    groupButton.add(getUpButton(chkBoxGroup));
    groupButton.add(getDwButton(chkBoxGroup));
    groupButton.add(getDelButton(chkBoxGroup));
    groupButton.add(getAddButton(chkBoxGroup));
    chkBoxGroup.add(groupButton);
    return chkBoxGroup;

  }

  @Override
  public InputGroup refreshResponse(int inx) {
    InputGroup inputGroup = inputGroups.get(inx);
    InputGroupButton groupButton = (InputGroupButton) inputGroup.getWidget(3);
    Button valueButton = (Button) groupButton.getWidget(0);
    valueButton.setType(ButtonType.DEFAULT);
    if (inx == 0) {
      valueButton.setType(ButtonType.INFO);
    }
    return inputGroup;
  }

  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    ArrayList<SurveyBuilderFormFieldValue> chkBoxes = new ArrayList<>();
    for (InputGroup inputGroup : inputGroups) {
      SurveyBuilderFormFieldValue formFieldValue = factory.formFieldValue().as();
      TextBox textBox = (TextBox) inputGroup.getWidget(1);
      TextBox refTextBox = (TextBox) inputGroup.getWidget(2);
      InputGroupButton groupButton = (InputGroupButton) inputGroup.getWidget(3);
      Button valueButton = (Button) groupButton.getWidget(0);

      formFieldValue.setId(valueButton.getText());
      formFieldValue.setLabel(textBox.getText());
      formFieldValue.setRef(refTextBox.getText());
      chkBoxes.add(formFieldValue);
    }
    return chkBoxes;
  }


  private Button getValueButton(final InputGroup group, SurveyBuilderFormFieldValue thisField) {
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
                  Button button = (Button) ((InputGroupButton) inputGroups.get(inx).getWidget(3)).getWidget(0);
                  button.setText(Integer.toString(startingValue + inx));
                }
              } catch (NumberFormatException ignored) {

              }
              modal.hide();
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
  @Override
  public boolean hasLabel() {
    return true;
  }
}