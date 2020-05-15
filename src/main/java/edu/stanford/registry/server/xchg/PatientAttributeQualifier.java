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

import edu.stanford.registry.shared.DataTable;

import java.util.StringTokenizer;

@SuppressWarnings("rawtypes")
public class PatientAttributeQualifier implements QualifierIntf {

  StringTokenizer stoken;
  private String qualifyingString = null;
  String[] tokens;

  public PatientAttributeQualifier(String qualifyingString) {
    this.qualifyingString = qualifyingString;
    if (qualifyingString != null) {
      stoken = new StringTokenizer(qualifyingString, ":");
      tokens = new String[stoken.countTokens()];
      int indx = 0;
      while (stoken.hasMoreTokens()) {
        tokens[indx] = stoken.nextToken();
        indx++;
      }
    } else {
      tokens = new String[0];
    }
  }

  @Override
  public String getQualifier() {

    return qualifyingString;
  }

  @Override
  public String[] getQualifiers() {

    return tokens;
  }

  @Override
  public boolean qualifies(DataTable dt) {

    return true;
  }

  @Override
  public boolean qualifies(String string) {
    // TODO implement me!
    return false;
  }

}
