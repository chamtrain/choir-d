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
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.CustomJQMSlider;
import com.sksamuel.jqm4gwt.html.Heading;

/**
 * This survey page presents two lines of instructions, a set of radio
 * button options, and a continue button. One of the radio buttons must
 * be selected before continuing.
 */
class SliderPage extends SurveyPage {
  interface Submit {
    void submit(NumericAnswer answer);
  }

  SliderPage(SliderQuestion question, final NumericAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new Heading(3, question.getTitle1()));
    }

    if (question.getTitle2() != null) {
      add(new Heading(3, question.getTitle2()));
    }

    final CustomJQMSlider slider = new CustomJQMSlider("", question.getLowerBound(), question.getUpperBound());
    add(slider);

    final JQMButton button = new JQMButton(new Button("Continue")) {};
    button.addStyleName(SurveyBundle.INSTANCE.css().continueButton());
    button.addTapHandler(new TapHandler() {
      @Override
      public void onTap(TapEvent event) {
        answer.setChoice(Integer.parseInt(slider.getValue()));
        submit.submit(answer);
      }
    });
    add(button);
  }
}