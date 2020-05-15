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

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.ScoresExportReport;
import edu.stanford.registry.server.service.ApiReportGenerator;
import edu.stanford.registry.shared.ApptAction;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.CustomClinicReportConfig;
import edu.stanford.registry.shared.MenuDef;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.PatientRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class PedOrthoCustomizer extends RegistryCustomizerDefault {

  public static final String SURVEY_NOSURVEY = "NoSurvey";
  public static final String SURVEY_INELIGIBLE = "Ineligible";
  public static final String SURVEY_SRS30 = "SRS30";
  public static final String SURVEY_SRS30_HM = "SRS30+HM";

  public final static String[] YESNO = { "", "Y", "N" };
  public final static String ATTR_SCOLIOSIS = "scoliosis";
  public final static String ATTR_SCOLIOSIS_TREATMENT_DATE = "scoliosisTreatmentDate";
  public final static String ATTR_SCOLIOSIS_CONSENT = "ScoliosisConsent";
  public final static String ATTR_SCOLIOSIS_PARENT_CONSENT = "ScoliosisParentConsent";
  public final static String ATTR_SCOLIOSIS_CHILD_ASSENT = "ScoliosisChildAssent";

  public static final String ATTR_REFUSED_SURVEY = "RefusedSurvey";

  final static String ATTR_TIMING_PARENT_CONSENT = "TimingParentConsent";
  final static String ATTR_TIMING_CHILD_ASSENT = "TimingChildAssent";
  //-------------------------------------------------------
  // Define custom actions
  //-------------------------------------------------------

  protected static final String[][] PARAMS_REFUSED_SURVEY = new String[][] {
    new String[] { Constants.ACTION_CMD_PARAM_NAME, ATTR_REFUSED_SURVEY },
    new String[] { Constants.ACTION_CMD_PARAM_VALUE, "true"}
  };

  private static final List<MenuDef> ENROLL_ACTIONS = Arrays.asList(new MenuDef("Register this patient", Constants.ACTION_CMD_ENROLL_POPUP),
      new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP));

  private static final List<MenuDef> ASSIGN_SURVEY_ACTIONS = Arrays.asList(new MenuDef("Assign survey", Constants.ACTION_CMD_ASSIGN_SURVEY_POPUP));

  private static final List<MenuDef> ASSESSMENT_ACTIONS = Arrays.asList(new MenuDef("Start assessment", Constants.ACTION_CMD_START_SURVEY_POPUP),
      new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP),
      new MenuDef("Patient Refused", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_REFUSED_SURVEY));

  private static final List<MenuDef> IN_PROGRESS_ACTIONS = Arrays.asList(new MenuDef("Continue assessment", Constants.ACTION_CMD_START_SURVEY_POPUP),
      new MenuDef("Print partial result", Constants.ACTION_CMD_PRINT),
      new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP),
      new MenuDef("Patient Refused", Constants.ACTION_CMD_SET_SURVEY_ATTR, PARAMS_REFUSED_SURVEY));

  private static final List<MenuDef> RECENTLY_COMPLETED_ACTIONS = Arrays.asList(new MenuDef("Re-print recent result", Constants.ACTION_CMD_PRINT_RECENT),
      new MenuDef("Start this assessment anyway", Constants.ACTION_CMD_START_SURVEY_POPUP));


  private static final Logger logger = LoggerFactory.getLogger(PedOrthoCustomizer.class);
  public PedOrthoCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();

    Map<String,List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("Emeryville", Collections.singletonList("ORTHO & SPORTS MED EMERYVILLE 6121 HOLLIS"));
    clinicFilterMapping.put("Los Gatos", Collections.singletonList("ORTHO & SPORTS MED LOS GATOS 555 KNOWLES"));
    clinicFilterMapping.put("Palo Alto - Welch", Collections.singletonList("ORTHO & SPORTS MED PALO ALTO 730 WELCH"));
    clinicFilterMapping.put("Pleasanton", Collections.singletonList("ORTHO & SPORTS MED PLEASANTON 5000 PLEASANTON"));
    clinicFilterMapping.put("San Francisco - CPMC", Collections.singletonList("ORTHO & SPORTS MED SAN FRANCISCO 3801 SACRAMENTO"));
    clinicFilterMapping.put("Walnut Creek", Collections.singletonList("ORTHO & SPORTS MED WALNUT CREEK 106 LA CASA VIA"));

    clientConfig.setClinicFilterEnabled(true);
    clientConfig.setClinicFilterAllEnabled(false);
    clientConfig.setClinicFilterValue("Palo Alto - Welch");
    clientConfig.setClinicFilterMapping(clinicFilterMapping);

    clientConfig.addCustomPatientAttribute(ATTR_SCOLIOSIS, "Scoliosis", YESNO);
    clientConfig.addCustomPatientAttribute(ATTR_SCOLIOSIS_TREATMENT_DATE, "Treatment Date",
        ClientConfig.PATIENT_ATTRIBUTE_TYPE_DATE);

    CustomClinicReportConfig exportConfig = new CustomClinicReportConfig( "Export Patient Scores", "shc/pedortho/exportScores.html");
    exportConfig.setWidgetType(CustomClinicReportConfig.WidgetType.api);
    clientConfig.setCustomReports(new CustomClinicReportConfig[] {
        exportConfig });

    return clientConfig;
  }

  @Override
  public ApiReportGenerator getCustomApiReportGenerator(String reportType) {
    if ("exportScores".equalsIgnoreCase(reportType)) {
      return new PedOrthoReportGenerator();
    }
    logger.warn("getCustomApiReportGenerator called for unrecognized report type {}", reportType);
    return null;
  }

  @Override
  public ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations) {
    MenuDefBeanFactory menuDefBeanFactory = AutoBeanFactorySource.create(MenuDefBeanFactory.class);
    MenuDefIntfUtils menuDefUtils = new MenuDefIntfUtils();
    final ApptAction ACTION_ENROLL = new ApptAction(Constants.ACTION_TYPE_ENROLL, false, Constants.OPT_ENROLL, menuDefUtils.asStringArray(ENROLL_ACTIONS, menuDefBeanFactory));
    final ApptAction ACTION_ASSIGN_SURVEY = new ApptAction(Constants.ACTION_TYPE_OTHER, false, Constants.OPT_ASSIGN_SURVEY, menuDefUtils.asStringArray(ASSIGN_SURVEY_ACTIONS, menuDefBeanFactory));
    final ApptAction ACTION_ASSESSMENT = new ApptAction(Constants.ACTION_TYPE_ASSESSMENT, true, Constants.OPT_ASSESSMENT, menuDefUtils.asStringArray(ASSESSMENT_ACTIONS, menuDefBeanFactory));
    final ApptAction ACTION_IN_PROGRESS = new ApptAction(Constants.ACTION_TYPE_IN_PROGRESS, true, Constants.OPT_IN_PROGRESS, menuDefUtils.asStringArray(IN_PROGRESS_ACTIONS, menuDefBeanFactory));
    final ApptAction ACTION_NOTHING_RECENTLY_COMPLETED = new ApptAction(Constants.ACTION_TYPE_OTHER, false, Constants.OPT_NOTHING_RECENTLY_COMPLETED, menuDefUtils.asStringArray(RECENTLY_COMPLETED_ACTIONS, menuDefBeanFactory));

    for (PatientRegistration registration : registrations ) {
      if (registration.hasDeclined()) {
        registration.setAction(menuDefUtils.getActionNothingDeclined(menuDefBeanFactory));
      } else if (!registration.hasConsented()) {
        registration.setAction(ACTION_ENROLL);
      } else {
        String surveyType = registration.getSurveyType();
        switch (surveyType) {
        case SURVEY_INELIGIBLE:
          registration.setAction(menuDefUtils.getActionNothingIneligible(menuDefBeanFactory));
          break;
        case SURVEY_NOSURVEY:
          registration.setAction(ACTION_ASSIGN_SURVEY);
          break;
        default:
          if (!registration.getIsDone()) {
            if (!registration.getSurveyRequired()) {
              registration.setAction(ACTION_NOTHING_RECENTLY_COMPLETED);
            } else if (registration.getNumberCompleted() > 0) {
              registration.setAction(ACTION_IN_PROGRESS);
            } else {
              registration.setAction(ACTION_ASSESSMENT);
            }
          } else if (registration.getNumberPrints() == 0) {
            registration.setAction(menuDefUtils.getActionPrint(menuDefBeanFactory));
          } else {
            registration.setAction(menuDefUtils.getActionNothingPrinted(menuDefBeanFactory));
          }
          break;
        }
      }
    }
    return registrations;
  }

  @Override
  public ScoresExportReport getScoresExportReport(Database database, Map<String,String[]> params, SiteInfo siteInfo) {
   return new PedOrthoSurveyData(database, siteInfo);
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new PedOrthoPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public Hl7CustomizerIntf getHl7Customizer() {
    return new PedOrthoHl7Customizer(siteInfo);
  }
}
