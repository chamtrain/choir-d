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
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.imports.ImportDefinition;
import edu.stanford.registry.server.imports.ImportDefinitionQueue;
import edu.stanford.registry.server.imports.ImportResources;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.InvalidPatientIdException;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.User;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class PatientProfile implements ImportDataSourceManagerIntf {

  private Database database;
  private ImportDefinitionQueue queue;
  @SuppressWarnings("unused")
  private ImportResources resources;

  private static final String[] NO_DEPENDANCIES = new String[0];
  private static final String[] DATA_FIELDS = new String[] { "PatientId", "FirstName", "LastName", "DtBirth" };
  private final ImportDefinition[] DATA_DEFINITIONS = new ImportDefinition[5];
  private PatientDao patientDao = null;
  private User user = null;
  private boolean initialized = false;
  private static final Logger logger = Logger.getLogger(PatientProfile.class);
  SiteInfo siteInfo;

  public PatientProfile() {
    // A public default constructor is required- this an XchgUtil datasource class
  }

  public PatientProfile(Database database, SiteInfo siteInfo, ImportDefinitionQueue queue, User authenticatedUser) {
    this.siteInfo = siteInfo;
    this.database = database;
    this.queue = queue;
    this.user = authenticatedUser;
  }

  @Override
  public void setDatabase(Database database, SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
    this.database = database;
  }

  @Override
  public void setQueue(ImportDefinitionQueue queue) {
    this.queue = queue;
  }

  public void init() throws Exception {
    if (queue == null) {
      throw new Exception("No field definitions!");
    }
    List<ImportDefinition> definitions = queue.getDefinitions();

    for (ImportDefinition def : definitions) {
      for (int f = 0; f < DATA_FIELDS.length; f++) {
        if (DATA_FIELDS[f].equals(def.getField())) {
          DATA_DEFINITIONS[f] = def;
        }
      }
    }
    initialized = true;
  }

  @Override
  public boolean importData(String[] fields) throws Exception {
    if (!initialized) {
      init();
    }
    if (DATA_DEFINITIONS[0] == null) {
      // we can't insert/update a patient without their patient id
      throw new InvalidDataElementException("Missing patientId");
    }
    // Patient id
    int col = DATA_DEFINITIONS[0].getColumn();
    if (fields.length < (DATA_DEFINITIONS[0].getColumn() + 1)) {
      throw new InvalidPatientIdException("MRN is missing");
    }
    logger.debug("Checking patientId of " + fields[col]);
    //String patientId = resources.validMrn(fields[col]);
    String patientId = getStringField(DATA_DEFINITIONS[0], fields[col]);
    // get Patient
    Patient patient = getPatientDao().getPatient(patientId);

    String firstName = null;
    String lastName = null;
    String dobString = null;
    Date dtBirth = null;

    if (DATA_DEFINITIONS[1] != null && fields.length > DATA_DEFINITIONS[1].getColumn()) {
      firstName = getStringField(DATA_DEFINITIONS[1], fields[DATA_DEFINITIONS[1].getColumn()]);
    }
    if (DATA_DEFINITIONS[2] != null && fields.length > DATA_DEFINITIONS[2].getColumn()) {
      lastName = getStringField(DATA_DEFINITIONS[2],fields[DATA_DEFINITIONS[2].getColumn()]);
    }
    if (DATA_DEFINITIONS[3] != null && fields.length > DATA_DEFINITIONS[3].getColumn()) {
      dobString = fields[DATA_DEFINITIONS[3].getColumn()];
    }
    if (dobString != null) {
      if (DATA_DEFINITIONS[3].getQualifier() != null && !DATA_DEFINITIONS[0].getQualifier().qualifies(dobString)) {
        throw new ImportException(DATA_DEFINITIONS[0].getQualifier().getClass().getName() + " failed to qualify :" + dobString);
      }
      if (DATA_DEFINITIONS[3].getFormatter()!= null) {
        dtBirth = DATA_DEFINITIONS[3].getFormatter().toDate(dobString);
      } else {
        dtBirth = siteInfo.parseDate(dobString);
      }
      // Convert the birth date from midnight to noon
      dtBirth = DateUtils.getDateNoon(siteInfo, dtBirth);
    }

    if (patient == null) { // create the patient
      patient = new Patient(patientId, firstName, lastName, dtBirth);
      getPatientDao().addPatient(patient);
    } else {
      // see if anything changed
      boolean updatePatient = false;
      if (firstName != null && !(firstName.trim()).equals(patient.getFirstName())) {
        patient.setFirstName(firstName.trim());
        updatePatient = true;
      }
      if (lastName != null && !(lastName.trim()).equals(patient.getLastName())) {
        patient.setLastName(lastName.trim());
        updatePatient = true;
      }
      if (dtBirth != null) {
        if (patient.getDtBirth() == null) {
          patient.setDtBirth(dtBirth);
          updatePatient = true;
        } else {
          String newVal = siteInfo.getDateOnlyFormatter().getDateString(dtBirth);
          String oldVal = siteInfo.getDateOnlyFormatter().getDateString(patient.getDtBirth());
          if (!newVal.equals(oldVal)) {
            patient.setDtBirth(dtBirth);
            updatePatient = true;
          }
        }
      }
      if (updatePatient) {
        getPatientDao().updatePatient(patient);
      }
    }

    return true;

  }

  @Override
  public void importDataEnd() throws Exception {
    // No processing needed on end of import data
  }

  @Override
  public String getDataSource() {
    return "PatientProfile";
  }

  @Override
  public void setResources(ImportResources res) {
    resources = res;
  }

  @Override
  public String[] getDataSourceDependancies() {
    return NO_DEPENDANCIES;
  }

  @Override
  public void addDependancyQueue(ImportDefinitionQueue queue) {
    // do nothing
  }

  public String getStringField(ImportDefinition iDef, String value) throws ParseException, ImportException {

    if (iDef.getQualifier() != null && !iDef.getQualifier().qualifies(value)) {
      throw new ImportException(iDef.getQualifier().getClass().getName() + " failed to qualify :" + value);
    }

    if (iDef.getFormatter() != null) {
      value = iDef.getFormatter().format(value).toString();
    }

    return value;
  }

  public Patient getPatient(String[] fields) throws Exception {
    if (!initialized) {
      init();
    }
    String patientId = getStringField(DATA_DEFINITIONS[0], fields[DATA_DEFINITIONS[0].getColumn()]);
    return getPatientDao().getPatient(patientId);
  }

  public PatientDao getPatientDao() throws Exception {
    if (!initialized) {
      init();
    }
    if (patientDao == null) {
      if (user == null) {
        user = ServerUtils.getAdminUser(database);
      }
      patientDao = new PatientDao(database, siteInfo.getSiteId(), user);
    }
    return patientDao;
  }
}
