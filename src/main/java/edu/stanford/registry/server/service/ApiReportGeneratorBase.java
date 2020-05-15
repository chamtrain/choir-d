package edu.stanford.registry.server.service;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.service.rest.api.ApiReportCommon;

import java.util.function.Supplier;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;

import com.github.susom.database.Database;

public class ApiReportGeneratorBase extends ApiReportCommon implements ApiReportGenerator {


  @Override
  public JSONObject getReportParameters(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName) {
    return getDefaultReportParameters();
  }

  @Override
  public JSONObject getReportData(Supplier<Database> databaseProvider, SiteInfo siteInfo, String reportName, JsonRepresentation jsonRepresentation) {
    return null;
  }

  public JSONObject getDefaultReportParameters() {
    JSONObject params = new JSONObject();
    params.accumulate("reportParameters", makeReportInputOption("From", "fromDt", "date"));
    params.accumulate("reportParameters", makeReportInputOption("To", "toDt", "date"));
    return params;
  }
}
