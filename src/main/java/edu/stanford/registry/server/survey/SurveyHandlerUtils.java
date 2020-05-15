/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.survey;

import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.SurveyRegistration;

/**
 * Utility class to access the ProcessType info for a survey, patient and attributes.
 */
public class SurveyHandlerUtils {
  private static final Logger logger = Logger.getLogger(SurveyHandlerUtils.class);

  final Supplier<Database> dbp;
  private final SiteInfo siteInfo;
  private final Long tokenId;

  private final SurveyRegistration surveyReg;
  final String processName; // the processType value


  public SurveyHandlerUtils(Supplier<Database> dbp, SiteInfo siteInfo, Long surveyTokenId) {
    this.dbp = dbp;
    this.siteInfo = siteInfo;
    tokenId = surveyTokenId;
    surveyReg = getSurveyRegistration(); // logs errors as needed
    processName = (surveyReg != null) ? surveyReg.getSurveyType() : "";
  }


  // See examples of how to get the actual json responses...
  //   need the provider/Local, section (study name), fieldId question#, response#, ref-tag


  /**
   * Returns the value of the ProcessType element's attribute that has the passed name, or ""
   */
  public String fetchProcessTypeAttributeValue(String attributeName) {
    if (surveyReg == null) {
      return "";
    }
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    String attrValue = xmlUtils.getAttribute(processName, attributeName);
    return attrValue == null ? "" : attrValue;
  }


  /**
   * Once we know the attribute name from the ProcessType, eg: ... tset="tset:attrName",
   * this can fetch the attribute, to see what the SetAttributeValue element set it to.
   */
  public PatientAttribute getPatientAttribute(String attrName) {
    String patientId = surveyReg.getPatientId();
    PatientDao patientDao = new PatientDao(dbp.get(), siteInfo.getSiteId());
    PatientAttribute attr = patientDao.getAttribute(patientId, attrName);
    return attr;
  }


  /**
   * After fetching the attribute value, we can process it, and then use this method to update it.
   */
  public void setPatientAttribute(PatientAttribute attr, String attrValue) {
    attr.setDataValue(attrValue);
    PatientDao patientDao = new PatientDao(dbp.get(), siteInfo.getSiteId(), ServerUtils.getAdminUser(dbp.get()));
    patientDao.insertAttribute(attr);
  }


  private SurveyRegistration getSurveyRegistration() {
    AssessDao assessDao = new AssessDao(dbp.get(), siteInfo);
    Long surveyRegId = assessDao.getSurveyRegIdByTokenId(tokenId);
    if (surveyRegId == null) {
      logger.error(siteInfo.getIdString()+"surveyRegId not found for token_id " + tokenId);
      return null;
    }

    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      logger.error(siteInfo.getSiteId()+"no surveyReg found for surveyRegId " + surveyRegId);
    }
    return registration;
  }


  public Long getTokenId() {  // NUKE??
    return tokenId;
  }

  public String getPatientId() {
    return surveyReg.getPatientId();
  }

  public Supplier<Database> getDbp() {  // NUKE??
    return dbp;
  }
}
