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

package edu.stanford.survey.client.ui;

import edu.stanford.survey.client.api.DisplayStatus;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.sksamuel.jqm4gwt.JQMIcon;
import com.sksamuel.jqm4gwt.JQMPage;
import com.sksamuel.jqm4gwt.JQMPopup;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

/**
 * Base class to use for pages within the survey.
 */
public class SurveyPage extends JQMPage {
  private final String pageId;

  SurveyPage() {
    pageId = Document.get().createUniqueId();
    getElement().setId(pageId);
  }

  public SurveyPage withSurveyRestart(final TapHandler callback, boolean requireConfirm) {
    getHeader().setBackButton(false);
    if (callback != null) {
//      if (requireConfirm) {
        JQMButton confirm = new JQMButton(new Button("End Survey")) {};
        confirm.addTapHandler(callback);
        JQMButton cancel = new JQMButton(new Button("Continue Survey")) {};
      JQMButton sitesMenu = new JQMButton(new Button("Return to Menu")) {
      };
        final JQMPopup popup = new JQMPopup(
            new Label("Are you sure you want to stop this survey?"),
            cancel,
            confirm,
            sitesMenu
        );
      sitesMenu.addTapHandler(callback);
        popup.setPadding(true);
        cancel.addTapHandler(new TapHandler() {
          @Override
          public void onTap(TapEvent event) {
            popup.close();
          }
        });
        JQMButton stopButton = new JQMButton("^", popup);
        stopButton.addStyleName(SurveyBundle.INSTANCE.css().stopButton());
        getHeader().setRightButton(stopButton);
        add(popup);
      // Commenting non-confirm way because it isn't working on iPads with iOS 7.0 (browser bug?)
//      } else {
//        JQMButton stopButton = new JQMButton("^");
//        stopButton.addClickHandler(callback);
//        getHeader().setRightButton(stopButton);
//      }
    }
    return this;
  }

  @Override
  protected void onPageShow() {
    if (enableFastclick()) {
      fastclick(pageId);
    }
  }

  protected boolean enableFastclick() {
    return true;
  }

  protected native String[] fastclick(String id) /*-{
    //$wnd.FastClick.attach($wnd.$('#' + id).get(0));
  }-*/;

  @Override
  protected void onPageHide() {
    // We replace pages each time the user submits, so make sure the old pages
    // get removed from the DOM so we don't leak memory
    removeFromParent();
  }

  public JQMHeader setHeader(DisplayStatus displayStatus) {
    JQMHeader header = new JQMHeader(displayStatus.getPageTitle());
    header.setBackButton(true);
    setHeader(header);
    JQMIcon icon = new JQMIcon();
    icon.setStyleName(SurveyBundle.INSTANCE.css().uiBtnLeft());
    header.insert(icon, 0);

    ProgressBar progressBar = new ProgressBar();
    progressBar.setPercent(displayStatus.getProgress());
    header.add(progressBar);

    return header;
  }
//  public void setEnabled(boolean enabled) {
//    setEnabled(this, enabled);
//  }
//
//  private void setEnabled(Widget widget, boolean enabled) {
//    if (widget instanceof HasWidgets) {
//      for (Widget child : ((HasWidgets) widget)) {
//        setEnabled(child, enabled);
//        if (child instanceof FocusWidget) {
//          ((FocusWidget) child).setEnabled(enabled);
//        }
//      }
//    }
//  }
}
