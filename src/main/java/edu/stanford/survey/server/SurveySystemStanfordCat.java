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

package edu.stanford.survey.server;

import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.server.CatAlgorithm.Item;
import edu.stanford.survey.server.CatAlgorithm.ItemBank;
import edu.stanford.survey.server.CatAlgorithm.Response;
import edu.stanford.survey.server.CatAlgorithm.Score;
import edu.stanford.survey.server.promis.Bank;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of computer adaptive testing (CAT) section using the Stanford
 * customized algorithm.
 */
public class SurveySystemStanfordCat extends SurveySystemBase {
  private final ItemBank bank;
  private final boolean allowSkipping;
  private final String providerId;
  private final String sectionId;
  private final List<Response> answers = new ArrayList<>();
  private final List<Item> ignore = new ArrayList<>();
  private CatAlgorithm2 cat;

  public SurveySystemStanfordCat(Bank bank, boolean allowSkipping, String providerId, String sectionId) {
    this.bank = bank.bank();
    this.allowSkipping = allowSkipping;
    this.providerId = providerId;
    this.sectionId = sectionId;
  }

  // 
  public SurveySystemStanfordCat() {
    this(Bank.physicalFunction2, false, "StanfordCat", Bank.physicalFunction2.officialName());
  }

  @Override
  public Question nextQuestion(Answer answer, Survey survey) {
    String validationError = null;
    if (answer != null) {
      String questionId = answer.getSubmitStatus().getQuestionId();
      String responseValue = answer.formFieldValue("response");
      if ("skip".equals(responseValue)) {
        if (!allowSkipping) {
          validationError = "This question may not be skipped";
        }
      } else {
        Response response = bank.response(questionId, responseValue);
        if (response == null) {
          validationError = "The response is not valid";
        }
      }
    }

    for (SurveyStep step : survey.answeredStepsByProviderSection(providerId, sectionId)) {
      readAnswer(step.answer(), answers, ignore);
    }

    if (validationError == null) {
      readAnswer(answer, answers, ignore);
    }

    Question next = cat2(bank, answers, ignore, 0);
    if (next != null) {
      next.validate(validationError);
    }
    return next;
  }

  public Score score() {
    if (cat == null) {
      throw new RuntimeException("You must call nextQuestion() before score()");
    }
    return cat.score(answers);
  }

  private void readAnswer(Answer answer, List<Response> answers, List<Item> ignore) {
    if (answer == null || !answer.providedBy(providerId) || !answer.inSection(sectionId)) {
      return;
    }

    String questionId = answer.getSubmitStatus().getQuestionId();
    String responseValue = answer.formFieldValue("response");
    if ("skip".equals(responseValue)) {
      Item item = bank.item(questionId);
      if (item != null) {
        ignore.add(bank.item(questionId));
      }
    } else {
      Response response = bank.response(questionId, responseValue);
      if (response != null) {
        answers.add(response);
      }
    }
  }

  private Question cat2(ItemBank bank, List<Response> answers, List<Item> ignore,
                        double priorTheta) {
    cat = new CatAlgorithmStanford();
    cat.initialize(bank, priorTheta);
    Item item = cat.nextItem(answers, ignore);
    if (item == null) {
      return null;
    }
    Question q = new Question(item.code());
    if (allowSkipping) {
      q.form(item.context(), item.prompt(),
          q.required(q.field("response", FieldType.radios,
              q.value(item.responses()[0].text()),
              q.value(item.responses()[1].text()),
              q.value(item.responses()[2].text()),
              q.value(item.responses()[3].text()),
              q.value(item.responses()[4].text()),
              q.value("skip", "Don't know or not applicable")
          ))
      );
    } else {
      q.form(item.context(), item.prompt(),
          q.required(q.field("response", FieldType.radios,
              q.value(item.responses()[0].text()),
              q.value(item.responses()[1].text()),
              q.value(item.responses()[2].text()),
              q.value(item.responses()[3].text()),
              q.value(item.responses()[4].text())
          ))
      );
    }
    q.getDisplayStatus().setSurveyProviderId(providerId);
    q.getDisplayStatus().setSurveySectionId(sectionId);
    return q;
  }
}
