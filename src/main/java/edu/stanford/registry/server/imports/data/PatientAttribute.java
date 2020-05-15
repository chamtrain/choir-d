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

package edu.stanford.registry.server.imports.data;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.Metric;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.ImportDefinition;
import edu.stanford.registry.server.imports.ImportDefinitionQueue;
import edu.stanford.registry.server.imports.ImportResources;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.server.xchg.data.AttributeFormat;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;

import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class PatientAttribute implements ImportDataSourceManagerIntf {
  private Database database;
  private ImportDefinitionQueue queue;
  private ImportResources resources;

  private static final String[] DEPENDS = { "PatientProfile" };
  private int PATIENT_ID_COLUMN = -1;
  private int[] ATTRIBUTE_COLUMNS = null;
  private String[] ATTRIBUTE_NAMES = null;
  private AttributeFormat[] ATTRIBUTE_FORMATTERS = null;
  private boolean initialized = false;

  private ImportDefinitionQueue profileQueue = null;
  private User user = null;
  private PatientDao patientDao = null;
  private static Logger logger = Logger.getLogger(PatientAttribute.class);
  Long siteId;

  public PatientAttribute() {
    // A public default constructor is required- this an XchgUtil datasource class
  }

  public PatientAttribute(Database database, Long siteId, ImportDefinitionQueue queue, User authenticatedUser) {
    this.siteId = siteId;
    logger.debug("PatientAttribute started");
    this.database = database;
    this.queue = queue;
    this.initialized = false;
    this.user = authenticatedUser;
    patientDao = new PatientDao(database, siteId, authenticatedUser);
  }

  @Override
  public void setDatabase(Database database, SiteInfo siteInfo) {
    this.siteId = siteInfo.getSiteId();
    this.database = database;
    this.user = ServerUtils.getAdminUser(database);
    patientDao = new PatientDao(this.database, siteId, user);
    logger.debug("PatientAttribute set database");
  }

  @Override
  public void setQueue(ImportDefinitionQueue queue) {
    this.queue = queue;
    logger.debug("PatientAttribute set queue");
  }

  public void init() throws Exception {
    if (queue == null) {
      throw new Exception(" no data ");
    }
    if (database == null) {
      throw new Exception(" no database ");
    }
    if (resources == null) {
      throw new Exception(" no definitions");
    }

    /*
     * Get the column where each field is located
     */
    if (queue.getDefinitions() == null) {
      throw new Exception(" queue definitions are missing");
    }
    if (profileQueue == null) {
      throw new Exception(" profile definitions missing");
    }

    /*
     * Make sure the patient id is there
     */
    if (PATIENT_ID_COLUMN < 0) {
      throw new Exception("Patient id column is missing from import file ");
    }

    Metric metric = new Metric(logger.isDebugEnabled());

    /*
     * Count how many attributes there could be
     */
    List<ImportDefinition> defs = (queue.getDefinitions());
    logger.debug(defs.size() + " definitions ");
    ATTRIBUTE_COLUMNS = new int[defs.size()];
    ATTRIBUTE_NAMES = new String[defs.size()];
    ATTRIBUTE_FORMATTERS = new AttributeFormat[defs.size()];

    for (int d = 0; d < defs.size(); d++) {
      ImportDefinition field = defs.get(d);
      // Qualifier is not implemented yet for patient attributes
      // String qualifierStrings[] = field.getQualifier().getQualifiers();
      ATTRIBUTE_COLUMNS[d] = field.getColumn();
      ATTRIBUTE_NAMES[d] = normalizeAttributeName(field.getField());
      ATTRIBUTE_FORMATTERS[d] = (AttributeFormat) field.getFormatter();
    }

    metric.checkpoint("prep");
    initialized = true;
  }

  @Override
  public boolean importData(String[] fields) throws Exception {
    if (!initialized) {
      logger.debug("initializing");
      init();
    }
    Patient patient;
    if (fields == null) {
      logger.debug("called with null fields");

    }
    try {
      String patientId = resources.validMrn(fields[PATIENT_ID_COLUMN]);

      // get Patient
      patient = patientDao.getPatient(patientId);
      if (patient == null) { // skip if patient doesn't exist
        throw new ImportException("Patient not found for mrn " + patientId);
      }
    } catch (Exception e) {
      logger.debug("Error looking up patient ", e);
      throw new ImportException("Error getting patient from column " + PATIENT_ID_COLUMN);
    }

    try {
      // Add the attribute
      if (ATTRIBUTE_COLUMNS != null) {
        for (int a = 0; a < ATTRIBUTE_NAMES.length; a++) {
          if (ATTRIBUTE_COLUMNS[a] > -1 && ATTRIBUTE_COLUMNS[a] < fields.length) {
            if (ATTRIBUTE_FORMATTERS[a] != null) {
              addAttribute(patient, ATTRIBUTE_NAMES[a], ATTRIBUTE_FORMATTERS[a].format(fields[ATTRIBUTE_COLUMNS[a]]),
                  ATTRIBUTE_FORMATTERS[a].getDataType());
            } else {
              addAttribute(patient, ATTRIBUTE_NAMES[a], fields[ATTRIBUTE_COLUMNS[a]],
                  edu.stanford.registry.shared.PatientAttribute.STRING);
            }
          }
        }
      }
    } catch (Exception e) {
      logger.debug("Error setting attribute ", e);
      throw new ImportException("Error setting attribute " + e.getMessage());
    }

    return true;

  }

  @Override
  public void importDataEnd() throws Exception {
    // No processing needed on end of import data
  }

  @Override
  public String getDataSource() {
    return "PatientAttribute";
  }

  @Override
  public void setResources(ImportResources res) {
    this.resources = res;

  }

  /**
   * Add or update the attribute for this patient.
   *
   * @param patient   Patients ID.
   * @return Email Address or an empty string if no email was found.
   * @throws InvalidDataElementException
   */
  protected void addAttribute(Patient patient, String aName, String attributeValueString, int attributeType)
      throws InvalidDataElementException {
    edu.stanford.registry.shared.PatientAttribute patt = null;
    boolean changed = false;
    if (attributeValueString == null || attributeValueString.trim().length() <= 0 ||
        aName == null || aName.trim().length() <= 0) {
      return;
    }

    patt = patient.getAttribute(aName);
    if (patt != null) {
      // Update existing attribute
      if (!Objects.equals(attributeValueString, patt.getDataValue())) {
        changed = true;
      }
      patt.setDataValue(attributeValueString);
    } else {
      // Add new attribute
      changed = true;
      patt = new edu.stanford.registry.shared.PatientAttribute(
          patient.getPatientId(), aName, attributeValueString, attributeType);
    }

    // Update or insert the attribute
    patientDao.insertAttribute(patt);

    // If the survey email address has changed and a manually entered
    // email address does not exist then reset the survey email valid flag
    if (aName.equals(Constants.ATTRIBUTE_SURVEYEMAIL) && changed) {
      if (!patient.hasAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_ALT)) {
        patt = new edu.stanford.registry.shared.PatientAttribute(
            patient.getPatientId(), Constants.ATTRIBUTE_SURVEYEMAIL_VALID, null,
            edu.stanford.registry.shared.PatientAttribute.STRING);
        patientDao.insertAttribute(patt);      
      }
    }
  }

  @Override
  public String[] getDataSourceDependancies() {
    return DEPENDS;
  }

  @Override
  public void addDependancyQueue(ImportDefinitionQueue queue) {
    profileQueue = queue;
    try {
      setPatientFieldColumn();
      logger.debug("patient field column is " + PATIENT_ID_COLUMN);
    } catch (Exception e) {
      logger.debug("Error setting patient id column" + e.toString(), e);
    }
  }

  private void setPatientFieldColumn() throws Exception {
    List<ImportDefinition> defs = profileQueue.getDefinitions();
    logger.debug(defs.size() + " definitions");
    for (ImportDefinition field : defs) {
      if (field == null) {
        throw new Exception(" field definition in queue is null");
      }
      if ("PatientId".equals(field.getField())) {
        PATIENT_ID_COLUMN = field.getColumn();
      }

    }
  }

  private String normalizeAttributeName(String attrName) {
    if (attrName == null) {
      return null;
    }
    attrName = attrName.trim();
    if (attrName.equalsIgnoreCase(Constants.ATTRIBUTE_SURVEYEMAIL)) {
      return Constants.ATTRIBUTE_SURVEYEMAIL;
    }
    if (attrName.equalsIgnoreCase(Constants.ATTRIBUTE_GENDER)) {
      return Constants.ATTRIBUTE_GENDER;
    }
    if (attrName.equalsIgnoreCase(Constants.ATTRIBUTE_RACE)) {
      return Constants.ATTRIBUTE_RACE;
    }
    if (attrName.equalsIgnoreCase(Constants.ATTRIBUTE_ETHNICITY)) {
      return Constants.ATTRIBUTE_ETHNICITY;
    }
    return attrName.toLowerCase();
  }
}
