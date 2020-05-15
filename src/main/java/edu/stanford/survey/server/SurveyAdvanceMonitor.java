/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Poll the database for additional progress on surveys and send notifications to any push recipients
 * as appropriate.
 *
 * This isn't thread-safe. Its caller should ensure only one thread is running this,
 * preferably with a database-lock, so 2 nodes can cover for each other if one goes down.
 *
 * This is just for one site, but it can probably work for any site
 */
public class SurveyAdvanceMonitor {
  private static final Logger log = Logger.getLogger(SurveyAdvanceMonitor.class);
  private static final HourlyErrorHandler errorHandler = new HourlyErrorHandler(log, "survey advance handler", 6);

  private Long surveySiteId;
  private SurveyAdvanceHandlerFactory handlerFactory;

  public SurveyAdvanceMonitor(Long siteId, SurveyAdvanceHandlerFactory handlerFactory) {
    surveySiteId = siteId;
    this.handlerFactory = handlerFactory;
  }

  public int pollAndNotify(Supplier<Database> dbp) {
    int counter = 0;
    final Database db = dbp.get();

    errorHandler.clearHandlersEveryNHours();

    lockToStartBatchAndEnsureSequentialChangeLogIsUpToDate(db);

    SurveyDao dao = new SurveyDao(db);

    // Who wants to know about survey advances?
    List<SurveyAdvancePush> recipients = dao.findSurveyAdvancePushEnabled(surveySiteId);

    // Figure out the earliest change wanted by any recipient, and who will handle them
    Map<String, SurveyAdvanceHandler> handlers = new HashMap<>();
    long minPushSeq = findEarliestChangeForRecipientsAndPopulateHandlers(recipients, handlers);

    List<SurveyAdvance> surveyAdvances = dao.findSurveyAdvance(surveySiteId, minPushSeq);

    // Avoid processing multiple advances for the same survey by walking backwards and tracking token ids
    Set<Long> tokenIds = new HashSet<>();
    Set<Long> skipAdvanceSequence = new HashSet<>();
    for (int i = surveyAdvances.size() - 1; i > 0; i--) {
      SurveyAdvance advance = surveyAdvances.get(i);
      if (!tokenIds.add(advance.getSurveyTokenId())) {
        skipAdvanceSequence.add(advance.getAdvanceSequence());
      }
    }

    for (SurveyAdvance advance : surveyAdvances) {
      if (skipAdvanceSequence.contains(advance.getAdvanceSequence())) {
        // There is a newer advance later in the list
        continue;
      }
      for (SurveyAdvancePush push : recipients) {
        SurveyAdvanceHandler handler = handlers.get(push.getRecipientName());
        Long pushedSurveySequence = push.getPushedSurveySequence();
        if (pushedSurveySequence == null) {
          pushedSurveySequence = 0L;
        }
        if (handler == null || pushedSurveySequence >= advance.getAdvanceSequence()) {
          continue;
        }

        counter++;
        try {
          handler.surveyAdvanced(advance, db);
          push.setPushedSurveySequence(advance.getAdvanceSequence());
          push.setLastPushedTime(new Date());
          push.setFailedCount(0L);
          push.setFailedSurveySequence(null);
          push.setLastFailedTime(null);
        } catch (ThreadDeath td) {
          throw td;
        } catch (Throwable t) {
          log.error("Unable to notify recipient '" + push.getRecipientName() + "' that survey '"
              + advance.getAdvanceSequence() + "' advanced", t);
          push.setFailedCount(push.getFailedCount() + 1);
          push.setFailedSurveySequence(advance.getAdvanceSequence());
          push.setLastFailedTime(new Date());

          // Remove the handler so we won't retry within this poll
          handlers.remove(push.getRecipientName());
        }
        dao.updateSurveyAdvancePush(push);
        db.commitNow();
      }
    }
    return counter;
  }


  private long findEarliestChangeForRecipientsAndPopulateHandlers(List<SurveyAdvancePush> recipients,
      Map<String, SurveyAdvanceHandler> handlers) {
    long minPushSeq = Long.MAX_VALUE;
    for (SurveyAdvancePush push : recipients) {
      String handlerName = push.getRecipientName();
      SurveyAdvanceHandler handler = handlerFactory.handlerForName(handlerName);

      if (handler == null) {  // abort if no handler
        errorHandler.missingHandlerError(surveySiteId, handlerName, handlerFactory.getClass().getName());
        continue;
      }

      handlers.put(push.getRecipientName(), handler);
      Long pushedSurveySequence = push.getPushedSurveySequence();
      if (pushedSurveySequence == null) {
        pushedSurveySequence = 0L;
      }
      minPushSeq = Math.min(minPushSeq, pushedSurveySequence);
    }
    return minPushSeq;
  }


