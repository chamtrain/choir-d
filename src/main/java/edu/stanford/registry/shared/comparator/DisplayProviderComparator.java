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

import edu.stanford.registry.shared.DisplayProvider;

import java.util.Comparator;

public class DisplayProviderComparator<T> implements Comparator<T> {


  public DisplayProviderComparator() {
  }

  @Override
  public int compare(T o1, T o2) {

    if (o1 == null || o2 == null) {
      return 0;
    }

    DisplayProvider dp1 = (DisplayProvider) o1;
    DisplayProvider dp2 = (DisplayProvider) o2;

    if (dp1.getDisplayName() == null || dp2.getDisplayName() == null) {
      return 0;
    }
    return dp1.getDisplayName().compareTo(dp2.getDisplayName());
  }
}
