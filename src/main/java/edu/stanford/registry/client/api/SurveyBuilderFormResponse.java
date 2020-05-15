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

import edu.stanford.survey.client.api.FieldType;

import java.util.List;
import java.util.Map;

public interface SurveyBuilderFormResponse {

  void setFieldType(FieldType fieldType);

  FieldType getFieldType();

  void setLabel(String label);

  String getLabel();

  Boolean getRequired();

  void setRequired(boolean isRequired);

  void setRef(String label);

  String getRef();

  int getOrder();

  void setOrder(int order);

  List<SurveyBuilderFormFieldValue> getValues();

  void setValues(List<SurveyBuilderFormFieldValue>  formFieldValues);

  Map<String, String> getAttributes();

  void setAttributes( Map<String, String> attributes);
}
