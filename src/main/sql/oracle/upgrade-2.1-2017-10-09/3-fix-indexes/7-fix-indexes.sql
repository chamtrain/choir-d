@column-formats
prompt 
prompt 
prompt 
prompt 
prompt ============= 7
prompt ============= Fix indexes on PATIENT_STUDY

column TABLE_NAME format a13
column COLUMN_NAME format a21


prompt ==== 1. on PATIENT_STUDY keep 2 and drop 1
-- keep patient_study_site_token_idx  on patient_id have survey_site_id and token

prompt ==   1a. Make one on patient/site/token for most queries, and to satisfy the FK on patient_id
CREATE INDEX patient_study_pat_site_tok_idx ON PATIENT_STUDY (patient_id, survey_site_id, token);

prompt ==   1b. Drop the now redundant PATIENT_STUDY_PAT_TOKEN_INX on survey_site_id, patient_id, token
DROP INDEX patient_study_pat_token_inx;

prompt ==   1c. Add the order number to a site, token index
CREATE INDEX patient_study_site_token_o_idx on PATIENT_STUDY (survey_site_id, token, order_number);

prompt == Continue with: @ 8-fix-indexes
prompt
set sqlprompt '(next is @8-fix-indexes) SQL> '
