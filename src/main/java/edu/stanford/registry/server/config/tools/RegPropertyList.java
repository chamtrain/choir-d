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

package edu.stanford.registry.server.config.tools;

import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.RegConfigCategory;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.RegConfigUsage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
//import edu.stanford.registry.server.config.RegConfigProperty.ValueType;

/**
 * Lists all the properties used in Registry, to document, so a program can output the doc
 * as html or markdown for the wiki
 *
 * @author Randy Strauss
 */
public class RegPropertyList implements Iterable<RegConfigProperty> {

  /**
   * If you're a developer with your own fork, specifying this constructor allows callers to
   * add your properties to this.  Your list should be a subclass of this one.
   * @param customProps Set of custom properties
   */
  public RegPropertyList(String customProps) {
    // TODO -- instantiate and then call its create method, add a param to LoadConfig to use this

  }

  public RegPropertyList() {
    create();
  }

  public RegConfigProperty fromName(String name) {
    for (RegConfigProperty p: propList) {
      if (p.getName().equalsIgnoreCase(name))
        return p;
    }
    return null;
  }

  private ArrayList<RegConfigProperty> create() {
    // ---- General

    mk(g, SR, "default.site",
        "If no siteId=X parameter exists in the clinic url, this site's url-parameter is assumed, "
            + "if the user has permission for it. "
            + "If the user does not have permission for this site, one of their permitted sites is chosen. "
            + "The siteId parameter will appear in their URL. If this is a survey URL, "
            + "the site is specified with an 's' parameter, e.g. s=ortho",
        "");

    mk(g, SI, "default.timezone",
        "Default timezone. Defaults to the system timezone.",
        "");

    mk(g, SR, "cache.reload.seconds",
        "Polling frequency for reloading the cache. This runs a quick query to see if any updates "
            + "have been made. If not, nothing more is done. If there are changes, the parameters for "
            + "the sites that have changed (or the global values) are reloaded in.",
        "30");

    mk(g, ST, "registry.jndi.datasource*",
        "JNDI name for the registry database (I don't know when this would be something other than 'registry.jndi.datasource')",
        "java:");

    mk(g, SR, "registry.batch.interval.seconds",
        "Batch processing frequency, if not set batch "
            + "processing is disabled {say what batch processing is}",
        "");

    mk(g, ST, "registry.jndi.datasource.flavor",
        "Identifies the type of database. Supported options "
            + "are: oracle, postgresql, derby",
        "oracle");

    mk(g, SR, "registry.polling",
        "Turns on(true) / off(false) the background tasks: polling for cache updates (config and users),"
            + "importing patient and appointment spreadsheets, and running survey advancing and completion. "
            + "Always set to TRUE for production but useful to turn off when in development",
        "true");

    mk(g, SR, "registry.load.config",
        "Turns on(true) / off(false) initial load of the configuration "
            + "parameters into the servers configuration cache. Probably this should always be true, "
            + "certainly for production",
        "true");

    mk(g, SR, "registry.reload.config",
        "Turns on(true) / off(false) reload the configuration cache (when polling) if values have changed. "
            + "Turn this false to keep the log shorter during development",
        "true");

    mk(g, SR, "registry.load.users",
        "Turns on(true) / off(false) initial load of users to the servers user cache. Probably this should always be true."
            + "If false, only survey-answering will work. Set to true or leave unset for production.",
        "true");

    mk(g, SR, "registry.reload.users",
        "Turns on(true) / off(false) reload the users cache (when polling) to load in changes."
            + "Set to true or leave unset for production.",
        "true");

    mk(g, ST, "registry.sql.in.exception.messages",
        "Include SQL in exception messages. This also "
            + "includes parameters in the exception messages if SQL parameter logging is enabled -- "
            + "Handy for development, but possible information disclosure risk depending on the errors."
            + "The server must be restarted for a change in this property to take effect.",
        "false");

    mk(g,  GL, "registry.sql.log.parameters",
        "Enable logging of the parameter values when SQL statements are logged. "
            + "Handy for development, but information disclosure risk depending on the errors. "
            + "The server must be restarted for a change in this property to take effect.",
        "false");

    mk(g, GL, "Hl7CustomizerFactory",
        "Used to identify a custom implementation of the Hl7CustomizerFactoryIntf interface. ",
        "edu.stanford.registry.server.hl7.Hl7CustomizerFactory");

    ret(g, "registry.devmode.user",
        "This no longer exists. So much is in the database now, it's a bad idea to run without one. "
            + "And you'd need authorities for a user (search for 'admin' in CreateRegistrySchema'.)",
        "");

    // ---- Import

    mk(i, SI, "import.url",
        "This is the applications API url. While it is site-specific, when used, the site id is added as "
            + "a parameter, so no site needs to customize it it. "
            + "When properly defined (and polling is enabled), a background import process "
            + "will periodically look for patient and appointment spreadsheets waiting to be "
            + "imported and call the service (using this url) to import and process them. "
            + "If this isn't set or the value isn't valid then imports will have to be started "
            + "manually for each site, via the UI. "
            + "Set the value to be http://&lt;host&gt;:&lt;port&gt;/registry/registry/svc/api/",
        "");

    mk(i, GL, "import.process.frequency",
        "The number of minutes till the import process should look again for "
            + "files waiting to be imported. NOTE: Zero disables importing after an initial import. "
            + "To disable completely, leave the import.url property undefined. In range 1-60. "
            + "Developers often disable this to cut down on log messages.",
        "5");

    mk(i, SP, "importDefinitionDirectory",
        "Directory where the patient and appointment import definition files are stored.",
        "$TOMCAT_HOME/webapps/registry/WEB-INF/classes/default/xchg/");

    mk(i, SP, "importDefinitionResource",
        "A folder for the classloader to read. The definitions for the appointment and patient "
        + "spreadsheet formats are read from <importDefinitionResource>/data_definitions/ . "
        + "These include ImportDefinitionTypes.xlsx, ImportDefinitionDatasources.xlsx, and "
        + "ImportDefinitionTypes.xlsx",
        "default/xchg");

    mk(i, SP, "importPendingFileDirectory",
        "Directory where the files waiting to be imported are found. "
            + "Each one of these folders must be unique in the set of all the pending and "
            + "processed folders for all the sites. "
            + "At the end of initialization, the first set of imports is done immediately. "
            + "Search the log for 'import' to see which sites are enabled and any errors. "
            + "Each directory must exist already.  If not set, import is disabled. "
            + "A global default might be something like "
            + "<TOMCAT_HOME>/webapps/registry/xchgin/{site}/in",
        "");

    mk(i, SP, "importProcessedFileDirectory",
        "Directory where the processed imported files are moved to when complete. "
            + "Each one of these folders must be unique in the set of all the pending and "
            + "processed folders for all the sites. "
            + "At the end of initialization, the first set of imports is done immediately. "
            + "Search the log for 'import' to see which sites are enabled and any errors. "
            + "Each directory must exist already.  If not set, import is disabled.  "
            + "This might be similar to the Pending directory, but 'out' instead of 'in'.",
        "");

    ret(i, "appointment.daysout.load*",
        "The original Appointment.java that used this was removed Jan 15, 2016.",
        "");

    ret(i, "appointment.initial.onload.when",
        "I believe this was used by the retired Appointment.java. WAS: "
            + "Set to either 'no_completed_surveys' or 'no_prior_appointments'. "
            + "If the value is set to 'no_prior_appointments' then patients are assigned survey "
            + "type 'Initial' only on their first visit. Otherwise they're assigned initial if "
            + "they've not completed a survey yet no matter how many appointments they've had.",
        "no_completed_surveys");

    mk(i, SI, "appointment.initialemail.daysout*",
        "Number of days before an appointment that patients are sent their 1st email survey invitation. "
            + "Used as the end date when selecting the appointments to process for emails and "
            + "also to build out the questionnaire details.",
        "");

    mk(i, SI, "appointment.lastsurvey.daysout*",
        "Number of days between requiring surveys. If a survey was done within this many days of "
            + "the current survey being evaluated then the current survey being evaluated is considered to be not required.",
        "");

    mk(i, SI, "appointment.noemail.withindays",
        "Minimum number of days between emailing the patient survey invitations and reminders.",
        "2");

    mk(i, SI, "appointment.reminderemail.daysout*",
        "The number of days prior to a scheduled appointment that reminder emails are "
            + "sent for patients who were emailed an initial survey invitation but have not "
            + "yet completed the survey.Use a comma separated value list for multiple reminders "
            + "for example: 4, 1 will send reminders to patients 4 days before the appointment "
            + "and the day before the appointment.",
        "");

    mk(i, SI, "appointment.surveyexpires.afterdays",
        "The number of days after a scheduled appointment that the token expires and the survey can no longer be taken.",
        "1");

    mk(i, SI, "appointment.surveyinvalid.afterdays",
        "The number of days after a scheduled appointment that the token is valid. "
            + "An expired token which is still valid will be re-directed to a current survey "
            + "if one exists. The re-direction is meant to handle the situation where the user "
            + "clicks on a link from an previous email.",
        "180");



    // ---- Email

    mk(e, SP, "registry.email.file",
        "The name of the file that the email should be written to. The default substitutes the"
            + "site name (the urlParam) for the '{site}' text if it",
        "{site}.email.log");

    mk(e, SI, "registry.email.mode",
        "This property must be set to \"production\" for emails to be sent. "
            + "Otherwise the process will run, the surveys will be built, the "
            + " email will be logged but not sent to the mail server.",
        "");

    mk(e, SI, "registry.email.from",
        "The apparent sender of email",
        "");

    mk(e, GL, "registry.email.production.host",
        "This property must be set to the name of the server that CHOIR is currently "
            + "running on for emails to be sent. If the value of this property does not "
            + "match the server's hostname, no actual email will be sent. "
            + "If there are multiple hosts, these must be static for each host (not database properties).",
        "");

    mk(e, SI, "registry.email.server",
        "This is the name of the SMTP host which will send the mail. If not set, no email will be sent.",
        "");

    mk(e, SI, "registry.email.port",
        "This is used to specify a non-standard port for the smtp mailer. ", "25");

    mk(e, SI, "registry.email.toParent",
        "If this is set, the EmailMonitor assumes email addresses of young patients actually "
            + "belong to the parents, so can be sent.",
        "");

    ret(e, "email.template.directory",
        "Retired in version 2. In version 1, email templates could be kept in a separate folder.",
        "");

    mk(e, SP, "emailTemplateResource",
        "Each file in this resource directory holds an email template of the same name. When the server "
            + "(version 2) is started, email templates will be read into the database from this resource folder "
            + "(on the classpath) if the database does not already have a template with this name.",
        "default/email-templates");

    // ---- Clinic Interface

    mk(c, SI, "aboutus.link",
        "URL for an optional \"About Us\" link in the Clinic interface's footer",
        "");
    mk(c, SI, "contact.link",
        "URL for an optional \"Contact\" link in the Clinic interface footer",
        "");
    mk(c, SI, "terms.link",
        "URL for an optional \"Terms\" link in the Clinic interface page footer",
        "");

    mk(c, SI, "default.dateTimeFormat",
        "Defines how to display dates with time. You should set the global, even if "
            + "to the current default, to avoid warnings in the log",
        "MM/dd/yyyy h:mm a");

    mk(c, SI, "default.dateFormat",
        "Defines how dates are displayed.",
        "MM/dd/yyyy");

    mk(c, SI, "default.scheduleSort",
        "Defines the default ordering on the schedule tab. Supported options are: "
            + " mrn, lastName, firstName, apptType, apptTime, surveyType",
        "apptTime");

    mk(c, SI, "PatientIdFormat",
        "Regex to validate the format of the patient id in the client application",
        "\\d{5,7}-\\d{1}|\\d{5,9}");

    mk(c, SI, "PatientIdFormatErrorMessage",
        "Error message when the format of the patient id is invalid.",
        "Patient Id must be 5-7 characters followed by \"-\" and a single digit.");

    mk(c, SI, "PatientIdFormatterClass",
        "A class that implements edu.stanford.registry.server.service.formatter.PatientIdFormatIntf "
        + "to format and validate IDs",
        "edu.stanford.registry.server.utils.PatientIdUnformatted");

    mk(c, SI, Constants.PATIENT_ID_LABEL,
        "Label used on the schedule and patient tabs for the patients unique identifier", "MRN");

    mk(c, SI, "RegistryCustomizerClass",
        "Java class implementing RegistryCustomizer interface to define customized actions.",
        "edu.stanford.registry.server.RegistryCustomizerDefault");

    mk(c, SI, "siteId",
        "For compatibility with version 1, the site Id is put into the clientParams using this key.",
        "");

    mk(c, SI, "siteName",
        "For compatibility with version 1, the site urlParam is put into the clientParams using this key.",
        "");

    mk(c, SI, "registry.name",
        "For compatibility with version 1, the site display name is put into the clientParams using this key. "
            + "So this comes from the survey_site table and is not configurable",
        "");

    mk(c, SI, "treatmentset.show.toclinic",
        "Shows the Treatment Sets bar to the clinic staff (it's always shown to physicians).",
        "Y");

    mk(c, SI, "treatmentset.show.emptybar",
        "If no treatment sets are assigned to a patient, show the empty bar and label.",
        "N");

    mk(c, SI, "treatmentset.show.fullstate",
        "Offer the user all treatmentset states, such as Declined, Disqualified, Complete, not just Not/Assigned states",
        "N");

    mk(c, SI, "emailtemplate.missing.suffix",
        "The suffix added to an email template name if it's used in process.xml but not defined.",
        " --- MISSING!");

    mk(c, SI, "survey.clinic.link.enabled",
        "Display survey token in Start Assessment dialog as a link which opens the survey in a new tab. ",
        "false");

    mk(g, SI, "registry.patient.search.by.site",
        "Patient searches are restricted by site. Only patients registered or declined in the "
            + "current site will be returned by patient searches. If false then patients "
            + "from any site will be returned.",
        "false");

    mk(g, SI, "enable.custom.assessment.config",
        "Enable feature to customize assessments",
        "N");

    // ---- Reporting

    mk(r, SI, "chart.url",
        "The url called to get a patient report. "
            + "While this is site-specific, most sites use the same URL. "
            + "The site-specific urlParam will be added to it.",
        "/registry/registry/svc/chart");

    mk(r, SI, "PromisQuestionReportFontSize",
        "The font size used when printing the promis questions",
        "11");

    mk(r, SP, "xlsx.template.directory",
        "Directory where report Excel templates are located",
        "default/reports");


    // ---- Patient-Survey

    mk(p, SI, "process.xml", "Store the whole process.xml file in this configuration parameter."
        + "If set, the process definitions come from the database, not the file system.", "");

    mk(p, SP, "xml_dir", "Directory where the xml files are stored. "
        + "If set, the xml_resource parameter is ignored", "");

    mk(p, SP, "xml_resource", "If the xml_dir is not defined it will check for this parameter."
        + "and load xml files as resources from the war file, with this as the path to the resource.",
        "default/xml");

    mk(p, SI, "factory.survey.complete",
        "Used to identify a custom implementation of the class called when a survey completes",
        "edu.stanford.registry.server.survey.SurveyCompleteHandlerFactoryImpl");

    mk(p, SI, "factory.survey.advance",
        "Used to identify a custom implementation of the class called when a survey advances",
        "edu.stanford.registry.server.surveySurveyAdvanceHandlerFactoryImpl");

    mk(p, GL, "factory.survey.system",
        "Used to identify a custom implementation of the SurveySystemFactory interface. "
            + "This is a copy of the sites defined in the database, plus more settings that are"
            + "not yet in the database.",
        "edu.stanford.registry.server.survey.SurveySystemFactoryImpl");

    mk(p, SI, "survey.link*",
        "URL of the surveys. Used in the emailed invitations to build the link to start surveys. "
            + "While this is site-specific, all the sites can use the same value- the site id parameter "
            + "will be added to it.",
        "");

    mk(p, SI, "SurveyClassForLocal",
        "Class used for survey Questionnaires with type=\"Local\"",
        "edu.stanford.registry.server.survey.RegistryAssessmentsService");

    mk(p, SI, "SurveyClassForLocalPromis",
        "Class used for survey Questionnaires with type \"LocalPromis\"",
        "edu.stanford.registry.server.survey.LocalPromisSurveyService");

    mk(p, SI, "SurveyClassForPROMIS",
        "Deprecated Class that was used for survey Questionnaires with type \"PROMIS\"",
        "edu.stanford.registry.server.survey.PromisSurveyService");

    mk(p, SI, "SurveyClassFor...",
        "Any survey service can have a property that tells its class name. For instance, one of "
        + "the Stanford sites specifies a SurveyClassForRepeatingSurveyService.",
        "see SurveyServiceFactory.java for defaults for the various survey types.");

    mk(p, ST, "property.key.prefix*",
        "This is used to allow different TomCat applications to have different user and password properties "
            + "on the same server.  This should be a static property, in the web.xml file.",
        "registry");

    mk(p, ST, "[property.key.prefix].service.user",
        "Application user used by the authentication "
            + "filter for the remote patient survey war to connect to the patient service",
        "-survey-app");

    mk(p, ST, "[property.key.prefix].service.password",
        "This password is used by the authentication filter to allow the remote patient survey "
            + "war to connect to the patient service. The request header X-Authentication must "
            + "match the value of this parameter in the configuration file",
        "");

    // ---- Assessment Center

    mk(a, GL, "promis.2.url*",
        "The URL for the NorthWestern API (only required if using NW PROMIS service)",
        "");

    mk(a, GL, "promis.2.registrationOID",
        "Provided by NorthWestern",
        "");

    mk(a, GL, "promis.2.token",
        "Provided by NorthWestern", "");

    mk(a, GL, "proxyHost",
        "Name of the proxy <b>host</b> (if your implementation will use a proxy to reach NorthWestern)",
        "");

    mk(a, GL, "proxyPort",
        "Name of the proxy <b>port</b> (if your implementation "
            + "will use a proxy to reach NorthWestern)",
        "");

    //propList.sort(null);
    return propList;
  }

