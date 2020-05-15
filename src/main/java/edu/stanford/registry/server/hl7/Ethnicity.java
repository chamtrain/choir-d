/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.hl7;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for converting 'Ethnicity' hl7 code to description
 * @author tpacht@stanford.edu
 * @since 09/2019
 */
public  class Ethnicity {

  private static final List<String> ethnicities = Arrays.asList(
      "Unknown", "Non-Hispanic/Non-Latino", "Hispanic/Latino", "Unknown", "Patient Refused");

  private static String getEthnicityDescription(int code) {
    if (code < ethnicities.size()) {
      return ethnicities.get(code);
    }
    return ethnicities.get(0);
  }

  static String getEthnicityDescription(String code) {
    try {
      return getEthnicityDescription(Integer.parseInt(code));
    } catch (NumberFormatException nfe) {
      return ethnicities.get(0);
    }
  }
}



