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

import java.util.Date;

public interface DataTable {
  /**
   * Objects that implement this will be able to be displayed and updated with
   * the dataTable object
   * <p/>
   * public String getTableName();
   * <p/>
   * /** The returns an array of headers for all data fields
   *
   * @return String[] descriptive headers for all elements
   */
  String[] getAllHeaders();

  /**
   * This returns an array indicating if fields can be modified 1 if yes, the
   * field can be modified and 0 if not (for things like date created).
   *
   * @return
   */
  int[] getChangeIndicators();

  /**
   * Returns the data formatted to strings
   *
   * @return
   */
  String[] getData(DateUtilsIntf utils);

  /**
   * Sets new values. The data that can't be changed is ignored.
   *
   * @param values
   * @throws InvalidDataElementException
   */
  void setData(String[] values) throws InvalidDataElementException;

  Integer getMetaVersion();

  void setMetaVersion(Integer vs);

  Date getDtCreated();

  void setDtCreated(Date created);

  Date getDtChanged();

  void setDtChanged(Date changed);

}
