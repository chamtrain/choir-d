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
 * AutoBean for the data to be displayed on a RadiosetPage.
 */
public interface SliderQuestion {
  String getTitle1();

  void setTitle1(String title);

  String getTitle2();

  void setTitle2(String title);

  int getLowerBound();

  void setLowerBound(int number);

  String getLowerBoundLabel();

  void setLowerBoundLabel(String label);

  int getUpperBound();

  void setUpperBound(int number);

  String getUpperBoundLabel();

  void setUpperBoundLabel(String label);

  boolean getRequired();
  void setRequired(boolean required);

  int getValue();
  void setValue(int value);

  boolean getShowValue();
  void setShowValue(boolean showValue);
}
