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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.SiteInfo;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple factory for creating and configuring a Mailer implementation based on properties in a ServletContext
 *
 * @author garricko
 */
public class MailerFactory {
  private static final Logger log = LoggerFactory.getLogger(MailerFactory.class);
  private static final String hostname = initLocalHostName();

  private static String initLocalHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.warn("Hostname could not be determined", e);
      return null;
    }
  }

  public Mailer create(SiteInfo siteInfo) {
    // Email mode is "production" to send real emails; missing or anything
    // else to write them to file
    final String emailModePropKey = "registry.email.mode";
    final String emailModeProduction = "production";
    // If this property is not set or does not match the current host's
    // hostname, email will be written to file
    final String productionHostPropKey = "registry.email.production.host";
    // SMTP server to which we should send mail
    String emailServerKey = "registry.email.server";
    // SMPT server port (defaults to 25)
    String emailPortKey = "registry.email.port";
    // The apparent sender of the email
    String emailFromKey = "registry.email.from";
    // The file to dump emails into when not in production mode
    String emailFileKey = "registry.email.file";
    String emailFileDefault = "{site}.email.log";

    // Read configuration properties
    String emailMode = siteInfo.getProperty(emailModePropKey);
    String productionHost = siteInfo.getGlobalProperty(productionHostPropKey);
    String emailServer = siteInfo.getProperty(emailServerKey);
    String emailPort = siteInfo.getProperty(emailPortKey);
    String emailFrom = siteInfo.getProperty(emailFromKey);
    String emailFile = siteInfo.getPathProperty(emailFileKey, null);
    Integer emailPortInt = null;

    boolean sendForReal = true;
    StringBuilder reasons = new StringBuilder();

    if (hostname == null && productionHost != null && !productionHost.equals("*")) {
      reasons.append("\n    Hostname could not be determined");
      sendForReal = false;
    }

    if (!emailModeProduction.equals(emailMode)) {
      reasons.append("\n    Property ").append(emailModePropKey).append(" is not \"").append(emailModeProduction)
          .append("\" (value is \"").append(emailMode).append("\")");
      sendForReal = false;
    }

    if (productionHost == null) {
      reasons.append("\n    Property ").append(productionHostPropKey).append(" is not set");
      sendForReal = false;
    }

    if (hostname != null && productionHost != null && !productionHost.equals("*") && !hostname.equals(productionHost)) {
      reasons.append("\n    Property ").append(productionHostPropKey).append(" is set to \"").append(productionHost)
          .append("\"").append(" but the hostname is \"").append(hostname).append("\"");
      sendForReal = false;
    }

    if (emailServer == null) {
      reasons.append("\n    Property ").append(emailServerKey).append(" is not set");
      sendForReal = false;
    }

    if ((emailPort != null) && !emailPort.equals("")) {
      if (emailPort.matches("[0-9]+")) {
        emailPortInt = Integer.valueOf(emailPort);
      } else {
        log.debug("Property " + emailPortKey + " for site '" + siteInfo.getUrlParam() + "' is not a valid port number '" + emailPort + "'");
        sendForReal = false;
      }
    }

    if (emailFrom == null) {
      reasons.append("\n    Property ").append(emailFromKey).append(" is not set");
      sendForReal = false;
    }

    if (emailFile == null) {
      log.debug("Property " + emailFileKey + " for site '" + siteInfo.getUrlParam() + "' was not set. Defaulting to \"" + emailFileDefault + "\"");
      emailFile = siteInfo.getPathProperty(emailFileKey, emailFileDefault);
    }

    File file = new File(emailFile);

    if (sendForReal) {
      log.info("Email for site '{}' will be sent via SMTP server {} and written to file: {}",
          siteInfo.getUrlParam(), emailServer, file.getAbsolutePath());
      return new MailerReal(emailFrom, emailServer, emailPortInt, file);
    } else {
      log.info("Email for site '{}' will be written to file instead of sent: {}\nReasons:{}",
          siteInfo.getUrlParam(), file.getAbsolutePath(), reasons);
      return new MailerToFile(emailFrom, file);
    }
  }
}
