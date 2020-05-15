/*
 * Copyright 2016-2017 The Board of Trustees of The Leland Stanford Junior University.
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

import edu.stanford.registry.server.DataTableObjectConverter;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlArgs;
import com.github.susom.database.SqlInsert;
import com.github.susom.database.SqlSelect;

/**
 * Created by tpacht on 12/5/2016.
 */
public class ActivityDao {

  public Long siteId;
  public Database database;

  private final static String SELECT_JUST_ACTIVITY =
      "SELECT a.activity_id, a.survey_site_id, a.patient_id, a.activity_dt, a.activity_type, " +
          "  a.token, a.meta_version, a.dt_created, a.dt_changed, a.user_principal_id, " +
          "  a.assessment_reg_id, 'admin' as display_name " +
          "FROM activity a";

  private final static String SELECT_ACTIVITY =
      "select a.activity_id, a.survey_site_id, a.patient_id, a.activity_dt, a.activity_type, " +
          "  a.token, a.meta_version, a.dt_created, a.dt_changed, a.user_principal_id, " +
          "  coalesce(a.assessment_reg_id, sr.assessment_reg_id) assessment_reg_id, u.display_name " +
          "from activity a  " +
          "  left join survey_registration sr on sr.survey_site_id = a.survey_site_id and sr.token = a.token " +
          "  left join user_principal u on u.user_principal_id = a.user_principal_id ";

  private final static String SELECT_NOTIFICATION =
      "select 0 as activity_id, n.survey_site_id, n.patient_id, n.email_dt as activity_dt, 'Email sent' as activity_type, " +
          "  null as token, n.meta_version, n.dt_created, n.dt_changed, null as user_principal_id, " +
          "  n.assessment_reg_id, null as display_name " +
          "from notification n ";
  private final static String ACTIVITY_ASSESSMENT_INCOMPLETE =
      "exists " +
          "( select * from survey_registration sr2 " +
          "  where sr2.assessment_reg_id = coalesce(a.assessment_reg_id, sr.assessment_reg_id) and " +
          "  not exists " +
          "  ( select * from activity a2 " +
          "    where a2.survey_site_id = sr2.survey_site_id and a2.token = sr2.token and " +
          "      a2.activity_type = '" + Constants.ACTIVITY_COMPLETED + "' " +
          "    ) " +
          ") ";

  private final static String NOTIFICATION_ASSESSMENT_INCOMPLETE =
      "exists " +
          "( select * from survey_registration sr2 " +
          "  where sr2.assessment_reg_id = n.assessment_reg_id and " +
          "  not exists " +
          "  ( select * from activity a2 " +
          "    where a2.survey_site_id = sr2.survey_site_id and a2.token = sr2.token and " +
          "      a2.activity_type = '" + Constants.ACTIVITY_COMPLETED + "' " +
          "    ) " +
          ") ";

  private final static String ORDER_BY = " order by patient_id, assessment_reg_id, token, activity_dt ";
  private final static String ORDER_BY_LATEST = "order by activity_dt desc";

  private static Logger logger = LoggerFactory.getLogger(ActivityDao.class);

  public ActivityDao(Database database, Long siteId) {
    this.database = database;
    this.siteId = siteId;
    if (this.database == null) throw new RuntimeException("Database is null");
  }

  public static String getActivityStatement(boolean includeCompleted) {
    if (includeCompleted) {
      return getSelectAllBetweenDates();
    } else {
      return getSelectUncompletedBetweenDates();
    }
  }
  public ArrayList<Activity> getActivity(Date fromTime, Date toTime,
                                                boolean includeCompleted) {
    String sql = getActivityStatement(includeCompleted);

    if (database == null) {
      logger.info("database is null");
    }

   logger.info("sql = " + sql);
    ArrayList<Activity> activities = null;
    try {
      activities = database.toSelect(sql)
          .argLong(siteId).argDate(fromTime).argDate(toTime)
          .argLong(siteId).argDate(fromTime).argDate(toTime)
          .query(getActivityHandler());
    } catch (Exception e) {
      logger.error(e.toString(), e);
    }

    if (activities == null) {
      logger.info("activities result is null");
    }
    return activities;
  }

