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

package edu.stanford.registry.server.survey;

import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.server.Answer;
import edu.stanford.survey.server.Question;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveySystemBase;
import edu.stanford.survey.server.TokenInvalidException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Implementation of the Pain Management Center patient satisfaction survey.
 */
public class PatientSatisfactionSurvey extends SurveySystemBase {
  private static final Logger log = Logger.getLogger(PatientSatisfactionSurvey.class);
  private SurveyItem startFrom;
  private Supplier<Database> database;

  public PatientSatisfactionSurvey(Supplier<Database> database) {
    this.database = database;
  }

  @Override
  public String validateStartToken(String token) throws TokenInvalidException {
    if ("start".equals(token)) {
      startFrom = Questions.values()[0];
      // Auto-generate a real token
      return null;
    } else if (token.startsWith("optout:")) {
      int rows = database.get().toUpdate("update patsat_token set opted_out='Y' where survey_token=?")
          .argString(token.substring("optout:".length())).update();
      if (rows != 1) {
        log.error("Unable to opt-out token (" + rows + " rows updated)");
      }
      startFrom = new SurveyItem() {
        @Override
        public Question question() {
          Question q = new Question("optOut");
          q.formTerminal("Your preference has been recorded.", "Note that opting out from the patient satisfaction "
              + "survey does not affect other communications you may receive from Stanford Medicine.");
          return q;
        }

        @Override
        public String validate(Answer a) {
          return null;
        }

        @Override
        public SurveyItem next() {
          return null;
        }
      };
      return null;
    }
    String reason = database.get().toSelect("select case when opted_out='Y' then 'You opted out from this survey' "
        + "when ? < token_valid_from or ? > token_valid_thru then 'This survey is no longer available. "
        + "Make sure you are using the link from the most recent email.' else 'Valid' end as reason "
        + "from patsat_token where survey_token=?")
        .argDateNowPerDb()
        .argDateNowPerDb()
        .argString(token).queryStringOrNull();
    if (reason != null && reason.equals("Valid")) {
      startFrom = Questions.values()[0];
      return token;
    }

    if (reason == null) {
      reason = "Invalid email link. Please go back to the original email and try again.";
    }

    throw new TokenInvalidException(reason);
  }

  @Override
  public Question startWithValidToken(String token, Survey survey) {
    return startFrom.question();
  }

  @Override
  public Question nextQuestion(Answer a, Survey survey) {
    if (a == null) {
      return Questions.values()[0].question();
    }

    SurveyItem current;
    if (a.providedBy("patientSatisfaction")) {
      current = Questions.valueOf(a.questionId());
    } else {
      throw new RuntimeException("Answer does not belong to this survey");
    }

    // Server-side validation of the provided answer
    String validationMessage = current.validate(a);
    if (validationMessage != null) {
      return current.question().validate(validationMessage);
    }

    // Early terminate survey if they didn't complete their appointment (e.g. cancel, reschedule)
    if (Questions.recentAppt.name().equals(a.questionId()) && a.formFieldContains("choice", "No")) {
      return null;
    }

    SurveyItem next = current.next();
    if (next != null) {
      return next.question();
    }
    return null;
  }

  private interface SurveyItem {
    Question question();

    String validate(Answer a);

    SurveyItem next();
  }

