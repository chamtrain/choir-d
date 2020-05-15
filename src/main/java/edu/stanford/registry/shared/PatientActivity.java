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

package edu.stanford.registry.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PatientActivity implements IsSerializable {

  /**
   * This is a combination object containing a "Patient" and "Registration"
   * along with a list of "Activity" objects for that registration and its
   * assessments.
   */
  public PatientActivity() {
  }

  public PatientActivity(Patient pat) {
    setPatient(pat);
  }

  private Patient pat = null;
  private ApptRegistration appt = null;
  private ArrayList<Activity> activityList = new ArrayList<>();

  public Patient getPatient() {
    return pat;
  }

  public void setPatient(Patient patient) {
    pat = patient;
  }

  public ApptRegistration getRegistration() {
    return appt;
  }

  public void setRegistration(ApptRegistration registration) {
    appt = registration;
  }

  public ArrayList<Activity> getActivities() {
    return activityList;
  }

  public void addActivity(Activity act) {
    activityList.add(act);
  }

  public void setActivity(int at, Activity act) {
    activityList.set(at, act);
  }

  public Activity getActivity(int at) {
    return activityList.get(at);
  }

  public int numberActivities() {
    return activityList.size();
  }
}
