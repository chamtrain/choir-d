/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.server.survey;

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.database.PatStudyDao;
import edu.stanford.registry.server.database.PatientDao;
import edu.stanford.registry.server.database.SurveySystDao;
import edu.stanford.registry.server.plugin.ScoreProvider;
import edu.stanford.registry.shared.ChartScore;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.GlobalHealthScore;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientAttribute;
import edu.stanford.registry.shared.PatientStudyExtendedData;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveyRegistration;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.server.Survey;
import edu.stanford.survey.server.SurveyAdvance;
import edu.stanford.survey.server.SurveyAdvanceHandler;
import edu.stanford.survey.server.SurveyAdvanceMonitor;
import edu.stanford.survey.server.SurveyAdvancePush;
import edu.stanford.survey.server.SurveyDao;
import edu.stanford.survey.server.SurveyQuery;
import edu.stanford.survey.server.SurveyStep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Schema;
import com.github.susom.database.Sql;
import com.github.susom.database.SqlInsert;

/**
 * Populate a square table of results upon completion of each survey.
 */
public class SurveyAdvanceHandlerSquareTable implements SurveyAdvanceHandler {
  private static final Logger log = Logger.getLogger(SurveyAdvanceHandlerSquareTable.class);
  private HashMap<String, String> providers = new HashMap<>();
  private HashMap<String, String> studies = new HashMap<>();
  SiteInfo siteInfo;

  SurveyAdvanceHandlerSquareTable(SiteInfo siteInfo) {
    this.siteInfo = siteInfo;
  }

