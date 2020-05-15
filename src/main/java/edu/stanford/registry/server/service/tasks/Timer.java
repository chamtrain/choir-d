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

package edu.stanford.registry.server.service.tasks;

/**
 * A simple task timer.  Reports how long it's been since its creation or restart.
 * @author Randy Strauss
 */
public class Timer {
  long time = System.currentTimeMillis();
  long diff = -1;

  
  public Timer() {
    restart();
  }

  /**
   * @return stops the timer and returns the number of milliseconds
   */
  public long getMillis() {
    if (diff < 0) {
      diff = (System.currentTimeMillis() - time);
    }
    return diff;
  }

  /**
   * @return elapsed time without stopping the timer
   */
  public long checkMillis() {
    return (System.currentTimeMillis() - time);
  }

  /**
   * Returns true if it has been this many hours or more, without stopping the timer
   */
  public boolean hasBeenHours(int hours) {
    long h = hours * (3600 * 1000);
    return checkMillis() >= h;
  }

  /**
   * Returns true if it has been this many minutes or more, without stopping the timer
   */
  public boolean hasBeenMinutes(int minutes) {
    long m = minutes * (60 * 1000);
    return checkMillis() >= m;
  }

  /**
   * Stop the timer and tell the number of seconds (rounded to hundredths of a second)
   * @return
   */
  public String getSeconds() {
    double f = getMillis() / 1000.0;
    return String.format("%.2f seconds", f);
  }

  /**
   * Stop the timer and tell the number of hours, rounds to hundredths of an hour
   * @return
   */
  public String getHours() {
    double f = getMillis() / 3600000.0;
    return String.format("%.2f hours", f);
  }

  /**
   * Stop the timer and tell the number of seconds (rounded)
   * @param num
   * @return
   */
  public String numPerHour(long num) {
    double hrs = getMillis() / 3600000.0;
    if (hrs < 0.0001) {
      return "unknown/hour";
    }
    double rate = num / hrs;
    return String.format("%.0f/hour", rate); // avg/hour rounded
  }

  /**
   * Stop the timer and tell the number of seconds (rounded)
   * @return
   */
  public String getMinutes() {
    double f = getMillis() / 60000.0;
    return String.format("%.2f minutes", f); // rounds
  }

  /**
   * Restart the time from now.
   */
  public void restart() {
    time = System.currentTimeMillis();
    diff = -1;
  }

  /**
   * Continue timing from the original time
   */
  public void continu() {
    diff = -1;
  }
}
