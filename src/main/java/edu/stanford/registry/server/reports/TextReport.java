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

package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.ResultGeneratorIntf;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.survey.PageNumber;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.susom.database.Database;

public class TextReport implements ResultGeneratorIntf {
  private static Logger logger = Logger.getLogger(TextReport.class);

 private static final String TAB = "\t";

 public static final Long VERSION = (long) 1;


  private Database database;
  ChartUtilities chartUtils;
  HashMap<String, Study> studyCodes = null;
  HashMap<String, SurveySystem> surveySystems = null;
  Float topOfPage = null;

  private SiteInfo siteInfo;
  PageNumber pageCount;

  private ArrayList<String> reportData;
  // defaults
  public TextReport(Supplier<Database> dbp, SiteInfo siteInfo) {
    this.database = dbp.get();
    /* get a studyCode:studyName map of the studies in the patients array */
    studyCodes = PDFUtils.getStudyCodeHash(database);
    surveySystems = PDFUtils.getSurveySystemHash(database);
    pageCount = new PageNumber(0);
    this.siteInfo = siteInfo;
    chartUtils = new ChartUtilities(siteInfo);
  }

  public TextReport() {

  }

  public ArrayList<String> makeReport(JSONObject jObject,
      ChartConfigurationOptions opts) throws IOException, InvalidDataElementException, JSONException {
    reportData = new ArrayList<>();
    if (jObject == null) {
      addLine("ERROR", "No data found");
      return reportData;
    }
    //reportData.add( jObject.toString() );
    addLine(jObject, JsonReport.TITLE);
    addKeyData(jObject, JsonReport.MRN);
    addKeyData(jObject, JsonReport.NAME);
    addKeyData(jObject, JsonReport.DOB);
    addKeyData(jObject, JsonReport.AGE);
    addKeyData(jObject, JsonReport.GENDER);
    if (!jObject.has(JsonReport.SURVEY_ARRAY)) {
      addLine("ERROR","No completed surveys found");
      return reportData;
    }


    Object obj = jObject.get(JsonReport.SURVEY_ARRAY);
    if (obj instanceof JSONArray) {
      JSONArray jsonStudies = (JSONArray) obj;
      for ( int j=0; j<jsonStudies.length(); j++ ) {
        addScores(jsonStudies.getJSONObject(j));
      }
      reportData.add(jObject.getString(JsonReport.INVERTED_MSG));
      if (jObject.has(JsonReport.ASSISTED_MSG)) {
        reportData.add(jObject.getString(JsonReport.ASSISTED_MSG));
      }
      for ( int j=0; j<jsonStudies.length(); j++ ) {
        try {
          JSONObject jsonStudy =  jsonStudies.getJSONObject(j);
          if (jsonStudy.has(JsonReport.TABLE)) {
            Table reportTable = jsonToTable(jsonStudy.getJSONObject(JsonReport.TABLE));
            if (reportTable != null) {
              ArrayList<TableRow> rows = reportTable.getRows();
              if (reportTable.getHeadings() != null && reportTable.getHeadings().size() > 0) {
                reportData.add(reportTable.getHeadings().get(0));
              }
              if (rows != null && rows.size() > 0) {
                for (TableRow row : rows ) {
                  if (row != null && row.getColumns() != null) {
                    ArrayList<TableColumn> columns = row.getColumns();
                    StringBuilder sBuf = new StringBuilder();
                    for (TableColumn column : columns) {
                       sBuf.append(column.getValue());
                       sBuf.append("\t");
                    }
                    reportData.add(sBuf.toString());
                  }
                }
              }
            }
          }
          if (jsonStudy.has(JsonReport.QUESTION_ARRAY)) {
            if (jsonStudy.has(JsonReport.SECTION_HEADING)) {
              reportData.add(jsonStudy.getString(JsonReport.SECTION_HEADING));
            }

            Object qObj = jsonStudy.get(JsonReport.QUESTION_ARRAY);

            if (qObj != null && qObj instanceof JSONArray) {
              JSONArray jsonQuestions = (JSONArray) qObj;
              for ( int q=0; q<jsonQuestions.length(); q++) {
                if (jsonQuestions.get(q) instanceof JSONObject) {
                  addQuestion(jsonQuestions.getJSONObject(q));

                }else {
                  reportData.add("jquestions at " + q + " is not a jsonobject");
                }
              }
            } else if (qObj != null && qObj instanceof JSONObject){
              JSONObject jsonQuestion = (JSONObject) qObj;
              addLine(jsonQuestion.getString(JsonReport.QUESTION), jsonQuestion.getString(JsonReport.ANSWER));
            }
          }
        } catch (Exception ex) {
            addLine("ERROR", ex.toString() );
            ex.printStackTrace();
        }
     }
   }
    //reportData.add(jObject.toString());
    reportData.add(getDocumentControlId() + "[" + jObject.getString(JsonReport.CONTROL_ID) + "]");
    return reportData;
  }




