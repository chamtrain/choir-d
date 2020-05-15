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

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
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
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SurveyResponseBuilderRadio extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

  private int startingValue = 0;

  public SurveyResponseBuilderRadio(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
    super(surveyBuilderFormResponse, forEdit);
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    return formResponse;
  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    if (formResponse.getValues() != null) {
      for (SurveyBuilderFormFieldValue field : formResponse.getValues()) {
        Row fieldRow = new Row();
        final InputGroup viewRadioGroup = new InputGroup();
        final InputGroupAddon radioIcon = new InputGroupAddon();
        radioIcon.setIcon(IconType.CIRCLE_O);
        viewRadioGroup.add(radioIcon);
        final InputGroupAddon radioText = new InputGroupAddon();
        radioText.setWidth("70%");
        radioText.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(field.getLabel()));
        radioText.addStyleName(RegistryResources.INSTANCE.css().leftLabel());
        viewRadioGroup.add(radioText);
        Column respColumn = new Column(ColumnSize.MD_8);
        respColumn.add(viewRadioGroup);
        fieldRow.add(respColumn);
        showResponses.add(fieldRow);
      }
    }
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    startingValue = 9999999;
    Div responseDiv = new Div();
    if (response != null && response.getValues() != null) {
      int inx = 0;
      for (SurveyBuilderFormFieldValue field : response.getValues()) {
        InputGroup radioChoice = getRadioChoice(field, inx);
        responseDiv.add(radioChoice);
        inputGroups.add(radioChoice);
        if (field.getId() != null) {
          try { // set it to the lowest value
            int id = Integer.parseInt(field.getId());
            if (id < startingValue) {
              startingValue = id;
            }
          } catch (NumberFormatException ignored) {

          }
        }
        inx ++;
      }
    }
    if (startingValue == 9999999) {
      startingValue = 0;
    }
    return responseDiv;
  }

  @Override
  public InputGroup getNewResponse() {
    return getRadioChoice(null, 0);
  }

  private InputGroup getRadioChoice(SurveyBuilderFormFieldValue field, int inx) {

    // Create a radio button choice
    final InputGroup radioGroup = new InputGroup();
    StyleHelper.addEnumStyleName(radioGroup, ColumnSize.LG_11);
    final InputGroupAddon radioIcon = new InputGroupAddon();
    radioIcon.setIcon(IconType.CIRCLE_O);
    radioGroup.add(radioIcon);
    final TextBox radioText = new TextBox();
    if (field != null) {
      radioText.setText(field.getLabel());
    }

    StyleHelper.addEnumStyleName(radioText, ColumnSize.LG_7);
    radioGroup.add(radioText);

    InputGroupButton groupButton = new InputGroupButton();
    groupButton.add(getValueButton(radioGroup, field, inx));
    groupButton.add(getUpButton(radioGroup));
    groupButton.add(getDwButton(radioGroup));
    groupButton.add(getDelButton(radioGroup));
    groupButton.add(getAddButton(radioGroup));
    radioGroup.add(groupButton);
    return radioGroup;

  }

  @Override
  public InputGroup refreshResponse(int inx) {
    InputGroup inputGroup = inputGroups.get(inx);
    InputGroupButton groupButton = (InputGroupButton) inputGroup.getWidget(2);
    Button valueButton = (Button) groupButton.getWidget(0);
    valueButton.setType(ButtonType.DEFAULT);
    if (inx == 0) {
      valueButton.setType(ButtonType.INFO);
    }
    return inputGroup;
  }


  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    ArrayList<SurveyBuilderFormFieldValue> radios = new ArrayList<>();

    for (InputGroup inputGroup : inputGroups) {
      SurveyBuilderFormFieldValue formFieldValue = factory.formFieldValue().as();
      TextBox textBox = (TextBox) inputGroup.getWidget(1);
      InputGroupButton groupButton = (InputGroupButton) inputGroup.getWidget(2);
      Button valueButton = (Button) groupButton.getWidget(0);
      formFieldValue.setId(valueButton.getText());
      formFieldValue.setLabel(textBox.getText());
      radios.add(formFieldValue);
    }
    return radios;
  }


  private Button getValueButton(final InputGroup group, SurveyBuilderFormFieldValue thisField, int inx) {
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
          final CheckBox reverseBox = new CheckBox();

          if (inputGroups.size() > 1) {
            FormLabel noteLabel = new FormLabel();
            noteLabel.setText("Note: This will change the value of all subsequent choices");
            modalBody.add(noteLabel);
            reverseBox.setText("Reverse score?");
            modalBody.add(FormWidgets.checkBoxColumn(reverseBox,  false));
            FormLabel revLabel = new FormLabel();
            revLabel.setText("Reverse scoring will subtract 1 for each subsequent item instead of adding 1 ");
            modalBody.add(revLabel);
            Button button0 = (Button) ((InputGroupButton) inputGroups.get(0).getWidget(2)).getWidget(0);
            Button button1 = (Button) ((InputGroupButton) inputGroups.get(1).getWidget(2)).getWidget(0);
            if (Integer.parseInt(button0.getText()) > Integer.parseInt(button1.getText())) {
              reverseBox.setValue(true);
            }
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
                modal.hide();
                for (int inx = 0; inx < inputGroups.size(); inx++) {
                  Button button = (Button) ((InputGroupButton) inputGroups.get(inx).getWidget(2)).getWidget(0);
                  if (reverseBox.getValue()) {
                    button.setText(Integer.toString(startingValue - inx));
                  } else {
                    button.setText(Integer.toString(startingValue + inx));
                  }
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

    if (inx == 0) {
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