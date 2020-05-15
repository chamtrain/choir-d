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
import edu.stanford.registry.client.api.SurveyBuilderFactory;
import edu.stanford.registry.client.api.SurveyBuilderForm;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition;
import edu.stanford.registry.client.api.SurveyBuilderFormCondition.Type;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormQuestion;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.survey.client.api.FieldType;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Legend;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconPosition;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.html.Italic;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class SurveyQuestionBuilder extends Form {
  private final SurveyBuilderFactory factory = GWT.create(SurveyBuilderFactory.class);
  private final SurveyResponseBuilderFactory builderFactory = new SurveyResponseBuilderFactory();
  private final ArrayList<ClickHandler> saveHandlers = new ArrayList<>();
  private final ArrayList<ClickHandler> discardHandlers = new ArrayList<>();
  private final ArrayList<Validator<String>> referenceValidateHandlers = new ArrayList<>();
  private final SurveyBuilderFormQuestion formQuestion;
  private final SurveyBuilderForm builderForm;
  private final Button save = new Button("Save question");
  private final Button discard = new Button("Return");
  private final Form responsesForm = new Form();
  private final Container grid = new Container();
  private final TextBox title1text = new TextBox();
  private final TextBox title2text = new TextBox();
  private final DropDownMenu menu = new DropDownMenu();

  private final Form conditionsForm = new Form();
  private final Container conditionGrid = new Container();
  private final DropDownMenu conditionMenu = new DropDownMenu();

  SurveyQuestionBuilder(SurveyBuilderForm surveyBuilderForm, SurveyBuilderFormQuestion surveyBuilderFormQuestion) {
    this.formQuestion = surveyBuilderFormQuestion;
    this.builderForm = surveyBuilderForm;
    buildPage();
  }

  public SurveyBuilderFormQuestion getFormQuestion() {
    return formQuestion;
  }

  void addSaveClickHandler(ClickHandler handler) {
    saveHandlers.add(handler);
  }

  void addDiscardClickHandlers(ClickHandler handler) {
    discardHandlers.add(handler);
  }

  void addValidateHandler(Validator<String> handler) {
    referenceValidateHandlers.add(handler);
  }

  private void buildPage() {
    this.clear();
    this.setType(FormType.HORIZONTAL);
    FieldSet fieldSet = new FieldSet();
    this.add(fieldSet);

    Legend newQuestionLabel = new Legend("Editing question ");
    fieldSet.add(newQuestionLabel);

    Container container = new Container();
    container.getElement().getStyle().setTextAlign(TextAlign.LEFT);
    final Row line1Row = new Row();
    title1text.setPlaceholder("First line of the question");
    final Column text1Column = FormWidgets.textBoxColumn(title1text, formQuestion.getTitle1());
    line1Row.add(text1Column);
    container.add(line1Row);
    final Row line2Row = new Row();
    title2text.setPlaceholder("Second line of the question");
    final Column text2Column = FormWidgets.textBoxColumn(title2text, formQuestion.getTitle2());
    line2Row.add(text2Column);
    container.add(line2Row);
    fieldSet.add(container);
    responsesForm.clear();
    responsesForm.add(getResponses());
    fieldSet.add(responsesForm);
    ButtonGroup response = new ButtonGroup();
    final Button anchorButton = new Button();
    anchorButton.setText("Add response");
    buildMenu();
    anchorButton.setDataToggle(Toggle.DROPDOWN);
    response.add(anchorButton);
    response.add(menu);
    conditionsForm.clear();
    conditionsForm.add(getConditions());
    fieldSet.add(conditionsForm);
    final Button conditionAnchorButton = new Button();
    conditionAnchorButton.setText("Add condition");
    buildConditionMenu();
    conditionAnchorButton.setDataToggle(Toggle.DROPDOWN);
    ButtonGroup conditionButtonGroup = new ButtonGroup();
    conditionButtonGroup.add(conditionAnchorButton);
    conditionButtonGroup.add(conditionMenu);
    save.setType(ButtonType.PRIMARY);
    save.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        save(event);
      }
    });

    discard.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        discard(event);
      }

    });
    StyleHelper.addEnumStyleName(response, ButtonSize.DEFAULT);
    final Row controlsRow = new Row();
    controlsRow.add(response);
    StyleHelper.addEnumStyleName(conditionButtonGroup, ButtonSize.DEFAULT);
    controlsRow.add(conditionButtonGroup);
    StyleHelper.addEnumStyleName(save, ButtonSize.DEFAULT);
    StyleHelper.addEnumStyleName(discard, ButtonSize.DEFAULT);
    controlsRow.add(save);
    controlsRow.add(discard);
    StyleHelper.addEnumStyleName(controlsRow, ColumnSize.MD_8);
    fieldSet.add(controlsRow);
  }

  private FormGroup getConditions() {
    final FormGroup conditionForm = new FormGroup();
    conditionGrid.clear();
    Legend condFormLegend = new Legend("Conditions");
    conditionForm.add(condFormLegend);
    conditionForm.add(conditionGrid);

    if (formQuestion.getConditions() == null || formQuestion.getConditions().size() == 0)
      return conditionForm;
    int inx=0;
    for (final SurveyBuilderFormCondition condition : formQuestion.getConditions()) {
      final Panel conditionPanel = new Panel();
      showReadOnlyCondition(conditionPanel, condition, inx);
      conditionPanel.setId(String.valueOf(conditionGrid.getWidgetCount()));
      conditionGrid.add(conditionPanel);
      inx++;
    }
    return conditionForm;
  }

  private FormGroup getResponses() {
    final FormGroup responseForm = new FormGroup();
    grid.clear();
    Legend responseFormLegend = new Legend("Responses");
    responseForm.add(responseFormLegend);
    responseForm.add(grid);

    if (formQuestion.getResponses() == null || formQuestion.getResponses().size() == 0)
      return responseForm;

    for (final SurveyBuilderFormResponse response : formQuestion.getResponses()) {
      final Panel respPanel = new Panel();
      showReadOnlyResponse(respPanel, response);
      respPanel.setId(String.valueOf(grid.getWidgetCount()));
      grid.add(respPanel);
    }
    return responseForm;
  }

  private void showReadOnlyResponse(final Panel respPanel, final SurveyBuilderFormResponse response) {
    if (response == null) {
      return;
    }
    String typeValue = "";
    String refValue = "";
    Icon reqIcon = new Icon(IconType.SQUARE_O);
    if (response.getRef() != null) {
      refValue = response.getRef();
    }
    if (response.getFieldType() != null) {
      typeValue = FormWidgets.getFieldTypeHeading(response.getFieldType());
    }
    if (response.getRequired() != null && response.getRequired()) {
      reqIcon = new Icon(IconType.CHECK_SQUARE_O);
    }

    Row respRow = new Row();
    respRow.add(FormWidgets.headingColumn("", ColumnSize.MD_1));
    respPanel.setId(String.valueOf(response.getOrder()));
    PanelHeader respPanelHdr = new PanelHeader();
    Heading respHeading = new Heading(HeadingSize.H4);
    final Anchor anchor = new Anchor();
    anchor.setIcon(IconType.CHEVRON_UP);
    anchor.setIconPosition(IconPosition.LEFT);
    anchor.setDataToggle(Toggle.COLLAPSE);
    anchor.setDataTarget("#"+response.getRef());
    anchor.setDataParent("#"+String.valueOf(response.getOrder()));
    anchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (anchor.getIcon() == IconType.CHEVRON_DOWN) {
          anchor.setIcon(IconType.CHEVRON_UP);
        } else {
          anchor.setIcon(IconType.CHEVRON_DOWN);
        }
      }
    });

    respHeading.add(anchor);
    respHeading.add(new Text(typeValue));
    respHeading.setAlignment(Alignment.LEFT);
    respPanelHdr.add(respHeading);
    respPanel.add(respPanelHdr);
    PanelCollapse respCollapse = new PanelCollapse();
    respCollapse.setId(response.getRef());
    PanelBody panelBody = new PanelBody();
    panelBody.add(respCollapse);
    respPanel.add(panelBody);

    SurveyResponseBuilder responseBuilder = builderFactory.getResponseBuilder(response, false);
    if (responseBuilder.hasLabel()) {
      if (response.getLabel() == null || response.getLabel().isEmpty()) {
        Paragraph p = new Paragraph();
        p.setEmphasis(Emphasis.MUTED);
        p.add(new Italic("no label"));
        respCollapse.add(p);
      } else {
        Column responseLabelColumn = FormWidgets.headingColumn(response.getLabel());
        ((Paragraph) responseLabelColumn.getWidget(0)).setHTML("<h4>" + Sanitizer.sanitizeHtml(response.getLabel()).asString() + "</h4>");
        respCollapse.add(responseLabelColumn);
      }
    }

    Container respRowsGrid = new Container();
    respRowsGrid.setPaddingLeft(0);
    respRowsGrid.setPaddingBottom(10);
    ArrayList<Row> responses = responseBuilder.showResponse();
    if (responses != null) {
      for (Row row : responses) {
        respRowsGrid.add(row);
      }
    }

    respRow.add(new Column(ColumnSize.MD_9, respRowsGrid));
    respCollapse.add(respRow);
    Row typeRow = new Row();
    Column iconColumn;
    if (responseBuilder.supportsRequired()) {
      iconColumn = FormWidgets.iconColumn(reqIcon, "Is required", ColumnSize.MD_2);
    } else {
      iconColumn = new Column(ColumnSize.MD_2);
    }
    typeRow.add(iconColumn);
    typeRow.add(FormWidgets.headingColumn("Column reference", ColumnSize.MD_2));
    typeRow.add(FormWidgets.valueColumn(refValue, ColumnSize.MD_2));
    Button editButton = new Button();
    editButton.setText("Edit response");
    editButton.addClickHandler(getFieldTypeClickHandler(respPanel, response));
    editButton.setType(ButtonType.PRIMARY);
    typeRow.add(FormWidgets.buttonColumn(editButton, ButtonType.PRIMARY, ColumnSize.MD_2));
    Button deleteButton = new Button();
    deleteButton.setText("Delete response");
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        formQuestion.getResponses().remove(response);
        grid.remove(respPanel);
      }
    });
    deleteButton.setType(ButtonType.DEFAULT);
    typeRow.add(FormWidgets.buttonColumn(deleteButton, ButtonType.DEFAULT));

    respPanel.add(typeRow);
    // Start with it open instead of collapsed
    respCollapse.setIn(!respCollapse.isIn());

  }
  private void showReadOnlyCondition(final Panel condPanel, int inx) {
    showReadOnlyCondition(condPanel, formQuestion.getConditions().get(inx), inx);
  }
  private void showReadOnlyCondition(final Panel condPanel, SurveyBuilderFormCondition condition, final int inx) {

    if (formQuestion == null || formQuestion.getConditions() == null || !(formQuestion.getConditions().size() > inx)) {
      return;
    }

    PanelHeader condPanelHdr = new PanelHeader();
    Heading condHeading = new Heading(HeadingSize.H4);

    final Anchor anchor = new Anchor();
    anchor.setIcon(IconType.CHEVRON_UP);
    anchor.setIconPosition(IconPosition.LEFT);
    anchor.setDataToggle(Toggle.COLLAPSE);
    anchor.setDataTarget("#cond"+inx);
    anchor.setDataParent("#condPanel"+String.valueOf(inx));
    anchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (anchor.getIcon() == IconType.CHEVRON_DOWN) {
          anchor.setIcon(IconType.CHEVRON_UP);
        } else {
          anchor.setIcon(IconType.CHEVRON_DOWN);
        }
      }
    });

    condHeading.add(anchor);
    condHeading.add(new Text(condition.getType().label()));
    condHeading.setAlignment(Alignment.LEFT);
    condPanelHdr.add(condHeading);
    condPanel.add(condPanelHdr);

    PanelCollapse condCollapse = new PanelCollapse();

    condCollapse.setId("cond" + inx);
    PanelBody panelBody = new PanelBody();
    condPanel.setId("condPanel" + inx);

    Row row = SurveyConditionBuilder.showCondition(condition );
        panelBody.add(row);

    condCollapse.add(panelBody);
    condPanel.add(condCollapse);


    Row controlRow = new Row();
    controlRow.add(FormWidgets.formLabelColumn(new FormLabel(), ColumnSize.MD_5));
    Button editButton = new Button();
    editButton.setText("Edit condition");
    editButton.addClickHandler(getConditionClickHandler(condPanel, condition.getType(), inx));
    editButton.setType(ButtonType.PRIMARY);
    controlRow.add(FormWidgets.buttonColumn(editButton, ButtonType.PRIMARY, ColumnSize.MD_1));
    Button deleteButton = new Button();
    deleteButton.setText("Delete condition");
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (formQuestion.getConditions() != null && formQuestion.getConditions().size() > inx) {
          formQuestion.getConditions().remove(inx);
          save(event);
        }
        conditionGrid.remove(condPanel);
      }
    });
    deleteButton.setType(ButtonType.DEFAULT);
    controlRow.add(FormWidgets.buttonColumn(deleteButton, ButtonType.DEFAULT));
    condPanel.add(controlRow);
    // Start with it open instead of collapsed
    condCollapse.setIn(!condCollapse.isIn());

  }
  private void discard(ClickEvent event) {
    if (!discardHandlers.isEmpty()) {
      for (ClickHandler clickHandler : discardHandlers) {
        clickHandler.onClick(event);
      }
      this.clear();
    }
  }

  private void save(ClickEvent event) {
    updateForm();
    if (!saveHandlers.isEmpty()) {
      for (ClickHandler clickHandler : saveHandlers) {
        clickHandler.onClick(event);
      }
    }
  }

  private void updateForm() {
    formQuestion.setTitle1(title1text.getText());
    formQuestion.setTitle2(title2text.getText());
    buildMenu();
  }

  private void buildMenu() {
    menu.clear();
    int numberResponses = formQuestion.getResponses() == null ? 0 : formQuestion.getResponses().size();


    if ( numberResponses > 0  && formQuestion.getResponses().get(0).getFieldType().toString().equals(FieldType.numericScale.toString())) {
      menu.add(new Button("Numeric scale answers can be the only response on an item"));
    } else if ( numberResponses > 0  && formQuestion.getResponses().get(0).getFieldType().toString().equals(FieldType.radioSetGrid.toString())) {
      menu.add(new Button("Grid response can be the only response on an item"));
    } else {
      menu.add(getFieldAnchorListItem("Set of radio buttons", FieldType.radios));
      menu.add(getFieldAnchorListItem("Set of check boxes", FieldType.checkboxes));
      menu.add(getFieldAnchorListItem("Drop down list", FieldType.dropdown));
      menu.add(getFieldAnchorListItem("Text or number input box", FieldType.text));
      menu.add(getFieldAnchorListItem("Set of text boxes", FieldType.textBoxSet));
      menu.add(getFieldAnchorListItem("Date picker", FieldType.datePicker));
      menu.add(getFieldAnchorListItem("View only collapsible content", FieldType.collapsibleContentField));
      menu.add(getFieldAnchorListItem("Horizontal numeric slider", FieldType.numericSlider));
    }
    if (numberResponses == 0) {
      menu.add(getFieldAnchorListItem("Horizontal numeric scale", FieldType.numericScale));
      menu.add(getFieldAnchorListItem("Radio Grid", FieldType.radioSetGrid));
    }
    menu.setVisible(true);
  }

  private void buildConditionMenu() {

    conditionMenu.clear();
    conditionMenu.add(getConditionAnchorListItem("Make this question dependant on a Patient Attribute", Type.patientAttribute));
    conditionMenu.add(getConditionAnchorListItem("Make this question dependant on a Previous Response", Type.response));

  }

  private AnchorListItem getConditionAnchorListItem(String title, Type conditionType ) {
    AnchorListItem listItem = new AnchorListItem(title);
    SurveyBuilderFormCondition condition;
    if (conditionType.value().equals(Type.patientAttribute.value())) {
       condition = factory.formCondition().as();
    } else {
       condition = factory.formCondition().as();
    }
    condition.setType(conditionType);
    listItem.addClickHandler(getConditionClickHandler(null,  conditionType, -1));
    return listItem;
  }

  private AnchorListItem getFieldAnchorListItem(String title, FieldType fieldType) {
    AnchorListItem listItem = new AnchorListItem(title);
    listItem.addClickHandler(
        getQuestionTypeHandler(fieldType)
    );
    return listItem;
  }

  private ClickHandler getQuestionTypeHandler(FieldType fieldType) {
    if (fieldType == null) {
      fieldType = FieldType.radios;
    }
    SurveyBuilderFormResponse formResponse = factory.formResponse().as();
    formResponse.setFieldType(fieldType);
    formResponse.setOrder(formQuestion.getResponses().size());
    return getFieldTypeClickHandler(formResponse);
  }

  private ClickHandler getFieldTypeClickHandler(final SurveyBuilderFormResponse formResponse) {

    return getFieldTypeClickHandler(null, formResponse);
  }

  private ClickHandler getFieldTypeClickHandler(final Panel inputPanel, final SurveyBuilderFormResponse formResponse){
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        save.setEnabled(false);
        discard.setEnabled(false);
        final Panel respPanel;
        if (inputPanel == null) {
          respPanel = new Panel();
          respPanel.setId(String.valueOf(grid.getWidgetCount()));
          grid.add(respPanel);
        } else {
          respPanel = inputPanel;
        }
        respPanel.clear();
        updateForm();
        final SurveyResponseBuilder rb = builderFactory.getResponseBuilder(formResponse, true);
        respPanel.add(rb);

        rb.addSaveClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            SurveyBuilderFormResponse editedResponse = rb.getFormResponse();
            boolean found = false;
            for (SurveyBuilderFormResponse response : formQuestion.getResponses()) {
              if (response == editedResponse) {
                found = true;
              }
            }
            if (!found) {
              formQuestion.getResponses().add(editedResponse);
            }
            updateForm();
            for (ClickHandler clickHandler : saveHandlers) {
              clickHandler.onClick(event);
            }
            respPanel.clear();
            showReadOnlyResponse(respPanel, formResponse);
            save.setEnabled(true);
            discard.setEnabled(true);
          }
        });
        rb.addDiscardHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            respPanel.clear();
            showReadOnlyResponse(respPanel, formResponse);
            save.setEnabled(true);
            discard.setEnabled(true);
          }
        });
        rb.addValidateHandler(new Validator<String>() {
          @Override
          public int getPriority() {
            return 0;
          }

          @Override
          public List<EditorError> validate(final Editor<String> editor, final String value) {
            List<EditorError> result = new ArrayList<>();
            // check that the reference isn't creating column names too long
            if (builderForm.getPrefix() != null && formResponse.getRef() != null &&
                (builderForm.getPrefix().length() + formResponse.getRef().length() > 30)) {
              result.add(new BasicEditorError(editor, value, "Reference will create a column name that exceeds the maximum size allowed"));
            }
            // make sure the reference is unique for this question
            for (SurveyBuilderFormResponse response : formQuestion.getResponses()) {
              if (!formResponse.equals(response)) {
                if (response.getRef() != null && response.getRef().equals(value)) {
                  result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
                } else {
                  for (SurveyBuilderFormFieldValue fieldValue : response.getValues()) {
                    if (fieldValue.getRef().equals(value)) {
                      result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
                    }
                  }
                }
              }
            }
            // then check the other questions
            if (result.size() == 0) {
              for (Validator<String> validator : referenceValidateHandlers) {
                result.addAll(validator.validate(editor, value));
              }
            }
            return result;
          }
        });
      }
    };
  }

  private ClickHandler getConditionClickHandler(final Panel inputPanel, final Type conditionType, final int inx){
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        save.setEnabled(false);
        discard.setEnabled(false);
        final Panel condPanel;
        if (inputPanel == null) {
          condPanel = new Panel();
          condPanel.setId(String.valueOf(conditionGrid.getWidgetCount()));
          conditionGrid.add(condPanel);
        } else {
          condPanel = inputPanel;
        }
        condPanel.clear();
        PanelHeader header = new PanelHeader();
        StyleHelper.addEnumStyleName(header, Alignment.LEFT);
        header.add( new Heading(HeadingSize.H2, "Editing"));
        condPanel.add(header);
        updateForm();
        final SurveyConditionBuilder surveyConditionBuilder;
        if (inx < 0) {
           SurveyBuilderFormCondition newCondition = factory.formCondition().as();
           newCondition.setType(conditionType);
           surveyConditionBuilder = new SurveyConditionBuilder(builderForm, newCondition, formQuestion.getOrder());
        } else {
          surveyConditionBuilder = new SurveyConditionBuilder(builderForm, formQuestion.getConditions().get(inx), formQuestion.getOrder());
        }

        PanelBody body = new PanelBody();
        body.add(surveyConditionBuilder);
        condPanel.add(body);
        surveyConditionBuilder.addSaveClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            SurveyBuilderFormCondition editedCondition = surveyConditionBuilder.getCondition();

            if (formQuestion.getConditions() != null && inx >=0 && inx < formQuestion.getConditions().size() ) {
              SurveyBuilderFormCondition condition = formQuestion.getConditions().get(inx);
              condition.setDataValue(editedCondition.getDataValue());
              condition.setMethod(editedCondition.getMethod());
              condition.setType(editedCondition.getType());
              condition.setAttributes(editedCondition.getAttributes());
            } else  {
              if (formQuestion.getConditions() == null) {
                formQuestion.setConditions(new ArrayList<SurveyBuilderFormCondition>());
              }
              formQuestion.getConditions().add(editedCondition);
            }

            updateForm();
            for (ClickHandler clickHandler : saveHandlers) {
              clickHandler.onClick(event);
            }
            condPanel.clear();
            if (inx < 0) {
              showReadOnlyCondition(condPanel, formQuestion.getConditions().size() - 1);
            } else {
              showReadOnlyCondition(condPanel, inx);
            }
            save.setEnabled(true);
            discard.setEnabled(true);
            conditionMenu.setVisible(true);

          }
        });
        surveyConditionBuilder.addDiscardHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            condPanel.clear();
            if (inx >=0 && inx < formQuestion.getConditions().size()) {
              showReadOnlyCondition(condPanel, inx);
            } else {
              conditionGrid.remove(condPanel);
            }
            save.setEnabled(true);
            discard.setEnabled(true);
            conditionMenu.setVisible(true);
          }
        });
        conditionMenu.setVisible(false);

      }
    };
  }
}

