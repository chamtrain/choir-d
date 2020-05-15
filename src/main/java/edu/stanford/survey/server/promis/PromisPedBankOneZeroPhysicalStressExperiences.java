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
 * Item bank for PROMIS assessment. Generated from OID 130BB4F8-D2F2-4D2F-AF84-550040C13E6D.
 */
public class PromisPedBankOneZeroPhysicalStressExperiences {
  private static final ItemBank bank = itemBank(0.0, 0.0, 5, 12, 4.0,
      item("EoS_S_011R1", "In the past 7 days,", "I felt as if I needed to move my legs a lot.", "", 1.583, new double[] { 0.713, 1.567, 2.427, 3.411 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_015R1", "In the past 7 days,", "A sudden noise made me jump.", "", 1.461, new double[] { 0.452, 1.465, 2.577, 3.496 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_017R1", "In the past 7 days,", "My muscles felt tight.", "", 1.71, new double[] { 0.503, 1.428, 2.517, 3.398 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_019R1", "In the past 7 days,", "I could not stay still for long.", "", 1.126, new double[] { 0.06, 1.152, 2.234, 3.32 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_024R1", "In the past 7 days,", "My mouth was dry.", "", 1.675, new double[] { 0.54, 1.569, 2.697, 3.828 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_026R1", "In the past 7 days,", "My breathing was fast, even when I was not exercising or playing hard.", "", 2.745, new double[] { 1.281, 1.955, 2.691, 3.388 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_029R1", "In the past 7 days,", "My palms were sweaty.", "", 1.473, new double[] { 0.488, 1.542, 2.639, 3.587 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_030R1", "In the past 7 days,", "I was sweaty, even when I was not exercising or playing hard.", "", 1.851, new double[] { 1.204, 2.022, 2.84, 3.552 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_031R1", "In the past 7 days,", "My heart pounded, even when I was not exercising or playing hard.", "", 2.112, new double[] { 0.779, 1.644, 2.546, 3.41 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_032R1", "In the past 7 days,", "My heart beat faster than usual, even when I was not exercising or playing hard.", "", 2.475, new double[] { 1.135, 1.877, 2.724, 3.349 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_033R1", "In the past 7 days,", "I had trouble breathing, even when I was not exercising or playing hard.", "", 2.397, new double[] { 1.376, 2.064, 2.975, 3.446 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_034R1", "In the past 7 days,", "I felt dizzy.", "", 2.257, new double[] { 0.993, 1.754, 2.606, 3.438 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_035R1", "In the past 7 days,", "My breathing was fast.", "", 2.432, new double[] { 1.182, 1.91, 2.892, 3.389 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_037R1", "In the past 7 days,", "My hands shook.", "", 2.368, new double[] { 1.3, 2.089, 2.782, 3.241 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_038R1", "In the past 7 days,", "My legs shook.", "", 2.52, new double[] { 1.402, 2.089, 2.807, 3.245 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_039R1", "In the past 7 days,", "My body shook.", "", 2.254, new double[] { 1.36, 2.119, 2.87, 3.599 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_042R1", "In the past 7 days,", "I had a headache.", "", 1.34, new double[] { -0.117, 1.025, 2.511, 3.73 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_043R1", "In the past 7 days,", "My neck hurt.", "", 1.973, new double[] { 0.942, 1.77, 2.673, 3.472 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_044R1", "In the past 7 days,", "My back hurt.", "", 1.626, new double[] { 0.427, 1.305, 2.337, 3.095 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_045R1", "In the past 7 days,", "I had a bad stomach ache.", "", 1.399, new double[] { 0.537, 1.663, 2.851, 4.166 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_046R1", "In the past 7 days,", "I had pain that really bothered me.", "", 1.824, new double[] { 0.685, 1.499, 2.4, 3.18 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_048R1", "In the past 7 days,", "My appetite changed.", "", 1.794, new double[] { 0.875, 1.628, 2.608, 3.541 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_049R1", "In the past 7 days,", "I felt some food coming up into my throat.", "", 1.765, new double[] { 1.282, 2.206, 3.264, 3.885 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_055R1", "In the past 7 days,", "I threw up.", "", 1.486, new double[] { 1.684, 2.653, 3.798, 4.672 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_064R1", "In the past 7 days,", "I had trouble staying asleep.", "", 1.334, new double[] { 0.48, 1.48, 2.577, 3.484 }, -1, "",
          response("Never", 1),
          response("Rarely", 2),
          response("Sometimes", 3),
          response("Often", 4),
          response("Always", 5)
      ),
      item("EoS_S_072R1", "In the past 7 days,", "My neck felt tight.", "", 1.851, new double[] { 0.889, 1.681, 2.678, 3.483 }, -1, "",
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
