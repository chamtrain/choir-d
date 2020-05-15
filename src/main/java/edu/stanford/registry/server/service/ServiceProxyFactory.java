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

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.Metric;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.server.ClientIdentifiers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;

public class ServiceProxyFactory {
  private static final Logger logger = LoggerFactory.getLogger(ServiceProxyFactory.class);
  private Builder dbProv;

  public ServiceProxyFactory(Builder dbProv) {
    this.dbProv = dbProv;
  }

  public Object createService(ClientIdentifiers clientIds, User user, ServerContext serverContext,
                              Service service, SiteInfo siteInfo) {

    return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { service
        .getInterfaceClass() }, new ServiceInvocationHandler(clientIds, user, dbProv, service, serverContext, siteInfo));
  }

  private static final class ServiceInvocationHandler implements InvocationHandler {
    private final Builder dbProv;
    private final Service service;
    private final ServerContext serverContext;
    private ClientIdentifiers clientIds;
    private final User user;
    private SiteInfo siteInfo;

    private ServiceInvocationHandler(ClientIdentifiers clientIds, User user, Builder dbProv, Service service,
                                     ServerContext serverContext, SiteInfo siteInfo) {
      this.clientIds = clientIds;
      this.user = user;
      this.dbProv = dbProv;
      this.service = service;
      this.serverContext = serverContext;
      this.siteInfo = siteInfo;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      // This is a bit of a hack - seems the json-rpc library should not be
      // calling this

      if (method.getName().equals("hashCode")) {
        return service.hashCode();
      }
      DatabaseProvider databaseProvider = dbProv.create();
      boolean commit = false;
      Metric metric = new Metric(logger.isDebugEnabled());
      Object serviceImpl = null;
      StringBuilder msg = new StringBuilder(100);
      try {
        ServiceImpl serviceImplWrapper = ServiceImpl.byService(service);
        serviceImpl = serviceImplWrapper.create(clientIds, user, databaseProvider, serverContext, siteInfo);
        metric.checkpoint("createSvc");
        if (serviceImpl != null)
          msg.append(serviceImpl.getClass().getName()).append(" . ").append(method.getName()).append("(").append(method.getParameterTypes().length).append(" params)");

        Method targetMethod = serviceImpl.getClass().getMethod(method.getName(), method.getParameterTypes());
        msg.append(" + calledWi(");

        Object result = targetMethod.invoke(serviceImpl, args);
        metric.checkpoint("invokeSvc");
        commit = true;
        return result;
      } catch (InvocationTargetException t) {
        if ("toString()".equals(method.getName()) && serviceImpl != null) {
          return serviceImpl.getClass()+".toString() IS_NOT_IMPLEMENTED";
        }
        msg.append(args == null ? 0 : args.length).append(" args)");
        logger.warn("SvcPrxyFctry, failed to call: "+msg+" ", t);
        // log.trace("Error invoking service method", t);
        if (t.getTargetException() != null) {
          throw t.getTargetException();
        }
        if (t.getCause() != null) {
          throw t.getCause();
        }
        if (msg.length() == 0) {
          throw new RuntimeException("Server error invoking method: "+method.getName(), t);
        } else {
          throw new RuntimeException("Server error invoking method: "+msg, t);
        }
      } finally {
        if (commit) {
          databaseProvider.commitAndClose();
          metric.checkpoint("commit");
        } else {
          databaseProvider.rollbackAndClose();
          metric.checkpoint("rollback");
        }
        metric.done();
        if (logger.isDebugEnabled()) {
          String site = (siteInfo == null) ? "noSite" : siteInfo.getIdString();
          logger.debug("Invoke (" + site + ") " + service + "." + method.getName() + " " + metric.getMessage());
        }
      }
    }
  }
}
