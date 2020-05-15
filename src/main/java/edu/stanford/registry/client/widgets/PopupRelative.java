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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Convenience class for creating and displaying popup content. Example usage:
 * <p><pre>
 *   Popup popup = new Popup(myButton, panelToShow, Popup.Align.RIGHT);
 *   ...
 *   popup.close();
 * </pre></p>
 */
public class PopupRelative {
  private static final WidgetsCssResource style = WidgetResources.INSTANCE.css();

  public enum Align {
    BELOW_LEFT, BELOW_RIGHT, BELOW_CENTER, COVER_LEFT, COVER_RIGHT, COVER_CENTER
  }

  private enum HorizontalAlign {
    LEFT, RIGHT, CENTER
  }

  private enum VerticalAlign {
    BELOW, COVER_BELOW //, ABOVE, COVER_ABOVE
  }

  public interface CloseCallback {
    void afterClose();
  }

  private CloseCallback closer;
  private PopupPanel popup;
  private HandlerRegistration handlerRegistration;

  /**
   * Construct a popup and display it immediately.
   *
   * @param relativeTo   the component causing the popup to display
   *                     (we position the popup right below this component)
   * @param popupContent the component to be displayed inside the popup
   * @param alignment    left indicates the left edge of the popup will align with
   *                     relativeTo's left edge; right means align right edges
   * @param closer       optionally get a callback after the popup has closed; may be null
   */
  public PopupRelative(final Widget relativeTo, Widget popupContent, final Align alignment, CloseCallback closer) {
    this(relativeTo.getElement(), popupContent, alignment, closer);
  }

  public PopupRelative(final Element relativeTo, Widget popupContent, final Align alignment, CloseCallback closer) {
    this(relativeTo, popupContent, alignment, true, closer);
    popup.addAutoHidePartner(relativeTo); // TODO remove it later to avoid leak
  }

  /**
   * Construct a popup and display automatically. Use if setting the autoHide to false (the constructor without this defaults to 'true')
   *
   * @param autoHide <code>true</code> if the popup should be automatically hidden when
   *                 the user clicks outside of it or the history token changes.
   *
  */
  public PopupRelative(final Element relativeTo, Widget popupContent, final Align alignment, boolean autoHide, CloseCallback closer) {
    this.closer = closer;
    popupContent.addStyleName(style.popupContent());
    popup = new PopupPanel(autoHide);
    popup.setStylePrimaryName(style.popupRelative());
    popup.setPreviewingAllNativeEvents(true);
    popup.setWidget(popupContent);
    final int labelWidth = relativeTo.getOffsetWidth();
    final int labelHeight = relativeTo.getOffsetHeight();
    popup.addCloseHandler(new CloseHandler<PopupPanel>() {
      @Override
      public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
        close();
        relativeTo.removeClassName(style.popupVisible());
      }
    });
    relativeTo.addClassName(style.popupVisible());
    popup.setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int popupWidth, int offsetHeight) {
        GWT.log(
            "popupWidth=" + popupWidth + " offsetHeight=" + offsetHeight + " labelWidth=" + labelWidth + " alignment="
                + alignment);

        HorizontalAlign horizontalAlign;
        VerticalAlign verticalAlign;
        if (alignment.name().contains(VerticalAlign.BELOW.name())) {
          verticalAlign = VerticalAlign.BELOW;
        } else {
          verticalAlign = VerticalAlign.COVER_BELOW;
        }
        if (alignment.name().contains(HorizontalAlign.LEFT.name())) {
          horizontalAlign = HorizontalAlign.LEFT;
        } else if (alignment.name().contains(HorizontalAlign.RIGHT.name())) {
          horizontalAlign = HorizontalAlign.RIGHT;
        } else {
          horizontalAlign = HorizontalAlign.CENTER;
        }

        RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
        int screenWidth = rootLayoutPanel.getOffsetWidth();
        int screenHeight = rootLayoutPanel.getOffsetHeight();

        int y;
        if (verticalAlign == VerticalAlign.BELOW) {
          y = relativeTo.getAbsoluteTop() + labelHeight;
        } else {
          y = relativeTo.getAbsoluteTop();
        }
        if (y + offsetHeight > screenHeight) {
          // Attempt 1: shift to above the target if it will fit
          if (relativeTo.getAbsoluteTop() - offsetHeight > 0) {
            y = relativeTo.getAbsoluteTop() - offsetHeight;
          } else {
            // Can't fit below, can't fit above, just cover it up starting from screen bottom
            y = Math.max(screenHeight - offsetHeight, 0);
          }
        }

        if (horizontalAlign == HorizontalAlign.CENTER) {
          int x = relativeTo.getAbsoluteLeft() + labelWidth / 2 - popupWidth / 2;
          if (x < 0) {
            x = 0;
          }
          if (x + popupWidth > screenWidth) {
            popup.setWidth("" + (screenWidth - x) + "px");
          }
          popup.setPopupPosition(x, y);
        } else if (horizontalAlign == HorizontalAlign.RIGHT) {
          int x = relativeTo.getAbsoluteRight() - popupWidth + 1;
          if (x < 0) {
            popup.setWidth("" + (popupWidth + x) + "px");
            x = 0;
          }
          popup.setPopupPosition(x, y);
        } else {
          int x = relativeTo.getAbsoluteLeft() + 1;
          if (x + popupWidth > screenWidth) {
            popup.setWidth("" + (screenWidth - x) + "px");
          }
          popup.setPopupPosition(x, y);
        }
      }
    });
    handlerRegistration = Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        GWT.log("Detected root resize");
        close();
      }
    });
  }

  public void close() {
    if (popup != null) {
      popup.hide();
//      popup.removeAutoHidePartner();
      popup = null;
    }
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
      handlerRegistration = null;
    }
    if (closer != null) {
      closer.afterClose();
    }
  }
}
