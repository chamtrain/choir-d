-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 8'
\echo '-- ======== Is the 3-column SURVEY_REG_PAT_TOK_UQ index on SURVEY_REGISTRATION unique?'

\echo
\echo '-- We expect this next query to produce a row like:'
\echo '--  | survey_reg_pat_tok_uq | non-uq | survey_site_id, patient_id, token'
\echo

SELECT '  ' as "  ", index_name, uniq, index_columns FROM tmp_ixs
 WHERE table_name='survey_registration' AND index_columns like '%,%,%';

\echo '-- If your output is "non-uq" like the output above, do:   \\i 9-sr-fix-pat-tok.sql'
\echo
\echo '-- If it says UNIQUE instead of NONUNIQUE, you are done, quit with: \\q'
\echo '-- And next:  % cd ../3-fix-indexes ; more ReadMe.txt'
\echo


