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
import java.util.List;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.HandleType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

public class SurveyResponseBuilderNumericSlider extends SurveyResponseBuilder implements SurveyResponseBuilderIntf {

  public SurveyResponseBuilderNumericSlider(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
    super(surveyBuilderFormResponse, forEdit);
  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    Row displayRow = new Row();
    final TextBox lowerBoundBox = new TextBox();
    lowerBoundBox.setEnabled(false);
    displayRow.add(FormWidgets.textBoxColumn(lowerBoundBox, Integer.toString(getLowerBoundId()), ColumnSize.MD_1));

    Slider slider = new Slider();
    slider.setMin(getLowerBoundId());
    slider.setMax(getUpperBoundId());
    slider.setWidth("100%");
    slider.setHandle(HandleType.SQUARE);
    slider.setEnabled(false);

    Column sliderCol = new Column(ColumnSize.MD_7);
    sliderCol.add(slider);
    displayRow.add(sliderCol);

    final TextBox upperBoundBox = new TextBox();
    upperBoundBox.setEnabled(false);
    displayRow.add(FormWidgets.textBoxColumn(upperBoundBox, Integer.toString(getUpperBoundId()), ColumnSize.MD_1));

    showResponses.add(displayRow);
    return showResponses;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    Div container = new Div();
    container.setWidth("100%");
    formResponse.setValues(response.getValues());
    InputGroup newResponse = getNewResponse();
    container.add(newResponse);
    inputGroups.add(newResponse);
    return container;
  }

  @Override
  public InputGroup getNewResponse() {
    InputGroup thisGroup = new InputGroup();
    buildResponse(thisGroup);
    return thisGroup;
  }

  private void buildResponse(InputGroup thisGroup) {
    thisGroup.clear();
    thisGroup.setWidth("100%");
    if (getUpperBoundId() == getLowerBoundId()) {
      setUpperBoundId(getLowerBoundId() + 10);
    }

    Row displayRow = new Row();
    final TextBox lowerBoundBox = new TextBox();
    displayRow.add(FormWidgets.textBoxColumn(lowerBoundBox, Integer.toString(getLowerBoundId()), ColumnSize.MD_1));
    lowerBoundBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setLowerBoundId(Integer.parseInt(lowerBoundBox.getText()));
      }
    });

    Slider slider = new Slider();
    slider.setMin(getLowerBoundId());
    slider.setMax(getUpperBoundId());
    slider.setWidth("100%");
    slider.setHandle(HandleType.SQUARE);
    slider.setEnabled(false);

    Column sliderCol = new Column(ColumnSize.MD_10);
    sliderCol.add(slider);
    displayRow.add(sliderCol);

    final TextBox upperBoundBox = new TextBox();
    displayRow.add(FormWidgets.textBoxColumn(upperBoundBox, Integer.toString(getUpperBoundId()) , ColumnSize.MD_1));
    upperBoundBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setUpperBoundId(Integer.parseInt(upperBoundBox.getText()));
      }
    });
    thisGroup.add(displayRow);
  }

  @Override
  public InputGroup refreshResponse(int inx) {
    if (inputGroups.size() > inx) {
      InputGroup inputGroup = inputGroups.get(inx);
      buildResponse(inputGroup);
      return inputGroup;
    }
    return getNewResponse();
  }

  @Override
  public ArrayList<SurveyBuilderFormFieldValue> getFormFieldValues() {
    ArrayList<SurveyBuilderFormFieldValue> fields = new ArrayList<>();
    SurveyBuilderFormFieldValue lowerBoundld = factory.formFieldValue().as();
    lowerBoundld.setId(Integer.toString(getLowerBoundId()));
    fields.add(lowerBoundld);
    SurveyBuilderFormFieldValue upperBoundld = factory.formFieldValue().as();
    upperBoundld.setId(Integer.toString(getUpperBoundId()));
    fields.add(upperBoundld);
    return fields;
  }

  private int getLowerBoundId() {
    try {
      if (formResponse != null && formResponse.getValues() != null && formResponse.getValues().size() > 0
          && formResponse.getValues().get(0) != null && formResponse.getValues().get(0).getId() != null)
        return Integer.parseInt(formResponse.getValues().get(0).getId());
    } catch (Exception ignored) {}
    return 0;
  }

  private int getUpperBoundId() {
    try {
      if (formResponse != null && formResponse.getValues() != null && formResponse.getValues().size() > 0 &&
        formResponse.getValues().get(formResponse.getValues().size() - 1).getId() != null)
        return Integer.parseInt(formResponse.getValues().get(formResponse.getValues().size() - 1).getId());
    } catch (Exception ignored) {}
    return 10;
  }

  private void setUpperBoundId(int id) {
    List<SurveyBuilderFormFieldValue> fieldValues =
        new ArrayList<>();
    SurveyBuilderFormFieldValue lowerBoundId = factory.formFieldValue().as();
    lowerBoundId.setId(Integer.toString(getLowerBoundId()));
    SurveyBuilderFormFieldValue upperBoundId = factory.formFieldValue().as();
    upperBoundId.setId(Integer.toString(id));
    fieldValues.add(lowerBoundId);
    fieldValues.add(upperBoundId);
    formResponse.setValues(fieldValues);
  }

  private void setLowerBoundId(int id) {
    List<SurveyBuilderFormFieldValue> fieldValues =
        new ArrayList<>();
    SurveyBuilderFormFieldValue lowerBoundId = factory.formFieldValue().as();
    lowerBoundId.setId(Integer.toString(id));
    SurveyBuilderFormFieldValue upperBoundId = factory.formFieldValue().as();
    upperBoundId.setId(Integer.toString(getUpperBoundId()));
    fieldValues.add(lowerBoundId);
    fieldValues.add(upperBoundId);
    formResponse.setValues(fieldValues);
  }

  @Override
  public boolean hasLabel() {
    return false;
  }

  @Override
  public boolean supportsRequired() {
    return false;
  }
}