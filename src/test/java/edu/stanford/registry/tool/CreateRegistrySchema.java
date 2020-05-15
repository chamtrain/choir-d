/*
 * Copyright 2014-2017 The Board of Trustees of The Leland Stanford Junior University.
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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;

import edu.stanford.registry.server.utils.ClassCreator;

/**
 * Utility for populating a database schema with the various tables and other
 * objects needed to run the registry system.
 *
 * To use your own data, set data.creation.class in your build.properties file.
 * Probably you'll want to subclass CreateRegDataDefault.
 *
 * @author garricko - split apart by rstr
 */
public class CreateRegistrySchema {

  private static void createSchemaAndSite(Supplier<Database> dbp, Logger log) {
    Database db = dbp.get();

    // First, make sure the data creator can be made
    CreateRegData creator = getDataCreator(log, db);  // custom or default
    if (creator == null) {
      log.error("Data creator could not be made. Aborting.");
      return;
    }

    // Create the different parts of the schema
    SurveySchema.create().execute(db);
    UserSchema.create().execute(db);
    RegistrySchema.create().execute(db);

    // Set all the data
    creator.createUsers();
    creator.createSites();
    creator.configSiteParameters();
    creator.configEmails();
    creator.createSurveySystemsAndStudies();
    creator.addSurveyCompletions();
    creator.addPatientResultTypes();
    creator.addOther(); // optional.
  }

  public static void main(String[] args) {
    try {
      boolean verbose = false;
      if (args.length == 1 && args[0].equals("-verbose")) {
        verbose = true;
      }

      // Initialize logging
      String log4jConfig = new File(verbose ? "log4j.xml" : "log4j-nodb.xml").getAbsolutePath();
      DOMConfigurator.configure(log4jConfig);
      Logger log = Logger.getLogger(CreateRegistrySchema.class);
      log.info("Initialized log4j using file: " + log4jConfig);
      log.info("-verbose is "+verbose);

      String url = System.getProperty("database.url");
      if (url == null || url.isEmpty() || url.contains("${")) {
        url = loadBuildDotProperties(log, "database.url");
      }
      String driver = System.getProperty("database.driver");
      String user = System.getProperty("database.user");
      String password = System.getProperty("database.password");

      if (url == null || url.isEmpty()) {
        System.err.println("You must set -Ddatabase.url=...");
        System.exit(1);
      }
      if (url.contains("${")) {
        System.err.println("Bad value for database.url="+url);
        System.exit(1);
      }

      if (driver != null) {
        Class.forName(driver).getConstructor().newInstance();
      }

      // Put all Derby related files inside ./build to keep our working copy clean
      File directory = new File("build").getAbsoluteFile();
      if (directory.exists() || directory.mkdirs()) {
        System.setProperty("derby.stream.error.file", new File(directory, "derby.log").getAbsolutePath());
      }

      Builder dbBuilder = DatabaseProvider.fromDriverManager(url, user, password);

      if (verbose) {
        dbBuilder = dbBuilder.withSqlParameterLogging();
      }

      dbBuilder.transact(dbp -> createSchemaAndSite(dbp, log)); // if there's an exception, this rolls back
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }


  private static String loadBuildDotProperties(Logger log, String keyToReturn) {
    try {
      File file = new File("../build.properties");
      if (!file.exists()) {
        log.info("Can't find properties file to load: " + file.getAbsolutePath());
        return null;
      }
      log.info("Loading properties from ("+file.length()+" bytes): "+file.getAbsolutePath());

      FileInputStream fileInput = new FileInputStream(file);
      Properties properties = new Properties();
      properties.load(fileInput);
      log.info("Read "+properties.size()+" properties");
      fileInput.close();

      Set<String> keys = properties.stringPropertyNames();
      for (String key: keys) {
        String value = properties.getProperty(key);
        if (key.contains("database.")) {
          if (key.startsWith("registry.database"))
            key = key.substring("registry.".length());
          System.setProperty(key, value);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      log.info("Failed trying to read ../build.properties");
    }
    return System.getProperty(keyToReturn);
  }


  private static CreateRegData getDataCreator(Logger logger, Database db) {
    ClassCreator<CreateRegData> classCreator =  new ClassCreator<>("CreateRegistryData", "data creator", logger, Database.class);

    String creatorClassname = System.getProperty("data.creation.class");
    if (creatorClassname == null || "${data.creation.class}".equals(creatorClassname)) {  // use the default
      creatorClassname = "edu.stanford.registry.tool.CreateRegDataDefault";
    }
    CreateRegData dataCreator = classCreator.createClass(creatorClassname, db);
    if (dataCreator == null) {
      logger.error("Failed to create data creator. Exiting");
    } else {
      logger.info("CREATED the data-creation class: "+creatorClassname+"\n\n");
    }

    return dataCreator;
  }
}
