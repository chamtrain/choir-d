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

package edu.stanford.survey.client.ui;


import com.google.gwt.dom.client.StyleInjector;
import edu.stanford.survey.client.api.SurveyService;
import edu.stanford.survey.client.api.SurveyServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import edu.stanford.survey.server.Survey;

/**
 * Entry point for a client to administer surveys.
 */
public class SurveyApp implements EntryPoint {
  /**
   * This is the entry point method for the application.
   */
  @Override
  public void onModuleLoad() {
    try {
      // Make sure CSS has been provided so error dialogs look ok
      SurveyBundle.INSTANCE.css().ensureInjected();
      StyleInjector.injectAtEnd("@media (max-width:576px) {" + SurveyBundle.INSTANCE.smallScreenCss().getText());

      // Make sure any unhandled exceptions get displayed and hopefully reported back to the server
      GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void onUncaughtException(Throwable e) {
          GWT.log("Unexpected error", e);
          Window.alert("Unexpected error: " + e.getMessage());
          e.printStackTrace();
        }
      });

      // Defer here so the uncaught exception handler will take effect
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          new SurveyUi(createSurveyService()).show();
        }
      });
    } catch (Throwable t) {
      GWT.log("Unexpected error", t);
      Window.alert("Unexpected error: " + t.getMessage());
      t.printStackTrace();
    }
  }

  protected SurveyServiceAsync createSurveyService() {
    SurveyServiceAsync surveyAsync = GWT.create(SurveyService.class);
    ((ServiceDefTarget) surveyAsync).setServiceEntryPoint("../survey/svc/survey2");
    return surveyAsync;
  }
}
