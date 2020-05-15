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

import edu.stanford.registry.client.TextBoxDatePicker;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

public class XMLTextBoxDatePicker extends TextBoxDatePicker implements XMLDocumentElementIntf {

  DateTimeFormat dtFmt = DateTimeFormat.getFormat("MM-dd-yyyy");

  public XMLTextBoxDatePicker(DateTimeFormat dtf, String tag) {
    super(dtf);
    setTag(tag);
  }

  String tag;

  @Override
  public void setTag(String tag) {
    this.tag = tag;
  }

  @Override
  public String getTag() {
    return tag;
  }

  public String getXMLString() {
    return "<" + getTag() + " value=\"" + dtFmt.format(getValue()) + "\">";
  }

  @Override
  public Element[] getElement(Document doc) {
    Element el = doc.createElement(getTag());
    el.setAttribute("value", dtFmt.format(getValue()));
    Element[] elements = { el };
    return elements;
  }

  /**
   * Has a date been selected
   */
  @Override
  public boolean hasData() {
    return (!(getValue() == null));
  }

  @Override
  public void reset() {
    setValue(new Date());
  }
}
