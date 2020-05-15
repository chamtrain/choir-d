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

import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.CommonUtils;
import edu.stanford.registry.shared.DateUtilsIntf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class DateFormatter implements DateUtilsIntf {
  private static final Logger logger = LoggerFactory.getLogger(DateFormatter.class);

  SiteInfo siteInfo;
  SimpleDateFormat dateFormat;
  SimpleDateFormat defaultDateFormat;

  DateFormatter(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;

    String dateFormatStr = siteInfo.getProperty("default.dateFormat");
    if (dateFormatStr == null) {
      logger.warn("Property 'default.dateFormat' is not configured, defaulting to: '" + CommonUtils.DFMT + "'\n");
      dateFormatStr = CommonUtils.DFMT;
    }

    dateFormat = DateUtils.newDateFormat(siteInfo, dateFormatStr);
    defaultDateFormat = DateUtils.newDateFormat(siteInfo, CommonUtils.DFMT);
  }


  /**
   * Parses the string using the site-specific date format. 
   * If this fails, it'll try the default format: "MM/dd/yyyy h:mm a"
   * @throws ParseException
   */
  public Date parseDate(String s) throws ParseException {
    try {
      return dateFormat.parse(s);
    } catch (ParseException e) {
      logger.warn(siteInfo.getIdString()+"parseDate('"+s+"') failed, format='"+dateFormat.toPattern()+"'");
      return defaultDateFormat.parse(s);
    }
  }


  /**
   * Returns the site-specific string date+time representation of the passed date
   */
  @Override // for DateUtilsIntf
  public String getDateString(Date dt) {
    return dateFormat.format(dt);
  }

}