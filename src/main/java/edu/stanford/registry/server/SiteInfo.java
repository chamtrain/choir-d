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

import edu.stanford.registry.server.config.PatientIdFormatterFactory;
import edu.stanford.registry.server.config.PropertyMap;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.randomset.RandomSetFactory;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.MailerFactory;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.Site;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

/**
 * This should not be cached longer than one service call, unless the holder checks
 * its revisionNumber against newer ones to ensure it's current.
 *
 * If the configuration changes, the copy cached in SitesInfo is replaced.
 *
 * This, and the resources cached here, should never store a database connection,
 * since most are only good for one service call.
 */
public class SiteInfo {
  private static final Logger logger = LoggerFactory.getLogger(SiteInfo.class);

  private final Site site;
  private long revisionNumber;
  protected Map<String,String>globalParams;
  protected HashMap<String,String>localParams;
  private boolean markHasBeenChecked;

  // All the site-specific resources cached here, initialized by initResources()
  private HashMap<String, String> clientParams;  // subset to move across the wire
  private Mailer mailer;
  private PatientIdFormatIntf patientIdFormatter;
  protected HashMap<String,String>emailTemplates;
  private HashMap<String,String>reminderTemplates;
  private HashMap<String,String>initialTemplates;
  private HashMap<String, RandomSetter> treatmentSets;
  private DateTimeFormatter dateTimeFormatter;
  private DateFormatter dateFormatter;
  private RegistryCustomizer registryCustomizer;


  /**
   * Creating a SiteInfo just creates the Site part of it. initSiteConfig() must be called
   */
  public SiteInfo(Long surveySiteId, String urlParam, String displayName, boolean enabled) {
    site = new Site(surveySiteId, urlParam, displayName, enabled);
  }

  // Exports info about the site, but doesn't extend Site since Site is Serializable

  public String getIdString() {
    return site.getIdString();
  }

  public Long getSiteId() {
    return site.getSiteId();
  }

  public String getUrlParam() {
    return site.getUrlParam();
  }

  public String getDisplayName() {
    return site.getDisplayName();
  }

  /**
   * Returns the URL parameter. There is no other site name.
   */
  public String getSiteName() {
    return getUrlParam();
  }

  /**
   * Initializes the site's configuration information from the app_config database.
   * @param globalSettings web server's global defaults
   * @return the initialized SiteInfo
   */
  public SiteInfo initSiteConfig(Supplier<Database> dbp, SitesInfo sitesInfo, Map<String,String> globalSettings,
                                 HashMap<String,String> localSettings,
                                 HashMap<String,String> emailTemplates,
                                 HashMap<String,String> treatmentSets) {
    // revisionNumber was set if/when it was decided this new SiteInfo would be needed
    globalParams = globalSettings;
    localParams = localSettings;
    clientParams = null;  // it'll be created the first time it's needed
    markHasBeenChecked = true;

    initResources(dbp, emailTemplates, treatmentSets);
    reportOnConfig();
    return this;
  }

  public long getRevisionNumber() {
    return revisionNumber;
  }

  public void setRevisionNumber(long newNumber) {
    revisionNumber = newNumber;
  }

  /**
   * Call if it is still used, but doesn't need to be updated
   */
  public void mark() {
    markHasBeenChecked = true;
  }

  /**
   * Side effect: clears the mark.
   * @return true if it had been marked (updated)
   */
  public boolean wasMarkedLeaveClear() {
    boolean was = markHasBeenChecked;
    markHasBeenChecked = false;
    return was;
  }

  // ==== Site or if null, Global

  /**
   * @return a site-specific parameter, or if there's none, a global default.
   */
  public String getProperty(String key) {
    if (localParams == null) {
      throw new RuntimeException("SurveySite "+site.getUrlParam()+" does not have its properties initialized.");
    } else if (globalParams == null) {
      throw new RuntimeException("SurveySite "+site.getUrlParam()+" has local, but no global properties initialized.");
    }

    String value = localParams.get(key);
    if (value != null) {
      return value;
    }

    return globalParams.get(key);
  }

  public String getProperty(String key, String dflt) {
    String value = getProperty(key);
    return (value != null) ? value : dflt;
  }

  public boolean getProperty(String key, boolean dflt) {
    return PropertyMap.getBool(getProperty(key), dflt);
  }

  /**
   * Return the property value. If it's not set, return the passed default,
   * replacing any occurrence of "{site}" with the site urlParam.
   */
  public String getPathProperty(String key, String dflt) {
    String value = getProperty(key, dflt);
    if (value == null) {
      return null;
    }
    return value.replaceAll("\\{site}", getSiteName());
  }

  // ==== Globals

  public String getGlobalProperty(String key) {
    return globalParams.get(key);
  }

  public String getGlobalProperty(String key, String dflt) {
    String value = globalParams.get(key);
    if (value == null || value.isEmpty()) {
      return dflt;
    }
    return value;
  }

  public boolean getGlobalProperty(String key, boolean dflt) {
    return PropertyMap.getBool(getGlobalProperty(key), dflt);
  }

  // ====

