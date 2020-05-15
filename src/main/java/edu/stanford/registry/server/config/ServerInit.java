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

package edu.stanford.registry.server.config;

import edu.stanford.registry.server.Log4jContextListener;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.service.BatchContext;
import edu.stanford.registry.server.service.ConnectionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Config;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.Flavor;
import com.github.susom.database.OptionsOverride;

/**
 * ServiceFilter calls ServerInit.getInstance() to init logging, the database and
 * ServerUtils, and as a side-effect, launch the poller to periodically update
 * the ServerContext (user,appConfig,sitesInfo), and run the
 * BatchContext to complete surveys and Import any new patients and appointments.
 */
public class ServerInit {
  static Logger logger;
  static ServerInit instance;

  Builder databaseBuilder;
  ServerContext serverContext;
  BatchContext batchContext;
  BackgroundThread poller;
  boolean errorDuringInit = false; // turn off filter if there's an error
  final String contextPath;
  final String realPath;


  public Builder getDatabaseBuilder() {
    return databaseBuilder;
  }

  public ServerContext getServerContext() {
    return serverContext;
  }

  public void destroy() {
    if (poller != null) {
      poller.destroy();
    }
  }

  public static ServerInit getInstance() {
    if (instance == null) {
      throw new RuntimeException("ServerInit was never initialized");
    }

    return instance;
  }

  /**
   * Creates the initialized server for ServiceFilter which stores it and destroys it.
   */
  public static synchronized ServerInit getInstance(ServletContext servletContext) throws ServletException {
    if (instance != null) {
      return instance;
    }
    instance = new ServerInit(servletContext);
    return instance;
  }


  /**
   * Initializes the system for main() methods. Note that the background process is disabled
   * (the one that refreshes the configuration, imports appointments, and advances and completes
   * surveys.)  If you're on a production machine, of course, it can be running its own copy
   * of this background thread.
   *
   * Two system properties are read, "build.properties" and "registryHome".
   * If the first is set, this property file is loaded.  If it's not specified,
   * "../build.properties" is loaded, if it exists.  If registryHome is not set, it's value
   * is taken from the property file.  If it's not in there, the current directory is used.
   *
   * Note: System properties are set using the Java command line.  For instance, to use the file
   * foo.properties, add to the command line: -Dbuild.properties=foo.properties
   *
   * Note the values from this file can be overwritten with System properties of the same name.
   * To say that again: Except for the build.properties filename and the registryHome folder,
   * to specify any property on the command line, it must override a property in the properties file.
   *
   * Note that System properties containing variables are ignored.  Variables are specified
   * by syntax like: ${x} or $(x).  Thus, System values containing "${" or "$(" are ignored.
   *
   * @param nameValuePairs These are property names and values listed as parameters.
   * These are given precedence. After the properties file is read,
   * these are simply added to the parameters, overwriting a property that came from the file,
   * if one with the same name was set.
   *
   * @param siteId If null, the sites and configuration parameters are read from the database.
   * (The parameters above are all global parameters, and won't be overwritten.)
   * If this site parameter is not null, the sites and configuration will NOT be read from the
   * database, and this site will be available in the serverInit.serverContext.getSitesInfo(),
   * along with all the configuration parameters.
   */
  public static synchronized ServerInit initForMain(Long siteId, String...nameValuePairs) {
    logger = LoggerFactory.getLogger(ServerInit.class);
    if ((nameValuePairs.length & 1) == 1)
      throw new RuntimeException("You must pass an even number of strings as name/value pairs, not: "
                                 + nameValuePairs.length);

    PropertyMap props = collectProperties(nameValuePairs);

    String applicationHome = props.getString("registryHome");
    if (applicationHome == null)
      applicationHome = "./";

    try {  // use the ServerInit for main methods
      instance = new ServerInit(siteId, props, applicationHome, applicationHome);
    } catch (ServletException e) {
      throw new RuntimeException("Error initializing server for main()", e);
    }
    return instance;
  }

  static String propFileName;

