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

package edu.stanford.registry.server.service;

import edu.stanford.registry.client.api.AssessmentObj;
import edu.stanford.registry.client.api.PluginPatientDataObj;
import edu.stanford.registry.client.api.PluginPatientHistoryDataObj;
import edu.stanford.registry.client.api.SurveyObj;
import edu.stanford.registry.client.service.AppointmentStatus;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.ResultGeneratorIntf;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerException;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.Metric;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.PluginDataDao;
import edu.stanford.registry.server.database.RandomSetDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.server.reports.JsonReport;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.server.reports.RegistrationReport;
import edu.stanford.registry.server.reports.SquareTableExportReport;
import edu.stanford.registry.server.reports.TextReport;
import edu.stanford.registry.server.reports.VisitsReport;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.service.rest.ApiStatusException;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.utils.ChartUtilities;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.HTMLParser;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.PDFUtils;
import edu.stanford.registry.server.utils.ReportUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptId;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.shared.DisplayProvider;
import edu.stanford.registry.shared.EmailSendStatus;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.PROMISScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientActivity;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientRegistrationSearch.SortBy;
import edu.stanford.registry.shared.PatientResult;
import edu.stanford.registry.shared.PatientResultType;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.PatientStudyObject;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.ResultAction;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.SurveyStart;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserPreference;
import edu.stanford.registry.shared.api.SiteObj;
import edu.stanford.registry.shared.comparator.ActivityComparator;
import edu.stanford.registry.shared.comparator.DisplayProviderComparator;
import edu.stanford.registry.shared.comparator.PatientRegistrationComparator;
import edu.stanford.registry.shared.comparator.StudyComparator;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.servlet.ServletException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.JFreeChart;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseException;
import com.keypoint.PngEncoder;

/**
 * Services available in the the clinic portion of the application
  *
 * @author tpacht
 */

public class ClinicServicesImpl implements ClinicServices {
  private static final Logger logger = LoggerFactory.getLogger(ClinicServicesImpl.class);
  private static final int XMARGIN = 20;
  private static final int TABLE_WIDTH = 130;
  private static final int CELL_MARGIN = 5;

  protected final Supplier<Database> dbp;
  protected final Long siteId;  // used by subclasses
  protected final User user;
  protected final SiteInfo siteInfo;
  private final int fontSize = 12;
  final protected AssessDao assessDao;
  final protected XMLFileUtils xmlUtils;
  // TODO: Inject in an EmailTemplate provider and a SurveyRegUtils provider- maybe in context?;


  public ClinicServicesImpl(User usr, Supplier<Database> databaseProvider, ServerContext context, SiteInfo siteInfo) {
    user = usr;
    dbp = databaseProvider;
    this.siteInfo = siteInfo;
    if (siteInfo == null) {
      // restlet status has a null siteInfo
      // throw new IllegalArgumentException("siteId must not be blank.");
      siteId = 0L;
      xmlUtils = null;
      assessDao = null;
    } else {
      siteId = siteInfo.getSiteId();
      assessDao = new AssessDao(dbp.get(), siteInfo);
      xmlUtils = XMLFileUtils.getInstance(siteInfo);
    }
  }

  /**
   * Get a list of process names that are currently active, ordered by the
   * process order attribute value
   */
  @Override
  public ArrayList<String> getActiveVisitProcessNames() {
    return xmlUtils.getActiveVisitProcessNames();
  }

  /**
   * Get a list of process names from the proceess.xml file
   */
  @Override
  public ArrayList<String> getProcessNames() {
    return xmlUtils.getProcessNames();
  }

  @Override
  /**
   * Get a list of process names from the process.xml file that are surveys
   */
  public ArrayList<String> getSurveyProcessNames() {
    return xmlUtils.getSurveyProcessNames();
  }

  /**
   * Get a list of the attributes for the process
   *
  public HashMap<String, String> getProcessAttributes(String processType) throws ServiceUnavailableException {
    // Never called...  If needed, move to ClinicServicesImpl which has the xmlUtils
    //return xmlUtils.getAttributes(processType);
    throw new ServiceUnavailableException("getProcessAttributes() was not in an interface");
  }*/

  /**
   * Get a map of all process attributes by process name
   */
  @Override
  public HashMap<String, HashMap<String, String>> getAllProcessAttributes() {
    HashMap<String, HashMap<String, String>> map = new HashMap<>();
    ArrayList<String> names = getProcessNames();
    if (names == null) {
      return map;

    }
    for (String name : names) {
      if (name != null) {
        HashMap<String, String> attributeMap = xmlUtils.getAttributes(name);
        map.put(name, attributeMap);
      }
    }
    return map;
  }

  /**
   * Get a map of process name and list of patient attributes for the process
   */
  @Override
  public HashMap<String, ArrayList<PatientAttribute>> getAllPatientAttributes() {
    return xmlUtils.getAllPatientAttributes();
  }

  @Override
  public Patient getPatient(String patientId) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    Patient patient = patientDao.getPatient(patientId);
    
    // Optionally limit the search to patients in the current site
    boolean limitPatientSearchToSite = siteInfo.getProperty("registry.patient.search.by.site", false);
    if (limitPatientSearchToSite && (patient != null)) {
      // Only return patients known to this site.
      // Registered is y/n if the patient has been registered or declined for this site.
      String registered = patient.getAttributeString(Constants.ATTRIBUTE_PARTICIPATES);
      if (registered == null) {
        patient = null;
      }
    }

