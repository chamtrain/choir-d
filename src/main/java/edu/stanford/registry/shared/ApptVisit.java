/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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

/**
 * @author Teresa Pacht <tpacht@stanford.edu>
 * @since 10/08/2019
 */
public class ApptVisit implements IsSerializable {

  private Long apptVisitId;
  private String visitType;
  private String visitDescription;
  private long visitEid;

  public ApptVisit() {
  }

  public Long getApptVisitId() {
    return apptVisitId;
  }

  public void setApptVisitId(Long id) {
    apptVisitId = id;
  }

  public String getVisitType() {
    return visitType;
  }

  public void setVisitType(String type) {
    visitType = type;
  }

  public String getVisitDescription() {
    return visitDescription;
  }

  public void setVisitDescription(String description) {
    visitDescription = description;
  }

  public Long getVisitEId() {
    return visitEid;
  }

  public void setVisitEId(Long id) {
    visitEid = id;
  }

}
