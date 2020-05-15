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

import edu.stanford.registry.server.ServerException;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.config.PropertyMap;
import edu.stanford.registry.server.config.PropertyMapFromHash;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.github.susom.database.Flavor;

public class TestHarness {
  private static final String JNDI_NAME = "something/very/configurable";
  private String databaseDriver;
  private String databaseUrl;
  private String databaseUser;
  private String databasePassword;
  private String log4jXmlFile;
  private String applicationHome;
  private Properties buildProperties;

  private List<Connection> realConnections = new ArrayList<>();
  private List<Boolean> realConnectionBusy = new ArrayList<>();
  private final Object connectionLock = new Object();
  private DataSource datasource;
  private HashMap<String, String> params = new HashMap<>();

  private static Logger logger = Logger.getLogger(TestHarness.class);

  public TestHarness() {
    // Load configuration options
    buildProperties = new Properties();
    try {
      // FileInputStream is = new FileInputStream("../registry.build.properties");
      FileInputStream is = new FileInputStream("../build.properties");
      buildProperties.load(is);
      is.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    log4jXmlFile = System.getProperty("log4j.configuration", buildProperties.getProperty("log4j.configuration",
        "log4j.xml"));
    if (log4jXmlFile.startsWith("file:")) {
      // The configureAndWatch() call doesn't seem to work with URLs
      log4jXmlFile = log4jXmlFile.substring(5);
    }

    databaseUrl = findAndSaveProperty("registry.database.url");
    if (databaseUrl == null) {
      databaseUrl = findAndSaveProperty("database.url");
    }

    databaseUser = findAndSaveProperty("registry.database.user");
    databasePassword = findAndSaveProperty("registry.database.password");
    databaseDriver = findAndSaveProperty("registry.database.driver");
    if (databaseDriver == null) {
      databaseDriver = findAndSaveProperty("database.driver");
    }

    applicationHome = findAndSaveProperty("registryHome");
    if (applicationHome == null) {
      applicationHome = "./";
    }
    logger.debug("databaseUrl = "+databaseUrl);
    // getProperty("client.logging.properties");
    // getProperty("server.name");
    // getProperty("PatientIdFormatterClass");
    Enumeration<?> names = buildProperties.keys();
    while (names.hasMoreElements()) {
      String key = (String) names.nextElement();
      findAndSaveProperty(key);
    }

    params.put("log4j.configuration", log4jXmlFile);
    params.put("jndi.datasource", JNDI_NAME);
    params.put("PatientIdFormatErrorMessage",
        "Patient Id must be 5-7 characters followed by - and a single digit.");
    params.put("PatientIdFormatterClass", "edu.stanford.registry.server.utils.StanfordMrn");
    params.put("database.driver", databaseDriver);
    params.put("email.template.directory", "src/main/resources/default/email-templates");
    params.put("PatientIdFormat", "d{5,7}-d{1}|d{5,9}");
    params.put("default.dateFormat", "MM/dd/yyyy");
    params.put("default.dateTimeFormat", "MM/dd/yyyy h:mm a");
    params.put("registry.email.file", "build/email.log");
  }

  
  public void disableLdap() {
    params.put("disable.ldap", "true");
  }

  public String getLog4jXmlFile() {
    return log4jXmlFile;
  }

  /**
   * @return parameters used in constructor, and a bunch more
   */
  public PropertyMap getInitParams() {
    return new PropertyMapFromHash(params);
  }

  public String getJndiDatasourceKey() {
    return params.get("jndi.datasource");
  }

  public Flavor getDatabaseFlavor() {
    return Flavor.fromJdbcUrl(databaseUrl);
  }

  public Connection getConnection() throws SQLException {
    return datasource.getConnection();
  }

  private Connection createConnectionProxy(Connection delegateTo, boolean realTransactions) {
    return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        new Class[] { Connection.class }, new ConnectionInvocationHandler(delegateTo, realTransactions));
  }

  /**
   * We use a dynamic proxy rather than decorator pattern because Java 6 added methods to the Connection interface which breaks source
   * compatibility with Java 5. This approach should compile and run cleanly in both versions.
   */
  private final class ConnectionInvocationHandler implements InvocationHandler {
    private final boolean realTransactions;
    private Connection connection;

    private ConnectionInvocationHandler(Connection connection, boolean realTransactions) {
      this.realTransactions = realTransactions;
      this.connection = connection;
    }

    Exception lastInvoke;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (connection == null) {
        if (method.getName().equals("toString")) {
          return "Closed proxy connection";
        }
        throw new ServerException("Attempt to use a connection("+connection+") after closing it", lastInvoke);
      }
      try {
        throw new Exception("Connection="+connection.toString()+": "+method.toString());
      } catch (Exception e) {
        lastInvoke = e;
      }
      if ((method.getName().equals("commit") || method.getName().equals("rollback")) && !realTransactions) {
        return null;
      }
      boolean release = (method.getName().equals("getClientInfo") && args.length == 1 && args[0] instanceof String
          && args[0].equals("release"));
      if (method.getName().equals("close") || release) {
        // Unlock the underlying connection so someone else can use it
        synchronized (connectionLock) {
          // connectionOwner = null;
          for (int i = 0; i < realConnections.size(); i++) {
            if (realConnections.get(i) == connection) {
              realConnectionBusy.set(i, Boolean.FALSE);
              break;
            }
          }
          connectionLock.notify();
        }
        // Make sure no one can use this after closing
        if (!release) {
          connection = null;
          return null;
        }
      }

