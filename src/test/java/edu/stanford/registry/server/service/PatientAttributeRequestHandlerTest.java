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

import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiPatientCommon;
import edu.stanford.registry.server.service.rest.api.PatientAttributeRequestHandler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.PrivateAccessor;

import java.lang.reflect.Method;
import java.util.Date;

import org.json.JSONObject;
import org.junit.Assert;
import org.restlet.ext.json.JsonRepresentation;

public class PatientAttributeRequestHandlerTest extends DatabaseTestCase {

  public void testGetPatientAttr_MissingPatientAttr() {
    Throwable t = getExcFrom_GetPatientAttribute("10050-3", "height");
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: "+t.getClass().getName()+": "+t.getMessage(),
        t instanceof NotFoundException);
  }
  public void testGetPatientAttr_MissingPatient() {
    Throwable t = getExcFrom_GetPatientAttributes("10050-3");
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: "+t.getClass().getName()+": "+t.getMessage(),
        t instanceof NotFoundException);
  }


  public void testGetPatientAttr_BadIdSyntax() {
    Throwable t = getExcFrom_GetPatientAttribute("bad-patient-id", "bad-attribute");
    Assert.assertNotNull("Expected a NumberFormatException, got no exception", t);
    Assert.assertTrue("Expected a NumberFormatException, got: "+t.getClass().getName()+": "+t.getMessage(),
        t instanceof NumberFormatException);
  }


  public void testGetPatientAttribute() {
    Patient pat = createPatient("10051-1", "Sam", "Smith", 30);
    JSONObject result = call_getPatientAttribute(pat.getPatientId(), "gender");
    Assert.assertNotNull("Expected a JSONObject, not null", result);
  }


  public void testGetPatientAttributes() {
    Patient pat = createPatient("10051-1", "Jane", "Doe", 70);
    JSONObject result = call_getPatientAttributes(pat.getPatientId());
    Assert.assertNotNull("Expected a JSONObject, not null", result);
  }

  public void testUpdatePatientAttr() {
    Patient pat = createPatient("10051-1", "Jane", "Doe", 70);
    PatientAttribute patientAttribute = pat.getAttribute("gender");
    patientAttribute.setDataValue("Female");
    JSONObject result = call_modPatientAttribute(pat, patientAttribute);
    Assert.assertNotNull("Expected a JSONObject, not null", result);
  }

  public void testDeletePatientAttr() {
    Patient pat = createPatient("10051-1", "Jane", "Doe", 70);
    PatientAttribute patientAttribute = pat.getAttribute("gender");
    JSONObject result = call_remPatientAttribute(pat, patientAttribute);
    Assert.assertNotNull("Expected a JSONObject, not null", result);
  }

  public void testDeletePatientAttr_diffPatient() {
    Patient pat1 = createPatient("10051-1", "Jane", "Doe", 70);
    Patient pat2 = createPatient("10050-3", "John", "Smith", 45);
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), makeAuthenticatedUser());
    PatientAttribute pat1Attribute = new PatientAttribute(pat1.getPatientId(), "newattname", "newval");
    pat1Attribute = patientDao.insertAttribute(pat1Attribute);
    PatientAttribute pat2Attribute = new PatientAttribute(pat2.getPatientId(), "newattname", "newval");
    patientDao.insertAttribute(pat2Attribute);

    Throwable t = getExcFrom_DelPatientAttribute(pat2, pat1Attribute);
    Assert.assertNotNull("Expected ApiStatusException, got no exception", t);
    Assert.assertTrue("Expected ApiStatusException, got: "+t.getClass().getName()+": "+t.getMessage(),
        t instanceof ApiStatusException);
  }
  // ======== method-specific utilities

  private Throwable getExcFrom_GetPatientAttribute(String patientId, String dataName) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getPatientAttribute", String.class, String.class);
    Assert.assertNotNull("Could not find the method getPatientAttribute()", method);
    return acc.callMethodGetExc(method,  patientId, dataName);
  }
  private Throwable getExcFrom_GetPatientAttributes(String patientId) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getPatientAttributes", String.class);
    Assert.assertNotNull("Could not find the method getPatientAttributes()", method);
    return acc.callMethodGetExc(method,  patientId);
  }
  private Throwable getExcFrom_DelPatientAttribute(Patient patient, PatientAttribute patientAttribute) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("deletePatientAttribute", String.class, JsonRepresentation.class, String.class);
    Assert.assertNotNull("Could not find the method getPatientAttribute()", method);
    ApiPatientCommonUtils utils = new ApiPatientCommonUtils();
    return acc.callMethodGetExc(method, patient.getPatientId(),
        new JsonRepresentation(utils.makePatientAttributeJson(getSiteInfo(), patientAttribute)), "/patattribute/rem/" + patient.getPatientId());
  }
  private JSONObject call_getPatientAttribute(String patientId, String dataName) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getPatientAttribute", String.class, String.class);
    Assert.assertNotNull("Could not find the method getPatientAttribute()", method);
    Object obj = acc.callMethod(method, patientId, dataName);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a "+obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject)obj;
  }

  private JSONObject call_getPatientAttributes(String patientId) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getPatientAttributes", String.class);
    Assert.assertNotNull("Could not find the method getPatientAttributes()", method);
    Object obj = acc.callMethod(method, patientId);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a "+obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject)obj;
  }

  private JSONObject call_modPatientAttribute(Patient patient, PatientAttribute attribute) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("updatePatientAttribute", String.class, JsonRepresentation.class, String.class);
    Assert.assertNotNull("Could not find the method updatePatientAttribute()", method);
    ApiPatientCommonUtils utils = new ApiPatientCommonUtils();

    Object obj = acc.callMethod(method, patient.getPatientId(),
        new JsonRepresentation(utils.makePatientAttributeJson(getSiteInfo(), attribute)), "/patattribute/mod/" + patient.getPatientId());
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a "+obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject)obj;
  }

  private JSONObject call_remPatientAttribute(Patient patient, PatientAttribute attribute) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientAttributeRequestHandler handler = new PatientAttributeRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientAttributeRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("deletePatientAttribute", String.class, JsonRepresentation.class, String.class);
    Assert.assertNotNull("Could not find the method deletePatientAttribute()", method);
    ApiPatientCommonUtils utils = new ApiPatientCommonUtils();

    Object obj = acc.callMethod(method, patient.getPatientId(),
        new JsonRepresentation(utils.makePatientAttributeJson(getSiteInfo(), attribute)), "/patattribute/rem/" + patient.getPatientId());
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a "+obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject)obj;
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
    Date dob = yearsAgoDob == 0 ? null : DateUtils.getDaysAgoDate(getSiteInfo(), 30 * 365);
    Patient testPatient = new Patient(id, firstName, lastName, dob);
    testPatient = patientDao.addPatient(testPatient);
    PatientAttribute patientAttribute = new PatientAttribute( id, "gender", "Male", PatientAttribute.STRING);
    patientAttribute = patientDao.insertAttribute(patientAttribute);
    testPatient.addAttribute(patientAttribute);
    return testPatient;
  }


  // Creates the service using the user from the cache
  private ClinicServices makeClinicServices() {
    User user = serverContext().userInfo().forName("admin");
    return new ClinicServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

  private User makeAuthenticatedUser() {
    return serverContext().userInfo().forName("admin");
  }

  private class ApiPatientCommonUtils extends ApiPatientCommon {

  }
}
