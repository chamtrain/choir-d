create table appt_visit(
    appt_visit_id bigint not null,
    visit_type varchar(50) not null,
    visit_description varchar(500) not null,
    visit_eid bigint not null,
    constraint app_visit_pk  primary key (appt_visit_id)
);
comment on column appt_visit.appt_visit_id is 'Internally generated primary key';
create index appt_visit_desc_ix on appt_visit (visit_description);
create unique index appt_visit_eid_ix_uq on appt_visit (visit_eid);
create sequence appt_visit_seq minvalue 1 maxvalue 999999999999999999 start with 1000 increment by 1 cache 20 no cycle;
