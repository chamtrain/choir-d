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

package edu.stanford.survey.test;

import edu.stanford.survey.server.CatAlgorithm;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm.Score;
import edu.stanford.survey.server.CatAlgorithm2;
import edu.stanford.survey.server.CatAlgorithmPromis;
import edu.stanford.survey.server.CatAlgorithmPromisTwoItemStop;
import edu.stanford.survey.server.CatAlgorithmStanford;
import edu.stanford.survey.server.CatAlgorithmStanfordPrior;
import edu.stanford.survey.server.promis.PromisAnger;
import edu.stanford.survey.server.promis.PromisDepression;
import edu.stanford.survey.server.promis.PromisDepression2;
import edu.stanford.survey.server.promis.PromisPhysicalFunction2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

import junit.framework.TestCase;

/**
 * Unit tests for PROMIS computer adaptive testing.
 */
public class CatAlgorithmPromisTest extends TestCase {
  static {
    String log4jXmlFile = System.getProperty("log4j.configuration", "log4j.xml");
    if (log4jXmlFile.startsWith("file:")) {
      // The configure() call doesn't like URLs
      log4jXmlFile = log4jXmlFile.substring(5);
    }
    DOMConfigurator.configure(log4jXmlFile);
  }

  /**
   * Choose the first response on all physical function questions using
   * the standard PROMIS algorithm.
   */
  public void testPhysicalFunctionZeros() throws Exception {
    CatAlgorithm cat = new CatAlgorithmPromis();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "PFC12", 0, 57.8, 6.58);
    checkNext(cat, answers, "PFB7", 0, 60.9, 5.85);
    checkNext(cat, answers, "PFA1", 0, 62.6, 5.57);
    checkNext(cat, answers, "PFC7r1", 0, 67.1, 5.76);
    checkNext(cat, answers, "PFC33r1", 0, 71.1, 5.61);
    checkNext(cat, answers, "PFA39r1", 0, 72.6, 5.25);
    checkNext(cat, answers, "PFA19r1", 0, 72.76112376772693, 5.1883950283302935);
    checkNext(cat, answers, "PFC35", 0, 72.84172441597576, 5.14812218028757);
    checkNext(cat, answers, "PFC13r1", 0, 72.90958424914663, 5.113548855809715);
    checkNext(cat, answers, "PFA33", 0, 72.97205868096536, 5.087221775099035);
    checkNext(cat, answers, "PFA13", 0, 73.01536435752392, 5.0678079246870205);
    checkNext(cat, answers, "PFB51", 0, 73.04508732063198, 5.053690700856052);
    checkDone(cat, answers);
  }

  public void testAngerTwoItemStop() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmPromisTwoItemStop();
    cat.initialize(PromisAnger.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDANG30", 0, 38.9, 6.69);
    checkNext(cat, answers, "EDANG09", 0, 36.0, 5.78);
    checkDone(cat, answers);
  }

  /**
   * Choose the first response on all physical function questions using
   * the Stanford algorithm.
   */
  public void testPhysicalFunctionZerosStanford() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmStanford();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "PFC12", 0, 57.8, 6.58);
    checkNext(cat, answers, "PFB7", 0, 60.9, 5.85);
    checkNext(cat, answers, "PFA1", 0, 62.6, 5.57);
    checkNext(cat, answers, "PFC7r1", 0, 67.1, 5.76);
    checkNext(cat, answers, "PFC33r1", 0, 71.1, 5.61);
    checkDone(cat, answers);
  }

  /**
   * Choose the first response on all physical function questions using
   * the Stanford algorithm.
   */
  public void testPhysicalFunctionSkipping() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmStanford();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();
    List<Item> ignore = new ArrayList<>();

    checkNext(cat, answers, ignore, "PFC12", 0, 57.8, 6.58);
    ignore.add(cat.bank().item("PFB7"));
    checkNext(cat, answers, ignore, "PFB5r1", 0, 59.9, 5.974411791066282);
    checkNext(cat, answers, ignore, "PFA1", 0, 62.0, 5.70);
    ignore.add(cat.bank().item("PFC7"));
    checkNext(cat, answers, ignore, "PFC7r1", 0, 66.89893714289028, 5.8743232176706055);
    ignore.add(cat.bank().item("PFC33r1"));
