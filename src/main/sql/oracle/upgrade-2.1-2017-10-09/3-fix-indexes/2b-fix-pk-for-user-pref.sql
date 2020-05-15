@column-formats

-- turn off echoing before/after variable substitution
SET VERIFY OFF
define TABLE = 'USER_PREFERENCE'
define PK = 'USER_PREF_PK'

prompt
prompt
prompt
prompt
prompt ============= 2b.
prompt ============= Fixing the &TABLE primary key to be on 3 columns

-- define a variable as the value of a column
SET TERM OFF
column up_pkname new_value up_pkname
column up_pkname format a25
SELECT (constraint_name) up_pkname 
FROM user_constraints WHERE table_name='&TABLE' AND constraint_type='P';
SET TERM ON

prompt == Dropping old the primary key, named &UP_PKNAME

ALTER TABLE &table DROP CONSTRAINT &up_pkname;

prompt == Creating the new one, named &PK on (USER_PRINCIPAL_ID, SURVEY_SITE_ID, PREFERENCE_KEY);

ALTER TABLE &TABLE ADD CONSTRAINT &PK PRIMARY KEY (USER_PRINCIPAL_ID, SURVEY_SITE_ID, PREFERENCE_KEY);

prompt == The following shows the columns of the primary key now:

column PRIMARY_KEY_NAME format a20
column TABLE_NAME format a16
column COLUMN_NAME format a18

SELECT '    ', i.table_name, i.index_name primary_key_name, col.column_name
  FROM user_indexes i, user_ind_columns col, user_constraints con
 WHERE i.table_name='&TABLE' AND col.table_name='&TABLE' AND con.table_name='&TABLE'
   AND i.index_name=col.index_name AND i.index_name=con.index_name AND con.constraint_type = 'P'
 ORDER by i.index_name, col.column_position, col.column_name;

prompt == There should be 3 lines of output for the 3 columns:
prompt ==   USER_PREFERENCE  USER_PREF_PK         USER_PRINCIPAL_ID
prompt ==   USER_PREFERENCE  USER_PREF_PK         SURVEY_SITE_ID
prompt ==   USER_PREFERENCE  USER_PREF_PK         PREFERENCE_KEY
prompt
prompt == If it is good now, proceed to:  @ 3a-chk-pk-for-survey_reg_attr
prompt