  public enum Questions implements SurveyItem {
    recentAppt {
      @Override
      void question(Question q) {
        q.form("Have you visited the Stanford Pain Management Center within the last 14 days?",
            q.required(q.field("choice", FieldType.radios,
                q.value("Yes"),
                q.value("No")
            ))
        );
      }
    }, lastVisit {
      @Override
      void question(Question q) {
        q.form("What was the date of your visit?",
            q.field("visitDate", FieldType.text, "mm/dd/yyyy")
        );
      }

      @Override
      public String validate(Answer a) {
        String visitDate = a.formFieldValue("visitDate");
        if (visitDate == null || visitDate.length() == 0) {
          return null;
        }
        if (!visitDate.matches("[0-9][0-9]?/[0-9][0-9]?/[0-9][0-9][0-9][0-9]")) {
          return "Please enter a date in mm/dd/yyyy format.";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
          sdf.setLenient(false);
          Date d = sdf.parse(visitDate);
          if (d.after(new Date())) {
            return "Please enter a date in the past.";
          }
          Calendar c = Calendar.getInstance();
          c.add(Calendar.DAY_OF_MONTH, -90);
          if (d.before(c.getTime())) {
            return "Please enter a date in the recent past.";
          }
        } catch (ParseException e) {
          log.trace("Couldn't parse date from user input: " + visitDate, e);
          return "Please enter a date in mm/dd/yyyy format.";
        }
        return null;
      }
    }, schedulerRespect {
      @Override
      void question(Question q) {
        q.form("Concerns and respect shown by appointment scheduler",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, checkinRespect {
      @Override
      void question(Question q) {
        q.form("Concerns and respect shown by staff at check-in",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, phoneContactEase {
      @Override
      void question(Question q) {
        q.form("Ease of contacting Clinic by phone",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good"),
                q.value("Not applicable")
            )
        );
      }
    }, convenientApptTimes {
      @Override
      void question(Question q) {
        q.form("Finding convenient clinic appointment times",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good")
            )
        );
      }
    }, updatedDelays {
      @Override
      void question(Question q) {
        q.form("Updated you about delays",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good"),
                q.value("Not applicable")
            )
        );
      }
    }, waitingTime {
      @Override
      void question(Question q) {
        q.form("Waiting time at the Clinic",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, nurseRespect {
      @Override
      void question(Question q) {
        q.form("Concerns and respect shown by nurse/assistant",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, providerEnoughTime {
      @Override
      void question(Question q) {
        q.form("Care provider spent enough time with you",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, providerRespect {
      @Override
      void question(Question q) {
        q.form("Concerns and respect shown by care provider",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, providerMaterial {
      @Override
      void question(Question q) {
        q.form("Instructions and educational material the care provider gave you",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good")
            )
        );
      }
    }, inputConsidered {
      @Override
      void question(Question q) {
        q.form("Your input was considered in treatment decisions",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much"),
                q.value("Not applicable")
            )
        );
      }
    }, careCoordination {
      @Override
      void question(Question q) {
        q.form("Clinic's ability to coordinate care with your other physicians",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good")
            )
        );
      }
    }, easyCare {
      @Override
      void question(Question q) {
        q.form("Easy to obtain new studies, lab work, psychology, or physical therapy",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good"),
                q.value("Not applicable")
            )
        );
      }
    }, easyMyHealthContact {
      @Override
      void question(Question q) {
        q.form("Ease of contacting Clinic by MyHealth",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good"),
                q.value("Not applicable")
            )
        );
      }
    }, staffWorkTogether {
      @Override
      void question(Question q) {
        q.form("Clinic staff worked together to care for you",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, betterManagePain {
      @Override
      void question(Question q) {
        q.form("You can now better manage your pain",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, moreActivities {
      @Override
      void question(Question q) {
        q.form("You can now perform more activities",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, planSatisfaction {
      @Override
      void question(Question q) {
        q.form("Your satisfaction with the current plan",
            q.field("choice", FieldType.radios,
                q.value("Very poor"),
                q.value("Poor"),
                q.value("Fair"),
                q.value("Good"),
                q.value("Very good")
            )
        );
      }
    }, metNeeds {
      @Override
      void question(Question q) {
        q.form("Our program met your needs",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, recommend {
      @Override
      void question(Question q) {
        q.form("Likelihood of your recommending the Clinic to others",
            q.field("choice", FieldType.radios,
                q.value("Very little"),
                q.value("Little"),
                q.value("A bit"),
                q.value("Much"),
                q.value("Very much")
            )
        );
      }
    }, otherFeedback {
      @Override
      void question(Question q) {
        q.form("Please provide any additional feedback below",
            q.field("feedback", FieldType.textArea)
        );
      }
    };

    @Override
    public final Question question() {
      Question q = new Question(this.name()).withProvider("patientSatisfaction");
      question(q);
      return q;
    }

    abstract void question(Question q);

    @Override
    public String validate(Answer a) {
      return null;
    }

    @Override
    public SurveyItem next() {
      int nextIndex = ordinal() + 1;
      if (nextIndex < Questions.values().length) {
        return Questions.values()[nextIndex];
      }
      return null;
    }
  }
}
