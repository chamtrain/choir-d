--
-- Registry schema upgrade to allow separate Parent and Child surveys for a single appointment.
--
-- The survey_registration table is split into three tables
--   appt_registration - This represents an appointment. An appointment has an assessment.
--   assessment_registration - This represents an assessment. An assessment may have multiple
--     surveys associated with it
--   survey_registration - This now represents a survey
--

--
-- Create assessment_registration table
--
CREATE TABLE ASSESSMENT_REGISTRATION 
( ASSESSMENT_REG_ID NUMBER(19,0) NOT NULL PRIMARY KEY, 
  SURVEY_SITE_ID NUMBER(19,0) NOT NULL, 
  PATIENT_ID VARCHAR2(50 BYTE) NOT NULL, 
  EMAIL_ADDR VARCHAR2(255 BYTE), 
  ASSESSMENT_DT TIMESTAMP (3) NOT NULL, 
  ASSESSMENT_TYPE VARCHAR2(200 BYTE), 
  META_VERSION NUMBER(10,0), 
  DT_CREATED TIMESTAMP (3), 
  DT_CHANGED TIMESTAMP (3)
);

ALTER TABLE ASSESSMENT_REGISTRATION ADD CONSTRAINT ASMT_REG_SITE_ID_FK FOREIGN KEY (SURVEY_SITE_ID)
REFERENCES SURVEY_SITE (SURVEY_SITE_ID) ENABLE;

ALTER TABLE ASSESSMENT_REGISTRATION ADD CONSTRAINT ASMT_REG_PATIENT_ID_FK FOREIGN KEY (PATIENT_ID)
REFERENCES PATIENT (PATIENT_ID) ENABLE;

CREATE INDEX ASMT_REG_SITE_ID_IDX ON ASSESSMENT_REGISTRATION (SURVEY_SITE_ID);

CREATE INDEX ASMT_REG_PATIENT_ID_IDX ON ASSESSMENT_REGISTRATION (PATIENT_ID);


--
-- Create appt_registration table
--
CREATE TABLE APPT_REGISTRATION 
( APPT_REG_ID NUMBER(19,0) NOT NULL PRIMARY KEY, 
  SURVEY_SITE_ID NUMBER(19,0) NOT NULL, 
  PATIENT_ID VARCHAR2(50 BYTE) NOT NULL, 
  ASSESSMENT_REG_ID NUMBER(19,0),
  VISIT_DT TIMESTAMP (3) NOT NULL, 
  REGISTRATION_TYPE CHAR(1 BYTE), 
  VISIT_TYPE VARCHAR2(20 BYTE), 
  APPT_COMPLETE CHAR(1 BYTE) CHECK (appt_complete in ('N', 'Y')),
  CLINIC VARCHAR2(200 BYTE), 
  ENCOUNTER_EID VARCHAR2(200 BYTE), 
  PROVIDER_ID NUMBER(19,0), 
  META_VERSION NUMBER(10,0), 
  DT_CREATED TIMESTAMP (3), 
  DT_CHANGED TIMESTAMP (3)
);

ALTER TABLE APPT_REGISTRATION ADD CONSTRAINT APPT_REG_SITE_ID_FK FOREIGN KEY (SURVEY_SITE_ID)
REFERENCES SURVEY_SITE (SURVEY_SITE_ID) ENABLE;
    
ALTER TABLE APPT_REGISTRATION ADD CONSTRAINT APPT_REG_PATIENT_ID_FK FOREIGN KEY (PATIENT_ID)
REFERENCES PATIENT (PATIENT_ID) ENABLE;

ALTER TABLE APPT_REGISTRATION ADD CONSTRAINT APPT_REG_ASMT_ID_FK FOREIGN KEY (ASSESSMENT_REG_ID)
REFERENCES ASSESSMENT_REGISTRATION (ASSESSMENT_REG_ID) ENABLE;

ALTER TABLE APPT_REGISTRATION ADD CONSTRAINT APPT_REG_PROV_ID_FK FOREIGN KEY (PROVIDER_ID)
REFERENCES PROVIDER (PROVIDER_ID) ENABLE;

CREATE INDEX APPT_REG_SITE_ID_IDX ON APPT_REGISTRATION (SURVEY_SITE_ID);

CREATE INDEX APPT_REG_PATIENT_ID_IDX ON APPT_REGISTRATION (PATIENT_ID);

CREATE INDEX APPT_REG_ASMT_ID_IDX ON APPT_REGISTRATION (ASSESSMENT_REG_ID);


--
-- Add columns to survey_registration table
--
ALTER TABLE SURVEY_REGISTRATION ADD (
  ASSESSMENT_REG_ID NUMBER(19,0),
  SURVEY_NAME VARCHAR(255),
  SURVEY_ORDER NUMBER(10,0)
);

