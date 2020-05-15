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

package edu.stanford.registry.server.service;

import edu.stanford.registry.server.ServerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
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
import javax.naming.spi.InitialContextFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.github.susom.database.Flavor;

/**
 * For development and testing only. This filter can be placed in front of the
 * others to configure JNDI for database access.
 */
public class DevModeFilter implements Filter {
  private String principal; // admin, by default, else if the next is true, use X-WEBAUTH-USER if it's set
  private boolean useHeaderForPrincipal; // does buildProperties.get{"devmode.use.header") == "true"?
  private String databaseDriver;
  private String databaseUrl;
  private String databaseFlavor;
  private String databaseUser;
  private String databasePassword;
  private List<Connection> realConnections = new ArrayList<>();
  private List<Boolean> realConnectionBusy = new ArrayList<>();
  private final Object connectionLock = new Object();
  private DataSource datasource;

  private static Logger logger = null;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
  ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    if (httpRequest.getUserPrincipal() == null) {
      filterChain.doFilter(new HttpServletRequestWrapper(httpRequest) {
        @Override
        public Principal getUserPrincipal() {
          return new Principal() {
            @Override
            public String getName() {
              String fromHeader = httpRequest.getHeader("X-WEBAUTH-USER");
              if (useHeaderForPrincipal && fromHeader != null && !fromHeader.isEmpty()) {
                return fromHeader;
              }
              return principal;
            }
          };
        }
      }, response);
    } else {
      filterChain.doFilter(httpRequest, response);
    }
  }


  private String getBuildPropertiesFile() {
    String propertiesFile = System.getProperty("build.properties");
    if (propertiesFile != null) {
      return propertiesFile;
    }
    if (propertiesFile == null) {
      File f = new File("../build.properties");
      if (f.exists()) {
        return f.getAbsolutePath();
      }
    }
    String userDir = System.getProperty("user.dir");  // the dir the user launched from
    if (userDir == null) {
      return null;
    }
    File f = new File(userDir, "../build.properties");
    return f.exists() ? f.getAbsolutePath() : null;
  }


  @Override
  public void init(final FilterConfig config) throws ServletException {
    logger = Logger.getLogger(DevModeFilter.class);

    // Load configuration file if provided
    Properties buildProperties = new Properties();
    String propertiesFile = getBuildPropertiesFile();
    try {
      if (propertiesFile != null) {
        FileInputStream is = new FileInputStream(propertiesFile);
        buildProperties.load(is);
        is.close();
        logger.info("Read properties from " + propertiesFile);
      } else {
        logger.debug("Not reading properties file (no vm option -Dbuild.properties=/full/path/build.properties)");
      }
    } catch (Exception e) {
      logger.warn("Unable to read properties from file " + propertiesFile, e);
    }

    // The default principal is admin because that is the admin in our test database
    principal = System.getProperty("devmode.user", buildProperties.getProperty("devmode.user", "admin"));
    useHeaderForPrincipal = "true".equals(buildProperties.getProperty("devmode.use.header", "false"));
    String msg = "All requests will be automatically authenticated as user " + principal;
    logger.info(useHeaderForPrincipal ? (msg + " unless overridden by X-WEBAUTH-USER header") : msg);

    databaseDriver =
        System.getProperty("registry.database.driver",
        buildProperties.getProperty("registry.database.driver"));
    databaseUrl =
        System.getProperty("registry.database.url",
        buildProperties.getProperty("registry.database.url"));
    databaseFlavor =
        System.getProperty("registry.database.flavor",
        buildProperties.getProperty("registry.database.flavor"));
    databaseUser =
        System.getProperty("registry.database.user",
        buildProperties.getProperty("registry.database.user"));
    databasePassword =
        System.getProperty("registry.database.password",
        buildProperties.getProperty("registry.database.password"));

    if (databaseUrl == null) {
      databaseUrl = "jdbc:postgresql://localhost/vagrant";
    }

    if (databaseFlavor == null) {
      databaseFlavor = Flavor.fromJdbcUrl(databaseUrl).toString();
    }

    if (databaseDriver == null) {
      if (Flavor.oracle.toString().equals(databaseFlavor)) {
        databaseDriver = "oracle.jdbc.OracleDriver";
      } else {
        databaseDriver = "org.postgresql.Driver";
      }
    }

    if (databaseUrl == null || databaseUser == null || databasePassword == null) {
      logger.info("Not creating a JNDI DataSource because registry.database.* property is missing");
    } else {
      try {
        config.getServletContext().setInitParameter("registry.jndi.datasource", "java:comp/jdbc/registryJndi");
        config.getServletContext().setInitParameter("registry.jndi.datasource.flavor", databaseFlavor);

        JndiContextFactory.context = createContext(true);
        System.setProperty("java.naming.factory.initial",
            "edu.stanford.registry.server.service.DevModeFilter$JndiContextFactory");
        logger.info("Created JNDI DataSource for " + databaseUrl);
        // Test context
        // Context jndi = new InitialContext();
        // ServletContext servletContext = config.getServletContext();
        // String dataSourceKey =
        // servletContext.getInitParameter("jndi.datasource");
        // logger.info("testing lookup of " + dataSourceKey);
        // jndi.lookup(dataSourceKey);
      } catch (Exception e) {
        logger.info("Create context failed ", e);
      }
    }
  }

  @Override
  public void destroy() {
    // Nothing to do
  }

  public static class JndiContextFactory implements InitialContextFactory {
    private static Context context;

    @Override
    public Context getInitialContext(Hashtable<?, ?> hashtable) throws NamingException {
      return context;
    }
  }

  public Context createContext(final boolean realTransactions) throws Exception {
    assert realConnections.isEmpty();
    assert realConnectionBusy.isEmpty();

    Class.forName(databaseDriver).getConstructor().newInstance();
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
            // Allocate a new one if we are in "real" mode or we don't already have one
            connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
            connection.setAutoCommit(false);
            realConnections.add(connection);
            realConnectionBusy.add(Boolean.TRUE);
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
      public Connection getConnection(String username, String password) {
        logger.debug("DevModeFilter$JndiContext.getConnection(" + username + ",********)");
        return null;
      }

      @Override
      public PrintWriter getLogWriter() throws SQLException {
        logger.debug("DevModeFilter$JndiContext.getLogWriter()");
        return null;
      }

      @Override
      public int getLoginTimeout() throws SQLException {
        logger.debug("DevModeFilter$JndiContext.getLoginTimeout()");
        return 0;
      }

      @Override
      public void setLogWriter(PrintWriter out) throws SQLException {
        logger.debug("DevModeFilter$JndiContext.setLogWriter()");
        return;

      }

      @Override
      public void setLoginTimeout(int seconds) throws SQLException {
        logger.debug("DevModeFilter$JndiContext.setLoginTimeout()");
        return;
      }

      @Override
      public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        logger.debug("DevModeFilter$JndiContext.isWrapperFor()");
        return false;
      }

      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException {
        logger.debug("DevModeFilter$JndiContext.unwrap()");
        return null;
      }

      @Override
      public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        logger.debug("DevModeFilter$JndiContext.getParentLogger()");
        return null;
      }
    };
    Context context = new Context() {
      // This is the only method we should be using on the context

      @Override
      public Object lookup(String name) throws NamingException {
        logger.info("DevModeFilter$JndiContext.lookup(" + name + ")");
        if (name.equals("java:/jdbc/registryJndi")) {
          return datasource;
        }
        if (name.equals("/jdbc/registryJndi")) {
          return datasource;
        }
        if (name.equals("java:comp/jdbc/registryJndi")) {
          return datasource;
        }
        if (name.equals("java:comp/env/jdbc/registryJndi")) {
          return datasource;
        }
        if (name.equals("jdbc/registryJndi")) {
          return datasource;
        }
        if (name.equals("java:comp/env/jdbc/patsatDB")) {
          return datasource;
        }
        // return datasource;
        throw new NamingException("DevModeFilter$JndiContext:Nothing in JNDI named '" + name + "'");
      }

      @Override
      public Object lookup(Name name) throws NamingException {
        logger.info("DevModeFilter$JndiContext.lookup(Name name) is the method we're in");
        return datasource;

      }

      // All remaining methods are no-ops
      @Override
      public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.addToEnvironment()");
        return null;
      }

      @Override
      public void bind(Name name, Object obj) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.bind(Name, Object)");
        return;
      }

      @Override
      public void bind(String name, Object obj) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.bind(" + name + ",Object");
        return;
      }

      @Override
      public void close() throws NamingException {
        logger.debug("DevModeFilter$JndiContext.close()");
        return;
      }

      @Override
      public Name composeName(Name name, Name prefix) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.composeName()");
        return null;
      }

      @Override
      public String composeName(String name, String prefix) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.composeName(" + name + "," + prefix + ")");
        return null;
      }

      @Override
      public Context createSubcontext(Name name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.createSubcontext()");
        return null;
      }

      @Override
      public Context createSubcontext(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.createSubcontext(" + name + ")");
        return null;
      }

      @Override
      public void destroySubcontext(Name name) throws NamingException {

        logger.debug("DevModeFilter$JndiContext.destroySubcontext()");
        return;
      }

      @Override
      public void destroySubcontext(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.destroySubcontext(" + name + ")");
        return;
      }

      @Override
      public Hashtable<?, ?> getEnvironment() throws NamingException {
        logger.debug("DevModeFilter$JndiContext.getEnvironment()");
        Hashtable<String, Object> env = new Hashtable<>();
        env.put("description", "This is the devModeFilter implementation of Context");
        env.put("java:comp/jdbc/registryJndi", datasource);
        env.put("java:comp/env/jdbc/registryJndi", datasource);
        env.put("/jdbc/registryJndi", datasource);
        env.put("jdbc/registryJndi", datasource);
        return env;
      }

      @Override
      public String getNameInNamespace() throws NamingException {
        logger.debug("DevModeFilter$JndiContext.getNameInNamespace()");
        return "DevmodeFilterInitialContext";
      }

      @Override
      public NameParser getNameParser(Name name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.getNameParser()");
        return null;
      }

      @Override
      public NameParser getNameParser(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.getNameParser(" + name + ")");
        return null;
      }

      @Override
      public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.list()");
        return null;
      }

      @Override
      public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.list(" + name + ")");
        return null;
      }

      @Override
      public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.listBindings()");
        return null;
      }

      @Override
      public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.listBindings(" + name + ")");
        return null;
      }

      @Override
      public Object lookupLink(Name name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.lookupLink()");
        return null;
      }

      @Override
      public Object lookupLink(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.lookupLink(" + name + ")");
        return null;
      }

      @Override
      public void rebind(Name name, Object obj) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.rebind()");
        return;
      }

      @Override
      public void rebind(String name, Object obj) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.rebind(" + name + ", Object)");
        return;
      }

      @Override
      public Object removeFromEnvironment(String propName) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.removeFromEnvironment(" + propName + ")");
        return null;
      }

      @Override
      public void rename(Name oldName, Name newName) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.rename(Name," + newName + ")");
        return;
      }

      @Override
      public void rename(String oldName, String newName) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.rename(" + oldName + "," + newName + ")");
        return;
      }

      @Override
      public void unbind(Name name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.unbind(Name)");
        return;
      }

      @Override
      public void unbind(String name) throws NamingException {
        logger.debug("DevModeFilter$JndiContext.unbind(" + name + ")");
        return;
      }

    };
    return context;
  }

  private Connection createConnectionProxy(Connection delegateTo, boolean realTransactions) {
    return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        new Class[] { Connection.class }, new ConnectionInvocationHandler(delegateTo, realTransactions));
  }

  /**
   * We use a dynamic proxy rather than decorator pattern because Java 6 added
   * methods to the Connection interface which breaks source compatibility with
   * Java 5. This approach should compile and run cleanly in both versions.
   */
  private final class ConnectionInvocationHandler implements InvocationHandler {
    private final boolean realTransactions;
    private Connection connection;

    private ConnectionInvocationHandler(Connection connection, boolean realTransactions) {
      this.realTransactions = realTransactions;
      this.connection = connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (connection == null) {
        if (method.getName().equals("toString")) {
          return "Closed proxy connection";
        }
        throw new ServerException("Attempt to use a connection after closing it");
      }
      if ((method.getName().equals("commit") || method.getName().equals("rollback")) && !realTransactions) {
        return null;
      }
      if (method.getName().equals("close")) {
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
        connection = null;
        return null;
      }
      return method.invoke(connection, args);
    }
  }
}