    return patient;
  }

  @Override
  public Date getPatientsNextAppointmentDate(String patientId) {
    /* called with "true" for appointments only */
    return assessDao.getPatientsNextScheduledDate(patientId, true, false);
  }

  @Override
  public Date getPatientsLastAppointmentDate(String patientId) {
    return assessDao.getPatientsLastAppointmentDate(patientId);
  }

  @Override
  public Date getPatientsNextSurveyDueDate(String patientId) {
    /** appointments only = false, uncompleted only = true **/
    return assessDao.getPatientsNextScheduledDate(patientId, false, true);
  }

  @Override
  public Date getPatientsLastSurveyDate(String patientId) {
    return assessDao.getPatientsLastScheduleCompletedDate(patientId, false);
  }

  private void createNotification(AssessmentRegistration registration) {
    // Check if unsent notification already exists
    ArrayList<Notification> notifications = assessDao.getUnsentNotifications(registration.getAssessmentId());
    if ((notifications == null) || (notifications.size() < 1)) {
      logger.debug("Notification not found, adding a new one for " + registration.getPatientId() + ";"
          + registration.getAssessmentId() + ";" + registration.getAssessmentType() + ";"
          + registration.getAssessmentDt().toString() + ";");
      Notification notify = new Notification(registration.getPatientId(), registration.getAssessmentId(),
          registration.getAssessmentType(), registration.getAssessmentDt(), 0, registration.getSurveySiteId());
      assessDao.insertNotification(notify);
    }
  }

  @Override
  public ArrayList<PatientActivity> searchForActivity(Date dtFrom, Date dtTo, boolean includeCompleted) {
    if (dtFrom == null || dtTo == null) {
      return new ArrayList<>();
    }

    Date fromTime = new Date(dtFrom.getTime()); // DateUtils.getTimestampStart(dtFrom);
    Date toTime = new Date(dtTo.getTime()); // DateUtils.getTimestampEnd(dtTo);

    logger.debug("getting activity by date range with includecompleted: " + includeCompleted);
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
    ArrayList<Activity> activities = activityDao.getActivity(fromTime, toTime, includeCompleted);
    return createPatientActivityArray(activities);
  }

  @Override
  public ArrayList<PatientActivity> searchForActivity(String patientId, boolean includeCompleted) {
    logger.debug("getting all patients activity with includecompleted: " + includeCompleted);
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
    ArrayList<Activity> activities = activityDao.getPatientsActivity(patientId, includeCompleted);
    return createPatientActivityArray(activities);
  }

  private ArrayList<PatientActivity> createPatientActivityArray(ArrayList<Activity> activities) {
    Hashtable<String, Patient> patients = new Hashtable<>();
    ArrayList<PatientActivity> patientsActivity = new ArrayList<>();
    // Current PatientActivity values
    String curPatientId = null;
    Long curAssessmentRegId = null;
    String curToken = null;
    PatientActivity curPatientActivity = null;

    for(Activity act : activities) {

      // Check if we need to create a new PatientActivity. A new PatientActivity
      // is needed when
      //   1. The patient for the activity changed
      //   2. The activity has an ApptRegId and the ApptRegId changed
      //   3. The activity does not have an ApptRegId but has a token
      //      and the token changed
      boolean createPatientActivity = false;
      if (!act.getPatientId().equals(curPatientId)) {
        createPatientActivity = true;
      } else if (act.getAssessmentRegId() != null && !act.getAssessmentRegId().equals(curAssessmentRegId)) {
        createPatientActivity = true;
      } else if (act.getToken() != null && !act.getToken().equals(curToken)) {
        createPatientActivity = true;
      }
      if (createPatientActivity) {
        // Save the current PatientActivity
        if (curPatientActivity != null) {
          patientsActivity.add(curPatientActivity);
          curPatientActivity = null;
        }

        // Look up the patient
        Patient pat = patients.get(act.getPatientId());
        if (pat == null) {
          PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
          pat = patientDao.getPatient(act.getPatientId());
          if (pat != null) {
            patients.put(act.getPatientId(), pat);
          }
        }

        // if patient found
        if (pat != null) {
          // Get the appointment registration
          ApptRegistration registration = null;
          if (act.getAssessmentRegId() != null) {
            registration = assessDao.getApptRegistrationByAssessmentId(new AssessmentId(act.getAssessmentRegId()));
          }
          // If not found then create a fake one
          if (registration == null) {
            String processType = xmlUtils.getProcess("activity", act.getActivityType());
            String visitType = null;
            if (processType != null) {
              xmlUtils.getAttribute(processType, "visitType");
            }
            logger.debug("createPatientActivityArray: creating registration for patient=" + pat.getPatientId()
                + ", SURVEY_TYPE=" + act.getActivityType() + ", REGISTRATION_TYPE='', VISIT_TYPE " + visitType);

            registration = new ApptRegistration(siteId, pat.getPatientId(), act.getActivityDt(), "",
                getDisplayNameFor(act.getActivityType()), "", visitType);
            act.setActivityType(getDisplayNameFor(act.getActivityType()));
            if (processType != null) {
              logger.debug("createPatientActivityArray: setting surveyType to " + processType);
              registration.setSurveyType(getDisplayNameFor(processType));
            }
            // And a fake survey so the token is included for display
            SurveyRegistration surveyRegistration = new SurveyRegistration();
            surveyRegistration.setPatientId(pat.getPatientId());
            surveyRegistration.setToken(act.getToken());
            surveyRegistration.setSurveyDt(act.getActivityDt());
            registration.addSurveyReg(surveyRegistration);
          }

          // Create a new PatientActivity
          curPatientActivity = new PatientActivity(pat);
          curPatientActivity.setRegistration(registration);
        }
      }

      // Update the current values
      curPatientId = act.getPatientId();
      curAssessmentRegId = act.getAssessmentRegId();
      curToken = act.getToken();

      // Add the activity to the PatientActivity
      if (curPatientActivity != null) {
        curPatientActivity.addActivity(act);
      }
    }

    if (curPatientActivity != null) {
      patientsActivity.add(curPatientActivity);
    }

    Collections.sort(patientsActivity, new ActivityComparator());
    return patientsActivity;
  }

  private String getDisplayNameFor(String name) {
    if (name == null) {
      return name;
    }
    if (Constants.ACTIVITY_CONSENTED.equals(name)) {
      return Constants.ACTIVITY_REGISTERED;
    }
    try {
    if (name.contains(Constants.ACTIVITY_CONSENTED)) {
      StringBuilder returnName = new StringBuilder();
      if (name.indexOf(Constants.ACTIVITY_CONSENTED) > 0) {
        returnName.append(name, 0, name.indexOf(Constants.ACTIVITY_CONSENTED)).append(Constants.ACTIVITY_REGISTERED);
        int afterString = name.indexOf(Constants.ACTIVITY_CONSENTED) + Constants.ACTIVITY_CONSENTED.length();
        if (name.length() > afterString) {
          returnName.append(name.substring(afterString));
        }
      return returnName.toString();
      }
    } } catch (Exception e) {
      logger.warn("problem parsing " + name + " to replace " + Constants.ACTIVITY_CONSENTED + " with " + Constants.ACTIVITY_REGISTERED);
    }
    return name;
  }

  @Override
  public ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByPatientId(String patientId, Date dtTo) {
    PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
    if (dtTo != null) {
      //Date toTime = DateUtils.getTimestampEnd(dtTo);
      return patStudyDao.getPatientStudyExtendedDataByPatientId(patientId, dtTo);
    } else {
      return patStudyDao.getPatientStudyExtendedDataByPatientId(patientId);
    }
  }

  @Override
  public ArrayList<PatientStudyExtendedData> searchForPatientStudyDataByName(String nameSearchString, Date dtFrom,
      Date dtTo) {
    PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
    if (dtFrom != null && dtTo != null) {
      Date fromTime = DateUtils.getTimestampStart(siteInfo, dtFrom);
      Date toTime = DateUtils.getTimestampEnd(siteInfo, dtTo);
      return patStudyDao.getPatientStudyDataByNameLike(nameSearchString, fromTime, toTime);
    } else {
      return patStudyDao.getPatientStudyDataByNameLike(nameSearchString);
    }
  }

  /*
   * public ArrayList<ChartScore> getScores(String patientId, Integer studyCode)
   * throws Exception {
   *
   * Database database = dbp.get();
   *
   * ArrayList<PatientStudyExtendedData> patients =
   * assessDao.getPatientStudyExtendedDataByPatientId(patientId);
   * return getScores(patients); }
   */
  public ArrayList<ChartScore> getScores(ArrayList<PatientStudyExtendedData> patients, Integer studyCode)
      throws Exception {
    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patients == null || studyCode == null) return scores;
    for (PatientStudyExtendedData patient : patients) {
      //
      if (patient != null && patient.getStudyCode() != null
          && patient.getStudyCode().intValue() == studyCode.intValue()) {
        PatientStudyExtendedData patientData = patient;
        ScoreProvider provider = SurveyServiceFactory.getFactory(siteInfo).
            getScoreProvider(dbp, patientData.getSurveySystemName(),
            patientData.getStudyDescription());
        ArrayList<ChartScore> theseScores = provider.getScore(patientData);
        if (theseScores != null && theseScores.size() > 0) {
          for (ChartScore theseScore : theseScores) {
            scores.add(theseScore);
          }
        }
      }
    }
    return scores;
  }

  @Override
  public ArrayList<Study> getStudies() {
    return getStudies(false);
  }

  @Override
  public ArrayList<Study> getStudies(boolean removeDuplicates) {
    SurveySystDao ssDao = new SurveySystDao(dbp.get());
    ArrayList<Study> studies = ssDao.getStudies();
    if (removeDuplicates) {
      Collections.sort(studies, new StudyComparator<Study>(StudyComparator.SORT_BY_CODE));
      for (int i = studies.size() - 1; i >= 0; i--) {
        if (i > 0) {
          if (studies.get(i).getStudyCode().equals(studies.get(i - 1).getStudyCode())) {
            studies.remove(i);
          }
        }
      }
    }
    return studies;
  }

  @Override
  public ArrayList<SurveySystem> getSurveySystems() throws Exception {
    SurveySystDao ssDao = new SurveySystDao(dbp);
    return ssDao.getSurveySystems();
  }

  @Override
  public AssessmentRegistration getAssessmentRegistration(AssessmentId assessmentId) {
    AssessmentRegistration registration = assessDao.getAssessmentById(assessmentId);
    return registration;
  }

  /**
   * Get the registrations scheduled any time of day within the days of the
   * dates provided.
   */
  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(java.util.Date dateFrom, java.util.Date dateTo,
                                                                     PatientRegistrationSearch searchOptions) throws DataException {
    ArrayList<PatientRegistration> registrations;
    registrations = assessDao.getPatientRegistrations(dateFrom, dateTo, searchOptions);
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    registrations = customizer.determinePatientRegActions(dbp.get(), registrations);
    if (searchOptions.getSortBy() == null) {
      searchOptions.setSortBy(siteInfo.getProperty(Constants.SCHED_SORT_PARAM));
    }
    if (searchOptions.getSortBy() == null) {
      searchOptions.setSortBy(Constants.SCHED_SORT_DEFAULT);
    }
    if (searchOptions.getSortBy() != null
        && searchOptions.getSortBy() != SortBy.apptTime) { // the query sorts by appointment time
      Comparator<PatientRegistration> comparator =
          new PatientRegistrationComparator<>(searchOptions.getSortBy(), searchOptions.getSortAscending());
      registrations.sort(comparator);
    }
    return registrations;
  }

  /**
   * Get the registrations scheduled any time of day within the days of the
   * dates provided by type.
   */
  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(java.util.Date dateFrom, java.util.Date dateTo,
                                                                     String registrationType, PatientRegistrationSearch searchOptions) throws DataException {
    ArrayList<PatientRegistration> registrations;
    registrations = assessDao.getPatientRegistrations(dateFrom, dateTo, registrationType, searchOptions);
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    registrations = customizer.determinePatientRegActions(dbp.get(), registrations);
    if (searchOptions.getSortBy() == null) {
      searchOptions.setSortBy(siteInfo.getProperty(Constants.SCHED_SORT_PARAM));
    }
    if (searchOptions.getSortBy() == null) {
      searchOptions.setSortBy(Constants.SCHED_SORT_DEFAULT);
    }
    if (searchOptions.getSortBy() != null && searchOptions.getSortBy() != SortBy.apptTime) {
      Comparator<PatientRegistration> comparator =
          new PatientRegistrationComparator<>(searchOptions.getSortBy(), searchOptions.getSortAscending());
      registrations.sort(comparator);
    }
    return registrations;
  }

  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, PatientRegistrationSearch searchOptions) {
    return assessDao.getPatientRegistrations(patientId, searchOptions);
  }

  @Override
  public ArrayList<PatientRegistration> searchForPatientRegistration(String patientId, String registrationType) {

    return assessDao.getPatientRegistrationsByType(patientId, registrationType);
  }

  @Override
  public ArrayList<PatientStudyExtendedData> searchForPatientStudyDataScores(String patientId) {
    PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
    ArrayList<PatientStudyExtendedData> patientData = new ArrayList<>();
    ArrayList<PatientStudyExtendedData> patients = patStudyDao.getPatientStudyExtendedDataByPatientId(patientId);
    //HashMap<String, SurveyServiceIntf> surveys = new HashMap<String, SurveyServiceIntf>();

    Map<String, Boolean> assisted = patStudyDao.getPatientAssistedStudyTokens(patientId);
    for (PatientStudyExtendedData patientStudy : patients) {
      if (patientStudy.getToken() != null) {
        if (assisted.get(patientStudy.getToken()) != null) patientStudy.setAssisted(true);
        patientData.add(patientStudy);
      }
    }

    return patientData;
  }

  /**
   * Search for patients by partial or full mrn
   */
  @Override
  public ArrayList<Patient> searchPatientsByPatientId(String patientId) {
    ArrayList<Patient> patients = new ArrayList<>();
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    Patient pat = patientDao.getPatient(patientId);
    if (pat != null) {
      patients.add(pat);
    }
    return patients;
  }

  /**
   * Search for patients by partial lastname
   */
  @Override
  public ArrayList<Patient> searchPatientsByName(String partialLastName) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    return patientDao.getPatientsByName(partialLastName);
  }

  /**
   * Search for patients by attribute
   */
  @Override
  public ArrayList<Patient> searchPatientsByAttr(String attr, String value) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    return patientDao.getPatientsByAttr(attr, value);
  }


  /**
   * Search for patients by email address
   */
  @Override
  public ArrayList<Patient> searchPatientsByEmail(String value) {
    ArrayList<Patient> result1 = searchPatientsByAttr(Constants.ATTRIBUTE_SURVEYEMAIL, value);
    if (result1 == null) {
      result1 = new ArrayList<>();
    }
    ArrayList<Patient> result2 = searchPatientsByAttr(Constants.ATTRIBUTE_SURVEYEMAIL_ALT, value);
    if (result2 != null) {
      for(Patient pat2 : result2) {
        boolean alreadyContainsId = false;
        for(Patient pat1 : result1) {
          if (pat2.getPatientId().equals(pat1.getPatientId())) {
            alreadyContainsId = true;
          }
        }
        if (!alreadyContainsId) {
          result1.add(pat2);
        }
      }
    }
    return result1;
  }

  /**
   * Search for patients.
   * 
   * The search string is interpreted as a patient id, email address,
   * partial last name or patient attribute.
   */
  @Override
  public ArrayList<Patient> searchPatients(String searchString) {
    if (searchString == null || searchString.trim().length() < 1) {
      throw new IllegalArgumentException("A search term cannot be blank.");
    }

    // Use the registry customizer to determine the type of search
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    String searchType = customizer.getPatientSearchType(searchString);
    String searchValue = customizer.getPatientSearchValue(searchType, searchString);

    ArrayList<Patient> patients;
    switch(searchType) {
    case RegistryCustomizer.PATIENT_SEARCH_BY_PATIENT_ID:
      patients = searchPatientsByPatientId(searchValue);
      break;
    case RegistryCustomizer.PATIENT_SEARCH_BY_EMAIL:
      patients = searchPatientsByEmail(searchValue);
      break;
    case RegistryCustomizer.PATIENT_SEARCH_BY_PARTIAL_NAME:
      patients = searchPatientsByName(searchValue);
      break;
    default:
      patients = searchPatientsByAttr(searchType, searchValue);
      break;
    }

    // Optionally limit the search to patients in the current site
    boolean limitPatientSearchToSite = siteInfo.getProperty("registry.patient.search.by.site", false);
    if (limitPatientSearchToSite && (patients != null)) {
      ArrayList<Patient> results = new ArrayList<>();
      for(Patient patient : patients) {
        // Only return patients known to this site.
        // Registered is y/n if the patient has been registered or declined for this site.
        String registered = patient.getAttributeString(Constants.ATTRIBUTE_PARTICIPATES);
        if (registered != null) {
          results.add(patient);
        }
      }
      patients = results;
    }

    return patients;
  }

  @Override
  public Patient addPatient(Patient patient) {
    Database database = dbp.get();
    try {
      if (patient == null) {
        return null;
      }
      String patientId = patient.getPatientId();
      if (patientId == null) {
        throw new ServerException("PatientId is missing");
      }

      patient.setPatientId(siteInfo.getPatientIdFormatter().format(patientId));
      Patient updatedPatient = new PatientDao(database, siteId).addPatient(patient);
      List<PatientAttribute> attributes = patient.getAttributes();
      if (attributes != null) {
        for (PatientAttribute attribute : attributes) {
          logger.debug("Adding attribute " + attribute.getDataName() + " to patient " + patient.getPatientId());
          attribute.setPatientId(patient.getPatientId());
          addPatientAttribute(attribute);
        }
      }
      return updatedPatient;
    } catch (Exception e) {
      logger.error("Error occurred adding patient " + e.toString(), e);
      throw new ServerException("Error adding patient");
    }
  }

  @Override
  public PatientRegistration addPatientRegistration(ApptRegistration registration, Patient patient) throws Exception {
    Database database = dbp.get();

    registration.setPatientId(patient.getPatientId()); // use the formatted one
    logger.debug("Adding " + registration.getRegistrationType() + " type registration of "
        + registration.getSurveyType() + " for patient " + registration.getPatientId()
        + " and survey date " + registration.getSurveyDt().toString());

    try {
      /**
       * Update or Add the patient
       */
      patient = insOrUpdPatient(patient);
    } catch (Exception e) {
      logger.error("Error occurred adding patient " + e.toString(), e);
      throw new ServerException("Error adding patient");
    }

    // Save the registration
    AssessDao assessDao = new AssessDao(database, siteInfo);
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    registration = surveyRegUtils.createRegistration(assessDao, registration);

    fillOutRegistration(database, registration, patient);
    PatientRegistration patientRegistration = assessDao.getPatientRegistrationByRegId(registration.getApptId());
    return patientRegistration;
  }

  private void fillOutRegistration(Database database, ApptRegistration registration, Patient patient)
      throws ServiceUnavailableException {
    Date nextApptDayToFind = getDaysOutDate();
    Date now = DateUtils.getDaysOutDate(siteInfo, 1); // only need notification if after tomorrow
    if ((registration.getSurveyDt().after(now) && registration.getSurveyDt().before(nextApptDayToFind))
        || registration.getSurveyDt().equals(nextApptDayToFind)) {
      // Add a notification row
      String send = xmlUtils.getAttribute(registration.getSurveyType(), "notification");
      if (Boolean.parseBoolean(send)) {
        if (registration.getSendEmail()) {
          createNotification(registration.getAssessment());
        }
      }
    }

    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    surveyRegUtils.registerAssessments(database, registration.getAssessment(), user);
  }

  /**
   * Find the appointments for this patient up to the daysOut parameter and
   * complete them
   */
  protected void completePendingRegistrations(Patient patient) {
    Date daysOut = getDaysOutDate();
    Date now = DateUtils.getDaysAgoDate(siteInfo, 1);
    daysOut = DateUtils.getDateEnd(siteInfo, daysOut);
    logger.debug("COMPLETING PENDING REGISTRATIONS FOR " + patient.getPatientId() + " BETWEEN " + now.toString()
        + " AND " + daysOut);
    ArrayList<PatientRegistration> registrations = searchForPatientRegistration(patient.getPatientId(), new PatientRegistrationSearch());
    if (registrations == null) {
      return;
    }

    /*
     * For each of this patients registrations with dates before the daysout
     * parameter days
     */
    for (PatientRegistration registration : registrations) {
      if ((registration.getSurveyDt().after(now) && registration.getSurveyDt().before(daysOut))
          || registration.getSurveyDt().equals(daysOut)) {
        ArrayList<Element> surveys = xmlUtils.getProcessQuestionaires(registration.getSurveyType());
        int registrationSurveys = registration.getNumberCompleted() + registration.getNumberPending();
        if (surveys != null && surveys.size() > 0 && registrationSurveys < surveys.size()) {
          // -- if this type has surveys and the patient registration does not
          // have them yet
          try {
            logger.debug("completing registration for " + registration.getSurveyDt());
            fillOutRegistration(dbp.get(), registration, patient);
          } catch (ServiceUnavailableException | DatabaseException e) {
            logger.error("Error completing registration for patient " + registration.getPatientId(), e);
          }
        }
      }
    }
  }

  @Override
  public Study registerAssessment(String surveySystemName, String assessmentName, String title, String explanation,
      int version) {
    SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(surveySystemName);
    return surveyService.registerAssessment(dbp.get(), assessmentName, title, explanation);
  }

  @Override
  public PatientAttribute addPatientAttribute(PatientAttribute patAttribute) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    return patientDao.insertAttribute(patAttribute);
  }


  /**
   * If the value is null or empty, deletes the attribute from the database and patient.
   * Else sets the patient's attribute (or adds it, if it doesn't exist),
   * and inserts or updates it in the database
   */
  private void utilSetAttribute(Patient patient, String name, String value) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    if (value == null || value.isEmpty()) {
      PatientAttribute attr = patient.removeAttribute(name);
      patientDao.deleteAttribute(attr);
    } else {
      PatientAttribute attr = patient.setAttribute(name, value);
      patientDao.insertAttribute(attr);
    }
  }


  /**
   * If
   */
  @Override
  public int deletePatientAttribute(PatientAttribute patAttribute) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    String patId = patAttribute.getPatientId();
    if (patAttribute != null && patAttribute.getPatientAttributeId() == null && patId != null
        && patAttribute.getDataName() != null) {
      patAttribute = patientDao.getAttribute(patId, patAttribute.getDataName());
    }
    if (patAttribute == null || patAttribute.getPatientAttributeId() == null) {
      return 0;
    }
    return patientDao.deleteAttribute(patAttribute);
  }

  @Override
  public void deletePatientRegistration(ApptRegistration apptReg) {
    AssessmentRegistration assessment = apptReg.getAssessment();
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    surveyRegUtils.deleteRegistration(dbp.get(), apptReg);

    // write the delete activity
    Long userPrincipalId = null;
    if (user != null) {
      userPrincipalId = user.getUserPrincipalId();
    }
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
    int numActivity = 0;
    if (assessment.getSurveyRegList() != null && assessment.getSurveyRegList().size() > 0
        && assessment.getSurveyRegList().get(0) != null) {
      for (SurveyRegistration surveyRegistration : assessment.getSurveyRegList()) {
        Activity activity = new Activity(apptReg.getPatientId(), Constants.ACTIVITY_DELETED,
            assessment.getAssessmentId(), surveyRegistration.getToken(), userPrincipalId);
        activityDao.createActivity(activity);
        numActivity++;
      }
    }
    if (numActivity == 0) {
      Activity activity = new Activity(apptReg.getPatientId(), Constants.ACTIVITY_DELETED,
          assessment.getAssessmentId(), null, userPrincipalId);
      activityDao.createActivity(activity);
    }
  }

  public String getActiveVisitProcessForName(String name, Date surveyDate) {
    return xmlUtils.getActiveProcessForName(name, surveyDate);
  }

  @Override
  public void extendRegistration(ApptRegistration apptReg, Date newDate) {
    AssessmentRegistration assessment = apptReg.getAssessment();
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    String surveyType = apptReg.getSurveyType();
    if(surveyType != null && surveyType.trim().length() > 0) {
      String type = surveyType;
      if(surveyType.contains(".")) {
        type = surveyType.substring(0, surveyType.indexOf("."));
      }
      String activeProcessType = getActiveVisitProcessForName(type, newDate);

      if (activeProcessType != null
          && !activeProcessType.equals(apptReg.getSurveyType())
          && apptReg.getNumberCompleted() == 0) {
        apptReg.setSurveyType(activeProcessType);
      }
    }
    surveyRegUtils.updateRegistration(dbp.get(), apptReg, newDate);
    // write the extend activity
    Activity activity = new Activity(apptReg.getPatientId(), Constants.ACTIVITY_EXTENDED,
        assessment.getAssessmentId(), null, null);
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
    activityDao.createActivity(activity);
  }

  @Override
  public Integer handlePendingNotifications(Mailer mailer, String serverUrl, HashMap<String, String> templates) {
    EmailMonitor monitor = new EmailMonitor(mailer, dbp, serverUrl, siteInfo);
    return monitor.handlePendingNotifications(templates, getEmailOutDate());
  }

  @Override
  public ArrayList<EmailSendStatus> sendEmail(ApptRegistration appt) throws Exception {
    logger.debug("tempnew: sendEmail starting send is " + appt.getSendEmail());
    if (appt.getSendEmail()) {
      createNotification(appt.getAssessment());
    }

    Mailer mailer = siteInfo.getMailer();
    String serverUrl = siteInfo.getProperty("survey.link");

    String templateName;
    if (appt.isAppointment()) {
      templateName = xmlUtils.getAttribute(appt.getSurveyType(),
          XMLFileUtils.ATTRIBUTE_APPOINTMENT_TEMPLATE);
    } else {
      templateName = xmlUtils.getAttribute(appt.getSurveyType(),
          XMLFileUtils.ATTRIBUTE_SCHEDULE_TEMPLATE);
    }
    EmailTemplateUtils emailTemplateUtils = new EmailTemplateUtils();

    HashMap<String, String> oneTemplate = new HashMap<String, String>(1);
    String content = emailTemplateUtils.getTemplate(siteInfo, templateName);
    if (content != null) {
      oneTemplate.put(templateName, content);
    }

    EmailMonitor monitor = new EmailMonitor(mailer, dbp, serverUrl, siteInfo);
    return monitor.sendEmail(oneTemplate, getEmailOutDate(), appt.getAssessment().getAssessmentId());
  }

  @Override
  public Patient updatePatient(Patient patient) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    return patientDao.updatePatient(patient);
  }

  @Override
  public PatientRegistration updatePatientRegistration(PatientRegistration patientRegistration, Date newSurveyDate) {
    Database database = dbp.get();

    Date oldSurveyDate = patientRegistration.getSurveyDt();

    Patient pat = patientRegistration.getPatient();
    updatePatient(pat);

    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    surveyRegUtils.updateRegistration(database, patientRegistration, newSurveyDate);

    // if the registration time has changed we need to update pending
    // notifications
    if (!newSurveyDate.equals(oldSurveyDate)) {
      String sql = "UPDATE NOTIFICATION SET SURVEY_DT = ? WHERE ASSESSMENT_REG_ID = ? AND EMAIL_DT IS NULL ";
      int cntUpdated = database.toUpdate(sql)
          .argDate(newSurveyDate)
          .argLong(patientRegistration.getApptRegId())
          .update();
      if (cntUpdated < 1 && patientRegistration.getSendEmail()) {
        createNotification(patientRegistration.getAssessment());
      }
    }

    return patientRegistration;
  }

  @Override
  public String getAssessments(String surveySystemName, int version) throws Exception {
    SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(surveySystemName);
    return surveyService.getAssessments(dbp.get(), version);
  }

  protected Patient insOrUpdPatient(Patient patient) {
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    Patient oldPatient = patientDao.getPatient(patient.getPatientId());
    if (oldPatient != null) {
      if (!oldPatient.equalsNamesDobConsent(patient)) {
        patientDao.updatePatient(patient);
      }
      return patient;
    } else {
      return addPatient(patient); // This also adds the attributes
    }
  }


  /**
   *
   */
  @Override
  public void acceptEnrollment(String patientId, String emailValue) {
    Patient patient = getPatient(patientId);
    if (patient == null) {
      logger.error("register called with null patient");
    } else {
      registerPatient(patient, emailValue, false);
    }
  }


  /**
   * If the patient doesn't exist this will add them. Then adds/updates any
   * attributes on the patient. If the participates attribute doesn't exist or
   * isn't set to 'y' will add/changes it. And adds an agrees activity.
   */
  @Override
  public Patient setPatientAgreesToSurvey(Patient patient) throws ServiceUnavailableException {
    return registerPatient(patient, null, true);
  }


  /**
   * This unifies setPatientAgreesToSurvey(), used by & acceptEnrollment().
   * Note there's also the RegistrationServiceImpl.setPatientParticipation()
   * which calls setPatientAgreesToSurvey() (or declineEnrollment())
   */
  private Patient registerPatient(Patient patient, String emailValue, boolean updateEmailAlwaysAndRefreshPatient) {
    patient = insOrUpdPatient(patient);

    // Remove any existing decline attributes
    if (patient.hasAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE)) {
      deletePatientAttribute(patient.getAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE));
    }
    if (patient.hasAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER)) {
      deletePatientAttribute(patient.getAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER));
    }

    if (updateEmailAlwaysAndRefreshPatient) { // difference
      utilUpdatePatientStringAttribute(patient, Constants.ATTRIBUTE_SURVEYEMAIL_ALT);
      utilUpdatePatientStringAttribute(patient, Constants.ATTRIBUTE_SURVEYEMAIL_VALID);
    } else if (!Objects.equals(emailValue, patient.getEmailAddress())) {
      utilSetAttribute(patient, Constants.ATTRIBUTE_SURVEYEMAIL_ALT, emailValue);
      utilSetAttribute(patient, Constants.ATTRIBUTE_SURVEYEMAIL_VALID, null);
    }

    if (!patient.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "y")) {
      utilSetAttribute(patient, Constants.ATTRIBUTE_PARTICIPATES, "y");
      addNewActivity(patient, Constants.ACTIVITY_AGREED);
      RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
      customizer.handlePatientRegistration(dbp.get(), patient);
      completePendingRegistrations(patient);   // for this morning through the days-out param
    }

    if (updateEmailAlwaysAndRefreshPatient) { // difference - refresh the patient attributes
      PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
      patient.setAttributes(patientDao.getAttributes(patient.getPatientId()));
    }

    return patient;
  }



  /**
   * Create an activity.
   * Give it a token so grouping and sorting by token works.
   */
  private void addNewActivity(Patient patient, String activityName) {
    Activity pactivity = new Activity(patient.getPatientId(), activityName, Token.generateToken('-', activityName));
    if (user != null) {
      pactivity.setUserPrincipalId(user.getUserPrincipalId());
      pactivity.setSurveySiteId(siteId);
    }
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
    activityDao.createActivity(pactivity);
  }


  /**
   * If the attribute doesn't exist adds it with a value of 'n' otherwise sets
   * it to 'n'. Adds a declines activity.
   */
  @Override
  public Patient declineEnrollment(Patient patient, DeclineReason reasonCode, String reasonOther) {
    patient = insOrUpdPatient(patient);  // update name, dob, consent

    /**
     * Create the participates attribute, the utils method will insert or update as needed
     */
    boolean alreadyWasDeclined = patient.attributeEquals(Constants.ATTRIBUTE_PARTICIPATES, "n");
    utilSetAttribute(patient, Constants.ATTRIBUTE_PARTICIPATES, "n");

    /*
     * Write reason
     */
    if (reasonOther != null && reasonOther.length() > 0 && reasonCode != DeclineReason.other) {
      logger.warn("Changing decline reason code from '" + reasonCode + "' to 'other'");
      reasonCode = DeclineReason.other;
    }

    if (reasonCode != null) {
      utilSetAttribute(patient, Constants.ATTRIBUTE_DECLINE_REASON_CODE, reasonCode.name());

      if (reasonCode.equals(DeclineReason.other) && reasonOther != null && !reasonOther.isEmpty()) {
        utilSetAttribute(patient, Constants.ATTRIBUTE_DECLINE_REASON_OTHER, reasonOther);
      } else if (patient.hasAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER)) {
        deletePatientAttribute(patient.getAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER));
      }
    }

    if (!alreadyWasDeclined) { /* give it a token so grouping and sorting by token works */
      String tokenValue = Token.generateToken('-', Constants.ACTIVITY_DECLINED);
      Activity pactivity = new Activity(patient.getPatientId(), Constants.ACTIVITY_DECLINED, tokenValue);
      if (user != null) {
        pactivity.setUserPrincipalId(user.getUserPrincipalId());
      }
      ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
      activityDao.createActivity(pactivity);
    }

    // get the attributes with the date created stamps
    PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
    patientDao.loadPatientAttributes(patient);
    return patient;
  }


  @Override
  public String updatePatientStudy(PatientStudy patStudy) {
    String xmlString = patStudy.getContents();
    if (xmlString != null) {
      logger.debug("Updating patient study with " + xmlString);
      try {
        PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
        patStudyDao.setPatientStudyContents(patStudy, xmlString);
      } catch (DatabaseException e) {
        logger.error(e.getMessage(), e);
        throw new IllegalArgumentException(e.toString());
      }
    }
    return null;
  }

  @Override
  public PatientStudy getPatientStudy(String patientId, Integer studyCode, String token) {
    logger.debug("Getting patient study " + studyCode + " for patient " + patientId + " for token=" + token);
    PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
    return patStudyDao.getPatientStudy(patientId, studyCode, token, true);
  }

  @Override
  public PatientStudyObject getPatientStudyObject(String patientId, Integer studyCode, String token)
      throws DataException {
    /* Get the patient study */
    PatStudyDao patStudyDao = new PatStudyDao(dbp.get(), siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(patientId, studyCode, token, true);
    if (patStudy == null) return null;
    PatientStudyObject returnObject = new PatientStudyObject(patStudy);
    SurveySystDao ssDao = new SurveySystDao(dbp);
    SurveySystem syst = ssDao.getSurveySystem(patStudy.getSurveySystemId());
    if (syst != null) {
      SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(
          syst.getSurveySystemName());
      logger.debug("calling surveyService: administer assessment for " + patStudy.getExternalReferenceId());
      returnObject.setQuestions(surveyService.getSurvey(dbp.get(), patStudy, user));
      return returnObject;
    } else {
      throw new DataException("Did not find a survey system for systemId " + patStudy.getSurveySystemId());
    }
  }


  private void markResult(AssessmentId assessmentId, ResultAction action) {
    AssessmentRegistration assessment = assessDao.getAssessmentById(assessmentId);
    Activity printedActivity = new Activity(assessment.getPatientId(),  action.getDisplayName(),
        assessment.getAssessmentId(), null, user.getUserPrincipalId());
    ActivityDao activityDao = new ActivityDao(dbp.get(), siteId);
    activityDao.createActivity(printedActivity);
  }

  public Date getDaysOutDate() {
    int initialDaysOutInt = -1;
    String initialDaysOutString = siteInfo.getProperty("appointment.initialemail.daysout");
    if (initialDaysOutString == null || initialDaysOutString.trim().length() < 1) {
      logger.error("Missing value for the 'appointment.initialemail.daysout' parameter.");
    } else {
      try {
        initialDaysOutInt = Integer.parseInt(initialDaysOutString);
        return DateUtils.getDaysOutDate(siteInfo, initialDaysOutInt);
      } catch (Exception e) {
        logger.error("Invalid value for the appointment.initialemail.daysout parameter, must be a valid integer", e);
      }
    }
    return new Date();
  }

  // chart utilities
  @Override
  public byte[] makePng(ArrayList<PatientStudyExtendedData> patientStudies, ChartConfigurationOptions opts)
      throws IOException {
    Metric metric = new Metric(logger.isDebugEnabled());

    if (patientStudies == null || patientStudies.size() < 1 || (patientStudies.get(0) == null)) {
      return new byte[0];
    }

    logger.debug("makePng looking for study with ssid: " + patientStudies.get(0).getSurveySystemId() + " studyCode: "
        + patientStudies.get(0).getStudyCode());
    SurveySystDao ssdao = new SurveySystDao(dbp);
    Study study = ssdao.getStudy(patientStudies.get(0).getSurveySystemId(), patientStudies.get(0).getStudyCode());
    if (study == null) {
      throw new IOException("Invalid study");
    }
    metric.checkpoint("db");
    byte[] pngData = null;
    try {
      ChartUtilities chartUtils = new ChartUtilities(siteInfo);
      PrintStudy pStudy = new PrintStudy(siteInfo, study, patientStudies.get(0).getSurveySystemName());
      ChartInfo chartInfo = chartUtils.createLineChart(dbp, patientStudies, pStudy, opts);
      if (chartInfo == null) {
        logger.error("makePng failed to create chartInfo");
        return new byte[0];
      }
      JFreeChart chart = chartInfo.getChart();
      chart.setRenderingHints(new RenderingHints(chartUtils.getRenderingHints()));
      metric.checkpoint("chart[" + study.getStudyDescription() + "](h=" + opts.getHeight() + ",w=" + opts.getHeight()
          + ")");
      float factor = 1.8f;
      int stretchWidth = Math.round(opts.getWidth() * factor);
      int stretchHeight = Math.round(opts.getHeight() * factor);
      PngEncoder encoder = new PngEncoder(getHighResImage(Constants.DPI_IMAGE_RESOLUTION, stretchWidth, stretchHeight,
          chart), false, 0, 9);
      encoder.setDpi(Constants.DPI_IMAGE_RESOLUTION, Constants.DPI_IMAGE_RESOLUTION);
      pngData = encoder.pngEncode();
    } catch (DataException | InvalidDataElementException e) {
      logger.error(e.getMessage(), e);
    }
    metric.checkpoint("png");
    logger.debug("Make PNG: " + metric.getMessage());
    return pngData;
  }

  public void drawExplanationText(PDPageContentStream contentStream, float yStart, float yEnd,
      ArrayList<PatientStudyExtendedData> patientStudies, Study study, ChartUtilities chartUtils) throws IOException {
    if (study == null) {
      logger.error("STUDY IS NULL");
      return;
    }
    ArrayList<ChartScore> scoresArray;
    try {
      scoresArray = getScores(patientStudies, study.getStudyCode());
    } catch (Exception e) {
      throw new IOException(e);
    }
    if (scoresArray == null) {
      logger.error("SCORES ARRAY IS NULL");
      return;
    }
    String explanation = study.getExplanation();
    if (explanation == null) {
      logger.error("STUDY EXPLANATION IS NULL");
      return;
    }
    HTMLParser writer = new HTMLParser(contentStream, yStart - 6.10f - 6.10f, XMARGIN * 1.0f,
        (XMARGIN + TABLE_WIDTH) * 1.0f);
    writer.writeText(chartUtils.formatExplanationText(dbp, getSurveySystem(study.getSurveySystemId()), study,
        scoresArray, siteId));
  }

  public void drawTable(ChartUtilities chartUtils, PDPageContentStream contentStream, float y,
      ArrayList<PatientStudyExtendedData> patientStudies, Integer study) throws IOException {

    // ArrayList<ChartScore> stats = chartUtils.getStats(patientStudies, study);
    ArrayList<ChartScore> stats = new ArrayList<>();
    final int ROWS = stats.size() + 1;
    PDFont font = PDType1Font.COURIER;

    final float rowHeight = getTextHeight(font) + 2.10f; // add some margin
    final float tableHeight = rowHeight * ROWS;
    final float[] COLUMN_WIDTH = { 45, 35, 35, 1 };

    // draw the lines between rows
    contentStream.setFont(PDType1Font.COURIER, (fontSize / 2));
    float y2 = y;
    for (int i = 0; i <= ROWS; i++) {
      contentStream.drawLine(XMARGIN, y2, XMARGIN + 115, y2);
      y2 -= rowHeight;
    }
    // draw the lines between columns
    float x2 = XMARGIN;
    for (int i = 0; i <= 3; i++) {
      contentStream.drawLine(x2, y, x2, y - tableHeight);
      x2 += COLUMN_WIDTH[i];
    }

    // now add the text

    float textX = XMARGIN + CELL_MARGIN;
    float textY = y - 6.10f;

    // Write the headings
    writePDFText(contentStream, "Date", textX, textY);
    textX += COLUMN_WIDTH[0];
    writePDFText(contentStream, "Score", textX, textY);
    textX += COLUMN_WIDTH[0];
    writePDFText(contentStream, "SE", textX, textY);

    // Write the patients data
    for (ChartScore stat : stats) {
      // move to the start of the next row
      textY -= rowHeight;
      textX = XMARGIN + CELL_MARGIN;

      // write the date column
      DateUtilsIntf dateFmtr = siteInfo.getDateFormatter();
      String dateString = dateFmtr.getDateString(new Date(stat.getDate().getTime())); // key.getTime()));
      writePDFText(contentStream, dateString, textX, textY);
      textX += COLUMN_WIDTH[0];
      // and score column
      BigDecimal score = stat.getScore(); // .setScale(4,
      // BigDecimal.ROUND_HALF_UP);
      writePDFText(contentStream, score.toString(), textX, textY);
      textX += COLUMN_WIDTH[1];
      // and std err column
      if (stat instanceof PROMISScore) {
        PROMISScore pscore = (PROMISScore) stat;
        BigDecimal stdErr = new BigDecimal(pscore.getStdError()).setScale(4, BigDecimal.ROUND_HALF_UP);
        writePDFText(contentStream, stdErr.toString(), textX, textY);
      }
    }
  }

  public float getTextHeight(PDFont font) {
    return getTextHeight(font, fontSize);
  }

  public float getTextHeight(PDFont font, float d) {
    float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    // textHeight = textHeight * (d / 2) * 1.05f;
    return textHeight;
  }

  public void writePDFText(PDPageContentStream contentStream, String string, float x, float y) throws IOException {
    contentStream.beginText();
    contentStream.moveTextPositionByAmount(x, y);
    contentStream.drawString(string);
    contentStream.endText();
  }

  private BufferedImage getHighResImage(int resolution, int width, int height, JFreeChart chart) {
    // int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();

    int screenResolution = 96; // using a default# instead as the linux box user
    // has "No X11 DISPLAY variable"
    double scaleRatio = resolution / screenResolution;
    int rasterWidth = (int) (width * scaleRatio);
    int rasterHeight = (int) (height * scaleRatio);

    BufferedImage image = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_RGB);

    Graphics2D g2 = image.createGraphics();

    g2.transform(AffineTransform.getScaleInstance(scaleRatio, scaleRatio));
    chart.draw(g2, new Rectangle2D.Double(0, 0, width, height), null);
    g2.dispose();

    return image;
  }


  @Override
  public PDDocument getPrintPdfs(ArrayList<Long> surveyRegistrationIds, ChartConfigurationOptions opts)
      throws ServletException,
      IOException {

    logger.debug("printpdfs starting for " + surveyRegistrationIds.size() + " patients");

    PDDocument pdf = new PDDocument();
    /* for (Long id : surveyRegistrationIds) {

      /*
      ArrayList<PatientStudyExtendedData> patientStudies = null;
      try {
        logger.debug("printpdfs processing " + id);
        patientStudies = searchForPatientStudyDataScores(id);
      } catch (Exception ex) {
        logger.error("Error getting patient studies ", ex);
        throw new ServletException(ex);
      }
      if (pdf == null) {
        pdf = makePdf(patientStudies, patientIds.get(p), opts);
      } else {
        pdf = makePdf(patientStudies, patientIds.get(p), pdf, opts);
      } * /

    } */
    return pdf;
  }

  @Override
  public String getScoresExplanation(String patientId, Integer studyCode,
      ArrayList<PatientStudyExtendedData> patientStudies) throws Exception {
    ArrayList<Study> studies = getStudies();
    Study study = null;
    if (studies != null) {
      for (Study study1 : studies) {
        if (study1 != null && study1.getStudyCode().intValue() == studyCode.intValue()) study = study1;
      }
    }
    if (study == null || study.getExplanation() == null) return "";
    SurveySystem sSys = getSurveySystem(study.getSurveySystemId());
    ArrayList<ChartScore> scoresArray = getScores(patientStudies, studyCode);
    ChartUtilities chartUtils = new ChartUtilities(siteInfo);
    return chartUtils.formatExplanationText(dbp, sSys, study, scoresArray, siteId);
  }

  @Override
  public ArrayList<ArrayList<Object>> averageSurveyTimeReport(Date fromDate, Date toDate) {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.averageSurveyTimeReport(dbp.get(), fromDate, toDate, siteId);
  }

  @Override
  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByMonth(Date fromDate, Date toDate) {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.averageSurveyTimeReportByMonth(dbp.get(), fromDate, toDate);
  }

  @Override
  public ArrayList<ArrayList<Object>> averageSurveyTimeReportByType(Date fromDate, Date toDate) {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.averageSurveyTimeReportByType(dbp.get(), fromDate, toDate);
  }

  @Override
  public ArrayList<ArrayList<Object>> complianceSummaryReport(Date from, Date to) {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.complianceSummaryReport(dbp.get(), from, to);
  }

  @Override
  public ArrayList<ArrayList<Object>> complianceReport1() throws IOException {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.complianceReport1(dbp.get());
  }

  @Override
  public ArrayList<ArrayList<Object>> registrationReportData(Date startDt, Date endDt) {
    RegistrationReport report = new RegistrationReport(dbp.get(), startDt, endDt);
    return report.getReportData(siteInfo);
  }

  @Override
  public ArrayList<ArrayList<Object>> visitsReportData(Date startDt, Date endDt) {
    VisitsReport report = new VisitsReport(dbp.get(), startDt, endDt, siteInfo);
    return report.getReportData();
  }

  @Override
  public ArrayList<ArrayList<Object>> exportSquareTable(String tableName) {
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportData(tableName);
  }

  @Override
  public ArrayList<ArrayList<Object>> exportSquareTable(String tableName, Date fromDt, Date toDt) {
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportData(tableName, fromDt, toDt);
  }

  @Override
  public ArrayList<ArrayList<Object>> patientSurveysReport(Date startDt, Date endDt) {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.patientSurveysReport(dbp.get(), startDt, endDt);
  }

  @Override
  public ArrayList<ArrayList<Object>> standardReport(String report, Date startDt, Date endDt) {
    ReportUtils utils = new ReportUtils(siteInfo);
    return utils.standardReport(dbp.get(), report, startDt, endDt);
  }

  @Override
  public String customReport(String reportType, Date startDt, Date endDt) {
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    ReportGenerator report = customizer.getCustomReportGenerator(reportType);
    if (report == null) {
      throw new IllegalArgumentException("No report generator found for report type " + reportType);
    }
    return report.createReport(dbp.get(), startDt, endDt, siteInfo);
  }

  @Override
  public JSONObject customApiReport(String reportName, JsonRepresentation jsonRepresentation) throws Exception {
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    if (customizer != null) {
      ApiReportGenerator reportGenerator = customizer.getCustomApiReportGenerator(reportName);
      if (reportGenerator != null) {
        if (jsonRepresentation == null) {
          return reportGenerator.getReportParameters(dbp, siteInfo, reportName);
        }
        JSONObject reportObject = reportGenerator.getReportData(dbp, siteInfo, reportName, jsonRepresentation);
        if (reportObject != null) {
          return reportObject;
        }
      }
    }
    throw new Exception("Unrecognized report");
  }

  @Override
  public void setAppointmentStatus(ApptId apptId, AppointmentStatus status) {
    String statusArg;
    switch (status) {
    case completed:
      statusArg = "Y";
      break;
    case notCompleted:
      statusArg = "N";
      break;
    default:
      statusArg = null;
    }
    int rows = dbp.get().toUpdate("update appt_registration set appt_complete=? where appt_reg_id = ?")
        .argString(statusArg)
        .argLong(apptId.getId())
        .update();
    if (rows != 1) {
      logger.warn("Wrong number of rows updating appointment status (ApptRegId=" + apptId + ", status=" + status
          + ", rows=" + rows + ")");
    }
  }

  private SurveySystem getSurveySystem(Integer surveySystemId) {
    SurveySystDao ssDao = new SurveySystDao(dbp);
    return ssDao.getSurveySystem(surveySystemId);
  }

  public PDPageContentStream startNewPage(PDPage page, PDPageContentStream contentStream, PDDocument pdf, PDFont font)
      throws IOException {

    return PDFUtils.startNewPage(page, contentStream, pdf, font, fontSize);

  }

  @Override
  public byte[] getImage(JFreeChart chart, int scale) throws IOException {
    return PDFUtils.getImage(chart, scale);

  }


  /**
   * If the patient object has the named attribute, ensure the database agrees
   * else ensure the database has no attribute of that name for the patient.
   */
  private String utilUpdatePatientStringAttribute(Patient pat, final String attributeName) {
    PatientAttribute attr = pat.getAttribute(attributeName);
    if (attr != null && attr.getDataValue() != null) {
      addPatientAttribute(attr);
      return attr.getDataValue();
    } else {
      attr = new PatientAttribute(pat.getPatientId(), attributeName, "", PatientAttribute.STRING);
      deletePatientAttribute(attr);
      return "";
    }
  }

  @Override
  public Boolean updatePatientEmail(Patient pat) { // xxx
    if (pat == null) {
      logger.error("updatePatientEmail called with null patient");
      return false;
    }
    try {
      //insOrUpdPatient(pat);
      String emailAddress = utilUpdatePatientStringAttribute(pat, Constants.ATTRIBUTE_SURVEYEMAIL_ALT);
      utilUpdatePatientStringAttribute(pat, Constants.ATTRIBUTE_SURVEYEMAIL_VALID);


      Date yesterday = DateUtils.getDaysAgoDate(siteInfo, 1);
      //ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrations(pat.getPatientId(), yesterday);
      RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
      ArrayList<PatientRegistration> registrations = customizer.getPatientRegistrations(assessDao, pat, yesterday);

      if (registrations != null) {
        setYesterdaysRegistrationStrings((r,s) -> r.setEmailAddr(s), registrations, emailAddress);
      }
      return true;
    } catch (Exception e) {
      logger.error(e.getMessage() + " occurred in updatePatientEmail", e);
    }

    return false;
  }

  interface StringSetter {
    void doIt(PatientRegistration pReg, String s);
  }

  void setYesterdaysRegistrationStrings(StringSetter setter, ArrayList<PatientRegistration> regs, String s) {
    Database database = dbp.get();
    for (PatientRegistration pReg : regs) {
      setter.doIt(pReg, s);
      SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
      surveyRegUtils.updateRegistration(database, pReg, pReg.getSurveyDt()); // updates all the columns
    }
  }

  @Override
  public Boolean updatePatientNotes(Patient pat) {
    if (pat == null) {
      logger.error("updatePatientNotes called with null patient");
      return false;
    }
    try {
      Date yesterday = DateUtils.getDaysAgoDate(siteInfo, 1);
      ArrayList<PatientRegistration> registrations = assessDao.getPatientRegistrations(pat.getPatientId(), yesterday);
      if (registrations != null) {
        String notes = utilUpdatePatientStringAttribute(pat, Constants.ATTRIBUTE_NOTES);
        setYesterdaysRegistrationStrings((pReg, s) -> pReg.setNotes(s), registrations, notes);
      }
      return true;
    } catch (Exception e) {
      logger.error(e.getMessage() + " occurred in updatePatientNotes", e);
    }
    return false;
  }

  @Override
  public SurveyStart getSurveyStartStatus(Patient patient, ApptId regId) {
    SurveyStart surveyStart = new SurveyStart();
    if (patient == null || !patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)) {
      surveyStart.setLevel(SurveyStart.LEVEL_ERROR);
      surveyStart.setStatus(SurveyStart.STATUS_NO_CONSENT);
      surveyStart.setMessage("The patient is not registered");
      return surveyStart;
    }
    PatientAttribute patientAttribute = patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES);
    if (patientAttribute == null || patientAttribute.getDataValue() == null
        ||
        !"y".equals(patientAttribute.getDataValue().toLowerCase())) {
      surveyStart.setLevel(SurveyStart.LEVEL_ERROR);
      surveyStart.setStatus(SurveyStart.STATUS_NO_CONSENT);
      surveyStart.setMessage("The patient is not registered");
      return surveyStart;
    }
    Date lastCompletedDate = assessDao.getPatientsLastScheduleCompletedDate(patient.getPatientId(), false);
    if (lastCompletedDate == null) {
      ApptRegistration apptRegistration = assessDao.getApptRegistrationByRegId(regId);
      if (apptRegistration != null && apptRegistration.getSurveyType() != null) {
        String type = apptRegistration.getSurveyType();
        logger.debug("type is " + type + " and indexOf('initial') is " + type.toLowerCase().indexOf("initial"));
        if (!(type.toLowerCase().contains("initial"))) {
          surveyStart.setLevel(SurveyStart.LEVEL_WARN);
          surveyStart.setStatus(SurveyStart.STATUS_NO_SURVEYS);
          surveyStart.setMessage("This patient has not completed any surveys");
          return surveyStart;
        }
      }
    }
    surveyStart.setLevel(SurveyStart.LEVEL_GOOD);
    return surveyStart;
  }

  @Override
  public ApptRegistration changeSurveyType(String patientId, ApptId apptId, String newType) throws IllegalArgumentException {
    ApptRegistration apptReg = assessDao.getApptRegistrationByRegId(apptId);
    if (apptReg == null || apptReg.getPatientId() == null || !apptReg.getPatientId().equals(patientId)) {
      throw new IllegalArgumentException("No survey was found for this patient with SurveyRegId " + apptId);
    }

    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    surveyRegUtils.changeSurveyType(dbp.get(), apptReg.getAssessment(), newType, user);

    return apptReg;
  }

  private Date getEmailOutDate() {
    String initialDaysOutString = siteInfo.getProperty("appointment.initialemail.daysout");
    int days = -1;
    if (initialDaysOutString != null) {
      try {
        days = Integer.parseInt(initialDaysOutString);
      } catch (Exception e) {
        logger.error("Error sending notifications", e);
      }
    }
    return new Date(DateUtils.getCalendarDayEnd(siteInfo, DateUtils.getDaysOutDate(siteInfo, days)).getTimeInMillis());
  }

  @Override
  public ArrayList<PDDocument> getPdfs(ArrayList<AssessmentId> assessmentIds, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException {

    logger.debug("getPdfs(assessmentIds)");
    ArrayList<PDDocument> pdfList = new ArrayList<>();
    if (assessmentIds == null) {
      logger.debug("assessmentIds is null returning empty list");
      return pdfList;
    }
    for (AssessmentId assessmentId : assessmentIds) {
      logger.debug("getting pdf for assessmentId " + assessmentId);
      pdfList.add(getPdf(assessmentId, opts, action));
    }

    return pdfList;
  }

  @Override
  public PDDocument getPdf(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException {
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    PatientReport pReport = customizer.getPatientReport(dbp.get(), siteInfo);
    /* First look in the database */
    PDDocument pdf = loadPdf(pReport, assessmentId, action);
    if (pdf != null) {
      markResult(assessmentId, action);
      return pdf;
    }
    /* Not found so get the registration and generate the report */
    AssessmentRegistration assessment = assessDao.getAssessmentById(assessmentId);
    if (assessment == null) return new PDDocument();
    return generatePdf(pReport, assessment, opts, action);
  }

  @Override
  public PDDocument getPdf(String patientId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException {
    //Find the last registration the patient completed
    ApptRegistration registration = assessDao.getLastCompletedRegistration(patientId, null);
    if (registration != null) {
      AssessmentRegistration assessment = registration.getAssessment();
      RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
      PatientReport pReport = customizer.getPatientReport(dbp.get(), siteInfo);
      /* First look in the database */
      PDDocument pdf = loadPdf(pReport, assessment.getAssessmentId(), action);
      if (pdf != null) return pdf;
      /* Not found in db so generate the report */
      return generatePdf(pReport, assessment, opts, action);
    }
    return new PDDocument();
  }

  @Override
  public PDDocument getPdfBefore(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException {
    AssessmentRegistration thisAssessment = assessDao.getAssessmentById(assessmentId);
    // Look out through the end of the day incase the recently assessed one was another appointment on the same day
    ApptRegistration priorRegistration =
        assessDao.getLastCompletedRegistrationBeforeThis(thisAssessment.getPatientId(), DateUtils.getDateEnd(siteInfo, thisAssessment.getAssessmentDt()));
    if (priorRegistration == null) {
      return null;  // there was no previous registration or assessment
    }
    AssessmentRegistration assessment = priorRegistration.getAssessment();
    return getPdf(assessment.getAssessmentId(), opts, action);
  }

  private PDDocument loadPdf(ResultGeneratorIntf pReport, AssessmentId assessmentId, ResultAction action) throws IOException {
    PatientResult result = loadReport(pReport, assessmentId, action);
    if (result != null) {
      return PDDocument.load(new ByteArrayInputStream(result.getResultBytes()));
    }
    return null;
  }

  /**
   * Returns null if pReport == null, and even sometimes if it is not null if there are no results yet
   */
  private PatientResult loadReport(ResultGeneratorIntf pReport, AssessmentId assessmentId, ResultAction action) throws IOException {

    if (pReport != null) {
      PatientResultType type = pReport.getResultType();
      PatientResult result = assessDao.getPatientResult(assessmentId, type.getPatientResTypId());
      return result;
    }

    return null;
  }

  private PDDocument generatePdf(PatientReport pReport, AssessmentRegistration assessment,
      ChartConfigurationOptions opts, ResultAction action) throws IOException {
    ReportUtils report = new ReportUtils(siteInfo);
    PDDocument pdf = report.generatePdf(dbp.get(), pReport, assessment, opts, false /*dont close pdf*/);
    markResult(assessment.getAssessmentId(), action);
    return pdf;
  }

  @Override
  public ArrayList<String> getText(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException, JSONException {
    TextReport tReport = new TextReport(dbp, siteInfo);
    /* First look in the database */
    PatientResult result = loadReport(tReport, assessmentId, action);
    if (result != null && result.getResultBytes() != null && result.getResultBytes().length > 0) {
      logger.debug("found text report with contents: " + new String(result.getResultBytes(), StandardCharsets.UTF_8));
      markResult(assessmentId, action);
      ArrayList<String> reportData = new ArrayList<>();
      ByteArrayInputStream bais = new ByteArrayInputStream(result.getResultBytes());
      InputStreamReader isr = new InputStreamReader(bais, StandardCharsets.UTF_8);
      try {
        BufferedReader response = new BufferedReader(isr);
        String line;
        while ((line = response.readLine()) != null) {
          reportData.add(line);
        }
      } catch (IOException ex) {
        logger.error("", ex);
      } finally {
        try {
          if (isr != null) isr.close();
        } catch (IOException ex) {
          logger.error("", ex);
        }
      }
      return reportData;
   }
    /* Not found so get the json and generate the text report */
    ReportUtils report = new ReportUtils(siteInfo);
    JSONObject json = getJSON(assessmentId, opts, action);
    ArrayList<String> reportArray = report.generateText(dbp.get(), siteId, tReport, json, assessmentId, opts);
    markResult(assessmentId, action);
    return reportArray;
  }

  @Override
  public JSONObject getJSON(AssessmentId assessmentId, ChartConfigurationOptions opts, ResultAction action)
      throws IOException, ServletException, JSONException {
    JsonReport jReport = new JsonReport(dbp, siteInfo, user);
    /* First look in the database */
    PatientResult result = loadReport(jReport, assessmentId, action);
    if (result != null && result.getResultBytes() != null && result.getResultBytes().length > 0) {
      logger.debug("found report with contents: " + new String(result.getResultBytes(), StandardCharsets.UTF_8));
      // check we're doing this for JSON output
      if (action.getPrefix() != null && action.getPrefix().equals('J')) { // check we're doing this for JSON output
        markResult(assessmentId, action);
      }
      JSONObject obj = new JSONObject(new String(result.getResultBytes(), StandardCharsets.UTF_8));
      if (action.getPrefix() != null && action.getPrefix().equals('J')) markResult(assessmentId, action);
      obj.append("patientResId", result.getPatientResId());
      return obj;
    }
    /* Not found so get the registration and generate the report */
    AssessmentRegistration assessment = assessDao.getAssessmentById(assessmentId);
    if (assessment == null) return new JSONObject();
    ReportUtils report = new ReportUtils(siteInfo);
    JSONObject json = report.generateJson(dbp.get(), jReport, assessment, opts);
    if (action.getPrefix() != null && action.getPrefix().equals('J')) markResult(assessment.getAssessmentId(), action);
    return json;
  }

  @Override
  public ScoreProvider getScoreProvider(ArrayList<PatientStudyExtendedData> patientStudies, Integer study) {
    ChartUtilities chartUtils = new ChartUtilities(siteInfo);
    return chartUtils.getScoreProvider(dbp, patientStudies, study, siteId);
  }

  @Override
  public ScoreProvider getScoreProvider(String serviceName, String studyName) {
    return SurveyServiceFactory.getFactory(siteInfo).getScoreProvider(dbp, serviceName, studyName);
  }

  @Override
  public ChartInfo createChartInfo(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
      boolean withChart, ChartConfigurationOptions opts) {
    ChartUtilities chartUtils = new ChartUtilities(siteInfo);
    return chartUtils.createChartInfo(dbp, patientStudies, study, false, opts);
  }

  @Override
  public List<DisplayProvider> findDisplayProviders() {
    UserDao userDao = new UserDao(dbp.get(), user, user);
    List<DisplayProvider> providers = userDao.findDisplayProviders(siteInfo);
    Collections.sort(providers, new DisplayProviderComparator<DisplayProvider>());
    return providers;
  }

  @Override
  public void updateUserPreferences(String key, String preferences) {
    UserDao userDao = new UserDao(dbp.get(), user, user);
    UserPreference preference = new UserPreference();
    preference.setUserPrincipalId(user.getUserPrincipalId());
    preference.setSurveySiteId(siteId);
    preference.setPreferenceKey(key);
    preference.setPreferenceValue(preferences);
    userDao.setUserPreference(preference);
    user.setUserPreferences(key, preferences);
  }

  @Override
  public void setSurveyRegAttribute(AssessmentId asmtId, String surveyName, String name, String newValue) {
    SurveyRegistrationAttributeDao surveyRegAttributeDao = new SurveyRegistrationAttributeDao(dbp.get());
    AssessmentRegistration asmtReg = getAssessmentRegistration(asmtId);
    if (asmtReg != null) {
      List<SurveyRegistration> surveyRegs = asmtReg.getSurveyRegList();
      for(SurveyRegistration surveyReg : surveyRegs) {
        // If surveyName is null then set the attribute for all surveys in the assessment
        // otherwise the attribute for the named survey
        if ((surveyName == null) || surveyName.equals(surveyReg.getSurveyName())) {
          surveyRegAttributeDao.setAttribute(surveyReg.getSurveyRegId(), name, newValue);
        }
      }
    }
  }

  @Override
  public SurveyRegistration getRegistration(String token) {
    return assessDao.getRegistration(token);
  }

  @Override
  public ApptRegistration getLastCompletedRegistration(String patientId) {
    return getApiClinicServices().getLastCompletedRegistration(assessDao, patientId);
  }

  @Override
  public SurveyObj getSurveyObj(String token) throws NotFoundException {
    return getApiClinicServices().getSurveyObj(assessDao, token);
  }

  @Override
  public AssessmentObj getAssessmentObj(Long assessmentId) throws NotFoundException {
    return getApiClinicServices().getAssessmentObj(assessDao, assessmentId);
  }

  @Override
  public ArrayList<AssessmentObj> getAssessmentObjs(String patientId) throws NotFoundException {
    return getApiClinicServices().getAssessmentObjs(assessDao, patientId);
  }

  private ApiClinicServicesUtils getApiClinicServices() {
    return new ApiClinicServicesUtils( user, dbp, siteInfo);
  }

  @Override
  public ArrayList<SiteObj> getSiteObjs() {
    return getApiClinicServices().getSiteObjs();
  }

  @Override
  public SiteObj getSiteObjById(Long id) {
    return getApiClinicServices().getSiteById(id);
  }

  @Override
  public SiteObj getSiteObjByParam(String param) {
    return getApiClinicServices().getSiteByParam(param);
  }

  private PluginDataDao getPluginDataDao() {
    return new PluginDataDao(dbp.get(), siteInfo);
  }

  @Override
  public PluginPatientDataObj findPluginPatientData(Long dataId) {
    return getPluginDataDao().findPluginPatientData(dataId);
  }

  @Override
  public PluginPatientDataObj findPluginPatientData(String dataType, String patientId, String dataVersion) {
    return getPluginDataDao().findPluginPatientData(dataType, patientId, dataVersion);
  }

  @Override
  public ArrayList<PluginPatientDataObj> findAllPluginPatientData(String dataType, String patientId, String dataVersion) {
    return getPluginDataDao().findAllPluginPatientData(dataType, patientId, dataVersion, null, null);
  }

  @Override
  public ArrayList<PluginPatientDataObj> findAllPluginPatientData(String dataType, String patientId, String dataVersion, Date fromTime, Date toTime) {
    return getPluginDataDao().findAllPluginPatientData(dataType, patientId, dataVersion, fromTime, toTime);
  }

  @Override
  public ArrayList<PluginPatientHistoryDataObj> findAllPluginPatientDataHistory(String dataType, String patientId, String dataVersion) {
    return getPluginDataDao().findAllPluginPatientDataHistory(dataType, patientId, dataVersion);
  }

  @Override
  public PluginPatientDataObj addPluginPatientData(String dataType, String patientId, String dataVersion, String dataValue) {
    return getPluginDataDao().addPluginPatientData(dataType, patientId, dataVersion, dataValue);
  }
   @Override
   public RandomSetParticipant updateRandomSetParticipant(RandomSetParticipant rsp) {
     RandomSetter randomSetter = siteInfo.getRandomSet(rsp.getName());
     if (randomSetter == null) { // defensive
       logger.error(siteInfo.getIdString()+" no RandomSet exists with name: "+rsp.getName());
       return rsp;
     }
     return randomSetter.updateParticipant(siteInfo, dbp.get(), user, rsp);
   }

   @Override
   public ArrayList<RandomSetParticipant> getRandomSets(String patientId) {
     RandomSetDao dao = new RandomSetDao(siteInfo, dbp.get());
     return dao.getTreatmentSetParticipantsForUi(patientId);
   }

   @Override
   public Boolean customActionMenuCommand(String action,AssessmentId asmtId, Map<String,String> params) {
     RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
     return customizer.customActionMenuCommand(dbp.get(), this, action, asmtId, params);
   }

   @Override
   public Map<String,String> getSurveyRegistrationAttributes(Long surveyRegId) {
     SurveyRegistrationAttributeDao  sregAttrDao = new SurveyRegistrationAttributeDao(dbp.get());
     return sregAttrDao.getAttributes(surveyRegId);
   }

   @Override
   public void setSurveyRegistrationAttribute(Long surveyRegId, String attrName, String attrValue) throws NotFoundException {
    AssessDao assessDao = new AssessDao(dbp.get(),siteInfo);
    SurveyRegistration surveyRegistration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (surveyRegistration == null) {
      throw new NotFoundException ("Not found");
    }
    SurveyRegistrationAttributeDao surveyRegAttributeDao = new SurveyRegistrationAttributeDao(dbp.get());
    surveyRegAttributeDao.setAttribute(surveyRegId, attrName, attrValue);
   }

  @Override
  public ApptRegistration updateApptRegistration(ApptRegistration apptRegistration) {
    assessDao.updateApptRegistration(apptRegistration);
    return apptRegistration;
  }

  @Override
  public void cancelRegistration(AssessmentId assessmentId) throws NotFoundException {
    ApptRegistration apptRegistration = assessDao.getApptRegistrationByAssessmentId(assessmentId);
    if (apptRegistration == null) {
      throw new NotFoundException("Assessment not found");
    }
    apptRegistration.setRegistrationType(Constants.REGISTRATION_TYPE_CANCELLED_APPOINTMENT);
    updateApptRegistration(apptRegistration);
  }

  @Override
  public JSONObject handleCustomApi(String callString, Map<String, String[]> params, JsonRepresentation jsonRep) 
      throws ApiStatusException {
    RegistryCustomizer customizer = siteInfo.getRegistryCustomizer();
    if (customizer != null) {
      ApiCustomHandler handler = customizer.getApiCustomHandler(dbp, siteInfo, user, this);
      if (handler != null) {
        return handler.handle(callString, params, jsonRep);
      }
    }

    logger.warn("Invalid API request " + callString);
    throw new ApiStatusException(Status.CLIENT_ERROR_BAD_REQUEST, callString, "Invalid api request");
  }

}
