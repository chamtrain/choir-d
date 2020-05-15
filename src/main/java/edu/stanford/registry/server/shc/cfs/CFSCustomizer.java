package edu.stanford.registry.server.shc.cfs;

import edu.stanford.registry.server.RegistryCustomizerDefault;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.ClientConfig;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Chronic Fatigue Syndrome Customizer
 *
 * Created by scweber on 01/06/2016.
 */
public class CFSCustomizer extends RegistryCustomizerDefault {

  public CFSCustomizer(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(RegistryCustomizerDefault.class);

  public static final String ATTR_CFS_CONSENT = "cfsConsent";
  public static final String ATTR_BASE_DATE = "cfsBaseDate";
  public static final String SURVEY_INITIAL = "CFS-NewPatient";
  public static final String SURVEY_FOLLOW_UP = "CFS-Consented";

  @Override
  public ClientConfig getClientConfig() {
       ClientConfig clientConfig = super.getClientConfig();
    // Turn on the option for the new PatientIdentification - version 1
    clientConfig.getParams().put("patientIdentificationViewVs", "1");
    String[] values = { "", "Y", "N" };
    clientConfig.addCustomPatientAttribute(ATTR_CFS_CONSENT, "Consented", values);
    Map<String, List<String>> clinicFilterMapping = new LinkedHashMap<>();
    clinicFilterMapping.put("CHRONIC FATIGUE CLINIC", Collections.singletonList("CHRONIC FATIGUE CLINIC ATHERTON"));
    clientConfig.setClinicFilterMapping(clinicFilterMapping);
    return clientConfig;
  }

  @Override
  public void scheduleSurveys(Database database, Date endDate, Long siteId) {
    CFSSurveyScheduler scheduler = new CFSSurveyScheduler(database, siteInfo);
    scheduler.scheduleSurveys(endDate);
  }
}

