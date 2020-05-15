package edu.stanford.registry.server.shc.orthohand;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.shared.ClientConfig;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 10/8/2015.
 */
public class OrthoHandCustomizer extends RegistryCustomizerDefault {

  private static final Logger logger = Logger.getLogger(OrthoHandCustomizer.class);

  public OrthoHandCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");
    String[] values = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute("orthoHandConsent", "Consented", values);
    Map<String, List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("ORTHO - HAND", Collections.singletonList("ORTHO - HAND"));
    return clientConfig;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    return new OrthoHandPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public List<File> getEmailAttachments(String template) {
    logger.debug("getEmailAttachments was called for template " + template);
    if (template != null && template.startsWith("Initial")) {
      logger.debug("getEmailAttachments is getting the ConsentForm");
      List<File> attachments = new ArrayList<>();
      URL url = getClass().getClassLoader().getResource("shc/orthohand/ConsentForm.pdf");
      if (url == null) {
        throw new RuntimeException("Did not find shc/orthohand/ConsentForm.pdf file");
      }
      attachments.add(new File(url.getFile()));
      logger.debug("getEmailAttachments is returning one attachment");
      return attachments;
    }
    return null;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    SurveyScheduler scheduler = new SurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }

  @Override
  public String IRBCountsConsentAttribute() {
    return "orthoHandConsent";
  }
}
