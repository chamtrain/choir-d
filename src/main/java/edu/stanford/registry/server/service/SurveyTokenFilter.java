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

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.log4j.MDC;

/**
 * Grab a survey token from the request headers and use it as the "authentication".
 */
public class SurveyTokenFilter implements Filter {
  @Override
  public void init(FilterConfig config) throws ServletException {
    // Nothing to do
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws ServletException, IOException {

    HttpServletRequest request = (HttpServletRequest) req;
    final String token = request.getHeader("X-SURVEY-TOKEN");
    final String resume = request.getHeader("X-RESUME-TOKEN");

    if (token != null && token.matches("[0-9a-zA-Z:]+")) {
      try {
        MDC.put("surveyToken", token);

        final Principal principal = new Principal() {
          @Override
          public String getName() {
            return "surveyToken:" + token;
          }
        };
        chain.doFilter(new HttpServletRequestWrapper(request) {
          @Override
          public Principal getUserPrincipal() {
            return principal;
          }
        }, resp);
      } finally {
        MDC.remove("surveyToken");
      }
    } else if (resume != null && resume.matches("[0-9a-zA-Z]+")) {
      try {
        MDC.put("surveyToken", "resume:" + resume);

        final Principal principal = new Principal() {
          @Override
          public String getName() {
            return "resumeToken:" + resume;
          }
        };
        chain.doFilter(new HttpServletRequestWrapper(request) {
          @Override
          public Principal getUserPrincipal() {
            return principal;
          }
        }, resp);
      } finally {
        MDC.remove("surveyToken");
      }
    } else {
      chain.doFilter(req, resp);
    }
  }

  @Override
  public void destroy() {
    // Nothing to do
  }
}
