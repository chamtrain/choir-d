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

import edu.stanford.survey.client.api.CollapsibleRadiosetQuestion;
import edu.stanford.survey.client.api.RadiosetAnswer;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;
import com.sksamuel.jqm4gwt.html.Paragraph;
import com.sksamuel.jqm4gwt.layout.JQMCollapsible;

/**
 * This survey page presents two lines of instructions, a set of radio
 * button options, and a continue button. One of the radio buttons must
 * be selected before continuing.
 */
class CollapsibleRadiosetPage extends SurveyPage {
  interface Submit {
    void submit(RadiosetAnswer answer);
  }

  CollapsibleRadiosetPage(CollapsibleRadiosetQuestion question, final RadiosetAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle1()).asString() + "</h3>"));

    }

    if (question.getTitle2() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle2()).asString() + "</h3>"));
    }

    final JQMRadioset radioset = new JQMRadioset();
    for (String choice : question.getChoices()) {
      radioset.addRadio(choice);
    }
    add(radioset);

    String content = question.getCollapsibleContent();
    if (!content.isEmpty()) {
      add(createCollapsible(question.getCollapsibleHeading(), content));
    }

    final Paragraph error = new Paragraph();
    error.addStyleName(SurveyBundle.INSTANCE.css().errorMessage());
    add(error);

    final JQMButton button = new JQMButton(new Button("Continue")) {};
    button.addStyleName(SurveyBundle.INSTANCE.css().continueButton());
    button.addTapHandler(new TapHandler() {
      @Override
      public void onTap(TapEvent event) {
        String selectedValue = radioset.getSelectedValue();
        if (selectedValue == null) {
          error.setText("Choose one of the options");
          return;
        }
        answer.setChoice(selectedValue);
        submit.submit(answer);
      }
    });
    add(button);
  }

  protected JQMCollapsible createCollapsible(String heading, String collapsibleContent) {
    JQMCollapsible collContent = new JQMCollapsible(heading);
    collContent.add(new HTML(Sanitizer.sanitizeHtml(collapsibleContent)));
    return collContent;
  }
}
