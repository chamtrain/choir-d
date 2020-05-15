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
package com.sksamuel.jqm4gwt.form.elements;

import edu.stanford.survey.client.ui.SurveyBundle;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBox;
import com.sksamuel.jqm4gwt.JQMCommon;

/*
 * Created by tpacht on 8/21/17 to implement stricter handling of non-numeric input in numeric survey form fields.
 */
public class CustomJQMNumber extends JQMNumber   {

  private final int OUT_OF_RANGE = 1;
  private final int INVALID_INT = 2;
  private final String OUT_OF_RANGE_MESSAGE = "Must be a number from " + this.getAttribute("min") + " to " + this.getAttribute("max");
  private final String INVALID_INT_MESSAGE = "Only numbers are allowed";
  public final String[] MESSAGES = {null, OUT_OF_RANGE_MESSAGE, INVALID_INT_MESSAGE};
  public CustomJQMNumber() {
    this(null);
  }
  BlurHandler blurHandler;
  ArrayList<HandlerRegistration> blurHandlers = new ArrayList<HandlerRegistration>();
  String step = "";
  public  CustomJQMNumber(String text) {
    super(text);
    setPattern("[0-9]*");
    addHandlers();
  }
  public CustomJQMNumber (String text, int min, int max) {
    super(text);
    setPattern("[0-9]*");
    setMin(String.valueOf(min));
    setMax(String.valueOf(max));
    JQMCommon.setAttribute(input.getElement(), "step", "1");
    addHandlers();
  }
  public void setPattern(String value) {
    JQMCommon.setAttribute(input.getElement(), "pattern", value);
  }

  public void addHandlers() {
    KeyDownHandler keyDownHandler = new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        String styleNames[] = getStyleName(getStyleElement()).split(",");
        for (String style : styleNames) {
          if (SurveyBundle.INSTANCE.css().errorHighlight().toString().equals(style)) {
            TextBox textBox = (TextBox) event.getSource();
            textBox.setValue("", true);
            removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
          }
        }
      }
    };
    addKeyDownHandler(keyDownHandler);

    KeyUpHandler keyUpHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getSource() instanceof TextBox) {
          TextBox textBox = (TextBox) event.getSource();
          if (event.getNativeKeyCode() >= 65 && event.getNativeKeyCode() <= 90) {
            //GWT.log("keyup event source is letter " + event.getSource().getClass().getCanonicalName());
            addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
            textBox.setValue("", true);
            refresh(getId(), "Numbers only");
          } else {
            GWT.log("keyup event source is " + event.getNativeKeyCode());
            if (textBox.getValue().length() < 1) {
              addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
              textBox.setValue("", true);
              refresh(getId(), "Only numbers are allowed");
            } else {
              try {
                if (getStep() == null || getStep().equals("") || getStep().equals("1")) {
                  Integer.parseInt(textBox.getValue().trim());
                } else {
                  Float.parseFloat(textBox.getValue().trim());
                }
                textBox.setValue(textBox.getValue().trim());
                return;
              } catch (NumberFormatException nfe) {
                GWT.log("keyup numberformationexception on " + textBox.getValue().trim());
                //textBox.setValue("", true);
                addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
                textBox.setValue("", true);
                refresh(getId(), "Only positive numbers may be used");
                return;
              }
            }
          }

        }

      }
    };
    addKeyUpHandler(keyUpHandler);

    blurHandler = new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {

        if (event.getSource() instanceof TextBox) {
          TextBox textBox = (TextBox) event.getSource();
          try {
            int result;
            if (step == null || step.equals("") || step.equals("1")) {
              result = checkRange(Integer.parseInt(textBox.getValue().trim()));
            } else {
              Float flt = Float.parseFloat(textBox.getValue().trim());
              result = checkRange( flt );
            }
            if (result > 0) {
              addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
              refresh(getId(), MESSAGES[result]);
              return;
            }
          } catch (NumberFormatException nfe) {
            addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
            refresh(getId(), MESSAGES[INVALID_INT]);
            refresh(getId(), "Numbers only");
            return;
          }
        }
      }
    };
    blurHandlers.add(addBlurHandler(blurHandler));
  }

  @Override
  protected void setType(String type) {
    input.getElement().setAttribute("type", type);
  }

  public void setMin(String min) {
    JQMCommon.setAttribute(input.getElement(), "min", min);
  }

  public void setMax(String max) {
    JQMCommon.setAttribute(input.getElement(), "max", max);
  }

  public void setStep(String step) {
    if (step == null) {
      return;
    }
    this.step = step.trim();
    JQMCommon.setAttribute(input.getElement(), "step", step);
  }

  public String getStep() {
    return step;
  }

  public static native void refresh(String id, String message) /*-{
    if ($wnd.$ === undefined || $wnd.$ === null) return; // jQuery is not loaded
    var el = $wnd.document.getElementById(id);
    el.setAttribute("placeholder", "Enter a Valid Number" );
    var inputEl = el.getElementsByTagName("input")[0];
    inputEl.value = "";
    inputEl.setAttribute("placeholder", message);
  }-*/;


  public int checkRange(int value) {
    String stringMin = this.getElement().getAttribute("min");
    String stringMax = this.getElement().getAttribute("max");

    try {
      if (stringMin != null) {
        final int min = Integer.parseInt(stringMin);
        if (value < min) {
          return OUT_OF_RANGE;
        }
      }
      if (stringMax != null) {
        final int max = Integer.parseInt(stringMax);
        if (value > max) {
          return OUT_OF_RANGE;
        }
      }
    } catch (NumberFormatException nfe) {
    }
    return 0;
  }

  public int checkRange(float value) {
    String stringMin = this.getElement().getAttribute("min");
    String stringMax = this.getElement().getAttribute("max");
    String stringStep = this.getElement().getAttribute("step");
    try {
      if (stringMin != null) {
        final int min = Integer.parseInt(stringMin);
        if (value < min) {
          return OUT_OF_RANGE;
        }
      }
      if (stringMax != null) {
        final int max = Integer.parseInt(stringMax);
        if (value > max) {
          return OUT_OF_RANGE;
        }
      }
      if (stringStep != null && stringStep.contains(".")) {
        int decPlaces = stringStep.length() - (stringStep.indexOf(".") + 1);
        Float decimals = new Float(value);
        if (decimals.toString().contains(".") && decimals.toString().length() - (decimals.toString().indexOf(".") + 1) > decPlaces) {
          return OUT_OF_RANGE;
        }
      }
    } catch (NumberFormatException nfe) {
      GWT.log("NumberFormatException occurred parsing the min/max/step values");
    }
    return 0;
  }

  public TextBox getInput() {
    return input;
  }

  @Override
  protected void onLoad() {
    if (blurHandler != null && blurHandlers.size() == 0) addBlurHandler(blurHandler);
  }

  @Override
  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return input.addBlurHandler(handler);
  }
}
