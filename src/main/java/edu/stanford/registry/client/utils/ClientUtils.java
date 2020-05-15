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
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.RegistryRpcRequestBuilder;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.User;

import java.util.Date;
import java.util.Objects;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class ClientUtils extends CommonUtils /* */ implements DateUtilsIntf {
  public static final long SECONDS_IN_A_DAY = 24 * 3600 * 1000;

  public static final int PATIENT_SEARCH_BY_PATIENT_ID = 0;
  public static final int PATIENT_SEARCH_BY_PARTIAL_NAME = 1;
  public static final int PATIENT_SEARCH_BY_EMAIL = 2;
  public static final Image LOADING_IMAGE = new Image(RegistryResources.INSTANCE.loadingImage());
  private final PopupPanel loadingPopUp = new PopupPanel();
  private static RegExp patientIdPattern = null;

  static final String CHART_URL = "/registry/registry/svc/chart"; // use getChartUrl() to add siteId

  public DateTimeFormat xmlFmtDt = DateTimeFormat.getFormat("yyyy-MM-dd");
  public DateTimeFormat xmlFmtDtTm = DateTimeFormat.getFormat("yyyy-MM-dd hh:mm:ss.S");
  private DateTimeFormat dateTimeFormat = null;
  private DateTimeFormat dateFormat = null;
  public DateTimeFormat yyyyFmt = DateTimeFormat.getFormat("yyyy");
  public DateTimeFormat mmFmt = DateTimeFormat.getFormat("MM");
  public DateTimeFormat ddFmt = DateTimeFormat.getFormat("dd");

  //protected Popup basicErrorPopUp = new Popup("ERROR");

  private ClientConfig clientConfig;
  private User user;
  protected Long siteId;

  public ClientUtils(ClientConfig clientConfig, User user) {
    super(clientConfig.getParams());
    this.clientConfig = clientConfig;
    this.siteId = clientConfig.getSiteId();
    this.user = user;
    if (patternString == null) {
      patternString = "^\\s*\\S.*$";
    }
    patientIdPattern = RegExp.compile(patternString);

    loadingPopUp.add(LOADING_IMAGE);
    loadingPopUp.setStylePrimaryName("undecoratedPopup");
  }

  public Long getSiteId() {
    return siteId;
  }

  /**
   * @return Gets a parameter configured for the site, or null if not found (never empty)
   */
  public String getParam(String key) {
    return clientConfig.getParam(key);
  }

  /**
   * @return a parameter configured for the site, or if null, the default
   */
  public String getParam(String key, String dflt) {
    return clientConfig.getParam(key, dflt);
  }

  /**
   * @return true if value.equals(getParam(key,dflt))
   */
  public boolean paramEquals(String key, String dflt, String toMatch) {
    return clientConfig.paramEquals(key, dflt, toMatch);
  }

  public boolean paramEqualsIgnoreCase(String key, String dflt, String toMatch) {
    return clientConfig.paramEqualsIgnoreCase(key, dflt, toMatch);
  }

  public ClientConfig getClientConfig() {
    return clientConfig;
  }

  public User getUser() {
    return user;
  }

  public boolean isPhysician() {
    return user.hasRole("PHYSICIAN", clientConfig.getSiteName());
  }

  public void showLoadingPopUp() {
    loadingPopUp.center();
  }

  public void hideLoadingPopUp() {
    loadingPopUp.hide();
  }

  public DateTimeFormat getDefaultDateTimeFormat() {
    if (dateTimeFormat == null) { // initialize on first call
      try {
        dateTimeFormat = DateTimeFormat.getFormat(getDateTimeFormatString());
      } catch (IllegalArgumentException iae) {
        dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);
      }
    }
    return dateTimeFormat;
  }

  public DateTimeFormat getDefaultDateFormat() {
    if (dateFormat == null) { // initialize on first call
      try {
        dateFormat = DateTimeFormat.getFormat(getDateFormatString());
      } catch (IllegalArgumentException iae) {
        dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_LONG);
      }
    }
    return dateFormat;
  }

  public static String getDateString(DateTimeFormat dtf, Date dt) {
    if (dt == null) {
      return "";
    } else {
      return dtf.format(dt);
    }
  }

  @Override
  public String getDateString(java.util.Date dt) {
    if (dt == null) {
      return "";
    } else {
      return getDefaultDateFormat().format(dt);
    }
  }

  public boolean isValidDate(String date) {
    if (date == null) {
      return false;
    }

    try {
      getDefaultDateFormat().parse(date);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private java.util.Date setTimeToZero(java.util.Date date) {

    String dateWithoutTime = xmlFmtDt.format(date) + " 00:00:00.1";
    return xmlFmtDtTm.parse(dateWithoutTime);
  }

  public long getStartOfDay(java.util.Date date) {
    return setTimeToZero(date).getTime();
  }

  public long getEndOfDay(java.util.Date date) {
    // return setTimeToZero(date).getTime() + SECONDS_IN_A_DAY - 1;
    String dateWithoutTime = xmlFmtDt.format(date) + " 23:59:59.0";
    return xmlFmtDtTm.parse(dateWithoutTime).getTime();
  }

  public long getMidDay(String dateString) {
    return getDefaultDateTimeFormat().parse(dateString + " 11:59 AM").getTime();
  }

  public Image getCompletedImage(boolean isCompleted) {
    Image img;
    if (isCompleted) {
      img = new Image(RegistryResources.INSTANCE.accept());
      img.setTitle("Survey has been completed");
    } else {
      img = new Image(RegistryResources.INSTANCE.decline());
      img.setTitle("Survey has not been completed");
    }
    // img.setStylePrimaryName("imageButton");
    return img;
  }

  public static boolean validString(RegExp pattern, String testString) {
    // MatchResult matcher = patientIdPattern.exec(testString);
    return pattern.test(testString);
  }

  public boolean isValidPatientId(String patientId) {

    // Check that it fits the pattern
    return validString(patientIdPattern, patientId);
  }

  public static java.util.Date addDays(java.util.Date dt, int days) {
    long time = dt.getTime() + (days * SECONDS_IN_A_DAY);
    return new java.util.Date(time);
  }

  public CustomTextBox makeRequiredField(String value) {
    FormData formData = new FormData();
    formData.setRequired(true);

    CustomTextBox ctb = new CustomTextBox(formData);
    ctb.setValue(value);
    return ctb;
  }

  public Label makeLabel(String str) {
    Label lbl = new Label();
    lbl.setText(str);
    return lbl;
  }

  public ValidEmailAddress makeEmailField(String value, InvalidEmailHandler handler) {
    ValidEmailAddress email = new ValidEmailAddress(this, false);
    email.setInvalidStyleName("dataListTextError");
    email.setValidStyleName("dataListTextColumn");
    email.addInvalidEmailHandler(handler);
    if (value != null) {
      email.setValue(value);
    }
    email.setRequired(false);

    return email;
  }

  /**
   * Use getChartUrl(), not this, so your URL gets the siteId
   */
  String getChartBaseUrl() {
    return getParam("chart.url", CHART_URL);
  }

  /**
   * The first pairs ("a","b","c","d") are added: & a = b & c = d
   * <br>If you want to blur some together, you can, like: "a=b&c", "d"
   * <br>If there's an odd last one, that's added after an ampersand- you must add your own =, etc.
   * @param params Add NO leading '?' or '&' or '=' !
   * @return the URL, with &?siteId=siteName as the first parameter
   */
  public String getChartUrl(String...params) {
    StringBuilder sb = new StringBuilder(100);
    sb.append(getChartBaseUrl());
    // add the ?siteId=siteUrl parameter
    String siteUrl = RegistryRpcRequestBuilder.getSiteName();
    sb.append('?').append(Constants.SITE_ID).append('=').append(siteUrl);
    int i = 0;
    int end = params.length & 0xFFFE;  // truncate last param
    for (;  i < end;  i+=2)
      sb.append("&").append(params[i]).append('=').append(params[i+1]);

    // append any final one.
    if (i < params.length)
      sb.append("&").append(params[i]);  // a whole string of height=1&width=2&etc

    return sb.toString();
  }

  public Popup makePopup(String headerText) {
    Label headerLabel = new Label(headerText);
    headerLabel.addStyleName(RegistryResources.INSTANCE.css().popupBox());
    headerLabel.addStyleName(RegistryResources.INSTANCE.css().heading());
    Popup popup = new Popup(headerLabel);
    popup.getWidget().addStyleName(RegistryResources.INSTANCE.css().popupBox());
    return popup;
  }


  public int getAge(Date dob) {
    int age = 0;
    if (dob == null) {
      return age;
    }
    Date now = new Date();
    try {
      /* Start with age: thisYear - yearBorn */
      age = Integer.parseInt(yyyyFmt.format(now)) - Integer.parseInt(yyyyFmt.format(dob));

      /* Subtract a year if the birth month is after this month */
      int thisMonth = Integer.parseInt(mmFmt.format(now));
      int dobMonth = Integer.parseInt(mmFmt.format(dob));
      if ( dobMonth > thisMonth) {
        age = age - 1;
      } else if (dobMonth == thisMonth) { /* If same month */
        /* Subtract a year if the birthday day is after today */
        if (Integer.parseInt(mmFmt.format(dob)) > Integer.parseInt(ddFmt.format(now))) {
          age = age - 1;
        }
      }
    } catch (NumberFormatException ex) {
      // ignore
    }
    return age;
  }


  /**
   * Update the email attribute of the patient object. This method does not
   * call the server to update the database.
   */
  public void setEmail(Patient patient, String emailAddr) {
    if (emailAddr != null) {
      emailAddr = emailAddr.trim();
      if (emailAddr.isEmpty()) {
        emailAddr = null;
      }
    }

    if (!Objects.equals(emailAddr, patient.getEmailAddress())) {
      if (isEmpty(emailAddr)) {
        patient.removeAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_ALT);
      } else if (patient.hasAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_ALT)) {
        patient.getAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_ALT).setDataValue(emailAddr);
      } else {
        PatientAttribute pattribute = new PatientAttribute(patient.getPatientId(), Constants.ATTRIBUTE_SURVEYEMAIL_ALT,
            emailAddr, PatientAttribute.STRING);
        patient.addAttribute(pattribute);
      }
      // Reset the survey email address valid flag
      patient.removeAttribute(Constants.ATTRIBUTE_SURVEYEMAIL_VALID);
    }
  }
}
