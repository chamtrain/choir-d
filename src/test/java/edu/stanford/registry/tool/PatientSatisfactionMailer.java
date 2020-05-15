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
package edu.stanford.registry.tool;

import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.MailerReal;
import edu.stanford.registry.server.utils.MailerToFile;
import edu.stanford.survey.server.SessionKeyGenerator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.Flavor;

/**
 * Look for patients seen recently, and email STEPx patient satisfaction survey
 * invitations for them if they meet the requirements.
 *
 * @author garricko
 */
public class PatientSatisfactionMailer {
  private final static Logger log = LoggerFactory.getLogger(PatientSatisfactionMailer.class);

  private static boolean isEmpty(String...strings) {
    for (String s: strings) {
      if (s==null || s.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    try {
      FileUtils.forceMkdir(new File("build"));

      String smtpHost = System.getProperty("smtp.host"); // if not configured mail is only written to the file, not sent
      String url = System.getProperty("database.url");
      String user = System.getProperty("database.user");
      String pwd = System.getProperty("database.password");
      if (isEmpty(url, user, pwd))
        throw new RuntimeException(
            String.format("Missing properties: database.url (%s), .user (%s), or .password ",
                url, user));

      /*
       * Controls whether the runner is prompted to send to emails or not
       */
      boolean doSendEmail = false;
      String mode = System.getProperty("email.mode");
      if (!isEmpty(mode) && "production".equals(mode)) {
        doSendEmail = true;
        log.info("Production mode. Sending REAL EMAILS");
      }
      boolean fileOnly = false;
      if (isEmpty(smtpHost)) {
        log.info("smtp.host is not configured. Emails will be written to a file only, not sent.");
        fileOnly = true; // writes to file only
      }
      Builder registry = DatabaseProvider.fromDriverManager(url, user, pwd).withSqlInExceptionMessages().withSqlParameterLogging();

      // Figure out which patients within the registry are eligible for a patient satisfaction survey
      // ...filter by appointment date
      // ...try to exclude no-show, cancel, reschedule, etc.
      // ...exclude non-unique email addresses (>1 patient sharing email address)
      final List<Appointment> appts = new ArrayList<>();
      registry.transact(dbp -> {
        String sqlString = "select appt_reg_id, patient_id, (select data_value from patient_attribute "
            + "where survey_site_id=1 and patient_id=appt_registration.patient_id "
            + "and data_name='surveyEmailAddress') as email_addr, visit_dt, visit_type, provider_id, "
            + "encounter_eid from appt_registration where survey_site_id=1 and registration_type='a' "
            + "and clinic in ('PAIN MANAGEMENT','NEURO AT PAIN') ";
        if (dbp.get().flavor().equals(Flavor.oracle)) {
          sqlString = sqlString + "and visit_dt between trunc(sysdate-3) and trunc(sysdate) ";
        } else {
          sqlString = sqlString + "and visit_dt between (date_trunc('day',current_timestamp) - interval '3 day') "
              + "and date_trunc('day',current_timestamp) ";
        }
        sqlString = sqlString
            + "and patient_id in (select patient_id from patient_attribute where data_name='participatesInSurveys' "
            + "and data_value='y') and (appt_complete is null or appt_complete='Y')";
          dbp.get().toSelect(sqlString)
              .query(rs -> {
                while (rs.next()) {
                  Appointment appt = new Appointment();
                  appt.apptRegId = rs.getLongOrNull(1);
                  appt.patientId = rs.getStringOrNull(2);
                  appt.emailAddr = rs.getStringOrNull(3);
                  if (appt.emailAddr == null) {
                    continue;
                  }
                  appt.visitDt = rs.getDateOrNull(4);
                  appt.visitType = rs.getStringOrNull(5);
                  appt.providerId = rs.getLongOrNull(6);
                  appt.encounterEid = rs.getStringOrNull(7);
                  appts.add(appt);
                }
                return null;
              });
      });

      final Long[] batchId = { null };
      if (appts.size() > 0) {
        log.info("Found " + appts.size() + " appointments");

        // Generate a token and store each email address with the token
        // ...exclude opt-outs
        // ...exclude previously surveyed
        final int[] added = { 0 };
        registry.transact(dbp -> {
            SessionKeyGenerator keygen = new SessionKeyGenerator();
            for (Appointment appt : appts) {
              if (dbp.get().toSelect("select count(1) from patsat_token where email_addr=? or patient_id=?")
                  .argString(appt.emailAddr).argString(appt.patientId).queryLongOrZero() == 0) {
                if (batchId[0] == null) {
                  batchId[0] = dbp.get().toSelect("select COALESCE(max(patsat_batch_id),0)+1 from patsat_batch")
                      .queryLongOrNull();
                  if (batchId[0] == null) {
                    throw new Exception("Could not assign a batch id");
                  }
                  dbp.get().toInsert("insert into patsat_batch (patsat_batch_id,load_date) values (?,?)")
                      .argLong(batchId[0]).argDateNowPerDb()
                      .insert(1);
                }
                Calendar validToDate = Calendar.getInstance();
                validToDate.add(Calendar.DAY_OF_YEAR, 30);
                String token = keygen.createLength(16);
                dbp.get().toInsert("insert into patsat_token (survey_token,email_addr,token_valid_from,"
                    + "token_valid_thru,patsat_batch_id,patient_id,survey_reg_id,appt_date,visit_type,opted_out,"
                    + "encounter_eid,provider_id) values (?,?,?,:toDt,?,?,?,?,?,'N',?,?)")
                    .argString(token).argString(appt.emailAddr)
                    .argDateNowPerDb()
                    .argDate(":toDt", validToDate.getTime())
                    .argLong(batchId[0]).argString(appt.patientId).argLong(appt.apptRegId)
                    .argDate(appt.visitDt).argString(appt.visitType).argString(appt.encounterEid)
                    .argLong(appt.providerId).insert(1);
                added[0]++;
              }
            }
        });

        if (batchId[0] != null && added[0] > 0) {
          log.info("Created batch " + batchId[0] + " with " + added[0] + " survey tokens");
        }
      } else {
        log.info("No appointments found");
      }

      if (batchId[0] == null) {
        log.info("Checking for unfinished batch");

        final int[] unsent = { 0 };
        registry.transact(dbp -> {
          StringBuilder sqlString = new StringBuilder();
          sqlString.append("select count(distinct survey_token) from (select survey_token ")
              .append("from patsat_token where patsat_batch_id=? ");
          if (dbp.get().flavor().equals(Flavor.oracle)) {
            sqlString.append(" minus ");
          } else {
            sqlString.append(" EXCEPT ");
          }
          sqlString.append("select survey_token from patsat_email) tokens");
            batchId[0] = dbp.get().toSelect("select max(patsat_batch_id) from patsat_batch").queryLongOrNull();
            if (batchId[0] != null) {
              unsent[0] = dbp.get().toSelect(sqlString.toString())
                  .argLong(batchId[0]).queryIntegerOrNull();
            }
        });
        if (batchId[0] != null && unsent[0] > 0) {
          log.info("Found " + unsent[0] + " unsent emails for prior batch " + batchId[0]);
        } else {
          log.info("No unfinished batch, exiting");
          System.exit(1);
        }
      }

      final String fromAddress = "\"Patient Satisfaction\" <patientsatisfaction@pain.stanford.edu>";

      final Mailer mailer;
      if (!doSendEmail) {
        if (fileOnly) {
          System.out.print("We are about to write emails to FILE ONLY! Are you sure you want that (yes|no)? ");
        } else {
          System.out.print("We are about to send REAL EMAILS. Are you sure you want that (yes|no)? ");
        }
        byte[] input = new byte[80];
        int bytesRead = System.in.read(input);
        if (new String(input, 0, bytesRead - 1, StandardCharsets.US_ASCII).equals("yes")) {
          doSendEmail=true;
        }
      }
      if (doSendEmail) {
        if (fileOnly) {
          mailer = new MailerToFile(fromAddress, new File("build/email.log"));
        } else {
          mailer = new MailerReal(fromAddress, smtpHost, null, new File("build/email.log"));
        }
      } else {
        System.out.print("Exiting, nothing sent");
        System.exit(1);
        return;
      }

      // Read each of the survey tokens for the requested batch
      final List<SurveyToken> tokens = new ArrayList<>();
      registry.transact(dbp -> dbp.get().toSelect("select email_addr, survey_token from patsat_token where patsat_batch_id=?")
          .argLong(batchId[0]).query(rs -> {
            while (rs.next()) {
              SurveyToken appt = new SurveyToken();
              appt.emailAddr = rs.getStringOrNull();
              appt.surveyToken = rs.getStringOrNull();
              tokens.add(appt);
            }
            return null;
          }
      ));
      log.info("Found " + tokens.size() + " surveys in batch id " + batchId[0]);

      // Process each survey token, sending email if necessary for each
      // Emails are written to database, sent, then committed one at a time to
      // try to minimize failures and facilitate error recovery
      final int[] emailCount = { 0 };
      for (final SurveyToken token : tokens) {
        registry.transact(dbp -> {
            if (dbp.get().toSelect("select count(1) from patsat_email where survey_token=?")
                .argString(token.surveyToken).queryLongOrZero() > 0) {
              log.info("Mail already sent for token: " + token.surveyToken);
              return;
            }

            // Just a sanity check since we are inserting into HTML
            assert token.surveyToken.matches("[a-zA-Z0-9]*");

            String subject = "Patient Satisfaction Survey";
            String bodyHtml = "<html><body><p>At the Stanford Pain Management Center, we aim to consistently deliver "
                + "the highest level of expert and compassionate care. To continue providing our patients an "
                + "outstanding experience, we would greatly appreciate your input.</p><p>Please find below a link to "
                + "a short survey on your recent experience with us. We hope that you will take a few minutes to "
                + "complete the survey.</p>"
                + "<p><a href=\"https://svy.stanford.edu/survey2/?s=sat&tk=" + token.surveyToken
                + "\">https://svy.stanford.edu/</a></p>"
                + "<p>Thank you for your feedback.</p><p>Sincerely,</p><p>Sean Mackey, MD, PhD<br>"
                + "Chief, Division of Pain Medicine</p><p>If you wish to opt-out of similar patient satisfaction "
                + "emails from the Stanford Pain Management Center you may "
                + "<a href=\"https://svy.stanford.edu/survey2/?s=sat&tk=optout:" + token.surveyToken
                + "\">click here</a>.</p>"
                + "</body></html>";

            if (mailer instanceof MailerReal) {
              dbp.get().toInsert("insert into patsat_email (survey_token,send_sequence,send_time,"
                  + "from_addr,to_addr,subject,body_html) values "
                  + "(?,1,?,?,?,?,?)").argString(token.surveyToken).argDateNowPerDb().argString(fromAddress)
                  .argString(token.emailAddr).argString(subject).argString(bodyHtml).insert(1);
            } else {
              log.info("Skipped insert into patsat_email for token (not real mailer): " + token.surveyToken);
            }

            if (mailer.sendHtml(token.emailAddr, null, null, subject, bodyHtml)) {
              emailCount[0]++;
              log.info("Created email for token: " + token.surveyToken);
            } else {
              throw new RuntimeException("Error sending email for token: " + token.surveyToken);
            }
        });
      }
      log.info("Sent a total of " + emailCount[0] + " emails");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static class Appointment {
    Long apptRegId;
    String patientId;
    String emailAddr;
    Date visitDt;
    String visitType;
    Long providerId;
    String encounterEid;
  }

  static class SurveyToken {
    String surveyToken;
    String emailAddr;
  }
}
