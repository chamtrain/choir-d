package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 2/24/2016.
 *
 * This is used by SurveyAdvanceHandlers which are created for surveys on a single site
 */
public abstract class SurveyAdvanceBase  {
  private HashMap<String, String> providers = new HashMap<>();
  private HashMap<String, String> studies = new HashMap<>();
  protected final SiteInfo siteInfo;

  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(SurveyAdvanceBase.class);

  public SurveyAdvanceBase(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  public Long getSurveyRegistrationId(SurveyAdvance survey, Supplier<Database> database) {
    return getSurveyRegistrationId(survey.getSurveySiteId(), survey.getSurveyTokenId(), database);
  }

  public Long getSurveyRegistrationId(Long surveySiteId, Long surveyTokenId, Supplier<Database> database) {
    return database.get().toSelect("select survey_reg_id from survey_registration sr, survey_token st"
        + " where sr.survey_site_id=st.survey_site_id and sr.token=st.survey_token"
        + " and st.survey_site_id=? and st.survey_token_id=?")
        .argLong(surveySiteId)
        .argLong(surveyTokenId)
        .queryLongOrNull();
  }


  public String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
  }
  public int processStudy(Supplier<Database> database, String localSurveyProvider, String studyName, Survey s, Sql sql, String separator) throws InvalidDataElementException, NumberFormatException {
    return processStudy(database, localSurveyProvider, studyName, s, sql, separator, studyName.substring(0,3));
  }

  public int processStudy(Supplier<Database> database, String providerId, String studyName, Survey s, Sql sql, String separator, String prefix)
    throws InvalidDataElementException, NumberFormatException {



    return 0;
  }

  public String getProviderId(Database db,  String providerName) {
    if (providers.get(providerName) == null) {
      String providerId = getInternalId(db,
          "SELECT survey_system_id FROM survey_system WHERE survey_system_name = ?", providerName);
      if (providerId != null) {
        providers.put(providerName, providerId);
      }
    }
    return providers.get(providerName);
  }

  public String getSectionId(Database db,  String sectionName) {
    if (studies.get(sectionName) == null) {
      String providerId = getInternalId(db,
          "SELECT study_code FROM study WHERE study_description = ?",sectionName);
      if (providerId != null) {
        studies.put(sectionName, providerId);
      }
    }
    return studies.get(sectionName);
  }

  public String getInternalId(Database db, String sqlString, String name) {
    return db.toSelect(sqlString).argString(name).query(
        new RowsHandler<String>() {
          @Override
          public String process(Rows rs) throws Exception {
            if (rs.next()) {
              return Integer.toString(rs.getIntegerOrNull());
            }
            return null;
          }
        });
  }


