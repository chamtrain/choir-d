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

import edu.stanford.registry.shared.PatientActivity;

import java.util.Comparator;
import java.util.Date;

public class ActivityComparator implements Comparator<PatientActivity> {
  public ActivityComparator() {
  }

  @Override
  public int compare(PatientActivity act1, PatientActivity act2) {
    int result;
    if ((act1 == null) && (act2 == null)) {
      result = 0;
    } else if ((act1 == null) && (act2 != null)) {
      result = 1;
    } else if ((act1 != null) && (act2 == null)) {
      result = -1;
    } else {
      String pat1 = act1.getPatient().getPatientId();
      String pat2 = act2.getPatient().getPatientId();
      result = byPatient(pat1, pat2);
      if (result == 0) {
        Date date1 = act1.getRegistration().getSurveyDt();
        Date date2 = act2.getRegistration().getSurveyDt();
        result = byDate(date1, date2);
      }
      return result;
    }
    return result;
  }

  private int byPatient(String pat1, String pat2) {
    if ((pat1 == null) && (pat2 == null)) {
      return 0;
    } else if ((pat1 == null) && (pat2 != null)) {
      return 1;
    } else if ((pat1 != null) && (pat2 == null)) {
      return -1;
    } else {
      return -(pat1.compareToIgnoreCase(pat2));
    }
  }

  private int byDate(Date date1, Date date2) {
    if ((date1 == null) && (date2 == null)) {
      return 0;
    } else if ((date1 == null) && (date2 != null)) {
      return 1;
    } else if ((date1 != null) && (date2 == null)) {
      return -1;
    } else {
      return -(date1.compareTo(date2));
    }
  }

}
