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
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Make sure we respect GWT's naming convention by explicitly preventing browser/proxy
 * caching of *.nocache.* resources and ensuring caching of *.cache.* resources.
 * Otherwise browsers may keep older versions of the application after we push a release
 * or may have suboptimal performance over limited bandwidth connections.
 *
 * Thanks to http://seewah.blogspot.com/2009/02/gwt-tips-2-nocachejs-getting-cached-in.html.
 */
public class CacheFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
      ServletException {
    String requestUri = ((HttpServletRequest) request).getRequestURI();

    // The default page (/web) should be treated as .nocache.html
    if (requestUri.contains(".nocache.") || requestUri.equals("/web") || requestUri.equals("/web/")) {
      Date now = new Date();
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setDateHeader("Date", now.getTime());
      httpResponse.setDateHeader("Expires", now.getTime() - 86400000L /* one day */);
      httpResponse.setHeader("Pragma", "no-cache");
      httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
    } else if (requestUri.contains(".cache.")) {
      Date now = new Date();
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      httpResponse.setDateHeader("Date", now.getTime());
      httpResponse.setDateHeader("Expires", now.getTime() + 31536000000L /* one year */);
      httpResponse.setHeader("Cache-control", "max-age=31536000");
    }

    filterChain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // nothing to do
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    // nothing to do
  }
}
