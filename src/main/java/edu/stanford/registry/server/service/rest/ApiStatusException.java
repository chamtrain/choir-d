/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.service.rest;

import org.restlet.data.Status;

/**
 * For API exceptions
 *
 * @author tpacht
 */
public class ApiStatusException extends Exception {
  private static final long serialVersionUID = 1L;

  private Status returnStatus = Status.SERVER_ERROR_INTERNAL;
  private String apiRequestPath;
  private String returnMessage = "call failed on server";

  public ApiStatusException(Status status, String requestPath) {
    setStatus(status);
    setRequestPath(requestPath);
  }

  public ApiStatusException(Status status, String requestPath, String message) {
    this(status, requestPath);
    setMessage(message);
  }

  private void setStatus(Status status) {
    this.returnStatus = status;
  }
  public Status getStatus() {
    return returnStatus;
  }

  public void setRequestPath(String status) {
    this.apiRequestPath = status;
  }
  String getRequestPath() {
    return apiRequestPath;
  }
  private void setMessage(String message) {
    returnMessage = message;
  }
  @Override
  public String getMessage() {
    return returnMessage;
  }
}
