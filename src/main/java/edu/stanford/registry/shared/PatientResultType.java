package edu.stanford.registry.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientResultType implements IsSerializable, Serializable, DataTable {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_PATIENT_REPORT=0;
	
	private Long patientResTypId = null;
	private Long surveySiteId = null;
	private String resultName = null;
	private String resultTitle = null;
	 
	public PatientResultType() {
		
	}
	public static final String[] HEADERS = { "Patient Result Type Id", "Survey Site Id", "Result Name", "Result Title" };

	public static final int[] CHANGE_INDICATORS = {0, 1, 1, 1};

	public void setPatientResTypId(Long id) {
		patientResTypId = id;
	}
	
	public Long getPatientResTypId() {
		return patientResTypId;
	}
		
	public void setSurveySiteId(Long id) {
		surveySiteId = id;
	}
	
	public Long getSurveySiteId() {
		return surveySiteId;
	}
	
	public void setResultName(String name) {
		resultName = name;
	}
	
	public String getResultName() {
		return resultName;
	}
	
	public void setResultTitle(String title) {
		resultTitle = title;
	}
	
	public String getResultTitle() {
		return resultTitle;
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
		String[] data = new String[4];
		data[0]=  getPatientResTypId() +"";
		data[1]=  getSurveySiteId().toString();
		data[2]=  getResultName();
		data[3]=  getResultTitle();
		return data;
	}

	@Override
	public void setData(String[] data) throws InvalidDataElementException {
		
		if (data == null || data.length < 4) {
		      throw new InvalidDataElementException("Invalid number of data elements ");
		}

		for (int i = 0; i < 4; i++) {
		      if (data[i] == null) {
		        throw new InvalidDataElementException("Invalid null data value");
		      }
		    }
		setPatientResTypId(Long.valueOf(data[0]));
		setSurveySiteId(Long.valueOf(data[1]));
		setResultName(data[2]);
		setResultTitle(data[3]);
	}

	@Override
	public Integer getMetaVersion() {
		return 0;
	}

	@Override
	public void setMetaVersion(Integer vs) {
		// no column
	}

	@Override
	public Date getDtChanged() {
		return null;
	}

	@Override
	public void setDtChanged(Date changed) {
		// no column
	}

	@Override
	public Date getDtCreated() {
		return null; // no column
	}

	@Override
	public void setDtCreated(Date created) {
		// no column
	}
}
