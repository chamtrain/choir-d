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

import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.SurveyQuestionIntf;

import java.util.Comparator;

/**
 * For server side DOM processing
 *
 * @author tpacht
 */
public class SurveyQuestionComparator<T>
    implements Comparator<T> {

  public final static int SORT_BY_ORDER_ASCENDING = 0;
  public final static int SORT_BY_ORDER_DESCENDING = 1;
  private int sortBy = SORT_BY_ORDER_ASCENDING;

  public void setSort(int sortOption) {
    sortBy = sortOption;
  }

  public int getSort() {
    return sortBy;
  }

  @Override
  public int compare(T o1, T o2) {
    SurveyQuestionIntf q1 = (SurveyQuestionIntf) o1;
    SurveyQuestionIntf q2 = (SurveyQuestionIntf) o2;

    // First sort by position, if possible, because PROMIS version 2 API provides
    // correct position information
    try {
      int q1Order = Integer.parseInt(q1.getAttribute(Constants.ORDER));
      int q2Order = Integer.parseInt(q2.getAttribute(Constants.ORDER));
      if (sortBy == SORT_BY_ORDER_ASCENDING) {
        if (q1Order  > q2Order) {
          return 1;
        }
        if (q1Order < q2Order) {
          return -1;
        }
        return 0;
      } else {
        if (q1Order  > q2Order) {
          return -1;
        }
        if (q1Order < q2Order) {
          return 1;
        }
        return 0;
      }

    } catch (Exception ex) {
      return 0;
    }

  }
}
