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

package edu.stanford.registry.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ValidIntegerEvent extends GwtEvent<InvalidIntegerHandler> {

  private static final Type<InvalidIntegerHandler> TYPE = new Type<>();

  private final String message;
  private final String value;

  public ValidIntegerEvent(String IntegerValue, String msg) {
    this.value = IntegerValue;
    this.message = msg;
  }

  public static Type<InvalidIntegerHandler> getType() {
    return TYPE;
  }

  /**
   * @returns The error message
   */
  public String getMessage() {
    return message;
  }

  public String getValue() {
    return value;
  }

  @Override
  protected void dispatch(InvalidIntegerHandler handler) {
    handler.onSuccessfulValidation(this);
  }

  @Override
  public Type<InvalidIntegerHandler> getAssociatedType() {
    return TYPE;
  }

}
