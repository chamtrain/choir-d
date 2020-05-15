@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 8
prompt -- ======== Is the 3-column SURVEY_REG_PAT_TOK_UQ index on SURVEY_REGISTRATION unique? ========

prompt
prompt -- This next query should produce 3 rows matching:
prompt --   SURVEY_REG_PAT_TOK_UQ        PATIENT_ID                     NONUNIQUE    <- expected output
prompt --   SURVEY_REG_PAT_TOK_UQ        SURVEY_SITE_ID                 NONUNIQUE    <- expected output
prompt --   SURVEY_REG_PAT_TOK_UQ        TOKEN                          NONUNIQUE    <- expected output
prompt

SET VERIFY OFF

SELECT '    ' "----" , i.index_name, c.column_name, i.uniqueness
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name='SURVEY_REGISTRATION' AND c.table_name='SURVEY_REGISTRATION'
   AND i.index_name=c.index_name
   AND i.index_name=UPPER('&SRPATINDEX')
 ORDER BY i.index_name, c.column_name;

SET VERIFY ON

prompt -- If your output matches the expected output above, do:   @ 9-sr-fix-pat-tok
prompt

prompt -- If they say UNIQUE instead of NONUNIQUE, you're done:  quit
prompt -- And next:  % cd ../3-fix-indexes ; more ReadMe.txt
prompt

