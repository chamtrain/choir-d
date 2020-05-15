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

import edu.stanford.registry.server.xchg.data.Constants;
import edu.stanford.registry.shared.DataTable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements ExportFormatterIntf<DataTable> {

  private SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
  //private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
  private SimpleDateFormat customFormat;

  /**
   * Returns a date formatter with the hard-coded default format: MM/dd/yyyy
   */
  public DateFormatter() {
  }

  public DateFormatter(String formatString) {
    customFormat = new SimpleDateFormat(formatString);
  }

  @Override
  public String format(Date dateIn) {
    if (customFormat != null) {
      return customFormat.format(dateIn);
    }
    return dateFormat.format(dateIn);
  }

  @Override
  public String format(String strIn) {
    return strIn;
  }

  @Override
  public String format(Integer intIn) {
    return intIn.toString();
  }

}