      return method.invoke(connection, args);

    }
  }

  public Context createContext(final boolean realTransactions) throws Exception {
    assert realConnections.isEmpty();
    assert realConnectionBusy.isEmpty();

    // Initialize parameters & ServerUtils
    new ServerUtils(applicationHome);

    if (databaseDriver != null) {
      Class.forName(databaseDriver);
    }

    // Set up a JDBC connection. Note we use only one connection for the test harness,
    // and the FakeFilter will synchronize all server calls. This allows better control
    // of database transactions so tests can execute a bunch of calls and then rollback

    datasource = new DataSource() {
      // This is the only method we should be using
      @Override
      public synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
        synchronized (connectionLock) {
          // Find an available connection
          for (int i = 0; i < realConnections.size(); i++) {
            if (!realConnectionBusy.get(i)) {
              connection = realConnections.get(i);
              realConnectionBusy.set(i, Boolean.TRUE);
              break;
            }
          }
          if (connection == null && (realConnections.isEmpty() || realTransactions)) {
            try {
              // Allocate a new one if we are in "real" mode or we don't already have one
              connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
              connection.setAutoCommit(false);
              realConnections.add(connection);
              realConnectionBusy.add(Boolean.TRUE);
            } catch (Exception ex) {
              ex.printStackTrace();
              throw new ServerException(ex);
            }
          }

          if (connection == null) {
            // Block for the shared one if we are not in "real" mode
            long deadline = System.currentTimeMillis() + 60000L;
            while (realConnectionBusy.get(0) && System.currentTimeMillis() < deadline) {
              try {
                connectionLock.wait(deadline - System.currentTimeMillis());
              } catch (InterruptedException e) {
                throw new ServerException("Timed out waiting for database connection", e);
              }
            }
            if (realConnectionBusy.get(0)) {
              throw new ServerException("Timed out waiting for database connection");
            }
            connection = realConnections.get(0);
            realConnectionBusy.set(0, Boolean.TRUE);
          }
        }
        return createConnectionProxy(connection, realTransactions);
      }

      // All remaining methods are stubbed
      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return null;
      }

      @Override
      public PrintWriter getLogWriter() throws SQLException {
        return null;
      }

      @Override
      public int getLoginTimeout() throws SQLException {
        return 0;
      }

      @Override
      public void setLogWriter(PrintWriter out) throws SQLException {
      }

      @Override
      public void setLoginTimeout(int seconds) throws SQLException {
      }

      @Override
      public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
      }

      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
      }

      @Override
      public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
      }
    };
    Context context = new Context() {
      // This is the only method we should be using on the context
      @Override
      public Object lookup(String name) throws NamingException {
       if (name.equals(JNDI_NAME)) { // What name is this?  ras!!!
          return datasource;
        }
        throw new NamingException("Nothing in JNDI named '" + name + "'");
      }

      // All remaining methods are no-ops
      @Override
      public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
      }

      @Override
      public void bind(Name name, Object obj) throws NamingException {
      }

      @Override
      public void bind(String name, Object obj) throws NamingException {
      }

      @Override
      public void close() throws NamingException {
      }

      @Override
      public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
      }

      @Override
      public String composeName(String name, String prefix) throws NamingException {
        return null;
      }

      @Override
      public Context createSubcontext(Name name) throws NamingException {
        return null;
      }

      @Override
      public Context createSubcontext(String name) throws NamingException {
        return null;
      }

      @Override
      public void destroySubcontext(Name name) throws NamingException {
      }

      @Override
      public void destroySubcontext(String name) throws NamingException {
      }

      @Override
      public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;
      }

      @Override
      public String getNameInNamespace() throws NamingException {
        return null;
      }

      @Override
      public NameParser getNameParser(Name name) throws NamingException {
        return null;
      }

      @Override
      public NameParser getNameParser(String name) throws NamingException {
        return null;
      }

      @Override
      public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;
      }

      @Override
      public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;
      }

      @Override
      public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;
      }

      @Override
      public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return null;
      }

      @Override
      public Object lookup(Name name) throws NamingException {
        return null;
      }

      @Override
      public Object lookupLink(Name name) throws NamingException {
        return null;
      }

      @Override
      public Object lookupLink(String name) throws NamingException {
        return null;
      }

      @Override
      public void rebind(Name name, Object obj) throws NamingException {
      }

      @Override
      public void rebind(String name, Object obj) throws NamingException {
      }

      @Override
      public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
      }

      @Override
      public void rename(Name oldName, Name newName) throws NamingException {
      }

      @Override
      public void rename(String oldName, String newName) throws NamingException {
      }

      @Override
      public void unbind(Name name) throws NamingException {
      }

      @Override
      public void unbind(String name) throws NamingException {
      }
    };
    return context;
  }

  public void shutdownAndCommit() throws Exception {
    shutdown(true);
  }

  public void shutdownAndRollback() throws Exception {
    shutdown(false);
  }

  public void rollback() throws Exception {
    for (Connection connection : realConnections) {
      try {
        connection.rollback();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void commit() throws Exception {
    for (Connection connection : realConnections) {
      try {
        connection.commit();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void shutdown(boolean commit) throws Exception {
    // Clean up the database connection
    if (commit) {
      commit();
    } else {
      rollback();
    }
    for (Connection connection : realConnections) {
      try {
        connection.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    realConnections.clear();

    if ("org.apache.derby.jdbc.EmbeddedDriver".equals(databaseDriver)) {
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    }
  }

  private String findAndSaveProperty(String propName) {
    String propValue = System.getProperty(propName, buildProperties.getProperty(propName));
    if (propValue != null) {
      if (propValue.startsWith("$(") || propValue.startsWith("${")) {
        propValue = buildProperties.getProperty(propName);
      }
      params.put(propName, propValue);
    }
    logger.debug("  TestHarness: prop "+propName+" = "+propValue);
    return propValue;
  }

}
