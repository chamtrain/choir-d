/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.shc.gi;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.Token;
import edu.stanford.registry.server.charts.ChartConfigurationOptions;
import edu.stanford.registry.server.charts.ChartMaker;
import edu.stanford.registry.server.database.ActivityDao;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.server.survey.ExtensibleScoreProvider;
import edu.stanford.registry.server.survey.NextQuestion;
import edu.stanford.registry.server.survey.PrintStudy;
import edu.stanford.registry.server.survey.QualifyQuestionService;
import edu.stanford.registry.server.survey.RegistryShortFormScoreProvider;
import edu.stanford.registry.server.survey.SurveyAdvanceBase;
import edu.stanford.registry.server.survey.SurveyServiceFactory;
import edu.stanford.registry.server.survey.SurveyServiceIntf;
import edu.stanford.registry.server.tools.CustomDateAxis;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.server.utils.Mailer;
import edu.stanford.registry.server.utils.XMLFileUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.DataException;
import edu.stanford.registry.shared.LocalScore;
import edu.stanford.registry.shared.MultiScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudy;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.ServiceUnavailableException;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.shared.survey.Table;
import edu.stanford.registry.shared.survey.TableColumn;
import edu.stanford.registry.shared.survey.TableRow;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.SubmitStatus;
import edu.stanford.survey.client.api.SurveyFactory;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.github.susom.database.Database;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

public class GISQSurveyService extends QualifyQuestionService {

