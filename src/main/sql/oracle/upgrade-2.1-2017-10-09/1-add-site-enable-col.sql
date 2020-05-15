prompt 
prompt 
prompt 
prompt 
prompt 
prompt ============= Step 1- Add a boolean ENABLED column to the SURVEY_SITE table ================
prompt ============= and add some columns to two square-tables, if they exist
prompt
prompt == We have been wanting to add an ENABLED (Y/N) column to the SURVEY_SITE table:
prompt == Here is the old table:
prompt
set linesize 80
column NAME format a16
column TYPE format a40
column SURVEY_SITE_ID format 99999
column URL_PARAM format a12
column DISPLAY_NAME format a40
column ENABLED format a7

DESCRIBE survey_site;

prompt == Now add the column:

ALTER TABLE survey_site ADD enabled CHAR(1) DEFAULT 'Y' NOT NULL;

prompt == And add a constraint so the value is always Y or N:

ALTER TABLE survey_site ADD CONSTRAINT survey_site_enabled_bool_ck CHECK (enabled in ('N', 'Y')) ENABLE;

prompt == Here is the table now:
prompt

DESCRIBE survey_site;

prompt == And just FYI, the contents
prompt
SELECT * FROM survey_site ORDER BY SURVEY_SITE_ID;

prompt
prompt == If you do not have the table RPT_PAIN_STD_SURVEYS_SQUARE, it is okay that this fails

alter table rpt_pain_std_surveys_square add (
    s5_treat_nerv_steriod number(10,0),
    s5_treat_education number(10,5),
    fa_disability_wcomp number(10,5),
    fa_disability_ssdi number (10,5),
    fa_missed_work number (10,5),
    fa_prod_reduced number (10,5),
    ptsd_guilty number (10,5)
);

prompt
prompt == If you do not have the table RPT_TREATMENTHX_SQUARE, it is okey that the next command fails
alter table rpt_treatmenthx_square add (
  t_inte_radiofreq char(1),
  t_inte_radiofreq_effect number(10,0)
);

prompt
prompt == We will quit sqlplus.  Next:  % cd 2-unique-ixs; cat ReadMe.txt
prompt
quit

-- commented out, for repeating this, for testing:  alter table survey_site drop column enabled;
