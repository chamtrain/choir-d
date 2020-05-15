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

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget representing a button with a drop down menu.
 *
 * You must call asButton() or asWidget() to create the clickHandler.
 * Note that only when the click-handler is called is the customizer
 * (which populates the menu) called.
 *
 * @author garricko
 */
public class PopdownButton implements IsWidget {
  private Button button;
  private SafeHtml image;
  private String text;
  private Customizer customizer;
  private PopupRelative popup;
  private Align align = Align.BELOW_LEFT;
  private ArrayList<String> menuStyles;
  private String style;

  /**
   * The customizer.customizePopup(button, menu) is called when it's first clicked.
   */
  public PopdownButton withMenu(Customizer customizer) {
    this.customizer = customizer;
    return this;
  }

  /**
   * Simply calls setText(text), to initialize the button's label
   */
  public PopdownButton withText(String text) {
    setText(text);
    return this;
  }

  /**
   * Initializes the button's label
   */
  public void setText(String text) {
    this.text = text;
    if (button != null) {
      button.setHTML(htmlText());
    }
  }

  public PopdownButton withIcon(ImageResource icon) {
    setIcon(icon);
    return this;
  }

  public void setIcon(ImageResource icon) {
    if (icon != null) {
      image = SafeHtmlUtils.fromTrustedString(new Image(icon).getElement().getString());
    } else {
      image = null;
    }
    if (button != null) {
      button.setHTML(htmlText());
    }
  }

  public PopdownButton align(Align align) {
    this.align = align;
    return this;
  }

  /**
   * Set the ordinary CSS style for this button. Only one style may be set this way.
   */
  public PopdownButton withStyle(String style) {
    assert this.style == null;

    this.style = style;
    if (button != null) {
      button.addStyleName(style);
    }
    return this;
  }

  /**
   * Set the CSS style for the popup content that is rendered when the button is clicked.
   * Any number of styles may be added this way.
   */
  public PopdownButton addMenuStyle(String style) {
    if (menuStyles == null) {
      menuStyles = new ArrayList<>(3);
    }
    menuStyles.add(style);
    return this;
  }

  public Button asButton() {
    if (button == null) {
      button = new Button(htmlText());
      button.addStyleName(WidgetResources.INSTANCE.css().popdownButton());
      if (style != null) {
        button.addStyleName(style);
      }
      button.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (popup == null) {
            Menu menu = new Menu();
            customizer.customizePopup(PopdownButton.this, menu);
            if (menuStyles != null) {
              for (String style : menuStyles) {
                menu.getMenuBar().addStyleName(style);
              }
            }
            popup = new PopupRelative(button, menu.getMenuBar(), align, new CloseCallback() {
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
      });
    }
    return button;
  }

  @Override
  public Widget asWidget() {
    return asButton();
  }

  public interface Customizer {
    void customizePopup(PopdownButton button, Menu menu);
  }

  private SafeHtml htmlText() {
    SafeHtmlBuilder builder = new SafeHtmlBuilder();
    if (image != null) {
      builder.append(image);
    }
    builder.appendHtmlConstant("<span>");
    if (text != null) {
      builder.appendEscaped(text);
    }
    // The explicit class here is just a hack for IE8 lacking support for CSS3 :last-child
    // \u25bc is the unicode character ▼ (down triangle)
    builder.appendHtmlConstant(
        "</span><span class=\"" + WidgetResources.INSTANCE.css().popdownButtonDownArrow() + "\">\u25bc</span>");
    return builder.toSafeHtml();
  }
}
