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

package edu.stanford.survey.client.ui;

import edu.stanford.survey.client.api.NumericAnswer;
import edu.stanford.survey.client.api.SliderQuestion;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;
import com.sksamuel.jqm4gwt.html.Paragraph;

/**
 * This survey page presents two lines of instructions, a set of radio
 * button options, and a continue button. One of the radio buttons must
 * be selected before continuing.
 */
class NumericScalePage extends SurveyPage {
  interface Submit {
    void submit(NumericAnswer answer);
  }

  String radiosetId;
  String leftLabel;
  String rightLabel;
  boolean limitWidth;

  NumericScalePage(final SliderQuestion question, final NumericAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle1()).asString() + "</h3>"));
    }

    if (question.getTitle2() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle2()).asString() + "</h3>"));
    }

    limitWidth = question.getShowValue();
    final JQMRadioset radioset = new JQMRadioset().withHorizontal();
    if ( question.getShowValue()) {
      radioset.addStyleName(SurveyBundle.INSTANCE.css().unlabeledFieldset());
    } else {
      radioset.addStyleName(SurveyBundle.INSTANCE.css().fullWidthUnlabeledFieldset());
    }
    if (question.getLowerBound() < question.getUpperBound()) {
      for (int choice = question.getLowerBound(); choice <= question.getUpperBound(); choice++) {
        radioset.addRadio(Integer.toString(choice), (question.getShowValue() ? Integer.toString(choice) : ""));
      }
    } else {
      for (int choice = question.getLowerBound(); choice >= question.getUpperBound(); choice--) {
        radioset.addRadio(Integer.toString(choice), (question.getShowValue() ? Integer.toString(choice) : ""));
      }
    }
    add(radioset);


    radiosetId = radioset.getId();
    if (question.getLowerBoundLabel() == null && question.getUpperBoundLabel() == null) {
      radioset.addStyleName(SurveyBundle.INSTANCE.css().unlabeledNumericScale());
    } else {
      radioset.addStyleName(SurveyBundle.INSTANCE.css().labeledNumericScale());
      leftLabel = Sanitizer.sanitizeHtml(question.getLowerBoundLabel()).asString();
      rightLabel = Sanitizer.sanitizeHtml(question.getUpperBoundLabel()).asString();
    }

    final Paragraph error = new Paragraph(" ");
    error.addStyleName(SurveyBundle.INSTANCE.css().errorMessage());
    add(error);

    final JQMButton button = new JQMButton(new Button("Continue")) {};
    button.addStyleName(SurveyBundle.INSTANCE.css().continueButton());
    button.addTapHandler(new TapHandler() {
      @Override
      public void onTap(TapEvent event) {
        String selectedValue = radioset.getSelectedValue();
        if (question.getRequired() && selectedValue == null) {
          error.setText("Choose one of the options");
          return;
        }
        if (selectedValue != null) {
          answer.setChoice(Integer.parseInt(selectedValue));
        }
        submit.submit(answer);
      }
    });
    add(button);
  }

  @Override
  protected void onPageBeforeShow() {
    if ((leftLabel != null && leftLabel.length() > 0) || (rightLabel != null && rightLabel.length() > 0)) {
      String prefix1 = "<span class=\"s-scale-left-label\" style=\"position: absolute; left:0; "+(limitWidth ? "right:70px; width:90px;" : "")+"top:"+(limitWidth ? "50" : "65")+"px;\">";
      String prefix2 = "<span class=\"s-scale-right-label\" style=\"position: absolute; "+(limitWidth ? "left:-70px; " : "")+"right:0; top:"+(limitWidth ? "50" : "65")+"px; text-align:right;\">";
      hackLabels(radiosetId, leftLabel, rightLabel, prefix1, prefix2);
    }
  }

  // This tries to overlay a couple (assumed to be short) labels underneath the buttons
  // The bottom is padded to accommodate this, and the top is also padded because there
  // is a css rule in the html to move the left label to the top if the screen width is
  // small to avoid obscuring it
  private native void hackLabels(String radiosetId, String lowerBoundLabel, String upperBoundLabel, String prefix1, String prefix2) /*-{
    //$wnd.$("#" + radiosetId).css({paddingBottom: "50px", paddingTop: "50px"});
    $wnd.$("#" + radiosetId
                    + " .ui-radio:first-child").prepend(prefix1
                    + lowerBoundLabel + '</span>');
    $wnd.$("#" + radiosetId
                    + " .ui-radio:last-child").append(prefix2
                    + upperBoundLabel + '</span>');
  }-*/;
}
