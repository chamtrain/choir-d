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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ImageLabel extends HorizontalPanel {
  private Image image = null;
  private Label label = null;
  private String labelStyle = null;

  public ImageLabel() {

  }

  public void setImage(Image img) {
    clear();
    image = img;
    add(image);
    if (label != null) {
      add(label);
    }
  }

  public void setLabelText(String str) {
    clear();
    label = new Label(str);
    if (labelStyle != null) {
      label.setStyleName(labelStyle);
    }
    if (image != null) {
      add(image);
    }
    add(label);
  }

  public void setLabelStyle(String str) {
    labelStyle = str;
    if (label != null && labelStyle != null) {
      label.setStyleName(labelStyle);
    }
  }
}
