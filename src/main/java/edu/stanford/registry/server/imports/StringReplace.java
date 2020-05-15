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

package edu.stanford.registry.server.imports;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.xchg.DateFormatter;

import java.util.HashMap;

public class StringReplace extends DateFormatter<String> {
  public HashMap<String, String> matchList = new HashMap<>();

  public StringReplace(SiteInfo siteInfo, String replacementList) {
    super(siteInfo);
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

}
