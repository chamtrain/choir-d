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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryQuestionUtils {

  private static final Logger logger = LoggerFactory.getLogger(RegistryQuestionUtils.class);

  public static boolean meetsCondition(Patient patient, String dataName, String dataValue, String condition) {
    if ((patient == null) || (dataName == null)) {
      return false;
    }

    String attrValue = null;
    if (Constants.ATTRIBUTE_SURVEYEMAIL.equals(dataName)) {
      attrValue = patient.getEmailAddress();
    } else if (patient.hasAttribute(dataName)) {
      attrValue = patient.getAttribute(dataName).getDataValue();
    } else if (dataName.equals("age")) {
      int age = DateUtils.getAge(patient.getDtBirth());
      if (age >= 0) {
        attrValue = Integer.toString(age);
      }
    }

    return compare(attrValue, dataValue, condition);
  }

  public static boolean hasResponse(String xmlDocument, String xpathQuery, String dataValue, String condition) {
    if (logger.isTraceEnabled()) {
      logger.trace("query:" + xpathQuery);
      logger.trace("dataValue:" +  dataValue + ", condition:" + condition);
    }
    if ((xmlDocument == null) || (xpathQuery == null)) {
      return false;
    }
    String value = XMLFileUtils.xPathQuery(xmlDocument, xpathQuery);
    return compare(value, dataValue, condition);
  }

  private static boolean compare(String attrValue, String dataValue, String condition) {
    if (condition == null) {
      // no condition is treated the same as 'exists'
      return (attrValue != null);
    } else if (condition.equals("exists")) {
      return (attrValue != null);
    } else if (condition.equals("notexists")) {
      return (attrValue == null);
    } else if (condition.equals("equal")) {
      if (dataValue != null) {
        return dataValue.equals(attrValue);
      } else {
        return (attrValue == null);
      }
    } else if (condition.equals("notequal")) {
      if (dataValue != null) {
        return !dataValue.equals(attrValue);
      } else {
        return (attrValue != null);
      }
    } else if (condition.equals("lessthan")) {
      if ((dataValue == null) || (attrValue == null)) { 
        return false;
      }
      try {
        return (Long.parseLong(attrValue) < Long.parseLong(dataValue));
      } catch (NumberFormatException e) {
        return (dataValue.compareTo(attrValue) > 0);
      }
    } else if (condition.equals("lessequal")) {
      if ((dataValue == null) || (attrValue == null)) { 
        return false;
      }
      try {
        return (Long.parseLong(attrValue) <= Long.parseLong(dataValue));
      } catch (NumberFormatException e) {
        return (dataValue.compareTo(attrValue) > 0);
      }
    } else if (condition.equals("greaterthan")) {
      if ((dataValue == null) || (attrValue == null)) { 
        return false;
      }
      try {
        return (Long.parseLong(attrValue) > Long.parseLong(dataValue));
      } catch (NumberFormatException e) {
        return (dataValue.compareTo(attrValue) < 0);
      }
    } else if (condition.equals("greaterequal")) {
      if ((dataValue == null) || (attrValue == null)) { 
        return false;
      }
      try {
        return (Long.parseLong(attrValue) >= Long.parseLong(dataValue));
      } catch (NumberFormatException e) {
        return (dataValue.compareTo(attrValue) < 0);
      }
    } else if (condition.equals("contains")) {
      if ((dataValue == null) || (attrValue == null)) {
        return false;
      }
      return attrValue.contains(dataValue);
    } else {
      return false;
    }
  }
}
