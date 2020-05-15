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

package edu.stanford.registry.server.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import edu.stanford.registry.server.SiteInfo;

public class DateUtils {

  public final static long MILISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

  /**
   * Returns the given date with the time set to the start of the day.
   */
  public static Date getDateStart(SiteInfo siteInfo, Date date) {
    if (date == null) {
      return null;
    }
    return getCalendarDayStart(siteInfo, date).getTime();
  }

  public static Date getTimestampStart(SiteInfo siteInfo, Date date) {
    if (date == null) {
      return null;
    }
    return getCalendarDayStart(siteInfo, date).getTime();
  }

  public static Date getDateEnd(SiteInfo siteInfo, Date date) {
    if (date == null) {
      return null;
    }
    return getCalendarDayEnd(siteInfo, date).getTime();
  }

  public static Date getTimestampEnd(SiteInfo siteInfo, Date date) {
    if (date == null) {
      return null;
    }
    return getCalendarDayEnd(siteInfo, date).getTime();
  }

  public static Date getDaysOutDate(SiteInfo siteInfo, int days) {
    return getDaysFromDate(siteInfo, new Date(), days);
  }

  public static Date getDaysAgoDate(SiteInfo siteInfo, int days) {
    return getDaysFromDate(siteInfo, new Date(), -days);
  }

  public static int getDaysAway(SiteInfo siteInfo, Date dt) {
    Date today = getDateStart(siteInfo, new Date());
    return (int) ((getDateStart(siteInfo, dt).getTime() - today.getTime()) / MILISECONDS_PER_DAY);
  }

  public static Date getDaysFromDate(SiteInfo siteInfo, Date date, int days) {
    Calendar calendar = DateUtils.getCalendarDayStart(siteInfo, date);
    calendar.add(Calendar.DAY_OF_YEAR, days);
    return calendar.getTime();
  }

  /**
   * Returns the given date with the time set to the beginning of the day.
   */
  public static Calendar getCalendarDayStart(SiteInfo siteInfo, Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = newCalendar(siteInfo);
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 1);
    return cal;
  }

  /**
   * Returns the given date with the time set to the end of the day.
   */
  public static Calendar getCalendarDayEnd(SiteInfo siteInfo, Date date) {
    Calendar cal = newCalendar(siteInfo);
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    cal.set(Calendar.MILLISECOND, 999);
    return cal;
  }

  /**
   * Get the date as noon 
   */
  public static Date getDateNoon(SiteInfo siteInfo, Date date) {
    if (date == null) {
      return null;
    }
    Calendar cal = newCalendar(siteInfo);
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 11);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    
    return cal.getTime();
  }

  /**
   * Get a persons age from their date of birth, returns -1 if the date is null or in the future!
   */
  public static int getAge(Date dtBirth) {
    if (dtBirth == null) {
      return -1;
    }
    Calendar now = Calendar.getInstance(Locale.US);
    Calendar dob = Calendar.getInstance(Locale.US);
    now.setTime(new Date());
    dob.setTime(dtBirth);

    if (dob.after(now)) {
      return -1;
    }
    int age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

    // Adjust down 1 year if their birthday is after todays date.
    if ((dob.get(Calendar.MONTH) > now.get(Calendar.MONTH))
        || (dob.get(Calendar.MONTH) == now.get(Calendar.MONTH) && dob.get(Calendar.DAY_OF_MONTH) > now
        .get(Calendar.DAY_OF_MONTH))) {
      age--;
    }

    return age;
  }

  /**
   * Create a date format instance using the time zone of the site.
   */
  public static SimpleDateFormat newDateFormat(SiteInfo siteInfo, String format) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(getTimeZone(siteInfo));
    return dateFormat;
  }

  /**
   * Create a calendar instance using the time zone of the site.
   */
  public static Calendar newCalendar(SiteInfo siteInfo) {
    return Calendar.getInstance(getTimeZone(siteInfo));
  }

  /**
   * Get the time zone specified for the site or the default time zone.
   */
  public static TimeZone getTimeZone(SiteInfo siteInfo) {
    TimeZone timezone = null;
    if (siteInfo != null) {
      String timezoneStr = siteInfo.getProperty("default.timezone");
      if ((timezoneStr != null) && (!timezoneStr.trim().isEmpty())) {
        timezone = TimeZone.getTimeZone(timezoneStr);
      }
    }
    if (timezone == null) {
      timezone = TimeZone.getDefault();
    }
    return timezone;
  }

  //
  // Old version of the methods which to not pass the SiteInfo parameter
  //

  @Deprecated
  public static Date getDateStart(Date date) {
    return getDateStart(null, date);
  }

  @Deprecated
  public static Date getTimestampStart(Date date) {
    return getTimestampStart(null, date);
  }

  @Deprecated
  public static Date getDateEnd(Date date) {
    return getDateEnd(null, date);
  }

  @Deprecated
  public static Date getTimestampEnd(Date date) {
    return getTimestampEnd(null, date);
  }

  @Deprecated
  public static Date getDaysOutDate(int days) {
    return getDaysOutDate(null, days);
  }

  @Deprecated
  public static Date getDaysAgoDate(int days) {
    return getDaysAgoDate(null, days);
  }

  @Deprecated
  public static int getDaysAway(Date dt) {
    return getDaysAway(null, dt);
  }

  @Deprecated
  public static Date getDaysFromDate(Date date, int days) {
    return getDaysFromDate(null, date, days);
  }

  @Deprecated
  public static Calendar getCalendarDayStart(Date date) {
    return getCalendarDayStart(null, date);
  }

  @Deprecated
  public static Calendar getCalendarDayEnd(Date date) {
    return getCalendarDayEnd(null, date);
  }
}
