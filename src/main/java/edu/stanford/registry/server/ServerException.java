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

package edu.stanford.registry.server;

public class ServerException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private String clientMessage;

  public ServerException() {
    super();
  }

  public ServerException(String serverMessage) {
    super(serverMessage);
  }

  public ServerException(String serverMessage, String clientMessage) {
    super(serverMessage);
    this.clientMessage = clientMessage;
  }

  public ServerException(Throwable cause) {
    super(cause);
  }

  public ServerException(String serverMessage, Throwable cause) {
    super(serverMessage, cause);
  }

  public ServerException(String serverMessage, String clientMessage,
                         Throwable cause) {
    super(serverMessage, cause);
    this.clientMessage = clientMessage;
  }

  public String getDefaultClientMessage() {
    return "The server experienced an internal error";
  }

  public String getClientMessage() {
    if (clientMessage == null) {
      return getDefaultClientMessage();
    }
    return clientMessage;
  }
}
