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

package edu.stanford.registry.server;

import edu.stanford.registry.shared.api.ClientService;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.User;

import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import com.google.gwt.logging.server.StackTraceDeobfuscator;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is created by Tomcat from reading web.xml
 * @author Nara
 */
@SuppressWarnings("deprecation")
public class ClientServiceImpl extends RemoteServiceServlet implements ClientService {
  private static final long serialVersionUID = -1406225831386044008L;

  private static Logger logger = Logger.getLogger(ClientServiceImpl.class);

  // These are for the logger, to deobfuscate the stack before passed to server
  private static PatternLayout layout = new PatternLayout("CLIENT: %d{ABSOLUTE} %c{2} %m");
  private StackTraceDeobfuscator deobfuscator;
  private String symbolMapPath;

  public ClientServiceImpl() {
  }

  public interface SessionCallback {
    void addWindowId(String windowId);

    boolean hasCachedWindowId(String windowId);
  }

  @Override
  public User getUser() {
    try {
      ClientService svc = getService();
      return svc.getUser();
    } catch (Exception e) {
      logger.error(e);
    }
    return null;
  }

  @Override
  public ClientConfig getClientConfig() {
    try {
      ClientService svc = getService();
      ClientConfig config = svc.getClientConfig();
      return config;
    } catch (Exception e) {
      logger.error(e);
    }
    return null;
  }

  @Override
  public HashMap<String, String> getInitParams() {
    try {
      ClientService svc = getService();
      return svc.getInitParams();
    } catch (Exception e) {
      logger.error(e);
    }
    return new HashMap<String, String>(0);
  }

  @Override
  public String getSiteConfig(String configName) {
    try {
      return getService().getSiteConfig(configName);
    } catch (Exception e) {
      logger.error(e);
    }
    return null;
  }

  @Override
  public void clientLog(List<LogRecord> records) {
    // No need to call the registry service's method.
    // This object's superclass methods are needed to deobfuscate the stack

    for (LogRecord record : records) {
      if (record.getLevel().intValue() > Level.WARNING.intValue()) {
        logger.error(formatLogRecord(record), record.getThrown());
      } else if (record.getLevel().intValue() > Level.INFO.intValue()) {
        logger.warn(formatLogRecord(record), record.getThrown());
      } else if (record.getLevel().intValue() == Level.INFO.intValue()) {
        logger.info(formatLogRecord(record), record.getThrown());
      } else {
        logger.debug(formatLogRecord(record), record.getThrown());
      }
    }
  }

  private String formatLogRecord(LogRecord record) {
    String message = record.getMessage();
    final Throwable thrown = record.getThrown();
    if (thrown != null && productionMode()) {
      thrown.setStackTrace(getDeobfuscator().deobfuscateStackTrace(thrown.getStackTrace(), getPermutationStrongName()));
      message += "\n--- Deobfuscated client stack trace:";
    }
    return layout.format(new LoggingEvent(record.getLoggerName(), Category.getInstance(record.getLoggerName()),
        Priority.DEBUG, message, thrown));
  }

  private StackTraceDeobfuscator getDeobfuscator() {

    if (deobfuscator == null) {
      HttpServletRequest request = getThreadLocalRequest();
      String uri = request.getRequestURI();
      StringTokenizer stok = new StringTokenizer(uri, "/", false);
      if (stok.countTokens() > 0) {
        symbolMapPath = stok.nextToken() + "/symbolMaps/"; // use the module name
      } else {
        symbolMapPath = "/registry/symbolMaps/";
      }
      deobfuscator = new StackTraceDeobfuscator(".") {

        @Override
        protected InputStream getSymbolMapInputStream(String permutationStrongName) throws IOException {
          // final String resource = "/web/symbolMaps/" + permutationStrongName + ".symbolMap";
          final String resource = symbolMapPath + permutationStrongName + ".symbolMap";
          logger.debug("Loading symbol map from path: " + resource);

          try {
            final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (stream == null) {
              throw new IOException("Couldn't find resource at: " + resource);
            }
            return stream;
          } catch (Exception e) {
            logger.warn("Unable to load symbol map: " + resource, e);
            // The superclass catches these
            throw new IOException("Couldn't find resource: " + resource, e);
          }
        }
      };
    }
    return deobfuscator;
  }

  private boolean productionMode() {
    return !"HostedMode".equals(getPermutationStrongName());
  }

  // ==== end of logging code


  private ClientService getService() throws Exception {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    ClientService clientServices = (ClientService) regRequest.getService();
    // logger.info("DELETEME ClientServicesImpl.getService() -> "+clientServices);
    return clientServices;
  }
}