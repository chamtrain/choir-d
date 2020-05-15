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
package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.hl7.Hl7CustomizerIntf;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.MenuDefIntfUtils;
import edu.stanford.registry.shared.MenuDefIntfUtils.MenuDefBeanFactory;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Created by scweber on 12/4/15.
 */
public class IRCustomizer extends RegistryCustomizerDefault {

  public IRCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(IRCustomizer.class);

  public static final String SURVEY_NOSURVEY = "NoSurvey";
  public static final String ATTR_IR_CONSENT = "irConsent";
  public static final String ATTR_DVT_BASE_DATE = "DVTBaseDate";
  public static final String ATTR_LYM_ARM_BASE_DATE = "LymArmBaseDate";
  public static final String ATTR_LYM_LEG_BASE_DATE = "LymLegBaseDate";
  public static final String ATTR_HCC_BASE_DATE = "HCCBaseDate";

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");
    String[] valueYesNo = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute(IRCustomizer.ATTR_LYM_ARM_BASE_DATE, "Lym Arm Date", ClientConfig.PATIENT_ATTRIBUTE_TYPE_DATE);
    clientConfig.addCustomPatientAttribute(IRCustomizer.ATTR_LYM_LEG_BASE_DATE, "Lym Leg Date", ClientConfig.PATIENT_ATTRIBUTE_TYPE_DATE);
    clientConfig.addCustomPatientAttribute(IRCustomizer.ATTR_DVT_BASE_DATE, "DVT Date", ClientConfig.PATIENT_ATTRIBUTE_TYPE_DATE);
    clientConfig.addCustomPatientAttribute(IRCustomizer.ATTR_HCC_BASE_DATE, "HCC Date", ClientConfig.PATIENT_ATTRIBUTE_TYPE_DATE);
    clientConfig.addCustomPatientAttribute(IRCustomizer.ATTR_IR_CONSENT, "Consented", valueYesNo);
    Map<String, List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("INTERVENTIONAL RADIOLOGY", Collections.singletonList("INTERVENTIONAL RADIOLOGY CC"));
    return clientConfig;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new IRPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    (new IRSurveyScheduler(database, siteInfo)).scheduleSurveys(endDate);
  }

  @Override
  public ArrayList<PatientRegistration> determinePatientRegActions(
      Database database, ArrayList<PatientRegistration> registrations) {
    MenuDefBeanFactory factory = AutoBeanFactorySource.create(MenuDefBeanFactory.class);
    MenuDefIntfUtils menuDefUtils = new MenuDefIntfUtils();

    for (PatientRegistration registration : registrations ) {
      if (registration.hasDeclined()) {
        registration.setAction(menuDefUtils.getActionNothingDeclined(factory));
      } else if (!registration.getIsDone()) {
        if (SURVEY_NOSURVEY.equals(registration.getSurveyType())) {
          registration.setAction(menuDefUtils.getActionAssignSurvey(factory));
        } else if (!registration.getSurveyRequired()) {
          registration.setAction(menuDefUtils.getActionNothingRecentlyCompleted(factory));
        } else if (registration.getNumberCompleted() > 0) {
          registration.setAction(menuDefUtils.getActionInProgress(factory));
        } else {
          if (!registration.hasConsented()) {
            // If the patient has a survey assigned and is not registered for surveys
            // then automatically register the patient for surveys
            registerPatient(database, siteInfo.getSiteId(), registration.getPatient());
          }
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
  public Hl7CustomizerIntf getHl7Customizer() {
    return new IRHl7Customizer(siteInfo);
  }

  private void registerPatient(Database database, Long siteId, Patient patient) {
    PatientAttribute pattribute = new PatientAttribute(
        patient.getPatientId(), Constants.ATTRIBUTE_PARTICIPATES, "y", PatientAttribute.STRING);
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    patAttribDao.insertAttribute(pattribute);
    patient.addAttribute(pattribute);
  }

}

