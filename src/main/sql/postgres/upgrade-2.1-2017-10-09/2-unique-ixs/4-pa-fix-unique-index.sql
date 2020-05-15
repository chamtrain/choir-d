-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 4'
\echo '-- ======== Change the patient_att_pat_dat_uq index to be UNIQUE ========'

\echo
\echo '-- First we will drop the temporary table, if it exists'

DROP TABLE IF EXISTS tmp_dup_patient_attribute;

\echo
\echo '-- Create a new temporary index, on the same 3 columns + meta_version'

CREATE UNIQUE INDEX patient_att_temporary ON patient_attribute (SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, META_VERSION);

\echo '-- drop the old index'

DROP          INDEX patient_att_pat_dat_uq;

\echo '-- create the new index'

CREATE UNIQUE INDEX patient_att_pat_dat_uq ON patient_attribute (SURVEY_SITE_ID, PATIENT_ID, DATA_NAME);

\echo
\echo '-- and drop the temporary index'
\echo

DROP INDEX patient_att_temporary;

\echo
\echo '-- We will drop and re-make the temporaray index table'
\i mk-tmp-ixs.sql
\echo '-- To confirm, here are the 3-column indexes on patient_attribute, showing uniqueness'
\echo

SELECT ' ' as "  ", index_name, uniq, index_columns FROM tmp_ixs 
WHERE table_name='patient_attribute' AND index_columns like '%,%,%';

\echo
\echo '-- There is no step 5, next is:  \\i 6a-chk-sr-site-tok-ix.sql'
\echo

