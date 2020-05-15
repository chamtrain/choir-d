@column-formats
prompt 
prompt 
prompt 
prompt 
prompt ============= 6
prompt ============= Fix indexes on 3 tables:  
prompt               ASSESSMENT_REGISTRATION
prompt               APPOINTMENT_REGISTRATION

column TABLE_NAME format a13
column COLUMN_NAME format a21

prompt
prompt == We reviewed the indexes.
prompt == It is good practice to have an index for each foreign key
prompt ==   But if the PK or another index already starts with the foreign key column(s)
prompt ==   then no additional index is needed.
prompt == The database layer was making indexes for foreign keys, unless an exact match occurred.
prompt ==   We fixed it, but existing systems have extra indexes.
prompt == Plus, having an index on a foreign key is an opportunity to add more columns to aid queries.
prompt ==   So we are making indexes with some extra columns
prompt == The last step in these scripts will query the database for all duplicate indexes to remove.
prompt


prompt ==== 1. Add an index:  ASSESSMENT_REGISTRATION (site, assessment_type)
CREATE INDEX asmt_reg_site_astype_idx ON assessment_registration (survey_site_id, assessment_type);


prompt ==== 2. Add an index: APPT_REGISTRATION (patient, site, visit_dt) instead of just (patient)

CREATE INDEX appt_reg_pat_site_visdt_idx ON appt_registration (patient_id, survey_site_id, visit_dt);

prompt ==   3b. Add one on (assessment_reg, site, patient) instead of just (assessment)
CREATE INDEX appt_reg_asmt_site_pat_idx ON appt_registration (assessment_reg_id,survey_site_id,patient_id);


prompt == You can view the indexes on these tables with one of:  @asr-list  @apr-list
prompt
prompt == When finished, proceed to:  @ 7-fix-indexes
prompt
