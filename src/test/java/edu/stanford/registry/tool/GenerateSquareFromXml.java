/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.tool;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.config.ServerInit;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.survey.SurveyAdvanceHandlerFactoryImpl;
import edu.stanford.registry.server.utils.SquareUtils;
import edu.stanford.registry.server.utils.SquareXml;
import edu.stanford.registry.shared.DataException;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceHandlerFactory;
import edu.stanford.survey.server.SurveyStep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.DatabaseProvider;


/**
 * Reads and validates *.xml survey files for a survey used by one site and fills
 * a single table with the results.
 *
 * This reads properties from your build.properties file as well as from the database.
 *
 * Your build.property file is read from ../build.properties by default, and can be set
 * in the System property (given to Java on the command line, like -Dname=value)
 *
 * 3 other properties can be set as system properties, or can be in your build.properties
 * file (or your config database): table (the table name),
 * prefix (a single prefix for all column names)
 * and docFile (the name of a single documentation file).
 *
 * If the table isn't specified by a property, we first prompt for the name.
 * If this table doesn't exist, it'll be created.
 * If the table exists (and isn't remade), you'll be asked whether
 * you want to create the columns for each study as well. If the table does not
 * initially exist, columns are always created.
 *
 * You'll be repeatedly prompted for 2-4 items for each study in the survey,
 * until you enter "q" for the study name
 * <br>1. studyName
 * <br>1a.createColumns (y or n) - if the table was created (not found), this is true. If it already existed, you'll be prompted each time.
 * <br>2. prefix - for each study, you can set a different column prefix. If a global prefix property was set, it is used for all studies
 * <br>3. documentation (y or n) - for each study, you can choose to generate documentation.
 *
 * If the property "docFile" is set, if you opt to write documentation, it is always writting to this file.
 * Otherwise the study name is used.  If needed, the extension ".csv" will be appended.

 * @author tpacht
 *
 * April 2017 - updated for postgres
 */
public class GenerateSquareFromXml extends SurveyAdvanceBase {

  private static final String[] yn = {"y", "n"};
  private static final Logger logger = Logger.getLogger(GenerateSquareFromXml.class);


  private final MessageHandler msgs = new MessageHandler();
  private DatabaseProvider dbp = null;

  private boolean tableFound = false;
  private Map<String, String> tableColumns;
  private Options options;
  private SquareXml squareXml;

  private GenerateSquareFromXml(SiteInfo siteInfo) {
    super(siteInfo);

    init(siteInfo);
    msgs.setValid(findTable(options.getTableName()));
    if (!msgs.isValid()) {
      msgs.outputLogs();
      return;  // if table isn't valid, we're done
    }

    // Handle table creation
    String siteId = siteInfo.getIdString();
    logger.info(siteId + "looking for the table " + options.getTableName());
    if (!tableFound) {
      System.out.println(siteId + "Table " + options.getTableName() + " doesn't exist");
      if (options.isCreateTable()) {
        makeTable(options.getTableName());
      }
    } else {
      System.out.println(siteId + "Found table " + options.getTableName());
    }

    // Process studies until quit is indicated
    while (!"q".equals(options.getStudyName())) {
      if (tableFound) {
        options.isCreateColumns(); // ask if we're adding the columns
      }
      options.getDocumentationChoice(); // ask if creating documentation files
      processStudy();
      options.clearStudy();
    }

    dbp.commitAndClose();
  }

  // Gets a site from a string- ignores a null or empty.
  // else dies if it doesn't get a site
  private static SiteInfo getSite(SitesInfo sitesInfo, String site) {
    if (site == null || site.isEmpty())
      return null;

    try {
      Long num = Long.valueOf(site);
      SiteInfo si = sitesInfo.getBySiteId(num);
      if (si != null)
        return si;
      System.err.println("Found no site with siteId="+num);
      System.exit(1);
    } catch (NumberFormatException e) {
      // do nothing, it wasn't a number
    }
    SiteInfo si = sitesInfo.byUrlParam(site);
    if (si != null)
      return si;
    System.err.println("Found no site with site param="+site);
    System.exit(1);
    return null;
  }

