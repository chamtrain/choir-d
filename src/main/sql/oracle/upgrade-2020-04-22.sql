/*
 * Oracle database script for multiple identity providers upgrade
 *
 * Step 1: Alter the database schema
 * Step 2: (optional) Rename the "Default" identity provider for your environment
 * Step 3: (optional) Remove the backup table. 
 *         This step is commented out and should be run later on, after 
 *         the upgrade has been successfully completed and confirmed.     
 */
-- Step 1
 
create table original_user_principal as (select user_principal_id, username, display_name, enabled, email_addr from user_principal);

alter table user_principal drop column username;
alter table user_principal drop column enabled;	
	
create table user_idp (
    idp_id numeric(19) not null,
    abbr_name varchar(16) not null,
    display_name varchar(256) not null, 
    constraint user_idp_pk primary key (idp_id),
    constraint u_idp_abbr_uq unique (abbr_name),
    constraint u_idp_abbr_lower_ck  check (lower(abbr_name) = abbr_name)
);

insert into user_idp (idp_id, abbr_name, display_name) 
    values (1, 'dflt', 'Default Identity Provider');
 
create table user_credential (
    idp_id numeric(19) not null,
    user_principal_id numeric(19) not null,
    username varchar(128) not null,
    enabled  char(1) default 'Y' check (enabled in ('Y','N')),
    constraint u_cred_pk primary key (idp_id, user_principal_id),
    constraint u_cred_idp_fk foreign key (idp_id) references user_idp,
    constraint u_cred_principal_fk foreign key (user_principal_id) references user_principal,
    constraint u_cred_username_lower_ck check (lower(username) = username),
    constraint u_cred_username_uq unique (username)
    );
    
comment on column user_credential.enabled is 'Disable to prevent this user from logging in or accessing anything (effectively a soft delete, since we don''t hard delete these';
comment on table user_credential is 'Each row represents a users credentials for a specific identity provider';

insert into user_credential (idp_id, user_principal_id, username, enabled) 
	select 1, user_principal_id, username, enabled 
	from original_user_principal; 
	
create sequence user_idp_sequence minvalue 2 maxvalue 999999999999999999 
    start with 10 increment by 1 cache 20 noorder nocycle;

/*
 *  Optionally edit and uncomment to 
 *  1. Change the default IDP to an appropriate name for your implementation
 *  2. Add additional IDP's needed
 *         NOTE: the value of 'abbr_name' must be lowercase
 */
 
--  Step 2 (optional)
--
-- update user_idp set abbr_name = 'som', display_name = 'Stanford School of Medicine' where abbr_name = 'dflt';

-- insert into user_idp (idp_id, abbr_name, display_name) values (user_idp_sequence.nextval(), 'shc', 'Stanford Health Care');

/*
 * Run ONLY after the upgrade has be thoroughly tested.
 */
-- Step 3 (optional)
--
/* drop table original_user_principal; */
