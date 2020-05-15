/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.service;

import edu.stanford.registry.client.api.ApiObjectFactory;
import edu.stanford.registry.client.api.AssessmentObj;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.rest.api.AssessmentRequestHandler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.PrivateAccessor;

import java.lang.reflect.Method;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class AssessmentRequestHandlerTest extends DatabaseTestCase {
  private final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);
  public void testGetByPatient_MissingPatient() {
    Throwable t = getExcFrom_GetAssessmentsByPatient("10050-3");
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: "+t.getClass().getName()+": "+t.getMessage(),
        t instanceof NotFoundException);
  }


  public void testGetPatient_BadIdSyntax() {
    Throwable t = getExcFrom_GetAssessmentsByPatient("bad-patient-id");
    Assert.assertNotNull("Expected a NumberFormatException, got no exception", t);
    Assert.assertTrue("Expected a NumberFormatException, got: "+t.getClass().getName()+": "+t.getMessage(),
        t instanceof NumberFormatException);
  }


  public void testGetAssessmentById() {
    Patient pat = createPatient("10051-1", "Sam", "Smith", 30);
    AssessmentRegistration registration = createAssessment(pat.getPatientId());
    AssessmentId id =  registration.getAssessmentId();
    JSONObject result = call_getAssessmentById(String.valueOf(id.getId().toString()));
    Assert.assertNotNull("Expected a JSONObject, not null", result);
    AssessmentObj object = AutoBeanCodex.decode(factory, AssessmentObj.class, result.toString()).as();
    Assert.assertEquals(id.getId(), object.getAssessmentId());
  }


  public void testGetAssessmentsByPatient() {
    Patient pat = createPatient("10052-9", "Jane", "Doe", 70);
    AssessmentRegistration registration = createAssessment(pat.getPatientId());
    JSONObject result = call_getAssessmentsByPatient(pat.getPatientId());
    assertTrue(result.has("ASSESSMENTS"));
    Assert.assertTrue("Expected a JSONArray back, got a "
        + result.get("ASSESSMENTS").getClass().getName(), result.get("ASSESSMENTS") instanceof JSONArray);
    JSONArray assessments = result.getJSONArray("ASSESSMENTS");
    boolean found = false;
    for (int inx = 0; inx < assessments.length(); inx++) {
      JSONObject assessmentJSON = (JSONObject) assessments.get(inx);
      AssessmentObj assessmentObj = AutoBeanCodex.decode(factory, AssessmentObj.class, assessmentJSON.toString()).as();
      if (registration.getAssessmentId().getId().longValue() == assessmentObj.getAssessmentId().longValue()) {
        found = true;
      }
    }
    Assert.assertTrue("Expected to find " + registration.getAssessmentId().getId(), found);
  }

  public void testCancelAssessment() {
    Patient pat = createPatient("10052-9", "Jane", "Doe", 70);
    AssessmentRegistration registration = createAssessment(pat.getPatientId());

    JSONObject responsetJSON = call_cancelAssessment(registration.getAssessmentId());
    Assert.assertTrue(responsetJSON.has("success"));
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    ApptRegistration apptRegistration = assessDao.getApptRegistrationByAssessmentId(registration.getAssessmentId());
    Assert.assertEquals(apptRegistration.getRegistrationType(), Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT);

  }

  // ======== method-specific utilities

  private Throwable getExcFrom_GetAssessmentsByPatient(String patientId) {
    ClinicServices clinicSvc = makeClinicServices();
    AssessmentRequestHandler handler = new AssessmentRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<AssessmentRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getAssessmentsByPatient", String.class);
    Assert.assertNotNull("Could not find the method getAsessmentsByPatient()", method);
    return acc.callMethodGetExc(method,  patientId);
  }

  private JSONObject call_getAssessmentById(String id) {
    ClinicServices clinicSvc = makeClinicServices();
    AssessmentRequestHandler handler = new AssessmentRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<AssessmentRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getAssessmentById",  String.class);
    Assert.assertNotNull("Could not find the method getAssessmentById()", method);
    Object obj = acc.callMethod(method, id);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a "+obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject)obj;
  }

  private JSONObject call_getAssessmentsByPatient(String patientId) {
    ClinicServices clinicSvc = makeClinicServices();
    AssessmentRequestHandler handler = new AssessmentRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<AssessmentRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getAssessmentsByPatient", String.class);
    Assert.assertNotNull("Could not find the method getAssessmentsByPatient()", method);
    Object obj = acc.callMethod(method, patientId);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a "+obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject)obj;
  }

  private JSONObject call_cancelAssessment(AssessmentId assessmentId) {
    ClinicServices clinicSvc = makeClinicServices();
    AssessmentRequestHandler handler = new AssessmentRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<AssessmentRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("cancelAssessment", String.class);
    Object obj = acc.callMethod(method, String.valueOf(assessmentId.getId()));
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }
  // ======== generic utilities

  /**
   * Creates a patient with the given id, name and birth date
   * @param id should not be null or empty
   * @param firstName can be null
   * @param lastName can be null
   * @param yearsAgoDob if 0, date is null, else create a date this many years ago
   * @return Patient the patient object that is created
   */
  private Patient createPatient(String id, String firstName, String lastName, int yearsAgoDob) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), makeAuthenticatedUser());
    Date dob = yearsAgoDob == 0 ? null : DateUtils.getDaysAgoDate(30 * 365);
    Patient testPatient = new Patient(id, firstName, lastName, dob);
    testPatient = patientDao.addPatient(testPatient);
    AssessmentRegistration registration = createAssessment(id);
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    assessDao.insertAssessmentRegistration(registration);
    return testPatient;
  }

  private AssessmentRegistration createAssessment(String patientId) {
    Date now = new Date();
    String type = "Initial";
    Long siteId = getSiteInfo().getSiteId();
    AssessmentRegistration registration = new AssessmentRegistration();
    SurveyRegistration surveyRegistration = new SurveyRegistration(siteId, patientId, now, type);
    //surveyRegistration.setToken("1234567890");
    surveyRegistration.setToken(String.valueOf(System.currentTimeMillis()));
    surveyRegistration.setSurveyName("default");
    surveyRegistration.setSurveyOrder(1L);
    registration.setSurveySiteId(siteId);
    registration.addSurveyReg(surveyRegistration);
    registration.setPatientId(patientId);
    registration.setAssessmentDt(now);
    registration.setAssessmentType(type);
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());

    registration = assessDao.insertAssessmentRegistration(registration);
    surveyRegistration.setAssessmentRegId(registration.getAssessmentRegId());
    assessDao.insertSurveyRegistration(surveyRegistration);

    ApptRegistration apptRegistration = new ApptRegistration(siteId, patientId, now, "", type, "a", "NEW" );
    apptRegistration.setAssessmentRegId(registration.getAssessmentRegId());
    assessDao.insertApptRegistration(apptRegistration);

    return registration;
  }

  // Creates the service using the user from the cache
  private ClinicServices makeClinicServices() {
    User user = serverContext().userInfo().forName("admin");
    return new ClinicServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

  private User makeAuthenticatedUser() {
    return serverContext().userInfo().forName("admin");
  }
}
