--
-- Create the survey registration attribute table and the
-- survey registration attribute history table.
--

create sequence survey_reg_attr_hist_seq;

create table survey_reg_attr (
  survey_reg_id number(19,0),
  data_name varchar2(255),
  data_value varchar2(4000) not null,
  primary key(survey_reg_id, data_name),
  constraint survey_reg_attr_fk foreign key (survey_reg_id) references survey_registration (survey_reg_id)
);

create table survey_reg_attr_hist (
  survey_reg_attr_hist_id number(19,0) primary key,
  change_time timestamp(3) not null,
  survey_reg_id number(19,0) not null,
  data_name varchar2(255) not null,
  data_value varchar2(4000)
);
