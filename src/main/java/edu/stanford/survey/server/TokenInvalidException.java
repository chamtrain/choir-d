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

package edu.stanford.survey.server;

/**
 * Indicates the survey token provided to start the survey was not valid.
 */
public class TokenInvalidException extends Exception {
  private static final long serialVersionUID = 1L;

  private String validationMessage;

  public TokenInvalidException(String message) {
    super(message);
    validationMessage = message;
  }

  public String getValidationMessage() {
    return validationMessage;
  }
}
