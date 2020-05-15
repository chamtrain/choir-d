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

package edu.stanford.registry.shared.xform;

import edu.stanford.registry.shared.survey.Contingency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectItem implements Serializable {
  private static final long serialVersionUID = -5642046383453300605L;
  String label, value, group, width, height;
  boolean isSelected = false;

  // HashMap<String, ConditionalChanges> conditions = new HashMap<String,
  // ConditionalChanges>();
  HashMap<String, ArrayList<Contingency>> contingencies = new HashMap<>();

  public SelectItem() {

  }

  public SelectItem(String lbl) {
    setLabel(lbl);
  }

  public void setLabel(String lbl) {
    label = lbl;
  }

  public String getLabel() {
    return label;
  }

  public void setValue(String val) {
    value = val;
  }

  public String getValue() {
    return value;
  }

  public void setGroup(String grp) {
    group = grp;
  }

  public String getGroup() {
    return group;
  }

  public void setSelected(boolean value) {
    isSelected = value;
  }

  public boolean getSelected() {
    return isSelected;
  }

  public void addContingency(String action, Contingency condition) {
    ArrayList<Contingency> conditions = contingencies.get(action);
    if (conditions == null) {
      conditions = new ArrayList<>();
    }
    conditions.add(condition);
    contingencies.put(action, conditions);
  }

  public ArrayList<Contingency> getContingency(String action) {
    return contingencies.get(action);
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getWidth() {
    return width;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getHeight() {
    return height;
  }
}
