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

package edu.stanford.registry.client.survey;

import edu.stanford.registry.client.api.SurveyBuilderFormResponse;
import edu.stanford.survey.client.api.FieldType;

import com.google.gwt.core.client.GWT;

public class SurveyResponseBuilderFactory {
  public SurveyResponseBuilder getResponseBuilder(SurveyBuilderFormResponse response, boolean edit) {
    if (FieldType.radios == response.getFieldType()) {
      GWT.log("returning new SurveyResponseBuilder");
      return new SurveyResponseBuilderRadio(response, edit);
    } else if (FieldType.checkboxes == response.getFieldType()) {
      return new SurveyResponseBuilderCheckbox(response, edit);
    } else if (FieldType.numericScale == response.getFieldType()) {
      return new SurveyResponseBuilderNumericScale(response, edit);
    } else if (FieldType.numericSlider == response.getFieldType()) {
      return new SurveyResponseBuilderNumericSlider(response, edit);
    } else if (FieldType.datePicker == response.getFieldType()) {
      return new SurveyResponseBuilderDatePicker(response, edit);
    } else if (FieldType.number == response.getFieldType() || FieldType.text == response.getFieldType()
        || FieldType.textArea == response.getFieldType()) {
      return new SurveyResponseBuilderInput(response, edit);
    } else if (FieldType.textBoxSet == response.getFieldType()) {
      return new SurveyResponseBuilderTextboxset(response, edit);
    } else if (FieldType.collapsibleContentField == response.getFieldType()) {
      return new SurveyResponseBuilderNoInput(response, edit);
    } else if (FieldType.dropdown == response.getFieldType()) {
      return new SurveyResponseBuilderDropDown(response, edit);
    } else if (FieldType.radioSetGrid == response.getFieldType()) {
      return new SurveyResponseBuilderGrid(response, edit );
    }
    return new SurveyResponseBuilderInput(response, edit);
  }
}