  private void addScores(JSONObject jObject ) throws IOException, InvalidDataElementException, JSONException {

    if (!jObject.has(JsonReport.SCORES_ARRAY)) {
      return;
    }
    Object obj = jObject.get(JsonReport.SCORES_ARRAY);
    if (obj instanceof JSONArray) {
      JSONArray jsonScores = (JSONArray) obj;
      for ( int j=0; j<jsonScores.length(); j++ ) {
        try {
          addScore(jsonScores.getJSONObject(j));
        } catch (JSONException e) {
          reportData.add("ERROR" + TAB + e.toString());
          logger.error(e);
        }
      }
    } else if (obj instanceof JSONObject) {
      addScore((JSONObject) obj);
    }
  }

  private void addScore(JSONObject jsonScore) throws JSONException {
    StringBuilder scoreBuf = new StringBuilder();
    if (jsonScore.has(JsonReport.SCORE_VALUE)) {
      scoreBuf.append(jsonScore.getInt(JsonReport.SCORE_VALUE));
    }
    if (jsonScore.has(JsonReport.SCORE_PERCENTILE)) {
      scoreBuf.append(" / ");
      scoreBuf.append(jsonScore.getLong(JsonReport.SCORE_PERCENTILE));
    }
    if (jsonScore.has(JsonReport.SCORE_CATEGORY)) {
      scoreBuf.append(" / ");
      scoreBuf.append(jsonScore.getString(JsonReport.SCORE_CATEGORY));
    }
    addLine(jsonScore.getString(JsonReport.SCORE_HEADING), scoreBuf.toString());
  }


  private void addQuestion(JSONObject jsonQuestion) throws JSONException  {
    if (jsonQuestion.has(JsonReport.QUESTION) && jsonQuestion.has(JsonReport.ANSWER)) {
      addLine( asString(jsonQuestion, JsonReport.QUESTION),
          asString(jsonQuestion, JsonReport.ANSWER));
    }
  }

  private String asString(JSONObject jsonObject, String key) throws JSONException {

    StringBuffer sBuf;
    Object obj = jsonObject.get(key);
    if (obj instanceof JSONArray) {
      sBuf = new StringBuffer();
      JSONArray jsonArray = (JSONArray) obj;
      for (int j=0; j<jsonArray.length(); j++) {
        if (j > 0) {
          sBuf.append(" ");
        }
        sBuf.append(jsonArray.getString(j));
      }
    } else {
      sBuf = new StringBuffer(jsonObject.getString(key));
    }
    return sBuf.toString();
  }



  private static String DOC_ID_NAME = "PARPTTEXT";
  private static final String FIELD_SEPARATOR = "/";
  private static final SimpleDateFormat DOC_ID_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd_HH:mm");

  private static final int UPPER_LIMIT = 99999999;

