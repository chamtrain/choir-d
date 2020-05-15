--
-- Total Joint Registry
--
-- Create tables to store the responses for the hip and knee surveys.
-- Note: A bilateral survey will create two rows in the table for the
-- same survey_reg_id, one for the left side and one for the right side.
--
-- Register SurveyCompleteHandler to store the responses in the tables.
--

create sequence tj_responses_seq;

create table tj_hip_responses (
  tj_responses_id bigint  primary key,
  survey_reg_id bigint  not null,
  survey_token varchar(50) not null,
  patient_id varchar(50) not null,
  scheduled timestamp(3) not null,
  completed timestamp(3) not null,
  survey_type varchar(200) not null,
  side varchar(8) not null,
  UCLA_Q1 varchar(4),
  VR12_GH1 varchar(4), 
  VR12_PF02 varchar(4), 
  VR12_PF04 varchar(4), 
  VR12_RP2 varchar(4), 
  VR12_RP3 varchar(4), 
  VR12_RE2 varchar(4), 
  VR12_RE3 varchar(4),
  VR12_BP2 varchar(4), 
  VR12_MH3 varchar(4), 
  VR12_VT2 varchar(4), 
  VR12_MH4 varchar(4), 
  VR12_SF2 varchar(4), 
  VR12_Q8 varchar(4), 
  VR12_Q9 varchar(4),
  HOOS_S1 varchar(4),
  HOOS_S2 varchar(4),
  HOOS_S3 varchar(4),
  HOOS_S4 varchar(4),
  HOOS_S5 varchar(4),
  HOOS_P1 varchar(4),
  HOOS_P2 varchar(4),
  HOOS_P3 varchar(4),
  HOOS_P4 varchar(4),
  HOOS_P5 varchar(4),
  HOOS_P6 varchar(4),
  HOOS_P7 varchar(4),
  HOOS_P8 varchar(4),
  HOOS_P9 varchar(4),
  HOOS_P10 varchar(4),
  HOOS_A1 varchar(4),
  HOOS_A2 varchar(4),
  HOOS_A3 varchar(4),
  HOOS_A4 varchar(4),
  HOOS_A5 varchar(4),
  HOOS_A6 varchar(4),
  HOOS_A7 varchar(4),
  HOOS_A8 varchar(4),
  HOOS_A9 varchar(4),
  HOOS_A10 varchar(4),
  HOOS_A11 varchar(4),
  HOOS_A12 varchar(4),
  HOOS_A13 varchar(4),
  HOOS_A14 varchar(4),
  HOOS_A15 varchar(4),
  HOOS_A16 varchar(4),
  HOOS_A17 varchar(4),
  HOOS_SP1 varchar(4),
  HOOS_SP2 varchar(4),
  HOOS_SP3 varchar(4),
  HOOS_SP4 varchar(4),
  HOOS_Q1 varchar(4),
  HOOS_Q2 varchar(4),
  HOOS_Q3 varchar(4),
  HOOS_Q4 varchar(4),
  HARRIS_Q1 varchar(4),
  HARRIS_Q2 varchar(4),
  HARRIS_Q3 varchar(4),
  HARRIS_Q4 varchar(4),
  HARRIS_Q5 varchar(4),
  HARRIS_Q6 varchar(4),
  HARRIS_Q7 varchar(4),
  HARRIS_Q8 varchar(4),
  CONSTRAINT tj_hip_responses_unique UNIQUE (survey_reg_id,side)
);