  private static final Logger logger = LoggerFactory.getLogger(GISQSurveyService.class);
  private final static String SURVEY_SYSTEM_NAME = "GISQSurveyService";
  private final SurveyFactory factory = AutoBeanFactorySource.create(SurveyFactory.class);
  private static final String CONSENT_RESOURCE = "shc/gi/ICF-Version-1-6_6_19-DP-1.pdf";
  private static final Map<String, String> gi_score_map = Stream.of(new String[][] {
      { "BellyPain-5-6", "39.3,4.4"},
      { "BellyPain-5-7", "43.3,3.8"},
      { "BellyPain-5-8", "47,3.3"},
      { "BellyPain-5-9", "49.9,3.1"},
      { "BellyPain-5-10", "52.3,3"},
      { "BellyPain-5-11", "54.4,3"},
      { "BellyPain-5-12", "56.5,3"},
      { "BellyPain-5-13", "58.4,2.9"},
      { "BellyPain-5-14", "60.2,2.8"},
      { "BellyPain-5-15", "61.9,2.8"},
      { "BellyPain-5-16", "63.5,2.8"},
      { "BellyPain-5-17", "65,2.8"},
      { "BellyPain-5-18", "66.6,2.8"},
      { "BellyPain-5-19", "68.1,2.7"},
      { "BellyPain-5-20", "69.6,2.7"},
      { "BellyPain-5-21", "71.1,2.8"},
      { "BellyPain-5-22", "72.8,2.8"},
      { "BellyPain-5-23", "74.7,3"},
      { "BellyPain-5-24", "76.8,3.1"},
      { "BellyPain-5-25", "80,3.7"},
      { "Constipation-9-12", "44.4,3.4"},
      { "Constipation-9-13", "46.1,3.1"},
      { "Constipation-9-14", "47.7,2.8"},
      { "Constipation-9-15", "49.0,2.6"},
      { "Constipation-9-16", "50.2,2.5"},
      { "Constipation-9-17", "51.3,2.4"},
      { "Constipation-9-18", "52.3,2.4"},
      { "Constipation-9-19", "53.3,2.3"},
      { "Constipation-9-20", "54.3,2.3"},
      { "Constipation-9-21", "55.2,2.3"},
      { "Constipation-9-22", "56.2,2.3"},
      { "Constipation-9-23", "57.1,2.3"},
      { "Constipation-9-24", "57.9,2.2"},
      { "Constipation-9-25", "58.8,2.2"},
      { "Constipation-9-26", "59.6,2.2"},
      { "Constipation-9-27", "60.4,2.2"},
      { "Constipation-9-28", "61.3,2.2"},
      { "Constipation-9-29", "62.1,2.2"},
      { "Constipation-9-30", "62.9,2.2"},
      { "Constipation-9-31", "63.7,2.3"},
      { "Constipation-9-32", "64.6,2.3"},
      { "Constipation-9-33", "65.4,2.3"},
      { "Constipation-9-34", "66.3,2.4"},
      { "Constipation-9-35", "67.2,2.5"},
      { "Constipation-9-36", "68.2,2.6"},
      { "Constipation-9-37", "69.2,2.7"},
      { "Constipation-9-38", "70.3,2.8"},
      { "Constipation-9-39", "71.4,3"},
      { "Constipation-9-40", "72.6,3.2"},
      { "Constipation-9-41", "73.9,3.4"},
      { "Constipation-9-42", "75.3,3.6"},
      { "Constipation-9-43", "76.9,3.9"},
      { "Constipation-9-44", "78.7,4.1"},
      { "Constipation-9-45", "80.8,4.2"},
      { "Diarrhea-6-8", "45.9,4.12"},
      { "Diarrhea-6-9", "48.2,3.69"},
      { "Diarrhea-6-10", "50.1,3.33"},
      { "Diarrhea-6-11", "51.9,2.65"},
      { "Diarrhea-6-12", "53.3,2.35"},
      { "Diarrhea-6-13", "54.5,2.25"},
      { "Diarrhea-6-14", "55.6,2.20"},
      { "Diarrhea-6-15", "56.7,2.16"},
      { "Diarrhea-6-16", "57.8,2.10"},
      { "Diarrhea-6-17", "58.8,2.05"},
      { "Diarrhea-6-18", "59.8,2.00"},
      { "Diarrhea-6-19", "60.7,1.98"},
      { "Diarrhea-6-20", "61.6,1.98"},
      { "Diarrhea-6-21", "62.5,1.98"},
      { "Diarrhea-6-22", "63.5,2.01"},
      { "Diarrhea-6-23", "64.4,2.07"},
      { "Diarrhea-6-24", "65.5,2.18"},
      { "Diarrhea-6-25", "66.6,2.34"},
      { "Diarrhea-6-26", "67.9,2.63"},
      { "Diarrhea-6-27", "69.2,2.90"},
      { "Diarrhea-6-28", "70.8,3.22"},
      { "Diarrhea-6-29", "72.3,3.50"},
      { "Diarrhea-6-30", "75.2,4.32"},
      { "GasBloating-13-14", "41.6,4.9"},
      { "GasBloating-13-15", "44.3,4.4"},
      { "GasBloating-13-16", "46.6,3.7"},
      { "GasBloating-13-17", "48.6,3.0"},
      { "GasBloating-13-18", "50.2,2.4"},
      { "GasBloating-13-19", "51.3,2.0"},
      { "GasBloating-13-20", "52.2,1.8"},
      { "GasBloating-13-21", "53.1,1.7"},
      { "GasBloating-13-22", "53.8,1.6"},
      { "GasBloating-13-23", "54.6,1.5"},
      { "GasBloating-13-24", "55.2,1.5"},
      { "GasBloating-13-25", "55.9,1.5"},
      { "GasBloating-13-26", "56.5,1.4"},
      { "GasBloating-13-27", "57.0,1.4"},
      { "GasBloating-13-28", "57.6,1.4"},
      { "GasBloating-13-29", "58.1,1.4"},
      { "GasBloating-13-30", "58.7,1.4"},
      { "GasBloating-13-31", "59.2,1.4"},
      { "GasBloating-13-32", "59.7,1.4"},
      { "GasBloating-13-33", "60.2,1.3"},
      { "GasBloating-13-34", "60.7,1.3"},
      { "GasBloating-13-35", "61.2,1.3"},
      { "GasBloating-13-36", "61.7,1.3"},
      { "GasBloating-13-37", "62.2,1.3"},
      { "GasBloating-13-38", "62.6,1.3"},
      { "GasBloating-13-39", "63.1,1.3"},
      { "GasBloating-13-40", "63.6,1.3"},
      { "GasBloating-13-41", "64.1,1.3"},
      { "GasBloating-13-42", "64.6,1.3"},
      { "GasBloating-13-43", "65.1,1.3"},
      { "GasBloating-13-44", "65.5,1.3"},
      { "GasBloating-13-45", "66.0,1.3"},
      { "GasBloating-13-46", "66.5,1.4"},
      { "GasBloating-13-47", "67.0,1.4"},
      { "GasBloating-13-48", "67.5,1.4"},
      { "GasBloating-13-49", "68.1,1.4"},
      { "GasBloating-13-50", "68.7,1.5"},
      { "GasBloating-13-51", "69.3,1.6"},
      { "GasBloating-13-52", "70.0,1.7"},
      { "GasBloating-13-53", "70.8,1.8"},
      { "GasBloating-13-54", "71.7,2.1"},
      { "GasBloating-13-55", "72.7,2.4"},
      { "GasBloating-13-56", "73.9,2.7"},
      { "GasBloating-13-57", "75.3,3.1"},
      { "GasBloating-13-58", "76.9,3.5"},
      { "GasBloating-13-59", "79.0,4.0"},
      { "NauseaVomiting-4-5", "45.0,6.3"},
      { "NauseaVomiting-4-6", "49.3,5.8"},
      { "NauseaVomiting-4-7", "52.9,5.3"},
      { "NauseaVomiting-4-8", "55.9,4.8"},
      { "NauseaVomiting-4-9", "58.7,4.0"},
      { "NauseaVomiting-4-10", "60.9,3.8"},
      { "NauseaVomiting-4-11", "62.8,3.7"},
      { "NauseaVomiting-4-12", "64.6,3.6"},
      { "NauseaVomiting-4-13", "66.4,3.6"},
      { "NauseaVomiting-4-14", "68.1,3.6"},
      { "NauseaVomiting-4-15", "69.8,3.6"},
      { "NauseaVomiting-4-16", "71.6,3.7"},
      { "NauseaVomiting-4-17", "73.5,3.8"},
      { "NauseaVomiting-4-18", "75.6,3.9"},
      { "NauseaVomiting-4-19", "77.9,4.1"},
      { "NauseaVomiting-4-20", "80.1,4.1"},
      { "Reflux-13-16", "38.7,5.1"},
      { "Reflux-13-17", "40.8,4.7"},
      { "Reflux-13-18", "42.7,4.4"},
      { "Reflux-13-19", "44.4,4.1"},
      { "Reflux-13-20", "46.0,3.9"},
      { "Reflux-13-21", "47.4,3.7"},
      { "Reflux-13-22", "48.7,3.5"},
      { "Reflux-13-23", "49.9,3.4"},
      { "Reflux-13-24", "51.1,3.2"},
      { "Reflux-13-25", "52.1,3.1"},
      { "Reflux-13-26", "53.1,3.0"},
      { "Reflux-13-27", "54.1,3.0"},
      { "Reflux-13-28", "55.0,2.9"},
      { "Reflux-13-29", "55.8,2.9"},
      { "Reflux-13-30", "56.7,2.9"},
      { "Reflux-13-31", "57.5,2.8"},
      { "Reflux-13-32", "58.3,2.8"},
      { "Reflux-13-33", "59.1,2.8"},
      { "Reflux-13-34", "59.9,2.8"},
      { "Reflux-13-35", "60.7,2.8"},
      { "Reflux-13-36", "61.4,2.8"},
      { "Reflux-13-37", "62.2,2.8"},
      { "Reflux-13-38", "62.9,2.8"},
      { "Reflux-13-39", "63.7,2.8"},
      { "Reflux-13-40", "64.4,2.8"},
      { "Reflux-13-41", "65.1,2.8"},
      { "Reflux-13-42", "65.9,2.8"},
      { "Reflux-13-43", "66.6,2.8"},
      { "Reflux-13-44", "67.3,2.8"},
      { "Reflux-13-45", "68.1,2.8"},
      { "Reflux-13-46", "68.8,2.8"},
      { "Reflux-13-47", "69.6,2.8"},
      { "Reflux-13-48", "70.3,2.8"},
      { "Reflux-13-49", "71.1,2.8"},
      { "Reflux-13-50", "71.9,2.8"},
      { "Reflux-13-51", "72.7,2.8"},
      { "Reflux-13-52", "73.5,2.9"},
      { "Reflux-13-53", "74.3,2.9"},
      { "Reflux-13-54", "75.2,3.0"},
      { "Reflux-13-55", "76.1,3.1"},
      { "Reflux-13-56", "77.0,3.1"},
      { "Reflux-13-57", "78.0,3.2"},
      { "Reflux-13-58", "79.1,3.3"},
      { "Reflux-13-59", "80.1,3.4"},
      { "Reflux-13-60", "81.2,3.4"},
      { "Reflux-13-61", "82.4,3.4"},
      { "Reflux-13-62", "83.4,3.3"},
      { "Reflux-13-63", "84.4,3.2"},
      { "Reflux-13-64", "85.3,3.0"},

  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

  private static final String BELLYPAIN_5 = "BellyPain-5-";
  private static final String CONSTIPATION_9 = "Constipation-9-";
  private static final String DIARRHEA_6 = "Diarrhea-6-";
  private static final String GASBLOATING_13 = "GasBloating-13-";
  private static final String NauseaVomiting_4 = "NauseaVomiting-4-";
  private static final String Reflux_13 = "Reflux-13-";
  /**
   * As an implementor of SurveyServiceIntf, this will be cached and must not cache a database.
   */
  public GISQSurveyService(String patientAttributeName, SiteInfo siteInfo) {
    super(patientAttributeName, siteInfo);
  }


  @Override
  public ScoreProvider getScoreProvider(Supplier<Database> dbp, String studyName) {
    if (studyName.contains("@")) {
      int inx = studyName.indexOf("@");
      studyName = studyName.substring(0, inx);
    }
    switch (studyName) {
    case "GISQGERDQ":
    case "GIPFDI20":
    case "GIPFIQ7":
      return new GISQScoreProvider(dbp.get(), siteInfo, studyName);
    case "GIACE":
      return new ACEScoreProvider(dbp.get(), siteInfo, studyName);
    case "GICIRS":
      return new CIRSScoreProvider(dbp.get(), siteInfo, studyName);
    case "GISQGCSI":
      return new GCSIScoreProvider(dbp.get(), siteInfo, studyName);
    case "GISQBEDQ":
      return new BEDQScoreProvider(dbp.get(), siteInfo, studyName);
    case "GISQEckardt":
      return new EckardtScoreProvider(dbp.get(), siteInfo, studyName);
    case "GISQRSI":
      return new QRSIScoreProvider(dbp.get(), siteInfo, studyName);
    case "gipromis":
      return new GIPromisScoreProvider(dbp.get(), siteInfo, studyName);

    }
    return new RegistryShortFormScoreProvider(dbp.get(), siteInfo);
  }

  @Override
  public NextQuestion handleResponse(Database database, PatientStudyExtendedData patStudyExtended, SubmitStatus submitStatus,
                                     String answerJson) {
    if (patStudyExtended == null || patStudyExtended.getDtChanged() != null) { // missing data or survey is already done
      return null;
    }

    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(patStudyExtended, true);
    if (patStudy == null) { // doesn't exist !
      throw new DataException("Patient Study not found for study " + patStudyExtended.getStudyCode() + " token "
          + patStudyExtended.getToken());
    }

    Patient patient = patStudyExtended.getPatient();
    if (patient == null) {
      throw new DataException("Patient not found for surveyRegId");
    }
    if (patStudyExtended.getStudyDescription() == null || patStudyExtended.getStudyDescription().isEmpty()) {
      throw new DataException("Study " + patStudyExtended.getStudyCode() + " Has no description value!");
    }
    logger.trace("first question for section " + patStudyExtended.getStudyDescription());

    if (patStudyExtended.getStudyDescription().equals("consent")) {
      handleConsent(database, patStudyExtended, submitStatus, answerJson);
    }


//    Symptom questions to determine which of the following apply (all Yes/No)
//    Have you experienced any heartburn in the last 1 year? Yes ==> GERD-Q, RSI
//    Do you have any difficulty or pain swallowing? Yes ==> Brief esophageal dysphagia questionnaire, Eckardt score
//    Do you have nausea and a full feeling after eating a little food in the last 1 year? Yes ==> GCSI total score
//    Have any of your activities, relationships, or feelings have been affected by your bladder, bowel, or vaginal symptoms or conditions over the last 3 months? Yes ==> PFIQ 7, PFDI 20

    // determine whether patients receive or skip the part of this survey being processed

    if (patStudyExtended.getStudyDescription().startsWith("GISQGERDQ") ||
        patStudyExtended.getStudyDescription().startsWith("GISQRSI")) {
      if ((patStudy.getContents() == null)  // This is the first question
          && !qualifyForGERDQandRSI(database, patStudyDao, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
    }


    if (patStudyExtended.getStudyDescription().startsWith("GISQBEDQ") ||
        patStudyExtended.getStudyDescription().startsWith("GISQEckardt")) {
      if ((patStudy.getContents() == null)  // This is the first question
          && !qualifyForBEDQandEckardt(database, patStudyDao, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
    }

    if (patStudyExtended.getStudyDescription().startsWith("GISQGCSI")) {
      if ((patStudy.getContents() == null)  // This is the first question
          && !qualifyForGCSI(database, patStudyDao, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
    }

    if (patStudyExtended.getStudyDescription().startsWith("GIPFIQ7") ||
        patStudyExtended.getStudyDescription().startsWith("GIPFDI20")) {
      if ((patStudy.getContents() == null)  // This is the first question
          && !qualifyForPFIQ7andPFDI20(database, patStudyDao, patStudyExtended)) {
        patStudyExtended.setContents(emptyForm);
        patStudyDao.setPatientStudyContents(patStudy, emptyForm, true);
        return null;
      }
    }

    return super.handleResponse(database, patStudyExtended, submitStatus, answerJson);
  }

  private void handleConsent(Database database, PatientStudyExtendedData patStudyExtended,
                             SubmitStatus submitStatus, String answerJson) {
    PatientDao patAttribDao = new PatientDao(database, siteId, ServerUtils.getAdminUser(database));
    ActivityDao activityDao = new ActivityDao(database, patStudyExtended.getSurveySiteId());
    if ("consent".equals(patStudyExtended.getStudyDescription())) {
      if (submitStatus == null) {
        // Handle the initial question
        PatientAttribute consentAttr = patAttribDao.getAttribute(patStudyExtended.getPatientId(), GICustomizer.PATTR_CONSENT);
        String consent = (consentAttr != null) ? consentAttr.getDataValue() : null;
        if ((consent != null) && consent.equals("Y")) {
          addConsentedQuestionnaires(database, patStudyExtended);
        }
      } else {
        FormAnswer formAnswer = AutoBeanCodex.decode(factory, FormAnswer.class, answerJson).as();
        for (FormFieldAnswer fieldAnswer : formAnswer.getFieldAnswers()) {
          String[] ids = fieldAnswer.getFieldId().split(":");
          if (ids.length == 3) {
            // Handle consent response
            if (ids[2].equals("consent")) {
              String choice = fieldAnswer.getChoice().get(0);
              if ("1".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), GICustomizer.PATTR_CONSENT, "Y", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);

                Token tok = new Token();
                Activity pactivity = new Activity(patStudyExtended.getPatientId(), edu.stanford.registry.shared.Constants.ACTIVITY_CONSENTED, tok.getToken());
                activityDao.createActivity(pactivity);
                addConsentedQuestionnaires(database, patStudyExtended);
              } else if ("0".equals(choice)) {
                PatientAttribute pattribute;
                pattribute = new PatientAttribute(patStudyExtended.getPatientId(), GICustomizer.PATTR_CONSENT, "N", PatientAttribute.STRING);
                patAttribDao.insertAttribute(pattribute);

                Token tok = new Token();
                Activity pactivity = new Activity(patStudyExtended.getPatientId(), edu.stanford.registry.shared.Constants.ACTIVITY_DECLINED, tok.getToken());
                activityDao.createActivity(pactivity);
              }
            }
            String email = null;
            if (patStudyExtended.getPatient() != null) {
              email = patStudyExtended.getPatient().getEmailAddress();
            }
            if ((email != null) && (!email.isEmpty())) {
              sendForm(email);
            }

          }
        }
      }
    }
  }

  protected void addConsentedQuestionnaires(Database database, PatientStudy patStudyExtended) {
    AssessDao assessDao = new AssessDao(database, siteInfo);
    String surveyType = assessDao.getSurveyType(patStudyExtended.getToken());
    XMLFileUtils xmlUtils = XMLFileUtils.getInstance(siteInfo);
    String consentedProcess = xmlUtils.getAttribute(surveyType, "optional_questionnaires");
    ArrayList<Element> processList = xmlUtils.getProcessQuestionaires(consentedProcess);
    if (processList != null) {
      for (Element questionaire : processList) {
        // Register the patient in each one
        String qType = questionaire.getAttribute("type");
        SurveyServiceIntf surveyService = SurveyServiceFactory.getFactory(siteInfo).getSurveyServiceImpl(qType);
        if (surveyService == null) {
          throw new ServiceUnavailableException("No service found for type: " + qType);
        }

        try {
          surveyService.registerAssessment(database, questionaire, patStudyExtended.getPatientId(),
              new Token(patStudyExtended.getToken()),
              ServerUtils.getAdminUser(database.get()));
        } catch (Exception ex) {
          logger.error("Error registering qtype: " + qType, ex);
        }
      }
    }
  }

  protected void sendForm(String emailAddr) {
    String templateName = "Send-consent-form";

    try {
      Mailer mailer = siteInfo.getMailer();
      EmailTemplateUtils emailUtils = new EmailTemplateUtils();

      String template = emailUtils.getTemplate(siteInfo, templateName);
      String subject = emailUtils.getEmailSubject(template);
      String body = emailUtils.getEmailBody(template);

      List<File> attachments = new ArrayList<>();
      URL url = getClass().getClassLoader().getResource(CONSENT_RESOURCE);
      if (url == null ) {
        throw new RuntimeException("Did not find resource " + CONSENT_RESOURCE);
      }
      attachments.add(new File(url.getFile()));

      mailer.sendTextWithAttachment(emailAddr, null, null, subject, body, attachments);
    } catch (Exception ex) {
      logger.error("ERROR trying to send " + templateName + " email with " + CONSENT_RESOURCE + " to " + emailAddr, ex);
    }
  }

  //  check if qualify for gastroesophageal reflux disease and reflux symptom index
  private Boolean qualifyForGERDQandRSI(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy) {
    // Find the GISQ screener
    ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArrayList = patStudyDao.getPatientStudyExtendedDataByToken(patientStudy.getToken());
    PatientStudyExtendedData data = null;
    for (PatientStudyExtendedData patientStudyExtendedData : patientStudyExtendedDataArrayList) {
      if (patientStudyExtendedData.getStudyDescription().equals("GISQ")
          || patientStudyExtendedData.getStudyDescription().startsWith("GISQ@")) {
        data = patientStudyExtendedData;
      }
    }
    //Have you experienced any heartburn in the last 1 year? Yes ==> GERD-Q, RSI
    if (data != null) {
      // if response to GISQ question 0 is 1
      return getSelection(database, data, "Order0", "0:0:gisq_heartburn") == 1;
    }
    return false;
  }

  //  check if qualify for brief esophageal dysphagia questionnaire, Eckardt score
  private Boolean qualifyForBEDQandEckardt(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy) {
    // Find the GISQ screener
    ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArrayList = patStudyDao.getPatientStudyExtendedDataByToken(patientStudy.getToken());
    PatientStudyExtendedData data = null;
    for (PatientStudyExtendedData patientStudyExtendedData : patientStudyExtendedDataArrayList) {
      if (patientStudyExtendedData.getStudyDescription().equals("GISQ")
          || patientStudyExtendedData.getStudyDescription().startsWith("GISQ@")) {
        data = patientStudyExtendedData;
      }
    }
//    Do you have any difficulty or pain swallowing? Yes ==> Brief esophageal dysphagia questionnaire, Eckardt score
    if (data != null) {
      // if response to GISQ question 1 is 1
      return getSelection(database, data, "Order1", "1:0:gisq_swallowing_difficulty") == 1;
    }
    return false;
  }

  //  Gastroparesis Cardinal Symptom Index (GCSI): development and validation of a patient reported assessment of severity of gastroparesis symptoms.
  private Boolean qualifyForGCSI(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy) {
    // Find the GISQ screener
    ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArrayList = patStudyDao.getPatientStudyExtendedDataByToken(patientStudy.getToken());
    PatientStudyExtendedData data = null;
    for (PatientStudyExtendedData patientStudyExtendedData : patientStudyExtendedDataArrayList) {
      if (patientStudyExtendedData.getStudyDescription().equals("GISQ")
          || patientStudyExtendedData.getStudyDescription().startsWith("GISQ@")) {
        data = patientStudyExtendedData;
      }
    }
//    Do you have nausea and a full feeling after eating a little food in the last 1 year? Yes ==> GCSI total score
    if (data != null) {
      // if response to GISQ question 2 is 1
      return getSelection(database, data, "Order2", "2:0:gisq_nausea_full") == 1;
    }
    return false;
  }

  //  PFIQ 7, PFDI 20
  private Boolean qualifyForPFIQ7andPFDI20(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudy) {
    // Find the GISQ screener
    ArrayList<PatientStudyExtendedData> patientStudyExtendedDataArrayList = patStudyDao.getPatientStudyExtendedDataByToken(patientStudy.getToken());
    PatientStudyExtendedData data = null;
    for (PatientStudyExtendedData patientStudyExtendedData : patientStudyExtendedDataArrayList) {
      if (patientStudyExtendedData.getStudyDescription().equals("GISQ")
          || patientStudyExtendedData.getStudyDescription().startsWith("GISQ@")) {
        data = patientStudyExtendedData;
      }
    }
//    Have any of your activities, relationships, or feelings have been affected by your bladder, bowel, or vaginal
//    symptoms or conditions over the last 3 months? Yes ==> PFIQ 7, PFDI 20
    if (data != null) {
      // if response to GISQ question 3 is 1
      return getSelection(database, data, "Order3", "3:0:gisq_affected") == 1;
    }
    return false;
  }

  private int getSelection(Database database, PatientStudyExtendedData registration, String question, String fieldId) {
    SurveyQuery surveyQuery = new SurveyQuery(database, new SurveyDao(database), siteInfo.getSiteId());
    Survey survey = surveyQuery.surveyBySurveyToken(registration.getToken());
    SurveyHelper helper = new SurveyHelper(siteInfo);
    String providerId = helper.getProviderId(database, getSurveySystemName());
    return helper.getSelect1Response(survey, providerId, String.valueOf(registration.getStudyCode()), question, fieldId);
  }


  @Override
  public String getSurveySystemName() {
    return SURVEY_SYSTEM_NAME;
  }

  @Override
  public Study registerAssessment(Database database, String name, String title, String explanation) {
    GISQSystem system = GISQSystem.getInstance(getSurveySystemName(), database);
    Study study = new Study(system.getSurveySystemId(), 0, name, 0);
    study.setTitle(title);
    study.setExplanation(explanation);
    SurveySystDao ssDao = new SurveySystDao(database);
    study = ssDao.insertStudy(study);
    return study;
  }

  @Override
  public SurveySystem getSurveySystem(Database database, String qType) {
    return GISQSystem.getInstance(getSurveySystemName(), database);
  }

  @Override
  public void registerAssessment(Database database, Element questionaire, String patientId,
                                 Token tok, User user) throws ServiceUnavailableException {
    if (patientId == null) {
      return;
    }

    GISQSystem system = GISQSystem.getInstance(getSurveySystemName(), database);
    String qOrder = questionaire.getAttribute(Constants.XFORM_ORDER);
    Integer order = Integer.valueOf(qOrder);

    // Get the study
    String studyName = questionaire.getAttribute("value");
    SurveySystDao ssDao = new SurveySystDao(database);
    Study study = ssDao.getStudy(system.getSurveySystemId(), studyName);

    // Add the study if it doesn't exist
    if (study == null) {
      logger.debug("Study " + studyName + " doesn't exist, CREATING ");
      study = registerAssessment(database, studyName, "", "");
    }

    // Get the patient and this study for this patient
    PatientDao patientDao = new PatientDao(database, siteId, user);
    Patient pat = patientDao.getPatient(patientId);
    PatStudyDao patStudyDao = new PatStudyDao(database, siteInfo);
    PatientStudy patStudy = patStudyDao.getPatientStudy(pat, study, tok);

    if (patStudy == null) { // not there yet so lets add it
      patStudy = new PatientStudy(this.siteId);
      patStudy.setExternalReferenceId("");
      patStudy.setMetaVersion(0);
      patStudy.setPatientId(pat.getPatientId());
      patStudy.setStudyCode(study.getStudyCode());
      patStudy.setSurveySystemId(study.getSurveySystemId());
      patStudy.setToken(tok.getToken());
      patStudy.setOrderNumber(order);
      patStudyDao.insertPatientStudy(patStudy);
    }

  }

  @SuppressWarnings("SameReturnValue")
  private NextQuestion stopQuestions(Database database, PatStudyDao patStudyDao, PatientStudyExtendedData patientStudyExtendedData,
                                     SubmitStatus submitStatus, String answerJson) {
    super.handleResponse(database, patientStudyExtendedData, submitStatus, answerJson); // record the answer
    PatientStudy patStudy = patStudyDao.getPatientStudy(patientStudyExtendedData, true); // get updated
    patStudyDao.setPatientStudyContents(patStudy, patStudy.getContents(), true);
    return null;
  }

  static private class SurveyHelper extends SurveyAdvanceBase {

    SurveyHelper(SiteInfo siteInfo) {
      super(siteInfo);
    }

    @Override
    public Integer getSelect1Response(Survey s, String provider, String section, String questionId, String fieldId) {
      logger.debug("looking for provider {}, section {}, questionId {}, fieldId {}", provider, section, questionId, fieldId);
      SurveyStep step = s.answeredStepByProviderSectionQuestion(provider, section, questionId);
      // if the step or step answer for this question id isn't found check if it's been stored within another question
      // This happens when conditional questions are shown on the same page as the prior question
      if (step != null) {
        Integer value = selectedFieldInt(step, fieldId);
        logger.debug("returning " + (value == null ? 0 : value));
        return value == null ? 0 : value;
      } else {
        logger.debug("returning a 0");
        return 0;
      }
    }
  }

//  GISQScoreProvider to calculate scores for GI related surveys

  private static class GISQScoreProvider extends ExtensibleScoreProvider {

    GISQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    public ArrayList<ChartScore> getScore(PatientStudyExtendedData patientData) {
      String studyName = patientData.getStudyDescription();
      if (studyName.contains("@")) {
        int inx = studyName.indexOf("@");
        studyName = studyName.substring(0, inx);
      }
      switch (studyName) {
      case "GISQGERDQ":
        return getTDScore(patientData);
      case "GIPFDI20":
        return getPFDI20Score(patientData);
      case "GIPFIQ7":
        return getPFIQ7Score(patientData);
      default:
        break;
      }
      return new ArrayList<>();
    }

    @Override
    public Table getTable(ArrayList<PatientStudyExtendedData> patientStudies,
                          PrintStudy printStudy, Patient patient) {
      Table table = new Table();
      table.setBorder(2);

      if (printStudy == null || printStudy.getStudyDescription() == null) {
        return table;
      }
      /* Find the last one */
      PatientStudyExtendedData patStudy = null;
      for (PatientStudyExtendedData patientStudy : patientStudies) {
        if (patientStudy.getStudyCode().intValue() == printStudy.getStudyCode().intValue()) {
          patStudy = patientStudy;
        }
      }
      if (patStudy == null) {
        return table;
      }
      if (patStudy.getContents() == null || patStudy.getContents().length() < 100) {
        return table;
      }
      String studyDesc = printStudy.getStudyDescription();

      if (studyDesc.indexOf("@") > 0) {
        studyDesc = studyDesc.substring(0, printStudy.getStudyDescription().indexOf("@"));
      }
      ArrayList<ChartScore> scoreArrayList = new ArrayList<>();
      switch (studyDesc) {
      case ("GISQGERDQ"):
        scoreArrayList = getTDScore(patStudy);
        if (scoreArrayList.size() > 0) {
          ChartScore score = scoreArrayList.get(0);
          table.addHeading("GERDQ SCORE");
          TableRow ghHeadingRow = new TableRow(500);
          ghHeadingRow.addColumn(new TableColumn("Date", 20));
          ghHeadingRow.addColumn(new TableColumn("GERDQ Score", 20));
          ghHeadingRow.addColumn(new TableColumn("%ile", 20));
          table.addRow(ghHeadingRow);

          int totalScore = score.getScore().intValue();
          int likelihood = 0;
//          https://www.aafp.org/afp/2010/0515/p1278.html
//          note: Add the point values for each corresponding answer.
//          Total score of 0 to 2 points = 0 percent likelihood of GERD; 3 to 7 points = 50 percent likelihood;
//          8 to 10 points = 79 percent likelihood; 11 to 18 points = 89 percent likelihood.
          if (totalScore <= 2 && totalScore >= 0) {
            likelihood = 0;
          } else if (totalScore >= 3 && totalScore <= 7) {
            likelihood = 50;
          } else if (totalScore >= 8 && totalScore <= 10) {
            likelihood = 79;
          } else if (totalScore >= 11 && totalScore <= 18) {
            likelihood = 89;
          }

          TableRow ghRow = new TableRow(100);
          ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
          ghRow.addColumn(new TableColumn(String.valueOf(score.getScore()), 20));
          ghRow.addColumn(new TableColumn(""+likelihood,  20));

          table.addRow(ghRow);
        }
        break;
      case ("GIPFDI20"):
        scoreArrayList = getPFDI20Score(patStudy);
        if (scoreArrayList.size() > 0) {
          ChartScore score = scoreArrayList.get(0);
          table.addHeading("PFDI20 SCORE");
          TableRow ghHeadingRow = new TableRow(500);
          ghHeadingRow.addColumn(new TableColumn("Date", 20));
          ghHeadingRow.addColumn(new TableColumn("PFDI20 Score", 20));
          table.addRow(ghHeadingRow);

//          Scoring the PFDI-20
//          Pelvic Organ prolapse Distress Inventory 6 (POPDI-6)
//          Colorectal-Anal distress Inventory 8 (CRAD-8) question 7-14
//          Urinary distress Inventory 6 (UDI-6) question 15-20
//          Scale Scores: Obtain the mean value of all of the answered items within the corresponding scale (possible value 0 to 4)
//          and then multiply by 25 to obtain the scale score (range 0 to 100). Missing items are dealt with by using the mean from
//          answered items only.
          int totalScore = score.getScore().intValue()/20 * 25;
          TableRow ghRow = new TableRow(100);
          ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
          ghRow.addColumn(new TableColumn(String.valueOf(totalScore), 20));

          table.addRow(ghRow);
        }
        break;
      case ("GIPFIQ7"):
        scoreArrayList = getPFIQ7Score(patStudy);
        if (scoreArrayList.size() > 0) {
          ChartScore score = scoreArrayList.get(0);
          table.addHeading("GIPFIQ7 SCORE");
          TableRow ghHeadingRow = new TableRow(500);
          ghHeadingRow.addColumn(new TableColumn("Date", 20));
          ghHeadingRow.addColumn(new TableColumn("GIPFIQ7 Score", 20));
          table.addRow(ghHeadingRow);

          int totalScore = score.getScore().intValue()/21 * 100/3;
          TableRow ghRow = new TableRow(100);
          ghRow.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 20));
          ghRow.addColumn(new TableColumn(String.valueOf(totalScore), 20));

          table.addRow(ghRow);
        }
        break;

      default:
        break;
      }
      return table;
    }

    private ArrayList<ChartScore> getScoreByFieldIds(PatientStudyExtendedData patientData, String[] fieldIds) {
      ArrayList<ChartScore> scoreArray = new ArrayList<>();
      if (fieldIds == null || fieldIds.length == 0)
        return scoreArray;
      Survey s = getSurvey(patientData);
      SurveyHelper helper = new SurveyHelper(siteInfo);
      String providerId = helper.getProviderId(dbp.get(), SURVEY_SYSTEM_NAME);
      int intScore = 0;
      for (int r = 0; r < fieldIds.length; r++) {
        int tmp = helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order" + r,
            r + ":" + 0 + fieldIds[r]);
        logger.debug("ref: " + fieldIds[r] + " score: " + tmp  + " token: " + s.getSurveyTokenId());
        intScore = intScore + tmp;
      }

      LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode()
          , patientData.getStudyDescription());
      localScore.setAnswer(fieldIds.length, new BigDecimal(intScore));
      scoreArray.add(localScore);
      return scoreArray;
    }

    private ArrayList<ChartScore> getTDScore(PatientStudyExtendedData patientData) {
      String[] fieldIds = { ":GISQ_GERDQ_1", ":GISQ_GERDQ_2", ":GISQ_GERDQ_3", ":GISQ_GERDQ_4", ":GISQ_GERDQ_5", ":GISQ_GERDQ_6" };
      return  getScoreByFieldIds(patientData, fieldIds);
    }

    private ArrayList<ChartScore> getPFDI20Score(PatientStudyExtendedData patientData) {
      //gi_pfdi20_1  //need to be in 3 groups
      String[] fieldIds = { ":gi_pfdi20_1", ":gi_pfdi20_2", ":gi_pfdi20_3", ":gi_pfdi20_4", ":gi_pfdi20_5", ":gi_pfdi20_6",
          ":gi_pfdi20_7", ":gi_pfdi20_8", ":gi_pfdi20_9", ":gi_pfdi20_10", ":gi_pfdi20_11", ":gi_pfdi20_12", ":gi_pfdi20_13", ":gi_pfdi20_14",
          ":gi_pfdi20_15", ":gi_pfdi20_16", ":gi_pfdi20_17", ":gi_pfdi20_18", ":gi_pfdi20_19", ":gi_pfdi20_20"

      };
      ArrayList<ChartScore> scoreArray = new ArrayList<>();
      if (fieldIds == null || fieldIds.length == 0)
        return scoreArray;
      Survey s = getSurvey(patientData);
      SurveyHelper helper = new SurveyHelper(siteInfo);
      String providerId = helper.getProviderId(dbp.get(), SURVEY_SYSTEM_NAME);
      int intScore = 0;
      for (int r = 1; r < fieldIds.length; r++) {
        int tmp = helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order" + r,
            r + ":" + 0 + fieldIds[r-1]);
        logger.debug("ref: " + fieldIds[r-1] + " score: " + tmp  + " token: " + s.getSurveyTokenId());
        intScore = intScore + tmp;
      }

      LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode()
          , patientData.getStudyDescription());
      localScore.setAnswer(fieldIds.length, new BigDecimal(intScore));
      scoreArray.add(localScore);
      return scoreArray;
    }

    private ArrayList<ChartScore> getPFIQ7Score(PatientStudyExtendedData patientData) {
      String[] fieldIds = { ":gi_pfiq7_1_1", ":gi_pfiq7_1_2", ":gi_pfiq7_1_3",
          ":gi_pfiq7_2_1", ":gi_pfiq7_2_2", ":gi_pfiq7_2_3",
          ":gi_pfiq7_3_1", ":gi_pfiq7_3_2", ":gi_pfiq7_3_3",
          ":gi_pfiq7_4_1", ":gi_pfiq7_4_2", ":gi_pfiq7_4_3",
          ":gi_pfiq7_5_1", ":gi_pfiq7_5_2", ":gi_pfiq7_5_3",
          ":gi_pfiq7_6_1", ":gi_pfiq7_6_2", ":gi_pfiq7_6_3",
          ":gi_pfiq7_7_1", ":gi_pfiq7_7_2", ":gi_pfiq7_7_3"
      };
      ArrayList<ChartScore> scoreArray = new ArrayList<>();
      Survey s = getSurvey(patientData);
      SurveyHelper helper = new SurveyHelper(siteInfo);
      String providerId = helper.getProviderId(dbp.get(), SURVEY_SYSTEM_NAME);
      int intScore = 0;
      for (int r = 1; r <= 7; r++) {
        for(int c =0; c <3; c++) {
          int tmp = helper.getSelect1Response(s, providerId, patientData.getStudyCode().toString(), "Order" + r,
              r + ":" + c + fieldIds[(r-1)*3 + c]);
          logger.debug("ref: " + fieldIds[(r-1)*3 + c] + " score: " + tmp + " : " + s.toString());
          intScore = intScore + tmp;
        }
      }

      LocalScore localScore = new LocalScore(patientData.getDtChanged(), patientData.getPatientId(), patientData.getStudyCode()
          , patientData.getStudyDescription());
      localScore.setAnswer(fieldIds.length, new BigDecimal(intScore));
      scoreArray.add(localScore);
      return scoreArray;
    }

    Survey getSurvey(PatientStudyExtendedData patientData) {
      SurveyDao surveyDao = new SurveyDao(dbp.get());
      SurveyQuery query = new SurveyQuery(dbp.get(), surveyDao, patientData.getSurveySiteId());
      return query.surveyBySurveyToken(patientData.getToken());
    }


  }
  static class ACEScoreProvider extends SumScoreProvider {
    private static final List<String> refs = Arrays.asList("GI_ACE_1_1", "GI_ACE_1_2", "GI_ACE_1_3", "GI_ACE_1_4", "GI_ACE_1_5", "GI_ACE_1_6", "GI_ACE_1_7", "GI_ACE_1_8", "GI_ACE_1_9", "GI_ACE_1_10");
    public ACEScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName, refs);
    }
  }
  static class GCSIScoreProvider extends AverageScoreProvider {
    private static final List<String> refs = Arrays.asList("GISQ_GCSI_1", "GISQ_GCSI_2", "GISQ_GCSI_3",
        "GISQ_GCSI_4", "GISQ_GCSI_5", "GISQ_GCSI_6");
    public GCSIScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName, refs);
    }
  }
  static class BEDQScoreProvider extends SumScoreProvider {
    private static final List<String> refs = Arrays.asList("GI_BEDQ_1",
        "GI_BEDQ_2", "GI_BEDQ_3", "GI_BEDQ_4", "GI_BEDQ_5", "GI_BEDQ_6",
        "GI_BEDQ_7", "GI_BEDQ_8", "GI_BEDQ_9", "GI_BEDQ_10");
    public BEDQScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName, refs);
    }
  }
  static class EckardtScoreProvider extends SumScoreProvider {
    private static final List<String> refs = Arrays.asList("GI_Eck_1",
        "GI_Eck_2", "GI_Eck_3", "GI_Eck_4", "GI_Eck_4_1", "GI_Eck_4_2");
    public EckardtScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName, refs);
    }
  }
  static class QRSIScoreProvider extends SumScoreProvider {
    private static final List<String> rsiRefs = Arrays.asList("GI_RSI_1",
        "GI_RSI_2", "GI_RSI_3", "GI_RSI_4", "GI_RSI_5", "GI_RSI_6",
        "GI_RSI_7", "GI_RSI_8", "GI_RSI_9");
    public QRSIScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName, rsiRefs);
    }
  }
  static class CIRSScoreProvider extends SumScoreProvider {
    private static final List<String> refs = Arrays.asList("gi_cirs_cardiac", "gi_cirs_vascular", "gi_cirs_respiratory", "gi_cirs_eent", "gi_cirs_upperGI", "gi_cirs_lowerGI", "gi_cirs_hepatic", "gi_cirs_renal", "gi_cirs_otherGU", "gi_cirs_msi",
    "gi_cirs_neurological", "gi_cirs_psychiatric", "gi_cirs_endocrine");
    public CIRSScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName, refs);
    }
  }

  /**
   * Score provider for GI Promis since no CAT available for these six scales
   */
  static class GIPromisScoreProvider extends ExtensibleScoreProvider {

    private static final String[] bellyPainQuestions = new String[] {
        "bp_often", "bp_rate", "bp_activities", "bp_bother", "bp_discomfort"};
    private static final String[] constipationQuestions = new String[] {
        "cp_5_1","cp_6_2","cp_7_3","cp_8_4","cp_9_5","cp_10_6","cp_11_7","cp_12_8","cp_13_9"};
    private static final String[] diarrheaQuestions = new String[] {
        "dia_14_1","dia_15_2","dia_16_3","dia_17_4","dia_18_5","dia_19_6"};
    private static final String[] gasQuestions = new String[] {
        "gas_20_1","gas_21_2","gas_22_3","gas_23_4","gas_24_5","gas_25_6","gas_26_7","gas_27_8","gas_28_9","gas_29_10","gas_30_11","gas_31_12","gas_32_13"};
    private static final String[] nauseaQuestions = new String[] {
        "nv_33_1","nv_34_2","nv_35_3","nv_36_4"};
    private static final String[] refluxQuestions = new String[] {
        "rf_37_1","rf_38_2","rf_39_3","rf_40_4","rf_41_5","rf_42_6","rf_43_7","rf_44_8","rf_45_9","rf_46_10","rf_47_11","rf_48_12","rf_49_13"};

    public GIPromisScoreProvider(Supplier<Database> dbp, SiteInfo siteInfo, String studyName) {
      super(dbp, siteInfo, studyName);
    }

    @Override
    protected ChartScore getScoreInternal(PatientStudyExtendedData patientData) {
      LocalScore localScore = new GISQSurveyService.GIPromisScore(patientData.getDtChanged(), patientData.getPatientId(),
          patientData.getStudyCode(), patientData.getStudyDescription());
      localScore.setAssisted(patientData.wasAssisted());

      loadAnswers(localScore, patientData);
      calculateScore(localScore, patientData);

      return localScore;
    }

    @Override
    protected void calculateScore(LocalScore chartScore, PatientStudyExtendedData patientData) {
      // BellyPain score
      int bellyPainAnswers = 0;
      int bellyPainSum = 0;
      logger.info("gi_score_map-> " + gi_score_map);
      for(String question : bellyPainQuestions) {
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          bellyPainAnswers = bellyPainAnswers + 1;
          bellyPainSum = bellyPainSum + ans;
        }
      }
      logger.info("bellyPainAnswers " + bellyPainAnswers + " bellyPainSum " + bellyPainSum);
      Double bellyPainScore = null;
      if (bellyPainAnswers == 5) {
        String tScoreSe = gi_score_map.get(BELLYPAIN_5 + bellyPainSum);
        logger.info("tScoreSe" + tScoreSe);

        if(tScoreSe != null &&!tScoreSe.isEmpty()){
          TScoreSEPair pair = new TScoreSEPair(tScoreSe);
          bellyPainScore = pair.getTScore();
        } else {
          logger.info("bellyPainSum not found in lookup table: ", bellyPainSum);
        }
      }

      // Constipation score
      int constipationAnswers = 0;
      int constipationSum = 0;
      for(String question : constipationQuestions) {
        logger.info("check cp question " + question);
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          logger.info("cp answer " + ans);
          constipationAnswers = constipationAnswers + 1;
          constipationSum = constipationSum + ans;
        }
      }
      logger.info("constipationQuestions " + constipationAnswers + " constipationSum " + constipationSum);

      Double constipationScore = null;
      if (constipationAnswers == 9) {
        if(constipationSum < 12) {
          constipationSum = 12;
        }
        String tScoreSe = gi_score_map.get(CONSTIPATION_9 + constipationSum);
        logger.info("tScoreSe" + tScoreSe);

        if(tScoreSe != null &&!tScoreSe.isEmpty()){
          TScoreSEPair pair = new TScoreSEPair(tScoreSe);
          constipationScore = pair.getTScore();
        } else {
          logger.info("constipationSum not found in lookup table: ", constipationSum);
        }
      }

      // Diarrhea score
      int diarrheaAnswers = 0;
      int diarrheaSum = 0;
      for(String question : diarrheaQuestions) {
        logger.info("check diarrhea question " + question);
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          logger.info("answer " + ans);
          if (ans < 99) {
            diarrheaAnswers = diarrheaAnswers + 1;
            diarrheaSum = diarrheaSum + ans;
          }
        }
      }
      Double diarrheaScore = null;
      if (diarrheaAnswers == 6) {
        if(diarrheaSum < 8) {
          diarrheaSum = 8;
        }
        String tScoreSe = gi_score_map.get(DIARRHEA_6 + diarrheaSum);
        logger.info("tScoreSe" + tScoreSe);

        if(tScoreSe != null &&!tScoreSe.isEmpty()){
          TScoreSEPair pair = new TScoreSEPair(tScoreSe);
          diarrheaScore = pair.getTScore();
        } else {
          logger.info("diarrheaSum not found in lookup table:" + diarrheaSum);
        }
      }

      // Gas and Bloating score
      //gas_20_1 need to ignore value, 99 or 100

      int gasAnswers = 0;
      int gasSum = 0;
      for(String question : gasQuestions) {
        logger.info("check gas question " + question);
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          logger.info("answer " + ans);
          gasAnswers = gasAnswers + 1;
          //questin gas_31_12, the value for choice 4, and 5 should all be 4
          if (question.equals("gas_31_12") && ans == 5) {
            ans = 4;
          }
          if (ans < 99) {
            gasSum = gasSum + ans;
          }
        }
      }
      Double gasScore = null;
      if (gasAnswers == 13) {
        if(gasSum < 14) {
          gasSum = 14;
        }
        String tScoreSe = gi_score_map.get(GASBLOATING_13 + gasSum);
        logger.info("tScoreSe" + tScoreSe);
        if(tScoreSe != null &&!tScoreSe.isEmpty()){
          TScoreSEPair pair = new TScoreSEPair(tScoreSe);
          gasScore = pair.getTScore();
        } else {
          logger.info("gasSum not found in lookup table:" + gasSum);
        }
      }
      //  nausea Score
      int nauseaAnswers = 0;
      int nauseaSum = 0;
      for(String question : nauseaQuestions) {
        logger.info("check nausea question " + question);
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          logger.info("answer " + ans);
          if (ans < 99) {
            nauseaAnswers = nauseaAnswers + 1;
            nauseaSum = nauseaSum + ans;
          }
        }
      }
      Double nauseaScore = null;
      if (nauseaAnswers == 4) {
        if(nauseaSum < 5) {
          nauseaSum = 5;
        }
        String tScoreSe = gi_score_map.get(NauseaVomiting_4 + nauseaSum);
        logger.info("tScoreSe" + tScoreSe);

        if(tScoreSe != null &&!tScoreSe.isEmpty()){
          TScoreSEPair pair = new TScoreSEPair(tScoreSe);
          nauseaScore = pair.getTScore();
        } else {
          logger.info("nauseaSum not found in lookup table:" + nauseaSum);
        }
      }
      // Reflux Score
      int refluxAnswers = 0;
      int refluxSum = 0;
      for(String question : refluxQuestions) {
        logger.info("check reflux question " + question);
        if (chartScore.isAnswered(question)) {
          int ans = chartScore.getAnswer(question).intValue();
          logger.info("answer " + ans);
          if (ans < 99) {
            refluxAnswers = refluxAnswers + 1;
            refluxSum = refluxSum + ans;
          }
        }
      }
      Double refluxScore = null;
      if (refluxAnswers == 13) {
        if(refluxSum < 16){
          refluxSum = 16;
        }
        String tScoreSe = gi_score_map.get(Reflux_13 + refluxSum);
        logger.info("tScoreSe" + tScoreSe);
        if(tScoreSe != null &&!tScoreSe.isEmpty()){
          TScoreSEPair pair = new TScoreSEPair(tScoreSe);
          refluxScore = pair.getTScore();
        } else {
          logger.info("refluxSum not found in lookup table:" + refluxSum);
        }
      }

      // Total score
      Double totalScore = (bellyPainScore !=null ? bellyPainScore : 0.0)
          + (constipationScore != null ? bellyPainScore : 0.0)
          + (diarrheaScore != null ? diarrheaScore : 0.0)
          + (gasScore != null ? gasScore : 0.0)
          + (refluxScore != null ? refluxScore : 0.0)
          + (nauseaScore != null ? nauseaScore : 0.0);


      Map<String,BigDecimal> scores = new LinkedHashMap<>();
      if (bellyPainScore != null) {
        scores.put("BellyPain", new BigDecimal(bellyPainScore));
      }
      if (constipationScore != null) {
        scores.put("Constipation", new BigDecimal(constipationScore));
      }
      if (diarrheaScore != null) {
        scores.put("Diarrhea", new BigDecimal(diarrheaScore));
      }
      if (gasScore != null) {
        scores.put("Gas", new BigDecimal(gasScore));
      }
      if (nauseaScore != null) {
        scores.put("Nausea", new BigDecimal(nauseaScore));
      }
      if (refluxScore != null) {
        scores.put("Reflux", new BigDecimal(refluxScore));
      }
