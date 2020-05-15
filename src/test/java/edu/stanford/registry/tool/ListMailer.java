package edu.stanford.registry.tool;

import edu.stanford.registry.server.ServerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class ListMailer {
  private static Logger logger = Logger.getLogger(ListMailer.class);
  final String emailModeProduction = "production";
  // properties
  final String emailFileKey = "list.email.output.file";
  final String emailFileDefault = "list.email.log";
  final String emailModePropKey = "list.email.mode";

  final String emailFromKey = "list.email.from";
  final String emailSubjectLine = "list.email.subject";
  final String emailContentFileName = "list.email.content.file";
  final String emailSendList = "list.email.recipients.file";
  //If this property is not set or does not match the current host's
  // hostname, email will be written to file
  final String productionHostPropKey = "list.email.production.host";
  final String emailServerKey = "list.email.server";

  private String smtpHost = null;
  private String emailFrom = null;

  private File emailFile = null;
  private String newLine = System.getProperty("line.separator");
  private String emailSubject = null;
  private String emailContents = null;
  private ArrayList<String> sendToList = null;
  private String attachmentFileName = null;
  private String attachmentShortName = null;
  boolean sendForReal = true;
  private int numberEmails = 0;

  public static void main(String[] args) {
    new ListMailer(args);
  }

  public ListMailer(String args[]) {
    String propertiesFile = System.getProperty("ListMailer.properties");
    if (args != null && args.length > 0) {
      propertiesFile = args[0];
    }
    if (propertiesFile == null) {
      System.out
          .println("Properties file missing! define either system property 'ListMailer.properties' or provide as an argument when running.");
    } else {

      ArrayList<String> errors = init(propertiesFile);
      if (errors.size() > 0) {
        System.out.println("Exiting!");
        for (String errorMsg : errors) {
          System.out.println(errorMsg);
        }
      } else {
        sendMail();
        System.out.println(getNumberEmails() + " email addresses were processed");
      }
    }
  }

  public ListMailer(Properties properties) {
    ArrayList<String> errors = initProperties(properties);
    if (errors.size() > 0) {
      for (String errorMsg : errors) {
        logger.error(errorMsg);
      }
      throw new ServerException("See log file for details");
    }
    sendMail();
  }

  private ArrayList<String> init(String propertiesFile) {
    String log4jConfig = System.getProperty("log4j.configuration");

    if (log4jConfig != null) {
      try {
        log4jConfig = new File(log4jConfig).getAbsolutePath();
        DOMConfigurator.configure(log4jConfig);
      } catch (Exception e) {
        System.err.println("Unable to configure log4j using file: " + log4jConfig);
        e.printStackTrace();
      }
    }
    logger = Logger.getLogger(ListMailer.class);
    logger.info("log4j has been configured with " + log4jConfig);
    // Load configuration file if provided
    Properties buildProperties = new Properties();

    try {
      if (propertiesFile != null) {
        FileInputStream is = new FileInputStream(propertiesFile);
        buildProperties.load(is);
        is.close();
        logger.info("Read properties from " + propertiesFile);
      }
    } catch (Exception e) {
      logger.warn("Unable to read properties from file " + propertiesFile, e);
    }
    return initProperties(buildProperties);
  }

  private ArrayList<String> initProperties(Properties buildProperties) {
    // Read configuration properties
    String productionHost = System.getProperty(productionHostPropKey,
        buildProperties.getProperty(productionHostPropKey, ""));
    String hostname = null;
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      logger.warn("Disabled email because hostname could not be determined", e);
      sendForReal = false;
    }

    String emailMode = System.getProperty(emailModePropKey,
        buildProperties.getProperty(emailModePropKey, "development"));
    if (!emailModeProduction.equals(emailMode)) {
      logger.info("Disabled email because property " + emailModePropKey + " is not \"" + emailModeProduction + "\"");
      sendForReal = false;
    }

    if (productionHost == null) {
      logger.info("Disabled email because property " + productionHostPropKey + " is not set");
      sendForReal = false;
    }

    if (hostname != null && productionHost != null && !hostname.equals(productionHost)) {
      logger.info("Disabled email because property " + productionHostPropKey + " is not \"" + hostname + "\"");
      sendForReal = false;
    }

    // SMTP server to which we should send mail

    String emailServer = System.getProperty(emailServerKey, buildProperties.getProperty(emailServerKey));
    if (emailServer == null) {
      logger.info("Disabled email because property " + emailServerKey + " is not set");
      sendForReal = false;
    }

    String emailFileName = System.getProperty(emailFileKey, buildProperties.getProperty(emailFileKey, ""));
    if (emailFileName == null) {
      logger.info("Property " + emailFileKey + " was not set. Defaulting to \"" + emailFileDefault + "\"");
      emailFileName = emailFileDefault;
    }
    emailFile = new File(emailFileName);

    /* Find the email address that we'll use as FROM on the message */
    emailFrom = System.getProperty(emailFromKey, buildProperties.getProperty(emailFromKey));
    if (emailFrom == null) {
      logger.info("Disabled email because property " + emailFromKey + " is not set");
      sendForReal = false;
    }
    if (sendForReal) {
      logger.info("Email will be sent via SMTP server " + emailServer + " and written to file: "
          + emailFile.getAbsolutePath());
      smtpHost = emailServer;

    } else {
      logger.info("Email will be only be written to file: " + emailFile.getAbsolutePath());
    }

    /* Find the subject of the email */
    emailSubject = buildProperties.getProperty(emailSubjectLine, buildProperties.getProperty(emailSubjectLine));

    /* Build the list of email addresses to send the emails to from an input file */
    String fileWithEmails = buildProperties.getProperty(emailSendList, buildProperties.getProperty(emailSendList));
    if (fileWithEmails != null) {
      File emailToFile = new File(fileWithEmails);
      BufferedReader response = null;
      if (emailToFile.exists()) {
        try {
          FileInputStream fis = new FileInputStream(emailToFile);
          sendToList = new ArrayList<>();
          response = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
          String line;

          while ((line = response.readLine()) != null) {
            sendToList.add(line);
          }
        } catch (IOException e) {
          logger.error("Error reading from file " + emailSendList);
        } finally {
          if (response != null) {
            try {
              response.close();
            } catch (IOException e) {
              logger.error("Error closing file reader for " + emailSendList, e);
            }
          }
        }
      }
    }
    /* Get the contents */
    String contentFileName = System.getProperty(emailContentFileName,
        buildProperties.getProperty(emailContentFileName, ""));
    try {
      emailContents = FileUtils.readFileToString(new File(contentFileName));
    } catch (IOException e) {
      logger.error("Failed to read the email contents from: " + contentFileName + " check the value of property: "
          + emailContentFileName + " is a readable file");
    }

    /* check for an attachment */
    final String emailAttachment = "list.email.attachment";
    attachmentFileName = System.getProperty(emailAttachment, buildProperties.getProperty(emailAttachment));
    if (attachmentFileName != null && attachmentFileName.trim().length() < 1) {
      attachmentFileName = null;
    }

    ArrayList<String> errors = new ArrayList<>();
    if (emailFrom == null) {
      errors.add("A valid FROM email address must be defined in the property :" + emailFromKey);
    }
    if (emailSubject == null || emailSubject.trim().length() < 1) {
      errors.add("A non-empty subject line must be defined in the property :" + emailSubjectLine);
    }
    if (emailContents == null) {
      errors.add("A valid filename must be defined for the content of the email in the property "
          + emailContentFileName);
    }
    if (sendToList == null || sendToList.size() < 1) {
      errors.add("No one to send to! A valid filename with the email recipients must be defined for property " + emailSendList);
    }
    if (attachmentFileName != null) {
      File aFile = new File(attachmentFileName);
      if (!aFile.exists()) {
        errors.add("Attachment file: " + aFile.getAbsolutePath() + " does not exist.");
      } else if (!aFile.canRead()) {
        errors.add("Cannot read attachment file: " + aFile.getAbsolutePath());
      } else {
        attachmentShortName = aFile.getName();
      }
    }
    return errors;
  }

  private void sendMail() {
    if (sendForReal) {
      if (attachmentFileName == null) {
        sendEmailWithoutAttachment();
      } else {
        sendEmailWithAttachment();
      }
    } else {
      sendEmailToFileOnly();
    }
  }

  private void sendEmailToFileOnly() {
    if (sendToList == null) {
      return;
    }
    for (String emailTo : sendToList) {

      StringBuilder builder = new StringBuilder(emailTo);
      builder.append(newLine);
      builder.append("---------- Mail would have been sent at ");
      builder.append(new Date());
      builder.append(newLine);
      try {
        addMessageAsString(builder, emailTo);
        writeEmailToFile(builder);
      } catch (Exception e) {
        logger.error("Problem generating email for " + emailTo, e);
      }
      numberEmails++;
    }
  }

  private void sendEmailWithoutAttachment() {
    if (sendToList == null) {
      return;
    }
    for (String emailTo : sendToList) {
      StringBuilder builder = new StringBuilder();
      try {
        addMessageAsString(builder, emailTo);
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        builder.append(e.getMessage());
      }
      logger.info("Sending email without an attachment to " + emailTo);

      Session session = getSession();
      session.setDebug(false);
      try {
        Message message = new CustomMimeMessage(session);
        message.setFrom(new InternetAddress(emailFrom));
        InternetAddress[] toAddress = InternetAddress.parse(emailTo);
        message.setRecipients(Message.RecipientType.TO, toAddress);
        message.setSubject(emailSubject);
        message.setSentDate(new Date());
        message.setContent(emailContents, "text/html");
        Transport.send(message);
      } catch (MessagingException me) {
        logger.error(me.getMessage(), me);
        logger.error("Unable to send email: " + builder.toString());
      }
      writeEmailToFile(builder);
      numberEmails++;
    }
  }

  private void sendEmailWithAttachment() {
    for (String emailTo : sendToList) {
      StringBuilder builder = new StringBuilder();
      try {
        addMessageAsString(builder, emailTo);
        sendEmailWithAttachment(emailTo);
      } catch (Exception e) {
        builder.append(e.getMessage());
      }
      writeEmailToFile(builder);
    }
  }

  private void sendEmailWithAttachment(String emailTo) throws AddressException, MessagingException {
    logger.info("Sending email with an attachment to " + emailTo);
    // Define message and headers
    Session session = getSession();
    session.setDebug(false);
    MimeMessage message = new CustomMimeMessage(session);
    message.setFrom(new InternetAddress(emailFrom));
    InternetAddress[] toAddress = InternetAddress.parse(emailTo);
    message.setRecipients(Message.RecipientType.TO, toAddress);
    message.setSubject(emailSubject);
    message.setSentDate(new Date());

    // Create the message part
    BodyPart messageBodyPart = new MimeBodyPart();

    // Fill the message
    //messageBodyPart.setText(emailContents);
    messageBodyPart.setContent(emailContents, "text/html");

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(messageBodyPart);

    // add the attachment

    messageBodyPart = new MimeBodyPart();
    if (attachmentFileName != null) {
      DataSource source = new FileDataSource(attachmentFileName);
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName(attachmentShortName);
      multipart.addBodyPart(messageBodyPart);
    }

    // Put parts in message
    message.setContent(multipart);

    // Send the message
    Transport.send(message);

  }

  private boolean writeEmailToFile(StringBuilder builder) {
    FileOutputStream out = null;
    String email = null;
    try {

      email = builder.toString();
      out = new FileOutputStream(emailFile, true);
      out.write(email.getBytes(StandardCharsets.UTF_8));
      return true;
    } catch (Exception e) {
      logger.error("Error writing email to disk: " + email, e);
      return false;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          logger.error("Error closing the email file", e);
        }
      }
    }
  }

  private Session getSession() {
    Properties props = new Properties();
    props.put("mail.smtp.host", smtpHost);
    props.put("mail.debug", "false");
    Session session = Session.getInstance(props);
    return session;
  }

  private StringBuilder addMessageAsString(StringBuilder builder, String emailTo) throws Exception {

    builder.append("From: ");
    if (emailFrom != null && emailFrom.length() > 0) {
      try {
        new InternetAddress(emailFrom); // Test that the addresses are valid
      } catch (Exception ex) {
        throw new Exception("ERROR: Invalid 'From' address " + emailFrom);
      }
    }
    builder.append(emailFrom);
    builder.append(newLine);
    builder.append("To: ");
    try {
      InternetAddress.parse(emailTo);
    } catch (Exception ex) {
      throw new Exception("ERROR: Invalid 'To' address " + emailTo);
    }
    builder.append(emailTo);
    builder.append(newLine);
    builder.append("Cc: ");
    builder.append(newLine);
    builder.append("Bcc: ");
    builder.append(newLine);
    builder.append("Subject: ");
    builder.append(emailSubject);
    builder.append(newLine);
    builder.append("Content:");
    builder.append(emailContents);
    builder.append(newLine);
    builder.append("----------");
    builder.append(newLine);
    return builder;
  }

  class CustomMimeMessage extends MimeMessage {
    private int id = 0;

    protected CustomMimeMessage(Folder folder, InputStream is, int msgnum) throws MessagingException {
      super(folder, is, msgnum);
    }

    public CustomMimeMessage(Session session) {
      super(session);
    }

    @Override
    protected void updateMessageID() throws MessagingException {
        setHeader("Message-ID", getGeneralizedIdentityMessage());
    }

    private String getGeneralizedIdentityMessage() throws MessagingException {

      String[] msgIdHeader = getHeader("Message-ID");
      if (msgIdHeader != null) {
        for (String str : msgIdHeader) {
          if (str.indexOf("JavaMail") > 1) {
            return str.substring(0, str.indexOf("JavaMail") - 1) + "JavaMail." + emailFrom + ">";
          }
        }
      }
      // Unique string is <hashcode.id.currentTime.JavaMail.emailAddr>
      StringBuilder s = new StringBuilder();
      s.append("<");
      s.append(s.hashCode()).append('.').append(getUniqueId()).append('.').
        append(System.currentTimeMillis()).append('.').
        append("JavaMail.").
        append(emailFrom);
      s.append(">");
      return s.toString();
    }

    private synchronized int getUniqueId() {
      return id++;
    }

  }

  public int getNumberEmails() {
    return numberEmails;
  }
}
