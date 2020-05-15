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

package edu.stanford.registry.server.xchg.data;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.xchg.DateFormatter;
import edu.stanford.registry.server.xchg.FormatterIntf;

import java.text.DecimalFormat;
import java.text.ParseException;

@SuppressWarnings("rawtypes")
public class NumberFormatter extends DateFormatter implements FormatterIntf {


  private static final String DEFAULT_PATTERN = "#,###.##";
  private DecimalFormat formatr;

  public NumberFormatter(SiteInfo siteInfo, String pattern) {
    super(siteInfo);
    if (pattern == null) {
      pattern = DEFAULT_PATTERN;
    }
    formatr = new DecimalFormat(pattern);

  }

  @Override
  public String format(String strIn) throws ParseException {
    if (strIn == null) {
      return null;
    }
    if (strIn.trim().length() < 1) {
      return strIn.trim();
    }
    Long value = formatr.parse(strIn.trim()).longValue();
    return value.toString();
  }


  public Integer toInt(String intIn) throws NumberFormatException, ParseException {
    if (intIn == null || intIn.trim().length() < 1) {
      return null;
    }
    return formatr.parse(intIn.trim()).intValue();
  }


}
