-- this sets up column formats for sqlplus

set linesize 200
set pagesize 1000
column INDEX_NAME format a28
column TABLE_NAME format a30
column COLUMN_NAME format a30
column UNIQUENESS format a9
column PAT_ID format a8
column PATIENT_ID format a8
column PATIENT format a8
column MAX_CHGD format a25
column MAX_DATE format a25
column NUM_VALUES format 9999999999
column NUM_VALS format 99999999
column NUM_CHGD format 99999999
column row_count format 99999999
column whole_time_span format a25
column delta_created format a25
column SITE     format 9999
column DATA_NAME format a15
column DATA_TYPE format a7
column DATA_VALUE format a20
column DT_CHANGED format a25
column DT_CREATED format a25
column WHOLE_TIME_SPAN format a25
column num_patients format 999999999999
column num_patient_attrs format  9999999999999999
column num_attr_histories format 999999999999999999
column num_dup_trios      format 9999999999999
column num_dup_rows       format 999999999999
column SURVEY_TYPE        format a24
column asreg_id           format 9999999999999999999
column assessment_reg_id  format 9999999999999999999
-- if you came here from script 1, edit this:
define PAINDEX=PATIENT_ATT_PAT_DAT_UQ

-- if you came here from script 6, edit this:
define SRSITEINDEX=SURVEY_REG_SITE_TOKEN_UQ

-- if you came here from script 8, edit this:
define SRPATINDEX=SURVEY_REG_PAT_TOK_UQ

