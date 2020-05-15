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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

/**
 * Stubbed implementation for a survey that exercises all question types. This is in the api
 * package so we can use it in "NoServer" mode and also stub from the server-side.
 */
public class SurveyServiceStub implements SurveyService {
  private static final String CHOICE_RADIOSET_PAGE = "List of radio buttons (RadiosetPage) use for PROMIS";
  private static final String CHOICE_SLIDER_PAGE = "Slider numeric scale (SliderPage)";
  private static final String CHOICE_NUMERIC_SCALE_PAGE = "Radio numeric scale with labels (NumericScalePage)";
  private static final String CHOICE_NUMERIC_SCALE_PAGE2 = "Radio numeric scale no labels (NumericScalePage)";
  private static final String CHOICE_NUMERIC_SCALE_PAGE3 = "Radio numeric scale with and without labels (FormPage)";
  private static final String CHOICE_BODY_MAP_MALE = "Male body image map (BodyMapPage)";
  private static final String CHOICE_BODY_MAP_FEMALE = "Female body image map (BodyMapPage)";
  private static final String CHOICE_LOGIN = "Login page (TextInputPage)";
  private static final String CHOICE_THANKS = "Thank you page (FormPage)";
  private static final String CHOICE_NUMBER = "Number";
  private static final String CHOICE_NUMBERS = "Multiple numbers";
  private static final String CHOICE_TEXT = "Text input";
  private static final String CHOICE_TEXT_AREA = "Text area";
  private static final String CHOICE_CHECKBOXES = "Checkboxes Optional";
  private static final String CHOICE_CHECKBOXES_REQUIRED = "Checkboxes Required";
  private static final String CHOICE_CHECKBOXES_AND_OR = "Checkboxes with and/or text area";
  private static final String CHOICE_CONDITIONAL = "Radios that show/hide another question";
  private static final String CHOICE_CHECKBOX_CONDITIONAL_3_LEVEL = "Checkboxes that show/hide 3 levels";
  private static final String CHOICE_CONDITIONAL_3_LEVEL = "Radios that show/hide 3 levels";
  private static final String CHOICE_COLLAPSIBLE_CONSENT = "Research Consent";
  private static final String CHOICE_DATE_PICKER = "Date Picker";
  int times = 1;
  private SurveyFactory factory;

  public SurveyServiceStub(SurveyFactory factory) {
    this.factory = factory;
  }

  @Override
  public String[] startSurvey(String systemId, String surveyToken) {
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setStyleSheetName(getStyleSheetName());
    displayStatus.setPageTitle("Test Stubbed Questions (Client Only blah blah blah blah blah blah blah blah blah)");
    return log("Starting: (" + systemId + "/" + surveyToken + ") ", displayStatus, questionList(displayStatus));
  }

  @Override
  public String[] resumeSurvey(String resumeToken) {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String[] continueSurvey(String statusJson, String answerJson) {
    GWT.log("Submitted: " + statusJson + " " + answerJson);
    String prefix = "Continuing: ";

    SubmitStatus submitStatus = AutoBeanCodex.decode(factory, SubmitStatus.class, statusJson).as();
    DisplayStatus displayStatus = factory.displayStatus().as();
    displayStatus.setStyleSheetName(getStyleSheetName());
    displayStatus.setPageTitle("Test Stubbed Questions (Client Only)");
    if (submitStatus.getQuestionType() == QuestionType.buttonList) {
      RadiosetAnswer answer = AutoBeanCodex.decode(factory, RadiosetAnswer.class, answerJson).as();

      if (CHOICE_RADIOSET_PAGE.equals(answer.getChoice())) {
        return log(prefix, displayStatus, applesQuestion(displayStatus));
      } else if (CHOICE_SLIDER_PAGE.equals(answer.getChoice())) {
        return log(prefix, displayStatus, sliderQuestion(displayStatus));
      } else if (CHOICE_NUMERIC_SCALE_PAGE.equals(answer.getChoice())) {
        return log(prefix, displayStatus, numericScaleQuestion(displayStatus, true));
      } else if (CHOICE_NUMERIC_SCALE_PAGE2.equals(answer.getChoice())) {
        return log(prefix, displayStatus, numericScaleQuestion(displayStatus, false));
      } else if (CHOICE_NUMERIC_SCALE_PAGE3.equals(answer.getChoice())) {
        return log(prefix, displayStatus, numericScaleFormQuestion(displayStatus));
      } else if (CHOICE_BODY_MAP_MALE.equals(answer.getChoice())) {
        return log(prefix, displayStatus, bodyMapQuestionMale(displayStatus));
      } else if (CHOICE_BODY_MAP_FEMALE.equals(answer.getChoice())) {
        return log(prefix, displayStatus, bodyMapQuestionFemale(displayStatus));
      } else if (CHOICE_LOGIN.equals(answer.getChoice())) {
        return log(prefix, displayStatus, textInputQuestion(displayStatus));
      } else if (CHOICE_THANKS.equals(answer.getChoice())) {
        return log(prefix, displayStatus, thankYou(displayStatus));
      } else if (CHOICE_NUMBER.equals(answer.getChoice())) {
        return log(prefix, displayStatus, number(displayStatus));
      } else if (CHOICE_NUMBERS.equals(answer.getChoice())) {
        return log(prefix, displayStatus, numbers(displayStatus));
      } else if (CHOICE_TEXT.equals(answer.getChoice())) {
        return log(prefix, displayStatus, text(displayStatus));
      } else if (CHOICE_TEXT_AREA.equals(answer.getChoice())) {
        return log(prefix, displayStatus, textArea(displayStatus));
      } else if (CHOICE_CHECKBOXES.equals(answer.getChoice())) {
        return log(prefix, displayStatus, checkboxes(displayStatus));
      } else if (CHOICE_CHECKBOXES_REQUIRED.equals(answer.getChoice())) {
        return log(prefix, displayStatus, checkboxesRequired(displayStatus));
      } else if (CHOICE_CHECKBOXES_AND_OR.equals(answer.getChoice())) {
        return log(prefix, displayStatus, checkboxesAndOr(displayStatus));
      } else if (CHOICE_CONDITIONAL.equals(answer.getChoice())) {
        return log(prefix, displayStatus, conditional(displayStatus));
      } else if (CHOICE_CONDITIONAL_3_LEVEL.equals(answer.getChoice())) {
        return log(prefix, displayStatus, conditional3Level(displayStatus));
      } else if (CHOICE_CHECKBOX_CONDITIONAL_3_LEVEL.equals(answer.getChoice())) {
        return log(prefix, displayStatus, checkboxConditional3Level(displayStatus));
      } else if (CHOICE_COLLAPSIBLE_CONSENT.equals(answer.getChoice())) {
        return log(prefix, displayStatus, consentQuestion(displayStatus));
      } else if (CHOICE_DATE_PICKER.equals(answer.getChoice())) {
        return log(prefix, displayStatus, datePicker(displayStatus));
      }
      throw new RuntimeException("Unknown selection on buttonList: " + answer.getChoice());
    } else if (submitStatus.getQuestionType() == QuestionType.textList) {
      TextInputAnswer answer = AutoBeanCodex.decode(factory, TextInputAnswer.class, answerJson).as();

      String pin = answer.getChoice().get("PIN code");
      if (pin == null || !pin.equals("123")) {
        displayStatus.setSessionStatus(SessionStatus.questionInvalid);
        AutoBean<TextInputQuestion> questionBean = textInputQuestion(displayStatus);
        questionBean.as().setServerValidationMessage("You should have typed \"123\" for the PIN code");
        return log(prefix, displayStatus, questionBean);
      }

      return log(prefix, displayStatus, questionList(displayStatus));
    } else {
      return log(prefix, displayStatus, questionList(displayStatus));
    }
  }

  private AutoBean<RadiosetQuestion> questionList(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.buttonList);

    AutoBean<RadiosetQuestion> bean = factory.radiosetQuestion();
    RadiosetQuestion question = bean.as();

    question.setTitle1("Choose a question type to test");
    ArrayList<String> choices = new ArrayList<>();
    choices.add(CHOICE_LOGIN);
    choices.add(CHOICE_RADIOSET_PAGE);
//        choices.add(CHOICE_SLIDER_PAGE);
    choices.add(CHOICE_NUMERIC_SCALE_PAGE);
    choices.add(CHOICE_NUMERIC_SCALE_PAGE2);
    choices.add(CHOICE_NUMERIC_SCALE_PAGE3);
    choices.add(CHOICE_BODY_MAP_MALE);
    choices.add(CHOICE_BODY_MAP_FEMALE);
    choices.add(CHOICE_THANKS);
    choices.add(CHOICE_NUMBER);
    choices.add(CHOICE_NUMBERS);
    choices.add(CHOICE_TEXT);
    choices.add(CHOICE_TEXT_AREA);
    choices.add(CHOICE_CHECKBOXES);
    choices.add(CHOICE_CHECKBOXES_REQUIRED);
    choices.add(CHOICE_CHECKBOXES_AND_OR);
    choices.add(CHOICE_CONDITIONAL);
    choices.add(CHOICE_CONDITIONAL_3_LEVEL);
    choices.add(CHOICE_CHECKBOX_CONDITIONAL_3_LEVEL);
    choices.add(CHOICE_COLLAPSIBLE_CONSENT);
    choices.add(CHOICE_DATE_PICKER);
    question.setChoices(choices);

    return bean;
  }

  private String[] log(String prefix, DisplayStatus status, AutoBean<?> question) {
    AutoBean<DisplayStatus> statusBean = AutoBeanUtils.getAutoBean(status);
    String statusJson = AutoBeanCodex.encode(statusBean).getPayload();

    String questionJson = AutoBeanCodex.encode(question).getPayload();

    GWT.log(prefix + statusJson + " " + questionJson);
    return new String[] { statusJson, questionJson };
  }

