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

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

public class FormatterFactory {

  private static FormatterFactory instance;
  private static Logger logger = Logger.getLogger(FormatterFactory.class);
  private static final String DATE_FORMATTER = "DateFormatter";
  private static final String STRING_REPLACE = "StringReplace";

  private FormatterFactory() {
  }

  public static FormatterFactory getFactory() {
    if (instance == null) {
      instance = new FormatterFactory();
    }

    return instance;
  }

  public ExportFormatterIntf<?> getFormatter(String serviceName,
                                             String formatterString) {

    if (formatterString == null) {
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
        return new DateFormatter(formatterClassName.substring(openBracket + 1,
            closeBracket));
      }
      return new DateFormatter();
    }

    if (STRING_REPLACE.equals(formatterClassName)) {
      return new StringReplace(formatterClassName.substring(openBracket + 1,
          closeBracket));
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
