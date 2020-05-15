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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.imports.ImportDefinitionQueue;
import edu.stanford.registry.server.imports.ImportDefinitionType;
import edu.stanford.registry.server.imports.ImportResources;

import java.util.HashMap;

import com.github.susom.database.Database;

public interface ImportTypeManagerIntf {

  void importData(String[] data) throws Exception;

  void importDataEnd() throws Exception;

  void setSiteInfo(SiteInfo siteInfo);

  void setDatabase(Database database);

  void setQueue(HashMap<String, ImportDefinitionQueue> queue);

  void setResources(ImportResources resources);

  void setType(ImportDefinitionType type);
}
