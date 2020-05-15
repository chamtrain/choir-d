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
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.utils.LocalPromisAssessmentUtils;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class LocalPromisScoreProvider extends PromisScoreProvider implements ScoreProvider {
  @SuppressWarnings("unused")
  private static Logger logger = Logger.getLogger(LocalPromisScoreProvider.class);
  int version = 1;

  public LocalPromisScoreProvider(SiteInfo siteInfo, int version) {
    super(siteInfo, version);
    this.version = version;
  }

  @Override
  public ArrayList<SurveyQuestionIntf> getSurvey(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
                                                 Patient patient, boolean allAnswers) {

    ArrayList<SurveyQuestionIntf> questions = new ArrayList<>();
    if (patientStudies == null || study == null || patient == null) {
      return questions;
    }
    PatientStudyExtendedData patStudy = null;
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        patStudy = patientStudy;
      }
    }
    if (patStudy == null) {
      return questions;
    }
    String xmlDocumentString = patStudy.getContents();
    if (xmlDocumentString == null || xmlDocumentString.length() < 1) {
      return questions;
    }

    //logger.debug("Calling getSurvey(" + xmlDocumentString + ")");
    return LocalPromisAssessmentUtils.getSurvey(xmlDocumentString, LocalPromisAssessmentUtils.itemBankFor(study));

  }
}
