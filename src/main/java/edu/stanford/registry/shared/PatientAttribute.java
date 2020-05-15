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

public class PatientAttribute extends DataTableBase implements IsSerializable, Serializable, DataTable {
  private static final long serialVersionUID = 1L;

  private Long patientAttributeId;
  private Long surveySiteId;
  private String patientId;
  private String dataName;
  private String dataValue;
  private String dataType;

  public static final String[] HEADERS = { "Patient Attribute Id", "Survey Site Id", "Patient Id", "Data Name", "Data Value",
      "Data Type", "MetaData Version",
      "Date Created", "Date Changed" };

  public final static String[] DATA_TYPES = { "string", "integer", "timestamp" };
  public final static int STRING = 0;
  public final static int INTEGER = 1;
  public final static int TIMESTAMP = 2;
  public final static int DATE = 3;

  public static final int[] CHANGE_INDICATORS = { 0, 0, 1, 1, 1, 1, 1, 0, 0 };

  public PatientAttribute() {
  }

  /**
   * Creates a String attribute with a null value
   */
  public PatientAttribute(String patientID, String name) {
    this(patientID, name, null, STRING);
  }

  /**
   * Creates a String attribute
   */
  public PatientAttribute(String patientID, String name, String value) {
    this(patientID, name, value, STRING);
  }

  public PatientAttribute(String patientID, String name, String value, int type) {
    setPatientId(patientID);
    setDataName(name);
    setDataValue(value);
    setDataType(type);
  }

  public PatientAttribute(Long patientAttributeId, String patientID, String name, String value, int type) {
    setPatientAttributeId(patientAttributeId);
    setPatientId(patientID);
    setDataName(name);
    setDataValue(value);
    setDataType(type);
  }

  public PatientAttribute(Long patientAttributeId, String patientID, String name, String value, int type,
      int version,
                          Date dtCreated, Date dtChanged) {
    this(patientAttributeId, patientID, name, value, type);
    this.setMetaVersion(version);
    this.setDtCreated(dtCreated);
    this.setDtChanged(dtChanged);
  }

  /**
   * Get the patient attribute key.
   *
   * @return id of the attribute record
   */
  public Long getPatientAttributeId() {
    return patientAttributeId;
  }

  /**
   * Set the patient attribute key.
   */
  public void setPatientAttributeId(Long patientAttId) {
    this.patientAttributeId = patientAttId;
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
    String data[] = new String[9];
    data[0] = getPatientAttributeId().toString();
    data[1] = getSurveySiteId().toString();
    data[2] = getPatientId();
    data[3] = getDataName();
    data[4] = getDataValue().toString();
    if (DATA_TYPES[TIMESTAMP].equals(getDataType())) {
      data[4] = getDataValue();
    }
    data[5] = getDataType();
    data[6] = getMetaVersion().toString();
    data[7] = utils.getDateString(getDtCreated());
    if (getDtChanged() == null) {
      data[8] = "";
    } else {
      data[8] = utils.getDateString(getDtChanged());
    }

    return data;
  }

  @Override
  public void setData(String[] data) throws InvalidDataElementException {
    // check that the array has the correct number of entries
    if (data == null || data.length != 9) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }

    // check that the required elements are not missing
    if (data[1] == null || data[2] == null || data[3] == null || data[4] == null || data[5] == null || data[6] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }

    try {
      setPatientAttributeId(Long.valueOf(data[0]));
      setSurveySiteId(Long.valueOf(data[1]));
      setPatientId(data[2]);
      setDataName(data[3]);
      setDataType(data[4]);
      setDataValue(data[5]);
      setMetaVersion(Integer.valueOf(data[6]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }
  }

}
