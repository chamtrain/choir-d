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
public class Gender {

  private final static Map<String, String> map = Stream.of(new String[][] {
      { "F", "Female" },
      { "M", "Male" },
      { "O", "Other" },
      { "U", "Unknown " },
      { "A", "Ambiguous" },
      { "N", "Not applicable" }
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

  static String getGenderDescription(String code) {
    return map.get(code);
  }

}
