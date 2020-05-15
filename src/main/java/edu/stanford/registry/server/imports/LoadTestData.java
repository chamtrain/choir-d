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
package edu.stanford.registry.server.imports;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.utils.ExcelReader;
import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.poi.ss.usermodel.Cell;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

public class LoadTestData {

  private String principal;
  private String databaseDriver;
  private String databaseUrl;
  private String databaseUser;
  private String databasePassword;

  private String fileName;
  private static Logger logger = Logger.getLogger(LoadTestData.class);
  private ArrayList<Study> studies;
  private ArrayList<SurveySystem> surveySystems;
  private SitesInfo sitesInfo;

  public static void main(String[] args) {
    new LoadTestData();
  }
  private final Calendar nowCal = Calendar.getInstance(Locale.US);
  private final Calendar surCal = Calendar.getInstance(Locale.US);

  LoadTestData() {
    try {
      init();
      if (databaseUrl == null || databaseUser == null || databasePassword == null) {
        logger.error("Not all connection information is available");
      }
      else 
      {
        DatabaseProvider.fromDriverManager(databaseUrl, databaseUser, databasePassword)
        .transact(dbp -> {
            ServerContext serverContext = new ServerContext(dbp);
            sitesInfo = serverContext.getSitesInfo();
            SurveySystDao ssDao = new SurveySystDao(dbp);
            studies = ssDao.getStudies();
            surveySystems= ssDao.getSurveySystems();
            loadData(dbp.get());
        });
      }
    } catch (Exception ioe) {
      logger.error(ioe);
    }
  }

  private void init () throws IOException {
    String log4jConfig = System.getProperty("log4j.configuration");

    if (log4jConfig != null) {
      try {
        log4jConfig = new File(log4jConfig).getAbsolutePath();
        DOMConfigurator.configure(log4jConfig);
      } catch (Exception e) {
        System.err.println("Unable to configure log4j using file: " + log4jConfig);
        e.printStackTrace();
      }
    }
    logger = Logger.getLogger(LoadTestData.class);

    // Load configuration file if provided
    Properties buildProperties = new Properties();
    String propertiesFile = System.getProperty("build.properties");
    try {
      if (propertiesFile != null) {
        FileInputStream is = new FileInputStream(propertiesFile);
        buildProperties.load(is);
        is.close();
        logger.info("Read properties from " + propertiesFile);
      } else {
        logger
        .debug("Not reading properties file (no vm option -Dbuild.properties=/full/path/registry.build.properties)");
      }
    } catch (Exception e) {
      logger.warn("Unable to read properties from file " + propertiesFile, e);
    }
    HashMap<String, String> initParams = new HashMap<>();

    // The default principal is admin because that is the admin in our test database
    principal = System.getProperty("dataload.user", buildProperties.getProperty("dataload.user", "admin"));
    initParams.put("dataload.user", principal);
    logger.info("All requests will be automatically authenticated as user " + principal);

    fileName =  System.getProperty("dataload.file", buildProperties.getProperty("dataload.file", "src\\main\\sql\\oracle\\patientstudy_test_data.xlsx"));
    initParams.put("dataload.file", fileName);

    databaseDriver = System.getProperty("database.driver", buildProperties.getProperty("database.driver", "oracle.jdbc.OracleDriver"));
    initParams.put("database.driver", databaseDriver);

    databaseUrl = System.getProperty("database.url", buildProperties.getProperty("database.url"));
    initParams.put("database.url", databaseUrl);

    databaseUser = System.getProperty("registry.database.user", buildProperties.getProperty("registry.database.user"));
    initParams.put("registry.database.user", databaseUser); 

    databasePassword = System.getProperty("registry.database.password",buildProperties.getProperty("registry.database.password"));
    initParams.put("registry.database.password", databasePassword);

    // initialize ServerUtils for DatabaseProvider use
    new ServerUtils("./");

  }

  // Make a pair of Daos hashed by siteId
  static class Daos {
    final AssessDao assess;
    final ActivityDao activity;

    Daos(Database database, SiteInfo siteInfo) {
      activity = new ActivityDao(database, siteInfo.getSiteId());
      assess = new AssessDao(database, siteInfo);
    }
  }

  // This just lets us cache the daos for each site instead of making a new one for each.
  static class DaoCache {
    Hashtable<Long, Daos> daoMap = new Hashtable<Long, Daos>();
    Database db;
    SitesInfo sitesInfo;
    DaoCache(Database db, SitesInfo sitesInfo) {
      this.sitesInfo = sitesInfo;
      this.db = db;
    }

    Daos get(Long siteId) {
      Daos d = daoMap.get(siteId);
      if (d == null)
        daoMap.put(siteId, d = new Daos(db, sitesInfo.getBySiteId(siteId)));
  
      return d;
    }
  }

