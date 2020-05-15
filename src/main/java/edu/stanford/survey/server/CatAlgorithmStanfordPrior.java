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
 * A computer adaptive testing algorithm for use with PROMIS instruments. This
 * version is intended to be compatible with {@link edu.stanford.survey.server.CatAlgorithmStanford}
 * while additionally allowing use of theta from a prior assessment to further reduce
 * the number of items administered.
 * <p/>
 * This is highly experimental, and it isn't clear whether it will be a good idea because
 * it may disturb the longitudinal qualities of the scores.
 */
public class CatAlgorithmStanfordPrior implements CatAlgorithm2 {
  private static final Logger log = Logger.getLogger(CatAlgorithmStanfordPrior.class);
  private ItemBank itemBank;
  private double[] thetas;
  private double[] prior;
  private Map<String, double[]> itemCodeToIcc = new HashMap<>();
  private Map<String, double[][]> itemCodeToLpMatrix = new HashMap<>();
  private int minItemsForThetaStop;

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

    // Don't allow extreme values for prior mean to prevent drifting ever further toward the edges
    if (priorTheta < -2) {
      priorTheta = -2;
    }
    if (priorTheta > 2) {
      priorTheta = 2;
    }

    // For cases where we have a prior, reduce the minimum items used to terminate extreme thetas
    if (priorTheta > 0.0001 || priorTheta < -0.0001) {
      minItemsForThetaStop = 1;
    } else {
      minItemsForThetaStop = itemBank.minItems();
    }

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
      prior[i] = new NormalDistribution(priorTheta, 1).density(thetas[i]);
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
    if (currentAssessmentResponses != null && currentAssessmentResponses.size() >= minItemsForThetaStop) {
      Score currentScore = score(currentAssessmentResponses);
      if (currentScore.theta() >= 2 || currentScore.theta() <= -2) {
        // Terminate at fringes regardless of minimum number of items
        return null;
      }
      if (currentAssessmentResponses.size() >= itemBank.minItems()) {
        if (currentScore.standardError() <= itemBank.minError()
            || currentAssessmentResponses.size() >= itemBank.maxItems()) {
          return null;
        }
        // Stop if both score and SE are fairly stable from last response to this one
        if (currentAssessmentResponses.size() > 1) {
          List<Response> previousResponses = new ArrayList<>();
          previousResponses.addAll(currentAssessmentResponses);
          previousResponses.remove(previousResponses.size()-1);
          Score previousScore = score(previousResponses);
          if (Math.abs(currentScore.theta()-previousScore.theta())/previousScore.theta() < 0.3 &&
              Math.abs(currentScore.standardError()-previousScore.standardError())/previousScore.standardError() < 0.1) {
            log.trace("Terminating because theta and se are stable");
            return null;
          }
        }
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
      if (currentIgnored != null && currentIgnored.contains(item)) {
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

    if (log.isTraceEnabled()) {
      List<Response> upper = new ArrayList<>();
      if (currentAssessmentResponses != null) {
        upper.addAll(currentAssessmentResponses);
      }
      List<Response> lower = new ArrayList<>(upper);
      upper.add(weightedItems.get(0).getItem().responses()[0]);
      Score upperScore = score(upper);
      lower.add(weightedItems.get(0).getItem().responses()[weightedItems.get(0).getItem().responses().length-1]);
      Score lowerScore = score(lower);
      if (log.isTraceEnabled()) {
        log.trace("Picked Item: " + weightedItems.get(0) + " potential range: " + upperScore.score()
            + " to " + lowerScore.score());
      }
    }

    return weightedItems.get(0).getItem();
  }

  @Override
  public Score score(List<Response> currentAssessmentResponses) {
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
