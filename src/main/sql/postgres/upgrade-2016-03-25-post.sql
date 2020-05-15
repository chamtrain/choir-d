--
-- Clean up after registry schema upgrade to allow separate Parent and Child surveys
-- for a single appointment.
--
-- Remove columns which are no longer needed
--

ALTER TABLE SURVEY_REGISTRATION DROP COLUMN EMAIL_ADDR;
ALTER TABLE SURVEY_REGISTRATION DROP COLUMN REGISTRATION_TYPE;
ALTER TABLE SURVEY_REGISTRATION DROP COLUMN VISIT_TYPE;
ALTER TABLE SURVEY_REGISTRATION DROP COLUMN APPT_COMPLETE;
ALTER TABLE SURVEY_REGISTRATION DROP COLUMN CLINIC;
ALTER TABLE SURVEY_REGISTRATION DROP COLUMN ENCOUNTER_EID;
ALTER TABLE SURVEY_REGISTRATION DROP COLUMN PROVIDER_ID;

ALTER TABLE NOTIFICATION DROP COLUMN SURVEY_REG_ID;
ALTER TABLE NOTIFICATION DROP COLUMN TOKEN;

ALTER TABLE PATIENT_RESULT DROP COLUMN SURVEY_REG_ID;