package edu.stanford.registry.server.shc.orthohand;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.CustomSurveyServiceIntf;
import edu.stanford.registry.server.survey.LocalPromisSurveyService;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.SurveySystem;

import java.util.HashMap;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

/**
 * Created by tpacht on 10/25/2016.
 */
public abstract class OrthoPromisSurveyService extends LocalPromisSurveyService implements CustomSurveyServiceIntf {
  private static final Logger logger = Logger.getLogger(OrthoPromisSurveyService.class);

  CustomSurveyServiceIntf service;
  public OrthoPromisSurveyService(SiteInfo siteInfo, CustomSurveyServiceIntf customService) {
    super(siteInfo);
    service = customService;
    checkService();
  }

  public OrthoPromisSurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new OrthoPromisScoreProvider(siteInfo, 1);
  }

  @Override
  public int getSurveySystemId(Database database) {
    return mySurveySystem.getInstance(database, getSurveySystemName()).getSurveySystemId();
  }

  @Override
  public String getTitle() {
    return service.getTitle();
  }

  @Override
  public void setValue(String value) {
    service.setValue(value);
  }

  void checkService() throws ServiceUnavailableException {
    if (service == null) {
      logger.debug("No service");
      throw new ServiceUnavailableException();
    }
  }
}
class mySurveySystem extends SurveySystem {

  private static final HashMap<String, mySurveySystem> mySystems = new HashMap<>();
  private static final Logger logger = Logger.getLogger(mySurveySystem.class);

  private mySurveySystem(Database database, String surveySystemName) throws DataException {
    SurveySystem ssys = new SurveySystDao(database).getOrCreateSurveySystem(surveySystemName, logger);
    this.copyFrom(ssys);
  }

  public static SurveySystem getInstance(Database database, String surveySystemName) throws DataException {
    if (mySystems.get(surveySystemName) == null) {
      mySystems.put(surveySystemName, new mySurveySystem(database, surveySystemName));
    }
    return mySystems.get(surveySystemName);
  }
}
