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

import java.util.Map;

/**
 * AutoBean for a survey page component configuration values.
 */
public interface SurveyPageComponent {

  String getComponentName();

  void setComponentName(String type);

  String getComponentType();

  void setComponentType(String type);

  String getComponentHeading();

  void setComponentHeading(String heading);

  Map <String, String> getAttributes();
  void setAttributes(Map <String, String> attributes);
}
