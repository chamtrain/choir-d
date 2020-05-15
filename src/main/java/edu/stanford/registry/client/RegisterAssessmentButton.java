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

package edu.stanford.registry.client;

import com.google.gwt.user.client.ui.Button;

public class RegisterAssessmentButton extends Button {

  private String surveySystemName = null;
  private String formName = null;
  private String title = null;
  private String explanation = null;
  private int version = 1;

  public RegisterAssessmentButton(String str) {
    super(str);
  }

  /**
   * Get the value of survey system name
   *
   * @return surveySystem
   */
  public String getSurveySystemName() {
    return surveySystemName;
  }

  /**
   * Set the value of survey system name
   */
  public void setSurveySystemName(String ssn) {
    surveySystemName = ssn;
  }

  public String getFormName() {
    return formName;
  }

  /**
   * Set the forms name
   */
  public void setFormName(String name) {
    formName = name;
  }

  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Set the study title field
   */
  @Override
  public void setTitle(String title) {
    formName = title;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explain) {
    explanation = explain;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int versionInt) {
    version = versionInt;
  }

}
