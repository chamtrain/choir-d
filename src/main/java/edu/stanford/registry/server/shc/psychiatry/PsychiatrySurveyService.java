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

import com.github.susom.database.Database;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.config.customassessment.AssignAssessmentConfigHandler;
import edu.stanford.registry.server.config.customassessment.CustomAssessmentConfigUtil;
import edu.stanford.registry.server.config.customassessment.InstrumentEntry;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.AssignedPatientAssessment;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientExtendedAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class PsychiatrySurveyService extends RegistryAssessmentsService {

  private static final Logger logger = LoggerFactory.getLogger(PsychiatrySurveyService.class);

  public PsychiatrySurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public void registerAssessment(Database database, Element questionnaire, String patientId,
      Token tok, User user) {

    String study = questionnaire.getAttribute("value");
    String clinicName = getClinic(database, tok);
    String assessmentType = PsychiatryCustomizer.DEFAULT_SURVEY_TYPE;

    String assignmentJSON = getAssignmentJSON(database, patientId, user);

    if (assignmentJSON != null && !assignmentJSON.isEmpty()) {
      // Patient level assessment config - all assigned assessments will be registered.
      AssignAssessmentConfigHandler handler = new AssignAssessmentConfigHandler(siteId, patientId, assignmentJSON);
      AssignedPatientAssessment asInCurClinic = handler.getAssignmentByClinic(clinicName);
      if (asInCurClinic.hasAssignment() && asInCurClinic.getFrequency(study) != null) {
        // Get the minimum frequency for the current study throughout the clinic, further check, insures to assess on the lowest frequency
        List<AssignedPatientAssessment> assignmentLists = handler.getAllAssignment();
        Integer frequency = getLowFrequency(study, assignmentLists);
        register(database, questionnaire, patientId, tok, user, study, frequency);
      }
    } else {
      // Clinic level assessment config
      // Check to make sure that its time that the patient takes this study configured bsaed on the clinic setting
      CustomAssessmentConfigUtil customAssessmentConfigUtil = getCustomAssessmentConfig();
      InstrumentEntry instrument = customAssessmentConfigUtil.getInstrument(clinicName, assessmentType, study);
      if (instrument != null && instrument.getFrequency() != null) {
        register(database, questionnaire, patientId, tok, user, study, instrument.getFrequency());
      } else {
        //skip
        logger.info("{} is not configured in the current clinic.", study);
      }

    }
  }


  private void register(Database database, Element questionnaire, String patientId, Token tok, User user, String study,
      Integer frequency) {
    if (isTimeToAssess(database, frequency, patientId, study)) {
      super.registerAssessment(database, questionnaire, patientId, tok, user);
      logger.info("{} is added to the assessment registration.", study);
    } else {
      logger.info("The patient is not yet to be assessed with the {}", study);
    }
  }


  private CustomAssessmentConfigUtil getCustomAssessmentConfig() {
    CustomAssessmentConfigUtil cConfig = null;

    if (siteInfo.getProperty(Constants.ENABLE_CUSTOM_ASSESSMENT_CONFIG).equalsIgnoreCase("y")) {
      String configValue = siteInfo.getProperty(Constants.CUSTOM_ASSESSMENT_CONFIG_NAME);

      // Customizing assessment configuration feature is enabled but nothing is configured, start with default config.
      if (configValue == null) {
        configValue = Constants.DEFAULT_CUSTOM_ASSESSMENT_CONFIG_VALUE;
      }
      cConfig = new CustomAssessmentConfigUtil(configValue);
    } else {
      //custom assessment config is not enabled is disabled
      throw new ServiceUnavailableException(
          "Initializing assessment configuration feature when site property is not enabled.");
    }
    return cConfig;
  }


  private Date getLastStudyDate(Database db, String patientId, String study) {
    PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);
    ArrayList<PatientStudyExtendedData> patExtendedList = patStudyDao.getPatientStudyExtendedDataByPatientId(patientId);
    PatientStudyExtendedData lastCompletedStudy = patExtendedList.stream()
        .filter(val -> val.getStudyDescription().equals(study))
        .max(Comparator.comparing(PatientStudyExtendedData::getDtChanged))
        .orElse(null);

    if (lastCompletedStudy != null) {
      return lastCompletedStudy.getDtChanged();
    }
    return null;
  }


  private boolean isTimeToAssess(Database db, Integer frequency, String patientId, String study) {
    // Is the study part of the passed assessment type?
    if (frequency != null && frequency > 0) {
      Date lastCompletedDate = DateUtils.getTimestampStart(siteInfo, getLastStudyDate(db, patientId, study));
      Date afterLastStudyDate = DateUtils.getDaysAgoDate(siteInfo, frequency);

      if (lastCompletedDate == null) {
        //First time this patient is being assessed the current study
        return true;
      }

      if (lastCompletedDate.compareTo(afterLastStudyDate) < 0 || lastCompletedDate.compareTo(afterLastStudyDate) == 0) {
        return true;
      }
    }
    return false;
  }


  private String getClinic(Database db, Token tok) {
    // get the clinic of the current appointment
    String clinic = db.toSelect("select clinic from appt_registration ar, survey_registration sr "
        + " where ar.SURVEY_SITE_ID = sr.survey_site_id and ar.assessment_reg_id = sr.assessment_reg_id "
        + " and sr.token = ?").argString(tok.getToken()).query(rs -> {
      if (rs.next()) {
        return rs.getStringOrNull(1);
      }
      return null;
    });

    return clinic;
  }


  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return PsychiatryScoreProviderFactory.create(dbp, siteInfo, studyName);
  }


  private String getAssignmentJSON(Database dbp, String patientId, User user) {
    PatientDao patientDao = new PatientDao(dbp, siteId, user);
    PatientExtendedAttribute attribute = patientDao
        .getExtendedAttribute(patientId, Constants.ASSIGNED_ASSESSMENT_DATA_NAME);
    if (attribute == null) {
      logger.info("No assigned assessments configured for :" + patientId);
      return null;
    }

    return attribute.getDataValue();
  }


  private Integer getLowFrequency(String study, List<AssignedPatientAssessment> list) {
    Integer min = null;
    for (AssignedPatientAssessment a : list) {
      Integer cur = a.getFrequency(study);
      if (cur != null) {
        if (min == null) {
          min = cur;
        } else {
          if (min > cur) {
            min = cur;
          }
        }
      }
    }
    return min;
  }

}
