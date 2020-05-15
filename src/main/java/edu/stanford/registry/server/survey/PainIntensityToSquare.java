package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.utils.SquareDocumentationBuilder;
import edu.stanford.registry.shared.Study;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 6/3/2016.
 */
public class PainIntensityToSquare implements SurveyToSquareIntf {

  private String[] columns = { "pain_intensity_worst","pain_intensity_average",
      "pain_intensity_now", "pain_intensity_least"};
  private String[] questionText = {"In the past 7 days...<BR>How intense was your pain at its <u>worst</u>?",
  "In the past 7 days...<BR>How intense was your <u>average</u> pain?",
  "What is your level of pain <u>right now</u>?",
  "In the past 7 days...<BR>How intense was your pain at its <u>least</u>?"};

  @Override
  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix, Sql sql, String separator) {
    int nbrArgs=0;
    String surveyProvider = study.getSurveySystemId().toString();
    String sectionId = study.getStudyCode().toString();

    Survey s = query.surveyBySurveyTokenId(Long.valueOf(tokenId.longValue()));
    SurveyStep
        step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "1");
    if (step != null) {
      sql.listSeparator(separator).append(prefix + "pain_intensity_worst").argInteger(step.answerNumeric());
      nbrArgs++;
    }
    step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "2");
    if (step != null) {
      sql.listSeparator(separator).append(prefix +"pain_intensity_average").argInteger(step.answerNumeric());
      nbrArgs++;
    }
    step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId,  "3");
    if (step != null) {
      sql.listSeparator(separator).append(prefix +"pain_intensity_now").argInteger(step.answerNumeric());
      nbrArgs++;
    }
    step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "4");
    if (step != null) {
      sql.listSeparator(separator).append(prefix +"pain_intensity_least").argInteger(step.answerNumeric());
      nbrArgs++;
    }
    return nbrArgs;
  }

  @Override
  public ArrayList<String> getSurveyDocumentation(Database database, Study study, String prefix) {
    ArrayList<String> documentationLog = new ArrayList<String>();
    for (int q=0; q<questionText.length; q++) {
      ArrayList<String> question = new ArrayList<>();
      question.add(questionText[q]);
      documentationLog.addAll(SquareDocumentationBuilder.question(question,
            (prefix + columns[q]).toUpperCase(), "Radio", ""));

        for (int value=0; value<11; value++) {
          String valueStr = Integer.toString(value);
          documentationLog.add(SquareDocumentationBuilder.option(valueStr, valueStr));
        }
    }
    return documentationLog;
  }


  @Override
  public LinkedHashMap<String, FieldType> getSquareTableColumns(Database database, Study study, String prefix) {
    LinkedHashMap<String, FieldType> returnColumns = new LinkedHashMap<>();
    for (String columnName : columns) {
      returnColumns.put(prefix + columnName, FieldType.radios);
    }
    return returnColumns;
  }
}
