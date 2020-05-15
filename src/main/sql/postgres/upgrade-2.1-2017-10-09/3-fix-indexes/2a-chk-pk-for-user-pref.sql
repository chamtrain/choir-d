-- PostGres script
\set IGNOREEOF 3

\set table  user_preference
\set pk  user_preference_pk 

\echo
\echo
\echo
\echo
\echo
\echo '============= 2a.'
\echo '============= Check the primary key for:  :TABLE  ================'
\echo
\echo '== The PK (Primary Key) of table :TABLE should be on '
\echo '==    3 columns:  USER_PRINCIPAL_ID, SURVEY_SITE_ID, PREFERENCE_KEY'
\echo '== Lets look at yours:'

\i list-table-pk.sql

\echo '== if your pk_name is user_principal_id or there are only 2 columns in the pk:'
\echo '== Then fix it with:  \\i 2b-fix-pk-for-user-pref.sql'
\echo
\echo '== Otherwise go to:  \\i 3a-chk-pk-for-survey_reg_attr.sql'
\echo
