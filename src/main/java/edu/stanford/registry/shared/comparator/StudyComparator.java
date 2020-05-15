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

import edu.stanford.registry.shared.Study;

import java.util.Comparator;

public class StudyComparator<T> implements Comparator<T> {
  // private static Logger logger = Logger.getLogger(ProcessAttributeComparator.class.getName());
  public final static int SORT_BY_CODE = 0;
  public final static int SORT_BY_DESCRIPTION = 1;
  public final static int SORT_BY_TITLE = 2;

  public int sortBy = 0;

  public StudyComparator() {
    // Default is sort by date
    this(SORT_BY_CODE);
  }

  public StudyComparator(int sortOption) {
    sortBy = sortOption;
  }

  @Override
  public int compare(T o1, T o2) {

    if (o1 == null || o2 == null) {
      // logger.log(Level.INFO, "one of the compare objects is null");
      return 0;
    }

    Study study1 = (Study) o1;
    Study sc2 = (Study) o2;
    switch (sortBy) {
    case SORT_BY_TITLE:
      return byTitle(study1, sc2);
    case SORT_BY_DESCRIPTION:
      return byDescription(study1, sc2);
    default:
      return byCode(study1, sc2);
    }

  }

  private int byTitle(Study study1, Study study2) {

    if (study1.getTitle() == null || study2.getTitle() == null) {
      return 0;
    }
    return study1.getTitle().compareTo(study2.getTitle());
  }

  private int byDescription(Study study1, Study study2) {

    if (study1.getStudyDescription() == null || study2.getStudyDescription() == null) {
      return 0;
    }

    return study1.getStudyDescription().compareTo(study2.getStudyDescription());
  }

  private int byCode(Study study1, Study study2) {
    return (study1.getStudyCode().compareTo(study2.getStudyCode()));
  }

}
