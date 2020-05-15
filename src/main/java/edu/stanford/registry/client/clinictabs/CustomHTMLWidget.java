/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.Patient;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * HTMLWidget for use with the API. Created by tpacht on 07/25/17.
 */
public class CustomHTMLWidget extends HTMLWidget {
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private PopupPanel myLoadingPopUp;
  private final static Image LOADING_IMAGE = new Image(RegistryResources.INSTANCE.loadingImage());

  public CustomHTMLWidget(ClientUtils clientUtils, String path, final Patient patient) {
    super(clientUtils, path);
    addFunctions(patient);
    exportShowLoadingPopup();
    exportHideLoadingPopup();
    super.load();
    setLoaded(true);
  }

  public void setPatient(Patient patient) {
    addFunctions(patient);
    refresh();
  }

  public void showLoadingPopup() {
    myLoadingPopUp = new PopupPanel();
    myLoadingPopUp.add(LOADING_IMAGE);
    myLoadingPopUp.setStylePrimaryName(css.undecoratedPopup());
    myLoadingPopUp.setSize("25px", "25px");
    myLoadingPopUp.setPopupPosition(300, 300);
    myLoadingPopUp.show();
  }

  public void hideLoadingPopup() {
    myLoadingPopUp.hide();
  }

  public native void exportShowLoadingPopup() /*-{
    var loadInstance = this;
    $wnd.showLoadingPopup = $entry(function () {
      loadInstance.@edu.stanford.registry.client.clinictabs.CustomHTMLWidget::showLoadingPopup()();
    });
  }-*/;

  public native void exportHideLoadingPopup() /*-{
    var hideInstance = this;
    $wnd.hideLoadingPopup = $entry(function () {
      hideInstance.@edu.stanford.registry.client.clinictabs.CustomHTMLWidget::hideLoadingPopup()();
    });
  }-*/;

  private void addFunctions(Patient patient) {
    injectJavaScript(" function choirApiV10() { return '" + URL.encode(GWT.getModuleBaseURL()) + "svc/apiV10/json/'; }");
    injectJavaScript(" function getSiteId() { return '" + URL.encode(getClientConfig().getParam("siteName")) + "'; }");
    if (patient != null) {
      injectJavaScript(" function getPatientId() { return  '" + URL.encode(patient.getPatientId()) + "'; }");
    }
  }

  public void addFunction(String functionName, String returnValue) {
    injectJavaScript(" function " + URL.encode(functionName) + "() { return '" + URL.encode(returnValue) + "'; }");
  }
}

