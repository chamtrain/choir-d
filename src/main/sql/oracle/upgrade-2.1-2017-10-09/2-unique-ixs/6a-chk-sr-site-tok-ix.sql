@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 6a
prompt -- ======== Making the SURVEY_REGISTRATION index on Site and Token unique
prompt
prompt -- The previous script should have found no Pairs, Trios or More.
prompt --   If there were some, you can not make an index till they are removed, use:  @ 6b-find-site-token-dups
prompt

prompt -- We expect the output from the next query to be:
prompt --   SURVEY_REG_SITE_TOKEN_UQ       NONUNIQUE SURVEY_SITE_ID, TOKEN    <- expected output

SET VERIFY OFF

-- set this variable to the name of the index, if there is one
define SRSITEINDEX=there_was_no_index
column site_token_index_name new_value SRSITEINDEX

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

SET VERIFY ON

prompt -- If there are no rows, there is no index on SURVEY_SITE_ID and TOKEN.
prompt --    confirm this by running:  @ sr-show-ixs.sql
prompt --    and then continue with:   @ 6b-sr-chk-site_tok_dups
prompt
prompt -- If it says NONUNIQUE, we will need to check for
prompt --    duplicates and fix them before creating the unique index-      run:  @ 6b-sr-chk-site_tok_dups
prompt
prompt -- If yours already says UNIQUE,
prompt --    if the unique index is on columns TOKEN, SURVEY_SITE_ID
prompt --       then they're unique, but we will change the column order-   run:  @ 6e-sr-fix-site-token-ix
prompt --    if the unique index is on columns SURVEY_SITE_ID, TOKEN, good,
prompt --       we are done with the index. But there is one more token problem:  @ 7-fix-act-tokens
prompt
