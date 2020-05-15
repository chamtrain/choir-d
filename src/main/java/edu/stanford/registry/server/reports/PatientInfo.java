package edu.stanford.registry.server.reports;

import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tpacht on 10/9/2015.
 */
public class PatientInfo {

    private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    private Patient patient = null;

    public PatientInfo(Patient patient) {
      this.patient = patient;
    }

    public String getPatientName() {
      if (patient != null) {
        return patient.getFirstName() + " " + patient.getLastName();
      }
      return "";
    }

    public String getMrn() {
      if (patient != null) {
        return patient.getPatientId();
      }
      return "";
    }

    public String getBirthDt() {
      if (patient != null && patient.getDtBirth() != null) {
        Date dob = new Date(patient.getDtBirth().getTime());
        return formatter.format(dob);
      }
      return "";
    }

    public String getAge() {
      if (patient != null && patient.getDtBirth() != null) {
        Date dob = new Date(patient.getDtBirth().getTime());
        int age = DateUtils.getAge(dob);
        if (age > 0) {
          return age + "";
        }
      }
      return "N/A";
    }

    public String getGender() {
      if (patient != null) {
        PatientAttribute pattrib = patient.getAttribute(Constants.ATTRIBUTE_GENDER);
        if (pattrib != null && pattrib.getDataValue().toString().length() > 0) {
          return pattrib.getDataValue().toString().substring(0, 1);
        }
      }
      return "N/A";
    }

    public Patient getPatient() {
      return patient;
    }
  }


