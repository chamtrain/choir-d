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

public class InvalidPatientEvent extends GwtEvent<InvalidPatientHandler> {
  private static final Type<InvalidPatientHandler> TYPE = new Type<>();

  private final String message;
  private final boolean isFormatError;
  private final String formattedId;

  public InvalidPatientEvent(String msg, boolean formatError) {
    this.message = msg;
    this.isFormatError = formatError;
    this.formattedId = "";
  }

  public InvalidPatientEvent(String msg, String patientId) {
    this.message = msg;
    this.isFormatError = false;
    this.formattedId = patientId;
  }

  public static Type<InvalidPatientHandler> getType() {
    return TYPE;
  }

  /**
   * @return The error message
   */
  public String getMessage() {
    return message;
  }

  public boolean formatError() {
    return isFormatError;
  }

  public String getFormattedId() {
    return formattedId;
  }

  @Override
  protected void dispatch(InvalidPatientHandler handler) {
    handler.onFailedValidation(this);
  }

  @Override
  public Type<InvalidPatientHandler> getAssociatedType() {
    return TYPE;
  }
}
