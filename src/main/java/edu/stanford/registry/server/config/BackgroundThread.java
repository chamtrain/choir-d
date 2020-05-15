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

package edu.stanford.registry.server.config;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.service.BatchContext;
import edu.stanford.registry.server.service.tasks.ImportRunner;
import edu.stanford.registry.server.service.tasks.Timer;
import edu.stanford.registry.server.utils.XMLFileUtils;

/**
 * Spawns a thread which polls, using this thread:
 * <br>New users, config and surveySites
 * <br>Imports patients and appointments
 * <br>Finishes and updates surveys (BatchContext).
 *
 * If the cache.reload.seconds==0 at the beginning, values will never be refreshed.
 * If it's not zero, it can't be turned off by setting to zero unless server is restarted.
 *
 * If other frequencies are set to zero, they'll turn on again within 5 minutes of a change.
 * Whenever a frequency is changed, it's logged (search for "lobal property" in the log.
 */
public class BackgroundThread implements Runnable {
  Logger logger = Logger.getLogger(BackgroundThread.class);
  Long MINUTES_5 = 5 * 60 * 1000L;  // sleep 5 minutes if disabled, before checking again

  boolean keepGoing = true;

  Thread theThread; // for simplicity- just one background thread
  SitesInfo sitesInfo;

  final ServerContext serverContext;
  final BatchContext batchContext;

  ImportRunner importPatients;
  ImportRunner importAppointments;
  String originalThreadName; // for the actual background thread

  final PriorityQueue<OneJob> queue = new PriorityQueue<>(new OneJobComparer());

  /**
   * We run one background thread to do whatever's necessary sequentially.
   * If parameters change, this can be reloaded.
   *
   * @param serverContext - to we can refresh caches
   * @param batchContext - to we can advance or complete surveys
   * @param doImport - to import new patients and/or appointments from files
   */
  BackgroundThread(ServerContext serverContext, BatchContext batchContext) {
    logger.info("Creating Background thread with jobs to reload config+users, update surveys and do imports");
    this.serverContext = serverContext;
    this.batchContext = batchContext;
    sitesInfo = serverContext.getSitesInfo();

    theThread = new Thread(this, "BackgroundThread");
    theThread.setUncaughtExceptionHandler(new BThreadExcHandler());
    theThread.setDaemon(true);

    init();
  }

