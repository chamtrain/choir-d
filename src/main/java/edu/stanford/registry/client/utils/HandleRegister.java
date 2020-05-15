/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.registry.client.event.ConsentChangeEvent;
import edu.stanford.registry.client.event.ConsentChangeHandler;
import edu.stanford.registry.client.event.ConsentChangeEvent.ConsentChangeType;
import edu.stanford.registry.client.service.Callback;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.shared.DeclineReason;
import edu.stanford.registry.client.widgets.RegistrationPopUp;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;


/**
 * Contains much of the logic to register the patient or decline them.
 * See also, DeclinePopup.
 *
 * In the beginning, they have not registered or declined.
 * Once declined, they can change and be registered.
 * Once registered, they can decline - a window pops up to accept a reason.
 *
 * If they decline<br>
 * On the schedule window, the recommended action is "Nothing (patient declined)"
 * but in the patient details, it says "Declined (the reason)"
 *
 * @author rstr
 */
abstract public class HandleRegister {
  final Logger logger;
  final private ClinicUtils clinicUtils;
  final private ClinicServiceAsync clinicService;
  final private boolean showSRPopup;

  public HandleRegister(Logger logger, ClinicUtils clinicUtil,
                 ClinicServiceAsync clinicService, boolean showSRPopup) {
    this.logger = logger;
    this.clinicUtils = clinicUtil;
    this.clinicService = clinicService;
    this.showSRPopup = showSRPopup;
  }


  public ConsentChangeHandler getConsentChangeHandler() {
    return new ConsentChangeHandler() {
      @Override
      public void onChange(ConsentChangeEvent event) {
        if (event.getConsentChangeType().equals(ConsentChangeType.consented)) {
          Patient pat = event.getPatient();
          doRegister(pat);
        }
        if (event.getConsentChangeType().equals(ConsentChangeEvent.ConsentChangeType.declined)) {
          doDeclined(event.getPatient());
        }
        if (event.getConsentChangeType().equals(ConsentChangeType.completedConsentForm)) {
          setRegisteredOnServer(event.getPatient());
        }
        if (event.getConsentChangeType().equals(ConsentChangeEvent.ConsentChangeType.completedDeclineForm)) {
          setDeclinedOnServer(event.getPatient());
        }
      }

    };
  }


  /**
   * If the patient, id, email and dob are set, tell the server to register,
   * otherwise, put up the mini-registration popup.
   */
  public void doRegister(final Patient patient) {
    if (patient == null || !clinicUtils.isValidPatientId(patient.getPatientId())
        || patient.getEmailAddress() == null || patient.getDtBirth() == null) {
      showMiniRegistration(patient, "Registered", true);
    } else {
      setRegisteredOnServer(patient);
    }
  }


  /**
   * If the patient, id or DOB isn't set up, shows the mini-registration popup,
   * otherwise tells the server the user has declined.
   */
  private void doDeclined(final Patient patient) {
    logger.log(Level.INFO, "in doDeclined");
    if (patient == null || !clinicUtils.isValidPatientId(patient.getPatientId()) || patient.getDtBirth() == null) {
      showMiniRegistration(patient, "Decline", false);
    } else {
      logger.log(Level.INFO, "calling setDeclined");
      setDeclinedOnServer(patient);
    }
  }


  private void showMiniRegistration(final Patient patient, final String pageTitle, final boolean isRegistering) {
    final RegistrationPopUp registerPopUp = new RegistrationPopUp(clinicUtils, clinicService);
    registerPopUp.addChangeHandler(getConsentChangeHandler());

    Long siteId = clinicUtils.getSiteId();
    clinicService.getFormattedPatientId(siteId, patient.getPatientId(), new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        logger.log(Level.SEVERE, "Failed to format patient id " + patient.getPatientId(), caught);
        registerPopUp.doMiniRegistrationPage(patient, pageTitle, isRegistering);
      }

      @Override
      public void onSuccess(String patientId) {
        patient.setPatientId(patientId);
        registerPopUp.doMiniRegistrationPage(patient, pageTitle, isRegistering);
      }
    });
  }


  public static void maybeSetReasonToOther(Patient pat) {
    if (pat.hasAttribute(Constants.ATTRIBUTE_DECLINE_REASON_OTHER)) {
      pat.setAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE, DeclineReason.other.name());
    }
  }


  /**
   * Override this to refresh your widgets.
   */
  abstract public void successOnSetRegisteredOrDeclined(Patient result);


  class HandleRegCallback extends Callback<Patient> {
    @Override
    protected void afterFailure() {
      clinicUtils.hideLoadingPopUp();
    }

    @Override
    public void handleSuccess(Patient result) {
      clinicUtils.hideLoadingPopUp();
      successOnSetRegisteredOrDeclined(result);
    }
  }


  private void setRegisteredOnServer(final Patient pat) {
    if (showSRPopup) {
      clinicUtils.showLoadingPopUp();
    }
    clinicService.setPatientAgreesToSurvey(pat, new HandleRegCallback());
  }


  private void setDeclinedOnServer(final Patient pat) {
    maybeSetReasonToOther(pat);
    if (pat.hasAttribute(Constants.ATTRIBUTE_DECLINE_REASON_CODE)) {
      DeclineReason reason = getDeclineReasonFromCode(pat);
      String declineReason = getDeclinedReason(pat, reason, false);

      clinicService.declineEnrollment(pat, reason, declineReason, new HandleRegCallback());
    }
  }


  public static DeclineReason getDeclineReasonFromCode(Patient pat) {
    String reasonCode = pat.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_CODE);
    return reasonCode == null ? null : DeclineReason.valueOf(reasonCode);
  }


  public static String getDeclinedReason(Patient pat, DeclineReason reason, boolean addParens) {
    String declineReason = "";
    if (reason == null) {
      return "";
    } else if (reason == DeclineReason.other) {
      declineReason = pat.getAttributeString(Constants.ATTRIBUTE_DECLINE_REASON_OTHER, "");
    }
    if (declineReason.isEmpty()) {
      declineReason = reason.getDisplayName();
    }
    return addParens ? ( "(" + declineReason + ")" ) : declineReason;
  }
}
