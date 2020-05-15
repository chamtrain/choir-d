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
 * Item bank for PROMIS assessment. Generated from OID EF8AE77E-1AA9-4EC7-809A-A30192DA5CF9.
 */
public class PromisPedBankOneZeroMeaningAndPurpose {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("SWB_FO_002R1", "Thinking about my life,", "I expect amazing things to happen to me.", "", 2.247, new double[] { -2.572, -1.814, -0.98, -0.064 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_005R1", "Thinking about my life,", "I expect things to work out for the best.", "", 2.407, new double[] { -3.162, -2.213, -1.306, -0.246 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_007R1", "Thinking about my life,", "I expect to have a job in the future.", "", 2.104, new double[] { -3.656, -2.748, -2.056, -1.064 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_008R1", "Thinking about my life,", "I expect to have a family in the future.", "", 1.612, new double[] { -3.534, -2.566, -1.736, -0.78 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_009R1", "Thinking about my life,", "I expect to be successful in the future.", "", 3.35, new double[] { -2.837, -2.201, -1.431, -0.537 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_010R1", "Thinking about my life,", "I expect to enjoy my future life.", "", 3.287, new double[] { -2.923, -2.334, -1.549, -0.573 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_011R1", "Thinking about my life,", "I expect to have a long life.", "", 2.057, new double[] { -3.259, -2.451, -1.554, -0.583 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_012R1", "Thinking about my life,", "I expect to have success in the future.", "", 3.658, new double[] { -2.791, -2.142, -1.388, -0.46 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_013R1", "Thinking about my life,", "I expect to achieve what I want in life.", "", 3.192, new double[] { -2.791, -2.047, -1.241, -0.307 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_015R1", "Thinking about my life,", "I look forward to what will happen in the future.", "", 3.088, new double[] { -2.828, -2.097, -1.355, -0.465 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_016R1", "Thinking about my life,", "I expect to succeed at what I try to do.", "", 2.914, new double[] { -3.064, -2.197, -1.401, -0.349 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_017R1", "Thinking about my life,", "When bad things happen, I expect them to get better.", "", 1.933, new double[] { -2.937, -2.136, -1.181, -0.031 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_020R1", "Thinking about my life,", "I have hope.", "", 3.121, new double[] { -2.766, -2.045, -1.314, -0.425 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_021R1", "Thinking about my life,", "I am full of hope.", "", 2.986, new double[] { -2.766, -1.893, -1.191, -0.272 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_022R1", "Thinking about my life,", "I always have hope.", "", 2.099, new double[] { -3.198, -2.16, -1.164, 0.031 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_023R1", "Thinking about my life,", "I feel hopeful about my plans for the future.", "", 3.236, new double[] { -2.822, -2.167, -1.365, -0.479 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_024R1", "Thinking about my life,", "I feel hopeful about my future.", "", 3.316, new double[] { -2.679, -2.079, -1.326, -0.401 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_025R1", "Thinking about my life,", "I am positive about my future.", "", 3.582, new double[] { -2.698, -1.962, -1.233, -0.312 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_026R1", "Thinking about my life,", "I can do almost anything if I have enough faith in myself.", "", 2.742, new double[] { -2.837, -2.133, -1.364, -0.405 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_029R1", "Thinking about my life,", "I have goals for myself.", "", 2.628, new double[] { -2.874, -2.086, -1.25, -0.399 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_031R1", "Thinking about my life,", "I make plans for my future.", "", 2.463, new double[] { -2.667, -1.996, -1.194, -0.331 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_032R1", "Thinking about my life,", "I have things I want to do in life.", "", 2.545, new double[] { -3.271, -2.506, -1.684, -0.64 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_033R1", "Thinking about my life,", "I have things I need to do in life.", "", 2.473, new double[] { -2.991, -2.421, -1.558, -0.558 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_034R1", "Thinking about my life,", "I have things I want to accomplish in life.", "", 2.793, new double[] { -3.039, -2.256, -1.473, -0.513 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_035R1", "Thinking about my life,", "The things I have done in the past will help me in the future.", "", 1.891, new double[] { -2.978, -2.126, -1.158, -0.068 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_036R1", "Thinking about my life,", "I expect to achieve my goals.", "", 2.631, new double[] { -3.056, -2.25, -1.318, -0.356 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_037R1", "Thinking about my life,", "I know where I am going in life.", "", 2.287, new double[] { -2.427, -1.735, -0.836, 0.072 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_038R1", "Thinking about my life,", "I can reach my goals in life.", "", 2.941, new double[] { -2.86, -2.155, -1.332, -0.287 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_039R1", "Thinking about my life,", "I want to make the most out of my life.", "", 3.038, new double[] { -3.01, -2.337, -1.602, -0.75 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_040R1", "Thinking about my life,", "My life is filled with important things.", "", 2.973, new double[] { -2.925, -2.103, -1.266, -0.302 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_041R1", "Thinking about my life,", "My life is important.", "", 2.966, new double[] { -2.87, -2.2, -1.515, -0.733 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_042R1", "Thinking about my life,", "I want to do what is important.", "", 2.002, new double[] { -3.518, -2.583, -1.554, -0.418 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_043R1", "Thinking about my life,", "I try to find meaning in life.", "", 2.006, new double[] { -2.778, -2.046, -1.16, -0.116 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_044R1", "Thinking about my life,", "My life has meaning.", "", 3.445, new double[] { -2.539, -2.0, -1.28, -0.504 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_045R1", "Thinking about my life,", "My life is filled with meaning.", "", 2.923, new double[] { -2.545, -1.923, -1.091, -0.189 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_046R1", "Thinking about my life,", "I try to find purpose in life.", "", 1.83, new double[] { -2.973, -2.152, -1.224, -0.15 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_047R1", "Thinking about my life,", "My life has purpose.", "", 3.435, new double[] { -2.51, -1.958, -1.19, -0.433 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_048R1", "Thinking about my life,", "My life is filled with purpose.", "", 3.462, new double[] { -2.471, -1.892, -1.104, -0.277 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_049R1", "Thinking about my life,", "I have a clear purpose in life.", "", 2.72, new double[] { -2.38, -1.729, -0.82, -0.015 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_050R1", "Thinking about my life,", "I know what makes my life meaningful.", "", 2.524, new double[] { -2.452, -1.825, -1.009, -0.132 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_051R1", "Thinking about my life,", "My life is filled with things that interest me.", "", 2.285, new double[] { -3.296, -2.347, -1.369, -0.207 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_052R1", "Thinking about my life,", "I have a reason for living.", "", 3.044, new double[] { -2.655, -2.133, -1.475, -0.723 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_053R1", "Thinking about my life,", "I am satisfied with my purpose in life.", "", 3.157, new double[] { -2.525, -1.933, -1.134, -0.237 }, -1, "",
          response("Not at all", 1),
          response("A little bit", 2),
          response("Somewhat", 3),
          response("Quite a bit", 4),
          response("Very much", 5)
      ),
      item("SWB_FO_054R1", "Thinking about my life,", "People will remember me when I die.", "", 1.815, new double[] { -2.961, -2.108, -1.174, -0.238 }, -1, "",
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
