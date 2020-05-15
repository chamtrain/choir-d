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

import edu.stanford.survey.client.api.BodyMapAnswer;
import edu.stanford.survey.client.api.BodyMapQuestion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckbox;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckset;
import com.sksamuel.jqm4gwt.html.Paragraph;

/**
 * This survey page presents two lines of instructions, a set of radio button options, and a continue button. One of the radio buttons must be selected before continuing.
 */
class BodyMapPage extends SurveyPage {
  private final String imagesId;
  private final String fillColor;
  private final String fillOpacity;
  private String checkboxId;
  private JQMCheckbox nopain;

  interface Submit {
    void submit(BodyMapAnswer answer);
  }

  BodyMapPage(BodyMapQuestion question, final BodyMapAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle1()).asString() + "</h3>"));
    }

    if (question.getTitle2() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle2()).asString() + "</h3>"));
    }

    HTML images = new HTML(question.getImgTag1() + question.getImgTag2() + question.getMapTag1()
        + question.getMapTag2());

    String highlightColor = "545454";
    if (question.getHighlightColor() != null && !question.getHighlightColor().isEmpty()) {
      highlightColor = question.getHighlightColor();
    }
    fillColor = highlightColor;
    String opacity = "0.2";
    if (question.getFillOpacity() != null) {
      opacity = question.getFillOpacity();
    }
    fillOpacity = opacity;

    imagesId = Document.get().createUniqueId();
    images.getElement().setId(imagesId);
    add(images);

    if (question.getNoPainCheckboxLabel() != null) {
      JQMCheckset checkbox = new JQMCheckset();
      checkboxId = Document.get().createUniqueId();
      checkbox.getElement().setId(checkboxId);
      nopain = new JQMCheckbox("nopain", question.getNoPainCheckboxLabel());
      checkbox.addCheckbox(nopain);
      add(checkbox);
    }

    final Paragraph error = new Paragraph();
    error.addStyleName(SurveyBundle.INSTANCE.css().errorMessage());
    add(error);

    final JQMButton button = new JQMButton(new Button("Continue")) {};
    button.addStyleName(SurveyBundle.INSTANCE.css().continueButton());
    button.addTapHandler(new TapHandler() {
      @Override
      public void onTap(TapEvent event) {
        String regions = selectedRegions(imagesId);

        if (nopain != null) {
          // If checkbox was requested, force either regions/checkbox but not both
          if ((regions == null || regions.length() == 0) && !nopain.isChecked()) {
            error.setText("Choose body regions or check the no pain checkbox");
            return;
          } else if (regions != null && regions.length() > 0 && nopain.isChecked()) {
            error.setText("Choose body regions or check the no pain checkbox, but not both");
            return;
          }
        }

        if (regions != null && regions.length() > 0) {
          answer.setRegionsCsv(regions);
        }
        submit.submit(answer);
      }
    });
    add(button);
  }

  @Override
  protected boolean enableFastclick() {
    // FastClick seems to break maphilight...
    return false;
  }

  @Override
  protected void onPageShow() {
    super.onPageShow();
    GWT.log("Setting mapHighlite with color:" + fillColor + " and fillOpacity:" + fillOpacity);
    highlight(imagesId, fillColor, Double.parseDouble(fillOpacity));
    if (checkboxId != null) {
      // And yet these don't work without FastClick
      fastclick(checkboxId);
    }
  }

  private native String[] highlight(String id, String imageFillColor, double imageFillOpacity) /*-{
    $wnd.$('#' + id + ' img').maphilight({
      // Disable fill and stroke in general to suppress hover behavior, which
      // causes problems on tablets
      fill: false,
      fillColor: imageFillColor,
      fillOpacity : imageFillOpacity,
      fade: false,
      stroke: false,
//			strokeColor : '000000',
//			strokeOpacity : 1,
//			strokeWidth : 1,
      wrapClass: 'hilightImgWrapper'
    });
    // Turn of FastClick library for image map because it breaks Firefox mobile
//		$wnd.$('#' + id + ' *').addClass('needsclick');
    $wnd.$('#' + id + ' map area').tap(
            function (e) {
              e.preventDefault();
              var data = $wnd.$(this).data('maphilight') || {};
//							var data = $wnd.$(this).mouseout().data(
//									'maphilight')
//									|| {};
              data.alwaysOn = !data.alwaysOn;
              data.fill = data.alwaysOn;
//              alert('Click: alwaysOn=' + data.alwaysOn + ' fill=' + data.fill);
//              $wnd.$('#' + errorId).innerHtml = 'Click: alwaysOn=' + data.alwaysOn + ' fill=' + data.fill;
              $wnd.$(this).data('maphilight', data).trigger('alwaysOn.maphilight');
            });
//    $wnd.$('#' + id + ' map area').dblclick(function(e) {e.preventDefault(); alert('double click')});
  }-*/;

  private native String selectedRegions(String id) /*-{
    return $wnd.$('#' + id + ' map area').map(function () {
      var data = $wnd.$(this).data('maphilight') || {};
      if (data.alwaysOn) {
        return this.id;
      } else {
        return null;
      }
    }).get().join();
  }-*/;
}
