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

package edu.stanford.registry.server.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlInsert;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSetParticipant;
import edu.stanford.registry.shared.RandomSet.RSType;

/**
 * Database Access Object for RandomSet and RandomSetParticipant
 */
public class RandomSetDao {
  final Database database;
  final Long siteId;
  SiteInfo siteInfo;  // sometimes this is null when used from RandomSetter

  private static Logger logger = LoggerFactory.getLogger(RandomSetDao.class);

  public RandomSetDao(SiteInfo siteInfo, Database db) {
    this.database = db;
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }

  /**
   * Do not use to fetch a list of participants.  For that you need a siteInfo
   */
  public RandomSetDao(Long siteId, Database db) {
    this.database = db;
    this.siteInfo = null;
    this.siteId = siteId;
  }

  /**
   * Get all TreatmentSets assigned to a patient or not, dropping ones that are old/closed.
   * Ones that are not assigned yet have a state of RandomSetParticipant.State.Unset.
   *
   * This requires this DAO be initialized with a SiteInfo, not just a siteId
   */
  public ArrayList<RandomSetParticipant> getTreatmentSetParticipantsForUi(String patientId) {
    return getPatientParticipation(patientId, true, false, RSType.TreatmentSet, true);
  }


  public ArrayList<RandomSetParticipant> getTreatmentSetParticipantsForReport(String patientId) {
    // Ignore ones that are for researching or closed studies,
    // or if the patient was disqualified for the study or declined, or is Unset for a reason
    return getPatientParticipation(patientId, true, true, RSType.TreatmentSet, false);
  }


  /**
   * Returns all RandomSets of the passed type that are assigned to a patient,
   * possibly dropping ones that are old/closed, possibly adding ones that are not assigned to the patient.
   *
   * This requires this DAO be initialized with a SiteInfo, not just a siteId
   * @param openOnly If true, filter out sets that are closed.
   * @param assignedOnly If true, filter out sets that are not Assigned, Withdrawn or Closed.
   * @param randomSetType If not null, only fetch ones of this type
   * @param addUnassigned If true, add RandomSets that are not assigned to this patient in Unset state
   */
  public ArrayList<RandomSetParticipant> getPatientParticipation(String patientId,
                                                                 boolean openOnly, boolean assignedOnly,
                                                                 RSType randomSetType, boolean unassigned) {
    return getPartOrHist(true, patientId, openOnly, assignedOnly, randomSetType, unassigned);
  }


  private ArrayList<RandomSetParticipant> getPartOrHist(boolean part, String patientId,
                                                        boolean openOnly, boolean assignedOnly,
                                                        RSType randomSetType, boolean unassigned) {
    String q = part ? SELECT_PATIENT_PART : SEL_PART_HIST;
    ArrayList<RandomSetParticipant> rspList = database.toSelect(q)
          .argString(patientId).argLong(siteId)
          .query(new RandomSetPartsFetcher(patientId, randomSetType, openOnly, assignedOnly));
    int n = rspList.size();

    if (!unassigned) {
      logger.debug("getPatientParticipation, got "+n+" existing ones");
      return rspList;
    } // else get unassigned ones, too

    for (Entry<String, RandomSetter> entry: siteInfo.getRandomSets()) { // add unassigned candidates
      RandomSetter setter = entry.getValue();
      RandomSet newRSet = setter.getRandomSet();
      if (mustAddRandomSet(rspList, newRSet)) {
        RandomSetParticipant p = new RandomSetParticipant(patientId, newRSet, RandomSetParticipant.State.Unset);
        p = siteInfo.getRegistryCustomizer().disableUiTreatmentSet(database, p);
        if (p != null) {  // if null, customizer doesn't want it shown
          rspList.add(p);
        }
      }
    }
    logger.debug("getPatientParticipation, got "+n+" existing ones and new="+(rspList.size()-n));
    return rspList;
  }


  public ArrayList<RandomSetParticipant> getParticipationHistory(String patientId) {
    return getPartOrHist(false, patientId, false, false, RSType.TreatmentSet, false);
  }