//    checkDone(cat, answers, ignore);
  }

  /**
   * Choose the first response on all physical function questions using
   * the Stanford algorithm and prior assessment.
   */
  public void testPhysicalFunctionZerosStanfordPrior() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmStanfordPrior();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    // Same inputs as the test above for the prior assessment
    List<Response> prior = new ArrayList<>();
    prior.add(cat.bank().item("PFC12").responses()[0]);
    prior.add(cat.bank().item("PFB7").responses()[0]);
    prior.add(cat.bank().item("PFA1").responses()[0]);
    prior.add(cat.bank().item("PFC7r1").responses()[0]);
    prior.add(cat.bank().item("PFC33r1").responses()[0]);

    // Initialize CAT with theta from the prior
    reinitialize(cat, prior);

    // Stabilize quickly with prior theta bounded to (-2, 2)
    checkNext(cat, answers, "PFC7r1", 0, 74.9, 7.013006085588326);  // run 5 miles
    checkDone(cat, answers);

    reinitialize(cat, answers);

    answers.clear();
    checkNext(cat, answers, "PFC7r1", 0, 74.9, 7.013006085588326);  // run 5 miles
    checkDone(cat, answers);

    answers.clear();
    checkNext(cat, answers, "PFC7r1", 1, 66.4, 5.83005942102099);  // run 5 miles
    checkNext(cat, answers, "PFC33r1", 1, 67.4, 4.66);  // run 10 miles
    checkNext(cat, answers, "PFA39r1", 1, 66.2, 3.91);
    checkNext(cat, answers, "PFA1", 1, 62.181982085517106, 3.5259510817191755);
//    checkNext(cat, answers, "PFA1", 1, 61.2, 3.05);
//    checkNext(cat, answers, "PFB7", 1, 58.4, 2.62);
    checkDone(cat, answers);
  }

  /**
   * Choose the last response on all physical function questions.
   */
  public void testPhysicalFunctionFours() throws Exception {
    CatAlgorithmPromis cat = new CatAlgorithmPromis();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "PFC12", 4, 33.3, 5.33);
    checkNext(cat, answers, "PFA11", 4, 28.1, 4.45);
    checkNext(cat, answers, "PFB13", 4, 22.8, 4.24);
    checkNext(cat, answers, "PFC56", 4, 19.9, 3.67);
    checkNext(cat, answers, "PFA55", 4, 17.1, 3.35);
    checkNext(cat, answers, "PFC46", 4, 15.4, 2.95);
    checkDone(cat, answers);
  }

  /**
   * Choose the last response on all physical function questions.
   */
  public void testPhysicalFunctionFoursStanford() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmStanford();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "PFC12", 4, 33.3, 5.33);
    checkNext(cat, answers, "PFA11", 4, 28.1, 4.45);
    checkNext(cat, answers, "PFB13", 4, 22.8, 4.24);
    checkNext(cat, answers, "PFC56", 4, 19.9, 3.67);
    checkNext(cat, answers, "PFA55", 4, 17.1, 3.35);
    // Stop due to stability
//    checkNext(cat, answers, "PFC46", 4, 15.4, 2.95);
    checkDone(cat, answers);
  }

  /**
   * Choose the last response on all physical function questions.
   */
  public void testPhysicalFunctionFoursStanfordPrior() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmStanfordPrior();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "PFC12", 4, 33.3, 5.33);
    checkNext(cat, answers, "PFA11", 4, 28.1, 4.45);
    checkNext(cat, answers, "PFB13", 4, 22.8, 4.24);
    checkNext(cat, answers, "PFC56", 4, 19.9, 3.67);
    // Stop due to theta range
//    checkNext(cat, answers, "PFA55", 4, 17.1, 3.35);
//    checkNext(cat, answers, "PFC46", 4, 15.4, 2.95);
    checkDone(cat, answers);
  }

  public void testPhysicalFunctionOscillate() throws Exception {
    CatAlgorithmPromis cat = new CatAlgorithmPromis();
    cat.initialize(PromisPhysicalFunction2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "PFC12", 0, 57.8, 6.58);
    checkNext(cat, answers, "PFB7", 4, 47.3, 4.36);
    checkNext(cat, answers, "PFB5r1", 0, 51.8, 3.5159096662468152);
    checkNext(cat, answers, "PFA4", 4, 46.3, 3.71);
    checkNext(cat, answers, "PFC36r1", 0, 48.8, 2.8929333842627676);
    checkDone(cat, answers);
  }