//      if (totalScore != null) {
//        scores.put("Total", new BigDecimal(totalScore));
//      }
      chartScore.setScores(scores);
    }

    @Override
    protected Table getTableInternal(PrintStudy study, ArrayList<ChartScore> scores, boolean headerRow) {
      Table table = new Table();
      if ((scores == null) || (scores.size() < 1) || (scores.get(0) == null)) {
        return table;
      }

      table.addHeading("PROMIS GI Score");

      TableRow colHeader = new TableRow(100);
      colHeader.addColumn(new TableColumn("Date", 11));
      colHeader.addColumn(new TableColumn("Belly Pain", 10));
      colHeader.addColumn(new TableColumn("Constipation", 13));
      colHeader.addColumn(new TableColumn("Diarrhea", 10));
      colHeader.addColumn(new TableColumn("Gas Bloating", 15));
      colHeader.addColumn(new TableColumn("Nausea Vomiting", 16));
      colHeader.addColumn(new TableColumn("Reflux", 10));
//      colHeader.addColumn(new TableColumn("Total", 10));

      table.addRow(colHeader);

      for (ChartScore score : scores) {
        TableRow row = new TableRow(100);
        row.setColumnGap(3);
        row.addColumn(new TableColumn(dateFormatter.format(score.getDate()), 11));
        Map<String,BigDecimal> scoreValues = score.getScores();
        BigDecimal bellyPainScore = scoreValues.get("BellyPain");
        if (bellyPainScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(bellyPainScore), 10));
        } else {
          row.addColumn(new TableColumn("", 10));
        }
        BigDecimal constipationScore = scoreValues.get("Constipation");
        if (constipationScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(constipationScore), 13));
        } else {
          row.addColumn(new TableColumn("", 13));
        }
        BigDecimal diarrheaScore = scoreValues.get("Diarrhea");
        if (diarrheaScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(diarrheaScore), 10));
        } else {
          row.addColumn(new TableColumn("", 10));
        }
        BigDecimal gasScore = scoreValues.get("Gas");
        if (gasScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(gasScore), 15));
        } else {
          row.addColumn(new TableColumn("", 15));
        }
        BigDecimal nvScore = scoreValues.get("Nausea");
        if (nvScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(nvScore), 16));
        } else {
          row.addColumn(new TableColumn("", 16));
        }
        BigDecimal rfScore = scoreValues.get("Reflux");
        if (rfScore != null) {
          row.addColumn(new TableColumn(scoreFormatter.format(rfScore), 10));
        } else {
          row.addColumn(new TableColumn("", 10));
        }
