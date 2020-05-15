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

package edu.stanford.registry.shared;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sun.istack.internal.NotNull;

import edu.stanford.registry.client.utils.ClientUtils;

/**
 * This is for recording patient participation in a RandomSet, and for communicating to the UI.
 * It includes a "disabledReadon" which is not stored in the database - it's the tool-tip
 * if the TreatmentSet is shown in the UI, added by the customizer.
 *
 * It includes states that are never put into the UI. Two are created for the EnterPatientData
 * menu.  And Disqualified/Declined, with a reason, might be wanted by a clinic to not have to
 * always wonder if a patient should be added to a TreatmentSet. A "reason" can be set
 * to remind the user why.
 *
 * Two fields, originalState and originalReason, are included to know if the state changed.
 */
public class RandomSetParticipant implements Serializable, IsSerializable {
  private static final long serialVersionUID = 1L;

  public enum State {
    Unset, // can be stored in the db with a reason
    NotYetQualified, // never stored in DB, this tells UI to gray this one, with a tool tip

    // These two might not be used. They let these options show in the UI, so the Dr. needn't wonder
    Disqualified, // e.g. already is receiving a treatment
    Declined,     // patient declined and Dr. wanted to note it.

    Assigned,  // These 3 have a groupName set
    Withdrawn, // Either patient wanted to stop or didn't participate enough (or died?)
    Completed; // Participation is over for this patient

    // Convenience methods
    public boolean isAssigned() {
      return this.equals(Assigned);
    }
    public boolean isWithdrawn() {
      return this.equals(Withdrawn);
    }
    public boolean isComplete() {
      return this.equals(Completed);
    }
    public boolean isAnAssignedState() {
      return isAssigned() || isWithdrawn() || isComplete();
    }
    /**
     * true if Unset or NotYetQualified
     */
    public boolean isNotSetup() {
      return this.equals(Unset) || this.equals(NotYetQualified);
    }
  }

  private long randomsetParticipantId = -1;  // -1 == unset, else from the database

  private String patientId;
  private RandomSet randomSet;

  @NotNull private State state;

  private String groupName;
  private String stratumName;
  private String reason = "";
  private Date assignedDate;
  private Date withdrawnDate;

  private long updateSequence;  // counts the database changes
  private Date updateTime;      // when last changed

  // These 3 are extra, for communicating with the UI
  private State originalState; // so we can tell when it changes
  private String originalReason;
  private String disabledReason = ""; // if shouldn't allow assignment to a treatment set

  public RandomSetParticipant() {  // default constructor needed for deserialization
  }

  /**
   * Used by the database
   */
  public RandomSetParticipant(long dbId, String patientId, RandomSet rset, State state,
      String group, String stratumName, Date assignedDate, Date withdrawnDate, String reason,
      Date upTime, long upSeq) {
    this(dbId, patientId, rset, state, group, stratumName, assignedDate, withdrawnDate, reason);
    updateSequence = upSeq;
    updateTime = upTime;
  }

  /**
   * For creating unset participants for the UI to let physicians assign
   */
  public RandomSetParticipant(String patientId, RandomSet rset, State state) {
    this(-1, patientId, rset, state, "", "", null, null, "");
  }

  public RandomSetParticipant(long dbId, String patientId, RandomSet rset, State state,
      String stratumName, String group, Date assignedDate, Date withdrawnDate, String reason) {
    this.randomsetParticipantId = dbId;
    this.patientId = patientId;
    this.randomSet = rset;
    setState(state);
    setGroup(group);
    setStratumName(stratumName);
    this.assignedDate = assignedDate;
    this.withdrawnDate = withdrawnDate;
    setReason(reason);
    this.resetOriginals();
  }


  /**
   * Only to be called after any changes to the object have been put into the database.
   */
  public RandomSetParticipant resetOriginals() {
    this.originalState = this.state;
    this.originalReason = this.reason;
    this.disabledReason = "";
    return this;
  }

