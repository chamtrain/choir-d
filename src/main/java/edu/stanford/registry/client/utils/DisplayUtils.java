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

package edu.stanford.registry.client.utils;

import edu.stanford.registry.client.CustomTextBox;
import edu.stanford.registry.client.FormData;
import edu.stanford.registry.client.TextBoxDatePicker;
import edu.stanford.registry.shared.ApptRegistration;

import java.util.Date;

import com.google.gwt.user.client.ui.Label;

public class DisplayUtils {
  public static CustomTextBox makeRequiredField(String value) {
    FormData formData = new FormData();
    formData.setRequired(true);

    CustomTextBox ctb = new CustomTextBox(formData);
    ctb.setValue(value);
    return ctb;
  }

  public static TextBoxDatePicker makelistDateField(ClientUtils utils) {
    return makelistDateField(utils, new Date());
  }

  public static TextBoxDatePicker makelistDateField(ClientUtils utils, Date dt) {
    TextBoxDatePicker listDt = new TextBoxDatePicker(utils.getDefaultDateFormat());
    listDt.setValue(dt);
    return listDt;
  }

  public static Label makeRegistrationTypeLabel(ApptRegistration surveyRegistration) {
    Label registrationTypeLabel = new Label();

    if (surveyRegistration.isCancelled()) {
      registrationTypeLabel.setText("Y*");
      registrationTypeLabel.setTitle("CANCELLED");
      registrationTypeLabel.addStyleName("cancelled");
    } else if (surveyRegistration.isAppointment()) {
      registrationTypeLabel.setText("Y");
    } else {
      registrationTypeLabel.setText("");
    }
    return registrationTypeLabel;
  }
}
