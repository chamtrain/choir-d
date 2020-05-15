-- These changes are also part of the 2.1 scripts

--
-- Add columns to the pain std surveys square table
--
alter table rpt_pain_std_surveys_square add (
  s5_treat_nerv_steriod number(10,0),
  s5_treat_education number(10,5),
  fa_disability_wcomp number(10,5),
  fa_disability_ssdi number (10,5),
  fa_missed_work number (10,5),
  fa_prod_reduced number (10,5),
  ptsd_guilty number (10,5)
);

--
-- Add columns to the pain treatments square table
--
alter table rpt_treatmenthx_square add (
  t_inte_radiofreq char(1),
  t_inte_radiofreq_effect number(10,0)
);
