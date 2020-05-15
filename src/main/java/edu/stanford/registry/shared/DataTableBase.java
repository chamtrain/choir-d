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

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DataTableBase implements IsSerializable, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -758583431339409525L;
  private Integer metaVersion;
  private Date dtCreated;
  private Date dtChanged;

  /**
   * Get the meta data version.
   *
   * @return meta version
   */
  public Integer getMetaVersion() {
    if (metaVersion == null) {
      metaVersion = 0;
    }
    return metaVersion;
  }

  /**
   * Sets the meta data version.
   *
   * @param vs
   */
  public void setMetaVersion(Integer vs) {
    metaVersion = vs;
  }

  /**
   * Get the date and time this record was created.
   *
   * @return date/time created
   */
  public Date getDtCreated() {
    return dtCreated;
  }

  /**
   * Sets the created date and time.
   *
   * @param created
   */
  public void setDtCreated(Date created) {
    dtCreated = created;
  }

  /**
   * Get the date and time this record was last modified.
   *
   * @return
   */
  public Date getDtChanged() {
    return dtChanged;
  }

  /**
   * Set the last modified date and time.
   *
   * @param changed
   */
  public void setDtChanged(Date changed) {
    dtChanged = changed;
  }

  public Date getNow() {
    return new java.util.Date();
  }

}