//        BigDecimal totalScore = scoreValues.get("Total");
//        if (totalScore != null) {
//          row.addColumn(new TableColumn(scoreFormatter.format(totalScore), 10));
//        } else {
//          row.addColumn(new TableColumn("", 10));
//        }
        table.addRow(row);
      }

      return table;
    }

    @Override
    public XYDataset getTimeSet(TimeSeries baseLineSeries, ArrayList<ChartScore> scores,
                                PrintStudy study, ChartConfigurationOptions opts) {
      final TimeSeriesCollection dataset = new TimeSeriesCollection();
      dataset.addSeries(baseLineSeries);
      if (scores == null || study == null) {
        return dataset;
      }

      TimeSeries timeDataSet1 = new TimeSeries("BellyPain");
      TimeSeries timeDataSet2 = new TimeSeries("Constipation");
      TimeSeries timeDataSet3 = new TimeSeries("Diarrhea");
      TimeSeries timeDataSet4 = new TimeSeries("Gas");
      TimeSeries timeDataSet5 = new TimeSeries("Nausea");
      TimeSeries timeDataSet6 = new TimeSeries("Reflux");
      for (ChartScore score : scores) {
        Map<String,BigDecimal> scoreValues = score.getScores();

        Day day = new Day(score.getDate());
        BigDecimal function = scoreValues.get("BellyPain");
        if (function != null) {
          timeDataSet1.addOrUpdate(day, function);
        }
        BigDecimal pain = scoreValues.get("Constipation");
        if (pain != null) {
          timeDataSet2.addOrUpdate(day, pain);
        }
        BigDecimal diarrhea = scoreValues.get("Diarrhea");
        if (diarrhea != null) {
          timeDataSet3.addOrUpdate(day, diarrhea);
        }
        BigDecimal gas = scoreValues.get("Gas");
        if (gas != null) {
          timeDataSet4.addOrUpdate(day, gas);
        }
        BigDecimal nausea = scoreValues.get("Nausea");
        if (nausea != null) {
          timeDataSet5.addOrUpdate(day, nausea);
        }
        BigDecimal reflux = scoreValues.get("Reflux");
        if (reflux != null) {
          timeDataSet6.addOrUpdate(day, reflux);
        }
      }
      dataset.addSeries(timeDataSet1);
      dataset.addSeries(timeDataSet2);
      dataset.addSeries(timeDataSet3);
      dataset.addSeries(timeDataSet4);
      dataset.addSeries(timeDataSet5);
      dataset.addSeries(timeDataSet6);
      return dataset;
    }

    @Override
    public XYPlot getPlot(XYErrorRenderer renderer, XYDataset ds, ArrayList<Study> studies,
                          ChartConfigurationOptions opts) {
      final NumberAxis rangeAxis = new NumberAxis("");
      if (studies == null || studies.size() < 1 || ds == null) {
        return new XYPlot(ds, new DateAxis(), rangeAxis, new XYErrorRenderer());
      }
      TimeSeriesCollection collection = (TimeSeriesCollection) ds;
      final CustomDateAxis domainAxis = new CustomDateAxis(collection);
      Font labelFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.LABELFONTSIZE);
      Font dtTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.DTTICKFONTSIZE);
      domainAxis.setLabelFont(labelFont);
      domainAxis.setTickLabelPaint(Color.black);
      domainAxis.setTickLabelFont(dtTickFont);
      domainAxis.setTickLabelInsets(new RectangleInsets(5, 60, 5, 5));
      Font numTickFont = ChartMaker.getFont(opts, Font.PLAIN, ChartMaker.NUMTICKFONTSIZE);
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelFont(labelFont);
      rangeAxis.setTickLabelFont(numTickFont);
      rangeAxis.setTickUnit(new NumberTickUnit(1));
      rangeAxis.setRange(0, 5);
      ChartMaker.setRendererOptions(renderer, opts, ds.getSeriesCount());
      final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);
      plot.setDomainGridlinePaint(Color.black);
      plot.setDomainGridlinesVisible(false);
      plot.setRangeGridlinePaint(Color.black);
      plot.setBackgroundPaint(Color.white);
      plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
      plot.setOutlineStroke(new BasicStroke(0.0f));
      plot.setInsets(ChartMaker.CHART_INSETS);

      return plot;
    }
  }

  /**
   * Chart score object for GI Promis
   */
  static class GIPromisScore extends LocalScore implements MultiScore {

    public GIPromisScore(Date dt, String patientId, Integer studyCode, String description) {
      super(dt, patientId, studyCode, description);
    }

    @Override
    public int getNumberOfScores() {
      return 7;
    }

    @Override
    public String getTitle(int scoreNumber, String studyDescription) {
      switch(scoreNumber) {
      case 1:
        return studyDescription + " - BellyPain";
      case 2:
        return studyDescription + " - Constipation";
      case 3:
        return studyDescription + " - Diarrhea";
      case 4:
        return studyDescription + " - Gas and Bloating";
      case 5:
        return studyDescription + " - Nausea and Vomiting";
      case 6:
        return studyDescription + " - Reflux";
      case 7:
        return studyDescription + " - Total";
      default:
        return studyDescription;
      }
    }
    @Override
    public double getScore(int scoreNumber) {
      Map<String,BigDecimal> scoreValues = getScores();
      BigDecimal score;
      switch(scoreNumber) {
      case 1:
        score = scoreValues.get("BellyPain");
        break;
      case 2:
        score = scoreValues.get("Constipation");
        break;
      case 3:
        score = scoreValues.get("Diarrhea");
        break;
      case 4:
        score = scoreValues.get("Gas");
        break;
      case 5:
        score = scoreValues.get("Nausea");
        break;
      case 6:
        score = scoreValues.get("Reflux");
        break;
      case 7:
        score = scoreValues.get("Total");
        break;
      default:
        score = null;
      }
      return (score != null) ? score.doubleValue() : 0;
    }
    @Override
    public Double getPercentileScore(int scoreNumber) {
      return null;
    }
  }
  static class TScoreSEPair {
    @Override
    public String toString() {
      return
          tScore +
              "SE " + se;
    }
    public TScoreSEPair(String string) {
      if(string == null || string.isEmpty()) {
        string = "0.0,0.0";
      }
      final StringTokenizer tok = new StringTokenizer(string, ",", false);
      tScore = Double.parseDouble(tok.nextToken().trim());
      se = Double.parseDouble(tok.nextToken().trim());
    }
    Double getTScore() {
      return tScore;
    }
    Double getSe() {
      return se;
    }

    Double tScore;
    Double se;
  }

}


class GISQSystem extends SurveySystem {

  private static final long serialVersionUID = -1;

  @SuppressWarnings({ "StaticVariableOfConcreteClass", "StaticNonFinalField" })
  private static GISQSystem me = null;

  private GISQSystem(SurveySystem ssys) throws DataException {
    this.copyFrom(ssys);
  }

  public static GISQSystem getInstance(String surveySystemName, Database database) throws DataException {
    if (me == null) {
      SurveySystDao ssDao = new SurveySystDao(database);
      SurveySystem ssys = ssDao.getOrCreateSurveySystem(surveySystemName, null);
      me = new GISQSystem(ssys);
    }
    return me;
  }

}



