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

import edu.stanford.survey.server.CatAlgorithm.Score;

/**
 * Simple wrapper for holding information about the results of an assessment.
 */
public class CatScore implements Score {
  private double theta;
  private final double score;
  private double se;

  /**
   * @param theta unscaled estimate of the latent trait
   * @param score estimate of latent trait ("the score")
   * @param se    standard error of the score
   */
  public CatScore(double theta, double score, double se) {
    this.theta = theta;
    this.score = score;
    this.se = se;
  }

  @Override
  public double theta() {
    return theta;
  }

  @Override
  public double score() {
    return score;
  }

  @Override
  public double standardError() {
    return se;
  }

  @Override
  public String toString() {
    return "theta=" + theta + ", score=" + score + ", se=" + se;
  }
}
