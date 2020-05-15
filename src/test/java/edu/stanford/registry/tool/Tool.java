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

package edu.stanford.registry.tool;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerException;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.Metric;
import edu.stanford.registry.server.service.Service;
import edu.stanford.registry.server.service.ServiceImpl;
import edu.stanford.registry.server.service.ServiceProxyFactory;
import edu.stanford.registry.shared.Log;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.TestHarness;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

import javax.naming.Context;

import org.apache.log4j.xml.DOMConfigurator;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;

/**
 * A convenience class for writing command line "server" code that uses services and/or database
 * connections. Just extend this class and add a main() method.
 */
public class Tool {
  private static TestHarness harness;
  private static ServerContext serverContext;
  @SuppressWarnings("unused")
  private static Supplier<Connection> connectionProvider;
  private static Builder databaseProviderBuilder;
  private static DatabaseProvider databaseProvider;

  static {
    try {
      harness = new TestHarness();

      // Disable connecting to the real LDAP server
      harness.disableLdap();

      DOMConfigurator.configureAndWatch(harness.getLog4jXmlFile(), 5000L);

      // Route all java.util.logging output into log4j (so we can format it like the rest)
//      JULLog4jBridge.assimilate();

      Context jndi = harness.createContext(true);
      connectionProvider = new Supplier<Connection>() {
        @Override
        public Connection get() {
          try {
            return harness.getConnection();
          } catch (SQLException e) {
            throw new ServerException("Could not allocate new connection", e);
          }
        }
      };
      databaseProviderBuilder = DatabaseProvider.fromJndi(jndi, harness.getJndiDatasourceKey(), harness.getDatabaseFlavor());
      databaseProvider = databaseProviderBuilder.create();

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
        @Override
        public void run() {
          shutdown();
        }
      }));
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  private static void shutdown() {
    try {
      harness.shutdownAndRollback();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected static ServerContext serverContext() {
    if (serverContext == null)
      serverContext = new ServerContext("testreg", harness.getInitParams(), databaseProviderBuilder, true, false, null);
    return serverContext;
  }

  public static boolean runTransaction(String userId, ServerRunnable runnable) {
    return runTransaction(serverContext().userInfo().forName(userId), runnable);
  }

  public static boolean runTransaction(User user, ServerRunnable runnable) {
    runnable.user = user;
    runnable.database = databaseProvider;
    boolean commit = false;
    Log log = serverContext().logFor(ServiceProxyFactory.class);
    Metric metric = new Metric(log.isDebugEnabled());
    try {
      runnable.run();
      metric.checkpoint("afterRun");
      commit = true;
    } finally {
      if (commit) {
        try {
          commit();
        } catch (Exception e) {
          log.error("Unable to commit", e);
        }
        metric.checkpoint("commit");
      } else {
        try {
          rollback();
        } catch (Exception e) {
          log.error("Unable to rollback", e);
        }
        metric.checkpoint("rollback");
      }
      metric.done();
      if (log.isDebugEnabled()) {
        log.debug("Transaction: " + metric.getMessage());
      }
    }
    return commit;
  }

  public static void commit() throws Exception {
    harness.commit();
  }

  public static void rollback() throws Exception {
    harness.rollback();
  }

  /**
   * Get the single shared database used by this test.
   */
  public static Database getDatabase() {
    return databaseProvider.get();
  }

  public static abstract class ServerRunnable implements Runnable {
    protected User user;
    protected Supplier<Database> database;

    Object createService(Service service, SiteInfo siteInfo) {
      return ServiceImpl.byService(service).create(null, user, database, serverContext, siteInfo);
    }
  }
}
