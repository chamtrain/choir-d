@column-formats
prompt 
prompt 
prompt 
prompt 
prompt ============= 5
prompt ============= Add 3 foreign keys that were missing
prompt 

prompt == Add patient_study->survey_site
ALTER TABLE patient_study
ADD CONSTRAINT patient_study_site_fk FOREIGN KEY (survey_site_id)
     REFERENCES survey_site (survey_site_id);

prompt == Add patient_study->patient
ALTER TABLE patient_study
ADD CONSTRAINT patient_study_patient_fk FOREIGN KEY (patient_id)
     REFERENCES patient (patient_id);

prompt == Add patient_attribute_history->survey_site
ALTER TABLE patient_attribute_HISTORY
ADD CONSTRAINT patient_att_hist_site_id_fk FOREIGN KEY (survey_site_id)
     REFERENCES survey_site (survey_site_id);

prompt == proceed to @ 6-fix-indexes
prompt