  private Random randomGenerator = new Random(System.currentTimeMillis());
  private String documentControlId = getResultName() + FIELD_SEPARATOR + "v" + getResultVersion() + FIELD_SEPARATOR
      + randomGenerator.nextInt(UPPER_LIMIT) + FIELD_SEPARATOR + DOC_ID_TIME_FMT.format(new Date());

  @Override
  public String getResultName() {
    return DOC_ID_NAME;
  }

  @Override
  public Long getResultVersion() {
    return VERSION;
  }

  @Override
  public String getResultTitle() {
    return getResultType().getResultTitle();
  }

  @Override
  public String getDocumentControlId() {
    return documentControlId;
  }

  @Override
  public PatientResultType getResultType() {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    return assessDao.getPatientResultType(getResultName());
  }

  private void addLine(JSONObject jObject, String key) {
    try {
    if (jObject.has(key)) {
      reportData.add(jObject.getString(key));
    } else {
      reportData.add(key + " not found in report");
    }
    } catch (JSONException je) {
      logger.error(je);
    }
  }

  private void addKeyData(JSONObject jObject, String key) {
    try {
    if (jObject.has(key)) {
      //reportData.add("\"" + key + "\",\"" + jObject.getString(key) + "\"");
      addLine(key, jObject.getString(key));
    }
    } catch (JSONException je) {
      logger.error(je);
    }
  }

  private void addLine(String key, String value) {
    reportData.add(key + TAB + value);
  }
  private TableRow makeTableRow(JSONObject jObj) throws JSONException {
    TableRow row = new TableRow();
    if (jObj != null && jObj.has("column")) {
      if (jObj.get("column") instanceof JSONArray) {
        JSONArray jsonColArr = (JSONArray) jObj.get("column");
        for ( int i=0; i<jsonColArr.length(); i++) {
          row.addColumn(makeTableColumn(jsonColArr.getJSONObject(i)));
        }
      } else if (jObj.get("column") instanceof JSONObject) {
        row.addColumn(makeTableColumn((JSONObject) jObj.get("column")));
      }
    }
    return row;
  }

  private TableColumn makeTableColumn(JSONObject jObj) throws JSONException {
    TableColumn tableColumn = new TableColumn();
    if (jObj != null && jObj.has("value")) {
      tableColumn.setValue(jObj.getString("value"));
    }
    if (jObj != null && jObj.has("width")) {
      tableColumn.setWidth(Integer.parseInt(jObj.getString("width")));
    }
    return tableColumn;
  }

  private Table jsonToTable(JSONObject jsonObject) throws JSONException {
    Table table = new Table();

    if (jsonObject.has(JsonReport.HEADING)) {
      Object jObj = jsonObject.get(JsonReport.HEADING);
      if (jObj != null && jObj instanceof JSONArray) {
        JSONArray jsonHeadingArr = (JSONArray) jObj;
        for ( int i=0; i<jsonHeadingArr.length(); i++) {
          if (jsonHeadingArr.get(i) instanceof JSONObject) {
            table.addHeading(jsonHeadingArr.getString(i) );
          }
        }
      } else if (jObj != null && jObj instanceof JSONObject){
        JSONObject jsonHeading = (JSONObject) jObj;
        table.addHeading(jsonHeading.toString());
      } else if (jObj != null && jObj instanceof String) {
        table.addHeading((String) jObj);
      }
    }
    if (jsonObject.has(JsonReport.ROWS)) {
      Object jObj = jsonObject.get(JsonReport.ROWS);
      if (jObj != null && jObj instanceof JSONArray) {
        JSONArray jsonRowArr = (JSONArray) jObj;
        for ( int i=0; i<jsonRowArr.length(); i++) {
          if (jsonRowArr.get(i) instanceof JSONObject) {
            table.addRow(makeTableRow((JSONObject)jsonRowArr.get(i)));
          }
        }
      } else if (jObj instanceof JSONObject) {
        table.addRow(makeTableRow((JSONObject)jObj));
      } else if (jObj instanceof String) {

      }
    }
    return table;
  }
}
