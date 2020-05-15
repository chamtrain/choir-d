@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 6e
prompt -- ======== Fixing the 2-column  SURVEY_REG_SITE_TOKEN_UQ  index on  SURVEY_REGISTRATION  ========

prompt -- First, create an index with an extra column

CREATE INDEX survey_reg_temporary_ix ON survey_registration (survey_site_id, token, survey_order);

SET VERIFY OFF
prompt -- Now, drop the old index
prompt --   You will get an error if there was no old index, but that is fine.

DROP INDEX &SRSITEINDEX;

prompt -- make the new UNIQUE index:

CREATE UNIQUE INDEX survey_reg_site_token_uq ON survey_registration (survey_site_id, token);

prompt -- and drop the temporary one

DROP INDEX survey_reg_temporary_ix;

prompt -- The output from the next query should now be:
prompt --   SURVEY_REG_SITE_TOKEN_UQ       UNIQUE    SURVEY_SITE_ID, TOKEN    <- expected output

column position format 99999999

-- if this query finds one, the "new_value" above will set SRSITEINDEX
column site_token_index_name format a30
column index_columns format a50

SELECT '    ' "    ", index_name site_token_index_name, uniqueness, index_columns FROM (
  SELECT i.index_name, i.uniqueness,
         listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
    FROM user_indexes i, user_ind_columns c
   WHERE i.table_name='SURVEY_REGISTRATION' AND c.table_name='SURVEY_REGISTRATION'
     AND i.index_name=c.index_name
   GROUP by i.index_name, i.uniqueness
) where index_columns IN ('TOKEN, SURVEY_SITE_ID', 'SURVEY_SITE_ID, TOKEN');

prompt -- Next, we'll fix the misleading ACTIVITY tokens:  @ 7-fix-act-tokens
prompt

SET VERIFY ON
