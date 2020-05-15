/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.shc.pain.PhysicalTherapyCompletionHandler;
import edu.stanford.registry.server.shc.totaljoint.TotalJointCompletionHandler;
import edu.stanford.registry.server.shc.trauma.TraumaCompletionHandler;
import edu.stanford.survey.server.SurveyCompleteHandler;
import edu.stanford.survey.server.SurveyCompleteHandlerFactory;

/**
 * Default class for the site property factory.survey.complete.
 * 
 * When batch processing is enabled for the server, either a specified handler or this
 * one will be run on surveys for the site that have been completed.  This will be passed
 * the recipientName of each recipient that wants to hear about a survey, and returns a
 * handler for it. Those handlers will be called for each completed survey. 
 */
public class SurveyCompleteHandlerFactoryImpl implements SurveyCompleteHandlerFactory {

  private final SiteInfo siteInfo;

  /**
   * Note: If a custom factory class is specified in the site property factory.survey.complete,
   * it can have one, both or neither of these parameters.
   *
   * @param serverContext Passed in for future use, if anything has a need beyond siteInfo
   * @param siteInfo Identifies the site this factory is set up for.
   */
  public SurveyCompleteHandlerFactoryImpl(ServerContext serverContext, SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  /**
   * Find a class that wants to be notified when surveys complete.
   *
   * @param recipientName name for this handler (see survey_complete_push.recipient_name)
   * @return the corresponding system, or null if none could be found
   */
  @Override
  public SurveyCompleteHandler handlerForName(String recipientName, Long siteId) {
    if (recipientName.equals("pdf")) {
      return new SurveyCompleteHandlerPdf(siteInfo);
    }
    if (recipientName.equals("squareTable")) {
      return new SurveyCompleteHandlerSquareTable(siteInfo);
    }
    if (recipientName.equals("tjComplete")) {
      return new TotalJointCompletionHandler(siteInfo);
    }
    if (recipientName.equals("traumaComplete")) {
      return new TraumaCompletionHandler(siteInfo);
    }
    if (recipientName.equals("physicalTherapyComplete")) {
      return new PhysicalTherapyCompletionHandler(siteInfo);
    }
    return null;
  }
}
