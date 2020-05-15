/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.SurveySystem;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Override the getSurveySystemId method for psych since it has a custom survey system
 */
public class SurveyAdvanceHandlerPsych extends SurveyAdvanceHandlerGenXmlSquareTable {
  private static final Logger log = Logger.getLogger(SurveyAdvanceHandlerPsych.class);


  private SurveySystem psychSurveySystem = null;

  public SurveyAdvanceHandlerPsych(long configId, SiteInfo siteInfo) {
    super(configId, siteInfo);
  }

  @Override
  public int getSurveySystemId(Database db) {
    if (psychSurveySystem == null) {
      log.debug("Getting surveySystemId for PainPsychologyService");
      psychSurveySystem = new SurveySystDao(db).getSurveySystem("PainPsychologyService");
    }
    return psychSurveySystem.getSurveySystemId();
  }

}
