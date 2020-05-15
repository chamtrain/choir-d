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
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.InlineHelpBlock;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.ComplexWidget;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.HasValidators;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public abstract class SurveyResponseBuilder extends Form implements SurveyResponseBuilderIntf{

  private final ArrayList<ClickHandler> saveHandlers = new ArrayList<>();
  private final ArrayList<ClickHandler> discardHandlers = new ArrayList<>();
  private final ArrayList<Validator<String>> referenceValidateHandlers = new ArrayList<>();
  final ArrayList<InputGroup> inputGroups = new ArrayList<>();
  SurveyBuilderFactory factory = GWT.create(SurveyBuilderFactory.class);
  final TextBox reference = new TextBox() ;
  private final TextBox label = new TextBox();
  private final CheckBox required = new CheckBox();
  private final Button saveButton = new Button("Save response");
  private final Button exitButton = new Button("Discard changes");
  private final FormGroup responseGroup = new FormGroup();
  final SurveyBuilderFormResponse formResponse;
  private final Column indentColumn = new Column(ColumnSize.MD_1);
  private Column responseCol = new Column(ColumnSize.MD_9);;

  public SurveyResponseBuilder(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean build) {
    this.formResponse = surveyBuilderFormResponse;
    if (build)
      buildPage(surveyBuilderFormResponse);
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    return formResponse;
  }

  void addSaveClickHandler(ClickHandler handler) {
    saveHandlers.add(handler);
  }

  void addDiscardHandler(ClickHandler handler) {
    discardHandlers.add(handler);
  }

  void addValidateHandler(Validator<String> handler) { referenceValidateHandlers.add(handler);}

  @Override
  public abstract ArrayList<Row> showResponse();
  @Override
  public abstract ComplexWidget editResponse(SurveyBuilderFormResponse response);
  @Override
  public abstract InputGroup refreshResponse(int index);

  private void buildPage(final SurveyBuilderFormResponse response) {
    responseGroup.clear();
    responseGroup.getElement().setAttribute("style", "margin: 0px;");
    responseGroup.getElement().getStyle().setMargin(0, Unit.PX);

    this.setType(FormType.HORIZONTAL);

    Panel panel = new Panel();
    PanelHeader respPanelHdr = new PanelHeader();
    Heading respHeading = new Heading(HeadingSize.H4);
    respHeading.setAlignment(Alignment.LEFT);
    respHeading.setText("Editing " + FormWidgets.getFieldTypeHeading(response.getFieldType()));
    respPanelHdr.add(respHeading);
    panel.add(respPanelHdr);
    PanelBody panelBody = new PanelBody();
    panel.add(panelBody);

    if (response.getRef() != null) {
      reference.setText(response.getRef());
    }
    reference.addValidator(new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(final Editor<String> editor, final String value) {
        List<EditorError> result = new ArrayList<>();
        String valueStr = value == null ? "" : value.trim();
        if (valueStr.length() < 1) {
          result.add(new BasicEditorError(reference, value, "Column reference is required"));
        } else if (!valueStr.trim().matches("^[a-zA-Z0-9_]*$")) {
          result.add(new BasicEditorError(reference, value, SurveyBuilder.ALPHA_NUM_ERROR));
        } else {
          if (getFormFieldValues() != null && getFormFieldValues().size() > 0) {
            for (SurveyBuilderFormFieldValue fieldValue : getFormFieldValues()) {
              if ((!reference.getId().equals(fieldValue.getId())) && fieldValue.getRef().equals(value)) { // check doesn't conflict with a field
                result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
              }
            }

            if (result.size() == 0) {
              result.addAll(checkReferenceHandlers(editor, value));
            }
          }
        }
        return result;
      }
    });


    reference.setId("reference");
    reference.setPlaceholder("Short name to uniquely identify the response, becomes part of the the export table column name");
    final Row topRow = new Row();
    topRow.add(indentColumn);
    if (hasLabel()) {
      label.setId("label");
      label.setPlaceholder("Optional response label");
      label.setHeight("60px");
      topRow.add(FormWidgets.textBoxColumn(label,
          formResponse.getLabel() != null ? formResponse.getLabel() : "", ColumnSize.MD_2));
    } else {
      responseCol = new Column(ColumnSize.MD_11);
    }

    if (response.getValues() != null) {
     responseCol.add(editResponse(response));
   } else {
      final InputGroup newGroup = getNewResponse();
      responseCol.add(newGroup);
      inputGroups.add(newGroup);
    }
    topRow.add(responseCol);
    panelBody.add(topRow);
    if (response.getRequired() != null) {
      required.setValue(response.getRequired());
    }

    final Row midRow = new Row();
    midRow.getElement().getStyle().setPaddingTop(15, Unit.PX);
    final Column reqCol = new Column(ColumnSize.MD_2);
    if (supportsRequired()) {
      reqCol.add(required);
      Heading reqHeading = new Heading(HeadingSize.H4, "Is required");
      reqHeading.setAlignment(Alignment.LEFT);
      required.getElement().getStyle().setPaddingTop(10, Unit.PX);
      reqCol.add(reqHeading);
      required.setPull(Pull.LEFT);
    }
    midRow.add(reqCol);
    midRow.add(FormWidgets.headingColumn("Column reference", ColumnSize.MD_2));
    FormGroup refGroup = new FormGroup();
    refGroup.add(FormWidgets.textBoxColumn(reference, response.getRef(), ColumnSize.MD_2));
    InlineHelpBlock refHelp = getInlineHelp();
    refGroup.add(refHelp);
    reference.addBlurHandler(getAlphaNumBlurHandler(refGroup, reference, refHelp));
    midRow.add(refGroup);
    panelBody.add(midRow);
    final Row bottomRow = new Row();
    /* TODO: Turn on when custom alerts have been implemented in the survey engine (& add to save) **
    final TextBox errorMsg = new TextBox();
    errorMsg.setPlaceholder("Please provide an answer");
    errorMsg.setText(getRequiredText());
    errorMsg.setTitle("Text displayed on continue when no response was provided");
    final Column errTextColumn = FormWidgets.textBoxColumn(errorMsg, "", ColumnSize.MD_5);
    errorMsg.setVisible(false);
    if (supportsRequired() && formResponse.getRequired() != null && formResponse.getRequired()) {
      errorMsg.setVisible(true);
    }
    bottomRow.add(errTextColumn);

    required.addClickHandler(new
                                 ClickHandler() {
                                   @Override
                                   public void onClick(ClickEvent event) {
                                     if (required.getValue()) {
                                       errorMsg.setVisible(true);
                                     } else {
                                       errorMsg.setVisible(false);
                                     }
                                   }
                                 });
    */
    panelBody.add(bottomRow);
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (validate()) {
          save(event);
        } else {
          // Remove the group error so the icon isn't highlighted
          final Alert alert = new Alert();
          alert.setType(AlertType.DANGER);
          alert.setDismissable(true);
          alert.setText("Correct the errors in the highlighted field(s) above before saving changes");
          bottomRow.add(alert);
          for (final HasValidators<?> child : getChildrenWithValidators(responseGroup)) {
            if (!child.validate() && child instanceof TextBox) {
              final TextBox childTextBox = (TextBox) child;
              StyleHelper.addEnumStyleName(childTextBox, AlertType.DANGER);
              childTextBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                  StyleHelper.removeEnumStyleName(childTextBox, AlertType.DANGER);
                  bottomRow.remove(alert);
                }
              });
            }
          }
        }
        // remove from the group so only the missing field is highlighted
        responseGroup.setValidationState(ValidationState.NONE);

      }
    });
    ButtonGroup buttonGroup = new ButtonGroup();
    saveButton.setType(ButtonType.PRIMARY);
    buttonGroup.add(saveButton);

    exitButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        discard(event);
      }
    });

    exitButton.setType(ButtonType.DEFAULT);
    buttonGroup.add(exitButton);
    Column buttonCol = new Column(ColumnSize.MD_4);
    buttonCol.add(buttonGroup);
    midRow.add(buttonCol);
    panelBody.add(bottomRow);
    Row emptyRow = new Row();
    panel.add(emptyRow);
    responseGroup.add(panel);
    this.add(responseGroup);

  }
  List<EditorError> checkReferenceHandlers(Editor<String> editor, String value) {
    List<EditorError> result = new ArrayList<>();
    if (!referenceValidateHandlers.isEmpty()) {
      for (Validator<String> validator : referenceValidateHandlers) {
        result.addAll(validator.validate(editor, value));
      }
    }
    return result;
  }
  void save(ClickEvent event) {
    formResponse.setRef(reference.getValue());
    formResponse.setRequired(required.getValue());
    formResponse.setLabel(label.getValue());
    formResponse.setValues(getFormFieldValues());

    if (!saveHandlers.isEmpty()) {
      for(ClickHandler clickHandler : saveHandlers) {
        clickHandler.onClick(event);
      }
    }
  }

  void setLabel(String str) {
    label.setValue(str);
  }

  private void discard(ClickEvent event) {
    if (!discardHandlers.isEmpty()) {
      for (ClickHandler clickHandler : discardHandlers) {
        clickHandler.onClick(event);
      }
    }
  }

  Button getUpButton(final InputGroup group) {
    final Button upButton = new Button();
    upButton.setIcon(IconType.ARROW_UP);
    upButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int inx = 0;
        for (InputGroup ans : inputGroups) {
          if (group.equals(ans) && inx > 0) {
            InputGroup prevGroup = inputGroups.get(inx - 1);
            inputGroups.set(inx - 1, group);
            inputGroups.set(inx, prevGroup);
          }
          inx++;
        }
        refreshResponseGroup();
      }
    });
    return upButton;
  }

  Button getDwButton(final InputGroup group) {
    final Button dwButton = new Button();
    dwButton.setIcon(IconType.ARROW_DOWN);
    dwButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int last = inputGroups.size() - 1;
        int inx = 0;
        for (InputGroup ans : inputGroups) {
          if (group.equals(ans) && inx < last) {
            InputGroup nextGroup = inputGroups.get(inx + 1);
            inputGroups.set(inx + 1, group);
            inputGroups.set(inx, nextGroup);
            inx = last;
          }
          inx++;
        }
        refreshResponseGroup();

      }
    });
    return dwButton;
  }
  Button getDelButton(final InputGroup group) {
    Button delButton = new Button();
    delButton.setIcon(IconType.MINUS);
    delButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (inputGroups.size() == 1) {
          if (group.getWidget(1) instanceof TextBox) {
            TextBox textBox = (TextBox) group.getWidget(1);
            textBox.clear();
          }
        } else {
          int inx = 0;
          for (InputGroup ans : inputGroups) {
            if (group.equals(ans)) {
              inputGroups.remove(inx);
            }
            inx++;
          }
          refreshResponseGroup();
        }
      }
    });
    return delButton;
  }
  Button getAddButton(final InputGroup group) {
    Button addButton = new Button();
    addButton.setIcon(IconType.PLUS);
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        InputGroup inputGroup = getNewResponse();
        int inx = 0;
        int last = inputGroups.size() - 1;
        for (InputGroup ans : inputGroups) {
          if (group.equals(ans)) {
            if (inx == last) {
              inputGroups.add(inputGroup);
            } else {
              inputGroups.add(inx + 1, inputGroup);
            }
          }
          inx++;
        }
        refreshResponseGroup();
      }
    });
    return addButton;
  }
  @Override
  public abstract InputGroup getNewResponse();

  @SuppressWarnings("unused")
  private FormGroup getResponseLabel() {
    FormGroup labelFormGroup = new FormGroup();
    FlowPanel labelPanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    StyleHelper.addEnumStyleName(labelPanel, ColumnSize.MD_3);
    label.setId("label");
    label.setPlaceholder("Optional label");
    labelPanel.add(label);
    labelFormGroup.add(labelPanel);
    return labelFormGroup;
  }

  private void refreshResponseGroup() {
    responseCol.clear();
    for (int inx=0; inx < inputGroups.size(); inx++) {
      refreshResponse(inx);
      responseCol.add(inputGroups.get(inx));
    }
  }

  public InputGroup getRequiredGroup() {
    InputGroup requiredGroup = new InputGroup();
    requiredGroup.add(required);
    return requiredGroup;
  }

  public String getRequiredText() {
    return "Please select a response";
  }

  public boolean supportsRequired() {
    return true;
  }

  @Override
  public abstract List<SurveyBuilderFormFieldValue> getFormFieldValues();


  @Override
  public abstract boolean hasLabel();

  private InlineHelpBlock getInlineHelp() {
    final InlineHelpBlock helpBlock = new InlineHelpBlock();
    helpBlock.setIconType(IconType.EXCLAMATION_TRIANGLE);
    helpBlock.setPull(Pull.LEFT);
    return helpBlock;
  }

  private BlurHandler getAlphaNumBlurHandler(final FormGroup group, final TextBox value, final InlineHelpBlock help) {
    return new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        if (!value.getValue().trim().matches("^[a-zA-Z0-9_]*$")) {
          group.setValidationState(ValidationState.ERROR);
          help.setError(SurveyBuilder.ALPHA_NUM_ERROR);
        } else {
          help.clearError();
        }

      }
    };
  }
}
