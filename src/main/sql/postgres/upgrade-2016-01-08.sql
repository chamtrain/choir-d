CREATE TABLE user_preference (
    user_principal_id bigint,
    survey_site_id bigint NOT NULL,
    preference_key character varying(4000) NOT NULL,
    preference_value text
);


alter table user_preference add constraint user_preference_pk primary key (user_principal_id, survey_site_id, preference_key);
