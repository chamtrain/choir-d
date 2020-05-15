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

import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker;

public class SurveyResponseBuilderDatePicker extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

   SurveyResponseBuilderDatePicker(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean build) {
    super(surveyBuilderFormResponse, build);
  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    Row fieldRow = new Row();
    StyleHelper.addEnumStyleName(fieldRow, ColumnSize.MD_2);
    final DatePicker picker  = new DatePicker();
    picker.setVisible(true);
    final Panel panel = new Panel();
    panel.add(picker);
    fieldRow.add(panel);
    showResponses.add(fieldRow);
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    Div thisForm = new Div();
    if (response != null ) {
      formResponse.setValues(response.getValues());
      formResponse.setFieldType(response.getFieldType());
      formResponse.setAttributes(response.getAttributes());
      if (response.getRequired() != null) {
        formResponse.setRequired(response.getRequired());
      }
      formResponse.setOrder(response.getOrder());
      formResponse.setLabel(response.getLabel());
      formResponse.setRef(response.getRef());
      InputGroup inputGroup = getNewResponse();
      inputGroups.add(inputGroup);
      thisForm.add(inputGroup);
    }
    return thisForm;
  }

  @Override
  public InputGroup refreshResponse(int index) {
    return null; // up & down buttons n/a to input
  }

  @Override
  public InputGroup getNewResponse() {

    final InputGroup inputGroup = new InputGroup();
    final DatePicker picker  = new DatePicker();
    final Panel panel = new Panel();
    panel.add(picker);
    inputGroup.add(panel);
    return inputGroup;
  }

  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    return new ArrayList<>();
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    Map<String, String> attributes = formResponse.getAttributes();
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    attributes.put("useFocus","true" );
    attributes.put("inlineBlind","true");
    formResponse.setAttributes(attributes);
    return formResponse;
  }

  @Override
  public String getRequiredText() {
    return "Please choose a date";
  }

  @Override
  public boolean hasLabel() {
    return true;
  }
}
