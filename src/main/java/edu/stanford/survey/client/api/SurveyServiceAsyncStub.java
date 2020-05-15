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

package edu.stanford.survey.client.api;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous wrapper for the stubbed test survey.
 */
public class SurveyServiceAsyncStub implements SurveyServiceAsync {
  private final SurveyService stub;

  public SurveyServiceAsyncStub(SurveyFactory factory) {
    stub = new SurveyServiceStub(factory);
  }

  @Override
  public void startSurvey(final String systemId, final String surveyToken, final AsyncCallback<String[]> async) {
    try {
      new Timer() {
        @Override
        public void run() {
          async.onSuccess(stub.startSurvey(systemId, surveyToken));
        }
      }.schedule(1000);
    } catch (Exception e) {
      async.onFailure(e);
    }
  }

  @Override
  public void continueSurvey(final String statusJson, final String answerJson, final AsyncCallback<String[]> async) {
    try {
      new Timer() {
        @Override
        public void run() {
          async.onSuccess(stub.continueSurvey(statusJson, answerJson));
        }
      }.schedule(1000);
    } catch (Exception e) {
      async.onFailure(e);
    }
  }

  @Override
  public void resumeSurvey(final String resumeToken, final AsyncCallback<String[]> async) {
    try {
      new Timer() {
        @Override
        public void run() {
          async.onSuccess(stub.resumeSurvey(resumeToken));
        }
      }.schedule(1000);
    } catch (Exception e) {
      async.onFailure(e);
    }
  }

  @Override
  public void addPlayerProgress(final String statusJson, final String targetId, final String action, final Long milliseconds,
      final AsyncCallback<Void> async) {
    try {
      new Timer() {
        @Override
        public void run() {
          stub.addPlayerProgress(statusJson, targetId, action, milliseconds);
          async.onSuccess(null);
        }
      }.schedule(1000);
    } catch (Exception e) {
      async.onFailure(e);
    }
    
  }

  @Override
  public void getSurveySites(final AsyncCallback<String[]> async) {
    try {
      new Timer() {
        @Override
        public void run() {
          stub.getSurveySites();
          async.onSuccess(null);
        }
      }.schedule(1000);
    } catch (Exception e) {
      async.onFailure(e);
    }
  }

}
