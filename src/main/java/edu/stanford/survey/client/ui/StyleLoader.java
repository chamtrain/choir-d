/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.survey.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Utility class to load custom stylesheets. This is a modified version of the gwt showcase StyleSheetLoader
 */
public class StyleLoader {      
  /**
   * A {@link Timer} that creates a small reference widget used to determine
   * when a new style sheet has finished loading. The widget has a natural width
   * of 0px, but when the style sheet is loaded, the width changes to 5px. The
   * style sheet should contain a style definition that is passed into the
   * constructor that defines a height and width greater than 0px.
   */

  private static class StyleLoadTimer extends Timer {
    private ScheduledCommand callback;
    private Label refWidget;
    private final static int MAX_WAIT=1000;
    private final static int WAIT=10;
    private int waiting = 0;

    /**
     * @param refStyleName the reference style name
     * @param callback the callback to execute when the style sheet loads
     */
    public StyleLoadTimer(String refStyleName, ScheduledCommand callback) {
      this.callback = callback;
      // Create the reference Widget
      refWidget = new Label();
      refWidget.setStyleName(refStyleName);
      refWidget.getElement().getStyle().setProperty("position", "absolute");
      refWidget.getElement().getStyle().setProperty("visibility", "hidden");
      refWidget.getElement().getStyle().setProperty("display", "inline");
      refWidget.getElement().getStyle().setPropertyPx("padding", 0);
      refWidget.getElement().getStyle().setPropertyPx("margin", 0);
      refWidget.getElement().getStyle().setPropertyPx("border", 0);
      refWidget.getElement().getStyle().setPropertyPx("top", 0);
      refWidget.getElement().getStyle().setPropertyPx("left", 0);
      RootPanel.get().add(refWidget);
    }

    @Override
    public void run() {
      // Redisplay the reference widget so it redraws itself
      refWidget.setVisible(false);
      refWidget.setVisible(true);

      // Check the dimensions of the reference widget
      if (refWidget.getOffsetWidth() > 0 || waiting > MAX_WAIT) {
        RootPanel.get().remove(refWidget);
        // Fire the callback in a DeferredCommand to ensure the browser has
        // enough time to parse the styles. Otherwise, we'll get weird styling
        // issues.
        Scheduler.get().scheduleDeferred(callback);
      } else {
        schedule(WAIT);
        waiting+= WAIT;
      }
    }
  }

  /**  
   * Convenience method for getting the document's head element.
   * 
   * @return the document's head element
   */
  public static native HeadElement getHeadElement() /*-{
    return $doc.getElementsByTagName("head")[0];
  }-*/;


  /**
   * Load a style sheet onto the page.
   * 
   * @param href the url of the style sheet
   */
  public static void loadStyleSheet(String href) {
    LinkElement linkElem = Document.get().createLinkElement();
    linkElem.setRel("stylesheet");
    linkElem.setType("text/css");
    linkElem.setHref(href);
    getHeadElement().appendChild(linkElem);
  }

  /**
   * Load a style sheet onto the page and fire a callback when it has loaded.
   * The style sheet should contain a style definition called refStyleName that
   * defines a height and width greater than 0px.
   * 
   * @param href the url of the style sheet
   * @param refStyleName the style name of the reference element
   * @param scheduledCommand the callback executed when the style sheet has loaded
   */
  public static void loadStyleSheet(String href, String refStyleName,
      ScheduledCommand scheduledCommand) {
    loadStyleSheet(href);
    waitForStyleSheet(refStyleName, scheduledCommand);
  }

  /**
   * Detect when a style sheet has loaded by placing an element on the page that
   * is affected by a rule in the style sheet, as described in
   * {@link #loadStyleSheet(String, String, ScheduledCommand)}. When the style sheet has
   * loaded, the callback will be executed.
   * 
   * @param refStyleName the style name of the reference element
   * @param callback the callback executed when the style sheet has loaded
   */
  public static void waitForStyleSheet(String refStyleName, ScheduledCommand callback) {
    new StyleLoadTimer(refStyleName, callback).run();
  }
}


