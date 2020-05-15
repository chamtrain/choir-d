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
import edu.stanford.survey.server.SurveyStep;

public class SurveyHelper  extends SurveyAdvanceBase {

    public SurveyHelper(SiteInfo siteInfo) {
      super(siteInfo);
    }

    @Override
    public Integer getSelect1Response(Survey s, String provider, String section, String questionId, String fieldId) {
      SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section, questionId);
      if (step != null) {
        Integer value = selectedFieldInt(step, fieldId);
        return value == null ? 0 : value;
      } else {
        return 0;
      }
    }
  }