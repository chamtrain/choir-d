/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
package com.sksamuel.jqm4gwt.form.elements;

import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.ui.Sanitizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.sksamuel.jqm4gwt.HasMini;
import com.sksamuel.jqm4gwt.HasText;
import com.sksamuel.jqm4gwt.JQMCommon;
import com.sksamuel.jqm4gwt.JQMWidget;
import com.sksamuel.jqm4gwt.form.JQMFieldset;
import com.sksamuel.jqm4gwt.html.Legend;
import com.sksamuel.jqm4gwt.layout.JQMTable;


public class CustomJQMRadiosetTable extends JQMTable
    implements HasText<CustomJQMRadiosetTable>, HasValue<String>,
      HasMini<CustomJQMRadiosetTable>,
    JQMFormWidget, HasClickHandlers {

  private boolean valueChangeHandlerInitialized;
  private final LinkedHashMap<String, String> rowLabels = new LinkedHashMap<>();
  private final HashMap<String, JQMTable> rowChoices = new HashMap<>();

  /**
   * The panel that is used for the controlgroup container
   */
  private Legend legend;
  /**
   * The input's that are used for the text boxes
   */
  private final ArrayList<MyRadioButton> radioButtons = new ArrayList<>();
  /**
   * The size of the grid containing the textboxes
   */
  private String labelText=null;
  private final int choices;
  private final int questions;
  private final boolean isRanking;

  /**
   * Creates a new {@link CustomJQMRadiosetTable} with the given values
   *
   */
  public CustomJQMRadiosetTable(FormField field) {
    isRanking = (field.getAttributes() != null && "true".equalsIgnoreCase(field.getAttributes().get("Ranking")));
    ArrayList<FormFieldValue> xValues = new ArrayList<>();
    ArrayList<FormFieldValue> yValues = new ArrayList<>();
    for (final FormFieldValue value : field.getValues()) {
      if (value.getId() != null && value.getId().contains(":")) {
        String[] parts = value.getId().split(":");
        if (parts.length > 1) {
          if (parts[0].equalsIgnoreCase("x-axis")) {
            xValues.add(value);
          } else {
            yValues.add(value);
          }
        }
      }
    }
    GWT.log(" ranking is " + isRanking);
    buildDisplay(xValues, yValues ,isRanking );
    choices = xValues.size();
    questions = yValues.size();
  }

   private void buildDisplay(ArrayList<FormFieldValue> xValues,
         ArrayList<FormFieldValue> yValues, boolean isRanking) {

     final int numberOfChoices = xValues.size(); //
     setupTable();
     // Create the column headings from the X-AXIS labels
     JQMTable headers = new JQMTable(numberOfChoices);
     for (final FormFieldValue xValue : xValues) {
       Legend legend = new Legend();
       legend.getElement().setAttribute("style", "text-align: left; padding-left: 12px;" );
       legend.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(xValue.getLabel()));
       headers.add(legend);
     }

     // Create the radio choices
     for (final FormFieldValue yValue : yValues) {
       // Create the set of radio button choices for the row
       final JQMTable choices = new JQMTable(numberOfChoices);
       String radioName = yValue.getId().split(":")[1];
       rowLabels.put(radioName, yValue.getLabel());
       for (final FormFieldValue   xValue : xValues) { // one radio button per choice
         String id = xValue.getId().split(":")[1];
         final MyRadioButton radioButton = new MyRadioButton(radioName, id);
         choices.add(radioButton);
         radioButtons.add(radioButton);
       }
       rowChoices.put(radioName, choices);
     }

     // build the page
     clear();
     setupTable();
     // build the first row
     Legend blank = new Legend();
     blank.setText("-");  // Add a blank placeholder for the top left column
     blank.getElement().setAttribute("Style", "color: #fff; " );
     add(blank);
     add(headers);

     // build the rows of questions and choices
     for (String key : rowLabels.keySet()) {
       JQMText text = new JQMText();
       text.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(rowLabels.get(key)));
       add(text);
       JQMTable choices = rowChoices.get(key);

       for ( int c=0; c< choices.getColumns(); c++) {
         MyRadioButton mybutton = (MyRadioButton) choices.getCellWidget(c);
         if (isRanking) {
           mybutton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
               handleClickButtonEvent(event);
             }
           });
         }
       }
       add(rowChoices.get(key));
     }
   }

   private void handleClickButtonEvent(ClickEvent event) {
     MyRadioButton radioButton = (MyRadioButton) event.getSource();

     for (String key : rowLabels.keySet()) {
       JQMTable choices = rowChoices.get(key);
       for (int c = 0; c < choices.getColumns(); c++) {
         MyRadioButton button = ((MyRadioButton) choices.getCellWidget(c));

         if (!button.getButtonName().equals(radioButton.getButtonName())) {
           if (button.getButtonId().equals(radioButton.getButtonId())) {

             MyRadioButton newButton = new MyRadioButton(button.getButtonName(), button.getButtonId() );
             if (button.getValue()) {
               button.setValue(false);
               choices.replaceCellWidget(c, newButton);
             }
             newButton.addClickHandler(new ClickHandler() {
               @Override
               public void onClick(ClickEvent event) {
                 handleClickButtonEvent(event);
               }
             });
           }
         }
       }

     }
     refreshAll();
   }

   public int getChoices() {
    return choices;
   }

   public int getQuestions() {
    return questions;
   }

   public boolean isRanking() {
    return isRanking;
   }

  private void setupTable() {
     this.setColumns(2);
    legend = new Legend();
    if (labelText != null) {
      legend.setText(labelText);
    }
  }

  private final ArrayList<HandlerRegistration> blurHandlers = new ArrayList<>();

  private void clearBlurHandlers() {
    for (HandlerRegistration blurHandler : blurHandlers) blurHandler.removeHandler();
    blurHandlers.clear();
  }

  @Override
  protected void onUnload() {
    clearBlurHandlers();
  }

  /**
   * no-op implementation required for {@link JQMFormWidget}
   */
  @Override
  public HandlerRegistration addBlurHandler(final BlurHandler handler) {
    clearBlurHandlers();
    return null;
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  @Override
  public Label addErrorLabel() {
    return null;
  }


  public void add(String text) {
    Label label = new Label();
    label.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml(text));
    this.add(label, "ui-label");
  }

  public void clear() {
    radioButtons.clear();
  }

  @Override
  public void setTheme(String themeName) {
    for (MyRadioButton radio : radioButtons) JQMCommon.applyTheme(radio, themeName);
  }

  @Override
  public JQMWidget withTheme(String themeName) {
    setTheme(themeName);
    return this;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    // Initialization code
    if (!valueChangeHandlerInitialized) {
      valueChangeHandlerInitialized = true;
    }
    return addHandler(handler, ValueChangeEvent.getType());
  }

  /**
   * Returns the text used for the main label
   */
  @Override
  public String getText() {
    return legend.getText();
  }

  @Override
  public void setText(String text) {
    labelText = text;

  }
  public void setHTML(SafeHtml html) {
    legend.getElement().setInnerSafeHtml(html);
  }

  /**
   *
   * Note: this method will return null until after the jquery init phase has
   * completed. That means if you call getValue() on an element in the intial
   * construction of a page then it will return null.
   *
   * @return the value of the currently selected radio button or null if no
   *         button is currently selected.
   *
   *
   */
  @Override
  public String getValue() {
    StringBuilder response = new StringBuilder();
    for (MyRadioButton radio : radioButtons) {
      response.append(radio.getButtonId());
    }
    return response.toString();
  }

  public String[] getValues() {
    ArrayList<String> ids = new ArrayList<>();
    for (String key : rowLabels.keySet()) {
      JQMTable choices = rowChoices.get(key);
      for (int c = 0; c < choices.getColumns(); c++) {
        MyRadioButton button = ((MyRadioButton) choices.getCellWidget(c));
        if (button.getValue()) {
          ids.add(key + ":" + button.getButtonId());
        }
      }
    }
    return ids.toArray(new String[0]);
  }

  @Override
  public void setValue(String value) {

  }

  @Override
  public void setValue(String value, boolean fireEvents) {

  }

  @Override
  public Widget add(Widget widget) {
    if (widget instanceof JQMRadio) {
      JQMRadio radio = (JQMRadio) widget;
      JQMFieldset fieldset = new JQMFieldset();
      fieldset.getElement().setId(Document.get().createUniqueId());
      fieldset.add((radio).getInput());
      add(fieldset);
      radio.setName(radio.getValue());
      return fieldset;
    }
    add(widget, "ui-label");
    return widget;
  }




  protected static native void refreshAll() /*-{
    $wnd.$("input[type='text']").each(function() {
      var w = $wnd.$(this);
      if (w.data('mobile-text') !== undefined) {
        w.text('refresh');
      }
    });
  }-*/;

  /**
   * Returns the number of radio options set on this radioset
   *
   * @return the integer number of options
   */
  public int size() {
    return radioButtons.size();
  }

  @Override
  public CustomJQMRadiosetTable withText(String text) {
    setText(text);
    return this;
  }

  @Override
  public boolean isMini() {
    return "true".equals(this.getElement().getAttribute("data-mini"));
  }

  /**
   * If set to true then renders a smaller version of the standard-sized element.
   */
  @Override
  public void setMini(boolean mini) {
    this.getElement().setAttribute("data-mini", String.valueOf(mini));
  }

  /**
   * If set to true then renders a smaller version of the standard-sized element.
   */
  @Override
  public CustomJQMRadiosetTable withMini(boolean mini) {
    setMini(mini);
    return this;

  }

  private class MyRadioButton extends RadioButton {
    private final String buttonName ;
    private final String buttonId ;

    MyRadioButton(String name, String id) {
      super(name);
      setId(id);
      buttonName = name;
      buttonId = id;
      this.getElement().setAttribute("Style","background: transparent; border-color: transparent;" );
      //labelElem.setAttribute("Style", "padding-top: 30px;" );


      NodeList<Element> els = this.getElement().getElementsByTagName(LabelElement.TAG);
      for (int e=0; e<els.getLength(); e++) {
        Element el = els.getItem(e);
        el.setAttribute("Style", "padding-top: 30px;" );
      }
    }
    String getButtonName() {
      return buttonName;
    }
    String getButtonId() {
      return buttonId;
    }

    public void setValue(Boolean value) {
      this.setValue(value, true );
    }
  }
}
