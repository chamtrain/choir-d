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

package edu.stanford.survey.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.codec.digest.DigestUtils;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlArgs;

/**
 * Store and retrieve various survey-related information.
 */
public class SurveyDao {
  private Supplier<Database> database;

  public SurveyDao(Supplier<Database> database) {
    this.database = database;
  }

  public void addSurveySite(Long siteId, String urlParam, String displayName) {
    database.get().toInsert("insert into survey_site (survey_site_id, url_param, display_name) values (?,?,?)")
        .argLong(siteId).argString(urlParam).argString(displayName).insert(1);
  }

  public Long findSurveySiteId(String urlParam) {
    return database.get().toSelect("select survey_site_id from survey_site where url_param=?")
        .argString(urlParam).queryLongOrNull();
  }

  public void addSurveyCompletePush(Long recipientId,Long siteId, String recipientName, String displayName) {
    database.get().toInsert("insert into survey_complete_push (survey_recipient_id, survey_site_id, recipient_name, "
        + "recipient_display_name, pushed_survey_sequence, failed_count, is_enabled) values (?,?,?,?, null, 0, 'Y')")
        .argLong(recipientId).argLong(siteId).argString(recipientName).argString(displayName).insert(1);
  }

  public void createSurveyToken(final SurveyToken st) {
    st.setLastActive(new Date());
    st.setLastSessionNumber(1L);
    st.setSurveyTokenId(database.get().toInsert("insert into survey_token (survey_token_id, survey_site_id, "
        + "survey_token, is_complete, last_step_number, last_session_number) values (:sti,:ssi,:st,:c,:step,1)")
        .argPkSeq(":sti", "survey_seq")
        .argLong(":ssi", st.getSurveySiteId())
        .argString(":st", st.getSurveyToken())
        .argString(":c", st.isComplete() ? "Y" : "N")
        .argLong(":step", st.getLastStepNumber()).insertReturningPkSeq("survey_token_id"));

    database.get().toInsert(
        "insert into survey_session (survey_site_id, survey_token_id, session_number, session_token, "
            + "resume_token, last_active, start_time) values (?,?,1,?,?,?,?)")
        .argLong(st.getSurveySiteId())
        .argLong(st.getSurveyTokenId())
        .argString(st.getSessionToken())
        .argString(st.getResumeToken())
        .argDate(st.getLastActive())
        .argDate(st.getLastActive()).insert(1);
  }

  public SurveyToken findSurveyTokenAndLockIt(final Long siteId, final String surveyToken) {
    return findSurveyTokenInternal("t.survey_site_id=? and t.survey_token=?",
        new SqlArgs().argLong(siteId).argString(surveyToken));
  }

  public SurveyToken findSurveyTokenBySessionAndLockIt(final String sessionToken) {
    return findSurveyTokenInternal("s.session_token=?", new SqlArgs().argString(sessionToken));
  }

  public SurveyToken findSurveyTokenByResumeAndLockIt(final String resumeToken) {
    return findSurveyTokenInternal("s.resume_token=?", new SqlArgs().argString(resumeToken));
  }

  private SurveyToken findSurveyTokenInternal(String criteriaSql, SqlArgs args) {
    Database db = database.get();
    return db.toSelect("select t.survey_token_id, t.survey_site_id, t.survey_token, s.session_token, "
        + "s.resume_token, s.last_active, t.is_complete, t.last_step_number, t.last_session_number from survey_token t "
        + "join survey_session s on (t.survey_site_id=s.survey_site_id and t.survey_token_id=s.survey_token_id and "
        + "t.last_session_number=s.session_number) where " + criteriaSql
        + db.when().derby("").other(" for update")).withArgs(args).query(
        new RowsHandler<SurveyToken>() {
          @Override
          public SurveyToken process(Rows rs) throws Exception {
            if (rs.next()) {
              SurveyToken st = new SurveyToken();
              st.setSurveyTokenId(rs.getLongOrNull(1));
              st.setSurveySiteId(rs.getLongOrNull(2));
              st.setSurveyToken(rs.getStringOrNull(3));
              st.setSessionToken(rs.getStringOrNull(4));
              st.setResumeToken(rs.getStringOrNull(5));
              st.setLastActive(rs.getDateOrNull(6));
              st.setComplete("Y".equals(rs.getStringOrNull(7)));
              st.setLastStepNumber(rs.getLongOrNull(8));
              st.setLastSessionNumber(rs.getLongOrNull(9));
              return st;
            } else {
              return null;
            }
          }
        });
  }

