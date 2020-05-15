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
package edu.stanford.registry.server;

import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.reports.PainManagementPatientReport;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomView;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.RandomSetParticipant;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.susom.database.Database;

public class DeveloperCustomizer extends PainManagementCustomizer {

  public DeveloperCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  /**
   * Configuration with definitions for viewing the API documentation and samples
   *
   * @return
   */
  @Override
  public ClientConfig getClientConfig() {

    ClientConfig clientConfig = super.getClientConfig();

    /*
      To see the api documentation a user needs to have both permissions:
          Import/export Data (ROLE_DATA_EXCHANGE)
          Schedule, Patients & Reports tab (ROLE_CLINIC_STAFF)
     */
    String[] apiPageAuthorities = {
        Constants.ROLE_DATA_EXCHANGE, Constants.ROLE_CLINIC_STAFF
    };
    clientConfig.addCustomTab(apiPageAuthorities, "docs/index.html", "API Documentation");
    /*
      To see the "Sample app tab" a user needs to have at least one of these views:
          View all API sample pages
          View API main tab sample"
      To see the "Sample patient tab" a user needs to have at least one of these views:
          View all API sample pages
          View API patient sub-tab sample
    */
    CustomView samplesView = clientConfig.addCustomView("apiSamples", "View all API sample tabs ");
    CustomView mainSampleView = clientConfig.addCustomView("apiMainSample", " View API sample app tab");
    CustomView patientSampleView = clientConfig.addCustomView("apiPatientSample", "View API sample patient tab");

    String[] mainTabAuthorities = { Constants.ROLE_CLINIC_STAFF, samplesView.getAuthorityName(), mainSampleView.getAuthorityName() };
    clientConfig.addCustomTab(mainTabAuthorities, "custom/samples/mainTabWithPatient.html", "Sample app tab");
    clientConfig.addCustomTab(mainTabAuthorities, "custom/samples/mainTabPluginPatient.html", "Sample plugin tab");

    String[] patientTabAuthorities = { Constants.ROLE_CLINIC_STAFF, samplesView.getAuthorityName(), patientSampleView.getAuthorityName() };
    clientConfig.addCustomPatientTab(patientTabAuthorities, "custom/samples/patientTabSample.html", "Sample patient tab");
    /*
      To see the "Extract square table data" tab a user needs to have the permission:
          API data extracts
     */
    clientConfig.addCustomTab(Constants.ROLE_API_EXTRACT, "custom/samples/mainTabExtract.html", "Extract square table data");

    return clientConfig;
  }

  @Override
  public Map<String, String> getEmailSubstitutions(Database database, PatientRegistration registration) {
    Map<String, String> substitutions = new HashMap<>();
    if (registration != null && registration.getPatient() != null) {
      StringBuilder sb = new StringBuilder();
      if (registration.getPatient().getFirstName() != null) {
        sb.append(registration.getPatient().getFirstName());
        sb.append(" ");
      }
      if (registration.getPatient().getLastName() != null) {
        sb.append(registration.getPatient().getLastName());
      }
      substitutions.put("[PATIENT_NAME]", sb.toString());
    }
    return substitutions;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new PainManagementPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  /**
   * Return null to remove this RandomSet from the list of buttons.  To disable the button in the
   * menu, with an optional tool tip, call part.disable(tip)
   */
  @Override
  public RandomSetParticipant disableUiTreatmentSet(Database db, RandomSetParticipant part) {
    if (part.getName().equals("DDTreatmentSet")) {
      ActivityDao dao = new ActivityDao(db, siteInfo.getSiteId());
      Activity act = dao.getLatestActivity(part.getPatientId(), Constants.ACTIVITY_COMPLETED);
      if (act == null) {
        part.disable("No survey has been completed.");
      }
    }
    return part;
  }


  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    PainSurveyScheduler scheduler = new PainSurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }
}