  /**
   * Set the withdrawn date, and update withdrawn boolean.
   *
   * @param date non-null (now) to withdraw, null to re-enroll
   */
  public void setWithdrawnDate(Date date) {
    withdrawnDate = date;
  }


  // ==== getters ====

  /**
   * Returns true if the user can withdraw or unwithdraw, false if declined or excluded
   */
  public boolean hasChoice() {
    return state.equals(State.Assigned) || state.equals(State.Withdrawn);
  }

  public State getOriginalState() {
    return originalState;
  }

  /**
   * @return the state, e.g. Unset, Disqualified, Assigned, Withdrawn...
   */
  public State getState() {
    return state;
  }

  /**
   * @param state If null, assumed to be Unset.
   */
  public void setState(State state) {
    this.state = (state == null) ? State.Unset : state;
  }

  public RandomSet getRandomSet() {
    return randomSet;
  }

  /**
   * This is the primary-key-id generated in the database. 0 means it's not yet in the database.
   */
  public long getRandomsetParticipantId() {
    return randomsetParticipantId;
  }

  /**
   * This is the primary-key-id generated in the database. 0 means it's not yet in the database.
   */
  public RandomSetParticipant setRandomsetParticipantId(long id) {
    randomsetParticipantId = id;
    return this;
  }

  // The values below are for the server, to update the withdrawn state

  /**
   * Returns the participating patient's ID
   */
  public String getPatientId() {
    return patientId;
  }

  /**
   * Returns the name of the RandomSet in which the patient is participating
   */
  public String getName() {
    return randomSet.getName();
  }


  /**
   * Set the name of the group to which the patient is being assigned
   */
  public void setGroup(String groupName) {
    this.groupName = (groupName == null) ? "" : groupName;
  }

  /**
   * Returns the group the patient is assigned to, or null if it's not an Assigned/Withdrawn/Completed state.
   */
  public String getGroup() {
    return groupName;
  }


  /**
   * Set the name of the group to which the patient is being assigned
   */
  public void setStratumName(String stratumName) {
    this.stratumName = (stratumName == null) ? "" : stratumName;
  }

  /**
   * Returns the group the patient is assigned to, or null if it's not an Assigned/Withdrawn/Completed state.
   */
  public String getStratumName() {
    return stratumName;
  }


  /**
   * Convenience method to tell if the state is Withdrawn
   */
  public boolean isWithdrawn() {
    return state.equals(State.Withdrawn);
  }

  public void setAssignedDate() {
    assignedDate = new Date();
  }

  /**
   * Returns the date the patient was first assigned to a group
   */
  public String getAssignedDate(ClientUtils utils) {
    return assignedDate == null ? "- - -"  : utils.getDateString(assignedDate);
  }

  /**
   * Returns the date the patient was first assigned to a group
   */
  public Date getAssignedDate() {
    return assignedDate;
  }

  /**
   * Returns a String representing the date the patient was last withdrawn from a group
   */
  public String getWithdrawnDate(ClientUtils utils) {
    return withdrawnDate == null ? "- - -"  : utils.getDateString(withdrawnDate);
  }

  /**
   * Returns the date the patient was last withdrawn from a group
   */
  public Date getWithdrawnDate() {
    return withdrawnDate;
  }