ALTER TABLE SURVEY_REGISTRATION ADD CONSTRAINT SURVEY_REG_ASMT_ID_FK FOREIGN KEY (ASSESSMENT_REG_ID)
REFERENCES ASSESSMENT_REGISTRATION (ASSESSMENT_REG_ID) DISABLE;

CREATE INDEX SURVEY_REG_ASMT_ID_IDX ON SURVEY_REGISTRATION (ASSESSMENT_REG_ID);


--
-- Add an assessment id to the existing survey registrations
--
update survey_registration set assessment_reg_id = survey_seq.nextval, survey_name = 'Default', survey_order = 1;

--
-- Create an assessment registration for each of the existing survey registrations
--
insert into assessment_registration 
( assessment_reg_id, survey_site_id, patient_id, email_addr, assessment_dt, assessment_type, meta_version, dt_created, dt_changed )
select
  assessment_reg_id, survey_site_id, patient_id, email_addr, survey_dt, survey_type, meta_version, dt_created, dt_changed
from survey_registration;

--
-- Create an appointment registration for each of the existing survey registrations
--
insert into appt_registration
( appt_reg_id, survey_site_id, patient_id, assessment_reg_id, visit_dt, registration_type, visit_type, appt_complete,
 clinic, encounter_eid, provider_id, meta_version, dt_created, dt_changed )
select
  survey_seq.nextval, survey_site_id, patient_id, assessment_reg_id, survey_dt, registration_type, visit_type, appt_complete,
  clinic, encounter_eid, provider_id, meta_version, dt_created, dt_changed
from survey_registration;

commit;

--
-- Modify the constraints on the survey_registration table
--
ALTER TABLE SURVEY_REGISTRATION MODIFY (ASSESSMENT_REG_ID NOT NULL);
ALTER TABLE SURVEY_REGISTRATION MODIFY (SURVEY_NAME NOT NULL);
ALTER TABLE SURVEY_REGISTRATION MODIFY (SURVEY_ORDER NOT NULL);
ALTER TABLE SURVEY_REGISTRATION ENABLE CONSTRAINT SURVEY_REG_ASMT_ID_FK;
ALTER TABLE SURVEY_REGISTRATION DISABLE CONSTRAINT SURVEY_REG_PROV_ID_FK;


--
-- Add assessment_reg_id column to notification table
--
ALTER TABLE NOTIFICATION ADD (ASSESSMENT_REG_ID NUMBER(19,0));

ALTER TABLE NOTIFICATION ADD CONSTRAINT NOT_ASMT_REG_ID_FK FOREIGN KEY (ASSESSMENT_REG_ID)
REFERENCES ASSESSMENT_REGISTRATION (ASSESSMENT_REG_ID) ENABLE;

CREATE INDEX NOT_ASMT_REG_ID_IDX ON NOTIFICATION (ASSESSMENT_REG_ID);

update notification n set assessment_reg_id = (select assessment_reg_id from survey_registration sr where sr.survey_reg_id = n.survey_reg_id);

commit;

ALTER TABLE NOTIFICATION MODIFY (ASSESSMENT_REG_ID NOT NULL);
ALTER TABLE NOTIFICATION MODIFY (SURVEY_REG_ID NULL);
ALTER TABLE NOTIFICATION MODIFY (TOKEN NULL);
ALTER TABLE NOTIFICATION DISABLE CONSTRAINT NOT_SUR_REG_ID_FK;


--
-- Add assessment_reg_id to activity table
--
ALTER TABLE ACTIVITY ADD (ASSESSMENT_REG_ID NUMBER(19,0));

CREATE INDEX ACTIVITY_SITE_ASMT_ATYP_IDX ON ACTIVITY (SURVEY_SITE_ID, ASSESSMENT_REG_ID, ACTIVITY_TYPE);

update activity a set assessment_reg_id = 
    ( select assessment_reg_id 
      from survey_registration sr
      where sr.survey_site_id = a.survey_site_id and sr.token = a.token
    )
where a.activity_type in ('Survey Type Changed','Chart Printed','Chart Viewed');

commit;


--
-- Add assessment_reg_id to patient_result table
--
ALTER TABLE PATIENT_RESULT ADD (ASSESSMENT_REG_ID NUMBER(19,0));

ALTER TABLE PATIENT_RESULT ADD CONSTRAINT PAT_RES_ASMT_REG_ID_FK FOREIGN KEY (ASSESSMENT_REG_ID)
REFERENCES ASSESSMENT_REGISTRATION (ASSESSMENT_REG_ID) ENABLE;

CREATE INDEX PAT_RES_ASMT_REG_ID_IDX ON PATIENT_RESULT (ASSESSMENT_REG_ID);

update patient_result p set assessment_reg_id = (select assessment_reg_id from survey_registration sr where sr.survey_reg_id = p.survey_reg_id);

commit;

ALTER TABLE PATIENT_RESULT MODIFY (ASSESSMENT_REG_ID NOT NULL);
ALTER TABLE PATIENT_RESULT MODIFY (SURVEY_REG_ID NULL);

