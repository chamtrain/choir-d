/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.hl7.provider;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.util.Terser;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Provider {

  private enum ID_TYPES { MSO_TYPE, SID_TYPE }
  private static final Logger logger = LoggerFactory.getLogger(Provider.class);

  protected String providerId = null;
  protected String mso = null;

  public String getProviderEId() {
    return this.providerId;
  }

  @SuppressWarnings("unused")
  public String getMso() {
    return this.mso;
  }

  protected void setFields(Terser terser,
                            String idLocation,
                            String providerIdLocation,
                            List<String> alternateProviderIdLocations) throws HL7Exception {
    // Using HL7 parsing to try to distinguish between SID and MSO in the first PV1 field.
    String id = terser.get(idLocation);
    if (isMso(id)) {
      this.mso = id;
      this.providerId = findEId(terser, providerIdLocation, alternateProviderIdLocations, ID_TYPES.SID_TYPE);

    } else if (isSid(id)) {
      this.providerId = id;
      this.mso = findEId(terser, providerIdLocation, alternateProviderIdLocations, ID_TYPES.MSO_TYPE);
    }
  }

  private static String findEId(Terser terser,
                               String location,
                               List<String> alternateLocations,
                               ID_TYPES idType) throws HL7Exception {

    // Try first location
    String primary = terser.get(location);
    if (checkType(primary, idType)) {
      return primary;
    }

    // First one didn't work.  Try alternates if any exist
    if (alternateLocations != null) {

      for (String altLocation: alternateLocations) {
        String alternate = terser.get(altLocation);
        if (checkType(alternate, idType)) {
          return alternate;
        }
      }
    }

    return null;  // No luck - didn't find one
  }

  private static boolean checkType(String string, ID_TYPES idType) {

    if (idType.equals(ID_TYPES.MSO_TYPE) && isMso(string)) {
      return true;
    } else return idType.equals(ID_TYPES.SID_TYPE) && isSid(string);

  }

  /**
   * Check if there is provider information at the specified terser location path in an HL7 message
   * @param terser The Terser object to use
   * @param location The location string of where to look for the provider id
   * @return true if there is provider information at the specified location
   */
  public static boolean findProvider(Terser terser,
                                        String location) {

    try {
      String string = terser.get(location);
      return (isMso(string) || isSid(string));
    } catch (HL7Exception exc) {
      // May happen if there is no segment
      logger.warn("Provider not found in location {}", location);
    }

    return false;
  }

  /**
   * Checks if the string is an MSO - a 6 digit integer value
   * @param string String value to be checked
   * @return true if string contains a value that looks like an MSO
   */
  protected static boolean isMso(String string) {
    boolean realMso = false;
    try {
      if (string != null && string.length() == 6) {
        Integer.parseInt(string);
        realMso = true;
      }
    } catch (Exception exc) {
      // Do nothing - this is a bad MSO, so we'll fall out with false
    }

    return realMso;
  }

  /**
   * Check if string looks like a SID - Should be a letter followed by an integer value
   * @param string String value to be checked
   * @return true if the string looks to be a SID
   */
  protected static boolean isSid(String string) {
    boolean realSid = false;

    try {
      if (string != null && string.length() > 6 && Character.isLetter(string.charAt(0))) {
        Integer.parseInt(string.substring(1));
        realSid = true;
      }
    } catch (Exception exc) {
      // Do nothing - this is not a SID, so we'll fall out with false
    }

    return realSid;
  }
}
