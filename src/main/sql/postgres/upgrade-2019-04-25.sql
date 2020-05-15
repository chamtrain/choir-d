create table patient_ext_attr(
    patient_ext_attr_id bigint not null,
    survey_site_id bigint not null,
    patient_id varchar(50) not null,
    data_name varchar(250) not null,
    data_value text,
    data_type varchar(50),
    dt_created timestamp(3),
    dt_changed timestamp(3),
    constraint pea_pk primary key (patient_ext_attr_id),
    constraint pat_ext_attr_p_fk foreign key (patient_id) references patient,
    constraint pat_ext_attr_s_fk foreign key (survey_site_id) references survey_site
);
comment on column patient_ext_attr.patient_ext_attr_id is 'Internally generated primary key';
create unique index pat_ext_attr_s_p_d_uq on patient_ext_attr (survey_site_id, patient_id, data_name);
create index pat_ext_attr_p_idx on patient_ext_attr (patient_id);

create table patient_ext_attr_hist(
    patient_ext_attr_hist_id bigint not null,
    patient_ext_attr_id bigint not null,
    survey_site_id bigint not null,
    patient_id varchar(50) not null,
    data_name varchar(250) not null,
    data_value text,
    data_type varchar(50),
    dt_created timestamp(3),
    dt_changed timestamp(3),
    user_principal_id bigint,
    constraint pat_ext_attr_hist_pk primary key (patient_ext_attr_hist_id),
    constraint pat_ext_attr_hist_p_fk foreign key (patient_id) references patient,
    constraint pat_ext_attr_hist_s_fk foreign key (survey_site_id) references survey_site
);
comment on column patient_ext_attr_hist.patient_ext_attr_hist_id is 'Internally generated primary key';
create index pat_ext_attr_hist_s_p_idx on patient_ext_attr_hist (survey_site_id, patient_id, data_name);
create index pat_ext_attr_hist_p_idx on patient_ext_attr_hist (patient_id);
