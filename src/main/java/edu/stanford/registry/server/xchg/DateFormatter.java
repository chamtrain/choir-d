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

package edu.stanford.registry.server.xchg;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.xchg.data.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter<T> implements FormatterIntf<T> {
  
  private SimpleDateFormat dateFormat;
  private SimpleDateFormat timeFormat;
  private SimpleDateFormat dateTimeFormat;
  private SimpleDateFormat customFormat;

  public DateFormatter(SiteInfo siteInfo) {
    this(siteInfo, Constants.DATE_FORMAT);
  }

  public DateFormatter(SiteInfo siteInfo, String formatString) {
    dateFormat = DateUtils.newDateFormat(siteInfo, formatString);
    timeFormat = DateUtils.newDateFormat(siteInfo, Constants.TIME_FORMAT);
    dateTimeFormat = DateUtils.newDateFormat(siteInfo, Constants.DATE_TIME_FORMAT);
  }

  @Override
  public Object format(String strIn) throws ParseException {
    if (customFormat != null) {
      return customFormat.format(toDate(strIn));
    } else {
      return dateFormat.format(toDate(strIn));
    }
  }

  @Override
  public Date toDate(String dateIn) throws ParseException {
    if (customFormat != null) {
      return new Date(customFormat.parse(dateIn).getTime());
    } else {
      return new Date(dateFormat.parse(dateIn).getTime());
    }
  }

  @Override
  public Date toTime(String timeIn) throws ParseException {
    if (customFormat != null) {
      return new Date(customFormat.parse(timeIn).getTime());
    } else {
      return new Date(timeFormat.parse(timeIn).getTime());
    }
  }

  @Override
  public Date toTimeStamp(String tstampIn) throws ParseException {
    if (customFormat != null) {
      return new Date(customFormat.parse(tstampIn).getTime());
    } else {
      return new Date(dateTimeFormat.parse(tstampIn).getTime());
    }
  }

}
