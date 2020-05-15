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
import edu.stanford.registry.shared.PatientAttribute;

import java.text.ParseException;

public class AttributeFormat extends DateFormatter<String> implements FormatterIntf<String> {
  int type;

  public AttributeFormat(SiteInfo siteInfo, String value) {
    super(siteInfo);
    if (value == null) {
      type = PatientAttribute.STRING;
    } else if (("integer").equals(value.toLowerCase())) {
      type = PatientAttribute.INTEGER;
    } else if (("date").equals(value.toLowerCase())) {
      type = PatientAttribute.DATE;
    } else if (("datetime").equals(value.toLowerCase())) {
      type = PatientAttribute.TIMESTAMP;
    }

  }

  @Override
  public String format(String strIn) throws ParseException {
    switch (type) {
    case PatientAttribute.TIMESTAMP:
      return toTime(strIn).toString();
    case PatientAttribute.DATE:
      return toDate(strIn).toString();
    case PatientAttribute.INTEGER:
      return toInt(strIn) + "";
    default:
      return strIn;
    }

  }


  public Integer toInt(String intIn) throws NumberFormatException {
    return Integer.parseInt(intIn);
  }

  public int getDataType() {
    return type;
  }

}
