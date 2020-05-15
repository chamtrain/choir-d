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

package edu.stanford.registry.client.widgets;

import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProcessXml {
  private final ClinicServiceAsync clinicService;

  private static ArrayList<String> processNames;
  private static ArrayList<String> activeProcessNames;
  private static ArrayList<String> surveyProcessNames;
  private static HashMap<String, HashMap<String, String>> processAttributes;
  private static HashMap<String, ArrayList<PatientAttribute>> processPatientAttributes;

  public ProcessXml(ClinicServiceAsync clinicService) {
    this.clinicService = clinicService;
    loadProcessNames();
    loadActiveProcessNames();
    loadSurveyProcessNames();
    loadProcessAttributes();
    loadPatientAttributes();
  }

  private void loadProcessNames() throws ServiceUnavailableException {
    clinicService.getProcessNames(new AsyncCallback<ArrayList<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        throw new ServiceUnavailableException(caught.getMessage());
      }

      @Override
      public void onSuccess(ArrayList<String> result) {
        processNames = result;
      }
    });
  }

  private void loadActiveProcessNames() throws ServiceUnavailableException {
    clinicService.getActiveVisitProcessNames(new AsyncCallback<ArrayList<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        throw new ServiceUnavailableException(caught.getMessage());
      }
 
      @Override
      public void onSuccess(ArrayList<String> result) {
        activeProcessNames = result;
      }
    });
   }
 
  private void loadSurveyProcessNames() throws ServiceUnavailableException {
    clinicService.getSurveyProcessNames(new AsyncCallback<ArrayList<String>>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        throw new ServiceUnavailableException(caught.getMessage());
      }

      @Override
      public void onSuccess(ArrayList<String> result) {
        surveyProcessNames = result;
      }
    });
  }

  private void loadProcessAttributes() throws ServiceUnavailableException {
    clinicService.getAllProcessAttributes(new AsyncCallback<HashMap<String, HashMap<String, String>>>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        throw new ServiceUnavailableException(clinicService.getClass().getName()+": "+caught.getMessage());
      }

      @Override
      public void onSuccess(HashMap<String, HashMap<String, String>> result) {
        processAttributes = result;
      }
    });
  }

  private void loadPatientAttributes() throws ServiceUnavailableException {
    clinicService.getAllPatientAttributes(new AsyncCallback<HashMap<String, ArrayList<PatientAttribute>>>() {
      @Override
      public void onFailure(Throwable caught) {
        caught.printStackTrace();
        throw new ServiceUnavailableException(caught.getMessage());
      }

      @Override
      public void onSuccess(HashMap<String, ArrayList<PatientAttribute>> result) {
        processPatientAttributes = result;
      }
    });
  }

  public ArrayList<String> getProcessNames() {
    return processNames;
  }

  public ArrayList<String> getActiveVisitProcessNames() {
    return activeProcessNames;
  }
 
  public HashMap<String, ArrayList<PatientAttribute>> getProcessPatientAttributes() {
    return processPatientAttributes;
  }

  public String getProcessAttribute(String processName, String processAttributeName) {
    HashMap<String, String> attributes = processAttributes.get(processName);
    if (attributes == null) {
      return null;
    }
    return attributes.get(processAttributeName);
  }

  public boolean isSurveyProcess(String processName) {
    return surveyProcessNames.contains(processName);
  }

}