  private final RegConfigCategory g = RegConfigCategory.G;
  private final RegConfigCategory i = RegConfigCategory.I;
  private final RegConfigCategory e = RegConfigCategory.E;
  private final RegConfigCategory c = RegConfigCategory.C;
  private final RegConfigCategory r = RegConfigCategory.R;
  private final RegConfigCategory p = RegConfigCategory.P;
  private final RegConfigCategory a = RegConfigCategory.A;
  //Category w = Category.W;

  private static final RegConfigUsage ST = RegConfigUsage.Static;
  private static final RegConfigUsage SR = RegConfigUsage.StaticRec;
  private static final RegConfigUsage GL = RegConfigUsage.Global;
  private static final RegConfigUsage SI = RegConfigUsage.SiteSpecific;
  private static final RegConfigUsage SP = RegConfigUsage.SitePath;

  private void mk(RegConfigCategory cat, RegConfigUsage usage, String name, String desc, String dflt) {
    propList.add(new RegConfigProperty(name, cat, usage, dflt, desc));
  }                                     // String defValue, ValueType valueType, String enumValue, String desc

  private final ArrayList<RegConfigProperty> propList = new ArrayList<>();


  /*
  public static RegConfigProperty make(String name, char category, Usage usage, String defValue, String desc) {
    return instance.make(name, category, usage, defValue, RegConfigProperty.ValueType.STRING, null, desc);
  }

  public static RegConfigProperty make(String name, char category, Usage usage, boolean defValue, String desc) {
    return instance.make(name, category, usage, defValue ? "Y" : "N", RegConfigProperty.ValueType.BOOL, null, desc);
  }

  public static RegConfigProperty make(String name, char category, Usage usage, int defValue, String desc) {
    return instance.make(name, category, usage, Integer.toString(defValue), RegConfigProperty.ValueType.INT, null, desc);
  }

  public static RegConfigProperty make(String name, char category, Usage usage, String defValue, String enumValue, String desc) {
    return instance.make(name, category, usage, defValue, RegConfigProperty.ValueType.ENUM, enumValue, desc);
  }
 /* * /

  private RegConfigProperty make(String name, char category, Usage usage, String defValue, RegConfigProperty.ValueType vType, String enumValue, String desc) {
    RegConfigProperty prop = new RegConfigProperty(name, category, usage, defValue, vType, enumValue, desc);
    int ix = propList.indexOf(prop) + 1;
    if (ix > 0)
      throw new RuntimeException(propList.size() + ". RegConfigProperty "+name+" already defined as property "+ix);
    return prop;
  }
  /* */
  private void ret(RegConfigCategory cat, String name, String desc, String dflt) {
    propList.add(new RegConfigProperty(name, cat, RegConfigUsage.Retired, dflt, desc));
  }

