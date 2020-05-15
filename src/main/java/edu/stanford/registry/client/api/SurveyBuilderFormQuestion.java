/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.client.api;

import edu.stanford.survey.client.api.FormQuestion;

import java.util.List;

public interface SurveyBuilderFormQuestion extends FormQuestion {

  String getOrder();

  void setOrder(String order);

  String getId();

  void setId(String id);

  String getConditionsType();
  
  void setConditionsType(String type);
  
  List<SurveyBuilderFormCondition> getConditions();
  void setConditions(List<SurveyBuilderFormCondition> conditions);

  List<SurveyBuilderFormResponse> getResponses();
  void setResponses(List<SurveyBuilderFormResponse> responses);
}
