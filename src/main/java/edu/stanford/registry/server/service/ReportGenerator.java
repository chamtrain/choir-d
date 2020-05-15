package edu.stanford.registry.server.service;

import java.util.Date;

import com.github.susom.database.Database;

import edu.stanford.registry.server.SiteInfo;

/**
 * Interface for a custom clinic report generator
 */

public interface ReportGenerator {

  /**
   * Generate a custom report and return the HTML to be displayed.
   */
  String createReport(Database database, Date fromDate, Date toDate, SiteInfo siteInfo);
  
}
