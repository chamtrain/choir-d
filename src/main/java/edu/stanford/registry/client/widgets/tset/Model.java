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
import java.util.List;

import edu.stanford.registry.client.clinictabs.TabWidget;
import edu.stanford.registry.client.service.ClinicServiceAsync;
import edu.stanford.registry.client.service.PhysicianServiceAsync;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.Menu;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.RandomSetParticipant;

/**
 * This contains the environment and model for the Treatment Set UI feature.
 *
 * Call:<br>
 * tsetUI = new TreatmentSetUI(tabParent, ... patient); // inserts the UI here, on the page, if there are assignments
 * <br>..<br>
 * tsetUI.addToList(ListBox enterPatientDataList);  // adds treatment set(s), if warranted
 *
 * @author rstr
 */
class Model {
  // === the environment
  final TabWidget tabParent;  // parent for any popup widget
  final ClinicUtils utils;
  final PhysicianServiceAsync physSvcs;
  final ClinicServiceAsync svcs;

  // Each of these has all of MVC
  Patient patient;
  TreatmentBar treatmentBar;
  Menu enterPatientDataMenu;
  final boolean siteEnabled;    // if false, there's never any UI
  final boolean showBarIfEmpty; // if true, show the bar even if empty
  final boolean showFullStates; // if false, show only withdrawn and assigned
  final boolean canSet;

  private ArrayList<RandomSetParticipant> list;

  // app_config parameters
  static private final String PARAM_LIST_NAMES     = "treatmentset.list";
  static private final String PARAM_SHOW_TOCLINIC  = "treatmentset.show.toclinic";
  static private final String PARAM_SHOW_EMPTYBAR  = "treatmentset.show.emptybar";
  static private final String PARAM_SHOW_FULLSTATE = "treatmentset.show.fullstate";

  /**
   * The model for TreatmentSets.  Note this is only available to physicians.
   *
   * This offers the environment, model and top-level UI components for TreatmentSets.
   *
   * Bar with buttons to bring up a ModifyParticipationWindow.
   * And it can call a method to bring up the window from the EnterPatientData menu
   * and remove the menu afterwards.
   *
   *
   */
  Model(TabWidget tabParent, ClinicServiceAsync csvcs, PhysicianServiceAsync psvcs, ClinicUtils utils) {
    this.tabParent = tabParent;
    this.utils = utils;
    this.svcs = csvcs;
    this.physSvcs = psvcs;

    String setlist = utils.getParam(PARAM_LIST_NAMES, "");
    if (setlist.isEmpty()) {
      siteEnabled = canSet = false; // site has not defined, so doesn't use, treatment sets
    } else if (utils.isPhysician() && physSvcs != null) {
      siteEnabled = canSet = true;
    } else {  // for a non-physician
      siteEnabled = utils.paramEquals(PARAM_SHOW_TOCLINIC, "Y", "Y");  // some exist
      canSet = false;
    }
    showBarIfEmpty = siteEnabled && utils.paramEquals(PARAM_SHOW_EMPTYBAR, "N", "Y");
    showFullStates = utils.paramEquals(PARAM_SHOW_FULLSTATE, "N", "Y");

    list = new ArrayList<>();
  }


  /**
   * Returns the list of participant records that have not been set,
   * for the physician's EnterPatientData record.
   */
  ArrayList<RandomSetParticipant> getUnsetOptions() {
    ArrayList<RandomSetParticipant> plist = new ArrayList<>(list.size());
    for (RandomSetParticipant part: list) {
      if (part.getState().isNotSetup()) {
        plist.add(part);
      }
    }
    return plist;
  }


  /**
   * Returns the number of participant records for this patient.
   * This includes both ones that have been assigned and unassigned ones with State "Unset".
   */
  int getListSize() {
    return list.size();
  }


  /**
   * Populates the list of participants (called when the patient changes)
   */
  void setList(ArrayList<RandomSetParticipant> newList) {
    list = (newList == null) ? new ArrayList<RandomSetParticipant>(0) : newList;
  }


  /**
   * Returns the participant record if it exists for the named treatment set.
   * If there is none, return null;
   */
  RandomSetParticipant get(String tsetName) {
    for (RandomSetParticipant part: list) {
      if (part.getName().equals(tsetName))
        return part;
    }
    return null;
  }


  /**
   * Replaces the participant, if there is one for its named treatment set,
   * with the one passed in, presumably from the server.
   */
  void swap(RandomSetParticipant participant) {
    for (RandomSetParticipant part: list) {
      if (part.getName().equals(participant.getName())) {
        list.remove(part);
        break;
      }
    }
    list.add(participant);
  }


  /**
   * Lets someone iterate over the participant records
   */
  List<RandomSetParticipant> getParticipants() {
    return list;
  }


  /**
   * Just sets the patient variable in the model.
   */
  void setPatient(Patient pat) {
    patient = pat;
  }


  void createTreatmentBar() {
    treatmentBar = new TreatmentBar(this);
  }
}
