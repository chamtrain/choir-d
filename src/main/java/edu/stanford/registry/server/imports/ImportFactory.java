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

package edu.stanford.registry.server.imports;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.export.CommonFormatter;
import edu.stanford.registry.server.imports.data.ImportDataSourceManagerIntf;
import edu.stanford.registry.server.imports.data.ImportTypeManagerIntf;
import edu.stanford.registry.server.utils.XchgUtils;
import edu.stanford.registry.server.xchg.DateFormatter;
import edu.stanford.registry.server.xchg.DefinitionDataSource;
import edu.stanford.registry.server.xchg.FormatterIntf;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.server.xchg.data.GenderFormat;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class ImportFactory {

  private static ImportFactory instance;
  private static Logger logger = Logger.getLogger(ImportFactory.class);
  private static final String DATE_FORMATTER = "DateFormatter";
  private static final String STRING_REPLACE = "StringReplace";
  private static final String GENDER_FORMATTER = "GenderFormat";

  private ImportFactory() {
  }

  public static ImportFactory getFactory() {
    if (instance == null) {
      instance = new ImportFactory();
    }

    return instance;
  }

  /**
   * Get an instance of the class that does importing for this type of data
   */
  public ImportDataSourceManagerIntf getDataSourceManager(SiteInfo siteInfo, DefinitionDataSource dataSource, ImportResources res,
      Database database, HashMap<String, ImportDefinitionQueue> queue) {

    if (dataSource == null || dataSource.getDataSource() == null) {
      return null;
    }

    String className = getDataSourceClassName(dataSource);
    ImportDataSourceManagerIntf dsm = getDataSourceManagerClass(siteInfo, className);

    if (dsm == null) {
      logger.error("No manager found for class: "+className);
      return null;
    }

    dsm.setDatabase(database, siteInfo);
    dsm.setResources(res);
    ImportDefinitionQueue defQueue = queue.get(dataSource.getDataSource());
    if (defQueue == null) {
      logger.debug("queuesMap is null");
    }
    dsm.setQueue(defQueue);
    String[] depends = dsm.getDataSourceDependancies(); // see if it needs the
    // data from other
    // datasources
    for (String depend : depends) {
      logger.debug("adding data for " + depend);
      dsm.addDependancyQueue(queue.get(depend));
    }
    return dsm;
  }

  public ImportTypeManagerIntf getTypeManager(SiteInfo siteInfo, ImportDefinitionType type, ImportResources res, Database database,
      HashMap<String, ImportDefinitionQueue> queue) {

    if (type == null || type.getType() == null) {
      return null;
    }

    String className = getTypeClassName(type);
    ImportTypeManagerIntf itmi = getTypeManagerClass(siteInfo, className);
    if (itmi != null) {
      itmi.setSiteInfo(siteInfo);
      itmi.setDatabase(database);
      itmi.setQueue(queue);
      itmi.setResources(res);
      itmi.setType(type);
    }
    return itmi;
  }

  private ImportTypeManagerIntf getTypeManagerClass(SiteInfo siteInfo, String className) {

    if (ServerUtils.isEmpty(className)) {
      return null;
    }

    Class<?> managerClass = getClassForName(className);
    if (managerClass == null) {
      logger.error("Cannot find ImportTypeManager of name: " + className);
      return null;
    }

    try {
      Constructor<?> constructor = getConstructor(managerClass);
      if (constructor != null)
        return (ImportTypeManagerIntf) constructor.newInstance();

      constructor = getConstructor(managerClass, SiteInfo.class);
      if (constructor != null)
        return (ImportTypeManagerIntf) constructor.newInstance(siteInfo);
    } catch (Exception ex) {
      logger.error("Problem creating ImportTypeManager class using name: " + className + "; "+
                   ex.getClass().getSimpleName()+" "+ex.getMessage());
    }
    return null;
  }

  private ImportDataSourceManagerIntf getDataSourceManagerClass(SiteInfo siteInfo, String className) {

    if (ServerUtils.isEmpty(className)) {
      return null;
    }

    Class<?> managerClass = getClassForName(className);
    if (managerClass == null) {
      logger.error("Cannot find ImportDataSourceManager of name: " + className);
      return null;
    }

    try {
      Constructor<?> constructor = getConstructor(managerClass);
      if (constructor != null)
        return (ImportDataSourceManagerIntf) constructor.newInstance();

      constructor = getConstructor(managerClass, SiteInfo.class);
      if (constructor != null)
        return (ImportDataSourceManagerIntf) constructor.newInstance(siteInfo);

    } catch (Exception ex) {
      logger.error("Cannot create ImportDataSourceManager class using name: " + className + "; "+
                   ex.getClass().getSimpleName()+" "+ex.getMessage());
    }
    return null;
  }


  /**
   * Get a data qualifier object to see if this data is to be included.
   */
  public QualifierIntf<?> getQualifier(SiteInfo siteInfo, String qualifierClassName) {
    if (ServerUtils.isEmpty(qualifierClassName)) {
      return null;
    }

    logger.debug("getQualifier starting class: " + qualifierClassName);

    // try the name and the defaultname for it
    qualifierClassName = qualifierClassName.trim();
    Class<?> qualifierClass = getClassForName(qualifierClassName);
    if (qualifierClass == null) {
      String name = XchgUtils.DefaultXchgQualifierClassName(qualifierClassName);
      qualifierClass = getClassForName(name);
      if (qualifierClass == null) {
        logger.warn("getQualifier didn't find classname returning null");
        return null;
      }
      qualifierClassName = name;
    }

    // shouldn't encounter any exceptions...
    try {
      Constructor<?> constructor = getConstructor(qualifierClass);
      if (constructor != null)
        return (QualifierIntf<?>) constructor.newInstance();

      constructor = getConstructor(qualifierClass, SiteInfo.class);
      if (constructor != null)
        return (QualifierIntf<?>) constructor.newInstance(siteInfo);

      logger.error("Cannot find a suitable constructor for Qualifier class: " + qualifierClassName);
    } catch (Exception ex) {
      logger.error("Cannot create Qualifier class using name: " + qualifierClassName);
    }
    return null;
  }

  Class<?> getClassForName(String name) {
      try {
        Class<?> theClass = Class.forName(name);
        return theClass;
      } catch (ClassNotFoundException e) {
        return null;
      }
  }

  Constructor<?> getConstructor(Class<?> cls) {
    try {
      Constructor<?> constructor = cls.getConstructor();
      return constructor;
    } catch (Exception e) {
      return null;
    }
  }

  public Constructor<?> getConstructor(Class<?> cls, Class<?>...classes) {
    try {
      return cls.getConstructor(classes);
    } catch (Exception e) {
      return (Constructor<?>)null;
    }
  }

  /**
   * Get a data formatter to transform the data being output.
   */
  public FormatterIntf<?> getFormatter(SiteInfo siteInfo, String serviceName, String formatterString) {
    logger.debug("getFormatter( " + serviceName + "," + formatterString + ") starting");
    if (ServerUtils.isEmpty(formatterString)) {
      logger.debug("serverutils(" + formatterString + ") empty. getFormatter returning commonformatter");
      return new CommonFormatter(siteInfo);
    }

    int openBracket = formatterString.indexOf("(");
    int closeBracket = formatterString.indexOf(")");
    String formatterClassName;
    if ((openBracket < 0 && closeBracket >= 0) || (openBracket >= 0 && closeBracket < 0)) {
      logger.debug("Mismatched brackets, getFormatter returning commonformatter");
      return new CommonFormatter(siteInfo);
    }
    if (openBracket < 0) {
      formatterClassName = formatterString.trim();
    } else {
      formatterClassName = formatterString.substring(0, openBracket);
    }
    if (formatterClassName.trim().length() < 1) {
      logger.debug("No formatter classname, getFormatter returning commonformatter");
      return new CommonFormatter(siteInfo);
    }

    if (DATE_FORMATTER.equals(formatterClassName)) {
      if (closeBracket > openBracket + 1) {
        logger.debug("getFormatter returning DateFormatter(with contents)");
        return new DateFormatter<String>(siteInfo, formatterString.substring(openBracket + 1, closeBracket));
      }
      logger.debug("getFormatter returning " + DATE_FORMATTER + " without contents");
      return new DateFormatter<String>(siteInfo);
    }

    if (STRING_REPLACE.equals(formatterClassName)) {
      logger.debug("getFormatter returning " + STRING_REPLACE + " with()");
      return new StringReplace(siteInfo, formatterString.substring(openBracket + 1, closeBracket));
    }

    if (GENDER_FORMATTER.equals(formatterClassName)) {
      logger.debug("getFormatter returning GenderFormat(" + formatterString.substring(openBracket + 1, closeBracket) + ")");
      return new GenderFormat(siteInfo, formatterString.substring(openBracket + 1, closeBracket));
    }
    /**
     * First try to load as a fully qualified name for handling customizations
     */
    Class<?> formatterClass = null;
    try {
      formatterClass = Class.forName(formatterClassName);
    } catch (Exception ex) {
      // ignore for now
    }

    /**
     * If not loaded try the default classpath for formatters.
     */
    if (formatterClass == null) {
      try {
        formatterClass = Class.forName(XchgUtils.DefaultXchgFormatterClassName(formatterClassName));
      } catch (Exception ex) {
        logger.error("Cannot find formatter class using name: " + formatterClassName);
      }
    }

    /**
     * If we got one then lets load it
     */
    if (formatterClass != null) {
      try {

        /**
         * If a string parameter is specified, try with it, and with SiteInfo and it
         */
        if (closeBracket > openBracket + 1) {
          String arg = formatterString.substring(openBracket + 1, closeBracket);
          Constructor<?> constructor = getConstructor(formatterClass, String.class);
          if (constructor != null) {
            logger.debug("getFormatter returning " + formatterClassName +"(" + formatterString.substring(openBracket + 1, closeBracket) + ")");
            return (FormatterIntf<?>) constructor.newInstance(arg);
          }
          constructor = getConstructor(formatterClass, SiteInfo.class, String.class);
          if (constructor != null) {
            logger.debug("getFormatter returning " + formatterClassName +"(siteInfo, " + formatterString.substring(openBracket + 1, closeBracket) + ")");
            return (FormatterIntf<?>) constructor.newInstance(siteInfo, arg);
          }
        }

        /**
         * Else try just with SiteInfo, or no parameter
         */
        Constructor<?> constructor  = getConstructor(formatterClass, SiteInfo.class);
        if (constructor != null)
          return (FormatterIntf<?>) constructor.newInstance(siteInfo);

        constructor = getConstructor(formatterClass);
        if (constructor != null)
          return (FormatterIntf<?>) constructor.newInstance();

        logger.error("Could not find a suitable constructor for class: "+formatterClassName);
      } catch (Exception ex) {
        logger.error("Error creating formatter for class: " + formatterClassName);
      }
    }
    return new CommonFormatter(siteInfo);

  }

  private static String getDataSourceClassName(DefinitionDataSource dataSource) {

    if (dataSource != null) {
      if (!ServerUtils.isEmpty(dataSource.getClassName())) {
        return dataSource.getClassName();
      }
      /*
       * When no class name in the Import definitions file see if the type is a predefined system type and return the manager.
       */
      return XchgUtils.DefaultImportDataSourceClassName(dataSource.getDataSource());
    }
    return null;
  }

  private static String getTypeClassName(ImportDefinitionType type) {
    if (type != null) {
      if (!ServerUtils.isEmpty(type.getClassName())) {
        return type.getClassName();
      }
      return XchgUtils.DefaultImportTypeClassName(type.getType());
    }
    return null;
  }
}
