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

import edu.stanford.registry.server.utils.Spreadsheet;
import edu.stanford.registry.server.xchg.DefinitionDataSourcesXlsx;
import edu.stanford.registry.server.xchg.data.Constants;

public class ExportDefinitionDataSourcesXlsx extends DefinitionDataSourcesXlsx {
  Spreadsheet sheet = null;

  public ExportDefinitionDataSourcesXlsx(String path) throws Exception {

    super(path, Constants.EXPORT_DEFINITION_DATA_SOURCES_FILENAME);
  }

}
