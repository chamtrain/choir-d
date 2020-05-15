package edu.stanford.registry.tool;

import edu.stanford.registry.server.RegistryDao;
import edu.stanford.registry.server.config.SiteDao;
import edu.stanford.survey.server.SurveyDao;

import com.github.susom.database.Database;

/**
 * This is to build a system like Stanford's actual release.
 * If you use docker, docker's context.xml properties will override some of these.
 *
 * @author rstr
 */
public class CreateRegDataMercury extends CreateRegDataDefault {
  public CreateRegDataMercury(Database database) {
    super(database, "1 ped tj pac hand ir cfs ccte ccpnprostate portho");
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

    ssDao.addSite(8L, "tj", "Stanford Total Joint Replacement Study");
    ssDao.addSite(9L, "pac", "Pre-Anesthesia Clinic");
    ssDao.addSite(10L, "hand", "Ortho Hand Clinic");
    ssDao.addSite(11L, "ir", "Interventional Radiology Clinic");
    ssDao.addSite(12L, "cfs", "Chronic Fatigue Syndrome");
    ssDao.addSite(13L, "ccte", "Patient Feedback");
    ssDao.addSite(14L, "ccpnprostate", "Patient Navigator - Prostate Cancer");
    ssDao.addSite(15L, "portho", "Pediatric Orthopedic Clinic");
    ssDao.addSite(100L, "bldr", "Survey Builder Test Site");
  }

  @Override
  public void addPatientResultTypes() {
    RegistryDao registry = new RegistryDao(db);
    long typeId = 1L;
    typeId = addPatientPartpResultTypes(registry, 1L, typeId, "Stanford Medicine Outcomes");
    typeId = addPatientPartpResultTypes(registry, 6L, typeId, "Pediatric Pain Management Clinic Outcomes");
    typeId = addPatientPartpResultTypes(registry, 8L, typeId, "Stanford Total Joint Replacement Study");
    typeId = addPatientPartpResultTypes(registry, 9L, typeId, "SHC Pre-Anesthetic Questionnaire");
    typeId = addPatientPartpResultTypes(registry, 10L, typeId, "Ortho Hand Questionnaire");
    typeId = addPatientPartpResultTypes(registry, 11L, typeId, "Interventional Radiology Questionnaire");
    typeId = addPatientPartpResultTypes(registry, 12L, typeId, "Chronic Fatigue Syndrome Questionnaire");
    typeId = addPatientPartpResultTypes(registry, 13L, typeId, "Patient Feedback Questionnaire");
    typeId = addPatientPartpResultTypes(registry, 14L, typeId, "Prostate Cancer Questionnaire");
    typeId = addPatientPartpResultTypes(registry, 15L, typeId, "Pediatric Orthopedic Clinic Study");
  }