  private void loadData(Database database) {
    Spreadsheet sheet = null;
    nowCal.setTime(new Date());
    try {
      sheet = ExcelReader.loadSpreadSheet(fileName);
      if (sheet == null) {
        logger.error("load file " + fileName + " is empty or invalid xlsx");
        return;
      } else {
        logger.info("Loaded file " + fileName + " with " + sheet.size() + " rows");
      }
      int insertCount = 0;
      int surveyDateDays = 0;
      // move past the headings
      SurveyRegistration registration = null;
      DaoCache doaCache = new DaoCache(database, sitesInfo);
      for (int rowPointer = 0; rowPointer < sheet.size(); rowPointer++) {
        ArrayList<Cell> row = sheet.getRow(rowPointer);
        PatientStudyData dataRow = new PatientStudyData(row);
        String token = dataRow.getToken();
        Daos daos = doaCache.get(dataRow.getSiteId());
        //Handle a new registration
        if (registration == null || (registration.getToken() != null && !registration.getToken().equals(token))) {
          registration = daos.assess.getRegistration(token);
          if (registration != null) {
            // Change to todays date, keeping the appointment time
            surCal.setTime(registration.getSurveyDt());
            if (nowCal.get(Calendar.YEAR) > surCal.get(Calendar.YEAR)) {
              surCal.add(Calendar.YEAR, nowCal.get(Calendar.YEAR) - surCal.get(Calendar.YEAR));
            }
            if (nowCal.get(Calendar.DAY_OF_YEAR) > surCal.get(Calendar.DAY_OF_YEAR)) {
              surCal.add(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR) - surCal.get(Calendar.DAY_OF_YEAR));
            }
            if (nowCal.get(Calendar.DAY_OF_YEAR) < surCal.get(Calendar.DAY_OF_YEAR)) {
              surCal.add(Calendar.DAY_OF_YEAR, (surCal.get(Calendar.DAY_OF_YEAR) - nowCal.get(Calendar.DAY_OF_YEAR))
                  * -1);
            }
            // Spread them over a couple of days
            if (surveyDateDays > 1) {
              surveyDateDays = 0;
            }
            surCal.add(Calendar.DAY_OF_YEAR, surveyDateDays);
            String sql = "UPDATE SURVEY_REGISTRATION SET SURVEY_DT = ? WHERE SURVEY_REG_ID = ? ";
            database.toUpdate(sql).argDate(new Date(surCal.getTimeInMillis())).argLong(registration.getSurveyRegId()).update(1);
            surveyDateDays++;
            Activity activity = new Activity(registration.getPatientId(), Constants.ACTIVITY_COMPLETED,
                registration.getToken());
            daos.activity.createActivity(activity);
          }
        }
        if (registration != null) {
          PatientStudy patStudy = new PatientStudy(registration.getSurveySiteId());
          patStudy.setDtCreated(new Date(nowCal.getTimeInMillis()));
          patStudy.setExternalReferenceId(dataRow.getExternalReference());
          patStudy.setMetaVersion(0);
          patStudy.setOrderNumber(dataRow.getOrderNumber());
          patStudy.setPatientId(registration.getPatientId());
          patStudy.setSurveyRegId(registration.getSurveyRegId());
          patStudy.setSurveySystemId(dataRow.getSurveySystemId());
          patStudy.setStudyCode(dataRow.getStudyCode(patStudy.getSurveySystemId()));
          patStudy.setToken(token);

          SiteInfo siteInfo = sitesInfo.getBySiteId(registration.getSurveySiteId());
          PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
          patStudy = patStudyDao.insertPatientStudy(patStudy);
          patStudyDao.setPatientStudyContents(patStudy, dataRow.getClob(), true);
          insertCount++;
        }
      }
      logger.info(insertCount + "patient_study rows were added into the database");


      /** Distribute patients survey dates:
       * 1. Get a list of surveys by patient, with the initial one as the oldest 
       * 2. Walk through the list for each patient backing up the survey date by 
       *    2 weeks * the number of surveys the patient has 
       *    (less the # already processed for that patient). 
       * 3. Update the surveys with the new dates.
       **/

      // TABLEREF survey_registration
      String sql = "WITH counts AS (SELECT patient_id, count(*) AS numsurveys FROM survey_registration GROUP BY patient_id),"  
          + " surveys AS (SELECT patient_id, survey_reg_id, survey_dt, survey_type,"
          + " substr(survey_type,1,1) as type_order , "
          + " decode(instr(survey_type,'.', 1, 1), 0, '.0000', substr(survey_type, instr(survey_type,'.', 1, 1))) as vs_order " 
          + " FROM survey_registration) "
          + " SELECT s.patient_id, s.survey_reg_id, s.survey_dt, c.numsurveys, type_order, vs_order "
          + " FROM counts c, surveys s WHERE c.patient_id = s.patient_id ORDER BY s.patient_id, s.type_order desc, vs_order asc ";
      ArrayList<SurveyRegistration> surveys = database.toSelect(sql)
          .query(new RowsHandler<ArrayList<SurveyRegistration>>() {
            @Override
            public ArrayList<SurveyRegistration> process(Rows rs) throws Exception {
              Hashtable<String, Integer> patientCounts = new Hashtable<>();
              ArrayList<SurveyRegistration> surveys = new ArrayList<>();
              while (rs.next()) {
                Integer numberOfSurveys = rs.getIntegerOrNull(4);
                if (numberOfSurveys > 1) {
                  String patientId = rs.getStringOrNull(1);
                  Integer counter = patientCounts.get(patientId);
                  if (counter == null) {
                    counter = numberOfSurveys;
                  }
                  if (counter > 1) {
                    counter = counter - 1;
                    SurveyRegistration sreg = new SurveyRegistration();
                    sreg.setSurveyRegId(rs.getLongOrNull(2));
                    surCal.setTime(rs.getDateOrNull(3));
                    surCal.add(Calendar.DAY_OF_YEAR, counter * -14);
                    sreg.setSurveyDt(new Date(surCal.getTimeInMillis()));
                    surveys.add(sreg);
                    patientCounts.put(patientId, counter);
                  }
                }
              }
              return surveys;
            }
          });
      // TABLEREF survey_registration
      sql = "UPDATE SURVEY_REGISTRATION SET SURVEY_DT = ? WHERE SURVEY_REG_ID = ? ";
      for (SurveyRegistration survey : surveys) {
        database.toUpdate(sql).argDate(survey.getSurveyDt()).argLong(survey.getSurveyRegId()).update(1);
      }

      /*
       * Set the dt_changed to just prior to the survey_dt for each row 
       */
      // TABLEREF survey_registration
      // TABLEREF patient_study
      sql = "UPDATE patient_study SET dt_changed = (SELECT survey_dt - 1/48 FROM survey_registration sr WHERE patient_study.survey_reg_id = sr.survey_reg_id)";
      int rows = database.toUpdate(sql).update();
      logger.debug("Updated " + rows + " patient_study rows");
    } catch (IOException e) {
      logger.error(e);
    }

  }

  private class PatientStudyData {
    private final String surveySystemName ;
    private final String studyDescription;
    private String token;
    private final String externalReference;
    private Double orderNumber = 0.0;
    private final String clob;
    private final Long siteId;
    public PatientStudyData(ArrayList<Cell> row) {
      if (row == null || row.size() < 7) {
        logger.error("row does not have seven cells");
      }
      surveySystemName = XchgUtils.getContents(row.get(0));
      studyDescription = XchgUtils.getContents(row.get(1));
      token = XchgUtils.getContents(row.get(2));
      externalReference= XchgUtils.getContents(row.get(3));
      if (row.get(4).getCellType() == Cell.CELL_TYPE_NUMERIC) {
        orderNumber= row.get(4).getNumericCellValue();
      } else {
        logger.warn("ordernumber contains invalid numeric value :" + row.get(4).getStringCellValue());
      }
      clob= XchgUtils.getContents(row.get(5));
      String doubleStr = String.valueOf(Math.floor(row.get(6).getNumericCellValue()));
      int decimalLocation = doubleStr.indexOf(".");
      if (decimalLocation > -1) doubleStr = doubleStr.substring(0, decimalLocation);
      siteId = Long.getLong(doubleStr);
    }

    public String getSurveySystemName() {
      return surveySystemName;
    }

    public String getStudyDescription() {
      return studyDescription;
    }

    public String getToken() {
      return token;
    }

    public String getExternalReference() {
      return externalReference;
    }

    public Integer getOrderNumber() {
      return orderNumber.intValue();
    }

    public String getClob() {
      return clob;
    }

    public Integer getSurveySystemId() {
      if (surveySystems != null && getSurveySystemName() != null) {
        for (SurveySystem surveySystem : surveySystems) {
          if (getSurveySystemName().trim().equals(surveySystem.getSurveySystemName())) {
            return surveySystem.getSurveySystemId();
          }
        }
      }
      return 0;
    }

    public Integer getStudyCode(Integer surveySystemId) {
      if (studies != null && getStudyDescription() != null && surveySystemId != null) {
        for (Study study : studies) {
          if (getStudyDescription().trim().equals(study.getStudyDescription())
              && study.getSurveySystemId().intValue() == surveySystemId.intValue()) {
            return study.getStudyCode();
          }
        }
      }
      return 0;
    }

    public Long getSiteId() {
      return siteId;
    }
  }




}