  public HashMap<String,String> getProperties() {
    return localParams;
  }

  public Site copySite() {
    return new Site(site.getSiteId(), site.getUrlParam(), site.getDisplayName(), site.isEnabled());
  }

  /**
   * Returns true if the site info- urlParam and displayName are the same
   */
  public boolean sameSiteStrings(SiteInfo other) {
    return getUrlParam().equals(other.getUrlParam()) && getDisplayName().equals(other.getDisplayName());
  }

  @Override
  public String toString() {
    return site.getIdString();
  }


  // ==== Below are site-specific resources, cached here

  /**
   * The returned map contains values for: siteId, siteName and registry.name
   * <br>And all the parameters in the DEFAULT_CLIENT_PARAMS array below (that have values)
   * <br>And the values for the keys in the comma-separated-list property "client.params".
   */
  public HashMap<String, String> getClientParams() {
      return clientParams;
  }


  /**
   * Returns a by-name map of the email templates for the site,
   */
  public HashMap<String, String> getEmailTemplates() {
    return emailTemplates;
  }

  /**
   * Returns a by-name map to cache the initial email templates for the site,
   * The caller fills it, from the emailTemplates
   */
  public HashMap<String, String> getInitialTemplates() {
    return initialTemplates;
  }

  /**
   * Returns a by-name map to cache the reminder email templates for the site,
   * The caller fills it, from the emailTemplates
   */
  public HashMap<String, String> getReminderTemplates() {
    return reminderTemplates;
  }


  /**
    * Returns a RandomSet for this site of the given name, or null if none is found.
    *
    * To add a RandomSet, add its definition to the database and a new SiteInfo will have it, after 30-120 seconds.
    * Or call serverContext.reload(false, true) right after the database is changed.
    */
   public RandomSetter getRandomSet(String name) {
     return treatmentSets.get(name);
   }


   /**
    * Returns the entries, so code can iterate over them
    */
   public Set<Entry<String, RandomSetter>> getRandomSets() {
     return treatmentSets.entrySet();
   }


/**
   * Returns a mailer configured for this site
   */
  public Mailer getMailer() {
    return mailer;
  }


  public PatientIdFormatIntf getPatientIdFormatter() {
    return patientIdFormatter;
  }


  /**
   * Returns an object that implements DateUtilsIntf and
   * can parse dates using the site-specific default.dateTimeFormat format
   */
  public DateUtilsIntf getDateFormatter() {
    return dateTimeFormatter;
  }

  /**
   * Returns an object that implements DateUtilsIntf and can parse dates using the site-specific default.dateFormat format
   */
  public DateUtilsIntf getDateOnlyFormatter() {
    return dateFormatter;
  }

  public Date parseDate(String string) throws ParseException {
    return dateTimeFormatter.parseDate(string);
  }

  public Date parseDateOnly(String string) throws ParseException {
    return dateFormatter.parseDate(string);
  }
  public RegistryCustomizer getRegistryCustomizer() {
    return registryCustomizer;
  }


  // Configuration property names
  public static final String REGISTRY_CUSTOMIZER_CLASS = "RegistryCustomizerClass";


  /**
   * If there's a problem creating any of these, e.g. due to a configuration typo,
   * it'll happen just after the server is started, or just after a configuration parameter is changed.
   * This is protected, not private, for ease of testing.
   */
  protected void initResources(Supplier<Database> dbp, HashMap<String, String> emailTemplates,
                                                       HashMap<String, String> treatmentSetJSONs) {
    patientIdFormatter = new PatientIdFormatterFactory().create(this);
    mailer = new MailerFactory().create(this);
    dateTimeFormatter = new DateTimeFormatter(this);
    dateFormatter = new DateFormatter(this);
    registryCustomizer = new RegistryCustomizerFactory().create(this);

    this.emailTemplates = emailTemplates;
    initialTemplates = new HashMap<String,String>(emailTemplates.size());
    reminderTemplates = new HashMap<String,String>(emailTemplates.size());
    RandomSetFactory rsf = new RandomSetFactory(this);
    treatmentSets = rsf.createRandomSets(dbp, treatmentSetJSONs);

    initClientParams();  // must occur after treatmentSets are initted
  }

  public static final String SITE_ID = "siteId";
  public static final String SITE_NAME = "siteName";

  public static final String REGISTRY_NAME = "registry.name";
  public static final String ABOUT_PARAM = "aboutus.link";
  public static final String TERMS_PARAM = "terms.link";
  public static final String CONTACT_PARAM = "contact.link";
  public static final String SURVEY_LINK_PARAM = "survey.link";
  public static final String SURVEY_LINK_ENABLED_PARAM = "survey.clinic.link.enabled";
  public static final String CHART_PARAM = "chart.url";
  public static final String TIME_PARAM = "default.dateTimeFormat";
  public static final String DATE_PARAM = "default.dateFormat";
  public static final String CONSENT_PARAM = "consent_form";
  public static final String PATIENT_ID_FMT = "PatientIdFormat";
  public static final String PATIENT_ID_FMT_MSG = "PatientIdFormatErrorMessage";
  public static final String EMAIL_TO_PARENT = "registry.email.toParent";
  public static final String EMAIL_OUT_DAYS = "appointment.initialemail.daysout";
  public static final String TSETS_SHOW_TO_CLINIC = "treatmentset.show.toclinic"; // show treatment sets to clinic staff
  public static final String TSETS_SHOW_EMPTY_BAR = "treatmentset.show.emptybar"; // else it's hidden
  public static final String TSETS_SHOW_FULL_STATE = "treatmentset.show.fullstate"; // v.s. just not & assigned states
  public static final String MISSING_EMAIL_SFX = "emailtemplate.missing.suffix";
  public static final String MISSING_EMAIL_SFX_DFLT = " --- MISSING!";

