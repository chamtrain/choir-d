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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;


public class SurveyRegUtilsDbTest extends DatabaseTestCase {
  private Supplier<Database> databaseProvider;
  private String surveyLink = "https://outcomes.stanford.edu";
  private String emailAddress = "testing@test.stanford.edu";

  final private Date dob = DateUtils.getDaysAgoDate(30 * 365);
  private User user;

  private static Logger logger = Logger.getLogger(SurveyRegUtilsDbTest.class);

  @Override
  protected void postSetUp() throws Exception {
    databaseProvider = getDatabaseProvider();
    user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    mailer = getSiteInfo().getMailer();
  }

  Mailer mailer = null;

  String getPatientAtr(PatientDao pdau, Patient pat, String atrName) {
    ArrayList<PatientAttribute> attrs = pdau.getAttributes(pat.getPatientId());
    for (PatientAttribute atr: attrs) {
      if (atr.getDataName().equals(atrName))
        return atr.getDataValue();
    }
    return null;
  }


  /**
   * This fails when the '||' in SurveyRegUtils.registerAssessments() is an '&&' (fixed November, 2017)
   * It's copied from NewEmailInvitationsTest
   */
  public void testNewAppointments() throws Exception {
    logger.info("testNewAppointments starting");

    // initialize some tools and variable
    AdministrativeServices adminSvc = getAdminService(user);
    Database db = databaseProvider.get();
    Utils utils = new Utils(db, getSiteInfo());
    // set up 2 days for emails
    Date surveyDt = initialDaysOut(0);

    // Create patient0 - no permission or email and appointment for the tests
    Patient testPatient0 = createPatient("10040-4", "John", "Doe");
    utils.addInitialRegistration(db, testPatient0.getPatientId(), surveyDt, emailAddress, "NPV60");
    adminSvc.setPatientAgreesToSurvey(testPatient0);

    Patient testPatient1 = createPatient("10041-2", "John", "Smith3");
    utils.addInitialRegistration(db, testPatient1.getPatientId(), surveyDt, emailAddress, "NPV60");
    adminSvc.setPatientAgreesToSurvey(testPatient1);
    checkNoPatientHas2RegistrationActivities(db);

    adminSvc.doSurveyInvitations(mailer, surveyLink);
    checkNoPatientHas2RegistrationActivities(db);
  }


  private Boolean checkNoPatientHas2RegistrationActivities(Database db) {
    String q = "SELECT patient_id, assessment_reg_id, token FROM activity "
              + "WHERE activity_type='Registered' ORDER BY patient_id, token, assessment_reg_id";
    return db.toSelect(q).query(new RowsHandler<Boolean>() {
      @Override public Boolean process(Rows rs) throws Exception {
        String lastPat = "", lastToken = "";
        Long lastAsmtId  = 0L;
        String msg = "";
        while (rs.next()) {
          String pat = rs.getStringOrEmpty(1);
          Long asmtId = rs.getLongOrZero(2);
          String token = rs.getStringOrEmpty(3);
          if (pat.equals(lastPat)) {
            msg = String.format("Patient %s has 2 'Registered' activities, for tokens (%s,%s) & asmt (%d,%d)",
                                pat, lastToken, token, lastAsmtId, asmtId);
            logger.error(msg);
          }
          lastPat = pat;  lastToken = token;  lastAsmtId = asmtId;
        }
        assertTrue(msg, msg.isEmpty());
        return Boolean.TRUE;
      }
    });
  }

  private Patient createPatient(String idea, String firstName, String lastName) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Patient testPatient = new Patient(idea, firstName, lastName, new java.util.Date(dob.getTime()));
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  private Date initialDaysOut(int plusDays) {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    return new Date(DateUtils.getDaysOutDate(days + plusDays).getTime());
  }

  private AdministrativeServices getAdminService(User user) {
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }
}
