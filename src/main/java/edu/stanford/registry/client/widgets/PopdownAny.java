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

import edu.stanford.registry.client.widgets.PopupRelative.Align;
import edu.stanford.registry.client.widgets.PopupRelative.CloseCallback;

import com.google.gwt.dom.client.Element;

/**
 * Widget representing a button with a drop down menu.
 *
 * @author garricko
 */
public class PopdownAny {
  private final Element relativeTo;
  private final Align align;
  private final Customizer customizer;
  private PopupRelative popup;

  public PopdownAny(Element relativeTo, final Align align, final Customizer customizer) {
    this.relativeTo = relativeTo;
    this.align = align;
    this.customizer = customizer;
  }

  public void show() {
    if (popup == null) {
      Menu menu = new Menu();
      customizer.customizePopup(menu);
      popup = new PopupRelative(relativeTo, menu.getMenuBar(), align, new CloseCallback() {
        @Override
        public void afterClose() {
          popup = null;
        }
      });
      menu.closePopupBeforeCommand(popup);
    } else {
      popup.close();
    }
  }

  public void hide() {
    if (popup != null) {
      popup.close();
    }
  }

  public void toggle() {
    if (popup == null) {
      show();
    } else {
      hide();
    }
  }

  public boolean isVisible() {
    return popup != null;
  }

  public interface Customizer {
    void customizePopup(Menu menu);
  }
}
