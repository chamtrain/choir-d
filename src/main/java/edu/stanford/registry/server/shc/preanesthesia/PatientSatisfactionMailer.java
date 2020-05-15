package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.MailerReal;
import edu.stanford.survey.server.SessionKeyGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.DbRun;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;

/**
 * Look for patients seen recently, and email STEPx patient satisfaction survey
 * invitations for them if they meet the requirements.
 *
 * @author garricko
 */
public class PatientSatisfactionMailer {
  private final static Logger log = LoggerFactory.getLogger(PatientSatisfactionMailer.class);

  public static void main(String[] args) {
    try {
      FileUtils.forceMkdir(new File("build"));

      String smtpHost = System.getProperty("smtp.host");
      Builder registry = DatabaseProvider.fromDriverManager(
          System.getProperty("database.url"),
          System.getProperty("database.user"),
          System.getProperty("database.password")
      ).withSqlInExceptionMessages().withSqlParameterLogging();

      // Figure out which patients within the registry are eligible for a patient satisfaction survey
      // ...filter by appointment date
      // ...try to exclude no-show, cancel, reschedule, etc.
      // ...exclude non-unique email addresses (>1 patient sharing email address)
      final List<Appointment> appts = new ArrayList<>();
      registry.transact(new DbRun() {
        @Override
        public void run(Provider<Database> dbp) throws Exception {
          dbp.get().toSelect("select appt_reg_id, patient_id, (select data_value from patient_attribute "
              + "where survey_site_id=9 and patient_id=appt_registration.patient_id "
              + "and data_name='surveyEmailAddress') as email_addr, visit_dt, visit_type, provider_id, "
              + "encounter_eid from appt_registration where survey_site_id=9 and registration_type='a' "
              + "and visit_dt between trunc(sysdate-70) and trunc(sysdate) "
              + "and patient_id in (select patient_id from patient_attribute where data_name='participatesInSurveys' "
              + "and data_value='y') and (appt_complete is null or appt_complete='Y')")
              .query(new RowsHandler<Object>() {
                @Override
                public Object process(Rows rs) throws Exception {
                  while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.apptRegId = rs.getLongOrNull(1);
                    appt.patientId = rs.getStringOrNull(2);
                    appt.emailAddr = "tpacht@stanford.edu"; // rs.getStringOrNull(3);
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
                }
              });
        }
      });

      final Long[] batchId = { null };
      if (appts.size() > 0) {
        log.info("Found " + appts.size() + " appointments");

        // Generate a token and store each email address with the token
        // ...exclude opt-outs
        // ...exclude previously surveyed
        final int[] added = { 0 };
        registry.transact(new DbRun() {
          @Override
          public void run(Provider<Database> dbp) throws Exception {
            SessionKeyGenerator keygen = new SessionKeyGenerator();
            for (Appointment appt : appts) {
              if (dbp.get().toSelect("select count(1) from patsat_token where email_addr=? or patient_id=?")
                  .argString(appt.emailAddr).argString(appt.patientId).queryLongOrZero() == 0) {
                if (batchId[0] == null) {
                  batchId[0] = dbp.get().toSelect("select nvl(max(patsat_batch_id),0)+1 from patsat_batch")
                      .queryLongOrNull();
                  if (batchId[0] == null) {
                    throw new Exception("Could not assign a batch id");
                  }
                  dbp.get().toInsert("insert into patsat_batch (patsat_batch_id,load_date) values (?,sysdate)")
                      .argLong(batchId[0]).insert(1);
                }

                String token = keygen.createLength(16);
                dbp.get().toInsert("insert into patsat_token (survey_token,email_addr,token_valid_from,"
                    + "token_valid_thru,patsat_batch_id,patient_id,survey_reg_id,appt_date,visit_type,opted_out,"
                    + "encounter_eid,provider_id) values (?,?,sysdate,sysdate+30,?,?,?,?,?,'N',?,?)").argString(token)
                    .argString(appt.emailAddr).argLong(batchId[0]).argString(appt.patientId).argLong(appt.apptRegId)
                    .argDate(appt.visitDt).argString(appt.visitType).argString(appt.encounterEid)
                    .argLong(appt.providerId).insert(1);
                added[0]++;
              }
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
        registry.transact(new DbRun() {
          @Override
          public void run(Provider<Database> dbp) throws Exception {
            batchId[0] = dbp.get().toSelect("select max(patsat_batch_id) from patsat_batch").queryLongOrNull();
            if (batchId[0] != null) {
              unsent[0] = dbp.get().toSelect("select count(distinct survey_token) from (select survey_token "
                  + "from patsat_token where patsat_batch_id=? minus select survey_token from patsat_email)")
                  .argLong(batchId[0]).queryIntegerOrNull();
            }
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
      System.out.print("We are about to send REAL EMAILS. Are you sure you want that (yes|no)? ");
      byte[] input = new byte[80];
      int bytesRead = System.in.read(input);
      if (new String(input, 0, bytesRead-1).equals("yes")) {
        mailer = new MailerReal(fromAddress, smtpHost, null, new File("build/pac-email.log"));
      } else {
        System.out.print("Exiting, nothing sent");
        System.exit(1);
        return;
      }

      // Read each of the survey tokens for the requested batch
      final List<SurveyToken> tokens = new ArrayList<>();
      registry.transact(new DbRun() {
        @Override
        public void run(Provider<Database> dbp) throws Exception {
          dbp.get().toSelect("select email_addr, survey_token from patsat_token where patsat_batch_id=?")
              .argLong(batchId[0]).query(new RowsHandler<Object>() {
                @Override
                public Object process(Rows rs) throws Exception {
                  while (rs.next()) {
                    SurveyToken appt = new SurveyToken();
                    appt.emailAddr = rs.getStringOrNull();
                    appt.surveyToken = rs.getStringOrNull();
                    tokens.add(appt);
                  }
                  return null;
                }
              }
          );
        }
      });
      log.info("Found " + tokens.size() + " surveys in batch id " + batchId[0]);

      // Process each survey token, sending email if necessary for each
      // Emails are written to database, sent, then committed one at a time to
      // try to minimize failures and facilitate error recovery
      final int[] emailCount = { 0 };
      for (final SurveyToken token : tokens) {
        registry.transact(new DbRun() {
          @Override
          public void run(Provider<Database> dbp) throws Exception {
            if (dbp.get().toSelect("select count(1) from patsat_email where survey_token=?")
                .argString(token.surveyToken).queryLongOrZero() > 0) {
              log.info("Mail already sent for token: " + token.surveyToken);
              return;
            }

            // Just a sanity check since we are inserting into HTML
            assert token.surveyToken.matches("[a-zA-Z0-9]*");

            String subject = "Patient Satisfaction Survey";
            String bodyHtml = "<html><body><p>At the Stanford Pre-anesthesia Clinic, we aim to consistently deliver "
                + "the highest level of expert and compassionate care. To continue providing our patients an "
                + "outstanding experience, we would greatly appreciate your input.</p><p>Please click the choice below "
                + " that best describes "
                + "how satisfied were you with your anesthesia services "
                + "<p></p>\n"
                + "<table cellpadding=\"4\" >\n"
                + "  <tr>"
                + "    <td bgcolor=\"purple\" valign=\"middle\" align=\"center\" width=\"100\"><div style=\"font-size: 18px; color: #ffffff; line-height: 1; margin: 0; padding: 10;\">"
                + "<a href=\"https://localhost:8443/registry/?s=pacsat&amp;tk=response:5:"+ token.surveyToken+"\" style=\"text-decoration: none; color: #ffffff; border: 0;\">Very Good"
                + "      </a></div>"
                + "    </td>"
                + "    <td bgcolor=\"blue\" valign=\"middle\" align=\"center\" width=\"100\" ><div style=\"font-size: 18px; color: #ffffff; line-height: 1; margin: 0; padding: 10;\">"
                + "<a href=\"https://localhost:8443/registry/?s=pacsat&amp;tk=response:4:"+ token.surveyToken+"\" style=\"text-decoration: none; color: #ffffff; border: 0;\">Good"
                + "      </a></div>"
                + "    </td>"
                + "    <td bgcolor=\"green\" valign=\"middle\" align=\"center\" width=\"100\" ><div style=\"font-size: 18px; color: #ffffff; line-height: 1; margin: 0; padding: 10;\">"
                + "<a href=\"https://localhost:8443/registry/?s=pacsat&amp;tk=response:3:"+ token.surveyToken+"\" style=\"text-decoration: none; color: #ffffff; border: 0;\">Average"
                + "      </a></div>"
                + "    </td>"
                + "     <td bgcolor=\"orange\" valign=\"middle\" align=\"center\" width=\"100\" ><div style=\"font-size: 18px; color: #ffffff; line-height: 1; margin: 0; padding: 10;\">"
                + "<a href=\"https://localhost:8443/registry/?s=pacsat&amp;tk=response:2:"+ token.surveyToken+"\" style=\"text-decoration: none; color: #ffffff; border: 0;\">Poor</a></div>\n"
                + "      </td>"
                + "     <td bgcolor=\"red\" valign=\"middle\" align=\"center\" width=\"100\" ><div style=\"font-size: 18px; color: #ffffff; line-height: 1; margin: 0; padding: 10;\">\n"
                + "<a href=\"https://localhost:8443/registry/?s=pacsat&amp;tk=response:1:"+ token.surveyToken+"\" style=\"text-decoration: none; color: #ffffff; border: 0;\">Very Poor</a></div>\n"
                + "      </td>"
                + "  </tr>"
                + "</table>"
                + "</body></html>";

            if (mailer instanceof MailerReal) {
              dbp.get().toInsert("insert into patsat_email (survey_token,send_sequence,send_time,"
                  + "from_addr,to_addr,subject,body_html) values "
                  + "(?,1,sysdate,?,?,?,?)").argString(token.surveyToken).argString(fromAddress)
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