  @Override
  public void addSurveyCompletions() {
    SurveyDao surveys = new SurveyDao(db);
    surveys.addSurveyCompletePush(1L, 1L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(2L, 6L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(3L, 8L, "squareTable", "TotalJoint survey complete handler");
    surveys.addSurveyCompletePush(4L, 9L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(5L, 10L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(6L, 11L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(7L, 12L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(8L, 13L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(9L, 14L, "pdf", "Create PDF report");
    surveys.addSurveyCompletePush(10L, 15L, "pdf", "Create PDF report");
  }

  @Override
  public void configSiteParameters() {
    setChoirGlobals(false);  // true => turn off polling for config changes, appt import, survey updates
    setChoirGlobalsMerc();

    setChoirPain1(); // At Stanford, site 1 is a separate pain clinic with a separate database
    setChoir2ForTests();
    setChoir3thru5();
    setChoir6();
    setChoir6Merc();

    setChoir8(); // these are just for mercury
    setChoir9();
    setChoir10();
    setChoir11();
    setChoir12();
    setChoir13();
  }

  /**
   * We configure pedpain site 6 because this is what email tests use.
   * Note the email bodies are fake, so these shouldn't be used in production.
   */
  @Override
  public void configEmails() {
    // 1 comes from default/email-templates
    /*
    configEmails(6, false, "CapConsent", "FollowUp18", "FollowUp18-reminder", "FollowUp2", "FollowUp2-reminder", "Initial2", "Initial2-reminder",
        "No-appointment2", "No-appointment2-reminder", "PedPainConsent", "PedPainConsent18", "PRePCConsent");
    configEmails(8, false, "FollowUp", "FollowUp-reminder", "Initial", "Initial-reminder",
        "No-appointment", "No-appointment-reminder", "TJAOutcomesConsent");
    configEmails(9, false, "FollowUp", "FollowUp-reminder", "Initial", "Initial-reminder",
        "No-appointment", "No-appointment-reminder");
    // TBD setChoir10();
    // TBD setChoir11();
    configEmails(12, true);
    configEmails(13, true);
    */
  }

  @Override
  /**
   * This is just here to remind you it's available if you subclass this file.
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
   * @param lowlog Turns off polling for new users and configs, and background jobs for import and survey advance/completion
   */
  protected void setChoirGlobalsMerc() {
    SiteParams siteParams = new SiteParams(db).setSite(0L);
    siteParams.add("factory.survey.system",  "edu.stanford.mercury.registry.SurveySystemFactoryImpl");
    siteParams.add("factory.survey.advance",  "edu.stanford.mercury.registry.SurveyAdvanceHandlerFactoryImpl");
    siteParams.add("factory.survey.complete",  "edu.stanford.mercury.registry.SurveyCompleteHandlerFactoryImpl");
    // Docker overrides these by setting them in context.xml
  }


  protected void setChoir6Merc() {
    SiteParams siteParams = new SiteParams(db).setSite(6L);  // ped = Pediatric Pain Management Clinic
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.shc.pedpain.PedPainCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.shc.pedpain.PedPainSurveyService");
  }

  protected void setChoir8() {
    SiteParams siteParams = new SiteParams(db).setSite(8L); // "Stanford Total Joint Replacement Study
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.lastsurvey.daysout", "42");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("appointment.surveyexpires.afterdays", "30");
    siteParams.add("appointment.surveyinvalid.afterdays", "30");
    siteParams.add("importDefinitionResource", "totaljoint/xchg/");
    siteParams.add("registry.email.from", "Stanford-joint-replacement@stanford.edu");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.mercury.registry.totaljoint.TotalJointCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.mercury.registry.totaljoint.TotalJointSurveyService");
    siteParams.add("xml_resource", "totaljoint/xml/");
  }

  protected void setChoir9() {
    SiteParams siteParams = new SiteParams(db).setSite(9L); // "Pre-Anesthesia Clinic
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.lastsurvey.daysout", "7");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("factory.survey.advance", "edu.stanford.registry.server.shc.preanesthesia.PreAnesthesiaAdvanceHandler");
    siteParams.add("importDefinitionResource", "preanesthesia/xchg/");
    siteParams.add("PromisQuestionReportFontSize", "7");
    siteParams.add("registry.email.from", "Stanford Medicine Outcomes <painregistry@stanfordmed.org>");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.shc.preanesthesia.PreAnesthesiaCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.shc.preanesthesia.PreAnesthesiaService");
    siteParams.add("SurveyClassForRepeatingSurveyService", "edu.stanford.registry.server.shc.preanesthesia.PreAnesthesiaRepeatingService");
    siteParams.add("xml_resource", "preanesthesia/xml/");
  }

  protected void setChoir10() {
    SiteParams siteParams = new SiteParams(db).setSite(10L);  // "Ortho Hand Clinic
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.lastsurvey.daysout", "42");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("appointment.surveyexpires.afterdays", "30");
    siteParams.add("appointment.surveyinvalid.afterdays", "30");
    siteParams.add("importDefinitionResource", "orthohand/xchg/");
    siteParams.add("registry.email.from", "Stanford Orthopedic Hand Clinic <hand_clinic_choir@lists.stanford.edu>");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.shc.orthohand.OrthoHandCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.shc.orthohand.OrthoHandSurveyService");
    siteParams.add("xml_resource", "orthohand/xml/");
  }

  protected void setChoir11() {
    SiteParams siteParams = new SiteParams(db).setSite(11L);  // "Interventional Radiology Clinic
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.lastsurvey.daysout", "14");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("registry.email.from", "Interventional Radiology Clinic <nobody@stanford.edu>");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.shc.interventionalradiology.IRCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.shc.interventionalradiology.IRSurveyService");
  }

  protected void setChoir12() {
    SiteParams siteParams = new SiteParams(db).setSite(12L);  // CFS - Chronic Fatigue Syndrome Clinic
    siteParams.add("emailTemplateResource", "cfs/email-templates/");
    siteParams.add("registry.email.from", "Chronic Fatigue Clinic <cfsresearchteam@stanford.edu>");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.mercury.registry.cfs.CFSCustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.mercury.registry.cfs.CFSSurveyService");
  }

  protected void setChoir13() {
    SiteParams siteParams = new SiteParams(db).setSite(13L);  // CCTE -
    siteParams.add("appointment.initialemail.daysout", "7");
    siteParams.add("appointment.lastsurvey.daysout", "14");
    siteParams.add("appointment.noemail.withindays", "2");
    siteParams.add("appointment.reminderemail.daysout", "4,1");
    siteParams.add("registry.email.from", "Stanford Cancer Center <nobody@stanford.edu>");
    siteParams.add("RegistryCustomizerClass", "edu.stanford.registry.server.shc.ccte.CCTECustomizer");
    siteParams.add("SurveyClassForLocal", "edu.stanford.registry.server.shc.ccte.CCTESurveyService");
  }

}
