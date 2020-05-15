--
-- Tables used by the pluginPatientData API
--

create sequence plugin_patient_sequence minvalue 1 maxvalue 999999999999999999
  start with 1000 increment by 1 cache 20 no cycle;

create sequence plugin_patient_change_sequence minvalue 1 maxvalue 999999999999999999
  start with 1000 increment by 1 cache 20 no cycle;

create table plugin_patient_data (
  data_id bigint not null,
  data_type character varying(255)  not null,
  data_version character varying(50)  not null,
  survey_site_id bigint not null,
  patient_id character varying(50)  not null,
  dt_created timestamp(3) without time zone not null,
  data_value text,
  constraint plugin_pat_dat_pk primary key (data_id),
  constraint plugin_pat_dat_site_fk foreign key (survey_site_id) references  survey_site(survey_site_id),
  constraint plugin_pat_dat_patient_fk foreign key (patient_id) references  patient (patient_id)
);

create table plugin_patient_data_history(
  data_hist_id bigint primary key,
  change_type character(1),
  data_id bigint not null,
  data_type character varying(255) not null,
  data_version character varying(50) not null,
  survey_site_id  bigint not null,
  patient_id character varying(50) not null,
  dt_created timestamp(3) not null,
  data_value text,
  CONSTRAINT plugin_pat_dat_his_chg_typ_ck CHECK (change_type = ANY (ARRAY['A'::bpchar, 'M'::bpchar])),
  constraint plugin_pat_dat_his_sit_fk foreign key (survey_site_id) references survey_site(survey_site_id),
  constraint plugin_pat_dat_his_pat_fk foreign key (patient_id) references patient(patient_id)
);



create index plugin_patient_history1 on plugin_patient_data_history (patient_id, data_type);
