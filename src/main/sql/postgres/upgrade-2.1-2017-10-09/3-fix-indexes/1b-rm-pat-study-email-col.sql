-- PostGres script
\set IGNOREEOF 3

\echo
\echo
\echo
\echo
\echo
\echo '============= 1b.'
\echo '============= Remove the unused EMAIL_ADDR column from PATIENT_STUDY ================'


\echo '== Before removing it:'

select table_name, column_name from information_schema.columns
 where table_name='patient_study' and column_name='email_addr';

\echo '== And the number of columns in your PATIENT_STUDY table:'

SELECT count(*) number_of_columns FROM information_schema.columns
 WHERE table_name='patient_study';

\echo '== Removing the column:'

ALTER TABLE PATIENT_STUDY drop column EMAIL_ADDR;

\echo '== After removing it:'

select table_name, column_name from information_schema.columns
 where table_name='patient_study' and column_name='email_addr';

\echo '== And the number of columns in your PATIENT_STUDY table is now:'

SELECT count(*) number_of_columns FROM information_schema.columns
 WHERE table_name='patient_study';

\echo '== Continue with: \\i 2a-chk-pk-for-user-pref.sql'
\echo
