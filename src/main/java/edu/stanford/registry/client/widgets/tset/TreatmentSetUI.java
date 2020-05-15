/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.widgets.tset;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import edu.stanford.registry.client.clinictabs.TabWidget;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.service.PhysicianServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.Menu;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.RandomSetParticipant;

/**
 * This handles the banner that tells about a treatment set,
 * plus has methods to add treatment sets to the Enter Patient Data popup.
 *
 * Call:<br>
 * tsetUI = new TreatmentSetUI(tabParent, ... patient); // inserts the UI here, on the page, if there are assignments
 * <br>..<br>
 * tsetUI.addToList(ListBox enterPatientDataList);  // adds treatment set(s), if warranted
 *
 * @author rstr
 */
public class TreatmentSetUI {
  private final Model model;


  /**
   * This offers a TreatmentBar with buttons to bring up a ModifyParticipationWindow.
   * And it can call a method to bring up the window from the EnterPatientData menu
   * and remove the menu afterwards.
   *
   * It also can export a listbox to show the participation instead of the bar.
   */
  public TreatmentSetUI(TabWidget tabParent, ClinicServiceAsync csvcs, PhysicianServiceAsync psvcs, ClinicUtils utils) {
    model = new Model(tabParent, csvcs, psvcs, utils);
  }


  /**
   * Returns a larger height for the patientHeader if the site is enabled
   */
  public double enlarge(double defaultHt) {
    return defaultHt + (model.siteEnabled ? 2.5 : 0);
  }


  /**
   * Adds a rowPanel (which might be hidden if no set is assigned to patient, and the site config allows)
   */
  public void addTreatmentBar(Patient pat, Panel panel) {
    model.createTreatmentBar();
    setPatient(pat);
    panel.add(model.treatmentBar.getBarPanel());
  }


  /**
   * Returns Unset options to add to EnterPatientData menu
   */
  public ArrayList<RandomSetParticipant> getNonSurveyTreatmentSetOptions() {
    return model.siteEnabled ? model.getUnsetOptions() : new ArrayList<RandomSetParticipant>(0);
  }


  /**
   * Refills the UI elements with new Participant data for the new patient
   */
  public void setPatient(Patient pat) {
    if (!model.siteEnabled) {
      return; // do nothing if site doesn't have treatment sets enabled
    }
    model.setPatient(pat);
    model.svcs.getRandomSets(pat.getPatientId(), new AsyncCallback<ArrayList<RandomSetParticipant>>() {
      @Override public void onFailure(Throwable caught) {
        model.setList(null);
      }
      @Override public void onSuccess(ArrayList<RandomSetParticipant> result) {
        model.setList(result);
        if (model.treatmentBar != null) {
          model.treatmentBar.modelWasUpdated();
        }
      }
    });
  }


  /**
   * EnterPatientData callback that puts up a UI window to register a patient
   * for a non-survey TreatmentSet button, so patient can receive/change state
   */
  public void assignTreatmentSet(Menu menu, String tsetName) {
    model.enterPatientDataMenu = menu;

    RandomSetParticipant participant = model.get(tsetName);
    if (participant == null) {
      return;
    }
    ModifyParticipationWindow window = new ModifyParticipationWindow(model);
    window.showAssignmentPopupWindow(participant);
  }


  /**
   * Returns true if the site has any open treatment sets.
   */
  public boolean isEnabled() {
    return model.siteEnabled;
  }

}