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

import edu.stanford.registry.client.utils.ClientUtils;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.TextBox;

public abstract class ValidTextBase extends TextBox {

  private Boolean isValidText = true;
  private String invalidStyleName = null;
  private String validStyleName = null;
  final HandlerManager handlerManager = new HandlerManager(this);
  private boolean required = false;
  private ClientUtils utils = null;

  public ValidTextBase(ClientUtils utils) {
    this.utils = utils;
    this.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        isValidText = true;
      }
    });

    this.addBlurHandler(new BlurHandler() {

      @Override
      public void onBlur(BlurEvent event) {
        if (isValidText) {
          checkValid();
        }
      }
    });
  }

  private void checkValid() {

    String value = getValue();
    if (value == null) {
      if (required) {
        setInvalid(getMissingEvent());
      }
      return;
    }

    if (value.length() < 1) {
      if (required) {
        setInvalid(getMissingEvent());
      }
      return;
    }
    if (isValidString()) {
      setValid();
    } else {
      setInvalid(getInvalidEvent());
    }
  }

  public abstract boolean isValidString();

  public abstract GwtEvent<?> getInvalidEvent();

  public abstract GwtEvent<?> getMissingEvent();

  public void setInvalidStyleName(String style) {
    invalidStyleName = style;
  }

  public String getInvalidStyleName() {
    return invalidStyleName;
  }

  public void setValidStyleName(String style) {
    validStyleName = style;
  }

  public boolean hasValidStyleName() {
    return (validStyleName != null);
  }

  public boolean hasInvalidStyleName() {
    return (invalidStyleName != null);
  }

  public String getValidStyleName() {
    return validStyleName;
  }

  public void setInvalid(GwtEvent<?> event) {
    setInvalid(event, true);
  }

  public void setInvalid(GwtEvent<?> event, boolean fireEvent) {
    isValidText = false;
    if (hasValidStyleName()) {
      removeStyleName(validStyleName);
    }
    if (hasInvalidStyleName()) {
      addStyleName(invalidStyleName);
    }
    if (fireEvent) {
      handlerManager.fireEvent(event);
    }
  }

  public void setValid() {
    isValidText = true;
    if (hasInvalidStyleName()) {
      removeStyleName(invalidStyleName);
    }
    if (hasValidStyleName()) {
      addStyleName(validStyleName);
    }
  }

  public void setValid(GwtEvent<?> event) {
    setValid(event, true);
  }

  public void setValid(GwtEvent<?> event, boolean fireEvent) {
    setValid();
    if (fireEvent) {
      handlerManager.fireEvent(event);
    }
  }

  public Boolean isValid() {
    return isValidText;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean req) {
    required = req;
  }

  public ClientUtils getUtils() {
    return utils;
  }
}
