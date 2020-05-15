package edu.stanford.registry.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientResSurveyRegLinkList  extends ArrayList<Long> implements IsSerializable, Serializable {
  private static final long serialVersionUID = 1L;

  Long surveySiteId;
  Long patientResId;
  
  public PatientResSurveyRegLinkList(Long surveySiteId, Long patientResultId) {
    super();
    this.surveySiteId = surveySiteId;
    this.patientResId = patientResultId;
  }
	
	public Long getPatientResultId() {
		return patientResId;
	}
	
	public List<PatientResSurveyRegLink> getResulttoSurveyRegistrations() {
	  ArrayList<PatientResSurveyRegLink> list = new ArrayList<>();
	  for (int i=0; i<this.size(); i++) {
	    list.add(new PatientResSurveyRegLink(patientResId, this.get(i), surveySiteId));
	  }
	  return list;
	}

	public void addRelationship(Long surveyRegId) {
		this.add(surveyRegId);
	}
	
	public void addRelationship(PatientResSurveyRegLink relationship) {
		this.add(relationship.getSurveyRegId());
	}
	
	public PatientResSurveyRegLink getRelationship(int index) {
	  return new PatientResSurveyRegLink(patientResId, this.get(index), surveySiteId);
	}
	
	public boolean contains(PatientResSurveyRegLink relationship) {
	  return this.contains(relationship.getSurveyRegId());
	}
}
