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

package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import static edu.stanford.survey.server.ItemBanks.*;

/**
 * Item bank for PROMIS assessment. Generated from OID AF705103-E93D-4B81-85C4-09B4614BDB07.
 */
public class NeuroQolPedShortFormOneZeroSRInteractionWPeers {
  private static final ItemBank bank = itemBank(0.0, 0.0, 8, 8, 3.0,
      item("NQSCLped11", "In the past 7 days", "I felt close to my friends", "", 3.92531, new double[] { -2.1081, -1.6621, -0.516, 0.2396 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped12", "In the past 7 days", "I was able to count on my friends", "", 3.26195, new double[] { -2.1475, -1.5528, -0.472, 0.3499 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped20", "In the past 7 days", "I felt comfortable with others my age", "", 4.08497, new double[] { -2.2209, -1.5882, -0.6861, -0.0731 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped28", "In the past 7 days", "I was happy with the friends I had", "", 3.10793, new double[] { -2.5039, -1.8726, -0.8857, 0.021 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped30", "In the past 7 days", "I felt comfortable talking with my friends", "", 4.49165, new double[] { -2.0487, -1.7106, -0.817, -0.039 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped32", "In the past 7 days", "I spent time with my friends", "", 2.79358, new double[] { -3.0149, -1.7881, -0.6687, 0.4748 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped36", "In the past 7 days", "My friends and I helped each other out", "", 2.76589, new double[] { -2.524, -1.8852, -0.3862, 0.6856 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("NQSCLped38", "In the past 7 days", "I had fun with my friends", "", 3.17547, new double[] { -2.4734, -1.9245, -0.7774, 0.1936 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
