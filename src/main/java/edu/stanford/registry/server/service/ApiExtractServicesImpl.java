/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.server.reports.SquareTableExportReport;
import edu.stanford.registry.shared.InvalidDataElementException;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.Database;

public class ApiExtractServicesImpl implements ApiExtractServices {
  private final Logger logger = LoggerFactory.getLogger(edu.stanford.registry.server.service.ApiExtractServicesImpl.class);
  private final Supplier<Database> dbp;
  private final SiteInfo siteInfo;

  ApiExtractServicesImpl(User usr, Supplier<Database> databaseProvider, ServerContext context, SiteInfo siteInfo) {
    dbp = databaseProvider;
    this.siteInfo = siteInfo;
  }

  @Override
  public ArrayList<String> listSquareTableColumns(String tablename) {
    logger.debug("in listSquareTableColumns (" + tablename + ")");
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportColumns(tablename);
  }

  @Override
  public ArrayList<ArrayList<Object>> exportSquareTable(String tableName) {
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportData(tableName);
  }

  @Override
  public ArrayList<ArrayList<Object>> exportSquareTable(String tableName, Date fromDt, Date toDt) {
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportData(tableName, fromDt, toDt);
  }

  @Override
  public ArrayList<ArrayList<Object>> exportSquareTable(String tablename, ArrayList<String> columns) throws InvalidDataElementException {
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportData(tablename, columns);
  }

  @Override
  public ArrayList<ArrayList<Object>> exportSquareTable(String tablename, Date fromDt, Date toDt, ArrayList<String> columns) throws InvalidDataElementException {
    SquareTableExportReport report = new SquareTableExportReport(dbp.get(), siteInfo);
    return report.getReportData(tablename, fromDt, toDt, columns);
  }

}
