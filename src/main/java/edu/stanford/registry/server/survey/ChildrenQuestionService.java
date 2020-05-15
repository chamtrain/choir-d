package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.survey.client.api.SubmitStatus;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 3/28/2016.
 */
public class ChildrenQuestionService extends QualifyQuestionService implements SurveyServiceIntf {
  private String childAttributeName = "children8to12";

  public ChildrenQuestionService(String patientAttributeName, SiteInfo siteInfo) {
    super(patientAttributeName, siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {

    NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
    if (nextQuestion == null) {
      PatientDao patientDao = new PatientDao(database, patStudyExtended.getSurveySiteId(), ServerUtils.getAdminUser(database));
      Patient patient = patientDao.getPatient(patStudyExtended.getPatientId());
      if (patient != null && patient.getAttribute(childAttributeName) != null
          && patient.getAttribute(childAttributeName).getDataValue() != null
          && attributeValue.Q.toString().equals(patient.getAttribute(childAttributeName).getDataValue())) { // set to done
        patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), childAttributeName, attributeValue.Y.toString(), PatientAttribute.STRING));
      }
    }
    return nextQuestion;
  }

  @Override
  public String getSurveySystemName() {
    return "ChildrenQuestionService";
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    ChildrenQuestionSystem service = ChildrenQuestionSystem.getInstance(getSurveySystemName(), database);
    Study study = new Study(service.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public SurveySystem getSurveySystem(Database database, String qType) {
    return ChildrenQuestionSystem.getInstance(qType, database);
  }


}


  class ChildrenQuestionSystem extends SurveySystem {
    private static final long serialVersionUID = -4382364022282098050L;
    private static ChildrenQuestionSystem me = null;

    private ChildrenQuestionSystem(String surveySystemName, Database database) throws DataException {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
      this.copyFrom(ssys);
    }

    public static ChildrenQuestionSystem getInstance(String surveySystemName, Database database) throws DataException {
      if (me == null) {
        me = new ChildrenQuestionSystem(surveySystemName, database);
      }
      return me;
    }
  }
