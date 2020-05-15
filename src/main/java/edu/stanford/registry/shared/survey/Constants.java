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

package edu.stanford.registry.shared.survey;

public class Constants {

  public static final String REGISTRY_SURVEY_SYSTEM_NAME = "Local";

  public static final String[] TYPE_ANSWER = { "radio", "input", "image", "map", "map_response", "heading", "select1",
      "select", "textboxset", "collapsible", "datepicker", "slider", "dropdown", "radiosetgrid" };
  public static final int TYPE_RADIO = 0;
  public static final int TYPE_INPUT = 1;
  public static final int TYPE_IMAGE = 2;
  public static final int TYPE_MAP = 3;
  public static final int TYPE_MAP_RESPONSE = 4;
  public static final int TYPE_LABEL_LIST = 5;
  public static final int TYPE_SELECT1 = 6;
  public static final int TYPE_SELECT = 7;
  public static final int TYPE_TEXTBOXSET = 8;
  public static final int TYPE_COLLAPSIBLE = 9;
  public static final int TYPE_DATEPICKER = 10;
  public static final int TYPE_SLIDER = 11;
  public static final int TYPE_DROPDOWN = 12;
  public static final int TYPE_RADIOSETGRID = 13;

  public static final int ALIGN_HORIZONTAL = 100;
  public static final int ALIGN_VERTICAL = 101;

  public static final int DISPLAY_ANSWERS_VERTICALLY = 1;

  public static final int SURVEY_STATUS_ASKING = 0;
  public static final int SURVEY_STATUS_ANSWERED = 1;
  public static final int SURVEY_STATUS_COMPLETED = 2;

  public static final String[] POSITION = { "left", "right", "above", "below" };
  public static final int POSITION_LEFT = 0;
  public static final int POSITION_RIGHT = 1;
  public static final int POSITION_ABOVE = 2;
  public static final int POSITION_BELOW = 3;

  public static final String[] FORMAT_DATATYPE = { "text", "integer" };
  public static final int DATATYPE_TEXT = 0;
  public static final int DATATYPE_INT = 1;

  public static final String ATTR_NAME = "data_name";
  public static final String ATTR_VALUE = "data_value";
  public static final String ATTR_CONDITION = "condition";

  public static final String ALERT = "Alert";
  public static final String ALIGN = "Align";
  public static final String APPEARANCE = "Appearance";
  public static final String CLASS = "Class";
  public static final String COLLAPSIBLE_CONTENT="CollapsibleContent";
  public static final String CONDITIONAL = "Conditional";
  public static final String DESCRIPTION = "Description";
  public static final String DESCRIPTION_POSITION = "DescriptionPosition";
  public static final String FOLLOW_UP_ORDER = "FollowUpOrder";
  public static final String FOLLOW_UP_ITEM = "FollowUpItem";
  public static final String FOOTING = "Footing";
  public static final String FORM = "Form";
  public static final String HEADING = "Heading";
  public static final String IMAGE = "Image";
  public static final String ITEMS = "Items";
  public static final String ITEM = "Item";
  public static final String ITEM_RESPONSE = "ItemResponse";
  public static final String ITEM_SCORE = "ItemScore";
  public static final String ITEM_CONDITIONS = "Conditions";
  public static final String NAME = "Name";
  public static final String ORDER = "Order";
  public static final String PATIENT_ATTRIBUTE = "PatientAttribute";
  public static final String REQUIRED_MIN = "RequiredMin";
  // used in conjunction with the Value attribute when the display string,
  // value and report string differ from each other
  // e.g. when showing a Likert scale question with annotations on each end
  // and nothing displayed in the response buttons making up the scale options
  public static final String REPORT_RESPONSE_TEXT = "ReportResponseText";
  public static final String RESPONSES = "Responses";
  public static final String RESPONSE = "Response";
  public static final String SET = "Set";
  public static final String SCORES = "Scores";
  public static final String SCORE = "Score";
  public static final String SCORE_NAME = "dataName";
  public static final String SCORE_ATTR_VALUE = "attribute-value";
  public static final String TIME_START = "TimeStarted";
  public static final String TIME_FINISH = "TimeFinished";
  public static final String TYPE = "Type";
  // used in conjunction with the ReportResponseText attribute when the display string,
  // value and report string differ from each other
  // e.g. when showing a Likert scale question with annotations on each end
  // and nothing displayed in the response buttons making up the scale options
  public static final String VALUE = "Value";
  public static final String VERTICAL = "Vertical";
  public static final String VISIBLE = "Visible";
  public static final String WHERE = "Where";

  public static final String XFORM_ALERT = "alert";
  public static final String XFORM_AREA = "area";
  public static final String XFORM_CHAR_WIDTH = "charwidth";
  public static final String XFORM_CHECKED = "checked";
  public static final String XFORM_FORMAT = "format";
  public static final String XFORM_HEIGHT = "height";
  public static final String XFORM_HINT = "hint";
  public static final String XFORM_IMG = "img";
  public static final String XFORM_ITEM = "item";
  public static final String XFORM_LABEL = "label";
  public static final String XFORM_LINES = "lines";
  public static final String XFORM_LOCATION = "location";
  public static final String XFORM_MAP = "map";
  public static final String XFORM_ORDER = "order";
  public static final String XFORM_REF = "ref";
  public static final String XFORM_VALUE = "value";
  public static final String XFORM_VERSION = "version";
  public static final String XFORM_WIDTH = "width";
  public static final String XFORM_XML = "xml";
  public static final String XFORM_SELECTED = "selected";
  public static final String XFORM_TRUE = "true";
  public static final String XFORM_FALSE = "false";

  public static final String ACTION_ONSELECT = "onselect";
  public static final String ACTION_ONDESELECT = "ondeselect";

  public static final String ERROR_STYLE = "dataListTextError";
  public static final String PROMIS_QUESTION_REPORT_FONTSIZE="PromisQuestionReportFontSize";
  public static final String REPORT_SMALL_FONTSIZE="7";
}
