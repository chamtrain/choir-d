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
package edu.stanford.registry.client.api;

import java.util.List;
/**
 * API Patient definition
 *
 * @author tpacht
 */
@SuppressWarnings("unused") // to be used in the next version of the api
public interface PatientObj {

  String getPatientId();

  void setPatientId(String patientID);

  String getFirstName();

  void setFirstName(String firstName);

  String getLastName();

  void setLastName(String lastName);

  String getDtBirth();

  void setDtBirth(String dob);

  String getDtCreated();

  void setDtCreated(String dtCreated);

  String getDtChanged();

  void setDtChanged(String dtChanged);

  List<PatientAttributeObj> getAttributes();

  void setAttributes(List<PatientAttributeObj> attributes);

}
