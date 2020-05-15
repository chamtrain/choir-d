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

import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceLogin {
  private User user;
  private Service service;
  private String ipAddress;
  private String rawUserAgent;
  private String javaVersion;
  private String javaVendor;
  private String osName;
  private String osVersion;
  private String osArch;

  public ServiceLogin(User user, Service service, String ipAddress, String rawUserAgent) {
    this.user = user;
    this.service = service;
    this.ipAddress = ipAddress;
    parseUserAgent(rawUserAgent);
  }

  public User getUser() {
    return user;
  }

  public Service getService() {
    return service;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getRawUserAgent() {
    return rawUserAgent;
  }

  public String getJavaVersion() {
    return javaVersion;
  }

  public String getJavaVendor() {
    return javaVendor;
  }

  public String getOsName() {
    return osName;
  }

  public String getOsVersion() {
    return osVersion;
  }

  public String getOsArch() {
    return osArch;
  }

  private void parseUserAgent(String agent) {
    if (agent == null) {
      return;
    }

    rawUserAgent = agent;

    List<String> java = parseVersionAndComments("Java", agent);
    if (!java.isEmpty()) {
      // Assume we received at least "Java/1.5.0_16" for example
      javaVersion = java.get(0);
    }
    if (java.size() == 5) {
      // Assume we received Java/${java.version} (${java.vendor};${os.name};${os.version};${os.arch})
      // For example "Java/1.5.0_16 (Sun Microsystems Inc.;Windows XP;5.1;x86)"
      javaVendor = java.get(1);
      osName = java.get(2);
      osVersion = java.get(3);
      osArch = java.get(4);
    }
  }

  /**
   * @param product the "product" portion of a user agent fragment (e.g. "Java" in "Java/1.5.0_16 (Sun Microsystems Inc.;Windows XP;5.1;x86)")
   * @param userAgent the entire user agent string
   * @return a possibly empty collection where the first element will be the version of the product and
   *         subsequent elements will be the ordered comments (if any)
   */
  private List<String> parseVersionAndComments(String product, String userAgent) {
    List<String> result = new ArrayList<>();

    Matcher m = Pattern.compile(".*" + product + "/([^ ]*)(?: \\(([^\\)]*)\\))?.*").matcher(userAgent);
    if (m.matches()) {
      result.add(m.group(1));
      String comments = m.group(2);
      if (comments != null) {
        for (String comment : comments.split(";")) {
          if (comment != null && comment.length() > 0) {
            result.add(comment);
          }
        }
      }
    }

    return result;
  }
}
