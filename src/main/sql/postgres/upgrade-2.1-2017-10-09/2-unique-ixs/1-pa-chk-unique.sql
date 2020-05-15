-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 1'
\echo '-- ======== Is the 3-column PATIENT_ATTRIBUTE index UNIQUE ========'
\echo
\echo '-- The problem: the index PATIENT_ATT_PAT_DAT_UQ on the PATIENT_ATTRIBUTE table was supposed.'
\echo '--      to be unique, but the definition that got to the database lost the "UNIQUE" property'
\echo '--      This index is on the columns:  SURVEY_SITE_ID, PATIENT_ID, DATA_NAME'
\echo

\echo '-- First, we will (drop and) make a temporary table of the indexes'
\i mk-tmp-ixs.sql

\echo '-- We expect the output from the next query will be:'
\echo '--    patient_att_pat_dat_uq | non-uq | survey_site_id, patient_id, data_name'
\echo 

SELECT ' ' as "  ", index_name, uniq, index_columns FROM tmp_ixs 
WHERE table_name='patient_attribute' AND index_columns like '%,%,%';

\echo '-- And this next query tells if you have all unique values of the 3 (trio) columns'
\echo '--   (number_per_trio=1) Or if you have duplicates (2), triplicates or more'
\echo

SELECT number_per_trio, count(*) num_wi_this_many_dups FROM
  (SELECT count(*) number_per_trio FROM patient_attribute 
    GROUP BY survey_site_id, patient_id, data_name
  ) x GROUP BY number_per_trio;

\echo '-- If your index says UNIQUE instead of "non-uq", you should have all singles.'
\echo '--    If this is true for you, run: \\i 6a-chk-sr-site-tok-ix.sql'
\echo
\echo '-- If there is only 1 line, with NUM_SAME_TRIOS = 1 (or you got "(0 rows))'
\echo '--   you have no duplicates, skip to \\i 4-pa-fix-unique-index.sql'
\echo
\echo '-- Otherwise, we must fix the duplicates. Proceed to:  \\i 2-pa-analyze-make-tmp.sql'
\echo '--   Note that it produces a lot of output, fyi, IF there are duplicates.'
\echo '--   It is a lot to study, but was helpful in making sure it is all correct.'
\echo

