--
-- Total Joint Registry
--
-- Create tables used by TotalJointSync to store data transferred from
-- the Stride schema.
--

create table tj_stride_patient (
  mrn varchar2(50) primary key,
  first_name varchar2(100),
  last_name varchar2(100),
  date_of_birth timestamp(3),
  date_of_death timestamp(3),
  gender varchar2(8),
  totaljoint_consent varchar2(20)
);

create table tj_surgery_date (
  mrn varchar2(50) not null,
  surgery_date timestamp(3),
  joint varchar(8),
  side varchar(8)
);

create table tj_stride_completed_survey (
  mrn varchar2(50) not null,
  joint varchar(8),
  side varchar(8),
  scheduled timestamp(3),
  completed timestamp(3)
);
