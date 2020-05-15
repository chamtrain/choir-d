/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.survey.client.api.BodyMapAnswer;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.NumericAnswer;
import edu.stanford.survey.client.api.QuestionType;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Interface for accessing data collected during a survey.
 */
public class SurveyQuery {
  private final Supplier<Database> database;
  private final SurveyDao dao;
  private final Long surveySiteId;

  public SurveyQuery(Supplier<Database> database, SurveyDao dao, Long surveySiteId) {
    this.database = database;
    this.dao = dao;
    this.surveySiteId = surveySiteId;
  }

  public Survey surveyBySurveyToken(final String token) {
    if (token == null) {
      return null;
    }
    return new LazySurvey(token);
  }

  public Survey surveyBySurveyTokenId(final Long surveyTokenId) {
    if (surveyTokenId == null) {
      return null;
    }
    return new LazySurvey(surveyTokenId);
  }

  /**
   * Be careful with this version because there are no checks that the parameters you pass
   * are consistent, and no guarantees what might happen if they are not.
   */
  public Survey surveyBySurveyTokenId(final Long surveyTokenId, final String surveyToken, final boolean isComplete) {
    if (surveyTokenId == null || surveyToken == null) {
      return null;
    }
    return new LazySurvey(surveyTokenId, surveyToken, isComplete);
  }

