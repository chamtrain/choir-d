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

package edu.stanford.registry.client.widgets;

import edu.stanford.registry.client.CustomTextBox;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

public class XMLCustomTextBox extends CustomTextBox implements XMLDocumentElementIntf {

  String tag;
  String showDescription;
  boolean isDescriptionValidValue = false;

  public XMLCustomTextBox(String tag) {
    setTag(tag);
  }

  @Override
  public String getTag() {
    return tag;
  }

  @Override
  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getXMLString() {
    return "<" + getTag() + " value=\"" + getText() + "\">";
  }

  @Override
  public Element[] getElement(Document doc) {
    Element el = doc.createElement(getTag());
    el.setAttribute("value", getText());
    Element[] elements = { el };
    return elements;
  }

  /**
   * Does it have a value assigned. It is considered to not have a value if its
   * the value set by the setDescription method unless its been explicitly set
   * to allow that value with setDescription('value', true).
   */
  @Override
  public boolean hasData() {
    if (getText() != null && getText().trim().length() > 0
        && (!getText().equals(showDescription) || isDescriptionValidValue)) {
      return true;
    }
    return false;
  }

  /**
   * This will set the initial value to some descriptive text on load. This
   * value will not be considered a valid value by hasData() unless it's been
   * specifically enabled.
   *
   * @param description to display as the field value on load.
   */
  public void setDescription(String description) {
    setDescription(description, false);
  }

  /**
   * This will set the initial value to some descriptive text and allow(true) or
   * disallow(false) this value to be considered legitimate by hasData().
   *
   * @param description to display in the field on load.
   */
  public void setDescription(String description, boolean valid) {
    showDescription = description;
    isDescriptionValidValue = valid;
    setValue(showDescription);
  }

  @Override
  public void reset() {
    setValue(showDescription);
  }
}
