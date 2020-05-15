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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

public class SurveyChartAxisFormat extends DecimalFormat {
  private static final long serialVersionUID = 1L;

  public SurveyChartAxisFormat() {
    super("##");
  }

  @Override
  public StringBuffer format(double arg0, StringBuffer arg1, FieldPosition arg2) {
    return format((long) arg0, arg1, arg2);
  }

  @Override
  public StringBuffer format(long number, StringBuffer arg1, FieldPosition arg2) {

    StringBuffer buf = new StringBuffer();
    int intValue = (Long.valueOf(number)).intValue() - 50;
    if (intValue == 0) {
      buf.append("Mean");
    } else {
      StringBuffer superBuf = super.format(intValue / 10, arg1, arg2);
      if (intValue > 0) {
        buf.append("+");
      }
      buf.append(superBuf.toString());
      buf.append(" SD");
    }
    return buf;
  }

  @Override
  public Number parse(String arg0, ParsePosition arg1) {
    // TODO Auto-generated method stub
    return null;
  }

}
