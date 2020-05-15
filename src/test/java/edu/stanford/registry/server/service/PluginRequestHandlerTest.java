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
import edu.stanford.registry.client.api.PluginPatientGetObj;
import edu.stanford.registry.client.api.PluginPatientStoreObj;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.PluginRequestHandler;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.PrivateAccessor;

import java.lang.reflect.Method;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.restlet.ext.json.JsonRepresentation;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PluginRequestHandlerTest extends DatabaseTestCase {

  private final ApiObjectFactory factory = AutoBeanFactorySource.create(ApiObjectFactory.class);

  public void testAddData_MissingPatient() {
    Throwable t = call_StoreDataErr(makeStoreObj("1000-2", "blob a", null), "testType");
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NotFoundException);
  }

  public void testAddData_BadSyntax() {
    Throwable t = call_StoreDataErr(makeStoreObj(null, "blob B", null), "aType");
    Assert.assertNotNull("Expected a NumberFormatException, got no exception", t);
    Assert.assertTrue("Expected a NumberFormatException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof ApiStatusException);
  }

  public void testAddNewData_Valid() {
    Patient pat = createPatient();
    JSONObject result = call_StoreData(makeStoreObj(pat.getPatientId(), "blob a", null), "testAType");
    Assert.assertNotNull("Expected a JSONObject, got null", result);
    Assert.assertNotNull(result.get("created"));
    Assert.assertNotNull(result.get("dataId"));
    JSONObject result2 = call_StoreData(makeStoreObj(pat.getPatientId(), "blob b", "3"), "testBType");
    Assert.assertNotNull(result2.get("created"));
    Assert.assertNotNull(result2.get("dataId"));
  }

  public void testAddUpdateData_Valid() {
    Patient pat = createPatient();
    JSONObject storeObj = makeStoreObj(pat.getPatientId(), "blob", null);
    System.out.println("calling storeData with:" + storeObj.toString());
    call_StoreData(storeObj, "testType");
    JSONObject result = call_StoreData(makeStoreObj(pat.getPatientId(), "new blob", null), "testType");
    Assert.assertNotNull("Expected a JSONObject, got null", result);
    JSONObject result2 = call_StoreData(makeStoreObj(pat.getPatientId(), "blob", null), "testType");
    Assert.assertNotNull("Expected a JSONObject again, got null", result);
    Assert.assertEquals(result.get("dataId"), result2.get("dataId"));
  }

  public void testGetLast_InvalidPatient() {
    Throwable t = call_GetLastErr(makeGetObj("1000-2", null, null, null));
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NotFoundException);
  }

  public void testGetLast_ValidPatient() {
    Patient pat = createPatient();
    call_StoreData(makeStoreObj(pat.getPatientId(), "blob", null), "testType");
    JSONObject result = call_GetLast(makeGetObj(pat.getPatientId(), null, null, null));
    Assert.assertNotNull("Expected a JSONObject, got null", result);
  }

  public void testReadAll_InvalidPatient() {
    Throwable t = call_GetAllErr(makeGetObj("1000-2", "blob", null, null));
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NotFoundException);
  }

  public void testReadAll_Valid() {
    Patient pat = createPatient();
    call_StoreData(makeStoreObj(pat.getPatientId(), "blob", null), "testType");

    JSONObject result = call_GetAll(makeGetObj(pat.getPatientId(), null,
        null, null));
    Assert.assertNotNull("Expected a JSONObject, got null", result);
    Assert.assertNotNull("Expected a JSONObject again, got null", result);

    JSONArray arrayObj = result.getJSONArray("pluginPatientData");
    Assert.assertEquals("Expected an array of one", 1, arrayObj.length());
  }

  public void testReadAll_ValidInRange() {
    Patient pat = createPatient();
    call_StoreData(makeStoreObj(pat.getPatientId(), "blob", null), "testType");

    JSONObject result = call_GetAll(makeGetObj(pat.getPatientId(), null,
        DateUtils.getDaysAgoDate(getSiteInfo(), 1),
        DateUtils.getDaysOutDate(getSiteInfo(), 1)));
    Assert.assertNotNull("Expected a JSONObject, got null", result);
    Assert.assertNotNull("Expected a JSONObject again, got null", result);

    JSONArray arrayObj = result.getJSONArray("pluginPatientData");
    Assert.assertEquals("Expected an array of one", 1, arrayObj.length());
  }

  // History
  public void testReadHistory_InvalidPatient() {
    Throwable t = call_GetHistoryErr(makeGetObj("1000-2", "blob", null, null));
    Assert.assertNotNull("Expected a NotFoundException, got no exception", t);
    Assert.assertTrue("Expected a NotFoundException, got: " + t.getClass().getName() + ": " + t.getMessage(),
        t instanceof NotFoundException);
  }

  public void testReadHistory_Valid() {
    Patient pat = createPatient();
    call_StoreData(makeStoreObj(pat.getPatientId(), "blob", null), "testType");

    JSONObject result = call_GetHistory(makeGetObj(pat.getPatientId(), null,
        null, null));
    Assert.assertNotNull("Expected a JSONObject, got null", result);
    Assert.assertNotNull("Expected a JSONObject again, got null", result);

    JSONArray arrayObj = result.getJSONArray("pluginPatientHistoryData");
    Assert.assertEquals("Expected an array of one", 1, arrayObj.length());
  }

  public void testReadHistory_ValidInRange() {
    Patient pat = createPatient();
    call_StoreData(makeStoreObj(pat.getPatientId(), "blob", null), "testType");
    JSONObject result = call_GetHistory(makeGetObj(pat.getPatientId(), null,
        DateUtils.getDaysAgoDate(getSiteInfo(), 1),
        DateUtils.getDaysOutDate(getSiteInfo(), 1)));
    Assert.assertNotNull("Expected a JSONObject, got null", result);
    Assert.assertNotNull("Expected a JSONObject again, got null", result);

    System.out.println(result.toString());
    JSONArray arrayObj = result.getJSONArray("pluginPatientHistoryData");
    Assert.assertEquals("Expected an array of one", 1, arrayObj.length());
  }

  private Throwable call_StoreDataErr(JSONObject dataObj, String dataType) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("storeData", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method storeData()", method);
    return acc.callMethodGetExc(method, new JsonRepresentation(dataObj), "/pluginData/patient/store/", dataType);
  }

  private JSONObject call_StoreData(JSONObject dataObj, String dataType) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("storeData", JsonRepresentation.class, String.class, String.class);

    Object obj = acc.callMethod(method, new JsonRepresentation(dataObj), "/pluginData/patient/store/", dataType);
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }

  private Throwable call_GetLastErr(JSONObject dataObj) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getLast", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method getLast()", method);
    return acc.callMethodGetExc(method, new JsonRepresentation(dataObj), "/pluginData/patient/getLast/testType", "testType");
  }

  private JSONObject call_GetLast(JSONObject jsonObject) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getLast", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method getLast()", method);
    Object obj = acc.callMethod(method, new JsonRepresentation(jsonObject), "/pluginData/patient/getLast/testType", "testType");
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);

    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }

  private Throwable call_GetAllErr(JSONObject dataObj) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getAll", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method getAll()", method);
    return acc.callMethodGetExc(method, new JsonRepresentation(dataObj), "/pluginData/patient/getAll/testType", "testType");
  }

  private JSONObject call_GetAll(JSONObject jsonObject) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getAll", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method getLast()", method);
    Object obj = acc.callMethod(method, new JsonRepresentation(jsonObject), "/pluginData/patient/getAll/testType", "testType");
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }

  private Throwable call_GetHistoryErr(JSONObject dataObj) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getHistory", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method getHistory()", method);
    return acc.callMethodGetExc(method, new JsonRepresentation(dataObj), "/pluginData/patient/getHistory/testType", "testType");
  }

  private JSONObject call_GetHistory(JSONObject jsonObject) {
    ClinicServices clinicSvc = makeClinicServices();
    PluginRequestHandler handler = new PluginRequestHandler(getSiteInfo(), clinicSvc);
    PrivateAccessor<PluginRequestHandler> acc = new PrivateAccessor<>(handler);
    Method method = acc.getMethod("getHistory", JsonRepresentation.class, String.class, String.class);
    Assert.assertNotNull("Could not find the method getHistory()", method);
    Object obj = acc.callMethod(method, new JsonRepresentation(jsonObject), "/pluginData/patient/getHistory/testType", "testType");
    Assert.assertNotNull("Expected a JSONObject back, got null", obj);
    Assert.assertTrue("Expected a JSONObject back, got a " + obj.getClass().getName(), obj instanceof JSONObject);
    return (JSONObject) obj;
  }
  // ======== generic utilities

  /**
   * Creates a patient with the given id, name and birth date
   *
   * @return Patient the patient object that is created
   */
  private Patient createPatient() {
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId());
    Date dob = DateUtils.getDaysAgoDate(getSiteInfo(), 30 * 365);
    Patient testPatient = new Patient("10051-1", "Jane", "Doe", dob);
    testPatient = patientDao.addPatient(testPatient);
    return testPatient;
  }


  // Creates the service using the user from the cache
  private ClinicServices makeClinicServices() {
    User user = serverContext().userInfo().forName("admin");
    return new ClinicServicesImpl(user, databaseProvider, serverContext(), getSiteInfo());
  }

  private JSONObject makeStoreObj(String patientId, String dataValue, String dataVersion) {
    PluginPatientStoreObj storeObj = factory.pluginPatientStoreObj().as();
    storeObj.setPatientId(patientId);
    storeObj.setDataValue(dataValue);
    storeObj.setDataVersion(dataVersion);
    AutoBean<PluginPatientStoreObj> storeObjBean = AutoBeanUtils.getAutoBean(storeObj);
    return new JSONObject(AutoBeanCodex.encode(storeObjBean).getPayload());
  }

  private JSONObject makeGetObj(String patientId, String dataVersion, Date start, Date end) {
    PluginPatientGetObj getObj = factory.pluginPatientGetObj().as();
    getObj.setPatientId(patientId);
    getObj.setDataVersion(dataVersion);
    if (start != null) {
      getObj.setFromTime(start.getTime());
    }
    if (end != null) {
      getObj.setToTime(end.getTime());
    }
    AutoBean<PluginPatientGetObj> getObjBean = AutoBeanUtils.getAutoBean(getObj);
    return new JSONObject(AutoBeanCodex.encode(getObjBean).getPayload());
  }


}