  private SurveyStep step(final SurveyProgressWithJson progress) {
    if (progress == null) {
      return null;
    }

    return new SurveyStep() {
      private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
      private FormQuestion formQuestion;

      @Override
      public Long stepNumber() {
        return progress.getStepNumber();
      }

      @Override
      public Answer answer() {
        String answerJson = progress.getAnswerJson();
        if (answerJson == null) {
          return null;
        }
        SubmitStatus submitStatus = new SubmitStatus() {
          @Override
          public long getCompatLevel() {
            return progress.getAnswerApiCompatLevel();
          }

          @Override
          public void setCompatLevel(long level) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public long getStepNumber() {
            return progress.getStepNumber();
          }

          @Override
          public void setStepNumber(long stepNumber) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public QuestionType getQuestionType() {
            return QuestionType.valueOf(progress.getQuestionType());
          }

          @Override
          public void setQuestionType(QuestionType type) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public String getQuestionId() {
            return progress.getQuestionId();
          }

          @Override
          public void setQuestionId(String questionid) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public String getSurveyProviderId() {
            return progress.getProviderId();
          }

          @Override
          public void setSurveyProviderId(String surveyProviderId) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public String getSurveySectionId() {
            return progress.getSectionId();
          }

          @Override
          public void setSurveySectionId(String surveySectionId) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public String getSessionToken() {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public void setSessionToken(String sessionToken) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public String getSurveySystemName() {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public void setSurveySystemName(String systemName) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public Long getCallTimeMillis() {
            return progress.getCallTimeMillis();
          }

          @Override
          public void setCallTimeMillis(Long callTimeMillis) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public Long getRenderTimeMillis() {
            return progress.getRenderTimeMillis();
          }

          @Override
          public void setRenderTimeMillis(Long renderTimeMillis) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public Long getThinkTimeMillis() {
            return progress.getThinkTimeMillis();
          }

          @Override
          public void setThinkTimeMillis(Long thinkTimeMillis) {
            throw new RuntimeException("Not implemented");
          }

          @Override
          public Long getRetryCount() {
            return progress.getRetryCount();
          }

          @Override
          public void setRetryCount(Long retries) {
            throw new RuntimeException("Not implemented");
          }
        };
        return new Answer(null, submitStatus, answerJson);
      }

      @Override
      public String answerJson() {
        return progress.getAnswerJson();
      }

      @Override
      public String answerRegionsCsv() {
        String json = answerJson();
        if (json == null) {
          return null;
        }
        BodyMapAnswer answer = AutoBeanCodex.decode(factory, BodyMapAnswer.class, json).as();
        return answer == null ? null : answer.getRegionsCsv();
      }

      @Override
      public Integer answerNumeric() {
        String json = answerJson();
        if (json == null) {
          return null;
        }
        NumericAnswer answer = AutoBeanCodex.decode(factory, NumericAnswer.class, json).as();
        return answer == null ? null : answer.getChoice();
      }

      @Override
      public String questionJson() {
        return progress.getQuestionJson();
      }

      @Override
      public FormQuestion questionForm() {
        if (formQuestion == null) {
          String json = questionJson();
          if (json == null) {
            return null;
          }
          formQuestion = AutoBeanCodex.decode(factory, FormQuestion.class, json).as();
        }
        return formQuestion;
      }

      @Override
      public FormField questionFormField(String fieldId) {
        FormQuestion question = questionForm();
        if (question != null) {
          return recursiveFindFormField(question.getFields(), fieldId);
        }
        return null;
      }

      private FormField recursiveFindFormField(List<FormField> fields, String fieldId) {
        if (fields == null || fields.isEmpty()) {
          return null;
        }
        for (FormField field : fields) {
          if (fieldId.equals(field.getFieldId())) {
            return field;
          }
          List<FormFieldValue> values = field.getValues();
          if (values != null) {
            for (FormFieldValue value : values) {
              FormField found = recursiveFindFormField(value.getFields(), fieldId);
              if (found != null) {
                return found;
              }
            }
          }
        }
        return null;
      }

      @Override
      public FormFieldValue questionFormFieldValue(String fieldId, String valueId) {
        FormField field = questionFormField(fieldId);
        if (field != null && field.getValues() != null) {
          for (FormFieldValue value : field.getValues()) {
            if (valueId.equals(value.getId())) {
              return value;
            }
          }
        }
        return null;
      }

      @Override
      public Date getQuestionTime() {
        return progress.getQuestionTime();
      }

      @Override
      public Date getAnswerTime() {
        return progress.getAnswerTime();
      }
    };
  }

  private class EagerSurvey implements Survey {
    private final Long surveyTokenId;
    private final String surveyToken;
    private final boolean isComplete;
    private List<SurveyProgressWithJson> stepsReverseOrder;

    public EagerSurvey(Long surveyTokenId, String surveyToken, boolean isComplete) {
      this.surveyTokenId = surveyTokenId;
      this.surveyToken = surveyToken;
      this.isComplete = isComplete;
      stepsReverseOrder = dao.findProgressEagerReverseOrder(surveySiteId, surveyTokenId);
    }

    @Override
    public boolean isComplete() {
      return isComplete;
    }

    @Override
    public Long getSurveyTokenId() {
      return surveyTokenId;
    }

    @Override
    public String getSurveyToken() {
      return surveyToken;
    }

    @Override
    public SurveyStep firstStep() {
      SurveyStep step = null;
      if (!stepsReverseOrder.isEmpty()) {
        step = step(stepsReverseOrder.get(stepsReverseOrder.size() - 1));
      }
      return step;
    }

    @Override
    public Date startTime() {
      SurveyStep firstStep = firstStep();
      if (firstStep == null) {
        return null;
      }
      return firstStep.getQuestionTime();
    }

    @Override
    public SurveyStep lastStep() {
      SurveyStep step = null;
      if (!stepsReverseOrder.isEmpty()) {
        step = step(stepsReverseOrder.get(0));
      }
      return step;
    }

    @Override
    public Date endTime() {
      SurveyStep lastStep = lastStep();
      if (lastStep == null) {
        return null;
      }
      return lastStep.getQuestionTime();
    }

    @Override
    public long serverTimeMillis() {
      return serverTimeMillis(300000);
    }

    @Override
    public long serverTimeMillis(int maxMillisPerStep) {
      long time = 0;
      for (SurveyProgressWithJson progress : stepsReverseOrder) {
        if (progress.getQuestionTime() != null && progress.getAnswerTime() != null) {
          time += Math.min(maxMillisPerStep, progress.getAnswerTime().getTime() - progress.getQuestionTime().getTime());
        }
      }
      return time;
    }

    @Override
    public long clientTimeMillis() {
      return clientTimeMillis(300000);
    }

    @Override
    public long clientTimeMillis(int maxMillisThinkTime) {
      long time = 0;
      for (SurveyProgressWithJson progress : stepsReverseOrder) {
        if (progress.getCallTimeMillis() != null) {
          time += progress.getCallTimeMillis();
        }
        if (progress.getRenderTimeMillis() != null) {
          time += progress.getRenderTimeMillis();
        }
        if (progress.getThinkTimeMillis() != null) {
          time += Math.min(maxMillisThinkTime, progress.getThinkTimeMillis());
        }
      }
      return time;
    }

    @Override
    public SurveyStep answeredStepByQuestion(String questionId) {
      if (questionId == null || questionId.length() == 0 || surveyTokenId == null) {
        return null;
      }

      for (SurveyProgressWithJson progress : stepsReverseOrder) {
        if (questionId.equals(progress.getQuestionId())) {
          return step(progress);
        }
      }
      return null;
    }

    @Override
    public SurveyStep answeredStepByProviderSectionQuestion(String providerId, String sectionId, String questionId) {
      if (questionId == null || questionId.length() == 0 || surveyTokenId == null) {
        return null;
      }

      for (SurveyProgressWithJson progress : stepsReverseOrder) {
        if (providerId.equals(progress.getProviderId())
            && sectionId.equals(progress.getSectionId())
            && questionId.equals(progress.getQuestionId())) {
          return step(progress);
        }
      }
      return null;
    }

    @Override
    public List<SurveyStep> answeredStepsByProvider(String providerId) {
      List<SurveyStep> steps = new ArrayList<>();
      if (providerId != null && providerId.length() != 0 && surveyTokenId != null) {
        Set<String> psq = new HashSet<>();
        for (SurveyProgressWithJson progress : stepsReverseOrder) {
          if (providerId.equals(progress.getProviderId())) {
            String psqKey = progress.getProviderId() + "\t" + progress.getSectionId() + "\t" + progress.getQuestionId();
            if (psq.contains(psqKey)) {
              continue;
            } else {
              psq.add(psqKey);
            }
            steps.add(step(progress));
          }
        }
        // The non-cached way returns them in sequence, while our cache is descending
        Collections.reverse(steps);
      }
      return steps;
    }

    @Override
    public List<SurveyStep> answeredStepsByProviderSection(String providerId, String sectionId) {
      List<SurveyStep> steps = new ArrayList<>();
      if (providerId != null && providerId.length() != 0 && surveyTokenId != null) {
        Set<String> psq = new HashSet<>();
        for (SurveyProgressWithJson progress : stepsReverseOrder) {
          if (providerId.equals(progress.getProviderId()) && sectionId.equals(progress.getSectionId())) {
            String psqKey = progress.getProviderId() + "\t" + progress.getSectionId() + "\t" + progress.getQuestionId();
            if (psq.contains(psqKey)) {
              continue;
            } else {
              psq.add(psqKey);
            }
            steps.add(step(progress));
          }
        }
        // The non-cached way returns them in sequence, while our cache is descending
        Collections.reverse(steps);
      }
      return steps;
    }
  }

  private class LazySurvey implements Survey {
    private Long surveyTokenId;
    private String surveyToken;
    private boolean isComplete;
    private EagerSurvey eagerSurvey;
    private boolean eagerLoadFailed;

    public LazySurvey(Long surveyTokenId) {
      if (surveyTokenId == null) {
        throw new IllegalArgumentException("surveyTokenId cannot be null");
      }
      this.surveyTokenId = surveyTokenId;
    }

    public LazySurvey(String surveyToken) {
      if (surveyToken == null) {
        throw new IllegalArgumentException("surveyToken cannot be null");
      }
      this.surveyToken = surveyToken;
    }

    public LazySurvey(Long surveyTokenId, String surveyToken, boolean isComplete) {
      if (surveyTokenId == null) {
        throw new IllegalArgumentException("surveyTokenId cannot be null");
      }
      if (surveyToken == null) {
        throw new IllegalArgumentException("surveyToken cannot be null");
      }
      this.surveyTokenId = surveyTokenId;
      this.surveyToken = surveyToken;
      this.isComplete = isComplete;
    }

    private EagerSurvey eager() {
      if (eagerSurvey == null && !eagerLoadFailed) {
        if (surveyTokenId != null && surveyToken != null) {
          eagerSurvey = new EagerSurvey(surveyTokenId, surveyToken, isComplete);
        } else if (surveyTokenId != null) {
          eagerSurvey = database.get().toSelect("select survey_token, is_complete from survey_token where "
                                                + "survey_site_id=? and survey_token_id=?")
              .argLong(surveySiteId)
              .argLong(surveyTokenId)
              .query(new RowsHandler<EagerSurvey>() {
                @Override
                public EagerSurvey process(Rows rs) throws Exception {
                  if (rs.next()) {
                    String surveyToken = rs.getStringOrNull();
                    boolean isComplete = rs.getBooleanOrFalse();
                    return new EagerSurvey(surveyTokenId, surveyToken, isComplete);
                  }
                  return null;
                }
              });
        } else if (surveyToken != null) {
          eagerSurvey = database.get().toSelect("select survey_token_id, is_complete from survey_token where "
                                                + "survey_site_id=? and survey_token=?")
              .argLong(surveySiteId)
              .argString(surveyToken)
              .query(new RowsHandler<EagerSurvey>() {
                @Override
                public EagerSurvey process(Rows rs) throws Exception {
                  if (rs.next()) {
                    Long surveyTokenId = rs.getLongOrNull();
                    boolean isComplete = rs.getBooleanOrFalse();
                    return new EagerSurvey(surveyTokenId, surveyToken, isComplete);
                  }
                  return null;
                }
              });
        }
        if (eagerSurvey == null) {
          eagerLoadFailed = true;
        }
      }
      return eagerSurvey;
    }

    @Override
    public boolean isComplete() {
      if (surveyTokenId != null && surveyToken != null) {
        return isComplete;
      }
      return eager() != null && eager().isComplete();
    }

    @Override
    public Long getSurveyTokenId() {
      if (surveyTokenId != null) {
        return surveyTokenId;
      }
      return eager() == null ? null : eager().getSurveyTokenId();
    }

    @Override
    public String getSurveyToken() {
      if (surveyToken != null) {
        return surveyToken;
      }
      return eager() == null ? null : eager().getSurveyToken();
    }

    @Override
    public SurveyStep firstStep() {
      return eager() == null ? null : eager().firstStep();
    }

    @Override
    public Date startTime() {
      return eager() == null ? null : eager().startTime();
    }

    @Override
    public SurveyStep lastStep() {
      return eager() == null ? null : eager().lastStep();
    }

    @Override
    public Date endTime() {
      return eager() == null ? null : eager().endTime();
    }

    @Override
    public long serverTimeMillis() {
      return eager() == null ? 0L : eager().serverTimeMillis();
    }

    @Override
    public long serverTimeMillis(int maxMillisPerStep) {
      return eager() == null ? 0L : eager().serverTimeMillis(maxMillisPerStep);
    }

    @Override
    public long clientTimeMillis() {
      return eager() == null ? 0L : eager().clientTimeMillis();
    }

    @Override
    public long clientTimeMillis(int maxMillisThinkTime) {
      return eager() == null ? 0L : eager().clientTimeMillis(maxMillisThinkTime);
    }

    @Override
    public SurveyStep answeredStepByQuestion(String questionId) {
      return eager() == null ? null : eager().answeredStepByQuestion(questionId);
    }

    @Override
    public SurveyStep answeredStepByProviderSectionQuestion(String providerId, String sectionId, String questionId) {
      return eager() == null ? null : eager().answeredStepByProviderSectionQuestion(providerId, sectionId, questionId);
    }

    @Override
    public List<SurveyStep> answeredStepsByProvider(String providerId) {
      return eager() == null ? null : eager().answeredStepsByProvider(providerId);
    }

    @Override
    public List<SurveyStep> answeredStepsByProviderSection(String providerId, String sectionId) {
      return eager() == null ? null : eager().answeredStepsByProviderSection(providerId, sectionId);
    }
  }
}
