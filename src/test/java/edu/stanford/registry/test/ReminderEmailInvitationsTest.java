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

package edu.stanford.registry.test;

import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Test cases to verify UserInfo works properly.
 */
public class ReminderEmailInvitationsTest extends DatabaseTestCase {
  private String surveyLink = "https://outcomes.stanford.edu";
  private String emailAddress = "testing@test.stanford.edu";
  private Date dob = DateUtils.getDaysAgoDate(30 * 365);
  private User user;

  private static final Logger logger = LoggerFactory.getLogger(ReminderEmailInvitationsTest.class);

  @Override
  protected void postSetUp() throws Exception {
    user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
  }

  /**
   * I'm not positive all these are needed
   */
  public void testPropertiesAreSetRight() {
    assertOnePropertyValue("appointment.initialemail.daysout", "7");
    assertOnePropertyValue("appointment.noemail.withindays", "2");
    assertOnePropertyValue("appointment.lastsurvey.daysout", "11");
    assertOnePropertyValue("appointment.reminderemail.daysout", "4,1");
    assertOnePropertyValue("appointment.scheduledsurvey.daysout", "90");
    assertOnePropertyValue("appointment_template", "apptTemplate");
  }

  public void testReminders() throws Exception {
    /**
     * Tests surveys that have had emails sent and are now on one of the reminder days
     */
    logger.info("testReminders starting");
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout"); // set above
    int days = Integer.parseInt(dayString);
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    Date[] reminderDates = utils.getReminderDates(days);
    Date reminderDate = new Date();
    for (Date reminderDate1 : reminderDates) {
      if (reminderDate1.after(reminderDate)) reminderDate = reminderDate1;
    }
    /*
     * Test 1: when survey date is one of the reminder days from today,
     * consented, has email address, but was sent email in the last 2 days
     * (no) [ex: drop in 5 days ahead, gets email, next day is reminder day]
     */
    Date today = new Date();
    Date yesterday = new Date(today.getTime() - DateUtils.MILISECONDS_PER_DAY);
    Date daysAgo3 = new Date(today.getTime() - 3 * DateUtils.MILISECONDS_PER_DAY);
    Patient patient = new Patient("10044-6", "John", "Doe", new java.util.Date(dob.getTime()));
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    patient = patientDao.addPatient(patient);
    String patientId = patient.getPatientId();
    Date apptTime = new Date(reminderDate.getTime());
    ApptRegistration apptRegistration0 = utils.getInitialRegistration(patientId, apptTime, emailAddress, "NPV60");
    apptRegistration0.setSendEmail(true);

    AdministrativeServices adminSvc = getAdminService(user);
    adminSvc.setPatientAgreesToSurvey(patient);
    adminSvc.addPatientRegistration(apptRegistration0, patient);

    // set the email date on its notification to yesterday
    int updCount = updateEmailDates("Test #1 (1): ", "yesterday", yesterday, apptRegistration0);
    assertEquals(1, updCount);
    // a reminder email should not be sent for this survey because one was
    // sent yesterday
    int emailsSent;// = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    //logger.info("Test #1 (0): " + emailsSent + " emails reminders were sent for survey that was email'd yesterday.");


    /*
     * Test 2: Survey date is one of the reminder days from today, consented,
     * has email address, not sent email in the last 2 days and the survey is
     * not done (yes)
     */

    // set the email date on its notification to 3 days ago
    // TABLEREF notification
    updCount = updateEmailDates("Test #2 (1): ", "3 days ago", daysAgo3, apptRegistration0);
    assertEquals(1, updCount);

    emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #2 (1): {} emails reminders were sent for survey email'd 3 days ago.", emailsSent);
    assertEquals(1, updCount);

    /*
     * Test 3: survey date is one of the reminder days from today, consented,
     * has email address, consented, has email address, not sent email in the
     * last 2 days but the survey is done (no)
     */
    // TABLEREF notification
    updCount = deleteNotifications("Test #3 (2): ", apptRegistration0.getAssessmentRegId());
    assertEquals(2, updCount);

    setAllStudiesToDone(apptRegistration0);

    // now run the emails
    emailsSent = adminSvc.doSurveyInvitations(getSiteInfo().getMailer(), surveyLink);
    logger.info("Test #3 (0): {} emails reminders were sent for a survey that has been completed.", emailsSent);
    assertEquals(0, emailsSent);
    logger.info("testReminders finished");
  }


  private int updateEmailDates(String title, String toWhen, Date surveyDt, ApptRegistration apptRegistration0) {
    int updCount = databaseProvider.get().toUpdate("update notification set email_dt = ? where assessment_reg_id = ?")
        .argDate(surveyDt).argLong(apptRegistration0.getAssessmentRegId()).update();
    logger.info( "{} {} notifications were updated to {}", title, updCount, toWhen);
    return updCount;
  }


  @SuppressWarnings("deprecation") // apptRegistration0.getSurveyReg()
  private void setAllStudiesToDone(ApptRegistration apptRegistration0) {
    /* get the PatientStudy entries for this registration */
    PatientStudy patientStudy = new PatientStudy(getSiteInfo().getSiteId());
    patientStudy.setToken(apptRegistration0.getSurveyReg().getToken());
    PatStudyDao patStudyDao = new PatStudyDao(databaseProvider.get(), getSiteInfo());
    ArrayList<PatientStudy> studyList = patStudyDao.getPatientStudiesByToken(
        apptRegistration0.getSurveyReg().getToken(), false);

    logger.info("Updating {} studies to DONE", studyList.size());
    // update each one to done
    Date dtChanged = new Date();
    for (PatientStudy aStudyList : studyList) {
      aStudyList.setDtChanged(dtChanged);
      patStudyDao.setPatientStudyContents(aStudyList, "<Form>done</Form>");
    }
  }

  private int deleteNotifications(String title, Long regId) {
    Database db = databaseProvider.get();
    // First, print out the notifications
    db.toSelect("select survey_site_id, patient_id, email_type, email_dt, survey_dt FROM notification WHERE assessment_reg_id=?").
      argLong(regId)
      .query(new RowsHandler<Boolean>() {
        @Override
        public Boolean process(Rows rs) throws Exception {
          logger.info(" ======== Notifications to be deleted: ");
          while (rs.next()) {
            Long siteId = rs.getLongOrNull("SURVEY_SITE_ID");
            String patientId = rs.getStringOrNull("PATIENT_ID");
            String emailType = rs.getStringOrNull("EMAIL_TYPE");
            String emailDate = rs.getStringOrEmpty("EMAIL_DT");
            String surveyDate = rs.getStringOrEmpty("SURVEY_DT");
            logger.info(" ===== Site:{} patient:{} emailType:{} emailDate:{} surveyDate:{}",
                siteId, patientId, emailType, emailDate, surveyDate);
          }
          return Boolean.FALSE; // ignore
        }
      });
    
    int updCount = db.toUpdate("delete from notification where assessment_reg_id = ?")
        .argLong(regId).update();
    logger.info("{} {} notifications were deleted.", title, updCount);
    return updCount;
  }

  private AdministrativeServices getAdminService(User user) {
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }
}
