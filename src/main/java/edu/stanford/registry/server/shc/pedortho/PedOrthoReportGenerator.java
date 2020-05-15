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
package edu.stanford.registry.server.shc.pedortho;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;

import java.util.List;
import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class PedOrthoReportGenerator extends ApiReportCommon implements ApiReportGenerator {

  private static final Logger logger = LoggerFactory.getLogger(PedOrthoReportGenerator.class);
  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) throws ApiStatusException {
    if (reportName != null && reportName.trim().equals("exportScores")) {
      PedOrthoSurveyData pedOrthoSurveyData = new PedOrthoSurveyData(databaseProvider.get(), siteInfo);
      JSONObject jsonObject = jsonRepresentation.getJsonObject();
      if (jsonObject == null) {
        logger.warn("handling report {} jsonObject is NULL!!!", reportName);
      }

      List<List<Object>> reportData = pedOrthoSurveyData.getReportData(siteInfo.getSiteId(), getStartDt(jsonObject), getEndDt(jsonObject));
      return getJSONObject(reportData);
    }
    logger.warn("Unrecognized report {} called, returning empty response", reportName);
    return new JSONObject();
  }

  @Override
  public JSONObject getReportParameters(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName)  {
    JSONObject params = new JSONObject();
    params.accumulate("reportParameters", makeReportInputOption("From", "fromDt", "date"));
    params.accumulate("reportParameters", makeReportInputOption("To", "toDt", "date"));
    return params;
  }
}