  /**
   * Note this does not allow you to update all attributes of SurveyToken, just
   * is complete, and last step number. Last active timestamp will be automatically updated to the
   * current time.
   */
  public void updateSurveyTokenTouchSession(SurveyToken surveyToken) {
    surveyToken.setLastActive(new Date());
    database.get().toUpdate("update survey_token set is_complete=?, last_step_number=? where survey_token_id=?")
        .argString(surveyToken.isComplete() ? "Y" : "N")
        .argLong(surveyToken.getLastStepNumber())
        .argLong(surveyToken.getSurveyTokenId()).update(1);
    database.get().toUpdate("update survey_session set last_active=? where survey_site_id=? and survey_token_id=? "
        + "and session_number=?")
        .argDate(surveyToken.getLastActive())
        .argLong(surveyToken.getSurveySiteId())
        .argLong(surveyToken.getSurveyTokenId())
        .argLong(surveyToken.getLastSessionNumber()).update(1);
    checkSurveyComplete(surveyToken);
  }

  public void updateSurveyTokenInvalidateSession(SurveyToken surveyToken) {
    surveyToken.setLastActive(new Date());
    database.get().toUpdate("update survey_session set last_active=?, expired_time=?, expired_reason='I' where "
        + "survey_site_id=? and survey_token_id=? and session_number=?")
        .argDate(surveyToken.getLastActive())
        .argDate(surveyToken.getLastActive())
        .argLong(surveyToken.getSurveySiteId())
        .argLong(surveyToken.getSurveyTokenId())
        .argLong(surveyToken.getLastSessionNumber()).update(1);
    database.get().toUpdate("update survey_token set is_complete=?, last_step_number=? where survey_token_id=?")
        .argString(surveyToken.isComplete() ? "Y" : "N")
        .argLong(surveyToken.getLastStepNumber())
        .argLong(surveyToken.getSurveyTokenId()).update(1);
    checkSurveyComplete(surveyToken);
  }

  public void updateSurveyTokenResumeSession(final SurveyToken st, String newSessionToken, String newResumeToken) {
    updateSurveyTokenNewSession(st, newSessionToken, newResumeToken, "R");
  }

  public void updateSurveyTokenRestartSession(final SurveyToken st, String newSessionToken, String newResumeToken) {
    updateSurveyTokenNewSession(st, newSessionToken, newResumeToken, "S");
  }

  private void updateSurveyTokenNewSession(final SurveyToken st, String newSessionToken, String newResumeToken,
                                           String expireReason) {
    st.setLastActive(new Date());
    database.get().toUpdate("update survey_session set expired_time=?, expired_reason=? where "
        + "survey_site_id=? and survey_token_id=? and session_number=?")
        .argDate(st.getLastActive())
        .argString(expireReason)
        .argLong(st.getSurveySiteId())
        .argLong(st.getSurveyTokenId())
        .argLong(st.getLastSessionNumber()).update(1);
    st.setSessionToken(newSessionToken);
    st.setResumeToken(newResumeToken);
    st.setLastSessionNumber(st.getLastSessionNumber() + 1);
    database.get().toInsert(
        "insert into survey_session (survey_site_id, survey_token_id, session_number, session_token, "
            + "resume_token, last_active, start_time) values (?,?,?,?,?,?,?)")
        .argLong(st.getSurveySiteId())
        .argLong(st.getSurveyTokenId())
        .argLong(st.getLastSessionNumber())
        .argString(st.getSessionToken())
        .argString(st.getResumeToken())
        .argDate(st.getLastActive())
        .argDate(st.getLastActive()).insert(1);
    database.get().toUpdate("update survey_token set is_complete=?, last_step_number=?, last_session_number=? "
        + "where survey_token_id=?")
        .argString(st.isComplete() ? "Y" : "N")
        .argLong(st.getLastStepNumber())
        .argLong(st.getLastSessionNumber())
        .argLong(st.getSurveyTokenId()).update(1);
    checkSurveyComplete(st);
  }

  /**
   * Upon completion of a survey we record it in a separate table to help facilitate
   * push/pull notifications with once and only once semantics.
   */
  private void checkSurveyComplete(final SurveyToken st) {
    if (st.isComplete()) {
      database.get().toInsert("insert into survey_complete (complete_sequence, survey_site_id, survey_token_id) "
          + "values (:pk,?,?)")
          .argLong(st.getSurveySiteId())
          .argLong(st.getSurveyTokenId())
          .argPkSeq(":pk", "survey_complete_seq").insert(1);
    }
  }

  public List<SurveyComplete> findSurveyComplete(long siteId, Long since) {
    return database.get().toSelect("select complete_sequence, survey_site_id, survey_token_id from survey_complete "
        + "where survey_site_id = ? and complete_sequence > ? order by complete_sequence asc")
        .argLong(siteId)
        .argLong(since)
        .query(new RowsHandler<List<SurveyComplete>>() {
      @Override
      public List<SurveyComplete> process(Rows rs) throws Exception {
        List<SurveyComplete> result = new ArrayList<>();
        while (rs.next()) {
          SurveyComplete sc = new SurveyComplete();
          sc.setCompleteSequence(rs.getLongOrNull(1));
          sc.setSurveySiteId(rs.getLongOrNull(2));
          sc.setSurveyTokenId(rs.getLongOrNull(3));
          result.add(sc);
        }
        return result;
      }
    });
  }

