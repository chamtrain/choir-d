/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.utils.ReportUtils;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;

import java.io.IOException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
/**
 * Create PDF reports upon completion of each survey.
 */
public class SurveyCompleteHandlerPdf implements SurveyCompleteHandler {
  private SiteInfo siteInfo;
  private static Logger logger = LoggerFactory.getLogger(SurveyCompleteHandlerPdf.class);


  public SurveyCompleteHandlerPdf(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }


  @Override
  public boolean surveyCompleted(SurveyComplete survey, Supplier<Database> dbp) {
    if (!siteInfo.getSiteId().equals(survey.getSurveySiteId())) {
      logger.debug(siteInfo.getIdString()+" not processing completed survey token id " + survey.getSurveyTokenId() + " for site " + survey.getSurveySiteId());
      return false;
    }
    AssessDao assessDao = new AssessDao(dbp.get(), siteInfo);
    Long apptRegId = assessDao.getApptRegIdByTokenId(survey.getSurveyTokenId());
    if (apptRegId == null) {
      logger.debug("apptRegId not found for siteId " + survey.getSurveySiteId() + " token_id " + survey.getSurveyTokenId());
      return false;
    }

    PatientRegistration registration = assessDao.getPatientRegistrationByRegId(new ApptId(apptRegId));
    if (registration == null) {
      throw new RuntimeException("no registration for apptRegId " + apptRegId);
    }

    if (registration.getNumberPending() != 0) {
      logger.debug("Appt registration not completed for survey token id " + survey.getSurveyTokenId() + " siteId " + survey.getSurveySiteId());
      return false;
    }

    try {
      ReportUtils report = new ReportUtils(siteInfo);
      RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
      AssessmentRegistration assessment = registration.getAssessment();
      report.generatePdf(dbp.get(), customizer.getPatientReport(dbp.get(), siteInfo), assessment,
          customizer.getConfigurationOptions(), true);
      return true;
    } catch (IOException e) {
      logger.error("generatePdf threw:", e);
      throw new RuntimeException("generatePdf threw " + e.toString());
    }
  }
}
