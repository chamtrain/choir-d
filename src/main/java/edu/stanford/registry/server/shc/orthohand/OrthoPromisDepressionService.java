package edu.stanford.registry.server.shc.orthohand;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.survey.CustomSurveyServiceIntf;

/**
 * Created by tpacht on 10/27/2016.
 */
public class OrthoPromisDepressionService extends OrthoPromisSurveyService {

  public OrthoPromisDepressionService(SiteInfo siteInfo, CustomSurveyServiceIntf customService) {
    super(siteInfo, customService);
  }

  public OrthoPromisDepressionService(SiteInfo siteInfo) {
    super(siteInfo);
    service = this;
    checkService();
  }

  @Override
  public String getStudyName() {
    return "PROMIS Depression Bank";
  }

  @Override
  public String getSurveySystemName() {
    return "edu.stanford.registry.server.shc.orthohand.OrthoPromisDepressionService";
  }

  @Override
  public String getTitle() {
    return "PROMIS Depression";
  }

  @Override
  public void setValue(String value) {
    //
  }
}
