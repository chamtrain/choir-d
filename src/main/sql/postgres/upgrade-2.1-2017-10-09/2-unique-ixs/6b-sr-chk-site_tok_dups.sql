-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 6b'
\echo '-- ======== Checking for duplicate SITE+TOKEN rows in SURVEY_REGISTRATION ========'
\echo
\echo '-- We want the site+token to be unique.'
\echo '-- There is a 50% chance you will get a duplicate token after 77,000 survey assignments.'
\echo '-- This query tells if all the site+tokesn are unique, or if there are pairs, trios or more'
\echo '--   (If no rows are found, no surveys have been assigned, that is fine.)'
\echo

SELECT CASE WHEN num_duplicates = 1 THEN 'unique'
            WHEN num_duplicates = 2 THEN 'pairs'
	    WHEN num_duplicates = 3 THEN 'trios' ELSE 'more' END num_same, 
       count(*) counts FROM
  (SELECT count(*) num_duplicates FROM survey_registration 
    GROUP BY survey_site_id, token, survey_name
  ) x GROUP BY num_duplicates;

\echo
\echo '-- You CAN NOT make the index unique unless there are only unique values.'
\echo
\echo '-- If there are duplicates (pairs, trios or more), handle them with:   \\i 6c-sr-mk-tmp-tables.sql'
\echo
\echo '-- If there are no duplicates, or no rows at all, create the unique index with:  \\i 6e-sr-fix-site-token-ix.sql'
\echo
