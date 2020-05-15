/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.tool;

import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.DbRun;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;


/**
 * Copy app_config data from one database to another by config_type or by a list of app_config_id's
 *
 * @author tpacht
 */
public class AppConfigCopy {


  public static void main(String[] args) {
    try {
      String siteId = System.getProperty("site");
      if (missing(siteId)) {
        System.err.println("No system parameter found for 'site'!");
        System.exit(1);
      }
      final Long surveySiteId = new Long(siteId);
      final String extractType = System.getProperty("byType");
      String extractIds = System.getProperty("byIds");
      if (missing(extractType) && missing(extractIds)) {
        System.err.println("You must define a system parameter for either byType or byIds!");
        System.exit(1);
      }

      Builder fromRegistry = DatabaseProvider.fromDriverManager(
          System.getProperty("fromdatabase.url"),
          System.getProperty("fromdatabase.user"),
          System.getProperty("fromdatabase.password")
      ).withSqlInExceptionMessages().withSqlParameterLogging();
      Builder toRegistry = DatabaseProvider.fromDriverManager(
          System.getProperty("todatabase.url"),
          System.getProperty("todatabase.user"),
          System.getProperty("todatabase.password")
      ).withSqlInExceptionMessages().withSqlParameterLogging();
      final List<AppConfigEntry> appConfigs = new ArrayList<>();
      fromRegistry.transact(new DbRun() {
        @Override
        public void run(Provider<Database> dbp) {
          AppConfigDao configDao = new AppConfigDao(dbp.get(), getAdminUser(dbp.get()));
          if (!missing(extractType)) {
            ArrayList<AppConfigEntry> fromConfigs =  configDao.findAllAppConfigEntry(surveySiteId, extractType);
            for (AppConfigEntry entry : fromConfigs) {
              appConfigs.add(entry);
            }
          } else {
            String[] ids = extractIds.split(",");
            for (String id : ids) {
              AppConfigEntry entry = configDao.findAppConfigEntry(new Long(id));
              appConfigs.add(entry);
            }
          }
        }
      });

      if (appConfigs.size() < 1) {
        String searchCriteria = !missing(extractType) ? "config_type " + extractType : "config ids " + extractIds;
        System.out.println("No APP_CONFIGs found for " + searchCriteria);
        System.exit(0);
      }
      toRegistry.transact(new DbRun() {
        @Override
        public void run(Provider<Database> dbp)  {
          ArrayList<String> messages = new ArrayList<>();
          AppConfigDao configDao = new AppConfigDao(dbp.get(), getAdminUser(dbp.get()));
          for (AppConfigEntry entry : appConfigs) {
            configDao.addOrEnableAppConfigEntry(surveySiteId, entry.getConfigType(), entry.getConfigName(), entry.getConfigValue());
            // get the history id to use
            Long revision = dbp.get().toSelect("SELECT max(revision_number) FROM app_config_change_history WHERE config_type = ? AND config_name = ? ")
                .argString(entry.getConfigType()).argString(entry.getConfigName()).query(new RowsHandler<Long>() {
                  @Override
                  public Long process(Rows rs) {
                    Long result = null;
                    if (rs.next()) {
                      result = rs.getLongOrNull();
                    }
                    return result;
                  }
                });
            messages.add("ADDED:" + entry.getConfigName() + "@" + revision + " ");
          }
          for (String message : messages) {
            System.out.println(message);
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static User getAdminUser(Database database) {
    User admin = new User();
    admin.setEnabled(true);
    admin.setUsername("admin");
    admin.setDisplayName("Admin");
    UserDao dao = new UserDao(database, null, null);
    admin.setUserPrincipalId(dao.findUserPrincipal("admin").userPrincipalId);
    return admin;
  }

  private static boolean missing(String parameter) {
    if (parameter == null || parameter.trim().isEmpty())
      return true;
    return false;
  }
}

