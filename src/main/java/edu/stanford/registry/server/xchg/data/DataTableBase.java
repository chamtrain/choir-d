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

import edu.stanford.registry.server.export.CommonQualifier;
import edu.stanford.registry.server.export.ExportFormatterIntf;
import edu.stanford.registry.server.export.data.DataTableIntf;
import edu.stanford.registry.server.xchg.QualifierIntf;

public class DataTableBase implements DataTableIntf {

  @Override
  public QualifierIntf<DataTableBase> getQualifier(String qualifyingString) {
    // TODO Auto-generated method stub
    return new CommonQualifier();
  }

  @Override
  public ExportFormatterIntf<String> getFormatter(String formatterString) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getChildrenDataSourceNames() {
    // Extending classes need to override this method.
    return null;
  }

}
