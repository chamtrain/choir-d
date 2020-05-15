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

package edu.stanford.registry.server;

import edu.stanford.registry.client.service.AdminService;
import edu.stanford.registry.server.service.AdministrativeServices;
import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.FieldVerifier;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.ProcessInfo;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

/**
 * The server side implementation of the RPC service.
 */

public class AdminServiceImpl extends ClinicServiceImpl implements AdminService {

  private static final long serialVersionUID = -4570170196740302686L;
  private static final Logger logger = Logger.getLogger(AdminServiceImpl.class);
  //private Mailer mailer;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public ArrayList<DataTable> getTableData(String tableName) throws IllegalArgumentException,
  ServiceUnavailableException {
    // Verify that the input is valid.
    if (!FieldVerifier.isValidName(tableName)) {
      throw new IllegalArgumentException("Name must be at least 4 characters long");
    }

    try {
      tableName = tableName.trim();
      tableName = ServerUtils.escapeInput(tableName);
      logger.debug("Looking for table: " + tableName);

      return getService().getTableData(tableName);
    } catch (InvalidDataElementException e) {
      String errorMessage = "Could not get data from table " + tableName + " because of error: " + e.getMessage();
      logger.error(errorMessage, e);
      throw new ServiceUnavailableException(errorMessage);
    }

  }

  @Override
  public int loadPendingImports(String definitionName) throws ServiceUnavailableException {
    try {
      return getService().loadPendingImports(definitionName);
    } catch (IOException e) {
      logger.error("IOException loading pending " + definitionName + " files", e);
      throw new ServiceUnavailableException("could not load pending files");
    }
  }

  @Override
  public ArrayList<String> getFileImportDefinitions() throws ServiceUnavailableException {
    try {
      return getService().getFileImportDefinitions();
    } catch (IOException e) {
      logger.error("IOException loading definition names ", e);
      throw new ServiceUnavailableException("could not load import definitions");
    }
  }

  @Override
  public int doSurveyInvitations() {
    try {
      SiteInfo siteInfo = getServiceSiteInfo();
      String url = siteInfo.getGlobalProperty("survey.link"); // customized by the service
      return getService().doSurveyInvitations(siteInfo.getMailer(), url);
    } catch (Exception e) {
      logger.error("Exception running doSurveyInvitations ", e);
      throw new ServiceUnavailableException(e.toString());
    }
  }

  public Date fromTime(Date dtFrom) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(dtFrom);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    return new Date(cal.getTimeInMillis());
  }

  public Date toTime(Date dtTo) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(dtTo);
    cal.set(Calendar.HOUR, 23);
    cal.set(Calendar.MINUTE, 59);
    cal.set(Calendar.SECOND, 59);
    return new Date(cal.getTimeInMillis());
  }

  @Override
  public boolean reloadXml() {
    getService().reloadXml();
    return true;
  }

  @Override
  public void reloadUsers() {
    getService().reloadUsers();
  }

  @Override
  public void reloadConfig() {
    getService().reloadConfig();
  }

  @Override
  public ArrayList<ProcessInfo> getRunningProcesses() {
    return getService().getRunningProcesses();
  }

  @Override
  public void clearProcesses() {
    getService().clearProcesses();
  }

  @Override
  public ConfigParam updateConfig(ConfigParam param) throws Exception {
    return getService().updateConfig(param);
  }

  @Override
  public ArrayList<RegConfigProperty> getRegConfigProperties () {
    return getService().getRegConfigProperties();
  }

  @Override
  public ConfigParam getConfig (RegConfigProperty property ) {
    return getService().getConfig(property);
  }
  private AdministrativeServices getService() {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    AdministrativeServices clinicService = (AdministrativeServices) regRequest.getService();
    return clinicService;
  }

  private SiteInfo getServiceSiteInfo() {
    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    return regRequest.getSiteInfo();
  }
}
