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
package edu.stanford.registry.tool;

import edu.stanford.registry.server.RegistryDao;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigDao.ConfigType;
import edu.stanford.registry.server.config.SiteDao;
import edu.stanford.registry.server.database.ApptVisitDao;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserIdp;
import edu.stanford.survey.server.SurveyDao;

import com.github.susom.database.Database;

/**
 * This populates the database with all the standard information in a registry build.
 * It only serves the sites 1 and 6/pedpain.
 *
 * You'll probably want to customize the sites, the configuration properties,
 * and the email templates.  You can do by sub-classing this and just copying and
 * overriding createSites(), configSiteParameters() and configEmails()
 *
 * @author rstr
 */
public class CreateRegDataDefault extends CreateRegData {
  private AppConfigDao appDao;
  // Database db - in the parent class
  @SuppressWarnings("unused")
  public CreateRegDataDefault(Database database) {
    super(database, "1 test stub sat cat ped");
    appDao = new AppConfigDao(database, new User(1L, "admin", "Admin", 1L, "", true));
  }

  protected CreateRegDataDefault(Database database, String adminSites) {
    super(database, adminSites);
    UserIdp userIdp = createIdp();
    appDao = new AppConfigDao(database, new User(userIdp.getIdpId(), "admin", "Admin", 1L, "", true));
  }

  @Override
  public void createSurveySystemsAndStudies() {
    // TODO It'd be great to add explanations of these study systems

    createLocalSystemStudies();
    createPromisSystemStudies();
    createLocalPromis2SystemStudies();
    createLocalPromisSystemStudies();
    createStanfordCatSystemStudies();
    createStanfordCatAllowSkipSystemStudies();

    addSurveySystem("ChronicMigraineSurveyService", "chronicMigraine", "Chronic Migraine");
    addSurveySystem("edu.stanford.registry.server.survey.MedsOpioid4SurveyService", "medsOpioid4A", "4 A''s of Opioid Treatment");
    addSurveySystem("edu.stanford.registry.server.survey.HeadacheSurveyService", "headache", "Headache");
    addSurveySystem("edu.stanford.registry.server.survey.OpioidSurveysService", "opioidPromisSurvey", "PROMIS Opioid Pain Medications");
    addSurveySystem("RepeatingSurveyService", "pacMedications", "Current Medications",
                                              "pacSurgeries", "Past Surgeries");
    addSurveySystem("QualifyQuestionService-GCQ", "gcq", "Global Cannabis Questionnaire");
    addSurveySystem("ChildrenQuestionService", "children", "Children 8 to 12");
    addSurveySystem("MediaService-survey.audiopage:clinicaudio.test"); // no actual studies to add
    addSurveySystem("COPCSService");
    addSurveySystem("GISQSurveyService");
    addSurveySystem("AngerService", "testTraitAngerConsent", "Consent to survey",
       "followTraitAngerConsent", "Consent to follow up survey");
  }

  @Override
  public void createUsers() {
    super.createUsers(); // creates the admin user
    UserDao users = new UserDao(db.get(), null, null);
    users.grantAuthority(users.findDefaultIdp().getIdpId(), "admin", "BUILDER[ped]"); // add to a second site, for testing
    // Add lines like the 2 below to add the first user or 2, 3...
    //userDao.addOrEnableUser("admin", "Admin Test-User", null);
    //grantAdminAuthorities("admin", userDao);
  }

  @Override
  public void createSites() {
    SiteDao ssDao = new SiteDao(db);
    ssDao.addSite(1L, "1", "Stanford Medicine");
    ssDao.addSite(2L, "test", "Test Survey Client");
    ssDao.addSite(3L, "stub", "Test Stubbed Questions");
    ssDao.addSite(4L, "sat", "Patient Satisfaction");
    ssDao.addSite(5L, "cat", "Test Stanford CAT");
    ssDao.addSite(6L, "ped", "Pediatric Pain Management Clinic");
    ssDao.addSite(100L, "bldr", "Survey Builder Test Site");
  }

