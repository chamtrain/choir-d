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

import java.util.Comparator;

import org.w3c.dom.Element;

/**
 * For server side DOM processing
 *
 * @author tpacht
 */
public class PROMISItemElementComparator<T> extends edu.stanford.registry.shared.comparator.PROMISItemElementComparator
    implements Comparator<T> {
  @Override
  public int compare(T o1, T o2) {
    Element element1 = (Element) o1;
    Element element2 = (Element) o2;

    int result = 0;

    // First sort by position, if possible, because PROMIS version 2 API provides
    // correct position information
    String position1 = element1.getAttribute("Position");
    String position2 = element2.getAttribute("Position");
    if (position1 != null && position1.length() > 0 && position2 != null && position2.length() > 0) {
      setDirection(ASCENDING);
      result = byIntValue(position1, position2);
    }

    // PROMIS version 1 API does not provide correct position information (usually
    // sets all position attributes to "1"), so fallback to using decreasing SE per
    // Northwestern recommendation
    if (result == 0) {
      setDirection(DESCENDING);
      result = byDoubleValue(element1.getAttribute("StdError"), element2.getAttribute("StdError"));
    }

    return result;
  }
}
