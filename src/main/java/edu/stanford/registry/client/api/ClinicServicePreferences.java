/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

/**
 * Created by tpacht on 11/24/2015.
 */
public interface ClinicServicePreferences {
  @PropertyName(value = "ProviderFilter")
  FilteredProviders getProviderFilter();

  @PropertyName(value = "ProviderFilter")
  void setProviderFilter(FilteredProviders filteredProviders);

  @PropertyName(value = "ClinicFilter")
  String getClinicFilter();

  @PropertyName(value = "ClinicFilter")
  void setClinicFilter(String filteredClinics);

  @PropertyName(value = "ScheduleSort")
  String getSchedSortColumn();

  @PropertyName(value = "ScheduleSort")
  void setSchedSortColumn(String column);

  @PropertyName(value = "ScheduleSortAsc")
  Boolean getSchedSortAsc();

  @PropertyName(value = "ScheduleSortAsc")
  void setSchedSortAsc(Boolean ascending);

}
