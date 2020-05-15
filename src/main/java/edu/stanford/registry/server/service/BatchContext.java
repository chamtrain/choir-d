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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.service.tasks.Timer;
import edu.stanford.registry.server.survey.SurveyAdvanceHandlerFactoryImpl;
import edu.stanford.registry.server.survey.SurveyCompleteHandlerFactoryImpl;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.server.SurveyAdvanceHandlerFactory;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyCompleteHandlerFactory;
import edu.stanford.survey.server.SurveyCompleteMonitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;

/**
 * This runs the SurveyCompleteMonitor and SurveyAdvanceMonitor in each site.
 *
 * TODO: Make this thread-safe, with a database lock, so multiple nodes can
 * supply redundancy (take over if one goes down.)
 *
 * @author rstr
 */
public final class BatchContext {
  static final Logger logger = Logger.getLogger(BatchContext.class);
  static final String FACTORY_COMPLETE = "factory.survey.complete";
  static final String FACTORY_ADVANCE = "factory.survey.advance";

  static boolean initted;  // so only one instance exists

  private final DatabaseProvider.Builder db;
  private final ServerContext serverContext;

  public BatchContext(Builder db, ServerContext sctxt, SitesInfo sitesInfo) {
    this.db = db;
    serverContext = sctxt;
    initialize();
  };


  SurveyCompleteHandlerFactory getCompleterFactory(SiteInfo siteInfo) {
    HandlerFactoryCreator<SurveyCompleteHandlerFactory> creator = new HandlerFactoryCreator<>(siteInfo, FACTORY_COMPLETE);
    if (creator.propIsNotSet()) {
      return new SurveyCompleteHandlerFactoryImpl(serverContext, siteInfo);
    } else {
      return creator.getHandlerFactory(serverContext);
    }
  }


  SurveyAdvanceHandlerFactory setAdvancerFactory(SiteInfo siteInfo) {
    HandlerFactoryCreator<SurveyAdvanceHandlerFactory> creator = new HandlerFactoryCreator<>(siteInfo, FACTORY_ADVANCE);
    if (creator.propIsNotSet()) {
      return new SurveyAdvanceHandlerFactoryImpl(serverContext, siteInfo);
    } else {
      return creator.getHandlerFactory(serverContext);
    }
  }


  void initialize() {
    if (initted) {
      logger.error("BatchContext.initialize was called twice- aborting the second");
      return;
    }
    initted = true;
  }

  boolean interrupted;
  public void interrupt() {
    interrupted = true;
  }

  static String completePropValue = "bogus";
  static String advancePropValue = "bogus";

  private void sayProp(SitesInfo sitesInfo, boolean complete) {
    String propName = complete ? FACTORY_COMPLETE : FACTORY_ADVANCE;
    String oldValue = complete ? completePropValue : advancePropValue;
    String newValue = sitesInfo.getGlobalProperty(propName);
    boolean changed = (newValue == null ? oldValue != null : !newValue.equals(oldValue));
    if (changed) {
      logger.info("Using new batch property: "+propName+" = "+newValue);
      if (complete) {
        completePropValue = newValue;
      } else {
        advancePropValue = newValue;
      }
    }
  }

  /**
   * This is run and controlled by the PollingThread
   */
  public synchronized void advanceAndCompleteSurveys() {
    SitesInfo sitesInfo = serverContext.getSitesInfo();
    sayProp(sitesInfo, true);
    sayProp(sitesInfo, false);
    Timer timer = new Timer();
    int counter = 0;
    for (SiteInfo siteInfo: sitesInfo) {
      if (interrupted) {
        interrupted = false;
        break;
      }
      // Let people turn it on/off per site
      if (siteInfo.getProperty("batch.survey.handling", true)) {
        counter += executeBatch(siteInfo);
      }
    }
    if (counter > 0) {
      logger.info("Batch advance/complete survey, updated "+counter+" in "+timer.getSeconds());
    }
  }


  // Execute the survey monitors, marking the log with a batch user, in a database transaction
  protected int executeBatch(SiteInfo siteInfo) {
    int counter = 0;
    try {
      DatabaseProvider databaseProvider = db.withTransactionControl().create();
      boolean commit = false;

      try {
        counter += executeBatch(databaseProvider, siteInfo);
        commit = true;
      } finally {
        if (commit) {
          databaseProvider.commitAndClose();
        } else {
          databaseProvider.rollbackAndClose();
        }
      }
    } catch (Exception e) {
      logger.error(siteInfo.getIdString()+"Error running batch job", e);
    }
    return counter;
  }