  static private boolean mustAddRandomSet(ArrayList<RandomSetParticipant> list, RandomSet rs) {
    String name = rs.getName();
    if (!rs.getState().stillAddingPatients() || !rs.getType().isForPatientUI()) {
      return false;
    }
    for (RandomSetParticipant part: list) {
      if (part.getName().equals(name)) {
        return false;
      }
    }
    return true;
  }


  static final String RSP_SEQUENCE_NAME = "randomset_part_id_seq";
  static final String RSP_ID_COLUMN = "participant_id";

  static final private String SELECT_PATIENT_PART =
      "SELECT set_name, "+RSP_ID_COLUMN+", state, " // 1,2,3
          + "stratum_name, group_name, "            // 4,5
          + "dt_assigned, dt_withdrawn, reason, "   // 6,7,8
          + "update_time, update_sequence "         // 9, 10
          + "FROM randomset_participant WHERE patient_id = ? AND survey_site_id=?";

  static final private String SEL_PART_HIST =
      SELECT_PATIENT_PART.replace("_participant", "_participant_hist") + " ORDER BY update_sequence";

  class RandomSetPartsFetcher implements RowsHandler<ArrayList<RandomSetParticipant>> {
    final String patientId;
    final RandomSet.RSType type;
    final boolean stillCollectingData;  // the RandomSet is
    final boolean assignedOnly;
    RandomSetPartsFetcher(String patientId, RandomSet.RSType type,
                          boolean stillCollectingData, boolean assignedOnly) {
      this.patientId = patientId;
      this.type = type;
      this.stillCollectingData = stillCollectingData;
      this.assignedOnly = assignedOnly;
    }

    @Override public ArrayList<RandomSetParticipant> process(Rows rs) throws Exception {
      ArrayList<RandomSetParticipant> list = new ArrayList<RandomSetParticipant>();
      while (rs.next()) {
        String rsetName = rs.getStringOrEmpty(1);
        RandomSetter rsetter = siteInfo.getRandomSet(rsetName);
        if (rsetter == null) {
          logger.warn(siteInfo.getIdString()+"No random set named "+rsetName+" exists- removed?");
          continue;
        }
        RandomSet rset = rsetter.getRandomSet();
        if (stillCollectingData && !rset.getState().stillCollectingData())
          continue;
        if (type != null && !rset.getType().equals(type)) {
          continue;
        }
        RandomSetParticipant.State rspState = RandomSetParticipant.State.valueOf(rs.getStringOrEmpty(3));
        if (assignedOnly && !rspState.isAssigned()) {
          continue;
        }
        list.add(new RandomSetParticipant(rs.getLongOrZero(2), patientId, rset, rspState,
            rs.getStringOrEmpty(4), rs.getStringOrEmpty(5),
            rs.getDateOrNull(6), rs.getDateOrNull(7), rs.getStringOrEmpty(8),
            rs.getDateOrNull(9), rs.getLongOrZero(10)));
      }
      return list;
    }
  }


  static final String RSP_INSERT = initInsertParticipation(false);
  static final String RSP_HIST_INSERT = initInsertParticipation(true);

  static final String RSP_TABLE = "randomset_participant";
  static final String RSP_HIST_TABLE = "randomset_participant_hist";

  static final String initInsertParticipation(boolean isHist) {
    String format = "INSERT INTO %s ("
        +RSP_ID_COLUMN+", update_sequence, update_time%s, "
        + " patient_id, survey_site_id, "
        + " set_name, state, stratum_name, group_name, "
        + " reason, dt_assigned, dt_withdrawn) VALUES (?,%s,?%s, ?,?, ?,?,?,?, ?,?,?)";
    return String.format(format,  (isHist ? RSP_HIST_TABLE : RSP_TABLE),
                         (isHist ? ", is_deleted" : ""),        // add in the is_deleted char column
                         (isHist ? "?" : 0),    // initial sequence number is zero
                         (isHist ? ",?" : "")); // add in the is_deleted char column value
  }

  static boolean FreshenDate = true;
  static boolean LeaveDate = false;
  static boolean InitSeq = true;
  static boolean IncrSeq = false;
  static String  NotDelete = "N";

