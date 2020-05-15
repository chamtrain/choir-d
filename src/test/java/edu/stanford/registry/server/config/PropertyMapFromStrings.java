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

package edu.stanford.registry.server.config;

import java.util.HashMap;

public class PropertyMapFromStrings extends PropertyMapFromHash {

  public PropertyMapFromStrings(String...strings) {
    super(makeHash(strings));
  }

  static private HashMap<String,String> makeHash(String...strings) {
    HashMap<String,String> map = new HashMap<String,String>(strings.length / 2);
    boolean doKey = true;
    String key = null;
    for (String s: strings) {
      if (doKey) {
        key = s;
      } else {
        map.put(key, s);
        key = null;
      }
      doKey = !doKey;
    }
    assert(key == null);
    return map;
  }

}
