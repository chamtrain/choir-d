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
 * Item bank for PROMIS assessment. Generated from OID 95748180-59F6-47F5-B4C7-81695845CE54.
 */
public class PromisPedShortFormOneZeroFatigue10a {
  private static final ItemBank bank = itemBank(0.0, 0.0, 10, 10, 3.0,
      item("2876R1", "In the past 7 days", "I got tired easily.", "", 1.66926, new double[] { -1.3393, -0.1497, 1.3837, 2.6176 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4196R1", "In the past 7 days", "I was too tired to enjoy the things I like to do.", "", 1.89877, new double[] { -0.0465, 0.9128, 1.943, 2.6192 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4208bR2", "In the past 7 days", "I was too tired to do things outside.", "", 1.53166, new double[] { -0.5961, 0.4373, 1.5536, 2.3558 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4210R2", "In the past 7 days", "I was so tired it was hard for me to pay attention.", "", 1.64127, new double[] { -0.72, 0.2768, 1.5261, 2.3919 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4212R1", "In the past 7 days", "Being tired made it hard for me to play or go out with my friends as much as I'd like.", "", 1.82093, new double[] { -0.0135, 1.0025, 2.0364, 2.84 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4213R1", "In the past 7 days", "I felt weak.", "", 1.67325, new double[] { -0.5098, 0.5465, 1.8625, 3.0526 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4220R1", "In the past 7 days", "I had trouble starting things because I was too tired.", "", 1.75975, new double[] { -0.7202, 0.3686, 1.7022, 2.9224 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4221R1", "In the past 7 days", "I had trouble finishing things because I was too tired.", "", 1.68255, new double[] { -0.8739, 0.0851, 1.2958, 2.1817 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4239aR2", "In the past 7 days", "Being tired made it hard for me to keep up with my schoolwork.", "", 1.68759, new double[] { -0.173, 0.5451, 1.5583, 2.2881 }, -1, "",
          response("Never", 1),
          response("Almost Never", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Almost Always", 5)
      ),
      item("4241R2", "In the past 7 days", "I was too tired to do sports or exercise.", "", 1.55375, new double[] { -0.1957, 0.7788, 1.8938, 2.5773 }, -1, "",
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
