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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.service.formatter.PatientIdFormatIntf;

public class StanfordMrn implements PatientIdFormatIntf {

  private String invalidMessage;

  public StanfordMrn() {
  }

  @Override
  public String format(String patientIdIn) throws NumberFormatException {
    // convert to be the full mrn in the format xxxxxxx-x
    String fullMrn = fixStanfordMRN(patientIdIn);

    // return without the leading 0's
    return getStrippedMrn(fullMrn);
  }

  @Override
  public boolean isValid(String patientIdIn) throws NumberFormatException {
    if (patientIdIn == null || patientIdIn.indexOf("-") < 1) {

      return false;
    }

    /** get fully padded mrn **/
    patientIdIn = fixStanfordMRN(patientIdIn);

    /** take off the checksum they added **/
    String base = getBase(patientIdIn);

    /** generates the checksum **/
    String newId = fixStanfordMRN(base);
    if (patientIdIn.equals(newId)) {
      return true;
    }
    // throw new Exception("Invalid check digit. MRN should be " + newId);
    setInvalidMessage("Invalid check digit. MRN should be " + newId);
    return false;

  }

  private void setInvalidMessage(String messageString) {
    invalidMessage = messageString;
  }

  @Override
  public String getInvalidMessage() {
    return invalidMessage;
  }

  @Override
  public String printFormat(String patientIdIn) {
    return "0000000".substring(0,9-patientIdIn.length()) + patientIdIn;
  }

  private static String fixStanfordMRN(String mrn) throws NumberFormatException {
    if (mrn == null) {
      return null;
    }
    mrn = mrn.trim();
    if (mrn.length() > 9 || (mrn.length() == 9 && !mrn.contains("-"))) {
      throw new NumberFormatException("mrn " + mrn + " not in any recognized format (too many digits)");
    }
    if (mrn.length() == 9) {
      return mrn;
    }
    if (mrn.length() == 8) {
      if (!mrn.contains("-")) {
        // original string was xxxxxxxx, convert to xxxxxxx-x
        return mrn.substring(0, 7) + "-" + mrn.substring(7, 8);
      }
    }
    // if the hyphen is present simply prefix with 0s
    if (mrn.contains("-")) {
      return zeroPrefix(mrn, 9);
    }
    // if we reach this point, we're dealing with a 7 digit or shorter string
    // with no hyphen. this must mean it's a really old MRN with no checkdigit.
    // pad to 7 characters with leading 0s then append a hyphen and a checkdigit
    if (mrn.length() < 7) {
      mrn = zeroPrefix(mrn, 7);
    }
    return mrn + "-" + getStanfordMRNChecksum(mrn);
  }

  private static String zeroPrefix(String mrn, int length) {
    StringBuilder sbuf = new StringBuilder();
    for (int i = mrn.length(); i < length; i++) {
      sbuf.append("0");
    }
    sbuf.append(mrn);
    return sbuf.toString();
  }

  private static String getStanfordMRNChecksum(String mrn) {
    if (mrn == null) {
      return null;
    }
    if (mrn.length() > 7) {
      mrn = mrn.substring(0, 7); // we'll take the checksum of the first 7
      // anyway.
    }
    String val = mrn;
    int mask[] = new int[] { 2, 1, 2, 1, 2, 1, 2 };
    int vals[] = new int[val.length()];
    StringBuilder sumString = new StringBuilder(20);
    for (int j = 0; j < val.length(); j++) {
      vals[j] = Integer.parseInt(val.substring(j, j + 1));
    }
    for (int j = 0; j < val.length(); j++) {
      int sum = vals[j] * mask[j];
      sumString.append(sum); // use string so that the 14=1+4 thing is easy.
    }
    int overall = 0;
    for (int j = 0; j < sumString.length(); j++) {
      overall += Integer.parseInt(sumString.substring(j, j + 1));
    }
    int checksum = (10 - (overall % 10));
    if (checksum == 10) {
      checksum = 0;
    }
    return Integer.toString(checksum);
  }

  private static String getStrippedMrn(String mrn) {
    if (mrn == null) {
      return null;
    }

    boolean nonZeroFound = false;
    int inx = -1;
    while (!nonZeroFound && inx < mrn.length()) {
      inx++;
      if (mrn.charAt(inx) != '0') {
        nonZeroFound = true;
      }
    }
    return mrn.substring(inx);
  }

  private static String getBase(String mrn) {
    if (mrn == null) {
      return null;
    }

    int dashInx = mrn.indexOf("-");
    if (dashInx < 1) {
      return mrn;
    }
    return mrn.substring(0, dashInx);
  }
}
