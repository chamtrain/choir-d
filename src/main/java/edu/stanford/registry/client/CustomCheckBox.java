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

import edu.stanford.registry.client.survey.SurveyFormData;
import edu.stanford.registry.client.survey.SurveyFormDataIntf;

import com.google.gwt.user.client.ui.CheckBox;

public class CustomCheckBox extends CheckBox implements FormCustomDataIntf, SurveyFormDataIntf {

  private FormData myExtraData = new FormData();
  private SurveyFormData surveyFormData = null;
  private String type;
  private boolean originalValueIsChecked;

  @Override
  public FormData getCustomData() {
    return myExtraData;
  }

  public String getType() {
    return type;
  }

  public void setType(String typ) {
    type = typ;
  }

  public void setOriginalValue(boolean isChecked) {
    originalValueIsChecked = isChecked;
    setValue(isChecked);
  }

  public boolean hasChanged() {
    if (getValue() && !originalValueIsChecked) {
      return true;
    }
    if (!getValue() && originalValueIsChecked) {
      return true;
    }
    return false;
  }

  @Override
  public void setFormData(SurveyFormData formData) {
    this.surveyFormData = formData;
  }

  @Override
  public SurveyFormData getFormData() {
    return surveyFormData;
  }

  @Override
  public boolean hasFormData() {
    if (surveyFormData != null) {
      return true;
    }
    return false;
  }

  @Override
  public void reset() {
    setValue(false);
  }
}
