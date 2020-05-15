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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class SurveyRegistrationManager {

  ArrayList<RegistrationService> services = new ArrayList<>();
  private static Logger logger = Logger.getLogger(SurveyRegistrationManager.class);

  public SurveyRegistrationManager(Database db, SiteInfo siteInfo) {
    XMLFileUtils utils = XMLFileUtils.getInstance(siteInfo);
    ArrayList<String> processNames = utils.getProcessNames();
    for (String processName : processNames) {
      logger.debug("adding process named " + processName);
      String type = utils.getAttribute(processName, XMLFileUtils.ATTRIBUTE_START_DT);
      String startDate = utils.getAttribute(processName, XMLFileUtils.ATTRIBUTE_EXPIRE_DT);
      RegistrationService service = new RegistrationService(db, processName, type, startDate, siteInfo);
      services.add(service);
    }
    logger.debug("Found " + services.size() + " process types");
  }

  public RegistrationService getRegistrationService(String name, Patient patient) {
    Date agreed = null;
    if (patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
      agreed = patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDtCreated();
    }
    return getRegistrationService(name, agreed);
  }

  public RegistrationService getRegistrationService(String name, Date agreed) {
    if (agreed == null) {
      return null;
    }
    logger.debug("getRegistrationService called for : " + name + "," + agreed.toString());
    for (RegistrationService service : services) {
      if (service.matches(name, agreed)) {
        logger.debug("getRegistrationService matches " + service.getSurveyType());
        return service;
      }
    }
    return null;
  }

}
