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

import edu.stanford.registry.client.xform.XFormIntf;
import edu.stanford.registry.client.xform.XFormSurveyIntf;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;

import java.io.Serializable;
import java.util.ArrayList;

public class StringsElement extends RegistryAnswer implements SurveyAnswerIntf, XFormSurveyIntf, Serializable {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_HEADING = 0;
  public static final int TYPE_TEXT = 1;
  public static final int TYPE_TEXTAREA = 2;

  public static final String[] APPEARANCE = { "label", "text", "textarea" };

  String ref = null;
  String elementLabel = null;
  String name = null;
  String tag = null;
  int align = Constants.ALIGN_VERTICAL;
  int labelPosition = Constants.POSITION_LEFT;
  int stringType = TYPE_HEADING;

  XFormIntf thisWidget = null;

  int appearanceOption = TYPE_HEADING;
  int sizeOption = 1;

  ArrayList<StringItem> items = new ArrayList<>();

  public StringsElement() {
  }

  public void setStringType(int typ) {
    if (typ < APPEARANCE.length) {
      stringType = typ;
    }
  }

  public int getStringType() {
    return stringType;
  }

  public void setRef(String reference) {
    ref = reference;
  }

  @Override
  public String getReference() {
    return ref;
  }

  @Override
  public void setLabel(String lbl) {
    elementLabel = lbl;
  }

  @Override
  public String getLabel() {
    return elementLabel;
  }

  public void setAppearance(int option) {
    if (option < APPEARANCE.length) {
      appearanceOption = option;
    }
  }

  public void setAppearance(String option) {
    for (int a = 0; a < APPEARANCE.length; a++) {
      if (APPEARANCE[a].equals(option)) {
        setAppearance(a);
      }
    }
  }

  public int getAppearance() {
    return appearanceOption;
  }

  public String getAppearanceString() {
    return APPEARANCE[appearanceOption];
  }

  public void setSize(int sz) {
    sizeOption = sz;
  }

  public int getSize() {
    return sizeOption;
  }

  public void addItem(StringItem item) {
    items.add(item);
  }

  public ArrayList<StringItem> getItems() {
    return items;
  }

  @Override
  public void setAlign(int align) {
    if (align == Constants.ALIGN_HORIZONTAL) {
      this.align = align;
    } else {
      this.align = Constants.ALIGN_VERTICAL;
    }
  }

  @Override
  public void setAlign(String align) {
    if (align == null) {
      return;
    }
    if (align.toLowerCase().equals("horizontal")) {
      this.align = Constants.ALIGN_HORIZONTAL;
    } else if (align.toLowerCase().equals("vertical")) {
      this.align = Constants.ALIGN_VERTICAL;
    }
  }

  @Override
  public int getAlign() {
    return align;
  }

  @Override
  public void setLabelPosition(int position) {
    if (position < Constants.POSITION.length) {
      labelPosition = position;
    }
  }

  @Override
  public int getLabelPosition() {
    return labelPosition;
  }

  @Override
  public boolean hasData() {
    if (items.size() > 0) {
      return true;
    }
    return false;
  }

  @Override
  public void reset() {
    // has no effect on a strings element
  }

}
