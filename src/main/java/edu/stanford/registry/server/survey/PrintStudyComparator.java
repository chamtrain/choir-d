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

package edu.stanford.registry.server.survey;

import java.util.Comparator;

public class PrintStudyComparator implements Comparator<PrintStudy> {

  @Override
  public int compare(PrintStudy ps1, PrintStudy ps2) {
    if (ps1 == null || ps2 == null) {
      return 0;
    }

    if (ps1.getPrintOrder() > ps2.getPrintOrder()) {
      return 1;
    }
    if (ps1.getPrintOrder() < ps2.getPrintOrder()) {
      return -1;
    }

    // Print order is the same, compare sub-order
    if (ps1.getPrintOrderSub() > ps2.getPrintOrderSub()) {
      return 1;
    }
    if (ps1.getPrintOrderSub() < ps2.getPrintOrderSub()) {
      return -1;
    }
    return 0;
  }

}
