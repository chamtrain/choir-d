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

package edu.stanford.registry.server.survey;

import edu.stanford.survey.client.api.DisplayStatus;

import java.io.Serializable;

import com.google.web.bindery.autobean.shared.AutoBean;

/**
 * Simple bean for combining status and question information to be sent to the survey client.
 */
public class NextQuestion implements Serializable {
  private static final long serialVersionUID = 1L;

  private DisplayStatus displayStatus;
  private AutoBean<?> question;

  public DisplayStatus getDisplayStatus() {
    return displayStatus;
  }

  public void setDisplayStatus(DisplayStatus displayStatus) {
    this.displayStatus = displayStatus;
  }

  public AutoBean<?> getQuestion() {
    return question;
  }

  public void setQuestion(AutoBean<?> question) {
    this.question = question;
  }


//  private String displayStatusJson;
//  private String questionJson;
//
//  public String getDisplayStatusJson() {
//    return displayStatusJson;
//  }
//
//  public void setDisplayStatusJson(String displayStatusJson) {
//    this.displayStatusJson = displayStatusJson;
//  }
//
//  public String getQuestionJson() {
//    return questionJson;
//  }
//
//  public void setQuestionJson(String questionJson) {
//    this.questionJson = questionJson;
//  }
}
