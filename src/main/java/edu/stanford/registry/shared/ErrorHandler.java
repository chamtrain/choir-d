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

package edu.stanford.registry.shared;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.logging.impl.FormatterImpl;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ErrorHandler {
  private static final Logger log = Logger.getLogger(ErrorHandler.class.getName());
  private static final StackTraceFormatter formatter = new StackTraceFormatter();

  public ErrorHandler() {
  }

  public void displayError(String title, String message1, Throwable caught) {
    displayError(title, message1, null, null, caught, null);
  }

  public void displayError(String title, String message1, String message2, Throwable caught) {
    displayError(title, message1, message2, null, caught, null);
  }

  public void displayError(String title, String message1, String message2, String detailsPre, Throwable caught,
                           String detailsPost) {
    log.log(Level.SEVERE, caught.toString(), caught);
    try {
      if (title == null) {
        title = "Error";
      }
      if (message1 == null) {
        message1 = "An unknown error occurred.";
      }
      if (message2 == null) {
        message2 = "";
      }
      final DialogBox dialog = new DialogBox(false, true);
      VerticalPanel vertical = new VerticalPanel();
      Label messageLabel1 = new Label(message1);
      Label messageLabel2 = new Label(message2);
      messageLabel1.setStylePrimaryName("serverResponseLabelError");
      messageLabel2.setStylePrimaryName("serverResponseLabelError");

      vertical.add(messageLabel1);
      vertical.add(messageLabel2);

      // Add a section containing additional technical information, if available
      StringBuilder details = new StringBuilder();
      if (detailsPre != null) {
        details.append(detailsPre);
      }
      if (caught != null) {
        details.append(formatter.getStackTraceAsString(caught));
      }
      if (detailsPost != null) {
        details.append(detailsPost);
      }
      final String detailsStr = details.toString();
      if (details.length() > 0) {
        DisclosurePanel disclosure = new DisclosurePanel("Technical Details");
//				disclosure.setAnimationEnabled(true);
        SafeHtml html = new SafeHtmlBuilder().appendHtmlConstant("<pre>").appendEscaped(detailsStr)
            .appendHtmlConstant("</pre>").toSafeHtml();
        disclosure.setContent(new HTML(html));
        // disclosure.setContent(new ScrollPanel(new HTML(html))); // TODO the sizing and scrolling doesn't work yet
        // disclosure.addOpenHandler(new OpenHandler<DisclosurePanel>() {
        // @Override
        // public void onOpen(OpenEvent<DisclosurePanel> disclosurePanelOpenEvent) {
        // dialog.center();
        // }
        // });
        // disclosure.addCloseHandler(new CloseHandler<DisclosurePanel>() {
        // @Override
        // public void onClose(CloseEvent<DisclosurePanel> disclosurePanelCloseEvent) {
        // dialog.center();
        // }
        // });
        vertical.add(disclosure);
      }
      log.log(Level.SEVERE, "Displaying error to user:\n  title: " + title + "\n  message1: " + message1
          + "\n  message2: " + message2 + "\n  details:\n" + detailsStr, caught);

      HorizontalPanel buttons = new HorizontalPanel();
      buttons.add(new Button("Refresh Page", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Location.reload();
        }
      }));
      buttons.add(new Button("Continue", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dialog.hide();
        }
      }));
      vertical.add(buttons);
      vertical.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_RIGHT);
      vertical.getElement().getStyle().setProperty("minWidth", Window.getClientWidth() / 2, Unit.PX);
      dialog.getElement().getStyle().setProperty("maxWidth", Window.getClientWidth() * 0.8, Unit.PX);
      dialog.getElement().getStyle().setProperty("maxHeight", Window.getClientHeight() * 0.8, Unit.PX);
      dialog.setText(title);
      dialog.setGlassEnabled(true);
//			dialog.setAnimationEnabled(true);
      dialog.setWidget(vertical);
      dialog.center();
      dialog.show();
    } catch (Throwable t) {
      log.log(Level.SEVERE, "Caught exception trying to display an error message:\n  title: " + title
          + "\n  message1: " + message1 + "\n  message2: " + message2 + "\n  detailsPre: " + detailsPre
          + "\n  detailsPost: " + detailsPost, t);
    }
  }

  /**
   * Hack to access the GWT functionality for printing a stacktrace.
   */
  private static final class StackTraceFormatter extends FormatterImpl {
    @SuppressWarnings("deprecation")
    public String getStackTraceAsString(Throwable t) {
      return super.getStackTraceAsString(t, "\n", "  ");
    }

    @Override
    public String format(LogRecord logRecord) {
      throw new RuntimeException("Method not implemented");
    }
  }
}