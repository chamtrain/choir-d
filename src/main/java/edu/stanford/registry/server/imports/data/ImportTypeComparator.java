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

import edu.stanford.registry.server.imports.ImportDefinitionType;

import java.util.Comparator;

public class ImportTypeComparator<T> implements Comparator<ImportDefinitionType> {

  @Override
  public int compare(ImportDefinitionType typ1, ImportDefinitionType typ2) {
    if (typ1 == null || typ2 == null) {
      return 0;
    }
    if (typ1.getOrder() > typ2.getOrder()) {
      return 1;
    }

    if (typ1.getOrder() < typ2.getOrder()) {
      return -1;
    }
    return 0;
  }

}
