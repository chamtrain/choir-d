package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.survey.TxSurveyAdvance;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.server.utils.TxSquareXml;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyStep;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Sql;

/**
 * Created by tpacht on 6/3/2016.
 */
public class PacPainMedsSurveyAdvance extends TxSurveyAdvance {
  private static final Logger log = Logger.getLogger(PacPainMedsSurveyAdvance.class);

  protected PacPainMedsSurveyAdvance(SiteInfo siteInfo) {
    super(siteInfo);
  }

  public int processStudy(Supplier<Database> database, String localSurveyProvider, String studyName, Survey s, Sql sql, String separator) throws InvalidDataElementException, NumberFormatException {

    TxSquareXml squareXml = new TxSquareXml(database.get(), siteInfo, studyName, "PREOP_PMEDS_", false);
    String sectionId = database.get().toSelect("SELECT study_code FROM study WHERE study_description = ?").argString(studyName).query(
        new RowsHandler<String>() {
          @Override
          public String process(Rows rs) throws Exception {
            if (rs.next()) {
              return Integer.toString(rs.getIntegerOrNull());
            }
            return null;
          }
        });
    SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, sectionId, "Order1");
    return processStep(squareXml, step,sql, separator, studyName);

  }
  public int processStep(SquareXml squareXml, SurveyStep step, Sql sql, String separator, String studyName) {
    int nbrArgs = 0;
    if (step != null) {
      LinkedHashMap<String, String> columns = squareXml.getColumns();
      LinkedHashMap<String, String> references = squareXml.getReferences();
      Object refKeys[] = references.keySet().toArray();
      int inx = 0;
      for (String columnName : columns.keySet()) {
        String ref = refKeys[inx].toString();
        String fieldId = references.get(ref);
        try {
          if (ref.endsWith("times") || ref.endsWith("pills")) {
            fieldId = fieldId + ":" + ref;
          }

          log.trace("ColumnName: " + columnName + " type:" + columns.get(columnName) + " ref:" + ref + " fieldId:" + fieldId);
          if (columns.get(columnName).equals("select1")) {

            Integer intColumn = selectedFieldChoice(step, fieldId);
            if (intColumn != null) {
              sql.listSeparator(separator).append(columnName).argInteger(intColumn);
              nbrArgs++;
            }
          } else if (columns.get(columnName).equals("select")) {
            String[] parts = fieldId.split(":");
            String choice = parts[2];
            if (selectedFieldChoice(step, parts[0] + ":" + parts[1], choice)) {
              sql.listSeparator(separator).append(columnName).argString("1");
              nbrArgs++;
            }
          } else if (columns.get(columnName).equals("input")) {
            String inputValue = formFieldValue(step, fieldId);
            if (inputValue != null) {
              sql.listSeparator(separator).append(columnName).argString(inputValue);
              nbrArgs++;
            }
          } else {
            throw new InvalidDataElementException("Unknown response type of " + columns.get(columnName) + " in study " + studyName);
          }
        } catch (Exception ex) {
          System.out.println(
              "failed to parse " + fieldId + " into fieldid, value for " + studyName + " field " + columnName);
        }
        inx++;
      }
    }
    return nbrArgs;
  }

}
