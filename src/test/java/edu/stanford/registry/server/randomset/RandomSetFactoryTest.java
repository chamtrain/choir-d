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
package edu.stanford.registry.server.randomset;

import org.junit.Assert;
import org.junit.Test;

import edu.stanford.registry.test.FakeSiteInfo;
import edu.stanford.registry.test.PrivateAccessor;
import edu.stanford.registry.tool.RandomSetsCreate;

public class RandomSetFactoryTest {
  static final String pkg = "edu.stanford.registry.server.randomset.";
  @Test
  public void testGetAdderKSort() {
    testAdderExists(RandomSetsCreate.TSET_KSort_BackPain, "KSort", "RandomSetKSort");
  }

  @Test
  public void testGetAdderPure() {
    testAdderExists(RandomSetsCreate.TSET_Pure_Migraine, "Pure", "RandomSetPure");
  }

  private void testAdderExists(String dummyName, String algorithm, String className) {
    FakeSiteInfo siteInfo = new FakeSiteInfo();

    RandomSetter randomSet = siteInfo.getRandomSet(dummyName);
    Assert.assertNotNull("RandomSetAdderFactory.getAdder("+dummyName+") should not be null", randomSet);
    String got = randomSet.getName();
    Assert.assertEquals("getAdder(name).getName() should equal name", dummyName, got);
    Assert.assertEquals(className, randomSet.getClass().getSimpleName());
  }

  @Test
  public void testRoundTripToJson() {
    RandomSetter rs = RandomSetsCreate.createBackPainRandomSet();
    String json = rs.toJsonString();

    FakeSiteInfo siteInfo = new FakeSiteInfo();
    RandomSetFactory fact = new RandomSetFactory(siteInfo);
    PrivateAccessor<RandomSetFactory> rsfAcc = new PrivateAccessor<RandomSetFactory>(fact);
    rsfAcc.callMethod("createRandomSet", rs.getName(), json);  // ensure it doesn't throw an exception
  }

}