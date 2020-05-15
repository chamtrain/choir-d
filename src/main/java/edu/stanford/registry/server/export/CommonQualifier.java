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

import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.server.xchg.data.DataTableBase;
import edu.stanford.registry.shared.DataTable;

public class CommonQualifier implements QualifierIntf<DataTableBase> {

  String[] qualifiers = new String[0];

  @Override
  public String getQualifier() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getQualifiers() {
    return qualifiers;
  }


  @Override
  public boolean qualifies(DataTable dt) {
    // By default everything qualifies
    return true;
  }

  @Override
  public boolean qualifies(String string) {
    // By default everything qualifies
    return true;
  }

}
