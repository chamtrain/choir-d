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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyComplete;
import edu.stanford.survey.server.SurveyCompleteHandler;

/**
 * A treatment-set survey has on its ProcessType, an attribute: <code>tset:name</code>
 * <br>where <b>name</b> is the name of the RandomSet for the site.
 * The name begins with "tset:" so the UI can easily find such attributes in phase 1,
 * plus it makes them easy to find in the process.xml and <survey>.xml files with grep.
 *
 * When the patient gives or denies consent, an attribute with this name
 * is set to "didConsent" or "didDecline" or a comment, beginning with a dash.
 *
 * This handler will find that value and either assign a group or add it to
 * the "declined" group, and set this attribute value to the group name or "declined".
 *
 * The survey may also have other answers.  All answers should set the attribute value,
 * in case the user selects one answer and then another.  An  and set the attribute value to
 */
public class SurveyHandlerTreatmentSet implements SurveyAdvanceHandler, SurveyCompleteHandler{
  private static final Logger logger = LoggerFactory.getLogger(SurveyHandlerTreatmentSet.class);

  private static final String TSET_PREFIX = "tset:";
  private static final String DID_CONSENT = "didConsent";
  private static final String DID_DECLINE = "didDecline";
  private static final String TSET_PROCESS_ATTR = "tset";

  private final SiteInfo siteInfo;
  private final User admin;

  public SurveyHandlerTreatmentSet(SiteInfo siteInfo, User admin) {
    this.siteInfo = siteInfo;
    this.admin = admin;
  }


  @Override
  public boolean surveyAdvanced(SurveyAdvance survey, Supplier<Database> database) {
    return handle(database, survey.getSurveyTokenId());
  }


  @Override
  public boolean surveyCompleted(SurveyComplete survey, Supplier<Database> database) {
    return handle(database, survey.getSurveyTokenId());
  }


  private boolean handle(Supplier<Database> dbp, Long surveyTokenId) {
    SurveyHandlerUtils surveyInfo = new SurveyHandlerUtils(dbp, siteInfo, surveyTokenId);

    String attrName = surveyInfo.fetchProcessTypeAttributeValue(TSET_PROCESS_ATTR);
    if (attrName.isEmpty()) {  // This ProcessType (survey type) has no TreatmentSet question
      return maybeWarnIfNoTset(surveyInfo.processName);
    }

    PatientAttribute attr = surveyInfo.getPatientAttribute(attrName);
    if (attr == null) {
      logger.debug("TreatmentSet attribute isn't set yet: "+attrName);
      return false;
    }

    String tsetName = getNameFromAttributeString(attrName);
    String value = attr.getDataValue();
    if (!DID_CONSENT.equals(value) && !DID_DECLINE.equals(value)) {
      logger.debug(String.format("TreatmentSet '%s' already has a value: %s", tsetName, value));
      return false;  // already set or is a -comment
    }


    RandomSetParticipant rsp = getGroupAssignment(surveyInfo, tsetName, DID_CONSENT.equals(value));
    String newValue = rsp.getValue();
    surveyInfo.setPatientAttribute(attr, newValue);
    String msg = "TreatmentSet '%s'- attr had value %s, new: %s";
    logger.debug(String.format(msg, tsetName, value, newValue));
    return true;
  }


  private String getNameFromAttributeString(String attrName) {
    if (attrName.startsWith(TSET_PREFIX)) {
      return attrName.substring(TSET_PREFIX.length());
    }
    String tsetName = attrName;
    String plus = "";
    int ix = attrName.indexOf(':');
    if (ix > 0) {
      plus = " (stripping the initial '"+attrName.substring(0,ix+1)+"')";
      tsetName = attrName.substring(ix+1);
    }
    logger.error(String.format("The TreatmentSet attr %s='\"%s\" did not start with the prefix: %s%s",
                               TSET_PROCESS_ATTR, attrName, TSET_PREFIX, plus));
    return tsetName;
  }


  private boolean maybeWarnIfNoTset(String process) {
    if (process.toLowerCase().contains("tset")) { // expect any process with tset in its name to have the attr...
      logger.warn("Survey/process "+process+" surprisingly has no attribute: "+TSET_PROCESS_ATTR+"=...");
    }
    return false;
  }


  private RandomSetParticipant getGroupAssignment(SurveyHandlerUtils utils, String tsetName, boolean consented) {
    RandomSetter set = siteInfo.getRandomSet(tsetName);
    if (set == null) {
      logger.error(siteInfo.getIdString()+"RandomSet not found: "+tsetName);
      return null;
    }
    RandomSetParticipant rsp = new RandomSetParticipant(utils.getPatientId(), set.getRandomSet(),
        consented ? RandomSetParticipant.State.Assigned : RandomSetParticipant.State.Declined);
    return set.updateParticipant(siteInfo, utils.dbp, admin, rsp);
  }
}
