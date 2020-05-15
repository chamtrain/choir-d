-- New table to store various configuration state associated with the user
create table user_preference (
  user_principal_id        number(18,0) not null references user_principal,
  survey_site_id	   number(19,0),
  preference_key           varchar2(4000) not null,
  preference_value         clob
);

alter table user_preference add constraint user_preference_pk primary key (user_principal_id, survey_site_id, preference_key);
