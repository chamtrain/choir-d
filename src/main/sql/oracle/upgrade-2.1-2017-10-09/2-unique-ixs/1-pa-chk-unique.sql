@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 1
prompt -- ======== Making the  PATIENT_ATTRIBUTEs, and the index, UNIQUE ========
prompt
prompt -- The problem: the index PATIENT_ATT_PAT_DAT_UQ on the PATIENT_ATTRIBUTE table is not unique.
prompt --      This index is on the columns:  SURVEY_SITE_ID, PATIENT_ID, DATA_NAME
prompt
prompt -- We expect the output from the next query will be:
prompt --   PATIENT_ATT_PAT_DAT_UQ    SURVEY_SITE_ID                 NONUNIQUE
prompt --   PATIENT_ATT_PAT_DAT_UQ    PATIENT_ID                     NONUNIQUE
prompt --   PATIENT_ATT_PAT_DAT_UQ    DATA_NAME                      NONUNIQUE

SET VERIFY OFF

SELECT '    ' "----", i.index_name, c.column_name, i.uniqueness
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name='PATIENT_ATTRIBUTE' AND c.table_name='PATIENT_ATTRIBUTE'
   AND i.index_name=c.index_name AND i.index_name=UPPER('&PAINDEX');

prompt -- If your output differs and say UNIQUE, great- you can skip to:  @ 6a-chk-sr-site-tok-ix
prompt
prompt -- If you got no rows selected, you must have renamed your index. If so,
prompt --    identify the correct index, using:  @ pa-show-ixs
prompt --    then edit the file  column-formats  and at the end, change PAINDEX to be the name of your index.
prompt --    and run this again:  @ 1-pa-chk-unique
prompt
prompt -- If yours is unique, the following query should return just a single row
prompt --    with num_same_trios = 1 for all rows, and no 2s (duplicates), 3s or more.
prompt --    otherwise, it'll tell you how many rows do NOT have unique trios- a problem we will fix.

SELECT num_same_trios, count(*) num_wi_this_many_dups FROM
  (SELECT count(*) num_same_trios FROM patient_attribute 
    GROUP BY survey_site_id, patient_id, data_name
  ) GROUP BY num_same_trios;

prompt -- As above, if your index is already unique, go to @ 6a-chk-sr-site-tok-ix
prompt
prompt -- If there is only 1 line, with NUM_SAME_TRIOS = 1 (or you have no data and got no rows selected)
prompt --   you have no duplicates, skip to @ 4-pa-fix-unique-index
prompt
prompt -- If you have duplicates proceed to:  @ 2-pa-analyze-make-tmp
prompt

SET VERIFY ON
