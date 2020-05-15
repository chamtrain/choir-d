package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * As an implementor of SurveyServiceIntf, this will be cached and should not cache a database.
 */
public class QualifyQuestionService extends RegistryAssessmentsService
    implements SurveyServiceIntf {

  public  static final String emptyForm = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Form><Items></Items></Form>";
  private static final String surveySystemNameBase ="QualifyQuestionService" ;
  private static Logger logger = Logger.getLogger(QualifyQuestionService.class);

  private SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);

  public enum attributeValue { Y, N, Q }
  private String patientAttributeName;

  /*
   * The first question is a qualifying question asked if they do not have the attribute or its value is Q
   * The attribute is set to Q when the response equals "1", set to "Y" when there's no more questions
   */
  public QualifyQuestionService(String fullServiceName, SiteInfo siteInfo) {
    super(siteInfo);
    if (fullServiceName.startsWith(surveySystemNameBase)) {
      this.patientAttributeName = fullServiceName.substring(1 + surveySystemNameBase.length());
    } else {
      this.patientAttributeName = fullServiceName; // old code, in case a non-Stanford developer copied it
    }
  }

  static public boolean isMyService(String serviceName) {
    return serviceName.startsWith(surveySystemNameBase);
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {

    QualifyQuestionSystem service = QualifyQuestionSystem.getInstance(surveySystemNameBase + name, database);
    Study study = new Study(service.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  public SurveySystem getSurveySystem(Database database, String qType) {
    return QualifyQuestionSystem.getInstance(qType, database);
  }
  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    String qType = questionaire.getAttribute("type");
    SurveySystem service = getSurveySystem(database, qType);

    String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
    Integer order = Integer.valueOf(qOrder);

    // Get the study
    String studyName = questionaire.getAttribute("value");
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(service.getSurveySystemId(), studyName);

    // Add the study if it doesn't exist
    if (study == null) {
      study = registerAssessment(database, studyName, studyName, "");
    }

    // Get the patient and this study for this patient
    PatientDao patientDao = new PatientDao(database, siteId, user);
    Patient pat = patientDao.getPatient(patientId);
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(pat, study, tok);

    if (patStudy == null) { // not there yet so lets add it
      patStudy = new PatientStudy(siteId);
      patStudy.setExternalReferenceId("");
      patStudy.setMetaVersion(0);
      patStudy.setPatientId(pat.getPatientId());
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(order);
      patStudyDao.insertPatientStudy(patStudy);
    }
  }


  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
      String answerJson)  {

    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);

    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null) { // doesn't exist !
      throw new DataException(
          "Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
              + patStudyExtended.getToken());
    }
    if (submitStatus != null) {
      PatientDao patientDao = new PatientDao(database, patStudyExtended.getSurveySiteId(), ServerUtils.getAdminUser(database));
      if (patStudy.getContents() == null) { // This is the first question

        Patient patient = patStudyExtended.getPatient();
        if (patient != null && patient.getAttribute(patientAttributeName) == null) { // not asked yet
          // process the answer
          logger.debug ("answerJson is :" + answerJson);
          FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
          for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
          /* The format of the fieldId is "ItemOrder:ResponseOrder:Ref" */
            String[] ids = fieldAnswer.getFieldId().split(":");
            if (ids.length > 2) {
              if ("1".equals(ids[0])) {  // Item 1
                if ("1".equals(ids[1])) {// response 1
                  logger.debug("handling item1:response1 " +fieldAnswer.getChoice().size() + " choices");
                  for (String v : fieldAnswer.getChoice()) {
                    logger.debug("choice is " + v);
                    if ("1".equals(v)) {  // YES
                      patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), patientAttributeName, attributeValue.Q.toString(), PatientAttribute.STRING));
                      logger.debug("setting : " + patientAttributeName + " Q");
                    } else if ("2".equals(v)) {
                      patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), patientAttributeName, attributeValue.N.toString(), PatientAttribute.STRING));
                      logger.debug("setting :" + patientAttributeName + " N");
                    } else if ("3".equals(v)) {
                      logger.debug("setting :" + patientAttributeName + " NA");
                      patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), patientAttributeName, "NA", PatientAttribute.STRING));
                    }
                    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
                  }
                }
              }
            }
          }
        }
        logger.debug("First question");
      }
      // Check they should be asked the survey questions
      /*
      Patient patient = patientDao.getPatient(database, patStudyExtended.getPatient().getPatientId());
      if (!patient.hasAttribute(patientAttributeName) || patient.getAttribute(patientAttributeName) == null || patient.getAttribute(patientAttributeName) == null
          || !attributeValue.Q.toString().equals(patient.getAttribute(patientAttributeName).getDataValue())) {
        // Write empty form to the consent questionnaire, add the other questionnaires
        patStudyDao.setPatientStudyContents(database, patStudy, emptyForm, true);
        // And move onto the next questionnaire
        return null;
    }
    */
  }

  // if this was the last question change the attribute value from Q to Y
  NextQuestion nextQuestion = super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  if (nextQuestion == null) {
    Patient patient = patStudyExtended.getPatient();
    if (patient != null && patient.getAttribute(patientAttributeName) != null && patient.getAttribute(patientAttributeName).getDataValue() != null
        && attributeValue.Q.toString().equals(patient.getAttribute(patientAttributeName).getDataValue())) { // set to done
      PatientDao patientDao = new PatientDao(database, patStudyExtended.getSurveySiteId(), ServerUtils.getAdminUser(database));
      patientDao.insertAttribute(new PatientAttribute(patient.getPatientId(), patientAttributeName, attributeValue.Y.toString(), PatientAttribute.STRING));
    }
  }
  return nextQuestion;
}
  public String getSurveySystemName() {
    return surveySystemNameBase + "-" + patientAttributeName;
  }
}
class myQualifySurveySystem extends SurveySystem {

  private static final long serialVersionUID = 6892518545769623563L;
  private static HashMap<String, myQualifySurveySystem> mySystems = new HashMap<>();
  private static Logger logger = Logger.getLogger(myQualifySurveySystem.class);

  private myQualifySurveySystem(Database database, String surveySystemName) throws DataException {
    SurveySystDao ssDao = new SurveySystDao(database);
    SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, logger);
    this.copyFrom(ssys);
  }

  public static SurveySystem getInstance(Database database, String surveySystemName) throws DataException {
    if (mySystems.get(surveySystemName) == null) {
      mySystems.put(surveySystemName, new myQualifySurveySystem(database, surveySystemName));
    }
    return mySystems.get(surveySystemName);

  }
}
  class QualifyQuestionSystem extends SurveySystem {

    /**
     *
     */
    private static final long serialVersionUID = -4382364022282098050L;
    private static QualifyQuestionSystem me = null;

    private QualifyQuestionSystem(String surveySystemName, Database database) throws DataException {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
      this.copyFrom(ssys);
    }

    public static QualifyQuestionSystem getInstance(String surveySystemName, Database database) throws DataException {
      if (me == null) {
        me = new QualifyQuestionSystem(surveySystemName, database);
      }
      return me;

    }

  }



