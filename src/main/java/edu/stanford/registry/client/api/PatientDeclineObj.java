package edu.stanford.registry.client.api;

import edu.stanford.registry.shared.DeclineReason;

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

public interface PatientDeclineObj {

  @PropertyName(value = "patientId")
  String getPatientId();

  void setPatientId(String patientId);

  @PropertyName(value = "declineReason")
  DeclineReason getDeclineReason();

  void setDeclineReason(DeclineReason declineReason);

  @PropertyName(value = "reasonOther")
  String getReasonOther();

  void setReasonOther(String reasonOther);

}
