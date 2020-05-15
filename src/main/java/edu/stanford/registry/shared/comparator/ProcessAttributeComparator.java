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

package edu.stanford.registry.shared.comparator;

import edu.stanford.registry.shared.ProcessAttribute;

import java.util.Comparator;

/**
 * Note, this must return a consistent ordering.
 *
 * A rel B ==> B -rel A
 * <br>A == B  and  A rel C  ==>  B rel C
 * <br>A < B  ==>  NOT (C < A  &&  B < C)
 *
 * A unit test now tests this.
 */
public class ProcessAttributeComparator implements Comparator<ProcessAttribute> {
  //	private static Logger logger = Logger.getLogger(ProcessAttributeComparator.class.getName());
  public final static int SORT_BY_VALUE = 0;
  public final static int SORT_BY_NAME = 1;
  public final static int SORT_BY_INTEGER = 2;
  public final static int SORT_BY_VALUE_EXPIRATION = 3;

  public int sortBy = 0;

  public ProcessAttributeComparator() {
    // Default is sort by date
    this(SORT_BY_VALUE_EXPIRATION);
  }

  public ProcessAttributeComparator(int sortOption) {
    sortBy = sortOption;
  }

  @Override
  public int compare(ProcessAttribute pa1, ProcessAttribute pa2) {

    if (pa1 == null || pa2 == null) {
      if (pa1 == null && pa2 == null)
        return 0;

      return (pa1 == null) ? -1 : 1;  // null less than non-null
    }

    switch (sortBy) {
    case SORT_BY_NAME:
      return byName(pa1, pa2);
    case SORT_BY_VALUE_EXPIRATION:
      return byValueExpiration(pa1, pa2);
    default:
      return byValue(pa1, pa2);
    }

  }

  private int byName(ProcessAttribute pa1, ProcessAttribute pa2) {
    if (pa1.getName() == null || pa2.getName() == null) {
      if (pa1.getName() == null && pa2.getName() == null)
        return 0;

      return (pa1.getName() == null) ? -1 : 1;
    }

    return pa1.getName().compareTo(pa2.getName());
  }

  private int byValue(ProcessAttribute pa1, ProcessAttribute pa2) {

    // null is smaller than something, but equal to another null
    if (pa1.getValue() == null || pa2.getValue() == null) {
      if (pa1.getValue() == null && pa2.getValue() == null)
        return 0;

      return (pa1.getValue() == null) ? -1 : 1;
    }

    // The easy way is to ALWAYS make a string out of an integer, and always use string ordering.
    String s1 = pa1.toString();
    String s2 = pa2.toString();
    int diff = s1.compareTo(s2);  // negative if s1 before s2
    return diff;
  }

  private int byValueExpiration(ProcessAttribute pa1, ProcessAttribute pa2) {

    // null is smaller than something, but equal to another null
    if (pa1.getValue() == null || pa2.getValue() == null) {
      if (pa1.getValue() == null && pa2.getValue() == null)
        return 0;

      return (pa1.getValue() == null) ? -1 : 1;
    }

    // The easy way is to ALWAYS make a string out of an integer, and always use string ordering.
    String s1 = pa1.getValue().toString();
    String s2 = pa2.getValue().toString();
    int diff = s1.compareTo(s2);  // negative if s1 before s2
    if (diff != 0)
      return diff;

    // I think string attributes always have null dates, but I don't think it matters...
    if (pa1.getEndDate() == null || pa2.getEndDate() == null) {
      if (pa1.getEndDate() == null && pa2.getEndDate() == null)
        return 0;
      return (pa2.getEndDate() == null) ? 1 : -1; // null < non-null, return >0 if smaller
    }

    diff = pa1.getEndDate().compareTo(pa2.getEndDate());
    return diff;  // could compare start dates at this point...
  }
}
