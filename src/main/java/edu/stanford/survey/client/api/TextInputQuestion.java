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

import java.util.List;

/**
 * AutoBean for the data to be displayed on a RadiosetPage.
 */
public interface TextInputQuestion {
  String getTitle1();

  void setTitle1(String title);

  String getTitle2();

  void setTitle2(String title);

  List<String> getTextBoxLabels();

  void setTextBoxLabels(List<String> choices);

  String getServerValidationMessage();

  void setServerValidationMessage(String message);
}
