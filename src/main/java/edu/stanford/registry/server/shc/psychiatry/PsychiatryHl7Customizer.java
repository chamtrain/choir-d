/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.hl7.Hl7Customizer;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.imports.data.Hl7Appointment;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;

import java.util.Date;

import com.github.susom.database.Database;

public class PsychiatryHl7Customizer extends Hl7Customizer implements Hl7CustomizerIntf {
  private final SiteInfo siteInfo;

  public PsychiatryHl7Customizer(SiteInfo siteInfo) {
    super(siteInfo);
    this.siteInfo = siteInfo;
  }

  public Hl7AppointmentIntf getHl7Appointment(Database database) {
    return new PsychiatryHl7Appointment(database, siteInfo);
  }

  public class PsychiatryHl7Appointment extends Hl7Appointment implements Hl7AppointmentIntf {

    public PsychiatryHl7Appointment(Database database, SiteInfo siteInfo) {
      super(database, siteInfo);
    }

    @Override
    public String getSurveyType(String patientId, Date apptDate) {
      // All assessment types are "Default"
      return PsychiatryCustomizer.DEFAULT_SURVEY_TYPE;
    }
  }
}
