/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.gi;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.server.reports.PatientReport;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.susom.database.Database;

public class GICustomizer extends RegistryCustomizerDefault {

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  public static final String RESEARCH_STUDY_ESOPHAGEAL = "esophageal";
  // Consent state values

  public static final String CONSENT_STATE_AGREED = "Y";
  public static final String CONSENT_STATE_DECLINED = "N";

  public static final String PATTR_RESEARCH_STUDY = "researchStudy";
  public static final String PATTR_CONSENT = "consent";

  public GICustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }
  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    Map<String, List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("DIGESTIVE HEALTH CENTER - OPC", Collections.singletonList("DIGESTIVE HEALTH CENTER - OPC"));
    return clientConfig;
  }

  @Override
  public ArrayList<String> apiExportTables() {
    ArrayList<String> tables = new ArrayList<>();
    tables.add("rpt_gi_surveys");
    return tables;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new GIPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

}
