/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.config;

import java.util.Enumeration;

import javax.servlet.ServletContext;

public class PropertyMapFromServletContext implements PropertyMap {
  final ServletContext sContext;
  final int size;

  public PropertyMapFromServletContext(ServletContext servletContext) {
    this.sContext = servletContext;
    size = computeSize(servletContext);
  }

  int computeSize(ServletContext servletContext) {
    int n = 0;
    Enumeration<String> e = servletContext.getInitParameterNames();
    while (e.hasMoreElements()) {
      n++;
      e.nextElement();
    }
    return n;
  }

  @Override
  public String getString(String key) {
    return sContext.getInitParameter(key);
  }

  @Override
  public Enumeration<String> getKeys() {
    return sContext.getInitParameterNames();
  }

  @Override
  public int size() {
    return size;
  }
}
