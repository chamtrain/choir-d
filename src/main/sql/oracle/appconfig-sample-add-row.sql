-- Sample Oracle script to ADD a new app_config row to the database
-- To see all rows having a string as part of their name, use:  appconfig-query.sql
prompt
prompt == This script adds a new row to appconfig, using paraters:  site, cType, cName, cValue, yourUserId
prompt ==   Note1: The value can be at most 250 characters, using this script (Oracle limit for variables)
prompt ==   Note2: yourUserId is your numeric user_principal.user_principal_id, not your user name
prompt ==    e.g.:  select * from user_principal where username='admin';
prompt ==   As of 3.0, ctype is one of (besides the emailtemplate and randomset you should not use this for):
prompt ==      configparam,  custom,  squaretable,  surveycontent,  builder
prompt

-- First, define your values- if the value has spaces, 
define cName=treatmentset.show.fullstate
define cValue=Y
define ctype=configparam
define site=1
define yourUserId=1

@ appconfig-add-row.sql

-- FYI, information about most (all?) parameters are below:
-- General System parameters
--   default.site  (GR)
--     If no siteId=X parameter exists in the clinic url, this site's
--       url-parameter is assumed, if the user has permission for it. If the user
--       does not have permission for this site, one of their permitted sites is
--       chosen. The siteId parameter will appear in their URL. If this is a
--       survey URL, the site is specified with an 's' parameter, e.g. s=ortho
--   cache.reload.seconds  (GR)
--     Polling frequency for reloading the cache. This runs a quick query to see
--       if any updates have been made. If not, nothing more is done. If there are
--       changes, the parameters for the sites that have changed (or the global
--       values) are reloaded in.
--     Default = 30
--   registry.jndi.datasource  (ST)
--     JNDI name for the registry database (I don't know when this would be
--       something other than 'registry.jndi.datasource')
--     Default = java:
--   registry.batch.interval.seconds  (GR)
--     Batch processing frequency, if not set batch processing is disabled {say
--       what batch processing is}
--   registry.jndi.datasource.flavor  (ST)
--     Identifies the type of database. Supported options are: oracle, postgresql,
--       derby
--     Default = oracle
--   registry.polling  (GR)
--     Turns on(true) / off(false) the background tasks: polling for cache updates
--       (config and users),importing patient and appointment spreadsheets, and
--       running survey advancing and completion. Always set to TRUE for
--       production but useful to turn off when in development
--     Default = true
--   registry.load.config  (GR)
--     Turns on(true) / off(false) initial load of the configuration parameters
--       into the servers configuration cache. Probably this should always be
--       true, certainly for production
--     Default = true
--   registry.reload.config  (GR)
--     Turns on(true) / off(false) reload the configuration cache (when polling)
--       if values have changed. Turn this false to keep the log shorter during
--       development
--     Default = true
--   registry.load.users  (GR)
--     Turns on(true) / off(false) initial load of users to the servers user
--       cache. Probably this should always be true.If false, only
--       survey-answering will work. Set to true or leave unset for production.
--     Default = true
--   registry.reload.users  (GR)
--     Turns on(true) / off(false) reload the users cache (when polling) to load
--       in changes.Set to true or leave unset for production.
--     Default = true
--   registry.sql.in.exception.messages  (ST)
--     Include SQL in exception messages. This also includes parameters in the
--       exception messages if SQL parameter logging is enabled -- Handy for
--       development, but possible information disclosure risk depending on the
--       errors.The server must be restarted for a change in this property to take
--       effect.
--     Default = false
--   registry.sql.log.parameters  (GL)
--     Enable logging of the parameter values when SQL statements are logged.
--       Handy for development, but information disclosure risk depending on the
--       errors. The server must be restarted for a change in this property to
--       take effect.
--     Default = false
--   registry.devmode.user  (RE)
--     This no longer exists. So much is in the database now, it's a bad idea to
--       run without one. And you'd need authorities for a user (search for
--       'admin' in CreateRegistrySchema'.)
-- 
-- Import For the importing of appointment and patient files
--   import.url  (GL)
--     This is the applications API url. When properly defined (and polling is
--       enabled), a background import process will periodically look for patient
--       and appointment spreadsheets waiting to be imported and call the service
--       (using this url) to import and process them. If this isn't set or the
--       value isn't valid then imports will have to be started manually for each
--       site, via the UI. Set value to be
--       http://&lt;host&gt;:&lt;port&gt;/registry/registry/svc/api/
--   import.process.frequency  (GL)
--     The number of minutes till the import process should look again for files
--       waiting to be imported. NOTE: Zero disables importing after an initial
--       import. To disable completely, leave the import.url property undefined.
--       In range 1-60. Developers often disable this to cut down on log messages.
--     Default = 5
--   importDefinitionDirectory  (SP)
--     Directory where the patient and appointment import definition files are
--       stored.
--     Default = $TOMCAT_HOME/webapps/registry/WEB-INF/classes/default/xchg/
--   importDefinitionResource  (SP)
--     A folder for the classloader to read. The definitions for the appointment
--       and patient spreadsheet formats are read from
--       <importDefinitionResource>/data_definitions/ . These include
--       ImportDefinitionTypes.xlsx, ImportDefinitionDatasources.xlsx, and
--       ImportDefinitionTypes.xlsx
--     Default = default/xchg
--   importPendingFileDirectory  (SP)
--     Directory where the files waiting to be imported are found. Each one of
--       these folders must be unique in the set of all the pending and processed
--       folders for all the sites. At the end of initialization, the first set of
--       imports is done immediately. Search the log for 'import' to see which
--       sites are enabled and any errors. Each directory must exist already.  If
--       not set, import is disabled. A global default might be something like
--       <TOMCAT_HOME>/webapps/registry/xchgin/{site}/in
--   importProcessedFileDirectory  (SP)
--     Directory where the processed imported files are moved to when complete.
--       Each one of these folders must be unique in the set of all the pending
--       and processed folders for all the sites. At the end of initialization,
--       the first set of imports is done immediately. Search the log for 'import'
--       to see which sites are enabled and any errors. Each directory must exist
--       already.  If not set, import is disabled.  This might be similar to the
--       Pending directory, but 'out' instead of 'in'.
--   appointment.daysout.load  (RE)
--     The original Appointment.java that used this was removed Jan 15, 2016.
--   appointment.initial.onload.when  (RE)
--     I believe this was used by the retired Appointment.java. WAS: Set to either
--       'no_completed_surveys' or 'no_prior_appointments'. If the value is set to
--       'no_prior_appointments' then patients are assigned survey type 'Initial'
--       only on their first visit. Otherwise they're assigned initial if they've
--       not completed a survey yet no matter how many appointments they've had.
--     Default = no_completed_surveys
--   appointment.initialemail.daysout  (SI)
--     Number of days before an appointment that patients are sent their 1st email
--       survey invitation. Used as the end date when selecting the appointments
--       to process for emails and also to build out the questionnaire details.
--   appointment.lastsurvey.daysout  (SI)
--     Number of days between requiring surveys. If a survey was done within this
--       many days of the current survey being evaluated then the current survey
--       being evaluated is considered to be not required.
--   appointment.noemail.withindays  (SI)
--     Minimum number of days between emailing the patient survey invitations and
--       reminders.
--     Default = 2
--   appointment.reminderemail.daysout  (SI)
--     The number of days prior to a scheduled appointment that reminder emails
--       are sent for patients who were emailed an initial survey invitation but
--       have not yet completed the survey.Use a comma separated value list for
--       multiple reminders for example: 4, 1 will send reminders to patients 4
--       days before the appointment and the day before the appointment.
--   appointment.surveyexpires.afterdays  (SI)
--     The number of days after a scheduled appointment that the token expires and
--       the survey can no longer be taken.
--     Default = 1
--   appointment.surveyinvalid.afterdays  (SI)
--     The number of days after a scheduled appointment that the token is valid.
--       An expired token which is still valid will be re-directed to a current
--       survey if one exists. The re-direction is meant to handle the situation
--       where the user clicks on a link from an previous email.
--     Default = 180
-- 
-- Email 
--   registry.email.file  (SP)
--     The name of the file that the email should be written to. The default
--       substitutes thesite name (the urlParam) for the '{site}' text if it
--     Default = {site}.email.log
--   registry.email.mode  (SI)
--     This property must be set to "production" for emails to be sent. Otherwise
--       the process will run, the surveys will be built, the  email will be
--       logged but not sent to the mail server.
--   registry.email.from  (SI)
--     The apparent sender of email
--   registry.email.production.host  (GL)
--     This property must be set to the name of the server that CHOIR is currently
--       running on for emails to be sent. If the value of this property does not
--       match the server's hostname, no actual email will be sent. If there are
--       multiple hosts, these must be static for each host (not database
--       properties).
--   registry.email.server  (SI)
--     This is the name of the SMTP host which will send the mail. If not set, no
--       email will be sent.
--   registry.email.toParent  (SI)
--     If this is set, the EmailMonitor assumes email addresses of young patients
--       actually belong to the parents, so can be sent.
--   email.template.directory  (RE)
--     Retired in version 2. In version 1, email templates could be kept in a
--       separate folder.
--   emailTemplateResource  (SP)
--     Each file in this resource directory holds an email template of the same
--       name. When the server (version 2) is started, email templates will be
--       read into the database from this resource folder (on the classpath) if
--       the database does not already have a template with this name.
--     Default = default/email-templates
-- 
-- Clinic Interface 
--   aboutus.link  (SI)
--     URL for an optional "About Us" link in the Clinic interface's footer
--   contact.link  (SI)
--     URL for an optional "Contact" link in the Clinic interface footer
--   terms.link  (SI)
--     URL for an optional "Terms" link in the Clinic interface page footer
--   default.dateTimeFormat  (SI)
--     Defines how to display dates with time. You should set the global, even if
--       to the current default, to avoid warnings in the log
--     Default = MM/dd/yyyy h:mm a
--   default.dateFormat  (SI)
--     Defines how dates are displayed.
--     Default = MM/dd/yyyy
--   PatientIdFormat  (SI)
--     Regex to validate the format of the patient id in the client application
--     Default = \d{5,7}-\d{1}|\d{5,9}
--   PatientIdFormatErrorMessage  (SI)
--     Error message when the format of the patient id is invalid.
--     Default = Patient Id must be 5-7 characters followed by "-" and a single digit.
--   PatientIdFormatterClass  (SI)
--     A class that implements
--       edu.stanford.registry.server.service.formatter.PatientIdFormatIntf to
--       format and validate IDs
--     Default = edu.stanford.registry.server.utils.PatientIdUnformatted
--   RegistryCustomizerClass  (SI)
--     Java class implementing RegistryCustomizer interface to define customized
--       actions on the schedule tab.
--     Default = edu.stanford.registry.server.RegistryCustomizerDefault
--   siteId  (SI)
--     For compatibility with version 1, the site Id is put into the clientParams
--       using this key.
--   siteName  (SI)
--     For compatibility with version 1, the site urlParam is put into the
--       clientParams using this key.
--   registry.name  (SI)
--     For compatibility with version 1, the site display name is put into the
--       clientParams using this key.
--   treatmentset.show.toclinic  (SI)
--     Shows the Treatment Sets bar to the clinic staff (it's always shown to
--       physicians).
--     Default = Y
--   treatmentset.show.emptybar  (SI)
--     If no treatment sets are assigned to a patient, show the empty bar and
--       label.
--     Default = N
--   treatmentset.show.fullstate  (SI)
--     Offer the user all treatmentset states, such as Declined, Disqualified,
--       Complete, not just Not/Assigned states
--     Default = N
-- 
-- Reporting 
--   chart.url  (SI)
--     The url called to get a patient report. While this is site-specific, most
--       sites use the same URL. The site-specific urlParam will be added to it.
--     Default = /registry/registry/svc/chart
--   PromisQuestionReportFontSize  (SI)
--     The font size used when printing the promis questions
--     Default = 11
--   xlsx.template.directory  (SP)
--     Directory where report Excel templates are located
--     Default = default/reports
-- 
-- Patient survey For survey-specific configuration
--   xml_dir  (SP)
--     Directory where the xml files are stored. If set, the xml_resource
--       parameter is ignored
--   xml_resource  (SP)
--     If the xml_dir is not defined it will check for this parameter.and load xml
--       files as resources from the war file, with this as the path to the
--       resource.
--     Default = default/xml
--   factory.survey.complete  (SI)
--     Used to identify a custom implementation of the class called when a survey
--       completes
--     Default = edu.stanford.registry.server.survey.SurveyCompleteHandlerFactoryImpl
--   factory.survey.advance  (SI)
--     Used to identify a custom implementation of the class called when a survey
--       advances
--     Default = edu.stanford.registry.server.surveySurveyAdvanceHandlerFactoryImpl
--   factory.survey.system  (GL)
--     Used to identify a custom implementation of the SurveySystemFactory
--       interface. This is a copy of the sites defined in the database, plus more
--       settings that arenot yet in the database.
--     Default = edu.stanford.registry.server.survey.SurveySystemFactoryImpl
--   survey.link  (SI)
--     URL of the surveys. Used in the emailed invitations to build the link to
--       start surveys. While this is site-specific, all the sites can use the
--       same value- the site id parameter will be added to it.
--   SurveyClassForLocal  (SI)
--     Class used for survey Questionnaires with type="Local"
--     Default = edu.stanford.registry.server.survey.RegistryAssessmentsService
--   SurveyClassForLocalPromis  (SI)
--     Class used for survey Questionnaires with type "LocalPromis"
--     Default = edu.stanford.registry.server.survey.LocalPromisSurveyService
--   SurveyClassForPROMIS  (SI)
--     Deprecated Class that was used for survey Questionnaires with type "PROMIS"
--     Default = edu.stanford.registry.server.survey.PromisSurveyService
--   SurveyClassFor...  (SI)
--     Any survey service can have a property that tells its class name. For
--       instance, one of the Stanford sites specifies a
--       SurveyClassForRepeatingSurveyService.
--     Default = see SurveyServiceFactory.java for defaults for the various survey types.
--   property.key.prefix  (ST)
--     This is used to allow different TomCat applications to have different user
--       and password properties on the same server.  This should be a static
--       property, in the web.xml file.
--     Default = registry
--   [property.key.prefix].service.user  (ST)
--     Application user used by the authentication filter for the remote patient
--       survey war to connect to the patient service
--     Default = -survey-app
--   [property.key.prefix].service.password  (ST)
--     This password is used by the authentication filter to allow the remote
--       patient survey war to connect to the patient service. The request header
--       X-Authentication must match the value of this parameter in the
--       configuration file
-- 
-- Assessment center 
--   promis.2.url  (GL)
--     The URL for the NorthWestern API (only required if using NW PROMIS service)
--   promis.2.registrationOID  (GL)
--     Provided by NorthWestern
--   promis.2.token  (GL)
--     Provided by NorthWestern
--   proxyHost  (GL)
--     Name of the proxy <b>host</b> (if your implementation will use a proxy to
--       reach NorthWestern)
--   proxyPort  (GL)
--     Name of the proxy <b>port</b> (if your implementation will use a proxy to
--       reach NorthWestern)