  protected int executeBatch(Supplier<Database> database, SiteInfo siteInfo) {
    // This is where we do the actual work
    SurveyCompleteHandlerFactory completerFactory = getCompleterFactory(siteInfo);
    SurveyAdvanceHandlerFactory advancerFactory = setAdvancerFactory(siteInfo);

    logger.info(siteInfo.getIdString()+"Executing the SurveyCompleteMonitor and SurveyAdvanceMonitor");
    int n = new SurveyCompleteMonitor(siteInfo.getSiteId(), completerFactory).pollAndNotify(database);
    n    += new SurveyAdvanceMonitor(siteInfo.getSiteId(), advancerFactory).pollAndNotify(database);
    return n;
  }

  public synchronized void sendEmails() {
    SitesInfo sitesInfo = serverContext.getSitesInfo();
    for (SiteInfo siteInfo: sitesInfo) {
      if (interrupted) {
        interrupted = false;
        break;
      }
      // Site specific enabled/disabled
      if (siteInfo.getProperty("batch.email.sending", false)) {
        sendEmails(siteInfo);
      }
    }
  }

  protected void sendEmails(SiteInfo siteInfo) {
    try {
      DatabaseProvider databaseProvider = db.withTransactionControl().create();
      boolean commit = false;

      try {
        User admin = ServerUtils.getAdminUser(databaseProvider.get());
        AdministrativeServices adminServices = new AdministrativeServicesImpl(admin, databaseProvider, serverContext, siteInfo);
        RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
        customizer.batchSendEmails(databaseProvider, adminServices);
        commit = true;
      } finally {
        if (commit) {
          databaseProvider.commitAndClose();
        } else {
          databaseProvider.rollbackAndClose();
        }
      }
    } catch (Exception e) {
      logger.error(siteInfo.getIdString()+"Error running batch job", e);
    }
  }

  /**
   * Handles creating a factory class from a property where the constructor takes (serverContext, sitesInfo),
   * or just one of them, or none. It's parameterized by the kind of factory.
   * @author rstr
   */
  static class HandlerFactoryCreator<T> {
    private SiteInfo si;
    private String propName;
    private String className;

    HandlerFactoryCreator(SiteInfo si, String propName) {
      this.si = si;
      this.propName = propName;
      className = si.getGlobalProperty(propName);
    }

    boolean propIsNotSet() {
      return className == null || className.isEmpty();
    }

    T getHandlerFactory(ServerContext ctxt) {  
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<T> cls;
      try {
        @SuppressWarnings("unchecked")
        Class<T> tmp = (Class<T>)loader.loadClass(className);
        cls = tmp;
      } catch (ClassNotFoundException cnfe) {
        String msg = "%sBatch survey processing aborted - no class found with name in property %s=%s";
        logger.error(String.format(msg, si.getIdString(), FACTORY_COMPLETE, className));
        return null;
      }
  
      try {
        Constructor<T> theClass;
        theClass = mkComplete(cls, ctxt.getClass(), si.getClass());
        if (theClass != null) {
          return theClass.newInstance(ctxt, si);
        }
  
        theClass = mkComplete(cls, ctxt.getClass());
        if (theClass != null) {
          return theClass.newInstance(ctxt);
        }
  
        theClass = mkComplete(cls, si.getClass());
        if (theClass != null) {
          return theClass.newInstance(si);
        }
  
        theClass = mkComplete(cls);
        if (cls != null) {
          return theClass.newInstance();
        }
      } catch (SecurityException |InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
        String msg = "%sBatch survey processing disabled - problem constructing %s=%s";
        logger.warn(String.format(msg, si.getIdString(), propName, className), e);
      }
  
      String msg = "%sBatch survey processing aborted - no constructor was found for property %s=%s";
      logger.warn(String.format(msg, si.getIdString(), propName, className));
      return null;
    }

    Constructor<T> mkComplete(Class<T> cls, Class<?>...parameterTypes) 
        throws SecurityException {
      try {
        return cls.getConstructor(parameterTypes);
      } catch (NoSuchMethodException e) {
        return null;
      }
    }
  }
}
