/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.shared.survey;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveyException extends Exception implements IsSerializable {
  private static final long serialVersionUID = 1L;

  public SurveyException() {
    super();
  }
  public SurveyException(String msg) {
    super(msg);
  }

  public SurveyException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public SurveyException(Exception e) {
    super(e);
  }
}
