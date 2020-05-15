package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.PainIntensityToSquare;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.survey.SurveyAdvanceGenerated;
import edu.stanford.registry.server.survey.SurveyAdvanceHandlerFactoryImpl;
import edu.stanford.registry.server.survey.SurveyAdvancePromis;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.utils.SquareUtils;
import edu.stanford.registry.server.utils.SquareUtils.SquareTableParameters;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.server.utils.TxSquareXml;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyAdvancePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DbCodeTx;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;
import com.github.susom.database.Transaction;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Created by tpacht on 5/12/2016.
 */
public class PreAnesthesiaAdvanceHandler extends SurveyAdvanceBase implements SurveyAdvanceHandler {
  private static final String tableName = "rpt_pac_std_surveys_square";
  private static final Logger log = LoggerFactory.getLogger(PreAnesthesiaAdvanceHandler.class);
  Database db;
  final static String[] xmlQuestionaires = { "pacBackground",  "pacIssues", "pacMenstrual", "pacIllness",
      "pacSmoking", "pacAlcohol", "pacDrugs", "pacQuestions", "pacContactInfo", "pacFollowUp", "pacPainDoc", "pacOpioids"};

  final static String[] promisQuestionnaires = { "PROMIS Bank v1.0 - Pain Interference", "PROMIS Bank v1.0 - Depression",
      "PROMIS Bank v1.0 - Anxiety", "PROMIS Bank v1.0 - Sleep Disturbance",  "PROMIS Bank v1.0 - Anger",
      "PROMIS Bank v1.0 - Fatigue", "PROMIS Physical Function Bank", "PROMIS Bank v2.0 - Social Isolation",
      "PROMIS Bank v1.2 - Physical Function", "PROMIS Bank v1.1 - Pain Interference"};

  final static String[] ghNames = {"gh_general_health","gh_general_quality_life","gh_physical_health","gh_mental_health",
      "gh_satisfaction_social","gh_carry_out_social","gh_carry_out_physical","gh_emotional_problems","gh_fatigue"};

  final static String prefix = "PREOP_";
  SurveySystem catSurveySystem = null;
  SurveySystem promisSurveySystem = null;
  SurveySystem localSurveySystem = null;
  HashMap<String, Study> studies = new HashMap<>();

