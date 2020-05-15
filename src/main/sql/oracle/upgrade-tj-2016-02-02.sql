--
-- Total Joint Registry
--
-- Create table tj_preop_surgery_date used by TotalJointSync to store data
-- transferred from the Stride schema.
--

create table tj_preop_surgery_date (
  mrn varchar2(50) not null,
  surgery_date timestamp(3),
  joint varchar(8),
  laterality varchar(12)
);
