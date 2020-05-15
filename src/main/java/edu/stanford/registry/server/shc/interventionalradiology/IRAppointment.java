package edu.stanford.registry.server.shc.interventionalradiology;

import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.imports.data.Appointment2;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.AssessmentRegistration;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.ProcessAttribute;

import java.util.Date;
import java.util.List;

public class IRAppointment extends Appointment2 {

  // Any creator must also call setDatabase(db, siteId)
  public IRAppointment() {
    super();
  }

  @Override
  protected String getSurveyType(String patientId, String visitType, Date apptDate, String curSurveyType) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    ApptRegistration lastCompleted = assessDao.getLastCompletedRegistration(patientId, apptDate);
    if (lastCompleted == null) {
      return (curSurveyType == null) ? IRCustomizer.SURVEY_NOSURVEY : curSurveyType;
    }

    AssessmentRegistration lastAssessment = lastCompleted.getAssessment();
    String lastType = lastAssessment.getAssessmentType();

    int visits = getIRConsent(patientId) ? 1 : 0;

    if (lastType.contains("DVT")) {
      return getFollowUp("DVT", visits, apptDate);
    } else if (lastType.contains("Lymphedema")) {
      return getFollowUp("Lymphedema", visits, apptDate);
    } else if (lastType.contains("LymLeg")) {
      return getFollowUp("LymLeg", visits, apptDate);
    } else if (lastType.contains("LymArm")) {
      return getFollowUp("LymArm", visits, apptDate);
    } else {
      return IRCustomizer.SURVEY_NOSURVEY;
    }
  }

  private boolean getIRConsent(String patientId) {
    PatientAttribute attr = patientDao.getAttribute(patientId, IRCustomizer.ATTR_IR_CONSENT);
    if (attr == null) {
      return false;
    }
    String strValue = attr.getDataValue();
    if ((strValue != null) && strValue.equalsIgnoreCase("Y")) {
      return true;
    }
    return false;
  }

  private String getFollowUp(String type, int visits, Date apptDt) {
    SurveyRegUtils surveyRegUtils = new SurveyRegUtils(siteInfo);
    List<ProcessAttribute> processAttributes = surveyRegUtils.getVisitTypes();

    for (ProcessAttribute processAttribute : processAttributes) {
      if (processAttribute.getName().contains(type)) {
        if (processAttribute.getInteger() > visits) {
          if (apptDt == null || processAttribute.qualifies(apptDt)) {
            return processAttribute.getName();
          }
        }
      }
    }

    return processAttributes.get(processAttributes.size() - 1).getName();
  }
}
