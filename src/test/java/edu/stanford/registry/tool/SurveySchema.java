/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.tool;

import com.github.susom.database.Flavor;
import com.github.susom.database.Schema;

/**
 * Representation of the survey related database objects.
 *
 * Note:  The database layer adds an index on a foreign key if one is needed.
 * We should scrutinize these and add our own index with more columns to help performance.
 * Indexes cost overhead, so we might as well make them more useful.
 *
 * @author garricko
 */
public class SurveySchema {
  public static Schema create() {
    return new Schema()
      .addTable("survey_site")
        .withComment("A department, clinic, etc. for multi-tenant segregation of data.\n"
            + "The rows in this table are not currently runtime configurable, and must\n"
            + "match the Java code - see SurveySystemFactoryImpl.\n"
            + "Many configuration parameters in appConfig customize the site.")
        .addColumn("survey_site_id").primaryKey().table()
        .addColumn("url_param").asString(80)
          .withComment("A short public identifier for this site, seen in the url and passed in email links").table()
        .addColumn("display_name").asString(255)
          .withComment("Title to display in the survey header").table()
        .addColumn("enabled").asBoolean().notNull().table()
        .addCheck("survey_site_enabled_bool_ck", "enabled in ('N', 'Y')").table()
        .schema()

      .addTable("survey_token")
        .withComment("Record of reserved survey tokens to ensure uniqueness within each site")
        .addColumn("survey_token_id").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("survey_token").asString(4000).notNull()
          .withComment("Uniquely identifies a survey within survey_site_id").table()
        .addColumn("is_complete").asStringFixed(1).notNull().table()
        .addColumn("last_session_number").asInteger().notNull()
          .withComment("0==>not started; most recent sent to client otherwise").table()
        .addColumn("last_step_number").asInteger().notNull()
          .withComment("0==>not started; most recent sent to client otherwise").table()
        .addCheck("survey_token_is_complete_ck", "is_complete in ('N', 'Y')").table()
        .addForeignKey("survey_token_site_fk", "survey_site_id").references("survey_site").table()
        .addIndex("survey_token_site_token_uq", "survey_site_id", "survey_token").unique().table().schema()

      .addTable("survey_session")
        .withComment("All active and expired browser sessions associated with a survey token")
        .addColumn("survey_site_id").asLong().notNull().withComment("pk1 of 3, fk").table()
        .addColumn("survey_token_id").asLong().notNull().withComment("pk2 of 3, fk").table()
        .addColumn("session_number").asInteger().notNull()
          .withComment("pk3 of 3, 1..n sequence of sessions for survey_token_id").table()
        .addColumn("session_token").asString(4000).notNull().unique("s_sess_stoken_uq")
          .withComment("Uniquely identifies a survey within survey_site_id").table()
        .addColumn("resume_token").asString(4000).notNull().unique("s_sess_rtoken_uq")
          .withComment("Can be persisted by browser to restart this survey").table()
        .addColumn("last_active").asDate().notNull()
          .withComment("In server time; touch at each step, timeout tokens from this time").table()
        .addColumn("start_time").asDate().notNull()
          .withComment("Time this session token became valid for use").table()
        .addColumn("expired_time").asDate()
          .withComment("Time this session token expired; null if it is active").table()
        .addColumn("expired_reason").asStringFixed(1)
          .withComment("Flag: S=start/restart, R=resume token used, I=invalidated").table()
        .addPrimaryKey("survey_session_pk", "survey_site_id", "survey_token_id", "session_number").table()
        .addCheck("survey_session_exp_reason_ck", "expired_reason in ('S', 'R', 'I')").table()
        .addForeignKey("s_sess_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("s_sess_token_fk", "survey_token_id").references("survey_token").table().schema()

      .addTable("survey_user_agent")
        .withComment("Record browser user agent strings, one row per unique string")
        .addColumn("survey_user_agent_id").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("user_agent_md5").asString(80).notNull().table()
        .addColumn("user_agent_str").asClob().notNull().table()
        .addForeignKey("s_ua_site_fk", "survey_site_id").references("survey_site").table()
        .addIndex("survey_ua_site_md5_uq", "survey_site_id", "user_agent_md5").unique().table()
        .customTableClause(Flavor.oracle, "lob(user_agent_str) store as securefile").schema()

      .addTable("survey_json")
        .withComment("Record JSON moving between client and server")
        .addColumn("survey_json_id").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("json").asClob().notNull().table()
        .addForeignKey("s_json_site_fk", "survey_site_id").references("survey_site").table()
        .customTableClause(Flavor.oracle, "lob(json) store as securefile (deduplicate)").schema()

      .addTable("survey_progress")
        .withComment("Every step in a survey (a \"page\" displayed to the user) is recorded in sequence here,\n"
            + "along with information to help decide what the next question should be")
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("survey_token_id").asLong().notNull().table()
        .addColumn("step_number").asInteger().notNull().table()
        .addColumn("step_status").asStringFixed(1).notNull()
          .withComment("Flag: Q=question sent, X=server validation failed, A=answer accepted").table()
        .addColumn("question_step_number").asInteger().notNull()
          .withComment("step_number of first send of this question (no validation)").table()
        .addColumn("survey_name").asString(4000)
          .withComment("Bookkeeping to help SurveySystem implementations choose the next question:\n"
              + "Survey containing this step, in case we bundled multiple surveys together").table()
        .addColumn("survey_compat_level").asInteger()
          .withComment("Bookkeeping to help SurveySystem implementations choose the next question:\n"
              + "Version of this survey at the time of this question, in case of upgrade during survey").table()
        .addColumn("provider_id").asString(4000)
          .withComment("Bookkeeping to help SurveySystem implementations choose the next question:\n"
              + "SurveySystem implementations can choose to use this").table()
        .addColumn("section_id").asString(4000)
          .withComment("Bookkeeping to help SurveySystem implementations choose the next question:\n"
              + "SurveySystem implementations can choose to use this").table()
        .addColumn("question_id").asString(4000)
          .withComment("Bookkeeping to help SurveySystem implementations choose the next question:\n"
              + "Primary means of tracking and identifying a \"question\" (one page in survey)").table()
        .addColumn("question_type").asString(4000).notNull().table()
        .addColumn("question_api_compat_level").asInteger().notNull().table()
        .addColumn("display_status_json_id").asLong().notNull().table()
        .addColumn("question_json_id").asLong().notNull().table()
        .addColumn("question_time").asDate().notNull().table()
        .addColumn("answer_api_compat_level").asInteger().table()
        .addColumn("submit_status_json_id").asLong().table()
        .addColumn("answer_json_id").asLong().table()
        .addColumn("answer_time").asDate().table()
        .addColumn("call_time_millis").asInteger()
          .withComment("Client measured time to call server and get question").table()
        .addColumn("render_time_millis").asInteger()
          .withComment("Client measured time to display question to screen").table()
        .addColumn("think_time_millis").asInteger()
          .withComment("Client measured time waiting for user to submit answer").table()
        .addColumn("retry_count").asInteger()
          .withComment("Number of times the client timed out and re-sent answer").table()
        .addColumn("user_agent_id").asLong().table()
        .addColumn("client_ip_address").asString(4000).table()
        .addColumn("device_token").asString(4000)
          .withComment("Token to be assigned when registering kiosks/tablets").table()
        .addPrimaryKey("survey_progress_pk", "survey_site_id", "survey_token_id", "step_number").table()
        .addForeignKey("s_prog_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("s_prog_token_fk", "survey_token_id").references("survey_token").table()
        .addForeignKey("s_prog_user_agent_fk", "user_agent_id").references("survey_user_agent").table()
        .addForeignKey("s_prog_display_st_fk", "display_status_json_id").references("survey_json").table()
        .addForeignKey("s_prog_question_fk", "question_json_id").references("survey_json").table()
        .addForeignKey("s_prog_submit_st_fk", "submit_status_json_id").references("survey_json").table()
        .addForeignKey("s_prog_answer_fk", "answer_json_id").references("survey_json").table()
        .addCheck("s_prog_step_status_ck", "step_status in ('Q', 'X', 'A')").table().schema()

      .addTable("survey_progress_dup")
        .withComment("Duplicate submissions of answers that we are ignoring")
        .addColumn("survey_progress_dup_id").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("survey_token_id").asLong().notNull().table()
        .addColumn("step_number").asInteger().notNull().table()
        .addColumn("question_type").asString(4000).notNull().table()
        .addColumn("question_api_compat_level").asInteger().table()
        .addColumn("answer_api_compat_level").asInteger().table()
        .addColumn("submit_status_json_id").asLong().table()
        .addColumn("answer_json_id").asLong().table()
        .addColumn("answer_time").asDate().notNull().table()
        .addColumn("call_time_millis").asInteger()
          .withComment("Client measured time to call server and get question").table()
        .addColumn("render_time_millis").asInteger()
          .withComment("Client measured time to display question to screen").table()
        .addColumn("think_time_millis").asInteger()
          .withComment("Client measured time waiting for user to submit answer").table()
        .addColumn("retry_count").asInteger()
          .withComment("Number of times the client timed out and re-sent answer").table()
        .addForeignKey("s_prog_dup_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("s_prog_dup_token_fk", "survey_token_id").references("survey_token").table()
        .addForeignKey("s_prog_dup_submit_st_fk", "submit_status_json_id").references("survey_json").table()
        .addForeignKey("s_prog_dup_answer_fk", "answer_json_id").references("survey_json").table().schema()

      .addTable("survey_complete")
        .withComment("Record completed surveys here for change tracking purposes")
        .addColumn("complete_sequence").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("survey_token_id").asLong().notNull().table()
        .addForeignKey("s_comp_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("s_comp_token_fk", "survey_token_id").references("survey_token").table()
        .addIndex("s_comp_site_token_uq", "survey_site_id", "survey_token_id").table().schema()

      .addTable("survey_complete_push")
        .withComment("Bookkeeping information for survey completion push notifications")
        .addColumn("survey_recipient_id").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("recipient_name").asString(80).table()
        .addColumn("recipient_display_name").asString(80).table()
        .addColumn("pushed_survey_sequence").asLong().withComment("The last successfully sent survey").table()
        .addColumn("last_pushed_time").asDate()
          .withComment("The time we successfully sent pushed_survey_sequence").table()
        .addColumn("failed_survey_sequence").asLong()
          .withComment("The latest push attempted and failed, or null if last push succeeded").table()
        .addColumn("failed_count").asInteger().notNull()
          .withComment("The number of times we have tried to send failed_survey_sequence, or 0").table()
        .addColumn("last_failed_time").asDate()
          .withComment("The most recent time we attempted to send failed_survey_sequence").table()
        .addColumn("is_enabled").asStringFixed(1).table()
        .addForeignKey("s_comp_push_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("survey_complete_push_fk", "pushed_survey_sequence").references("survey_complete").table()
        .addCheck("s_comp_push_enabled_ck", "is_enabled in ('N', 'Y')").table().schema()

      .addTable("survey_advance")
        .withComment("Record survey advancement (from both survey_progress and survey_player_progress)"
            + " here for change tracking purposes. One row may represent any amount of progress. The"
            + " only guarantees are that a row in here indicates some progress, and all progress will"
            + " eventually be recorded here (it is done asynchronously with no time guarantee).")
        .addColumn("advance_sequence").primaryKey().table()
        .addColumn("advance_time").asDate().notNull().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("survey_token_id").asLong().notNull().table()
        .addColumn("is_complete").asBoolean().notNull().table()
        .addColumn("last_step_number").asInteger().notNull().table()
        .addColumn("player_progress_count").asInteger().notNull()
          .withComment("Sum the number of rows in survey_player_progress for this survey at the time of this advance,"
              + " which effectively indicates whether a player progressed in some way.").table()
        .addForeignKey("s_adv_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("s_adv_token_fk", "survey_token_id").references("survey_token").table()
        .addIndex("s_adv_site_token_uq", "survey_site_id", "survey_token_id", "last_step_number",
            "player_progress_count").unique().table().schema()

      .addTable("survey_advance_status")
        .withComment("Provide locking and timestamp tracking for processes maintaining survey_advance.")
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("check_time").asDate().notNull().table().schema()

      .addTable("survey_advance_push")
        .withComment("Bookkeeping information for survey completion push notifications")
        .addColumn("survey_recipient_id").primaryKey().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("recipient_name").asString(80).table()
        .addColumn("recipient_display_name").asString(80).table()
        .addColumn("pushed_survey_sequence").asLong().withComment("The last successfully sent survey").table()
        .addColumn("last_pushed_time").asDate()
          .withComment("The time we successfully sent pushed_survey_sequence").table()
        .addColumn("failed_survey_sequence").asLong()
          .withComment("The latest push attempted and failed, or null if last push succeeded").table()
        .addColumn("failed_count").asInteger().notNull()
          .withComment("The number of times we have tried to send failed_survey_sequence, or 0").table()
        .addColumn("last_failed_time").asDate()
          .withComment("The most recent time we attempted to send failed_survey_sequence").table()
        .addColumn("is_enabled").asBoolean().table()
        .addForeignKey("s_adv_push_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("survey_adv_push_fk", "pushed_survey_sequence").references("survey_advance").table()
        .addCheck("s_adv_push_enabled_ck", "is_enabled in ('N', 'Y')").table().schema()

      .addTable("survey_player_progress")
        .withComment("Record survey video player actions here")
        .addColumn("survey_player_progress_id").primaryKey().table()
        .addColumn("survey_token_id").asLong().notNull().table()
        .addColumn("survey_site_id").asLong().notNull().table()
        .addColumn("player_id").asString(255).table()
        .addColumn("player_action").asString(255).table()
        .addColumn("player_time_millis").asInteger().withComment("Represents the number of milliseconds into the video").table()
        .addColumn("posted_time").asDate().notNull().table()
        .addForeignKey("s_player_progress_site_fk", "survey_site_id").references("survey_site").table()
        .addForeignKey("s_player_progress_token_fk", "survey_token_id").references("survey_token").table()
        .schema()
      .addSequence("survey_block_seq").max(799999999999999999L).increment(1000).start(1000).schema()
      .addSequence("survey_seq").min(800000000000000000L).max(999999999999999999L).schema()
      .addSequence("survey_complete_seq").max(999999999999999999L).order().schema()
      .addSequence("survey_advance_seq").max(999999999999999999L).order().schema();
  }
}
