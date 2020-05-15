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
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class SurveyMapRegions {

  private static final Logger logger = LoggerFactory.getLogger(SurveyMapRegions.class);
  private final HashMap<MapGender, ArrayList<String>> selections = new HashMap<>();

  public SurveyMapRegions(Database db, SiteInfo siteInfo, String token, String mapName) {
    SurveyHelper surveyHelper = new SurveyHelper(siteInfo);
    SurveyDao surveyDao = new SurveyDao(db);
    SurveyQuery query = new SurveyQuery(db, surveyDao, siteInfo.getSiteId());
    Survey survey = query.surveyBySurveyToken(token);
    SurveyStep step1 = survey.answeredStepByProviderSectionQuestion(surveyHelper.getProviderId(db, "Local"), surveyHelper.getSectionId(db, mapName), "1");
    addSelections(step1, MapGender.M);
    SurveyStep step2 = survey.answeredStepByProviderSectionQuestion(surveyHelper.getProviderId(db, "Local"), surveyHelper.getSectionId(db, mapName), "2");
    addSelections(step2, MapGender.F);
  }

  private void addSelections(SurveyStep step, MapGender gender) {
    ArrayList<String> genderSelections = getGenderSelections(gender);

    if (step != null) {

      String choicesStr = step.answerRegionsCsv();
      if (choicesStr != null && !choicesStr.isEmpty()) {
        String[] choices = choicesStr.split(",");
        Collections.addAll(genderSelections, choices);
      }
    }
    selections.put(gender, genderSelections);
  }

  public boolean wasRegionSelectedOnEitherMap(String regionId) {
    logger.debug("wasRegionSelectedOnEitherMap({}) called", regionId);
    if (regionId == null) {
      return false;
    }
    if (wasRegionSelectedOnGenderMap(regionId, MapGender.M)) {
      return true;
    }
    return wasRegionSelectedOnGenderMap(regionId, MapGender.F);
  }

  public boolean wasRegionSelectedOnGenderMap(String regionId, MapGender gender) {
    if (regionId == null) {
      logger.debug("wasRegionSelectedOnGenderMap was called with null regionId, returning false");
      return false;
    }
    logger.debug("wasRegionSelectedOnGenderMap({}, {}) returning {}", regionId, gender, getGenderSelections(gender).contains(regionId));
    return getGenderSelections(gender).contains(regionId);
  }

  private ArrayList<String> getGenderSelections(MapGender gender) {
    ArrayList<String> genderSelections = selections.get(gender);
    if (genderSelections == null) {
      genderSelections = new ArrayList<>();
    }
    return genderSelections;
  }

  public enum MapGender {M, F}

  static private class SurveyHelper extends SurveyAdvanceBase {
    SurveyHelper(SiteInfo siteInfo) {
      super(siteInfo);
    }
  }
}
