/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.shc.pain.PhysicalTherapyService;
import edu.stanford.registry.shared.SurveySystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Override the getSurveySystemId method for psych since it has a custom survey system
 */
public class SurveyAdvanceHandlerCustomService extends SurveyAdvanceHandlerGenXmlSquareTable {
  private static final Logger log = LoggerFactory.getLogger(SurveyAdvanceHandlerCustomService.class);

  private final long configId;


  public SurveyAdvanceHandlerCustomService(long configId, SiteInfo siteInfo) {
    super(configId, siteInfo);
    this.configId = configId;
  }

  @Override
  public int getSurveySystemId(Database db) {
    AppConfigDao appConfigDao = new AppConfigDao(db);
    log.debug("Getting app_config for id: {}", configId);
    AppConfigEntry appConfigEntry = appConfigDao.findAppConfigEntry(configId);
    if (appConfigEntry == null) {
      return super.getSurveySystemId(db);
    }
    SurveySystem surveySystem;
    switch (appConfigEntry.getConfigName()) {
      case "empower":
        surveySystem = new SurveySystDao(db).getSurveySystem("EmpowerStudyService");
        break;
      default:
        surveySystem = new SurveySystDao(db).getSurveySystem(appConfigEntry.getConfigName());
        break;
    }

    for (String surveyName : PhysicalTherapyService.surveys) {
      if (appConfigEntry.getConfigName().equals(surveyName) ||
        appConfigEntry.getConfigName().startsWith(surveyName + "@")) {
        surveySystem = new SurveySystDao(db).getSurveySystem("PhysicalTherapyService");
      }
    }

    if (surveySystem == null) {
      return super.getSurveySystemId(db);
    }
    log.debug("Returning survey system id {} for config_name {}", surveySystem.getSurveySystemId(), appConfigEntry.getConfigName());
    return surveySystem.getSurveySystemId();
  }
}
