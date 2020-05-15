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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Button with an image and text
 */
public class ImageButton implements IsWidget {
  private Button button;
  private SafeHtml image;
  private String text;

  public ImageButton(ImageResource icon, String text) {
    setIcon(icon);
    setText(text);
    button = new Button(htmlText());
    button.addStyleName(WidgetResources.INSTANCE.css().popdownButton());
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

  public void setText(String text) {
    this.text = text;
    if (button != null) {
      button.setHTML(htmlText());
    }
  }

  public void setStyle(String styleName) {
    button.setStyleName(styleName);
  }

  public void addStyleName(String styleName) {
    button.addStyleName(styleName);
  }

  public void setTitle(String title) {
    button.setTitle(title);
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
    builder.appendHtmlConstant("</span>");
    return builder.toSafeHtml();
  }

  @Override
  public Widget asWidget() {
    return button;
  }


  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return button.addClickHandler(handler);
  }

}
