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
import java.util.HashMap;
import java.util.List;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.widgets.Popup;
import edu.stanford.registry.shared.RandomSetCategory;
import edu.stanford.registry.shared.RandomSetCategory.Value;
import edu.stanford.registry.shared.RandomSetGroup;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.RandomSetParticipant.State;

import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.PanelType;

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
class ModifyParticipationWindow {
  private final Model model;
  private final UpdateParticipantCB updater;
  private RandomSetParticipant participant;

  // The view: UI widgets that store the user's selection(s)
  private final CatRBList catRBList = new CatRBList();
  private final ArrayList<RadioButton> choices = new ArrayList<>(5);
  private final ArrayList<Radio> withdrawChoices = new ArrayList<>(3);
  private final Button closeButton = new Button("Update");
  // This maps RadioButton strings to states to discover the state set by the selected choice
  private final HashMap<String,RandomSetParticipant.State> stringState = new HashMap<>(10);
  private final org.gwtbootstrap3.client.ui.Panel withdrawReasonsPanel = new org.gwtbootstrap3.client.ui.Panel();
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  /**
   * Called from the button or the EnterPatientData menu
   */
  ModifyParticipationWindow(Model model) {
    this.model = model;
    this.updater = new UpdateParticipantCB(model);
  }


  void showAssignmentPopupWindow(RandomSetParticipant part) {
    participant = part;
    createAssignmentPopupWindow(part);
  }

  private static final String CompleteTip = "The patient has reached the end of the trial regimen";
  private static final String UnWithdrawTip = "Patient reconsidered or the 'Withdraw' button was accidentally hit, "
      + "or was hit for the wrong patient";
  private static final String WithdrawTip = "The patient wants to withdraw from the study- perhaps they want a "
      + "different treatment or won't fill out surveys";
  private static final String UnsetTip = "The patient is not part of the study, but might be qualified";
  private static final String NoTip = null;

  /**
   * A list of the various labels and tool tips for states.
   * Different ones (for the same state) are used depending on the initial state.
   */
  private enum StateButton {
    NotAssigned("Not Randomized", RandomSetParticipant.State.Unset, UnsetTip),

    Assign("Randomize to a treatment group", RandomSetParticipant.State.Assigned, NoTip),
    Assigned("Randomized to a treatment group", RandomSetParticipant.State.Assigned, NoTip),
    UnWithdraw("Un-withdraw, return to the group", RandomSetParticipant.State.Assigned, UnWithdrawTip),
    UnComplete("Change back to participating", RandomSetParticipant.State.Assigned, NoTip),

    Withdraw("Withdraw", RandomSetParticipant.State.Withdrawn, WithdrawTip),
    Withdrawn("Withdrawn", RandomSetParticipant.State.Withdrawn, NoTip),

    Completed("Participation has completed", RandomSetParticipant.State.Completed, CompleteTip),

    Decline("Patient declines consent", RandomSetParticipant.State.Declined, NoTip),
    Declined("Patient declines consent", RandomSetParticipant.State.Declined, NoTip),
    Disqualify("Disqualify this patient", RandomSetParticipant.State.Disqualified, NoTip),
    Disqualified("Was not qualified", RandomSetParticipant.State.Disqualified, NoTip);

    String text; // all these instance variables are used below in addChoice()
    String tip;  // tool tip for more information
    RandomSetParticipant.State state;
    StateButton(String text, RandomSetParticipant.State state, String tip) {
      this.text = text;
      this.state = state;
    }
  }

  private enum WithdrawReasons {
    WithdrawRefused("Patient refused"),
    WithdrawError("Patient enrolled in error"),
    WithdrawOther("Other");
    final String text;
    WithdrawReasons(String text) {
      this.text = text;
    }
    String getDescription() {
      return text;
    }
  }

  static final String group = "tsetGroup";

