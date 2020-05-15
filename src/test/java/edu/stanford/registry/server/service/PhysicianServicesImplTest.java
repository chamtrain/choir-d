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
package edu.stanford.registry.server.service;

import edu.stanford.registry.client.api.ApiObjectFactory;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Assert;

import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PhysicianServicesImplTest extends DatabaseTestCase {
  private final String testPatientId = "8888888-8";
  private final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);

  User user;
  PhysicianServices services;

  @Override
  protected void postSetUp() {
    user = serverContext().userInfo().forName("admin");
    services = makePhysicianServices();
    createReportType();
    createPatient(testPatientId, "Harry","Hoodini", 20);
  }

  public void testGetProcessNames() {
    ArrayList<String> processNames = services.getProcessNames(testPatientId);
    Assert.assertNotNull("Expecting an arrayList", processNames);
    Assert.assertTrue("Expecting process name list ", processNames.size() > 0);
  }

  public void testCreateSurvey() {
    ArrayList<String> processNames = services.getProcessNames(testPatientId);
    String token = services.createSurvey(testPatientId, processNames.get(0));
    Assert.assertNotNull("Expected a token, not null", token);
  }

  public void testGetSurveyJson() {
    // create the registration
    Utils utils1 = new Utils(databaseProvider.get(), getSiteInfo());
    ApptRegistration registration = utils1.getRegistration(testPatientId, DateUtils.getDaysOutDate(getSiteInfo(), 7),
        "test@test.tst", "QST Measurement", "");
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    SurveyRegUtils regUtils = new SurveyRegUtils(getSiteInfo());
    registration = regUtils.createRegistration(assessDao, registration);

    // Put json in the patient study
    SurveyRegistration surveyRegistration = registration.getSurveyRegList().get(0);
    regUtils.registerAssessments(databaseProvider.get(), surveyRegistration, user);
    PatStudyDao patStudyDao = new PatStudyDao(databaseProvider.get(), getSiteInfo());
    ArrayList<PatientStudy> studies = patStudyDao.getPatientStudiesByToken(surveyRegistration.getToken(), false);
    if (studies.size() < 1) {
      registration.setSurveyType("PhysicianInput.0001");
      assessDao.updateApptRegistration(registration);
      surveyRegistration.setSurveyType("PhysicianInput.0001");
      assessDao.updateSurveyRegistration(surveyRegistration);
      regUtils.registerAssessments(databaseProvider.get(), surveyRegistration, user);
      studies = patStudyDao.getPatientStudiesByToken(surveyRegistration.getToken(), false);
    }
    patStudyDao.setPatientStudyContents(studies.get(0),
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<Form Class=\"surveyInlineBordered\" DateFinished=\"\" DateStarted=\"\" Description=\"QST Measurement\" questionsPerPage=\"6\">  "
            + "<Items class=\"surveyInline\">    "
            + "  <Item Class=\"registrySurvey\" ItemResponse=\"1\" ItemScore=\"0\" Order=\"1\" RequiredMax=\"3\" RequiredMin=\"3\" TimeFinished=\"1495843904552\" required=\"true\">   "
            + "   <Description>Right trapezius</Description> "
            + "   <Responses>        "
            + "     <Response Align=\"horizontal\" Order=\"1\" StyleName=\"physicianSurvey\" Type=\"textboxset\" required=\"true\">"
            + "       <ref>TRAPEZIUSRIGHT</ref><item><label>1</label><value>0.5</value></item><item><label>2</label><value>0.5</value></item><item><label>3</label><value>0.5</value></item>"
            + "     </Response>"
            + "   </Responses>   "
            + "  </Item>  "
            + "  <Item ItemResponse=\"1\" ItemScore=\"0\" Order=\"2\" RequiredMax=\"3\" RequiredMin=\"3\" TimeFinished=\"1495843904552\" required=\"true\"> "
            + "   <Description>Left Trapezius</Description> "
            + "   <Responses>        "
            + "     <Response Align=\"horizontal\" Order=\"1\" StyleName=\"physicianSurvey\" Type=\"textboxset\" required=\"true\">"
            + "        <ref>TRAPEZIUSLEFT</ref><item><label>1</label><value>0.5</value></item><item><label>2</label><value>0.5</value></item><item><label>3</label><value>0.5</value></item>"
            + "   </Response>     "
            + "   </Responses>"
            + "  </Item>"
            + "</Items></Form>");

    writeSurveyToken(surveyRegistration.getToken());
    // test the method
    String json = services.getSurveyJson(registration.getApptId());
    Assert.assertNotNull(json);
  }

  public void testResultType() {
    AssessDao assessDao = new AssessDao(databaseProvider.get(), getSiteInfo());
    PatientResultType resultType = assessDao.getPatientResultType("PARPTJSONTEST");
    Assert.assertNotNull(resultType);
  }

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
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Date dob = yearsAgoDob == 0 ? null : DateUtils.getDaysAgoDate(getSiteInfo(),30 * 365);
    Patient testPatient = new Patient(id, firstName, lastName, dob);
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }

  private PhysicianServices makePhysicianServices() {
    User user = serverContext().userInfo().forName("admin");
    return new PhysicianServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }
  private void createReportType () {
    String sql ="INSERT into patient_result_type ( patient_res_typ_id, survey_site_id, result_name, result_title )"
        + "values ( ?, ?, ?, ?)";
    databaseProvider.get().toInsert(sql).argInteger(999).argLong(getSiteInfo().getSiteId()).argString("PARPTJSONTEST").argString(" Title ")
        .insert();
  }

  private void writeSurveyToken(String token) {
    String sql = "INSERT into survey_token (survey_token_id, survey_site_id, survey_token, is_complete, last_session_number, last_step_number)"
        + " values  (?, ?, ?, ?, ?, ?)";
    databaseProvider.get().toInsert(sql).argLong(9999999l).argLong(getSiteInfo().getSiteId())
        .argString(token).argString("Y").argInteger(1).argInteger(2).insert();
  }
}
