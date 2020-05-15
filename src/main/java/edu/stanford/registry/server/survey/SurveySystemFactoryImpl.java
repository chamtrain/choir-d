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
import edu.stanford.registry.server.config.AppConfig;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.shc.preanesthesia.PacPatientSatisfactionSurvey;
import edu.stanford.survey.server.SurveySystem;
import edu.stanford.survey.server.SurveySystemStanfordCat;
import edu.stanford.survey.server.SurveySystemStub;
import edu.stanford.survey.server.SurveySystemTester;
import edu.stanford.survey.server.promis.Bank;

import java.util.function.Supplier;

import com.github.susom.database.Database;

/**
 * Place to figure out which survey system should be used. This must currently be
 * hard-coded to match the contents of the survey_site table in the database.
 *
 * Re-factored in Winter, 2017 to be site-based, using a base class, rather than having
 * all the site information spread across all the methods.
 */
public class SurveySystemFactoryImpl extends SurveySystemFactoryBase {

  protected final String EMPOWER_URL_KEY = "empower.service.url";
  protected final String OHSU_URL_KEY = "ohsu.service.url";

  /**
   * See the comments in SurveySystemFactoryBase.
   */
  public SurveySystemFactoryImpl(Supplier<Database> database, AppConfig config, SitesInfo sitesInfo) {
    super(database, config, sitesInfo);
    init();
    setDefaultSite(1L);
    setNullNamePropertyKey(REG_URL_KEY);
  }


  private void init() {
    final String PainCss = "painmanagement-2016-01-19.cache.css";
    final String DfltCss = "default.css";
    final String HandCss = "orthohand-2016-01-19.cache.css";
    final String SomLCss = "som-logo.css";
    final String None = "";
    final String EmpowerCss = "empower-2018-05-01.cache.css";
    
    addSite(1L,     "1",    None,    REG_URL_KEY,     PainCss, "Stanford Pain Clinic");
    addSite(2L,   "test", "stest",  REG_URL_KEY,  DfltCss, "Test Survey Client");
    addSite(3L,   "stub", "sstub",  REG_URL_KEY,  DfltCss, "Test Stubbed Questions");
    addSite(4L,   "sat",    None,    REG_URL_KEY,     DfltCss, "Patient Satisfaction");
    addSite(5L,   "cat",    None,    REG_URL_KEY,     DfltCss, "Test Stanford CAT");
    addSite(6L,   "ped",    None,    CHOIR_URL_KEY,   PainCss, "Pediatric Pain Management Clinic");
    addSite(8L,   "tj" ,    None,    CHOIR_URL_KEY,   PainCss, "Stanford Total Joint Replacement");
    addSite(9L,   "pac",    None,    CHOIR_URL_KEY,   PainCss, "Anesthesia Preoperative Assessment Clinic");
    addSite(10L,  "hand",   None,    CHOIR_URL_KEY,   HandCss, "Orthopaedic Hand Surgery");
    addSite(11L,  "ir",     None,    CHOIR_URL_KEY,   SomLCss, "Interventional Radiology");
    addSite(12L,  "cfs",    None,    CHOIR_URL_KEY,   SomLCss,    "Chronic Fatigue Syndrome");
    addSite(13L,  "ccte",   None,    CHOIR_URL_KEY,   SomLCss,    "Cancer Care Patient Feedback");
    addSite(15L,  "portho", None,    CHOIR_URL_KEY,   DfltCss, "Pediatric Orthopedic Clinic");
    addSite(16L,  "trauma", None,    CHOIR_URL_KEY,   DfltCss, "Trauma Outcomes Project");
    addSite(17L,  "psy",    None,    CHOIR_URL_KEY,   DfltCss, "Psychiatry Measurement Based Care");
    addSite(18L,  "gi",     None,    CHOIR_URL_KEY,   DfltCss, "Digestive Health");
    addSite(21L, "rwc",  "", EMPOWER_URL_KEY, EmpowerCss, "Stanford Pain Management Center");
    addSite(22L, "spc",  "", EMPOWER_URL_KEY, EmpowerCss, "Stanford Primary Care");
    addSite(23L, "va",   "", EMPOWER_URL_KEY, EmpowerCss, "Phoenix VA Health Care System");
    addSite(24L, "ih",   "", EMPOWER_URL_KEY, EmpowerCss, "Intermountain Healthcare");
    addSite(25L, "rlsl", "", EMPOWER_URL_KEY, EmpowerCss, "MedNOW Clinic");
    addSite(26L, "rls",  "", EMPOWER_URL_KEY, EmpowerCss, "Richard L. Stieg, M.D.");
    addSite(27L, "rlse", "", EMPOWER_URL_KEY, EmpowerCss, "Vail Integrative Medical Group");
    addSite(28L, "ko", "", EMPOWER_URL_KEY, EmpowerCss, "Kaiser Oakland");
    //change CHOIR_URL_KEY to OHSU_URL_KEY
    addSite(50L, "ohsu", "", OHSU_URL_KEY, DfltCss, "OHSU Youth Pain Study");
    addSite(100L, "bldr",  None,    REG_URL_KEY,      DfltCss, "Test surveybuilder surveys");
  }

  @Override
  public Long siteIdFor(String systemName) {
    return super.siteIdFor(systemName);
  }

  @Override
  public String proxyPropertyKeyFor(String systemName) {
    return super.proxyPropertyKeyFor(systemName);
  }

  @Override
  public SurveySystem systemForSiteId(Long siteId) {
    SiteInfo siteInfo = sitesInfo.getBySiteId(siteId);
    switch (siteId.intValue()) {
      case 1:   return new SurveySystemPainRegistry(database, appConfig, siteInfo);
      case 2:   return new SurveySystemTester();
      case 3:   return new SurveySystemStub();
      case 4:   return new PatientSatisfactionSurvey(database);
      case 5:   return new SurveySystemStanfordCat(Bank.physicalFunction2, false, "StanfordCat",
                                                   Bank.physicalFunction2.officialName());
      case 6:   break;
      case 8:   break;
      case 9:   break;
      case 10:  break;
      case 11:  break;
      case 12:  break;
      case 13:  break;
      case 14:  return new PacPatientSatisfactionSurvey(database, siteInfo, appConfig);
      case 15:  break;
      case 16:  break;
      case 17:  break;
      case 18:  break;
      case 50:  break;
      case 100: return new SurveySystemBuilder(database, appConfig, siteInfo);
      default: return null;
    }
    return new SurveySystemPainRegistry(database, appConfig, siteInfo);
  }

  public String[] proxyPropertyKeys() {
    return new String[] { REG_URL_KEY, CHOIR_URL_KEY, EMPOWER_URL_KEY , OHSU_URL_KEY};
  }
}