  @Override
  public void addSurveyCompletions() {
    SurveyDao surveys = new SurveyDao(db);
    surveys.addSurveyCompletePush(1L, 1L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(2L, 6L, "pdf", "Create PDF report");
  }

  protected long addPatientPartpResultTypes(RegistryDao dao, Long siteId, long typeId, String title) {
    dao.addPatientResultType(typeId, siteId, "PARPT", title);
    dao.addPatientResultType(typeId+1, siteId, "PARPTJSON", title);
    dao.addPatientResultType(typeId+2, siteId, "PARPTTEXT", title);
    return typeId + 3;
  }

  @Override
  public void addPatientResultTypes() {
    RegistryDao registry = new RegistryDao(db);
    long typeId = 1L;
    typeId = addPatientPartpResultTypes(registry, 1L, typeId, "Stanford Medicine Outcomes");
    addPatientPartpResultTypes(registry, 6L, typeId, "Pediatric Pain Management Clinic Outcomes");
  }

  @Override
  public void configSiteParameters() {
    setChoirGlobals(true);
    setChoirPain1(); // At Stanford, site 1 is a separate pain clinic with a separate database
    setChoir2ForTests();
    setChoir3thru5();
    setChoir6();
  }

  /**
   * We configure pedpain site 6 because this is what email tests use.
   * Note the email bodies are fake, so these shouldn't be used in production.
   */
  @Override
  public void configEmails() {
    /*configEmails(6, false, "CapConsent", "FollowUp18", "FollowUp18-reminder", "FollowUp2", "FollowUp2-reminder", "Initial2", "Initial2-reminder",
                 "No-appointment2", "No-appointment2-reminder", "PedPainConsent", "PedPainConsent18", "PRePCConsent");
                 */
  }

  @Override
  /*
    This is just here to remind you it's available if you subclass this file.
   */
  public void addOther() {
    // optional
  }

  // We have nothing more, for addOther()

  // ======== end of main Override methods


  /**
   * These are the values we set for Choir, a bit customized for a localhost:8080 build.
   *
   * When run out of docker, the context.xml values it sets will override these database globals.
   *
   * @param lowLog Turns off polling for new users and configs, and background jobs for import and survey advance/completion
   */
  protected void setChoirGlobals(boolean lowLog) {
    SiteParams siteParams = new SiteParams(db).setSite(0L);
    String batchInterval = "30";
    String importFreq = "2";
    if (lowLog) {
      batchInterval = "0";
      importFreq = "0";
    }
    siteParams.add("registry.batch.interval.seconds",  batchInterval);
    siteParams.add("import.process.frequency",  importFreq);  // minutes -->
    siteParams.add("importPendingFileDirectory",  "/var/tmp/{site}/xchgin"); // docker: "/appointments/{site}/xchgin");
    siteParams.add("importProcessedFileDirectory",  "/var/tmp/{site}/xchgout"); // docker
    siteParams.add("import.url",  "http://localhost:8080/registry/registry/svc/api/");

    // MRN and date/time formatting -->
    siteParams.add("PatientIdFormatterClass",  "edu.stanford.registry.server.utils.StanfordMrn");
    siteParams.add("PatientIdFormatErrorMessage",  "Patient Id must be 5-7 characters followed by - and a single digit.");
    siteParams.add("default.dateFormat",  "MM/dd/yyyy");
    siteParams.add("default.dateTimeFormat",  "MM/dd/yyyy h:mm a");

    // These control the optional links at the footer of the page -->
    siteParams.add("aboutus.link",  "https://med.stanford.edu/researchit.html");
    siteParams.add("terms.link",  "https://www.stanford.edu/site/terms.html");
    siteParams.add("contact.link",  "https://choir.stanford.edu/contact/");

    // Configure sending of emails -->
    siteParams.add("survey.link",  "https://localhost:8080/survey"); // docker/context.xml value is parameterized
    siteParams.add("registry.email.mode",  "not-prod");   // disabled here - docker/context.xml value is parameterized
    siteParams.add("registry.email.production.host",  "*");
    siteParams.add("registry.email.server",  "smtp");
    siteParams.add("registry.email.file",  "/tmp/{site}.email.log"); // tmp instead of /app/logs

    // Configure char url and resource dirs -->
    siteParams.add("chart.url",  "/registry/registry/svc/chart"); // docker/choir has /choir/registry/svc/chart
    siteParams.add("importDefinitionResource",  "{site}/xchg/");
    siteParams.add("emailTemplateResource",  "{site}/email-templates/"); // reads this only at startup for new ones

    // These are used only by ServiceProxyAuthFilter, if there's a separate survey instance
    siteParams.add("property.key.prefix",       SET_IN_PRODUCTION("registry")); // default for development"
    siteParams.add("registry.service.user",     SET_IN_PRODUCTION("registry")); // default for development"
    siteParams.add("registry.service.password", SET_IN_PRODUCTION("registry")); // default for development"

    // These are defaults for CHOIR, not set in context.xml files
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.lastsurvey.daysout", "14");
    //siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    //siteParams.add("appointment.scheduledsurvey.daysout", "90"); // no longer used by the code
    //siteParams.add("appointment.surveyexpires.afterdays", "30");

    siteParams.add(SiteInfo.TSETS_SHOW_TO_CLINIC, "Y"); // Y => show treatment set assignment to clinic staff
    siteParams.add(SiteInfo.TSETS_SHOW_EMPTY_BAR, "N"); // Y => show empty bar if no treatment sets are assigned
    siteParams.add(SiteInfo.TSETS_SHOW_FULL_STATE, "N"); // Y ==> offer more than just Not Assigned and Assigned states
}


  protected void setChoirPain1() {
    SiteParams siteParams = new SiteParams(db).setSite(1L);
    //siteParams.add("default.site", "1");  // If you have a default site- one that needs no site id on the URL, set these

    siteParams.add("appointment.daysout.load", "1");
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.lastsurvey.daysout", "14");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("appointment.scheduledsurvey.daysout", "90");
    siteParams.add("appointment_template", "apptTemplate");
    // siteParams.add("appointment.scheduledsurvey.daysout", "90"); // no longer used by the code
    siteParams.add("PromisQuestionReportFontSize", "7");
    siteParams.add("registry.email.from", "Stanford OutComes <painregistry@stanfordmed.org>");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.PainManagementCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.survey.RegistryAssessmentsService");
    siteParams.add("SurveyClassForPROMIS", "edu.stanford.registry.server.survey.PromisSurveyService");

    siteParams.add("emailTemplateResource",   "default/email-templates/");
    siteParams.add("appointment.lastsurvey.daysout", "11");

    siteParams.add("importDefinitionResource",  "default/xchg/"); // the choir-pain definitions are the default

    siteParams.add("satisfaction.service.url",  "http://localhost:8080/registry/patsat/survey2");
    siteParams.addSurveyClassFor("PROMIS",      "edu.stanford.registry.server.survey.PromisSurveyService");
    siteParams.addSurveyClassFor("Local",       "edu.stanford.registry.server.survey.RegistryAssessmentsService");
    siteParams.addSurveyClassFor("registry.email.from", "Support@Hospital.comic");

    siteParams.add("survey.link",  "https://localhost:8080/survey2?s=1");
    siteParams.add("xml_resource", "sample/painxml");  // by default, this would be "{site}/xml/"
    siteParams.addRandomSet(RandomSetsCreate.createBackPainRandomSet());
    siteParams.addRandomSet(RandomSetsCreate.createMigraineRandomSetKSort());
    siteParams.addRandomSet(RandomSetsCreate.createDDRandomSet());
    appDao.addOrEnableAppConfigEntry(siteParams.site, "physician", "physician.survey.path", "http://localhost:8787/survey2");
  }


  protected void putTestEmailTemplates(long siteNum) {
    SiteParams siteParams = new SiteParams(db).setSite(siteNum);  // "Test Survey Client"
    siteParams.setType(ConfigType.EMAILTEMPLATE);
    String[] templates = { "FollowUp", "FollowUp-reminder",
        "Initial", "Initial-reminder", "No-appointment", "No-appointment-reminder", "DDTset" };
    for (String name: templates) {
      siteParams.add(name, "Subject: Testing "+name+" template\nHello,\n"
          + "click on this link:\n[SURVEY_LINK] \n"
          + "and take the " + name + " survey by [SURVEY_DATE] \n,Yours truly,\nUs");
    }
  }


  protected void setChoir2ForTests() {
    SiteParams siteParams = new SiteParams(db).setSite(2L);  // "Test Survey Client" - AND FOR AUTOMATED DbTests
    siteParams.add("xml_dir",             NO_VALUE);   // don't set this or it takes precedence
    siteParams.add("importDefinitionResource",  "default/xchg/");
    siteParams.add("importPendingFileDirectory",   NO_VALUE);  // Disable looking for files to import
    siteParams.add("importProcessedFileDirectory", NO_VALUE);
    siteParams.add("batch.survey.handling", "false");  // turn off survey advance and completion

    siteParams.add("appointment.daysout.load", "1");
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.lastsurvey.daysout", "11");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("appointment.scheduledsurvey.daysout", "90"); // only used by a test!!!
    siteParams.add("appointment_template", "apptTemplate"); // needed?
    siteParams.add("email.template.directory", "/var/tmp"); // needed?
    siteParams.add("default.dateTimeFormat", "MM/dd/yyyy h:mm a");
    putTestEmailTemplates(siteParams.site);

    siteParams.addRandomSet(RandomSetsCreate.createBackPainRandomSet());
    siteParams.addRandomSet(RandomSetsCreate.createMigraineRandomSet());
    siteParams.addRandomSet(RandomSetsCreate.createDDRandomSet());
  }


  protected void setChoir3thru5() {
    SiteParams siteParams = new SiteParams(db).setSite(3L);
    siteParams.add("importDefinitionResource",     NO_VALUE);
    siteParams.add("importPendingFileDirectory",   NO_VALUE);  // Disable looking for files to import
    siteParams.add("importProcessedFileDirectory", NO_VALUE);
    siteParams.add("batch.survey.handling", "false");  // turn off survey advance and completion

    siteParams.setSite(4L); // "Patient Satisfaction");
    siteParams.add("importDefinitionResource",     NO_VALUE);
    siteParams.add("importPendingFileDirectory",   NO_VALUE);  // Disable looking for files to import
    siteParams.add("importProcessedFileDirectory", NO_VALUE);

    siteParams.setSite(5L); // "Test Stanford CAT");
    siteParams.add("importDefinitionResource",     NO_VALUE);
    siteParams.add("importPendingFileDirectory",   NO_VALUE);  // Disable looking for files to import
    siteParams.add("importProcessedFileDirectory", NO_VALUE);
  }


  protected void setChoir6() {
    SiteParams siteParams = new SiteParams(db).setSite(6L);  // ped = Pediatric Pain Management Clinic
    siteParams.add("appointment.lastsurvey.daysout", "30");
    siteParams.add("appointment.lastsurvey.daysout", "11");  // for test
    siteParams.add("appointment.surveyexpires.afterdays", "8");
    siteParams.add("appointment.surveyinvalid.afterdays", "8");
    siteParams.add("emailTemplateResource", "pedpain/email-templates/");  // source code has dir pedpain, not ped
    siteParams.add("registry.email.from", "Stanford Pediatric Pain Management Clinic <pediatricpain@stanfordchildrens.org>");
    siteParams.add("registry.email.toParent", "true");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.shc.pedpain.PedPainCustomizer"); // the real one is in mercury
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.survey.RegistryAssessmentsService"); // the real one is in mercury
    siteParams.add("xml_resource", "default/xml/");
    //siteParams.add("xml_resource", "pedpain/xml/");
    // Add visit codes for tests
    ApptVisitDao apptVisitDao = new ApptVisitDao(db);
    final String PROC60 = "PROC60";
    final String DAY_REHAB = "DAY REHAB";
    final String PREP = "PREP";
    final String CAPTIVATE = "CAPTIVATE";
    apptVisitDao.insertApptVisit(PROC60, PROC60, 10000L);
    apptVisitDao.insertApptVisit(DAY_REHAB, DAY_REHAB,  10001L);
    apptVisitDao.insertApptVisit(PREP, PREP, 100002L);
    apptVisitDao.insertApptVisit(CAPTIVATE, CAPTIVATE, 100003L);
    siteParams.add("xml_resource", "sample/pedxml/");
    appDao.addOrEnableAppConfigEntry(siteParams.site, "physician", "physician.survey.path", "http://localhost:8787/survey2");
    
  }


  // ====== Detailed study additions

  protected void createLocalSystemStudies() {
    addSurveySystem("Local",
        "names", "names",
        "painIntensity", "Pain Intensity",
        "bodymap", "Body Map",
        "painExperience", "Pain Experience",
        "stanfordFive", "Stanford Five and Treatment Expectations",
        "faDay", "Functional Assessment",
        "faWorking", null,
        "faDisability", null,
        "faLawsuit", null,
        "sleepImpair", "Sleep Impairment",
        "psychHistory", "Psychology History",
        "ptsd", "Post Traumatic Stress Disorder",
        "healthUtil", "Healthcare Utilization",
        "background", "Background",
        "education", "Education",
        "questions", "Questions",
        "otherPainDocs", "Other Pain Physicians",
        "smoking", "Smoking",
        "alcohol", "Alcohol and drugs",
        "painCatastrophizingScale", "Pain Catastrophizing Scale",
        "globalHealth", "Global Health",
        "research", null,
        "parentGlobalHealth", "Parent Global Health",
        "proxyPainIntensity", "Parent Proxy Pain Intensity",
        "proxyBodymap", "Parent Proxy Body Map",
        "proxyPainCatastrophizingScale", "Parent Proxy Pain Catastrophizing Scale",
        "pedPainCatastrophizingScale", "Pediatric Pain Catastrophizing Scale",
        "primaryReason", "Primary Referral Reason",
        "secondaryReason", "Secondary Referral Reason",
        "otherDiagnoses", "Other Diagnoses",
        "medsOpioid", "Opioid Pain Medications",
        "medsNerve", "Nerve Pain Medications",
        "medsHeadache", "Headache Medications",
        "medsMuscle", "Muscle Relaxant Medications",
        "medsMood", "Mood Medications",
        "medsAnxiety", "Anxiety Medications",
        "medsSleep", "Sleep Medications",
        "medsNSAID", "NSAID Medications",
        "medsADV", "Pain Medications",
        "medsCAM", "Complementary and Alternative Medications",
        "medsOther", "Other Pain Medications",
        "treatmentsRehab", "Rehabilitative Modalities",
        "treatmentsPsych", "Psychological Treatments",
        "treatmentsCAM", "Complementary and Alternative Medicine Treatments",
        "treatmentsIntervention", "Interventional Procedures",
        "treatmentsADV", "Specialized Pain Management Interventions or Surgeries",
        "treatmentsOther", "Other Interventions or Surgeries",
        "treatmentsOther", "Other Interventions or Surgeries",
        "opioidRisk", "Opioid Risk Tool",
        "opioidRiskReworded", "Opioid Risk Tool",
        "injustice", "Injustice Experience (IEQ)",
        "cpaq", "Chronic Pain Acceptance (CPAQ-8)",

        "pacBackground", "SHC Recent Surgery",
        "pacIssues", "Health Conditions",
        "pacIllness", null,
        "pacSmoking", "Smoking, Alcohol and Drugs",
        "pacAlcohol", null,
        "pacQuestions", "Questions");
  }

  protected void createPromisSystemStudies() {
    addSurveySystem("PROMIS",
        "PROMIS Pain Behavior Bank", "PROMIS Pain Behavior",
        "PROMIS Fatigue Bank", "PROMIS Fatigue",
        "PROMIS Depression Bank", "PROMIS Depression",
        "PROMIS Anxiety Bank", "PROMIS Anxiety");
  }

  protected void createLocalPromisSystemStudies() {
    addSurveySystem("LocalPromis",
        "PROMIS Pain Behavior Bank", "PROMIS Pain Behavior",
        "PROMIS Bank v1.2 - Upper Extremity", "PROMIS Physical Function - Upper Extremity",
        "PROMIS Bank v1.2 - Mobility", "PROMIS Physical Function - Mobility",
        "PROMIS Fatigue Bank", "PROMIS Fatigue",
        "PROMIS Depression Bank", "PROMIS Depression",
        "PROMIS Anxiety Bank", "PROMIS Anxiety",
        "PROMIS Bank v1.0 - Sleep Disturbance", "PROMIS Sleep Disturbance",
        "PROMIS Bank v1.0 - Sleep-Related Impairment", "PROMIS Sleep-Related Impairment",
        "PROMIS Bank v1.0 - Anger", "PROMIS Anger",
        "PROMIS Ped Bank v1.0 - Pain Interference", "PROMIS Pediatric Pain Interference",
        "PROMIS Ped Bank v1.0 - Mobility", "PROMIS Pediatric Mobility",
        "PROMIS Ped Bank v1.0 - Fatigue", "PROMIS Pediatric Fatigue",
        "PROMIS Ped Bank v1.1 - Depressive Sx", "PROMIS Pediatric Depression",
        "PROMIS Ped Bank v1.1 - Anxiety", "PROMIS Pediatric Anxiety",
        "PROMIS Ped Bank v1.0 - Peer Rel", "PROMIS Pediatric Peer Relations",
        "PROMIS Parent Proxy Bank v1.0 - Pain Interference", "PROMIS Parent Proxy Pain Interference",
        "PROMIS Parent Proxy Bank v1.0 - Mobility", "PROMIS Parent Proxy Mobility",
        "PROMIS Parent Proxy Bank v1.0 - Fatigue", "PROMIS Parent Proxy Fatigue",
        "PROMIS Parent Proxy Bank v1.1 - Depressive Sx", "PROMIS Parent Proxy Depression",
        "PROMIS Parent Proxy Bank v1.1 - Anxiety", "PROMIS Parent Proxy Anxiety",
        "PROMIS Parent Proxy Bank v1.0 - Peer Relations", "PROMIS Parent Proxy Peer Relations",
        "PROMIS Bank v2.0 - Emotional Support", "PROMIS Emotional Support",
        "PROMIS Bank v2.0 - Instrumental Support", "PROMIS Instrumental Support",
        "PROMIS Bank v2.0 - Satisfaction Roles Activities", "PROMIS Satisfaction Roles Activities",
        "PROMIS Bank v2.0 - Social Isolation", "PROMIS Social Isolation",
        "PROMIS Bank v2.0 - Ability to Participate Social", "PROMIS Ability to Participate Social",
        "PROMIS Bank v1.1 - Pain Interference", "PROMIS Pain Interference",
        "PROMIS Bank v1.2 - Physical Function", "PROMIS Physical Function");
  }

  protected void createLocalPromis2SystemStudies() {
    addSurveySystem("PROMIS.2",
        "PROMIS Pain Behavior Bank", "PROMIS Pain Behavior",
        "PROMIS Fatigue Bank", "PROMIS Fatigue",
        "PROMIS Depression Bank", "PROMIS Depression",
        "PROMIS Anxiety Bank", "PROMIS Anxiety",
        "PROMIS Bank v1.0 - Sleep Disturbance", "PROMIS Sleep Disturbance",
        "PROMIS Bank v1.0 - Sleep-Related Impairment", "PROMIS Sleep-Related Impairment",
        "PROMIS Bank v1.0 - Anger", "PROMIS Anger");
  }

  protected void createStanfordCatAllowSkipSystemStudies() {
    addSurveySystem("StanfordCatAllowSkip",
        "painInterference1", "PROMIS Pain Intensity",
        "painBehavior", "PROMIS Pain Behavior",
        "physicalFunction2", "PROMIS Physical Function",
        "fatigue2", "PROMIS Fatigue",
        "depression2", "PROMIS Depression",
        "anxiety2", "PROMIS Anxiety",
        "sleepDisturbance", "PROMIS Sleep Disturbance",
        "sleepRelatedImpairment", "PROMIS Sleep-Related Impairment",
        "anger", "PROMIS Anger");
  }

  protected void createStanfordCatSystemStudies() {
    addSurveySystem("StanfordCat",
        "painInterference1", "PROMIS Pain Interference",
        "painBehavior", "PROMIS Pain Behavior",
        "physicalFunction2", "PROMIS Physical Function",
        "fatigue2", "PROMIS Fatigue",
        "depression2", "PROMIS Depression",
        "anxiety2", "PROMIS Anxiety",
        "sleepDisturbance", "PROMIS Sleep Disturbance",
        "sleepRelatedImpairment", "PROMIS Sleep-Related Impairment",
        "anger", "PROMIS Anger");
  }

  /**
   * Create a survey system and add name,title pairs to it.
   * @param systemName Name of the survey system
   * @param namesAndTitles Names and Titles of studies for the survey system
   */
  protected void addSurveySystem(String systemName, String...namesAndTitles) {
    RegistryDao registryDao = new RegistryDao(db);
    Long systemId = registryDao.addSurveySystem(systemName);
    String name = null;
    for (String s: namesAndTitles) {
      if (name == null) {
        name = s;
        //continue;
      } else {
        registryDao.addStudy(systemId, name, s);
        name = null;
      }
    }
  }

  private UserIdp createIdp() {
    UserDao userDao = new UserDao(db.get(), null, null);
    return userDao.addOrUpdateIdp("dflt", "Default Identity Provider");
  }
}
