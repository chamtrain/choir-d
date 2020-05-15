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

package edu.stanford.registry.shared;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public interface ChartScore {

  String getPatientId();

  Integer getStudyCode();

  String getStudyDescription();

  Date getDate();

  String getCategoryLabel();

  boolean getAssisted();

  void setAssisted(boolean isAssisted);

  boolean wasReplaced();

  void setReplaced(boolean wasReplaced);

  /**
   * Return the single score value. 
   */
  BigDecimal getScore();

  /**
   * Return a map of all the score values.
   * 
   * If the ChartScore only contains a single score value then the map
   * will contain a single pair. Generally this will be the pair
   * {getStudyDescription(), getScore()}.
   * 
   * If the ChartScore contains multiple score values then the map
   * will contain multiple pairs of the form
   * {score_value_name, score_value}.
   */
  Map<String,BigDecimal> getScores();

  ChartScore clone();

}
