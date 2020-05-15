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

import edu.stanford.registry.shared.ChartScore;

import java.util.Comparator;

public class ChartScoreComparator<T> implements Comparator<T> {
  public final static int SORT_BY_DATE = 0;
  public final static int SORT_BY_PATIENT_AND_DATE = 1;

  public int sortBy = 0;

  public ChartScoreComparator() {
    // Default is sort by date
    this(SORT_BY_DATE);
  }

  /**
   * Sort by the specified sort option
   */
  public ChartScoreComparator(int sortOption) {
    sortBy = sortOption;
  }

  @Override
  public int compare(T o1, T o2) {
    if (o1 == null || o2 == null) {
      return 0;
    }

    ChartScore score1 = (ChartScore) o1;
    ChartScore score2 = (ChartScore) o2;
    switch (sortBy) {
    case SORT_BY_DATE:
      return byDate(score1, score2);
    case SORT_BY_PATIENT_AND_DATE:
      return byPatientAndDate(score1, score2);
    default:
      return byDate(score1, score2);
    }

  }

  private int byDate(ChartScore cs1, ChartScore cs2) {

    if (cs1.getDate() == null || cs2.getDate() == null) {
      return 0;
    }
    if (cs1.getDate().after(cs2.getDate())) {
      return 1;
    } else if (cs1.getDate().before(cs2.getDate())) {
      return -1;
    }
    return 0;
  }

  private int byPatientAndDate(ChartScore cs1, ChartScore cs2) {
    if (cs1.getPatientId() == null || cs2.getPatientId() == null) {
      return 0;
    }

    if (cs1.getPatientId().compareTo(cs2.getPatientId()) == 0) {
      return byDate(cs1, cs2);
    }

    return cs1.getPatientId().compareTo(cs2.getPatientId());
  }

}
