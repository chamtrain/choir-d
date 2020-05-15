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

import java.util.Comparator;

import com.google.gwt.xml.client.Element;

/*
 *  For client side DOM processing
 */
public class PROMISItemElementComparator<T> extends edu.stanford.registry.shared.comparator.PROMISItemElementComparator
    implements Comparator<T> {

  public PROMISItemElementComparator() {

  }

  public PROMISItemElementComparator(int sortOption, int direction) {
    setSort(sortOption);
    setDirection(direction);

  }

  @Override
  public int compare(T o1, T o2) {
    if (o1 == null || o2 == null) {
      return 0;
    }
    Element element1 = (Element) o1;
    Element element2 = (Element) o2;

    switch (getSort()) {
    case SORT_BY_POSITION:
      return byIntValue(element1.getAttribute("Position"), element2.getAttribute("Position"));
    case SORT_BY_ORDER:
      return byIntValue(element1.getAttribute("Order"), element2.getAttribute("Order"));
    default:
      return byDoubleValue(element1.getAttribute("StdError"), element2.getAttribute("StdError"));
    }

  }

}
