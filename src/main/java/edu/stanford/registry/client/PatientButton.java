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

import edu.stanford.registry.shared.Patient;

import com.google.gwt.user.client.ui.Button;

public class PatientButton extends Button {

  private Patient pat;

  public PatientButton(Patient patIn) {
    super();
    pat = patIn;
  }

  public PatientButton(String str) {
    super(str);
  }

  public void setPatient(Patient patIn) {
    pat = patIn;
  }

  public String getPatientId() {
    return pat.getPatientId();
  }

  public String getPatientName() {
    return pat.getFirstName() + " " + pat.getLastName();
  }

  public Patient getPatient() {
    return pat;
  }

}
