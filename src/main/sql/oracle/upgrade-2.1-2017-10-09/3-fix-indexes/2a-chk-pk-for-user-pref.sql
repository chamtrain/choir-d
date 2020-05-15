@column-formats

-- turn off echoing before/after variable substitution
SET VERIFY OFF
define TABLE = 'USER_PREFERENCE'
define PK = 'USER_PREFERENCE_PK'

prompt 
prompt 
prompt 
prompt 
prompt 
prompt ============= 2a.
prompt ============= Check the primary key for:  &TABLE  ================
prompt
prompt == The PK (Primary Key) of table &TABLE should be on 
prompt ==    3 columns:  USER_PRINCIPAL_ID, SURVEY_SITE_ID, PREFERENCE_KEY
prompt == Lets look at yours:

column TABLE_NAME format a16
column COLUMN_NAME format a18
column PRIMARY_KEY_NAME format a20

SELECT '   ', i.table_name, i.index_name primary_key_name, col.column_name
  FROM user_indexes i, user_ind_columns col, user_constraints con
 WHERE i.table_name='&TABLE' AND col.table_name='&TABLE' AND con.table_name='&TABLE'
   AND i.index_name=col.index_name AND i.index_name=con.index_name AND con.constraint_type = 'P'
 ORDER by i.index_name, col.column_position, col.column_name;

prompt == if your output is NOT:
prompt ==  USER_PREFERENCE  USER_PREFERENCE_PK   USER_PRINCIPAL_ID
prompt ==  USER_PREFERENCE  USER_PREFERENCE_PK   SURVEY_SITE_ID
prompt ==  USER_PREFERENCE  USER_PREFERENCE_PK   PREFERENCE_KEY
prompt
prompt == Then fix it with:  @ 2b-fix-pk-for-user-pref
prompt
prompt == Otherwise go to:  @ 3a-chk-pk-for-survey_reg_attr
prompt
