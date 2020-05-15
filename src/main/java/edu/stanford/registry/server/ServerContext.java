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

package edu.stanford.registry.server;

import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.config.PropertyMap;
import edu.stanford.registry.server.config.PropertyMapFromHash;
import edu.stanford.registry.server.config.PropertyMapFromServletContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.security.UserInfo;
import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.server.service.ServiceLogin;
import edu.stanford.registry.server.service.TypedProvider;
import edu.stanford.registry.server.service.tasks.Timer;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.shared.Log;
import edu.stanford.registry.shared.ProcessInfo;
import edu.stanford.survey.server.SurveySystemFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;

public final class ServerContext {
  private static final Logger logger = Logger.getLogger(ServerContext.class);

  private DatabaseProvider.Builder db;
  private edu.stanford.registry.server.service.TypedProvider<Log> logProvider;
  private Set<Service> restrictServices;
  private Map<String, ProcessInfo> processes = new LinkedHashMap<>();
  private final Object processesLock = new Object();
  private String surveySystemFactoryClass;
  private UserInfo userInfo;
  private AppConfig appConfig;
  private SitesInfo sitesInfo;
  private final String contextPath;


  /**
   * Cakked by AdministrativeServicesTest
   */
  public ServerContext(ServletContext servletContext, DatabaseProvider.Builder db,
                       boolean loadUsers, boolean loadConfig,
                       Set<Service> restrictServices) {
    this(servletContext.getContextPath(), new PropertyMapFromServletContext(servletContext),
        db, loadUsers, loadConfig, restrictServices);
  }


  /**
   * Called by main methods/tests in LoadTestData and SquareTable programs
   */
  public ServerContext(Supplier<Database> dbp) throws Exception {
    contextPath = "./";
    initForMainMethods(dbp);
  }


  /**
   * Called by main methods/tests: PreAnestesiaAdvanceHandler and DatabaseTestCase
   */
  public ServerContext(String contextPath, Map<String,String>map, DatabaseProvider.Builder db,
                       boolean loadUsers, boolean loadConfig, Set<Service> restrictServices) {
    this(contextPath, new PropertyMapFromHash(map), db, loadUsers, loadConfig, restrictServices);
  }


  /**
   * The constructor for the web service, called by ServerInit
   */
  public ServerContext(String contextPath, PropertyMap initParams, DatabaseProvider.Builder db,
                       boolean loadUsers, boolean loadConfig, Set<Service> restrictServices) {
    this.contextPath = contextPath;
    this.db = db;
    this.restrictServices = restrictServices;
    this.logProvider = new TypedProvider<Log>() {
      @Override
      public Log get(Class<?> forType) {
        return Log4jLog.get(forType);
      }
    };

    initialize(initParams, loadUsers, loadConfig);

    // Initializes the email templates
    if (loadConfig) {
      db.transact((dbp,tx) -> {
        tx.setRollbackOnError(false); // defensive: commit partial results if an exception is thrown
        initEmailTemplates(dbp);
      });
    }
  }


  void initEmailTemplates(Supplier<Database> dbp) {
    // dbp can be null during tests. initTemplateUtilsForSites can handle a null
    Database database = (dbp == null) ? null : dbp.get();
    boolean needsReload = EmailTemplateUtils.initTemplateUtilsForSites(database, sitesInfo);
    if (!needsReload) {
      return;
    }

    appConfig.refresh(dbp.get());
    sitesInfo.refresh(dbp);
    // Don't pass in the database so it doesn't load from files if no DB templates are found.
    EmailTemplateUtils.initTemplateUtilsForSites(null, sitesInfo); // load only from config
  }


  public void setSurveySystemFactoryClass(String factoryClass) {
    surveySystemFactoryClass = factoryClass;
  }


  static final String DEFAULT_SSFACTORY = "edu.stanford.registry.server.survey.SurveySystemFactoryImpl";
  public SurveySystemFactory createSurveySystemFactory(Supplier<Database> db, AppConfig config, SitesInfo sitesInfo) {
    if (surveySystemFactoryClass == null) {
      surveySystemFactoryClass = DEFAULT_SSFACTORY;
    }

    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    Class<?> factoryClass;
    try {
      factoryClass = contextClassLoader.loadClass(surveySystemFactoryClass);
    } catch (Exception e) {
      throw new ServerException("Unable to load class: factory.survey.system=" + surveySystemFactoryClass, e);
    }

    Constructor<?> constructor;
    try {
      constructor = factoryClass.getConstructor(Supplier.class, AppConfig.class, SitesInfo.class);
    } catch (Exception e) {
      throw new ServerException("Unable to find constructor for: factory.survey.system=" + surveySystemFactoryClass, e);
    }

    SurveySystemFactory ssf;
    try {
      ssf = (SurveySystemFactory)constructor.newInstance(db, config, sitesInfo);
    } catch (Exception e) {
      throw new ServerException("Unable to create instance of: factory.survey.system=" + surveySystemFactoryClass, e);
    }

    String plus = (DEFAULT_SSFACTORY.equals(surveySystemFactoryClass)) ? " (default): " : ": ";
    logger.debug("Created SurveySystemFactory " + plus + surveySystemFactoryClass);
    return ssf;
  }


