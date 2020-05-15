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

package edu.stanford.registry.shared;

import java.util.Date;

public class ProcessAttribute {

  public static final int STRING = 0;
  public static final int INTEGER = 1;

  private String processName;
  private String processValue;
  private Integer processIntegerValue;
  private int type = STRING;
  private Date startDate;
  private Date endDate;

  public ProcessAttribute(String name, Integer value) {
    processName = name;
    processIntegerValue = value;
    type = INTEGER;
  }

  public ProcessAttribute(String name, String value) {
    processName = name;
    processValue = value;

  }

  public String getName() {
    return processName;
  }

  public Object getValue() {
    if (type == INTEGER) {
      return processIntegerValue;
    }
    return processValue;
  }

  public String getString() {
    if (type == INTEGER) {
      return processIntegerValue.toString();
    }
    return processValue;
  }

  public Integer getInteger() {
    return processIntegerValue;
  }

  public int getType() {
    return type;
  }

  public void setStartDate(Date dt) {
    startDate = dt;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setEndDate(Date dt) {
    endDate = dt;
  }

  public Date getEndDate() {
    return endDate;
  }

  public boolean qualifies(Date agreed) {
    // not agreed, does not qualify
    if (agreed == null) {
      return false;
    }

    // both start and end dates are null so yes qualifies
    if (startDate == null && endDate == null) {
      return true;
    }

    // at least one (start/end) is not null
    if (startDate == null) {
      if (agreed.before(endDate)) {
        return true;
      }
    } else if (endDate == null) {
      if (agreed.equals(startDate) || agreed.after(startDate)) {
        return true;
      }
    } else { // both are not null
      if (agreed.after(startDate) && (endDate == null || agreed.before(endDate))) {
        return true;
      }
    }
    return false;
  }

  /**
   * A simple date->string interface so toString(formatter) can work
   */
  public interface FmtDate {
    String format(Date d); // must handle null, too
  }
  static private String types[] = { "str", "int" };

  /**
   * This can't include SimpleDateFormat, so the caller must pass in a formatter.
   * Use:  (date) -> date == null ? "null" : new SimpleDateFormat("MM/dd/yyyy").format(date)
   */
  public String toString(FmtDate fmt) {
    return processName + "/" + types[type] + "/" + getValue() + "/[" + fmt.format(startDate) + "-" + fmt.format(endDate) + "]";
  }
}
