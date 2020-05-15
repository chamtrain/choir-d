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

package edu.stanford.registry.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientStudyExtendedData extends PatientStudy implements IsSerializable, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -8237242601996669008L;
  private Patient patient;
  private String studyDescription;
  private String surveySystemName;
  private Integer numberCompleted;
  private boolean assisted;

  // Keep this one, since it's serializable and DataTableObjectConverter creates them
  public PatientStudyExtendedData() {
    super();
  }

	public PatientStudyExtendedData(Long siteId) {
	  super(siteId);
	}

	public PatientStudyExtendedData(PatientStudy patStudy) {
    super(patStudy.getPatientStudyId(), patStudy.getSurveySiteId(), patStudy.getSurveyRegId(), patStudy.getPatientId(),
        patStudy.getSurveySystemId(), patStudy.getStudyCode(), patStudy.getToken(), patStudy
		    .getExternalReferenceId(), patStudy.getOrderNumber(), patStudy.getMetaVersion(), patStudy.getDtCreated(),
		    patStudy.getDtChanged());
	}

	public void setPatient(Patient pat) {
		if (pat != null) {
			patient = pat;
		}
	}

	public Patient getPatient() {
		if (patient == null) {
			patient = new Patient();
		}
		return patient;
	}

	public String getFirstName() {
		return getPatient().getFirstName();
	}

	public void setFirstName(String name) {
		getPatient().setFirstName(name);
	}

	public String getLastName() {
		return getPatient().getLastName();
	}

	public void setLastName(String name) {
  		getPatient().setLastName(name);
	}

	public void setStudy(Study study) {
		if (study != null) {
			setStudyDescription(study.getStudyDescription());
		}
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public void setStudyDescription(String desc) {
		studyDescription = desc;
	}

	public String getSurveySystemName() {
		return surveySystemName;
	}

	public void setSurveySystemName(String name) {
		surveySystemName = name;
	}

	public void setNumberCompleted(Integer num) {
  		numberCompleted = num;
	}

	public Integer getNumberCompleted() {
  		return numberCompleted;
	}

	public boolean isValid() throws InvalidCredentials {
		return true;
	}

  public boolean wasAssisted() {
    return assisted;
  }

  public void setAssisted(boolean wasAssisted) {
    assisted = wasAssisted;
  }
}
