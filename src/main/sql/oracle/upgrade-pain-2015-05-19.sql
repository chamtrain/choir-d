-- Just for installation using the adult pain surveys
--
-- Create a square table to represent survey results, and
-- register a push event listener to populate it.

create table rpt_pain_surveys_square (
  survey_site_id                 numeric(19),
  survey_token_id                numeric(19),
  assessment_type                varchar2(50),
  survey_scheduled               timestamp(3),
  survey_started                 timestamp(3),
  survey_ended                   timestamp(3),
  is_complete                    char(1),
  survey_user_time_ms            numeric(19),
  patient_id                     varchar2(50),
  patient_dob                    timestamp(3),
  patient_gender                 varchar2(250),
  patient_race                   varchar2(250),
  patient_ethnicity              varchar2(250),
  patient_name_self_report       varchar2(4000),
  was_assisted                   varchar2(4000),
  assisted_by                    varchar2(4000),
  bodymap_regions_csv            varchar2(4000),
  pain_intensity_worst           numeric(10),
  pain_intensity_average         numeric(10),
  pain_intensity_now             numeric(10),
  pain_intensity_least           numeric(10),
  pat_or_dr_questions            varchar2(4000),
  promis_pain_interference       numeric(10,5),
  promis_pain_behavior           numeric(10,5),
  promis_physical_function       numeric(10,5),
  promis_fatigue                 numeric(10,5),
  promis_depression              numeric(10,5),
  promis_anxiety                 numeric(10,5),
  promis_sleep_disturb_v1_0      numeric(10,5),
  promis_sleep_impair_v1_0       numeric(10,5),
  promis_anger_v1_0              numeric(10,5),
  promis_emot_support_v2_0       numeric(10,5),
  promis_sat_roles_act_v2_0      numeric(10,5),
  promis_social_iso_v2_0         numeric(10,5),
  constraint rpt_pain_surveys_square_pk     primary key (survey_site_id, survey_token_id)
);

insert into survey_advance_push (survey_site_id, survey_recipient_id, recipient_name,
                                 recipient_display_name, pushed_survey_sequence, failed_count, is_enabled)
values (1, 1001, 'squareTable', 'Put survey results in square table', null, 0, 'Y');