  /**
   * Retrieve all rows from the survey_complete_push table.
   */
  public List<SurveyCompletePush> findSurveyCompletePushEnabled(Long siteId) {
    return database.get().toSelect("select survey_recipient_id, survey_site_id, recipient_name, recipient_display_name, "
        + "pushed_survey_sequence, last_pushed_time, failed_survey_sequence, failed_count, last_failed_time, is_enabled"
        + " from survey_complete_push where survey_site_id = ? and is_enabled='Y' for update")
        .argLong(siteId).
        query(new RowsHandler<List<SurveyCompletePush>>() {
      @Override
      public List<SurveyCompletePush> process(Rows rs) throws Exception {
        List<SurveyCompletePush> result = new ArrayList<>();
        while (rs.next()) {
          SurveyCompletePush push = new SurveyCompletePush();
          push.setSurveyRecipientId(rs.getLongOrNull(1));
          push.setSurveySiteId(rs.getLongOrNull(2));
          push.setRecipientName(rs.getStringOrNull(3));
          push.setRecipientDisplayName(rs.getStringOrNull(4));
          push.setPushedSurveySequence(rs.getLongOrNull(5));
          push.setLastPushedTime(rs.getDateOrNull(6));
          push.setFailedSurveySequence(rs.getLongOrNull(7));
          push.setFailedCount(rs.getLongOrNull(8));
          push.setLastFailedTime(rs.getDateOrNull(9));
          push.setEnabled("Y".equals(rs.getStringOrNull(10)));
          result.add(push);
        }
        return result;
      }
    });
  }

  public SurveyCompletePush findSurveyCompletePush(Long siteId, String recipientName) {
    return database.get().toSelect("select survey_recipient_id, survey_site_id, recipient_name,"
        + " recipient_display_name, pushed_survey_sequence, last_pushed_time, failed_survey_sequence,"
        + " failed_count, last_failed_time, is_enabled from survey_complete_push"
        + " where survey_site_id=? and recipient_name=?")
        .argLong(siteId)
        .argString(recipientName)
        .query(new RowsHandler<SurveyCompletePush>() {
          @Override
          public SurveyCompletePush process(Rows rs) throws Exception {
            SurveyCompletePush push = null;
            if (rs.next()) {
              push = new SurveyCompletePush();
              push.setSurveyRecipientId(rs.getLongOrNull(1));
              push.setSurveySiteId(rs.getLongOrNull(2));
              push.setRecipientName(rs.getStringOrNull(3));
              push.setRecipientDisplayName(rs.getStringOrNull(4));
              push.setPushedSurveySequence(rs.getLongOrNull(5));
              push.setLastPushedTime(rs.getDateOrNull(6));
              push.setFailedSurveySequence(rs.getLongOrNull(7));
              push.setFailedCount(rs.getLongOrNull(8));
              push.setLastFailedTime(rs.getDateOrNull(9));
              push.setEnabled("Y".equals(rs.getStringOrNull(10)));
            }
            return push;
          }
        });
  }

  public void insertSurveyCompletePush(SurveyCompletePush push) {
    database.get().toInsert("insert into survey_complete_push (survey_recipient_id, survey_site_id, recipient_name,"
        + " recipient_display_name, pushed_survey_sequence, last_pushed_time, failed_survey_sequence,"
        + " failed_count, last_failed_time, is_enabled) values (?,?,?,?,?,?,?,?,?,?)")
        .argPkSeq("survey_seq")
        .argLong(push.getSurveySiteId())
        .argString(push.getRecipientName())
        .argString(push.getRecipientDisplayName())
        .argLong(push.getPushedSurveySequence())
        .argDate(push.getLastPushedTime())
        .argLong(push.getFailedSurveySequence())
        .argLong(push.getFailedCount())
        .argDate(push.getLastFailedTime())
        .argBoolean(push.isEnabled())
        .insert(1);
  }

  public void updateSurveyCompletePush(SurveyCompletePush push) {
    database.get().toUpdate("update survey_complete_push set recipient_name=?, "
        + "recipient_display_name=?, pushed_survey_sequence=?, last_pushed_time=?, failed_survey_sequence=?, "
        + "failed_count=?, last_failed_time=?, is_enabled=? where survey_recipient_id=?")
        .argString(push.getRecipientName())
        .argString(push.getRecipientDisplayName())
        .argLong(push.getPushedSurveySequence())
        .argDate(push.getLastPushedTime())
        .argLong(push.getFailedSurveySequence())
        .argLong(push.getFailedCount())
        .argDate(push.getLastFailedTime())
        .argString(push.isEnabled() ? "Y" : "N")
        .argLong(push.getSurveyRecipientId()).update(1);
  }

