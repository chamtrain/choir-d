package edu.stanford.registry.server.shc.ccte;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

import edu.stanford.registry.server.shc.orthohand.OrthoHandPatientReport;
import edu.stanford.registry.server.shc.orthohand.SurveyScheduler;
import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.shared.ClientConfig;

/**
 * Created by scweber on 02/16/2016.
 */
public class CCTECustomizer extends RegistryCustomizerDefault {
  public CCTECustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private static final Logger logger = Logger.getLogger(CCTECustomizer.class);

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");
    // String[] values = { "", "Y", "N" };
    return clientConfig;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new OrthoHandPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public List<File> getEmailAttachments(String template) {
    logger.debug("getEmailAttachments was called for template " + template);
    // no email attachments for CCTE
    return null;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    SurveyScheduler scheduler = new SurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }
}

