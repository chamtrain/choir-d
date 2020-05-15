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

import edu.stanford.registry.shared.survey.SurveyQuestionIntf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class PatientStudyObject extends PatientStudy implements Serializable {

	private static final long serialVersionUID = -7931310489621329086L;
  ArrayList<SurveyQuestionIntf> questions;

	public PatientStudyObject() {

	}

	public PatientStudyObject(PatientStudy patStudy) {
		super(patStudy);
	}

  public PatientStudyObject(Long patientStudyId, Long surveySiteId, Long surveyRegId, String patientID,
      Integer surveySystemId, Integer studyCode, String token, String extRef,
	    Integer order, Integer version) {
    super(patientStudyId, surveySiteId, surveyRegId, patientID, surveySystemId, studyCode, token, extRef, order,
        version);
	}

  public PatientStudyObject(Long patientStudyId, Long surveySiteId, Long surveyRegId, String patientID,
      Integer surveySystemId, Integer studyCode, String token, String extRef,
	    Integer order, Integer version, Date dtCreated, Date dtChanged) {
    super(patientStudyId, surveySiteId, surveyRegId, patientID, surveySystemId, studyCode, token, extRef, order,
        version, dtCreated, dtChanged);
	}

	public ArrayList<SurveyQuestionIntf> getQuestions() {
		return questions;
	}

	public void setQuestions(ArrayList<SurveyQuestionIntf> questions) {
		this.questions = questions;
	}

}
