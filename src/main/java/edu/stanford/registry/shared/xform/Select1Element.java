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

import edu.stanford.registry.shared.survey.Constants;

public class Select1Element extends SelectElement {
  private static final long serialVersionUID = 1L;

  // public XFormIntf getWidget(String name, String tag) {
  // if (SelectElement.APPEARANCE_FULL == getAppearance()) {
  // /* display check boxes */
  // return new XML-RadioButtonList(this, name, tag);
  // } else {
  // /* display list box */
  // return getListBox(false, name, tag);
  // } }

  @Override
  public void setSelected(int inx, boolean value) {
    if (value) { // first set all the others to false
      reset();
    }
    items.get(inx).setSelected(value);
  }

  @Override
  public int getLines() {
    return getFormatIntValue(Constants.XFORM_LINES, 3);
  }

}
