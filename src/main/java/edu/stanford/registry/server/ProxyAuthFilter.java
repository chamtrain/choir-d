/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.MDC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter sanity checks to make sure WebAuth authentication took place,
 * and provides the resulting principal to the downstream servlets.
 * 
 * @author garricko
 */
public class ProxyAuthFilter implements Filter {
  private static final String PROXYAUTH_PROXY_HOSTS = "proxyauth.proxy.hosts";
  private static final Logger log = LoggerFactory.getLogger(ProxyAuthFilter.class);

  private String header;     // the name of the header containing the user name
  private String attribute;  // or, the name of attribute containing the user name

  private Set<String> proxyHosts = new HashSet<>(); // only these hosts and addresses are allowed
  private String forwardedIp; // if set, and a header has this name, its value is the host & addr downstream


  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
                  throws IOException, ServletException {

    if (!request.isSecure()) {
      throw new ServletException("You must be using an encrypted channel such as TLS/SSL");
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    approveProxyHosts(httpRequest);

    final ProxyHost proxyHost = getProxyHostFromRequestOrForwardedIpHeader(httpRequest);

    final String user = getUserOrThrowException(httpRequest);
    ProxyAuthRequestWrapper wrappedRequest = new ProxyAuthRequestWrapper(httpRequest, user, proxyHost);

    // Put the user in the MDC and continue the filter chain with the wrapped request
    try {
      MDC.put("userId", user);
      filterChain.doFilter(wrappedRequest, response);
    } finally {
      MDC.remove("userId");
    }
  }


  @Override
  public void init(final FilterConfig config) throws ServletException {

    // Require explicit enabling of reading authenticated user from a header because
    // it requires careful configuration of the proxy to avoid security problems
    // (have to make sure the proxy will not pass this header from the client)
    header = config.getServletContext().getInitParameter("proxyauth.header");
    attribute = config.getServletContext().getInitParameter("proxyauth.attribute");
    forwardedIp = config.getServletContext().getInitParameter("proxyauth.forwarded.ip");

    // Allow for locking down access to a specific list of proxy servers (comma separated
    // fully qualified host names or ip addresses)
    String proxyHostsProp = config.getServletContext().getInitParameter(PROXYAUTH_PROXY_HOSTS);
    if (proxyHostsProp != null && proxyHostsProp.length() > 0) {
      for (String proxy : proxyHostsProp.split(",")) {
        if (proxy != null && proxy.length() > 0) {
          proxyHosts.add(proxy);
        }
      }
    }
  }

  @Override
  public void destroy() {
    // Nothing to do
  }

  static final class ProxyHost {
    final String remoteHost, remoteAddr;

    ProxyHost(String host, String addr) {
      remoteHost = host;
      remoteAddr = addr;
    }
  }


  void approveProxyHosts(HttpServletRequest request) throws ServletException {
    if (proxyHosts.isEmpty())
      return;  // no approved proxies have been configured (must be a dev installation)

    String remoteHost = request.getRemoteHost();
    String remoteAddr = request.getRemoteAddr();

    if (proxyHosts.contains(remoteAddr) || !proxyHosts.contains(remoteHost))
      return;  // all is well. It's approved

    System.err.println("Rejecting proxy " + remoteHost + " (" + remoteAddr + ")");
    throw new ServletException("Unknown proxy (check " + PROXYAUTH_PROXY_HOSTS + " value)");
  }


  ProxyHost getProxyHostFromRequestOrForwardedIpHeader(HttpServletRequest request) {
    if (forwardedIp != null) {
      // Make the real IP available to the application
      String ip = request.getHeader(forwardedIp);
      if (ip != null)
        return new ProxyHost(ip, ip);
    }

    return new ProxyHost(request.getRemoteHost(), request.getRemoteAddr());
  }


  String getUserOrThrowException(HttpServletRequest request) {
    String user = null;
    if (attribute != null) {
      // Try to read the username from a mod_jk attribute
      user = (String) request.getAttribute(attribute);
      if (user == null) {
        throw new SecurityException("No remote user received from proxy (check proxyauth.attribute)");
      }

    } else if (header != null) {
      // Try to read the username from a mod_proxy_http header
      user = request.getHeader(header);
      if (user == null) {
        throw new SecurityException("No remote user received from proxy (check proxyauth.header)");
      } else {
        user = user.substring(user.indexOf(":")+1); //For IAM and GCIP methods in IAP
      }

      // When running in Google Cloud using the Identity Aware Proxy the Google authenticated
      // user is prefixed with 'accounts.google.com:'. Remove this prefix from the username.
      //if (user.startsWith("accounts.google.com:")) {
      // user = user.substring("accounts.google.com:".length());
      //}

    } else {
      throw new SecurityException("Proxy authentication is not configured");
    }

    if (!user.matches("[a-z0-9.]+([@][a-z0-9]+([.][a-z0-9]+)*)?")) {
      log.info("ServletRequest User : " + user);
      throw new SecurityException("Invalid user received from proxy");
    }

    return user;
  }


  static class ProxyAuthRequestWrapper extends HttpServletRequestWrapper {
    final String user, proxyHost, proxyAddr;

    final Principal principal;

    ProxyAuthRequestWrapper(HttpServletRequest request, String usr, ProxyHost host) {
      super(request);
      user = usr;
      proxyHost = host.remoteHost;
      proxyAddr = host.remoteAddr;

      principal = new Principal() {
        @Override
        public String getName() {
          return user;
        }
      };
    }

    @Override
    public Principal getUserPrincipal() {
      return principal;
    }

    @Override
    public String getRemoteHost() {
      return proxyHost;
    }

    @Override
    public String getRemoteAddr() {
      return proxyAddr;
    }
  }


}
