--
-- Add hoos_version ('HOOS' or 'HOOS_JR') to tj_hip_responses table
--
alter table tj_hip_responses add (hoos_version varchar(16));

--
-- Add koos_version ('KOOS' or 'KOOS_JR') to tj_knee_responses table
--
alter table tj_knee_responses add (koos_version varchar(16));
