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

public class PromisAnswer implements SurveyAnswerIntf, Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -5834293387729848727L;
  private ArrayList<String> description = new ArrayList<>();
  private int type = Constants.TYPE_RADIO;
  private HashMap<String, String> attributes = new HashMap<>();
  private boolean isSelected = false;

  @Override
  public ArrayList<String> getText() {
    return description;
  }

  @Override
  public void addText(String answer) {
    description.add(answer);
  }

  @Override
  public int getType() {

    return type;
  }

  @Override
  public void setType(int type) {
    this.type = type;

  }

  @Override
  public Set<String> getAttributeKeys() {

    return attributes.keySet();
  }

  @Override
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  public boolean hasAttribute(String key) {
    return attributes.containsKey(key);
  }

  @Override
  public void setAttribute(String key, String value) {
    attributes.put(key, value);

  }

  public void setSelected(boolean sel) {
    isSelected = sel;
  }

  @Override
  public boolean getSelected() {
    return isSelected;
  }

  public String getDescription() {
    if (getText() != null && getText().size() > 0) {
      return getText().get(0);
    }
    return null;
  }

  @Override
  public ArrayList<String> getResponse() {
    return description;
  }

  @Override
  public String getClientId() {
    String oid = nvl(getAttribute("ItemResponseOID"));
    String val = nvl(getAttribute("Value"));
    return oid + "|" + val;

  }

  @Override
  public void setClientId(String id) {
    // split the id into the 'responseOID|value' attributes
    if (id != null) {
      String[] parts = id.split("|");
      if (parts.length > 0) {
        setAttribute("ItemResponseOID", parts[0]);
      }
      if (parts.length > 1) {
        setAttribute("Value", parts[1]);
      }
    }

  }

  private String nvl(String str) {
    return nvl(str, "");
  }

  private String nvl(String str, String defaultWhenNull) {
    if (str == null) {
      return defaultWhenNull;
    }
    return str;
  }
}
