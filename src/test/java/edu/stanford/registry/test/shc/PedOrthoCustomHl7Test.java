/*
 * Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.test.shc;

import edu.stanford.registry.server.RegistryCustomizer;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.imports.data.Hl7AppointmentIntf;
import edu.stanford.registry.server.shc.interventionalradiology.IRCustomizer;
import edu.stanford.registry.server.shc.pedortho.PedOrthoCustomizer;
import edu.stanford.registry.server.utils.ClassCreator;
import edu.stanford.registry.server.xchg.ImportException;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.test.DatabaseTestCase;

import java.util.HashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;

public class PedOrthoCustomHl7Test extends DatabaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(PedOrthoCustomHl7Test.class);

  private Supplier<Database> databaseProvider;
  private RegistryCustomizer customizer;
  private Hl7AppointmentIntf appointmentIntf;

  @Override
  protected void postSetUp() {
    databaseProvider = getDatabaseProvider();
    SiteInfo pedOrthoSiteInfo = new PedOrthoSiteInfo(getDatabaseProvider(), serverContext.getSitesInfo(), getSiteInfo());
    customizer = pedOrthoSiteInfo.getRegistryCustomizer();
    appointmentIntf = customizer.getHl7Customizer().getHl7Appointment(databaseProvider.get());
    logger.warn("postSetUp complete");
  }

  public void testHl7AddNoSurveyAppointment() throws Exception {
    Hl7AppointmentIntf appointmentIntf = customizer.getHl7Customizer().getHl7Appointment(databaseProvider.get());

    Patient patient = appointmentIntf.processPatient("8888888-8", "First", "Last", "19840111");
    assertNotNull("Expected a patient object", patient);

    ApptRegistration apptRegistration = processAppointment(patient, "044310");
    assertNotNull("Expected an appointment registration", apptRegistration);
    assertEquals(apptRegistration.getRegistrationType(), Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT);
    assertEquals("Expected type to be nosurvey ", apptRegistration.getSurveyType(), PedOrthoCustomizer.SURVEY_NOSURVEY);
  }

  private ApptRegistration processAppointment(Patient patient, String providerEid) throws ImportException {
    assertEquals(appointmentIntf.getClass().getName(), "edu.stanford.registry.server.shc.pedortho.PedOrthoHl7Customizer$PedOrthoHl7Appointment");
    return appointmentIntf.processAppointment(patient, "20200122090000", "100", "PROC60",
        1, "1234567890", providerEid, "ORTHO & SPORTS MED EMERYVILLE 6121 HOLLIS");

  }
}

class PedOrthoSiteInfo extends SiteInfo {
  private static final Logger mylogger = LoggerFactory.getLogger(PedOrthoSiteInfo.class);

  public PedOrthoSiteInfo(DatabaseProvider databaseProvider, SitesInfo sitesInfo, SiteInfo siteInfo) {
    super(siteInfo.getSiteId(), siteInfo.getUrlParam(), siteInfo.getDisplayName(), true);
    initSiteConfig(databaseProvider, sitesInfo, sitesInfo.getGlobalPropertyMap().getMap(), siteInfo.getProperties(),
        siteInfo.getEmailTemplates(), new HashMap<>());

  }

  public RegistryCustomizer getRegistryCustomizer() {
    ClassCreator<IRCustomizer> customizerCreator =
        new ClassCreator<IRCustomizer>("RegistryCustomizerFactory.create", "PedOrthoCustomizer", mylogger, SiteInfo.class)
            .check("edu.stanford.registry.server.shc.pedortho.PedOrthoCustomizer")
            .check("edu.stanford.registry.server.shc.pedortho.PedOrthoCustomizer");
    return customizerCreator.createClass("edu.stanford.registry.server.shc.pedortho.PedOrthoCustomizer", this);
  }

}
