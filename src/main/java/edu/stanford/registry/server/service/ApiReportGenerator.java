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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.rest.ApiStatusException;

import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;

import com.github.susom.database.Database;

/**
 * Interface for custom API clinic report generators
 *
 * @author tpacht
 */

public interface ApiReportGenerator {

  /**
   * Generate the custom report
   * @param  databaseProvider Provider to access the database
   * @param siteInfo Site iniformation
   * @param reportName The name passed into the report generator
   * @param jsonRepresentation Contains the report parameter values
   * @return JSONObject The report data
   */
  JSONObject getReportData (Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation)
      throws ApiStatusException;

  /**
   * Gets the parameters for running the custom report
   * @param databaseProvider Provider to access the database
   * @param siteInfo Site information object
   * @param reportName The name passed into the report generator
   * @return JSONObject With the parameters for running the report
   * @throws ApiStatusException If the report name isn't supported
   */
  JSONObject getReportParameters(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName) throws ApiStatusException;
  
}
