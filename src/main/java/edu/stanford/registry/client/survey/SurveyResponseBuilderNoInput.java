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

import edu.stanford.registry.client.Sanitizer;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class SurveyResponseBuilderNoInput extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {
  private final String DEFAULT_LABEL = "More Information";

  public SurveyResponseBuilderNoInput(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean build) {
    super(surveyBuilderFormResponse, build);
  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    if (formResponse != null && formResponse.getValues()!= null) {
      for (SurveyBuilderFormFieldValue field : formResponse.getValues()) {
        Row fieldRow = new Row();
        Summernote note = new Summernote();
        note.setToolbar(getToolbar());
        note.setCode(field.getLabel() == null ? "" : field.getLabel());
        note.setAirMode(true);
        note.setEnabled(false);
        fieldRow.add(note);

        StyleHelper.addEnumStyleName(note, ColumnSize.MD_2);
        showResponses.add(fieldRow);
      }
    }
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    Div thisForm = new Div();
    if (response != null ) {
      formResponse.setValues(response.getValues());
      formResponse.setFieldType(response.getFieldType());
      formResponse.setAttributes(response.getAttributes());
      if (response.getRequired() != null) {
        formResponse.setRequired(response.getRequired());
      }
      formResponse.setOrder(response.getOrder());
      formResponse.setLabel(response.getLabel());
      formResponse.setRef(response.getRef());
      CustomInputGroup inputGroup = getNewResponse();
      inputGroup.getInputNote().setPlaceholder(response.getFieldType().toString());
      for (SurveyBuilderFormFieldValue field : getFormFieldValues()) {
        inputGroup.getInputNote().setCode(field.getLabel());
      }
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
    final CustomInputGroup inputGroup = new CustomInputGroup();
    inputGroup.setWidth("100%");
    final Summernote content = new Summernote();
    content.setToolbar(getToolbar());
    final Row collapseRow = new Row();
    final Row contentRow = new Row();
    final Icon icon1 = new Icon(IconType.EDIT);
    icon1.setTitle("Edit content label");
    icon1.setId("ed");
    icon1.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (icon1.getId().equals("ed")) {
          icon1.setType(IconType.SAVE);
          icon1.setTitle("Save");
          icon1.setId("up");

          //inputGroup.getButton().setVisible(false);
          collapseRow.clear();
          collapseRow.add(FormWidgets.textBoxColumn(inputGroup.getTextBox(), formResponse.getLabel(), ColumnSize.MD_3));
          collapseRow.add(FormWidgets.iconColumn(icon1, "", ColumnSize.LG_1));
        } else {
          icon1.setType(IconType.EDIT);
          icon1.setTitle("Edit content label");
          icon1.setId("ed");
          inputGroup.getButton().getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(inputGroup.getTextBox().getText()));
          formResponse.setLabel(inputGroup.getTextBox().getText());
          collapseRow.clear();
          collapseRow.add(inputGroup.getButton());
          collapseRow.add(FormWidgets.iconColumn(icon1, "", ColumnSize.LG_1));
        }
      }
    });
    if (formResponse.getLabel() == null || formResponse.getLabel().isEmpty()) {
      formResponse.setLabel(DEFAULT_LABEL);
    }
    final Button collapsibleContentButton = new Button();
    collapsibleContentButton.setIcon(IconType.MINUS_CIRCLE);
    if (formResponse.getLabel() != null && noValue(inputGroup.getTextBox()) ) {
      collapsibleContentButton.clear();
      collapsibleContentButton.add(new HTML(formResponse.getLabel()));
    }
    collapsibleContentButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (collapsibleContentButton.getIcon() == IconType.MINUS_CIRCLE) {
          collapsibleContentButton.setIcon(IconType.PLUS_CIRCLE);
          contentRow.setVisible(false);

        } else {
          collapsibleContentButton.setIcon(IconType.MINUS_CIRCLE);
          contentRow.setVisible(true);
        }
      }
    });
    collapsibleContentButton.setPull(Pull.LEFT);
    collapseRow.add(collapsibleContentButton);
    collapseRow.add(FormWidgets.iconColumn(icon1, "", ColumnSize.LG_1));
    TextBox textBox = new TextBox();
    inputGroup.add(collapseRow);
    contentRow.add(FormWidgets.headingColumn(""));
    contentRow.add(content);

    inputGroup.add(contentRow);
    inputGroup.setInputNote(content);
    inputGroup.setButton(collapsibleContentButton);
    inputGroup.setTextBox(textBox);
    return inputGroup;
  }

  @Override
  public List<SurveyBuilderFormFieldValue> getFormFieldValues() {
    if (formResponse.getValues() == null || formResponse.getValues().size() < 1) {
      formResponse.setValues(new ArrayList<SurveyBuilderFormFieldValue>());
    }
    return formResponse.getValues();
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {

    Map<String, String> attributes = formResponse.getAttributes();
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    formResponse.setAttributes(attributes);
    CustomInputGroup inputGroup = (CustomInputGroup) inputGroups.get(0);
    List<SurveyBuilderFormFieldValue> fields = new ArrayList<>();
    SurveyBuilderFormFieldValue newFormFieldValue= factory.formFieldValue().as();
    newFormFieldValue.setId("0");
    newFormFieldValue.setLabel(inputGroup.getInputNote().getCode());
    fields.add(newFormFieldValue);
    formResponse.setValues(fields);
    if (formResponse.getLabel() == null || formResponse.getLabel().isEmpty()) {
      formResponse.setLabel(DEFAULT_LABEL);
    }
    return formResponse;
  }

  @Override
  public String getRequiredText() {
    return "Please enter a response";
  }

  @Override
  public InputGroup getRequiredGroup() { return new InputGroup(); }

  static private class CustomInputGroup extends  InputGroup {
    Summernote summernote;
    Button button;
    TextBox textBox;
    private void setInputNote( Summernote note) {
      this.summernote = note;
    }

    private Summernote getInputNote() {
      return summernote;
    }

    private void setButton(Button button) {
      this.button = button;
    }

    private Button getButton() {
      return button;
    }

    private void setTextBox(TextBox textBox) {
      this.textBox = textBox;
    }

    private TextBox getTextBox() {
      return textBox;
    }
  }

  @Override
  public boolean hasLabel() {
    return false;
  }

  @Override
  public boolean supportsRequired() {
    return false;
  }

  @Override
  void save(ClickEvent event) {
    super.setLabel(formResponse.getLabel());
    super.save(event);
  }

  private boolean noValue(TextBox textBox) {
    if (textBox == null)
      return true;
    if (textBox.getValue() == null)
      return true;
    return textBox.getValue().isEmpty();
  }

  private Toolbar getToolbar() {
    Toolbar newToolbar = new Toolbar();

    newToolbar.addGroup(ToolbarButton.STYLE, ToolbarButton.BOLD, ToolbarButton.ITALIC, ToolbarButton.UNDERLINE,
        ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.HR,
        ToolbarButton.UNDO, ToolbarButton.REDO,
        ToolbarButton.CODE_VIEW);
    return newToolbar;
  }
}