  class BThreadExcHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      logger.warn("BackgroundThread was interrupted by an exception", e);
    }
  }

  void init() {
    queue.clear();
    addServerContextJob();
    addImportJob();
    addBatchContextJob(batchContext != null);
    addSendEmailJob(batchContext != null);

    if (queue.size() == 0) {
      logger.info("All background jobs are disabled. The background thread won't run");
      return;
    }

    // We could add a job to check the config params, and if any change
    //    stop the thread and then call init() again.
    theThread.start();  // launches the thread and calls run
  }

  final static String CACHE_RELOAD_SECS = "cache.reload.seconds";
  final static String RELOAD_CONFIG = "registry.reload.config";
  final static String RELOAD_USERS = "registry.reload.users";

  OneJob addServerContextJob() {
    // Refresh the cache every 5 seconds to 10 minutes, unless it's turned off, with zero
    long interval = getInterval("cache.reload.seconds", 30, 's', 5, 600, -1);
    if (interval == 0) {
      logger.warn("Cache reloading disabled, cache.reload.seconds=0. Change and restart to turn on refreshing.");
      return null;
    }
    boolean reloadConfig = sitesInfo.getGlobalProperty(RELOAD_CONFIG, true);
    if (!reloadConfig) {
      logger.info("Cache reloading is disabled, since "+RELOAD_CONFIG+" is not true");
      if (!sitesInfo.getGlobalProperty(RELOAD_USERS, true))
        return null;   // both off, no point...
    }

    return new OneJob("ServerConfig", interval, false) {
      // This can't change - it's either on and shouldn't be turned of by accident
      // or off and can't be turned on.  Though later we could in the UI, doesn't work wi multi-servers...
      final boolean reloadConfig = sitesInfo.getGlobalProperty(RELOAD_CONFIG, true);

      @Override
      void theJob() {
        millis = getInterval(CACHE_RELOAD_SECS, 30, 's', 5, 600, millis);
        final boolean reloadUsers = sitesInfo.getGlobalProperty(RELOAD_USERS, true); // this can change
        serverContext.reload(reloadUsers, reloadConfig);
      }
    };
  }

  void addImportJob() {
    String importUrl = sitesInfo.getGlobalProperty("import.url");
    if (importUrl == null || importUrl.isEmpty()) {
      logger.info("Periodic checking for patient/appointment importing is disabled, import.url is not set");
      return;
    }

    importPatients = new ImportRunner(sitesInfo, "Patient");
    importAppointments = new ImportRunner(sitesInfo, "Appointment");
    long interval = getInterval("import.process.frequency", 5, 'm', 1, 60, -1);
    new OneJob("Imports", interval, true) {
      Timer hourlyTimer;
      final Long hour = 60 * 60000L; // 60 minutes in milliseconds

      boolean beVerbose() {
        if (hourlyTimer == null) {  // first time, be verbose
          hourlyTimer = new Timer();
          return true;
        }
        return hourlyTimer.checkMillis() > hour;
      }

      @Override
      void theJob() {
        millis = getInterval("import.process.frequency", 5, 'm', 1, 60, millis);
        if (millis > 0) { // if millis==0, it's off till it's turned on
          importPatients.run(beVerbose()); // give verbose output every hour
          importAppointments.run(false); // info about the url and folder are the same
        }
      }
    };
  }

  void addBatchContextJob(boolean enabled) {
    if (!enabled) {
      logger.info("batch updating of surveys is disabled- change properties and restart server to enable");
      return;
    }
    long interval = getInterval("registry.batch.interval.seconds", 120, 's', 15, 1800, -1); // 15s..2m..30min
    new OneJob("SurveyUpdater", interval, true) {
      @Override
      void theJob() {
        millis = getInterval("registry.batch.interval.seconds", 120, 's', 15, 1800, millis);
        if (millis > 0) {
          batchContext.advanceAndCompleteSurveys();
        }
      }
    };
  }

  void addSendEmailJob(boolean enabled) {
    if (!enabled) {
      logger.info("Batch sending of emails is disabled- change properties and restart server to enable");
      return;
    }
    long interval = getInterval("registry.sendEmail.interval.minutes", 0, 'm', 1, 24*60, -1);
    
    new OneJob("Mailer", interval, false) {
      @Override
      void theJob() {
        millis = getInterval("registry.sendEmail.interval.minutes", 0, 'm', 1, 24*60, millis);
        if (millis > 0) {
          batchContext.sendEmails();
        }
      }
    };
  }

  void initXmlFileUtils() {
    for (SiteInfo siteInfo: sitesInfo) {
      try {
        XMLFileUtils.fillCache(siteInfo);
      } catch (Throwable t) {
        // ok
      }
    }
  }

  public void stopRunning() {
    if (!keepGoing) {
      return;  // this is usually called a second time if sleep is interrupted
    }
    keepGoing = false;
    Exception exc = null;
    if (logger.isDebugEnabled()) {
      try {
        throw new Exception("Showing stack when backgroundThread is told to stop");
      } catch (Exception e) {
        exc = e;
      }
    }
    logger.debug("StopRunning was called.", exc);
  }

  @Override
  public void run() {
    initXmlFileUtils();  // do this at first, once, so the first users don't have to wait

    long numJobs = 0;
    Timer timer = new Timer();
    while (keepGoing && !Thread.currentThread().isInterrupted()) {
      sleepTill(queue.peek().getNextRunTime());

      if (keepGoing)
        try {
          MDC.put("userId", "<polling>");

          // This is counting jobs run, to make it obvious if a job isn't sleeping.
          if ((numJobs++ % 200) == 0)  { // if surveys and cache run every 30 seconds, we'll get 1 line/hour
            logger.debug(numJobs+" polling/background jobs have been run so far, in "+timer.getMinutes()+" = "+timer.numPerHour(numJobs));
            timer.continu();
          }

          queue.peek().runJob();

        } catch (ThreadDeath t) {
          logger.error("Terminating polling thread (received ThreadDeath)", t);
          throw t;
        } catch (Throwable t) {
          logger.error("Unhandled throwable while polling", t);
        } finally {
          MDC.clear();
        }
    }
    logger.info("Background thread has stopped. - ");
  }

  long sleepLogN;  // will say 16/256 sleep messages, about 6%, every 2'ish hours
  void sleepTill(long nextRunTime) {
    long sleepTime = nextRunTime - System.currentTimeMillis();
    if (sleepTime <= 0) {
      return;
    }
    if ((255 & ++sleepLogN) < 16) {
      logger.trace(sleepLogN + ". sleeping for "+(sleepTime/1000)+" secs");
    }
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      logger.debug("Sleep caught an exception" + e.getLocalizedMessage());
      stopRunning();
    }
  }

  static class OneJobComparer implements Comparator<OneJob> {
    @Override
    public int compare(OneJob o1, OneJob o2) {
      long diff = o1.getNextRunTime() - o2.getNextRunTime();
      return (int)diff;  // there are approximately 20 days in an integer.
    }
  }

  // This is a separate method to aid testing
  void logJobMsg(String name) {
    logger.info("running background job: "+name);
  }

  abstract class OneJob {
    final String name;
    long millis;
    long nextRunTime; // this determines when it'll wake, and its order in the queue

    OneJob(String name, long millis, boolean runNow) {
      this.name = name;
      this.millis = millis;
      nextRunTime = getCurrentTime() + (runNow ? 0 : this.millis);
      logger.info("Background job '"+name+"' will run every "+(millis/1000)+" seconds");
      queue.add(this);
    }

    long getNextRunTime() {
      return nextRunTime;
    }

    void runJob() {
      logJobMsg(name);
      // > 4999 to ensure there's at least 5 seconds before a job runs again, else logs get much too big
      nextRunTime = getCurrentTime() + ((millis > 4999) ? millis : MINUTES_5); // awake in 5 min to see if props change
      try {
        theJob();
        queue.add(queue.poll());  // pop and push it to sort it into the list
      } catch (Throwable e) {
        logger.error("Exception in PollingThread.runJob: "+name, e);
      }
    }

    // Wrap getting the current time, so easy to mock
    long getCurrentTime() {
      return System.currentTimeMillis();
    }

    abstract void theJob();
  }

  @SuppressWarnings("deprecation")  // for pollingThread.stop, after trying politely
  public void destroy() {
    if (theThread != null && theThread.isAlive()) {
      stopRunning();
      logger.debug("Sending the background thread an interrupt now");
      theThread.interrupt();  // wake it up from sleep
      try {
        theThread.join(35000);  // 35 seconds... should be enough to finish the one job
      } catch (InterruptedException e) {
        logger.warn("Interrupted while waiting for polling thread to terminate", e);
      }
      if (theThread.isAlive()) {
        logger.warn("Unable to terminate polling thread nicely, now using blunt force");
        theThread.stop();
      }
    }
  }

  /**
   * This fetches a value, reports it if it's different, and returns it in milliseconds.
   * It allows a zero value, but otherwise keeps it between min and max.
   */
  long getInterval(String key, int defaultNum, char minOrSec, int min, int max, long oldValue) {
    String freq = sitesInfo.getGlobalProperty(key);
    int multiplier = (minOrSec == 'm') ? 60000 : 1000;
    String units = (minOrSec == 'm') ? " minutes" : " seconds";
    logger.info("getInterval 1- "+key+"="+freq);
    if (freq != null && !freq.isEmpty()) {
      try {
        long val = Long.valueOf(freq);
        if (val == 0) {
          return getIntervalForZero(key, max, oldValue, multiplier, units);
        }

        val = (val < min) ? min : ((max < val) ? max : val); // ensure min <= val <= max
        val *= multiplier;
        if (val != oldValue)
          logger.info("Global property "+key+" is now "+freq+units);
        return val;
      } catch (NumberFormatException e) {
        long val = defaultNum * multiplier;
        if (val != oldValue) { // only give this error once
          logger.error("Non-numeric global property "+key+" = "+freq+"; using default " + defaultNum + units);
        }
        return val;
      }
    }

    // no value was set, use default
    long val = defaultNum * multiplier;
    if (val != oldValue) {
      logger.info("Global property "+key+", no value was found, using default " + defaultNum + units);
    }
    return val;
  }

  /**
   * Separate set-to-zero logic.  Don't warn if we're not changing it.
   * Return a special number if they're trying to turn off caching, and warn
   */
  private long getIntervalForZero(String key, int max, long oldValue, int multiplier, String units) {
    if (oldValue == 0) {
      return 0;  // already off - no warning is needed
    }

    boolean thisIsCacheInterval = key.equals(CACHE_RELOAD_SECS);
    if (oldValue < 0) { // signifies the first time
      if (thisIsCacheInterval) {
        logger.warn("Global property "+key+" is initialized to ZERO - You must restart the server if configuration properties change");
      } else {
        logger.info("Global property "+key+" is initialized to ZERO "+units+", turning it off");
      }
      return 0;
    }

    if (!thisIsCacheInterval) {
      logger.warn("Setting global property "+key+"=0 is turning it off");
      return 0;
    }

    // don't let people turn off cache reloading unless they restart server
    long maxPlus1 = 1 + (max * multiplier); // a special value > max, so we don't repeat warning next time.
    if (oldValue != maxPlus1) {
      logger.warn("CAREFUL: Setting global property "+key+"=0 won't take effect till restart. Setting to max="+max+units);
    }
    return maxPlus1;
  }
}