  public List<SurveyAdvance> findSurveyAdvance(Long siteId, Long since) {
    return database.get().toSelect("select advance_sequence, survey_site_id, survey_token_id from survey_advance "
        + "where survey_site_id = ? and advance_sequence > ? order by advance_sequence asc")
        .argLong(siteId).argLong(since).query(new RowsHandler<List<SurveyAdvance>>() {
          @Override
          public List<SurveyAdvance> process(Rows rs) throws Exception {
            List<SurveyAdvance> result = new ArrayList<>();
            while (rs.next()) {
              SurveyAdvance sc = new SurveyAdvance();
              sc.setAdvanceSequence(rs.getLongOrNull(1));
              sc.setSurveySiteId(rs.getLongOrNull(2));
              sc.setSurveyTokenId(rs.getLongOrNull(3));
              result.add(sc);
            }
            return result;
          }
        });
  }

  /**
   * Retrieve all rows from the survey_complete_push table.
   */
  public List<SurveyAdvancePush> findSurveyAdvancePushEnabled(Long siteId) {
    return database.get().toSelect("select survey_recipient_id, survey_site_id, recipient_name, recipient_display_name, "
        + "pushed_survey_sequence, last_pushed_time, failed_survey_sequence, failed_count, last_failed_time, is_enabled"
        + " from survey_advance_push where survey_site_id = ? and is_enabled='Y' for update")
        .argLong(siteId).query(new RowsHandler<List<SurveyAdvancePush>>() {
          @Override
          public List<SurveyAdvancePush> process(Rows rs) throws Exception {
            List<SurveyAdvancePush> result = new ArrayList<>();
            while (rs.next()) {
              SurveyAdvancePush push = new SurveyAdvancePush();
              push.setSurveyRecipientId(rs.getLongOrNull(1));
              push.setSurveySiteId(rs.getLongOrNull(2));
              push.setRecipientName(rs.getStringOrNull(3));
              push.setRecipientDisplayName(rs.getStringOrNull(4));
              push.setPushedSurveySequence(rs.getLongOrNull(5));
              push.setLastPushedTime(rs.getDateOrNull(6));
              push.setFailedSurveySequence(rs.getLongOrNull(7));
              push.setFailedCount(rs.getLongOrNull(8));
              push.setLastFailedTime(rs.getDateOrNull(9));
              push.setEnabled("Y".equals(rs.getStringOrNull(10)));
              result.add(push);
            }
            return result;
          }
        });
  }

  public SurveyAdvancePush findSurveyAdvancePush(Long siteId, String recipientName) {
    return database.get().toSelect("select survey_recipient_id, survey_site_id, recipient_name,"
        + " recipient_display_name, pushed_survey_sequence, last_pushed_time, failed_survey_sequence,"
        + " failed_count, last_failed_time, is_enabled from survey_advance_push"
        + " where survey_site_id=? and recipient_name=?")
        .argLong(siteId)
        .argString(recipientName)
        .query(new RowsHandler<SurveyAdvancePush>() {
          @Override
          public SurveyAdvancePush process(Rows rs) throws Exception {
            SurveyAdvancePush push = null;
            if (rs.next()) {
              push = new SurveyAdvancePush();
              push.setSurveyRecipientId(rs.getLongOrNull(1));
              push.setSurveySiteId(rs.getLongOrNull(2));
              push.setRecipientName(rs.getStringOrNull(3));
              push.setRecipientDisplayName(rs.getStringOrNull(4));
              push.setPushedSurveySequence(rs.getLongOrNull(5));
              push.setLastPushedTime(rs.getDateOrNull(6));
              push.setFailedSurveySequence(rs.getLongOrNull(7));
              push.setFailedCount(rs.getLongOrNull(8));
              push.setLastFailedTime(rs.getDateOrNull(9));
              push.setEnabled("Y".equals(rs.getStringOrNull(10)));
            }
            return push;
          }
        });
  }

  public void insertSurveyAdvancePush(SurveyAdvancePush push) {
    database.get().toInsert("insert into survey_advance_push (survey_recipient_id, survey_site_id, recipient_name,"
        + " recipient_display_name, pushed_survey_sequence, last_pushed_time, failed_survey_sequence,"
        + " failed_count, last_failed_time, is_enabled) values (?,?,?,?,?,?,?,?,?,?)")
        .argPkSeq("survey_seq")
        .argLong(push.getSurveySiteId())
        .argString(push.getRecipientName())
        .argString(push.getRecipientDisplayName())
        .argLong(push.getPushedSurveySequence())
        .argDate(push.getLastPushedTime())
        .argLong(push.getFailedSurveySequence())
        .argLong(push.getFailedCount())
        .argDate(push.getLastFailedTime())
        .argBoolean(push.isEnabled())
        .insert(1);
  }

