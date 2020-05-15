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

package edu.stanford.registry.server.utils;

import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.comparator.ChartScoreComparator;

import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYDataset;

import com.github.susom.database.Database;

public class ChartUtilities {
  public static final int PAGE_IMAGE_HEIGHT = 306;
  public static final int PAGE_IMAGE_WIDTH = 675;
  private static Logger logger = Logger.getLogger(ChartUtilities.class);
  private final SiteInfo siteInfo;

  public ChartUtilities(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  public ChartInfo createLineChart(Supplier<Database> dbp, ArrayList<PatientStudyExtendedData> patientStudies,
                                   PrintStudy study, ChartConfigurationOptions opts) {
    if (study == null || patientStudies == null || siteInfo == null) {
      if (study == null)
        logger.error("Cant create line chart, Study is null");
      if (patientStudies == null)
        logger.error("cant create line chart, patientStudy data is null");
      if (siteInfo == null)
        logger.error("can't create line chart, siteInfo is null");
      return null;
    }
    ScoreProvider provider = getScoreProvider(dbp, patientStudies, study.getStudyCode(), siteInfo.getSiteId());
    ArrayList<ChartScore> stats = getStats(dbp.get(), provider, patientStudies, study);

    // XYDataset ds = getTimeSet(stats);
    if (provider == null) {
      return null;
    }
    XYDataset ds = provider.getTimeSet(getBaseLineSeries(stats), stats, study, opts);
    return provider.createLineChart(stats, ds, study, opts);
  }

  public ChartInfo createChartInfo(Supplier<Database> dbp, ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
      boolean withChart, ChartConfigurationOptions opts) {
    if (withChart) {
      return createLineChart(dbp, patientStudies, study, opts);
    }

    ScoreProvider provider = getScoreProvider(dbp, patientStudies, study.getStudyCode(), siteInfo.getSiteId());
    return createChartInfo(dbp, patientStudies, study, provider, opts);
  }

  public ChartInfo createChartInfo(Supplier<Database> dbp, ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study,
      ScoreProvider provider, ChartConfigurationOptions opts) {
    if (provider == null || patientStudies == null || study == null) {
      return null;
    }
    String surveySystemName = null;
    for (PatientStudyExtendedData patientStudy : patientStudies) {
      if (patientStudy != null && patientStudy.getStudyCode() != null
          && patientStudy.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        surveySystemName = patientStudy.getSurveySystemName();
      }
    }
    ArrayList<ChartScore> stats = getStats(dbp.get(), provider, patientStudies, study);
    XYDataset ds = provider.getTimeSet(getBaseLineSeries(stats), stats, study, opts);
    return new ChartInfo(surveySystemName, stats, ds, study);
  }

  private ArrayList<ChartScore> getStats(Database database, ScoreProvider provider, ArrayList<PatientStudyExtendedData> patientStudies,
      Study study) {

    ArrayList<ChartScore> scores = new ArrayList<>();
    // HashMap<String, SurveyServiceIntf> surveys = new HashMap<String,
        // SurveyServiceIntf>();
    if (patientStudies == null || study == null) {
      return scores;
    }
    if (provider == null) {
      logger.debug("No ScoreProvider was found for study " + study);
      return scores;
    }
    SurveySystDao dao = new SurveySystDao(database);
    HashMap<Integer, Integer> replacements = dao.getStudiesReplaced();
    try {
      for (PatientStudyExtendedData patientStudy1 : patientStudies) {
        // Also include the scores of any patient studies for the study this one replaced
        if (patientStudy1 != null && patientStudy1.getStudyCode() != null) {
          int count = 0; // safety incase replacement codes loop
          while (replacements.get(patientStudy1.getStudyCode()) != null && count < 100) {
            count++;
            patientStudy1.setStudyCode(replacements.get(patientStudy1.getStudyCode()));
          }
        }

        if (patientStudy1 != null && patientStudy1.getStudyCode() != null
            && patientStudy1.getStudyCode().intValue() == study.getStudyCode().intValue()) {
          PatientStudyExtendedData patientStudy = patientStudy1;
          ArrayList<ChartScore> score = provider.getScore(patientStudy1);
          if (score == null || score.size() < 1) {
            logger.debug("ScoreProvider: " + provider.getDescription() + " returned no score for patient "
                + patientStudy.getPatientId() + " study " + patientStudy.getStudyDescription());
          } else {
            for (ChartScore aScore : score) {
              if (!patientStudy1.getStudyDescription().equals(study.getStudyDescription())) {
                aScore.setReplaced(true);
              }
              scores.add(aScore);
            }
          }
        }
      }
      Collections.sort(scores, new ChartScoreComparator<ChartScore>(ChartScoreComparator.SORT_BY_DATE));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return scores;
  }

  public static TimeSeries getBaseLineSeries() {
    final TimeSeries timeDataSet = new TimeSeries("");

    Calendar cal = Calendar.getInstance();
    Date now = new Date();
    cal.setTime(now);
    for (int i = 0; i < 6; i++) {
      timeDataSet.add(new Day(cal.getTime()), 0);
      cal.add(Calendar.MONTH, -1);
    }
    return timeDataSet;
  }

  public static TimeSeries getBaseLineSeries(ArrayList<ChartScore> stats) {
    final TimeSeries timeDataSet = new TimeSeries("");
    if (stats == null || stats.size() < 1) {
      return getBaseLineSeries();
    }
    Date firstDate = null;
    Date lastDate = null;
    for (ChartScore score : stats) {
      if (firstDate == null || score.getDate().before(firstDate)) {
        firstDate = score.getDate();
      }
      if (lastDate == null || score.getDate().after(lastDate)) {
        lastDate = score.getDate();
      }
    }
    if (firstDate == null) {
      firstDate = new Date();
    }
    if (lastDate == null) {
      lastDate = new Date();
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(firstDate);
    cal.set(Calendar.DAY_OF_MONTH, 1);
    while (cal.getTime().before(lastDate) || timeDataSet.getItemCount() < 6) {
      timeDataSet.add(new Day(cal.getTime()), 0);
      cal.add(Calendar.MONTH, 1);
    }
    timeDataSet.add(new Day(cal.getTime()), 0);

    /*for (int s = 0; s < stats.size(); s++) {
      ChartScore score = stats.get(s);
      if (score.getAssisted()) {
        timeDataSet.addOrUpdate(new Day(score.getDate()), 999.99);
      }
    }*/
    return timeDataSet;
  }

  /* private String getStudyName(ArrayList<PatientStudyExtendedData> studies, Integer studyCode) {
    if (studies != null && studyCode != null) {
      for (int i = 0; i < studies.size(); i++) {
        if (studies.get(i) != null && studies.get(i).getStudyCode() != null
            && studies.get(i).getStudyCode().intValue() == studyCode.intValue()) {
          return studies.get(i).getStudyDescription();
        }
      }
    }
    return "";
  } */

  public Map<RenderingHints.Key, Object> getRenderingHints() {
    Map<RenderingHints.Key, Object> map = new HashMap<>();

    map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    map.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    map.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    // map.put(RenderingHints.KEY_STROKE_CONTROL,
    // RenderingHints.VALUE_STROKE_PURE);
    // map.put(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_OFF);
    map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    return map;
  }

  public String formatExplanationText(Supplier<Database> dbp, SurveySystem sSys, Study study,
                                      ArrayList<ChartScore> scores, Long siteId) {
    if (scores == null || study == null || sSys == null) return null;
    SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(sSys.getSurveySystemName());
    if (surveyService == null) {
      logger.error("No survey service was found for type " + sSys.getSurveySystemName());
      return study.getExplanation();
    }
    return surveyService.getScoreProvider(dbp, study.getStudyDescription()).formatExplanationText(study, scores);
  }

  public ScoreProvider getScoreProvider(Supplier<Database> dbp, ArrayList<PatientStudyExtendedData> patientStudies,
                                        Integer study, Long siteId) {
    try {
      //for (PatientStudyExtendedData patientStudy1 : patientStudies) {
      for (int inx=patientStudies.size() - 1; inx >=0; inx--) { // read in reverse to use the newest provider not oldest
        PatientStudyExtendedData patientStudy1 = patientStudies.get(inx);
        if (patientStudy1 != null && patientStudy1.getStudyCode() != null
            && patientStudy1.getStudyCode().intValue() == study.intValue()) {
          PatientStudyExtendedData patientStudy = patientStudy1;
          return SurveyServiceFactory.getFactory(siteInfo).getScoreProvider(dbp, patientStudy.getSurveySystemName(),
              patientStudy.getStudyDescription());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
