package edu.stanford.registry.server.shc.orthohand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;

import com.github.susom.database.Database;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.CustomSurveyServiceIntf;
import edu.stanford.registry.shared.Study;

/**
 * Created by tpacht on 10/27/2016.
 */
public class OrthoPromisUpperExtService extends OrthoPromisSurveyService {

  public OrthoPromisUpperExtService(SiteInfo siteInfo, CustomSurveyServiceIntf customService) {
    super(siteInfo, customService);
  }

  public OrthoPromisUpperExtService(SiteInfo siteInfo) {
    super(siteInfo);
    service = this;
    checkService();
  }

  @Override
  public String getStudyName() {
    return "PROMIS Bank v1.2 - Upper Extremity";
  }

  @Override
  public String getSurveySystemName() {
    return "edu.stanford.registry.server.shc.orthohand.OrthoPromisUpperExtService";
  }

  @Override
  public String getTitle() {
    return "PROMIS Upper Extremity";
  }

  @Override
  public void setValue(String value) {
    //
  }
  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    return new OrthoPromisUpperExtScoreProvider(1);
  }

  private class OrthoPromisUpperExtScoreProvider extends OrthoPromisScoreProvider {
    public OrthoPromisUpperExtScoreProvider(int version) {
      super(getSiteInfo(), version);
      this.version = version;
    }

    @Override
    public XYPlot getPlot(ChartInfo chartInfo, ArrayList<Study> studies,
                          ChartConfigurationOptions opts) {

      XYPlot plot = super.getPlot(chartInfo, studies, opts);

      @SuppressWarnings("unchecked")
      Collection<IntervalMarker> markers = plot.getRangeMarkers(Layer.BACKGROUND);

      for (IntervalMarker marker : markers ) {
        if (marker != null && marker.getLabel() != null && "Worse".equals(marker.getLabel())) {
          marker.setLabel("Better"); // remove the worse label
        }
      }

      return plot;
    }
  }
}
