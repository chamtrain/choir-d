package edu.stanford.registry.server.shc.pedortho;

import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.imports.data.Appointment2;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class PedOrthoAppointment extends Appointment2 {

  private static Logger logger = Logger.getLogger(PedOrthoAppointment.class);
  private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

  public PedOrthoAppointment() {
    super();
  }

  @Override
  protected String getSurveyType(Patient patient, String visitType, Date apptDate, String providerEid, String curSurveyType) {
    String surveyType;
    try {
      if (patient.attributeEquals(PedOrthoCustomizer.ATTR_SCOLIOSIS, "Y")) {
        if ((providerEid != null) && (providerEid.trim().equals("044310"))) {
          // Assign SRS30 + HealthMindset for Dr Tileston's patients
          surveyType = getSurveyTypeFor(PedOrthoCustomizer.SURVEY_SRS30_HM, apptDate);
        } else {
          surveyType = getSurveyTypeFor(PedOrthoCustomizer.SURVEY_SRS30, apptDate);
        }
      } else {
        surveyType = PedOrthoCustomizer.SURVEY_NOSURVEY;
      }
    } catch (Exception e) {
      logger.error("Unexpected error while processing appointment for " + patient.getPatientId() + " on " + dateTimeFormat.format(apptDate), e);
      surveyType = PedOrthoCustomizer.SURVEY_NOSURVEY;
    }

    return surveyType;
  }

  @Override
  protected void processRegistration(Patient patient, ApptRegistration reg) {
    // Default the Appointment Complete status to Not Completed
    // This is needed because the 'Hide items I dealt with' option on the schedule tab
    // only hides items that have Appointment Complete set.
    if (reg.getApptComplete() == null) {
      reg.setApptComplete(Constants.REGISTRATION_APPT_NOT_COMPLETED);
      AssessDao assessDao = new AssessDao(database, siteInfo);
      assessDao.updateApptRegistration(reg);
    }
  }

  private String getSurveyTypeFor(String name, Date apptDate) throws Exception {
    XMLFileUtils xmlFileUtils = XMLFileUtils.getInstance(siteInfo);
    String surveyType = xmlFileUtils.getActiveProcessForName(name, apptDate);
    if (surveyType == null) {
      throw new Exception("Process not found for survey name " + name + " and date " + apptDate);
    }
    return surveyType;
  }
}
