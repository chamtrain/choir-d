-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 6e'
\echo '-- ======== Fixing the 2-column  SURVEY_REG_SITE_TOKEN_UQ  index on  SURVEY_REGISTRATION'

\echo '-- First, create an index with an extra column'

CREATE INDEX survey_reg_temporary_ix ON survey_registration (survey_site_id, token, survey_order);

\echo
\echo '-- Here, again, is the old index'

SELECT ' ' as "  ", index_name, uniq, index_columns FROM tmp_ixs 
WHERE table_name='survey_registration' AND 
      index_columns = 'survey_site_id, token';

-- set a default value - the old name
\set index_name survey_reg_site_token_uq

-- the ending slash-g puts the result into the index_name variable

SELECT index_name FROM tmp_ixs 
WHERE table_name='survey_registration' AND 
      index_columns = 'survey_site_id, token' \gset
;

\echo
\echo '-- We will drop this'
\echo '--   You will get an error if there was no old index, but that is fine.'

DROP INDEX :index_name;

\echo '-- make the new UNIQUE index:'

CREATE UNIQUE INDEX survey_reg_site_token_uq ON survey_registration (survey_site_id, token);

\echo
\echo '-- and drop the temporary one'

DROP INDEX survey_reg_temporary_ix;

\echo
\i mk-tmp-ixs.sql
\echo

SELECT ' ' as "  ", index_name, uniq, index_columns FROM tmp_ixs 
WHERE table_name='survey_registration' ORDER BY index_columns;

\echo '-- Those are all the indexes for this table. We want to see a line like the next:'
\echo '--  | survey_reg_site_token_uq  | UNIQUE | survey_site_id, token'
\echo

\echo '-- Next, we will fix any misleading ACTIVITY tokens:  \\i 7-fix-act-tokens.sql'
\echo

