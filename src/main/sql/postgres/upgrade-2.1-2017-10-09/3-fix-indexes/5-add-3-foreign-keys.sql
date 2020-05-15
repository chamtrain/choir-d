-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo '============= 5.sql'
\echo '============= Add 3 foreign keys that were missing.sql'
\echo
\echo '== Add patient_study->survey_site.sql'
ALTER TABLE patient_study
ADD CONSTRAINT patient_study_site_fk FOREIGN KEY (survey_site_id)
     REFERENCES survey_site (survey_site_id);

\echo
\echo '== Add patient_study->patient.sql'
ALTER TABLE patient_study
ADD CONSTRAINT patient_study_patient_fk FOREIGN KEY (patient_id)
     REFERENCES patient (patient_id);

\echo
\echo '== Add patient_attribute_history->survey_site.sql'
ALTER TABLE patient_attribute_HISTORY
ADD CONSTRAINT patient_att_hist_site_id_fk FOREIGN KEY (survey_site_id)
     REFERENCES survey_site (survey_site_id);

\echo
\echo '== proceed to:  \\i 6-fix-indexes.sql'
\echo
