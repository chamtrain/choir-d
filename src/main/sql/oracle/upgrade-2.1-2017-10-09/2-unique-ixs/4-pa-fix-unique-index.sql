@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 4
prompt -- ======== Change the patient_att_pat_dat_uq index to be UNIQUE ========

prompt --
prompt -- We can't just rename the old index - Oracle won't let us have 2 indexes on the same columns
prompt -- So to index faster, we'll add a new index with an extra column, delete the old index
prompt --   and then re-create the old index, but with "unique"
prompt
prompt -- Create a new temporary one, on the same 3 columns + meta_version

CREATE UNIQUE INDEX patient_att_temporary ON patient_attribute (SURVEY_SITE_ID, PATIENT_ID, DATA_NAME, META_VERSION);

prompt -- drop the old one

DROP          INDEX patient_att_pat_dat_uq;

prompt -- create the new one

CREATE UNIQUE INDEX patient_att_pat_dat_uq ON patient_attribute (SURVEY_SITE_ID, PATIENT_ID, DATA_NAME);

prompt --
prompt -- and drop the temporary one
prompt --

DROP INDEX patient_att_temporary;

prompt --
prompt -- To confirm, here are the index columns on patient_attribute, showing uniqueness
prompt --

SELECT i.index_name, c.column_name, i.uniqueness
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name='PATIENT_ATTRIBUTE' AND c.table_name='PATIENT_ATTRIBUTE'
   AND i.index_name=c.index_name AND i.index_name='PATIENT_ATT_PAT_DAT_UQ' ORDER BY c.column_name;

prompt -- Next, just drop the temporary analysis table:  @ 5-pa-attr-tmp-cleanup
prompt -- If you had no duplicates, it'll say "table or view does not exist" - that is fine.
prompt  

