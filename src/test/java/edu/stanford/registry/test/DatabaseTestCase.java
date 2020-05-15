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

package edu.stanford.registry.test;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.config.PropertyMap;
import edu.stanford.registry.server.config.ServerInit;
import edu.stanford.registry.server.SiteInfo;

import java.io.File;
import java.util.function.Supplier;

import javax.naming.Context;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Ignore;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.Flavor;

import junit.framework.TestCase;

/**
 * Super class for tests that require a database to be accessible.
 *
 * @author garricko
 */
@Ignore
public class DatabaseTestCase extends TestCase {
  // Set this to true to have a test case change the database, so you can inspect it (then re-initialize the database)
  static final boolean breakTestsByCommitting = false; // MUST BE false WHEN CHECKED IN!

  private TestHarness harness;
  protected ServerContext serverContext;
  private Builder databaseProviderBuilder;
  protected DatabaseProvider databaseProvider;
  private SiteInfo siteInfo;

  static {
    String log4jXmlFile = System.getProperty("log4j.configuration", "log4j.xml");
    if (log4jXmlFile.startsWith("file:")) {
      // The configure() call doesn't like URLs
      log4jXmlFile = log4jXmlFile.substring(5);
    }
    DOMConfigurator.configure(log4jXmlFile);

    // Put all Derby related files inside ./build to keep our working copy clean
    File directory = new File("build").getAbsoluteFile();
    if (directory.exists() || directory.mkdirs()) {
      System.setProperty("derby.stream.error.file", new File(directory, "derby.log").getAbsolutePath());
    }
  }

  public DatabaseTestCase() {
    super();
  }

  protected SiteInfo getSiteInfo() {
    return siteInfo;
  }

  @Override
  protected final void setUp() throws Exception {
    harness = new TestHarness();

    // Disable connecting to the real LDAP server
    harness.disableLdap();

    Context jndi = harness.createContext(false);
    String dataSourceKey = harness.getJndiDatasourceKey();
    Flavor flavor = harness.getDatabaseFlavor();

    databaseProvider = DatabaseProvider.fromJndi(jndi, dataSourceKey, flavor)
        .withSqlParameterLogging()
        .withSqlInExceptionMessages()
        .withDatePerAppOnly()
        .withTransactionControlSilentlyIgnored().create();
    databaseProviderBuilder = databaseProvider.fakeBuilder();

    serverContext();
    ServerUtils.initialize(".");
    ServerInit.getInstance(databaseProviderBuilder, serverContext(), "./", "./");
    postSetUp();
  }

  protected final ServerContext serverContext() {
    if (serverContext == null) {
      PropertyMap params = harness.getInitParams();
      serverContext = new ServerContext("./", params, databaseProviderBuilder, true, true, null);

      //HashMap<String,String>tsetJsons = new HashMap<String,String>();
      //tsetJsons.put(RandomSetsCreate.TSET_KSort_BackPain, RandomSetsCreate.createBackPainRandomSet().toJsonString());
      //tsetJsons.put(RandomSetsCreate.TSET_Pure_Migraine, RandomSetsCreate.createMigraineRandomSet().toJsonString());

      //serverContext.getSitesInfo().addTestProperties(siteInfo.getSiteId(), siteInfo.getProperties(), tsetJsons);
      siteInfo = serverContext.getSitesInfo().getBySiteId(1L); //siteInfo.getSiteId());
      /* HashMap<String, String> emailTemplates = siteInfo.getEmailTemplates();
      emailTemplates.put("FollowUp","Subject\nBody\n");
      emailTemplates.put("FollowUp-reminder","Subject\nBody\n");
      emailTemplates.put("Initial","Subject\nBody\n");
      emailTemplates.put("Initial-reminder","Subject\nBody\n");
      emailTemplates.put("No-appointment","Subject\nBody\n");
      emailTemplates.put("No-appointment-reminder","Subject\nBody\n");
      /* */

    }
    return serverContext;
  }

  protected void assertOnePropertyValue(String key, String expected) {
    String got = getSiteInfo().getProperty(key);
    assertEquals(expected, got);
  }

  protected void postSetUp() throws Exception {
    // Hook for subclasses to override
  }

  protected void preTearDown() throws Exception {
    // Hook for subclasses to override
  }

  @Override
  protected final void tearDown() throws Exception {
    preTearDown();
    if (breakTestsByCommitting) {
      harness.shutdownAndCommit();
    } else {
      harness.shutdownAndRollback();
    }
  }

  /**
   * Get the single shared database used by this test.
   */
  public Database getDatabase() {
    return getDatabaseProvider().get();
  }

  /**
   * Provider for the single shared database used by this test.
   */
  public Builder getDatabaseProviderBuilder() {
    return databaseProviderBuilder;
  }

  public Supplier<Database> getDatabaseSupplier() {
    return getDatabaseProvider();
  }
  public DatabaseProvider getDatabaseProvider() {
    if (databaseProvider == null) {
      databaseProvider = databaseProviderBuilder.create();
    }
    return databaseProvider;
  }

}
