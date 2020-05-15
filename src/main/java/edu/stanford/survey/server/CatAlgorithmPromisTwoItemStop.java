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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;

/**
 * A computer adaptive testing algorithm for use with PROMIS instruments.
 *
 * The intent of this implementation is to be compatible with Northwestern except
 * for the addition of a special stopping rule validated by Karon Cook: if subject
 * answers most extreme selection towards "best health" for the first two items,
 * stop the assessment and deem the score to be 1 SD towards best health.
 * <p/>
 * This implementation currently just stops after the two items with whatever is
 * the current score (does not deem the score to be anything particular). See
 * comments in the code below for details.
 * <p/>
 * This implementation implements CatAlgorithm2, but does not honor priors or
 * item skipping (those parameters are ignored if provided).
 */
public class CatAlgorithmPromisTwoItemStop implements CatAlgorithm2 {
  private static final Logger log = Logger.getLogger(CatAlgorithmPromisTwoItemStop.class);
  private ItemBank itemBank;
  private double[] thetas;
  private double[] prior;
  private Map<String, double[]> itemCodeToIcc = new HashMap<>();
  private Map<String, double[][]> itemCodeToLpMatrix = new HashMap<>();

  @Override
  public ItemBank bank() {
    return itemBank;
  }

  @Override
  public CatAlgorithm2 initialize(ItemBank itemBank) {
    return initialize(itemBank, 0);
  }

  @Override
  public CatAlgorithm2 initialize(ItemBank itemBank, double priorTheta) {
    this.itemBank = itemBank;

    double thetaMin = -4;
    double thetaMax = 4;
    double thetaIncrement = 0.1;
    int nTheta = (int) ((thetaMax - thetaMin) / thetaIncrement) + 1;
    thetas = new double[nTheta];
    for (int i = 0; i < nTheta; i++) {
      thetas[i] = thetaMin + thetaIncrement * (double) i;
    }
    thetas[nTheta - 1] = thetaMax;

    prior = new double[thetas.length];
    double cumsum = 0;
    for (int i = 0; i < thetas.length; i++) {
      prior[i] = new NormalDistribution().density(thetas[i]);
      cumsum += prior[i];
    }
    for (int i = 0; i < thetas.length; i++) {
      prior[i] /= cumsum;
    }

    // Calculate the Item Characteristic Curve (ICC)
    double d = 1;
    for (Item item : itemBank.items()) {
      int nCategory =  0;
      for (Response r : item.responses()) {
        if (r.difficulty() < 1 || r.difficulty() > item.betas().length + 1) {
          throw new IllegalArgumentException("Item " + item.code() + " has a response with a difficulty out of range");
        }
        nCategory = Math.max(nCategory, r.difficulty());
      }

      double[][] pMatrix = new double[nTheta][];
      double[][] lpMatrix = new double[nTheta][];
      double[] icc = new double[nTheta];
      double alpha = item.alpha();

      for (int i = 0; i < nTheta; i++) {
        pMatrix[i] = new double[nCategory];
        lpMatrix[i] = new double[nCategory];
        double[] pstar = new double[nCategory + 1];
        pstar[0] = 1;
        for (int j = 0; j < nCategory - 1; j++) {
          double b = item.betas()[j];
          pstar[j + 1] = Math.exp(d * alpha * (thetas[i] - b)) / (1 + Math.exp(d * alpha * (thetas[i] - b)));
        }
        pstar[nCategory] = 0;

        for (int j = 0; j < nCategory; j++) {
          pMatrix[i][j] = pstar[j] - pstar[j + 1];
          lpMatrix[i][j] = Math.log(pMatrix[i][j]);
        }

        icc[i] = 0;
        for (int j = 0; j < nCategory; j++) {
          icc[i] += Math.pow((pstar[j] * (1 - pstar[j])) - (pstar[j + 1] * (1 - pstar[j + 1])), 2) / (pstar[j] - pstar[j
              + 1]);
        }
        icc[i] *= Math.pow(d * alpha, 2);
      }
      itemCodeToIcc.put(item.code(), icc);
      itemCodeToLpMatrix.put(item.code(), lpMatrix);
    }

    return this;
  }

