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

import edu.stanford.registry.shared.Patient;

import com.google.gwt.event.shared.GwtEvent;

public class ValidPatientEvent extends GwtEvent<ValidPatientHandler> {
  private static final Type<ValidPatientHandler> TYPE = new Type<>();

  private final Patient patient;

  public ValidPatientEvent(Patient pat) {
    this.patient = pat;
  }

  public static Type<ValidPatientHandler> getType() {
    return TYPE;
  }

  public Patient getPatient() {
    return patient;
  }

  @Override
  protected void dispatch(ValidPatientHandler handler) {
    handler.onPassedValidation(this);
  }

  @Override
  public Type<ValidPatientHandler> getAssociatedType() {
    return TYPE;
  }
}
