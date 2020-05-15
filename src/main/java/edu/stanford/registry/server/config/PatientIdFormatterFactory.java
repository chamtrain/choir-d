/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.server.utils.PatientIdUnformatted;

public class PatientIdFormatterFactory {
  private static final Logger logger = Logger.getLogger(PatientIdFormatterFactory.class);

  public PatientIdFormatterFactory() { }


  public PatientIdFormatIntf create(SiteInfo siteInfo) {
    String className = siteInfo.getProperty("PatientIdFormatterClass");
    PatientIdFormatIntf theFormatter = null;

    if (className != null && !className.isEmpty()) {
      theFormatter = createFormatter(siteInfo, className);
    }

    // If it failed, or className wasn't specified, return the default
    if (theFormatter == null) {
      logger.debug(siteInfo.getIdString() + "PatientIdFormatter is set to the default: PatientIdUnformatted");
      return new PatientIdUnformatted();
    }

    return theFormatter;
  }


  private PatientIdFormatIntf createFormatter(SiteInfo siteInfo, String className) {
    try {
      Class<?> PatientIdFormatIntf = Class.forName(className);
      Constructor<?> constructor = PatientIdFormatIntf.getConstructor();
      PatientIdFormatIntf ret = (PatientIdFormatIntf) constructor.newInstance();
      logger.debug(getMsg(siteInfo, className, false));
      return ret;
    } catch (Exception ex) {
      logger.error(getMsg(siteInfo, className, true) + "; error: " + ex.getMessage());
      return null;
    }
  }


  private String getMsg(SiteInfo siteInfo, String className, boolean isErr) {
    String isNow = isErr ? " can NOT be set to " : " is now set to ";
    return siteInfo.getIdString() + "PatientIdFormatter" + isNow + ": " + className;
  }
}
