@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 9
prompt -- ======== Fixing the 3-column,  SURVEY_REG_PAT_TOK_UQ  index on  SURVEY_REGISTRATION  ========

prompt -- Create a temporary index with one additional column

CREATE INDEX survey_reg_temporary_ix ON survey_registration (patient_id, token, survey_site_id, SURVEY_ORDER);

prompt -- Now, drop the old index 

DROP INDEX &SRPATINDEX;

prompt -- make the new UNIQUE index:

CREATE UNIQUE INDEX survey_reg_pat_tok_uq ON survey_registration (patient_id, token, survey_site_id);

prompt -- and drop the temporary one

DROP INDEX survey_reg_temporary_ix;

prompt --
prompt -- This next query should produce 3 rows matching:
prompt
prompt --   SURVEY_REG_PAT_TOK_UQ     PATIENT_ID                     UNIQUE    <- expected output
prompt --   SURVEY_REG_PAT_TOK_UQ     SURVEY_SITE_ID                 UNIQUE    <- expected output
prompt --   SURVEY_REG_PAT_TOK_UQ     TOKEN                          UNIQUE    <- expected output
prompt

SELECT '    ' "----" , i.index_name, c.column_name, i.uniqueness
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name='SURVEY_REGISTRATION' AND c.table_name='SURVEY_REGISTRATION'
   AND i.index_name=c.index_name
   AND i.index_name='SURVEY_REG_PAT_TOK_UQ'
 ORDER by i.index_name, c.column_name;

prompt -- Done (if there were no errors...), and quitting sqlplus
prompt -- Next  % cd ../3-fix-indexes ; more ReadMe.txt
prompt
quit
