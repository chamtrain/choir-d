package edu.stanford.registry.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientResSurveyRegLink implements IsSerializable, Serializable, DataTable {
  private static final long serialVersionUID = 1L;
	
	private Long patientResId =null;
	private Long surveyRegId=null;
	private Long surveySiteId = null;
	public static final String[] HEADERS = { "Patient Result Id", "Survey Registration Id", "Survey Site Id" };

  public static final int[] CHANGE_INDICATORS = {0, 1, 0};
  
	public PatientResSurveyRegLink() {
	}
	
	public PatientResSurveyRegLink(Long patientResId, Long surveyRegId, Long surveySiteId ) {
		this.patientResId = patientResId;
		this.surveyRegId = surveyRegId;
		this.surveySiteId = surveySiteId;
	}

	public Long getPatientResId() {
		return patientResId; 
	}
	
	public void setPatientResId(Long id) {
		patientResId = id;
	}
	
	public Long getSurveyRegId() {
		return surveyRegId; 
	}
	
	public void setSurveyRegId(Long id) {
		surveyRegId = id;
	}
	
	public void setSurveySiteId(Long id) {
		surveySiteId = id;
	}
	
	public Long getSurveySiteId() {
		return surveySiteId;
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
    String[] data = new String[3];
    data[0]=  getPatientResId().toString();
    data[1]=  getSurveyRegId().toString();
    data[1]=  getSurveySiteId().toString();
    return data;
  }

  @Override
  public void setData(String[] data) throws InvalidDataElementException {
    if (data == null || data.length < 3) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    for (int i = 0; i < 3; i++) {
      if (data[i] == null) {
        throw new InvalidDataElementException("Invalid null data value");
      }
    }
    setPatientResId(Long.valueOf(data[0]));
    setSurveyRegId(Long.valueOf(data[1]));
    setSurveySiteId(Long.valueOf(data[1]));
  }

  @Override
  public Integer getMetaVersion() {
    return null;
  }

  @Override
  public void setMetaVersion(Integer vs) {
  }

  @Override
  public Date getDtCreated() {
    return null;
  }

  @Override
  public void setDtCreated(Date created) {
  }

  @Override
  public Date getDtChanged() {
    return null;
  }

  @Override
  public void setDtChanged(Date changed) {
  }
}
