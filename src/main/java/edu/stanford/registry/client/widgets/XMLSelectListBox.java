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

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

public class XMLSelectListBox extends SelectListBox implements XMLDocumentElementIntf {
  @Override
  public Element[] getElement(Document doc) {
    int count = 0;
    for (int i = 0; i < listBox.getItemCount(); i++) {
      if (listBox.isItemSelected(i) && listBox.getItemText(i) != null && listBox.getItemText(i).trim().length() > 0) {
        count++;
      }
    }
    Element[] elementList = new Element[count];
    doc.createElement(getTag() + "List");

    count = 0;
    for (int i = 0; i < listBox.getItemCount(); i++) {
      if (listBox.isItemSelected(i) && listBox.getItemText(i) != null && listBox.getItemText(i).trim().length() > 0) {
        Element el = doc.createElement(getTag());
        // el.appendChild(doc.createTextNode(listBox.getItemText(i)));
        el.setAttribute("value", listBox.getItemText(i));
        elementList[count] = el;
      }
    }
    return elementList;
  }
}
