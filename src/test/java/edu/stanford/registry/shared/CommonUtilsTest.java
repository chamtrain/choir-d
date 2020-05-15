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

package edu.stanford.registry.shared;

import org.junit.Assert;
import org.junit.Test;

//import com.google.gwt.junit.client.GWTTestCase;

import edu.stanford.registry.test.FakeSiteInfo;

public class CommonUtilsTest {

  FakeSiteInfo siteInfo = new FakeSiteInfo();


  @Test
  public void testCountLine1() {
    callCountlines("one", 1);
  }


  @Test
  public void testCountLineTest1empty() {
    callCountlines("", 1);
  }


  @Test
  public void testCountLineTest3() {
    callCountlines("aaaa\nbbb\nccc", 3);
  }


  @Test
  public void countLineTest3empty() {
    callCountlines("\n\n", 3);
  }


  // ==== utilities

  private void callCountlines(String s, int expect) {
    CommonUtils utils = new CommonUtils(siteInfo.getClientParams());

    int num = utils.countLines(s);
    Assert.assertEquals("Counted wrong number of lines for: "+s.replace("\n", "|"), expect, num);
  }

}