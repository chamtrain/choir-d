/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.shared.PatientRegistration;
import edu.stanford.registry.shared.PatientRegistrationSearch;
import edu.stanford.registry.shared.PatientRegistrationSearch.SortBy;

import java.util.Comparator;

public class PatientRegistrationComparator<T> implements Comparator<T> {


  private static final int ASC = 1;
  private static final int DES = -1;
  private final int sortDirection;
  public PatientRegistrationSearch.SortBy sortBy = SortBy.apptTime;

  public PatientRegistrationComparator() {
    // Default is sort by date
    this(SortBy.apptTime, true);
  }

  public PatientRegistrationComparator(PatientRegistrationSearch.SortBy sortBy, boolean asc) {
    this.sortBy = sortBy;
    this.sortDirection = asc ? ASC : DES;
  }

  @Override
  public int compare(T o1, T o2) {

    if (o1 == null || o2 == null) {
      return 0;
    }
    return sortDirection * internalCompare((PatientRegistration) o1, (PatientRegistration) o2);
  }

  private int internalCompare(PatientRegistration registration1, PatientRegistration registration2) {

    switch (sortBy) {
    case mrn:
      return byMrn(registration1, registration2);
    case lastName:
      return byLastName(registration1, registration2);
    case firstName:
      return byFirstName(registration1, registration2);
    case apptType:
      return byApptType(registration1, registration2);
    case surveyType:
      return bySurveyType(registration1, registration2);
    default:
      return byApptTime(registration1, registration2);
    }

  }

  private int byMrn(PatientRegistration registration1, PatientRegistration registration2) {

    if (registration1.getPatient().getPatientId() == null || registration2.getPatient().getPatientId() == null) {
      return 0;
    }
    return registration1.getPatient().getPatientId().compareTo(registration2.getPatient().getPatientId());
  }

  private int byLastName(PatientRegistration registration1, PatientRegistration registration2) {

    if (registration1.getPatient().getLastName() == null || registration2.getPatient().getLastName() == null) {
      return 0;
    }
    return registration1.getPatient().getLastName().compareTo(registration2.getPatient().getLastName());
  }

  private int byFirstName(PatientRegistration registration1, PatientRegistration registration2) {

    if (registration1.getPatient().getFirstName() == null || registration2.getPatient().getFirstName() == null) {
      return 0;
    }
    return registration1.getPatient().getFirstName().compareTo(registration2.getPatient().getFirstName());
  }

  private int byApptType(PatientRegistration registration1, PatientRegistration registration2) {

    if (registration1.getVisitType() == null || registration2.getVisitType() == null) {
      return 0;
    }
    return registration1.getVisitType().compareTo(registration2.getVisitType());
  }


  private int bySurveyType(PatientRegistration registration1, PatientRegistration registration2) {
    if (registration1.getSurveyType() == null || registration2.getSurveyType() == null) {
      return 0;
    }

    return registration1.getSurveyType().compareTo(registration2.getSurveyType());
  }

  private int byApptTime(PatientRegistration registration1, PatientRegistration registration2) {
    if (registration1.getSurveyDt() == null || registration2.getSurveyDt() == null) {
      return 0;
    }
    return registration1.getSurveyDt().compareTo(registration2.getSurveyDt());
  }
}
