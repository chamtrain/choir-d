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

import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.utils.ClientUtils;

import com.google.gwt.event.shared.GwtEvent;

public class ValidEmailAddress extends ValidTextBase {

  boolean required = false;

  public ValidEmailAddress(ClientUtils utils, boolean isRequired) {
    super(utils);

    required = isRequired;
  }

  @Override
  public GwtEvent<?> getMissingEvent() {
    return new InvalidEmailEvent(null, "Must enter a valid email address.");
  }

  @Override
  public GwtEvent<?> getInvalidEvent() {
    return new InvalidEmailEvent(getValue(), getValue() + " is not a valid email address.");
  }

  @Override
  public String getValue() {
    String originalValue = super.getValue();
    if (originalValue != null) {
      return originalValue.trim();
    }
    return originalValue;

  }

  @Override
  public boolean isValidString() {
    if (!required && (getValue() == null || getValue().length() < 1)) {
      return true;
    }
    return isValidEmailString(getValue());
  }

  public native boolean isValidEmailString(String email) /*-{
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    return emailPattern.test(email);
  }-*/;

  public void addInvalidEmailHandler(InvalidEmailHandler handler) {
    handlerManager.addHandler(InvalidEmailEvent.getType(), handler);
  }

  @Override
  public void setText(String txt) {
    if (txt != null) {
      txt = txt.trim();
    }
    super.setText(txt);
  }

}
