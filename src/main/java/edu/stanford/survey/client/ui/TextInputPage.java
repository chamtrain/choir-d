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

import edu.stanford.survey.client.api.TextInputAnswer;
import edu.stanford.survey.client.api.TextInputQuestion;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.ui.Button;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMText;
import com.sksamuel.jqm4gwt.html.Heading;
import com.sksamuel.jqm4gwt.html.Paragraph;

/**
 * A flat list of label plus text input, with no validation.
 */
class TextInputPage extends SurveyPage {
  private ArrayList<JQMText> texts = new ArrayList<>();
  private final Paragraph error;

  interface Submit {
    void submit(TextInputAnswer answer);
  }

  TextInputPage(TextInputQuestion question, final TextInputAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new Heading(3, question.getTitle1()));
    }

    if (question.getTitle2() != null) {
      add(new Heading(3, question.getTitle2()));
    }

    for (final String label : question.getTextBoxLabels()) {
      JQMText text = new JQMText(label);
      texts.add(text);
      add(text);
    }

    error = new Paragraph();
    error.addStyleName(SurveyBundle.INSTANCE.css().errorMessage());
    add(error);

    final JQMButton button = new JQMButton(new Button("Continue")) {};
    button.addStyleName(SurveyBundle.INSTANCE.css().continueButton());
    button.addTapHandler(new TapHandler() {
      @Override
      public void onTap(TapEvent event) {
        HashMap<String, String> labelValuePairs = new HashMap<>();
        for (JQMText text : texts) {
          labelValuePairs.put(text.getText(), text.getValue());
        }
        answer.setChoice(labelValuePairs);
        submit.submit(answer);
      }
    });
    add(button);
  }

  void serverValidationFailed(TextInputQuestion question, String validationMessage) {
    if (validationMessage == null) {
      error.setText(question.getServerValidationMessage());
    } else {
      error.setText(validationMessage);
    }
  }
}