  @Override
  public Iterator<RegConfigProperty> iterator() {
    return propList.iterator();
  }

  private ArrayList<StringMatcher> propMatchers;

  /**
   * Returns a comma+space separated list of possible properties you meant.
   * Used only if the string doesn't match exactly.
   */
  public RegConfigProperty[] closeMatches(String trial, int n) {
    if (propMatchers == null) {
      propMatchers = new ArrayList<>(propList.size());
      for (RegConfigProperty p: propList) {
        propMatchers.add(new StringMatcher(p));
      }
    }

    StringMatcher sm = new StringMatcher(trial);
    for (StringMatcher prop: propMatchers) {
      sm.computeScore(prop);
    }
    propMatchers.sort(sm);
    int m = (propMatchers.size() <= n) ? (propMatchers.size() - 1) : n;
    int hardLimit = (propMatchers.size() <= n*3) ? propMatchers.size() : (n*3);
    for (int i = n;  i < hardLimit;  i++) { // include any with great scores
      if (propMatchers.get(i).score >= StringMatcher.GREAT_SCORE) {
        m++;
      }
    }
    RegConfigProperty[] rps = new RegConfigProperty[m];
    for (int i = 0;  i < m;  i++) {  // return the m properties
      rps[i] = propMatchers.get(i).rp;
    }
    return rps;
  }

