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

import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.server.service.AdministrativeServicesImpl;
import edu.stanford.registry.server.shc.pedpain.PedPainCustomizer;
import edu.stanford.registry.server.shc.pedpain.PedPainSurveyScheduler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.User;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class PedPainSurveySchedulerTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(PedPainSurveySchedulerTest.class);
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
  private Date dob;

  private Supplier<Database> databaseProvider;

  private AdministrativeServices adminSvc;

  //Test class setup
  private PedPainSurveyScheduler scheduler;
  private Method scheduleFollowUps;
  private static final String METHOD_NAME = "scheduleFollowUpSurveys";
  private Object[] parameters;

  @Override
  protected void postSetUp() throws Exception {
    databaseProvider = getDatabaseProvider();
    final User user = new Utils(databaseProvider.get(), getSiteInfo()).getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    adminSvc = getAdminService(user);

    // Reference to Survey scheduler class
    scheduler = getSurveyScheduler();
    // Define the method parameters and set up the method we're testing
    Class[] parameterTypes = new Class[2];
    parameterTypes[0] = Database.class;
    parameterTypes[1] = Date.class;
    scheduleFollowUps = scheduler.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
    scheduleFollowUps.setAccessible(true);

    //List of params to be passed for invocation
    parameters = new Object[2];
    dob = DateUtils.getDaysAgoDate(getSiteInfo(), 12 * 365);
  }

  private PedPainSurveyScheduler getSurveyScheduler() {
    return new PedPainSurveyScheduler(getSiteInfo());
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

  private PatientAttribute addEmailAttribute(Patient patient) {
    String id = patient.getPatientId();
    String emailAddress = "testing@test.stanford.edu";
    PatientAttribute patAttribute =
        new PatientAttribute(id, Constants.ATTRIBUTE_SURVEYEMAIL, emailAddress, PatientAttribute.STRING);
    return adminSvc.addPatientAttribute(patAttribute);
  }

  private Date initialDaysOut() {
    String dayString = getSiteInfo().getProperty("appointment.initialemail.daysout");
    int days = Integer.parseInt(dayString);
    return new Date(DateUtils.getDaysOutDate(getSiteInfo(), days).getTime());
  }

  public void testScheduleFollowUps() throws Exception {
    Date surveyDt = initialDaysOut();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(surveyDt);
    // Create an eligible patient
    calendar.add(Calendar.MONTH, -6);
    createPatientBaseDate("99999-0", "John0", "Wayne0", PedPainCustomizer.ATTR_INITIAL,  calendar.getTime());

    // And a not eligible patient
    calendar.add(Calendar.MONTH, -4);
    createPatientBaseDate("99999-1", "John1", "Wayne1", PedPainCustomizer.ATTR_PREP_END, calendar.getTime());

    //init the two params
    parameters[0] = databaseProvider.get();
    parameters[1] = DateUtils.getTimestampEnd(getSiteInfo(), surveyDt);
    int count = (Integer) scheduleFollowUps.invoke(scheduler, parameters);
    logger.debug("calling server with surveyDt {}", surveyDt);
    assertEquals("Expecting only 1 to be created", 1, count);
    count = (Integer) scheduleFollowUps.invoke(scheduler, parameters);
    assertEquals(0, count); // But if we run again it doesn't create another duplicate survey for the eligible patient
  }

  public void testScheduleFollowUpsAll() throws Exception {
    Date surveyDt = initialDaysOut();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(surveyDt);

    calendar.add(Calendar.MONTH, -3);

    // test patient with both initial and PRepEnd attributes
    Patient patient0 = createPatientBaseDate("99999-3", "John3m", "Wayne3", PedPainCustomizer.ATTR_INITIAL, calendar.getTime());
    calendar.setTime(DateUtils.getDaysFromDate(getSiteInfo(), calendar.getTime(), -10));
    addAttribute(patient0, PedPainCustomizer.ATTR_PREP_END, calendar.getTime());

    // test patients for 6, 9 months, 1, 2 & 3 years
    calendar.add(Calendar.MONTH, -3);
    createPatientBaseDate("99999-6", "John6", "Wayne6m", PedPainCustomizer.ATTR_INITIAL, calendar.getTime());

    calendar.add(Calendar.MONTH, -3);
    createPatientBaseDate("99999-9", "John9", "Wayne9m", PedPainCustomizer.ATTR_INITIAL, calendar.getTime());

    calendar.add(Calendar.MONTH, -3);
    createPatientBaseDate("77777-1", "John1", "Wayne1y", PedPainCustomizer.ATTR_PREP_END, calendar.getTime());

    calendar.add(Calendar.YEAR, -1);
    createPatientBaseDate("77777-2", "John1", "Wayne1y", PedPainCustomizer.ATTR_PREP_END, calendar.getTime());

    calendar.add(Calendar.YEAR, -1);
    createPatientBaseDate("77777-3", "John1", "Wayne1y", PedPainCustomizer.ATTR_PREP_END, calendar.getTime());

    //init the two params
    parameters[0] = databaseProvider.get();
    parameters[1] = DateUtils.getTimestampEnd(getSiteInfo(), surveyDt);
    int count = (Integer) scheduleFollowUps.invoke(scheduler, parameters);
    logger.debug("calling server with surveyDt {}", surveyDt);
    assertEquals("Expecting 6 follow ups to be created", 6, count); //  This is checked against local process.xml


    PatientRegistrationSearch searchOptions = new PatientRegistrationSearch();
    searchOptions.setOption(PatientRegistrationSearch.CONSENTED);
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    List<PatientRegistration> patientRegistrations = assessDao.getPatientRegistrations(
        DateUtils.getDaysFromDate(getSiteInfo(),surveyDt, -1), DateUtils.getDaysFromDate(getSiteInfo(), surveyDt, 1),
        searchOptions);
                                            
    assertNotNull("Expecting a list of patientRegistrations", patientRegistrations);
    assertEquals("Expected a list.size of 6 ", 6, patientRegistrations.size());

    int count3mn = 0;
    int count1yr = 0;
    for (PatientRegistration patientRegistration : patientRegistrations) {
      logger.info(patientRegistration.getPatient().getPatientId() + " has visit type " + patientRegistration.getVisitType());
      if ("survey 3mn".equals(patientRegistration.getVisitType())) {
        count3mn++;
      } else if ("survey 1yr".equals(patientRegistration.getVisitType())) {
        count1yr++;
      }
    }
    assertEquals(count3mn,1);
    assertEquals(count1yr,1);
  }

  private Patient createPatientBaseDate(String patientId, String firstName, String lastName, String attributeName, Date attributeDate) {
    Patient patient = createPatient(patientId, firstName, lastName);
    patient.addAttribute(addEmailAttribute(patient));
    adminSvc.setPatientAgreesToSurvey(patient);
    patient.addAttribute(addAttribute(patient, attributeName, attributeDate));
    return patient;
  }

  private PatientAttribute addAttribute(Patient patient, String attributeName, Date attributeDate) {
    PatientAttribute patientAttribute =
        new PatientAttribute(patient.getPatientId(), attributeName, dateFormat.format(attributeDate));
    return adminSvc.addPatientAttribute(patientAttribute);
  }
}

