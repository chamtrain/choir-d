-- PostGres script
\set IGNOREEOF 3

\echo
\echo
\echo
\echo
\echo
\echo '============= 3a.'
\echo '============= Check primary key for:  Survey_Reg_Attr_Pk  ================'
\echo
\echo '== The PK (Primary Key) of table SURVEY_REG_ATTR should be on 2 columns: '
\echo '==    2 columns:  SURVEY_REG_ID, DATA_NAME'
\echo '== Lets look at yours:'

\set table  survey_reg_attr
\i list-table-pk.sql

\echo '== If you do not have both columns in your index_columns'
\echo '== Then fix it with:  \\i 3b-fix-pk-for-survey_reg_attr.sql'
\echo
\echo '== Otherwise go to:  \\i 4a-chk-user-principal-name-uq.sql'
\echo