  public static void main(String[] args) {
    if (args.length > 0) {
      System.err.println("This program expects zero args, not "+args.length);
      System.err.println("The build.properties file may be specified as a system property: -Dbuild.properties=../build.properties");
      System.err.println("The site, table and (column) prefix may be specified as system properties or in the build.properties file");
      return;
    }

    ServerInit serverInit = ServerInit.initForMain(null, // makes all the sites load in from the database
        "email.template.directory", "src/main/resources/default/email-templates",
        "PatientIdFormat", "d{5,7}-d{1}|d{5,9}",
        "default.dateFormat", "MM/dd/yyyy",
        "default.dateTimeFormat", "MM/dd/yyyy h:mm a");
    SitesInfo sitesInfo = serverInit.getServerContext().getSitesInfo();
    SiteInfo siteInfo = getSite(sitesInfo, System.getProperty("site"));
    if (siteInfo == null)
      siteInfo = getSite(sitesInfo, serverInit.getServerContext().getSitesInfo().getGlobalProperty("site"));
    if (siteInfo == null)
      siteInfo = getSite(sitesInfo, "1");

    System.out.println("Using site: "+siteInfo.getIdString());

    new GenerateSquareFromXml(siteInfo);
  }

  private void init(SiteInfo siteInfo) {
    options = new Options(siteInfo);
    try {
      this.dbp = getDb();
    } catch (Exception ex) {
      logger.error(ex);
      ex.printStackTrace();
    }
  }