  /**
   * The call must add the patient to a group, if appropriate.
   * If it already has a primary key value, the participant is updated,
   * otherwise it's inserted and the table id is set.
   * Either way, one of the dates might also be set, as well.
   */
  public RandomSetParticipant updateOrInsertParticipant(RandomSetParticipant rsp) {
    if (!rsp.changed()) {
      return null;
    } else if (rsp.getRandomsetParticipantId() < 0) {
      insertPartOrHist(rsp, FreshenDate, true, null);
      insertPartOrHist(rsp, LeaveDate,   false, NotDelete);
    } else {
      insertPartOrHist(rsp, FreshenDate, false, NotDelete); // increments sequence and sets date
      updateParticipant(rsp);
    }
    return rsp;
  }


  /**
   * Inserts into the Participant or its History table, refreshing the date and incrementing the sequence when appropriate
   * @param rsp The participant object, which might be changed by this method
   * @param initDate If true, sets the updateTime to the current time
   * @param init If true, sets the ID from the database sequence, else if initDate, increments the updateSequence
   * @param del The value to mark the history row: Y or N
   * @return the RandomSetParticipant passed in, with its ID updated.
   */
  protected RandomSetParticipant insertPartOrHist(RandomSetParticipant rsp, boolean initDate, boolean init, String del) {
    maybeUpdateDates(rsp);
    SqlInsert ins = database.toInsert(init ? RSP_INSERT : RSP_HIST_INSERT);
    Date updateTime = initDate ? rsp.setUpdateTime() : rsp.getUpdateTime();
    // first handle ID, sequence, date and isDeleted
    if (init) {
      ins = ins.argPkSeq(RSP_SEQUENCE_NAME).argDate(updateTime);
    } else {
      long sequence = (initDate ? rsp.incUpdateSequence() : rsp.getUpdateSequence());
      ins = ins.argLong(rsp.getRandomsetParticipantId());
      ins = ins.argLong(sequence).argDate(updateTime).argString(del);
    }
    // the rest is all the same
    ins = ins.argString(rsp.getPatientId())
        .argLong(siteId)
        .argString(rsp.getName())
        .argString(rsp.getState().toString())
        .argString(rsp.getStratumName())
        .argString(rsp.getGroup())
        .argString(rsp.getReason())
        .argDate(rsp.getAssignedDate())
        .argDate(rsp.getWithdrawnDate());
    if (init) {
      return rsp.setRandomsetParticipantId(ins.insertReturningPkSeq(RSP_ID_COLUMN)).resetOriginals();
    }
    ins.insert(1);
    return rsp;
  }


  static String SQL_UPDATE_PART =
      "UPDATE randomset_participant "
      + "SET update_sequence=?, update_time=?, state=?, stratum_name=?, group_name=?, reason=?, dt_assigned=?, dt_withdrawn=? "
      + "WHERE "+RSP_ID_COLUMN+"=?";

  protected RandomSetParticipant updateParticipant(RandomSetParticipant rsp) {
    maybeUpdateDates(rsp);
    database.toUpdate(SQL_UPDATE_PART)
              .argLong(rsp.getUpdateSequence())
              .argDate(rsp.getUpdateTime())
              .argString(rsp.getState().toString())
              .argString(rsp.getStratumName())
              .argString(rsp.getGroup())
              .argString(rsp.getReason())
              .argDate(rsp.getAssignedDate())
              .argDate(rsp.getWithdrawnDate())
              .argLong(rsp.getRandomsetParticipantId())
              .update(1);
    rsp.resetOriginals();
    return rsp;
  }


  private void maybeUpdateDates(RandomSetParticipant rsp) {
    if (rsp.getState().isAnAssignedState() && rsp.getAssignedDate() == null) {
      // if it was already assigned, keep the initial assigned date
      rsp.setAssignedDate();
    }
    if (rsp.getState().isWithdrawn() && (rsp.getWithdrawnDate() == null || !rsp.getOriginalState().isWithdrawn())) {
      // always keep the late withdrawn date
      rsp.setWithdrawnDate(new Date());
    }
  }


  // ==== RandomSet

