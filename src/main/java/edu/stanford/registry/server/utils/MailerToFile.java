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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the mailer interface that writes the emails to a local file.
 *
 * @author garricko
 */
public class MailerToFile implements Mailer {
  private static final String DEFAULT_MESSAGE = "---------- Mail would have been sent at ";
  private static final Logger log = LoggerFactory.getLogger(MailerToFile.class);
  private final String sender;
  private final File file;
  private final String newLine = System.getProperty("line.separator");
  private final String logMessage;

  public MailerToFile(String sender, File file) {
    this.sender = sender;
    this.file = file;
    this.logMessage = DEFAULT_MESSAGE;
  }

  public MailerToFile(String sender, File file, String logMessage) {
    this.sender = sender;
    this.file = file;
    this.logMessage = logMessage;
  }

  @Override
  public boolean sendText(String to, String cc, String bcc, String subject, String content) {
    return sendMail(to, cc, bcc, subject, content, false);
  }

  @Override
  public boolean sendHtml(String to, String cc, String bcc, String subject, String content) {
    return sendMail(to, cc, bcc, subject, content, true);
  }

  private boolean sendMail(String to, String cc, String bcc, String subject, String content, boolean isHtml) {
    FileOutputStream out = null;
    String email = null;
    try {
      StringBuilder builder = new StringBuilder();
      builder.append(newLine);
      builder.append(logMessage);
      builder.append(new Date());
      builder.append(newLine);
      builder.append(":From: ");
      if (sender != null && sender.length() > 0) {
        try {
          new InternetAddress(sender); // Test that the addresses are valid
        } catch (Exception ex) {
          throw new Exception("ERROR: Invalid 'From' address " + sender);
        }
      }
      builder.append(sender);
      builder.append(newLine);
      builder.append("To: ");
      try {
        InternetAddress.parse(to);
      } catch (Exception ex) {
        throw new Exception("ERROR: Invalid 'To' address " + to);
      }
      builder.append(to);
      builder.append(newLine);
      builder.append("Cc: ");
      if (cc != null && cc.length() > 0) {
        try {
          InternetAddress.parse(cc);
        } catch (Exception ex) {
          throw new Exception("ERROR: Invalid 'Cc' address " + cc);
        }
      }
      builder.append(cc);
      builder.append(newLine);
      builder.append("Bcc: ");
      if (bcc != null && bcc.length() > 0) {
        try {
          InternetAddress.parse(bcc);
        } catch (Exception ex) {
          throw new Exception("ERROR: Invalid 'Bcc' address " + bcc);
        }
      }
      builder.append(bcc);
      builder.append(newLine);
      builder.append("Subject: ");
      builder.append(subject);
      if (isHtml) {
        builder.append("Content html:\n");
      } else {
        builder.append("Content text:\n");
      }
      builder.append(content);
      builder.append(newLine);
      builder.append("----------");
      builder.append(newLine);
      email = builder.toString();
      out = new FileOutputStream(file, true);
      out.write(email.getBytes(StandardCharsets.UTF_8));
      return true;
    } catch (Exception e) {
      log.error("Error writing email to disk: {}", email, e);
      return false;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          log.error("Error closing the email file", e);
        }
      }
    }
  }

  @Override
  public boolean sendTextWithAttachment(String to, String cc, String bcc, String subject, String content, List<File> attachments) {
   FileOutputStream out = null;
    String email = null;
    try {
      StringBuilder builder = new StringBuilder();
      builder.append(newLine);
      builder.append(logMessage);
      builder.append(new Date());
      builder.append(newLine);
      builder.append(":From: ");
      if (sender != null && sender.length() > 0) {
        try {
          new InternetAddress(sender); // Test that the addresses are valid
        } catch (Exception ex) {
          throw new Exception("ERROR: Invalid 'From' address " + sender);
        }
      }
      builder.append(sender);
      builder.append(newLine);
      builder.append("To: ");
      try {
        InternetAddress.parse(to);
      } catch (Exception ex) {
        throw new Exception("ERROR: Invalid 'To' address " + to);
      }
      builder.append(to);
      builder.append(newLine);
      builder.append("Cc: ");
      if (cc != null && cc.length() > 0) {
        try {
          InternetAddress.parse(cc);
        } catch (Exception ex) {
          throw new Exception("ERROR: Invalid 'Cc' address " + cc);
        }
      }
      builder.append(cc);
      builder.append(newLine);
      builder.append("Bcc: ");
      if (bcc != null && bcc.length() > 0) {
        try {
          InternetAddress.parse(bcc);
        } catch (Exception ex) {
          throw new Exception("ERROR: Invalid 'Bcc' address " + bcc);
        }
      }
      builder.append(bcc);
      builder.append(newLine);
      builder.append("Subject: ");
      builder.append(subject);
      builder.append("Content:\n");
      builder.append(content);
      builder.append(newLine);
      if ((attachments != null) && (attachments.size() > 0)) {
        for(File attachment : attachments) {
          builder.append("Attachment: ").append(attachment.getName());
          builder.append(newLine);
        }
      }
      builder.append("----------");
      builder.append(newLine);
      email = builder.toString();
      out = new FileOutputStream(file, true);
      out.write(email.getBytes(StandardCharsets.UTF_8));
      return true;
    } catch (Exception e) {
      log.error("Error writing email to disk: {}", email, e);
      return false;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          log.error("Error closing the email file", e);
        }
      }
    }
  }
}