  private boolean choiceChangedParticipant() {
    //consoleLog("choiceChanged: stringState size="+(stringState==null?-1:stringState.size()));
    String choice = null;
    for (RadioButton w: choices) {
      if (w.getValue()) {  // finds the value that was set
        choice = w.getText();
        break;
      }
    }
    //consoleLog("choiceChanged: will get oldState, participant is "+((participant==null)?"null":"not null"));
    RandomSetParticipant.State oldState = participant.getState();
    RandomSetParticipant.State newState = stringState.get(choice);
    catRBList.setStratumNameIfAssigned(participant);

    //consoleLog("Old state was "+oldState+" --> "+newState+" #states="+stringState.size());
    if (oldState == newState || (oldState.isNotSetup() && newState.isNotSetup())) {
      return false;
    }
    participant.setState(newState);
    //consoleLog("Set participant state, changed="+participant.changed());
    return true;
  }

  /**
   * Adds the choice (radio) button, turning it on if its state = participant's state
   */
  private void addChoice(StateButton btns) {
    boolean checked = btns.state == participant.getState();
    stringState.put(btns.text, btns.state);
    RadioButton rba = new RadioButton(group, btns.text);
    //consoleLog("addChoice "+state.name()+(checked?" is checked":" unchecked"));
    rba.setValue(checked);
    if (btns.tip != null) {
      rba.setTitle(btns.tip);
    }
    if (btns.state == State.Withdrawn) {
      rba.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          withdrawReasonsPanel.setVisible(true);
          closeButton.setEnabled(false);
          for (Radio withdrawChoice : withdrawChoices) {
            if (withdrawChoice.getValue()) {
              closeButton.setEnabled(true);
            }
          }
        }
      });
    }
    else {
      rba.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          withdrawReasonsPanel.setVisible(false);
        }
      });
    }
    choices.add(rba);
  }


  /**
   * Creates a popup window with the group explanation and a withdraw or rejoin button.
   * @param heading Treatment Set: Name|title(if different)|description
   * @param widgets All the radio buttons
   * @param assignIx The index of the Assign radio button, to add the category button right after it.
   */
  private void createPopup(String heading, ArrayList<RadioButton> widgets, int assignIx, boolean showGroupDesc) {
    final Popup randomSetPopup = model.tabParent.makePopup("Treatment Set");
    randomSetPopup.setModal(false);
    VerticalPanel panel = new VerticalPanel();
    panel.addStyleName(RegistryResources.INSTANCE.css().tsetPopupPanel());

    addSummaryText(panel, heading);

    // add radio button widgets
    boolean first = true;
    int i = -1;
    for (RadioButton rb: widgets) {
      if (rb != null) { // the button is null if the patient just declined
        panel.add(rb);
        if (first && showGroupDesc) {
          addGroupDesc(panel);
          first = false;
        }
      }
      if (++i == assignIx) {
        catRBList.addCategoryValueRadioButtons(rb, panel, participant.getRandomSet().getCategories());
      }
    }
    addWithDrawReasonForm( panel );

    addCloseCancelButtons(randomSetPopup);
    addClickHandlersToEnableCloseButton(closeButton, widgets, assignIx);

    catRBList.noteCloseButtonAndAddClickHandlers(closeButton);

    randomSetPopup.setGlassEnabled(true);
    randomSetPopup.showMessage(panel);
  }


  /**
   * This adds to each non-Assign radio button a click-handler which enables the Update button.
   * <p>Because the Assign button can have multiple sets of category radio buttons, the close button is
   * disabled if it or one of its category value buttons is clicked and not all categories have had values chosen.
   * And all of the non-Assign radio buttons must enable the close button.
   */
  private void addClickHandlersToEnableCloseButton(final Button closeButton, ArrayList<RadioButton> widgets, int assignIx) {
    ClickHandler enableCloseClickHandler = new ClickHandler() {
      @Override public void onClick(ClickEvent event) {
        if (!withdrawReasonsPanel.isVisible()) {
          closeButton.setEnabled(true);
        }
      }
    };
    int i = -1;
    for (RadioButton rb: widgets) {
      if (++i != assignIx) {
        rb.addClickHandler(enableCloseClickHandler);
      }
    }
  }

  private void addGroupDesc(VerticalPanel panel) {
    String groupName = participant.getGroup();
    String desc = null;
    RandomSetGroup group = participant.getRandomSet().getGroup(groupName);
    if (group != null) { // defensive check
      desc = group.getDescription();
    }
    if (desc != null && !desc.isEmpty()) {
      StringBuilder sb = new StringBuilder(desc.length()+100);
      String text = sb.append("Assigned group: ").append(group.getGroupName()).append('\n').append(desc).toString();
      TextArea area = new TextArea();
      area.setStyleDependentName("tsetGroupDesc", true);
      area.setCharacterWidth(70);
      area.setVisibleLines(model.utils.countLines(text));
      area.setText(text);
      panel.add(area);
    }
  }


  private void addCloseCancelButtons(final Popup randomSetPopup) {

    closeButton.addClickHandler(new ClickHandler() {
      @Override public void onClick(ClickEvent event) {
        randomSetPopup.hide();
        if (choiceChangedParticipant()) {
          updater.update(participant);
        }
      }
    });

    final Button cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      @Override public void onClick(ClickEvent event) {
        randomSetPopup.hide();
      }
    });

    List<Button> buttons = new ArrayList<>();
    buttons.add(closeButton);
    buttons.add(cancelButton);

    randomSetPopup.setCustomButtons(buttons);
  }


  private void addSummaryText(Panel panel, String summary) {
    if (summary != null && !summary.isEmpty()) {
      TextArea summaryText = new TextArea();
      summaryText.setCharacterWidth(80);
      summaryText.setVisibleLines(model.utils.countLines(summary));
      summaryText.setText(summary);
      panel.add(summaryText);
    }
  }


  private void createAssignmentPopupWindow(RandomSetParticipant rsp) {
    RandomSet rset = rsp.getRandomSet();
    boolean fullState = model.showFullStates;

    StringBuilder sb = new StringBuilder(200);
    String name = rsp.getName().replaceAll("tset:", "").replace("TreatmentSet", "");
    sb.append("Treatment Set: ").append(name);
    if (!name.equals(rset.getTitle())) {
      sb.append('\n').append(rset.getTitle());
    }
    sb.append('\n').append(rset.getDescription());

    boolean showGroupDesc = false;
    int assignIx = -1; // index of the to-assign radio, to add the category+values
    switch (rsp.getState()) {
    case NotYetQualified: // shouldn't occur- if this state came in, menu item would be gray
    case Unset:
      addChoice(StateButton.NotAssigned);
      assignIx = addChoiceAssign();
      if (fullState) {
        addChoice(StateButton.Decline);
        addChoice(StateButton.Disqualify);
      }
      break;

    case Assigned:
      addChoice(StateButton.Assigned);
      addChoice(StateButton.Withdraw);
      if (fullState) { // TODO: really should show only if after group duration compete
        addChoice(StateButton.Completed);
      }
      showGroupDesc = true;
      break;

    case Completed:
      addChoice(StateButton.Assigned);
      addChoice(StateButton.Completed);
      break;

    case Withdrawn:
      addChoice(StateButton.UnWithdraw);
      addChoice(StateButton.Withdrawn);
      break;

    case Declined:
      addChoice(StateButton.NotAssigned);
      assignIx = addChoiceAssign();
      addChoice(StateButton.Declined);
      addChoice(StateButton.Disqualify);
      break;

    case Disqualified:
      addChoice(StateButton.NotAssigned);
      assignIx = addChoiceAssign();
      addChoice(StateButton.Decline);
      addChoice(StateButton.Disqualified);
      break;

    default:
      return;
    }
    createPopup(sb.toString(), choices, assignIx, showGroupDesc);
  }


  static class CatRBList implements ClickHandler {
    boolean isUsed = false;
    ArrayList<CategoryRB> catRBs;
    RadioButton assignedButton;
    Button submitButton;  // if any checked, button is grayed till all values are checked
    int numCategories = 0;

    public void addCategoryValueRadioButtons(RadioButton assignedBtn, Panel panel, RandomSetCategory cats[]) {
      isUsed = true;
      assignedButton = assignedBtn;
      numCategories = (cats == null) ? 0 : cats.length;
      catRBs = new ArrayList<>(numCategories);
      for (int i = 0;  i < numCategories;  i++) {
        catRBs.add(new CategoryRB(cats[i], panel));
      }
    }


    public void setStratumNameIfAssigned(RandomSetParticipant rsp) {
      if (!rsp.getStratumName().isEmpty() || !isUsed || assignedButton == null || !assignedButton.getValue()) {
        return;  // keep the old one
      }
      String stratumName = RandomSetCategory.NoStratumName;  // "all"
      if (numCategories > 0) {
        StringBuilder sb = new StringBuilder(100);
        for (CategoryRB cat: catRBs) {
          cat.addCatEqVal(sb);
        }
        stratumName = sb.toString();
      }
      rsp.setStratumName(stratumName);
    }


    private void enableSubmitIfAllCategoriesAreAssigned() {
      assignedButton.setValue(true); // in case just a category was clicked
      if (!isUsed) {
        return;
      }
      for (CategoryRB catRB: catRBs) {
        if (!catRB.isOneChecked()) {
          submitButton.setEnabled(false);
          return;
        }
      }
      submitButton.setEnabled(true);
    }


    public void noteCloseButtonAndAddClickHandlers(Button closeButton) {
      submitButton = closeButton;
      if (isUsed) {  // add enable callback to all raido buttons
        assignedButton.addClickHandler(this);
        for (CategoryRB catRB: catRBs) {
          catRB.addClickHandler(this);
        }
      }
    }

    @Override
    public void onClick(ClickEvent event) {
      enableSubmitIfAllCategoriesAreAssigned();
    }
  }


  /**
   * A class containing a category and one radio button per value to add to the popup panel,
   * plus methods to interrogate the radio button to see if they're set (to enable the Update
   * button) and to get the chosen value for the stratum name.
   */
  static class CategoryRB {
    ArrayList<RadioButton>valueRBs = new ArrayList<RadioButton>(2);
    RandomSetCategory cat;

    CategoryRB(RandomSetCategory cat, Panel panel) {
      this.cat = cat;
      Label label = new Label(cat.getQuestion());
      label.setStyleName(css.tsetPPCatLabel());
      panel.add(label);
      Value[] vals = cat.getValues();
      for (RandomSetCategory.Value val: vals) {
        RadioButton rb = new RadioButton(cat.getName(), val.getAnswer());
        rb.setStyleName(css.tsetPPCatLabel());
        valueRBs.add(rb);
        panel.add(rb);
      }
    }

    public void addClickHandler(ClickHandler handler) {
      for (RadioButton rb: valueRBs) {
        rb.addClickHandler(handler);
      }
    }

    public boolean isOneChecked() {
      for (RadioButton rb: valueRBs) {
        if (rb.getValue()) {
          return true;
        }
      }
      return false;
    }

    // Only called if one is set
    public void addCatEqVal(StringBuilder sb) {
      for (int i = 0;  i < valueRBs.size();  i++) {
        RadioButton rb = valueRBs.get(i);
        if (rb.getValue()) {
          cat.addValueToNameID(sb, cat.getValues()[i]);
          return;
        }
      }
    }
  }


  /**
   * Returns the index of the Assign button, so category/value buttons can be added
   */
  private int addChoiceAssign() {
    addChoice(StateButton.Assign);
    return choices.size() - 1;
  }


  /**
   * After the pop-up window changes the participant, this sends it to the server
   */
  static class UpdateParticipantCB implements AsyncCallback<RandomSetParticipant>{
    final private Model ui;


    UpdateParticipantCB(Model ui) {
      this.ui = ui;
    }


    void update(RandomSetParticipant participant) {
      if (participant.changed()) {
        ui.physSvcs.updateRandomSetParticipant(participant, this);
      }
    }


    @Override
    public void onFailure(Throwable caught) {
    }


    /**
     * Called after sending the updated participation to the server
     */
    @Override public void onSuccess(RandomSetParticipant part) {
      if (part == null) {
        return;  // cancel
      }
      ui.patient.setAttribute(part.getAttrName(), part.getValue());
      if (ui.treatmentBar != null) {
        ui.treatmentBar.updateParticipant(part);
      }
      if (ui.enterPatientDataMenu != null ) {
        ui.enterPatientDataMenu.removeItem(part.getName());
      }
      ui.swap(part);
    }
  }

  private void addWithDrawReasonForm(Panel panel) {
    withdrawReasonsPanel.setType(PanelType.DEFAULT);
    withdrawReasonsPanel.addStyleName(css.tsetPPSubMenu());
    withdrawReasonsPanel.setWidth("375px");

    PanelHeader panelHeader = new PanelHeader();
    Label reasonLabel = new Label("Reason for withdrawing patient");
    panelHeader.add(reasonLabel);
    withdrawReasonsPanel.add(panelHeader);

    PanelBody panelBody = new PanelBody();
    final TextBox otherExplain = new TextBox();
    otherExplain.addStyleName(css.tsetPPWithdrawReason());
    otherExplain.setEnabled(false);
    otherExplain.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        TextBox other = (TextBox) event.getSource();
        if (other.getValue() == null || other.getValue().isEmpty()) {
          participant.setReason(WithdrawReasons.WithdrawRefused.toString());
        } else {
          participant.setReason(other.getValue());
        }
      }
    });
    Radio refusal = new Radio("withdraw", WithdrawReasons.WithdrawRefused.getDescription());
    refusal.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        participant.setReason(WithdrawReasons.WithdrawRefused.toString());
        otherExplain.clear();
        otherExplain.setEnabled(false);
        closeButton.setEnabled(true);
      }
    });
    withdrawChoices.add(refusal);
    panelBody.add(refusal);
    Radio error = new Radio("withdraw", WithdrawReasons.WithdrawError.getDescription() );
    error.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        participant.setReason(WithdrawReasons.WithdrawError.toString());
        otherExplain.clear();
        otherExplain.setEnabled(false);
        closeButton.setEnabled(true);
      }
    });
    withdrawChoices.add(error);
    panelBody.add(error);
    Radio other = new Radio("withdraw", WithdrawReasons.WithdrawOther.getDescription());
    other.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        participant.setReason(WithdrawReasons.WithdrawOther.toString());
        otherExplain.setEnabled(true);
        closeButton.setEnabled(true);
      }
    });
    withdrawChoices.add(other);
    panelBody.add(other);
    panelBody.add(otherExplain);

    if (participant.getState() == State.Withdrawn && participant.getReason() != null && !participant.getReason().isEmpty()) {
      if (participant.getReason().equals(WithdrawReasons.WithdrawRefused.toString())) {
        refusal.setValue(true);
      } else if (participant.getReason().equals(WithdrawReasons.WithdrawError.toString())) {
        error.setValue(true);
      } else {
        other.setValue(true);
        if (!participant.getReason().equals(WithdrawReasons.WithdrawOther.toString())) {
          otherExplain.setValue(participant.getReason());
        }
        otherExplain.setEnabled(true);
      }
    }
    withdrawReasonsPanel.add(panelBody);

    if (participant.getState() == State.Withdrawn) {
      withdrawReasonsPanel.setVisible(true);
    } else {
      withdrawReasonsPanel.setVisible(false);
    }
    panel.add(withdrawReasonsPanel);
  }
}
