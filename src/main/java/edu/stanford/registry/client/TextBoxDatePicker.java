/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.client;

import java.util.Date;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DatePicker;

public class TextBoxDatePicker extends Composite implements HasChangeHandlers,
    HasClickHandlers, FormCustomDataIntf,
    Focusable {

  private final DatePicker picker = new DatePicker();
  private final TextBox textBox = new TextBox();
  private final Button closeButton = new Button(new Image(RegistryResources.INSTANCE.cancel()).toString());

  private boolean required;
  private DateTimeFormat dateTimeFormat;
  private Date dateValue = null;
  private Label errorLabel = new Label();
  private PopupPanel pickerPopUp = new PopupPanel(true);
  // private PopupPanel errorPopUp = new PopupPanel(true);
  protected ErrorDialogWidget errorPopUp;
  private FormData myExtraData = new FormData();
  final HandlerManager handlerManager = new HandlerManager(this);

  public TextBoxDatePicker(DateTimeFormat dtf) {
    this(dtf, true);
  }

  public TextBoxDatePicker(DateTimeFormat dtf, boolean valueRequired) {
    required = valueRequired;
    dateTimeFormat = dtf;
    errorLabel.setStylePrimaryName("serverResponseLabelError");
    initWidget(textBox);

    picker.addValueChangeHandler(new ValueChangeHandler<Date>() {
      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        Date date = event.getValue();
        dateValue = date;
        String dateString = dateTimeFormat.format(date);
        textBox.setText(dateString);
        pickerPopUp.hide();
        fireChangeEvent();
      }
    });

    ClickHandler dpClickHandler = new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        pickerPopUp.clear();
        pickerPopUp.add(picker);
        pickerPopUp.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
        pickerPopUp.show();

      }

    };

    textBox.addClickHandler(dpClickHandler);

    ChangeHandler tbChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        if (event.getSource() == textBox) {
          pickerPopUp.hide();

          if ((textBox.getText() != null) && !textBox.getText().trim().equals("")) {
            try {
              Date newDate = dateTimeFormat.parse(textBox.getText());
              picker.setValue(newDate);
              setValue(newDate);
              fireChangeEvent();
            } catch (Exception e) {
              pickerPopUp.clear();

              // closeButton.setText("textBox.getText() is not a valid date!");
              pickerPopUp.add(closeButton);
              pickerPopUp.show();
              setValue(dateValue);
            }
          } else {
            if (required) {
              pickerPopUp.clear();
              pickerPopUp.add(closeButton);
              pickerPopUp.show();
              setValue(dateValue);
            } else {
              picker.setValue(new Date());
              setValue(null);
              fireChangeEvent();
            }
          }

        }
      }
    };

    textBox.addChangeHandler(tbChangeHandler);

    // Add a handler to the error popup close button to
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (errorPopUp != null) {
          errorPopUp.hide();
        }
        pickerPopUp.hide();
      }
    });

  }

  public void setValue(Date date) {
    if (date == null) {
      dateValue = null;
      textBox.setText("");
    } else {
      dateValue = date;
      textBox.setText(dateTimeFormat.format(date));
    }
  }

  /**
   * allows for observing change and click events on the entire widget
   */
  @Override
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  public Date getValue() {
    return dateValue;
  }

  public void setEnabled(boolean isEnabled) {
    textBox.setEnabled(isEnabled);
  }

  @Override
  public FormData getCustomData() {
    return myExtraData;
  }

  @Override
  public int getTabIndex() {
    return textBox.getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    textBox.setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    textBox.setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    textBox.setTabIndex(index);
  }

  private void fireChangeEvent() {
    NativeEvent nativeEvent = Document.get().createChangeEvent();
    ChangeEvent.fireNativeEvent(nativeEvent, this);
  }


}
