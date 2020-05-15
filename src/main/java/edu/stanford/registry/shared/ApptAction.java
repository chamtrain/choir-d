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

public class ApptAction implements IsSerializable {

  protected String actionType;
  protected boolean actionNeeded;
  protected String label;
  protected  String[] menuDefJson;

  public ApptAction() {
  }

  public ApptAction(String actionType, boolean actionNeeded, String label, String[] menuDefJson) {
    this.actionType = actionType;
    this.actionNeeded = actionNeeded;
    this.label = label;
    this.menuDefJson = menuDefJson;
  }

  public String getActionType() {
    return actionType;
  }

  public void setActionType(String actionType) {
    this.actionType = actionType;
  }

  public boolean isActionNeeded() {
    return actionNeeded;
  }

  public void setActionNeeded(boolean actionNeeded) {
    this.actionNeeded = actionNeeded;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String[] getMenuDefs() {
    return menuDefJson;
  }
}
