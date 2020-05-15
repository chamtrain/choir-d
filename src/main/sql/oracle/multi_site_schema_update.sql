--
-- Update registry schema to support multiple sites in the same
-- database schema.
--

--
--  Add activity_id and survey_site_id columns to activity table
--

-- Create new sequence for activity_id
create sequence activity_id_seq minvalue 1 maxvalue 999999999999999999 
start with 1000 increment by 1 cache 20 noorder nocycle;

-- Drop old primary key constraint
alter table activity drop constraint activity_pk drop index;

-- Add activity_id and survey_site_id columns
alter table activity add (activity_id number(19,0), survey_site_id number(19,0));

-- Add foreign key constraint for survey_site_id
alter table activity add constraint activity_site_id_fk foreign key (survey_site_id) 
references survey_site (survey_site_id);

-- Set activity_id values
update activity set activity_id = activity_id_seq.nextval;

-- Set survey_site_id values
update activity set survey_site_id = ( select distinct(survey_site_id) from survey_registration );

commit;

-- Make activity_id the primary key
alter table activity add constraint activity_pk primary key (activity_id);

set serveroutput on format_wrapped; 
declare
    already_exists exception;
    already_indexed exception;
    pragma exception_init( already_exists, -955 );
    pragma exception_init( already_indexed, -01408 );
    begin
    execute immediate 'create index activity_pat_dt_idx on activity (patient_id, activity_dt )';
    dbms_output.put_line( 'activity_pat_dt_idx CREATED' );
    exception
        when already_exists then DBMS_OUTPUT.put_line( 'activity_pat_dt_idx SKIPPED' );
        when already_indexed then DBMS_OUTPUT.put_line( 'activity_pat_dt_idx SKIPPED' );
        null;
    end;
/

-- drop the token, activity type index if it exists
declare 
 index_exists pls_integer;
 begin
     select count(*) into index_exists from USER_INDEXES where table_name = 'ACTIVITY' and index_name = 'ACTIVITY_TOK_TYPE_IX';
     if index_exists > 0 then
        execute immediate 'drop index activity_tok_type_ix';
        DBMS_OUTPUT.put_line('INDEX activity_tok_type_ix DROPPED');
    end if;
end;
/

-- replace with one for site, token, activity type
declare
    already_exists exception;
    already_indexed exception;
    pragma exception_init( already_exists, -955 );
    pragma exception_init( already_indexed, -01408 );
    begin
    execute immediate 'create index activity_site_tok_atyp_idx on activity (survey_site_id, token, activity_type)';
    dbms_output.put_line( 'activity_site_tok_atyp_idx CREATED' );
    exception
        when already_exists then DBMS_OUTPUT.put_line( 'activity_site_tok_atyp_idx SKIPPED' );
        when already_indexed then DBMS_OUTPUT.put_line( 'activity_site_tok_atyp_idx SKIPPED' );
        null;
    end;
/   

-- Add not null to survey_site_id
alter table activity modify (survey_site_id constraint activity_site_id not null);

--
-- Add survey_site_id to patient_attribute table
--

-- Add survey_site_id column
alter table patient_attribute add (survey_site_id number(19,0));

-- Add foreign key constraint for survey_site_id
alter table patient_attribute add constraint patient_att_site_id_fk foreign key (survey_site_id) 
references survey_site (survey_site_id);

-- Set survey_site_id values
update patient_attribute set survey_site_id = ( select distinct(survey_site_id) from survey_registration );

commit;

-- Add not null to survey_site_id
alter table patient_attribute modify (survey_site_id constraint patient_att_site_id not null);

-- drop and re-create the patient_att_pat_dat_uq index to include the survey_site_id
declare 
 index_exists pls_integer;
 begin
     select count(*) into index_exists from USER_INDEXES where table_name = 'PATIENT_ATTRIBUTE' and index_name = 'PATIENT_ATT_PAT_DAT_UQ';
     if index_exists > 0 then
        execute immediate 'drop index patient_att_pat_dat_uq';
        DBMS_OUTPUT.put_line('INDEX patient_att_pat_dat_uq DROPPED');
    end if;
end;
/

create unique index patient_att_pat_dat_uq on patient_attribute (survey_site_id, patient_id, data_name);

--
-- Add survey_site_id to patient_attribute_history table
--

-- Add survey_site_id column
alter table patient_attribute_history add (survey_site_id number(19,0));

-- Set survey_site_id values
update patient_attribute_history set survey_site_id = ( select distinct(survey_site_id) from survey_registration );

commit;

-- Add not null to survey_site_id
alter table patient_attribute_history modify (survey_site_id constraint patient_att_his_site_id not null);

--
--  Add survey_site_id to notification table
--

-- Add survey_site_id column
alter table notification add (survey_site_id number(19,0));

