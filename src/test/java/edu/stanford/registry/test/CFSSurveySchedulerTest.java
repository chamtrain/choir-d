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
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.shc.cfs.CFSCustomizer;
import edu.stanford.registry.server.shc.cfs.CFSSurveyScheduler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class CFSSurveySchedulerTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(CFSSurveySchedulerTest.class);
  private final String emailAddress = "testing@test.stanford.edu";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private Date dob;

  private Supplier<Database> databaseProvider;
  private Utils utils;
  private AdministrativeServices adminSvc;

  //Test class setup
  private CFSSurveyScheduler scheduler;
  private Method scheduleThreeMonthsFollowUp;
  private static final String METHOD_NAME = "scheduleThreeMonthsFollowUp";
  private Object[] parameters;

  private Method scheduleFollowUps;
  private Object[] schParameters;

  @Override
  protected void postSetUp() throws Exception {
    databaseProvider = getDatabaseProvider();
    User user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    adminSvc = getAdminService(user);
    utils = new Utils(databaseProvider.get(), getSiteInfo());
    dob = DateUtils.getDaysAgoDate(getSiteInfo(), 30 * 365);
    //Reference to Survey scheduler class
    scheduler = getCFSSurveyScheduler();

    //scheduleThreeMonthsFollowUp: has two params, setting up them below
    Class[] parameterTypes = new Class[1];
    parameterTypes[0] = Date.class;

    //Reference to method to be invoked
    scheduleThreeMonthsFollowUp = scheduler.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
    scheduleThreeMonthsFollowUp.setAccessible(true);

    //List of params to be passed for invocation
    parameters = new Object[1];
    schParameters = new Object[1];
    scheduleFollowUps = scheduler.getClass().getDeclaredMethod("scheduleFollowUp", Date.class);
    scheduleFollowUps.setAccessible(true);
  }

  private CFSSurveyScheduler getCFSSurveyScheduler() {
    return new CFSSurveyScheduler(databaseProvider.get(), getSiteInfo());
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

  private Date initialDaysOut() {
    return new Date(DateUtils.getDaysOutDate(getSiteInfo(), numberInitialDaysOut()).getTime());
  }

  private int numberInitialDaysOut() {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    return Integer.parseInt(dayString);
  }

  public void testScheduleThreeMonthsFollowUp() throws Exception {
    logger.info("testScheduleThreeMonthsFollowUp starting");

    Date surveyDt = initialDaysOut();

    //A patient with two appointments in the past: 100days ago and 10days ago
    Patient patient0 = createPatient("99999-0", "John0", "Doe0");
    addEmailAttribute(patient0);
    adminSvc.setPatientAgreesToSurvey(patient0);
    utils.addInitialRegistration(databaseProvider.get(), patient0.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 100), emailAddress, "RPV60").setApptComplete(AppointmentStatus.completed.toString());
    utils.addFollowUpRegistration(databaseProvider.get(), patient0.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(),10), emailAddress, "RTN").setApptComplete(AppointmentStatus.completed.toString());
    // * Not eligible

    //Patient with 2 past appointments and one coming up in 3 days
    Patient patient2 = createPatient("99999-2", "John2", "Doe2");
    addEmailAttribute(patient2);
    adminSvc.setPatientAgreesToSurvey(patient2);
    utils.addInitialRegistration(databaseProvider.get(), patient2.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 10), emailAddress, "RPV60").setApptComplete(AppointmentStatus.completed.toString());
    utils.addFollowUpRegistration(databaseProvider.get(), patient2.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 5), emailAddress, "RTN");
    utils.addFollowUpRegistration(databaseProvider.get(), patient2.getPatientId(), DateUtils.getDaysOutDate(getSiteInfo(), 3), emailAddress, "RTN");
    // * Not eligible

    //Three appointments: past 2 in range and a third in the future outside of the 15days
    Patient patient3 = createPatient("99999-3", "John3", "Doe3");
    addEmailAttribute(patient3);
    adminSvc.setPatientAgreesToSurvey(patient3);
    utils.addInitialRegistration(databaseProvider.get(), patient3.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 10), emailAddress, "RPV60").setApptComplete(AppointmentStatus.completed.toString());
    utils.addFollowUpRegistration(databaseProvider.get(), patient3.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 5), emailAddress, "RTN").setApptComplete(AppointmentStatus.completed.toString());
    utils.addFollowUpRegistration(databaseProvider.get(), patient3.getPatientId(), DateUtils.getDaysOutDate(getSiteInfo(), 17), emailAddress, "RTN");
    // * Not eligible

    //One initial visit before 10days
    Patient patient5 = createPatient("99999-5", "John5", "Doe5");
    addEmailAttribute(patient5);
    adminSvc.setPatientAgreesToSurvey(patient5);
    utils.addInitialRegistration(databaseProvider.get(), patient5.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 10), emailAddress, "RPV60").setApptComplete(AppointmentStatus.completed.toString());
    // * Not eligible

    //One initial visit after 5days
    Patient patient6 = createPatient("99999-6", "John6", "Doe6");
    addEmailAttribute(patient6);
    adminSvc.setPatientAgreesToSurvey(patient6);
    utils.addInitialRegistration(databaseProvider.get(), patient6.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 5), emailAddress, "RPV60").setApptComplete(AppointmentStatus.completed.toString());
    // * Not eligible

    //Initial visit after 17days
    Patient patient7 = createPatient("99999-7", "John7", "Doe7");
    addEmailAttribute(patient7);
    adminSvc.setPatientAgreesToSurvey(patient7);
    utils.addInitialRegistration(databaseProvider.get(), patient7.getPatientId(), DateUtils.getDaysOutDate(getSiteInfo(), 17), emailAddress, "RPV60");
    // * Not Eligible

    //Initial visit after 35days
    Patient patient8 = createPatient("99999-8", "John8", "Doe8");
    addEmailAttribute(patient8);
    adminSvc.setPatientAgreesToSurvey(patient8);
    utils.addInitialRegistration(databaseProvider.get(), patient8.getPatientId(), DateUtils.getDaysOutDate(getSiteInfo(), 35), emailAddress, "RPV60");
    // * Not Eligible

    //Only one initial visit before 400days
    Patient patient9 = createPatient("99999-9", "John9", "Doe9");
    addEmailAttribute(patient9);
    adminSvc.setPatientAgreesToSurvey(patient9);
    utils.addInitialRegistration(databaseProvider.get(), patient9.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 400), emailAddress, "RPV60");
    // * Not Eligible - since this registration is past a year mark
    /*
        utils.addFollowUpRegistration(databaseProvider.get(), patient9.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 100), emailAddress, "RTN").setApptComplete(AppointmentStatus.completed.toString());
        if a patient, who's last visit was before the year mark, and got a visit in the last year - WILL BE surveyed
    */

    //A patient with two appointments in the past: 95days ago and 17days later
    Patient patient1 = createPatient("99999-1", "John1", "Doe1");
    addEmailAttribute(patient1);
    adminSvc.setPatientAgreesToSurvey(patient1);
    utils.addInitialRegistration(databaseProvider.get(), patient1.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 95), emailAddress, "RPV60").setApptComplete(AppointmentStatus.completed.toString());
    utils.addFollowUpRegistration(databaseProvider.get(), patient1.getPatientId(), DateUtils.getDaysOutDate(getSiteInfo(), 17), emailAddress, "RTN");
    // * Eligible

    //Only one initial visit before 100days
    Patient patient4 = createPatient("99999-4", "John4", "Doe4");
    addEmailAttribute(patient4);
    adminSvc.setPatientAgreesToSurvey(patient4);
    utils.addInitialRegistration(databaseProvider.get(), patient4.getPatientId(), DateUtils.getDaysAgoDate(getSiteInfo(), 100), emailAddress, "RPV60");
    // * Eligible

    // Eligible: 2
    // Not eligible: 8

    //init the two params
    parameters[0] = DateUtils.getTimestampEnd(getSiteInfo(), surveyDt);

    //invoke method for testing
    int count = (Integer) scheduleThreeMonthsFollowUp.invoke(scheduler, parameters);
    assertEquals(count, 2); // Test needs CFS xml, otherwise build will fail. This is checked against local process.xml
  }


  // Test case with 1 eligible patient and 1 not eligible patient
  public void testScheduleFollowUps() throws Exception {
    //List of params to be passed for invocation
    Date surveyDate = DateUtils.getTimestampEnd(getSiteInfo(), initialDaysOut());
    schParameters[0] = surveyDate;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(surveyDate);

    //Add a patient with a base date a year ago - eligible
    calendar.add(Calendar.YEAR, -1);
    createPatientBaseDate("99999-0", "John0", "Doe0", calendar.getTime());

    //Add a patient with a base date 15 months ago - not eligible
    calendar.add(Calendar.MONTH, -3);
    createPatientBaseDate("99999-1", "John1", "Doe1", calendar.getTime()); // Not eligible

    int surveysSent = (Integer) scheduleFollowUps.invoke(scheduler, schParameters);
    assertEquals("Expecting one follow up to have been created", 1, surveysSent);
  }

  // Test sending for 10 years and stopping
  public void testScheduleFollowUps10yrs() throws Exception {
    //List of params to be passed for invocation
    Date surveyDate = DateUtils.getTimestampEnd(getSiteInfo(), initialDaysOut());
    schParameters[0] = surveyDate;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(surveyDate);
    calendar.setTime(surveyDate);
    for (int yr=1; yr < 15; yr++) {
      calendar.add(Calendar.YEAR, -1);
      createPatientBaseDate("88888-" + yr, "Adam" + yr, "West" + yr, calendar.getTime());
    }
    int surveysSent = (Integer) scheduleFollowUps.invoke(scheduler, schParameters);
    assertEquals("Expecting ten follow up to have been created", 10, surveysSent);
  }

  private void createPatientBaseDate(String patientId, String firstName, String lastName, Date date) {
    Patient patient = createPatient(patientId, firstName, lastName);
    addEmailAttribute(patient);
    adminSvc.setPatientAgreesToSurvey(patient);
    PatientAttribute patientAttribute =
        new PatientAttribute(patient.getPatientId(), CFSCustomizer.ATTR_BASE_DATE, dateFormat.format(date));
    adminSvc.addPatientAttribute(patientAttribute);
  }
}

