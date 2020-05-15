/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;
import edu.stanford.registry.server.survey.AngerService;
import edu.stanford.registry.server.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.SqlSelect;

public class AngerReports extends ApiReportCommon implements ApiReportGenerator {

  private static final String PATIENTS = "patients";
  private static final String CONSENTYES =  "Consent-Yes";
  private static final String CONSENTNO =  "Consent-No";
  private static final String FOLLOWUPYES = "FollowUp-Yes";
  private static final String FOLLOWUPNO = "FollowUp-No";
  private static final Logger logger = LoggerFactory.getLogger(AngerReports.class);
  private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    JSONObject jsonObject = jsonRepresentation.getJsonObject();
    if (jsonObject == null) {
      logger.warn("handling report {} jsonObject is NULL!!!", reportName);
    }
    if (reportName == null) {
      logger.warn("reportname is null");
      return null;
    }

    if (reportName.endsWith(PATIENTS)) {
      logger.trace(PATIENTS + " reports");
      return getReturnObject(patientsByAttribute(databaseProvider.get(), siteInfo , getStartDt(jsonObject), getEndDt(jsonObject),  getChoice(jsonObject,"patresponse" )));
    }
    return  jsonObject;
  }

  @Override
  public JSONObject getReportParameters(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName) throws ApiStatusException {
    if (!reportName.endsWith(PATIENTS)) {
      throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, "Report " + reportName + " not supported");
    }
    logger.debug("getting params for {}", reportName  );
    JSONObject params = new JSONObject();
    JSONObject fromParam = makeReportInputOption("From", "fromDt", "date");
    JSONObject toParam = makeReportInputOption("To", "toDt", "date");
    // Default the from date to a year ago
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.YEAR, -1);
    fromParam.put("value", formatter.format(calendar.getTime()));
    params.accumulate("reportParameters", fromParam);
    // And the to date to today
    toParam.put("value", formatter.format(today));
    params.accumulate("reportParameters", toParam);
    ArrayList<String> statustypes = new ArrayList<>();
    statustypes.add(CONSENTYES);
    statustypes.add(CONSENTNO);
    statustypes.add(FOLLOWUPYES);
    statustypes.add(FOLLOWUPNO);
    params.accumulate("reportParameters", makeReportSelectOption("radio","Patients with response", "patresponse", statustypes));

    // Get the 'to date' counts
    String sql = "select data_name, data_value, count(*) from patient_attribute "
        + "where data_name in (?, ?) group by data_name, data_value";
    ArrayList<JSONObject> counts =

    databaseProvider.get().toSelect(sql)
         .argString(AngerService.takeConsent).argString(AngerService.followConsent)
         .query(rs -> {
          ArrayList<JSONObject> objs = new ArrayList<>();
           while (rs.next()) {
             JSONObject jsonObject = new JSONObject();
             String name = convertName(rs.getStringOrEmpty(1), rs.getStringOrEmpty(2));
             jsonObject.put("name", name);
             jsonObject.put("count",rs.getIntegerOrZero(3) );
             objs.add( jsonObject );
           }
           return objs;
         });
     for (JSONObject obj : counts) {
       params.accumulate("summarycounts", obj );
     }

    return params;
  }

  private String convertName(String dataName, String dataValue) {
    if (AngerService.takeConsent.equals(dataName)) {
      if ("Y".equals(dataValue)) {
        return CONSENTYES;
      } else {
        return CONSENTNO;
      }
    } else {
      if ("Y".equals(dataValue)) {
        return FOLLOWUPYES;
      } else {
        return FOLLOWUPNO;
      }
    }
  }

  private ArrayList<ArrayList<Object>> patientsByAttribute(Database database, SiteInfo siteInfo, Date fromDate, Date toDate, String patientResponse) {
    String sql =  "SELECT PAT.PATIENT_ID, PAT.FIRST_NAME, PAT.LAST_NAME, PAT.DT_BIRTH, ATTR.DATA_NAME, ATTR.DATA_VALUE, ATTR.DT_CREATED"
        + " FROM PATIENT PAT, PATIENT_ATTRIBUTE ATTR WHERE ATTR.PATIENT_ID = PAT.PATIENT_ID AND "
        + " ATTR.SURVEY_SITE_ID = :site AND ATTR.DT_CREATED BETWEEN ? AND ? AND ATTR.DATA_NAME = ? AND ATTR.DATA_VALUE = ? ";


    logger.debug("starting patientsByAttribute");
    SqlSelect select = database.toSelect(sql)
        .argLong(":site", siteInfo.getSiteId())
        .argDate(DateUtils.getTimestampStart(siteInfo, fromDate))
        .argDate(DateUtils.getTimestampEnd(siteInfo, toDate));


      switch (patientResponse) {
      case CONSENTYES:
        select = select.argString(AngerService.takeConsent).argString("Y");
        break;
      case CONSENTNO:
        select = select.argString(AngerService.takeConsent).argString("N");
        break;
      case FOLLOWUPYES:
        select = select.argString(AngerService.followConsent).argString("Y");
        break;
      case FOLLOWUPNO:
        select = select.argString(AngerService.followConsent).argString("N");
        break;
      }

    return select
        .query(rs -> {
          ArrayList<ArrayList<Object>> report = new ArrayList<>();
          /* headings */
          ArrayList<Object> head = new ArrayList<>();
          head.add("Patient Id");
          head.add("First Name");
          head.add("Last Name");
          head.add("Date Birth");
          head.add("Question");
          head.add("Response");
          head.add("Date");
          report.add(head);
          while (rs.next()) {
            ArrayList<Object> line = new ArrayList<>();
            line.add(rs.getStringOrEmpty(1));
            line.add(rs.getStringOrEmpty(2));
            line.add(rs.getStringOrEmpty(3));
            line.add(rs.getDateOrNull(4));
            String attr = rs.getStringOrEmpty(5);
            String val = rs.getStringOrEmpty(6);
            if (AngerService.takeConsent.equals(attr)) {
              if ("Y".equals(val)) {
                line.add(CONSENTYES);
              } else {
                line.add(CONSENTNO);
              }
            }
            if (AngerService.followConsent.equals(attr)) {
              if ("Y".equals(val)) {
                line.add(FOLLOWUPYES);
              } else
                line.add(FOLLOWUPNO);
            }
            line.add(val);
            line.add(rs.getDateOrNull(7));
            report.add(line);
          }
          return report;
        });
  }
}
