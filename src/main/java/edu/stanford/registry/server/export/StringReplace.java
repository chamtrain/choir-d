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

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.shared.DataTable;

import java.util.Date;
import java.util.HashMap;

public class StringReplace implements ExportFormatterIntf<DataTable> {

  public HashMap<String, String> matchList = new HashMap<>();

  public StringReplace(String replacementList) {
    String[] pairs = ServerUtils.getTokens(replacementList, ",");
    for (String pair : pairs) {
      String[] values = ServerUtils.getTokens(pair, "=");
      matchList.put(values[0], values[1]);
    }
  }

  @Override
  public String format(String strIn) {
    if (matchList.containsKey(strIn)) {
      return matchList.get(strIn);
    }
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

}
