package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.shared.Study;
import edu.stanford.survey.client.api.FieldType;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 4/6/2016.
 * This is a custom version of SurveyToSquare for the Cannibis Global Questionnaire (GCQ)
 */
public class GCQToSquare extends SurveyAdvanceBase implements SurveyToSquareIntf {
  private static final Logger logger = Logger.getLogger(GCQToSquare.class);

  public GCQToSquare(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public int addCompletedSurveyValues(Database database, Long tokenId, SurveyQuery query, Study study, String prefix, Sql sql, String separator) {
    String surveyProvider = study.getSurveySystemId().toString();
    String sectionId = study.getStudyCode().toString();
    SquareXml squareXml = new SquareXml(database, siteInfo, study.getStudyDescription(), prefix);
    int nbrArgs=0;
    Survey s = query.surveyBySurveyTokenId(Long.valueOf(tokenId.longValue()));
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object refKeys[] = references.keySet().toArray();
    int inx = 0;

    for (String columnName : columns.keySet()) {
      String refKey = refKeys[inx].toString();
      String fieldId = references.get(refKey) + ":" + refKey;
      try {
        String[] parts = fieldId.split(":");
        if (columns.get(columnName).equals("select1")) {
          Integer intColumn = getSelect1Response(s, surveyProvider, sectionId, "Order" + parts[0], fieldId);
          if (intColumn != null) {
            sql.listSeparator(separator).append(columnName).argInteger(intColumn);
            nbrArgs++;
          }
        } else if (columns.get(columnName).equals("select") ) {
          if (parts[0].equals("4")) {
            if (isCheckboxSelected(s,surveyProvider, sectionId, "Order4", "4:1:ADMINISTRATION", parts[2] )) {
               sql.listSeparator(separator).append(columnName).argString("1");
              nbrArgs++;
            }
          } else {
            if (isCheckboxSelected(s, surveyProvider, sectionId, "Order" + parts[0], fieldId, parts[2])) {
              sql.listSeparator(separator).append(columnName).argString("1");
              nbrArgs++;
            }
          }
        } else if (columns.get(columnName).equals("input")) {
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
        } else {
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
