package edu.stanford.registry.server.shc.ccte;

import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.RegistryAssessmentsService;


/**
 * Created by scweber on 02/16/2016
 */
public class CCTESurveyService extends RegistryAssessmentsService {
  public CCTESurveyService(SiteInfo siteInfo) {
    super(siteInfo);
  }

  private static final Logger logger = Logger.getLogger(CCTESurveyService.class);

  protected static Logger getLogger() {
    return logger;
  }

  /**
   * Return the appropriate score provider based on the study name.
   */


  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new CCTEScoreProvider(dbp.get(), siteInfo);
  }



  public static class CCTEScoreProvider extends ExtensibleScoreProvider {

    public CCTEScoreProvider (Supplier<Database> dbp, SiteInfo siteInfo) {
      super(dbp, siteInfo, "CCTE");
    }
  }
}
