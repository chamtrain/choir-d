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

package edu.stanford.survey.client.api;

/**
 * AutoBean for the result submitted from a RadiosetPage.
 */
public interface BodyMapAnswer {
  /**
   * An enumeration of the body map areas selected (the area ids).
   *
   * @return comma separated list of area ids, or null if none selected
   */
  String getRegionsCsv();

  void setRegionsCsv(String choice);
}
