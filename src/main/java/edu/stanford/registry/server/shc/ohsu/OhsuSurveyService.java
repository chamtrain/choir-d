/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.shc.ohsu;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.client.api.SubmitStatus;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.github.susom.database.Database;

public class OhsuSurveyService extends RegistryAssessmentsService
    implements SurveyServiceIntf {

  private static Study emailAddressStudy = null;

  public OhsuSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExt,
                                     SubmitStatus submitStatus, String answer) {
    String studyDescription = patStudyExt.getStudyDescription();
    if ("EmailAddress".equals(studyDescription)) {
      if (submitStatus == null) {
        if (!promptForEmail(patStudyExt.getPatient())) {
          String xmlDocumentString = XMLFileUtils.getInstance(siteInfo).getXML(database, studyDescription);
          PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
          patStudyDao.setPatientStudyContents(patStudyExt, xmlDocumentString, true);
          return null;
        }
        return super.handleResponse(database, patStudyExt, submitStatus, answer);
      }
    }
    return super.handleResponse(database, patStudyExt, submitStatus, answer);
  }

  /**
   * Determine if the patient should be prompted for their email address.
   * Patients who do not have an email address and patients who have
   * turned 18 are prompted for their email address.
   */
  private boolean promptForEmail(Patient patient) {
    if (patient == null) {
      return false;
    }

    if (patient.getEmailAddress() == null) {
      return true;
    }

    if (DateUtils.getAge(patient.getDtBirth()) >= 18) {
      // Get the date the mail address was last updated
      Date emailAttrUpdated = null;
      PatientAttribute emailAttr = patient.getAttribute(Constants.ATTRIBUTE_SURVEYEMAIL);
      if (emailAttr != null) {
        emailAttrUpdated = (emailAttr.getDtChanged() != null) ? emailAttr.getDtChanged() : emailAttr.getDtCreated();
      }

      Date emailAltAttrUpdated = null;
      PatientAttribute emailAltAttr = patient.getAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_ALT);
      if (emailAltAttr != null) {
        emailAltAttrUpdated = (emailAltAttr.getDtChanged() != null) ? emailAltAttr.getDtChanged() : emailAltAttr.getDtCreated();
      }

      Date emailUpdated = null;
      if (emailAttrUpdated == null) {
        emailUpdated = emailAltAttrUpdated;
      } else if (emailAltAttrUpdated == null) {
        emailUpdated = emailAttrUpdated;
      } else {
        emailUpdated = emailAltAttrUpdated.after(emailAttrUpdated) ? emailAltAttrUpdated : emailAttrUpdated;
      }

      if (emailUpdated == null) {
        return true;
      }

      Date birthDate = patient.getDtBirth();
      if (birthDate == null) {
        return false;
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(birthDate);
      cal.add(Calendar.YEAR, 18);
      cal.add(Calendar.MONTH, -1);
      Date turned18 = cal.getTime();

      // If email was last updated before the patient turned 18 then
      // prompt for a new email address as the patient is now completing
      // the survey for themselves
      if (emailUpdated.before(turned18)) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected void assessmentCompleted(Database database, Patient patient, PatientStudy patStudy) {
    if (emailAddressStudy == null) {
      emailAddressStudy = getStudy(database, "EmailAddress");
    }

    if ((emailAddressStudy != null) &&
        (patStudy.getStudyCode().intValue() == emailAddressStudy.getStudyCode().intValue()) ) {
      handleEmailAddr(database, patient, patStudy);
    }


  }

  private void handleEmailAddr(Database database, Patient patient, PatientStudy patStudy) {
    // Get the XML content
    String xmlString = patStudy.getContents();

    // Get the email response value
    String email = XMLFileUtils.xPathQuery(xmlString, "//Response[@ref='email']/value");

    // Set the email attribute if an email address was provided in the
    // EmailAddress survey
    if ((email != null) && !email.equals("")) {
      User admin = ServerUtils.getAdminUser(database);
      PatientDao patientDao = new PatientDao(database, siteId, admin);
      PatientAttribute emailAttr;
      // Clear the email address and set it to make sure the changed date
      // gets updated even if the email address did not change as we depend
      // on the changed date to determine if we should prompt for the email
      emailAttr = new PatientAttribute(
          patient.getPatientId(),Constants.ATTRIBUTE_SURVEYEMAIL_ALT,null,PatientAttribute.STRING);
      patientDao.insertAttribute(emailAttr);
      emailAttr = new PatientAttribute(
          patient.getPatientId(),Constants.ATTRIBUTE_SURVEYEMAIL_ALT,email,PatientAttribute.STRING);
      patientDao.insertAttribute(emailAttr);
    }
  }

  private void handleConsent(Patient patient, PatientStudy patStudy) {
    // Get the XML content
    String xmlString = patStudy.getContents();

    // Look up the ItemResponse value for the Item element with ref='consent'
    String consentResponse = XMLFileUtils.xPathQuery(xmlString, "//Item[@ref='consent']/@ItemResponse");
    // Look up the ItemResponse value for the Item element with ref='assent'
    String assentResponse = XMLFileUtils.xPathQuery(xmlString, "//Item[@ref='assent']/@ItemResponse");

    // The Yes response has response order 0. If Yes send the OhsuConsent email
    // The Yes response has response order 0.
    // If Yes to the consent and not No for the assent then sent email
    if (((consentResponse != null) && consentResponse.equals("0")) &&
        ((assentResponse == null) || !assentResponse.equals("1")) ) {
      String emailAddr = patient.getEmailAddress();

      if (emailAddr != null) {
        Mailer mailer = siteInfo.getMailer();
        EmailTemplateUtils emailUtils = new EmailTemplateUtils();

        String template = emailUtils.getTemplate(siteInfo, "OhsuConsent");
        String subject = emailUtils.getEmailSubject(template);
        String body = emailUtils.getEmailBody(template);

        List<File> attachments = new ArrayList<>();
        URL url = getClass().getClassLoader().getResource("Ohsu/CaregiverConsentForm.pdf");
        if (url == null ) {
          throw new RuntimeException("Did not find CaregiverConsentForm.pdf file");
        }
        attachments.add(new File(url.getFile()));
        url = getClass().getClassLoader().getResource("Ohsu/ChildAssentForm.pdf");
        if (url == null) {
          throw new RuntimeException("Did not find ChildAssentForm.pdf file");
        }
        attachments.add(new File(url.getFile()));

        try {
          mailer.sendTextWithAttachment(emailAddr, null, null, subject, body, attachments);
        } catch (Exception ex) {
          throw new RuntimeException("ERROR trying to send mail OhsuConsent to " + emailAddr, ex);
        }
      }
    }
  }

  /**
   * Return the appropriate score provider based on the study name.
   */
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return OhsuScoreProviderFactory.getScoreProvider(dbp, siteInfo, studyName);
  }

}
