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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Patient extends DataTableBase implements IsSerializable, Serializable, DataTable {
  private static final long serialVersionUID = 1L;

  private String patientId;
  private String firstName;
  private String lastName;
  private Date dtBirth;
  private String consent;
  private List<PatientAttribute> attributes;
  private String notes;

  public static final String[] HEADERS = { "Patient Id", "First Name", "Last Name", "Date of Birth", "Consent",
      "MetaData Version", "Date Created", "Date Changed" };

  public static final int[] CHANGE_INDICATORS = { 0, 1, 1, 1, 1, 0, 0 };

  public Patient() {

  }

  /**
   * Patient
   */
  public Patient(String patientID, String firstName, String lastName, Date dob) {
    setPatientId(patientID);
    setFirstName(firstName);
    setLastName(lastName);
    setDtBirth(dob);
  }

  public Patient(String patientID, String firstName, String lastName, Date dob, String consent, Integer version,
                 Date dtCreated, Date dtChanged) {
    this(patientID, firstName, lastName, dob);
    setConsent(consent);
    setMetaVersion(version);
    setDtCreated(dtCreated);
    setDtChanged(dtChanged);
  }

  /**
   * Get the patient id.
   *
   * @return patientsId
   */
  public String getPatientId() {
    return patientId;
  }

  /**
   * Set the patient id.
   */
  public void setPatientId(String patientID) {
    patientId = patientID;
  }

  /**
   * Gets the patients first name.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Set the value of the patients first name.
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Gets the patients last name.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the value of the patients last name.
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Get the date of birth.
   */
  public Date getDtBirth() {
    return dtBirth;
  }

  /**
   * Set the date of birth value.
   */
  public void setDtBirth(Date dob) {
    dtBirth = dob;
  }

  /**
   * Get the flag indicating that the consent was given
   */
  public String getConsent() {
    if (consent == null) {
      return "n";
    }
    return consent;
  }

  /**
   * Set the consent flag (y/n)
   */
  public void setConsent(String yn) {
    consent = yn;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   * Returns this patient's attribute list (not a copy of it)
   */
  public List<PatientAttribute> getAttributes() {
    return attributes;
  }

  /**
   * Sets this patient's attributes to the passed list (not a copy of it)
   */
  public void setAttributes(List<PatientAttribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * This patient will share the passed patient's attributes (not a copy of them).
   */
  public void shareAttributes(Patient fromPatient) {
    this.attributes = fromPatient.getAttributes();
  }


  private void ensureAttributesExist() {
    if (attributes == null) {
      attributes = new ArrayList<PatientAttribute>();
    }
  }


  /**
   * Removes any existing attribute with the same name, then adds
   * the passed attribute. Does nothing if the attribute name is null.
   */
  public void addAttribute(PatientAttribute attribute) {
    if (attribute.getDataName() == null) {
      return;
    }
    removeAttribute(attribute.getDataName());
    ensureAttributesExist();

    attributes.add(attribute);
  }

  /**
   * Removes all attributes with the given non-null name.
   * @return true if there were any
   */
  public PatientAttribute removeAttribute(String name) {
    if ((name == null) || (attributes == null)) {
      return null;
    }
    PatientAttribute returnVal = null;
    for (int inx = attributes.size(); inx > 0; inx--) {
      if (name.equals(attributes.get(inx - 1).getDataName())) {
        returnVal = (returnVal == null) ? attributes.get(inx - 1) : returnVal; // return the first one
        attributes.remove(inx - 1);
      }
    }
    return returnVal;
  }

  public boolean hasAttribute(String name) {
    return null != getAttribute(name);
  }

  /**
   * Sets the attribute to the value, even if null, creating a String attribute if none exists.
   * @return the attribute
   */
  public PatientAttribute setAttribute(String name, String value) {
    PatientAttribute attr = getAttribute(name);
    if (attr == null) {
      ensureAttributesExist();
      attr = new PatientAttribute(getPatientId(), name, value, PatientAttribute.STRING);
      attributes.add(attr);
    } else {
      String attrValue = attr.getDataValue();
      if (value == null) {
        if (attrValue != null) {
          attr.setDataValue(null);
        }
      } else if (!value.equals(attrValue)) {
        attr.setDataValue(value);
      }
    }
    return attr;
  }


  /**
   * Returns true if the named attribute exists and its data value.equals(match)
   * or both value and match are null.
   */
  public boolean attributeEquals(String name, String match) {
    PatientAttribute attr = getAttribute(name);
    if (attr == null) {
      return false;
    }
    String value = attr.getDataValue();
    if (match == null) {
      return value == null;
    }
    return match.equals(value);
  }

  public PatientAttribute getAttribute(String name) {
    if (name != null && attributes != null) {
      for (PatientAttribute attribute : attributes) {
        if (name.equals(attribute.getDataName())) {
          return attribute;
        }
      }
    }
    return null;
  }

  /**
   * Returns the attribute value, or
   * defaultValue if there is no such attribute or the attribute value is null (shouldn't happen)
   */
  public String getAttributeString(String name, String defaultValue) {
    PatientAttribute attr = getAttribute(name);
    if (attr != null) {
      String value = attr.getDataValue();
      if (value != null)
        return value;
    }
    return defaultValue;
  }

  /**
   * Returns null if there is no such attribute
   */
  public String getAttributeString(String name) {
    return getAttributeString(name, null);
  }

  /**
   * Returns the ATTRIBUTE_SURVEYEMAIL_ALT attribute value if it's set,
   * otherwise the ATTRIBUTE_SURVEYEMAIL attribute value, else null (never the empty string)
   */
  public String getEmailAddress() {
    String value = getAttributeString(Constants.ATTRIBUTE_SURVEYEMAIL_ALT);
    if ((value != null) && !value.isEmpty()) {
      return value;
    }
    value = getAttributeString(Constants.ATTRIBUTE_SURVEYEMAIL);
    if ((value != null) && !value.equals("")) {
      return value;
    }
    return null;
  }

  public boolean hasValidEmail() {
    if (hasAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID)
        && "n".equals(getAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID).getDataValue())) {
      return false;
    }
    return true;
  }

  public boolean hasDeclined() {
    if (hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "n".equals(getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
      return true;
    }
    return false;
  }

  public boolean hasConsented() {
    if (hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
      return true;
    }
    return false;

  }

  public boolean equalsNamesDobConsent(Patient otherPatient) {
    if (otherPatient == null) {
      return false;
    }
    if (getDtBirth() == null && otherPatient.getDtBirth() != null) {
      return false;
    }
    if (getDtBirth() != null ) {
      if (otherPatient.getDtBirth() == null) {
        return false;
      }
      if (!getDtBirth().equals(otherPatient.getDtBirth())) {
        return false;
      }
    }
    if (!isEqual(getFirstName(), otherPatient.getFirstName())) {
      return false;
    }
    if (!isEqual(getLastName(), otherPatient.getLastName())) {
      return false;
    }
    if (!getConsent().equals(otherPatient.getConsent()) ) {
      return false;
    }
    return true;
  }

  private boolean isEqual(String string1, String string2) {
    if (string1 == null && string2 == null) {
      return true;
    }
    if (string1 == null && string2 != null) {
      return false;
    }
    return (string1.equals(string2));
  }

  /**
   * Get the display column headers.
   */
  @Override
  public String[] getAllHeaders() {
    return HEADERS;
  }

  @Override
  public int[] getChangeIndicators() {
    return CHANGE_INDICATORS;
  }

  /**
   * Return the patient data as a String array
   *
   * @return Array of patients values
   */
  @Override
  public String[] getData(DateUtilsIntf utils) {
    String data[] = new String[8];
    data[0] = getPatientId();
    data[1] = getFirstName();
    data[2] = getLastName();
    data[3] = utils.getDateString(getDtBirth());
    data[4] = getConsent();
    if (getMetaVersion() != null) {
      data[5] = getMetaVersion().toString();
    } else {
      data[5] = "0";
    }
    data[6] = utils.getDateString(getDtCreated());
    if (getDtChanged() == null) {
      data[7] = "";
    } else {
      data[7] = utils.getDateString(getDtChanged());
    }

    return data;
  }

  /**
   * setData will set the local values for the elements that can be changed and will set the dtChanged value to now.
   */
  @Override
  public void setData(String[] data) throws InvalidDataElementException {
    // check that the array has the correct number of entries
    if (data == null || data.length != 8) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    // check that the first 5 elements are not missing
    if (data[0] == null || data[1] == null || data[2] == null || data[3] == null || data[4] == null
        || data[5] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }

    try {
      setPatientId(data[0]);
      setFirstName(data[1]);
      setLastName(data[2]);
      setDtBirth(new Date(Long.valueOf(data[3])));
      setConsent(data[4]);
      setMetaVersion(Integer.valueOf(data[5]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }
  }

  /**
   * Updates this object with all the details from the passed one, for the UI.
   * So updating the patient in PatientDetail will also update the one in the Scheduler.
   * So if you change in the Detail pane, then go back to the Scheduler, it won't be
   * updated (it doesn't change as the model changes), but if you click again on the
   * patient, you'll get the update.
   */
  public void updateFrom(Patient pat) {
    setMetaVersion(pat.getMetaVersion());
    setDtCreated(pat.getDtCreated());
    setDtChanged(pat.getDtChanged());
    firstName = pat.getFirstName();
    lastName = pat.getLastName();
    dtBirth = pat.getDtBirth();
    consent = pat.getConsent();
    if (pat.attributes != null && pat.attributes.size() > 0) {
      attributes = pat.attributes;
    }
    notes = pat.getNotes();
  }
}
