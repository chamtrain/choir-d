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
package edu.stanford.registry.tool;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.ServerInit;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.DbRun;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Create new 'AngerFollowUp' surveys for patients with a 'followTraitAngerConsent' attribute set to 'Y'
 * who were previously sent at least one 'AngerFollowUp' survey but have never completed one.
 * This will only build the surveys, run the 'Send Email' function to actually create and send the emails.
 *
 * System parameters that need to be set to run are:
 *      database.url
 *      database.user
 *      database.password
 *
 * @author tpacht
 */
public class PatientFollowupSurveyMaker {
  private final static Logger logger = LoggerFactory.getLogger(PatientFollowupSurveyMaker.class);
  private final Calendar surveyDt;
  private final Calendar endRangeDate;
  private final SiteInfo siteInfo;
  private final SimpleDateFormat dateFormat;
  private final String processType;
  private final String visitType;
  private final String attribute;

  private static boolean isEmpty(String...strings) {
    for (String s: strings) {
      if (s==null || s.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    try {
      /*
       * Check that the required connection parameters have been provided
       */
      FileUtils.forceMkdir(new File("build"));
      String url = System.getProperty("database.url");
      String user = System.getProperty("database.user");
      String pwd = System.getProperty("database.password");
      if (isEmpty(url, user, pwd))
        throw new RuntimeException(
            String.format("Missing properties: database.url (%s), .user (%s), .password (%s)",
                 url, user, pwd));

      /*
       * Initialize the SitesInfo and create the database connection
       */
      ServerInit serverInit = ServerInit.initForMain(null, // makes all the sites load in from the database
          "email.template.directory", "src/main/resources/default/email-templates",
          "PatientIdFormat", "d{5,7}-d{1}|d{5,9}",
          "default.dateFormat", "MM/dd/yyyy",
          "default.dateTimeFormat", "MM/dd/yyyy h:mm a");
      SitesInfo sitesInfo = serverInit.getServerContext().getSitesInfo();
      Builder registry = DatabaseProvider.fromDriverManager(url, user, pwd).withSqlInExceptionMessages().withSqlParameterLogging();

      /*
       * Run the process to create the surveys
       */
      PatientFollowupSurveyMaker generator = new PatientFollowupSurveyMaker(sitesInfo,  "AngerFollowUp", "followTraitAngerConsent");
      generator.makeFollowUps(registry);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private PatientFollowupSurveyMaker(SitesInfo sitesInfo, String surveyType, String patientAttribute) {
    siteInfo = sitesInfo.getBySiteId(1L);
    dateFormat = DateUtils.newDateFormat(siteInfo, "yyyy-MM-dd hh:mm:ss.S");
    // Make the survey date a week from tomorrow
    surveyDt = Calendar.getInstance();
    surveyDt.setTime(new Date());
    surveyDt.add(Calendar.DAY_OF_YEAR, 7);
    endOfDay(surveyDt);

    // Get the end of range date as 3 months ago
    endRangeDate = Calendar.getInstance();
    endRangeDate.setTime(new Date());
    endRangeDate.add(Calendar.MONTH, -3);
    endRangeDate.add(Calendar.DAY_OF_YEAR, -8);
    endOfDay(endRangeDate); // set it to 11:59 pm

    processType = XMLFileUtils.getInstance(siteInfo).getActiveProcessForName(surveyType, surveyDt.getTime());
    visitType = XMLFileUtils.getInstance(siteInfo).getAttribute(processType, "visitType");
    attribute = patientAttribute;
  }

  private void makeFollowUps(Builder registry) {
    /*
     * Find the patients that have consented. Have the named attribute, set to "Y"
     * who have been previously sent this type of survey but have never started one.
     */
    String noResponseSQL = "select distinct pa.patient_id "
          + "from patient_attribute pa left join patient p on p.patient_id = pa.patient_id "
          + "where pa.data_value = 'Y' and pa.data_name = ? "
          + "and pa.survey_site_id = :site and pa.dt_created < ? "
          + "and pa.patient_id not in " // patients who have never started this type of survey
          + "    (select patient_id from rpt_pain_std_surveys_square where assessment_type = ?) "
          + "and exists "  // where previously sent one
          + "    (SELECT * from survey_registration sa where survey_type = ? "
          + "     and sa.patient_id = pa.patient_id and sa.survey_site_id = pa.survey_site_id) "
          + "and not exists " // don't have one pending
          + "    (SELECT * from survey_registration sreg WHERE  sreg.survey_site_id = :site and sreg.patient_id = pa.patient_id "
          + "     and sreg.survey_type = ? and survey_dt between trunc(sysdate) and trunc(sysdate + 14)) ";
    List<String> noResponsePatients = getPatients(registry, noResponseSQL, endRangeDate.getTime());
    logger.info("Found {} patients who did not start the {} assessment",
        noResponsePatients.size(), processType);
    buildOutAngerSurveys(noResponsePatients, registry);

    /*
     * Find the patients that have consented. Have the named attribute, set to "Y"
     * who have been previously sent this type of survey, started the survey but didn't finish.
     */
    String notFinishedSQL = "select distinct pa.patient_id "
          + "from rpt_pain_std_surveys_square sq, patient_attribute pa "
          + "where sq.patient_id = pa.patient_id and pa.data_value = 'Y' "
          + "and pa.data_name = ? and pa.survey_site_id = :site and pa.dt_created < ? "
          + "and assessment_type = ? and is_complete = 'N' "  // have started started taking this type of survey
          + "and not exists " // bud didn't ever finish any that were sent
          + "   (SELECT * from rpt_pain_std_surveys_square sq where sq.patient_id = pa.patient_id "
          + "    and assessment_type = ? and is_complete = 'Y') "
          + "and not exists " // don't have one pending
          + "   (SELECT * from survey_registration sreg WHERE  sreg.survey_site_id = :site and sreg.patient_id = pa.patient_id "
          + "    and sreg.survey_type = ? and survey_dt between trunc(sysdate) and trunc(sysdate + 14)) ";
    List<String> notFinishedPatients = getPatients(registry, notFinishedSQL, endRangeDate.getTime());
    logger.info("Found {} patients that started, but did not finish the {} assessment ",
        notFinishedPatients.size(), processType);
    buildOutAngerSurveys(notFinishedPatients, registry);
  }

  /*
   * Builds the surveys for the list of patients provided
   */
  private void buildOutAngerSurveys(List<String> patientList, Builder builder) {
    builder.transact(new DbRun() {
      @Override
      public void run(Provider<Database> dbp) {
        int counts = 0;
        try {
          if (patientList.size() > 0) {
            SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
            PatientDao patAttribDao = new PatientDao(dbp.get(), siteInfo.getSiteId(), ServerUtils.getAdminUser(dbp.get()));

            for (String patientId : patientList) {
              /*
               *  Make sure the patient is registered, has an email address and hasn't been sent a survey for this treatment set
               *  within 27 days of this surveys date, and then create them a standalone survey registration
               */
              Patient thisPatient = patAttribDao.getPatient(patientId);
              if (thisPatient.hasConsented()
                  && thisPatient.hasValidEmail() && thisPatient.getEmailAddress() != null) {
                counts = counts
                    + createAssessment(dbp.get(), surveyRegUtils, surveyDt.getTime(), patientId, thisPatient.getEmailAddress());
              } else {
                if (!thisPatient.hasConsented()) {
                  logger.debug("Not sending {} survey to {}. Patient has not consented", processType, patientId);
                }
                if (!thisPatient.hasValidEmail()) {
                  logger.debug("Not sending {} survey to {}. Patient does not have a valid email", processType, patientId);
                }
                else if (thisPatient.getEmailAddress() == null) {
                  logger.debug("Not sending {} survey to {}. Patients email address is null", processType, patientId);
                }
              }
            }
          }
        } catch (Exception ex) {
          logger.error("Error creating {} surveys, dated {} ", processType, dateFormat.format(surveyDt.getTime()), ex);
        }
        logger.info("Created {} {} surveys, dated {}", counts, processType, dateFormat.format(surveyDt.getTime()));
      }
    });
  }

  private List<String> getPatients(Builder registry, String sql, Date rangeEnd) {
    List<String> patientList = new ArrayList<>();
    registry.transact(dbp -> {
      dbp.get().toSelect(sql)
          .argString(attribute)
          .argLong(":site", siteInfo.getSiteId())
          .argDate(rangeEnd)
          .argString(processType)
          .argString(processType)
          .argString(processType)
          .query(new RowsHandler<Object>() {
            @Override
            public Object process(Rows rs) throws Exception {
              while (rs.next()) {
                patientList.add(rs.getStringOrEmpty());
              }
              return null;
            }
          });
    });
    return patientList;
  }

  private int createAssessment(Database database, SurveyRegUtils surveyRegUtils,
                                      Date surveyDate, String patientId, String emailAddress) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ApptRegistration reg = new ApptRegistration(siteInfo.getSiteId(), patientId, surveyDate, emailAddress, processType,
        Constants.REGISTRATION_TYPE_STANDALONE_SURVEY, visitType);

    reg = surveyRegUtils.createRegistration(assessDao, reg);
    logger.debug("buildOutTreatmentSetSurveys Created a new registration {} with survey type: {}, visit type: {}, dated {} ",
        reg.getApptRegId(), reg.getSurveyType(), reg.getVisitType(),  dateFormat.format(reg.getAssessmentDt()));
    return 1;
  }

  private static void endOfDay(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 0);
  }

}
