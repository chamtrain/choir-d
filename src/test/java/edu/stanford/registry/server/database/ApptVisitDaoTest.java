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
package edu.stanford.registry.server.database;

import edu.stanford.registry.shared.ApptVisit;
import edu.stanford.registry.test.DatabaseTestCase;

import java.util.ArrayList;

/**
 * @author Teresa Pacht <tpacht@stanford.edu>
 * @since 10/08/2019
 */
public class ApptVisitDaoTest extends DatabaseTestCase {
  private ApptVisitDao apptVisitDao;
  private ApptVisit testApptVisit;
  private final Long testApptVisitEid = 1000L;
  private final String testVisitType =  "testType";
  private final String testVisitDesc = "testDescription";

  @Override
  protected void postSetUp() {
    apptVisitDao = new ApptVisitDao(databaseProvider.get());
    testApptVisit = apptVisitDao.insertApptVisit(testVisitType, testVisitDesc, testApptVisitEid);
  }
  
  public void testGetApptVisitById() {
    ApptVisit apptVisit = apptVisitDao.getApptVisitById(testApptVisit.getApptVisitId());
    assertNotNull("Expecting an apptVisit with id " + testApptVisit.getApptVisitId(), apptVisit);
  }

  public void testGetApptVisitByEid() {
    ApptVisit apptVisit = apptVisitDao.getApptVisitByEid(testApptVisitEid);
    assertNotNull("Expecting an apptVisit with eid " + testApptVisitEid, apptVisit);
    assertEquals("Expecting an apptVisit with eid " + testApptVisitEid, testApptVisitEid, apptVisit.getVisitEId());
  }
  
  public void testGetApptVisitByType() {
    ArrayList<ApptVisit> visits = apptVisitDao.getApptVisitByType(testVisitType);
    assertNotNull("Expecting a list of appt_visit objects", visits);
    assertEquals("Expected a list.size of 1 ", 1, visits.size());
  }

  public void testGetApptVisitByDesc() {
    ArrayList<ApptVisit> visits = apptVisitDao.getApptVisitByDescription(testVisitDesc);
    assertNotNull("Expecting a list of appt_visit objects", visits);
    assertEquals("Expected a list.size of 1 ",1, visits.size());
  }

  public void testInsertApptVisit() {
    final String type = "newType";
    final String desc = "New Appt Visit Description";
    ApptVisit visit = apptVisitDao.insertApptVisit(type, desc, 2222L);
    assertNotNull("Expected ApptVisit to be returned from insert", visit);
    assertNotNull("Expected apptVisitId to be set", visit.getApptVisitId());
    assertEquals("Expected apptType to be what we set ", type, visit.getVisitType());
    assertEquals("Expected description to be what we set  ", desc, visit.getVisitDescription());
  }
}