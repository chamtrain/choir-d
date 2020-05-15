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

package edu.stanford.registry.server.export;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.xchg.DateFormatter;
import edu.stanford.registry.server.xchg.FormatterIntf;
import edu.stanford.registry.shared.DataTable;

import java.text.ParseException;
import java.util.Date;

public class CommonFormatter implements ExportFormatterIntf<DataTable>, FormatterIntf<DataTable> {

  @SuppressWarnings("rawtypes")
  private DateFormatter dateFormatter;

  @SuppressWarnings("rawtypes")
  public CommonFormatter() {
    dateFormatter = new DateFormatter(null);
  }

  @SuppressWarnings("rawtypes")
  public CommonFormatter(SiteInfo siteInfo) {
    dateFormatter = new DateFormatter(siteInfo);
  }

  @Override
  public String format(String strIn) {
    return strIn;
  }

  @Override
  public String format(Date dateIn) {
    return format(dateIn.toString());
  }

  @Override
  public String format(Integer intIn) {
    return format(intIn.toString());
  }


  public Integer toInt(String intIn) {

    return Integer.parseInt(intIn);
  }

  @Override
  public Date toDate(String dateIn) throws ParseException {
    return dateFormatter.toDate(dateIn);
  }

  @Override
  public Date toTime(String timeIn) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date toTimeStamp(String tstampIn) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

}
