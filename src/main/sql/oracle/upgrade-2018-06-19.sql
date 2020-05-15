--
-- Tables used by the pluginPatientData API
--

create sequence plugin_patient_sequence minvalue 1 maxvalue 999999999999999999
  start with 1000 increment by 1 cache 20 noorder nocycle;

create sequence plugin_patient_change_sequence minvalue 1 maxvalue 999999999999999999
  start with 1000 increment by 1 cache 20 noorder nocycle;

create table plugin_patient_data (
  data_id number(19,0)  not null,
  data_type varchar2(255)  not null,
  data_version varchar2(50)  not null,
  survey_site_id  number(19,0) not null,
  patient_id varchar2(50)  not null,
  dt_created timestamp(3)  not null,
  data_value clob,
  constraint plugin_pat_dat_pk primary key (data_id),
  constraint plugin_pat_dat_site_fk foreign key (survey_site_id) references survey_site,
  constraint plugin_pat_dat_patient_fk foreign key (patient_id) references patient (patient_id)
);

create table plugin_patient_data_history(
  data_hist_id number(19,0) primary key,
  change_type char(1) check (change_type in ('A','M')), -- Flag: A=added, M=modified
  data_id number(19,0) not null,
  data_type varchar2(255)  not null,
  data_version varchar2(50)  not null,
  survey_site_id  number(19,0) not null,
  patient_id varchar2(50)  not null,
  dt_created timestamp(3)  not null,
  data_value clob,
  constraint plugin_pat_dat_his_site_fk foreign key (survey_site_id) references survey_site,
  constraint plugin_pat_dat_his_patient_fk foreign key (patient_id) references patient (patient_id)
);

create index plugin_patient_history1 on plugin_patient_data_history (patient_id, data_type);
