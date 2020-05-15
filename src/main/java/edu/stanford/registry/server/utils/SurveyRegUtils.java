/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.SurveyRegistrationAttributeDao;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Notification;
import edu.stanford.registry.shared.ProcessAttribute;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.comparator.ProcessAttributeComparator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.github.susom.database.ConstraintViolationException;
import com.github.susom.database.Database;

public class SurveyRegUtils {
  private static Logger logger = Logger.getLogger(SurveyRegUtils.class);

  //final protected Database database;
  final protected SiteInfo siteInfo;
  final protected XMLFileUtils suppliedXMLFileUtils;

  public SurveyRegUtils(SiteInfo siteInfo) {
    this(siteInfo, null);
  }

  // For testing
  public SurveyRegUtils(SiteInfo siteInfo, XMLFileUtils supplied) {
    this.siteInfo = siteInfo;
    suppliedXMLFileUtils = supplied;
  }

  // Let tests provide their own XMLFileUtils
  private XMLFileUtils getXMLFileUtils() {
    return suppliedXMLFileUtils != null ? suppliedXMLFileUtils : XMLFileUtils.getInstance(siteInfo);
  }

  /**
   * Create an appointment registration.
   */
  public ApptRegistration createRegistration(AssessDao assessDao, ApptRegistration apptReg) {
    XMLFileUtils xmlFileUtils = getXMLFileUtils();
    String processType = apptReg.getSurveyType();

    // Insert the assessment registration
    AssessmentRegistration assessmentReg = assessDao.insertAssessmentRegistration(apptReg.getAssessment());

    // Insert the appointment registration
    apptReg = assessDao.insertApptRegistration(apptReg);

    // For each process survey (like parent and child), create a survey registration and add it to the appointment
    List<String> surveyNames = xmlFileUtils.getProcessSurveyNames(processType);
    int order = 1;
    for (String surveyName : surveyNames) {
      SurveyRegistration surveyReg = createSurveyReg(assessDao, xmlFileUtils, processType, assessmentReg, surveyName,
          apptReg.getSurveySiteId(), apptReg.getPatientId(), apptReg.getSurveyDt(), order);
      apptReg.addSurveyReg(surveyReg);
      order += 1;
    }

    return apptReg;
  }

  // Even if there are 10 million tokens exist someday, with 2 billion possible values,
  // there's 1/200 chances we generate a duplicate. If we try 8 times, there is just 1 in 200^8
  // chance of not finding a new number, which is about 1/6*10^17 which is about 1/2^59...
  private final int NumTokenTries = 8;

  /**
   * This creates a new SurveyRegistration with a new random token.  Since it's random, it might
   * conflict with a previous one and trigger a ConstraintViolationException. If that happens,
   * we try again.
   */
  private SurveyRegistration createSurveyReg(AssessDao assessDao, XMLFileUtils xmlFileUtils, String processType,
      AssessmentRegistration assessmentReg, String surveyName, Long siteId, String patientId, Date surveyDt,
      int order) {
    String surveyType = xmlFileUtils.getProcessSurveyType(processType, surveyName);
    SurveyRegistration surveyReg = new SurveyRegistration(siteId, patientId, surveyDt, surveyType);
    surveyReg.setAssessmentRegId(assessmentReg.getAssessmentRegId());
    surveyReg.setSurveyName(surveyName);
    surveyReg.setSurveyOrder(Long.valueOf(order));

    int tryNum = 1;
    do {  // once in a while we should see a single exception
      try {
        surveyReg.setToken((new Token()).getToken()); // creates a simple random-number token
        surveyReg = assessDao.insertSurveyRegistration(surveyReg);
        return surveyReg;
      } catch (ConstraintViolationException e) {
        String msg = siteInfo.getIdString()+"Try #"+tryNum+" generated a duplicate SurveyReg token";
        if (tryNum < 3) {
          logger.info(msg);
        } else if (tryNum < 4) {
          logger.warn(msg);
        } else {
          logger.error(msg);
          if (tryNum == NumTokenTries) {  // 50% chance of non-unique site+token in 77k tries
            throw e;
          }
        }
      }
    } while (tryNum++ < NumTokenTries);

    return null;  // it'll throw before it reaches here, but the IDE doesn't figure it out.
  }


  /**
   * Delete an appointment registration.
   */
  public void deleteRegistration(Database database, ApptRegistration apptReg) {
    SurveyRegistrationAttributeDao regAttrDao = new SurveyRegistrationAttributeDao(database);

    List<SurveyRegistration> surveyRegs = apptReg.getSurveyRegList();
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    AssessDao assessDao = new AssessDao(database, siteInfo);

    for(SurveyRegistration surveyReg : surveyRegs) {
      // delete the survey registration attributes
      regAttrDao.deleteAttributes(surveyReg.getSurveyRegId());

      // delete the patient_study rows for the survey registration token
      patStudyDao.deletePatientStudy(surveyReg.getToken());

      // delete the  survey registration
      assessDao.deleteSurveyRegistration(surveyReg);
    }

    AssessmentRegistration assessment = apptReg.getAssessment();

    // delete the notifications for this registration
    assessDao.deleteNotifications(assessment.getAssessmentId());

    // delete the appointment registration
    assessDao.deleteApptRegistration(apptReg);

    // delete the assessment registration
    assessDao.deleteAssessmentRegistration(assessment);
  }

