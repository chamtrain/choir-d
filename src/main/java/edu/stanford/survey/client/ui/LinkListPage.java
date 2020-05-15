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

import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.client.api.SurveySite;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.form.JQMForm;
import com.sksamuel.jqm4gwt.toolbar.JQMHeader;

/**
 * This survey page presents two lines of instructions, a set of radio
 * button options, and a continue button. One of the radio buttons must
 * be selected before continuing.
 */
class LinkListPage extends SurveyPage {
  private SurveyFactory factory = GWT.create(SurveyFactory.class);

  LinkListPage(String[] sites) {


    JQMButton buttonLeft = new JQMButton(" ");
    buttonLeft.setStyleName(SurveyBundle.INSTANCE.css().uiStanfordLogoLeft());

    JQMButton buttonRight = new JQMButton(" ");
    buttonRight.setStyleName(SurveyBundle.INSTANCE.css().uiChoirLogoRight());
    JQMHeader header = new JQMHeader("CHOIR Survey Sites", buttonLeft, buttonRight);
    header.addStyleName(SurveyBundle.INSTANCE.css().uiLinkTitle());
    header.setBackButton(false);
    setHeader(header);
    addStyleName(SurveyBundle.INSTANCE.css().fullWidthUnlabeledFieldset());
    addStyleName(SurveyBundle.INSTANCE.css().stopButton());

    JQMForm form = new JQMForm();
    form.addStyleName(SurveyBundle.INSTANCE.css().uiLinkForm());


    //form.addStyleName("ui-content");
    //form.addStyleName("ui-page");
    //form.addStyleName("ui-page-theme-a");
    form.addStyleName(SurveyBundle.INSTANCE.css().fullWidthUnlabeledFieldset());

    UrlBuilder builder = Window.Location.createUrlBuilder();
    for (String siteString : sites) {
      SurveySite site = AutoBeanCodex.decode(factory, SurveySite.class, siteString).as();
      builder.setParameter("s", site.getUrlParam());
      form.add(new HTML("<p><h3 class=\"ui-link-h3\"><a href=\"" + builder.buildString() + "\">" +
          Sanitizer.sanitizeHtml(site.getDisplayName()).asString() + "</a></h3></p><p></p><p></p>"));

    }
    add(form);
  }
}
