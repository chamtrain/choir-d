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
package edu.stanford.registry.server.database;

import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatStudyDaoTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(PatStudyDaoTest.class);
  private final String BODYMAP = "bodymap";
  private final String patientId = "8888888-8";
  private PatStudyDao patStudyDao;
  private Patient testPatient0;
  private User user;
  private Study bodymapStudy;

  private SurveyRegistration surveyRegistration1, surveyRegistration2;
  ApptRegistration apptRegistration1, apptRegistration2;
  private Date today, days7Out;
  private SurveySystDao ssDao;
  @Override
  protected void postSetUp() {
    Utils utils1 = new Utils(databaseProvider.get(), getSiteInfo());
    patStudyDao = new PatStudyDao(databaseProvider.get(), getSiteInfo());
    ssDao = new SurveySystDao(databaseProvider.get());
    user = utils1.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    bodymapStudy =  ssDao.getStudy(ssDao.getSurveySystem("Local").getSurveySystemId(), BODYMAP);
    days7Out =  DateUtils.getDaysOutDate(getSiteInfo(), 7);
    today = DateUtils.getDaysOutDate(getSiteInfo(), 0);
    DateUtils.getDaysOutDate(getSiteInfo(), 7 );
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), user);
    testPatient0 = new Patient(patientId, "John", "Doe", DateUtils.getDaysOutDate(getSiteInfo(), 35 * 365));
    testPatient0 = patientDao.addPatient(testPatient0);
    String emailAddress = "testing@test.stanford.edu";
    ApptRegistration apptRegistration1 = utils1.addInitialRegistration(databaseProvider.get(), patientId, today, emailAddress, "NPV");
    ApptRegistration apptRegistration2 = utils1.addInitialRegistration(databaseProvider.get(),patientId, days7Out , emailAddress, "RPV30");
    SurveyRegUtils utils = new SurveyRegUtils(getSiteInfo());
    utils.registerAssessments(databaseProvider.get(), apptRegistration1.getAssessment(), user);
    utils.registerAssessments(databaseProvider.get(), apptRegistration2.getAssessment(), user);
    surveyRegistration1 = apptRegistration1.getSurveyReg("Default");
    surveyRegistration2 = apptRegistration2.getSurveyReg("Default");
    logger.debug("Starting PatStudyDao tests");
  }

  public void test_getPatientStudyByStudyAndToken() {
    PatientStudy patientStudy = patStudyDao.getPatientStudy(testPatient0, bodymapStudy, new Token(surveyRegistration1.getToken()));
    assertNotNull("Expected to find the patientStudy", patientStudy);
    assertNull("Expecting null study contents", patientStudy.getContents());
  }

  public void test_getPatientStudyByExtendedData() {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    ArrayList<PatientStudyExtendedData> extendedData = patStudyDao.getPatientStudyExtendedDataByPatientId(patientId, DateUtils.getDaysOutDate(getSiteInfo(), 2));
    PatientStudy patientStudy = patStudyDao.getPatientStudy(extendedData.get(0), false);
    assertNotNull(patientStudy);
  }

  public void test_getPatientStudyByStudyCodeAndToken() {
      updatePatientStudy(BODYMAP,surveyRegistration1.getToken() );
      PatientStudy patientStudy = patStudyDao.getPatientStudy(testPatient0.getPatientId(), bodymapStudy.getStudyCode(), surveyRegistration1.getToken(), true);
      assertNotNull("Expected to find the patientStudy", patientStudy);
      assertNotNull("Expecting study contents", patientStudy.getContents());
  }

  public void test_getPatientStudyExtendedDataByPatientAndStudy() {
    updatePatientStudy("BODYMAP", surveyRegistration1.getToken());
    ArrayList<PatientStudyExtendedData> extendedData = patStudyDao.getPatientStudyExtendedDataByPatientAndStudy(testPatient0.getPatientId(), "BODYMAP");
    assertNotNull(extendedData);
    assertEquals(1, extendedData.size());
  }

  public void test_getPatientStudyExtendedDataByPatientAndDate() {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    updatePatientStudy(BODYMAP, surveyRegistration2.getToken() );
    // gets completed ones
    ArrayList<PatientStudyExtendedData> extendedData = patStudyDao.getPatientStudyExtendedDataByPatientId(patientId, today);
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > 0);
    int sizeToday = extendedData.size();
    extendedData = patStudyDao.getPatientStudyExtendedDataByPatientId(patientId, today , days7Out );
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > sizeToday);
  }

  public void test_getPatientStudyExtendedDataByPatientId() {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    // gets completed ones
    ArrayList<PatientStudyExtendedData> patientStudies = patStudyDao.getPatientStudyExtendedDataByPatientId(testPatient0.getPatientId());
    assertNotNull(patientStudies);
    assertTrue("expecting several to be returned", patientStudies.size() > 0);
  }

  public void test_getPatientStudyExtendedDataByToken () {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    // gets completed ones
    ArrayList<PatientStudyExtendedData> extendedData = patStudyDao.getPatientStudyExtendedDataByToken(surveyRegistration1.getToken());
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > 0);
  }

  public void test_getPatientStudyExtendedDataByTokenSurveySystem () {
    // gets first non-completed one
    PatientStudyExtendedData extendedData = patStudyDao.getPatientStudyExtendedDataByToken(new Token(surveyRegistration2.getToken()), user);
    assertNotNull(extendedData);

    extendedData = patStudyDao.getPatientStudyExtendedDataByToken(new Token(surveyRegistration2.getToken()), ssDao.getSurveySystem("Local").getSurveySystemName(), user);
    assertNotNull(extendedData);
  }

  public void test_getPatientStudyDataByNameLike () {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    // gets completed ones
    ArrayList<PatientStudyExtendedData> extendedData =  patStudyDao.getPatientStudyDataByNameLike("Do");
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > 0);
    Date twoWeeksBack = DateUtils.getDaysAgoDate(getSiteInfo(), 14 );
    Date oneWeeksBack = DateUtils.getDaysAgoDate(getSiteInfo(), 7 );
    extendedData = patStudyDao.getPatientStudyDataByNameLike("Do", twoWeeksBack, oneWeeksBack);
    assertNotNull(extendedData);
    assertEquals("expecting none to be returned", extendedData.size(), 0);
    extendedData = patStudyDao.getPatientStudyDataByNameLike("Do", oneWeeksBack, DateUtils.getDaysOutDate(getSiteInfo(), 7 ));
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > 0);
  }

  public void test_getPatientStudyDataBySurveyRegIdAndStudyDescription () {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    ArrayList<PatientStudyExtendedData> extendedData = patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(surveyRegistration1.getSurveyRegId(), BODYMAP);
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > 0);
  }

  public void test_getPatientStudyDataBySurveyRegId() {
    updatePatientStudy(BODYMAP, surveyRegistration1.getToken());
    ArrayList<PatientStudyExtendedData> extendedData = patStudyDao.getPatientStudyDataBySurveyRegId(surveyRegistration1.getSurveyRegId());
    assertNotNull(extendedData);
    assertTrue("expecting several to be returned", extendedData.size() > 0);
  }

  public void test_getProgress () {
    double progress = patStudyDao.getProgress(surveyRegistration2.getToken());
    assertEquals("Expecting no progress", 0, progress, 0.0);
    updatePatientStudy("names", surveyRegistration2.getToken());
    updatePatientStudy(BODYMAP, surveyRegistration2.getToken());
    progress = patStudyDao.getProgress(surveyRegistration2.getToken());
    assertTrue("expecting some progress", progress > 0);
  }

  public void test_insertPatientStudy () {
    PatientStudy patientStudy = doInsertPatientStudy();
    assertNotNull("Expected a patient study", patientStudy);
  }

  public void test_deletePatientStudy() {
    PatientStudy patientStudy = doInsertPatientStudy();
    PatientStudy foundPatStudy = patStudyDao.getPatientStudy(patientStudy.getPatientId(), patientStudy.getStudyCode(), patientStudy.getToken(), false);
    assertNotNull("Expected a patient study", foundPatStudy);

    patStudyDao.deletePatientStudy(foundPatStudy.getToken());
    foundPatStudy = patStudyDao.getPatientStudy(patientStudy.getPatientId(), patientStudy.getStudyCode(), patientStudy.getToken(), false);
    assertNull("Expected no study found", foundPatStudy);
  }

  private void updatePatientStudy(String studyName, String token) {
    PatientStudy patientStudy = patStudyDao.getPatientStudy(testPatient0, bodymapStudy, new Token(token));
    patStudyDao.setPatientStudyContents(patientStudy, "<Form></Form>");
  }
  private PatientStudy doInsertPatientStudy() {
    Study study = createNewStudy("newStudy", "New Study");

    PatientStudy patStudy = new PatientStudy(getSiteInfo().getSiteId());
    patStudy.setExternalReferenceId("");
    patStudy.setMetaVersion(0);
    patStudy.setPatientId(surveyRegistration1.getPatientId());
    patStudy.setStudyCode(study.getStudyCode());
    patStudy.setSurveySystemId(study.getSurveySystemId());
    patStudy.setToken(surveyRegistration1.getToken());
    patStudy.setOrderNumber(2);

    return patStudyDao.insertPatientStudy(patStudy);
  }

  private Study createNewStudy(String name, String title) {
    Study newStudy = new Study();
    newStudy.setStudyDescription(name);
    newStudy.setSurveySystemId(bodymapStudy.getSurveySystemId());
    newStudy.setTitle(title);
    return ssDao.insertStudy(newStudy);
  }
}