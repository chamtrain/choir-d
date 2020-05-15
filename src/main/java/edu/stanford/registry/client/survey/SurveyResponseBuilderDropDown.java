/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.client.Sanitizer;
import edu.stanford.registry.client.api.SurveyBuilderFormFieldValue;
import edu.stanford.registry.client.api.SurveyBuilderFormResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupAddon;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

public class SurveyResponseBuilderDropDown extends SurveyResponseBuilderRadio {

  public SurveyResponseBuilderDropDown(SurveyBuilderFormResponse surveyBuilderFormResponse, boolean forEdit) {
    super(surveyBuilderFormResponse, forEdit);

  }

  @Override
  public ArrayList<Row> showResponse() {
    ArrayList<Row> showResponses = new ArrayList<>();
    ButtonGroup buttonGroup = new ButtonGroup();
    Button optionButton = new Button("Select an option");
    optionButton.setDataToggle(Toggle.DROPDOWN);
    optionButton.setToggleCaret(true);
    optionButton.setWidth("90%");
    buttonGroup.add(optionButton);
    DropDownMenu dropDownMenu = new DropDownMenu();
    dropDownMenu.setPaddingLeft(5.0);
    dropDownMenu.setPaddingRight(5.0);
    if (formResponse.getValues() != null) {
      for (SurveyBuilderFormFieldValue field : formResponse.getValues()) {
        AnchorListItem listItem = new AnchorListItem();
        listItem.getElement().setInnerSafeHtml(Sanitizer.sanitizeHtml((" " + field.getLabel() + " ")));
        dropDownMenu.add(listItem);
      }
      buttonGroup.add(dropDownMenu);
    }
    Row listRow = new Row();
    Column col1 = new Column(ColumnSize.MD_1);
    listRow.add(col1);
    Column col2 = new Column(ColumnSize.MD_9);
    col2.getElement().setAttribute("style", "text-align: left;");
    buttonGroup.setWidth("95%");
    col2.add(buttonGroup);
    listRow.add(col2);
    showResponses.add(listRow);
    Row filterRow = new Row();
    Column spacer = new Column(ColumnSize.MD_1);
    filterRow.add(spacer);
    filterRow.add(getSearchFilterColumn(false));
    showResponses.add(filterRow);
    return showResponses;
  }

  public InputGroup getNewResponse() {
    InputGroup choiceGroup = super.getNewResponse();
    changeIcon(choiceGroup, inputGroups.size() == 0 ? IconType.CHEVRON_CIRCLE_DOWN : null);
    return choiceGroup;
  }

  @Override
  public Div editResponse(SurveyBuilderFormResponse response) {
    Div div = super.editResponse(response);
    IconType icon = IconType.CHEVRON_CIRCLE_DOWN;
    for (int i = 0; i < div.getWidgetCount(); i++) {
      changeIcon((InputGroup) div.getWidget(i), icon);
      icon = null;
    }
    div.add(getSearchFilterColumn(true));
    return div;
  }

  @Override
  public SurveyBuilderFormResponse getFormResponse() {
    formResponse.setAttributes(getFormAttributes());
    return formResponse;
  }

  @Override
  public InputGroup refreshResponse(int inx) {
    InputGroup group = super.refreshResponse(inx);
    changeIcon(group, (inx == 0) ? IconType.CHEVRON_CIRCLE_DOWN : null);
    return group;
  }

  private Map<String, String> getFormAttributes() {

    Map<String, String> attributes = formResponse.getAttributes();
    if (attributes == null) {
      attributes = new HashMap<>();
      formResponse.setAttributes(attributes);
    }
    return attributes;
  }

  private void changeIcon(InputGroup inputGroup, IconType icon) {
    InputGroupAddon addon = (InputGroupAddon) inputGroup.getWidget(0);
    addon.setIcon(icon);
    addon.setWidth("37px");
  }

  private Column getSearchFilterColumn(boolean isEnabled) {
    final InlineCheckBox searchFilter = new InlineCheckBox("Search filter");
    searchFilter.getElement().setAttribute("style", "font-size: large;");
    if (getFormAttributes().get("Filter") != null) {
      searchFilter.setValue("true".equals(getFormAttributes().get("Filter")));
    }
    searchFilter.setEnabled(isEnabled);
    searchFilter.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {

        getFormAttributes().put("Filter", searchFilter.getValue().toString());
      }
    });
    Column col = new Column(ColumnSize.MD_3);
    col.add(searchFilter);
    return col;
  }
}
