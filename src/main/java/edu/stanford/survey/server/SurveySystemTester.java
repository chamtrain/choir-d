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

/**
 * The intent of this survey is to exercise all features of the survey client, and
 * make it easy to test the client consistently on many browsers and devices. The
 * survey guides you through each client feature, tells you what to do to verify
 * that feature, and allows you to record any problems.
 */
public class SurveySystemTester extends SurveySystemBase {
  private Questions startFrom;
  private boolean quick;

  @Override
  public String validateStartToken(String token) throws TokenInvalidException {
    if ("start".equals(token)) {
      // Start at the beginning of the test survey and include problem report pages
      startFrom = Questions.values()[0];
    } else if ("quick".equals(token)) {
      // Start at the beginning but skip the problem report pages
      startFrom = Questions.values()[0];
      quick = true;
    } else {
      // Start from an arbitrary location
      startFrom = Questions.valueOf(token);
    }

    if (startFrom != null) {
      // Auto-generate the real token
      return null;
    }

    throw new TokenInvalidException("Bad token");
  }

  @Override
  public Question startWithValidToken(String token, Survey survey) {
    Question question = startFrom.question();

    // Optionally skip all of the problem report pages
    if (quick) {
      question.withProvider("quick");
    }

    return question;
  }

  @Override
  public Question nextQuestion(Answer a, Survey survey) {
    if (a.questionIdHasPrefix("problem:")) {
      String lastQuestionId = a.questionIdAfterPrefix("problem:");

      // If the user specifically request it, send them back to the previous question
      if (a.formFieldContains("repeat", "Repeat the last question")) {
        return Questions.valueOf(lastQuestionId).question();
      }

      // Otherwise advance to the next question
      int nextQuestionOrdinal = Questions.valueOf(lastQuestionId).ordinal() + 1;
      if (nextQuestionOrdinal < Questions.values().length) {
        return Questions.values()[nextQuestionOrdinal].question();
      }

      // Ran out of questions, all done
      return null;
    }

    // Server-side validation of the provided answer
    Questions current = Questions.valueOf(a.questionId());
    String validationMessage = current.validate(a);
    if (validationMessage != null) {
      return current.question().validate(validationMessage);
    }

    // Unless we are in "quick" mode, in between each "real" question, insert a
    // question allowing the user to indicate any problems, and optionally repeat
    // the previous question
    if (a.providedBy("quick")) {
      int nextQuestionOrdinal = current.ordinal() + 1;
      if (nextQuestionOrdinal < Questions.values().length) {
        return Questions.values()[nextQuestionOrdinal].question().withProvider("quick");
      }
      return null;
    } else {
      return recordProblem(a.questionId());
    }
  }

  private enum Questions {
    formNumericField {
      @Override
      void question(Question q) {
        q.form(this.name(), "Click continue without entering anything",
            q.field("number", FieldType.number, "Numeric field")
        );
      }

      @Override
      String validate(Answer a) {
        if (a.formFieldValue("number") != null) {
          return "You should not have entered a value";
        }
        return null;
      }
    }, formNumericFieldRequired {
      @Override
      void question(Question q) {
        q.form(this.name(), "1. Click continue to see the required field validation message<br>"
            + "2. Verify the virtual keyboard goes into numeric mode<br>3. Enter \"0\" and click continue",
            q.required(q.field("number", FieldType.number))
        );
      }

      @Override
      String validate(Answer a) {
        if (!a.formFieldEquals("number", "0")) {
          return "You should have entered 0";
        }
        return null;
      }
    }, formNumericFieldRange {
      @Override
      void question(Question q) {
        q.form(this.name(), "1. Enter \"-2\" and click continue<br>2. Enter \"2\" and click continue<br>"
            + "3. Enter \"-1\" and click continue",
            q.range(q.field("number", FieldType.number), "-1", "1")
        );
      }

      @Override
      String validate(Answer a) {
        if (!a.formFieldEquals("number", "-1")) {
          return "You should have entered -1";
        }
        return null;
      }
    };

    final Question question() {
      Question q = new Question(this.name());
      question(q);
      return q;
    }

    abstract void question(Question q);

    String validate(Answer a) {
      return null;
    }
  }

  private Question recordProblem(String lastQuestionId) {
    Question q = new Question("problem:" + lastQuestionId);
    q.form("If there was any problem with the last test, record it here:",
        q.field("problem", FieldType.textArea),
        q.field("repeat", FieldType.checkboxes, q.value("Repeat the last question"))
    );
    return q;
  }

//  private Question smoking() {
//    Question q = new Question("smoking");
//    q.form("Do you smoke?",
//        q.field("smoke", FieldType.radios,
//            q.value("No"),
//            q.value("Yes",
//                q.field("smokePacks", FieldType.number, "How many packs per day?")
//            )
//        )
//    );
//    return q;
//  }
}
