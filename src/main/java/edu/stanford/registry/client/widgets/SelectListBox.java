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

import edu.stanford.registry.client.xform.XFormIntf;
import edu.stanford.registry.client.xform.XMLWidget;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.ListBox;

public class SelectListBox extends XMLWidget implements XFormIntf {

  ListBox listBox;

  public SelectListBox() {
  }

  public SelectListBox(boolean multiple, String elementName, String elementTag, String elementLabel, String references) {

    setName(elementName);
    setTag(elementTag);
    setLabel(elementLabel);
    setReference(references);
    listBox = new ListBox();
    listBox.setMultipleSelect(multiple);
    initWidget(listBox);
  }

  public ListBox getListBox() {
    return listBox;
  }

  /**
   * Has data been selected
   */
  @Override
  public boolean hasData() {
    for (int i = 0; i < listBox.getItemCount(); i++) {
      if (listBox.isItemSelected(i) && listBox.getItemText(i) != null && listBox.getItemText(i).trim().length() > 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ArrayList<String> getResponse() {
    ArrayList<String> responses = new ArrayList<>();
    for (int i = 0; i < listBox.getItemCount(); i++) {
      if (listBox.isItemSelected(i) && listBox.getItemText(i) != null && listBox.getItemText(i).trim().length() > 0) {
        responses.add(listBox.getItemText(i).trim());
      }
    }
    return responses;
  }

  @Override
  public boolean getSelected() {
    return hasData();
  }

  // clears all selected items
  @Override
  public void reset() {
    for (int i = 0; i < listBox.getItemCount(); i++) {
      listBox.setItemSelected(i, false);
    }
  }
}
