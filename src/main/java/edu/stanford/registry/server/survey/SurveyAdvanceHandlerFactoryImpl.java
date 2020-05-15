/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.shc.gi.GIAdvanceHandler;
import edu.stanford.registry.server.shc.interventionalradiology.IRAdvanceHandler;
import edu.stanford.registry.server.shc.orthohand.OrthoHandAdvanceHandler;
import edu.stanford.registry.server.shc.pedpain.PedPainAdvanceHandler;
import edu.stanford.registry.server.shc.preanesthesia.PreAnesthesiaAdvanceHandler;
import edu.stanford.registry.server.shc.trauma.SurveyAdvanceHandlerTrauma;
import edu.stanford.registry.server.shc.trauma.TraumaAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceHandlerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for hiding the lookup of handlers that will do things upon survey
 * advancement.
 *
 * This is created for a single site and creates a new handler for each survey.
 */
public class SurveyAdvanceHandlerFactoryImpl implements SurveyAdvanceHandlerFactory {

  private final SiteInfo siteInfo;
  private static final Logger log = LoggerFactory.getLogger(SurveyAdvanceHandlerFactoryImpl.class);
  /* This is used by the batch process
   */
  public SurveyAdvanceHandlerFactoryImpl(ServerContext serverContext, SiteInfo siteInfo) {
    this(siteInfo);
  }

  /* This is used by the main methods of some of the square-table creators
   */
  public SurveyAdvanceHandlerFactoryImpl(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  /**
   * Find a class that wants to be notified when surveys advance.
   *
   * @param recipientName name for this handler (see survey_advance_push.recipient_name)
   * @return the corresponding system, or null if none could be found
   */
  @Override
  public SurveyAdvanceHandler handlerForName(String recipientName) {

    if (recipientName.equals("squareTable")) {
      return new SurveyAdvanceHandlerSquareTable(siteInfo);
    }
    if (recipientName.equals("giSquareTable")) {
      return new GIAdvanceHandler(siteInfo);
    }
    if (recipientName.equals("cmSquareTable")) {
      return new SurveyAdvanceHandlerChronicMigraineSquareTable(siteInfo);
    }
    if (recipientName.equals("txSquareTable")) {
      return new SurveyAdvanceHandlerTreatmentHxSquareTable(siteInfo);
    }
    if (recipientName.equals("headacheSquareTable")) {
      return new SurveyAdvanceHandlerHeadacheSquareTable(siteInfo);
    }
    if (recipientName.startsWith("generateFromXml[")) {
       try {
        return new SurveyAdvanceHandlerGenXmlSquareTable(getAppConfigId(recipientName), siteInfo);
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    if (recipientName.startsWith("generatePsychFromXml[")) {
      try {
        return new SurveyAdvanceHandlerPsych(getAppConfigId(recipientName), siteInfo);
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    if (recipientName.startsWith("generateCOPCSFromXml[")) {
      try {
        return new SurveyAdvanceHandlerCOPCS(getAppConfigId(recipientName), siteInfo);
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    if (recipientName.startsWith("generateGISQFromXml[")) {
      try {
        Long appConfigId = Long.valueOf(recipientName.substring(
            recipientName.indexOf("[") + 1, recipientName.indexOf("]")));
        return new SurveyAdvanceHandlerGISQ(appConfigId, siteInfo);
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }

    if (recipientName.equals("pacSquareTable")) {
      return new PreAnesthesiaAdvanceHandler(siteInfo);
    }
    if (recipientName.equals("pacStdSquareTable")) {
      return new PreAnesthesiaAdvanceHandler(siteInfo);
    }
    if (recipientName.equals("IRAdvanceHandler")) {
      return new IRAdvanceHandler(siteInfo);
    }
    if (recipientName.startsWith("generateTraumaFromXml[")) {
      try {
        return new SurveyAdvanceHandlerTrauma(getAppConfigId(recipientName), siteInfo);
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    if (recipientName.equals("TraumaAdvanceHandler")) {
      return new TraumaAdvanceHandler(siteInfo);
    }
    if (recipientName.equals("handSquareTable")) {
      return new OrthoHandAdvanceHandler(siteInfo);
    }
    if (recipientName.equals("PedPainAdvanceHandler")) {
      return new PedPainAdvanceHandler(siteInfo);
    }
    if (recipientName.startsWith("genFromCustomServiceXml[")) {
      try {
        log.debug("Returning {} for {} ", "SurveyAdvanceHandlerCustomService", recipientName);
        return new SurveyAdvanceHandlerCustomService(getAppConfigId(recipientName), siteInfo);
      } catch (NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    return null;
  }
  private Long getAppConfigId(String recipientName) {
    return Long.parseLong(recipientName.substring(recipientName.indexOf("[") + 1, recipientName.indexOf("]")));
  }
}