-- Add foreign key constraint for survey_site_id
alter table notification add constraint notification_site_id_fk foreign key (survey_site_id) 
references survey_site (survey_site_id);

-- Set survey_site_id values
update notification set survey_site_id = ( select distinct(survey_site_id) from survey_registration );

commit;

-- Add not null to survey_site_id
alter table notification modify (survey_site_id constraint notification_site_id not null);

declare
    already_exists exception;
    already_indexed exception;
    pragma exception_init( already_exists, -955 );
    pragma exception_init( already_indexed, -01408 );
    begin
    execute immediate 'create index not_sur_reg_id_idx on notification (survey_reg_id)';
    dbms_output.put_line( 'not_sur_reg_id_idx CREATED' );
    exception
        when already_exists then DBMS_OUTPUT.put_line( 'not_sur_reg_id_idx SKIPPED' );
        when already_indexed then DBMS_OUTPUT.put_line( 'not_sur_reg_id_idx SKIPPED' );
        null;
    end;
/

--
-- Add survey_site_id to survey_complete_push table
--

-- Add survey_site_id column
alter table survey_complete_push add (survey_site_id number(19,0));

-- Add foreign key constraint for survey_site_id
alter table survey_complete_push add constraint s_comp_push_site_fk foreign key (survey_site_id) 
references survey_site (survey_site_id);

-- Set survey_site_id values
update survey_complete_push set survey_site_id = ( select distinct(survey_site_id) from survey_registration );

commit;

-- Add not null to survey_site_id
alter table survey_complete_push modify (survey_site_id constraint survey_complete_push_site_id not null);

--
-- Update the user_authority table
--

-- Add the site name to the authority
update user_authority set authority = authority || '[' || 
  (select url_param from survey_site where survey_site_id = (select distinct(survey_site_id) from survey_registration)) ||
  ']';

commit;

-- Create new table to hold survey video actions
create table survey_player_progress (
  survey_player_progress_id  numeric(19)   not null, -- pk
  survey_token_id            numeric(19)   not null, -- fk
  survey_site_id             numeric(19)   not null, -- fk
  player_id                  varchar(255)  not null,
  player_action              varchar(255)  not null,
  player_time_millis         numeric, 
  posted_time                timestamp,
  constraint s_player_progress_pk primary key (survey_player_progress_id),
  constraint s_player_progress_token_fk foreign key (survey_token_id) references survey_token,
  constraint s_player_progress_site_fk foreign key (survey_site_id) references survey_site
);

-- Add a table for the list of MRNs for fake patients used for testing - exclude from reports
create table patient_test_only (
  patient_id varchar2(50), 
  constraint patient_test_only_pk primary key (patient_id),
  constraint patient_test_only_fk foreign key (patient_id) references patient
);

-- Create a new table for application site specific configuration parameters
create table app_config (
  app_config_id  numeric(19)   not null, -- pk
  survey_site_id numeric(19)   not null, -- fk
  config_name  varchar2(255)   not null,
  config_value varchar2(4000)  not null,
  enabled char(1) default 'Y' check (enabled in ('Y','N')),
  constraint app_config_pk primary key (app_config_id),
  constraint app_config_site_fk foreign key (survey_site_id) references survey_site
);
       
-- There should be a row in this table for every change to app_config
-- (to record in a structured way any application configuration changes by site).
 create table app_config_change_history ( 
  revision_number   numeric(19)    not null, -- pk
  user_principal_id numeric(19)    not null,
  changed_at_time   date           default sysdate not null,
  change_type       char(1)        check (change_type in ('A','D','M','E')), -- Flag: A=added, M=modified, D=disabled 
  app_config_id     numeric(19)    not null, 
  survey_site_id    numeric(19)    not null,
  config_name       varchar2(255)  not null,
  config_value      varchar2(4000) not null,
  constraint app_conf_hist_pk primary key (revision_number),
  constraint app_conf_hist_principal_fk foreign key (user_principal_id) references user_principal,
  constraint app_conf_hist_config_id_fk foreign key (app_config_id) references app_config
);
  
create sequence app_config_sequence minvalue 1 maxvalue 999999999999999999 
start with 1000 increment by 1 cache 20 noorder nocycle;
     
create sequence app_config_change_sequence minvalue 1 maxvalue 999999999999999999 
start with 1000 increment by 1 cache 20 noorder nocycle;

create table service_audit (
  service_audit_id               numeric(19) not null,
  username                       varchar2(128),
  ip_address                     varchar2(40),
  service_path                   varchar2(20),
  login_time                     timestamp(3),
  java_version                   varchar2(20),
  java_vendor                    varchar2(20),
  os_name                        varchar2(20),
  os_version                     varchar2(20),
  os_arch                        varchar2(20),
  user_agent                     varchar2(4000),
  constraint service_audit_pk               primary key (service_audit_id)
);

comment on column service_audit.service_audit_id is
'Internally generated primary key';

