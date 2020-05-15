-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 6a'
\echo '-- ======== Making the SURVEY_REGISTRATION index on Site and Token unique'
\echo
\echo '-- The previous script should have found no Pairs, Trios or More.'
\echo '--   If there were some, you can not make an index till they are removed, use:  \\i 6b-find-site-token-dups.sql'
\echo

\echo '-- We expect the output from the next query to be:'
\echo '--  | survey_reg_site_token_uq | non-uq | survey_site_id, token'
\echo

SELECT ' ' as "  ", index_name, uniq, index_columns FROM tmp_ixs 
WHERE table_name='survey_registration' AND 
      index_columns = 'survey_site_id, token';

\echo '-- If there are no rows or it says NONUNIQUE, we will need to check for '
\echo '--    duplicates and fix them before creating the unique index-      run:  \\i 6b-sr-chk-site_tok_dups.sql'
\echo
\echo '-- If yours already says UNIQUE,'
\echo '--    if the unique index is on columns TOKEN, SURVEY_SITE_ID'
\echo '--       then they are unique, but we will change the column order-   run:  \\i 6e-sr-fix-site-token-ix.sql'
\echo '--    if the unique index is on columns SURVEY_SITE_ID, TOKEN, good,'
\echo '--       we are done with the index. But there is one more token problem:  \\i 7-fix-act-tokens.sql'
\echo
