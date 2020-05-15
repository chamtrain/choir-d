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

import edu.stanford.registry.shared.ServiceUnavailableException;

import java.sql.Connection;
import java.util.function.Supplier;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class ConnectionProvider implements Supplier<Connection> {
  private Context context;
  private String lookupKey;
  private DataSource dataSource = null;

  private static Logger logger = Logger.getLogger(ConnectionProvider.class);

  public ConnectionProvider(Context context, String lookupKey) {
    this.context = context;
    this.lookupKey = lookupKey;
  }

  @Override
  public Connection get() {
    if (dataSource == null) {
      try {
        dataSource = getDataSource(context, lookupKey);
      } catch (Exception e) {
        throw new ServiceUnavailableException("Unable to lookup JNDI DataSource " + lookupKey, e);
      }
    }
    try {
      return dataSource.getConnection();
    } catch (Exception e) {
      dataSource = null;
      throw new ServiceUnavailableException("Unable to getConnection from provided datasource", e);
    }
  }

  public static DataSource getDataSource(Context context, String lookupKey) {
    try {
      if (context == null) {
        logger.debug("ConnectionProvider called with null context and lookupKey " + lookupKey
            + ": getting new initialcontext from " + System.getProperty("java.naming.factory.initial"));
        context = new InitialContext();
      } else {
        if (logger.isTraceEnabled()) {
          logger.trace("ConnectionProvider called with context " + context.toString() + " lookupKey " + lookupKey);
        }
      }
      return (DataSource) context.lookup(lookupKey);
    } catch (Exception e) {
      logger.error("Unable to locate the DataSource in this context using key " + lookupKey);
    }

    // trying with context "java:comp/env/"
    try {
      logger.warn("Trying same key again with context java:comp/env");
      Context envCtx = (Context) context.lookup("java:comp/env/");
      return (DataSource) envCtx.lookup(lookupKey);
    } catch (Exception ex) {
      logger.error("Unable to locate the DataSource in JNDI using context java:comp/env/ and key " + lookupKey);
    }

    throw new ServiceUnavailableException(
        "All attempts failed to locate the DataSource in JNDI using key " + lookupKey);

  }
}