  /**
   * The physician might add this reason for a state setting.
   */
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = (reason == null) ? "" : reason;
  }

  public String getAttrName() {
    return "tset:" + randomSet.getName();
  }

  /**
   * The 1-line summary, eg:  ThisTreatment - withdrew 2wk 2da ago
   */
  public String getValueSummary() {
    RandomSetGroup g = randomSet.getGroup(getGroup());
    String desc = (g == null) ? "" : g.getDescription();
    switch (state) {
    case Assigned:
      return daysAgo("group: "+groupName+", assigned ", getAssignedDate(), desc);

    case Completed:
      return daysAgo("Completed, group: "+groupName+" assigned ", getAssignedDate(), desc);

    case Withdrawn:
      String s = daysAgo("Withdrew ", getWithdrawnDate(), "");
      return daysAgo(s+", assigned to group: "+groupName, getAssignedDate(), desc);

    default:
      return state.toString();
    }
  }

  public String getPDFSummary() {
    return getValueSummary();
  }

  /**
   * The proper value for the PatientAttribute
   */
  public String getValue() {
    switch (state) {
    case Assigned:
      return "group "+groupName;
    case Completed:
      return "Completed "+groupName;
    case Withdrawn:
      return "Withdrew from "+groupName;
    default:  // if isNotSetup(), no value is needed
      return state.isNotSetup() ? "" : state.toString();
    }
  }

  public boolean changed() {
    return !state.equals(originalState) || !reason.equals(originalReason);
  }

  /**
   * Produces a string with a, b, and telling when the date was, like: 4mo 3wk 2days ago
   */
  static private String daysAgo(String prefix, Date date, String sfx) {
    long then = date.getTime();
    long now = System.currentTimeMillis();
    StringBuilder sb = new StringBuilder(30 + prefix.length()+sfx.length()).append(prefix);
    int initialLength = sb.length();

    int daysAgo = Math.round((now - then) / (24 * 60 * 60000));
    daysAgo = addPeriod(sb, 1, 1+(365*4), 4, "yr", daysAgo);
    daysAgo = addPeriod(sb, 3, 61, 2, "mo", daysAgo);
    daysAgo = addPeriod(sb, 1, 7, 1, "wk", daysAgo);
    /*     */ addPeriod(sb, 1, 1, 1, "day", daysAgo);
    String tail = (sb.length() == initialLength) ? "today" : "s ago";
    return sb.append(tail).append(sfx.isEmpty()?sfx:"\n\n").append(sfx).toString();
  }

  static private int addPeriod(StringBuilder sb, int min, int num, int denom, String unit, int daysAgo) {
    int minDays = (min * num) / denom;
    if (daysAgo < minDays) {
      return daysAgo;
    }
    if (sb.length() > 0) {  // add delimiter
      sb.append(' ');
    }
    int number = (daysAgo * denom) / num;
    sb.append(number).append(unit);
    return daysAgo - ((number * num) / denom);
  }

  /**
   * @param reasonToolTip Must not be null or empty to disable this treatment set
   */
  public void disable(String reasonToolTip) {
    disabledReason = reasonToolTip==null ? "" : reasonToolTip;
  }

  public boolean isEnabled() {
    return disabledReason.isEmpty();
  }

  public String getDisabledString() {
    return disabledReason;
  }

  public long getUpdateSequence() {
    return updateSequence;
  }

  public long incUpdateSequence() {
    return ++updateSequence;
  }

  public Date setUpdateTime() {
    return updateTime = new Date();
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  /**
   * Returns a comparator to sort participants by assigned date.
   */
  static public Comparator<RandomSetParticipant> getComparator(boolean ascending) {
     return new RSPComparator(ascending);
  }

  /**
   * Comparator to sort participants by most recent assigned (or update) date
   */
  static class RSPComparator implements Comparator<RandomSetParticipant> {
    final boolean ascending;
    RSPComparator(boolean ascend) {
      ascending = ascend;
    }
    @Override
    public int compare(RandomSetParticipant o1, RandomSetParticipant o2) {
      Date d1 = o1.getAssignedDate() == null ? o1.getUpdateTime() : o1.getAssignedDate();
      Date d2 = o2.getAssignedDate() == null ? o2.getUpdateTime() : o2.getAssignedDate();
      if (d1 == null || d2 == null) {  // defensive- updateTime is never null
        return (d1 == null) ? ((d2 == null) ? 0 : 1) : -1;
      }
      int comp = d1.compareTo(d2); // compare these backwards to get descending order
      return ascending ? comp : -comp;
    }
  }
}