  private AutoBean<RadiosetQuestion> applesQuestion(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.radioset);

    AutoBean<RadiosetQuestion> bean = factory.radiosetQuestion();
    RadiosetQuestion question = bean.as();

    question.setTitle1("In the last 7 days");
    question.setTitle2("How many times have you eaten " + times++ + " apples?");
    ArrayList<String> choices = new ArrayList<>();
    choices.add("None at all");
    choices.add("A few");
    choices.add("Some bit");
    choices.add("Bunches");
    choices.add("A whole awful lot");
    question.setChoices(choices);

    return bean;
  }

  private AutoBean<SliderQuestion> sliderQuestion(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.slider);

    AutoBean<SliderQuestion> bean = factory.sliderQuestion();
    SliderQuestion question = bean.as();

    question.setTitle1("In the past 7 days");
    question.setTitle2("How intense was your pain at its WORST?");

    question.setLowerBound(0);
    question.setLowerBoundLabel("No pain");
    question.setUpperBound(10);
    question.setUpperBoundLabel("Pain as bad as you can imagine");

    return bean;
  }

  private AutoBean<SliderQuestion> numericScaleQuestion(DisplayStatus displayStatus, boolean addLabels) {
    displayStatus.setQuestionType(QuestionType.numericScale);

    AutoBean<SliderQuestion> bean = factory.sliderQuestion();
    SliderQuestion question = bean.as();

    question.setTitle1("In the past 7 days");
    if (addLabels) {
      question.setTitle2("How intense was your pain on AVERAGE?");
      question.setLowerBoundLabel("No pain");
      question.setUpperBoundLabel("Pain as bad as you can imagine");
    } else {
      question.setTitle2("How intense was your pain at its WORST?");
    }

    question.setLowerBound(0);
    question.setUpperBound(10);

    return bean;
  }

  private AutoBean<FormQuestion> numericScaleFormQuestion(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    FormField withLabels = field("with", FieldType.numericScale, "How intense was your pain WITH labels?");
    withLabels.setMin("0");
    withLabels.setMax("10");
    Map<String, String> attrs = new HashMap<>();
    attrs.put("leftLabel", "No pain");
    attrs.put("rightLabel", "Pain as bad as you can imagine");
    withLabels.setAttributes(attrs);

    FormField withoutLabels = field("without", FieldType.numericScale, "How intense was your pain WITHOUT labels?");
    withoutLabels.setMin("0");
    withoutLabels.setMax("10");

    return form("How do <em>you</em> like <b>bold</b>, <i>italic</i>,"
        + " and <u>underlined</u> text?<p>another paragraph</p><h1>heading 1</h1><h2>heading 2</h2>"
        + "<h3>heading 3</h3><h4>heading 4</h4><h5>heading 5 unordered list</h5><ul><li>apples</li><li>bananas</li>"
        + "</ul><h6>heading 6 ordered list</h6><ol><li>cars</li><li>trucks</li></ol>", withLabels, withoutLabels);
  }

  private AutoBean<BodyMapQuestion> bodyMapQuestionMale(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.bodyMap);

    AutoBean<BodyMapQuestion> bean = factory.bodyMapQuestion();
    BodyMapQuestion question = bean.as();

    question.setTitle1("Select the areas where you are experiencing pain");
    question.setImgTag1("<img name=\"male_front\" src=\"images/maps/male_front.071813.cache.png\" width=\"280\" height=\"580\" id=\"male_front\" usemap=\"#m_male_front\" alt=\"Male front image\" border=\"0\"/>\n");
    question.setMapTag1("<map Order=\"1\" name=\"m_male_front\" id=\"m_male_front\" >\n"
        + "<area shape=\"poly\" coords=\"149,530,149,539,151,544,151,551,148,558,150,562,156,563,159,561,172,562,174,559,178,559,178,556,172,549,169,543,169,540,170,537,168,532,153,531,149,530\" href=\"#mf36\" id=\"136\" alt=\"136\" />\n"
        + "<area shape=\"poly\" coords=\"133,531,131,539,131,546,132,554,133,560,130,562,124,562,123,561,110,561,107,559,103,559,103,557,107,553,112,542,112,540,111,538,113,531,113,531,133,531\" href=\"#mf35\" id=\"135\" alt=\"135\" />\n"
        + "<area shape=\"poly\" coords=\"107,504,113,526,113,531,122,531,129,531,131,530,131,526,130,515,129,502,125,500,118,500,114,501,107,504\" href=\"#mf33\" id=\"133\" alt=\"133\" />\n"
        + "<area shape=\"poly\" coords=\"149,530,150,525,152,516,151,501,156,501,157,499,165,499,167,501,170,502,174,504,168,527,168,532,162,532,155,532,149,530\" href=\"#mf34\" id=\"134\" alt=\"134\" />\n"
        + "<area shape=\"poly\" coords=\"151,435,151,453,150,465,150,481,151,491,151,501,156,500,164,499,170,502,174,504,178,491,183,474,185,463,185,449,184,436,183,431,179,435,171,438,159,437,151,435\" href=\"#mf32\" id=\"132\" alt=\"132\" />\n"
        + "<area shape=\"poly\" coords=\"98,432,96,444,95,454,96,463,99,475,107,503,115,501,120,499,125,500,129,501,130,495,131,476,131,450,130,441,129,436,121,437,113,437,106,436,98,432\" href=\"#mf31\" id=\"131\" alt=\"131\" />\n"
        + "<area shape=\"poly\" coords=\"146,403,147,412,149,422,152,429,152,434,162,437,172,436,178,434,183,431,181,417,179,407,179,400,171,397,165,395,160,396,154,399,150,400,146,403\" href=\"#mf30\" id=\"130\" alt=\"130\" />\n"
        + "<area shape=\"poly\" coords=\"99,431,100,418,102,409,102,401,107,397,122,397,129,400,134,403,134,411,133,419,132,425,130,428,130,434,123,437,111,437,105,435,105,435,99,431\" href=\"#mf29\" id=\"129\" alt=\"129\" />\n"
        + "<area shape=\"poly\" coords=\"142,311,143,322,144,334,144,344,144,358,145,371,146,382,145,394,147,402,152,399,159,397,163,396,173,396,176,398,179,399,180,392,182,380,187,361,190,349,194,333,194,327,195,313,188,310,181,306,175,300,173,296,172,293,168,295,164,297,161,300,157,304,154,308,148,310,142,311\" href=\"#mf27\" id=\"127\" alt=\"127\" />\n"
        + "<area shape=\"poly\" coords=\"86,313,87,329,89,344,94,361,98,382,102,393,102,400,108,397,122,396,129,400,135,402,136,374,136,337,139,320,139,311,132,309,127,306,119,299,114,294,111,293,108,297,105,301,103,303,100,306,96,308,94,309,90,311,86,313\" href=\"#mf26\" id=\"126\" alt=\"126\" />\n"
        + "<area shape=\"poly\" coords=\"243,282,249,284,255,289,260,294,268,301,267,304,262,304,255,298,253,296,256,328,257,336,255,336,251,323,249,315,248,316,250,327,252,335,252,339,250,338,249,337,248,337,248,340,246,340,239,323,236,318,238,325,239,333,240,337,235,331,233,323,232,317,230,314,227,310,226,300,225,290,228,292,237,292,240,290,243,287,243,282\" href=\"#mf28\" id=\"128\" alt=\"128\" />\n"
        + "<area shape=\"poly\" coords=\"38,282,38,288,44,292,52,293,56,290,56,301,54,309,52,314,50,314,47,327,44,335,43,337,41,336,43,328,45,319,43,320,41,325,40,330,36,338,34,339,32,337,29,338,29,333,33,318,32,315,31,315,30,321,29,328,27,333,25,335,26,324,28,310,28,297,27,297,20,302,13,303,13,301,20,295,26,288,31,285,36,282,38,282\" href=\"#mf25\" id=\"125\" alt=\"125\" />\n"
        + "<area shape=\"poly\" coords=\"86,312,87,291,90,272,91,257,96,258,102,261,106,265,110,271,112,277,112,289,109,295,106,300,103,304,99,307,93,310,86,312\" href=\"#mf20\" id=\"120\" alt=\"120\" />\n"
        + "<area shape=\"poly\" coords=\"235,268,238,276,242,282,243,287,237,292,229,292,226,291,217,275,220,271,226,268,235,268\" href=\"#mf24\" id=\"124\" alt=\"124\" />\n"
        + "<area shape=\"poly\" coords=\"200,223,201,239,203,246,209,261,217,274,221,270,227,267,233,268,235,267,233,254,232,243,230,231,228,226,226,221,222,223,218,224,207,225,203,224,200,223\" href=\"#mf18\" id=\"118\" alt=\"118\" />\n"
        + "<area shape=\"poly\" coords=\"140,274,136,274,131,278,126,283,122,288,117,290,112,291,110,293,115,295,117,297,126,305,131,309,138,310,141,310,140,274\" href=\"#mf21\" id=\"121\" alt=\"121\" />\n"
        + "<area shape=\"poly\" coords=\"170,291,163,290,156,283,150,278,145,274,141,273,141,310,148,310,152,308,160,301,165,296,169,294,172,294,170,291\" href=\"#mf22\" id=\"122\" alt=\"122\" />\n"
        + "<area shape=\"poly\" coords=\"191,269,189,257,184,258,177,262,174,267,171,273,170,279,171,291,174,295,177,301,182,305,187,310,195,313,195,293,193,275,191,269\" href=\"#mf23\" id=\"123\" alt=\"123\" />\n"
        + "<area shape=\"poly\" coords=\"171,290,165,290,158,286,149,277,145,274,141,273,141,196,146,196,151,200,155,205,159,212,164,215,169,216,187,217,187,231,189,238,189,251,189,256,184,258,179,260,176,263,173,266,171,271,170,276,171,290\" href=\"#mf17\" id=\"117\" alt=\"17\" />\n"
        + "<area shape=\"poly\" coords=\"95,217,94,232,92,240,92,245,92,256,97,258,103,261,107,264,111,271,111,276,112,287,110,292,116,291,119,288,123,285,129,279,129,279,137,273,140,273,140,247,140,197,137,196,130,200,125,207,120,214,112,216,109,216,95,217\" href=\"#mf16\" id=\"116\" alt=\"116\" />\n"
        + "<area shape=\"poly\" coords=\"200,222,200,215,197,208,203,203,212,203,215,204,220,205,220,212,225,217,226,221,220,224,206,224,200,222\" href=\"#mf14\" id=\"114\" alt=\"114\" />\n"
        + "<area shape=\"poly\" coords=\"56,289,64,274,60,270,54,268,46,268,41,280,38,282,38,286,39,288,42,291,45,292,52,293,56,289\" href=\"#mf19\" id=\"119\" alt=\"119\" />\n"
        + "<area shape=\"poly\" coords=\"46,267,49,251,49,243,51,230,55,220,61,224,70,225,78,225,81,223,81,235,79,244,76,252,74,257,69,267,64,274,61,270,56,267,52,267,46,267\" href=\"#mf15\" id=\"115\" alt=\"115\" />\n"
        + "<area shape=\"poly\" coords=\"81,222,81,216,84,208,77,203,69,203,63,204,61,205,60,212,55,219,62,224,77,224,81,222\" href=\"#mf13\" id=\"113\" alt=\"113\" />\n"
        + "<area shape=\"poly\" coords=\"85,207,89,187,91,180,91,170,89,162,85,153,77,158,67,162,62,163,62,169,63,178,62,185,62,204,69,203,69,202,77,203,80,204,85,207\" href=\"#mf11\" id=\"111\" alt=\"111\" />\n"
        + "<area shape=\"poly\" coords=\"220,205,211,203,205,203,199,206,197,208,193,195,191,183,189,172,192,161,197,153,204,156,212,160,219,163,218,173,219,180,220,194,220,205\" href=\"#mf12\" id=\"112\" alt=\"112\" />\n"
        + "<area shape=\"poly\" coords=\"219,162,205,157,199,153,190,147,181,138,173,130,167,121,162,113,156,103,160,99,169,102,180,107,187,111,195,113,203,115,210,121,214,127,217,134,220,145,219,162\" href=\"#mf10\" id=\"110\" alt=\"110\" />\n"
        + "<area shape=\"poly\" coords=\"155,103,162,115,171,128,177,135,188,146,197,153,193,160,190,171,190,182,191,190,188,208,187,216,171,216,164,214,160,212,157,206,152,200,147,197,145,195,141,195,141,109,146,107,151,105,155,103\" href=\"#mf9\" id=\"109\" alt=\"109\" />\n"
        + "<area shape=\"poly\" coords=\"140,195,140,109,131,106,125,103,116,121,109,130,101,139,93,147,85,153,90,162,91,170,91,180,90,187,92,197,93,210,94,216,111,216,117,215,121,213,125,206,130,200,134,196,140,195\" href=\"#mf8\" id=\"108\" alt=\"108\" />\n"
        + "<area shape=\"poly\" coords=\"119,99,94,111,81,114,73,118,67,125,65,133,61,148,62,163,78,157,93,146,101,138,113,125,122,109,125,103,119,99\" href=\"#mf7\" id=\"107\" alt=\"107\" />\n"
        + "<area shape=\"poly\" coords=\"158,81,158,98,160,99,150,106,144,108,141,108,141,85,148,84,154,82,158,81\" href=\"#mf6\" id=\"106\" alt=\"106\" />\n"
        + "<area shape=\"poly\" coords=\"140,108,140,85,125,81,122,81,122,96,119,99,129,105,140,108\" href=\"#mf5\" id=\"105\" alt=\"105\" />\n"
        + "<area shape=\"poly\" coords=\"114,40,130,43,140,43,140,84,123,80,118,75,117,66,109,55,109,49,110,47,113,47,116,50,115,46,114,40\" href=\"#mf3\" id=\"103\" alt=\"103\" />\n"
        + "<area shape=\"poly\" coords=\"141,84,150,83,156,81,160,78,163,72,163,66,170,56,172,49,170,47,167,47,164,49,165,46,165,41,149,42,141,43,141,84\" href=\"#mf4\" id=\"104\" alt=\"104\" />\n"
        + "<area shape=\"poly\" coords=\"165,40,165,31,162,25,158,21,153,19,147,18,141,18,141,42,152,42,159,41,165,40\" href=\"#mf2\" id=\"102\" alt=\"102\" />\n"
        + "<area shape=\"poly\" coords=\"140,43,140,19,140,18,132,18,126,20,121,22,118,26,115,30,114,34,115,39,126,41,130,42,140,43\" href=\"#mf1\" id=\"101\" alt=\"101\" />\n"
        + "</map>");
    question.setImgTag2("<img name=\"male_back\" src=\"images/maps/male_back.071813.cache.png\" width=\"291\" height=\"579\" id=\"male_back\" usemap=\"#m_male_back\" alt=\"Male back image\" border=\"0\" />");
    question.setMapTag2("<map Order=\"2\" name=\"m_male_back\" id=\"m_male_back\">\n"
        + "<area shape=\"poly\" coords=\"157,525,155,532,155,537,157,543,157,554,155,558,155,563,161,564,165,562,177,563,178,562,180,561,184,561,184,558,178,551,175,544,176,541,176,538,174,533,175,526,162,526,157,525\" href=\"#mb38\" id=\"238\" alt=\"238\" />\n"
        + "<area shape=\"poly\" coords=\"109,559,110,556,115,551,119,543,117,539,119,533,118,527,127,527,132,527,135,526,138,532,138,538,137,543,136,553,137,557,139,563,137,564,130,564,126,562,116,563,113,561,109,559\" href=\"#mb37\" id=\"237\" alt=\"237\" />\n"
        + "<area shape=\"poly\" coords=\"158,503,158,520,157,524,162,526,174,526,180,506,174,502,169,501,164,500,158,503\" href=\"#mb36\" id=\"236\" alt=\"236\" />\n"
        + "<area shape=\"poly\" coords=\"113,506,118,526,132,526,136,524,135,504,131,502,121,502,113,506\" href=\"#mb35\" id=\"235\" alt=\"235\" />\n"
        + "<area shape=\"poly\" coords=\"158,435,156,456,155,476,157,493,158,502,164,501,169,501,174,502,181,505,191,464,192,443,191,436,189,432,184,435,180,438,171,437,163,436,158,435\" href=\"#mb34\" id=\"234\" alt=\"234\" />\n"
        + "<area shape=\"poly\" coords=\"104,433,102,447,102,461,104,470,113,505,119,502,121,501,132,501,135,502,137,476,137,456,135,436,130,437,116,437,109,435,104,433\" href=\"#mb33\" id=\"233\" alt=\"233\" />\n"
        + "<area shape=\"poly\" coords=\"153,403,153,412,155,422,158,429,158,434,165,437,178,437,184,435,189,431,188,423,186,413,185,400,180,397,167,397,161,398,156,401,153,403\" href=\"#mb32\" id=\"232\" alt=\"232\" />\n"
        + "<area shape=\"poly\" coords=\"108,400,108,412,106,421,104,431,110,436,114,437,128,437,135,435,136,428,138,419,140,411,140,404,134,399,126,397,114,396,108,400\" href=\"#mb31\" id=\"231\" alt=\"231\" />\n"
        + "<area shape=\"poly\" coords=\"232,289,238,291,242,288,249,280,254,281,261,286,268,294,271,297,276,301,273,303,269,301,261,296,260,300,261,313,264,335,262,335,259,325,256,314,256,322,257,326,259,333,260,338,258,338,257,336,254,338,247,327,244,318,245,326,247,332,247,335,243,330,240,320,238,312,235,311,232,300,232,289\" href=\"#mb30\" id=\"230\" alt=\"230\" />\n"
        + "<area shape=\"poly\" coords=\"150,321,150,339,150,357,151,378,152,403,159,398,168,396,178,396,184,398,186,399,186,393,195,358,200,339,201,329,201,318,194,321,182,324,175,326,161,327,153,323,150,321\" href=\"#mb29\" id=\"229\" alt=\"229\" />\n"
        + "<area shape=\"poly\" coords=\"91,317,93,335,98,357,104,384,107,394,107,400,114,396,125,397,134,398,138,401,141,403,142,386,142,355,143,336,144,321,140,324,133,327,118,327,113,324,101,321,91,317\" href=\"#mb28\" id=\"228\" alt=\"228\" />\n"
        + "<area shape=\"poly\" coords=\"44,280,37,282,30,288,26,293,21,297,17,299,17,302,23,302,32,295,32,309,30,322,28,335,31,335,37,314,35,328,33,337,36,336,39,339,49,318,49,325,45,335,47,337,53,325,55,313,57,312,60,302,60,290,57,291,52,291,47,286,44,280\" href=\"#mb27\" id=\"227\" alt=\"227\" />\n"
        + "<area shape=\"poly\" coords=\"50,266,47,275,44,280,48,287,53,291,58,292,60,289,69,273,64,268,60,266,50,266\" href=\"#mb221\" id=\"221\" alt=\"221\" />\n"
        + "<area shape=\"poly\" coords=\"121,46,120,39,120,35,128,38,136,39,146,38,146,82,132,82,129,81,128,76,125,73,123,69,122,61,119,58,116,53,115,49,115,45,117,42,120,43,121,46\" href=\"#mb3\" id=\"203\" alt=\"203\" />\n"
        + "<area shape=\"poly\" coords=\"164,81,165,75,169,70,170,62,176,53,178,45,175,42,172,44,170,46,172,40,172,36,159,37,147,38,147,82,164,81\" href=\"#mb4\" id=\"204\" alt=\"204\" />\n"
        + "<area shape=\"poly\" coords=\"120,34,120,27,123,21,131,15,140,13,146,13,146,38,138,38,130,37,124,36,121,36,120,34\" href=\"#mb1\" id=\"201\" alt=\"201\" />\n"
        + "<area shape=\"poly\" coords=\"171,24,172,36,159,37,146,37,147,14,155,14,164,17,171,24\" href=\"#mb2\" id=\"202\" alt=\"202\" />\n"
        + "<area shape=\"poly\" coords=\"164,82,164,92,167,95,149,105,148,105,147,84,146,83,161,83,164,82\" href=\"#mb6\" id=\"206\" alt=\"206\" />\n"
        + "<area shape=\"poly\" coords=\"146,82,147,105,141,104,131,100,125,95,128,92,128,81,133,83,146,82\" href=\"#mb5\" id=\"205\" alt=\"205\" />\n"
        + "<area shape=\"poly\" coords=\"69,271,80,250,84,242,86,233,87,221,81,223,70,222,63,221,61,218,58,224,57,233,56,241,54,252,52,260,51,265,55,265,61,266,65,268,69,271\" href=\"#mb17\" id=\"217\" alt=\"217\" />\n"
        + "<area shape=\"poly\" coords=\"61,218,66,222,80,222,86,220,87,214,90,204,83,201,72,201,66,204,66,209,61,218\" href=\"#mb15\" id=\"215\" alt=\"215\" />\n"
        + "<area shape=\"poly\" coords=\"90,204,94,191,95,184,90,162,85,160,77,161,71,160,67,159,68,169,67,176,66,187,66,204,73,200,83,200,90,204\" href=\"#mb11\" id=\"211\" alt=\"211\" />\n"
        + "<area shape=\"poly\" coords=\"233,289,238,290,243,287,248,281,249,279,248,278,245,272,243,267,240,266,232,267,227,269,224,273,233,289\" href=\"#mb26\" id=\"226\" alt=\"226\" />\n"
        + "<area shape=\"poly\" coords=\"243,265,239,249,238,238,237,229,233,218,227,223,214,222,209,221,206,220,207,233,207,239,209,244,223,272,229,268,237,266,243,265\" href=\"#mb20\" id=\"220\" alt=\"220\" />\n"
        + "<area shape=\"poly\" coords=\"233,218,227,222,213,222,207,219,206,213,204,206,210,201,220,200,225,201,227,203,228,209,233,218\" href=\"#mb16\" id=\"216\" alt=\"216\" />\n"
        + "<area shape=\"poly\" coords=\"227,202,227,184,226,175,225,168,226,163,225,159,212,161,201,159,198,161,197,169,197,175,198,182,201,193,203,206,208,202,213,200,217,200,221,200,227,202\" href=\"#mb14\" id=\"214\" alt=\"214\" />\n"
        + "<area shape=\"poly\" coords=\"124,96,131,99,129,107,126,116,122,125,115,136,105,148,99,154,91,159,87,161,78,161,70,159,67,158,66,147,67,138,70,128,74,120,78,115,89,109,98,108,124,96\" href=\"#mb7\" id=\"207\" alt=\"207\" />\n"
        + "<area shape=\"poly\" coords=\"216,116,208,111,202,109,194,108,167,96,162,99,165,112,170,123,177,134,184,144,192,152,199,159,203,160,211,161,219,160,225,159,227,142,224,130,220,122,216,116\" href=\"#mb10\" id=\"210\" alt=\"210\" />\n"
        + "<area shape=\"poly\" coords=\"161,99,147,106,146,165,175,165,201,160,198,158,187,149,179,139,172,127,167,116,163,106,161,99\" href=\"#mb\" id=\"209\" alt=\"209\" />\n"
        + "<area shape=\"poly\" coords=\"131,100,127,115,122,127,115,138,108,146,101,153,91,160,121,165,146,165,146,106,131,100\" href=\"#mb8\" id=\"208\" alt=\"208\" />\n"
        + "<area shape=\"poly\" coords=\"201,161,197,184,195,196,194,210,194,226,172,230,148,231,147,166,160,165,173,165,190,162,201,161\" href=\"#mb13\" id=\"213\" alt=\"213\" />\n"
        + "<area shape=\"poly\" coords=\"91,161,96,188,98,201,99,215,100,226,114,228,128,232,146,231,146,166,133,166,124,166,112,164,102,162,91,161\" href=\"#mb12\" id=\"212\" alt=\"212\" />\n"
        + "<area shape=\"poly\" coords=\"150,320,160,326,174,327,186,323,198,319,201,317,189,310,182,301,178,292,177,287,176,272,178,265,181,258,170,263,160,265,146,266,147,291,147,309,150,320\" href=\"#mb24\" id=\"224\" alt=\"224\" />\n"
        + "<area shape=\"poly\" coords=\"202,316,202,295,199,272,197,260,195,249,191,250,186,253,181,259,178,267,176,274,177,286,180,296,184,303,190,310,202,316\" href=\"#mb25\" id=\"225\" alt=\"225\" />\n"
        + "<area shape=\"poly\" coords=\"193,226,195,234,195,248,188,250,184,254,181,258,171,262,158,265,147,265,147,232,158,232,173,230,183,228,189,227,193,226\" href=\"#mb19\" id=\"219\" alt=\"219\" />\n"
        + "<area shape=\"poly\" coords=\"101,226,98,233,98,241,98,248,104,249,110,255,113,258,127,263,141,264,146,265,146,232,129,232,118,230,110,228,101,226\" href=\"#mb18\" id=\"218\" alt=\"218\" />\n"
        + "<area shape=\"poly\" coords=\"146,266,146,305,144,320,136,325,130,326,121,326,113,325,104,322,93,317,101,312,108,306,114,297,116,288,117,270,114,262,113,258,121,262,129,264,139,265,146,266\" href=\"#mb23\" id=\"223\" alt=\"223\" />\n"
        + "<area shape=\"poly\" coords=\"98,249,96,260,96,260,94,279,92,287,91,317,103,310,110,303,115,293,117,282,117,269,113,260,107,252,102,249,98,249\" href=\"#mb22\" id=\"222\" alt=\"222\" />\n"
        + "</map>");
    question.setNoPainCheckboxLabel("I have no pain");

    return bean;
  }

  private AutoBean<BodyMapQuestion> bodyMapQuestionFemale(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.bodyMap);

    AutoBean<BodyMapQuestion> bean = factory.bodyMapQuestion();
    BodyMapQuestion question = bean.as();

    question.setTitle1("Select the areas where you are experiencing pain");
    question.setImgTag1("<img name=\"female_front\" src=\"images/maps/female_front.071813.cache.png\" width=\"280\" height=\"580\" id=\"female_front\" usemap=\"#m_female_front\" alt=\"Female front image\" border=\"0\" />");
    question.setMapTag1("<map Order=\"1\" name=\"m_female_front\" id=\"m_female_front\"  class=\"imgmap\">\n"
        + "<area shape=\"poly\" coords=\"149,536,148,542,150,557,147,564,150,568,155,568,158,566,171,566,174,564,177,564,178,562,172,556,168,548,168,541,167,536,163,538,160,538,149,536\" href=\"#ff36\" id=\"136\" alt=\"136\" />\n"
        + "<area shape=\"poly\" coords=\"113,537,111,541,110,549,106,557,101,562,101,563,106,565,109,567,122,566,129,568,132,563,129,555,129,544,131,536,125,538,119,538,113,537\" href=\"#ff35\" id=\"135\" alt=\"135\" />\n"
        + "<area shape=\"poly\" coords=\"149,535,152,526,151,504,156,502,167,502,173,506,167,533,167,536,163,538,155,538,149,535\" href=\"#ff34\" id=\"134\" alt=\"134\" />\n"
        + "<area shape=\"poly\" coords=\"105,506,111,502,124,503,127,504,127,526,130,533,129,536,123,538,115,538,113,536,108,515,105,506\" href=\"#ff33\" id=\"133\" alt=\"133\" />\n"
        + "<area shape=\"poly\" coords=\"151,503,150,486,150,449,151,434,156,436,165,437,172,434,178,430,181,436,182,441,182,462,180,474,177,490,173,505,166,502,161,502,153,503,151,503\" href=\"#ff32\" id=\"132\" alt=\"132\" />\n"
        + "<area shape=\"poly\" coords=\"100,430,97,448,97,462,100,481,106,506,112,502,117,502,124,502,127,504,129,486,129,449,129,446,128,434,123,436,114,437,106,434,100,430\" href=\"#ff31\" id=\"131\" alt=\"131\" />\n"
        + "<area shape=\"poly\" coords=\"151,434,147,412,146,401,158,397,166,396,168,395,176,397,179,399,178,421,179,429,171,435,164,436,157,436,151,434\" href=\"#ff30\" id=\"130\" alt=\"130\" />\n"
        + "<area shape=\"poly\" coords=\"100,429,100,403,99,398,104,396,111,396,114,395,121,397,128,399,133,401,132,412,130,423,129,431,128,435,121,437,114,436,106,433,100,429\" href=\"#ff29\" id=\"129\" alt=\"129\" />\n"
        + "<area shape=\"poly\" coords=\"146,401,143,380,143,343,141,319,140,312,140,305,144,304,148,302,154,296,159,289,165,284,171,281,175,280,180,289,185,297,190,304,194,307,194,318,192,336,187,360,183,382,180,391,179,398,174,396,168,396,161,397,154,397,146,401\" href=\"#ff27\" id=\"127\" alt=\"127\" />\n"
        + "<area shape=\"poly\" coords=\"99,397,88,345,84,317,85,308,99,289,104,279,115,286,121,291,130,301,135,304,139,305,139,312,135,340,135,380,133,400,124,398,112,395,103,397,99,397\" href=\"#ff26\" id=\"126\" alt=\"126\" />\n"
        + "<area shape=\"poly\" coords=\"218,297,218,315,219,320,220,338,221,343,223,343,224,327,231,347,234,347,235,346,237,347,239,346,238,326,241,339,242,343,245,345,245,339,243,327,242,311,251,319,257,318,258,315,250,309,245,303,235,293,230,298,227,300,222,299,218,297\" href=\"#ff28\" id=\"128\" alt=\"128\" />\n"
        + "<area shape=\"poly\" coords=\"22,318,22,315,28,310,31,305,44,293,51,300,57,300,61,298,61,314,60,321,60,337,58,343,57,344,56,343,55,327,49,347,46,347,45,346,42,347,41,347,41,327,38,340,35,345,34,345,34,339,36,328,37,311,28,319,22,318\" href=\"#ff25\" id=\"125\" alt=\"125\" />\n"
        + "<area shape=\"poly\" coords=\"44,293,46,287,50,273,60,273,65,275,68,278,63,291,61,297,57,299,52,300,44,293\" href=\"#ff19\" id=\"119\" alt=\"119\" />\n"
        + "<area shape=\"poly\" coords=\"211,278,218,296,223,299,227,299,235,293,229,273,220,273,214,275,211,278\" href=\"#ff24\" id=\"124\" alt=\"124\" />\n"
        + "<area shape=\"poly\" coords=\"183,245,178,251,175,257,173,260,173,269,175,279,181,290,187,299,194,307,194,281,189,259,183,245\" href=\"#ff23\" id=\"123\" alt=\"123\" />\n"
        + "<area shape=\"poly\" coords=\"175,276,165,276,159,272,154,268,150,262,145,259,142,258,140,258,140,304,143,304,148,302,153,297,156,292,161,287,167,283,172,280,175,279,175,276\" href=\"#ff22\" id=\"122\" alt=\"122\" />\n"
        + "<area shape=\"poly\" coords=\"140,258,133,259,128,263,124,268,120,273,114,275,105,275,104,279,114,284,119,288,127,297,133,303,135,304,139,305,140,258\" href=\"#ff21\" id=\"121\" alt=\"121\" />\n"
        + "<area shape=\"poly\" coords=\"96,245,87,268,84,284,85,307,97,292,102,282,105,274,106,260,102,251,96,245\" href=\"#ff20\" id=\"120\" alt=\"120\" />\n"
        + "<area shape=\"poly\" coords=\"228,272,220,235,218,224,215,218,207,223,202,224,197,224,191,223,188,222,191,232,195,245,202,261,211,278,214,275,220,273,228,272\" href=\"#ff18\" id=\"118\" alt=\"118\" />\n"
        + "<area shape=\"poly\" coords=\"215,218,213,213,210,210,211,204,203,200,194,200,187,205,187,217,188,221,192,223,198,223,202,223,209,222,215,218\" href=\"#ff16\" id=\"116\" alt=\"116\" />\n"
        + "<area shape=\"poly\" coords=\"205,164,210,202,202,200,194,199,186,205,181,176,181,160,183,155,184,154,187,154,193,158,203,163,205,164\" href=\"#ff14\" id=\"114\" alt=\"114\" />\n"
        + "<area shape=\"poly\" coords=\"140,182,152,183,158,188,166,193,178,193,181,191,177,210,175,219,176,226,183,246,177,250,173,258,172,262,173,270,174,275,165,275,158,272,150,262,143,258,140,257,140,182\" href=\"#ff13\" id=\"113\" alt=\"113\" />\n"
        + "<area shape=\"poly\" coords=\"96,244,103,226,103,217,98,191,103,193,110,193,116,192,124,186,130,183,133,182,139,182,140,258,133,258,129,262,125,268,119,272,113,275,105,275,106,260,102,251,96,244\" href=\"#ff12\" id=\"112\" alt=\"112\" />\n"
        + "<area shape=\"poly\" coords=\"50,272,56,245,62,223,64,218,71,222,88,223,91,221,90,228,88,236,83,246,78,258,73,270,69,277,63,274,58,273,50,272\" href=\"#ff17\" id=\"117\" alt=\"117\" />\n"
        + "<area shape=\"poly\" coords=\"64,218,67,213,69,210,69,203,78,200,85,200,92,205,91,220,87,222,72,222,64,218\" href=\"#ff15\" id=\"115\" alt=\"115\" />\n"
        + "<area shape=\"poly\" coords=\"69,201,71,186,73,171,74,164,83,160,90,156,93,153,96,156,98,160,97,188,94,203,91,204,87,200,81,199,75,200,70,202,69,201\" href=\"#ff11\" id=\"111\" alt=\"111\" />\n"
        + "<area shape=\"poly\" coords=\"205,163,195,159,188,154,179,147,172,137,164,127,159,116,155,108,151,100,156,98,160,102,167,107,173,111,179,113,186,114,192,117,196,120,200,125,202,132,204,141,205,148,205,163\" href=\"#ff10\" id=\"110\" alt=\"110\" />\n"
        + "<area shape=\"poly\" coords=\"151,101,146,103,140,103,140,180,147,182,153,184,157,186,163,191,167,193,178,193,181,191,182,186,181,178,181,165,181,161,182,156,183,154,186,153,180,147,175,142,169,134,160,121,156,111,151,101\" href=\"#ff9\" id=\"109\" alt=\"109\" />\n"
        + "<area shape=\"poly\" coords=\"139,103,139,180,132,182,127,183,122,186,118,189,114,192,104,192,99,191,97,188,98,171,97,158,95,154,94,153,103,143,111,132,117,123,122,113,127,104,128,101,134,103,139,103\" href=\"#ff8\" id=\"108\" alt=\"108\" />\n"
        + "<area shape=\"poly\" coords=\"122,97,117,103,110,108,101,111,93,113,87,116,83,119,80,122,78,128,75,138,74,150,74,164,85,158,93,153,99,147,105,140,110,133,115,125,120,117,126,105,128,101,122,97\" href=\"#ff7\" id=\"107\" alt=\"107\" />\n"
        + "<area shape=\"poly\" coords=\"140,79,140,102,145,102,151,100,155,98,155,77,147,79,140,79\" href=\"#ff6\" id=\"106\" alt=\"106\" />\n"
        + "<area shape=\"poly\" coords=\"123,77,132,78,139,79,139,102,134,102,128,100,123,97,123,77\" href=\"#ff5\" id=\"105\" alt=\"105\" />\n"
        + "<area shape=\"poly\" coords=\"140,13,150,15,156,17,160,22,163,27,149,31,140,31,140,13\" href=\"#ff2\" id=\"102\" alt=\"102\" />\n"
        + "<area shape=\"poly\" coords=\"116,27,118,23,121,20,125,17,129,15,134,14,139,13,139,31,132,31,124,30,116,27\" href=\"#ff1\" id=\"101\" alt=\"101\" />\n"
        + "<area shape=\"poly\" coords=\"163,28,154,30,146,32,140,32,140,78,147,78,155,76,158,72,160,68,161,64,162,62,168,53,169,49,169,45,168,43,164,44,164,31,163,28\" href=\"#ff4\" id=\"104\" alt=\"104\" />\n"
        + "<area shape=\"poly\" coords=\"139,78,126,77,122,76,118,66,117,61,111,55,110,47,110,45,112,43,114,43,115,44,115,30,116,28,123,30,125,30,133,31,139,32,139,78\" href=\"#ff3\" id=\"103\" alt=\"103\" />\n"
        + "</map>");
    question.setImgTag2("<map Order=\"1\" name=\"m_female_front\" id=\"m_female_front\"  class=\"imgmap\">\n"
        + "<area shape=\"poly\" coords=\"149,536,148,542,150,557,147,564,150,568,155,568,158,566,171,566,174,564,177,564,178,562,172,556,168,548,168,541,167,536,163,538,160,538,149,536\" href=\"#ff36\" id=\"136\" alt=\"136\" />\n"
        + "<area shape=\"poly\" coords=\"113,537,111,541,110,549,106,557,101,562,101,563,106,565,109,567,122,566,129,568,132,563,129,555,129,544,131,536,125,538,119,538,113,537\" href=\"#ff35\" id=\"135\" alt=\"135\" />\n"
        + "<area shape=\"poly\" coords=\"149,535,152,526,151,504,156,502,167,502,173,506,167,533,167,536,163,538,155,538,149,535\" href=\"#ff34\" id=\"134\" alt=\"134\" />\n"
        + "<area shape=\"poly\" coords=\"105,506,111,502,124,503,127,504,127,526,130,533,129,536,123,538,115,538,113,536,108,515,105,506\" href=\"#ff33\" id=\"133\" alt=\"133\" />\n"
        + "<area shape=\"poly\" coords=\"151,503,150,486,150,449,151,434,156,436,165,437,172,434,178,430,181,436,182,441,182,462,180,474,177,490,173,505,166,502,161,502,153,503,151,503\" href=\"#ff32\" id=\"132\" alt=\"132\" />\n"
        + "<area shape=\"poly\" coords=\"100,430,97,448,97,462,100,481,106,506,112,502,117,502,124,502,127,504,129,486,129,449,129,446,128,434,123,436,114,437,106,434,100,430\" href=\"#ff31\" id=\"131\" alt=\"131\" />\n"
        + "<area shape=\"poly\" coords=\"151,434,147,412,146,401,158,397,166,396,168,395,176,397,179,399,178,421,179,429,171,435,164,436,157,436,151,434\" href=\"#ff30\" id=\"130\" alt=\"130\" />\n"
        + "<area shape=\"poly\" coords=\"100,429,100,403,99,398,104,396,111,396,114,395,121,397,128,399,133,401,132,412,130,423,129,431,128,435,121,437,114,436,106,433,100,429\" href=\"#ff29\" id=\"129\" alt=\"129\" />\n"
        + "<area shape=\"poly\" coords=\"146,401,143,380,143,343,141,319,140,312,140,305,144,304,148,302,154,296,159,289,165,284,171,281,175,280,180,289,185,297,190,304,194,307,194,318,192,336,187,360,183,382,180,391,179,398,174,396,168,396,161,397,154,397,146,401\" href=\"#ff27\" id=\"127\" alt=\"127\" />\n"
        + "<area shape=\"poly\" coords=\"99,397,88,345,84,317,85,308,99,289,104,279,115,286,121,291,130,301,135,304,139,305,139,312,135,340,135,380,133,400,124,398,112,395,103,397,99,397\" href=\"#ff26\" id=\"126\" alt=\"126\" />\n"
        + "<area shape=\"poly\" coords=\"218,297,218,315,219,320,220,338,221,343,223,343,224,327,231,347,234,347,235,346,237,347,239,346,238,326,241,339,242,343,245,345,245,339,243,327,242,311,251,319,257,318,258,315,250,309,245,303,235,293,230,298,227,300,222,299,218,297\" href=\"#ff28\" id=\"128\" alt=\"128\" />\n"
        + "<area shape=\"poly\" coords=\"22,318,22,315,28,310,31,305,44,293,51,300,57,300,61,298,61,314,60,321,60,337,58,343,57,344,56,343,55,327,49,347,46,347,45,346,42,347,41,347,41,327,38,340,35,345,34,345,34,339,36,328,37,311,28,319,22,318\" href=\"#ff25\" id=\"125\" alt=\"125\" />\n"
        + "<area shape=\"poly\" coords=\"44,293,46,287,50,273,60,273,65,275,68,278,63,291,61,297,57,299,52,300,44,293\" href=\"#ff19\" id=\"119\" alt=\"119\" />\n"
        + "<area shape=\"poly\" coords=\"211,278,218,296,223,299,227,299,235,293,229,273,220,273,214,275,211,278\" href=\"#ff24\" id=\"124\" alt=\"124\" />\n"
        + "<area shape=\"poly\" coords=\"183,245,178,251,175,257,173,260,173,269,175,279,181,290,187,299,194,307,194,281,189,259,183,245\" href=\"#ff23\" id=\"123\" alt=\"123\" />\n"
        + "<area shape=\"poly\" coords=\"175,276,165,276,159,272,154,268,150,262,145,259,142,258,140,258,140,304,143,304,148,302,153,297,156,292,161,287,167,283,172,280,175,279,175,276\" href=\"#ff22\" id=\"122\" alt=\"122\" />\n"
        + "<area shape=\"poly\" coords=\"140,258,133,259,128,263,124,268,120,273,114,275,105,275,104,279,114,284,119,288,127,297,133,303,135,304,139,305,140,258\" href=\"#ff21\" id=\"121\" alt=\"121\" />\n"
        + "<area shape=\"poly\" coords=\"96,245,87,268,84,284,85,307,97,292,102,282,105,274,106,260,102,251,96,245\" href=\"#ff20\" id=\"120\" alt=\"120\" />\n"
        + "<area shape=\"poly\" coords=\"228,272,220,235,218,224,215,218,207,223,202,224,197,224,191,223,188,222,191,232,195,245,202,261,211,278,214,275,220,273,228,272\" href=\"#ff18\" id=\"118\" alt=\"118\" />\n"
        + "<area shape=\"poly\" coords=\"215,218,213,213,210,210,211,204,203,200,194,200,187,205,187,217,188,221,192,223,198,223,202,223,209,222,215,218\" href=\"#ff16\" id=\"116\" alt=\"116\" />\n"
        + "<area shape=\"poly\" coords=\"205,164,210,202,202,200,194,199,186,205,181,176,181,160,183,155,184,154,187,154,193,158,203,163,205,164\" href=\"#ff14\" id=\"114\" alt=\"114\" />\n"
        + "<area shape=\"poly\" coords=\"140,182,152,183,158,188,166,193,178,193,181,191,177,210,175,219,176,226,183,246,177,250,173,258,172,262,173,270,174,275,165,275,158,272,150,262,143,258,140,257,140,182\" href=\"#ff13\" id=\"113\" alt=\"113\" />\n"
        + "<area shape=\"poly\" coords=\"96,244,103,226,103,217,98,191,103,193,110,193,116,192,124,186,130,183,133,182,139,182,140,258,133,258,129,262,125,268,119,272,113,275,105,275,106,260,102,251,96,244\" href=\"#ff12\" id=\"112\" alt=\"112\" />\n"
        + "<area shape=\"poly\" coords=\"50,272,56,245,62,223,64,218,71,222,88,223,91,221,90,228,88,236,83,246,78,258,73,270,69,277,63,274,58,273,50,272\" href=\"#ff17\" id=\"117\" alt=\"117\" />\n"
        + "<area shape=\"poly\" coords=\"64,218,67,213,69,210,69,203,78,200,85,200,92,205,91,220,87,222,72,222,64,218\" href=\"#ff15\" id=\"115\" alt=\"115\" />\n"
        + "<area shape=\"poly\" coords=\"69,201,71,186,73,171,74,164,83,160,90,156,93,153,96,156,98,160,97,188,94,203,91,204,87,200,81,199,75,200,70,202,69,201\" href=\"#ff11\" id=\"111\" alt=\"111\" />\n"
        + "<area shape=\"poly\" coords=\"205,163,195,159,188,154,179,147,172,137,164,127,159,116,155,108,151,100,156,98,160,102,167,107,173,111,179,113,186,114,192,117,196,120,200,125,202,132,204,141,205,148,205,163\" href=\"#ff10\" id=\"110\" alt=\"110\" />\n"
        + "<area shape=\"poly\" coords=\"151,101,146,103,140,103,140,180,147,182,153,184,157,186,163,191,167,193,178,193,181,191,182,186,181,178,181,165,181,161,182,156,183,154,186,153,180,147,175,142,169,134,160,121,156,111,151,101\" href=\"#ff9\" id=\"109\" alt=\"109\" />\n"
        + "<area shape=\"poly\" coords=\"139,103,139,180,132,182,127,183,122,186,118,189,114,192,104,192,99,191,97,188,98,171,97,158,95,154,94,153,103,143,111,132,117,123,122,113,127,104,128,101,134,103,139,103\" href=\"#ff8\" id=\"108\" alt=\"108\" />\n"
        + "<area shape=\"poly\" coords=\"122,97,117,103,110,108,101,111,93,113,87,116,83,119,80,122,78,128,75,138,74,150,74,164,85,158,93,153,99,147,105,140,110,133,115,125,120,117,126,105,128,101,122,97\" href=\"#ff7\" id=\"107\" alt=\"107\" />\n"
        + "<area shape=\"poly\" coords=\"140,79,140,102,145,102,151,100,155,98,155,77,147,79,140,79\" href=\"#ff6\" id=\"106\" alt=\"106\" />\n"
        + "<area shape=\"poly\" coords=\"123,77,132,78,139,79,139,102,134,102,128,100,123,97,123,77\" href=\"#ff5\" id=\"105\" alt=\"105\" />\n"
        + "<area shape=\"poly\" coords=\"140,13,150,15,156,17,160,22,163,27,149,31,140,31,140,13\" href=\"#ff2\" id=\"102\" alt=\"102\" />\n"
        + "<area shape=\"poly\" coords=\"116,27,118,23,121,20,125,17,129,15,134,14,139,13,139,31,132,31,124,30,116,27\" href=\"#ff1\" id=\"101\" alt=\"101\" />\n"
        + "<area shape=\"poly\" coords=\"163,28,154,30,146,32,140,32,140,78,147,78,155,76,158,72,160,68,161,64,162,62,168,53,169,49,169,45,168,43,164,44,164,31,163,28\" href=\"#ff4\" id=\"104\" alt=\"104\" />\n"
        + "<area shape=\"poly\" coords=\"139,78,126,77,122,76,118,66,117,61,111,55,110,47,110,45,112,43,114,43,115,44,115,30,116,28,123,30,125,30,133,31,139,32,139,78\" href=\"#ff3\" id=\"103\" alt=\"103\" />\n"
        + "</map>\n"
        + "<img name=\"female_back\" src=\"images/maps/female_back.071813.cache.png\" width=\"263\" height=\"568\" id=\"female_back\" usemap=\"#m_female_back\" alt=\"Female back image\" border=\"0\"/>");
    question.setMapTag2("<map Order=\"2\" name=\"m_female_back\" id=\"m_female_back\">\n"
        + "<area shape=\"poly\" coords=\"139,521,139,529,140,537,138,542,137,545,137,549,140,551,144,552,147,549,159,550,162,547,166,547,167,545,163,543,160,539,156,529,156,524,155,523,155,521,152,522,145,522,139,521\" href=\"#fb38\" id=\"238\" alt=\"238\" />\n"
        + "<area shape=\"poly\" coords=\"104,520,102,522,102,526,101,532,99,537,96,542,93,545,93,548,97,548,99,550,112,550,115,551,118,552,121,549,122,547,121,543,119,539,119,528,121,526,120,520,114,522,107,523,104,520\" href=\"#fb37\" id=\"237\" alt=\"237\" />\n"
        + "<area shape=\"poly\" coords=\"138,520,140,513,140,511,141,489,145,488,145,487,154,487,159,490,162,491,159,502,157,513,155,516,155,519,151,522,147,523,138,520\" href=\"#fb36\" id=\"236\" alt=\"236\" />\n"
        + "<area shape=\"poly\" coords=\"103,520,102,517,100,504,97,491,101,489,103,487,113,487,118,490,117,509,119,515,120,519,116,521,112,523,108,522,103,520\" href=\"#fb35\" id=\"235\" alt=\"235\" />\n"
        + "<area shape=\"poly\" coords=\"140,488,139,469,138,440,139,437,140,423,147,424,154,424,161,420,167,417,170,428,170,435,170,450,167,467,163,483,162,491,157,489,153,488,146,488,140,488\" href=\"#fb34\" id=\"234\" alt=\"234\" />\n"
        + "<area shape=\"poly\" coords=\"97,491,91,468,89,454,88,430,90,422,91,418,94,419,102,423,115,424,118,422,119,432,120,446,120,448,120,463,119,476,118,489,113,488,104,487,97,491\" href=\"#fb33\" id=\"233\" alt=\"233\" />\n"
        + "<area shape=\"poly\" coords=\"140,423,138,410,136,398,135,389,143,388,149,386,163,386,167,388,167,393,166,415,168,418,159,422,151,424,147,424,140,423\" href=\"#fb32\" id=\"232\" alt=\"232\" />\n"
        + "<area shape=\"poly\" coords=\"92,418,93,409,93,396,91,387,96,385,104,386,113,386,123,390,121,400,120,411,118,422,112,424,106,424,97,420,92,418\" href=\"#fb31\" id=\"231\" alt=\"231\" />\n"
        + "<area shape=\"poly\" coords=\"204,290,203,301,206,316,207,332,208,334,210,334,211,319,214,329,217,338,219,338,221,337,222,338,224,338,223,319,226,329,230,335,231,332,228,317,228,304,233,307,237,310,241,310,243,308,240,306,235,301,231,294,221,285,218,289,214,291,207,292,204,290\" href=\"#fb30\" id=\"230\" alt=\"230\" />\n"
        + "<area shape=\"poly\" coords=\"135,389,134,379,133,339,132,318,131,306,131,305,138,308,150,309,163,306,171,303,179,300,182,300,182,315,179,332,176,346,173,361,171,373,168,387,161,386,155,385,144,387,135,389\" href=\"#fb29\" id=\"229\" alt=\"229\" />\n"
        + "<area shape=\"poly\" coords=\"123,390,112,386,95,386,91,387,86,362,82,344,79,326,77,308,77,298,90,304,101,307,105,308,124,308,128,305,127,326,125,342,126,371,123,390\" href=\"#fb28\" id=\"228\" alt=\"228\" />\n"
        + "<area shape=\"poly\" coords=\"27,296,38,286,44,291,52,291,54,290,54,306,52,311,53,329,51,331,51,334,50,335,49,333,48,319,46,326,45,331,42,337,41,339,41,339,40,339,38,337,36,338,34,336,35,324,33,328,31,333,30,335,27,336,28,331,31,318,31,310,31,303,29,305,25,309,22,310,17,310,17,307,19,305,23,303,24,299,27,296\" href=\"#fb27\" id=\"227\" alt=\"227\" />\n"
        + "<area shape=\"poly\" coords=\"205,290,197,272,201,269,204,267,207,266,216,266,220,283,220,285,219,288,216,290,215,292,208,291,205,290\" href=\"#fb26\" id=\"226\" alt=\"226\" />\n"
        + "<area shape=\"poly\" coords=\"163,250,162,254,161,268,165,275,170,284,174,291,182,299,167,304,154,307,148,309,138,308,131,306,130,304,130,256,136,255,149,253,158,251,161,250,163,250\" href=\"#fb24\" id=\"224\" alt=\"224\" />\n"
        + "<area shape=\"poly\" coords=\"78,299,91,303,105,308,124,307,128,305,129,303,130,256,118,254,111,253,102,252,99,251,96,251,97,256,97,265,95,271,92,279,88,285,84,291,78,299\" href=\"#fb23\" id=\"223\" alt=\"223\" />\n"
        + "<area shape=\"poly\" coords=\"170,240,175,250,178,260,181,271,182,282,182,299,178,295,173,289,168,280,164,272,162,267,162,253,164,249,166,244,170,240\" href=\"#fb25\" id=\"225\" alt=\"225\" />\n"
        + "<area shape=\"poly\" coords=\"77,298,77,275,78,265,82,255,86,245,88,240,94,245,96,251,97,256,98,263,96,269,93,277,89,283,85,289,81,294,77,298\" href=\"#fb22\" id=\"222\" alt=\"222\" />\n"
        + "<area shape=\"poly\" coords=\"175,216,178,224,184,241,191,258,197,271,202,267,207,266,215,265,210,241,205,222,202,215,202,214,194,217,181,217,175,216\" href=\"#fb20\" id=\"220\" alt=\"220\" />\n"
        + "<area shape=\"poly\" coords=\"37,285,41,272,43,266,53,266,57,267,62,272,56,282,54,290,51,292,44,291,40,288,37,285\" href=\"#fb21\" id=\"221\" alt=\"221\" />\n"
        + "<area shape=\"poly\" coords=\"171,239,166,245,163,251,144,254,135,255,130,255,130,217,149,217,158,216,159,214,164,215,165,223,171,239\" href=\"#fb19\" id=\"219\" alt=\"219\" />\n"
        + "<area shape=\"poly\" coords=\"88,240,95,221,95,215,109,217,129,217,129,255,111,254,102,252,97,251,93,245,88,240\" href=\"#fb18\" id=\"218\" alt=\"218\" />\n"
        + "<area shape=\"poly\" coords=\"44,265,50,233,54,221,56,214,63,217,78,217,83,216,82,222,78,232,74,244,68,257,62,271,55,266,48,265,44,265\" href=\"#fb17\" id=\"217\" alt=\"217\" />\n"
        + "<area shape=\"poly\" coords=\"201,213,202,211,197,207,197,199,192,197,189,196,182,196,174,200,174,213,176,216,179,217,194,218,198,215,201,213\" href=\"#fb16\" id=\"216\" alt=\"216\" />\n"
        + "<area shape=\"poly\" coords=\"196,198,196,190,193,168,192,161,189,160,182,156,174,150,171,153,169,160,169,173,170,186,174,200,179,197,182,196,190,196,193,198,196,198\" href=\"#fb14\" id=\"214\" alt=\"214\" />\n"
        + "<area shape=\"poly\" coords=\"171,153,156,155,130,155,130,216,147,216,157,215,164,214,166,201,169,191,170,186,169,175,169,172,169,159,171,153\" href=\"#fb13\" id=\"213\" alt=\"213\" />\n"
        + "<area shape=\"poly\" coords=\"129,216,129,154,106,154,96,153,88,153,90,159,89,179,88,185,93,204,95,214,110,216,129,216\" href=\"#fb12\" id=\"212\" alt=\"212\" />\n"
        + "<area shape=\"poly\" coords=\"57,213,58,210,60,207,61,206,62,198,64,198,65,197,68,197,69,195,77,196,80,198,83,200,83,216,78,217,65,217,57,213\" href=\"#fb15\" id=\"215\" alt=\"215\" />\n"
        + "<area shape=\"poly\" coords=\"61,198,63,186,65,173,67,165,67,161,73,159,76,157,80,155,83,152,85,151,88,154,90,161,89,177,88,184,87,190,85,196,84,200,80,197,76,196,69,196,64,197,61,198\" href=\"#fb11\" id=\"211\" alt=\"211\" />\n"
        + "<area shape=\"poly\" coords=\"192,161,192,145,190,130,187,123,184,118,179,115,172,112,164,110,157,107,150,101,146,97,140,100,145,110,148,115,150,120,154,127,159,133,163,139,166,142,170,147,175,151,181,155,186,159,190,161,192,161\" href=\"#fb10\" id=\"210\" alt=\"210\" />\n"
        + "<area shape=\"poly\" coords=\"173,150,170,152,152,155,130,154,130,101,134,101,138,100,140,100,142,105,145,112,150,121,156,129,160,136,163,139,166,143,173,150\" href=\"#fb9\" id=\"209\" alt=\"209\" />\n"
        + "<area shape=\"poly\" coords=\"129,102,129,154,110,155,97,153,88,153,85,150,92,144,96,138,100,134,103,128,107,124,109,119,111,116,113,112,115,108,117,103,118,103,117,99,123,101,129,102\" href=\"#fb8\" id=\"208\" alt=\"208\" />\n"
        + "<area shape=\"poly\" coords=\"114,97,108,102,100,107,91,111,81,114,74,118,71,122,69,129,67,140,67,161,75,157,80,153,86,149,92,143,97,137,102,131,106,124,110,117,113,111,116,105,117,102,117,99,114,97\" href=\"#fb7\" id=\"207\" alt=\"207\" />\n"
        + "<area shape=\"poly\" coords=\"145,97,145,77,136,78,130,79,130,101,136,101,142,99,145,97\" href=\"#fb6\" id=\"206\" alt=\"206\" />\n"
        + "<area shape=\"poly\" coords=\"114,97,119,99,124,101,129,101,129,79,126,78,118,77,114,77,114,97\" href=\"#fb5\" id=\"205\" alt=\"205\" />\n"
        + "<area shape=\"poly\" coords=\"130,78,138,77,145,76,149,70,151,65,151,62,152,61,156,55,158,52,158,45,153,45,153,29,146,31,136,32,130,33,130,78\" href=\"#fb4\" id=\"204\" alt=\"204\" />\n"
        + "<area shape=\"poly\" coords=\"129,78,129,33,119,33,111,31,107,29,105,31,105,45,104,44,102,45,101,46,100,51,103,56,106,60,109,63,109,68,110,73,114,76,124,78,129,78\" href=\"#fb3\" id=\"203\" alt=\"203\" />\n"
        + "<area shape=\"poly\" coords=\"151,29,149,25,145,20,140,17,133,15,129,15,130,32,139,32,147,30,151,29\" href=\"#fb2\" id=\"202\" alt=\"202\" />\n"
        + "<area shape=\"poly\" coords=\"108,25,107,28,115,31,120,32,129,33,129,15,120,15,114,19,110,23,108,25\" href=\"fb1\" id=\"201\" alt=\"201\" />\n"
        + "</map>");
    question.setFemale(true);

    return bean;
  }

  private AutoBean<TextInputQuestion> textInputQuestion(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.textList);

    AutoBean<TextInputQuestion> bean = factory.textInputQuestion();
    TextInputQuestion question = bean.as();

    ArrayList<String> labels = new ArrayList<>();
    labels.add("First name");
    labels.add("Last name");
    labels.add("PIN code");
    question.setTextBoxLabels(labels);

    return bean;
  }

  private AutoBean<FormQuestion> thankYou(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    FormField videoField = field("thankYou", FieldType.videoLink);
    HashMap <String, String> attributes = new HashMap<>();

    attributes.put("partnerId", "1392761");
    attributes.put("uiConfId", "13627922");
    attributes.put("targetId","kaltura_player_54de54b87483f");
    attributes.put("wid", "0_qxhk8jxu");
    attributes.put("entryId", "0_9bbcta99");
    attributes.put("videoHeight", "168");
    attributes.put("videoWidth", "299");
    attributes.put("frameWidth","365");
    attributes.put("frameHeight","216");
    videoField.setAttributes(attributes);

    AutoBean<FormQuestion> form = form("Thank you for completing this questionnaire.", videoField);
    form.as().setTerminal(true);
    return form;
  }

  private AutoBean<FormQuestion> number(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("In the past 6 months, how many times did you visit a physician?",
            "Do not include visits while in the hospital or to a hospital emergency room.",
            required(range(field("physicianVisits", FieldType.number, "Number of visits"), "0", "200"))
        );
  }

  private AutoBean<FormQuestion> numbers(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("How long have you had your pain problem?",
            field("painYears", FieldType.number, "Years"),
            field("painMonths", FieldType.number, "Months"),
            field("painDays", FieldType.number, "Days")
        );
  }

  private AutoBean<FormQuestion> text(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Who is your primary care physician?",
            required(field("physician", FieldType.text))
        );
  }

  private AutoBean<FormQuestion> textArea(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("What are the specific question(s) that you or your doctor want answered today?",
            required(field("questions", FieldType.textArea))
        );
  }

  private AutoBean<FormQuestion> checkboxes(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Describe your current pain",
            field("currentPainDesc", FieldType.checkboxes,
                value("Throbbing"),
                value("Shooting"),
                value("Stabbing"),
                value("Sharp"),
                value("Cramping"),
                value("Gnawing"),
                value("Hot"),
                value("Burning"),
                value("Aching"),
                value("Heavy"),
                value("Tender"),
                value("Splitting"),
                value("Tiring"),
                value("Exhausting"),
                value("Sickening"),
                value("Fearful"),
                value("Punishing"),
                value("Cruel")
            )
        );
  }

  private AutoBean<FormQuestion> checkboxesRequired(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Describe your current pain",
            required(field("currentPainDesc", FieldType.checkboxes,
                value("Throbbing"),
                value("Shooting"),
                value("Stabbing")
            ))
        );
  }

  private AutoBean<FormQuestion> checkboxesAndOr(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Please describe the timing of your pain",
            field("painTiming", FieldType.checkboxes,
                value("Brief"),
                value("Constant"),
                value("Comes and goes"),
                value("Continuous"),
                value("Always there"),
                value("Appears and disappears"),
                value("Intermittent")
            ),
            field("painTimingOther", FieldType.textArea, "and/or")
        );
  }

  private AutoBean<FormQuestion> conditional(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Do you smoke?",
            field("smoke", FieldType.radios,
                value("No"),
                value("Yes",
                    field("smokePacks", FieldType.number, "How many packs per day?")
                )
            )
        );
  }

  private AutoBean<FormQuestion> conditional3Level(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Are you receiving any kind of disability?",
            required(field("disability", FieldType.radios,
                value("No"),
                value("Yes",
                    required(field("disabilityType", FieldType.radios, "What kind of disability?",
                        value("Worker's Compensation"),
                        value("Social Security Disability Insurance (SSDI)"),
                        value("Other",
                            required(field("disabilityTypeOther", FieldType.textArea, "What other kind?"))
                        )
                    ))
                )
            ))
        );
  }

  private AutoBean<FormQuestion> checkboxConditional3Level(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("Which do you like (choose all that apply)?",
          field("foodPref", FieldType.checkboxes,
            value("Apples",
              field("appleType", FieldType.checkboxes, "What kind of apples?",
                value("Granny Smith"),
                value("Fuji"),
                value("Other",
                  required(field("appleTypeOther", FieldType.textArea, "What other kind of apples?"))
                )
              )
            ),
            value("Oranges",
              field("orangeType", FieldType.checkboxes, "What kind of oranges?",
                value("Navel"),
                value("Mandarin"),
                value("Other",
                  required(field("orangeTypeOther", FieldType.textArea, "What other kind of oranges?"))
                )
              )
            )
          )
        );
  }

  private AutoBean<CollapsibleRadiosetQuestion> consentQuestion(DisplayStatus displayStatus) {

    displayStatus.setQuestionType(QuestionType.collapsibleRadioset);
    AutoBean<CollapsibleRadiosetQuestion> bean = factory.collapsibleRadiosetQuestion();
    CollapsibleRadiosetQuestion question = bean.as();
    question.setTitle1("May we include you in our Research Database?");
    //question.setTitle2("How many times have you eaten apples?");
    question.setCollapsibleHeading("More Information");
    question.setCollapsibleContent(
        "<p>Many of the physicians here at the Stanford Pain Center conduct research to learn more about chronic pain and develop new therapies to treat pain.</p>\n"
            + "          <p><b>We would like to include you in our Research Database</b> so that we may contact you regarding current or future research studies.</p>\n"
            + "          <p>Indicating \"Yes\" to the research database:\n"
            + "            <ul>\n"
            + "              <li>Allows us to access information from your medical record (e.g. name, contact information, age, pain condition) to match you with research studies for which you may be eligible. </li>\n"
            + "              <li>It does not obligate you to be a research participant, but allows us to contact you if we think you may be eligible for one of our research studies.</li>\n"
            + "              <li>You may contact the research team at any time to be removed from our contact list.</li>\n"
            + "            </ul>\n"
            + "          </p>\n"
            + "          <p>You may choose not to participate, or not to speak with us further about our research at any time, without affecting your medical treatment.\n"
            + "          Completing this form is voluntary and is not related to your medical appointment. All the information we collect will remain as confidential as possible as required by law.  \n"
            + "          We will never share your personal identifying information outside of the Stanford School of Medicine Researchers who are approved to access this information.\n"
            + "          </p><p>For general information about participant rights, contact 1-866-680-2906.</p>");

    ArrayList<String> choices = new ArrayList<>();
    choices.add("Yes, your research team may contact me to discuss research opportunities.");
    choices.add("No thank you.");
    choices.add("Ask me again later");
    question.setChoices(choices);
    return bean;
  }

  private AutoBean<FormQuestion> datePicker(DisplayStatus displayStatus) {
    displayStatus.setQuestionType(QuestionType.form);

    return
        form("What date do you like?",
            required(field("datepicker", FieldType.datePicker))
        );

  }
  private AutoBean<FormQuestion> form(String title1, FormField... fields) {
    return form(title1, null, fields);
  }

  private AutoBean<FormQuestion> form(String title1, String title2, FormField... fields) {
    AutoBean<FormQuestion> bean = factory.formQuestion();
    FormQuestion question = bean.as();

    question.setTitle1(title1);
    question.setTitle2(title2);

    ArrayList<FormField> fieldList = new ArrayList<>();
    Collections.addAll(fieldList, fields);
    question.setFields(fieldList);

    return bean;
  }

  private FormField required(FormField field) {
    field.setRequired(true);
    return field;
  }

  private FormField range(FormField field, String min, String max) {
    field.setMin(min);
    field.setMax(max);
    return field;
  }

  private FormField field(String fieldId, FieldType type, String label) {
    FormField field = factory.field().as();
    field.setFieldId(fieldId);
    field.setType(type);
    field.setLabel(label);
    return field;
  }

  private FormField field(String fieldId, FieldType type, FormFieldValue... values) {
    return field(fieldId, type, null, values);
  }

  private FormField field(String fieldId, FieldType type, String label, FormFieldValue... values) {
    FormField field = factory.field().as();
    field.setFieldId(fieldId);
    field.setType(type);
    field.setLabel(label);
    ArrayList<FormFieldValue> valuesList = new ArrayList<>();
    Collections.addAll(valuesList, values);
    field.setValues(valuesList);
    return field;
  }

  private FormFieldValue value(String label) {
    return value("id-" + label, label);
  }

  private FormFieldValue value(String id, String label) {
    FormFieldValue value = factory.value().as();
    value.setId(id);
    value.setLabel(label);
    return value;
  }

  private FormFieldValue value(String label, FormField... fields) {
    return value("id-" + label, label, fields);
  }

  private FormFieldValue value(String id, String label, FormField... fields) {
    FormFieldValue value = factory.value().as();
    value.setId(id);
    value.setLabel(label);

    ArrayList<FormField> fieldList = new ArrayList<>();
    Collections.addAll(fieldList, fields);
    value.setFields(fieldList);

    return value;
  }

  public String getStyleSheetName() {
    return "painmanagement-2016-01-19.cache.css";
  }

  @Override
  public void addPlayerProgress(String statusJson, String targetId, String action, Long milliseconds) {
    GWT.log("Received player progress " + action + " for target " + targetId + ": " + milliseconds + " " + statusJson);
  }

  @Override
  public String[] getSurveySites() {
    SurveySite ssite = factory.surveySite().as();
    ssite.setDisplayName("Default site");
    ssite.setEnabled("Y");
    ssite.setSiteId(1L);
    ssite.setUrlParam("1");
    AutoBean<SurveySite> siteBean = AutoBeanUtils.getAutoBean(ssite);
    String siteJson = AutoBeanCodex.encode(siteBean).getPayload();
    return new String[] { siteJson };
  }

}
