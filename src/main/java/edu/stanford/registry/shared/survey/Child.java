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

package edu.stanford.registry.shared.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Child implements Serializable {
  private static final long serialVersionUID = 1L;

  String childName = null;
  HashMap<String, String> attributes = new HashMap<>();
  ArrayList<Child> children = null;
  boolean selected = false;

  public Child() {

  }

  public Child(String name) {
    childName = name;
  }

  public Child(String name, HashMap<String, String> attributes) {
    this.childName = name;
    this.attributes = attributes;
  }

  public int getNumberAttributes() {

    return attributes.size();
  }

  public String getName() {
    return childName;
  }

  public void addChild(Child child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
  }

  public int getNumberChildren() {
    if (children == null) {
      return 0;
    }
    return children.size();

  }

  public ArrayList<Child> getChildren() {
    return children;
  }

  public boolean hasChildren() {
    return children != null || children.size() > 0;
  }

  public Set<String> getAttributeKeys() {

    return attributes.keySet();
  }

  public String getAttribute(String key) {
    return attributes.get(key);
  }

  public void setAttribute(String key, String value) {
    attributes.put(key, value);

  }

  public boolean hasAttribute(String key) {
    return attributes.containsKey(key);
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }
}
