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

package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ApptId implements IsSerializable {

  private Long apptId = null;

  public ApptId() {
  }

  public ApptId(Long apptId) {
    this.apptId = apptId;
  }

  public Long getId() {
    return apptId;
  }

  public void setId(Long appId) {
    this.apptId = appId;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj != null) && (obj instanceof ApptId) && apptId.equals(((ApptId)obj).getId());
  }

  @Override
  public int hashCode() {
    return apptId.hashCode();
  }

  @Override
  public String toString() {
    return (apptId == null) ? "null" : apptId.toString();
  }
}
