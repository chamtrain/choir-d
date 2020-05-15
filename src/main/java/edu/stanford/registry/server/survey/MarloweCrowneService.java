package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Created by tpacht on 3/28/2016.
 */
public class MarloweCrowneService extends QualifyQuestionService implements SurveyServiceIntf {
  private static final String marloweCrowneAttributeName = "marloweCrowne";
  private static final Logger logger = LoggerFactory.getLogger(MarloweCrowneService.class);
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  public MarloweCrowneService(SiteInfo siteInfo) {
    super(marloweCrowneAttributeName, siteInfo);
  }
  public MarloweCrowneService(String attributeName, SiteInfo siteInfo) {
    super(attributeName, siteInfo);
  }
  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {

    if (submitStatus == null) {

      PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
      PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
      if (patStudy.getContents() == null) { // first question

        Patient patient = patStudyExtended.getPatient();
        if (!patient.hasAttribute(marloweCrowneAttributeName)) {
          /*
           *  Haven't been asked yet so send back the consent question
           */
          return super.handleResponse(database, patStudyExtended, submitStatus, answerJson); // ask the consent question
        } else {
          /*
           * skip the consent question, this assumes we ask the consent only once
           */
          patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
          if (patient.attributeEquals(marloweCrowneAttributeName,"N")) {
            skipMarloweCrowneSurvey(database, patStudyExtended);
          }
        }
      }
    } else {
      return
          handleAnswer(database, patStudyExtended, submitStatus, answerJson);
    }
   return null;
  }

  private NextQuestion handleAnswer(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus, String answerJson) {
    logger.trace(getSurveySystemName() + " checking consent question ");

    Patient patient = patStudyExtended.getPatient();
    PatientDao patientDao = new PatientDao(database, patStudyExtended.getSurveySiteId(), ServerUtils.getAdminUser(database));
    FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
    logger.debug("Writing response");
    NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
    for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
      if ("0".equals(fieldAnswer.getChoice().get(0))) { // YES; write the attribute
        logger.debug("Writing attribute = Y");
              patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), marloweCrowneAttributeName, attributeValue.Y.toString(),
                  PatientAttribute.STRING));
      } else { // NO; write the attribute,
        logger.debug("Writing attribute = N");
        patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), marloweCrowneAttributeName, attributeValue.N.toString(),
            PatientAttribute.STRING));
        skipMarloweCrowneSurvey(database, patStudyExtended);
        return null;
      }
    }
    return nextQuestion;
  }

  private void skipMarloweCrowneSurvey(Database database, PatientStudyExtendedData patStudyExtended) {
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudyExtendedData patientStudyExtendedData = patStudyDao.getPatientStudyExtendedDataByToken(new Token(patStudyExtended.getToken()),
        "Local", ServerUtils.getAdminUser(database));

    if (patientStudyExtendedData.getStudyDescription().startsWith("MCSDSsf")) {
      logger.debug("Setting next survey ( MCSDSsf ) to empty");
      patStudyDao.setPatientStudyContents(patientStudyExtendedData, emptyForm, true);

    }
  }

  @Override
  public String getSurveySystemName() {
    return "MarloweCrowneService";
  }

  public int getSurveySystemId(Database database) {
    return mySurveySystem.getInstance(database, getSurveySystemName()).getSurveySystemId();
  }
  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    MarloweCrownQuestionSystem service = new MarloweCrownQuestionSystem(getSurveySystemName(), database);
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

  static class MarloweCrownQuestionSystem extends SurveySystem {
    private static final long serialVersionUID = -0L;

    private MarloweCrownQuestionSystem(String surveySystemName, Database database) throws DataException {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
      this.copyFrom(ssys);
    }
  }
}


