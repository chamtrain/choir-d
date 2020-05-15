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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Move this to the client code only, out of shared
 *
 * This contains date and patientId utilities for a single site, so it's no longer
 * used as a superclass of ServerUtils.
 *
 * When one of these is made, the current configuration parameters for a site
 * create the underlying data.
 */
public class CommonUtils {

  public static final String REGEX_EMAIL = "^[\\w\\.-]*[a-zA-Z0-9]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
  public static final long SECONDS_IN_A_DAY = 24 * 3600 * 1000;

  private StringBuffer logMessages;
  //private static CommonUtils instance = null;
  public String dateFormatStr = null;
  public String dateTimeFormatStr = null;

  //protected HashMap<String, String> params = null;

  String patientIdError = null;
  public String patternString = null;
  public static final String DTFMT = "MM/dd/yyyy h:mm a";
  public static final String DFMT = "MM/dd/yyyy";

  public static final String[] EMAIL_TEMPLATE_VARIABLE_NAMES = { "[SURVEY_LINK]", "[SURVEY_DATE]",
      "[PATIENT_EMAIL_ADDRESS]", "[PATIENT_NAME]", "[TOKEN]" };
  public static final String[] EMAIL_TEMPLATE_VARIABLE_TYPES = { "STRING", "DATE" };

  public CommonUtils(HashMap<String, String> initParameters) {
    initialize(initParameters);
  }

  /**
   * For a client, the client config is passed in, so it'll get the site-specific configuration
   * @param clientParams
   */
  private void initialize(Map<String, String> clientParams) {

    logMessages = new StringBuffer();

    dateTimeFormatStr = clientParams.get("default.dateTimeFormat");
    if (dateTimeFormatStr == null) {
      addLogMessage("Property default.dateTimeFormat is not configured, defaulting to: '" + DTFMT + "'\n");
      dateTimeFormatStr = DTFMT;
    }
    dateFormatStr = clientParams.get("default.dateFormat");
    if (dateFormatStr == null) {
      addLogMessage("Property default.dateFormat is not configured, defaulting to: '" + DFMT + "'.\n");
      dateFormatStr = DFMT;
    }

    patternString = clientParams.get("PatientIdFormat");
    if (patternString == null) {
      patternString = "\\d{5,7}-\\d{1}|\\d{5,9}";
      addLogMessage("Property PatientIdFormat is not configured, defaulting to: '" + patternString + "'.\n");
    }
    patientIdError = clientParams.get("PatientIdFormatErrorMessage");
    if (patientIdError == null) {
      patientIdError = "Patient Id must be 5-7 characters followed by \"-\" and a single digit.";
      addLogMessage("Property PatientIdFormatErrorMessage is not configured, defaulting to: '" + patientIdError + "'.\n");
    }

    if (clientParams.get(Constants.PATIENT_ID_LABEL) == null
        || clientParams.get(Constants.PATIENT_ID_LABEL).isEmpty()) {
      clientParams.put(Constants.PATIENT_ID_LABEL, "MRN"); // set it to the default
    }

    if (clientParams.get(Constants.SCHED_SORT_PARAM) == null
        || clientParams.get(Constants.SCHED_SORT_PARAM).isEmpty()) {
      clientParams.put(Constants.SCHED_SORT_PARAM, Constants.SCHED_SORT_DEFAULT);
    }
    addLogMessage("Utilites have been initialized");
    //instance = this;
  }

  public static Date getNow() {
    return new java.util.Date();
  }

  public String getDateTimeFormatString() {
    return dateTimeFormatStr;
  }

  public String getDateFormatString() {
    return dateFormatStr;
  }

  public static Date dateFromYyyyDashMmDashDd(String yyyyDashMmDashDd) {
    return new Date(java.sql.Date.valueOf(yyyyDashMmDashDd).getTime());
  }

  public static String dateToYyyyDashMmDashDd(Date date) {
    return new java.sql.Date(date.getTime()).toString();
  }

  public String getInitializationLogMessages() {
    return logMessages.toString();
  }

  /*public static synchronized CommonUtils getInstance() throws DataException {
    if (instance == null) {
      throw new DataException("Not initialized");
    }
    return instance;
  } */

  public boolean isValidEmail(String email) {
    return email.matches(REGEX_EMAIL);
  }

  public String getPatientIdFormatError() {
    return blankIfNull(patientIdError);
  }

  public static String blankIfNull(String str) {
    if (str == null) {
      return "";
    }
    return str;
  }

  public static boolean isEmpty(String str) {
    if (str == null) {
      return true;
    }
    if (str.trim().length() < 1) {
      return true;
    }
    return false;
  }

  public void addLogMessage(String str) {
    if (logMessages == null) {
      logMessages = new StringBuffer();
    }
    logMessages.append(str);
  }

  public String getLogMessage() {
    String msg = logMessages.toString();
    logMessages = new StringBuffer();
    return msg;
  }

  public void addParameters(HashMap<String, String> initParameters, String realPath) {
    // override this method to add parameters for the container being
    // initialized
  }

  @SuppressWarnings("unused")
  private static String replace(String text, String variable, String value) {
    int inx = text.indexOf(variable);

    while (inx > -1) {
      StringBuilder sbuf = new StringBuilder();
      sbuf.append(text.subSequence(0, inx));
      sbuf.append(value);
      if (text.length() > inx + value.length()) {
        sbuf.append(text.substring(inx + variable.length()));
      }
      text = sbuf.toString();
      inx = text.indexOf(variable);
    }
    return text;
  }


  /**
   * Returns the number of lines in a string, that is, 1 + the number of carriage returns (\\n)
   */
  public int countLines(String s) {
    int n = 1;
    for (int ix = s.indexOf('\n');  ix >= 0;  ix = s.indexOf('\n', ix+1)) {
      n++;
    }
    return n;
  }
}
