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
package edu.stanford.registry.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * For adding custom html pages into CHOIR.
 *
 * @author tpacht
 */
public  class CustomTab implements IsSerializable  {
  private String path;
  private String title;
  private String[] authority;
  CustomTab() {
  }

  CustomTab(String[] authority, String path, String title) {
    setAuthority(authority);
    setPath(path);
    setTitle(title);
  }

  public void setAuthority(String[] authority) {
    this.authority = authority;
  }

  public String[] getAuthority() {
    return authority;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }
}