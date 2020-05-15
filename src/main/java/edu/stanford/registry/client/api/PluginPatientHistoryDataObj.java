/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

public interface PluginPatientHistoryDataObj {

  Long getHistoryId();

  void setHistoryId(long longOrZero);

  String getChangeType();

  void setChangeType(String type);

  Long getDataId();

  void setDataId(Long dataId);

  String getDataType();

  void setDataType(String dataType);

  Long getCreatedTime();

  void setCreatedTime(Long time);

  String getPatientId();

  void setPatientId(String patientID);

  String getDataVersion();

  void setDataVersion(String version);

  String getDataValue();

  void setDataValue(String data);

}
