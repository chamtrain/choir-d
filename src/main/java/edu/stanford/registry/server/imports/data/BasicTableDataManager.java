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
import edu.stanford.registry.server.imports.ImportFactory;
import edu.stanford.registry.server.imports.ImportResources;
import edu.stanford.registry.server.xchg.DefinitionDataSource;
import edu.stanford.registry.server.xchg.ImportException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class BasicTableDataManager implements ImportTypeManagerIntf {
  private static Logger logger = Logger.getLogger(BasicTableDataManager.class);

  private SiteInfo siteInfo;
  private Database database;
  private HashMap<String, ImportDefinitionQueue> queuesMap;
  private ImportResources resources;
  private boolean initialized = false;
  private ImportDataSourceManagerIntf[] managers = null;
  private ImportDefinitionType handlingType;

  public BasicTableDataManager() {
    // A public default construct is required- ImportFactory creates this from its name
  }

  public BasicTableDataManager(SiteInfo siteInfo, Database database, ImportResources resources, HashMap<String, ImportDefinitionQueue> queue,
      ImportDefinitionType type) {
    this.siteInfo = siteInfo;
    this.database = database;
    this.resources = resources;
    this.queuesMap = queue;
    this.handlingType = type;
  }

  @Override
  public void importData(String[] data) throws Exception {

    if (!initialized) {
      try {
        init();
      } finally {
        if (managers == null) { // already threw an exception for no managers- avoid an NPE if this is called again
          managers = new ImportDataSourceManagerIntf[0];
        }
      }
    }

    if (data == null) {
      throw new ImportException("No data");
    }

    for (ImportDataSourceManagerIntf manager : managers) {
      if (manager != null) {  // if 1+ mgrs werent assigned due to a bad classname, another NPE is distracting
        manager.importData(data);
      }
    }
  }

  @Override
  public void importDataEnd() throws Exception {
    if (managers != null) {
      for (ImportDataSourceManagerIntf manager : managers) {
        if (manager != null) {
          manager.importDataEnd();
        }
      }
    }
  }

  @Override
  public void setSiteInfo(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  @Override
  public void setDatabase(Database database) {
    this.database = database;
  }

  @Override
  public void setQueue(HashMap<String, ImportDefinitionQueue> queue) {
    queuesMap = queue;
  }

  @Override
  public void setResources(ImportResources resources) {
    this.resources = resources;
  }

  private void init() throws Exception {

    ImportFactory factory = ImportFactory.getFactory();

    // Check that we have what we need
    if (resources == null || resources.getDataSourceDefinitionsHash() == null) {
      throw new ImportException("Resources definitions are missing");
    }

    if (database == null) {
      throw new ImportException("Database is missing");
    }

    if (queuesMap == null) {
      throw new ImportException("No data managers are defined");
    }
    logger.debug("queuesMap has " + queuesMap.size() + " entries");

    // create an ordered list of datasource managers
    Iterator<Map.Entry<String, ImportDefinitionQueue>> mapIt = queuesMap.entrySet().iterator();
    ArrayList<DefinitionDataSource> dataSourceList = new ArrayList<>();
    while (mapIt.hasNext()) {
      Map.Entry<String, ImportDefinitionQueue> entry = mapIt.next();
      String dataSourceName = entry.getKey();
      DefinitionDataSource ds = resources.getDefinitionDataSource(dataSourceName);
      if (handlingType != null && handlingType.getType() != null && ds != null
          && handlingType.getType().equals(ds.getType())) {
        dataSourceList.add(ds);
      }
      // mapIt.remove(); // force it to the next one
    }

    Collections.sort(dataSourceList, new ImportDataSourceComparator<DefinitionDataSource>());
    managers = new ImportDataSourceManagerIntf[dataSourceList.size()];
    for (int d = 0; d < dataSourceList.size(); d++) {
      logger.debug("Creating manager for " + dataSourceList.get(d).getDataSource());

      ImportDataSourceManagerIntf manager = factory.getDataSourceManager(siteInfo, dataSourceList.get(d), resources, database,
          queuesMap);
      if (manager == null) {
        throw new Exception("manager not found for datasource " + dataSourceList.get(d).getDataSource());
      }
      managers[d] = manager;
      initialized = true;
    }
    logger.debug(managers.length + " managers will be used");

  }

  @Override
  public void setType(ImportDefinitionType type) {
    handlingType = type;
  }
}
