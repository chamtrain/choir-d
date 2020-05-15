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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for converting 'Race' hl7 code to description
 * @author tpacht@stanford.edu
 * @since 09/2019
 */
public class Race {

  private static final Map<String, String> map = Stream.of(new String[][] {
      { "A", "Native Hawaiian or Other Pacific Islander" }, // aslo "Asian - Historical Conv"
      { "B", "Black or African American"},
      { "E", "Asian, non-Hispanic"},
      { "H", "Hispanic - Historical Conv"},
      { "I",  "Native American, non-Hispanic"},
      { "N", "American Indian or Alaska Native"},
      { "O", "Other"}, //  also "Other, non-Hispanic"},
      { "P", "Pacific Islander, non-Hispanic"},
      { "PR", "Patient Refused"},
      { "S", "Asian"},
      { "U", "Unknown"}, // also "Race and Ethnicity Unknown"
      { "W", "White"}, // also "White, non-Hispanic"
      { "yes", "Black, non-Hispanic"},
      { "1", "White, Hispanic"},
      { "2", "Black, Hispanic"},
      { "3", "Native American, Hispanic"},
      { "5", "Other, Hispanic"},
      { "6", "Asian, Hispanic"},
      { "7", "Pacific Islander, Hispanic"}
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public static String getRaceDescription(String code) {
      return map.get(code);
    }

}
