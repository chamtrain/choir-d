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
 * Item bank for PROMIS assessment. Generated from OID 2B92F44C-302B-4C35-8772-3548BC43CB6A.
 */
public class NeuroQolBankOneZeroPosAffectWellBeing {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item(" NQPPF12", "Lately", "I felt hopeful", "", 4.96074, new double[] { -1.6487, -0.8302, 0.1234, 0.8777 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item(" NQPPF14", "Lately", "I had a sense of well-being", "", 6.60979, new double[] { -1.4063, -0.7089, 0.0725, 0.8153 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item(" NQPPF15", "Lately", "My life was satisfying", "", 5.82867, new double[] { -1.3787, -0.6955, 0.1716, 0.8919 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF02", "Lately", "I was able to enjoy life", "", 2.85836, new double[] { -1.6382, -0.843, 0.1359, 1.2391 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF03", "Lately", "I felt a sense of purpose in my life", "", 3.69836, new double[] { -1.3651, -0.6816, 0.1953, 1.0351 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF04", "Lately", "I could laugh and see the humor in situations", "", 2.73128, new double[] { -1.8634, -1.2606, -0.1628, 0.7927 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF05", "Lately", "I was able to be at ease and feel relaxed", "", 3.03956, new double[] { -1.6355, -0.8474, 0.0259, 1.2794 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF06", "Lately", "I looked forward with enjoyment to upcoming events", "", 3.43471, new double[] { -1.5461, -0.9063, 0.1045, 1.0355 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF07", "Lately", "Many areas of my life were interesting to me", "", 4.01345, new double[] { -1.468, -0.6688, 0.1761, 1.075 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF08", "Lately", "I felt emotionally stable", "", 2.65589, new double[] { -1.6319, -1.048, -0.18, 0.7789 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF10", "Lately", "I felt lovable", "", 3.04784, new double[] { -1.6718, -0.8209, 0.0994, 0.9884 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF11", "Lately", "I felt confident", "", 3.43655, new double[] { -1.5457, -0.8165, 0.0139, 0.9641 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF13", "Lately", "I had a good life", "", 5.20641, new double[] { -1.5009, -0.8824, 0.0111, 0.6963 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF16", "Lately", "I had a sense of balance in my life", "", 4.91912, new double[] { -1.3921, -0.6029, 0.2022, 0.9628 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF17", "Lately", "My life had meaning", "", 5.60018, new double[] { -1.389, -0.845, 8.0E-4, 0.6868 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF18", "Lately", "My life was peaceful", "", 3.19238, new double[] { -1.6418, -0.7983, 0.0738, 1.1679 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF19", "Lately", "My life was worth living", "", 4.15583, new double[] { -1.8868, -1.0572, -0.29, 0.306 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF20", "Lately", "My life had purpose", "", 5.09529, new double[] { -1.5216, -0.9021, -0.1202, 0.5311 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF21", "Lately", "I was living life to the fullest", "", 3.6484, new double[] { -1.1253, -0.4402, 0.3556, 1.1312 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF22", "Lately", "I felt cheerful", "", 4.58501, new double[] { -1.6527, -0.8785, 0.0931, 1.1194 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF23", "Lately", "In most ways my life was close to my ideal", "", 3.63087, new double[] { -0.8407, -0.2679, 0.4779, 1.4662 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF24", "Lately", "I had good control of my thoughts", "", 2.82777, new double[] { -1.8748, -1.0353, -0.1059, 0.7589 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("NQPPF26", "Lately", "Even when things were going badly, I still had hope", "", 3.19493, new double[] { -1.8862, -1.0821, -0.0979, 0.7449 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
