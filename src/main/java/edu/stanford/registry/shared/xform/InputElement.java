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

import edu.stanford.registry.client.xform.XFormSurveyIntf;
import edu.stanford.registry.shared.survey.Child;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;

import java.io.Serializable;
import java.util.ArrayList;

public class InputElement extends RegistryAnswer implements SurveyAnswerIntf, XFormSurveyIntf, Serializable {
  private static final long serialVersionUID = 1L;

  int align = Constants.ALIGN_VERTICAL;
  int labelPosition = Constants.POSITION_LEFT;

  public InputElement() {
    setType(Constants.TYPE_INPUT);
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

  public int getDataType() {
    Child format = getChild(Constants.XFORM_FORMAT);
    if (format != null) {
      String dataType = format.getAttribute("datatype");
      for (int tInx = 0; tInx < Constants.FORMAT_DATATYPE.length; tInx++) {
        if (Constants.FORMAT_DATATYPE[tInx].equals(dataType)) {
          return tInx;
        }
      }
    }
    return 0; // default to text
  }

  @Override
  public boolean hasData() {
    if (getStringValue() != null && getStringValue().trim().length() > 0) {
      return true;
    }
    return false;
  }

  @Override
  public boolean getSelected() {
    return hasData();
  }

  @Override
  public void reset() {
    setValue("");
    setSelected(false);
  }

  @Override
  public void setAttribute(String key, String value) throws NumberFormatException {
    super.setAttribute(key, value);
    if (key != null) {
      if (Constants.XFORM_LABEL.equals(key)) {
        setLabel(value);
      }
      if (Constants.XFORM_LOCATION.equals(key)) {
        for (int locInx = 0; locInx < Constants.POSITION.length; locInx++) {
          if (Constants.POSITION[locInx].equals(value)) {
            setLabelPosition(locInx);
          }
        }
      }
    }
  }

  @Override
  public ArrayList<String> getResponse() {
    ArrayList<String> response = new ArrayList<>();
    if (getValue() != null && getValue().trim().length() > 0) {
      if (getLabelPosition() == Constants.POSITION_ABOVE || getLabelPosition() == Constants.POSITION_LEFT) {
        response.add(getLabel() + " " + getValue());
      } else {
        response.add(getValue() + " " + getLabel());
      }
    }
    return response;
  }

}