  public static final String TREATMENT_SETS = "treatmentset.list";

  // Instead of changing this, put comma-separated keys into the client.param property.
  public static final String[] DEFAULT_CLIENT_PARAMS = {
      REGISTRY_NAME, ABOUT_PARAM, TERMS_PARAM, CONTACT_PARAM, SURVEY_LINK_PARAM,
      SURVEY_LINK_ENABLED_PARAM, CHART_PARAM, TIME_PARAM, DATE_PARAM, CONSENT_PARAM,
      PATIENT_ID_FMT, PATIENT_ID_FMT_MSG, EMAIL_TO_PARENT, EMAIL_OUT_DAYS, Constants.SCHED_SORT_PARAM,
      TSETS_SHOW_TO_CLINIC, TSETS_SHOW_EMPTY_BAR, TSETS_SHOW_FULL_STATE, Constants.PATIENT_ID_LABEL,
      Constants.CUSTOM_ASSESSMENT_CONFIG_NAME, Constants.ENABLE_CUSTOM_ASSESSMENT_CONFIG
  };

  private void initClientParams() {
    clientParams = new HashMap<>();
    clientParams.put(SITE_ID, site.getSiteId().toString());
    clientParams.put(SITE_NAME, site.getUrlParam());
    clientParams.put(REGISTRY_NAME, site.getDisplayName());

    clientParams.put(MISSING_EMAIL_SFX, getProperty(MISSING_EMAIL_SFX, MISSING_EMAIL_SFX_DFLT));
    for (String key: DEFAULT_CLIENT_PARAMS) {
      String value = getProperty(key);
      if (value != null) {
        clientParams.put(key, value);
      }
    }

    // Let users add client parameters, if needed
    String client_params = getProperty("client.params");
    if (client_params != null && !client_params.isEmpty()) {
      client_params = client_params.replaceAll(" ", ""); // remove any spaces (e.g. after commas)
      String []list = client_params.split(",");

      for (String key: list) {
        String value = getProperty(key);
        if (value != null) {
          clientParams.put(key, value);
        }
      }
    }

    // Add the Treatment Sets
    StringBuilder sb = new StringBuilder(100);
    String delim = "";
    for (RandomSetter rsetter: treatmentSets.values()) {
      RandomSet rset = rsetter.getRandomSet();
      if (rset.getType().isForPatientUI()) {
        sb.append(delim).append(rset.getName());
        delim = (delim.isEmpty()) ? ",," : delim;
      }
    }
    clientParams.put(TREATMENT_SETS, sb.toString());
  }

  // Known site props to report the site values for when config changes
  static public final String knownSiteProps[] = {
      "PatientIdFormat",
      "PatientIdFormatErrorMessage",
      "PatientIdFormatterClass",
      "PromisQuestionReportFontSize",
      "RegistryCustomizerClass",
      "SurveyClassForLocal",
      "SurveyClassForLocalPromis",
      "SurveyClassForPROMIS",
      // "SurveyClassFor..." -- there can be more...
      "aboutus.link",
      "appointment.initialemail.daysout*",
      "appointment.lastsurvey.daysout*",
      "appointment.noemail.withindays",
      "appointment.reminderemail.daysout*",
      "appointment.surveyexpires.afterdays",
      "appointment.surveyinvalid.afterdays",
      "chart.url",
      "contact.link",
      "default.dateFormat",
      "default.dateTimeFormat",
      "default.timezone",
      "emailTemplateResource",
      "emailtemplate.missing.suffix",
      "factory.survey.advance",
      "factory.survey.complete",
      "import.url",
      "importDefinitionDirectory",
      "importDefinitionResource",
      "importPendingFileDirectory",
      "importProcessedFileDirectory",
      "registry.email.file",
      "registry.email.from",
      "registry.email.mode",
      "registry.email.server",
      "registry.email.port",
      "registry.email.toParent",
      "survey.link",
      "terms.link",
      "treatmentset.show.emptybar",
      "treatmentset.show.fullstate",
      "treatmentset.show.toclinic",
      "xlsx.template.directory",
      "xml_dir",
      "xml_resource"
  };
  private void reportOnConfig() {
    if (!logger.isDebugEnabled()) {
      return;
    }
    for (String key: knownSiteProps) {
      String value = getProperty(key);
      if (value != null) {
        logger.debug(this.getSiteId()+" Key "+key+" siteValue: "+value);
      }
    }
  }
}
