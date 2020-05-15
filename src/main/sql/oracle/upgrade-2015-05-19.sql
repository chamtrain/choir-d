-- Tables needed for a new push event mechanism that can
-- provide notifications every time a survey has changed,
-- even if it has not been marked completed.

create table survey_advance (
  advance_sequence               numeric(19) not null,
  advance_time                   timestamp(3) not null,
  survey_site_id                 numeric(19) not null,
  survey_token_id                numeric(19) not null,
  is_complete                    char(1) not null,
  last_step_number               numeric(10) not null,
  player_progress_count          numeric(10) not null,
  constraint survey_advance_pk              primary key (advance_sequence),
  constraint s_adv_site_token_uq            unique (survey_site_id, survey_token_id, last_step_number, player_progress_count)
);

comment on table survey_advance is
'Record survey advancement (from both survey_progress and survey_player_progress) here for change tracking purposes. One row may represent any amount of progress. The only guarantees are that a row in here indicates some progress, and all progress will eventually be recorded here (it is done asynchronously with no time guarantee).';

comment on column survey_advance.advance_sequence is
'Internally generated primary key';

comment on column survey_advance.player_progress_count is
'Sum the number of rows in survey_player_progress for this survey at the time of this advance, which effectively indicates whether a player progressed in some way.';

create table survey_advance_status (
  survey_site_id                 numeric(19) not null,
  check_time                     timestamp(3) not null
);

comment on table survey_advance_status is
'Provide locking and timestamp tracking for processes maintaining survey_advance.';

create table survey_advance_push (
  survey_recipient_id            numeric(19) not null,
  survey_site_id                 numeric(19) not null,
  recipient_name                 varchar2(80),
  recipient_display_name         varchar2(80),
  pushed_survey_sequence         numeric(19),
  last_pushed_time               timestamp(3),
  failed_survey_sequence         numeric(19),
  failed_count                   numeric(10) not null,
  last_failed_time               timestamp(3),
  is_enabled                     char(1),
  constraint survey_advance_push_pk         primary key (survey_recipient_id),
  constraint s_adv_push_enabled_ck          check (is_enabled in ('N', 'Y'))
);

comment on table survey_advance_push is
'Bookkeeping information for survey completion push notifications';

comment on column survey_advance_push.survey_recipient_id is
'Internally generated primary key';

comment on column survey_advance_push.pushed_survey_sequence is
'The last successfully sent survey';

comment on column survey_advance_push.last_pushed_time is
'The time we successfully sent pushed_survey_sequence';

comment on column survey_advance_push.failed_survey_sequence is
'The latest push attempted and failed, or null if last push succeeded';

comment on column survey_advance_push.failed_count is
'The number of times we have tried to send failed_survey_sequence, or 0';

comment on column survey_advance_push.last_failed_time is
'The most recent time we attempted to send failed_survey_sequence';

alter table survey_advance add constraint s_adv_site_fk
foreign key (survey_site_id) references survey_site;

alter table survey_advance add constraint s_adv_token_fk
foreign key (survey_token_id) references survey_token;

alter table survey_advance_push add constraint s_adv_push_site_fk
foreign key (survey_site_id) references survey_site;

alter table survey_advance_push add constraint survey_adv_push_fk
foreign key (pushed_survey_sequence) references survey_advance;

create sequence survey_advance_seq minvalue 1 maxvalue 999999999999999999 start with 1 increment by 1 cache 20 order nocycle;
