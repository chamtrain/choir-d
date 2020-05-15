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
package edu.stanford.registry.server.shc.pain;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.hl7.Hl7AutoConsentCustomizer;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;

import com.github.susom.database.Database;

public class PainHl7Appointment extends Hl7AutoConsentCustomizer implements Hl7AppointmentIntf {
  public PainHl7Appointment(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
  }

  public boolean skipType(String visitType, String department) {
    if (visitType != null) {
      // do not import CONF (conference about patient) appointments
      return visitType.toUpperCase().equals("CONF") || visitType.toUpperCase().equals("TEAM CONF");
    }
    return false;
  }
}
