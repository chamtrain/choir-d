/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Poll the database for completed surveys and send notifications to any push recipients
 * as appropriate.
 *
 * This isn't thread-safe. Its caller should ensure only one thread is running this,
 * preferably with a database-lock, so 2 nodes can cover for each other if one goes down.
 */
public class SurveyCompleteMonitor {
  private static final Logger log = Logger.getLogger(SurveyCompleteMonitor.class);
  private static final HourlyErrorHandler errorHandler = new HourlyErrorHandler(log, "survey complete handler", 6);

  private Long siteId;
  private SurveyCompleteHandlerFactory handlerFactory;

  public SurveyCompleteMonitor(Long siteId, SurveyCompleteHandlerFactory handlerFactory) {
    this.siteId = siteId;
    this.handlerFactory = handlerFactory;
  }

  public int pollAndNotify(Supplier<Database> database) {
    SurveyDao dao = new SurveyDao(database);
    int counter = 0;

    errorHandler.clearHandlersEveryNHours(); // repeat error messages every 6 hours

    // Who wants to know about survey completions?
    List<SurveyCompletePush> recipients = dao.findSurveyCompletePushEnabled(siteId);

    // Figure out the earliest change wanted by any recipient, and who will handle them
    long minPushSeq = Long.MAX_VALUE;
    Map<String, SurveyCompleteHandler> handlers = new HashMap<>();
    for (SurveyCompletePush push : recipients) {
      String handlerName = push.getRecipientName();
      SurveyCompleteHandler handler = handlerFactory.handlerForName(handlerName, siteId);

      if (handler == null) {
        errorHandler.missingHandlerError(siteId, handlerName, handlerFactory.getClass().getName());
        continue;
      }

      handlers.put(push.getRecipientName(), handler);
      Long pushedSurveySequence = push.getPushedSurveySequence();
      if (pushedSurveySequence == null) {
        pushedSurveySequence = 0L;
      }
      minPushSeq = Math.min(minPushSeq, pushedSurveySequence);
    }

    for (SurveyComplete completion : dao.findSurveyComplete(siteId, minPushSeq)) {
      for (SurveyCompletePush push : recipients) {
        SurveyCompleteHandler handler = handlers.get(push.getRecipientName());
        Long pushedSurveySequence = push.getPushedSurveySequence();
        if (pushedSurveySequence == null) {
          pushedSurveySequence = 0L;
        }
        if (handler == null || pushedSurveySequence >= completion.getCompleteSequence()) {
          continue;
        }

        counter++;
        try {
          handler.surveyCompleted(completion, database);
          push.setPushedSurveySequence(completion.getCompleteSequence());
          push.setLastPushedTime(new Date());
          push.setFailedCount(0L);
          push.setFailedSurveySequence(null);
          push.setLastFailedTime(null);
        } catch (ThreadDeath td) {
          throw td;
        } catch (Throwable t) {
          log.error("Unable to notify recipient '" + push.getRecipientName() + "' that survey '"
              + completion.getCompleteSequence() + "' completed", t);
          push.setFailedCount(push.getFailedCount() + 1);
          push.setFailedSurveySequence(completion.getCompleteSequence());
          push.setLastFailedTime(new Date());

          // Remove the handler so we won't retry within this poll
          handlers.remove(push.getRecipientName());
        }
        dao.updateSurveyCompletePush(push);
        database.get().commitNow();
      }
    }
    return counter;
  }

}
