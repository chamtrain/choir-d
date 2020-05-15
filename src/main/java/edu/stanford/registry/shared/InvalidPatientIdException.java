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

// I don't know why, but GWT threw an exception for a bad MRN until I added IsSerializable
public class InvalidPatientIdException extends RuntimeException implements Serializable, IsSerializable {

  private boolean formatError = false;
  private String formattedString = null;

  public InvalidPatientIdException() {
  }

  public InvalidPatientIdException(String msg) {
    super(msg);
  }

  public InvalidPatientIdException(String msg, boolean isFormatError) {
    super(msg);
    setFormatError(isFormatError);
  }

  public InvalidPatientIdException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public InvalidPatientIdException(String msg, boolean isFormatError, Throwable cause) {
    super(msg, cause);
    setFormatError(isFormatError);
  }

  public InvalidPatientIdException(String msg, String patientId) {
    super(msg);
    setFormatError(false);
    setFormattedString(patientId);
  }

  /**
   * Default
   */
  private static final long serialVersionUID = 1L;

  public void setFormatError(boolean isFormatError) {
    formatError = isFormatError;
  }

  public boolean isFormatError() {
    return formatError;
  }

  public void setFormattedString(String str) {
    formattedString = str;
  }

  public String getFormattedString() {
    return formattedString;
  }
}
