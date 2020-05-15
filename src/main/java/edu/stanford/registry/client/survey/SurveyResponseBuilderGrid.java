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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupAddon;
import org.gwtbootstrap3.client.ui.InputGroupButton;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SurveyResponseBuilderGrid extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

  private int startingValue = 0;
  private MyInputGroup myInputGroup;
  private ArrayList<SurveyBuilderFormFieldValue> xValues = new ArrayList<>();
  private ArrayList<SurveyBuilderFormFieldValue> yValues = new ArrayList<>();
  public SurveyResponseBuilderGrid(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
    super(surveyBuilderFormResponse, forEdit);
    if (myInputGroup == null) {
      initializeMyInputGroup(formResponse);
    }
  }

  private void initializeMyInputGroup(SurveyBuilderFormResponse surveyBuilderFormResponse) {
    xValues = new ArrayList<>();
    yValues = new ArrayList<>();
    ArrayList<TextArea> headers = new ArrayList<>();
    if (surveyBuilderFormResponse != null && surveyBuilderFormResponse.getValues() != null) {

      // First split out the headers across the top from the responses down the left side
      for (SurveyBuilderFormFieldValue field : surveyBuilderFormResponse.getValues()) {
        if (field.getId() != null && field.getId().contains(":")) {
          String[] parts = field.getId().split(":");
          if (parts.length > 0) {
            if (parts[0].equalsIgnoreCase("x-axis")) {
              xValues.add(field);
            } else {
              yValues.add(field);
            }
          }
        }
      }

      // then build the response labels across the top
      for (SurveyBuilderFormFieldValue heading : xValues) {
        TextArea labelBox = new TextArea();
        labelBox.setText(heading.getLabel());

        String[] parts = heading.getId().split(":");
        if (parts.length > 1) {
          labelBox.setId(parts[1]);
        }

        if (heading.getId() != null) {
          try { // set it to the lowest value
            int id = Integer.parseInt(heading.getId());
            if (id < startingValue) {
              startingValue = id;
            }
            labelBox.setValue(String.valueOf(id));
          } catch (NumberFormatException ignored) {

          }
        }
        headers.add(labelBox);
      }
    }
    myInputGroup = new MyInputGroup(headers);
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    return formResponse;
  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    Row hRow = new Row();
    Label blank = new Label();
    hRow.add(addInColumn(blank, ColumnSize.LG_2));

    for (TextArea header : myInputGroup.headers) {
      header.setEnabled(false);
      hRow.add(addInColumn(header, ColumnSize.LG_2));
    }
    showResponses.add(hRow);

    for (SurveyBuilderFormFieldValue field : yValues) {

      if (field != null) {
        Row fieldRow = new Row();
        final TextBox radioText = new TextBox();
        radioText.setText(field.getLabel());
        fieldRow.add(addInColumn(radioText, ColumnSize.LG_2 ));
        for (int i=0; i< myInputGroup.headers.size(); i++) {
          final InputGroup viewRadioGroup = new InputGroup();
          final InputGroupAddon radioBoxIcon = new InputGroupAddon();
          radioBoxIcon.setIcon(IconType.CIRCLE_O);
          viewRadioGroup.add(radioBoxIcon);
          fieldRow.add(addInColumn(viewRadioGroup, ColumnSize.LG_2));
        }
        showResponses.add(fieldRow);
      }
    }
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {

    initializeMyInputGroup(response);
    startingValue = 9999999;
    Div responseDiv = new Div();

    for (TextArea header : myInputGroup.headers) {
      header.setEnabled(true);
    }
    // create the rows with labels down the left side followed by controls and the reference tag
    for (SurveyBuilderFormFieldValue field : yValues) {
      MyDataRow radioRow = getRadioChoice(field, field.getRef());
      myInputGroup.addRow(radioRow);
    }

    if (startingValue == 9999999) {
      startingValue = 0;
    }
    responseDiv.add(myInputGroup);
    return responseDiv;
  }

  @Override
  public InputGroup getNewResponse() {
    MyDataRow radioRow = getRadioChoice(null,   null);

    if (myInputGroup == null) {
      initializeMyInputGroup(formResponse);
    }

    myInputGroup.addRow(radioRow);
    return myInputGroup;
  }

  private MyDataRow getRadioChoice(SurveyBuilderFormFieldValue field,  String responseRef) {

    // Create a radio button row
    final TextArea radioText = new TextArea();
    if (field != null) {
      radioText.setText(field.getLabel());
    }

    radioText.setPlaceholder("Row Label");
    SurveyBuilderValidatorHelper.addValueRequiredValidator(radioText);

    final TextBox radioRef = new TextBox();
    radioRef.setPlaceholder("Reference");
    if (field != null) {
      if (field.getRef() != null && !field.getRef().isEmpty()) {
        radioRef.setText(field.getRef());
      } else if (responseRef != null && !responseRef.equals("null")) {
        radioRef.setText(responseRef + "_" + field.getId());
      }
    }
    SurveyBuilderValidatorHelper.addValueRequiredValidator(radioRef);
    radioRef.addValidator(new Validator<String>() {
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
          if (reference.getText().equals(value)) {
            result.add(new BasicEditorError(editor, value, "Reference is not unique!"));
          } else {
            result.addAll(checkReferenceHandlers(editor, value));
          }
        }
        return result;
      }
    });

    final InputGroup radioGroup = new InputGroup();
    Tooltip radioRefTip = new Tooltip(SurveyBuilder.ALPHA_NUM_TIP);
    radioRefTip.add(radioRef);
    InputGroupButton groupButton1 = new InputGroupButton();
    InputGroupButton groupButton2 = new InputGroupButton();
    Button upButton  = getUpButton(radioGroup);
    Button delButton = getDelButton(radioGroup);
    Button dwButton = getDwButton(radioGroup);
    if (myInputGroup != null && myInputGroup.rows.size()==1) {
      upButton.setEnabled(false);
      delButton.setEnabled(false);
    }
    groupButton1.add(upButton);
    groupButton1.add(dwButton);
    groupButton2.add(delButton);
    groupButton2.add(getAddButton(radioGroup));
    radioGroup.add(groupButton1);
    for (int i=0; i<5; i++) {
      final InputGroupAddon radioIcon = new InputGroupAddon();
      radioIcon.setIcon(IconType.CIRCLE_O);
      radioGroup.add(radioIcon);
    }
    radioGroup.add(groupButton2);
    final MyDataRow myDataRow =  new MyDataRow(radioText,  radioGroup, radioRef);

    dwButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        int last = myInputGroup.rows.size() - 1;
        boolean done = false;
        for (int i=0; i< last; i++) {
          MyDataRow row = myInputGroup.rows.get(i);
          if (!done && radioGroup.equals(row.getRadioGroup())) {
            if (i+ 1 < myInputGroup.rows.size()) {
              MyDataRow nextRow = myInputGroup.rows.get(i + 1);
              myInputGroup.rows.set(i, nextRow);
              myInputGroup.rows.set(i + 1, row);
              myInputGroup.refreshRows();
              done = true;
            }
          }

        }
      }
    });
    upButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean done = false;
        for (int i = 1; i < myInputGroup.rows.size() ; i++) {
          MyDataRow row = myInputGroup.rows.get(i);
          if (!done && radioGroup.equals(row.getRadioGroup())) {
            myInputGroup.rows.set(i, myInputGroup.rows.get(i - 1));
            myInputGroup.rows.set(i - 1, row);
            myInputGroup.refreshRows();
            done = true;
          }
        }
      }

    });

    delButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int index = -1;
        for (int i = 1; i < myInputGroup.rows.size() ; i++) {
          MyDataRow row = myInputGroup.rows.get(i);

          InputGroupButton groupButton = (InputGroupButton) row.getRadioGroup().getWidget(MyInputGroup.DELBUTTON);
          if (event.getSource().equals(groupButton.getWidget(0)) ){
            index = i;
          }
        }
        if (index >=0) {
          myInputGroup.rows.remove(index);
          myInputGroup.refreshRows();
        }
      }
    });
    return myDataRow;
  }

  @Override
  public InputGroup refreshResponse(int inx) {
    if (inx == 0 && inputGroups.size() > 0) {
        myInputGroup.refreshRows();
    }
    return inputGroups.get(0);
  }

  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    formResponse.getAttributes();
    ArrayList<SurveyBuilderFormFieldValue> formFieldValues = new ArrayList<>();
     for (TextArea header: myInputGroup.headers) {
       if (header.getText() != null && !header.getText().isEmpty()) {
         SurveyBuilderFormFieldValue formFieldValue = factory.formFieldValue().as();
         formFieldValue.setId("x-axis:" + header.getId());
         formFieldValue.setLabel(header.getText());
         formFieldValues.add(formFieldValue);
       }
     }
     for (MyDataRow dataRow: myInputGroup.rows) {
       SurveyBuilderFormFieldValue formFieldValue = factory.formFieldValue().as();
       formFieldValue.setId("y-axis:" + dataRow.getRefBox().getText());
       formFieldValue.setLabel(dataRow.getLabel().getText());
       formFieldValue.setRef(dataRow.getRefBox().getText());
       formFieldValues.add(formFieldValue);
     }
    return formFieldValues;
  }

  private Button getValueButton( ) {

    final Button valueButton = new Button();
    valueButton.setText("Change ");
    valueButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        final Modal modal = new Modal();
        modal.setClosable(true);
        final ModalBody modalBody = new ModalBody();
        if (myInputGroup != null) {
          if (myInputGroup.headers.size() > 0) {
            modalBody.add(FormWidgets.formLabelFor("Change the starting value for the response columns ", "val"));
            final TextBox tbox = new TextBox();
            String parts[] = myInputGroup.headers.get(0).getId().split(":");
            if (parts.length > 1)
              tbox.setValue(parts[1]);
            else
              tbox.setValue(String.valueOf(startingValue));
            tbox.setId("val");
            modalBody.add(tbox);
            final CheckBox reverseBox = new CheckBox();

            FormLabel noteLabel = new FormLabel();
            noteLabel.setText("Note: This will change the value of all subsequent columns");
            modalBody.add(noteLabel);
            reverseBox.setText("Reverse score?");
            modalBody.add(FormWidgets.checkBoxColumn(reverseBox, false));
            FormLabel revLabel = new FormLabel();
            revLabel.setText("Reverse scoring will subtract 1 for each subsequent item instead of adding 1 ");
            modalBody.add(revLabel);
            if (myInputGroup.headers.size() > 1) {
              if (Integer.parseInt(myInputGroup.getResponseValues().get(0).getText()) > Integer.parseInt(
                  myInputGroup.getResponseValues().get(1).getText()))
                reverseBox.setValue(true);
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
                  modal.setVisible(false);
                  int val = startingValue;
                  for (int i=0; i< myInputGroup.headers.size(); i++) {
                    myInputGroup.headers.get(i).setId(String.valueOf(val));
                    myInputGroup.getResponseValues().get(i).setText(String.valueOf(val));
                    if (reverseBox.getValue()) {
                      val--;
                    } else {
                      val++;
                    }
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
      }
    });
    valueButton.setType(ButtonType.INFO);
    valueButton.setSize(ButtonSize.EXTRA_SMALL);
    if ("0".equals(valueButton.getText())) {
      valueButton.setType(ButtonType.INFO);
    }
    valueButton.setTitle("Change the value assigned when the response is selected");

    return valueButton;
  }
  @Override
  public boolean hasLabel() {
    return false;
  }

  @Override
  public boolean supportsRequired() {
    return true;
  }

  private Column getRankingColumn(boolean isEnabled) {
    final InlineCheckBox isRanking = new InlineCheckBox("Ranking");
    isRanking.getElement().setAttribute("style", "font-size: large;");
    if (getFormAttributes().get("Ranking") != null) {
      isRanking.setValue("true".equals(getFormAttributes().get("Ranking")));
    }
    isRanking.setEnabled(isEnabled);
    isRanking.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {

        getFormAttributes().put("Ranking", isRanking.getValue().toString());
      }
    });
    Column col = new Column(ColumnSize.MD_3);
    col.add(isRanking);
    return col;
  }

  private Map<String, String> getFormAttributes() {

    Map<String, String> attributes = formResponse.getAttributes();
    if (attributes == null) {
      attributes = new HashMap<>();
      formResponse.setAttributes(attributes);
    }
    return attributes;
  }

  private Column addInColumn(Widget w, ColumnSize sz) {
    Column column = new Column(sz);
    column.add(w);
    column.getElement().setAttribute("Style", "padding: 0px; 2px;" );
    return column;
  }

  private class MyInputGroup extends InputGroup {

    private ArrayList<TextArea> headers;
    private ArrayList<Label> responseValues = new ArrayList<>();
    private ArrayList<MyDataRow> rows = new ArrayList<>();
    Button reOrderButton;
    private InputGroup rowsGroup = new InputGroup();
    final static int UPBUTTON = 0;
    final static int DWBUTTON = 1;
    final static int DELBUTTON = 6;
    final static int ADDBUTTON = 7;

    MyInputGroup(ArrayList<TextArea> headers) {

      this.headers = headers;
      int idStart = startingValue;

      boolean isReverse = false;
      if (this.headers != null) {
        if (this.headers.size() > 0) {
          try {
            idStart = Integer.parseInt(this.headers.get(headers.size() -1).getId());
          } catch (NumberFormatException nfe) {
            // ignore
          }
          if (this.headers.size() > 1 && this.headers.get(1).getValue() != null)
            if (this.headers.get(0).getId() != null &&
                Integer.parseInt(this.headers.get(0).getId()) > idStart)
              isReverse = true;
        }

        int id = idStart;
        while (headers.size() < 5) {
          TextArea emptyHeader = new TextArea();
          emptyHeader.setId(String.valueOf(id));
          emptyHeader.setPlaceholder("Response Label");
          headers.add(emptyHeader);
          if (isReverse)
            id--;
          else
            id++;
        }
        final Row hRow = new Row();
        final Row vRow = new Row();
        StyleHelper.addEnumStyleName(hRow, ColumnSize.LG_12);
        StyleHelper.addEnumStyleName(vRow, ColumnSize.LG_12);
        StyleHelper.addEnumStyleName(rowsGroup,ColumnSize.LG_12 );
        rowsGroup.setWidth("95%");
        Label blank = new Label();
        hRow.add(addInColumn(blank, ColumnSize.LG_2));
        reOrderButton = getValueButton();
        vRow.add(addInColumn(reOrderButton, ColumnSize.LG_2));


        for (int i=0 ; i < headers.size(); i++) {
          TextArea header = headers.get(i);
          final int index=i;
          header.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              hRow.getWidget(index).addStyleName(ValidationState.SUCCESS.getCssName());
            }
          });
          header.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
              TextArea textArea = (TextArea) event.getSource();
              if (textArea.getText().trim().length() == 0) {
                hRow.getWidget(index).removeStyleName(ValidationState.SUCCESS.getCssName());
              } else {
                hRow.getWidget(index).addStyleName(ValidationState.SUCCESS.getCssName());
              }
            }
          });
          hRow.add(addInColumn(header, ColumnSize.LG_2));
          Label valueLabel = new Label(header.getId());
          valueLabel.addStyleName(ButtonType.INFO.getCssName());
          responseValues.add(valueLabel);
          vRow.add(addInColumn(valueLabel, ColumnSize.LG_2));
          if (header.getText().trim().length() > 0) {
            vRow.getWidget(index).addStyleName(ValidationState.SUCCESS.getCssName());
          }
        }
        add (vRow);
        add (hRow);
        add (rowsGroup);
        add(getRankingColumn(true));

      }
    }

    public MyDataRow addRow(TextArea label, InputGroup radioGroup, TextBox refBox) {

      MyDataRow row = new MyDataRow(label, radioGroup , refBox );

      addRow(row);
      return row;
    }

    public void addRow(MyDataRow row) {
      rows.add(row);
      rowsGroup.add(row);
      if (rows.size() ==1) {
        enableButtons(row.getRadioGroup(), false );
      }
    }

    public ArrayList<Label> getResponseValues() {
      return responseValues;
    }

    void refreshRows() {
       rowsGroup.clear();

       for (MyDataRow row : rows) {
         InputGroup radioGroup = row.getRadioGroup();

         if (rowsGroup.getWidgetCount() == 0) { // first row
           enableButtons(radioGroup, false );
           if (rows.size() > 1) {
             enableButton(radioGroup,MyInputGroup.DWBUTTON , true);
           }
         } else {
           enableButtons(radioGroup, true);
         }

         // up, down, del
         rowsGroup.add(row);
         if (rowsGroup.getWidgetCount() == rows.size()) { // turn off down on last row
           enableButton(radioGroup, 1, false );
         }
       }
    }

    void enableButtons (InputGroup radioGroup, boolean enabled) {
      enableButton(radioGroup,MyInputGroup.UPBUTTON , enabled);
      enableButton(radioGroup,MyInputGroup.DWBUTTON, enabled);
      enableButton(radioGroup, MyInputGroup.DELBUTTON, enabled);
    }

    void enableButton (InputGroup radioGroup, int buttonNumber, boolean enabled) {
      InputGroupButton groupButton;
      if (buttonNumber  == UPBUTTON || buttonNumber == DWBUTTON) {
        groupButton = (InputGroupButton) radioGroup.getWidget(0);
      } else {
        groupButton = (InputGroupButton) radioGroup.getWidget(DELBUTTON);
        buttonNumber = buttonNumber - DELBUTTON;
      }
      Button button =  (Button)groupButton.getWidget(buttonNumber);
      button.setEnabled(enabled);
    }
  }

  private class MyDataRow extends Row {
    TextArea label = null;

    InputGroup radioControls = null;
    // InputGroupButton and InputGroupAddOn(s)
    TextBox refBox = null;

    MyDataRow(TextArea label, InputGroup radioGroup, TextBox refBox) {
      setLabel(label);
      setRadioGroup(radioGroup);
      setRefBox(refBox);
    }

    private void setLabel(TextArea label) {
      this.label = label;
      add(addInColumn(label, ColumnSize.LG_2 ));
    }

    public TextArea  getLabel() {
      return label;
    }

    private void setRadioGroup(InputGroup radioGroup) {
      this.radioControls = radioGroup;
      add(addInColumn(radioGroup, ColumnSize.LG_8 ));
    }

    public InputGroup getRadioGroup() {
      return radioControls;
    }

    private void setRefBox(TextBox refBox) {
      this.refBox = refBox;
      add(addInColumn(refBox, ColumnSize.LG_2));
    }

    TextBox getRefBox() {
      return refBox;
    }
  }

}