create sequence service_audit_seq minvalue 1 maxvalue 999999999999999999 
start with 1000 increment by 1 cache 20 noorder nocycle;
 
--
-- changing token from integer to string
--
alter table activity add (token_str  varchar2(4000)) ;
update activity set token_str =  to_char(token);
alter table activity drop column token;
alter table activity rename column token_str to token;
commit;

declare
begin
  execute immediate 'drop index not_sur_reg_pat_tok_idx';
  execute immediate 'drop index not_sur_reg_pat_tok_fk';
  exception 
    when others then null;
end;
/

alter table notification add (token_str  varchar2(4000)) ;
update notification set token_str = to_char(token);
alter table notification drop column token;
alter table notification rename column token_str to token;
alter table notification modify (token constraint not_token not null);
create index not_sur_reg_pat_tok_idx on notification (patient_id, token);
create index not_token on notification(token);
commit;


declare 
 column_exists pls_integer;
 begin
     select count(*) into column_exists from USER_TAB_COLUMNS where table_name = 'PATIENT_STUDY' and COLUMN_NAME ='XML_RESULTS';
     if column_exists > 0 then
        execute immediate 'alter trigger PATIENT_STUDY_BIU_TRIGGER disable';
        DBMS_OUTPUT.put_line('trigger DISABLED');
    end if;
end;
/
        
drop index patient_study_pat_token_inx;
alter table patient_study add (token_str  varchar2(4000)) ;
update patient_study set token_str = to_char(token);
-- ******************************************************************
-- * IF USING BASIC COMPRESSION ON THE PATIENT_STUDY TABLE COMMENT  *
-- * OUT THE NEXT LINE THAT DROPS THE TOKEN COLUMN AND USE THE TWO  *
-- * FOLLOWING LINES TO RENAME IT AND SET IT TO UNUSED INSTEAD !    *
-- ******************************************************************
alter table patient_study drop column token;
-- alter table patient_study rename column token to token_old;
-- alter table patient_study set unused column token_old;
-- ******************************************************************
alter table patient_study rename column token_str to token;
alter table patient_study modify (token constraint patient_study_token not null);

commit; 


declare
    already_exists exception;
    already_indexed exception;
    pragma exception_init( already_exists, -955 );
    pragma exception_init( already_indexed, -01408 );
    begin
    execute immediate 'create index patient_study_pat_token_inx on patient_study (survey_site_id, patient_id, token)';
    dbms_output.put_line( 'patient_study_pat_token_inx CREATED' );
    exception
        when already_exists then DBMS_OUTPUT.put_line( 'patient_study_pat_token_inx SKIPPED' );
        when already_indexed then DBMS_OUTPUT.put_line( 'patient_study_pat_token_inx SKIPPED' );
        null;
    end;
/
drop index PATIENT_STUDY_SURVEY_REG_IDX;
declare
    already_exists exception;
    already_indexed exception;
    pragma exception_init( already_exists, -955 );
    pragma exception_init( already_indexed, -01408 );
    begin
        execute immediate 'create index PATIENT_STUDY_SURVEY_REG_IDX on patient_study (survey_reg_id)';
    dbms_output.put_line( 'patient_study_pat_token_inx CREATED' );
    exception
        when already_exists then DBMS_OUTPUT.put_line( 'patient_study_survey_reg_idx SKIPPED' );
        when already_indexed then DBMS_OUTPUT.put_line( 'patient_study_survey_reg_idx SKIPPED' );
        null;
    end;
/

declare 
 column_exists pls_integer;
 begin
    select count(*) into column_exists from USER_TAB_COLUMNS where table_name = 'PATIENT_STUDY' and COLUMN_NAME ='XML_RESULTS';
    if column_exists > 0 then
        execute immediate 'create or replace TRIGGER PATIENT_STUDY_BIU_TRIGGER
        BEFORE INSERT OR UPDATE ON PATIENT_STUDY
            FOR EACH ROW WHEN (new.XML_CLOB IS NOT NULL)
            DECLARE
            BEGIN
                if (ISVALIDXML(:new.XML_CLOB) = 1 ) then
                    :new.XML_RESULTS := xmlType( :new.XML_CLOB , null, 1, 1);
                end if;
        END;';
        execute immediate 'alter trigger PATIENT_STUDY_BIU_TRIGGER enable';
        DBMS_OUTPUT.put_line('trigger ENABLED');
    end if;
end;
/

drop index survey_reg_pat_tok_uq;
alter table survey_registration add (token_str  varchar2(4000)) ;
update survey_registration set token_str = to_char(token);
alter table survey_registration drop column token;
alter table survey_registration rename column token_str to token;
alter table survey_registration modify (token constraint survey_reg_token not null);
create unique index survey_reg_pat_tok_uq on survey_registration (survey_site_id, patient_id, token);
commit;


