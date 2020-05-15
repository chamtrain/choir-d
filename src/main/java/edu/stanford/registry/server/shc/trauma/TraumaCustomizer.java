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

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.reports.SquareTableExportGenerator;
import edu.stanford.registry.server.service.ReportGenerator;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.CustomClinicReportConfig;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientRegistration;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
/**
 * Customizer for Trauma Outcomes Project. Created by tpacht on 62/16/2017.
 * Specified in APP_CONFIG: config_type "configparam", configName "RegistryCustomizerClass"
 */
@SuppressWarnings("unused")
public class TraumaCustomizer extends RegistryCustomizerDefault {

  @SuppressWarnings("unused")
  public TraumaCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private static final Logger logger = LoggerFactory.getLogger(TraumaCustomizer.class);
  private static final String SQUARETABLEREPORT = "Survey responses";

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");

    String[] empty = {};
    clientConfig.addCustomPatientAttribute("Family1", "First Care Giver Name", empty);
    clientConfig.addCustomPatientAttribute("Family2", "First Care Giver Email", empty);
    clientConfig.addCustomPatientAttribute("Family3", "Second Care Giver Name", empty);
    clientConfig.addCustomPatientAttribute("Family4", "Second Care Giver Email", empty);
    String[] values = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute("traumaConsent", "Consented", values);

    clientConfig.setCustomReports(new CustomClinicReportConfig[] {
        new CustomClinicReportConfig(SQUARETABLEREPORT, SQUARETABLEREPORT)
    });
    return clientConfig;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new PatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public List<File> getEmailAttachments(String template) {
    logger.debug("getEmailAttachments was called for template {}", template);
    return null;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    /* Site owner requested we stop sending followup surveys 04/23/2018
    SurveyScheduler scheduler = new SurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
    */
  }

  /**
   * Include only the ones for the patient, excluding the family ones that are for the caregivers
   */
  @Override
  public ArrayList<PatientRegistration> getPatientRegistrations(AssessDao assessDao, Patient pat, Date date) {
    ArrayList<PatientRegistration> allRegistrations = assessDao.getPatientRegistrations(pat.getPatientId(), date);
    ArrayList<PatientRegistration> patRegistrations = new ArrayList<>();
    for (PatientRegistration registration : allRegistrations) {
      if (registration.getSurveyType() != null && !registration.getSurveyType().startsWith("Family")) {
        patRegistrations.add(registration);
      }
    }
    return patRegistrations;
  }

  @Override
  public ReportGenerator getCustomReportGenerator(String reportType) {
    if (reportType.equals(SQUARETABLEREPORT)) {
      return new SquareTableExportGenerator("RPT_TRAUMA_SURVEYS");
    }
    return null;
  }

  @Override
  public ArrayList<String> apiExportTables() {
    ArrayList<String> tables = new ArrayList<>();
    tables.add("rpt_trauma_surveys");
    return tables;
  }

}