  public Activity getLatestActivity(String patientId, String activityType) {
    String sql = SELECT_JUST_ACTIVITY +
        " WHERE survey_site_id=? AND patient_id=? AND activity_type=? " + ORDER_BY_LATEST;
    SqlSelect select = database.toSelect(sql).
        argLong(siteId).
        argString(patientId).
        argString(activityType).withMaxRows(1);
    ArrayList<Activity> list = select.query(getActivityHandler());
    return list.size() > 0 ? list.get(0) : null;
  }

  /**
   * Only return the single latest activity
   * @param includeCompleted if false, don't return activities if the survey was ultimately completed
   * @return
   */
  public Activity getPatientsActivityLatest(String patientId, boolean includeCompleted) {
    ArrayList<Activity> activities = getPatientsActivity(patientId, includeCompleted, true);
    if (activities.size() == 0)
      return null;

    return activities.get(0);
  }

  public ArrayList<Activity> getPatientsActivity(String patientId, boolean includeCompleted) {
    return getPatientsActivity(patientId, includeCompleted, false);
  }


  public ArrayList<Activity> getPatientsActivity(String patientId, boolean includeCompleted, boolean justLatest) {
    String orderBy = (justLatest) ? ORDER_BY_LATEST : ORDER_BY;
    String sql;
    if (includeCompleted) {
      sql = SELECT_ACTIVITY + " WHERE a.SURVEY_SITE_ID = ? AND a.PATIENT_ID = ? "
          + " UNION "
          + SELECT_NOTIFICATION + " WHERE n.SURVEY_SITE_ID = ? AND n.email_dt is not null AND n.PATIENT_ID = ? "
          + orderBy;
    } else {
      sql = SELECT_ACTIVITY + " WHERE a.SURVEY_SITE_ID = ? AND a.PATIENT_ID = ? "
          + " AND " + ACTIVITY_ASSESSMENT_INCOMPLETE
          + " UNION "
          + SELECT_NOTIFICATION + " WHERE n.SURVEY_SITE_ID = ? AND n.email_dt is not null  AND n.PATIENT_ID = ? "
          + " AND " + NOTIFICATION_ASSESSMENT_INCOMPLETE
          + orderBy;
    }
    SqlSelect select = database.toSelect(sql).
        argLong(siteId).
        argString(patientId).
        argLong(siteId).
        argString(patientId);

    if (justLatest)
       select = select.withMaxRows(1);
     return select.query(getActivityHandler());
  }

  public ArrayList<Activity> getActivityByToken(String token) {

    return getActivityByToken(token, null);
  }

  public ArrayList<Activity> getActivityByToken(String token, String activityType) {
    logger.debug("getActivityByToken(" + token + "," + activityType + ")");
    SqlArgs args = new SqlArgs();
    String sql = SELECT_ACTIVITY + " WHERE a.SURVEY_SITE_ID =  ? AND a.TOKEN = ? AND a.TOKEN = sr.TOKEN";
    args.argLong(siteId);
    args.argString(token);
    if (activityType != null) {
      args.argString(activityType);
      sql = sql + " AND ACTIVITY_TYPE = ?";
    }
    return database.toSelect(sql).apply(args).query(getActivityHandler());
  }

  public ArrayList<Activity> getActivityByAssessmentId(AssessmentId assessmentId, String activityType) {
    return getActivityByAssessmentId(assessmentId.getId(), activityType);
  }

