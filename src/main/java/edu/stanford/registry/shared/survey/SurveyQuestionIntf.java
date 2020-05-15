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

public interface SurveyQuestionIntf {

  int getNumber();

  void setNumber(int questionNumber);

  String getId();

  void setId(String questionId);

  ArrayList<String> getText();

  ArrayList<SurveyAnswerIntf> getAnswers();

  ArrayList<SurveyAnswerIntf> getAnswers(boolean onlySelected);

  boolean getAnswered();

  void setAnswered(boolean ans);

  Set<String> getAttributeKeys();

  String getAttribute(String key);

  void setAttribute(String key, String value);
}
