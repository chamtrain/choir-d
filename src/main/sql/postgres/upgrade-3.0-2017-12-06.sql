-- Oracle script to add 4 RandomSet tables to the database, for choir 3.0.0
--   randomset, randomset_stratum, randomset_participation, randomset_participation_hist

\echo
\echo '== create table randomset'
create table randomset (
  survey_site_id                 bigint not null,
  set_name                       varchar(50) not null,
  state                          varchar(50),
  algorithm                      varchar(20),
  dt_end                         timestamp(3),
  target_size                    integer,
  study_length_days              integer,
  constraint randomset_state_pk             primary key (survey_site_id, set_name)
);
comment on table randomset is 
'Most randomset algorithms save state to keep groups balanced';
comment on column randomset.set_name is 
'Not a foreign key because names are from app_config, only unique for the randomset type';
comment on column randomset.state is 
'Enrolling,NotEnrolling,Researching,Closed';
comment on column randomset.algorithm is 
'Just FYI, since the set definition is hidden in a json';
comment on column randomset.dt_end is 
'optional end-date';
comment on column randomset.target_size is 
'optional total number of participants';
comment on column randomset.study_length_days is 
'optional length of each patients participation';

\echo '== create table randomset_stratum'
create table randomset_stratum (
  survey_site_id                 bigint not null,
  set_name                       varchar(50) not null,
  stratum_name                   varchar(200) not null,
  data                           varchar(4000),
  counter                        integer,
  constraint randomset_stratum_pk           primary key (survey_site_id, set_name, stratum_name)
);
comment on table randomset_stratum is 
'Each stratum is for a combination of category settings';
comment on column randomset_stratum.stratum_name is 
'comma-separated list of category=value strings';
comment on column randomset_stratum.data is 
'the meaning of this depends on the algorithm used';
comment on column randomset_stratum.counter is 
'increments every time it''s changed, for cache maintenance';

\echo '== create table randomset_participant'
create table randomset_participant (
  participant_id                 bigint not null,
  patient_id                     varchar(50),
  survey_site_id                 bigint not null,
  set_name                       varchar(50) not null,
  stratum_name                   varchar(50),
  state                          varchar(50) not null,
  group_name                     varchar(50),
  reason                         varchar(250),
  dt_assigned                    timestamp(3),
  dt_withdrawn                   timestamp(3),
  update_time                    timestamp(3),
  update_sequence                bigint,
  constraint randomset_participant_pk       primary key (participant_id)
);
comment on table randomset_participant is 
'NotYetQualified,Disqualified,Declined, and Assigned,Withdrawn,Completed';
comment on column randomset_participant.participant_id is 
'Internally generated primary key';
comment on column randomset_participant.stratum_name is 
'Empty unless state is Assigned, Withdrawn or Completed';
comment on column randomset_participant.group_name is 
'null if in a non-assigned state. Assigned states: Assigned, Withdrawn, Completed';
comment on column randomset_participant.reason is 
'Why it''s in the current state, optional';
comment on column randomset_participant.dt_assigned is 
'First time, if withdrawn and added back in';
comment on column randomset_participant.dt_withdrawn is 
'last time. If added back, this is null';

\echo '== create table randomset_participant_hist'
create table randomset_participant_hist (
  participant_id                 bigint not null,
  patient_id                     varchar(50),
  survey_site_id                 bigint not null,
  set_name                       varchar(50) not null,
  stratum_name                   varchar(50),
  state                          varchar(50) not null,
  group_name                     varchar(50),
  reason                         varchar(250),
  dt_assigned                    timestamp(3),
  dt_withdrawn                   timestamp(3),
  update_time                    timestamp(3),
  update_sequence                bigint,
  is_deleted                     char(1),
  constraint randomset_participant_hist_pk  primary key (participant_id, update_sequence)
);
comment on column randomset_participant_hist.participant_id is 
'Internally generated primary key';
comment on column randomset_participant_hist.stratum_name is 
'Empty unless state is Assigned, Withdrawn or Completed';
comment on column randomset_participant_hist.group_name is 
'null if in a non-assigned state. Assigned states: Assigned, Withdrawn, Completed';
comment on column randomset_participant_hist.reason is 
'Why it''s in the current state, optional';
comment on column randomset_participant_hist.dt_assigned is 
'First time, if withdrawn and added back in';
comment on column randomset_participant_hist.dt_withdrawn is 
'last time. If added back, this is null';

\echo '== create sequence randomset_part_id_seq'
create sequence randomset_part_id_seq minvalue 1 maxvalue 999999999999999999 start with 1000 increment by 1 cache 20 no cycle;

\echo '== adding constraints and foreign keys'
alter table randomset add constraint randomset_site_fk
  foreign key (survey_site_id) references survey_site;
alter table randomset_stratum add constraint randomset_stratum_rset_fk
  foreign key (survey_site_id, set_name) references randomset;
alter table randomset_participant add constraint randomset_part_pat_fk
  foreign key (patient_id) references patient;
alter table randomset_participant add constraint randomset_fk
  foreign key (survey_site_id, set_name) references randomset;

\echo '== adding 3 indexes on randomset_participant and randomset_participant_hist'
create unique index randomset_part_pk on randomset_participant (patient_id, survey_site_id, set_name);
create index randomset_site_name_ix on randomset_participant (survey_site_id, set_name, stratum_name, group_name);
create index randomset_participant_hist_ix on randomset_participant_hist (participant_id);

\echo '== done'
\echo