  static final String STATE_INSERT = "INSERT INTO randomset "
      + "(survey_site_id, set_name, algorithm, state, "
      + "dt_end, target_size, study_length_days) values (?,?,?,'Enrolling',  ?,?,?)";

  public void insertSet(RandomSet rset) {
    try {
      database.toInsert(STATE_INSERT)
          .argLong(rset.getSiteId())
          .argString(rset.getName())
          .argString(rset.getAlgorithm())
          .argDate(rset.getEndDate())
          .argInteger(rset.getTargetPopulation())
          .argInteger(rset.getDurationDays())
          .insert(1);
    } catch (Exception ex) {
      logger.error(ex.getLocalizedMessage(), ex);
    }
  }


  static final String STRATUM_INSERT = "INSERT INTO randomset_stratum "
      + "(survey_site_id, set_name, stratum_name) values (?,?,?)";

  /**
   * Used during initialization. When a RandomSet is read in, if it's not in the database,
   * it and its stratums are created.
   */
  public void insertSetStratum(RandomSet rset, String stratumName) {
    database.toInsert(STRATUM_INSERT)
    .argLong(rset.getSiteId())
    .argString(rset.getName())
    .argString(stratumName)
    .insert(1);
    logger.debug("Inserted RandomSetStratum({}, {}, {})", rset.getSiteId(), rset.getName(), stratumName);
  }


  /**
   * Initializes the state of a RandomSet from the database. Sets its internal life-cycle state,
   * target size, and length of study as well as counter and data for the random-algorithm.
   * Returns Boolean.FALSE if the row wasn't found.
   */
  public Boolean fetchRandomState(RandomSet rset) {
    String q = "SELECT state, dt_end, target_size, study_length_days "
             + "FROM randomset WHERE survey_site_id=? AND set_name=?";
    return database.toSelect(q).argLong(rset.getSiteId()).argString(rset.getName())
             .query(new RowsHandler<Boolean>() {
      @Override
      public Boolean process(Rows rs) throws Exception {
        if (!rs.next()) {
          return Boolean.FALSE;
        }
        rset.setState(rs.getStringOrEmpty(1));
        rset.setEndDate(rs.getDateOrNull(2));
        rset.setTargetPopulation(rs.getIntegerOrZero(3));
        rset.setPatientDuration(rs.getIntegerOrZero(4));
        return Boolean.TRUE;
      }});
  }


  /**
   * Used by RandomSetters to get the counter and randomizing data from the DB,
   * use and change them, after which they database row is updated and unlocked.
   *
   * <p>The row is fetched, then the caller's shouldUpdate(counter, data) is called.
   * The caller uses the data then updates the
   */
  public abstract static class RandomSetStateAccessor {
    private long counter;
    private String data;

    /**
     * Optionally called by shouldUpdate() to update the stratum's random data value.
     */
    public void setData(String s) {
      data = s;
    }

    /**
     * Optionally called by shouldUpdate() to update the stratum's random counter value.
     */
    public void setCounter(long c) {
      counter = c;
    }

    /**
     * If shouldUpdate() returns true, this Dao uses this method to get the new data to put in the database.
     */
    public String getData() {
      return data;
    }

    /**
     * If shouldUpdate() returns true, this Dao uses this method to get the new counter to put in the database.
     */
    public long getCounter() {
      return counter;
    }

    /**
     * Return true after updating the counter and data instance variables if you want to update the
     * RandomSet's random data.
     * <p>
     * For instance, for KSort, this
     * <br>1) checks if the data is null/empty and if so, creates it.
     * <br>2) Then it lops off the first random assignment, and uses it
     * <br>3) and sets this.data to the rest of the random data.
     * <br>4) increments the counter and returns true.
     */
    public abstract boolean shouldUpdate(long counter, String data);

    /**
     * This might be overridden, say, to just log the message...
     */
    protected void rowMissing(long siteId, String rsetName) {
      throw new RuntimeException("No RandomSet data found in DB for site="+siteId+": "+rsetName);
    }
  }


