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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RandomSet implements Serializable, IsSerializable {
  private static final long serialVersionUID = 1L;
  public  static final String NoStratumName = "all";  // If has no strata, participants will be assigned to "all"

  // RandomSet usages in the code
  public enum RSType {
    TreatmentSet,
    UnSet;  // just to have an alternative, for testing
    public boolean isForPatientUI() {
      return this.equals(TreatmentSet);
    }
  }

  public enum RSState {
    // I don't think we need a private/test state
    Enrolling,    // patients can be added to it
    NotEnrolling, // patients can not be added, but patients are still submitting data
    Researching,  // all data collection is done, hide from patient UI
    Closed;        // hide from the UI

    public boolean stillAddingPatients() {
      return this.equals(Enrolling);
    }
    public boolean stillCollectingData() {
      return this.equals(Enrolling) || this.equals(NotEnrolling);
    }
    public boolean isClosed() {
      return this.equals(Closed);
    }
  }

  private String name;         // eg: "arm/RSI" This must not be changed from its value in the completeHandler
  private Long   siteId;

  private String algorithm;    // initially KSort and Pure
  private String title;        // An editable title for the group
  private RSType type;  // for UI, first is TreatmentSet
  private String description;  // long description
  private int durationDays;   // how long a patient is in the trial
  private Date endDate;

  // These are from the database
  private String userName;     // the lead doctor or researcher?  Or this could be in the description.

  private int targetPopulation; // for alerts, and used by some algorithms
  private RSState state;  // a state variable -
  //private long assessmentId;  // which survey is the one that gives consent to join this set

  private List<RandomSetGroup> groups;  // this must be a list, not a hash. Every group has a number/index.
  private RandomSetCategory[] categories;

  private int numPatients;
  private int numWithdrawn;
  private int numDeclined;
  private int numExcluded;
  private Date firstAssignedDate;
  private Date lastAssignedDate;

  public RandomSet() { }

  public RandomSet(Long siteId, String name, String algorithm) {
    this.name = name;
    this.siteId = siteId;
    this.algorithm = algorithm;
    this.state = RSState.Enrolling; // don't leave it null
  }

  public void init(String title, RSType type, String desc, String user, int targetSize, Date enddt, int daysDuration) {
    this.title = title;
    this.type = type;
    this.description = desc;
    this.userName = user;
    this.targetPopulation = targetSize;
    this.endDate = enddt;
    this.setPatientDuration(daysDuration);
    this.durationDays = daysDuration;
    setState(null);  // always initialize to Enrolling, then ASAP will ask DB for truth
  }

  public void initGroups(ArrayList<RandomSetGroup> groups) {
    this.groups = groups;
  }

  /**
   * The DB is queried to determine these, from the participant table.
   */
  public void initData(int numPatients, int numWithdrawn, int numDeclined, int numExcluded, Date first, Date last) {
    this.numPatients = numPatients;
    this.numWithdrawn = numWithdrawn;
    this.numDeclined = numDeclined;
    this.numExcluded = numExcluded;
    firstAssignedDate = first;
    lastAssignedDate = last;
  }

  public int incPatients(int inc) {
    return numPatients += inc;
  }

  public int incWithdrawn(int inc) {
    return numWithdrawn += inc;
  }

  public int incDeclined(int inc) {
    return numDeclined += inc;
  }

  public int incExcluded(int inc) {
    return numExcluded += inc;
  }


  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the siteId
   */
  public Long getSiteId() {
    return siteId;
  }

  /**
   * @return the algorith, eg KSort or Pure
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the type
   */
  public RSType getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(RSType type) {
    this.type = type;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  public String getUsername() {
    return userName;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the targetPopulation
   */
  public int getTargetPopulation() {
    return targetPopulation;
  }

  /**
   * @param targetPopulation the targetPopulation to set
   */
  public void setTargetPopulation(int targetPopulation) {
    this.targetPopulation = targetPopulation;
  }

  public RSState getState() {
    return state;
  }

  public void setState(String state) {
    if (state == null || state.isEmpty()) {
      this.state = RSState.Enrolling;
    } else try {
      this.state = RSState.valueOf(state);
    } catch (Throwable t) {
      this.state = RSState.Enrolling;
    }
  }

  /**
   * @return the numPatients
   */
  public int getNumPatients() {
    return numPatients;
  }

  /**
   * Records the assigned date if the one stored is null or later
   */
  public void setFirstAssignedDate(Date assignedDate) {
    if (firstAssignedDate == null || assignedDate.before(firstAssignedDate)) {
      firstAssignedDate = assignedDate;
    }
  }

  /**
   * @param lastAssignedDate the lastAssignedDate to set
   */
  public void setLastAssignedDate(Date lastAssignedDate) {
    this.lastAssignedDate = lastAssignedDate;
  }

  public RandomSetGroup getGroup(String groupName) {
    for (RandomSetGroup g: groups) {
      if (g.getGroupName().equals(groupName))
        return g;
    }
    return null;
  }

  public RandomSetGroup getGroup(int ix) {
    return groups.get(ix);
  }

  public List<RandomSetGroup> getGroups() {
    return groups;
  }

  /**
   * Returns the sum of the group block sizes, but at least 1. For computing percentages.
   */
  public int getBlockSize() {
    int blockSize = 0;
    for (RandomSetGroup g: groups) {
      blockSize += g.getTargetPerBlock();
    }
    return (blockSize == 0) ? 1 : blockSize;  // ensure we avoid an arithmetic error
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDt) {
    endDate = endDt;
  }

  /**
   * Sets the approximate duration of a patient's participation in the trial (study).
   * <br>Consider lessening a long interval by a week or two, to allow a low approximate ending.
   * <br>Note: There are, on average, 30.43 days/month, so use:  ((months * 61)/2)
   */
  public void setPatientDuration(int numDays) {
      durationDays = numDays;  // 30.43 days/month
  }

  public int getDurationDays() {
    return durationDays;
  }

  /**
   * This will be rewritten and used to produce a quick report in the UI.
   * Meanwhile, it prevents the internal state from seeming to be unused.
   */
  public String getStats() {
    return numPatients + " " + numWithdrawn + " " + numDeclined + " " + numExcluded + " "
         + firstAssignedDate + lastAssignedDate + ", blockSize=" + this.getBlockSize();
  }


  public void setCategories(RandomSetCategory[] cats) {
    categories = cats;
  }

  /**
   * This is for the UI, so it can present a list of category sections each with a set of
   * radio button answers to assign a patient to the right stratum.
   * @return null if there are no strata, otherwise returns the array of categories
   */
  public RandomSetCategory[] getCategories() {
    return categories;
  }

  /**
   * Returns text for the UI to display as a patient's stratum.
   * @param stratumName "all" or <category>=<value> or <cat1>=<val1>,cat2=val2...
   * @return One string with separate lines of: Category: Value
   */
  public String stratumNameToDescription(String stratumName) {
    if (NoStratumName.equals(stratumName)) {
      return "";
    }
    String names[] = stratumName.split(",");
    StringBuilder sb = new StringBuilder(200);
    String delim="";
    for (String catEqVal: names) {
      RandomSetCategory cat = getCategory(catEqVal);
      sb.append(delim).append(cat.getDescription(delim, catEqVal, ": "));
      delim = "\n";
    }
    return sb.toString();
  }

  private RandomSetCategory getCategory(String nameEqVal) {
    for (int i = 0;  i < categories.length;  i++) {
      RandomSetCategory cat = categories[i];
      if (nameEqVal.startsWith(cat.getName())) {
        if (nameEqVal.charAt(cat.getName().length()) == '=') {
          return cat;
        }
      }
    }
    return null;
  }

}