  static PropertyMap collectProperties(String[] nameValuePairs) {
    Properties buildProperties = new Properties();
    propFileName = System.getProperty("build.properties", "../build.properties");
    File propFile = new File(propFileName);
    if (!propFile.exists() && !propFile.isDirectory()) {
      String msg = "You must either have a ../build.properties file, or give to Java -Dbuild.properties=<filename>" +
          " - at least the database properties must come from that file.";
      logger.error(msg);
      throw new RuntimeException(msg);
    }
    logger.debug("Reading properties from: "+propFile);
    try {
      FileInputStream is = new FileInputStream(propFile);
      buildProperties.load(is);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    HashMap<String,String> params = new HashMap<String,String>(30);

    // Put into params, the properties from build.properties
    Enumeration<?> names = buildProperties.keys();
    while (names.hasMoreElements()) {
      String key = (String) names.nextElement();
      String value = System.getProperty(key);

      if (value == null || value.contains("${") || value.contains("$(")) {
        value = buildProperties.getProperty(key);
      }

      logger.debug("Initial parameters: "+key+" = "+value);
      params.put(key, value);
    }

    // Get registryHome, first from System, then from file, with the current dir as default
    String homeProp = "registryHome";
    if (System.getProperty(homeProp) != null) {
      params.put(homeProp, System.getProperty(homeProp));
    }

    // Now add the name/value pairs
    for (int i = 1;  i < nameValuePairs.length;  i+=2) {
      params.put(nameValuePairs[i-1], nameValuePairs[i]);
    }

    String jndiKey = "registry.jndi.datasource";
    String dataSource = params.get(jndiKey);
    if (dataSource == null || dataSource.isEmpty()) {
      params.put(jndiKey, "java:");
    }

    return new PropertyMapFromHash(params);
  }


  /**
   * For tests
   * @param databaseBuilder
   * @param serverContext
   * @param contextPath  "test" or the specific test
   */
  public static ServerInit getInstance(Builder databaseBuilder, ServerContext serverContext, String contextPath, String realPath) {
    if (instance != null) {
      return instance;
    }

    instance = new ServerInit(databaseBuilder, serverContext, contextPath, realPath);
    return instance;
  }

  /**
   * For tests
   * @param databaseBuilder
   * @param serverContext
   * @param contextPath  "test" or the specific test
   */
  public ServerInit(Builder databaseBuilder, ServerContext serverContext, String contextPath, String realPath) {
    this.contextPath = contextPath;
    this.realPath = realPath;
    logger = LoggerFactory.getLogger(ServerInit.class);
    this.databaseBuilder = databaseBuilder;
    this.serverContext = serverContext;
  }


  /**
   * Called by ServiceFilter
   *
   * Creates the initialized server for ServiceFilter- the logger, database
   * and initial users, sites and properties.
   *
   * If you fake (subclass) the serverContext, you just need to implement
   * <br>log(String)
   * <br>String getContextPath()
   * <br>String getRealPath()
   * <br>Enumeration<String> e = servletContext.getInitParameterNames()
   * <br>String getInitParameter(String key)
   *
   * To allow JUST the dev user, set registry.devmode.user to the dev user name.
   * <br>If this isn't set, eliminate all user by setting registry.load.users = N
   *
   * To only load in the static properties, set registry.load.config = N
   * Otherwise they're loaded and refreshed every cache.reload.seconds
   * <br>If the cache isn't loaded, no sites are known.
   *
   * Regardless of other settings, if (registry.polling = N), no background thread is launched
   * to update caches (user, config), look for import files or update surveys
   *
   * @param servletContext
   * @throws ServletException
   */
  protected ServerInit(ServletContext servletContext) throws ServletException {
    String originalName = Thread.currentThread().getName();
    Thread.currentThread().setName("ServerInit");

    contextPath = servletContext.getContextPath();
    realPath = servletContext.getRealPath("./");
    logger = setupLogger(servletContext);
    if (logger == null) {
      return;
    }

    logger.info("ServerInit: logger created");

    Config config = Config.from()
        .systemProperties()
        .propertyFile(System.getProperty("properties", "").split(","))
        .custom(servletContext::getInitParameter).get();

    HashMap<String, String> paramMap = new HashMap();
    Enumeration<String> e = servletContext.getInitParameterNames();
    while (e.hasMoreElements()) {
      String key = e.nextElement();
      if (config.getString(key) != null) {
        paramMap.put(key, config.getString(key));
      } else if (servletContext.getInitParameter(key) != null) {
        paramMap.put(key, servletContext.getInitParameter(key));
      }
    }
    PropertyMap initParams = new PropertyMapFromHash(paramMap);
    logger.info("ServerInit: initParams created, size={}", initParams.size());
    databaseBuilder = setupDatabase(initParams);
    if (databaseBuilder == null) {
      return;
    }

    logger.info("ServerInit: database created");
    // First half of this initialization must be done first...
    ServerUtils.initialize(realPath);

    boolean loadUsers = initParams.getProperty("registry.load.users", true);
    boolean loadConfig = initParams.getProperty("registry.load.config", true);
    setupServerContext(initParams, databaseBuilder, loadUsers, loadConfig); // always do an initial load
    logger.info("ServerInit: serverContext created");

    boolean polling = serverContext.getSitesInfo().getGlobalProperty("registry.polling", true);
    if (polling) {
      setupBatchContext(serverContext, initParams, databaseBuilder);
      poller = new BackgroundThread(serverContext, batchContext);
    } else {
      logger.info("polling with the PollingThread is disabled");
    }

    // As soon as the system is initialized, check that this works- some OpenJDK versions had problems
    ChartMaker.initializeUiManagerDefaultFontFamily();
    Thread.currentThread().setName(originalName);
  }

  /**
   * For main methods
   * @param initialProps
   * @param contextPath
   * @param realPath
   * @throws ServletException
   */
  public ServerInit(Long siteId, PropertyMap initialProps, String contextPath, String realPath)
      throws ServletException {

    this.contextPath = contextPath;
    this.realPath = realPath;

    logger = LoggerFactory.getLogger(this.getClass());
    if (logger == null) {
      return;
    }

    databaseBuilder = DatabaseProvider.fromPropertyFileOrSystemProperties(propFileName, "registry.");

    if (databaseBuilder == null) {
      return;
    }

    // First half of this initialization must be done first...
    ServerUtils.initialize(realPath);

    if (siteId == null) {
      setupServerContext(initialProps, databaseBuilder, true, true); // do an initial load
    } else {
      setupServerContext(initialProps, databaseBuilder, false, false);
      SitesInfo sitesInfo = serverContext.getSitesInfo();
      sitesInfo.addTestProperties(siteId, initialProps.getMap(), null); // okay to use these again
    }
  }


  /**
   * If this fails, it'll return null
   * @return
   */
  Logger setupLogger(ServletContext servletContext) {
    boolean wasAlreadyInitted = false;
    if (Log4jContextListener.get() == null) {
      servletContext.log("ServerInit(servletContext="+contextPath+"");
      new Log4jContextListener().contextInitialized(servletContext);
    } else {
      wasAlreadyInitted = true;
      servletContext.log("ERROR: ServerInit.setupLogger("+contextPath+") - was already initialized!");
    }

    Logger logr = LoggerFactory.getLogger(ServerInit.class);
    if (logr == null) {
      errorDuringInit = true;
      servletContext.log("ServiceFilter.init failed to init logger- app can not run");
      throw new RuntimeException("ServiceFilter.init failed to init logger- app can not run");
    }
    if (wasAlreadyInitted) {
      logr.warn("ServerInit.setupLogger() - was already initialized!");
    } else {
      logr.info("ServerInit.setupLogger() succeeded");
    }
    return logr;
  }


  Builder setupDatabase(PropertyMap initParams) throws ServletException {
    String dataSourceKey = initParams.getString("registry.jndi.datasource");
    try {
      Builder builder = setupDbBuilder(initParams, dataSourceKey);
      if (builder == null) {
        return null;
      }

      DatabaseProvider dbp = builder.create();
      if (dbp != null) {
        logger.info("ServerInit.setupDatabase() succeeded");
        return builder;
      }

      logger.error("Failed to create the DatabaseProvider from the builder");
      return null;
    } catch (Throwable t) {
      if (dataSourceKey != null && dataSourceKey.contains("patsat")) {
        t = null;  // no need to show stack trace for common patsat error;
      }
      logger.error("Could not initialize ServiceFilter for "+dataSourceKey, t);

      errorDuringInit = true; // letting a random error through could kill init of everything!
      return null;
    }
  }

  Builder setupDbBuilder(PropertyMap initParams, String dataSourceKey) throws ServletException {
    Context jndi = getJndi(dataSourceKey); // can throw ServletException
    if (jndi == null) {
      return null;
    }

    String dataSourceFlavorProp = "registry.jndi.datasource.flavor";
    String dataSourceFlavorValue = initParams.getString(dataSourceFlavorProp);
    Flavor dataSourceFlavor = null;
    try {
      if (dataSourceFlavorValue != null) {
        dataSourceFlavor = Flavor.valueOf(dataSourceFlavorValue);
      }
    } catch (Exception e) {
      logger.error("Property " + dataSourceFlavorProp + " is not valid ("+dataSourceFlavorValue+
          "), valid values: " + Arrays.asList(Flavor.values()), e);
    }
    if (dataSourceFlavor == null) {
      dataSourceFlavor = Flavor.oracle;
      logger.warn("Treating JNDI datasource as 'oracle' flavor. You should set property " + dataSourceFlavorProp
          + " to one of " + Arrays.asList(Flavor.values()));
    } else {
      logger.debug("Treating JNDI datasource as flavor " + dataSourceFlavor);
    }
    // Configure access to DataSource from JNDI
    // Allowing transaction control for now because EmailMonitor uses it from the services
    Builder builder = DatabaseProvider.fromJndi(jndi, dataSourceKey, dataSourceFlavor).withTransactionControl();
    if (builder == null) {
      logger.warn("No database builder was found with key '"+dataSourceKey+"', flavor: "+dataSourceFlavor);
      return null;
    }
    if (initParams.getProperty("registry.sql.log.parameters", false)) {
      builder = builder.withSqlParameterLogging();
    }
    if (initParams.getProperty("registry.sql.in.exception.messages", false)) {
      builder = builder.withSqlInExceptionMessages();
    }

    // In order to execute TRIS code from inside registry surveys, we allow access
    // to the underlying SQL Connection so we can create TRIS Database from it. We
    // can remove this once TRIS switches to com.github.susom.database classes.
    if (initParams.getProperty("registry.db.allow.connection.access", false))
      builder = builder.withOptions(new OptionsOverride() {
        @Override
        public boolean allowConnectionAccess() {
          return true;
        }
      });
    return builder;
  }

  Context getJndi(String dataSourceKey) throws ServletException {
    if (dataSourceKey == null) {
      dataSourceKey = "java:/jdbc/registryJndi";
      logger.info("datasource is null setting it to "+dataSourceKey);
    }
    logger.debug("ServerInit: Using JNDI datasource " + dataSourceKey);
    /* Get the initial context */
    Context jndi = null;
    try {
      jndi = new InitialContext();
      logger.info("ServiceFilter got new initialcontext from " + System.getProperty("java.naming.factory.initial"));
    } catch (NamingException e) {
      throw new ServletException("ServiceFilter: Unable to access JNDI", e);
    }

    try {
      logger.info("jndi name is " + jndi.getNameInNamespace());
      ConnectionProvider.getDataSource(jndi, dataSourceKey);
    } catch (Exception e) {
      logger.error("FATAL: Services disabled because there is no DataSource in JNDI at " + dataSourceKey, e);
      jndi = null;
    }
    return jndi;
  }

  /**
   * Sets up the ServerContext which is passed to most services.
   * @param initParams - used to initialize appConfig globals (siteId==0)
   * @param loadUsers - to disable loading any users, pass false.
   */
  void setupServerContext(PropertyMap initParams, Builder dbBuilder, boolean loadUsers, boolean loadConfig) throws ServletException {
    serverContext = new ServerContext(contextPath, initParams, dbBuilder, loadUsers, loadConfig, null); // no restrictServices
    if (loadConfig && serverContext.getSitesInfo().getNumberOfSites() == 0) {
      throw new ServletException("No sites are configured in the database- there must be at least one.");
    }
    SitesInfo sitesInfo = serverContext.getSitesInfo();

    logger.info("ServerInit.setupServerContext() succeeded, number of sites: "+sitesInfo.getNumberOfSites());

    // The ServerContext loads the globals
    serverContext.setSurveySystemFactoryClass(sitesInfo.getGlobalProperty("factory.survey.system"));
  }

  /**
   * Sets the serverContext and batchContext instance variables.
   * @param serverContext2
   */
  void setupBatchContext(ServerContext serverContext2, PropertyMap initParams, Builder dbBuilder) {
    SitesInfo sitesInfo = serverContext.getSitesInfo();

    batchContext = new BatchContext(dbBuilder, serverContext, sitesInfo);
    logger.info("ServerInit.setupBatchContext() succeeded");
  }
}
