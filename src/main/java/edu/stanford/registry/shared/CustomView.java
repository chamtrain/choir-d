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
package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * For adding custom html pages into CHOIR.
 *
 * @author tpacht
 */
public class CustomView implements IsSerializable {
  public static final String PREFIX = "CUSTOMVIEW_";
  private String viewName;
  private String title;

  CustomView() {
  }

  CustomView(String roleName, String title, String siteName) {

    setRoleName(roleName, siteName);
    setTitle(title);
  }


  public void setRoleName(String roleName, String siteName) {
    if (roleName == null || siteName == null) {
      return;
    }
    this.viewName = PREFIX + roleName.toUpperCase() + "[" + siteName + "]";
  }

  public String getAuthorityName() {
    return viewName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}