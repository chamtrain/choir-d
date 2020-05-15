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
package edu.stanford.registry.shared.api;

/**
 * API Patient Attribute definition
 *
 * @author tpacht
 */

public interface SiteObj {


  Long getSiteId();

  void setSiteId(Long siteId);

  String getUrlParam();

  void setUrlParam(String Enabled);

  String getDisplayName();

  void setDisplayName(String DisplayName);

  String getEnabled();

  void setEnabled(String Enabled);

}
