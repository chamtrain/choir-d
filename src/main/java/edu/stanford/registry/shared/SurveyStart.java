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

/**
 * This represents an identified system user.
 *
 * @author tpacht
 */
public class SurveyStart implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_START_SURVEY = 0;
  public static final int LEVEL_GOOD = 0;
  public static final int LEVEL_WARN = 1;
  public static final int LEVEL_ERROR = 2;

  // Good
  public static final int STATUS_OK = 0;

  // Warn
  public static final int STATUS_TOO_SOON = 10;
  public static final int STATUS_NO_SURVEYS = 11;

  // Error
  public static final int STATUS_NO_CONSENT = 20;

  private int level = 0;

  private String message;
  private Integer status;

  public String getMessage() {
    return message;
  }

  public void setMessage(String msg) {
    message = msg;
  }

  public Integer getStatus() {
    return status;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setStatus(Integer stat) {
    status = stat;
  }
}
