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
package edu.stanford.registry.test;

import edu.stanford.registry.client.service.AppointmentStatus;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.shc.interventionalradiology.IRCustomizer;
import edu.stanford.registry.server.shc.interventionalradiology.IRSurveyScheduler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import com.github.susom.database.Database;

public class IRSurveySchedulerTest extends DatabaseTestCase {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private final String emailAddress = "testing@test.stanford.edu";
  private Date dob;

  private Supplier<Database> databaseProvider;
  private Utils utils;
  private AdministrativeServices adminSvc;

  //Test class setup
  private IRSurveyScheduler scheduler;
  private static final String METHOD_NAME = "scheduleFollowUp";
  private Object[] parameters;
  private SurveyRegUtils regUtils;
  private Patient patient0;
  private Patient patient1;
  private Patient patient2;

  @Override
  protected void postSetUp() throws Exception {
    databaseProvider = getDatabaseProvider();
    User user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    adminSvc = getAdminService(user);
    utils = new Utils(databaseProvider.get(), getSiteInfo());
    dob = DateUtils.getDaysAgoDate(getSiteInfo(), 30 * 365);
    //Reference to Survey scheduler class
    scheduler = getIRSurveyScheduler();
    setUpCall();
    //scheduleThreeMonthsFollowUp: has two params, setting up them below

    regUtils = new SurveyRegUtils(getSiteInfo());
    patient0 = createPatient("99999-0", "John0", "Doe0");
    patient1 = createPatient("99999-1", "John1", "Doe1");
    patient2 = createPatient("99999-2", "John2", "Doe2");
  }

  private IRSurveyScheduler getIRSurveyScheduler() {
    return new IRSurveyScheduler(databaseProvider.get(), getSiteInfo());
  }

