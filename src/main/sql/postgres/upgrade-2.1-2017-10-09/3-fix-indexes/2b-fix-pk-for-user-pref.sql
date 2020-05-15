-- PostGres script
\set IGNOREEOF 3

\set table  user_preference 
\set pk  user_pref_pk 

\echo
\echo
\echo
\echo
\echo '============= 2b.'
\echo '============= Fixing the :TABLE primary key to be on 3 columns'

-- define a variable as the value of a column

SELECT tc.constraint_name up_pkname
  FROM information_schema.table_constraints tc 
 WHERE tc.constraint_type = 'PRIMARY KEY' 
       AND tc.table_name = LOWER(:'table') \gset 
;

\echo '== Dropping old the primary key, named: ' :up_pkname

ALTER TABLE user_preference DROP CONSTRAINT :up_pkname;

\echo '== Creating the new one, named :PK on (USER_PRINCIPAL_ID, SURVEY_SITE_ID, PREFERENCE_KEY);'

ALTER TABLE user_preference ADD CONSTRAINT user_pref_pk PRIMARY KEy (user_principal_id, survey_site_id, preference_key);

\echo '== The following shows the columns of the primary key now:'

\i list-table-pk.sql

\echo '== There should be 3 columns.'
\echo '== If it is good now, proceed to:  \\i 3a-chk-pk-for-survey_reg_attr.sql'
\echo