//  Response	Theta	Score	SE
//  EDDEP29=1	-0.80	42.0	6.59
//  EDDEP36=1	-1.11	38.9	5.94
//  EDDEP17=1	-1.26	37.4	5.63
//  EDDEP26=1	-1.33	36.7	5.48
//  EDDEP46=1	-1.41	35.9	5.41
//  EDDEP54=1	-1.46	35.4	5.34
//  EDDEP31=1	-1.49	35.1	5.27
//  EDDEP28=1	-1.51	34.9	5.24
//  EDDEP50=1	-1.54	34.6	5.23
//  EDDEP23=1	-1.56	34.4	5.20
//  EDDEP30=1	-1.57	34.3	5.18
//  EDDEP21=1	-1.58	34.2	5.16
  public void testDepressionZeros() throws Exception {
    CatAlgorithm cat = new CatAlgorithmPromis();
    cat.initialize(PromisDepression2.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDDEP29", 0, 42.0, 6.59);
    checkNext(cat, answers, "EDDEP36", 0, 38.9, 5.94);
    checkNext(cat, answers, "EDDEP17", 0, 37.4, 5.63 );
    checkNext(cat, answers, "EDDEP26", 0, 36.7, 5.48);
    checkNext(cat, answers, "EDDEP46", 0, 35.9, 5.41);
    checkNext(cat, answers, "EDDEP54", 0, 35.4, 5.34);
    checkNext(cat, answers, "EDDEP31", 0, 35.1, 5.27);
    checkNext(cat, answers, "EDDEP28", 0, 34.9, 5.24);
    checkNext(cat, answers, "EDDEP50", 0, 34.6, 5.23);
    checkNext(cat, answers, "EDDEP23", 0, 34.4, 5.20);
    checkNext(cat, answers, "EDDEP30", 0, 34.3, 5.18);
    checkNext(cat, answers, "EDDEP21", 0, 34.2, 5.16);
    checkDone(cat, answers);
  }

//  Response	Theta	Score	SE
//  EDDEP29=3	0.83	58.3	4.32
//  EDDEP41=3	1.12	61.2	3.05
//  EDDEP04=3	1.19	61.9	2.47
//  EDDEP06=3	1.22	62.2	2.15
  public void testDepressionTwos() throws Exception {
    CatAlgorithm cat = new CatAlgorithmPromis();
    ItemBank bank = PromisDepression2.bank();
    cat.initialize(bank);
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDDEP29", 2, 58.3, 4.32);
    checkNext(cat, answers, "EDDEP41", 2, 61.2, 3.05);
    checkNext(cat, answers, "EDDEP04", 2, 61.9, 2.47);
    checkNext(cat, answers, "EDDEP06", 2, 62.2, 2.15);
    checkDone(cat, answers);
  }

  public void testDepression32124() throws Exception {
    CatAlgorithm cat = new CatAlgorithmPromis();
    ItemBank bank = PromisDepression.bank();
    cat.initialize(bank);
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDDEP29", 3, 65.0, 4.49);
    checkNext(cat, answers, "EDDEP41", 2, 64.8, 3.03);
    checkNext(cat, answers, "EDDEP04", 1, 62.2, 2.68);
    checkNext(cat, answers, "EDDEP06", 2, 62.4, 2.29);
    checkNext(cat, answers, "EDDEP05", 4, 64.4, 2.29);
    checkDone(cat, answers);
  }

  public void testDepressionFours() throws Exception {
    CatAlgorithm cat = new CatAlgorithmPromis();
    ItemBank bank = PromisDepression.bank();
    cat.initialize(bank);
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDDEP29", 4, 72.4, 5.3);
    checkNext(cat, answers, "EDDEP41", 4, 77.0, 4.01);
    checkNext(cat, answers, "EDDEP04", 4, 78.4, 3.64);
    checkNext(cat, answers, "EDDEP06", 4, 79.3, 3.46);
    checkNext(cat, answers, "EDDEP39", 4, 80.7, 3.41);
    checkNext(cat, answers, "EDDEP09", 4, 81.3, 3.3);
    checkNext(cat, answers, "EDDEP45", 4, 82.2, 3.22);
    checkNext(cat, answers, "EDDEP44", 4, 83.3, 3.14);
    checkNext(cat, answers, "EDDEP30", 4, 83.8, 3.04);
    checkNext(cat, answers, "EDDEP42", 4, 84.4, 2.94);
    checkDone(cat, answers);
  }

  public void testAnger() throws Exception {
    CatAlgorithm cat = new CatAlgorithmPromis().initialize(PromisAnger.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDANG30", 0, 38.9, 6.69);
    checkNext(cat, answers, "EDANG09", 0, 36.0, 5.78);
    checkNext(cat, answers, "EDANG35", 3, 42.7, 4.97);
    checkNext(cat, answers, "EDANG03", 3, 48.5, 5.23);
    checkNext(cat, answers, "EDANG06", 2, 52.5, 4.62);
    checkNext(cat, answers, "EDANG15", 2, 56.5, 3.87);
    checkNext(cat, answers, "EDANG25", 1, 57.1, 3.22);
    checkNext(cat, answers, "EDANG28", 2, 58.1, 2.86);
    checkDone(cat, answers);
  }

  public void testAngerStanford() throws Exception {
    CatAlgorithm2 cat = new CatAlgorithmStanford().initialize(PromisAnger.bank());
    List<Response> answers = new ArrayList<>();

    checkNext(cat, answers, "EDANG30", 0, 38.9, 6.69);
    checkNext(cat, answers, "EDANG09", 0, 36.0, 5.78);
    checkNext(cat, answers, "EDANG35", 3, 42.7, 4.97);
    checkNext(cat, answers, "EDANG03", 3, 48.5, 5.23);
//    checkNext(cat, answers, "EDANG06", 2, 52.5, 4.62);
//    checkNext(cat, answers, "EDANG15", 2, 56.5, 3.87);
//    checkNext(cat, answers, "EDANG25", 1, 57.1, 3.22);
//    checkNext(cat, answers, "EDANG28", 2, 58.1, 2.86);
    checkDone(cat, answers);
  }

  private void reinitialize(CatAlgorithm2 cat, List<Response> prior) {
    Score score = cat.score(prior);
    System.out.println("Re-initializing CAT using prior theta: " + score.theta());
    cat.initialize(PromisPhysicalFunction2.bank(), score.theta());
  }

  private void checkDone(CatAlgorithm cat, List<Response> answers) {
    Item item = cat.nextItem(answers);
    assertNull("Survey should have terminated, but returned item " + (item == null ? "null" : item.code()), item);
  }

  private void checkDone(CatAlgorithm2 cat, List<Response> answers) {
    checkDone(cat, answers, null);
  }

  private String printCheckNext(Item item, List<Response> answers, double score, double standardError) {
    int responseIndex;
    if (answers.isEmpty()) {
      responseIndex = 0;
    } else {
      responseIndex = answers.get(answers.size()-1).index();
    }
    return "checkNext(cat, answers, \""
        + (item == null ? "null" : item.code()) + "\", " + responseIndex + ", "
        + new BigDecimal(score).setScale(1, RoundingMode.HALF_UP) + ", "
        + new BigDecimal(standardError).setScale(2, RoundingMode.HALF_UP) + ");";
  }

  private void checkDone(CatAlgorithm2 cat, List<Response> answers, List<Item> ignore) {
    Item item = cat.nextItem(answers, ignore);
    String itemCode = item == null ? "null" : item.code();
    assertNull("Survey should have terminated, but returned item " + itemCode + ": "
        + printCheckNext(item, answers, 0, 0), item);
  }

  private void checkNext(CatAlgorithm cat, List<Response> answers, String expectedItemCode, int chooseResponse,
                         double eap, double se) {
    Item item = cat.nextItem(answers);
    Score score = cat.score(answers);

    assertNotNull("Survey should not have terminated yet", item);
    assertEquals("Survey returned wrong item: " + printCheckNext(item, answers,
        score.score(), score.standardError()), expectedItemCode, item.code());

    answers.add(item.responses()[chooseResponse]);
    score = cat.score(answers);
    System.out.println("Item " + answers.size() + ": " + item.code() + " chose: " + chooseResponse
        + " score: " + score);

    assertEquals("Incorrect score: " + printCheckNext(item, answers,
        score.score(), score.standardError()), eap, score.score(), 0.05);
    assertEquals("Incorrect standard error: " + printCheckNext(item, answers,
        score.score(), score.standardError()), se, score.standardError(), 0.005);
  }

  private void checkNext(CatAlgorithm2 cat, List<Response> answers, String expectedItemCode,
                         int chooseResponse, double eap, double se) {
    checkNext(cat, answers, null, expectedItemCode, chooseResponse, eap, se);
  }

  private void checkNext(CatAlgorithm2 cat, List<Response> answers, List<Item> ignore, String expectedItemCode,
                         int chooseResponse, double eap, double se) {
    Item item = cat.nextItem(answers, ignore);
    Score score = cat.score(answers);

    assertNotNull("Survey should not have terminated yet", item);
    assertEquals("Survey returned wrong item: " + printCheckNext(item, answers,
        score.score(), score.standardError()), expectedItemCode, item.code());

    answers.add(item.responses()[chooseResponse]);
    score = cat.score(answers);
    System.out.println("Item " + answers.size() + ": " + item.code() + " chose: " + chooseResponse
        + " score: " + score);

    assertEquals("Incorrect score: " + printCheckNext(item, answers,
        score.score(), score.standardError()), eap, score.score(), 0.05);
    assertEquals("Incorrect standard error: " + printCheckNext(item, answers,
        score.score(), score.standardError()), se, score.standardError(), 0.005);
  }
}
