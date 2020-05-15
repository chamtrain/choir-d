package edu.stanford.registry.server.shc.preanesthesia;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.reports.PatientReport;
import edu.stanford.registry.shared.ClientConfig;
import edu.stanford.registry.shared.PatientRegistration;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 7/2/2015.
 */
public class PreAnesthesiaCustomizer extends RegistryCustomizerDefault {

  public PreAnesthesiaCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private static final Logger logger = Logger.getLogger(PreAnesthesiaCustomizer.class);
  @Override
  public ClientConfig getClientConfig() {
    ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");

    // Add this sites consent attribute
    String[] values = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute("pacFollowUp", "Follow ups", values);
    return clientConfig;
  }

  @Override
  public PatientReport getPatientReport(Database database, SiteInfo siteInfo) {
    logger.debug("returning PreAnesthesiaPatientReport");
    return new PreAnesthesiaPatientReport(database, siteInfo, ServerUtils.getAdminUser(database));
  }

  @Override
  public boolean registrationNotifiable(Database database,
                                        PatientRegistration registration, int lastSurveyDaysOut, Date throughDate) {
    return true;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    SurveyScheduler scheduler = new SurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }

  @Override
  public ArrayList<String> apiExportTables() {
    ArrayList<String> tables = new ArrayList<>();
    tables.add("rpt_pac_std_surveys_square");
    return tables;
  }

}