  private void processStudy() {

    try {
      msgs.reset();
      String studyName = options.getStudyName();

      tableColumns = SquareUtils.getTableColumns(dbp.get(), options.getTableName());
      squareXml = new SquareXml(dbp.get(), siteInfo, studyName, options.getPrefix(), options.getDocumentationChoice());
      msgs.setValid(squareXml.isValid());
      msgs.addErrors(squareXml.getProblemLog());

      msgs.infoMessage("XML Validation: " + studyName + ".xml File is "+(msgs.isValid()?"":"NOT ")+"valid");

      if (msgs.isValid() && tableFound) {
        addColumns(options.isCreateColumns(), options.getTableName());
      }

      if (msgs.isValid()) {
        msgs.infoMessage("SUCCESS - study is valid");

        if (options.getDocumentationChoice()) {
          try {
            String docFileName = options.getDocFileName();
            File documentationFile = new File(docFileName);
            boolean fileIsNew = true;
            if (documentationFile.exists()) {
              fileIsNew = false;
            }
            msgs.infoMessage("Documentation has been written to file: " + documentationFile.getAbsolutePath());
            FileOutputStream out = new FileOutputStream(documentationFile, true);
            BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            String title =
                dbp.get().toSelect("select title FROM study WHERE study_description = ?").argString(options.getStudyName()).query(
                    rs -> ((rs.next()) ? rs.getStringOrEmpty() : options.getStudyName()));

            if (!fileIsNew) {
              buf.newLine(); // add a space after the existing lines
            }
            buf.write(title);
            buf.newLine();
            if (fileIsNew) { // put a heading at the very top
              buf.write("Question, Field, Type, Response, Value");
              buf.newLine();
            }
            for (String line : squareXml.getDocumentationLog()) {
              buf.write(line);
              buf.newLine();
            }
            buf.flush();
            buf.close();
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }

        }
        msgs.outputLogs();
      }

      if (msgs.isValid() && tableFound) {  // why tableFound?
        try {
          testStudy();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      if (!msgs.isValid())
        msgs.errorMessage("FAILURE of study: "+studyName);

    } catch (DataException de) {
      logger.error(de.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      msgs.outputLogs();
    }
  }

  private void testStudy() throws InstantiationException {

    String surveyTokenId = options.getTestTokenId();
    for (; !"q".equals(surveyTokenId); surveyTokenId = options.getTestTokenId()) {
      Long tokenId;
      try {
        tokenId = Long.valueOf(surveyTokenId);
      } catch (NumberFormatException nfe) {
        System.out.println(surveyTokenId + " is not a valid number/surveyTokenId!");
        continue;
      }

      // validate each token separately, so there's a better error statement to output
      //    rather than just a debug statement (now a warning), e.g.:
      // surveyRegId not found for siteId 6 token_id 74009995
      Long value = validateOrFixTokenIdForSite(tokenId, siteInfo.getSiteId());
      if (value == null)
        continue;  // the above already output errors
      test(value);
    }

    logger.debug("done");
  }

  /**
   * Validate the tokenId, else it fails pretty silently, hidden from user.
   * Plus, compensate for a newbie error - using a Token instead of a TokenId.
   * @return tokenId if it's good, realTokenId if it was a token instead, null if it was just bad
   */
  private Long validateOrFixTokenIdForSite(Long tokenId, Long siteId) {
    Long gotSite = tokenIdForWhatSite(tokenId, siteId);
    if (siteId.equals(gotSite))
      return tokenId;  // it's a valid token for this site

    if (gotSite != null) {
      logger.error(tokenId+" is a token id for a survey in site "+gotSite+" not "+siteId);
      return null;
    }

    logger.warn(tokenId+" is not an active survey token");

    return dbp.get().toSelect("select survey_token_id, survey_site_id from survey_token where survey_token = ?")
        .argString("" + tokenId)
        .query(rs -> {
          Long site; // none
          while (rs.next()) {
            Long tokenId1 = rs.getLongOrNull();
            site = rs.getLongOrNull();
            if (siteId.equals(site)) {
              logger.warn("For site " + siteId + ", " + tokenId1 + " was the token for tokenId=" + tokenId1
                  + ". Will use that instead.");
              return tokenId1; // return sought site, if found
            } else {
              logger.warn(tokenId1 + " is a token (not id), for site: " + site);
            }
          }
          return null; // no real tokenId was found
        });
  }

  private Long tokenIdForWhatSite(Long tokenId, Long siteId) {
    return dbp.get().toSelect("select survey_site_id from survey_token where survey_token_id = ?")
        .argLong(tokenId)
        .query(rs -> {
          Long site = null; // none
          while (rs.next()) {
            site = rs.getLongOrNull();
            if (siteId.equals(site))
              return siteId; // return sought site, if found
          }
          return site; // return null or a found site
        });
  }

  private boolean findTable(String tableName) {

    if (tableName.length() > 27) {
      msgs.errorMessage("Tablename: " + tableName + " is not valid.");
      msgs.errorMessage("Tablenames cannot be longer than 27 characters.");
      return false;
    } else {
      boolean found = SquareUtils.findTable(dbp, tableName);
      if (found) {
        tableFound = true;
      }
      return true;
    }
  }

  private boolean hasColumn(String tableName, String xmlColumn) {
    if (tableColumns.containsKey(xmlColumn)) {
      msgs.errorMessage(tableName + " already has column named: "+xmlColumn);
      return true;
    }

    return false;
  }

  private void makeTable(String tableName) {
    SquareUtils.makeTable(dbp, tableName);
    msgs.infoMessage("Created table " + tableName);
    tableFound = true;
  }

  private void addColumns(boolean update, String tableName) {
    LinkedHashMap<String, String> columns = squareXml.getColumns();
    LinkedHashMap<String, String> references = squareXml.getReferences();
    Object refKeys[] = references.keySet().toArray();
    int inx = 0;
    System.out.println("Existing columns: "+String.join(", ", columns.keySet()));
    System.out.println("New Refs: "+String.join(", ", references.keySet()));
    for (String columnName : columns.keySet()) {
      String type = columns.get(columnName);
      String ref = refKeys[inx].toString();
      if (update && !hasColumn(tableName, columnName)) {
        SquareUtils.makeColumn(dbp.get(), tableName, options.getPrefix() + ref, type);
      }
      msgs.infoMessage("alter table " + options.getTableName() + " add " + columnName + " " + SquareUtils.getDatabaseColumnType ( dbp.get(), type ) + "; -- type: " + type + " column for ref: " + ref + " " + references.get(ref) );
      inx++;
    }
  }


  private DatabaseProvider getDb() {
    return DatabaseProvider.fromPropertyFileOrSystemProperties(
        System.getProperty("build.properties", "../build.properties"), "registry.")
        .withSqlParameterLogging().withTransactionControl().create();
  }

  private void test(Long tokenId) throws InstantiationException {
    String advanceHandlerFactoryClass = siteInfo.getProperty("factory.survey.advance");
    logger.debug("handler is class " + advanceHandlerFactoryClass);
    SurveyAdvanceHandlerFactory advanceHandlerFactory;
    if (advanceHandlerFactoryClass == null) {
      advanceHandlerFactory = new SurveyAdvanceHandlerFactoryImpl(siteInfo);
    } else {
      try {
        Class<DatabaseProvider> dbpClass = DatabaseProvider.class;
        Class<SiteInfo> siClass = SiteInfo.class;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        @SuppressWarnings("unchecked") // if this generates exception, probably the class name is incorrect
            Class<SurveyAdvanceHandlerFactory> factoryClass = (Class<SurveyAdvanceHandlerFactory>) loader.loadClass(advanceHandlerFactoryClass);

        try {
          // If this generates an exception, probably the constructor needs to change to accept the two parameters
          Constructor<SurveyAdvanceHandlerFactory> factory = factoryClass.getConstructor(dbpClass, siClass);
          advanceHandlerFactory = factory.newInstance(dbp, siteInfo);
        } catch (NoSuchMethodException | InvocationTargetException | SecurityException e) {
          // If this generates an exception, probably the constructor needs to change to accept (dbp,si) or no params
          Constructor<SurveyAdvanceHandlerFactory> factory = factoryClass.getConstructor(); // 1.0 had no parameters
          advanceHandlerFactory = factory.newInstance();
        }
      } catch (Exception e) {
        throw new InstantiationException(e.getMessage() + " was: " + e.getClass().getName());
      }
    }

    AppConfigEntry parameters = SquareUtils.getConfig(dbp.get(), siteInfo, options.getStudyName());
    Long appConfigId;
    if (parameters == null) {
      parameters = SquareUtils.addConfig(dbp.get(), siteInfo, options.getTableName(), options.getStudyName(), options.getPrefix());
      if (parameters == null) {
        appConfigId = getGenerateXmlConfigId(options.getStudyName());
      } else {
        appConfigId = parameters.getAppConfigId();
      }
      logger.debug("Added app_config entry for study. Use SURVEY_ADVANCE_PUSH.RECIPIENT_NAME " +
          "generateFromXml[" + appConfigId + "]");
    } else {
      appConfigId = parameters.getAppConfigId();
    }


    SurveyAdvanceHandler advanceHandler = advanceHandlerFactory.handlerForName("generateFromXml[" + appConfigId + "]");
    SurveyAdvance sa = new SurveyAdvance();
    sa.setAdvanceSequence(1L);
    sa.setSurveySiteId(siteInfo.getSiteId());
    sa.setSurveyTokenId(tokenId);
    advanceHandler.surveyAdvanced(sa, dbp.get());
  }

  /**
   * Collects info and error messages - any error sets valid = false
   *
   * This is initially used for setting up the table,
   * then reset and used for each study
   */
  static class MessageHandler {
    private boolean valid = true;
    private final ArrayList<String> infoLog = new ArrayList<>();
    private final ArrayList<String> errLog = new ArrayList<>();

    // Reset for each study
    void reset() {
      valid = true;
      infoLog.clear();
      errLog.clear();
    }

    private void errorMessage(String message) {
      errLog.add("\t" + message);
      valid = false;
    }

    void addErrors(ArrayList<String> errs) {
      if (errs != null)
        for (String problem: errs)
          errorMessage(problem);
    }

    private void infoMessage(String message) {
      infoLog.add("\t" + message);
    }

    private void outputLogs() {
      for (String output : infoLog)
        System.out.println("\t\t " + output);
      System.out.flush();
      infoLog.clear();
      pause();

      for (String output : errLog)
        System.err.println("\t\t " + output);
      System.err.flush();
      pause();
      infoLog.clear();
    }

    boolean isValid() {
      return valid;
    }

    // Just called at the start of a study
    void setValid(boolean isValid) {
      valid = isValid;
    }

    void pause() {
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } // give System.out or err time to flush
    }
  }


  @Override
  public String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
  }

  private Long getGenerateXmlConfigId (String configName) {
    dbp.get().toSelect(
        "select app_config_id from app_config where config_type = 'squaretable' and config_name = ? ")
        .argString(configName).query(
        rs -> {
          if (rs.next()) {
            return rs.getLongOrNull(1);
          }
          return null;
        });
    return null;
  }

  /**
   * A class to supply the information needed, a bit from the properties (table for the table name
   * and "prefix" for a global prefix for all columns) and most from user input.
   */
  static private class Options {
    Boolean createTable;
    String tableName; // prompted for once if "table" property is not set
    String globalPrefix;
    String globalDocFileName;

    // You'll be repeatedly prompted for these 3 or 4 items until you enter "q" for the study name
    String studyName = null; // prompted for repeatedly
    Boolean createColumns;   // if table was created (not found), this is true. If found, you'll be prompted
    String prefix = null;    // for each study, you can set a different column prefix
    Boolean documentation;

    Options(SiteInfo siteInfo) {
      // We only get these properties from the System, or from your build.properties (or database properties)
      globalPrefix = System.getProperty("prefix");
      if (globalPrefix == null)
        globalPrefix = siteInfo.getProperty("prefix");

      tableName = System.getProperty("table");
      if (tableName == null)
        tableName = siteInfo.getProperty("table");

      globalDocFileName = System.getProperty("docFile");
      if (globalDocFileName == null)
        globalDocFileName = siteInfo.getProperty("docFile");
      if (globalDocFileName != null)
        globalDocFileName = ensureEndsInCsv(globalDocFileName);
    }

    String getDocFileName() {
      if (globalDocFileName != null)
        return globalDocFileName;

      return ensureEndsInCsv(getStudyName());
    }

    String ensureEndsInCsv(String name) {
      if (!name.endsWith(".csv"))
        return name + ".csv";

      return name;
    }

    boolean getDocumentationChoice() {
      if (documentation == null) {
        documentation = getBooleanResponse("Do you want to generate documentation (y/n)?");
      }
      return documentation;
    }

    String getTableName() {
      if (tableName == null || tableName.isEmpty()) {
        tableName = SquareUtils.getResponse("What is the name of the square table?", true, null);
      }
      return tableName;
    }

    boolean isCreateTable() {
      if (createTable == null) {
        createTable = getBooleanResponse("Do you want to create it now(y/n)?");
      }
      if (!createTable) {
        createColumns = false;
      }
      return createTable;
    }

    boolean isCreateColumns() {
      if (createColumns == null) {
        createColumns = getBooleanResponse("Do you want to create the columns (y/n)?");
      }
      return createColumns;
    }

    boolean getBooleanResponse(String question) {
      String answer =  SquareUtils.getResponse(question, true, yn);
      return "y".equals(answer);
    }

    String getStudyName() {

      if (studyName == null || studyName.isEmpty()) {
        studyName = SquareUtils.getResponse("Enter a study name to process or q to quit ?", true, null);
      }
      return studyName;
    }

    private void clearStudy() {
      studyName = null;
      prefix = null;
      createColumns = null;
      documentation = null;
    }

    String getPrefix() {
      if (prefix == null) {
        prefix = globalPrefix;
      }
      while (prefix == null || prefix.isEmpty()) {
        prefix = SquareUtils.getResponse("What prefix should we use for the column names?", true, null);
      }
      if (lastChar(prefix) != '_') // or do we want to allow two under bars?
        prefix += '_';

      return prefix.toUpperCase();
    }

    char lastChar(String s) {
      if (s == null || s.isEmpty())
        return 0;
      return s.charAt(s.length() - 1);
    }

    //public String getTestSite() {
    //  return SquareUtils.getResponse("Enter the siteId (site number)", true, sites);
    //}

    String getTestTokenId() {
      return SquareUtils.getResponse("Enter a surveyTokenId to test adding a row to the table or enter 'q' to quit", false);
    }

  }


}
