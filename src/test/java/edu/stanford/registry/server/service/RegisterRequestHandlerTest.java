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
package edu.stanford.registry.server.service;

import edu.stanford.registry.client.api.ApiObjectFactory;
import edu.stanford.registry.client.api.PatientAttributeObj;
import edu.stanford.registry.client.api.PatientDeclineObj;
import edu.stanford.registry.client.api.PatientObj;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.rest.api.ApiPatientCommon;
import edu.stanford.registry.server.service.rest.api.PatientRequestHandler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.PrivateAccessor;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class RegisterRequestHandlerTest extends DatabaseTestCase {
  private final String testPatientId = "10051-1";
  private final String testPatientId2 = "10050-3";
  private final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);

  public void testGetPatient_MissingPatient() {
    Throwable t = getExcFrom_GetPatientById(testPatientId2);
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NotFoundException);
  }


  public void testGetPatient_BadIdSyntax() {
    Throwable t = getExcFrom_GetPatientById("bad-patient-id");
    Assert.assertNotNull("Expected a NumberFormatException, got no exception", t);
    Assert.assertTrue("Expected a NumberFormatException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NumberFormatException);
  }


  public void testGetPatient() {
    Patient pat = createPatient(testPatientId, "Sam", "Smith", 30);
    JSONObject result = call_GetPatientById(pat.getPatientId());
    Assert.assertNotNull("Expected a JSONObject, not null", result);
    String patientId = (String) result.get("patientId");
    Assert.assertEquals(patientId, testPatientId);
  }


  public void testGetPatient_NullNamesAndDob() {
    Patient pat = createPatient(testPatientId, null, null, 0);
    JSONObject result = call_GetPatientById(pat.getPatientId());
    Assert.assertNotNull("Expected a JSONObject, not null", result);
    String patientId = (String) result.get("patientId");
    Assert.assertEquals(patientId, testPatientId);
  }

  public void testUpdatePatient() {

    createPatient(testPatientId2, "A", "Patient", 0);
    Date dob = DateUtils.getDaysAgoDate(getSiteInfo(),30 * 365);
    Patient testPatient = new Patient(testPatientId2, "T", "Patient", dob);
    PatientAttribute attr = new PatientAttribute(testPatientId2, Constants.ATTRIBUTE_SURVEYEMAIL_ALT, "test@stanford.edu");
    testPatient.addAttribute(attr);
    ApiPatientUtil apiPatient = new ApiPatientUtil();
    JSONObject patientJson = apiPatient.makePatientJson(getSiteInfo(), testPatient);
    JSONObject result = call_UpdatePatient(patientJson);

    Assert.assertNotNull("Expected a JSONObject, not null", result);
    String patientId = (String) result.get("patientId");
    Assert.assertEquals(patientId, testPatientId2);

    //JSONObject newResult = call_GetPatientById(patientId);
    PatientObj patientObj = AutoBeanCodex.decode(factory, PatientObj.class, result.toString()).as();

    Assert.assertEquals(patientObj.getFirstName(), "T");
    List<PatientAttributeObj> attributes = patientObj.getAttributes();
    PatientAttributeObj email = getAttribute(attributes, Constants.ATTRIBUTE_SURVEYEMAIL_ALT);
    Assert.assertNotNull(email);
    Assert.assertEquals(email.getValue(), "test@stanford.edu");

    testPatient.setPatientId("bad-patient-id");
    Throwable t = getExcFrom_ObjectMethod("updatePatient", apiPatient.makePatientJson(getSiteInfo(), testPatient));
    Assert.assertNotNull("Expected a NumberFormatException, got no exception", t);
    Assert.assertTrue("Expected a NumberFormatException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NumberFormatException);
  }

  public void testRegisterPatient() {
    createPatient(testPatientId, null, null, 0);
    Date dob = DateUtils.getDaysAgoDate(getSiteInfo(),30 * 365);
    Patient testPatient = new Patient(testPatientId, null, null, dob);
    ApiPatientUtil apiPatient = new ApiPatientUtil();
    JSONObject patientJson = apiPatient.makePatientJson(getSiteInfo(), testPatient);
    JSONObject result = call_patientMethod("registerPatient", patientJson);
    Assert.assertNotNull("Expected a JSONObject, not null", result);
    String patientId = (String) result.get("patientId");
    Assert.assertEquals(patientId, testPatientId);
    PatientObj patientObj = AutoBeanCodex.decode(factory, PatientObj.class, result.toString()).as();
    List<PatientAttributeObj> attributes = patientObj.getAttributes();
    PatientAttributeObj participates = getAttribute(attributes, Constants.ATTRIBUTE_PARTICIPATES);
    Assert.assertNotNull(participates);
    Assert.assertEquals("y", participates.getValue());

    testPatient.setPatientId("bad-patient-id");
    Throwable t = getExcFrom_ObjectMethod("registerPatient", apiPatient.makePatientJson(getSiteInfo(), testPatient));
    Assert.assertNotNull("Expected a NumberFormatException, got no exception", t);
    Assert.assertTrue("Expected a NumberFormatException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NumberFormatException);
  }

  public void testDeclinePatient() {
    createPatient(testPatientId, null, null, 0);

    JSONObject patientDeclineJson = makePatientDeclineObject(testPatientId, DeclineReason.provider, null);
    JSONObject result = call_patientMethod("declinePatient", patientDeclineJson);
    Assert.assertNotNull("Expected a JSONObject, not null", result);
    String patientId = (String) result.get("patientId");
    Assert.assertEquals(patientId, testPatientId);
    PatientObj patientObj = AutoBeanCodex.decode(factory, PatientObj.class, result.toString()).as();
    List<PatientAttributeObj> attributes = patientObj.getAttributes();
    PatientAttributeObj participates = getAttribute(attributes, Constants.ATTRIBUTE_PARTICIPATES);
    Assert.assertNotNull(participates);
    Assert.assertEquals("n", participates.getValue());

    createPatient(testPatientId2, "F", "N", 20);
    patientDeclineJson = makePatientDeclineObject(testPatientId2, DeclineReason.other, "something");
    result = call_patientMethod("declinePatient", patientDeclineJson);
    Assert.assertNotNull("Expected a JSONObject, not null", result);
    patientId = (String) result.get("patientId");
    Assert.assertEquals(patientId, testPatientId2);
    patientObj = AutoBeanCodex.decode(factory, PatientObj.class, result.toString()).as();
    attributes = patientObj.getAttributes();
    participates = getAttribute(attributes, Constants.ATTRIBUTE_PARTICIPATES);
    Assert.assertNotNull(participates);
    Assert.assertEquals("n", participates.getValue());
    PatientAttributeObj declined = getAttribute(attributes, Constants.ATTRIBUTE_DECLINE_REASON_CODE);
    Assert.assertNotNull(declined);
    declined = getAttribute(attributes, Constants.ATTRIBUTE_DECLINE_REASON_OTHER);
    Assert.assertNotNull(declined);
    Assert.assertEquals("something", declined.getValue());
  }

  private Throwable getExcFrom_GetPatientById(String patientId) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientRequestHandler handler = new PatientRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getPatientById", String.class);
    Assert.assertNotNull("Could not find the method getPatientById()", method);
    return acc.callMethodGetExc(method, patientId);
  }


  private JSONObject call_GetPatientById(String patientId) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientRequestHandler handler = new PatientRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getPatientById", String.class);
    Assert.assertNotNull("Could not find the method getPatientById()", method);
    Object obj = acc.callMethod(method, patientId);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }

  private JSONObject call_UpdatePatient(JSONObject jsonObject) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientRequestHandler handler = new PatientRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("updatePatient", JsonRepresentation.class);
    Assert.assertNotNull("Could not find the method updatePatient()", method);
    Object obj = acc.callMethod(method, new JsonRepresentation(jsonObject));
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);

    return (JSONObject) obj;
  }

  private JSONObject call_patientMethod(String methodName, JSONObject jsonObject) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientRequestHandler handler = new PatientRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod(methodName, JsonRepresentation.class);
    System.out.println("Method " + methodName + " accessible is " + method.isAccessible());
    Assert.assertNotNull("Could not find the method " + methodName, method);
    Object obj = acc.callMethod(method, new JsonRepresentation(jsonObject));
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);

    return (JSONObject) obj;
  }

  private Throwable getExcFrom_ObjectMethod(String methodName, JSONObject jsonObject) {
    ClinicServices clinicSvc = makeClinicServices();
    PatientRequestHandler handler = new PatientRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PatientRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod(methodName, JsonRepresentation.class);
    Assert.assertNotNull("Could not find the method " + methodName + "()", method);
    return acc.callMethodGetExc(method, new JsonRepresentation(jsonObject));
  }
  // ======== generic utilities

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


  // Creates the service using the user from the cache
  private ClinicServices makeClinicServices() {
    User user = serverContext().userInfo().forName("admin");
    return new ClinicServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

  private JSONObject makePatientDeclineObject(String patientId, DeclineReason reason, String otherReason) {
    PatientDeclineObj patientDeclineObj = factory.patientDeclineObj().as();
    patientDeclineObj.setPatientId(patientId);
    patientDeclineObj.setDeclineReason(reason);
    patientDeclineObj.setReasonOther(otherReason);


    AutoBean<?> autobean = AutoBeanUtils.getAutoBean(patientDeclineObj);
    String json = AutoBeanCodex.encode(autobean).getPayload();
    return new JSONObject(json);
  }

  private PatientAttributeObj getAttribute(List<PatientAttributeObj> attributes, String dataName) {
    if (dataName == null) {
      return null;
    }
    for (PatientAttributeObj attributeObj : attributes) {
      if (dataName.equals(attributeObj.getName())) {
        return attributeObj;
      }
    }
    return null;
  }

  private class ApiPatientUtil extends ApiPatientCommon {
  }
}
