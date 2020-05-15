package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 5/17/2016.
 */
public class SurveyAdvanceGenerated extends SurveyAdvanceBase implements SurveyToSquareIntf {
  public SurveyAdvanceGenerated(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private static final Logger logger = Logger.getLogger(SurveyAdvanceGenerated.class);
  @Override
  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix, Sql sql, String separator) {
    String surveyProvider = study.getSurveySystemId().toString();
    String sectionId = study.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix);
    int nbrArgs=0;
    Survey s = query.surveyBySurveyTokenId(tokenId);
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    LinkedHashMap<String, String> groupReferences = squareXml.getGroupRefs();
    Object refKeys[] = references.keySet().toArray();
    int inx = 0;

    for (String columnName : columns.keySet()) {
      String refKey = refKeys[inx].toString();
      String fieldId = references.get(refKey) + ":" + refKey;
      try {
        String[] parts = fieldId.split(":");
        if (columns.get(columnName).equals("select1") ||
            columns.get(columnName).equals(Constants.TYPE_ANSWER[Constants.TYPE_DROPDOWN])) {
          Integer intColumn = getSelect1Response(s, surveyProvider, sectionId, "Order" + parts[0], fieldId);
          if (intColumn != null) {
            sql.listSeparator(separator).append(columnName).argInteger(intColumn);
            nbrArgs++;
          }
        } else if (columns.get(columnName).equals("select") ) {
            if (isCheckboxSelected(s, surveyProvider, sectionId, "Order" + parts[0], fieldId, parts[2])) {
              sql.listSeparator(separator).append(columnName).argString("1");
              nbrArgs++;
            } else if (parts.length > 3 && groupReferences != null && groupReferences.get(parts[3]) != null) {
              String groupFieldId = parts[0] + ":" + parts[1] + ":" + groupReferences.get(parts[3]);
              if (isCheckboxSelected(s, surveyProvider, sectionId, "Order" + parts[0], groupFieldId, parts[2])) {
                sql.listSeparator(separator).append(columnName).argString("1");
                nbrArgs++;
              }
            }
        } else if (columns.get(columnName).equals("input") || columns.get(columnName).equals("datePicker") || columns.get(columnName).equals("slider") ) {
          String response = getInputStringResponse(s, surveyProvider, sectionId, "Order" + parts[0], fieldId);
          if (response != null) {
            sql.listSeparator(separator).append(columnName).argString(response);
            nbrArgs++;
          }
        } else if (columns.get(columnName).equals("radio")  ) { //&&  step.answerNumeric()!= null){
          Integer response = getRadioIntegerResponse(s, surveyProvider, sectionId, parts[0]);
          if (response != null) {
            sql.listSeparator(separator).append(columnName).argInteger(response);
            nbrArgs++;
          }
        } else if (columns.get(columnName).equals("textboxset" )) {
          if (parts.length > 2) {
            String textBoxNum = parts[2];
            fieldId = parts[0] + ":" + parts[1] + ":" + refKey.substring(0, refKey.indexOf(textBoxNum));
            String response = getInputStringResponseChoice(s, surveyProvider, sectionId, "Order" + parts[0], fieldId, Integer.parseInt(textBoxNum) -1 );
            if (response != null) {
              sql.listSeparator(separator).append(columnName).argString(response);
              nbrArgs++;
            }
          }
        } else if (columns.get(columnName).equalsIgnoreCase("radiosetgrid")) {
          if (parts.length > 0) {
            fieldId = parts[0] + ":" + parts[1]; // Use only the first two as the columns are in the choices
            SurveyStep step = s.answeredStepByProviderSectionQuestion(surveyProvider, sectionId, "Order" + parts[0]);
            if (step != null && step.answer() != null && step.answer().form() != null) {
              List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
              if (fields != null) {
                for (FormFieldAnswer field : fields) {
                  if (field != null && field.getFieldId() != null && field.getFieldId().equals(fieldId)) {
                    List<String> choices = step.answer().formFieldValues(fieldId);
                    if (choices != null) {
                      for (String choice : choices) {
                        String[] choiceParts = choice.split(":");
                        if (choiceParts.length > 0 && choiceParts[0] != null && choiceParts[0].equals(refKey)) {
                          try {
                            Integer choiceValue = Integer.parseInt(choiceParts[1]);
                            sql.listSeparator(separator).append(columnName).argInteger(choiceValue);
                            nbrArgs++;
                          } catch (NumberFormatException nfe) {
                            logger.warn("survey_token_id " + tokenId + " has an invalid numeric choice of " + choiceParts[1] + " on radiosetgrid column " + columnName);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        else {
          logger.error("Cannot process column type " + columns.get(columnName) + " in study " + study.getStudyDescription());
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        System.out.println("failed to parse " + refKey + " into fieldid, value for " + sectionId + " field " + columnName);
      }
      inx++;
    }
    return nbrArgs;
  }

  @Override
  public ArrayList<String> getSurveyDocumentation(Database database, Study study, String prefix) {
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix, true);
    return squareXml.getDocumentationLog();
  }

  @Override
  public LinkedHashMap<String, FieldType> getSquareTableColumns(Database database, Study study, String prefix) {
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix, false);
    return squareXml.getColumnTypes();
  }


}
