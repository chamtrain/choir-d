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
CREATE TABLE assessment_registration (
    assessment_reg_id bigint NOT NULL,
    survey_site_id bigint NOT NULL,
    patient_id character varying(50) NOT NULL,
    email_addr character varying(255),
    assessment_dt timestamp(3) without time zone NOT NULL,
    assessment_type character varying(200) NOT NULL,
    meta_version integer,
    dt_created timestamp(3) without time zone,
    dt_changed timestamp(3) without time zone
);

ALTER TABLE ONLY assessment_registration
    ADD CONSTRAINT assessment_registration_pk PRIMARY KEY (assessment_reg_id);

ALTER TABLE ONLY assessment_registration
    ADD CONSTRAINT asmt_reg_site_id_fk FOREIGN KEY (survey_site_id) REFERENCES survey_site(survey_site_id);

ALTER TABLE ONLY assessment_registration
    ADD CONSTRAINT asmt_reg_patient_id_fk FOREIGN KEY (patient_id) REFERENCES patient(patient_id);

CREATE INDEX asmt_reg_site_id_idx ON assessment_registration USING btree (survey_site_id);

CREATE INDEX asmt_reg_patient_id_idx ON assessment_registration USING btree (patient_id);


--
-- Create appt_registration table
--
CREATE TABLE appt_registration (
    appt_reg_id bigint NOT NULL,
    survey_site_id bigint NOT NULL,
    patient_id character varying(50) NOT NULL,
    assessment_reg_id bigint,
    visit_dt timestamp(3) without time zone NOT NULL,
    registration_type character(1),
    visit_type character varying(20),
    appt_complete character(1),
    clinic character varying(200),
    encounter_eid character varying(200),
    provider_id bigint,
    meta_version integer,
    dt_created timestamp(3) without time zone,
    dt_changed timestamp(3) without time zone,
    CONSTRAINT appt_reg_appt_complete_ck CHECK ((appt_complete = ANY (ARRAY['N'::bpchar, 'Y'::bpchar])))
);

ALTER TABLE ONLY appt_registration
    ADD CONSTRAINT appt_registration_pk PRIMARY KEY (appt_reg_id);

ALTER TABLE ONLY appt_registration
    ADD CONSTRAINT appt_reg_site_id_fk FOREIGN KEY (survey_site_id) REFERENCES survey_site(survey_site_id);

ALTER TABLE ONLY appt_registration
    ADD CONSTRAINT appt_reg_patient_id_fk FOREIGN KEY (patient_id) REFERENCES patient(patient_id);

ALTER TABLE ONLY appt_registration
    ADD CONSTRAINT appt_reg_asmt_id_fk FOREIGN KEY (assessment_reg_id) REFERENCES assessment_registration(assessment_reg_id);

		ALTER TABLE ONLY appt_registration
		    ADD CONSTRAINT appt_reg_prov_id_fk FOREIGN KEY (provider_id) REFERENCES provider(provider_id);

CREATE INDEX appt_reg_site_id_idx ON appt_registration USING btree (survey_site_id);

CREATE INDEX appt_reg_patient_id_idx ON appt_registration USING btree (patient_id);

CREATE INDEX appt_reg_asmt_id_idx ON appt_registration USING btree (assessment_reg_id);


--
-- Add columns to survey_registration table
--
ALTER TABLE SURVEY_REGISTRATION ADD ASSESSMENT_REG_ID bigint;
ALTER TABLE SURVEY_REGISTRATION ADD SURVEY_NAME VARCHAR(255);
ALTER TABLE SURVEY_REGISTRATION ADD SURVEY_ORDER bigint;

--
-- Add an assessment id to the existing survey registrations
--
update survey_registration set assessment_reg_id = nextval('survey_seq'), survey_name = 'Default', survey_order = 1;

--
-- Create an assessment registration for each of the existing survey registrations
--
insert into assessment_registration
( assessment_reg_id, survey_site_id, patient_id, email_addr, assessment_dt, assessment_type, meta_version, dt_created, dt_changed )
select
  assessment_reg_id, survey_site_id, patient_id, email_addr, survey_dt, survey_type, meta_version, dt_created, dt_changed
