/*
 * Copyright 2016-2017 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.database;

import edu.stanford.registry.server.DataTableObjectConverter;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientExtendedAttribute;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlInsert;
import com.github.susom.database.SqlSelect;

public class PatientDao {
  /**
   * CRUD operations for patientHistory-related data in our database.
   *
   * @author tpacht
   */
  private static final Logger logger = LoggerFactory.getLogger(PatientDao.class);
  private final Database database;
  private final Long siteId;
  private User authenticatedUser;
  // patient_attribute_history
  private final String HISTORY_INSERT = "INSERT INTO patient_attribute_history (patient_attribute_history_id, patient_attribute_id, survey_site_id, patient_id, data_name,  data_value, data_type, "
      + " meta_version, dt_created, dt_changed, user_principal_id) VALUES (:pk, ?, ?, ?, ?, ?, ?, ?, ?, null, ?)";
  // patient_attribute_history
  private final String HISTORY_UPDATE = "INSERT INTO patient_attribute_history (patient_attribute_history_id, patient_attribute_id, survey_site_id, patient_id, data_name,  data_value, data_type, "
      + " meta_version, dt_created, dt_changed, user_principal_id) VALUES (:pk, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
  // patient_attribute_history
  private final String HISTORY_DELETE = "INSERT INTO patient_attribute_history (patient_attribute_history_id, patient_attribute_id, survey_site_id, patient_id, data_name,  data_value, data_type, "
      + " meta_version, dt_created, dt_changed, user_principal_id) VALUES (:pk, ?, ?, ?, ?, ?, ?, ?, ?, :now, ?)";
  // patient
  private static final String PATIENT_SELECT =  "SELECT PATIENT.PATIENT_ID, PATIENT.FIRST_NAME, PATIENT.LAST_NAME, "
      + "PATIENT.DT_BIRTH, PATIENT.CONSENT, PATIENT.META_VERSION, PATIENT.DT_CREATED, PATIENT.DT_CHANGED "
      + " FROM PATIENT ";
  private static final String PATIENT_INSERT = "INSERT INTO PATIENT (PATIENT_ID, FIRST_NAME, LAST_NAME, DT_BIRTH, CONSENT, "
      + "META_VERSION, DT_CREATED, DT_CHANGED) VALUES (?, ?, ?, ?, ?, ?, :now, null)";
  private static final String PATIENT_UPDATE = "UPDATE PATIENT SET FIRST_NAME = ?, LAST_NAME = ?, DT_BIRTH = ?, "
      + "CONSENT = ?, META_VERSION = ?, DT_CHANGED = :now WHERE PATIENT_ID = ? ";
  private static final String PATIENT_ATTR_SELECT = "SELECT PATIENT_ATTRIBUTE_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, DATA_TYPE, META_VERSION, DT_CREATED, DT_CHANGED "
      + " FROM PATIENT_ATTRIBUTE WHERE SURVEY_SITE_ID = ? AND PATIENT_ID = ? ";
  // patient_ext_attr
  private static final String PATIENT_EXT_ATTR_SELECT = "SELECT PATIENT_EXT_ATTR_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, DATA_TYPE, DT_CREATED, DT_CHANGED "
      + " FROM PATIENT_EXT_ATTR WHERE SURVEY_SITE_ID = ? AND PATIENT_ID = ? ";

  private static RowsHandler<ArrayList<Patient>> getPatientHandler() {
    return new RowsHandler<ArrayList<Patient>>() {
      @Override
      public ArrayList<Patient> process(Rows rs) throws Exception {

        return DataTableObjectConverter.convertToObjects(rs, Patient.class);
      }

    };
  }

  public PatientDao(Database database, Long siteId) {
    this.database = database;
    this.siteId = siteId;
  }

  public PatientDao(Database database, Long siteId, User authenticatedUser) {
    this(database, siteId);
    this.authenticatedUser = authenticatedUser;
  }

  public Patient getPatient(String patientId) {
    if (patientId == null)
      return null;
    ArrayList<Patient> results = database.toSelect(
       PATIENT_SELECT + " WHERE PATIENT_ID = ? ")
        .argString(patientId).query(getPatientHandler());

    if (results.size() > 0) {
      Patient patient = results.get(0);
      ArrayList<PatientAttribute> attributes = getAttributes(patientId);
      patient.setAttributes(attributes);
      return patient;
    }
    return null;
  }

  public Patient addPatient(Patient patient) {
    if (patient == null) {
      return null;
    }
    patient.setMetaVersion(0);
    patient.setDtCreated(database.nowPerApp());

    @SuppressWarnings("unused")
    int count =  database.toInsert(PATIENT_INSERT)
        .argString(patient.getPatientId())
        .argString(patient.getFirstName())
        .argString(patient.getLastName())
        .argDate(patient.getDtBirth())
        .argString(patient.getConsent())
        .argInteger(patient.getMetaVersion())
        .argDate(":now", patient.getDtCreated()).insert();
    return patient;
  }

  public Patient getPatientByToken(String token) {
    if (token == null) return null;

    String sql = PATIENT_SELECT
        + " WHERE patient_id in (select patient_id from patient_study where survey_site_id = :site and token = ?)";
    ArrayList<Patient> results = database.toSelect(sql)
        .argLong(":site", siteId)
        .argString(token)
        .query(getPatientHandler());
    if (results != null && results.size() > 0) {
      Patient patient = results.get(0);
      ArrayList<PatientAttribute> attributes = getAttributes(patient.getPatientId());
      patient.setAttributes(attributes);
      return patient;
    }
    return null;
  }

  /**
   * Gets a list of patients with a last name like the input string
   *
   * @param partialLastName Portion of the patients name to search for
   * @return A list of matching patients
   */
  public ArrayList<Patient> getPatientsByName(String partialLastName) {
    ArrayList<Patient> results = database.toSelect(PATIENT_SELECT + " WHERE lower(LAST_NAME) LIKE lower(?)")
        .argString("%" + partialLastName + "%").query(getPatientHandler());
    if (results != null && results.size() > 0) {
      for (Patient pat : results) {
        pat.setAttributes(getAttributes(pat.getPatientId()));
      }
    }
    return results;
  }

  /**
   * Gets a list of patients with the attribute having the specified value.
   */
  public ArrayList<Patient> getPatientsByAttr(String attr, String value) {
    String  SEARCH_BY_ATTR =  " , PATIENT_ATTRIBUTE ATTR WHERE ATTR.PATIENT_ID = PATIENT.PATIENT_ID AND " +
        "ATTR.SURVEY_SITE_ID = :site AND ATTR.DATA_NAME = ? AND LOWER(ATTR.DATA_VALUE) LIKE LOWER(?)";

    ArrayList<Patient> results = database.toSelect(
       PATIENT_SELECT + SEARCH_BY_ATTR)
        .argLong(":site", siteId)
        .argString(attr)
        .argString(value)
        .query(getPatientHandler());
    if (results != null && results.size() > 0) {
      for (Patient pat : results) {
        pat.setAttributes(getAttributes(pat.getPatientId()));
      }
    }
    return results;
  }

  public Patient updatePatient(Patient patient) {
    database.toUpdate(PATIENT_UPDATE) .argString(patient.getFirstName())
        .argString(patient.getLastName())
        .argDate(patient.getDtBirth())
        .argString(patient.getConsent())
        .argInteger(patient.getMetaVersion())
        .argString(patient.getPatientId())
        .argDateNowPerDb(":now").update(1);
    return patient;
  }

  public PatientAttribute getAttribute(String patientId, String dataName) {
    final PatientAttribute attribute = new PatientAttribute();

    PatientAttribute returnAttribute = database.toSelect(
        PATIENT_ATTR_SELECT +  " AND DATA_NAME = ? ")
        .argLong(siteId)
        .argString(patientId)
        .argString(dataName)
        .query(new RowsHandler<PatientAttribute>() {
          @Override
          public PatientAttribute process(Rows rs) throws Exception {
            return DataTableObjectConverter.convertFirstRowToObject(rs, attribute.getClass());
          }
        });
    return (checkAttribute(patientId, dataName, returnAttribute));
  }

  public PatientExtendedAttribute getExtendedAttribute(String patientId, String dataName) {

    PatientExtendedAttribute returnExtendedAttribute = database.toSelect(
        PATIENT_EXT_ATTR_SELECT + " AND DATA_NAME = ? ")
        .argLong(siteId)
        .argString(patientId)
        .argString(dataName)
        .query(new SinglePatientExtAttrCollector());
    return returnExtendedAttribute;
  }

  public ArrayList<PatientExtendedAttribute> getExtendedAttributes(String patientId) {
    return database.toSelect(PATIENT_EXT_ATTR_SELECT)
        .argLong(siteId)
        .argString(patientId)
        .query(new MultiplePatientExtAttrCollector());
  }

  public ArrayList<PatientAttribute> getAttributes(String patientId) {
    return database.toSelect(PATIENT_ATTR_SELECT)
        .argLong(siteId)
        .argString(patientId)
        .query(new PatientAttributeHandler());
  }

  /**
   * Patient objects can be created without fetching their attributes from the database.
   * This fetches
   */
  public void loadPatientAttributes(Patient pat) {
    ArrayList<PatientAttribute> attrs = getAttributes(pat.getPatientId());
    pat.setAttributes(attrs);
  }

  static class PatientAttributeHandler implements RowsHandler<ArrayList<PatientAttribute>> {
    @Override
    public ArrayList<PatientAttribute> process(Rows rs) throws Exception {
      return DataTableObjectConverter.convertToObjects(rs, PatientAttribute.class);
    }
  }

  private final static String fields = "PATIENT_ATTRIBUTE_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, "
      + "DATA_TYPE, META_VERSION, DT_CREATED, DT_CHANGED";
  private final static String PA_TABLE = "patient_attribute";
  private final static String QUERY_MANY_PATIENTS = "SELECT "+fields+" FROM "+PA_TABLE+" WHERE survey_site_id = :siteId and patient_id IN (?)";


  public void loadPatientsAttributes(ArrayList<Patient> pats) {
    if (pats == null || pats.isEmpty()) {
      return;
    }
    if (pats.size() == 1) {
      loadPatientAttributes(pats.get(0));
      return;
    }

    HashMap<String, List<PatientAttribute>> patToHashedAttrList = makeHashOfPatients(pats);
    String[] uniquePatientIdArray = getUniqueHashedPatients(patToHashedAttrList);

    QuerySetExpander expander = new QuerySetExpander("loadPatientsAttrs", uniquePatientIdArray.length, QUERY_MANY_PATIENTS);
    for (String query: expander) {
      SqlSelect sqlSelect = database.toSelect(query);
      sqlSelect = sqlSelect.argLong("siteId", siteId);
      sqlSelect = expander.argStrings(sqlSelect, ix -> uniquePatientIdArray[ix]);

      ArrayList<PatientAttribute>tmpList = sqlSelect.query(new RowsHandler<ArrayList<PatientAttribute>>() {
        @Override public ArrayList<PatientAttribute> process(Rows rs) throws Exception {
          return DataTableObjectConverter.convertToObjects(rs, PatientAttribute.class);
        }
      });
      if (tmpList != null) {  // play it safe
        distributeAttrsToPatients(patToHashedAttrList, tmpList);
      }
    }
  }


  /**
   * Returns a list of the unique patients, so they can be referenced by index
   */
  private String[] getUniqueHashedPatients(HashMap<String, List<PatientAttribute>> patToHashedAttrList) {
    Set<String> patientKeySet = patToHashedAttrList.keySet();
    String ids[] = new String[patientKeySet.size()];
    int ix = 0;
    for (String s: patientKeySet) {
        ids[ix++] = s;
    }
    return ids;
  }


  /**
   * This returns a patientId -> attrList hash for all the patients AND ensures that if two patients
   * have the same ID (but are different objects), they share the same attribute list.
   */
  private HashMap<String, List<PatientAttribute>> makeHashOfPatients(ArrayList<Patient> patients) {
    HashMap<String, List<PatientAttribute>> patAttLists = new HashMap<>(patients.size());

    // Put each patient's attribute list into a hash by patientId. If some patients are duplicates, share hashed list
    for (Patient pat: patients) {
      List<PatientAttribute> list = patAttLists.get(pat.getPatientId());
      if (list != null) {  // this is a duplicate patient- share the list we already have
        pat.setAttributes(list);
        continue;
      }
      // else put the list into hash
      list = pat.getAttributes();
      if (list == null) { // don't expect this...
        pat.setAttributes(list = new ArrayList<PatientAttribute>()); // make a list and store in the patient
      } else {
        list.clear();
      }
      patAttLists.put(pat.getPatientId(), list);
    }
    return patAttLists;
  }


  /**
   * Distributes each attribute in the fetched list to its hashed lists.
   */
  private void distributeAttrsToPatients(HashMap<String, List<PatientAttribute>> idToList,
                                         ArrayList<PatientAttribute>attrList) {
    for (PatientAttribute attr: attrList) {
      idToList.get(attr.getPatientId()).add(attr);
    }
    logger.debug("For {} patients, loaded {} attributes ", idToList.size(), attrList.size());
  }



  // =====

  public PatientDao withAuthenticatedUser(User user) {
    this.authenticatedUser = user;
    return this;
  }

  private void requireAuthUser() {
    if (authenticatedUser == null)
      throw new RuntimeException("Called PatientDao.insertAttribute(a) lacking withAuthenticatedUser(user)");
  }

  // Requires patDor.withAuthenticatedUser(user).insert...
  public PatientAttribute insertAttribute(PatientAttribute attrib) {
    requireAuthUser();
    attrib.setMetaVersion(0);

    // If it exists already see if the value has changed and if so, update it, or delete it if null.
    int rowCount = 0;
    PatientAttribute pattribute = getAttribute(attrib.getPatientId(), attrib.getDataName());
    if (pattribute != null) {
      if (attrib.getDataValue() == null) {
        attrib.setPatientAttributeId(pattribute.getPatientAttributeId());
        deleteAttribute(pattribute);
        writeHistoryDelete(attrib);
      } else if (!attrib.getDataValue().equals(pattribute.getDataValue())) {
        attrib.setPatientAttributeId(pattribute.getPatientAttributeId());
        String sqlString = "UPDATE PATIENT_ATTRIBUTE SET DATA_VALUE = ?, DATA_TYPE = ?, META_VERSION = ?, DT_CHANGED = :now"
            + " WHERE PATIENT_ATTRIBUTE_ID = ? ";
        database.toUpdate(sqlString)
            .argString(attrib.getDataValue() == null ? null : attrib.getDataValue())
            .argString(attrib.getDataType())
            .argInteger(attrib.getMetaVersion())
            .argDateNowPerDb(":now")
            .argLong(attrib.getPatientAttributeId())
            .update();
        attrib = getAttribute(attrib.getPatientId(), attrib.getDataName());
        writeHistoryUpdate(attrib);
      }
      return attrib;
    }

    // Check for null value
    if (attrib.getDataValue() == null) {
      logger.debug("Not adding attribute. Value is null");
      return attrib;
    }

    // Not there so now add it
    logger.debug("Adding attribute: " + attrib.getDataName() + "=" + attrib.getDataValue());

    String sqlString = "INSERT INTO PATIENT_ATTRIBUTE"
        + " (PATIENT_ATTRIBUTE_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME,  DATA_VALUE, DATA_TYPE, META_VERSION, DT_CREATED, DT_CHANGED)"
        + " VALUES (:pk, :site, ?, ?, ?, ?, ?, :now, null)";
    rowCount = database.toInsert(sqlString)
        .argString(attrib.getPatientId())
        .argString(attrib.getDataName())
        .argString(attrib.getDataValue() == null ? null : attrib.getDataValue())
        .argString(attrib.getDataType())
        .argInteger(attrib.getMetaVersion())
        .argPkSeq(":pk", "patient_seq")
        .argLong(":site", siteId)
        .argDateNowPerDb(":now").insert();
    if (rowCount != 1) {
      logger.error("Something went wrong adding patient attribute, add returned: " + rowCount);
    }
    attrib = getAttribute(attrib.getPatientId(), attrib.getDataName());
    writeHistoryInsert(attrib);
    return attrib;
  }

  public PatientExtendedAttribute insertExtendedAttribute(PatientExtendedAttribute attrib) {
    if (authenticatedUser == null)
      throw new RuntimeException("Called PatientDao.insertExtendedAttribute(a) lacking withAuthenticatedUser(user)");

    // If it exists already see if the value has changed and if so, update it, or delete it if null.
    int rowCount = 0;
    PatientExtendedAttribute patientExtendedAttribute = getExtendedAttribute(attrib.getPatientId(), attrib.getDataName());
    if (patientExtendedAttribute != null) {
      if (attrib.getDataValue() == null) {
        attrib.setPatientExtAttrId(patientExtendedAttribute.getPatientExtAttrId());
        rowCount = deleteExtendedAttribute(patientExtendedAttribute);
        writeExtendedHistoryDelete(attrib);
      } else if (!attrib.getDataValue().equals(patientExtendedAttribute.getDataValue())) {
        attrib.setPatientExtAttrId(patientExtendedAttribute.getPatientExtAttrId());
        String sqlString = "UPDATE PATIENT_EXT_ATTR SET DATA_VALUE = ?, DATA_TYPE = ?, DT_CHANGED = :now"
            + " WHERE PATIENT_EXT_ATTR_ID = ? ";
        database.toUpdate(sqlString)
            .argString(attrib.getDataValue() == null ? null : attrib.getDataValue())
            .argString(attrib.getDataType())
            .argDateNowPerDb(":now")
            .argLong(attrib.getPatientExtAttrId())
            .update();
        attrib = getExtendedAttribute(attrib.getPatientId(), attrib.getDataName());
        writeExtendedHistoryUpdate(attrib);
      }
      return attrib;
    }

    // Check for null value
    if (attrib.getDataValue() == null) {
      logger.debug("Not adding extended attribute. Value is null");
      return attrib;
    }

    // Not there so now add it
    logger.debug("Adding extended attribute: " + attrib.getDataName() + "=" + attrib.getDataValue());

    String sqlString = "INSERT INTO PATIENT_EXT_ATTR"
        + " (PATIENT_EXT_ATTR_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, DATA_TYPE, DT_CREATED, DT_CHANGED)"
        + " VALUES (:pk, :site, ?, ?, ?, ?, :now, null)";
    rowCount = database.toInsert(sqlString)
        .argString(attrib.getPatientId())
        .argString(attrib.getDataName())
        .argString(attrib.getDataValue() == null ? null : attrib.getDataValue())
        .argString(attrib.getDataType())
        .argPkSeq(":pk", "patient_seq")
        .argLong(":site", siteId)
        .argDateNowPerDb(":now").insert();
    if (rowCount != 1) {
      logger.error("Something went wrong adding patient extended attribute, add returned: " + rowCount);
    }
    attrib = getExtendedAttribute(attrib.getPatientId(), attrib.getDataName());
    writeExtendedHistoryInsert(attrib);
    return attrib;
  }

  /**
   * Deletes a record from patient extended attribute
   * @param attrib
   * @return number of deleted rows
   */
  public int deleteExtendedAttribute(PatientExtendedAttribute attrib) {
    if (authenticatedUser == null)
      throw new RuntimeException("Called PatientDao.deleteExtendedAttribute(a) lacking withAuthenticatedUser(user)");
    if (attrib == null || attrib.getPatientExtAttrId() == null)
      return 0;

    int rowCount = database.toUpdate("DELETE FROM PATIENT_EXT_ATTR WHERE PATIENT_EXT_ATTR_ID = ? ")
        .argLong(attrib.getPatientExtAttrId()).update();
    if (rowCount != 1) {
      logger.error(attrib.getDataType() + " Attribute for " + attrib.getPatientId()
          + " was not deleted, delete returned: " + rowCount);
      return rowCount;
    }
    writeExtendedHistoryDelete(attrib);
    return rowCount;
  }

  // Requires withAuthenticatedUser(user)
  public int deleteAttribute(PatientAttribute attrib) {
    requireAuthUser();
    if (attrib == null || attrib.getPatientAttributeId() == null)
      return 0;

    int rowCount = database.toUpdate("DELETE FROM PATIENT_ATTRIBUTE WHERE PATIENT_ATTRIBUTE_ID = ? ")
        .argLong(attrib.getPatientAttributeId()).update();
    if (rowCount != 1) {
      logger.error(attrib.getDataType() + " Attribute for " + attrib.getPatientId()
          + " was not deleted, delete returned: " + rowCount);
      return rowCount;
    }
    writeHistoryDelete(attrib);

    return rowCount;

  }

  // ==== utilities

  private void writeHistoryDelete(PatientAttribute attrib) {
    /* insert a row in the history table for the delete */
    SqlInsert insert = (database.toInsert(HISTORY_DELETE)
    .argPkSeq(":pk", "patient_seq")
    .argLong(attrib.getPatientAttributeId())
    .argLong(siteId)
    .argString(attrib.getPatientId())
    .argString(attrib.getDataName())
    .argString(attrib.getDataValue() == null ? "null" : attrib.getDataValue().toString())
    .argString(attrib.getDataType())
    .argInteger(attrib.getMetaVersion())
    .argDate(attrib.getDtCreated())
    .argDateNowPerDb(":now")
    .argLong(authenticatedUser.getUserPrincipalId()));
    insert.insert(1);

  }

  private void writeExtendedHistoryInsert(PatientExtendedAttribute extAttrib) {
    /* insert a row in the extended history table*/
    final String EXTENDED_HISTORY_INSERT = "INSERT INTO PATIENT_EXT_ATTR_HIST (PATIENT_EXT_ATTR_HIST_ID, PATIENT_EXT_ATTR_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, DATA_TYPE, "
        + " DT_CREATED, DT_CHANGED, USER_PRINCIPAL_ID) VALUES(:pk, ?, ?, ?, ?, ?, ?, ?, NULL, ?)";
    SqlInsert insert = (database.toInsert(EXTENDED_HISTORY_INSERT)
        .argLong(extAttrib.getPatientExtAttrId())
        .argLong(siteId)
        .argString(extAttrib.getPatientId())
        .argString(extAttrib.getDataName())
        .argString(extAttrib.getDataValue() == null ? null : extAttrib.getDataValue())
        .argString(extAttrib.getDataType()))
        .argDate(extAttrib.getDtCreated() == null ? new Date() : extAttrib.getDtCreated())
        .argLong(authenticatedUser.getUserPrincipalId())
        .argPkSeq(":pk", "patient_seq");
    insert.insert(1);
  }

  private void writeExtendedHistoryUpdate(PatientExtendedAttribute extAttrib) {
    /* insert a row in the history table for the update */
    final String EXTENDED_HISTORY_UPDATE =
        "INSERT INTO PATIENT_EXT_ATTR_HIST (PATIENT_EXT_ATTR_HIST_ID, PATIENT_EXT_ATTR_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, DATA_TYPE, "
            + " DT_CREATED, DT_CHANGED, USER_PRINCIPAL_ID) VALUES(:pk, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    SqlInsert insert = (database.toInsert(EXTENDED_HISTORY_UPDATE)
        .argPkSeq(":pk", "patient_seq")
        .argLong(extAttrib.getPatientExtAttrId())
        .argLong(siteId)
        .argString(extAttrib.getPatientId())
        .argString(extAttrib.getDataName())
        .argString(extAttrib.getDataValue() == null ? null : extAttrib.getDataValue())
        .argString(extAttrib.getDataType()))
        .argDate(extAttrib.getDtCreated())
        .argDate(extAttrib.getDtChanged())
        .argLong(authenticatedUser.getUserPrincipalId());
    insert.insert(1);
  }

  private void writeExtendedHistoryDelete(PatientExtendedAttribute extAttrib) {
    /* insert a row in the history table for the delete */
    final String EXTENDED_HISTORY_DELETE =
        "INSERT INTO PATIENT_EXT_ATTR_HIST (PATIENT_EXT_ATTR_HIST_ID, PATIENT_EXT_ATTR_ID, SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, DATA_VALUE, DATA_TYPE, "
            + " DT_CREATED, DT_CHANGED, USER_PRINCIPAL_ID) VALUES(:pk, ?, ?, ?, ?, ?, ?, ?, :NOW, ?)";
    SqlInsert insert = (database.toInsert(EXTENDED_HISTORY_DELETE)
        .argPkSeq(":pk", "patient_seq")
        .argLong(extAttrib.getPatientExtAttrId())
        .argLong(siteId)
        .argString(extAttrib.getPatientId())
        .argString(extAttrib.getDataName())
        .argString(extAttrib.getDataValue() == null ? "null" : extAttrib.getDataValue())
        .argString(extAttrib.getDataType())
        .argDate(extAttrib.getDtCreated())
        .argDateNowPerDb(":now")
        .argLong(authenticatedUser.getUserPrincipalId()));
    insert.insert(1);
  }

  private void writeHistoryUpdate(PatientAttribute attrib) {
    /* insert a row in the history table for the update */
    SqlInsert insert = (database.toInsert(HISTORY_UPDATE)
    .argPkSeq(":pk", "patient_seq")
    .argLong(attrib.getPatientAttributeId())
    .argLong(siteId)
    .argString(attrib.getPatientId())
    .argString(attrib.getDataName())
    .argString(attrib.getDataValue() == null ? null : attrib.getDataValue().toString())
    .argString(attrib.getDataType())
    .argInteger(attrib.getMetaVersion()))
    .argDate(attrib.getDtCreated())
    .argDate(attrib.getDtChanged())
    .argLong(authenticatedUser.getUserPrincipalId());
    insert.insert(1);
  }

  private void writeHistoryInsert(PatientAttribute attrib) {
    /* insert a row in the history table for the insert */
    SqlInsert insert = (database.toInsert(HISTORY_INSERT)
    .argLong(attrib.getPatientAttributeId())
    .argLong(siteId)
    .argString(attrib.getPatientId())
    .argString(attrib.getDataName())
    .argString(attrib.getDataValue() == null ? null : attrib.getDataValue().toString())
    .argString(attrib.getDataType())
    .argInteger(attrib.getMetaVersion()))
    .argDate(attrib.getDtCreated() == null ? new Date() : attrib.getDtCreated())
    .argLong(authenticatedUser.getUserPrincipalId())
    .argPkSeq(":pk", "patient_seq");
    insert.insert(1);
  }

   private static final String orientationVideo = "orientationVideo";
   private static final int UPPER_LIMIT = 99999999;
   private boolean checking = false;

   public PatientAttribute checkAttribute(String patientId, String dataName, PatientAttribute attribute) {
      // "orientationVideo" is a special case. if they don't have the attribute yet then we create it
      if (!checking && orientationVideo.equals(dataName) && attribute == null) {
        attribute = new PatientAttribute();
        attribute.setPatientId(patientId);
        attribute.setDataName(dataName);
        attribute.setDataType(PatientAttribute.STRING);
        // get a random # if odd then yes they get the video
        Random randomGenerator = new Random(System.currentTimeMillis());
        if ( (randomGenerator.nextInt(UPPER_LIMIT) & 1) == 0 ) {
          attribute.setDataValue("N"); // Even #
        } else {
          attribute.setDataValue("Y"); // Odd #
        }
        checking = true;
        insertAttribute(attribute);
      }
      return attribute;
    }


   static public class Supplier {
     public PatientDao get(Database database, Long siteId) {
       return new PatientDao(database, siteId);
     }

     public PatientDao get(Database database, Long siteId, User authenticatedUser) {
       return new PatientDao(database, siteId, authenticatedUser);
     }
   }

  static class PatientExtAttrCollector {
    protected PatientExtendedAttribute newAttribFromRS(Rows rs) {
      PatientExtendedAttribute result = new PatientExtendedAttribute();
      result.setPatientExtAttrId(rs.getLongOrNull(1));
      result.setSurveySiteId(rs.getLongOrNull(2));
      result.setPatientId(rs.getStringOrNull(3));
      result.setDataName(rs.getStringOrNull(4));
      result.setDataValue(rs.getStringOrNull(5));
      result.setDataType(rs.getStringOrNull(6));
      result.setDtCreated(rs.getDateOrNull(7));
      result.setDtChanged(rs.getDateOrNull(8));
      return result;
    }
  }

  static class SinglePatientExtAttrCollector extends PatientExtAttrCollector implements RowsHandler<PatientExtendedAttribute> {
    @Override
    public PatientExtendedAttribute process(Rows rs) throws Exception {
      try {
        if (rs.next()) {
          return newAttribFromRS(rs);
        }
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return null;
      }
      return null;
    }
  }

  static class MultiplePatientExtAttrCollector extends PatientExtAttrCollector implements RowsHandler<ArrayList<PatientExtendedAttribute>> {
    @Override
    public ArrayList<PatientExtendedAttribute> process(Rows rs) throws Exception {
      ArrayList<PatientExtendedAttribute> resultList = new ArrayList<>();
      try {
        while (rs.next()) {
          resultList.add(newAttribFromRS(rs));
        }
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return null;
      }
      return resultList;
    }
  }
}

