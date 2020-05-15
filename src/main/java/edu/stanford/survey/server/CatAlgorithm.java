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

import java.util.List;

/**
 * Encapsulate the logic for conducting an assessment using computer adaptive testing
 * (item response theory).
 */
public interface CatAlgorithm {
  interface ItemBank {
    Item[] items();

    double priorAlpha();

    double priorBeta();

    /**
     * Convenience method for finding a particular item by code.
     *
     * @param itemCode value returned by Item.code()
     * @return the item from this bank with the specified code, or null if none could be found
     */
    Item item(String itemCode);

    /**
     * Convenience method to find a response. Included in the interface because the implementation
     * should provide an efficient implementation of this that takes some burden off the client.
     *
     * @param itemCode     value returned from Item.code()
     * @param responseText value returned from Response.text()
     * @return the Response from this bank corresponding to the provided parameters, or null
     * if none could be found
     */
    Response response(String itemCode, String responseText);

    /**
     * Do not stop until at least this many items have been administered, regardless of standard error.
     */
    int minItems();

    /**
     * Stop after administering this many items, regardless of standard error.
     */
    int maxItems();

    /**
     * Stop if the standard error dips below this value, within the bounds of min/max items above.
     */
    double minError();
  }

  interface Item {
    /**
     * Link to the item bank to which this is a part.
     */
    ItemBank bank();

    /**
     * Unique identifier for this item.
     */
    String code();

    /**
     * Pre-amble for the question (for example, "In the last 7 days...").
     */
    String context();

    /**
     * Question to be answered (for example, "How often did you eat an apple?").
     */
    String prompt();

    /**
     * A shorter summary of the prompt to be used for reporting answers.
     */
    String promptBrief();

    /**
     * Ordered list of possible responses to the question.
     */
    Response[] responses();

    /**
     * Convenience method for finding a specific response.
     *
     * @param text value returned by text()
     * @return the response, or null if none could be found
     */
    Response response(String text);

    /**
     * Intercept for the logistic model.
     */
    double alpha();

    /**
     * Weighting for each parameter of the logistic model, with parameters being
     * the distance between one response and the next higher difficulty response.
     */
    double[] betas();

    /**
     * Used to split items into categories to perform alpha-stratification.
     */
    int strata();

    String category();
  }

  interface Response {
    Item item();

    String text();

    int difficulty();

    int index();
  }

  interface Score {
    double theta();

    double score();

    double standardError();
  }

  ItemBank bank();

  CatAlgorithm initialize(ItemBank itemBank);

  Item nextItem(List<Response> currentAssessmentResponses);

  Score score(List<Response> currentAssessmentResponses);
}
