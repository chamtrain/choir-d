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

package edu.stanford.registry.shared.survey;

import java.util.ArrayList;
import java.util.Set;

public interface SurveyAnswerIntf {

  ArrayList<String> getText();

  void addText(String answer);

  int getType();

  void setType(int type);

  Set<String> getAttributeKeys();

  String getAttribute(String key);

  void setAttribute(String key, String value);

  ArrayList<String> getResponse();

  boolean getSelected();

  /**
   * The id uniquely identifies the answer it should contain all fields necessary to reconstruct the answer upon return
   */
  String getClientId();

  void setClientId(String id);

}
