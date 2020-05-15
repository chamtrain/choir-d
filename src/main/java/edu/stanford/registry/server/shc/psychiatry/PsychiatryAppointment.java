/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.psychiatry;

import edu.stanford.registry.server.imports.data.Appointment2;
import edu.stanford.registry.shared.Patient;

import java.util.Date;

public class PsychiatryAppointment extends Appointment2 {
  public PsychiatryAppointment() {
    super();
  }

  @Override
  protected String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType) {
    // 04/03/2019: All assessment types are "Default"
    return PsychiatryCustomizer.DEFAULT_SURVEY_TYPE;
  }
}
