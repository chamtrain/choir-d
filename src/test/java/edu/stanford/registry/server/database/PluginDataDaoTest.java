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

import edu.stanford.registry.client.api.PluginPatientDataObj;
import edu.stanford.registry.client.api.PluginPatientHistoryDataObj;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.DatabaseTestCase;
import edu.stanford.registry.test.Utils;

import java.util.ArrayList;
import java.util.Date;

public class PluginDataDaoTest extends DatabaseTestCase {
  //private static final Logger logger = LoggerFactory.getLogger(PluginDataDaoTest.class);
  private PluginDataDao pluginDao;
  private Patient testPatient0;


  @Override
  protected void postSetUp() {

    pluginDao = new PluginDataDao(databaseProvider.get(), getSiteInfo());
    Utils utils = new Utils(databaseProvider.get(), getSiteInfo());
    User user = utils.getUser(databaseProvider, serverContext.getSitesInfo(), "admin");
    PatientDao patientDao = new PatientDao(databaseProvider.get(), getSiteInfo().getSiteId(), user);
    testPatient0 = new Patient("8888888-8", "John", "Doe", new Date(DateUtils.getDaysAgoDate(getSiteInfo(), (35
        * 365)).getTime()));
    testPatient0 = patientDao.addPatient(testPatient0);
  }

  public void test_storePluginData() {
    PluginPatientDataObj result =
        pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "2", "some random test string");
    assertEquals(result.getDataType(), "testingType");
    PluginPatientDataObj result2 =
        pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "2", "updated");
    assertEquals(result2.getDataValue(), "updated");
  }

  public void test_getPluginDataById() {
    PluginPatientDataObj result =
        pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "1", "some random test string");
    PluginPatientDataObj result2 = pluginDao.findPluginPatientData(result.getDataId());
    assertEquals(result.toString(), result2.toString());
  }

  public void test_findPluginPatientData() {
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "1", "some random test string");
    PluginPatientDataObj result =
        pluginDao.findPluginPatientData("testingType", testPatient0.getPatientId(), "1");
    assertEquals(result.getDataType(), "testingType");
    assertEquals(result.getPatientId(), testPatient0.getPatientId());
  }

  public void test_findAllPluginPatientData() {
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "1", "some random test string");
    ArrayList<PluginPatientDataObj> dataArray = pluginDao.findAllPluginPatientData("testingType", testPatient0.getPatientId(), "2", null, null);
    assertEquals(dataArray.size(), 0);
    ArrayList<PluginPatientDataObj> dataArray2 = pluginDao.findAllPluginPatientData("testingType", testPatient0.getPatientId(), null, null, null);
    assertEquals(dataArray2.size(), 1);
  }

  public void test_findAllPluginPatientDataInRange() {
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "1", "some random test string");
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "3", "some other test string");

    ArrayList<PluginPatientDataObj> dataArray2 = pluginDao.findAllPluginPatientData("testingType", testPatient0.getPatientId(), null,
        DateUtils.getDaysAgoDate(getSiteInfo(), 2),
        DateUtils.getDaysOutDate(getSiteInfo(), 2));
    assertEquals(dataArray2.size(), 2);
  }

  public void test_findAllPluginPatientDataHistory() {
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "3", "some random test string");
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "1", "changed random test string");
    pluginDao.addPluginPatientData("testingType", testPatient0.getPatientId(), "1", "changed again random test string");
    ArrayList<PluginPatientHistoryDataObj> dataArray = pluginDao.findAllPluginPatientDataHistory("testingType", testPatient0.getPatientId(), "2");
    assertEquals(dataArray.size(), 0);
    ArrayList<PluginPatientHistoryDataObj> dataArray2 = pluginDao.findAllPluginPatientDataHistory("testingType", testPatient0.getPatientId(), "1");
    assertEquals(dataArray2.size(), 2);
    ArrayList<PluginPatientHistoryDataObj> dataArray3 = pluginDao.findAllPluginPatientDataHistory("testingType", testPatient0.getPatientId(), null);
    assertEquals(dataArray3.size(), 3);
  }
}
