-- Each row represents a batch load of patients/appointments into the patsat_token table
-- See edu.stanford.registry.tool.PatientSatisfactionEmailer for loading.
create table patsat_batch (
  patsat_batch_id numeric(19) not null,
  load_date timestamp not null,
  constraint patsat_batch_pk primary key (patsat_batch_id)
);

-- Each row represents a survey token that can be used to take a patient satisfaction survey,
-- along with various bookkeeping information about that particular survey.
create table patsat_token (
  survey_token varchar(80) not null,
  email_addr varchar(255) not null,
  token_valid_from timestamp not null,
  token_valid_thru timestamp not null,
  patsat_batch_id numeric(19) not null,
  patient_id varchar(50) not null,
  survey_reg_id numeric(19) not null,
  appt_date timestamp not null,
  visit_type varchar(20),
  opted_out char(1) check (opted_out in ('N', 'Y')),
  encounter_eid varchar(200),
  provider_id numeric(19),
  constraint patsat_token_pk primary key (survey_token),
  constraint patsat_batch_id foreign key (patsat_batch_id) references patsat_batch
);

-- Place to record emails we have sent, both as an audit trail, and so we can know what was
-- send to calculate reminders, suppress future emails, etc. Each row represents an email
-- that was actually sent.
create table patsat_email (
  survey_token varchar(80) not null,
  send_sequence numeric(19) not null,
  send_time timestamp not null,
  from_addr varchar(255) not null,
  to_addr varchar(255) not null,
  subject varchar(255) not null,
  body_html clob not null,
  constraint patsat_email_pk primary key (survey_token, send_sequence)
);
