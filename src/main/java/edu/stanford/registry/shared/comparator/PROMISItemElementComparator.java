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

public abstract class PROMISItemElementComparator {
  public final static int SORT_BY_STDERR = 0;
  public final static int SORT_BY_POSITION = 1;
  public final static int SORT_BY_ORDER = 2;

  public final static int ASCENDING = 0;
  public final static int DESCENDING = 1;

  // default is to sort by stdErr desc
  private int sortBy = SORT_BY_STDERR;

  private int greater = -1; // default to DESCENDING
  private int smaller = 1;

  public void setSort(int sortOption) {
    sortBy = sortOption;
  }

  public int getSort() {
    return sortBy;
  }

  public void setDirection(int direction) {
    if (direction == ASCENDING) {
      greater = 1;
      smaller = -1;
    } else {
      greater = -1;
      smaller = 1;
    }
  }

  public int byDoubleValue(String dblString1, String dblString2) {
    if (dblString1 == null || dblString2 == null) {
      return 0;
    }
    try {
      Double dbl1 = Double.valueOf(dblString1);
      Double dbl2 = Double.valueOf(dblString2);
      if (dbl1 > dbl2) {
        return greater;
      }
      if (dbl1 < dbl2) {
        return smaller;
      }
      return 0;
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  public int byIntValue(String intString1, String intString2) {

    if (intString1 == null || intString2 == null) {
      return 0;
    }

    try {
      int int1 = Integer.parseInt(intString1);
      int int2 = Integer.parseInt(intString2);
      if (int1 > int2) {
        return greater;
      }
      if (int1 < int2) {
        return smaller;
      }
      return 0;
    } catch (NumberFormatException nfe) {
      return 0;
    }

  }
}
