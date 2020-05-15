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

package edu.stanford.registry.server.service;

import edu.stanford.registry.shared.api.ClientService;
import edu.stanford.survey.client.api.SurveyService;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public enum Service {

  ADMIN_SERVICES(AdministrativeServices.class, "/admin") {
  },
  CHART_SERVICES(ClinicServices.class, "/chart") {
  },
  CLIENT_SERVICES(ClientService.class, "/clientService") {
  },
  CLINIC_SERVICES(ClinicServices.class, "/clinicService") {
  },
  EDITOR_SERVICES(EditorServices.class, "/editorService") {
  },
  FILE_SERVICES(AdministrativeServices.class, "/dataImport") {
  },
  SURVEY2_SERVICE(SurveyService.class, "/survey2") {
  },
  REGISTER_SERVICES(RegisterServices.class, "/registrationService") {
  },
  REST_SERVICES(AdministrativeServices.class, "/api") {
  },
  SECURITY_SERVICES(SecurityServices.class, "/securityService") {
  },
  PHYSICIAN_SERVICES(PhysicianServices.class, "/physicianService") {
  },
  BUILDER_SERVICES(BuilderServices.class, "/builderService") {
  },
  APPLICATION_API_SERVICES(ClientService.class, "/apiV10") {
  },
  API_EXTRACT_SERVICES(ApiExtractServices.class, "/apiV10/extract") {
  },
  ASSESSMENT_CONFIG_SERVICES(AssessmentConfigServices.class, "/assessmentConfigService") {
  },
  CONFIGURE_PATIENT_ASSESSMENT_SERVICES(ConfigurePatientAssessmentServices.class, "/configurePatientAssessmentService") {
  };
  private static final Logger logger = Logger.getLogger(Service.class);
  private static final Map<String, Service> urlPathToService = new HashMap<>();

  private Class<?> serviceInterface;
  private String urlPath;

  static {
    for (Service service : Service.values()) {
      logger.info("adding service urlPathToService(" + service.urlPath.toString() + "," + 
                  service.getDeclaringClass().toString() + ")");
      urlPathToService.put(service.urlPath, service);
    }
  }

  Service(Class<?> serviceInterface, String urlPath) {
    this.serviceInterface = serviceInterface;
    this.urlPath = urlPath;
  }

  public Class<?> getInterfaceClass() {
    return serviceInterface;
  }

  public String getUrlPath() {
    return urlPath;
  }

  public static Service byUrlPath(String urlPath) {
    Service svc = urlPathToService.get(urlPath);
    if (svc != null)
      return svc;

    logger.error("Bad service name: "+urlPath);
    return null;
  }

}
