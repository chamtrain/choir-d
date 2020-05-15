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
package edu.stanford.registry.server;

import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.reports.AngerReports;
import edu.stanford.registry.server.reports.EmpowerInterestReport;
import edu.stanford.registry.server.reports.OpioidResponsesReport;
import edu.stanford.registry.server.reports.PainManagementPatientReport;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.SurveyRegPushReport;
import edu.stanford.registry.server.reports.SurveyStatisticReports;
import edu.stanford.registry.server.reports.TreatmentSetReportGenerator;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.server.shc.pain.PainHl7Customizer;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptAction;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomClinicReportConfig;
import edu.stanford.registry.shared.CustomView;
import edu.stanford.registry.shared.MenuDef;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.RandomSetParticipant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PainManagementCustomizer extends RegistryCustomizerDefault {

  private static final Logger logger = LoggerFactory.getLogger(PainManagementCustomizer.class);
  public static final String CONFIG_SEND_BOTOX = "SendProcBotox";
  public static final String CONFIG_STOP_BOTOX = "StopProcBotox";

  public PainManagementCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private final String EMPOWER_NEW_VISIT = "NPV60EMPOWER";
  private final String EMPOWER_RET_VISIT = "RPV30EMPOWER";
  private final List<MenuDef> EMPOWER_VISIT_ACTIONS = Arrays.asList(
      new MenuDef("Re-print recent result", Constants.ACTION_CMD_PRINT_RECENT),
      new MenuDef("Start this assessment anyway", Constants.ACTION_CMD_START_SURVEY_POPUP)
  );

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");

    Map<String,List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("Pain Management", Arrays.asList("PAIN MANAGEMENT","PAIN MANAGEMENT - PSYCHOLOGY"));
    clinicFilterMapping.put("Neuro at Pain", Collections.singletonList("NEURO AT PAIN"));
    clinicFilterMapping.put("Neuro at Hoover", Collections.singletonList("NEURO AT HOOVER"));
    clinicFilterMapping.put("Pain Management - CCSB", Collections.singletonList("CCSB THIRD FLOOR PAIN MANAGEMENT"));
    clinicFilterMapping.put("Pain Management - Santa Clara", Collections.singletonList("PAIN MANAGEMENT - SANTA CLARA"));
    clinicFilterMapping.put("Pain Management - Emeryville", Arrays.asList("PAIN MANAGEMENT 3RD FLOOR - SHCE", "PAIN MANAGEMENT 3RD FLOOR POD2 - SHCE"));
    clinicFilterMapping.put("Pain Management - AMG San Pablo", Collections.singletonList("PAIN MANAGEMENT AMG SAN PABLO"));
    clinicFilterMapping.put("Pain Management - FMA San Jose O’Connor", Collections.singletonList("PAIN MANAGEMENT FMA SAN JOSE"));
    clinicFilterMapping.put("Pain at Pelvic Health Center", Collections.singletonList("PAIN AT PELVIC HEALTH CENTER"));
    clinicFilterMapping.put("Pain Management - Livermore", Collections.singletonList("PAIN MANAGEMENT CAP VC LIVERMORE"));
    clientConfig.setClinicFilterEnabled(true);
    clientConfig.setClinicFilterAllEnabled(true);
    clientConfig.setClinicFilterValue(null);
    clientConfig.setClinicFilterMapping(clinicFilterMapping);

    // Add more site specific attributes
    String[] values = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute(Constants.ATTRIBUTE_HEADACHE, "Headache Patient:", values);
    clientConfig.addCustomPatientAttribute(Constants.ATTRIBUTE_PROMIS_PAIN_MEDS, "Opioid Surveys:", values);
    clientConfig.addCustomPatientAttribute("painPsychology", "Pain Psychology", values);

    CustomClinicReportConfig tsetConfig = new CustomClinicReportConfig("Treatment Set Participants", "reports/tsetLast7days.html");
    tsetConfig.setWidgetType(CustomClinicReportConfig.WidgetType.api);
    CustomClinicReportConfig statsConfig = new CustomClinicReportConfig( "Survey Patient Statistics", "reports/surveyStats.html");
    statsConfig.setWidgetType(CustomClinicReportConfig.WidgetType.api);
    CustomClinicReportConfig empowerConfig = new CustomClinicReportConfig( "Patients Interested in Empower", "shc/pain/empowerInterest-report.html");
    empowerConfig.setWidgetType(CustomClinicReportConfig.WidgetType.api);
    empowerConfig.setStartDaysBack(7);
    clientConfig.setCustomReports(new CustomClinicReportConfig[] {
        tsetConfig, statsConfig, empowerConfig
    });

    CustomView angerReportView = clientConfig.addCustomView("angerReportView","View the Anger Trait survey report page" );
    String[] angerTabAuthorities = {Constants.ROLE_CLINIC_STAFF, angerReportView.getAuthorityName() };
    clientConfig.addCustomTab(angerTabAuthorities , "reports/angerSurveys.html", "Anger survey data");

    CustomView opioidsResponsesView = clientConfig.addCustomView("opioidResponsesView", "Opioids survey resposes report");
    String[] opioidTabAuthorities = {Constants.ROLE_CLINIC_STAFF, opioidsResponsesView.getAuthorityName() };
    clientConfig.addCustomTab(opioidTabAuthorities, "shc/pain/opioidResponses.html", "Opioid Use Report");

    CustomView botoxSurveysView = clientConfig.addCustomView("botoxSurveys", "View / Stop Botox surveys");
    String[] botoxSurveysAuthorities = { Constants.ROLE_CLINIC_STAFF, botoxSurveysView.getAuthorityName() };
    clientConfig.addCustomPatientTab(botoxSurveysAuthorities, "tabs/pain-botoxpushreport.html", "View/Stop Botox Surveys");
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
  public ApiReportGenerator getCustomApiReportGenerator(String reportType) {
    if (reportType.equalsIgnoreCase("tset7days")) {
      return new TreatmentSetReportGenerator();
    }
    if (reportType.startsWith("angersurveys")) {
      return new AngerReports();
    }
    if (reportType.equals("opioidResponses")) {
      return new OpioidResponsesReport();
    }
    if (reportType.equalsIgnoreCase("surveystats")) {
      return new SurveyStatisticReports();
    }
    if (reportType.equalsIgnoreCase("surveyRegsPush")) {
      return new SurveyRegPushReport();
    }
    if (reportType.equalsIgnoreCase("empowerInterest")) {
      return new EmpowerInterestReport();
    }
    return null;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    PainSurveyScheduler scheduler = new PainSurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }

  @Override
  public ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations) {
    MenuDefBeanFactory factory = AutoBeanFactorySource.create(MenuDefBeanFactory.class);
    MenuDefIntfUtils menuDefUtils = new MenuDefIntfUtils();
    ApptAction ACTION_NOTHING_EMPOWER_VISIT = new ApptAction(Constants.ACTION_TYPE_OTHER, false, "Nothing (Empower) | \u25bc", menuDefUtils.asStringArray(EMPOWER_VISIT_ACTIONS, factory));

    for (PatientRegistration registration : registrations) {
      if (registration.hasDeclined()) {
        registration.setAction(menuDefUtils.getActionNothingDeclined(factory));
      } else if (!registration.hasConsented()) {
        registration.setAction(menuDefUtils.getActionEnroll(factory));
      } else if (!registration.getIsDone()) {
        if (!registration.getSurveyRequired()) {
          registration.setAction(menuDefUtils.getActionNothingRecentlyCompleted(factory));
        } else if (registration.getNumberCompleted() > 0) {
          registration.setAction(menuDefUtils.getActionInProgress(factory));
        } else if (EMPOWER_RET_VISIT.equals(registration.getVisitType())
            || EMPOWER_NEW_VISIT.equals(registration.getVisitType())) {
          registration.setAction(ACTION_NOTHING_EMPOWER_VISIT);
        } else {
          registration.setAction(menuDefUtils.getActionAssessment(factory));
        }
      } else if (registration.getNumberPrints() == 0) {
        registration.setAction(menuDefUtils.getActionPrint(factory));
      } else {
        registration.setAction(menuDefUtils.getActionNothingPrinted(factory));
      }
    }
    return registrations;
  }

  @Override
  public boolean registrationNotifiable(Database database, PatientRegistration registration, int lastSurveyDaysOut,
                                        Date throughDate) {
    logger.debug("in registrationNotifiable for {}", registration.getVisitType());
    if (EMPOWER_NEW_VISIT.equals(registration.getVisitType())) {
      logger.trace("registrationNotifiable is returning false for assessment_reg_id {}, Empower visit type: {}", registration.getAssessmentRegId(), registration.getVisitType());
      return false;
    }
    if (EMPOWER_RET_VISIT.equals(registration.getVisitType())) {
      logger.trace("registrationNotifiable is returning false for assessment_reg_id {}, Empower visit type: {}", registration.getAssessmentRegId(), registration.getVisitType());
      return false;
    }

    ArrayList<String> excludeTypes = XMLFileUtils.getInstance(siteInfo).getExcludeFromSurveyCntVisits();
    if (excludeTypes.contains(registration.getVisitType())) {
      logger.debug("Sending notification for patient {}, ApptRegId {} even if there were completed surveys " +
              " within {} days of {} because ApptType {} is on the excludeTypes list", registration.getPatientId(),
          registration.getApptId(), lastSurveyDaysOut,
          siteInfo.getDateFormatter().getDateString(throughDate), registration.getVisitType());
      return true;
    }
    return super.registrationNotifiable(database, registration, lastSurveyDaysOut, throughDate);
  }

  @Override
  public ArrayList<String> apiExportTables() {
    String[] KNOWN_TABLES =  {  "rpt_pain_std_surveys_square", "rpt_pain_psych_square", "rpt_headache_square",
        "rpt_chronic_migraine_square", "rpt_physician_square", "rpt_treatmenthx_square"
    };
    ArrayList<String> tables = new ArrayList<>();
    Collections.addAll(tables, KNOWN_TABLES);
    return tables;
  }

  @Override
  public Hl7CustomizerIntf getHl7Customizer() {
    return new PainHl7Customizer(siteInfo);
  }
}
