-- this sets up column formats for sqlplus

set linesize 200
set pagesize 1000
column INDEX_NAME format a25
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
column primary_key_name	  format a25
column constraint_name    format a25
column index_columns      format a55
