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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Panel;
import edu.stanford.registry.shared.RandomSetParticipant;

/**
 * This handles the visual bar that tells a patient's treatment set assignments.
 * It contains a list of the buttons that bring up the ModifyParticipationPopup.
 *
 * Call:<br>
 * tsetUI = new TreatmentSetUI(tabParent, ... patient); // inserts the UI here, on the page, if there are assignments
 * <br>..<br>
 * tsetUI.addToList(ListBox enterPatientDataList);  // adds treatment set(s), if warranted
 *
 * @author rstr
 */
/**
 * View and Update logic for the button bar
 */
class TreatmentBar {
  final private Model model;
  final private FlowPanel rowPanel;
  private ArrayList<TreatmentButton> showParts;

  TreatmentBar(Model model) {
    this.model = model;
    rowPanel = new FlowPanel();
    rowPanel.setStyleName("treatmentBar");
  }


  void modelWasUpdated() {
    if (showParts == null) {
      showParts = new ArrayList<>(model.getListSize());
    } else {
      showParts.clear();
    }
    for (RandomSetParticipant part: model.getParticipants()) {
      if (!part.getState().isNotSetup()) {
        showParts.add(new TreatmentButton(model, rowPanel, part));
      }
    }
    display();
  }


  private void addLabel() {
    InlineHTML label = new InlineHTML("Treatment Set: ");
    rowPanel.add(label);
  }


  Panel getBarPanel() {
    return rowPanel;
  }


  private void display() {
    sortShownParticipants();
    rowPanel.clear();
    addLabel();
    for (TreatmentButton tb: showParts) {
      rowPanel.add(tb.button);
    }
    if (showParts.size() == 0 && model.showBarIfEmpty) {
      rowPanel.setHeight("2em");
    }
    boolean isVisible = (showParts.size() > 0) || model.showBarIfEmpty;
    rowPanel.setVisible(isVisible);
  }


  private Date maxDate(Date d, Date e) {
    if (d == null) {
      return (e == null) ? new Date() : e;
    } else if (e == null) {
      return d;
    }
    return d.before(e) ? d : e;
  }


  public void updateParticipant(RandomSetParticipant p) {
    for (TreatmentButton btn: showParts) {
      if (btn.isForParticipant(p)) {
        btn.updateParticipant(p);
        return;
      }
    }
    showParts.add(new TreatmentButton(model, rowPanel, p));
    display();
  }


  private void sortShownParticipants() {
    Collections.sort(showParts, /*);
      showParts.sort( */ new Comparator<TreatmentButton>() {
      @Override public int compare(TreatmentButton tb1, TreatmentButton tb2) {
        Date date1 = maxDate(tb1.participant.getAssignedDate(), tb1.participant.getWithdrawnDate());
        Date date2 = maxDate(tb2.participant.getAssignedDate(), tb2.participant.getWithdrawnDate());
        return (date2.after(date1)) ? 1 : -1;  // TODO: check this
      }
    });
  }



  /**
   * This object is a button to show a TreatmentSet participant info and lets user change the state.
   */
  static class TreatmentButton implements ClickHandler {
    private final Model ui;
    private RandomSetParticipant participant;
    private final Button button;


    TreatmentButton(Model model, Panel parent, RandomSetParticipant part) {
      this.ui = model;
      participant = part;
      button = new Button();
      updateParticipant(part);
      if (model.canSet) {
        button.addClickHandler(this);
      } else {
        button.setEnabled(false);
      }
      parent.add(button);
    }


    void updateParticipant(RandomSetParticipant part) {
      participant = part;
      String s = part.getValue().replaceAll("TreatmentSet", "");
      s = s.isEmpty() ? part.getName() : (part.getName() + ": " + part.getValue());
      button.setText(s);
      button.setTitle(part.getValueSummary());
    }


    /**
     * Convenience routine so the bar can find the button it needs
     */
    boolean isForParticipant(RandomSetParticipant p) {
      return (participant == null) ? p == null : participant.getName().equals(p.getName());
    }


    @Override
    public void onClick(ClickEvent event) {
      ModifyParticipationWindow window = new ModifyParticipationWindow(ui);
      window.showAssignmentPopupWindow(participant);
    }
  } // ==== end of TreatmentButton

}