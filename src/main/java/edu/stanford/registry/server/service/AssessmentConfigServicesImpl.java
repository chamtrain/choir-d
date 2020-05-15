/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

import com.github.susom.database.Database;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerException;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.customassessment.CustomAssessmentConfigUtil;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.AssessmentConfig;
import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class AssessmentConfigServicesImpl implements AssessmentConfigServices {

  private static final Logger logger = LoggerFactory.getLogger(AssessmentConfigServicesImpl.class);
  protected final ServerContext context;
  protected final Supplier<Database> dbp;
  protected final User user;
  private final XMLFileUtils xmlUtils;
  protected SiteInfo siteInfo;
  protected CustomAssessmentConfigUtil cConfig;

  public AssessmentConfigServicesImpl(User user, Supplier<Database> dbp, ServerContext context, SiteInfo siteInfo) {
    this.dbp = dbp;
    this.context = context;
    this.siteInfo = siteInfo;
    this.user = user;
    xmlUtils = XMLFileUtils.getInstance(siteInfo);
    initConfig();
  }

  @Override
  public AssessmentConfig updateCustomAssessmentConfig(AssessmentConfig assessmentConfig) {
    try {
      if (assessmentConfig != null) {
        cConfig.initObject(assessmentConfig);
        String json = cConfig.serializeToJson();

        if (updateCustomAssessmentConfigJSON(json)) {
          context.reload(false, true);
          siteInfo = context.getSiteInfo(siteInfo.getSiteId());
          return getAssessmentConfig(assessmentConfig.getClinicName(), assessmentConfig.getAssessmentType());
        } else {
          return null;
        }
      }
      return null;
    } catch (Exception e) {
      logger.error("Error occurred when updating custom assessment config. " + e.toString(), e);
      throw new ServerException("Error getting assigned instruments");
    }
  }

  private boolean updateCustomAssessmentConfigJSON(String json) {
    try {
      if (json == null || json.isEmpty()) {
        return false;
      }
      AppConfigDao configDao = new AppConfigDao(dbp.get(), user);
      return configDao.addOrEnableAppConfigEntry(siteInfo.getSiteId(), ConfigParam.CONFIG_TYPE,
          Constants.CUSTOM_ASSESSMENT_CONFIG_NAME, json);
    } catch (Exception e) {
      logger.error("Error when saving config " + e.toString(), e);
    }
    return false;
  }

  /**
   * Initializes the config bean from loaded app config value
   */
  protected void initConfig() {
    if (siteInfo.getProperty(Constants.ENABLE_CUSTOM_ASSESSMENT_CONFIG).equalsIgnoreCase("y")) {
      String configValue = siteInfo.getProperty(Constants.CUSTOM_ASSESSMENT_CONFIG_NAME);

      // Customizing assessment configuration feature is enabled but nothing is configured, start with empty config.
      if (configValue == null) {
        configValue = Constants.DEFAULT_CUSTOM_ASSESSMENT_CONFIG_VALUE;
      }
      cConfig = new CustomAssessmentConfigUtil(configValue);
    } else {
      //custom assessment config is not enabled is disabled
      throw new ServiceUnavailableException(
          "Initializing assessment configuration feature when site property is not enabled.");
    }
  }

  @Override
  public AssessmentConfig getAssessmentConfig(String clinicName, String assessmentType) {
    if (cConfig != null) {
      Map<String, Integer> instruments = cConfig.getInstruments(clinicName, assessmentType);
      if (instruments != null && !instruments.isEmpty()) {
        return new AssessmentConfig(clinicName, assessmentType, instruments, siteInfo.getSiteId());
      }
    }
    return null;
  }

  @Override
  public Set<String> getAllQuestionnaires(String assessmentType) {
    try {
      ArrayList<Element> questionnaires = xmlUtils.getProcessQuestionaires(assessmentType);
      if (questionnaires != null && !questionnaires.isEmpty()) {
        Set<String> instruments = new HashSet<>();
        for (Element questionnaire : questionnaires) {
          instruments.add(questionnaire.getAttribute("xml"));
        }
        return instruments;
      }
    } catch (Exception e) {
      logger.error("Error occurred getting all questionnaires. " + e.toString(), e);
    }
    return null;
  }
}