  public ArrayList<Activity> getActivityByAssessmentId(Long assessmentId, String activityType) {
    logger.debug("getActivityByAssessmentId(" + assessmentId + "," + activityType + ", " + siteId + ")");
    SqlArgs args = new SqlArgs();
    String sql = SELECT_ACTIVITY + " WHERE a.SURVEY_SITE_ID = ? AND ACTIVITY_TYPE = ? AND a.ASSESSMENT_REG_ID = ? ";
    args.argLong(siteId);
    args.argString(activityType);
    args.argLong(assessmentId);
    return database.toSelect(sql).apply(args).query(getActivityHandler());
  }

  public void createActivity(Activity activity) {
    String sql ="INSERT INTO ACTIVITY (ACTIVITY_ID, SURVEY_SITE_ID, PATIENT_ID, ACTIVITY_DT, ACTIVITY_TYPE, TOKEN, ASSESSMENT_REG_ID,"
        + " META_VERSION, DT_CREATED, DT_CHANGED, USER_PRINCIPAL_ID)"
        + " VALUES (:pk, :site, ?, :now, ?, ?, ?, ?, :now, null, " + (hasUserPrincipalId(activity) ? "?" : "null") + ")";
    Date now = database.nowPerApp();
    SqlInsert insert;
    if (hasUserPrincipalId(activity)) {
      insert = database.toInsert(sql)
          .argPkSeq(":pk", "activity_id_seq")
          .argLong(":site", siteId)
          .argDate(":now", now)
          .argString(activity.getPatientId())
          .argString(activity.getActivityType())
          .argString(activity.getToken())
          .argLong(activity.getAssessmentRegId())
          .argInteger(activity.getMetaVersion())
          .argLong(activity.getUserPrincipalId());
    } else {
      insert = database.toInsert(sql)
          .argPkSeq(":pk", "activity_id_seq")
          .argLong(":site", siteId)
          .argDate(":now", now)
          .argString(activity.getPatientId())
          .argString(activity.getActivityType())
          .argString(activity.getToken())
          .argLong(activity.getAssessmentRegId())
          .argInteger(activity.getMetaVersion());
    }


    Long activityId =  insert.insertReturningPkSeq("activity_id");
    activity.setActivityId(activityId);
    activity.setActivityDt(now);
    activity.setDtCreated(now);
  }

  public int deleteActivity(Activity act) {
    String sql = "DELETE FROM ACTIVITY WHERE ACTIVITY_ID = ?";
    return database.toDelete(sql)
        .argLong(act.getActivityId())
        .update();
  }

  private RowsHandler<ArrayList<Activity>> getActivityHandler() {
    return new RowsHandler<ArrayList<Activity>>() {
      @Override
      public ArrayList<Activity> process(Rows rs) throws Exception {
        return DataTableObjectConverter.convertToObjects(rs, Activity.class);
      }
    };
  }

  private static String getSelectAllBetweenDates() {
    String sql =
        SELECT_ACTIVITY + " WHERE a.SURVEY_SITE_ID = ? "
            + " AND sr.SURVEY_DT BETWEEN ? AND ? "
            + " UNION "
            + SELECT_NOTIFICATION + " WHERE n.SURVEY_SITE_ID = ? "
            + " AND n.email_dt BETWEEN ? AND ? "
            + ORDER_BY;
    return sql;
  }

  private static String getSelectUncompletedBetweenDates() {
    String sql =
        SELECT_ACTIVITY + " WHERE a.SURVEY_SITE_ID = ?  AND sr.SURVEY_DT BETWEEN ? AND ? "
            + " AND " + ACTIVITY_ASSESSMENT_INCOMPLETE
            + " UNION "
            + SELECT_NOTIFICATION + " WHERE n.SURVEY_SITE_ID = ? AND n.email_dt BETWEEN ? AND ? "
            + " AND " + NOTIFICATION_ASSESSMENT_INCOMPLETE
            + ORDER_BY;
    return sql;
  }
  private boolean hasUserPrincipalId(Activity activity) {
    return (activity != null && activity.getUserPrincipalId() != null);
  }

}