  public PreAnesthesiaAdvanceHandler(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance surveyAdvance, Supplier<Database> dbp) {
    db = dbp.get();
    boolean exists = db.toSelect("select 'Y' from " +tableName + " where survey_site_id=? and survey_token_id=?")
        .argLong(surveyAdvance.getSurveySiteId())
        .argLong(surveyAdvance.getSurveyTokenId())
        .queryBooleanOrFalse();

    log.debug("Exists is " + exists);
    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;

      SurveyRegistration surveyRegistration = getSurveyRegistration(db, surveyAdvance);
      if (exists) {
        sql.listStart("update " +tableName +" set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into " + tableName + " (survey_site_id,survey_token_id,patient_id,")
            .argLong(surveyAdvance.getSurveySiteId())
            .argLong(surveyAdvance.getSurveyTokenId())
            .argString(surveyRegistration.getPatientId());
        separator = ",";
        nbrArgs += 3;
      }
      Survey s = getSurvey(db, surveyAdvance);
      sql.listSeparator(separator).append("assessment_type").argString(surveyRegistration.getSurveyType());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_scheduled").argDate(surveyRegistration.getSurveyDt());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_started").argDate(s.startTime());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_ended").argDate(s.endTime());
      nbrArgs++;
      sql.listSeparator(separator).append("is_complete").argBoolean(s.isComplete());
      nbrArgs++;
      // Should migrate to tracking serverTimeMillis() and userTimeMillis(), but need to regenerate table
      sql.listSeparator(separator).append("survey_user_time_ms").argLong(s.serverTimeMillis(Integer.MAX_VALUE));
      nbrArgs++;
      log.debug("getting list of questionaires for " + surveyRegistration.getSurveyType());
      // get the process names for the survey
      ArrayList<Element> questionnaires = XMLFileUtils.getInstance(siteInfo).getProcessQuestionaires(surveyRegistration.getSurveyType());
      if (questionnaires != null) {
        for (Element questionnaire : questionnaires) {
          String studyDescription = questionnaire.getAttribute("value");
          String provider = questionnaire.getAttribute("type");
          log.debug("processing " + studyDescription + " with provider " + provider );

          SurveySystDao ssDao = new SurveySystDao(dbp);
          SurveyDao surveyDao = new SurveyDao(db);
          SurveyQuery query = new SurveyQuery(db, surveyDao, surveyAdvance.getSurveySiteId());

          if ("Local".equals(provider)) {
            PreAnesthesiaService service = new PreAnesthesiaService(siteInfo);
            if (localSurveySystem == null) {
              localSurveySystem = ssDao.getSurveySystem("Local");
            }
            try {
              Study study = studies.get(studyDescription);
              if (study == null) {
                study = ssDao.getStudy(localSurveySystem.getSurveySystemId(), studyDescription);
                studies.put(studyDescription, study);
              }

              if ("pacPainMeds".equals(studyDescription)) {
                try {
                  PacPainMedsSurveyAdvance medsSurveyAdvance = new PacPainMedsSurveyAdvance(siteInfo);
                  nbrArgs += medsSurveyAdvance.processStudy(db, getProviderId(db, "Local"), "pacPainMeds", s, sql, separator);
                } catch (InvalidDataElementException | NumberFormatException ex) {
                  log.error(ex.getMessage() + " handline pacPainMeds", ex);
                }
              } else if ("painIntensity".equals(studyDescription)) {
                PainIntensityToSquare painIntensityAdvance = new PainIntensityToSquare();
                nbrArgs += painIntensityAdvance.addCompletedSurveyValues(db, surveyAdvance.getSurveyTokenId(), query, study, prefix, sql, separator);
              } else if ("bodymap".equals(studyDescription)) {
                nbrArgs += bodyMap(dbp.get(), s, sql, separator);
              } else if ("globalHealth".equals(studyDescription)) {
                nbrArgs += globalHealth(dbp.get(), s, sql, separator, surveyRegistration);
              } else if (studyDescription != null && studyDescription.contains("@")) {
                // Questionnaires created in the Survey builder
                nbrArgs += surveyBuilder(dbp.get(), ssDao, query, s, sql, separator, studyDescription);
              } else {
                nbrArgs += service.addCompletedSurveyValues(dbp.get(), surveyAdvance.getSurveyTokenId(), query, study, prefix, sql, separator);
              }
            } catch (NumberFormatException ex) {
              log.error(ex.getMessage(), ex);
            }
          } else if ("StanfordCat".equals(provider)) {
            if (catSurveySystem == null) {
              catSurveySystem = new SurveySystDao(dbp).getSurveySystem("StanfordCat");
            }
            SurveyAdvancePromis surveyAdvancePromis = new SurveyAdvancePromis(db, siteInfo);
            for (String promisQuestionnaire : promisQuestionnaires) {
              if (promisQuestionnaire.equals(studyDescription)) {
                Study promisStudy = studies.get(promisQuestionnaire);
                if (promisStudy == null) {
                  promisStudy = ssDao.getStudy(catSurveySystem.getSurveySystemId(), promisQuestionnaire);
                  studies.put(promisQuestionnaire, promisStudy);
                }
                nbrArgs += surveyAdvancePromis.addCompletedSurveyValues(db, surveyAdvance.getSurveyTokenId(), query, promisStudy, prefix, sql, separator);
              }
            }
          } else if ("LocalPromis".equals(provider)) {
            if (promisSurveySystem == null) {
              promisSurveySystem = new SurveySystDao(dbp).getSurveySystem("LocalPromis");
            }
            SurveyAdvancePromis surveyAdvancePromis = new SurveyAdvancePromis(db, siteInfo);
            Study promisStudy = studies.get(studyDescription);
            if (promisStudy == null) {
              promisStudy = ssDao.getStudy(promisSurveySystem.getSurveySystemId(), studyDescription);
              studies.put(studyDescription, promisStudy);
            }
            nbrArgs += surveyAdvancePromis.addCompletedSurveyValues(db, surveyAdvance.getSurveyTokenId(), query, promisStudy, prefix, sql, separator);
          }
        }
      }
      if ((exists && nbrArgs > 0) || (!exists && nbrArgs > 3)) {
        if (exists) {
          sql.listEnd("=? where survey_site_id=? and survey_token_id=?")
              .argLong(surveyAdvance.getSurveySiteId())
              .argLong(surveyAdvance.getSurveyTokenId());
        } else {
          sql.append(") values (?");
          while (nbrArgs-- > 1) {
            sql.append(",?");
          }
          sql.append(")");
        }
        try {
          db.toInsert(sql.sql()).apply(sql).insert(1);
        } catch (Exception ex) {
          log.error("Error trying to insert database", ex);
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error storing survey registration for site " + surveyAdvance.getSurveySiteId() + " token " + surveyAdvance.getSurveyTokenId() +
          " in " + tableName, e);
    }
  }
  private int bodyMap(Database db, Survey s, Sql sql, String separator) {
    int nbrArgs=0;
    String localSurveyProvider = getProviderId(db, "Local");
    SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "bodymap"), "2");
    if (step != null) {
      sql.listSeparator(separator).append(prefix + "bodymap_regions_csv").argString(step.answerRegionsCsv());
      nbrArgs++;
    } else {
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "bodymap"), "1");
      if (step != null) {
        sql.listSeparator(separator).append(prefix + "bodymap_regions_csv").argString(step.answerRegionsCsv());
        nbrArgs++;
      }
    }
    return nbrArgs;
  }

  private int globalHealth(Supplier<Database> dbp, Survey s, Sql sql, String separator, SurveyRegistration surveyRegistration) {
    int nbrArgs = 0;
    String localSurveyProvider = getProviderId(dbp.get(), "Local");
    for (int q=0; q<ghNames.length; q++) {
      int order = q + 1;
      SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "globalHealth"), "Order" + order);
      if (step != null) {
        sql.listSeparator(separator).append(prefix + ghNames[q]).argInteger(selectedFieldInt(step, order + ":1"));
        nbrArgs++;
      }
    }
    if (nbrArgs > 0) {
      PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);
      ArrayList<PatientStudyExtendedData> patStudies = patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(
                                                          surveyRegistration.getSurveyRegId(), "globalHealth");
      PatientStudyExtendedData patientStudy = null;
      if ((patStudies != null) && (patStudies.size() == 1)) {
        patientStudy = patStudies.get(0);
      }

      // Get the chart score for the patient study
      ChartScore chartScore = null;
      if (patientStudy != null) {
        ScoreProvider scoreProvider = SurveyServiceFactory.getFactory(siteInfo)
            .getScoreProvider(db, patientStudy.getSurveySystemName(), "globalHealth");
        ArrayList<ChartScore> chartScores = scoreProvider.getScore(patientStudy);

        if ((chartScores != null) || (chartScores.size() == 1)) {
          chartScore = chartScores.get(0);
        }
      }
      if (chartScore != null && chartScore instanceof GlobalHealthScore) {
        GlobalHealthScore ghScore = (GlobalHealthScore) chartScore;
        sql.listSeparator(separator).append(prefix + "gh_mental_score").argBigDecimal(new BigDecimal(ghScore.getMentalHealthTScore()));
        nbrArgs++;
        sql.listSeparator(separator).append(prefix + "gh_physical_score").argBigDecimal(new BigDecimal(ghScore.getPhysicalHealthTScore()));
        nbrArgs++;
      }
    }
    return nbrArgs;
  }

  private int surveyBuilder(Supplier<Database> dbp,SurveySystDao ssDao, SurveyQuery query, Survey s, Sql sql, String separator, String studyDescription) {
    int nbrArgs = 0;
    String localSurveyProvider = getProviderId(dbp.get(), "Local");
    AppConfigDao appConfigDao = new AppConfigDao(dbp.get());
    AppConfigEntry appConfigEntry = appConfigDao.findAppConfigEntry(siteInfo.getSiteId(), "squaretable", studyDescription);
    log.trace("surveyBuilder handling starting for {}", studyDescription);
    if (appConfigEntry != null) {
      /* Get the configuration from app_config for the square table processing */
      AppConfigEntry parameters = SquareUtils.getConfig(db, siteInfo, appConfigEntry.getAppConfigId());
      if (parameters != null) {
        SquareTableParameters squareTableParameters = AutoBeanCodex.decode(SquareUtils.getFactory(),
            SquareTableParameters.class, parameters.getConfigValue()).as();
        Study study = ssDao.getStudy(Integer.parseInt(localSurveyProvider), parameters.getConfigName());
        if (study != null) {
          SurveyAdvanceGenerated surveyAdvanceGenerated = new SurveyAdvanceGenerated(siteInfo);
          nbrArgs += surveyAdvanceGenerated.addCompletedSurveyValues(db, s.getSurveyTokenId(), query, study, squareTableParameters.getPrefix(), sql, separator);
        }
      }
    }
    return nbrArgs;
  }

  static final long SITE_ID_VALUE = 9L;  // this was hard-coded in the main() routine...

  static ServerContext initServerUtilsAndGetSiteIdOf9(String propFilename) {
    // Initialize server utils
    Properties buildProperties = new Properties();
    try {
      FileInputStream is = new FileInputStream(propFilename);
      buildProperties.load(is);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    HashMap<String, String> params = new HashMap<>();
    Enumeration<?> names = buildProperties.keys();
    while (names.hasMoreElements()) {
      String key = (String) names.nextElement();
      params.put(key, buildProperties.getProperty(key));
    }
    new ServerUtils("./");
    ServerContext ctxt = new ServerContext("./", params, null, false, false, null);
    ctxt.getSitesInfo().addTestProperties(SITE_ID_VALUE, params, null);

    // If the siteId in the properties file isn't 9, complain and use 9 anyway
    String s = params.get(Constants.SITE_ID);
    if (s != null && !s.isEmpty()) {
      try {
        Long x = Long.valueOf(s);
        if (x.longValue() != SITE_ID_VALUE) {
          throw new RuntimeException("Site ID in property file ("+x+") differed from expected: "+SITE_ID_VALUE);
        }
      } catch (NumberFormatException nfe) {
        log.error("Error- will use siteId="+SITE_ID_VALUE+" due to error converting '"+s+"' to number from file: "+propFilename);

      }
    }
    log.warn("No siteId property was found in build.properties- using site ID "+SITE_ID_VALUE);
    return ctxt;
  }

  public static void main(String[] args) {
    // initialize using -Dbuild.properties
    final String propertiesFile = System.getProperty("build.properties");

    DatabaseProvider.fromPropertyFile(propertiesFile, "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact(new DbCodeTx() { // this seems to compile with java 1.7, so can't use a Lambda
          public void run(Supplier<Database> dbp, Transaction t) throws Exception {
            ServerContext serverContext = initServerUtilsAndGetSiteIdOf9(propertiesFile);
            SiteInfo siteInfo = serverContext.getSiteInfo(SITE_ID_VALUE);

            // Drop & recreate the rpt_TreatmentHx_Square table
            dbp.get().dropTableQuietly(tableName);
            Schema schema = new Schema().addTable(tableName)
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("assessment_type").asString(50).table()
                .addColumn("survey_scheduled").asDate().table()
                .addColumn("survey_started").asDate().table()
                .addColumn("survey_ended").asDate().table()
                .addColumn("is_complete").asBoolean().table()
                .addColumn("survey_user_time_ms").asLong().table()
                .addColumn("patient_id").asString(50).table()
                .addPrimaryKey(tableName, "survey_site_id", "survey_token_id").table()
                .schema();
            schema.execute(dbp.get());
            int inx = 0;

            // add the columns for each of the xmlQuestionaires[]
            SurveySystem surveySystem = new SurveySystDao(dbp).getSurveySystem("Local");
            for (int x = 0; x < xmlQuestionaires.length; x++) {
              SquareXml squareXml = new SquareXml(dbp.get(), siteInfo, xmlQuestionaires[x], prefix, false);
              LinkedHashMap<String, String> columns = squareXml.getColumns();
              inx = inx + addColumns(dbp.get(), columns);
            }

            // add the columns for the pacPainMeds.xml
            TxSquareXml squareXml = new TxSquareXml(dbp.get(), siteInfo, "pacPainMeds", "PREOP_PMEDS_", false);
            LinkedHashMap<String, String> painMedsColumns = squareXml.getColumns();
            addColumns(dbp.get(), painMedsColumns);

            // add the body regions column
            addColumn(dbp.get(), prefix + "bodymap_regions_csv", dbp.get().flavor().typeStringVar(4000));

            SurveySystDao ssDao = new SurveySystDao(dbp);

            // add painIntensity.xml columns
            PainIntensityToSquare painIntensityAdvance = new PainIntensityToSquare();
            Study painStudy = ssDao.getStudy(surveySystem.getSurveySystemId(), "painIntensity");
            LinkedHashMap<String, edu.stanford.survey.client.api.FieldType> painIntensityColumns =
                painIntensityAdvance.getSquareTableColumns(dbp.get(), painStudy, prefix);
            for (final String columnName : painIntensityColumns.keySet()) {
              dbp.get().ddl(
                  "alter table " + tableName + "  add (" + columnName + " "
                      + SquareUtils.getDatabaseColumnType(dbp.get(), edu.stanford.survey.client.api.FieldType.radios.toString())
                      + ")").execute();
              inx++;
            }

            // This was hard-coded here:  Long siteId = 9L;
            // add the PROMIS scores columns
            SurveyAdvancePromis surveyAdvancePromis = new SurveyAdvancePromis(dbp.get(), siteInfo);
            SurveySystem catSurveySystem = new SurveySystDao(dbp).getSurveySystem("StanfordCat");
            for (String promisQuestionnaire : promisQuestionnaires) {
              Study promisStudy = ssDao.getStudy(catSurveySystem.getSurveySystemId(), promisQuestionnaire);
              dbp.get().ddl(
                  "alter table " + tableName + "  add (" + surveyAdvancePromis.getColumnName(promisStudy, "PREOP_")
                      + " "
                      + SquareUtils.getDatabaseColumnType(dbp.get(), "promis") + ")").execute();
              inx++;
            }

            // add global health columns
            for (String name : ghNames) {
              addColumn(dbp.get(), prefix + name, dbp.get().flavor().typeInteger());
            }
            addColumn(dbp.get(), prefix + "gh_mental_score", dbp.get().flavor().typeBigDecimal(10, 5));
            addColumn(dbp.get(), prefix + "gh_physical_score", dbp.get().flavor().typeBigDecimal(10,5));

            // Reset the handler so it will repopulate the table
            SurveyDao surveyDao = new SurveyDao(dbp.get());

            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteInfo.getSiteId(), "pacStdSquareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteInfo.getSiteId());
              push.setRecipientName("pacStdSquareTable");
              push.setRecipientDisplayName("Populate " + tableName + " table");
              push.setFailedCount(0L);
              push.setEnabled(true);
              surveyDao.insertSurveyAdvancePush(push);
            } else {
              push.setPushedSurveySequence(null);
              push.setLastPushedTime(null);
              surveyDao.updateSurveyAdvancePush(push);
            }
            new ServerUtils(".");
            SurveyAdvanceMonitor monitor = new SurveyAdvanceMonitor(siteInfo.getSiteId(), new SurveyAdvanceHandlerFactoryImpl(siteInfo));
            monitor.pollAndNotify(dbp);
          }
        });
    }
    private static int addColumns(Database database,  LinkedHashMap<String, String> columns) {
      int cnt = 0;
      for (final String columnName : columns.keySet()) {
        String type = columns.get(columnName);
        addColumn(database, columnName, type);
        cnt++;
      }
      return cnt;
    }

  private static void addColumn(Database database, String columnName, String type) {

    database.ddl(
        "alter table " + tableName + "  add (" + columnName + " "
            + SquareUtils.getDatabaseColumnType(database, type) + ")").execute();
  }

}
