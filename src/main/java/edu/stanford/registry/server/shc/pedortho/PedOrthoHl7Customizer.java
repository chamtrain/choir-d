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
package edu.stanford.registry.server.shc.pedortho;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.hl7.Hl7Customizer;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.imports.data.Hl7Appointment;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Patient;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class PedOrthoHl7Customizer extends Hl7Customizer implements Hl7CustomizerIntf {
  private final SiteInfo siteInfo;

  private static final Logger logger = LoggerFactory.getLogger(PedOrthoHl7Customizer.class);
  private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

  public PedOrthoHl7Customizer(SiteInfo siteInfo) {
    super(siteInfo);
    this.siteInfo = siteInfo;
  }

  public Hl7AppointmentIntf getHl7Appointment(Database database) {
    return new PedOrthoHl7Appointment(database, siteInfo);
  }

  public class PedOrthoHl7Appointment extends Hl7Appointment implements Hl7AppointmentIntf {

    public PedOrthoHl7Appointment(Database database, SiteInfo siteInfo) {
      super(database, siteInfo);
    }

    @Override
    public String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType) {
      if (patient == null) {
        return PedOrthoCustomizer.SURVEY_NOSURVEY;
      }
      String surveyType;
      try {
        if (patient.attributeEquals(PedOrthoCustomizer.ATTR_SCOLIOSIS, "Y")) {
          if ((providerEid != null) && (providerEid.trim().equals("044310"))) {
            // Assign SRS30 + HealthMindset for Dr Tileston's patients
            surveyType = getSurveyTypeFor(PedOrthoCustomizer.SURVEY_SRS30_HM, apptDate);
          } else {
            surveyType = getSurveyTypeFor(PedOrthoCustomizer.SURVEY_SRS30, apptDate);
          }
        } else {
          surveyType = PedOrthoCustomizer.SURVEY_NOSURVEY;
        }
      } catch (Exception e) {
        logger.error("Unexpected error while processing appointment for " + patient.getPatientId() + " on "
            + dateTimeFormat.format(apptDate), e);
        surveyType = PedOrthoCustomizer.SURVEY_NOSURVEY;
      }

      return surveyType;
    }
  }

  private String getSurveyTypeFor(String name, Date apptDate) throws Exception {
    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    String surveyType = xmlFileUtils.getActiveProcessForName(name, apptDate);
    if (surveyType == null) {
      throw new Exception("Process not found for survey name " + name + " and date " + apptDate);
    }
    return surveyType;
  }
}
