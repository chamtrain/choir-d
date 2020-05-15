-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo '============= 6.sql'
\echo '============= Fix indexes on 3 tables:  .sql'
\echo '==            ASSESSMENT_REGISTRATION.sql'
\echo '==            APPOINTMENT_REGISTRATION.sql'


\echo
\echo '== We reviewed the indexes.'
\echo '== It is good practice to have an index for each foreign key.'
\echo '==   But if the PK or another index already starts with the foreign key column(s)'
\echo '==   then no additional index is needed.'
\echo '== The database layer was making indexes for foreign keys, unless an EXACT match occurred.'
\echo '==   We fixed it, but existing systems have extra indexes.'
\echo '== Plus, having an index on a foreign key is an opportunity to add more columns to aid queries.'
\echo '==   So we are making indexes with some extra columns here.'
\echo '== In the nexzt script, we will query the database for all duplicate indexes to remove.'
\echo


\echo '==== 1. Add an index:  ASSESSMENT_REGISTRATION (site, assessment_type)'
CREATE INDEX asmt_reg_site_astype_idx ON assessment_registration (survey_site_id, assessment_type);


\echo '==== 2. Add an index: APPT_REGISTRATION (patient, site, visit_dt) instead of just (patient)'
CREATE INDEX appt_reg_pat_site_visdt_idx ON appt_registration (patient_id, survey_site_id, visit_dt);


\echo '==-- 3. Add one on (assessment_reg, site, patient) instead of just (assessment)'
CREATE INDEX appt_reg_asmt_site_pat_idx ON appt_registration (assessment_reg_id,survey_site_id,patient_id);


\echo '============= Fix indexes on PATIENT_STUDY.sql'

\echo '==   1a. Make one on patient/site/token for most queries, and to satisfy the FK on patient_id.sql'
CREATE INDEX patient_study_pat_site_tok_idx ON PATIENT_STUDY (patient_id, survey_site_id, token);

\echo '==   1b. Drop the now redundant PATIENT_STUDY_PAT_TOKEN_INX on survey_site_id, patient_id, token.sql'
DROP INDEX patient_study_pat_token_inx;

\echo '==   1c. Add the order number to a site, token index.sql'
CREATE INDEX patient_study_site_token_o_idx on PATIENT_STUDY (survey_site_id, token, order_number);


\echo '======== NOTIFICATION.sql'

\echo '== Add index NOT_PATIENT_SITE_SURDT_IDX on patient, site, survey_dt.sql'
create index NOT_PATIENT_SITE_SURDT_IDX on NOTIFICATION (patient_id, survey_site_id, survey_dt);

\echo '== Add index NOTIFICATION_SITE_SURDT_IDX on:   site, survey_dt.sql'
create index NOTIFICATION_SITE_SURDT_IDX on NOTIFICATION (survey_site_id, survey_dt);


\echo
\echo '======== PATIENT_ATTRIBUTE_HISTORY.sql'

\echo '== add index PAT_ATT_HIS_SITE_PAT_NAME_IDX on:  survey_site_id, patient_id, data_name.sql'
create index PAT_ATT_HIS_SITE_PAT_NAME_IDX on PATIENT_ATTRIBUTE_HISTORY (survey_site_id, patient_id, data_name);

\echo '== drop unnec and poorly named index PATIENT_ATT_HIS_PAT_ID_IDX on patient_id, data_name.sql'
drop index PATIENT_ATT_HIS_PAT_ID_IDX;


\echo
\echo '======== PATIENT_RESULT_TYPE.sql'

\echo '== add a better index PATIENT_RES_TYP_SITE_NAME_IDX:  UNIQUE on site, result_name.sql'
create UNIQUE index PATIENT_RES_TYP_SITE_NAME_IDX on PATIENT_RESULT_TYPE (survey_site_id, result_name);


\echo
\echo '======== PATIENT_RESULT.sql'
\echo '== add a better index PAT_RES_SITE_ASMTREG_IDX on site, assessment_reg_id, patient_res_typ_id.sql'
create index PAT_RES_SITE_ASMTREG_TYPE_IDX on PATIENT_RESULT (survey_site_id, assessment_reg_id, patient_res_typ_id);


\echo
\echo '======== APP_CONFIG - add 2 indexes for queries.sql'
\echo '== add index APP_CONFIG_SITE_ENA_TYPE_IX on:  site, enabled, config_type.sql'
create index APP_CONFIG_SITE_ENA_TYPE_IX on APP_CONFIG (survey_site_id, enabled, config_type);

\echo '== add unique index APP_CONFIG_SITE_TYPE_NAME_UQ on:  survey_site_id", "config_type", "config_name.sql'
create index APP_CONFIG_SITE_TYPE_NAME_UQ on APP_CONFIG (survey_site_id, config_type, config_name);


\echo '== You can check these tables with:  \\d table_name'
\echo
\echo '== Just one more: \\i 7-remove-redundant-ixs.sql'
\echo
