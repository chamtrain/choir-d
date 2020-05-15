/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientExtendedAttribute;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientDaoTest extends DatabaseTestCase {
  private static final Logger logger = LoggerFactory.getLogger(PatientDaoTest.class);
  private final String patientId = "8888888-8";
  private final String patientId2 = "999999999";
  private PatientAttribute genderAttribute;
  private PatientAttribute ethnicAttribute;
  private PatientExtendedAttribute extendedAttribute;
  private PatientDao patientDao;
  private PatientDao patientDaoWithUser;
  private Patient testPatient0;
  private User user;
  private Utils utils1;

  @Override
  protected void postSetUp() {
    utils1 = new Utils(databaseProvider.get(), getSiteInfo());
    user = utils1.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    patientDaoWithUser = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), user);
    testPatient0 = new Patient(patientId, "John", "Doe", DateUtils.getDaysOutDate(getSiteInfo(), 35 * 365));
    testPatient0 = patientDao.addPatient(testPatient0);
    genderAttribute = new PatientAttribute(testPatient0.getPatientId(), "gender", "Male");
    ethnicAttribute = new PatientAttribute(testPatient0.getPatientId(), "ethnicity", "hispanic");
    extendedAttribute = new PatientExtendedAttribute(testPatient0.getPatientId(), "longvalue", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
    logger.debug("Starting PatientDao tests");
  }

  public void test_getPatient() {
    assertNotNull("Expected to find the patient", testPatient0);
    Patient noPatient = patientDao.getPatient(patientId2);
    assertNull("Expected not to find patient ", noPatient);
  }

  public void test_addPatient() {
    Patient newPatient = new Patient(patientId2, "Jane", "Smith", DateUtils.getDaysOutDate(getSiteInfo(), 53 * 365));
    patientDao.addPatient(newPatient);
    assertNotNull("Expected to find the patient", newPatient);
  }

  public void test_getPatientByToken() {
    ApptRegistration apptRegistration1 = utils1.addInitialRegistration(databaseProvider.get(), patientId,
        DateUtils.getDaysOutDate(getSiteInfo(), 0), "testing@test.stanford.edu", "NPV");
    SurveyRegistration surveyRegistration1 = apptRegistration1.getSurveyReg("Default");
    Patient patient1 = patientDao.getPatientByToken(surveyRegistration1.getToken());
    assertNull("Expected to not find the patient", patient1);
    SurveyRegUtils utils = new SurveyRegUtils(getSiteInfo());
    utils.registerAssessments(databaseProvider.get(), apptRegistration1.getAssessment(), user);
    Patient patient2 = patientDao.getPatientByToken(surveyRegistration1.getToken());
    assertNotNull("Expected to find the patient", patient2);
  }

  public void test_getPatientsByName() {
    ArrayList<Patient> patients = patientDao.getPatientsByName("XYZABC123");
    assertNotNull("Expected to find an array", patients);
    assertEquals("Expected to find 0 patients", 0, patients.size());
    patients = patientDao.getPatientsByName("Doe");
    assertNotNull("Expected to find an array", patients);
    assertEquals("Expected to find 0 patients", 1, patients.size());
  }

  public void test_getPatientsByAttr() {
    PatientAttribute pat1Attribute = new PatientAttribute(testPatient0.getPatientId(), "gender", "Male");
    patientDaoWithUser.insertAttribute(pat1Attribute);
    ArrayList<Patient> patients = patientDao.getPatientsByAttr("ethnicity", "hispanic");
    assertNotNull("Expected to find an array", patients);
    assertEquals("Expected to find 0 patients", 0, patients.size());
    patients = patientDao.getPatientsByAttr("gender", "male");
    assertEquals("Expected to find 1 patients", 1, patients.size());
  }

  public void test_updatePatient() {
    PatientAttribute pat1Attribute = new PatientAttribute(testPatient0.getPatientId(), "gender", "Male");
    patientDaoWithUser.insertAttribute(pat1Attribute);
    testPatient0.setFirstName("Luis");
    testPatient0 = patientDao.updatePatient(testPatient0);
    assertNotNull("Expected to find testpatient", testPatient0);
    assertEquals("Expected the test patients name to be changed ", "Luis", testPatient0.getFirstName());
  }

  public void test_getAttribute() {
    String gender = "gender";
    PatientAttribute patientAttribute = new PatientAttribute(testPatient0.getPatientId(), gender, "Male");
    patientDaoWithUser.insertAttribute(patientAttribute);
    PatientAttribute attribute = patientDao.getAttribute(testPatient0.getPatientId(), gender);
    assertNotNull("Expected to find the attribute", attribute);
    assertEquals("Expected the attribute to be for this patient", testPatient0.getPatientId(), attribute.getPatientId());
    assertEquals("Expected the attribute type to be gender", gender, attribute.getDataName());
    assertEquals("Expected the attribute value to be male", "Male", attribute.getDataValue());

    String video = "orientationVideo";
    PatientAttribute videoAttribute = patientDaoWithUser.getAttribute(testPatient0.getPatientId(), video);
    assertNotNull("Expected the video attribute to be created", videoAttribute);
    assertEquals("Expected the attribute to be for this patient", testPatient0.getPatientId(), videoAttribute.getPatientId());
    assertEquals("Expected the attribute type to be gender", video, videoAttribute.getDataName());
  }


  public void test_getAttributes() {
    patientDaoWithUser.insertAttribute(genderAttribute);
    patientDaoWithUser.insertAttribute(ethnicAttribute);
    ArrayList<PatientAttribute> attributes = patientDao.getAttributes(testPatient0.getPatientId());
    assertNotNull("Expected to find attributes", attributes);
    assertEquals("Expected to find 2 attributes", 2, attributes.size());
  }

  public void test_insertAttribute() {
    patientDaoWithUser.insertAttribute(genderAttribute);
    patientDaoWithUser.insertAttribute(ethnicAttribute);
    ArrayList<PatientAttribute> attributes = patientDao.getAttributes(testPatient0.getPatientId());
    assertNotNull("Expected to find attributes", attributes);
    assertEquals("Expected to find 2 attributes", 2, attributes.size());
  }

  public void test_deleteAttribute() {
    patientDaoWithUser.insertAttribute(genderAttribute);
    ethnicAttribute = patientDaoWithUser.insertAttribute(ethnicAttribute);
    int success = patientDaoWithUser.deleteAttribute(ethnicAttribute);
    assertEquals("Expected 1 returned on delete", 1, success);
    ArrayList<PatientAttribute> attributes = patientDao.getAttributes(testPatient0.getPatientId());
    assertNotNull("Expected attribute array", attributes);
    assertEquals("Expected one attribute", 1, attributes.size());
    PatientAttribute patientAttribute = attributes.get(0);
    assertEquals("Expected gender attribut to be returned", "gender", patientAttribute.getDataName());
  }

  public void test_updateAttribute() {
    patientDaoWithUser.insertAttribute(genderAttribute);
    PatientAttribute patientAttribute = patientDao.getAttribute(genderAttribute.getPatientId(), genderAttribute.getDataName());
    patientAttribute.setDataValue("Female");
    patientDaoWithUser.insertAttribute(patientAttribute);
    PatientAttribute attribute = patientDao.getAttribute(genderAttribute.getPatientId(), genderAttribute.getDataName());
    assertNotNull("Expected to find gender attribute!", attribute);
    assertFalse("Expected the value of gender to be different", attribute.getDataValue().equals(genderAttribute.getDataValue()));
  }

  public void test_getExtendedAttribute() {
    String longValue = "longvalue";
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute);
    PatientExtendedAttribute attribute = patientDao.getExtendedAttribute(testPatient0.getPatientId(), longValue);
    assertNotNull("Expected to find the extended attribute", attribute);
    assertEquals("Expected the extended attribute to be for this patient", testPatient0.getPatientId(), attribute.getPatientId());
    assertEquals("Expected the extended attribute type to be longvalue", longValue, attribute.getDataName());
    assertEquals("Expected the extended attribute value to be lorem ipsum ...", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", attribute.getDataValue());
  }

  public void test_getExtendedAttributes() {
    PatientExtendedAttribute extendedAttribute0 = new PatientExtendedAttribute(testPatient0.getPatientId(), "nameVal0", "value 0");
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute0);
    PatientExtendedAttribute extendedAttribute1 = new PatientExtendedAttribute(testPatient0.getPatientId(), "nameVal0", "new val 0");
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute1);
    PatientExtendedAttribute extendedAttribute2 = new PatientExtendedAttribute(testPatient0.getPatientId(), "nameVal2", "value 2");
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute2);
    PatientExtendedAttribute extendedAttribute3 = new PatientExtendedAttribute(testPatient0.getPatientId(), "nameVal3", "value 3");
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute3);
    ArrayList<PatientExtendedAttribute> attributes = patientDao.getExtendedAttributes(testPatient0.getPatientId());
    assertNotNull("Expected to find extended attributes", attributes);
    assertEquals("Expected to find 4 extended attribute", 3, attributes.size());
  }

  public void test_insertExtendedAttribute() {
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute);
    ArrayList<PatientExtendedAttribute> attributes = patientDao.getExtendedAttributes(testPatient0.getPatientId());
    assertNotNull("Expected to find extended attributes", attributes);
    assertEquals("Expected to find 1 extended attribute", 1, attributes.size());
  }

  public void test_updateExtendedAttribute() {
    patientDaoWithUser.insertExtendedAttribute(extendedAttribute);
    PatientExtendedAttribute updatedAttribute = patientDao.getExtendedAttribute(extendedAttribute.getPatientId(), extendedAttribute.getDataName());
    updatedAttribute.setDataValue("Mauris gravida nisi vitae nibh euismod, aliquam congue odio mollis. Nunc in sapien placerat, lacinia arcu sed, tincidunt lectus. Aenean dictum pellentesque metus at pulvinar. Phasellus ac velit consectetur, interdum ante at, tincidunt sem. Maecenas ac dui cursus ligula luctus luctus vel eget lacus.");
    patientDaoWithUser.insertExtendedAttribute(updatedAttribute);
    PatientExtendedAttribute attribute = patientDao.getExtendedAttribute(extendedAttribute.getPatientId(), extendedAttribute.getDataName());
    assertNotNull("Expected to find extended attribute!", attribute);
    assertFalse("Expected the value of longvalue to be different", attribute.getDataValue().equals(extendedAttribute.getDataValue()));
  }
}