  public void updateSurveyAdvancePush(SurveyAdvancePush push) {
    database.get().toUpdate("update survey_advance_push set recipient_name=?, "
        + "recipient_display_name=?, pushed_survey_sequence=?, last_pushed_time=?, failed_survey_sequence=?, "
        + "failed_count=?, last_failed_time=?, is_enabled=? where survey_recipient_id=?")
        .argString(push.getRecipientName())
        .argString(push.getRecipientDisplayName())
        .argLong(push.getPushedSurveySequence())
        .argDate(push.getLastPushedTime())
        .argLong(push.getFailedSurveySequence())
        .argLong(push.getFailedCount())
        .argDate(push.getLastFailedTime())
        .argString(push.isEnabled() ? "Y" : "N")
        .argLong(push.getSurveyRecipientId()).update(1);
  }

  public Long findOrCreateUserAgent(Long siteId, String userAgentString) {
    String md5 = DigestUtils.md5Hex(userAgentString);

    Long id = database.get().toSelect("select survey_user_agent_id from survey_user_agent where survey_site_id=? "
        + "and user_agent_md5=?").argLong(siteId).argString(md5).queryLongOrNull();

    if (id == null) {
      // TODO handle the race condition
      id = database.get().toInsert(
          "insert into survey_user_agent (survey_user_agent_id, survey_site_id, user_agent_md5, "
              + "user_agent_str) values (:pk,:site,:md5,:ua)")
          .argPkSeq(":pk", "survey_seq")
          .argLong(":site", siteId)
          .argString(":md5", md5)
          .argString(":ua", userAgentString).insertReturningPkSeq("survey_user_agent_id");
    }

    return id;
  }

  public Long createJson(Long siteId, String json) {
    return database.get().toInsert("insert into survey_json (survey_site_id, survey_json_id, json) "
        + "values (:site,:pk,:json)")
        .argPkSeq(":pk", "survey_seq")
        .argLong(":site", siteId)
        .argString(":json", json).insertReturningPkSeq("survey_json_id");
  }

  public String findJson(Long jsonId) {
    return database.get().toSelect("select json from survey_json where survey_json_id=?")
        .argLong(jsonId).queryStringOrNull();
  }

