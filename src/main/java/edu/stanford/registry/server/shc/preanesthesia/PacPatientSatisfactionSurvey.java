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

package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.PageConfiguration;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SessionStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveyPageComponent;
import edu.stanford.survey.server.Answer;
import edu.stanford.survey.server.Question;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveySystemBase;
import edu.stanford.survey.server.TokenInvalidException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Implementation of the Pre-anesthesia clinics patient satisfaction survey.
 */
public class PacPatientSatisfactionSurvey extends SurveySystemBase {
  private static final Logger log = Logger.getLogger(PacPatientSatisfactionSurvey.class);
  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private SurveyItem startFrom;
  private Supplier<Database> database;
  private AppConfig appConfig;
  private Long siteId;
  private SiteInfo siteInfo;

  public PacPatientSatisfactionSurvey(Supplier<Database> database, SiteInfo siteInfo, AppConfig appConfig) {
    this.database = database;
    this.appConfig = appConfig;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  @Override
  public String validateStartToken(String token) throws TokenInvalidException {
    if (token.startsWith("response")) {
      startFrom = Questions.values()[0];

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
        + "when sysdate < token_valid_from or sysdate > token_valid_thru then 'This survey is no longer available. "
        + "Make sure you are using the link from the most recent email.' else 'Valid' end as reason "
        + "from patsat_token where survey_token=?").argString(token).queryStringOrNull();
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
  @Override
  public Question getThankYouPage(String surveyToken) {
    if (surveyToken != null && surveyToken.matches("[0-9]*")) {
      return getThankYouPage(surveyToken, appConfig);
    }
    return null;
  }

  private interface SurveyItem {
    Question question();

    String validate(Answer a);

    SurveyItem next();
  }

  public Question getThankYouPage(String token, AppConfig appConfig) {
    Question question = new Question(":thanks", QuestionType.form);
    question.getDisplayStatus().setSessionStatus(SessionStatus.clearSession);

    AssessDao assessDao = new AssessDao(database.get(), siteInfo);
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    String surveyType = assessDao.getSurveyType(token);
    String action = xmlUtils.getAttribute(surveyType, "onComplete");
    if (action != null && "close".equals(action.toLowerCase())) {
      Question q = new Question(":close", QuestionType.close);
      AutoBean<FormQuestion> form = q.formQuestion("Processing ...");
      form.as().setTerminal(true);
      q.setQuestion(form);
      return q;
    }
    /*
     *  Look in the database for the thank you page configuration entry.
     */
    String thankYouPageOptionsJson = appConfig.forName(siteInfo.getSiteId(), "survey", "survey.thankyoupage");
    if (thankYouPageOptionsJson == null) {
      return simpleThankYou(question);
    }

    if (surveyType == null) {
      return simpleThankYou(question);
    }

    PatientDao patientDao = new PatientDao(database.get(), siteId, ServerUtils.getAdminUser(database.get()));
    Patient patient = patientDao.getPatientByToken(token);
    if (patient == null) {
      return simpleThankYou(question);
    }

    /*
     * See if the process.xml has any media listed for the thank you page.
     */
    ArrayList<String> mediaNames = xmlUtils.getMediaNames(surveyType,"survey.thankyoupage" );
    if (mediaNames == null || mediaNames.size() < 1) {
      log.debug("No media for survey type: " + surveyType);
      return simpleThankYou(question);
    }

    /*
     * Find the component with the name configured in process.xml.
     * Currently the only component type we know how to deal with is a FieldType.videoLink
     * and its attributes are those needed for a kaltura player.
     */
    PageConfiguration pageConfiguration = AutoBeanCodex.decode(factory, PageConfiguration.class, thankYouPageOptionsJson).as();
    List<SurveyPageComponent> surveyPageComponents = pageConfiguration.getSurveyPageComponents();
    int fieldNumber=1;
    ArrayList<FormField> videoFields = new ArrayList<>();
    for (String mediaName : mediaNames) {
      for (SurveyPageComponent surveyPageComponent : surveyPageComponents) {
        Map<String, String> attributes;
        if (mediaName != null && surveyPageComponent != null
            && mediaName.equals(surveyPageComponent.getComponentName())
            && FieldType.videoLink.toString().equals(surveyPageComponent.getComponentType())) {
          attributes = surveyPageComponent.getAttributes();
          // See if there are qualifying attributes
          Map <String, String> qualifyingAttributes = xmlUtils.getMediaAttributes(surveyType, "survey.thankyoupage",
              mediaName);
          // Patient qualifies for seeing the video if they have all of the qualifying attribute/value listed in processType
          if (qualifyingAttributes != null) {
            for (String dataName : qualifyingAttributes.keySet()) {
              String dataValue = qualifyingAttributes.get(dataName);
              if (dataValue != null) {
                PatientAttribute patientAttribute = patientDao.getAttribute(patient.getPatientId(), dataName);
                if (patientAttribute == null || patientAttribute.getDataValue() == null ||
                    !dataValue.equals(patientAttribute.getDataValue())) {
                  log.debug("patient doesn't qualify for media");
                  attributes = null;
                }
              }
            }
          }
          if (attributes != null) {
            // Add the heading if there is one
            if (surveyPageComponent.getComponentHeading() != null) {
              FormField headingField = factory.field().as();
              headingField.setType(FieldType.heading);
              headingField.setFieldId("1:" + fieldNumber + ":thankYou");
              fieldNumber++;
              headingField.setLabel(surveyPageComponent.getComponentHeading());
              videoFields.add(headingField);
            }
            // Add the component field
            FormField videoField = factory.field().as();
            videoField.setType(FieldType.videoLink);
            videoField.setFieldId("1:" + fieldNumber + ":thankYou");
            fieldNumber++;
            videoField.setAttributes(attributes);
            videoFields.add(videoField);
          }
        }
      }
    }

    if (videoFields.size() < 1) { // no media components
      return simpleThankYou(question);
    }

    // Make the question
    AutoBean<FormQuestion> form = factory.formQuestion();
    ArrayList<FormField> fields = new ArrayList<>();
    FormField headingField1 = factory.field().as();
    headingField1.setType(FieldType.heading);
    headingField1.setFieldId("1:0:thankYou");
    headingField1.setLabel("Thank you for your response.");
    fields.add(headingField1);

    for (FormField videoField : videoFields) {
      fields.add(videoField);
    }

    FormQuestion formQuestion = form.as();
    formQuestion.setFields(fields);
    question.setQuestion(form);
    return question;
  }

  private Question simpleThankYou(Question q) {
    AutoBean<FormQuestion> form = q.formQuestion("Thank you for your response.");
    form.as().setTerminal(true);
    q.setQuestion(form);
    return q;
  }

  public enum Questions implements SurveyItem {
    recentAppt {
      void question(Question q) {
        q.form("Have you visited the Stanford Pain Management Center within the last 14 days?",
            q.required(q.field("choice", FieldType.radios,
                q.value("Yes"),
                q.value("No")
                ))
            );
      }
    }, lastVisit {
      void question(Question q) {
        q.form("What was the date of your visit?",
            q.field("visitDate", FieldType.text, "mm/dd/yyyy")
            );
      }

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
      void question(Question q) {
        q.form("Please provide any additional feedback below",
            q.field("feedback", FieldType.textArea)
            );
      }
    };

    public final Question question() {
      Question q = new Question(this.name()).withProvider("patientSatisfaction");
      question(q);
      return q;
    }

    abstract void question(Question q);

    public String validate(Answer a) {
      return null;
    }

    public SurveyItem next() {
      int nextIndex = ordinal() + 1;
      if (nextIndex < Questions.values().length) {
        return Questions.values()[nextIndex];
      }
      return null;
    }
  }
}