  /**
   * Fetches the counter and randomizing data from the database,
   * calls the accessor to use and change it, then updates the database.
   * @param stratumName
   */
  public Boolean fetchAndUpdateRandomState(RandomSet rset, String stratumName, final RandomSetStateAccessor accessor) {
    if (stratumName == null || stratumName.isEmpty()) {
      throw new RuntimeException("fetchAndUpdateRandomState, rset="+rset.getName()+" stratumName aint set: "+stratumName);
    }
    String q = "SELECT counter, data FROM randomset_stratum "
             + "WHERE survey_site_id=? AND set_name=? AND stratum_name=? FOR UPDATE";
    return database.toSelect(q)
        .argLong(rset.getSiteId())
        .argString(rset.getName())
        .argString(stratumName)
        .query(new RowsHandler<Boolean>() {
      @Override
      public Boolean process(Rows rs) throws Exception {
        if (!rs.next()) {
          accessor.rowMissing(rset.getSiteId(), rset.getName());
          return Boolean.FALSE;
        }
        accessor.setCounter(rs.getIntegerOrZero(1));
        accessor.setData(rs.getStringOrEmpty(2));
        if (accessor.shouldUpdate(accessor.getCounter(), accessor.getData())) {
          database.toUpdate("UPDATE randomset_stratum SET data=?, counter=? "
                          + "WHERE survey_site_id=? AND set_name=? AND stratum_name=?")
          .argString(accessor.data)
          .argLong(accessor.counter)
          .argLong(rset.getSiteId())
          .argString(rset.getName())
          .argString(stratumName)
          .update(1);
        }
        return Boolean.TRUE;
      }});
  }


  static final String COUNT_GROUPS =
      "SELECT state, stratum_name, group_name, count(*), min(dt_withdrawn)" // 1 2 3
          + ", min(dt_assigned), max(dt_assigned)"  // 4 5
        //+ ", min(dt_withdrawn), max(dt_withdrawn) " // 6 7
          + "FROM randomset_participant "
          + "WHERE survey_site_id=? AND set_name=? "
          + "GROUP BY state, stratum_name, group_name";


  /**
   * Initialize's a RandomSet's stratum and group counts and first/last dates
   */
  public void initPatientCount(RandomSetter rsetter) {
    /*  Omit until needed
    final RandomSet rset = rsetter.getRandomSet();
    database.toSelect(COUNT_GROUPS).argLong(rset.getSiteId()).argString(rset.getName())
    .query(new RowsHandler<Boolean>() {
      @Override
      public Boolean process(Rows rs) throws Exception {
        // ensure this RandomSet's instance variables are unset
        Date firstAssignedDate = null,  lastAssignedDate = null;
        int numDeclined = 0,  numPatients = 0,  numWithdrawn = 0,  numExcluded = 0;
        while (rs.next()) {
          String stateStr = rs.getStringOrEmpty(1);
          String gname = rs.getStringOrEmpty(2);
          RandomSetParticipant.State state;
          state = RandomSetParticipant.State.valueOf(stateStr);
          int count = rs.getIntegerOrZero(3);
          Date gfirstAssigned = rs.getDateOrNull(5);
          Date glastAssigned = rs.getDateOrNull(6);

          RandomStratum strat =
          RandomGroup g = groupHash.get(gname);
          rsetter.incRsetForState(state, g, count);  // changed to this state from the original state
          if ((firstAssignedDate == null) || (gfirstAssigned != null && gfirstAssigned.before(firstAssignedDate))) {
            firstAssignedDate = gfirstAssigned;
          }
          if ((lastAssignedDate == null) || (glastAssigned != null && lastAssignedDate.before(glastAssigned))) {
            lastAssignedDate = gfirstAssigned;
          }
        }
        rset.initData(numPatients, numWithdrawn, numDeclined, numExcluded, firstAssignedDate, lastAssignedDate);
        return Boolean.TRUE;
      }
    });
    /* */
  }


  // RandomSetDao.Supplier, for testability

  static public class Supplier {
    public RandomSetDao get(Long siteId, Database db) {
      return new RandomSetDao(siteId, db);
    }

    public RandomSetDao get(SiteInfo siteInfo, Database db) {
      return new RandomSetDao(siteInfo, db);
    }
  }

}