  /**
   * No UserInfo is created
   */
  private void initForMainMethods(Supplier<Database> dbp) throws Exception {
    PropertyMap initParams = new PropertyMapFromHash(null);
    logger.debug("ServerContext.initialize: Creating empty appConfig object");
    appConfig = new AppConfig(initParams);
    logger.debug("ServerContext.initialize: Creating empty sitesInfo object");
    sitesInfo = new SitesInfo(contextPath, true, initParams.getMap(), appConfig);
    
    appConfig.refresh(dbp);
    sitesInfo.refresh(dbp);
  }

  private void initialize(PropertyMap initParams, final boolean loadUsers, final boolean loadConfig) {
    // These are created just once, empty, then updated
    if (appConfig == null) {
      logger.debug("ServerContext.initialize: Creating empty appConfig object");
      appConfig = new AppConfig(initParams);
      logger.debug("ServerContext.initialize: Creating empty sitesInfo object");
      sitesInfo = new SitesInfo(contextPath, true, initParams.getMap(), appConfig);
    }
    // userInfo must be created after appConfig & sitesInfo are created, then loaded first
    userInfo = new UserInfo(loadUsers, sitesInfo, restrictServices);

    if (!loadUsers && !loadConfig) {
      logger.debug("ServerContext.initialize: no database access was done");
      return;
    }

    // The following doesn't do anything in a non-DB test, when the database is mocked
    db.transact(dbp -> {
      logger.debug("ServerContext.initialize: initting objects -- appConfig");
      appConfig.refresh(dbp);
      sitesInfo.refresh(dbp); // must be AFTER the appConfig it uses for data
      userInfo.load(dbp);     // must be done AFTER sitesInfo, to initialize user sites
      logger.debug("ServerContext.initialize: sitesInfo.size = "+sitesInfo.getNumberOfSites());
    });
    logger.debug("ServerContext is initialized");
  }

  /**
   * This is run by the PollingThread. It's synchronized because later we'll make it
   * poke'able by the UI.
   * @param reloadUsers
   * @param reloadConfig
   * @param reloadSurveySites
   */
  public synchronized void reload(final boolean reloadUsers, final boolean reloadConfig) {
    if (!reloadUsers && !reloadConfig) {
      return;
    }

    Timer timer = new Timer();
    db.transact(dbp -> {
      if (reloadUsers) {
        userInfo.load(dbp);
      }
      if (reloadConfig) {
        appConfig.refresh(dbp);
        sitesInfo.refresh(dbp);
      }
    });
    logger.info("Reloaded users and config in "+timer.getSeconds());
  }

  public Log logFor(Class<?> category) {
    return logProvider.get(category);
  }

  public void recordServiceLogin(final ServiceLogin login) {

    if (logger.isDebugEnabled()) {
      logger.debug("Granted " + login.getUser().getUsername() + " at "
          + login.getIpAddress() + " access to " + login.getService().getUrlPath()
          + " with agent "
          + login.getRawUserAgent());
    }

    db.transact(dbp -> dbp.get()
        .toInsert("insert into service_audit (service_audit_id,username,ip_address,service_path,"
            + "login_time,java_version,java_vendor,os_name,os_version,os_arch,user_agent) "
            + "values(?,?,?,?,?,?,?,?,?,?,?)")
            .argPkSeq("service_audit_seq")
            .argString(trunc(login.getUser().getUsername(), 128))
            .argString(trunc(login.getIpAddress(), 40))
            .argString(trunc(login.getService().getUrlPath(), 20))
            .argDateNowPerDb()
            .argString(trunc(login.getJavaVersion(), 20))
            .argString(trunc(login.getJavaVendor(), 20))
            .argString(trunc(login.getOsName(), 20))
            .argString(trunc(login.getOsVersion(), 20))
            .argString(trunc(login.getOsArch(), 20))
            .argString(trunc(login.getRawUserAgent(), 4000)).insert(1));
  }

  public UserInfo userInfo() {
    return userInfo;
  }
  
  public AppConfig appConfig() {
    return appConfig;
  }

  /**
   * @return the configuration parameters for the server and all the sites
   */
  public SitesInfo getSitesInfo() {
    return sitesInfo;
  }

  public SiteInfo getSiteInfo(Long siteId) {
    return sitesInfo.getBySiteId(siteId);
  }

  private String trunc(String input, int length) {
    if (input == null) {
      return "";
    }

    if (input.length() > length) {
      return input.substring(0, length);
    }

    return input;
  }

//  public ProcessInfo getProcess(String processName) {
//    ProcessInfo processInfo;
//    synchronized (processesLock) {
//      processInfo = processes.get(processName);
//    }
//    return processInfo;
//  }
//
//  public boolean isRunning(String processName) {
//    return (getProcess(processName) != null);
//  }

  public boolean addProcess(ProcessInfo processInfo) {
    synchronized (processesLock) {
      if (processes.containsKey(processInfo.getProcessName())) {
        return false;
      }
      processes.put(processInfo.getProcessName(), processInfo);
      return true;
    }
  }

  public void removeProcess(ProcessInfo processInfo) {
    synchronized (processesLock) {
      processes.remove(processInfo.getProcessName());
    }
  }

  /**
   * This should only be used if a process aborted and didn't invoke the remove.
   */
  public void clearProcesses() {
    synchronized (processesLock) {
      processes.clear();
    }
  }

  public ArrayList<ProcessInfo> getProcesses() {
    ArrayList<ProcessInfo> list = new ArrayList<>();
    synchronized (processesLock) {
      for (String key : processes.keySet()) {
        list.add(processes.get(key));
      }
    }
    return list;
  }
}
