-- PostGres script
\set IGNOREEOF 3

\set table  survey_reg_attr 
\set pk  survey_reg_attr_pk 

\echo
\echo
\echo
\echo
\echo '============= 3b.'
\echo '============= Fixing the Survey_Reg_Attr Pk to be 2 columns'

SELECT tc.constraint_name sra_pkname
  FROM information_schema.table_constraints tc 
 WHERE tc.constraint_type = 'PRIMARY KEY' 
       AND tc.table_name = LOWER(:'table') \gset 
;

\echo '== Dropping old the primary key, named: ' :sra_pkname

ALTER TABLE :table DROP CONSTRAINT :sra_pkname;

\echo '== Creating the new one, named :pk on (SURVEY_REG_ID, DATA_NAME);'

ALTER TABLE :table ADD CONSTRAINT :pk PRIMARY KEY (SURVEY_REG_ID, DATA_NAME);

\echo '== The primary key now should have 2 columns, SURVEY_REG_ID, DATA_NAME'

\i list-table-pk.sql

\echo '== If it matches now, proceed to:  \\i 4a-chk-user-principal-name-uq.sql'
\echo
