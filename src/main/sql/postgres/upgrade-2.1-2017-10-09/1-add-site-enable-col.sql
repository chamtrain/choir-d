-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '============= Step 1- Add a boolean ENABLED column to the SURVEY_SITE table ================'
\echo '============= and add some columns to two square-tables, if they exist'
\echo
\echo '== We have been wanting to add an ENABLED (Y/N) column to the SURVEY_SITE table:'
\echo '== Here is the old table, showing the columns for one row:'
\echo
SELECT * FROM survey_site WHERE length(display_name) = (SELECT max(length(display_name)) FROM survey_site) limit 1;

\echo '== Now add the column:'

ALTER TABLE survey_site ADD enabled CHAR(1) DEFAULT 'Y' NOT NULL;

\echo '== And add a constraint so the value is always Y or N:'

ALTER TABLE survey_site ADD CONSTRAINT survey_site_enabled_bool_ck CHECK (enabled in ('N', 'Y'));

\echo '== Here are the columns now, showing all the rows'
\echo
SELECT * FROM survey_site ORDER BY SURVEY_SITE_ID;


\echo
\echo '== If you do not have the table RPT_PAIN_STD_SURVEYS_SQUARE, it is okay that this fails'

ALTER TABLE rpt_pain_std_surveys_square
    ADD s5_treat_nerv_steriod numeric(10,0),
    ADD s5_treat_education numeric(10,5),
    ADD fa_disability_wcomp numeric(10,5),
    ADD fa_disability_ssdi numeric (10,5),
    ADD fa_missed_work numeric (10,5),
    ADD fa_prod_reduced numeric (10,5),
    ADD ptsd_guilty numeric (10,5);

\echo
\echo '== If you do not have the table RPT_TREATMENTHX_SQUARE, it is okey that the next command fails'
ALTER TABLE rpt_treatmenthx_square
  ADD t_inte_radiofreq char(1),
  ADD t_inte_radiofreq_effect numeric(10,0);

\echo
\echo '== Next:  % cd 2-unique-ixs; cat readme.txt'
\echo

-- commented out, for repeating this, for testing:  alter table survey_site drop column enabled;
