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
import java.util.Iterator;
import java.util.Map;

public class PropertyMapFromHash implements PropertyMap {
  private final Map<String,String> map;

  public PropertyMapFromHash(Map<String,String> map) {
    this.map = (map == null) ? new HashMap<String,String>(0) : map;
  }

  @Override
  public String getString(String key) {
    return map.get(key);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Enumeration<String> getKeys() {
    return new IteratorEnumeration(map.keySet().iterator());
  }

  static public class IteratorEnumeration implements Enumeration<String> {
    Iterator<String> iterator;

    IteratorEnumeration(Iterator<String> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasMoreElements() {
      return iterator.hasNext();
    }

    @Override
    public String nextElement() {
      return iterator.next();
    }
  }
}
