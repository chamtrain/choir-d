/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
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
 * Item bank for PROMIS anxiety assessment. Generated from OID FFCDF6E3-8B17-4673-AB38-C677FFF6DBAF.
 */
public class PromisAnxiety2 {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("EDANX01", "In the past 7 days", "I felt fearful", "", 3.60215, new double[] { 0.3416, 1.0895, 1.9601, 2.6987 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX02", "In the past 7 days", "I felt frightened", "", 3.45041, new double[] { 0.4912, 1.33, 2.1552, 2.8802 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX03", "In the past 7 days", "It scared me when I felt nervous", "", 3.33656, new double[] { 0.6213, 1.2432, 2.0615, 2.7586 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX05", "In the past 7 days", "I felt anxious", "", 3.35528, new double[] { -0.1897, 0.5981, 1.5749, 2.4458 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX07", "In the past 7 days", "I felt like I needed help for my anxiety", "", 3.55093, new double[] { 0.5394, 1.046, 1.865, 2.3847 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX08", "In the past 7 days", "I was concerned about my mental health", "", 2.84824, new double[] { 0.368, 1.0641, 1.8533, 2.5447 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX12", "In the past 7 days", "I felt upset", "", 2.7345, new double[] { -0.5687, 0.4721, 1.6717, 2.9695 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX13", "In the past 7 days", "I had a racing or pounding heart", "", 1.42999, new double[] { 0.3203, 1.3853, 2.7406, 4.6573 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX16", "In the past 7 days", "I was anxious if my normal routine was disturbed", "", 1.80263, new double[] { -0.0383, 0.8787, 2.1126, 3.0457 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX18", "In the past 7 days", "I had sudden feelings of panic", "", 2.98577, new double[] { 0.5662, 1.3021, 2.1496, 3.0608 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX20", "In the past 7 days", "I was easily startled", "", 1.72352, new double[] { 0.0104, 1.1685, 2.203, 3.1085 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX21", "In the past 7 days", "I had trouble paying attention", "", 2.23209, new double[] { -0.3004, 0.7373, 1.8598, 2.9248 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX24", "In the past 7 days", "I avoided public places or activities", "", 1.83002, new double[] { 0.3319, 0.9821, 1.8873, 3.0235 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX26", "In the past 7 days", "I felt fidgety", "", 1.95065, new double[] { -0.0265, 0.8792, 2.0145, 3.2407 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX27", "In the past 7 days", "I felt something awful would happen", "", 2.86023, new double[] { 0.4361, 1.1269, 2.0295, 2.7778 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX30", "In the past 7 days", "I felt worried", "", 3.03184, new double[] { -0.5154, 0.3156, 1.3516, 2.2997 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX33", "In the past 7 days", "I felt terrified", "", 2.58191, new double[] { 1.147, 1.8167, 2.7165, 3.5891 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX37", "In the past 7 days", "I worried about other people's reactions to me", "", 1.70917, new double[] { -0.0396, 0.8836, 2.0016, 3.1044 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX40", "In the past 7 days", "I found it hard to focus on anything other than my anxiety", "", 3.8832, new double[] { 0.4859, 1.2632, 2.1111, 2.8985 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX41", "In the past 7 days", "My worries overwhelmed me", "", 3.65951, new double[] { 0.3644, 1.0338, 1.7805, 2.6212 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX44", "In the past 7 days", "I had twitching or trembling muscles", "", 1.2705, new double[] { 0.5719, 1.459, 2.6779, 3.9723 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX46", "In the past 7 days", "I felt nervous", "", 3.39814, new double[] { -0.2166, 0.6321, 1.6444, 2.7312 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX47", "In the past 7 days", "I felt indecisive", "", 2.53651, new double[] { -0.239, 0.6778, 1.7956, 2.9007 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX48", "In the past 7 days", "Many situations made me worry", "", 3.04334, new double[] { -0.3316, 0.5559, 1.4559, 2.3399 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX49", "In the past 7 days", "I had difficulty sleeping", "", 1.5157, new double[] { -0.8298, 0.0869, 1.1677, 2.3797 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX51", "In the past 7 days", "I had trouble relaxing", "", 2.41166, new double[] { -0.4618, 0.3984, 1.3636, 2.4028 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX53", "In the past 7 days", "I felt uneasy", "", 3.65611, new double[] { -0.2318, 0.5952, 1.5635, 2.4991 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX54", "In the past 7 days", "I felt tense", "", 3.35033, new double[] { -0.5094, 0.3107, 1.2502, 2.2966 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EDANX55", "In the past 7 days", "I had difficulty calming down", "", 3.12733, new double[] { 0.0744, 0.9409, 1.8477, 2.7757 }, -1, "",
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
