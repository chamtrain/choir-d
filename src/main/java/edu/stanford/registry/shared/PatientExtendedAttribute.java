/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

public class PatientExtendedAttribute implements IsSerializable, Serializable {
  private static final long serialVersionUID = 1L;
  private Long patientExtAttrId;
  private Long surveySiteId;
  private String patientId;
  private String dataName;
  private String dataValue;
  private String dataType;
  private Date dtCreated;
  private Date dtChanged;

  public static final String[] HEADERS = { "Patient Extended Attribute Id", "Survey Site Id", "Patient Id", "Data Name",
      "Data Value", "Data Type", "Date Created", "Date Changed" };
  public final static String[] DATA_TYPES = { "string", "integer", "timestamp" };
  public final static int STRING = 0;
  public final static int INTEGER = 1;
  public final static int TIMESTAMP = 2;
  public final static int DATE = 3;

  public static final int[] CHANGE_INDICATORS = { 0, 0, 1, 1, 1, 1, 0, 0 };

  public PatientExtendedAttribute() {
  }

  /**
   * Creates a String attribute
   */
  public PatientExtendedAttribute(String patientID, String name, String value) {
    this(patientID, name, value, STRING);
  }

  public PatientExtendedAttribute(String patientID, String name, String value, int type) {
    setPatientId(patientID);
    setDataName(name);
    setDataValue(value);
    setDataType(type);
  }

  /**
   * Get the patient extended attribute id
   *
   * @return id of the extended attribute
   */
  public Long getPatientExtAttrId() {
    return patientExtAttrId;
  }

  /**
   * Sets the patient extended attribute Id
   *
   * @param patientExtAttrId
   */
  public void setPatientExtAttrId(Long patientExtAttrId) {
    this.patientExtAttrId = patientExtAttrId;
  }

  /**
   * Get the survey site
   */
  public Long getSurveySiteId() {
    return surveySiteId;
  }

  /**
   * Set the survey site
   */
  public void setSurveySiteId(Long surveySiteId) {
    this.surveySiteId = surveySiteId;
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
   * Gets the name of the data element.
   *
   * @return data name
   */
  public String getDataName() {
    return dataName;
  }

  /**
   * Set the the name of the data element.
   */
  public void setDataName(String name) {
    this.dataName = name;
  }

  /**
   * Gets the value of the attribute.
   *
   * @return value
   */
  public String getDataValue() {
    return dataValue;
  }

  /**
   * Sets the value of the attribute.
   */
  public void setDataValue(String value) {
    this.dataValue = value;
  }

  /**
   * Get the type of data object;
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * Set the dataType
   */
  public void setDataType(String type) {
    dataType = type;
  }

  /**
   * Set the dataType
   *
   * @param dataTypeIndex one of the supported DATA_TYPES[] values
   */
  public void setDataType(int dataTypeIndex) {
    if (dataTypeIndex < DATA_TYPES.length) {
      setDataType(DATA_TYPES[dataTypeIndex]);
    }
  }

  /**
   * Get the date and time this record was created.
   *
   * @return date/time created
   */
  public Date getDtCreated() {
    return dtCreated;
  }

  /**
   * Sets the created date and time.
   *
   * @param created
   */
  public void setDtCreated(Date created) {
    dtCreated = created;
  }

  /**
   * Get the date and time this record was last modified.
   *
   * @return
   */
  public Date getDtChanged() {
    return dtChanged;
  }

  /**
   * Set the last modified date and time.
   *
   * @param changed
   */
  public void setDtChanged(Date changed) {
    dtChanged = changed;
  }
}
