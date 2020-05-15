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

package edu.stanford.registry.server.imports.data;

import edu.stanford.registry.server.xchg.DefinitionDataSource;

import java.util.Comparator;

public class ImportDataSourceComparator<T> implements Comparator<DefinitionDataSource> {

  @Override
  public int compare(DefinitionDataSource src1, DefinitionDataSource src2) {
    if (src1 == null || src2 == null) {
      return 0;
    }
    if (src1.getImportOrder() > src2.getImportOrder()) {
      return 1;
    }

    if (src1.getImportOrder() < src2.getImportOrder()) {
      return -1;
    }
    return 0;
  }

}