create table tj_knee_responses (
  tj_responses_id bigint primary key,
  survey_reg_id bigint not null,
  survey_token varchar(50) not null,
  patient_id varchar(50) not null,
  scheduled timestamp(3) not null,
  completed timestamp(3) not null,
  survey_type varchar(200) not null,
  side varchar(8) not null,
  UCLA_Q1 varchar(4),
  VR12_GH1 varchar(4), 
  VR12_PF02 varchar(4), 
  VR12_PF04 varchar(4), 
  VR12_RP2 varchar(4), 
  VR12_RP3 varchar(4), 
  VR12_RE2 varchar(4), 
  VR12_RE3 varchar(4),
  VR12_BP2 varchar(4), 
  VR12_MH3 varchar(4), 
  VR12_VT2 varchar(4), 
  VR12_MH4 varchar(4), 
  VR12_SF2 varchar(4), 
  VR12_Q8 varchar(4), 
  VR12_Q9 varchar(4),
  KOOS_S1 varchar(4),
  KOOS_S2 varchar(4),
  KOOS_S3 varchar(4),
  KOOS_S4 varchar(4),
  KOOS_S5 varchar(4),
  KOOS_S6 varchar(4),
  KOOS_S7 varchar(4),
  KOOS_P1 varchar(4),
  KOOS_P2 varchar(4),
  KOOS_P3 varchar(4),
  KOOS_P4 varchar(4),
  KOOS_P5 varchar(4),
  KOOS_P6 varchar(4),
  KOOS_P7 varchar(4),
  KOOS_P8 varchar(4),
  KOOS_P9 varchar(4),
  KOOS_A1 varchar(4),
  KOOS_A2 varchar(4),
  KOOS_A3 varchar(4),
  KOOS_A4 varchar(4),
  KOOS_A5 varchar(4),
  KOOS_A6 varchar(4),
  KOOS_A7 varchar(4),
  KOOS_A8 varchar(4),
  KOOS_A9 varchar(4),
  KOOS_A10 varchar(4),
  KOOS_A11 varchar(4),
  KOOS_A12 varchar(4),
  KOOS_A13 varchar(4),
  KOOS_A14 varchar(4),
  KOOS_A15 varchar(4),
  KOOS_A16 varchar(4),
  KOOS_A17 varchar(4),
  KOOS_SP1 varchar(4),
  KOOS_SP2 varchar(4),
  KOOS_SP3 varchar(4),
  KOOS_SP4 varchar(4),
  KOOS_SP5 varchar(4),
  KOOS_Q1 varchar(4),
  KOOS_Q2 varchar(4),
  KOOS_Q3 varchar(4),
  KOOS_Q4 varchar(4),
  KS_S1 varchar(4),
  KS_S2 varchar(4),
  KS_S3 varchar(4),
  KS_PS1 varchar(4),
  KS_PS2 varchar(4),
  KS_PS3 varchar(4),
  KS_PS4 varchar(4),
  KS_PS5 varchar(4),
  KS_PE1 varchar(4),
  KS_PE2 varchar(4),
  KS_PE3 varchar(4),
  KS_PE4 varchar(4),
  KS_PE5 varchar(4),
  KS_PE6 varchar(4),
  KS_FA1 varchar(4),
  KS_FA2 varchar(4),
  KS_FA3 varchar(80),
  KS_FA4 varchar(4),
  KS_Q1 varchar(4),
  KS_Q2 varchar(4),
  KS_Q3 varchar(4),
  KS_Q4 varchar(4),
  KS_Q5 varchar(4),
  KS_Q6 varchar(4),
  KS_Q7 varchar(4),
  KS_Q8 varchar(4),
  KS_Q9 varchar(4),
  KS_Q10 varchar(4),
  KS_Q11 varchar(4),
  KS_Q12 varchar(4),
  KS_Q13 varchar(4),
  KS_R1A varchar(4),
  KS_R1B varchar(4),
  KS_R2A varchar(4),
  KS_R2B varchar(4),
  KS_R3A varchar(4),
  KS_R3B varchar(4),
  CONSTRAINT tj_knee_responses_unique UNIQUE (survey_reg_id,side)
);

-- Create tables used by TotalJointSync to store data transferred from
-- the Stride schema.
--

create table tj_stride_patient (
  mrn varchar(50) primary key,
  first_name varchar(100),
  last_name varchar(100),
  date_of_birth timestamp(3),
  date_of_death timestamp(3),
  gender varchar(8),
  totaljoint_consent varchar(20)
);

create table tj_surgery_date (
  mrn varchar(50) not null,
  surgery_date timestamp(3),
  joint varchar(8),
  side varchar(8)
);

create table tj_stride_completed_survey (
  mrn varchar(50) not null,
  joint varchar(8),
  side varchar(8),
  scheduled timestamp(3),
  completed timestamp(3)
);

--
-- Create table tj_preop_surgery_date used by TotalJointSync to store data
-- transferred from the Stride schema.
--

create table tj_preop_surgery_date (
  mrn varchar(50) not null,
  surgery_date timestamp(3),
  joint varchar(8),
  laterality varchar(12)
);

--
-- Add hoos_version ('HOOS' or 'HOOS_JR') to tj_hip_responses table
--
alter table tj_hip_responses add hoos_version varchar(16);

--
-- Add koos_version ('KOOS' or 'KOOS_JR') to tj_knee_responses table
--
alter table tj_knee_responses add koos_version varchar(16);