  public void createProgress(SurveyProgress progress) {
    database.get().toInsert("insert into survey_progress (survey_site_id, survey_token_id, step_number, step_status, "
        + "question_step_number, question_api_compat_level, display_status_json_id, question_json_id, question_time, "
        + "answer_api_compat_level, submit_status_json_id, answer_json_id, answer_time, provider_id, section_id, "
        + "question_id, question_type, user_agent_id, survey_compat_level, call_time_millis, render_time_millis, "
        + "think_time_millis, retry_count, survey_name, "
        + "client_ip_address, device_token) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
        .argLong(progress.getSurveySiteId())
        .argLong(progress.getSurveyTokenId())
        .argLong(progress.getStepNumber())
        .argString(progress.getStepStatus())
        .argLong(progress.getQuestionStepNumber())
        .argLong(progress.getQuestionApiCompatLevel())
        .argLong(progress.getDisplayStatusJsonId())
        .argLong(progress.getQuestionJsonId())
        .argDate(progress.getQuestionTime())
        .argLong(progress.getAnswerApiCompatLevel())
        .argLong(progress.getSubmitStatusJsonId())
        .argLong(progress.getAnswerJsonId())
        .argDate(progress.getAnswerTime())
        .argString(progress.getProviderId())
        .argString(progress.getSectionId())
        .argString(progress.getQuestionId())
        .argString(progress.getQuestionType())
        .argLong(progress.getUserAgentId())
        .argLong(progress.getSurveyCompatLevel())
        .argLong(progress.getCallTimeMillis())
        .argLong(progress.getRenderTimeMillis())
        .argLong(progress.getThinkTimeMillis())
        .argLong(progress.getRetryCount())
        .argString(progress.getSurveyName())
        .argString(progress.getClientIpAddress())
        .argString(progress.getDeviceToken()).insert(1);
  }

  public SurveyProgress findProgress(Long siteId, Long surveyTokenId, Long stepNumber) {
    return database.get().toSelect("select survey_site_id, survey_token_id, step_number, step_status, "
        + "question_step_number, question_api_compat_level, display_status_json_id, question_json_id, question_time, "
        + "answer_api_compat_level, submit_status_json_id, answer_json_id, answer_time, provider_id, section_id, "
        + "question_id, question_type, user_agent_id, survey_compat_level, call_time_millis, render_time_millis, "
        + "think_time_millis, retry_count, survey_name, client_ip_address, device_token "
        + "from survey_progress where survey_site_id=? and survey_token_id=? and step_number=?")
        .argLong(siteId).argLong(surveyTokenId).argLong(stepNumber).query(new RowsHandler<SurveyProgress>() {
          @Override
          public SurveyProgress process(Rows rs) throws Exception {
            if (rs.next()) {
              SurveyProgress progress = new SurveyProgress();
              progress.setSurveySiteId(rs.getLongOrNull(1));
              progress.setSurveyTokenId(rs.getLongOrNull(2));
              progress.setStepNumber(rs.getLongOrNull(3));
              progress.setStepStatus(rs.getStringOrNull(4));
              progress.setQuestionStepNumber(rs.getLongOrNull(5));
              progress.setQuestionApiCompatLevel(rs.getLongOrNull(6));
              progress.setDisplayStatusJsonId(rs.getLongOrNull(7));
              progress.setQuestionJsonId(rs.getLongOrNull(8));
              progress.setQuestionTime(rs.getDateOrNull(9));
              progress.setAnswerApiCompatLevel(rs.getLongOrNull(10));
              progress.setSubmitStatusJsonId(rs.getLongOrNull(11));
              progress.setAnswerJsonId(rs.getLongOrNull(12));
              progress.setAnswerTime(rs.getDateOrNull(13));
              progress.setProviderId(rs.getStringOrNull(14));
              progress.setSectionId(rs.getStringOrNull(15));
              progress.setQuestionId(rs.getStringOrNull(16));
              progress.setQuestionType(rs.getStringOrNull(17));
              progress.setUserAgentId(rs.getLongOrNull(18));
              progress.setSurveyCompatLevel(rs.getLongOrNull(19));
              progress.setCallTimeMillis(rs.getLongOrNull(20));
              progress.setRenderTimeMillis(rs.getLongOrNull(21));
              progress.setThinkTimeMillis(rs.getLongOrNull(22));
              progress.setRetryCount(rs.getLongOrNull(23));
              progress.setSurveyName(rs.getStringOrNull(24));
              progress.setClientIpAddress(rs.getStringOrNull(25));
              progress.setDeviceToken(rs.getStringOrNull(26));
              return progress;
            }
            return null;
          }
        });
  }

  public SurveyProgress findProgressAnsweredByQuestionId(Long siteId, Long surveyTokenId, String questionId) {
    return database.get().toSelect("select survey_site_id, survey_token_id, step_number, step_status, "
        + "question_step_number, question_api_compat_level, display_status_json_id, question_json_id, question_time, "
        + "answer_api_compat_level, submit_status_json_id, answer_json_id, answer_time, provider_id, section_id, "
        + "question_id, question_type, user_agent_id, survey_compat_level, call_time_millis, render_time_millis, "
        + "think_time_millis, retry_count, survey_name, client_ip_address, device_token "
        + "from survey_progress where survey_site_id=? and survey_token_id=? and step_status='A' and question_id=? "
        + "order by step_number desc")
        .argLong(siteId).argLong(surveyTokenId).argString(questionId).query(new RowsHandler<SurveyProgress>() {
          @Override
          public SurveyProgress process(Rows rs) throws Exception {
            if (rs.next()) {
              SurveyProgress progress = new SurveyProgress();
              progress.setSurveySiteId(rs.getLongOrNull(1));
              progress.setSurveyTokenId(rs.getLongOrNull(2));
              progress.setStepNumber(rs.getLongOrNull(3));
              progress.setStepStatus(rs.getStringOrNull(4));
              progress.setQuestionStepNumber(rs.getLongOrNull(5));
              progress.setQuestionApiCompatLevel(rs.getLongOrNull(6));
              progress.setDisplayStatusJsonId(rs.getLongOrNull(7));
              progress.setQuestionJsonId(rs.getLongOrNull(8));
              progress.setQuestionTime(rs.getDateOrNull(9));
              progress.setAnswerApiCompatLevel(rs.getLongOrNull(10));
              progress.setSubmitStatusJsonId(rs.getLongOrNull(11));
              progress.setAnswerJsonId(rs.getLongOrNull(12));
              progress.setAnswerTime(rs.getDateOrNull(13));
              progress.setProviderId(rs.getStringOrNull(14));
              progress.setSectionId(rs.getStringOrNull(15));
              progress.setQuestionId(rs.getStringOrNull(16));
              progress.setQuestionType(rs.getStringOrNull(17));
              progress.setUserAgentId(rs.getLongOrNull(18));
              progress.setSurveyCompatLevel(rs.getLongOrNull(19));
              progress.setCallTimeMillis(rs.getLongOrNull(20));
              progress.setRenderTimeMillis(rs.getLongOrNull(21));
              progress.setThinkTimeMillis(rs.getLongOrNull(22));
              progress.setRetryCount(rs.getLongOrNull(23));
              progress.setSurveyName(rs.getStringOrNull(24));
              progress.setClientIpAddress(rs.getStringOrNull(25));
              progress.setDeviceToken(rs.getStringOrNull(26));
              return progress;
            }
            return null;
          }
        });
  }

  public List<SurveyProgress> findProgressAnsweredByProvider(Long siteId, Long surveyTokenId, String providerId) {
    return database.get().toSelect("select survey_site_id, survey_token_id, step_number, step_status, "
        + "question_step_number, question_api_compat_level, display_status_json_id, question_json_id, question_time, "
        + "answer_api_compat_level, submit_status_json_id, answer_json_id, answer_time, provider_id, section_id, "
        + "question_id, question_type, user_agent_id, survey_compat_level, call_time_millis, render_time_millis, "
        + "think_time_millis, retry_count, survey_name, client_ip_address, device_token "
        + "from survey_progress where survey_site_id=? and survey_token_id=? and step_status='A' and provider_id=? "
        + "order by step_number")
        .argLong(siteId).argLong(surveyTokenId).argString(providerId).query(new RowsHandler<List<SurveyProgress>>() {
          @Override
          public List<SurveyProgress> process(Rows rs) throws Exception {
            List<SurveyProgress> result = new ArrayList<>();
            while (rs.next()) {
              SurveyProgress progress = new SurveyProgress();
              progress.setSurveySiteId(rs.getLongOrNull(1));
              progress.setSurveyTokenId(rs.getLongOrNull(2));
              progress.setStepNumber(rs.getLongOrNull(3));
              progress.setStepStatus(rs.getStringOrNull(4));
              progress.setQuestionStepNumber(rs.getLongOrNull(5));
              progress.setQuestionApiCompatLevel(rs.getLongOrNull(6));
              progress.setDisplayStatusJsonId(rs.getLongOrNull(7));
              progress.setQuestionJsonId(rs.getLongOrNull(8));
              progress.setQuestionTime(rs.getDateOrNull(9));
              progress.setAnswerApiCompatLevel(rs.getLongOrNull(10));
              progress.setSubmitStatusJsonId(rs.getLongOrNull(11));
              progress.setAnswerJsonId(rs.getLongOrNull(12));
              progress.setAnswerTime(rs.getDateOrNull(13));
              progress.setProviderId(rs.getStringOrNull(14));
              progress.setSectionId(rs.getStringOrNull(15));
              progress.setQuestionId(rs.getStringOrNull(16));
              progress.setQuestionType(rs.getStringOrNull(17));
              progress.setUserAgentId(rs.getLongOrNull(18));
              progress.setSurveyCompatLevel(rs.getLongOrNull(19));
              progress.setCallTimeMillis(rs.getLongOrNull(20));
              progress.setRenderTimeMillis(rs.getLongOrNull(21));
              progress.setThinkTimeMillis(rs.getLongOrNull(22));
              progress.setRetryCount(rs.getLongOrNull(23));
              progress.setSurveyName(rs.getStringOrNull(24));
              progress.setClientIpAddress(rs.getStringOrNull(25));
              progress.setDeviceToken(rs.getStringOrNull(26));
              result.add(progress);
            }
            return result;
          }
        });
  }

  public List<SurveyProgressWithJson> findProgressEagerReverseOrder(Long siteId, Long surveyTokenId) {
    return database.get().toSelect("select sp.survey_site_id, survey_token_id, step_number, step_status, "
        + "question_step_number, question_api_compat_level, display_status_json_id, question_json_id, question_time, "
        + "answer_api_compat_level, submit_status_json_id, answer_json_id, answer_time, provider_id, section_id, "
        + "question_id, question_type, user_agent_id, survey_compat_level, call_time_millis, render_time_millis, "
        + "think_time_millis, retry_count, survey_name, client_ip_address, device_token, q.json, a.json "
        + "from survey_progress sp left outer join survey_json a on sp.answer_json_id=a.survey_json_id "
        + "left outer join survey_json q on sp.question_json_id=q.survey_json_id "
        + "where sp.survey_site_id=? and survey_token_id=? order by step_number desc")
        .argLong(siteId)
        .argLong(surveyTokenId)
        .query(new RowsHandler<List<SurveyProgressWithJson>>() {
          @Override
          public List<SurveyProgressWithJson> process(Rows rs) throws Exception {
            List<SurveyProgressWithJson> result = new ArrayList<>();
            while (rs.next()) {
              SurveyProgressWithJson progress = new SurveyProgressWithJson();
              progress.setSurveySiteId(rs.getLongOrNull(1));
              progress.setSurveyTokenId(rs.getLongOrNull(2));
              progress.setStepNumber(rs.getLongOrNull(3));
              progress.setStepStatus(rs.getStringOrNull(4));
              progress.setQuestionStepNumber(rs.getLongOrNull(5));
              progress.setQuestionApiCompatLevel(rs.getLongOrNull(6));
              progress.setDisplayStatusJsonId(rs.getLongOrNull(7));
              progress.setQuestionJsonId(rs.getLongOrNull(8));
              progress.setQuestionTime(rs.getDateOrNull(9));
              progress.setAnswerApiCompatLevel(rs.getLongOrNull(10));
              progress.setSubmitStatusJsonId(rs.getLongOrNull(11));
              progress.setAnswerJsonId(rs.getLongOrNull(12));
              progress.setAnswerTime(rs.getDateOrNull(13));
              progress.setProviderId(rs.getStringOrNull(14));
              progress.setSectionId(rs.getStringOrNull(15));
              progress.setQuestionId(rs.getStringOrNull(16));
              progress.setQuestionType(rs.getStringOrNull(17));
              progress.setUserAgentId(rs.getLongOrNull(18));
              progress.setSurveyCompatLevel(rs.getLongOrNull(19));
              progress.setCallTimeMillis(rs.getLongOrNull(20));
              progress.setRenderTimeMillis(rs.getLongOrNull(21));
              progress.setThinkTimeMillis(rs.getLongOrNull(22));
              progress.setRetryCount(rs.getLongOrNull(23));
              progress.setSurveyName(rs.getStringOrNull(24));
              progress.setClientIpAddress(rs.getStringOrNull(25));
              progress.setDeviceToken(rs.getStringOrNull(26));
              progress.setQuestionJson(rs.getStringOrNull(27));
              progress.setAnswerJson(rs.getStringOrNull(28));
              result.add(progress);
            }
            return result;
          }
        });
  }

  /**
   * Note this does not allow modifying most attributes, just status and answer related things.
   *
   * @param progress to update; must contain at least survey siteId id, survey token id, and step number for lookup
   */
  public void updateProgressAnswer(SurveyProgress progress) {
    database.get().toUpdate("update survey_progress set step_status=?, answer_api_compat_level=?, "
        + "submit_status_json_id=?, answer_json_id=?, call_time_millis=?, render_time_millis=?, "
        + "think_time_millis=?, retry_count=?, answer_time=? "
        + " where survey_site_id=? and survey_token_id=? and step_number=?")
        .argString(progress.getStepStatus())
        .argLong(progress.getAnswerApiCompatLevel())
        .argLong(progress.getSubmitStatusJsonId())
        .argLong(progress.getAnswerJsonId())
        .argLong(progress.getCallTimeMillis())
        .argLong(progress.getRenderTimeMillis())
        .argLong(progress.getThinkTimeMillis())
        .argLong(progress.getRetryCount())
        .argDate(progress.getAnswerTime())
        .argLong(progress.getSurveySiteId())
        .argLong(progress.getSurveyTokenId())
        .argLong(progress.getStepNumber()).update(1);
  }

  public void createProgressDup(SurveyProgress progress) {
    database.get().toInsert("insert into survey_progress_dup (survey_progress_dup_id, survey_site_id, survey_token_id, "
        + "step_number, answer_api_compat_level, submit_status_json_id, answer_json_id, answer_time, "
        + "question_type, call_time_millis, render_time_millis, think_time_millis, retry_count) "
        + "values (:pk,?,?,?,?,?,?,?,?,?,?,?,?)")
        .argLong(progress.getSurveySiteId())
        .argLong(progress.getSurveyTokenId())
        .argLong(progress.getStepNumber())
        .argLong(progress.getAnswerApiCompatLevel())
        .argLong(progress.getSubmitStatusJsonId())
        .argLong(progress.getAnswerJsonId())
        .argDate(progress.getAnswerTime())
        .argString(progress.getQuestionType())
        .argLong(progress.getCallTimeMillis())
        .argLong(progress.getRenderTimeMillis())
        .argLong(progress.getThinkTimeMillis())
        .argLong(progress.getRetryCount())
        .argPkSeq(":pk", "survey_seq").insert(1);
  }
  
  public void addPlayerProgress(Long siteId, Long surveyTokenId, String targetId, String action, Long milliseconds) {
    database.get().toInsert("insert into survey_player_progress (survey_player_progress_id, survey_token_id, survey_site_id, "
        + "player_id, player_action, player_time_millis, posted_time) "
        + "values (:pk, ?,?,?,?,?,:dt)")
        .argLong(surveyTokenId)
        .argLong(siteId)
        .argString(targetId)
        .argString(action)
        .argLong(milliseconds)
        .argDateNowPerDb(":dt")
        .argPkSeq(":pk", "survey_seq").insert(1);
  }
}
