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

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.sksamuel.jqm4gwt.HasMini;
import com.sksamuel.jqm4gwt.HasText;
import com.sksamuel.jqm4gwt.events.HasTapHandlers;
import com.sksamuel.jqm4gwt.events.JQMComponentEvents;
import com.sksamuel.jqm4gwt.events.JQMHandlerRegistration;
import com.sksamuel.jqm4gwt.events.JQMHandlerRegistration.WidgetHandlerCounter;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.JQMFieldset;

public class CustomJQMRadioset extends JQMRadioset
    implements HasText<JQMRadioset>, HasValue<String>,
    HasMini<JQMRadioset>, HasTapHandlers, HasSelectionHandlers<String>, HasClickHandlers,
    JQMFormWidget {

  private boolean valueChangeHandlerInitialized;
  /**
   * The panel that is used for the controlgroup container
   */

  private final RadioButton radio = new RadioButton("");
  private String radioValue = null;
  private JQMFieldset fieldset;
  private final MyFormLabel myFormlabel;

  /**
   * Create a new {@link JQMTextArea} with no label text
   */
  public CustomJQMRadioset() {
     setupFieldset();
     myFormlabel = new MyFormLabel("");
   }

  private void setupFieldset() {
    if (fieldset != null) remove(fieldset);
    // the fieldset is the inner container and is contained inside the flow
    fieldset = new JQMFieldset();
    fieldset.getElement().setId(Document.get().createUniqueId() + "MEMEME");
    add(fieldset);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

    if (!valueChangeHandlerInitialized) {
      valueChangeHandlerInitialized = true;

      this.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          SelectionEvent.fire(CustomJQMRadioset.this, getValue());
          ValueChangeEvent.fire(CustomJQMRadioset.this, getValue());
        }
      });

    }
    return addHandler(handler, ValueChangeEvent.getType());
  }

  /**
   * Adds a new radio button to this radioset using the given name and value.
   * Returns a JQMRadio instance which can be used to change the values of the radio button.
   *
   * This method is the same as calling addRadio(String, String, "").
   *
   * @param name
   *            the name of this radio option.
   ** @param value
   *            the value to associate with this radio option. This will be
   *            the value returned by methods that query the selected value.
   *            The value will also be used for the label.
   *
   * @return a JQMRadio instance to adjust the added radio button
   */
   public RadioButton setRadio(String name, String value) {
    return addRadio(name, value, "");
  }

  /**
   * Adds a new radio button to this radioset using the given name, value and text.
   * Returns a JQMRadio instance which can be used to change the value and
   * label of the radio button.
   *
   * @param name
   *            the name of this radio option.
   *
   * @param value
   *            the value to associate with this radio option. This will be
   *            the value returned by methods that query the selected value.
   *
   * @param text
   *            the label to show for this radio option.
   *
   * @return a JQMRadio instance to adjust the added radio button
   */
   public RadioButton addRadio(String name, String value, String text) {
    radio.setName(name);
    radioValue = value;
    addRadio(radio);
    return radio;
  }


  /**
   * UiBinder call method to add a radio button
   *
   * @param radio Add a radio choice
   */
  @UiChild(tagname = "radio")
  public void addRadio(RadioButton radio) {

    fieldset.add(radio);
    this.addSelectionHandler(new SelectionHandler<String>() {
      @Override
      public void onSelection(SelectionEvent<String> event) {

      }
    });
  }

  @Override
  public String getText() {
    return myFormlabel.getText();
  }

  @Override
  public void setText(String text) {
    myFormlabel.setText(text);
  }

  @Override
  public boolean isMini() {
    return "true".equals(getAttribute("data-mini"));
  }

  /**
   * If set to true then renders a smaller version of the standard-sized element.
   */
  @Override
  public void setMini(boolean mini) {
    setAttribute("data-mini", String.valueOf(mini));
  }

  /**
   * If set to true then renders a smaller version of the standard-sized element.
   */
  @Override
  public CustomJQMRadioset withMini(boolean mini) {
    setMini(mini);
    return this;
  }

  @Override
  public HandlerRegistration addTapHandler(TapHandler handler) {
    radio.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
      }
    });
    radio.addTouchStartHandler(new TouchStartHandler() {
      @Override
      public void onTouchStart(TouchStartEvent event) {
      }
    });
    radio.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
      }
    });

    // this is not a native browser event so we will have to manage it via JS
    return JQMHandlerRegistration.registerJQueryHandler(new WidgetHandlerCounter() {
      @Override
      public int getHandlerCountForWidget(Type<?> type) {
        return getHandlerCount(type);
      }

    }, this, handler, JQMComponentEvents.TAP_EVENT, TapEvent.getType());
  }

  @Override
  public CustomJQMRadioset withText(String text) {
    setText(text);
    return this;
  }

  public boolean isSelected() {
    return  isChecked();
  }

  public String getRadioValue () {
    return radioValue;
  }

  public String getRadioName () {
    return radio.getName();
  }

  public MyFormLabel getRadioLabel () {
    return myFormlabel;
  }

  public void setChecked(boolean val) {
    radio.setValue(val);
  }

  public boolean isChecked() {
    return radio.getValue();
  }

  public RadioButton getRadio() {
    return radio;
  }

  public JQMFieldset getFieldset() {
    return fieldset;
  }


  class MyFormLabel extends Label implements HasTapHandlers, HasClickHandlers {

    private boolean tapHandlerInitialized = false;
    private boolean clickHandlerInitialized = false;

    MyFormLabel(String name) {
      super();
      this.getElement().setId(name);
    }

    @Override
    public HandlerRegistration addTapHandler(TapHandler handler) {
      if (!tapHandlerInitialized) {
        tapHandlerInitialized = true;
        this.addTapHandler(new TapHandler() {
          @Override
          public void onTap(TapEvent event) {
            SelectionEvent.fire(CustomJQMRadioset.this, getValue());
            ValueChangeEvent.fire(CustomJQMRadioset.this, getValue());
          }
        });
        return addHandler(handler, TapEvent.getType());
      }
      return null;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
      if (!clickHandlerInitialized) {
        clickHandlerInitialized = true;
        this.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
          }
        });
        return addClickHandler(handler);
      }
      return null;
    }
  }
}
