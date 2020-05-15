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

import com.google.gwt.user.client.rpc.IsSerializable;

public class RandomSetGroup implements Serializable, IsSerializable {
  private static final long serialVersionUID = 1L;

  /** A small integer 1+, unique in the set */
  private int groupId;
  /** Unique within the set. */
  private String groupName; // eg: 'Exercise+Opioids,',
                            // 'Ibu+Exercise,thenOpioids', ... only unique
                            // within a set
  /**
   * These are added up to form the block size, and determine the relative
   * percentage of each group
   */
  private int targetPerBlock;
  /** if true, withdrawals shrink the participant_count */
  private boolean subtractWithdrawals;
  /** 0 means there's no maximum. Only used if closeWhenMaxIsReached is true */
  private int maxSize;
  /** if true, when it reaches this size, this group stops growing */
  private boolean closeWhenMaxIsReached;
  /** Details about this group- perhaps dosage information about a drug */
  private String description = "";

  // These are computed and/or gotten from the RandomParticipant table
  private boolean closed;
  private int patientCount; // same as aggregation query over RandomParticipant
                            // table (+logic for subtract options...)
  private int withdrawnCount;

  public RandomSetGroup() {
  } // Serialization needs a default constructor

  /**
   * This is used when a group is read from the database.
   *
   * @param id 1..n
   * @param name Descriptive name
   * @param targetPerBlock If algorithms isn't block-based, this can be a percent, relative
   *                       to other groups targets
   * @param subtractWithdrawals Usually false, for simplicity- an external study might want true.
   * @param maxSize This closes the group if closeWhenMaxIsReached is true. Else it's just reported.
   * @param closeWhenMaxIsReached If true, the group is closed when the maxSize is reached.
   */
  public RandomSetGroup(int id, String name, String desc, int targetPerBlock, boolean subtractWithdrawals, int maxSize,
      boolean closeWhenMaxIsReached) {
    groupId = id;
    groupName = name;
    description = desc == null ? "" : desc;
    this.targetPerBlock = targetPerBlock;
    this.subtractWithdrawals = subtractWithdrawals;
    this.maxSize = maxSize;
    this.closeWhenMaxIsReached = closeWhenMaxIsReached;
    this.closed = false;
  }

  /**
   * Use this when a group is first created.
   */
  public RandomSetGroup(int id, String name, String desc, int targetPercent, boolean subtractWithdrawals, int maxSize) {
    this(id, name, desc, targetPercent, subtractWithdrawals, maxSize, /* closeWhenMaxIsReached */ true);
  }

  public RandomSetGroup setDescription(String desc) {
    this.description = desc;
    return this;
  }

  /**
   * Call this when a patient is added to this group to increase the patient
   * count.
   */
  public RandomSetGroup addPatient() {
    patientCount++;
    if (patientCount == maxSize) {
      closed = closeWhenMaxIsReached;
    }
    return this;
  }

  public void receivePatientCounts(int count, boolean participating) {
    if (participating) {
      patientCount = count;
    } else {
      withdrawnCount = count;
    }
    if (maxSize > 0 && closeWhenMaxIsReached) {
      count = patientCount + (subtractWithdrawals ? 0 : withdrawnCount);
      closed = (count >= maxSize);
    }
  }

  // ==== Below here are just getters - they don't call markAsChanged()

  /**
   * This must be the index of the group in the RandomSet's group list
   */
  public int getGroupId() {
    return groupId;
  }

  public boolean isClosed() {
    return closed;
  }

  /**
   * Returns the groupName, never null or empty
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * Returns the target number of group participants per block. Like a target
   * percent, but per blockSize
   */
  public int getTargetPerBlock() {
    return targetPerBlock;
  }

  /**
   * Returns true if should subtract withdrawals from counting toward the target
   * percentage
   */
  public boolean shouldSubtractWithdrawals() {
    return subtractWithdrawals;
  }

  /**
   * Returns the desired maximum size of this group. After this, it might not be
   * assigned to, depending on the algorithm.
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Returns the description of the group, possibly with instructions to the
   * physician, to show in the UI.
   */
  public String getDescription() {
    return description;
  }


  /*
   * Possibly use this later, to make a quick summary of a group as text in the UI

  private String formattedContent;  // "must be initialized on server...";
  private static final String tenSp = "          ";
  public static final String summaryHeader = "   " + tenSp + "Group Name" + tenSp
      + ": Tgt% #Pats SubWs? #Wths MaxSz  Open  CloseAtMax\n";
  public static final String summaryFormat = "   %30s: %3d%% %5d %6s %5d %5d %6s %10s\n";
  */

  /* *
   * Hack: This must be run on the server because GWT7, can't handle
   * String.format(format, objects). It cannot be compiled. This refreshes the
   * summary string with up-to-date numbers before it's sent to the client.
   *
   * /
  public void refreshSummaryString(String s) { // String.format() must be done
                                               // in server, not here
    formattedContent = s; // String.format(RandomGroup.summaryFormat,
                          // getSummaryObjects());
  }

  public Object[] getSummaryObjects() {
    return new Object[] { getGroupName(), // %-30s
        getTargetPerBlock(), // %2d%
        getPatientCount(), // %5d
        (shouldSubtractWithdrawals() ? "InclWs" : "NotWs"), // %6s
        getWithdrawnCount(), // %5d
        getMaxSize(), // %5d
        (isOpen() ? "Open" : "Closed"), // 6s
        (shouldCloseOnMax() ? "CloseAtMax" : "StayOpen") }; // %10s
  }

  public String getSummaryString() {
    return formattedContent;
  } /* */


  /**
   * Returns true if no more patients should be assigned to this group when it
   * reaches its maximum.
   */
  public boolean shouldCloseOnMax() {
    return closeWhenMaxIsReached;
  }

  /**
   * Returns true if this group can still be assigned to.
   */
  public boolean isOpen() {
    return !this.closed;
  }

}