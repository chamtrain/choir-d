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

import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.SurveyRequestHandler;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.PrivateAccessor;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyToken;

import java.lang.reflect.Method;
import java.util.Date;

import org.json.JSONObject;
import org.junit.Assert;

public class SurveyRequestHandlerTest extends DatabaseTestCase {

  public void testGetSurveyByToken_MissingToken() {
    Throwable t = getExcFrom_GetSurveyByToken("1234");
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NotFoundException);
  }

  public void testGetSurveyByToken_invalid() {
    Throwable t = getExcFrom_GetSurveyByToken("<b>1234");
    Assert.assertNotNull("Expected an ApiStatusException, got no exception", t);
    Assert.assertTrue("Expected ApiStatusException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof ApiStatusException);
  }

  public void testGetSurveyByToken() {
    Patient pat = createPatient("10051-1", "Sam", "Smith", 30);
    AssessmentRegistration registration = createAssessment(pat.getPatientId());
    SurveyRegistration surveyRegistration = registration.getSurveyRegList().get(0);
    JSONObject result = call_getSurveyByToken(surveyRegistration.getToken());
    Assert.assertNotNull("Expected a JSONObject, not null", result);
  }


  // ======== method-specific utilities

  private Throwable getExcFrom_GetSurveyByToken(String token) {
    ClinicServices clinicSvc = makeClinicServices();
    SurveyRequestHandler handler = new SurveyRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<SurveyRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getSurveyByToken", String.class);
    Assert.assertNotNull("Could not find the method getSurveyByToken()", method);
    return acc.callMethodGetExc(method, token);
  }

  private JSONObject call_getSurveyByToken(String id) {
    ClinicServices clinicSvc = makeClinicServices();
    SurveyRequestHandler handler = new SurveyRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<SurveyRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getSurveyByToken", String.class);
    Assert.assertNotNull("Could not find the method getSurveyByToken()", method);
    Object obj = acc.callMethod(method, id);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }

  // generic utilities

  /**
   * Creates a patient with the given id, name and birth date
   *
   * @param id          should not be null or empty
   * @param firstName   can be null
   * @param lastName    can be null
   * @param yearsAgoDob if 0, date is null, else create a date this many years ago
   * @return Patient the patient object that is created
   */
  private Patient createPatient(String id, String firstName, String lastName, int yearsAgoDob) {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), makeAuthenticatedUser());
    Date dob = yearsAgoDob == 0 ? null : DateUtils.getDaysAgoDate(30 * 365);
    Patient testPatient = new Patient(id, firstName, lastName, dob);
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  private AssessmentRegistration createAssessment(String patientId) {
    Date now = new Date();
    String type = "Initial";

    Long siteId = getSiteInfo().getSiteId();
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());

    /*
     * Create an Assessment registration
     */
    AssessmentRegistration registration = new AssessmentRegistration();
    registration.setSurveySiteId(siteId);
    registration.setPatientId(patientId);
    registration.setAssessmentDt(now);
    registration.setAssessmentType(type);
    registration.setDtCreated(now);
    registration = assessDao.insertAssessmentRegistration(registration);

    SurveyRegistration surveyRegistration = createSurveyRegistration(assessDao, registration, type);
    /*
     * Add the Survey registration to the Assessment registration
     */
    registration.addSurveyReg(surveyRegistration);
    assessDao.updateAssessmentRegistration(registration);
    createApptRegistration(assessDao, registration, type);
    createPatientStudy(surveyRegistration);
    createSurveyToken(surveyRegistration);
    return registration;
  }
  /**
   * Create a Survey registration
   */
  private SurveyRegistration createSurveyRegistration(AssessDao assessDao, AssessmentRegistration registration, String type) {

    String token = "1234567890";
    SurveyRegistration surveyRegistration = new SurveyRegistration(registration.getSurveySiteId(), registration.getPatientId(),
        registration.getAssessmentDt(), type);
    surveyRegistration.setToken(token);
    surveyRegistration.setSurveyName("default");
    surveyRegistration.setSurveyOrder(1L);
    surveyRegistration.setAssessmentRegId(registration.getAssessmentRegId());
    surveyRegistration = assessDao.insertSurveyRegistration(surveyRegistration);

    return surveyRegistration;
  }

  /**
   * Create the Appointment registration
   */
  private void createApptRegistration(AssessDao assessDao, AssessmentRegistration registration, String type) {
    ApptRegistration apptRegistration = new ApptRegistration(registration.getSurveySiteId(), registration.getPatientId(),
        registration.getAssessmentDt(), "", type, "a", "NEW");
    apptRegistration.setAssessmentRegId(registration.getAssessmentRegId());
    assessDao.insertApptRegistration(apptRegistration);

    SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(getSiteInfo()).getSurveyServiceImpl("Local");
    if (surveyService == null) {
      throw new ServiceUnavailableException("No survey service found for type: " + "Local");
    }
  }
  /**
   * Create the Patient study
   */
  private void createPatientStudy(SurveyRegistration surveyRegistration) {
    SurveySystDao ssDao = new SurveySystDao(databaseProvider.get());
    SurveySystem localSystem = ssDao.getSurveySystem("Local");
    Study study = ssDao.getStudy(localSystem.getSurveySystemId(), "names");
    PatStudyDao patStudyDao = new PatStudyDao(databaseProvider.get(), getSiteInfo());
    Token tok = new Token(surveyRegistration.getToken());
    PatientStudy patStudy = new PatientStudy(surveyRegistration.getSurveySiteId());
    patStudy.setExternalReferenceId(""); // the last question answered
    patStudy.setMetaVersion(0);
    patStudy.setPatientId(surveyRegistration.getPatientId());
    patStudy.setStudyCode(study.getStudyCode());
    patStudy.setSurveySystemId(study.getSurveySystemId());
    patStudy.setToken(tok.getToken());
    patStudy.setOrderNumber(1);
    patStudy.setContents("<Form></Form>");
    patStudy.setSurveyRegId(surveyRegistration.getSurveyRegId());
    patStudy.setSurveySiteId(getSiteInfo().getSiteId());
    patStudy.setDtChanged(surveyRegistration.getDtChanged());
    patStudy.setDtCreated(surveyRegistration.getDtCreated());
    patStudyDao.insertPatientStudy(patStudy);
  }
  /**
   * Create the Survey token
   */
  private void createSurveyToken(SurveyRegistration surveyRegistration) {
    SurveyDao dao = new SurveyDao(getDatabaseProvider());
    SurveyToken st = new SurveyToken();
    st.setSurveySiteId(surveyRegistration.getSurveySiteId());
    st.setSessionToken("session123");
    st.setResumeToken("resume456");
    st.setLastStepNumber(1L);
    st.setSurveyToken(surveyRegistration.getToken());
    st.setComplete(true);
    dao.createSurveyToken(st);
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