  /**
   * Update the appointment registration.
   */
  public void updateRegistration(Database database, ApptRegistration apptReg, Date newTime) {
    apptReg.setVisitDt(newTime);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    assessDao.updateApptRegistration(apptReg);

    AssessmentRegistration asmtReg = apptReg.getAssessment();
    if (asmtReg != null) {
      asmtReg.setAssessmentDt(newTime);
      assessDao.updateAssessmentRegistration(asmtReg);
    }

    List<SurveyRegistration> surveyRegs = apptReg.getSurveyRegList();
    for(SurveyRegistration surveyReg : surveyRegs) {
      surveyReg.setSurveyDt(newTime);
      assessDao.updateSurveyRegistration(surveyReg);
    }
  }

  /**
   * Change the survey type for the assessment registration.
   *
   * Throws an IllegalArgumentException if the survey has been started or
   * if the processType is incompatible and email has been sent out already.
   */
  public void changeSurveyType(Database database, AssessmentRegistration assessment, String processType, User user)
      throws IllegalArgumentException {
    XMLFileUtils xmlFileUtils = getXMLFileUtils();

    // Check if the survey has already been started
    if (assessment.getNumberCompleted() > 0) {
      throw new IllegalArgumentException("This survey cannot be changed, it has been started");
    }

//    AssessmentRegistration assessmentReg = apptReg.getAssessment();
    List<SurveyRegistration> surveyRegList = assessment.getSurveyRegList();
    List<String> newSurveyNames = xmlFileUtils.getProcessSurveyNames(processType);

    // Determine if the new process type has the same number of surveys and
    // the same survey names
    boolean surveysMatch = true;
    if (surveyRegList.size() != newSurveyNames.size()) {
      surveysMatch = false;
    } else {
      for(String newSurveyName : newSurveyNames) {
        boolean found = false;
        for(SurveyRegistration surveyReg : surveyRegList) {
          if (newSurveyName.equals(surveyReg.getSurveyName())) {
            found = true;
          }
        }
        if (!found) {
          surveysMatch = false;
        }
      }
    }

    AssessDao assessDao = new AssessDao(database, siteInfo);
    // If the number and names of the surveys do not match and an email has
    // already been sent then do not allow the appointment type to be changed
    // as this would cause the tokens sent out in the email to become invalid.
    if (!surveysMatch) {
      List<Notification> sentNotifications =
          assessDao.getSentNotifications(assessment.getAssessmentId());
      if ((sentNotifications != null) && (sentNotifications.size() > 0)) {
        throw new IllegalArgumentException("This survey cannot be changed, the new type is incompatible");
      }
    }

    // Delete the patient studies for the survey registrations
    boolean registerAssessments = false;
    if (surveyRegList != null) {
      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      for (SurveyRegistration surveyReg : surveyRegList) {
        int rowsDeleted = patStudyDao.deletePatientStudy(surveyReg.getToken());
        if (rowsDeleted > 0) {
          registerAssessments = true;
        }
      }
    }

    if (surveysMatch) {
      // If the number and names of the surveys match then we can update the
      // existing survey registrations
      for(SurveyRegistration surveyReg : surveyRegList) {
        String newSurveyType = xmlFileUtils.getProcessSurveyType(processType, surveyReg.getSurveyName());
        assessDao.updateRegistrationType(surveyReg, newSurveyType);
      }
    } else {
      // If the number and names of the surveys do not match then we need to
      // delete the existing survey registrations and create new ones
      for(SurveyRegistration surveyReg : surveyRegList) {
        assessDao.deleteSurveyRegistration(surveyReg);
      }

      // Create a survey registration for each name and store it in the assessment
      assessment.setSurveyRegList(new ArrayList<>());
      Long siteId = assessment.getSurveySiteId();
      String patientId = assessment.getPatientId();
      Date surveyDt = assessment.getAssessmentDt();

      int order = 1;
      for (String surveyName : newSurveyNames) {
        SurveyRegistration surveyReg = createSurveyReg(assessDao, xmlFileUtils, processType, assessment, surveyName,
            siteId, patientId, surveyDt, order);
        assessment.addSurveyReg(surveyReg);
        order += 1;
        // TODO: PatientRegistration needs to be updated on the client side to display the updated tokens
      }
    }

    // Change the assessment type
    assessDao.updateRegistrationType(assessment, processType);

    // Create a survey type changed activity
    Activity newActivity = new Activity(assessment.getPatientId(), Constants.ACTIVITY_SURVEY_TYPE_CHANGED, assessment.getAssessmentId(), null, null);
    if (user != null) {
      newActivity.setUserPrincipalId(user.getUserPrincipalId());
      newActivity.setSurveySiteId(siteInfo.getSiteId());
    }
    ActivityDao activityDao = new ActivityDao(database, siteInfo.getSiteId());
    activityDao.createActivity(newActivity);

    // If patient_study rows existed then register the new patient studies
    if (registerAssessments) {
      registerAssessments(database, assessment, user);
    }
  }

