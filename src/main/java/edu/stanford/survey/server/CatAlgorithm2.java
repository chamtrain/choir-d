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

package edu.stanford.survey.server;

import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm.Score;

import java.util.List;

/**
 * Encapsulate the logic for conducting an assessment using computer adaptive testing
 * (item response theory).
 */
public interface CatAlgorithm2 {
  /**
   * Prepare this algorithm for use with the given item bank. Must be called before
   * nextItem() or score(). Use this version if you don't want to use prior thetas.
   * Implementations should allow this method to be safely called multiple times to
   * reuse an instance of the algorithm and should make sure prior theta is deemed
   * to be zero.
   */
  CatAlgorithm2 initialize(ItemBank itemBank);

  /**
   * Prepare this algorithm for use with the given item bank. Must be called before
   * nextItem() or score().
   * Implementations should allow this method to be safely called multiple times to
   * reuse an instance of the algorithm.
   *
   * @param priorTheta use zero if there is no prior or you don't want to use it
   */
  CatAlgorithm2 initialize(ItemBank itemBank, double priorTheta);

  /**
   * Access the item bank provided to the initialize() call.
   */
  ItemBank bank();

  /**
   * Calculate the current score based on the responses given so far.
   */
  Score score(List<Response> currentAssessmentResponses);

  /**
   * Determine what item should be administered next based on the responses given so far.
   *
   * @return the next item, or null if we are done with this assessment
   */
  Item nextItem(List<Response> currentAssessmentResponses);

  /**
   * @param currentAssessmentResponses answers provided so far, to be included in scoring and question selection
   * @param currentIgnored items that have been skipped, to be excluded from scoring and question selection
   */
  Item nextItem(List<Response> currentAssessmentResponses, List<Item> currentIgnored);
}