  @Override
  public boolean surveyAdvanced(SurveyAdvance survey, Supplier<Database> dbp) {
    Long surveyRegId = getSurveyRegistrationId(survey, dbp);
    if (surveyRegId == null) {
      log.debug("surveyRegId not found for siteId " + survey.getSurveySiteId() + " token_id "
          + survey.getSurveyTokenId());
      return false;
    }

    Database db = dbp.get();
    AssessDao assessDao = new AssessDao(db, siteInfo);
    SurveyRegistration registration = assessDao.getSurveyRegistrationByRegId(surveyRegId);
    if (registration == null) {
      throw new RuntimeException("no registration for siteId " + surveyRegId + " token_id " + surveyRegId);
    }

    boolean exists = db.toSelect("select 'Y' from rpt_pain_std_surveys_square where survey_site_id=? and survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryBooleanOrFalse();

    try {
      Sql sql = new Sql();
      int nbrArgs = 0;
      String separator;
      if (exists) {
        sql.listStart("update rpt_pain_std_surveys_square set ");
        separator = "=?, ";
      } else {
        sql.listStart("insert into rpt_pain_std_surveys_square (survey_site_id,survey_token_id,")
            .argLong(survey.getSurveySiteId())
            .argLong(survey.getSurveyTokenId());
        separator = ",";
        nbrArgs += 2;
      }

      // Metadata for the assessment
      SurveyDao surveyDao = new SurveyDao(db);
      SurveyQuery query = new SurveyQuery(db, surveyDao, survey.getSurveySiteId());
      Survey s = query.surveyBySurveyTokenId(survey.getSurveyTokenId());
      sql.listSeparator(separator).append("assessment_type").argString(registration.getSurveyType());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_scheduled").argDate(registration.getSurveyDt());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_started").argDate(s.startTime());
      nbrArgs++;
      sql.listSeparator(separator).append("survey_ended").argDate(s.endTime());
      nbrArgs++;
      sql.listSeparator(separator).append("is_complete").argBoolean(s.isComplete());
      nbrArgs++;
      // Should migrate to tracking serverTimeMillis() and userTimeMillis(), but need to regenerate table
      sql.listSeparator(separator).append("survey_user_time_ms").argLong(s.serverTimeMillis(Integer.MAX_VALUE));
      nbrArgs++;

      // Add patient demographics
      PatientDao patientDao = new PatientDao(db, siteInfo.getSiteId(), null);
      Patient patient = patientDao.getPatient(registration.getPatientId());
      sql.listSeparator(separator).append("patient_id").argString(patient.getPatientId());
      nbrArgs++;
      sql.listSeparator(separator).append("patient_dob").argDate(patient.getDtBirth());
      nbrArgs++;
      sql.listSeparator(separator).append("patient_gender").argString(getPatientAttribute(patient,
          Constants.ATTRIBUTE_GENDER));
      nbrArgs++;
      sql.listSeparator(separator).append("patient_race").argString(getPatientAttribute(patient,
          Constants.ATTRIBUTE_RACE));
      nbrArgs++;
      sql.listSeparator(separator).append("patient_ethnicity").argString(getPatientAttribute(patient,
          Constants.ATTRIBUTE_ETHNICITY));
      nbrArgs++;

      // Add regular assessment answers
      String localSurveyProvider = getProviderId(db, "Local");
      SurveyStep step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "names"), "Order1");
      sql.listSeparator(separator).append("patient_name_self_report").argString(formFieldValue(step,
          "1:1:PATIENTNAME"));
      nbrArgs++;
      sql.listSeparator(separator).append("was_assisted").argString(formFieldLabel(step, "2:1:ASSISTED"));
      nbrArgs++;
      sql.listSeparator(separator).append("assisted_by").argString(formFieldLabel(step, "3:1:HELPER"));
      nbrArgs++;
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "bodymap"), "2");
      if (step != null) {
        sql.listSeparator(separator).append("bodymap_regions_csv").argString(step.answerRegionsCsv());
        nbrArgs++;
      } else {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "bodymap"), "1");
        if (step != null) {
          sql.listSeparator(separator).append("bodymap_regions_csv").argString(step.answerRegionsCsv());
          nbrArgs++;
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painIntensity"), "1");
      if (step != null) {
        sql.listSeparator(separator).append("pain_intensity_worst").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painIntensity"), "2");
      if (step != null) {
        sql.listSeparator(separator).append("pain_intensity_average").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painIntensity"), "3");
      if (step != null) {
        sql.listSeparator(separator).append("pain_intensity_now").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painIntensity"), "4");
      if (step != null) {
        sql.listSeparator(separator).append("pain_intensity_least").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painIntensityRP"), "1");
      if (step != null) {
        sql.listSeparator(separator).append("pain_intensity_worst").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painIntensityRP"), "2");
      if (step != null) {
        sql.listSeparator(separator).append("pain_intensity_average").argInteger(step.answerNumeric());
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painExperience"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("pain_exp_duration_years").argInteger(selectedFieldInt(step, "1:1:PAINDURATION"));
        nbrArgs++;
        sql.listSeparator(separator).append("pain_exp_duration_months").argInteger(selectedFieldInt(step, "1:2:PAINDURATION"));
        nbrArgs++;
        sql.listSeparator(separator).append("pain_exp_duration_days").argInteger(selectedFieldInt(step, "1:3:PAINDURATION"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painExperience"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("pain_exp_started").argString(selectedField(step, "2:1"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painExperience"), "Order3");
      if (step != null) {
        String[] columnNames = { "","pain_exp_quality_throbbing",
            "pain_exp_quality_shooting",
            "pain_exp_quality_stabbing",
            "pain_exp_quality_sharp",
            "pain_exp_quality_cramping",
            "pain_exp_quality_gnawing",
            "pain_exp_quality_hot",
            "pain_exp_quality_burning",
            "pain_exp_quality_aching",
            "pain_exp_quality_heavy",
            "pain_exp_quality_tender",
            "pain_exp_quality_splitting",
            "pain_exp_quality_tiring",
            "pain_exp_quality_exhausting",
            "pain_exp_quality_sickening",
            "pain_exp_quality_fearful",
            "pain_exp_quality_punishing",
            "pain_exp_quality_cruel"};
        for (int c=1; c<18; c++) {
          if (selectedFieldChoice(step, "3:1:PAINQUALITY", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        for (int c=10; c<columnNames.length; c++) {
          if (selectedFieldChoice(step, "3:2:PAINQUALITY", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painExperience"), "Order4");
      if (step !=null) {
        String[] columnNames = { "", "pain_exp_time_brief", "pain_exp_time_constant",
            "pain_exp_time_comes", "pain_exp_time_continuous", "pain_exp_time_always",
            "pain_exp_time_appears", "pain_exp_time_intermittent"};
        for (int c=1; c<columnNames.length; c++) {
          if (selectedFieldChoice(step, "4:1:PAINTIME", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("pain_exp_time_and_or").argString(formFieldValue(step, "4:2:PAINTIMEtext"));
        nbrArgs++;
      }

      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painExperience"), "Order5");
      if (step != null) {
        String[] columnNames = { "", "pain_exp_relief_nothing", "pain_exp_relief_acupuncture",
            "pain_exp_relief_bedrest", "pain_exp_relief_chiropractic", "pain_exp_relief_dark",
            "pain_exp_relief_exercise", "pain_exp_relief_massage", "pain_exp_relief_medications",
            "pain_exp_relief_heat", "pain_exp_relief_ice", "pain_exp_relief_movement",
            "pain_exp_relief_physical", "pain_exp_relief_quiet", "pain_exp_relief_sitting",
            "pain_exp_relief_standing", "pain_exp_relief_walking" };
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "5:1:PAINRELIEF", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("pain_exp_relief_and_or").argString(formFieldValue(step, "5:2:PAINRELIEF"));
        nbrArgs++;
      }

      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painExperience"), "Order6");
      if (step != null) {
      String[] columnNames = { "","pain_exp_worse_nothing","pain_exp_worse_acupuncture","pain_exp_worse_bedrest",
          "pain_exp_worse_chiropractic", "pain_exp_worse_dark", "pain_exp_worse_exercise",
          "pain_exp_worse_massage", "pain_exp_worse_medications", "pain_exp_worse_heat", "pain_exp_worse_ice",
          "pain_exp_worse_movement","pain_exp_worse_physical","pain_exp_worse_quiet","pain_exp_worse_sitting",
          "pain_exp_worse_standing","pain_exp_worse_walking","pain_exp_worse_stress"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "6:1:PAINWORSE", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("pain_exp_worse_and_or").argString(formFieldValue(step, "6:2:PAINWORSE"));
        nbrArgs++;
      }
      // Stanford 5 - original
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFive"), "Order1");
      if (step != null) {
        String[] columnNames = { "","s5_cause_accident", "s5_cause_injury", "s5_cause_undiagnosed",
            "s5_cause_muscle", "s5_cause_nerve", "s5_cause_disk", "s5_cause_bone", "s5_cause_cancer",
            "s5_cause_infection", "s5_cause_unknown"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "1:1:S5CAUSE", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_cause_and_or").argString(formFieldValue(step, "1:2:S5CAUSE"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFive"), "Order2");
      if (step != null) {
        String[] columnNames = { "","s5_impact_laying_in_bed", "s5_impact_cannot_do_job", "s5_impact_feeling_isolated",
            "s5_impact_reduced_social", "s5_impact_reduced_rec"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "2:1:S5IMPACT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_impact_and_or").argString(formFieldValue(step, "2:2:S5IMPACT"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFive"), "Order3");
      if (step != null) {
        String[] columnNames = { "","s5_diff_physical_activities","s5_diff_return_to_work", "s5_diff_more_housework"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "3:1:S5DIFFERENT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_diff_and_or").argString(formFieldValue(step, "3:2:S5DIFFERENT"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFive"), "Order4");
      if (step != null) {
        String[] columnNames = { "", "s5_treat_medications", "s5_treat_surgery", "s5_treat_physical",
            "s5_treat_natural", "s5_treat_complementary", "s5_treat_psychological", "s5_treat_mind_body",
            "s5_treat_finding_diagnosis", "s5_treat_unknown" };
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "4:1:S5TREATMENT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_treat_and_or").argString(formFieldValue(step, "4:2:S5TREATMENT"));
        nbrArgs++;
      }
      // Stanford 5 - version 2
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFiveV2"), "Order1");
      if (step != null) {
        String[] columnNames = { "","s5_cause_accident", "s5_cause_injury", "s5_cause_undiagnosed",
           "s5_cause_muscle", "s5_cause_nerve", "s5_cause_disk", "s5_cause_bone", "s5_cause_cancer",
          "s5_cause_infection", "s5_cause_unknown"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "1:1:S5CAUSE", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_cause_and_or").argString(formFieldValue(step, "1:2:S5CAUSE"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFiveV2"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("s5_motor_vehicle_accident").argInteger(selectedFieldInt(step, "2:1:S5MOTOACC"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFiveV2"), "Order3");
      if (step != null) {
        sql.listSeparator(separator).append("s5_diagnosed_arth_fibro").argInteger(selectedFieldInt(step, "3:1:S5ARTHFIBRO"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFiveV2"), "Order4");
      if (step != null) {
        String[] columnNames = { "","s5_impact_laying_in_bed", "s5_impact_cannot_do_job", "s5_impact_feeling_isolated",
            "s5_impact_reduced_social", "s5_impact_reduced_rec"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "4:1:S5IMPACT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_impact_and_or").argString(formFieldValue(step, "4:2:S5IMPACT"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFiveV2"), "Order5");
      if (step != null) {
        String[] columnNames = { "","s5_diff_physical_activities","s5_diff_return_to_work", "s5_diff_more_housework"};

        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "5:1:S5DIFFERENT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_diff_and_or").argString(formFieldValue(step, "5:2:S5DIFFERENT"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanfordFiveV2"), "Order6");
      if (step != null) {
        String[] columnNames = { "", "s5_treat_medications", "s5_treat_surgery", "s5_treat_physical",
            "s5_treat_natural", "s5_treat_complementary", "s5_treat_psychological", "s5_treat_mind_body",
            "s5_treat_finding_diagnosis", "s5_treat_unknown", "s5_treat_nerv_steriod", "s5_treat_education"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "6:1:S5TREATMENT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_treat_and_or").argString(formFieldValue(step, "6:2:S5TREATMENT"));
        nbrArgs++;
      }
      // Stanford 5 - S5IMPACT question stand alone
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "stanford5impact"), "Order1");
      if (step != null) {
        String[] columnNames = { "","s5_impact_laying_in_bed", "s5_impact_cannot_do_job", "s5_impact_feeling_isolated",
            "s5_impact_reduced_social", "s5_impact_reduced_rec"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "1:1:S5IMPACT", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("s5_impact_and_or").argString(formFieldValue(step, "1:2:S5IMPACT"));
        nbrArgs++;
      }
      // faDay
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faDay"), "Order1");
      if (step == null) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faDayV2"), "Order1");
      }
      if (step != null) {
        String[] columnNames = {"", "fa_avg_day_in_bed", "fa_avg_day_school", "fa_avg_day_work", "fa_avg_day_family"};
        for (int c = 1; c < columnNames.length; c++) {
          if (selectedFieldChoice(step, "1:1", c)) {
            sql.listSeparator(separator).append(columnNames[c]).argInteger(1);
            nbrArgs++;
          }
        }
        sql.listSeparator(separator).append("fa_avg_day_and_or").argString(formFieldValue(step, "1:2:text"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faDayV2"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("fa_missed_work").argInteger(selectedFieldInt(step, "2:1:MISSEDWORK"));
        nbrArgs++;
      }
      if (step != null) {
        sql.listSeparator(separator).append("fa_prod_reduced").argInteger(selectedFieldInt(step, "3:1:PRODREDUCED"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "angerFUfaDay"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("fa_missed_work").argInteger(selectedFieldInt(step, "1:1:MISSEDWORK"));
        nbrArgs++;
      }
      if (step != null) {
        sql.listSeparator(separator).append("fa_prod_reduced").argInteger(selectedFieldInt(step, "2:1:PRODREDUCED"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "angerFUopioids"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("ops_currently_taking").argInteger(selectedFieldInt(step, "1:1:OPS_CURRENTLY_TAKING"));
        nbrArgs++;
        sql.listSeparator(separator).append("ops_exper_cravings").argInteger(selectedFieldInt(step,"2:1:OPS_EXPER_CRAVINGS" ));
      }
      // faWorking
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faWorking"), "Order1");
      if (step == null) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faWorkingV2"), "Order1");
      }
      if (step != null) {
        sql.listSeparator(separator).append("fa_job").argString(formFieldValue(step, "1:1:FAJOB"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_working").argInteger(selectedFieldInt(step, "2:1:FAWORKING"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_workinglast_years").argInteger(selectedFieldInt(step, "3:1:FAWORKINGLAST"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_workinglast_months").argInteger(selectedFieldInt(step, "3:2:duration"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_workinglast_days").argInteger(selectedFieldInt(step, "3:3:duration"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_notworking_pain").argInteger(selectedFieldInt(step, "4:1:FANOTWORKPAIN"));
        nbrArgs++;
      }
      // faDisability
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faDisability"), "Order1");
      if (step != null) {
        int selectedDisability = selectedFieldInt(step, "1:1");
        if (selectedDisability == 1 || selectedDisability == 2) {
          sql.listSeparator(separator).append("fa_disability").argInteger(selectedDisability);
          nbrArgs++;
        }
        if (selectedFieldChoice(step, "2:1:FADISABILITY", 1)) {
          sql.listSeparator(separator).append("fa_disability_wcomp").argInteger(1);
          nbrArgs++;
        }
        if (selectedFieldChoice(step, "2:1:FADISABILITY", 2)) {
          sql.listSeparator(separator).append("fa_disability_ssdi").argInteger(1);
          nbrArgs++;
        }
        sql.listSeparator(separator).append("fa_disability_text").argString(formFieldValue(step, "3:1:text"));
        nbrArgs++;
      }
      // faLawsuit
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faLawsuit"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("fa_lawsuit").argInteger(selectedFieldInt(step, "1:1"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "faLawsuit"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("fa_legal_problems").argInteger(selectedFieldInt(step, "2:1"));
        nbrArgs++;
      }
      // sleepImpair
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "sleepImpair"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("sleep_impair_snore").argInteger(selectedFieldInt(step, "1:1"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "sleepImpair"), "Order2");
      if (step != null) {
        String sleepHours = selectedField(step, "2:1:duration");
        try {
          if (sleepHours != null) {
            BigDecimal decValue = new BigDecimal (sleepHours);
            sql.listSeparator(separator).append("sleep_impair_avg_hours").argBigDecimal(decValue);
            nbrArgs++;
          }
        } catch (NumberFormatException nfe) {
          System.out.println("sleep average hours " + sleepHours + " is not a valid number, not saved to square table for token_id" );
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "sleepImpair"), "Order3");
      if (step != null && selectedFieldChoice(step, "3:1", 1)) {
        sql.listSeparator(separator).append("sleep_impair_difficulty_init").argInteger(1);
        nbrArgs++;
      }
      // sleepImpair
      if (step != null && selectedFieldChoice(step, "3:1", 2)) {
        sql.listSeparator(separator).append("sleep_impair_difficulty_maint").argInteger(1);
        nbrArgs++;
      }

      // MedsOpioid4SurveyService
      String medsOpioid4ASurveyProvider = getProviderId(dbp.get(), "edu.stanford.registry.server.survey.MedsOpioid4SurveyService");
      if (medsOpioid4ASurveyProvider != null) {
        List<SurveyStep> steps = s.answeredStepsByProviderSection(medsOpioid4ASurveyProvider, getSectionId(db, "medsOpioid4A"));
        if (steps != null && steps.size() > 0) {
          step = steps.get(0);
          if (step != null) {
            sql.listSeparator(separator).append("opioid4_improve_function").argInteger(selectedFieldInt(step, "0:1:FOURA1"));
            sql.listSeparator(separator).append("opioid4_affect_pain_level").argInteger(selectedFieldInt(step, "1:1:FOURA2"));
            nbrArgs = nbrArgs + 2;
            //sql.listSeparator(separator).append("opioid4_taking_differently").argInteger(selectedFieldInt(step, "2:1:FOURA3"));
            String columnNames2[] = { "OPIOID4_TAKING_MORE", "OPIOID4_TAKING_STOCKPILING", "OPIOID4_TAKING_CHG_DOSING",
                "OPIOID4_TAKING_GETTING_MORE", "OPIOID4_TAKING_NOT_AS_MUCH", "OPIOID4_TAKING_NOT_TAKING" };
            for (int c = 0; c < columnNames2.length; c++) {
              if (selectedFieldChoice(step, "2:1:FOURA3", c)) {
                sql.listSeparator(separator).append(columnNames2[c]).argInteger(1);
                nbrArgs++;
              }
            }
            String columnNames3[] = { "OPIOID4_PROB_CONSTIPATION", "OPIOID4_PROB_LOSS_SEX_INTEREST", "OPIOID4_PROB_SENSITIVITY",
                "OPIOID4_PROB_SLOWED_THINKING", "OPIOID4_PROB_NAUSEA", "OPIOID4_PROB_DROWSINESS",
                "OPIOID4_PROB_OTHERS", "OPIOID4_PROB_WEIGHT_GAIN", "OPIOID4_PROB_WEIGHT_LOSS" };
            for (int c = 0; c < columnNames3.length; c++) {
              if (selectedFieldChoice(step, "3:1:FOURA4", c)) {
                sql.listSeparator(separator).append(columnNames3[c]).argInteger(1);
                nbrArgs++;
              }
            }
            //sql.listSeparator(separator).append("opioid4_prob_side_effects").argInteger(selectedFieldInt(step, "3:1:FOURA4"));
          }
        }
      }
      // psy a.k.a PSYCHOLOGY HISTORY
      String PSYFIELDS[] = {"psy_pre17_upheaval", "psy_post17_upheaval","psy_neglected","psy_child_chronic_pain", "psy_currently_threatened","psy_psych_hospitalized" };
      for (int q=1; q<7; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "psychHistory"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(PSYFIELDS[q-1]).argInteger(selectedValueInt(step));
          nbrArgs++;
        }
      }
      // PTSD
      String PTSDFIELDS[] = {"ptsd_traumatic_event", "ptsd_nightmares","ptsd_avoid","ptsd_on_guard","ptsd_detached", "ptsd_guilty"};
      for (int q=1; q<6; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "ptsd"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(PTSDFIELDS[q-1]).argInteger(selectedValueInt(step));
          nbrArgs++;
        }
      }
      // PTSD Version 2
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "ptsdV2"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append(PTSDFIELDS[0]).argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "ptsdV2"), "Order2");
      if (step != null) {
        for (int q=2; q<7; q++) {
          sql.listSeparator(separator).append(PTSDFIELDS[q-1]).argInteger(selectedFieldInt(step, q +":1"));
          nbrArgs++;
        }
      }
      // healthcare utilization
      String HUFIELDS[] = {"healthutil_phys_visits", "healthutil_emergency_room","healthutil_hospital_overnights","healthutil_hospital_total"};
      for (int q=1; q<5; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "healthUtil"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(HUFIELDS[q-1]).argString(selectedValue(step));
          nbrArgs++;
        }
      }
     // Background
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "background"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("bg_miles").argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "background"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("bg_time").argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "background"), "Order3");
      if (step != null) {
        sql.listSeparator(separator).append("bg_pcp").argString(selectedValue(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "background"), "Order4");
      if (step != null) {
        sql.listSeparator(separator).append("bg_refer").argString(selectedValue(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "background"), "Order5");
      if (step != null) {
        sql.listSeparator(separator).append("bg_marital").argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      // background V2
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "backgroundV2"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("bg_miles_txt").argString(selectedValue(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "backgroundV2"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("bg_time_txt").argString(selectedValue(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "backgroundV2"), "Order3");
      if (step != null) {
        sql.listSeparator(separator).append("bg_pcp").argString(selectedValue(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "backgroundV2"), "Order4");
      if (step != null) {
        sql.listSeparator(separator).append("bg_refer").argString(selectedValue(step));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "backgroundV2"), "Order5");
      if (step != null) {
        sql.listSeparator(separator).append("bg_marital").argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      // education
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "education"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("education").argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      // questions
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "questions"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("pat_or_dr_questions").argString(formFieldValue(step, "1:1:QUESTIONS"));
        nbrArgs++;
      }
      // otherPainDocs
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "otherPainDocs"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("other_pain_docs").argString(selectedValue(step));
        nbrArgs++;
      }

      // smoking
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "smoking"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("smoke_now").argInteger(selectedFieldInt(step, "1:1:SMOKENOW"));
        nbrArgs++;
        sql.listSeparator(separator).append("smoke_ppd").argString(selectedField(step, "2:1:SMOKEPPD"));
        nbrArgs++;
      }

      // alcohol
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "alcohol"), "Order1");
      if (step != null) {
        sql.listSeparator(separator).append("alcohol_now").argInteger(selectedFieldInt(step, "1:1:ALCOHOLNOW"));
        nbrArgs++;
        sql.listSeparator(separator).append("alcohol_per_day").argString(selectedField(step, "2:1:ALCOHOLPERDAY"));
        nbrArgs++;
        sql.listSeparator(separator).append("alcohol_per_week").argString(selectedField(step, "3:1:ALCOHOLPERWEEK"));
        nbrArgs++;
        sql.listSeparator(separator).append("alcohol_binge").argString(selectedField(step, "4:1:ALCOHOLBINGE"));
        nbrArgs++;
        sql.listSeparator(separator).append("alcohol_for_pain").argString(selectedField(step, "5:1:ALCOHOLFORPAIN"));
        nbrArgs++;
        sql.listSeparator(separator).append("drugs_10yr").argInteger(selectedFieldInt(step, "6:1:DRUGS10YR"));
        nbrArgs++;
        sql.listSeparator(separator).append("drugs_problem").argString(selectedField(step, "7:1:DRUGSCUT"));
        nbrArgs++;
        sql.listSeparator(separator).append("drugs_tx").argInteger(selectedFieldInt(step, "8:1:DRUGSTX"));
        nbrArgs++;
      }
      // painCatastrophizingScale a.k.a. PCS
      int pcsCnt=nbrArgs;
      String[] pcsNames = {"", "pcs_pain_will_end", "pcs_cant_go_on", "pcs_never_get_better", "pcs_overwhelms_me",
              "pcs_cant_stand_it", "pcs_will_get_worse", "pcs_other_painful_events", "pcs_want_to_go_away",
              "pcs_out_of_mind", "pcs_how_much_hurts", "pcs_want_to_stop", "pcs_reduce_intensity", "pcs_something_serious"};
      for (int q=1; q<14; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painCatastrophizingScaleV2"), "Order" + q);
        if (step == null) {
          step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "painCatastrophizingScale"), "Order" + q);
        }
        if (step != null) {
          sql.listSeparator(separator).append(pcsNames[q]).argInteger(selectedValueInt(step));
          nbrArgs++;
        }
      }
      if (pcsCnt < nbrArgs) {
        BigDecimal pcsScore = score(db, registration,"painCatastrophizingScale");
        if (pcsScore == null) {
          pcsScore = score(db, registration, "painCatastrophizingScaleV2");
        }
        if (pcsScore != null) {
          sql.listSeparator(separator).append("pcs_score").argBigDecimal(pcsScore);
          nbrArgs++;
        }
      }
      // Global Health
      int ghCnt = nbrArgs;
      String[] ghNames = {"",  "gh_general_health","gh_general_quality_life","gh_physical_health","gh_mental_health",
                  "gh_satisfaction_social","gh_carry_out_social","gh_carry_out_physical","gh_emotional_problems","gh_fatigue"};
      for (int q=1; q<10; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "globalHealth"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(ghNames[q]).argInteger(selectedFieldInt(step, q + ":1"));
          nbrArgs++;
        }
      }
      if (ghCnt < nbrArgs) {
        ChartScore chartScore = chartScore(db, registration, "globalHealth");
        if (chartScore != null && chartScore instanceof GlobalHealthScore) {
          GlobalHealthScore ghScore = (GlobalHealthScore) chartScore;
          sql.listSeparator(separator).append("gh_mental_score").argBigDecimal(new BigDecimal(ghScore.getMentalHealthTScore()));
          nbrArgs++;
          sql.listSeparator(separator).append("gh_physical_score").argBigDecimal(new BigDecimal(ghScore.getPhysicalHealthTScore()));
          nbrArgs++;
        }
      }
      // OpioidSurveysService (Opioid Promis Survey)
      String opioidSurveyProvider = getProviderId(dbp.get(), "edu.stanford.registry.server.survey.OpioidSurveysService");
      String opioidSurveySectionId = getSectionId(db, "opioidPromisSurvey");
      if (opioidSurveyProvider != null && opioidSurveySectionId != null) {
        String[] opsNames = {"ops_currently_taking", "ops_exper_cravings", "ops_used_street_drugs", "ops_needed_more", "ops_wanted_more",
            "ops_used_more_b4_wore_off", "ops_felt_anxious", "ops_got_from_other", "ops_borrowed", "ops_ran_out_early", "ops_counted_hours", "ops_saved_my_unused",
            "ops_kept_hidden_supply", "ops_used_someone_elses", "ops_hid_my_use", "ops_more_than_1_provider", "ops_others_obtained",
            "ops_emergency_room", "ops_told_lost", "ops_abused_meds", "ops_gone_too_soon", "ops_more_than_suppose_to", "ops_used_against", "ops_higher_dose",
            "ops_additional_meds", "ops_less_effective"};
        for (int q = 0; q <= 25; q++) {
          step = s.answeredStepByProviderSectionQuestion(opioidSurveyProvider, opioidSurveySectionId, "PromisOpioidQ" + q);
          if (step != null) {
            sql.listSeparator(separator).append(opsNames[q]).argInteger(selectedFieldInt(step, q + ":1:OPIOIDSURVEYS" + q));
            nbrArgs++;
          }
        }
      }
      // OpioidRiskReworded
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order0");
      // male patients are asked question 1, not male patients are asked question 2
      if (step != null) {
        sql.listSeparator(separator).append("ort_fam_alcohol_abuse").argInteger(selectedFieldInt(step, "0:0"));
        nbrArgs++;
        sql.listSeparator(separator).append("ort_fam_ill_drug_abuse").argInteger(selectedFieldInt(step, "0:1"));
        nbrArgs++;
        sql.listSeparator(separator).append("ort_fam_pre_drug_abuse").argInteger(selectedFieldInt(step, "0:2"));
        nbrArgs++;
      } else {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order1");
        if (step != null) {
          sql.listSeparator(separator).append("ort_fam_alcohol_abuse").argInteger(selectedFieldInt(step, "1:0"));
          nbrArgs++;
          sql.listSeparator(separator).append("ort_fam_ill_drug_abuse").argInteger(selectedFieldInt(step, "1:1"));
          nbrArgs++;
          sql.listSeparator(separator).append("ort_fam_pre_drug_abuse").argInteger(selectedFieldInt(step, "1:2"));
          nbrArgs++;
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order2");
      if (step != null) {
        sql.listSeparator(separator).append("ort_per_alcohol_abuse").argInteger(selectedFieldInt(step, "2:0"));
        nbrArgs++;
        sql.listSeparator(separator).append("ort_per_ill_drug_abuse").argInteger(selectedFieldInt(step, "2:1"));
        nbrArgs++;
        sql.listSeparator(separator).append("ort_per_pre_drug_abuse").argInteger(selectedFieldInt(step, "2:2"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order3");
      if (step != null) {
        sql.listSeparator(separator).append("ort_age_16_to_45").argInteger(selectedValueInt(step));
        nbrArgs++;
      }
      // male patients are asked question 4, not male patients are asked question 5
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order4");
      if (step != null && selectedLabel(step) != null) {
        sql.listSeparator(separator).append("ort_sexual_abuse").argInteger(selectedValueInt(step));
        nbrArgs++;
      } else {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order5");
        if (step != null) {
          sql.listSeparator(separator).append("ort_sexual_abuse").argInteger(selectedValueInt(step));
          nbrArgs++;
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "opioidRiskReworded"), "Order6");
      if (step != null) {
        sql.listSeparator(separator).append("ort_psy_depression").argInteger(selectedFieldInt(step,"6:1"));
        nbrArgs++;
        sql.listSeparator(separator).append("ort_psy_add_ocd_bis").argInteger(selectedFieldInt(step,"6:0"));
        nbrArgs++;
      }
      sql.listSeparator(separator).append("ort_score").argBigDecimal(score(db, registration,"opioidRiskReworded"));
      nbrArgs++;

      // injustice a.k.a. IEQ (12 questions, all select1/radio buttons)
      int ieqCnt=nbrArgs;
      String[] ieqNames = {"", "ieq_how_severe", "ieq_never_the_same", "ieq_someones_negligence",
              "ieq_no_should_live", "ieq_want_life_back", "ieq_permanent_way","ieq_so_unfair","ieq_taken_seriously",
              "ieq_make_up_for","ieq_been_robbed","ieq_never_achieve_dreams","ieq_happened_to_me" };

      for (int q=1; q<13; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "injustice"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(ieqNames[q]).argInteger(selectedValueInt(step));
          nbrArgs++;
        }
      }
      if (ieqCnt < nbrArgs) {
        sql.listSeparator(separator).append("ieq_score").argBigDecimal(score(db, registration,"injustice"));
        nbrArgs++;
      }
      // acceptances a.k.a. CPAQ (8 questions, all select1/radio buttons)
      int cpaqCnt=nbrArgs;
      String[] cpaqNames = {"","cpaq_business_of_living","cpaq_under_control_priority","cpaq_normal_life","cpaq_serious_plans",
              "cpaq_full_life","cpaq_care_of_resp","cpaq_avoid_situations","cpaq_worries_fears"};
      for (int q=1; q<9; q++) {
        step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "cpaq"), "Order" + q);
        if (step != null) {
          sql.listSeparator(separator).append(cpaqNames[q]).argInteger(selectedValueInt(step));
          nbrArgs++;
        }
      }
      if (cpaqCnt < nbrArgs) {
        sql.listSeparator(separator).append("cpaq_score").argBigDecimal(score(db, registration,"cpaq"));
        nbrArgs++;
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "research"), "1");
      if (step != null && step.answer() != null) {
        sql.listSeparator(separator).append("research").argInteger(getResearchChoice(step.answer().getAnswerJson()));
        nbrArgs++;
      }

      String childrenSurveyProvider = getProviderId(dbp.get(), "ChildrenQuestionService");
      String sectionId = getSectionId(dbp.get(), "children");
      if (childrenSurveyProvider != null && sectionId != null && !sectionId.isEmpty()) {
        step = s.answeredStepByProviderSectionQuestion(childrenSurveyProvider, sectionId, "Order1");
        if (step != null) {
          sql.listSeparator(separator).append("children_8_to_12").argInteger(selectedFieldInt(step, "1:1:BGCHILD812"));
          nbrArgs++;
        }
      }
      step = s.answeredStepByProviderSectionQuestion(localSurveyProvider, getSectionId(db, "workRelated"), "Order1");

      // workRelated.. 2nd and 3rd questions are the same as faDayV2
      if (step != null) {
        sql.listSeparator(separator).append("fa_work_status").argInteger(selectedFieldInt(step, "1:1:WORK_STATUS"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_missed_work").argInteger(selectedFieldInt(step, "2:1:MISSED_WORK"));
        nbrArgs++;
        sql.listSeparator(separator).append("fa_prod_reduced").argInteger(selectedFieldInt(step, "3:1:PROD_REDUCED"));
        nbrArgs++;
      }

      // Marlowe-Crowne consent
      List <SurveyStep> stepList = s.answeredStepsByProvider(getProviderId(dbp.get(), "MarloweCrowneService"));
      if (stepList != null) {
        for (SurveyStep surveyStep : stepList) {
          sql.listSeparator(separator).append("MCSDS_CONSENT").argInteger(selectedFieldInt(surveyStep, "0:0:CONSENT"));
        }

      }
      // This item bank was named "intensity" in PROMIS, but is actually "interference"
      BigDecimal promisScore = score(db, registration, "PROMIS Bank v1.1 - Pain Interference");
      if (promisScore == null || promisScore.longValue() == 0L) {
        promisScore = score(db, registration, "PROMIS Pain Intensity Bank"); // check for the old one
      }
      sql.listSeparator(separator).append("promis_pain_interference").argBigDecimal(promisScore);
      nbrArgs++;
      sql.listSeparator(separator).append("promis_pain_behavior").argBigDecimal(score(db, registration,
          "PROMIS Pain Behavior Bank"));
      nbrArgs++;
      promisScore = score(db, registration, "PROMIS Bank v1.2 - Physical Function");
      if (promisScore == null || promisScore.longValue() == 0L) {
        promisScore = score(db, registration, "PROMIS Physical Function Bank"); // check for the old one
      }
      sql.listSeparator(separator).append("promis_physical_function").argBigDecimal(promisScore);
      nbrArgs++;
      sql.listSeparator(separator).append("promis_physical_function_upe").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.2 - Upper Extremity"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_physical_function_mob").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.2 - Mobility"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_fatigue").argBigDecimal(score(db, registration,
          "PROMIS Fatigue Bank"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_depression").argBigDecimal(score(db, registration,
          "PROMIS Depression Bank"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_anxiety").argBigDecimal(score(db, registration,
          "PROMIS Anxiety Bank"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_sleep_disturb_v1_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.0 - Sleep Disturbance"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_sleep_impair_v1_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.0 - Sleep-Related Impairment"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_anger_v1_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v1.0 - Anger"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_emot_support_v2_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v2.0 - Emotional Support"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_sat_roles_act_v2_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v2.0 - Satisfaction Roles Activities"));
      nbrArgs++;
      sql.listSeparator(separator).append("promis_social_iso_v2_0").argBigDecimal(score(db, registration,
          "PROMIS Bank v2.0 - Social Isolation"));
      nbrArgs++;

      // Get the study for the Global Cannibus Questionnaire and call the utility to add its values to the sql
      try {
        String surveyProviderStr = getProviderId(dbp.get(), "QualifyQuestionService-GCQ");
        if (surveyProviderStr != null) {
          int surveyProvider = Integer.parseInt(surveyProviderStr);
          SurveySystDao ssDao = new SurveySystDao(dbp);
          Study study = ssDao.getStudy(surveyProvider, "gcq");
          GCQToSquare s2square = new GCQToSquare(siteInfo);
          nbrArgs = nbrArgs + s2square.addCompletedSurveyValues(db, survey.getSurveyTokenId(), query, study, "GCQ_", sql, separator);
        } else {
          System.err.println("Could not find QualifyQuestionService-GCQ !");
        }
      } catch (Exception ex) {
        System.err.println("Could not process the GCQ for token_id " + surveyRegId);
      }

      if (exists) {
        sql.listEnd("=? where survey_site_id=? and survey_token_id=?")
            .argLong(survey.getSurveySiteId())
            .argLong(survey.getSurveyTokenId());
      } else {
        sql.append(") values (?");
        while (nbrArgs-- > 1) {
          sql.append(",?");
        }
        sql.append(")");
      }
      try {
        db.toInsert(sql.sql()).apply(sql).insert(1);
      } catch (Exception ex) {
        log.error(ex);
        return false;
      }
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Error storing survey_registration_id " + surveyRegId + " in rpt_pain_std_surveys_square", e);
    }
  }

  BigDecimal score(Database db, SurveyRegistration reg, String studyDesc) {
    ChartScore chartScore = chartScore(db, reg, studyDesc);
    return chartScore == null ? null : chartScore.getScore();
  }

  ChartScore chartScore(Database db, SurveyRegistration reg, String studyDesc) {
    // Find the patient study for the survey registration
    PatStudyDao patStudyDao = new PatStudyDao(db, siteInfo);
    ArrayList<PatientStudyExtendedData> patStudies =
        patStudyDao.getPatientStudyDataBySurveyRegIdAndStudyDescription(reg.getSurveyRegId(), studyDesc);
    PatientStudyExtendedData patientStudy;
    if ((patStudies == null) || (patStudies.size() == 0)) {
      // patient study not found, ignore
      patientStudy = null;
    } else if (patStudies.size() > 1) {
      // more than one patient study found
      throw new RuntimeException("More than one patient study found for survey registration " + reg.getSurveyRegId()
          + " and study " + studyDesc);
    } else {
      patientStudy = patStudies.get(0);
    }
    // Get the chart score for the patient study
    ChartScore chartScore = null;
    if (patientStudy != null) {
      ScoreProvider scoreProvider = SurveyServiceFactory.getFactory(siteInfo)
          .getScoreProvider(db, patientStudy.getSurveySystemName(), studyDesc);
      ArrayList<ChartScore> chartScores = scoreProvider.getScore(patientStudy);

      if ((chartScores == null) || (chartScores.size() == 0)) {
        log.error("ScoreProvider did not return a ChartScore for PatientStudy " + patientStudy.getPatientStudyId());
      } else if (chartScores.size() > 1) {
        log.error("ScoreProvider returned more than one ChartScore for PatientStudy "
            + patientStudy.getPatientStudyId());
      } else {
        chartScore = chartScores.get(0);
      }
    }
    return chartScore;
  }

  String formFieldValue(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      return step.answer().formFieldValue(fieldId);
    }
  }


  String formFieldLabel(SurveyStep step, String fieldId) {
    if (step == null || step.answer() == null) {
      return null;
    } else {
      String answerId = step.answer().formFieldValue(fieldId);
      String answerLabel = null;
      if (answerId != null) {
        FormFieldValue fieldValue = step.questionFormFieldValue(fieldId, answerId);
        if (fieldValue == null) {
          // Probably need to revise the container interface to provide support for logging warnings
          log.error("Could not locate question field value for fieldId: " + fieldId + " answerId: " + answerId
              + "\nanswerJson: " + step.answerJson() + "\nquestionJson: " + step.questionJson());
          answerLabel = "id --> " + answerId;
        } else {
          answerLabel = fieldValue.getLabel();
        }
      }
      return answerLabel;
    }
  }

  String selectedValue(SurveyStep step) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            return formFieldValue(step, field.getFieldId());
          }
        }
      }
    }
    return null;
  }
  Integer selectedValueInt(SurveyStep step) {
    if (step == null) {
      return null;
    }
    String selectedValue  = selectedValue(step);
    if (selectedValue != null) {
      try {
        return Integer.parseInt(selectedValue);
      } catch (NumberFormatException nfe) {
        log.warn("Unable to save question " + step.questionJson() + " with answer " + selectedValue + " as integer");
      }
    }
    return null;
  }

  String selectedField(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            if (field.getFieldId().equals(fieldId)) {
              try {
                return formFieldValue(step, field.getFieldId());
              } catch (NumberFormatException nfe) {
                log.warn("Unable to save question " + step.questionJson() + " with answer " + step.answer().getAnswerJson() + " as integer");
              }
            }
          }
        }
      }
    }
    return null;
  }
  int selectedFieldInt(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            if (field.getFieldId().equals(fieldId)) {
              try {
                return Integer.parseInt(formFieldValue(step, field.getFieldId()));
              } catch (NumberFormatException nfe) {
                log.warn("Unable to save question " + step.questionJson() + " with answer " + step.answer().getAnswerJson() + " as integer");
              }
            }
          }
        }
      }
    }
    return 0;
  }

  boolean selectedFieldChoice(SurveyStep step, String fieldId, int choice) {
  if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null && field.getFieldId().equals(fieldId)) {
            List<String> choices = step.answer().formFieldValues(fieldId);
            if (choices != null){
               for (String c : choices) {
                 if (c != null && Integer.toString(choice).equals(c)) {
                   return true;
                 }
              }
            }
          }
        }
      }
    }
    return false;
  }

  int selectedFieldChoiceInt(SurveyStep step, String fieldId) {
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getFieldId() != null && formFieldValue(step, field.getFieldId()) != null && field.getFieldId().equals(fieldId)) {
            List<String> choices = step.answer().formFieldValues(fieldId);
            if (choices != null){
              for (String c : choices) {
                if (c != null) {
                  return Integer.parseInt(c);
                }
              }
            }
          }
        }
      }
    }
    return 0;
  }
  String selectedLabel(SurveyStep step) {
    StringBuilder responses = null;
    if (step != null && step.answer() != null && step.answer().form() != null ) {
      List<FormFieldAnswer> fields = step.answer().form().getFieldAnswers();
      if (fields != null) {
        for (FormFieldAnswer field : fields) {
          if (field != null && field.getChoice() != null && step.questionFormField(field.getFieldId()) != null && formFieldValue(step, field.getFieldId()) != null) {
            //System.out.println("field.getFieldId = " + field.getFieldId());
            //System.out.println("formFieldValue=" + formFieldValue(step, field.getFieldId()) );
            //System.out.println("questionFormField=" + step.questionFormField(field.getFieldId()));
            for (FormFieldValue value : step.questionFormField(field.getFieldId()).getValues()) {
              if (value != null && value.getId() != null && value.getId().equals(formFieldValue(step, field.getFieldId()) )) {
                //System.out.println("formFieldLabel=" + value.getLabel());
                if (responses != null) {
                  responses.append(",");
                } else {
                  responses = new StringBuilder();
                }
                responses.append(value.getLabel());
              }
            }
          }
        }
      }
    }
    if (responses != null) {
      return responses.toString();
    }
    return null;
  }

  private String getPatientAttribute(Patient patient, String attribute) {
    PatientAttribute attr = patient.getAttribute(attribute);
    return (attr == null) ? null : attr.getDataValue();
  }

  private Long getSurveyRegistrationId(SurveyAdvance survey, Supplier<Database> database) {
    // TABLEREF survey_registration
    return database.get().toSelect("select survey_reg_id from survey_registration sr, survey_token st"
        + " where sr.survey_site_id=st.survey_site_id and sr.token=st.survey_token"
        + " and st.survey_site_id=? and st.survey_token_id=?")
        .argLong(survey.getSurveySiteId())
        .argLong(survey.getSurveyTokenId())
        .queryLongOrNull();
  }

  public static void main(String[] args) {
    DatabaseProvider.fromPropertyFile("../build.properties", "registry.")
        .withSqlParameterLogging()
        .withTransactionControl()
        .transact((Supplier<Database> dbp) -> {
            ServerContext serverContext = new ServerContext(dbp);
            SiteInfo siteInfo = serverContext.getSiteInfo(1L);
            Long siteId = siteInfo.getSiteId();
            Schema schema = new Schema().addTable("rpt_pain_std_surveys_square")
                .addColumn("survey_site_id").asLong().table()
                .addColumn("survey_token_id").asLong().table()
                .addColumn("assessment_type").asString(50).table()
                .addColumn("survey_scheduled").asDate().table()
                .addColumn("survey_started").asDate().table()
                .addColumn("survey_ended").asDate().table()
                .addColumn("is_complete").asBoolean().table()
                .addColumn("survey_user_time_ms").asLong().table()
                .addColumn("patient_id").asString(50).table()
                .addColumn("patient_dob").asDate().table()
                .addColumn("patient_gender").asString(250).table()
                .addColumn("patient_race").asString(250).table()
                .addColumn("patient_ethnicity").asString(250).table()
                .addColumn("patient_name_self_report").asString(4000).table()
                .addColumn("was_assisted").asString(4000).table()
                .addColumn("assisted_by").asString(4000).table()
                .addColumn("bodymap_regions_csv").asString(4000).table()
                .addColumn("pain_intensity_worst").asInteger().table()
                .addColumn("pain_intensity_average").asInteger().table()
                .addColumn("pain_intensity_now").asInteger().table()
                .addColumn("pain_intensity_least").asInteger().table()
                .addColumn("pain_exp_duration_years").asInteger().table()
                .addColumn("pain_exp_duration_months").asInteger().table()
                .addColumn("pain_exp_duration_days").asInteger().table()
                .addColumn("pain_exp_started").asString(4000).table()
                .addColumn("pain_exp_quality_throbbing").asInteger().table()
                .addColumn("pain_exp_quality_shooting").asInteger().table()
                .addColumn("pain_exp_quality_stabbing").asInteger().table()
                .addColumn("pain_exp_quality_sharp").asInteger().table()
                .addColumn("pain_exp_quality_cramping").asInteger().table()
                .addColumn("pain_exp_quality_gnawing").asInteger().table()
                .addColumn("pain_exp_quality_hot").asInteger().table()
                .addColumn("pain_exp_quality_burning").asInteger().table()
                .addColumn("pain_exp_quality_aching").asInteger().table()
                .addColumn("pain_exp_quality_heavy").asInteger().table()
                .addColumn("pain_exp_quality_tender").asInteger().table()
                .addColumn("pain_exp_quality_splitting").asInteger().table()
                .addColumn("pain_exp_quality_tiring").asInteger().table()
                .addColumn("pain_exp_quality_exhausting").asInteger().table()
                .addColumn("pain_exp_quality_sickening").asInteger().table()
                .addColumn("pain_exp_quality_fearful").asInteger().table()
                .addColumn("pain_exp_quality_punishing").asInteger().table()
                .addColumn("pain_exp_quality_cruel").asInteger().table()
                .addColumn("pain_exp_time_brief").asInteger().table()
                .addColumn("pain_exp_time_constant").asInteger().table()
                .addColumn("pain_exp_time_comes").asInteger().table()
                .addColumn("pain_exp_time_continuous").asInteger().table()
                .addColumn("pain_exp_time_always").asInteger().table()
                .addColumn("pain_exp_time_appears").asInteger().table()
                .addColumn("pain_exp_time_intermittent").asInteger().table()
                .addColumn("pain_exp_time_and_or").asString(4000).table()
                .addColumn("pain_exp_relief_nothing").asInteger().table()
                .addColumn("pain_exp_relief_acupuncture").asInteger().table()
                .addColumn("pain_exp_relief_bedrest").asInteger().table()
                .addColumn("pain_exp_relief_chiropractic").asInteger().table()
                .addColumn("pain_exp_relief_dark").asInteger().table()
                .addColumn("pain_exp_relief_exercise").asInteger().table()
                .addColumn("pain_exp_relief_massage").asInteger().table()
                .addColumn("pain_exp_relief_medications").asInteger().table()
                .addColumn("pain_exp_relief_heat").asInteger().table()
                .addColumn("pain_exp_relief_ice").asInteger().table()
                .addColumn("pain_exp_relief_movement").asInteger().table()
                .addColumn("pain_exp_relief_physical").asInteger().table()
                .addColumn("pain_exp_relief_quiet").asInteger().table()
                .addColumn("pain_exp_relief_sitting").asInteger().table()
                .addColumn("pain_exp_relief_standing").asInteger().table()
                .addColumn("pain_exp_relief_walking").asInteger().table()
                .addColumn("pain_exp_relief_and_or").asString(4000).table()
                .addColumn("pain_exp_worse_nothing").asInteger().table()
                .addColumn("pain_exp_worse_acupuncture").asInteger().table()
                .addColumn("pain_exp_worse_bedrest").asInteger().table()
                .addColumn("pain_exp_worse_chiropractic").asInteger().table()
                .addColumn("pain_exp_worse_dark").asInteger().table()
                .addColumn("pain_exp_worse_exercise").asInteger().table()
                .addColumn("pain_exp_worse_massage").asInteger().table()
                .addColumn("pain_exp_worse_medications").asInteger().table()
                .addColumn("pain_exp_worse_heat").asInteger().table()
                .addColumn("pain_exp_worse_ice").asInteger().table()
                .addColumn("pain_exp_worse_movement").asInteger().table()
                .addColumn("pain_exp_worse_physical").asInteger().table()
                .addColumn("pain_exp_worse_quiet").asInteger().table()
                .addColumn("pain_exp_worse_sitting").asInteger().table()
                .addColumn("pain_exp_worse_standing").asInteger().table()
                .addColumn("pain_exp_worse_walking").asInteger().table()
                .addColumn("pain_exp_worse_stress").asInteger().table()
                .addColumn("pain_exp_worse_and_or").asString(4000).table()
                .addColumn("s5_cause_accident").asInteger().table()
                .addColumn("s5_cause_injury").asInteger().table()
                .addColumn("s5_cause_undiagnosed").asInteger().table()
                .addColumn("s5_cause_muscle").asInteger().table()
                .addColumn("s5_cause_nerve").asInteger().table()
                .addColumn("s5_cause_disk").asInteger().table()
                .addColumn("s5_cause_bone").asInteger().table()
                .addColumn("s5_cause_cancer").asInteger().table()
                .addColumn("s5_cause_infection").asInteger().table()
                .addColumn("s5_cause_unknown").asInteger().table()
                .addColumn("s5_cause_and_or").asString(4000).table()
                .addColumn("s5_motor_vehicle_accident").asInteger().table()
                .addColumn("s5_diagnosed_arth_fibro").asInteger().table()
                .addColumn("s5_impact_laying_in_bed").asInteger().table()
                .addColumn("s5_impact_cannot_do_job").asInteger().table()
                .addColumn("s5_impact_feeling_isolated").asInteger().table()
                .addColumn("s5_impact_reduced_social").asInteger().table()
                .addColumn("s5_impact_reduced_rec").asInteger().table()
                .addColumn("s5_impact_and_or").asString(4000).table()
                .addColumn("s5_diff_physical_activities").asInteger().table()
                .addColumn("s5_diff_return_to_work").asInteger().table()
                .addColumn("s5_diff_more_housework").asInteger().table()
                .addColumn("s5_diff_and_or").asString(4000).table()
                .addColumn("s5_treat_medications").asInteger().table()
                .addColumn("s5_treat_surgery").asInteger().table()
                .addColumn("s5_treat_physical").asInteger().table()
                .addColumn("s5_treat_natural").asInteger().table()
                .addColumn("s5_treat_complementary").asInteger().table()
                .addColumn("s5_treat_psychological").asInteger().table()
                .addColumn("s5_treat_mind_body").asInteger().table()
                .addColumn("s5_treat_finding_diagnosis").asInteger().table()
                .addColumn("s5_treat_unknown").asInteger().table()
                .addColumn("s5_treat_nerv_steriod").asInteger().table()
                .addColumn("s5_treat_education").asInteger().table()
                .addColumn("s5_treat_and_or").asString(4000).table()
                .addColumn("fa_avg_day_in_bed").asInteger().table()
                .addColumn("fa_avg_day_school").asInteger().table()
                .addColumn("fa_avg_day_work").asInteger().table()
                .addColumn("fa_avg_day_family").asInteger().table()
                .addColumn("fa_avg_day_and_or").asString(4000).table()
                .addColumn("fa_missed_work").asInteger().table()
                .addColumn("fa_prod_reduced").asInteger().table()
                .addColumn("fa_work_status").asInteger().table()
                .addColumn("fa_job").asString(4000).table()
                .addColumn("fa_working").asInteger().table()
                .addColumn("fa_workinglast_years").asInteger().table()
                .addColumn("fa_workinglast_months").asInteger().table()
                .addColumn("fa_workinglast_days").asInteger().table()
                .addColumn("fa_disability").asInteger().table()
                .addColumn("fa_disability_kind").asInteger().table()
                .addColumn("fa_disability_wcomp").asInteger().table()
                .addColumn("fa_disability_ssdi").asInteger().table()
                .addColumn("fa_disability_text").asString(4000).table()
                .addColumn("fa_lawsuit").asInteger().table()
                .addColumn("fa_legal_problems").asInteger().table()
                .addColumn("sleep_impair_snore").asInteger().table()
                .addColumn("sleep_impair_avg_hours").asBigDecimal(12,2).table()
                .addColumn("sleep_impair_difficulty_init").asInteger().table()
                .addColumn("sleep_impair_difficulty_maint").asInteger().table()
                .addColumn("opioid4_improve_function").asInteger().table()
                .addColumn("opioid4_affect_pain_level").asInteger().table()
                .addColumn("opioid4_taking_more").asInteger().table()
                .addColumn("opioid4_taking_stockpiling").asInteger().table()
                .addColumn("opioid4_taking_chg_dosing").asInteger().table()
                .addColumn("opioid4_taking_getting_more").asInteger().table()
                .addColumn("opioid4_taking_not_as_much").asInteger().table()
                .addColumn("opioid4_taking_not_taking").asInteger().table()
                .addColumn("opioid4_prob_constipation").asInteger().table()
                .addColumn("opioid4_prob_loss_sex_interest").asInteger().table()
                .addColumn("opioid4_prob_sensitivity").asInteger().table()
                .addColumn("opioid4_prob_slowed_thinking").asInteger().table()
                .addColumn("opioid4_prob_nausea").asInteger().table()
                .addColumn("opioid4_prob_drowsiness").asInteger().table()
                .addColumn("opioid4_prob_others").asInteger().table()
                .addColumn("opioid4_prob_weight_gain").asInteger().table()
                .addColumn("opioid4_prob_weight_loss").asInteger().table()
                .addColumn("psy_pre17_upheaval").asInteger().table()
                .addColumn("psy_post17_upheaval").asInteger().table()
                .addColumn("psy_neglected").asInteger().table()
                .addColumn("psy_child_chronic_pain").asInteger().table()
                .addColumn("psy_currently_threatened").asInteger().table()
                .addColumn("psy_psych_hospitalized").asInteger().table()
                .addColumn("ptsd_traumatic_event").asInteger().table()
                .addColumn("ptsd_nightmares").asInteger().table()
                .addColumn("ptsd_avoid").asInteger().table()
                .addColumn("ptsd_on_guard").asInteger().table()
                .addColumn("ptsd_detached").asInteger().table()
                .addColumn("ptsd_guilty").asInteger().table()
                .addColumn("healthutil_phys_visits").asString(4000).table()
                .addColumn("healthutil_emergency_room").asString(4000).table()
                .addColumn("healthutil_hospital_overnights").asString(4000).table()
                .addColumn("healthutil_hospital_total").asString(4000).table()
                .addColumn("bg_miles").asInteger().table()
                .addColumn("bg_time").asInteger().table()
                .addColumn("bg_pcp").asString(4000).table()
                .addColumn("bg_refer").asString(4000).table()
                .addColumn("bg_marital").asInteger().table()
                .addColumn("education").asInteger().table()
                .addColumn("pat_or_dr_questions").asString(4000).table()
                .addColumn("other_pain_docs").asString(4000).table()
                .addColumn("smoke_now").asInteger().table()
                .addColumn("smoke_ppd").asString(4000).table()
                .addColumn("alcohol_now").asInteger().table()
                .addColumn("alcohol_per_day").asString(4000).table()
                .addColumn("alcohol_per_week").asString(4000).table()
                .addColumn("alcohol_binge").asString(4000).table()
                .addColumn("alcohol_for_pain").asString(4000).table()
                .addColumn("drugs_10yr").asInteger().table()
                .addColumn("drugs_problem").asString(4000).table()
                .addColumn("drugs_tx").asInteger().table()
                .addColumn("pcs_pain_will_end").asInteger().table()
                .addColumn("pcs_cant_go_on").asInteger().table()
                .addColumn("pcs_never_get_better").asInteger().table()
                .addColumn("pcs_overwhelms_me").asInteger().table()
                .addColumn("pcs_cant_stand_it").asInteger().table()
                .addColumn("pcs_will_get_worse").asInteger().table()
                .addColumn("pcs_other_painful_events").asInteger().table()
                .addColumn("pcs_want_to_go_away").asInteger().table()
                .addColumn("pcs_out_of_mind").asInteger().table()
                .addColumn("pcs_how_much_hurts").asInteger().table()
                .addColumn("pcs_want_to_stop").asInteger().table()
                .addColumn("pcs_reduce_intensity").asInteger().table()
                .addColumn("pcs_something_serious").asInteger().table()
                .addColumn("pcs_score").asInteger().table()
                .addColumn("gh_general_health").asInteger().table()
                .addColumn("gh_general_quality_life").asInteger().table()
                .addColumn("gh_physical_health").asInteger().table()
                .addColumn("gh_mental_health").asInteger().table()
                .addColumn("gh_satisfaction_social").asInteger().table()
                .addColumn("gh_carry_out_social").asInteger().table()
                .addColumn("gh_carry_out_physical").asInteger().table()
                .addColumn("gh_emotional_problems").asInteger().table()
                .addColumn("gh_fatigue").asInteger().table()
                .addColumn("gh_mental_score").asBigDecimal(10, 5).table()
                .addColumn("gh_physical_score").asBigDecimal(10, 5).table()
                .addColumn("promis_pain_interference").asBigDecimal(10, 5).table()
                .addColumn("promis_pain_behavior").asBigDecimal(10, 5).table()
                .addColumn("promis_physical_function").asBigDecimal(10, 5).table()
                .addColumn("promis_physical_function_upe").asBigDecimal(10, 5).table()
                .addColumn("promis_physical_function_mob").asBigDecimal(10, 5).table()
                .addColumn("promis_fatigue").asBigDecimal(10, 5).table()
                .addColumn("promis_depression").asBigDecimal(10, 5).table()
                .addColumn("promis_anxiety").asBigDecimal(10, 5).table()
                .addColumn("promis_sleep_disturb_v1_0").asBigDecimal(10, 5).table()
                .addColumn("promis_sleep_impair_v1_0").asBigDecimal(10, 5).table()
                .addColumn("promis_anger_v1_0").asBigDecimal(10, 5).table()
                .addColumn("promis_emot_support_v2_0").asBigDecimal(10, 5).table()
                .addColumn("promis_sat_roles_act_v2_0").asBigDecimal(10, 5).table()
                .addColumn("promis_social_iso_v2_0").asBigDecimal(10, 5).table()
                .addColumn("ops_currently_taking").asInteger().table()
                .addColumn("ops_exper_cravings").asInteger().table()
                .addColumn("ops_used_street_drugs").asInteger().table()
                .addColumn("ops_needed_more").asInteger().table()
                .addColumn("ops_wanted_more").asInteger().table()
                .addColumn("ops_used_more_b4_wore_off").asInteger().table()
                .addColumn("ops_felt_anxious").asInteger().table()
                .addColumn("ops_got_from_other").asInteger().table()
                .addColumn("ops_borrowed").asInteger().table()
                .addColumn("ops_ran_out_early").asInteger().table()
                .addColumn("ops_counted_hours").asInteger().table()
                .addColumn("ops_saved_my_unused").asInteger().table()
                .addColumn("ops_kept_hidden_supply").asInteger().table()
                .addColumn("ops_used_someone_elses").asInteger().table()
                .addColumn("ops_hid_my_use").asInteger().table()
                .addColumn("ops_more_than_1_provider").asInteger().table()
                .addColumn("ops_others_obtained").asInteger().table()
                .addColumn("ops_emergency_room").asInteger().table()
                .addColumn("ops_told_lost").asInteger().table()
                .addColumn("ops_abused_meds").asInteger().table()
                .addColumn("ops_gone_too_soon").asInteger().table()
                .addColumn("ops_more_than_suppose_to").asInteger().table()
                .addColumn("ops_used_against").asInteger().table()
                .addColumn("ops_higher_dose").asInteger().table()
                .addColumn("ops_additional_meds").asInteger().table()
                .addColumn("ops_less_effective").asInteger().table()
                .addColumn("ort_fam_alcohol_abuse").asInteger().table()
                .addColumn("ort_fam_ill_drug_abuse").asInteger().table()
                .addColumn("ort_fam_pre_drug_abuse").asInteger().table()
                .addColumn("ort_per_alcohol_abuse").asInteger().table()
                .addColumn("ort_per_ill_drug_abuse").asInteger().table()
                .addColumn("ort_per_pre_drug_abuse").asInteger().table()
                .addColumn("ort_age_16_to_45").asInteger().table()
                .addColumn("ort_sexual_abuse").asInteger().table()
                .addColumn("ort_psy_depression").asInteger().table()
                .addColumn("ort_psy_add_ocd_bis").asInteger().table()
                .addColumn("ort_score").asInteger().table()
                .addColumn("ieq_how_severe").asInteger().table()
                .addColumn("ieq_never_the_same").asInteger().table()
                .addColumn("ieq_someones_negligence").asInteger().table()
                .addColumn("ieq_no_should_live").asInteger().table()
                .addColumn("ieq_want_life_back").asInteger().table()
                .addColumn("ieq_permanent_way").asInteger().table()
                .addColumn("ieq_so_unfair").asInteger().table()
                .addColumn("ieq_taken_seriously").asInteger().table()
                .addColumn("ieq_make_up_for").asInteger().table()
                .addColumn("ieq_been_robbed").asInteger().table()
                .addColumn("ieq_never_achieve_dreams").asInteger().table()
                .addColumn("ieq_happened_to_me").asInteger().table()
                .addColumn("ieq_score").asInteger().table()
                .addColumn("cpaq_business_of_living").asInteger().table()
                .addColumn("cpaq_under_control_priority").asInteger().table()
                .addColumn("cpaq_normal_life").asInteger().table()
                .addColumn("cpaq_serious_plans").asInteger().table()
                .addColumn("cpaq_full_life").asInteger().table()
                .addColumn("cpaq_care_of_resp").asInteger().table()
                .addColumn("cpaq_avoid_situations").asInteger().table()
                .addColumn("cpaq_worries_fears").asInteger().table()
                .addColumn("cpaq_score").asInteger().table()
                .addColumn("research").asInteger().table()
                .addColumn("bg_miles_txt").asString(4000).table()
                .addColumn("bg_time_txt").asString(4000).table()
                .addColumn("fa_notworking_pain").asInteger().table()
                .addColumn("children_8_TO_12").asInteger().table()
                .addColumn("GCQ_USING").asInteger().table()
                .addColumn("GCQ_GCQDAILYHC").asString(4000).table()
                .addColumn("GCQ_GCQDAILYTHC").asString(4000).table()
                .addColumn("GCQ_GCQDAILYCBD").asString(4000).table()
                .addColumn("GCQ_GCQDAILYOTHER").asString(4000).table()
                .addColumn("GCQ_FREQUENCY").asInteger().table()
                .addColumn("GCQ_ADMINISTRATIONEDIBLE").asStringFixed(1).table()
                .addColumn("GCQ_ADMINISTRATIONOIL").asStringFixed(1).table()
                .addColumn("GCQ_ADMINISTRATIONTINCTURE").asStringFixed(1).table()
                .addColumn("GCQ_ADMINISTRATIONSMOKED").asStringFixed(1).table()
                .addColumn("GCQ_ADMINISTRATIONVAPORIZ").asStringFixed(1).table()
                .addColumn("GCQ_ADMINISTRATIONTOPICAL ").asStringFixed(1).table()
                .addColumn("GCQ_ADMINOTHER").asString(4000).table()
                .addColumn("GCQ_DURATION").asString(4000).table()
                .addColumn("GCQ_DESIREDTHC").asInteger().table()
                .addColumn("GCQ_DESIREDCBD").asInteger().table()
                .addColumn("GCQ_STRAINPREF").asInteger().table()
                .addColumn("GCQ_MEDICAL").asInteger().table()
                .addColumn("GCQ_RECSOCIAL").asInteger().table()
                .addColumn("GCQ_SPIRITUAL").asInteger().table()
                .addColumn("MCSDS_CONSENT").asInteger().table()
                .addPrimaryKey("rpt_pain_std_surveys_square_pk", "survey_site_id", "survey_token_id").table()
                .schema();

//            System.out.println(schema.print(Flavor.oracle));
//            if (true) { System.exit(0); }

            // Drop and re-create the square table representation
            Database db = dbp.get();

            db.dropTableQuietly("rpt_pain_std_surveys_square");
            schema.execute(db);

            // Reset the handler so it will repopulate the square table
            SurveyDao surveyDao = new SurveyDao(db);
            SurveyAdvancePush push = surveyDao.findSurveyAdvancePush(siteId, "squareTable");
            if (push == null) {
              push = new SurveyAdvancePush();
              push.setSurveySiteId(siteId);
              push.setRecipientName("squareTable");
              push.setRecipientDisplayName("Populate rpt_pain_std_surveys_square table");
              push.setFailedCount(0L);
              push.setEnabled(true);
              surveyDao.insertSurveyAdvancePush(push);
            } else {
              push.setPushedSurveySequence(null);
              push.setLastPushedTime(null);
              surveyDao.updateSurveyAdvancePush(push);
            }

            new ServerUtils(".");
            SurveyAdvanceMonitor monitor = new SurveyAdvanceMonitor(siteId, new SurveyAdvanceHandlerFactoryImpl(siteInfo));
            monitor.pollAndNotify(dbp);
        });
  }



  private String getProviderId(Database db,  String providerName) {
    if (providers.get(providerName) == null) {
      String providerId = getInternalId(db,
          "SELECT survey_system_id FROM survey_system WHERE survey_system_name = ?", providerName);
      if (providerId != null) {
        providers.put(providerName, providerId);
      }
    }
    return providers.get(providerName);
  }

  private String getSectionId(Database db,  String sectionName) {
    if (studies.get(sectionName) == null) {
      String providerId = getInternalId(db,
          "SELECT study_code FROM study WHERE study_description = ?",sectionName);
      if (providerId != null) {
        studies.put(sectionName, providerId);
      } else {
        return "";
      }
    }
    return studies.get(sectionName);
  }

  private String getInternalId(Database db, String sqlString, String name) {
      return db.toSelect(sqlString).argString(name).query(
          new RowsHandler<String>() {
            @Override
            public String process(Rows rs) throws Exception {
              if (rs.next()) {
                return Integer.toString(rs.getIntegerOrNull());
              }
              return null;
            }
          });
  }

  private int getResearchChoice(String json) {
    if (json != null && json.startsWith("{\"choice\":\"Yes")) {
      return 1;
    }
    if (json != null && json.startsWith("{\"choice\":\"Ask")) {
      return 3;
    }
    return 2;
  }
}
