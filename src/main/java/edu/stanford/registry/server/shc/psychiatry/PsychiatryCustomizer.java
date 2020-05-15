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

package edu.stanford.registry.server.shc.psychiatry;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomView;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;

public class PsychiatryCustomizer extends RegistryCustomizerDefault {
  public static final String PSY_CONSENT = "psyConsent";
  public static final String CONTINUITY_CLINIC = "Continuity Clinic";
  public static final String CONFIDENTIAL_SUPPORT_TEAM = "Confidential Support Team";
  public static final String PSYCHOSOCIAL_TREATMENT_CLINIC = "Psychosocial Treatment Clinic";
  public static final String SPORT_MEDICINE = "Sports Medicine";
  public static final String WOMENS_WELLNESS_CLINIC = "Womens Wellness Clinic";
  public static final String DEFAULT_SURVEY_TYPE = "Default";

  @SuppressWarnings("unused")
  public PsychiatryCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");
    String[] values = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute(PSY_CONSENT, "Consented", values);

    clientConfig.setClinicFilterEnabled(true);
    clientConfig.setClinicFilterAllEnabled(true);
    clientConfig.setClinicFilterValue(null);

    Map<String, List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put(CONTINUITY_CLINIC, Collections.singletonList(CONTINUITY_CLINIC));
    clinicFilterMapping.put(CONFIDENTIAL_SUPPORT_TEAM, Collections.singletonList(CONFIDENTIAL_SUPPORT_TEAM));
    clinicFilterMapping.put(PSYCHOSOCIAL_TREATMENT_CLINIC, Collections.singletonList(PSYCHOSOCIAL_TREATMENT_CLINIC));
    clinicFilterMapping.put(SPORT_MEDICINE, Collections.singletonList(SPORT_MEDICINE));
    clinicFilterMapping.put(WOMENS_WELLNESS_CLINIC, Collections.singletonList(WOMENS_WELLNESS_CLINIC));
    clientConfig.setClinicFilterMapping(clinicFilterMapping);

    CustomView patientReport = clientConfig.addCustomView("latestPatientSurvey", "Latest Completed Survey Report");
    String[] patientReportAuthorities = { Constants.ROLE_CLINIC_STAFF, patientReport.getAuthorityName() };
    clientConfig.addCustomPatientTab(patientReportAuthorities, "tabs/latestpatientsurveyreport.html", "Latest Completed Survey Report");

    return clientConfig;
  }

  @Override
  public Hl7CustomizerIntf getHl7Customizer() {
    return new PsychiatryHl7Customizer(siteInfo);
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    // TODO: is there any scheduling done here?
  }
}

