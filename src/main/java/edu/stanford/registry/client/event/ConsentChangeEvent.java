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

public class ConsentChangeEvent extends GwtEvent<ConsentChangeHandler> {
  private final static Type<ConsentChangeHandler> TYPE = new Type<>();

  private final Patient patient;

  public Patient getPatient() {
    return patient;
  }

  @Override
  public Type<ConsentChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(ConsentChangeHandler handler) {
    handler.onChange(this);

  }

  public enum ConsentChangeType {
    consented, declined, completedConsentForm, completedDeclineForm
  }

  private final ConsentChangeType consentChangeType;

  public ConsentChangeEvent(Patient pat, ConsentChangeType consentChangeType) {
    this.patient = pat;
    this.consentChangeType = consentChangeType;
  }

  public static Type<ConsentChangeHandler> getType() {
    return TYPE;
  }

  public ConsentChangeType getConsentChangeType() {
    return consentChangeType;
  }

}
