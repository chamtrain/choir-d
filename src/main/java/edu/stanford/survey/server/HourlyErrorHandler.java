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

package edu.stanford.survey.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

import org.apache.log4j.Logger;

/**
 * TODO: Rewrite to be generic:  Constructed with a String.format, and have 2 error methods
 * <br>errorOnceEveryNHours(hashString, int n, Object...formatParams)
 * <br>errorOnceEveryNHours(int n, Object...formatParams)
 * <br>And two more that return either a null or the message string so the caller can
 * log it (so its Classname.method would appear in the log, if the logger reports those)
 * <br>And add a LongSupplier to a constructor so it's easily tested.
 * <br>This could also count the number of each error, and report that when the cache
 * is cleared (so 2 errors/N hours), or when the next error occurs...
 *
 * This supports concurrency.  In two possible race conditions, two errors can be output.
 *
 * @author rstr
 */
public class HourlyErrorHandler {
  private final LongSupplier nowGetter;
  private final Logger logger;
  private final String handlerType;
  private final int nHours;

  // This could be a ConcurrentHashSet, but there's no such thing...
  private final ConcurrentHashMap<String,HourlyErrorHandler> missingHandlers = new ConcurrentHashMap<>();
  private long startTime; // so can clear once every N hours

  HourlyErrorHandler(Logger logger, String handlerType, int nHours) {
    this(new NowGetter(), logger, handlerType, nHours);
  }

  HourlyErrorHandler(LongSupplier nowGetter, Logger logger, String handlerType, int nHours) {
    this.logger = logger;
    this.handlerType = handlerType;
    this.nowGetter = nowGetter;
    this.nHours = nHours;
    startTime = nowGetter.getAsLong();
  }

  /**
   * This outputs a certain kind of message for the Survey*Monitors.
   */
  protected void missingHandlerError(Long siteId, String handlerName, String factoryClassName) {
    String handlerForFactory = handlerName + factoryClassName;
    if (null == missingHandlers.put(handlerForFactory, this)) {
      logger.error("Site: "+siteId+": Could not find a "+handlerType+" of type '"+handlerName+"' using factory "+factoryClassName);
    }
  }

  /**
   * Call this every so often and if n or more hours have passed, the cache will be cleared so that
   * new errors will be logged.
   *
   * @param n Should be greater than zero (we don't fix it so it can be changed in the debugger)
   */
  void clearHandlersEveryNHours() {
    long now = nowGetter.getAsLong();
    long diffHours = (now - startTime) / (3600 * 1000);

    if (diffHours >= nHours) {  // repeat error messages every n hours
      startTime = now; // restart the clock
      missingHandlers.clear();
    }
  }

  static class NowGetter implements LongSupplier {
    @Override
    public long getAsLong() {
      return System.currentTimeMillis();
    }
  }
}