  static class StringMatcher implements Comparable<StringMatcher>, Comparator<StringMatcher>{
    final static int GREAT_SCORE = 60000;
    final static int TOP_SCORE   = 90000;

    final String s;
    final int length;
    final byte[] counts = new byte[37];
    int score;  // a set of these will be scored against a word
    RegConfigProperty rp;

    StringMatcher(RegConfigProperty rp) {
      this(rp.getName());
      this.rp = rp;
    }

    StringMatcher(String s) {
      this.s = s.toLowerCase();
      this.length = s.length();
      countChars();
    }

    private void countChars() {
      for (int i = 0;  i < s.length();  i++) {
        char c = s.charAt(i);
        if (c == '.' || c == '_') {
          counts[26]++;
        } else if ('a' <= c && c <= 'z') {
          counts[c - 'a']++;
        }
      }
    }

    void computeScore(StringMatcher prop) {
      int count = 0;
      for (int i = 0;  i < counts.length;  i++) {
        count += (counts[i] < prop.counts[i]) ? counts[i] : prop.counts[i];
      }

      if (prop.s.contains(s)) {
        int ix = prop.s.indexOf(s);
        prop.score = TOP_SCORE - (100*ix) - (10 * prop.s.length()); // doesn't get better than this!
        return;
      }
      if (s.contains(prop.s)) {
        int ix = s.indexOf(prop.s);
        prop.score = TOP_SCORE - (100*ix) - (10 * s.length()); // doesn't get better than this!
        return;
      }

      if (prop.s.length() < s.length()) {
        prop.score = 1000 * count ;
      } else {
        prop.score = 1000 * count;
      }
      prop.score += 100 - prop.length / 2; // favor shorter matches;
    }

    /**
     * Compares so the higher scores come first
     */
    @Override
    public int compareTo(StringMatcher o) {
      if (o.score == score) {
        if (o.length == length) {
          return o.s.compareTo(s);
        }
        return o.length - length;
      }
      return o.score - score;
    }

    @Override
    public int compare(StringMatcher o1, StringMatcher o2) {
      return o1.compareTo(o2);
    }

  }
}
