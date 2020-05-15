-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 9'
\echo '-- ======== Fixing the 3-column,  SURVEY_REG_PAT_TOK_UQ  index on  SURVEY_REGISTRATION  ========'
\echo
\echo '-- Create a temporary index with one additional column'

CREATE INDEX survey_reg_temporary_ix ON survey_registration (patient_id, token, survey_site_id, SURVEY_ORDER);

\echo '-- Now, drop the old index '

\set index_name survey_reg_pat_tok_uq

SELECT index_name FROM tmp_ixs
 WHERE table_name='survey_registration' AND index_columns like '%,%,%' \gset
;

DROP INDEX :index_name;

\echo '-- make the new UNIQUE index:'

CREATE UNIQUE INDEX survey_reg_site_pat_tok_uq ON survey_registration (survey_site_id, patient_id, token);

\echo '-- and drop the temporary one'

DROP INDEX survey_reg_temporary_ix;

\i mk-tmp-ixs.sql

\echo
\echo '-- We will show you all the indexes on SURVEY_REGISTRATION'
\echo '--   Ensure there is one like the next line'
\echo '-- | survey_reg_pat_tok_uq | non-uq | survey_site_id, patient_id, token'
\echo 

SELECT index_name, uniq, index_columns FROM tmp_ixs
 WHERE table_name='survey_registration' order by index_columns;

\echo
\echo '-- Done (if there were no errors...)   Now quit psql with:  \\q'
\echo '-- Next:    % cd ../3-fix-indexes ; more ReadMe.txt'
\echo
\q