from survey_registration;

ALTER TABLE ONLY survey_registration
    ADD CONSTRAINT survey_reg_asmt_id_fk FOREIGN KEY (assessment_reg_id) REFERENCES assessment_registration(assessment_reg_id);

CREATE INDEX survey_reg_asmt_id_idx ON survey_registration USING btree (assessment_reg_id);

--
-- Create an appointment registration for each of the existing survey registrations
--
insert into appt_registration
( appt_reg_id, survey_site_id, patient_id, assessment_reg_id, visit_dt, registration_type, visit_type, appt_complete,
 clinic, encounter_eid, provider_id, meta_version, dt_created, dt_changed )
select
  nextval('survey_seq'), survey_site_id, patient_id, assessment_reg_id, survey_dt, registration_type, visit_type, appt_complete,
  clinic, encounter_eid, provider_id, meta_version, dt_created, dt_changed
from survey_registration;

-- commit;

--
-- Modify the constraints on the survey_registration table
--
ALTER TABLE SURVEY_REGISTRATION ALTER ASSESSMENT_REG_ID set NOT NULL;
ALTER TABLE SURVEY_REGISTRATION ALTER SURVEY_NAME set NOT NULL;
ALTER TABLE SURVEY_REGISTRATION ALTER SURVEY_ORDER set NOT NULL;
ALTER TABLE SURVEY_REGISTRATION DROP CONSTRAINT SURVEY_REG_PROV_ID_FK;


--
-- Add assessment_reg_id column to notification table
--
ALTER TABLE NOTIFICATION ADD ASSESSMENT_REG_ID bigint;

ALTER TABLE ONLY notification
    ADD CONSTRAINT not_asmt_reg_id_fk FOREIGN KEY (assessment_reg_id) REFERENCES assessment_registration(assessment_reg_id);

CREATE INDEX not_asmt_reg_id_idx ON notification USING btree (assessment_reg_id);

update notification n set assessment_reg_id = (select assessment_reg_id from survey_registration sr where sr.survey_reg_id = n.survey_reg_id);

-- commit;

ALTER TABLE NOTIFICATION ALTER ASSESSMENT_REG_ID set NOT NULL;
ALTER TABLE NOTIFICATION ALTER SURVEY_REG_ID drop NOT NULL;
ALTER TABLE NOTIFICATION ALTER TOKEN drop NOT NULL;
ALTER TABLE NOTIFICATION DROP CONSTRAINT NOT_SUR_REG_ID_FK;


--
-- Add assessment_reg_id to activity table
--
ALTER TABLE ACTIVITY ADD ASSESSMENT_REG_ID bigint;

CREATE INDEX activity_site_asmt_atyp_idx ON activity USING btree (survey_site_id, assessment_reg_id, activity_type);

update activity a set assessment_reg_id =
    ( select assessment_reg_id
      from survey_registration sr
      where sr.survey_site_id = a.survey_site_id and sr.token = a.token
    )
where a.activity_type in ('Survey Type Changed','Chart Printed','Chart Viewed');

-- commit;


--
-- Add assessment_reg_id to patient_result table
--
ALTER TABLE PATIENT_RESULT ADD ASSESSMENT_REG_ID bigint;

ALTER TABLE ONLY patient_result
    ADD CONSTRAINT pat_res_asmt_reg_id_fk FOREIGN KEY (assessment_reg_id) REFERENCES assessment_registration(assessment_reg_id);

CREATE INDEX pat_res_asmt_reg_id_idx ON patient_result USING btree (assessment_reg_id);

update patient_result p set assessment_reg_id = (select assessment_reg_id from survey_registration sr where sr.survey_reg_id = p.survey_reg_id);

-- commit;

ALTER TABLE PATIENT_RESULT ALTER ASSESSMENT_REG_ID set NOT NULL;
ALTER TABLE PATIENT_RESULT ALTER SURVEY_REG_ID drop NOT NULL;