  /**
   * Register the assessments (patient studies) for the survey registrations in an assessment.
   */
  public void registerAssessments(Database database, AssessmentRegistration assessment, User user) {
    for(SurveyRegistration surveyReg : assessment.getSurveyRegList()) {
      registerAssessments(database, surveyReg, user);
    }
  }

  /**
   * Register the assessments (patient studies) for the survey registration.
   */
  public void registerAssessments(Database database, SurveyRegistration registration, User user) {
    if (registration.getNumberCompleted() > 0 || registration.getNumberPending() > 0) {
      // PatientStudies already exist
      logger.debug("Not registering assessments for survey registration id " + registration.getSurveyRegId() +
          " because patient studies already exist");
      return;
    }
    String processName = registration.getSurveyType();
    String patientId = registration.getPatientId();
    Token token = new Token(registration.getToken());
    // Get the list of questionnaires for the process type
    List<Element> questionnaires = getXMLFileUtils().getProcessQuestionaires(processName);
    // Register the assessments for the list of questionnaires
    if (questionnaires != null) {
      for (Element questionnaire : questionnaires) {
        // Get the survey service for the questionnaire type
        String type = questionnaire.getAttribute("type");
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(type);
        if (surveyService == null) {
          throw new ServiceUnavailableException("No survey service found for type: " + type);
        }
        // Register an assessment for the questionnaire
        surveyService.registerAssessment(database, questionnaire, patientId, token, user);
      }
      registration.setNumberPending(questionnaires.size());
    }
    // Create a Registered activity
    Activity newActivity = new Activity(patientId, Constants.ACTIVITY_REGISTERED, registration.getToken());
    if (user != null) {
      newActivity.setUserPrincipalId(user.getUserPrincipalId());
      newActivity.setSurveySiteId(siteInfo.getSiteId());
    }
    ActivityDao activityDao = new ActivityDao(database, registration.getSurveySiteId());
    activityDao.createActivity(newActivity);
  }

  /**
   * Determine the survey type by the type that's valid for # visits and the survey date
   *
   * @param numberAppointments
   * @param surveyDt
   * @return
   */
  public String getVisitType(int numberAppointments, Date surveyDt) throws DataException {
    ArrayList<ProcessAttribute> processAttributes = getVisitTypes();
    for (ProcessAttribute processAttribute : processAttributes) {
      if (processAttribute.getInteger() > numberAppointments) {
        // check start and end dates
        if (surveyDt == null || processAttribute.qualifies(surveyDt)) {
          return processAttribute.getName();
        }
      }
    }
    String name = processAttributes.get(processAttributes.size() - 1).getName();
    logger.debug("GetVisitType-found none: numberAppts="+numberAppointments+", surveyDt="+surveyDt+
                 ", returning last processAttribute="+name);
    return name;
  }

  /**
   * Gets an array of process attributes for processes with the 'visit'
   * attribute, sorted by visit value;
   *
   * @return
   */
  public ArrayList<ProcessAttribute> getVisitTypes() throws DataException {
    ArrayList<ProcessAttribute> processAttributes = new ArrayList<>();
    XMLFileUtils utils = getXMLFileUtils();
    ArrayList<String> names = utils.getProcessNames();
    if (names != null && names.size() > 0) {
      for (String name : names) {
        try {
          String visitAttributeValue = utils.getAttribute(name, "visit");
          Integer visitNumber = Integer.parseInt(visitAttributeValue);
          ProcessAttribute processAttrib = new ProcessAttribute(name, visitNumber);
          processAttrib.setStartDate(utils.getAttributeDate(processAttrib.getName(), XMLFileUtils.ATTRIBUTE_START_DT));
          processAttrib.setEndDate(utils.getAttributeDate(processAttrib.getName(), XMLFileUtils.ATTRIBUTE_EXPIRE_DT));
          processAttributes.add(processAttrib);
        } catch (Exception e) { // ignore
        }
      }
    }
    if (processAttributes.size() > 0) {
      try {
        processAttributes.sort(new ProcessAttributeComparator());
      } catch (Throwable t) {
        logger.error("Got an error, returning list anyway...", t);
      }
    } else {
      throw new DataException("No visit types found!");
    }
    return processAttributes;
  }
}
