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
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientResult implements IsSerializable, Serializable, DataTable {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_PATIENT_REPORT=0;
	
	private Long patientResId =null;
	private Long assessmentRegId=null;
	private Long surveySiteId = null;
	private String documentControlId = null;
	private Long patientResTypId = null;
	private Long patientResVs = null;
	private Date dtCreated;
	private byte[] resultBytes = null;
	
	 
	public PatientResult() {
		
	}
	
	public static final String[] HEADERS = { "Patient Result Id", "Assessment Reg Id", "Survey Site Id",
	      "Document Control Id", "Patient Result Type Id", "Patient Result Version", "Date Created", "Contents" };

	public static final int[] CHANGE_INDICATORS = { 1, 1, 1, 1, 1, 1, 0, 0 };

	public Long getPatientResId() {
		return patientResId; 
	}
	
	public void setPatientResId(Long id) {
		patientResId = id;
	}

	public AssessmentId getAssessmentId() {
	  return new AssessmentId(assessmentRegId);
	}

	public Long getAssessmentRegId() {
    return assessmentRegId;
  }

  public void setAssessmentRegId(Long assessmentRegId) {
    this.assessmentRegId = assessmentRegId;
  }

  public void setSurveySiteId(Long id) {
		surveySiteId = id;
	}
	
	public Long getSurveySiteId() {
		return surveySiteId;
	}
	
	public void setDocumentControlId(String id) {
		documentControlId = id;
	}
	
	public String getDocumentControlId() {
		return documentControlId;
	}
	
	public void setPatientResTypId(Long id) {
		patientResTypId = id;
	}
	
	public Long getPatientResTypId() {
		return patientResTypId;
	}
	
	public void setPatientResVs(Long vs) {
		patientResVs = vs;
	}
	
	public Long getPatientResVs() {
		return patientResVs;
	}

	@Override
  public void setDtCreated(Date created) {
	    dtCreated = created;
	}

	@Override
  public Date getDtCreated() {
	    return dtCreated;
	}
	
	public void setResultBytes(byte[] bytes) {
		resultBytes = bytes;
	}
	
	public byte[] getResultBytes() {
		return resultBytes;
	}
	
	public byte[] getResultBlob() {
		return  getResultBytes();
	}
	
	public void setResultBlob(byte[] bytes) {
		setResultBytes(bytes);
	}

	@Override
	public String[] getAllHeaders() {
		return HEADERS;
	}

	@Override
	public int[] getChangeIndicators() {
		return CHANGE_INDICATORS;
	}

	@Override
	public String[] getData(DateUtilsIntf utils) {
		String[] data = new String[7];
		data[0]= getPatientResId().toString();
		data[1]=  getAssessmentRegId().toString();
		data[2]=  getSurveySiteId().toString();
		data[3]=  getDocumentControlId();
		data[4]=  getPatientResTypId() +"";
		data[5]=  getPatientResVs()+"";
		data[6]=  utils.getDateString(getDtCreated());
		return data;
	}

	@Override
	public void setData(String[] data) throws InvalidDataElementException {
		
		if (data == null || data.length < 6) {
		      throw new InvalidDataElementException("Invalid number of data elements ");
		}

		for (int i = 0; i < 6; i++) {
		      if (data[i] == null) {
		        throw new InvalidDataElementException("Invalid null data value");
		      }
		    }
		setPatientResId(Long.valueOf(data[0]));
		setAssessmentRegId(Long.valueOf(data[1]));
		setSurveySiteId(Long.valueOf(data[2]));
		setDocumentControlId(data[3]);
		setPatientResTypId(Long.valueOf(data[4]));
		setPatientResVs(Long.valueOf(data[5]));
	}

	@Override
	public Integer getMetaVersion() {
		return 0;
	}

	@Override
	public void setMetaVersion(Integer vs) {
		// not used
	}

	@Override
	public Date getDtChanged() {
		return null;
	}

	@Override
	public void setDtChanged(Date changed) {
		// not used
	}
}