  private void lockToStartBatchAndEnsureSequentialChangeLogIsUpToDate(Database db) {
    // Lock to start batch, and determine the last check time
    Date checkTime = db.toSelect("select check_time from survey_advance_status where survey_site_id=? for update")
        .argLong(surveySiteId).queryDateOrNull();

    if (checkTime == null) {
      Calendar cal = Calendar.getInstance();
      cal.set(2000, Calendar.JANUARY, 1);
      checkTime = cal.getTime();
      db.toInsert("insert into survey_advance_status (survey_site_id, check_time) values (?,?)")
          .argLong(surveySiteId).argDate(checkTime).insert(1);
    }

    // Fudge back a bit in case there were uncommitted transactions during last run
    // or clocks were out of sync a bit
    Date startTime = new Date(checkTime.getTime() - (60 * 60 * 1000));

    // Scan for changes and make sure the sequential change log (survey_advance) is up to date
    Date advanceTime = db.toSelect(
          "with survey_token_ids as ("
        + "select survey_token_id from survey_session where survey_site_id=? and last_active>?"
        + " union"
        + " select survey_token_id from survey_player_progress where survey_site_id=? and posted_time>?"
        + "), date_and_player_count as ("
        + "select survey_token_id, max(advance_time) as advance_time,"
        + " sum(player_progress_count) as player_progress_count from ("
        + "select i.survey_token_id, last_active as advance_time, 0 as player_progress_count"
        + " from survey_token_ids i join survey_token t on i.survey_token_id=t.survey_token_id"
        + " join survey_session s on t.survey_token_id=s.survey_token_id and t.last_session_number=s.session_number"
        + " union all"
        + " select i.survey_token_id, posted_time as advance_time,"
        + " case when p.survey_token_id is not null then 1 else 0 end as player_progress_count"
        + " from survey_token_ids i join survey_player_progress p on i.survey_token_id=p.survey_token_id"
        + ") SubTable group by survey_token_id) "
        + "select survey_site_id, i.survey_token_id, advance_time, is_complete, last_step_number, player_progress_count"
        + " from survey_token_ids i join date_and_player_count d on i.survey_token_id=d.survey_token_id"
        + " join survey_token t on i.survey_token_id=t.survey_token_id order by advance_time")
        .argLong(surveySiteId)
        .argDate(startTime)
        .argLong(surveySiteId)
        .argDate(startTime).query(new RowsHandler<Date>() {
          @Override
          public Date process(Rows rs) throws Exception {
            Date advanceTime = null;
            while (rs.next()) {
              if (db.toSelect("select 'Y' from survey_advance where survey_token_id=? and last_step_number=?"
                  + " and player_progress_count=?")
                  .argLong(rs.getLongOrNull("survey_token_id"))
                  .argInteger(rs.getIntegerOrNull("last_step_number"))
                  .argInteger(rs.getIntegerOrNull("player_progress_count")).queryBooleanOrFalse()) {
                continue;
              }

              advanceTime = rs.getDateOrNull("advance_time");
              db.toInsert("insert into survey_advance (advance_sequence, advance_time, survey_site_id, survey_token_id,"
                  + " is_complete, last_step_number, player_progress_count) values (?,?,?,?,?,?,?)")
                  .argPkSeq("survey_advance_seq")
                  .argDate(advanceTime)
                  .argLong(rs.getLongOrNull("survey_site_id"))
                  .argLong(rs.getLongOrNull("survey_token_id"))
                  .argBoolean(rs.getBooleanOrNull("is_complete"))
                  .argInteger(rs.getIntegerOrNull("last_step_number"))
                  .argInteger(rs.getIntegerOrNull("player_progress_count")).insert(1);
            }
            return advanceTime;
          }
        });
    if (advanceTime != null) {
      db.toUpdate("update survey_advance_status set check_time=? where survey_site_id=?")
          .argDate(advanceTime).argLong(surveySiteId).update(1);
    }
    db.commitNow();
  }

}
