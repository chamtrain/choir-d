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
package edu.stanford.registry.server.shc.trauma;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.survey.SurveyAdvanceHandlerGenXmlSquareTable;
import edu.stanford.registry.shared.SurveySystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * Override the getSurveySystemId method for trauma since it has a custom survey system
 */
public class SurveyAdvanceHandlerTrauma extends SurveyAdvanceHandlerGenXmlSquareTable {
  private static final Logger log = LoggerFactory.getLogger(SurveyAdvanceHandlerTrauma.class);


  private SurveySystem traumaSurveySystem = null;

  public SurveyAdvanceHandlerTrauma(long configId, SiteInfo siteInfo) {
    super(configId, siteInfo);
  }

  public int getSurveySystemId(Database db) {
    if (traumaSurveySystem == null) {
      log.trace("Getting surveySystemId for TraumaSurveyService");
      traumaSurveySystem = new SurveySystDao(db).getSurveySystem("Local");
    }
    return traumaSurveySystem.getSurveySystemId();
  }
}
