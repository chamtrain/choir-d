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

public class InvalidCredentials extends Exception implements Serializable {

 
  private static final long serialVersionUID = -5599036132575298836L;
  public final static int TOKEN_NOT_FOUND = 0;
  public final static int SURVEY_IS_COMPLETED = 1;
  public final static int PATIENT_NOT_CONSENTED = 2;
  public final static int TOKEN_NOT_REGISTERED = 3;
  public final static int NO_PATIENT_NAME = 4;
  public final static int SURVEY_EXPIRED = 5;

  public static final String DEFAULT_MESSAGE = "Please enter a valid token";
  public static final String COMPLETE_MESSAGE = "This survey has already been completed.\r\nPlease make sure you are using the link from the most recent email.";
  public static final String EXPIRED_MESSAGE = "This survey is no longer available.\r\nPlease make sure you are using the link from the most recent email.";
  public final static String[] MESSAGE = { DEFAULT_MESSAGE, COMPLETE_MESSAGE,
      "This survey is no longer available, please contact the clinic.",
      "This survey is not ready, please contact the clinic.",
      "The patient information for this survey is incomplete, please contact the clinic.",
      EXPIRED_MESSAGE };

  private int error = 0;
  private String token = "";

  public InvalidCredentials() {
  }

  public InvalidCredentials(String msg) {
    super(msg);
  }

  public InvalidCredentials(String msg, Throwable cause) {
    super(msg, cause);
  }

  public InvalidCredentials(Exception e) {
    super(e);
  }

  public InvalidCredentials(int errorType, String token) {
    this.error = errorType;
    this.token = token;
  }

  public InvalidCredentials(int errorType, String token, Throwable cause) {
    super(MESSAGE[errorType], cause);
    error = errorType;
  }

  @Override
  public String getMessage() {
    return MESSAGE[error];
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
