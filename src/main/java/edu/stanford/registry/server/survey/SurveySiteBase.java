package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.SiteInfo;

/**
 * As an subclass of SurveyServiceIntf, this will be cached and should not cache a database.
 * Caching the siteInfo is fine.
 */
abstract class SurveySiteBase implements SurveyServiceIntf {
  protected final SiteInfo siteInfo;
  protected final Long siteId;

  protected SurveySiteBase(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
    this.siteId = siteInfo.getSiteId();
  }
  
  @Override
  public Long getSiteId() {
    return siteId;
  }

  @Override
  public SiteInfo getSiteInfo() {
    return siteInfo;
  }

}
