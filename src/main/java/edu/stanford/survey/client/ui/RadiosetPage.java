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

import edu.stanford.survey.client.api.RadiosetAnswer;
import edu.stanford.survey.client.api.RadiosetQuestion;

import com.google.gwt.user.client.ui.Button;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;
import com.sksamuel.jqm4gwt.html.Heading;
import com.sksamuel.jqm4gwt.html.Paragraph;

/**
 * This survey page presents two lines of instructions, a set of radio
 * button options, and a continue button. One of the radio buttons must
 * be selected before continuing.
 */
class RadiosetPage extends SurveyPage {
  interface Submit {
    void submit(RadiosetAnswer answer);
  }

  RadiosetPage(RadiosetQuestion question, final RadiosetAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new Heading(3, question.getTitle1()));
    }

    if (question.getTitle2() != null) {
      add(new Heading(3, question.getTitle2()));
    }

    final JQMRadioset radioset = new JQMRadioset();
    for (String choice : question.getChoices()) {
      radioset.addRadio(choice);
    }
    add(radioset);

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
}
