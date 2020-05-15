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

package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.github.susom.database.Database;

/**
 * Utility class to handle registering patients by appointments type.
 *
 * @author tpacht
 */
public class RegistrationService {

  Database db;
  private ArrayList<QuestionService> questions;
  private SimpleDateFormat dtFormat = new SimpleDateFormat("MM-dd-yyyy");
  private String surveyType;
  private Date startDt;
  private Date expirationDt;
  private static Logger logger = Logger.getLogger(RegistrationService.class);

  public RegistrationService(Database db, String surveyType, String startDateString, String expireDateString, SiteInfo siteInfo) {
    this.db = db;
    this.surveyType = surveyType;
    try {
      if (startDateString == null || startDateString.trim().length() < 10) {
        startDt = dtFormat.parse("01-01-1900");
      } else {
        startDt = dtFormat.parse(startDateString);
      }
    } catch (ParseException e) {
      logger.error("Invalid start date " + startDateString);
    }
    try {
      if (expireDateString == null || expireDateString.trim().length() < 10) {
        expirationDt = dtFormat.parse("01-01-2199");
      } else {
        expirationDt = dtFormat.parse(expireDateString);
      }
    } catch (ParseException e) {
      logger.error("Invalid expiration date " + expireDateString);
    }
    questions = new ArrayList<>();
    ArrayList<Element> questionaires = XMLFileUtils.getInstance(siteInfo).getProcessQuestionaires(surveyType);
    if (questionaires != null) {
      for (Element questionaire : questionaires) {
        String qType = questionaire.getAttribute("type");
        // Add a question service for this questionaire
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(qType);
        questions.add(new QuestionService(questionaire, surveyService));
      }
    }
  }

  public String getName() {
    if (surveyType != null && surveyType.contains(".")) return surveyType.substring(0, surveyType.indexOf("."));
    return surveyType;
  }

  public String getSurveyType() {
    return surveyType;
  }

  public Date getExpirationDate() {
    return expirationDt;
  }

  public Date getStartDate() {
    return startDt;
  }

  public void registerPatient(Database database, String patientId, Long siteId, Token tok, User user)
      throws ServiceUnavailableException {

    if (database == null) {
      throw new ServiceUnavailableException("database is null");
    }
    if (patientId == null) {
      throw new ServiceUnavailableException("patientId is null");
    }
    if (tok == null) {
      throw new ServiceUnavailableException("token is null");
    }

    //ArrayList<PatientStudyExtendedData> data = new ArrayList<PatientStudyExtendedData>();
    for (QuestionService question : questions) {
      //  data.add(questions.get(q).register(database, patientId, tok));
      question.register(database, patientId, siteId, tok, user);
    }
    // return data;
  }

  public boolean matches(String surveyName, Date patientAgreedDate) {
    if (surveyName == null) {
      return false;
    }
    return surveyName.equals(getSurveyType());
    /**
     * Not trying to set initial by date agreed anymore, do exact match only
     *
     if (qualifies(patientAgreedDate)) {
     if (getName().equals(surveyName)) {
     return true;
     }
     if (getSurveyType().equals(surveyName)) {
     return true;
     }
     if (surveyName.contains(".") && surveyName.length() > surveyName.indexOf(".")
     && getName().equals(surveyName.substring(0, surveyName.indexOf(".")))) {
     return true;
     }
     }
     return false;
     **/

  }

  @SuppressWarnings("unused")
  private boolean qualifies(Date agreed) {
    // not agreed, does not qualify
    if (agreed == null) {
      return false;
    }

    // both start and end dates are null so yes qualifies
    if (startDt == null && expirationDt == null) {
      return true;
    }

    // at least one (start/end) is not null
    if (startDt == null) {
      if (agreed.before(expirationDt)) {
        return true;
      }
    } else if (expirationDt == null) {
      if (agreed.equals(startDt) || agreed.after(startDt)) {
        return true;
      }
    } else { // both are not null
      if (agreed.after(startDt) && (expirationDt == null || agreed.before(expirationDt))) {
        return true;
      }
    }
    return false;
  }

  static private class QuestionService {
    private Element questionaire;
    private SurveyServiceIntf surveyService;

    public QuestionService(Element questionaire, SurveyServiceIntf surveyService) {
      this.questionaire = questionaire;
      this.surveyService = surveyService;
    }

    //public Element getQuestionaire() {
    //  return questionaire;
    //}

    //public SurveyServiceIntf getService() {
    //  return surveyService;
    //}

    public void register(Database database, String patientId, Long siteId, Token tok, User user)
        throws ServiceUnavailableException {
      surveyService.registerAssessment(database, questionaire, patientId, tok, user);
    }
  }

}
