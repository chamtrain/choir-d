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

package edu.stanford.registry.client;

import edu.stanford.registry.client.event.InvalidPatientEvent;
import edu.stanford.registry.client.event.InvalidPatientHandler;
import edu.stanford.registry.client.event.ValidPatientEvent;
import edu.stanford.registry.client.event.ValidPatientHandler;
import edu.stanford.registry.client.service.PatientIdServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.InvalidPatientIdException;
import edu.stanford.registry.shared.Patient;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ValidPatientId extends ValidTextBase {
  public ValidPatientId(ClientUtils utils, PatientIdServiceAsync service) {
    super(utils);
    idService = service;
  }

  private static final Logger log = Logger.getLogger(ValidPatientId.class.getName());
  private PatientIdServiceAsync idService;
  private Patient thisPatient = null;
  private boolean newPatient = false;
  private boolean isFormatError = false;
  private InvalidPatientEvent invalidEvent = null;

  // static {
  //   new RegistryEntryPoint().setServiceEntryPoint(idService, "clinicService");
  // }

  @Override
  public boolean isValidString() {
    return isValidPatient();
  }

  public boolean isValidPatient() {
    invalidEvent = null;
    final boolean validPatient = true;
    isFormatError = false;

    // validate the mrn
    String mrn = getValue();
    if (mrn == null || mrn.trim().length() < 1) {
      setValue("");
      setInvalid(new InvalidPatientEvent("Missing patient id!", true));
      isFormatError = true;
      return false;
    }

    if (!getUtils().isValidPatientId(mrn)) {
      getUtils();
      setInvalid(new InvalidPatientEvent(mrn + " is not a valid patientId format. "
          + getUtils().getPatientIdFormatError(), true));
      isFormatError = true;
      return false;
    }

    // When making the asynchronous call we need to call the methods that
    // fire events directly
    idService.getPatient(mrn, new AsyncCallback<Patient>() {
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof InvalidPatientIdException) {
          InvalidPatientIdException ipid = (InvalidPatientIdException) caught;
          if (ipid.isFormatError()) {
            setInvalid(new InvalidPatientEvent(ipid.getMessage(), ipid.isFormatError()), true);
          } else {
            setInvalid(new InvalidPatientEvent(ipid.getMessage(), ipid.getFormattedString()), true);
          }
        } else {
          setInvalid(new InvalidPatientEvent(caught.toString(), false), true);
        }
      }

      @Override
      public void onSuccess(Patient patientresult) {

        thisPatient = patientresult;
        if (thisPatient == null) {
          log.log(Level.INFO, "ValidPatientId got success, but patient is null!");
          if (!newPatient) {
            isFormatError = false;
            setInvalid(new InvalidPatientEvent("Not found", false), true);
          }
        } else {
          setValid(new ValidPatientEvent(patientresult));
        }
      }
    });

    return validPatient;
  }

  @Override
  public GwtEvent<?> getMissingEvent() {
    return new InvalidPatientEvent("Must enter a valid patient id.", true);
  }

  public void addInvalidPatientHandler(InvalidPatientHandler handler) {
    handlerManager.addHandler(InvalidPatientEvent.getType(), handler);
  }

  public void addValidPatientHandler(ValidPatientHandler handler) {
    handlerManager.addHandler(ValidPatientEvent.getType(), handler);
  }

  public void setNewPatient(boolean isNew) {
    newPatient = isNew;
  }

  public boolean isNewPatient() {
    return newPatient;
  }

  public Patient getPatient() {
    return thisPatient;
  }

  @Override
  public GwtEvent<?> getInvalidEvent() {
    if (invalidEvent != null) {
      return invalidEvent;
    }
    return new InvalidPatientEvent(getValue() + " is not a valid patient id.", isFormatError);
  }

  public void setInvalid(InvalidPatientEvent event) {
    invalidEvent = event;
    super.setInvalid(event, false);
  }

}
