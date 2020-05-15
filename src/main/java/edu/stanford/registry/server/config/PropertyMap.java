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

import java.util.Enumeration;
import java.util.HashMap;

/**
 * The purpose of this is to have a read-only string->string interface
 * that can handle a variety of sources, including a request which has an
 * Enumeration instead of an iterator.
 *
 * @See PropertyMapFromHash
 * @See PropertyMapFromServletContext
 */
public interface PropertyMap {

  String getString(String key);

  default String getString(String key, String dflt) {
    String value = getString(key);
    return (value == null) ? dflt : value;
  }

  default String getTrimmedString(String key, String dflt) {
    String value = getString(key);
    return (value == null) ? dflt : value.trim();
  }

  Enumeration<String> getKeys();

  int size();

  /**
   * @return A COPY of mappings, as a HashMap.
   */
  default HashMap<String,String> getMap() {
    Enumeration<String> enumer = getKeys();
    HashMap<String,String> map = new HashMap<>();
    while (enumer.hasMoreElements()) {
      String key = enumer.nextElement();
      map.put(key, getString(key));
    }
    return map;
  }

  /**
   * @param propertyName The property to fetch
   * @param dflt the value to return if the property isn't set or isn't "true" or "false" (ignoring case)
   * @return true if value is "true" (ignoring case), false if value is "false", else dflt
   */
  default boolean getProperty(String propertyName, boolean dflt) {
    return getBool(getString(propertyName), dflt);
  }

  /**
   * This can be used instead of Boolean.parseBoolean(), if you pass dflt=false
   *
   * @param value the string to interpret as a boolean
   * @param dflt The value to use if value is null, empty or not one of y,n,true,false (ignoring case)
   * @return true if value is "true" (ignoring case), false if value is "false", else dflt
   */
  public static boolean getBool(String value, boolean dflt) {
    if ("true".equalsIgnoreCase(value)) {
      return true;
    } else if ("false".equalsIgnoreCase(value)) {
      return false;
    }
    return dflt;
  }
}
