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

package edu.stanford.registry.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveyProgressLevel implements IsSerializable, Serializable {
  private static final long serialVersionUID = 140531612207980479L;
  private String token;
  private int numberOfStudies;
  private int numberCompleted;

  public SurveyProgressLevel() {
    token = "";
    numberOfStudies = 0;
    numberCompleted = 0;
  }

  public SurveyProgressLevel(String token, int numberOfStudies, int numberCompleted) {
    this.token = token;
    this.numberOfStudies = numberOfStudies;
    this.numberCompleted = numberCompleted;
  }

  public void setToken(String tok) {
    token = tok;
  }

  public String getToken() {
    return token;
  }

  public void setNumberOfStudies(int num) {
    numberOfStudies = num;
  }

  public int getNumberOfStudies() {
    return numberOfStudies;
  }

  public void setNumberCompleted(int num) {
    numberCompleted = num;
  }

  public int getNumberCompleted() {
    return numberCompleted;
  }
}