  private AdministrativeServices getAdminService(User user) {
    return new AdministrativeServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

  private Patient createPatient(String idea, String firstName, String lastName) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Patient testPatient = new Patient(idea, firstName, lastName, new Date(dob.getTime()));
    testPatient.setConsent("y");
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  private void addEmailAttribute(Patient patient) {
    String id = patient.getPatientId();
    PatientAttribute patAttribute =
        new PatientAttribute(id, Constants.ATTRIBUTE_SURVEYEMAIL, emailAddress, PatientAttribute.STRING);
    adminSvc.addPatientAttribute(patAttribute);
  }

  private void addBaseDateAttribute(Patient patient, String type, Date date) {
    PatientAttribute patientAttribute =
        new PatientAttribute(patient.getPatientId(), type, dateFormat.format(date));
    adminSvc.addPatientAttribute(patientAttribute);
  }

  private Date initialDaysOut() {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    return new Date(DateUtils.getDaysOutDate(getSiteInfo(), days ).getTime());
  }

  public void testScheduleDVTFollowUp() throws Exception {
    //Reference to method to be invoked
    Method scheduleFollowUp = scheduler.getClass().getDeclaredMethod(METHOD_NAME, String.class, String.class, Date.class);
    scheduleFollowUp.setAccessible(true);

    //List of params to be passed for invocation
    Object[] schParameters = new Object[3];

    // Follow up schedule is 1 month, 6 months and 12 months

    //Patient with 2 past appointments and one coming up in 3 days
    addEmailAttribute(patient2);
    adminSvc.setPatientAgreesToSurvey(patient2);

    Date now = new Date();
    Calendar calendar = Calendar.getInstance();

    //A patient with two appointments in the past: 1 and 2 months ago, and another 3 days from now
    calendar.setTime(now);
    calendar.add(Calendar.MONTH, -1);
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    addBaseDateAttribute(patient2, IRCustomizer.ATTR_DVT_BASE_DATE, calendar.getTime());
    addRegistration(databaseProvider.get(), patient2.getPatientId(), calendar.getTime(), emailAddress, "DVT.0120", "RTN").setApptComplete(AppointmentStatus.completed.toString());
    calendar.setTime(now);
    calendar.add(Calendar.MONTH, -2);
    calendar.add(Calendar.DAY_OF_YEAR, 2);
    addRegistration(databaseProvider.get(), patient2.getPatientId(), calendar.getTime(), emailAddress, "DVT.0120", "RTN");
    calendar.setTime(now);
    calendar.add(Calendar.DAY_OF_YEAR, 3);
    addRegistration(databaseProvider.get(), patient2.getPatientId(), calendar.getTime(), emailAddress, "DVT.0120","RTN");
    // * Not eligible

    //A patient with a DEVT base date and appointment 1 months ago and another appt 2 months ago
    addEmailAttribute(patient1);
    adminSvc.setPatientAgreesToSurvey(patient1);

    calendar.setTime(now);
    calendar.add(Calendar.MONTH, -1);

    addBaseDateAttribute(patient1,     IRCustomizer.ATTR_DVT_BASE_DATE, calendar.getTime());
    addRegistration(databaseProvider.get(), patient1.getPatientId(), calendar.getTime(), emailAddress, "DVT.0416", "RTN").setApptComplete(AppointmentStatus.completed.toString());
    calendar.add(Calendar.MONTH, -1);
    addRegistration(databaseProvider.get(), patient1.getPatientId(), calendar.getTime(), emailAddress, "DVT.0416", "RTN").setApptComplete(AppointmentStatus.completed.toString());

    // * Eligible

    schParameters[0] = IRCustomizer.ATTR_DVT_BASE_DATE;
    schParameters[1] = "DVT";
    schParameters[2] = DateUtils.getTimestampEnd(getSiteInfo(), initialDaysOut());
    int count = (Integer) scheduleFollowUp.invoke(scheduler, schParameters);
    assertEquals("Expecting one follow up to have been created", 1, count);
  }

  public void testScheduleHCCFollowUp() throws Exception {
    //Reference to method to be invoked
    Method scheduleFollowUp = scheduler.getClass().getDeclaredMethod("scheduleFollowUp", String.class, String.class, Date.class);
    scheduleFollowUp.setAccessible(true);

    // Follow up schedule is 1 week, 2 months and 6 months

    //Patient with 2 past appointments and one coming up in 3 days
    addEmailAttribute(patient0);
    adminSvc.setPatientAgreesToSurvey(patient0);

    Date now = new Date();
    Calendar calendar = Calendar.getInstance();

    // A patient with an HCC base date 2 months ago and an office visit 25 days ago and another in 3 days
    calendar.setTime(now);
    calendar.add(Calendar.MONTH, -2);
    calendar.add(Calendar.DAY_OF_YEAR, 1);
    addBaseDateAttribute(patient0,     IRCustomizer.ATTR_HCC_BASE_DATE, calendar.getTime());

    calendar.add(Calendar.DAY_OF_YEAR, 5);
    addRegistration(databaseProvider.get(), patient0.getPatientId(), calendar.getTime(), emailAddress, "InitialHCC.1118", "NEW").setApptComplete(AppointmentStatus.completed.toString());
    calendar.setTime(now);

    calendar.add(Calendar.DAY_OF_YEAR, -25);
    String emailAddress2 = "mytesting@test.stanford.edu";
    addRegistration(databaseProvider.get(), patient2.getPatientId(), calendar.getTime(), emailAddress2, "FollowUp.1118", "RTN");
    calendar.setTime(now);
    calendar.add(Calendar.DAY_OF_YEAR, 3);
    addRegistration(databaseProvider.get(), patient2.getPatientId(), calendar.getTime(), emailAddress2, "FollowUp.1118","RTN");
    // *  Eligible

    parameters[0] = IRCustomizer.ATTR_HCC_BASE_DATE;
    parameters[1] = "HCC";
    parameters[2] = DateUtils.getTimestampEnd(getSiteInfo(), initialDaysOut());

    int count = (Integer) scheduleFollowUp.invoke(scheduler, parameters);
    assertEquals("Expecting one follow up to have been created", 1, count);

    //A patient with two appointments in the past: 1 months ago and 2 months ago
    addEmailAttribute(patient1);
    adminSvc.setPatientAgreesToSurvey(patient1);

    calendar.setTime(now);
    calendar.add(Calendar.MONTH, -7);
    calendar.add(Calendar.DAY_OF_YEAR, 1);

    addBaseDateAttribute(patient2, IRCustomizer.ATTR_HCC_BASE_DATE, calendar.getTime());
    // * Eligible
    count = (Integer) scheduleFollowUp.invoke(scheduler, parameters);
    assertEquals("Expecting one follow up to have been created", 1, count);
  }

  private ApptRegistration addRegistration(Database database, String patientId, Date surveyDt, String emailAddress, String surveyType, String visitCode) {
    ApptRegistration apptRegistration = utils.getRegistration(patientId, surveyDt, emailAddress, surveyType, visitCode);
    AssessDao assessDao = new AssessDao(database, getSiteInfo());
    return regUtils.createRegistration(assessDao, apptRegistration);
  }

  private void setUpCall() throws Exception {
    Class[] parameterTypes = new Class[3];
    parameterTypes[0] = String.class;
    parameterTypes[1] = String.class;
    parameterTypes[2] = Date.class;

    //Reference to method to be invoked
    Method scheduleFollowUp = scheduler.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
    scheduleFollowUp.setAccessible(true);

    //List of params to be passed for invocation
    parameters = new Object[3];
  }
}