  @Override
  public Item nextItem(List<Response> currentAssessmentResponses) {
    return nextItem(currentAssessmentResponses, null);
  }

  @Override
  public Item nextItem(List<Response> currentAssessmentResponses, List<Item> currentIgnored) {
    if (currentAssessmentResponses != null && currentAssessmentResponses.size() == 2) {
      // TODO this isn't correct yet, as it doesn't account for the direction (might be last index
      // that represents "best health"
      if (currentAssessmentResponses.get(0).index() == 0 && currentAssessmentResponses.get(1).index() == 0) {
        return null;
      }
    }

    if (currentAssessmentResponses != null && currentAssessmentResponses.size() >= itemBank.minItems()) {
      Score currentScore = score(currentAssessmentResponses);
      if (currentScore.standardError() <= itemBank.minError()
          || currentAssessmentResponses.size() >= itemBank.maxItems()) {
        return null;
      }
    }

    double[] loglik = logLikelihood(currentAssessmentResponses);

    double cumsumDen = 0;
    for (int i = 0; i < thetas.length; i++) {
      cumsumDen += Math.exp(loglik[i]) * prior[i];
    }

    double[] posterior = new double[thetas.length];
    for (int i = 0; i < thetas.length; i++) {
      posterior[i] = Math.exp(loglik[i]) * prior[i] / cumsumDen;
    }

    List<WeightedItem> weightedItems = new ArrayList<>();
    for (Item item : itemBank.items()) {
      if (alreadySeen(currentAssessmentResponses, item)) {
        continue;
      }
      double informationContent = 0;
      for (int i = 0; i < thetas.length; i++) {
        informationContent += itemCodeToIcc.get(item.code())[i] * posterior[i];
      }
      weightedItems.add(new WeightedItem(item, informationContent));
    }
    Collections.sort(weightedItems);
    if (log.isTraceEnabled()) {
      log.trace("Items: " + weightedItems);
    }

    if (weightedItems.isEmpty()) {
      log.warn("Ran out of questions in the item bank");
      return null;
    }

    return weightedItems.get(0).getItem();
  }

  @Override
  public Score score(List<Response> currentAssessmentResponses) {
    // TODO to match Karon's rule we would need to adjust scoring to deem score = 40 for 2 item rule case
    double[] loglik = logLikelihood(currentAssessmentResponses);

    double meanNum = 0, cumsumDen = 0, variance = 0;
    for (int i = 0; i < thetas.length; i++) {
      double den = Math.exp(loglik[i]) * prior[i];
      meanNum += thetas[i] * den;
      cumsumDen += den;
    }
    double theta = meanNum / cumsumDen;

    double[] posterior = new double[thetas.length];
    for (int i = 0; i < thetas.length; i++) {
      posterior[i] = Math.exp(loglik[i]) * prior[i] / cumsumDen;
    }

    for (int i = 0; i < thetas.length; i++) {
      variance += Math.pow(thetas[i] - theta, 2) * posterior[i];
    }
    double se = Math.sqrt(variance) * 10.0;
    double score = theta * 10.0 + 50.0;

    return new CatScore(theta, score, se);
  }

  private double[] logLikelihood(List<Response> currentAssessmentResponses) {
    double[] loglik = new double[thetas.length];
    if (currentAssessmentResponses != null) {
      for (Response response : currentAssessmentResponses) {
        for (int i = 0; i < loglik.length; i++) {
          // Note the shift to one -1 difficulty: IMPORTANT!!!!
          loglik[i] += itemCodeToLpMatrix.get(response.item().code())[i][response.difficulty() - 1];
        }
      }
    }
    return loglik;
  }

  private boolean alreadySeen(List<Response> currentAssessmentResponses, Item item) {
    if (currentAssessmentResponses != null) {
      for (Response response : currentAssessmentResponses) {
        if (response.item().code().equals(item.code())) {
          return true;
        }
      }
    }
    return false;
  }
}
