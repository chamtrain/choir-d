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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

/**
 * Implementation of the mailer interface that uses JavaMail.
 *
 * @author garricko
 */
public class MailerReal implements Mailer {
  private static final Logger log = Logger.getLogger(MailerReal.class);
  private String sender;
  private String smtpHost;
  private Integer smtpPort;
  private MailerToFile mailerFile;

  /**
   * @param sender who the email is from, not null
   * @param smtpHost server to use for sending the email, not null
   * @param file a file to also write emails, null means do not write to file
   */
  public MailerReal(String sender, String smtpHost, Integer smtpPort, File file) {
    this.sender = sender;
    this.smtpHost = smtpHost;
    this.smtpPort = smtpPort;
    if (file != null) {
      this.mailerFile = new MailerToFile(sender, file, "---------- Mail has been sent ");
    }
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
    try {
      Properties props = new Properties();
      props.put("mail.smtp.host", smtpHost);
      props.put("mail.debug", "false");
      if (smtpPort != null) {
        props.put("mail.smtp.port", smtpPort);
      }
      Session session = Session.getInstance(props);
      session.setDebug(false);
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(sender));
      InternetAddress[] toAddress = InternetAddress.parse(to);
      message.setRecipients(Message.RecipientType.TO, toAddress);
      if (cc != null && cc.length() > 0) {
        InternetAddress[] ccAddress = InternetAddress.parse(cc);
        message.setRecipients(Message.RecipientType.CC, ccAddress);
      }
      if (bcc != null && bcc.length() > 0) {
        InternetAddress[] bccAddress = InternetAddress.parse(bcc);
        message.setRecipients(Message.RecipientType.BCC, bccAddress);
      }
      message.setSubject(subject);
      message.setSentDate(new Date());
      if (isHtml) {
        message.setContent(content, "text/html; charset=utf-8");
      } else {
        message.setText(content);
      }
      Transport.send(message);

      if (mailerFile != null) {
        mailerFile.sendText(to, cc, bcc, subject, content);
      }
      return true;
    } catch (Exception e) {
      StringBuilder builder = new StringBuilder();
      builder.append("From: ");
      builder.append(sender);
      builder.append("\nTo: ");
      builder.append(to);
      builder.append("\nCc: ");
      builder.append(cc);
      builder.append("\nBcc: ");
      builder.append(bcc);
      builder.append("\nSubject: ");
      builder.append(subject);
      builder.append("\nContent:\n");
      builder.append(content);
      log.error("Unable to send email: " + builder.toString(), e);
      return false;
    }
  }

  @Override
  public boolean sendTextWithAttachment(String to, String cc, String bcc, String subject,
      String content, List<File> attachments) {
    try {
      Properties props = new Properties();
      props.put("mail.smtp.host", smtpHost);
      props.put("mail.debug", "false");
      if (smtpPort != null) {
        props.put("mail.smtp.port", smtpPort);
      }
      Session session = Session.getInstance(props);
      session.setDebug(false);
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(sender));
      InternetAddress[] toAddress = InternetAddress.parse(to);
      message.setRecipients(Message.RecipientType.TO, toAddress);
      if (cc != null && cc.length() > 0) {
        InternetAddress[] ccAddress = InternetAddress.parse(cc);
        message.setRecipients(Message.RecipientType.CC, ccAddress);
      }
      if (bcc != null && bcc.length() > 0) {
        InternetAddress[] bccAddress = InternetAddress.parse(bcc);
        message.setRecipients(Message.RecipientType.BCC, bccAddress);
      }
      message.setSubject(subject);
      message.setSentDate(new Date());

      // Create a multi-part message
      Multipart mpart = new MimeMultipart();
      message.setContent(mpart);

      // Part one is the message text
      MimeBodyPart msgBody = new MimeBodyPart();
      msgBody.setText(content);
      mpart.addBodyPart(msgBody);

      // Part two is the attachment
      if ((attachments != null) && (attachments.size() > 0)) {
        for(File attachment : attachments) {
          MimeBodyPart msgAttachment = new MimeBodyPart();
          msgAttachment.attachFile(attachment);
          mpart.addBodyPart(msgAttachment);
        }
      }

      Transport.send(message);

      if (mailerFile != null) {
        mailerFile.sendTextWithAttachment(to, cc, bcc, subject, content, attachments);
      }

      return true;
    } catch (Exception e) {
      StringBuilder builder = new StringBuilder();
      builder.append("From: ");
      builder.append(sender);
      builder.append("\nTo: ");
      builder.append(to);
      builder.append("\nCc: ");
      builder.append(cc);
      builder.append("\nBcc: ");
      builder.append(bcc);
      builder.append("\nSubject: ");
      builder.append(subject);
      builder.append("\nContent:\n");
      builder.append(content);
      log.error("Unable to send email: " + builder.toString(), e);
      return false;
    }
  }
}
