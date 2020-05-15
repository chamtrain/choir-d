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

public class RandomSetTestUtil {
  //private static Logger logger = Logger.getLogger(XMLFileUtils.class);
  private int randomSetIdNumber = 100;

  int getNextSetIdForTesting() {
    return randomSetIdNumber++;
  }

  /*
  public void createOneGroupSet(String algorithm, boolean plusControl) {
    String desc = "This is a random set just for testing/desc";
    RandomSet set = new RandomSet(getNextSetIdForTesting(), "OneGroup", 6L,
                                  RandomSetType.TreatmentSet, "OneGroup/Title", desc,
                                  "DrWho", new RandomSetFactory().getAdder(algorithm), 10, null);

    RandomGroup g;
    g = new RandomGroup()
    i
    // Ensure there's an initial and a follow-up with a null end-date, for future appointments
    String initial = null;
    String followup = null;
    Assert.assertNotNull("Expected to find at least one Initial process with no expiration date", initial);
    Assert.assertNotNull("Expected to find at least one FollowUp process with no expiration date", followup);
  }
*/

}