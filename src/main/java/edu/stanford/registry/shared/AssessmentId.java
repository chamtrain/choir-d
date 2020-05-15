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

public class AssessmentId implements IsSerializable {

  private Long assessmentId = null;

  public AssessmentId() {
  }

  public AssessmentId(Long assessmentId) {
    this.assessmentId = assessmentId;
  }

  public Long getId() {
    return assessmentId;
  }

  public void setId(Long assessmentId) {
    this.assessmentId = assessmentId;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj != null) && (obj instanceof AssessmentId) && assessmentId.equals(((AssessmentId)obj).getId());
  }

  @Override
  public int hashCode() {
    return assessmentId.hashCode();
  }

  @Override
  public String toString() {
    return (assessmentId == null) ? "null" : assessmentId.toString();
  }
}
