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

package edu.stanford.registry.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.DateUtilsIntf;

public class DateTimeFormatter implements DateUtilsIntf {
  private static final Logger logger = Logger.getLogger(SiteInfo.class);

  SiteInfo siteInfo;
  SimpleDateFormat dateTimeFormat;
  SimpleDateFormat defaultDateTimeFormat;

  DateTimeFormatter(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;

    String dateTimeFormatStr = siteInfo.getProperty("default.dateTimeFormat");
    if (dateTimeFormatStr == null) {
      logger.warn("Property 'default.dateTimeFormat' is not configured, defaulting to: '" + CommonUtils.DTFMT + "'\n");
      dateTimeFormatStr = CommonUtils.DTFMT;
    }

    dateTimeFormat = DateUtils.newDateFormat(siteInfo, dateTimeFormatStr);
    defaultDateTimeFormat = DateUtils.newDateFormat(siteInfo, CommonUtils.DTFMT);
  }


  /**
   * Parses the string using the site-specific date and time format.
   * If this fails, it'll try the default format: "MM/dd/yyyy h:mm a"
   * @throws ParseException
   */
  public Date parseDate(String s) throws ParseException {
    try {
      return dateTimeFormat.parse(s);
    } catch (ParseException e) {
      logger.warn(siteInfo.getIdString()+"parseDate('"+s+"') failed, format='"+dateTimeFormat.toPattern()+"'");
      return defaultDateTimeFormat.parse(s);  // try the default
    }
  }


  /**
   * Returns the site-specific string date+time representation of the passed date
   */
  @Override // for DateUtilsIntf
  public String getDateString(Date dt) {
    return dateTimeFormat.format(dt);
  }

}