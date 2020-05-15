package edu.stanford.registry.server.shc.orthohand;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.plugin.ScoreService;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.susom.database.Database;

/**
 * Score provider for the Ortho hand PRWHE survey. Created by tpacht on 1/22/2016.
 */
public class PrwheScoreProvider extends ExtensibleScoreProvider {
  private static final Logger logger = Logger.getLogger(PrwheScoreProvider.class);
  public PrwheScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
    super(dbp, siteInfo, studyName);
  }
  @Override
  public boolean acceptsSurveyName(String studyName) {
    if ("prwhe".equals(studyName)) {
      return true;
    }
    return false;
  }
  @Override
  public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {

    ArrayList<ChartScore> scores = new ArrayList<>();
    if (patientData == null) {
      return scores;
    }
    try {
      Document doc = ScoreService.getDocument(patientData);
      if (doc == null) {
        return scores;
      }
      Element docElement = doc.getDocumentElement();

      if (docElement.getTagName().equals("Form")) {
        PrwheScore chartScore = new PrwheScore(patientData.getDtChanged(), patientData.getPatientId(),
            patientData.getStudyCode(), patientData.getStudyDescription());

        NodeList itemsList = doc.getElementsByTagName("Items");
        NodeList itemList;
        if (itemsList != null && itemsList.getLength() > 0) {
          Element itemsNode = (Element) itemsList.item(0);
          itemList = itemsNode.getElementsByTagName("Item");
        } else {
          itemList = doc.getElementsByTagName("Item");
        }

        if (itemList != null) {
          for (int itemInx = 0; itemInx < itemList.getLength(); itemInx++) {
            Element itemNode = (Element) itemList.item(itemInx);
            String scoreAttrib = itemNode.getAttribute("ItemScore");
            String orderAttrib = itemNode.getAttribute("Order");
            chartScore.setAnswer(Integer.parseInt(orderAttrib), new BigDecimal(Integer.parseInt(scoreAttrib)));
          }
        }
        chartScore.setAssisted(patientData.wasAssisted());
        scores.add(chartScore);

      }
    } catch (IOException ioe) {
      logger.error(
          "IOException parsing xml for patient " + patientData.getPatientId() + " study " + patientData.getStudyCode(),
          ioe);
    } catch (ParserConfigurationException pe) {
      logger.error(
          "ParserException parsing xml for patient " + patientData.getPatientId() + " study "
              + patientData.getStudyCode(), pe);
    } catch (SAXException se) {
      logger
          .error(
              "SAXException parsing xml for patient " + patientData.getPatientId() + " study "
                  + patientData.getStudyCode(), se);
    }

    logger.debug("getScore returning " + scores.size());
    return scores;
  }

  public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> stats, PrintStudy study,
                              ChartConfigurationOptions opts) {

    final TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(baseLineSeries);
    if (stats == null || study == null) {
      return dataset;
    }



      TimeSeries timeDataSetPhysic = new TimeSeries("Physical Health");
      TimeSeries timeDataSetMental = new TimeSeries("Mental Health");
      for (ChartScore stat1 : stats) {
        PrwheScore score = (PrwheScore) stat1;
        ArrayList<BigDecimal> answers = score.getAnswers();
        if (answers != null) {
          Day day = new Day(score.getDate());
          try {
            double painScore = score.getPainScore();
            double functionScore = score.getFunctionScore();

            timeDataSetPhysic.addOrUpdate(day, painScore);
            timeDataSetMental.addOrUpdate(day, functionScore);
          } catch (SeriesException duplicates) {
            // ignore duplicates
          }
        }
      }
      dataset.addSeries(timeDataSetPhysic);
      dataset.addSeries(timeDataSetMental);
      return dataset;

  }
  public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies, PrintStudy study, Patient patient) {
    //ArrayList<SurveyQuestionIntf> questions;
    Table table = new Table();
    //ArrayList<String> headings = new ArrayList<>();
    // get the last one of this study type
    PatientStudyExtendedData prwheStudy = null;
    for (PatientStudyExtendedData patientStudy2 : patientStudies) {
      if (patientStudy2.getStudyCode().intValue() == study.getStudyCode().intValue()) {
        prwheStudy = patientStudy2;
      }
    }
    if (prwheStudy != null) {
      ArrayList<ChartScore> scores = getScore(prwheStudy);
      table.addHeading("PRWHE");
      TableRow ghHeadingRow = new TableRow(500);
      ghHeadingRow.addColumn(new TableColumn("Date", 20));
      ghHeadingRow.addColumn(new TableColumn("Pain Score", 20));
      ghHeadingRow.addColumn(new TableColumn("%ile", 20));
      ghHeadingRow.addColumn(new TableColumn("Function Score", 20));
      ghHeadingRow.addColumn(new TableColumn("%ile", 20));
      table.addRow(ghHeadingRow);

      for (ChartScore score1 : scores) {
        PrwheScore score = (PrwheScore) score1;
        TableRow ghRow = new TableRow(100);
        ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
        Double painScore = score.getPainScore();
        ghRow.addColumn(new TableColumn(painScore.toString(), 20));
        ghRow.addColumn(new TableColumn(calculatePercentile(painScore).toString(), 20));
        Double functionScore = score.getFunctionScore();
        ghRow.addColumn(new TableColumn(functionScore.toString(), 20));
        ghRow.addColumn(new TableColumn(calculatePercentile(functionScore).toString(), 20));
        table.addRow(ghRow);
      }
    }
    return table;
  }

  public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                               ChartConfigurationOptions opts) {
    XYPlot plot = super.getPlot(renderer, ds, studies, opts);
    Collection<IntervalMarker> markers = plot.getRangeMarkers(Layer.BACKGROUND);

    for (IntervalMarker marker : markers ) {
      marker.setLabel(""); // remove the worse label
    }
    return plot;
  }

  static class PrwheScore extends LocalScore implements MultiScore {

    public PrwheScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    public double getPainScore() {
      int score = 0;
      for (int i=1; i<6; i++) {
        score += getAnswer(i).intValue();
      }
      return score;
    }

    public double getFunctionScore() {
      double score = 0;
      for (int i=6; i<16; i++) {
        score+= getAnswer(i).intValue();
      }
      score =  score/2.0;
      return Math.round(score);
    }

    @Override
    public int getNumberOfScores() {
      return 2;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch (scoreNumber) {
      case 1:
        return studyDescription + " - Pain";
      case 2:
        return studyDescription + " - Function";
      default:
        return studyDescription;
      }
    }

    @Override
    public double getScore(int scoreNumber) {
      switch (scoreNumber) {
      case 1:
        return getPainScore();
      case 2:
        return getFunctionScore();
      default:
        return 0;
      }
    }

    public Double getPercentileScore(int scoreNumber) {
      return null;
    }


  }

  @Override
  public int getStudyIndex(String studyDescription) {
    return 6; // used by getPlot(). This gets the same chart background as GLOBAL_HEALTH uses.
  }
}
