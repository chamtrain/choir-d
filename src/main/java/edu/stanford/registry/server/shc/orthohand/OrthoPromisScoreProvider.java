package edu.stanford.registry.server.shc.orthohand;

import java.util.ArrayList;
import java.util.Collection;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.Layer;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.survey.ChartInfo;
import edu.stanford.registry.server.survey.LocalPromisScoreProvider;
import edu.stanford.registry.shared.Study;

/**
 * Created by tpacht on 10/25/2016.
 */
public class OrthoPromisScoreProvider extends LocalPromisScoreProvider {
  int version;

  public OrthoPromisScoreProvider(SiteInfo siteInfo, int version) {
    super(siteInfo, version);
    this.version = version;
  }

  @Override
  public XYPlot getPlot(ChartInfo chartInfo, ArrayList<Study> studies,
                        ChartConfigurationOptions opts) {

    XYPlot plot = super.getPlot(chartInfo, studies, opts);
    //String highLabel = "Worse";
    if (studies.get(0).getStudyDescription() != null && "PROMIS Bank v1.2 - Upper Extremity".equals(studies.get(0).getStudyDescription())) {
      Collection<IntervalMarker> markers = plot.getRangeMarkers(Layer.BACKGROUND);

      for (IntervalMarker marker : markers ) {
        if (marker != null && marker.getLabel() != null && "Worse".equals(marker.getLabel())) {
          marker.setLabel("Better"); // remove the worse label
        }
      }
    }

    return plot;
  }



}
