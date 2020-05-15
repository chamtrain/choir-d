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
 * Item bank for PROMIS sleep disturbance assessment. Generated from OID F625E32A-B26D-4228-BBC2-DD6C2E4EEA6E.
 */
public class PromisSleepDisturbance {
  private static final ItemBank bank = itemBank(0.0, 0.0, 4, 12, 3.0,
      item("Sleep105", "In the past 7 days", "My sleep was restful.", "", 2.4485, new double[] { -1.2024, -0.1474, 0.7206, 1.5918 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep106", "In the past 7 days", "My sleep was light.", "", 1.5143, new double[] { -0.6541, 0.4386, 1.5903, 2.6085 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep107", "In the past 7 days", "My sleep was deep.", "", 1.5697, new double[] { -1.5219, -0.3506, 0.663, 1.9195 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep108", "In the past 7 days", "My sleep was restless.", "", 2.296, new double[] { -0.2885, 0.687, 1.4524, 2.326 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep109", "In the past 7 days", "My sleep quality was...", "", 3.385, new double[] { -1.2193, -0.0027, 1.0761, 1.902 }, -1, "",
          response("Very poor", 5),
          response("Poor", 4),
          response("Fair", 3),
          response("Good", 2),
          response("Very good", 1)
      ),
      item("Sleep110", "In the past 7 days", "I got enough sleep.", "", 2.1699, new double[] { -1.5596, -0.1623, 0.7654, 1.8068 }, -1, "",
          response("Never", 5),
          response("Rarely", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Always", 1)
      ),
      item("Sleep115", "In the past 7 days", "I was satisfied with my sleep.", "", 2.7714, new double[] { -1.248, -0.3351, 0.4303, 1.095 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep116", "In the past 7 days", "My sleep was refreshing.", "", 2.5768, new double[] { -1.3528, -0.34, 0.4935, 1.2847 }, -1, "",
          response("Not at all", 5),
          response("A little bit", 4),
          response("Somewhat", 3),
          response("Quite a bit", 2),
          response("Very much", 1)
      ),
      item("Sleep125", "In the past 7 days", "I felt lousy when I woke up.", "", 1.912, new double[] { -0.1422, 0.6848, 1.317, 2.0697 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep20", "In the past 7 days", "I had a problem with my sleep.", "", 2.7964, new double[] { -0.5612, 0.3267, 0.9827, 1.7406 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep42", "In the past 7 days", "It was easy for me to fall asleep.", "", 2.0887, new double[] { -1.1022, 0.0507, 0.9392, 1.8382 }, -1, "",
          response("Never", 5),
          response("Rarely", 4),
          response("Sometimes", 3),
          response("Often", 2),
          response("Always", 1)
      ),
      item("Sleep44", "In the past 7 days", "I had difficulty falling asleep.", "", 2.5097, new double[] { -0.4634, 0.3113, 0.978, 1.7204 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep45", "In the past 7 days", "I laid in bed for hours waiting to fall asleep.", "", 2.1782, new double[] { 0.0261, 0.8465, 1.5456, 2.3769 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep50", "In the past 7 days", "I woke up too early and could not fall back asleep.", "", 1.1862, new double[] { -0.9836, 0.3339, 1.7587, 3.2965 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep65", "In the past 7 days", "I felt physically tense at bedtime.", "", 1.6432, new double[] { 0.2117, 1.1306, 2.0191, 2.9589 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep67", "In the past 7 days", "I worried about not being able to fall asleep.", "", 2.3734, new double[] { 0.2796, 1.0192, 1.6162, 2.3726 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep68", "In the past 7 days", "I felt worried at bedtime.", "", 1.7709, new double[] { 0.2211, 1.2143, 1.9546, 2.7259 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep69", "In the past 7 days", "I had trouble stopping my thoughts at bedtime.", "", 1.7542, new double[] { -0.5701, 0.4147, 1.0382, 1.8091 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep70", "In the past 7 days", "I felt sad at bedtime.", "", 1.403, new double[] { 0.6715, 1.5563, 2.2615, 3.1267 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep71", "In the past 7 days", "I had trouble getting into a comfortable position to sleep.", "", 1.5237, new double[] { -0.1883, 0.951, 1.7617, 2.7191 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep72", "In the past 7 days", "I tried hard to get to sleep.", "", 2.4743, new double[] { 0.0272, 0.6644, 1.1899, 1.8778 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep78", "In the past 7 days", "Stress disturbed my sleep.", "", 1.9882, new double[] { -0.017, 0.8949, 1.608, 2.2248 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep86", "In the past 7 days", "I tossed and turned at night.", "", 1.8526, new double[] { -0.5829, 0.6593, 1.37, 2.2987 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("Sleep87", "In the past 7 days", "I had trouble staying asleep.", "", 2.1858, new double[] { -0.8961, 0.0972, 0.9951, 1.7785 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep90", "In the past 7 days", "I had trouble sleeping.", "", 3.6552, new double[] { -0.6142, 0.1635, 0.9581, 1.6228 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep92", "In the past 7 days", "I woke up and had trouble falling back to sleep.", "", 2.169, new double[] { -0.5461, 0.3567, 1.3123, 2.237 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("Sleep93", "In the past 7 days", "I was afraid I would not get back to sleep after waking up.", "", 1.9715, new double[] { 0.2182, 1.0302, 1.6472, 2.4726 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      )
  );

  public static ItemBank bank() {
    return bank;
  }
}
