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

package edu.stanford.registry.server.export;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.export.data.DataTableIntf;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.server.xchg.data.Constants;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

public class ExportFactory {

  private static ExportFactory instance;
  private static Logger logger = Logger.getLogger(ExportFactory.class);
  private static final String DATE_FORMATTER = "DateFormatter";
  private static final String STRING_REPLACE = "StringReplace";

  private ExportFactory() {
  }

  public static ExportFactory getFactory() {
    if (instance == null) {
      instance = new ExportFactory();
    }

    return instance;
  }

  public ExportFormatterIntf<?> getManager(String exportType, String className) {

    if (exportType == null) {
      return null;
    }

		/*
     * When no class name in the export definitions file see if the type is a predefined system type and return the manager.
		 */
    if (ServerUtils.isEmpty(className)) {
      for (int inx = 0; inx < Constants.EXPORT_TYPES.length; inx++) {
        if (Constants.EXPORT_TYPES[inx].equals(exportType)) {
          className = Constants.EXPORT_DEFAULT_OBJECTS_PACXAGE + Constants.EXPORT_TYPE_CLASSNAMES[inx];
        }
      }
    }

    if (ServerUtils.isEmpty(className)) {
      return null;
    }

    try {
      Class<?> managerClass = Class.forName(className);
      Constructor<?> constructor = managerClass.getConstructor();
      return (ExportFormatterIntf<?>) constructor.newInstance();
    } catch (Exception ex) {
      logger.error("Cannot create class using name: " + className);
    }
    return null;

  }

  /**
   * Get a data qualifier object to see if this data is to be included.
   *
   * @param dataSource
   * @param qualifierClassName
   * @param qualifyingString
   * @return
   */
  public QualifierIntf<?> getQualifier(String dataSource, String qualifierClassName, String qualifyingString) {
		/*
		 * When no class name in the export definitions file then the data source is the class and the path is the default system export path.
		 */
    if (ServerUtils.isEmpty(qualifierClassName)) {
      qualifierClassName = Constants.EXPORT_DEFAULT_OBJECTS_PACXAGE + dataSource;
    }

    try {
      Class<?> qualifierClass = Class.forName(qualifierClassName);
      Constructor<?> constructor = qualifierClass.getConstructor();
      DataTableIntf dataTableObject = (DataTableIntf) constructor.newInstance();
      return dataTableObject.getQualifier(qualifyingString);
    } catch (Exception ex) {
      logger.error("Cannot create class using name: " + qualifierClassName);
    }
    return null;
  }

  /**
   * Get a data formatter to transform the data being output.
   */
  public ExportFormatterIntf<?> getFormatter(String serviceName, String formatterString) {

    if (ServerUtils.isEmpty(formatterString)) {
      return new CommonFormatter();
    }

    int openBracket = formatterString.indexOf("(");
    int closeBracket = formatterString.indexOf(")");

    if (openBracket < 1 || closeBracket < 1) {
      return new CommonFormatter();
    }

    String formatterClassName = formatterString.substring(0, openBracket);
    if (formatterClassName.trim().length() < 1) {
      return new CommonFormatter();
    }

    if (DATE_FORMATTER.equals(formatterClassName)) {
      if (closeBracket > openBracket + 1) {
        return new DateFormatter(formatterClassName.substring(openBracket + 1, closeBracket));
      }
      return new DateFormatter();
    }

    if (STRING_REPLACE.equals(formatterClassName)) {
      return new StringReplace(formatterClassName.substring(openBracket + 1, closeBracket));
    }

    try {
      Class<?> surveyImplClass = Class.forName(formatterClassName);
      Constructor<?> constructor = surveyImplClass.getConstructor();
      return (ExportFormatterIntf<?>) constructor.newInstance();
    } catch (Exception ex) {
      logger.error("Cannot create class using name: " + formatterClassName);
    }
    return null;

  }
}
