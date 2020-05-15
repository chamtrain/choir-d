/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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


package edu.stanford.registry.server.service;

import com.github.susom.database.Database;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.customassessment.AssignAssessmentConfigHandler;
import edu.stanford.registry.server.config.customassessment.CustomPatientAssessmentConfigUtil;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.shared.AssignedPatientAssessment;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.PatientAssignedAssessmentEntry;
import edu.stanford.registry.shared.PatientExtendedAttribute;
import edu.stanford.registry.shared.User;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurePatientAssessmentServicesImpl extends AssessmentConfigServicesImpl implements
    ConfigurePatientAssessmentServices {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurePatientAssessmentServicesImpl.class);

  public ConfigurePatientAssessmentServicesImpl(User user, Supplier<Database> dbp, ServerContext context,
      SiteInfo siteInfo) {
    super(user, dbp, context, siteInfo);
  }

  @Override
  public AssignedPatientAssessment updatePatientAssignedAssessments(AssignedPatientAssessment assigned) {
    try {
      if (assigned != null) {
        CustomPatientAssessmentConfigUtil cConfig = getCurrentConfig(assigned.getPatientId());
        String j = cConfig.getJson();
        AssignAssessmentConfigHandler handler = new AssignAssessmentConfigHandler(
            assigned.getSiteId(),
            assigned.getPatientId(),
            cConfig);
        handler.addOrUpdate(assigned);
        String json = handler.getConfigJson();

        return updatePatientAssignedAssessmentJSON(json, assigned);
      }
    } catch (Exception e) {
      logger.error("Error occurred when updating assignment " + e.toString(), e);
    }
    return null;
  }

  @Override
  public PatientAssignedAssessmentEntry getLowFrequencyByInstrumentName(String patientId, Long siteId,
      String instrument) {
    List<AssignedPatientAssessment> list = getAllAssignment(patientId, siteId);
    PatientAssignedAssessmentEntry low = null;
    Integer min = null;
    if (list != null && list.size() > 0) {
      for (AssignedPatientAssessment a : list) {
        Integer cur = a.getFrequency(instrument);
        if (cur != null) {
          if (min == null) {
            min = cur;
            low = new PatientAssignedAssessmentEntry(a.getPatientId(), cur, a.getClinicName());
          } else {
            if (min > cur) {
              min = cur;
              low = new PatientAssignedAssessmentEntry(a.getPatientId(), cur, a.getClinicName());
            }
          }
        }
      }
    }
    return low;
  }

  private AssignedPatientAssessment updatePatientAssignedAssessmentJSON(String json,
      AssignedPatientAssessment assignedPatientAssessment) {
    if (json == null || json.isEmpty()) {
      logger.error(
          "Could not save assessment for patient :" + assignedPatientAssessment.getPatientId());
      return null;
    }

    PatientDao patientDao = new PatientDao(dbp.get(), siteInfo.getSiteId(), user);
    PatientExtendedAttribute attrib = patientDao.getExtendedAttribute(assignedPatientAssessment.getPatientId(),
        Constants.ASSIGNED_ASSESSMENT_DATA_NAME);
    if (attrib == null) {
      attrib = new PatientExtendedAttribute(assignedPatientAssessment.getPatientId(),
          Constants.ASSIGNED_ASSESSMENT_DATA_NAME, json);
    } else {
      attrib.setDataValue(json);
    }

    PatientExtendedAttribute result = patientDao.insertExtendedAttribute(attrib);
    if (result != null) {
      return assignedPatientAssessment;
    } else {
      logger.error(
          "Could not save assessment for patient :" + assignedPatientAssessment.getPatientId());
      return null;
    }
  }

  @Override
  public AssignedPatientAssessment getAssignmentByClinic(String patientId, Long siteId, String clinicName) {
    try {
      List<AssignedPatientAssessment> list = getAllAssignment(patientId, siteId);
      if (list == null) {
        logger.info("No assigned assessments configured for :" + patientId);
        return null;
      }

      for (AssignedPatientAssessment a : list) {
        if (StringUtils.equalsIgnoreCase(a.getClinicName(), clinicName)) {
          return a;
        }
      }
    } catch (Exception e) {
      logger.error("Error occurred getting patient assigned assessments for the current clinic " + e.toString(), e);
    }
    return null;
  }

  @Override
  public List<AssignedPatientAssessment> getAllAssignment(String patientId, Long siteId) {
    try {
      PatientDao patientDao = new PatientDao(dbp.get(), siteId, user);
      PatientExtendedAttribute attribute = patientDao
          .getExtendedAttribute(patientId, Constants.ASSIGNED_ASSESSMENT_DATA_NAME);
      if (attribute == null) {
        logger.info("No assigned assessments configured for :" + patientId);
        return Collections.emptyList();
      }

      CustomPatientAssessmentConfigUtil cConfig = new CustomPatientAssessmentConfigUtil(attribute.getDataValue());
      AssignAssessmentConfigHandler handler = new AssignAssessmentConfigHandler(siteId, patientId, cConfig);
      return handler.getAllAssignment();
    } catch (Exception e) {
      logger.error("Error occurred when getting all assigned assignments" + e.toString(), e);
    }
    return null;
  }

  private CustomPatientAssessmentConfigUtil getCurrentConfig(String patientId) {
    CustomPatientAssessmentConfigUtil cConfig;
    PatientDao patientDao = new PatientDao(dbp.get(), siteInfo.getSiteId(), user);
    PatientExtendedAttribute attrib = patientDao
        .getExtendedAttribute(patientId, Constants.ASSIGNED_ASSESSMENT_DATA_NAME);
    if (attrib != null) {
      cConfig = new CustomPatientAssessmentConfigUtil(attrib.getDataValue());
    } else {
      cConfig = new CustomPatientAssessmentConfigUtil("");
    }
    return cConfig;
  }
}
