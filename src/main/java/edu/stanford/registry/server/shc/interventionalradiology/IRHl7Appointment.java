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
package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.data.Hl7Appointment;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.ProcessAttribute;

import java.util.Date;
import java.util.List;

import com.github.susom.database.Database;

public class IRHl7Appointment extends Hl7Appointment implements Hl7AppointmentIntf {
  private final SiteInfo siteInfo;
  private final Database database;
  private final PatientDao patientDao;
  public IRHl7Appointment(Database database, SiteInfo siteInfo) {
    super(database, siteInfo);
    this.database = database;
    this.siteInfo = siteInfo;
    this.patientDao = new PatientDao(database, siteInfo.getSiteId());
  }

  @Override
  public String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ApptRegistration lastCompleted = assessDao.getLastCompletedRegistration(patientId, apptDate);
    if (lastCompleted == null) {
      return (curSurveyType == null) ? IRCustomizer.SURVEY_NOSURVEY : curSurveyType;
    }

    AssessmentRegistration lastAssessment = lastCompleted.getAssessment();
    String lastType = lastAssessment.getAssessmentType();

    int visits = getIRConsent(patientId) ? 1 : 0;

    if (lastType.contains("DVT")) {
      return getFollowUp("DVT", visits, apptDate);
    } else if (lastType.contains("Lymphedema")) {
      return getFollowUp("Lymphedema", visits, apptDate);
    } else if (lastType.contains("LymLeg")) {
      return getFollowUp("LymLeg", visits, apptDate);
    } else if (lastType.contains("LymArm")) {
      return getFollowUp("LymArm", visits, apptDate);
    } else {
      return IRCustomizer.SURVEY_NOSURVEY;
    }
  }

  private boolean getIRConsent(String patientId) {
    PatientAttribute attr = patientDao.getAttribute(patientId, IRCustomizer.ATTR_IR_CONSENT);
    if (attr == null) {
      return false;
    }
    String strValue = attr.getDataValue();
    return (strValue != null) && strValue.equalsIgnoreCase("Y");
  }

  private String getFollowUp(String type, int visits, Date apptDt) {
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    List<ProcessAttribute> processAttributes = surveyRegUtils.getVisitTypes();

    for (ProcessAttribute processAttribute : processAttributes) {
      if (processAttribute.getName().contains(type)) {
        if (processAttribute.getInteger() > visits) {
          if (apptDt == null || processAttribute.qualifies(apptDt)) {
            return processAttribute.getName();
          }
        }
      }
    }

    return processAttributes.get(processAttributes.size() - 1).getName();
  }
}
