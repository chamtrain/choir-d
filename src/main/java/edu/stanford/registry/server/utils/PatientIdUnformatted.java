package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;

public class PatientIdUnformatted implements PatientIdFormatIntf {

  @Override
  public String format(String patientIdIn) throws NumberFormatException {
    return patientIdIn;
  }

  @Override
  public boolean isValid(String patientIdIn) throws NumberFormatException {
    return true;
  }

  @Override
  public String getInvalidMessage() {
    return null;
  }

  @Override
  public String printFormat(String patientIdIn) {
    return patientIdIn;
  }
}
