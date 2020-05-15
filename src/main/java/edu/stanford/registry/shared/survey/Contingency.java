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
import java.util.Set;

public class Contingency implements Serializable {
  private static final long serialVersionUID = 1L;

  // HashMap<String, String> attributes = new HashMap<String, String>();
  ArrayList<Child> children = null;
  public static final String[] TYPES = { Constants.ITEM.toLowerCase(), "attribute", "state" };
  public static final int ITEM = 0;
  public static final int ATTRIBUTE = 1;
  public static final int STATE = 2;

  int type;
  String attribute, value;

  public Contingency() {

  }

  public Contingency(String type, String attribute, String value) {
    this.type = getType(type);
    setAttrValue(attribute, value);
  }

  public Contingency(int type, String attribute, String value) {
    this.type = type;
    setAttrValue(attribute, value);
  }

  private void setAttrValue(String attribute, String value) {
    this.attribute = attribute;
    this.value = value;
  }

  public static int getType(String typeString) {
    if (typeString == null) {
      typeString = "";
    }
    for (int typInx = 0; typInx < TYPES.length; typInx++) {
      if (TYPES[typInx].equals(typeString.toLowerCase())) {
        return typInx;
      }
    }
    return -1; // temp
    // throw new DataException(typeString +
    // " is not a valid condition type");
  }

  public int getType() {
    return type;
  }

  public String getAttribute() {
    return attribute;
  }

  public String getValue() {
    return value;
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

  public Child getChild(String name) {
    if (hasChildren()) {
      for (int cInx = 0; cInx < getNumberChildren(); cInx++) {
        if (children.get(cInx).childName.equals(name)) {
          return children.get(cInx);
        }
      }
    }

    Child child = new Child(name);
    addChild(child);
    return child;
  }

  public boolean hasChildren() {
    if (children != null && children.size() > 0) {
      return true;
    }
    return false;

  }

  public Set<String> getAttributeKeys(int type) {
    if (type < TYPES.length) {
      return getAttributeKeys(TYPES[type]);
    }
    return getAttributeKeys("");
  }

  public Set<String> getAttributeKeys(String name) {
    return getChild(name).getAttributeKeys();
  }

  public String getAttribute(String name, String key) {
    return getChild(name).getAttribute(key);
  }

  public void setAttribute(String name, String key, String value) {
    getChild(name).setAttribute(key, value);
  }

  public boolean hasAttribute(String name, String key) {
    return getChild(name).hasAttribute(key);
  }

}
