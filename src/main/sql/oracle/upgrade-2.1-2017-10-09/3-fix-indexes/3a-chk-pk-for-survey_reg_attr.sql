@column-formats

prompt 
prompt 
prompt 
prompt 
prompt 
prompt ============= 3a.
prompt ============= Check primary key for:  Survey_Reg_Attr_Pk  ================
prompt
prompt == The PK (Primary Key) of table SURVEY_REG_ATTR should be on 2 columns: 
prompt ==    2 columns:  SURVEY_REG_ID, DATA_NAME
prompt == Lets look at yours:

column TABLE_NAME format a16
column COLUMN_NAME format a18

SELECT '    ', i.table_name, i.index_name primary_key_name, col.column_name
  FROM user_indexes i, user_ind_columns col, user_constraints con
 WHERE i.table_name='SURVEY_REG_ATTR' AND col.table_name='SURVEY_REG_ATTR' AND con.table_name='SURVEY_REG_ATTR'
   AND i.index_name=col.index_name AND i.index_name=con.index_name AND con.constraint_type = 'P'
 ORDER by i.index_name, col.column_position, col.column_name;

prompt == if your output is NOT:
prompt ==   SURVEY_REG_ATTR  SURVEY_REG_ATTR_PK        SURVEY_REG_ID
prompt ==   SURVEY_REG_ATTR  SURVEY_REG_ATTR_PK        DATA_NAME
prompt
prompt == Then fix it with:  @ 3b-fix-pk-for-survey_reg_attr
prompt
prompt == Otherwise go to:  @ 4a-chk-user-principal-name-uq
prompt
prompt == You can also look at all of its indexes:  @ sra-list
prompt