  public SurveyRegistration getSurveyRegistration(Database db, SurveyAdvance surveyAdvance) {
    Long surveyRegId = getSurveyRegistrationId(surveyAdvance.getSurveySiteId(), surveyAdvance.getSurveyTokenId(), db);
    if (surveyRegId == null) {
      throw new RuntimeException("surveyRegId not found for siteId " + surveyAdvance.getSurveySiteId() + " token_id "
          + surveyAdvance.getSurveyTokenId());
    }
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("no registration for siteId " + surveyRegId + " token_id " + surveyRegId);
    }
    return registration;
  }

  public Survey getSurvey(Database db, SurveyAdvance surveyAdvance) {
    SurveyDao surveyDao = new SurveyDao(db);
    SurveyQuery query = new SurveyQuery(db, surveyDao, surveyAdvance.getSurveySiteId());
    return query.surveyBySurveyTokenId(surveyAdvance.getSurveyTokenId());

  }

  public Integer selectedFieldChoice(SurveyStep step, String fieldId) throws NumberFormatException {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null && field.getFieldId().equals(fieldId)) {
            List<String> choices = step.answer().formFieldValues(fieldId);
            if (choices != null && choices.size() > 0){
              return Integer.parseInt(choices.get(0));
            }
          }
        }
      }
    }
    return null;
  }

  public boolean selectedFieldChoice(SurveyStep step, String fieldId, String choice) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null && field.getFieldId().equals(fieldId)) {
            List<String> choices = step.answer().formFieldValues(fieldId);
            if (choices != null){
              for (String c : choices) {
                if (c != null && choice.equals(c)) {
                  return true;
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  public Integer selectedFieldInt(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            if (field.getFieldId().equals(fieldId)) {
              try {
                return Integer.parseInt(formFieldValue(step, field.getFieldId()));
              } catch (NumberFormatException nfe) {
                throw new NumberFormatException("Unable to save question " + step.questionJson() + " with answer " + step.answer().getAnswerJson() + " as integer");
              }
            }
          }
        }
      }
    }
    return null;
  }

  public String selectedValue(SurveyStep step) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            return formFieldValue(step, field.getFieldId());
          }
        }
      }
    }
    return null;
  }

  public String selectedField(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            if (field.getFieldId().equals(fieldId)) {
              return formFieldValue(step, field.getFieldId());
            }
          }
        }
      }
    }
    return null;
  }

  public Integer getSelect1Response(Survey s, String provider, String section, String questionId, String fieldId) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section , questionId);
    // if the step or step answer for this question id isn't found check if it's been stored within another question
    // This happens when conditional questions are shown on the same page as the prior question
    if (step == null || step.answer() == null || step.answer().form() == null) {
      step = lookInOtherSteps(s, provider, section, fieldId);
    }
    if (step !=null) {
      return selectedFieldInt(step, fieldId);
    } else {
      return null;
    }
  }

  public String getSelect1ResponseStr(Survey s, String provider, String section, String questionId, String fieldId) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section , questionId);
    // if the step or step answer for this question id isn't found check if it's been stored within another question
    // This happens when conditional questions are shown on the same page as the prior question
    if (step == null || step.answer() == null || step.answer().form() == null) {
      step = lookInOtherSteps(s, provider, section, fieldId);
    }
    if (step !=null) {
      return selectedField(step, fieldId);
    } else {
      return null;
    }
  }
  
  public String getInputStringResponse(Survey s, String provider, String section, String questionId) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section, questionId);
    if (step != null) {
      return selectedValue(step);
    } else {
      return null;
    }
  }

  public String getInputStringResponse(Survey s, String provider, String section, String questionId, String fieldId) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section, questionId);
    // if the step or step answer for this question id isn't found check if it's been stored within another question
    // This happens when conditional questions are shown on the same page as the prior question
    if (step == null || step.answer() == null || step.answer().form() == null) {
      step = lookInOtherSteps(s, provider, section, fieldId);
    }
    if (step != null) {
      return formFieldValue(step, fieldId);
    } else {
      return null;
    }
  }

  public String getInputStringResponseChoice(Survey s, String provider, String section, String questionId, String fieldId, int choice) throws NumberFormatException {
    //SurveyStep step = // s.answeredStepByProviderSectionQuestion(provider, section, questionId);
    List<SurveyStep> steps = s.answeredStepsByProviderSection(provider, section);
    for (SurveyStep step : steps) {
      if (step != null && step.answer() != null && step.answer().form() != null) {
        List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
        if (fields != null) {
          for (FormFieldAnswer field : fields) {
            if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null
                && field.getFieldId().equals(fieldId)) {
              List<String> choices = step.answer().formFieldValues(fieldId);
              if (choices != null && choices.size() > 0) {
                return choices.get(choice);
              }
            }
          }
        }
      }
    }
    return null;
  }

  public boolean isCheckboxSelected(Survey s, String provider, String section, String questionId, String fieldId, String choice) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section, questionId);
    // if the step or step answer for this question id isn't found check if it's been stored within another question
    // This happens when conditional questions are shown on the same page as the prior question
    if (step == null || step.answer() == null || step.answer().form() == null) {
      step = lookInOtherSteps(s, provider, section, fieldId);
    }
    return selectedFieldChoice(step, fieldId, choice);
  }

  public Integer getRadioIntegerResponse(Survey s, String provider, String sectionId, String questionId) {
    SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, sectionId, questionId);
    if (step != null) {
      return step.answerNumeric();
    }
    return null;
  }

  Integer getRadiosetgridChoice( SurveyStep step, String fieldId)
    throws NumberFormatException {
    String[] parts = fieldId.split(":");
    if (parts.length < 3) {
      return null;
    }
    String refKey = parts[2];
    fieldId = parts[0] + ":" + parts[1];
    List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
    if (fields != null) {
      for (FormFieldAnswer field : fields) {
        if (field != null && field.getFieldId() != null && field.getFieldId().equals(fieldId)) {
          List<String> choices = step.answer().formFieldValues(fieldId);
          if (choices != null) {
            for (String choice : choices) {
              String[] choiceParts = choice.split(":");
              if (choiceParts.length > 0 && choiceParts[0] != null && choiceParts[0].equals(refKey)) {
                return Integer.parseInt(choiceParts[1]);
              }
            }
          }
        }
      }
    }
    return null;
  }

  public String getQuestionId(String section, String[] fieldIdParts) {
    if (fieldIdParts != null && fieldIdParts.length > 0) {
      if ("bodymap".equals(section)) {
        return fieldIdParts[0];
      }
    }
    if (fieldIdParts.length > 0) {
      return "Order" + fieldIdParts[0];
    }
    return null;
  }

  public String getFieldId(SquareXml squareXml, int inx) {
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object[] refKeys = references.keySet().toArray();
    String refKey = refKeys[inx].toString();
    return references.get(refKey) + ":" + refKey;
  }

  private SurveyStep lookInOtherSteps(Survey s, String provider, String section, String fieldId) {
    // checks if the field id is found in another step within the section
    log.trace("lookInOtherSteps starting for " + s.getSurveyToken() + " with provider " + provider + " section " + section + " fieldId " + fieldId);
    List<SurveyStep> steps = s.answeredStepsByProviderSection(provider, section);
    for (SurveyStep surveyStep : steps) {
      if (stepHasField(surveyStep, fieldId)) {
        return surveyStep;
      }
    }
    return null;
  }
  private boolean stepHasField(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null
              && field.getFieldId().equals(fieldId)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
