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

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.User;
import edu.stanford.survey.server.ClientIdentifiers;
import edu.stanford.survey.server.SurveyServiceImpl;
import edu.stanford.survey.server.SurveySystemFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.github.susom.database.Database;

public enum ServiceImpl {

  ADMIN_SERVICES(Service.ADMIN_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new AdministrativeServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  CHART_SERVICES(Service.CHART_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new ClinicServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  CLIENT_SERVICES(Service.CLIENT_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext context, SiteInfo siteInfo) {
      return new ClientServicesImpl(user, databaseProvider, context, siteInfo);
    }
  },
  CLINIC_SERVICES(Service.CLINIC_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new ClinicServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  EDITOR_SERVICES(Service.EDITOR_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new EditorServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  FILE_SERVICES(Service.FILE_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new AdministrativeServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  SURVEY2_SERVICE(Service.SURVEY2_SERVICE) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo thisSiteInfoIsNull) {
      SurveySystemFactory factory = serverContext.createSurveySystemFactory(databaseProvider, serverContext.appConfig(), serverContext.getSitesInfo());
      return new SurveyServiceImpl(factory, databaseProvider, clientIds);
    }
  },
  REGISTER_SERVICES(Service.REGISTER_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new RegisterServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  REST_SERVICES(Service.REST_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new AdministrativeServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  SECURITY_SERVICE(Service.SECURITY_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new SecurityServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  PHYSICIAN_SERVICE(Service.PHYSICIAN_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new PhysicianServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  BUILDER_SERVICE(Service.BUILDER_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new BuilderServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  APPLICATION_API_SERVICES(Service.APPLICATION_API_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new ClientServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  API_EXTRACT_SERVICES(Service.API_EXTRACT_SERVICES) {
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new ApiExtractServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  ASSESSMENT_CONFIG_SERVICE(Service.ASSESSMENT_CONFIG_SERVICES){
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                         ServerContext serverContext, SiteInfo siteInfo) {
      return new AssessmentConfigServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  },
  CONFIGURE_PATIENT_ASSESSMENT_SERVICES(Service.CONFIGURE_PATIENT_ASSESSMENT_SERVICES){
    @Override
    public Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
        ServerContext serverContext, SiteInfo siteInfo) {
      return new ConfigurePatientAssessmentServicesImpl(user, databaseProvider, serverContext, siteInfo);
    }
  };

  private static final Map<Service, ServiceImpl> serviceToImpl = new EnumMap<>(Service.class);
  private Service service;

  static {
    for (ServiceImpl impl : ServiceImpl.values()) {
      serviceToImpl.put(impl.service, impl);
    }
  }

  ServiceImpl(Service service) {
    this.service = service;
  }

  public Service getService() {
    return service;
  }

  public abstract Object create(ClientIdentifiers clientIds, User user, Supplier<Database> databaseProvider,
                                ServerContext serverContext, SiteInfo siteInfo);

  public static ServiceImpl byService(Service service) {
    return serviceToImpl.get(service);
  }
}
