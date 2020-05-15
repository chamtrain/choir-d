-- PostGres script
\set IGNOREEOF 3

\echo
\echo
\echo
\echo
\echo '============= 1a.'
\echo '============= chk PATIENT_STUDY unused EMAIL_ADDR column ================'
\echo
\echo '== The EMAIL_ADDR column should not be in table PATIENT_STUDY'
\echo '== Lets look if your table contains it.'
\echo '==   If it says "no rows selected", you do not have the column'

select table_name, column_name from information_schema.columns
 where table_name='patient_study' and column_name='email_addr';

\echo '== If the column was not listed by the query above, '
\echo '==   then this next one will have an error, which you can ignore'
\echo '==   else the next query will count PATIENT_STUDY rows wi null and non-null email_addr values:'

SELECT count(case when email_addr is null then 1 end) rows_wi_null_email_addr,
       count(email_addr)                              rows_wi_nonnull_email_addr
FROM patient_study;

\echo '== If not all of our PATIENT_STUDY rows have null email_addrs,'
\echo '==   we do not know how they got there- perhaps someone, or your code added them?'
\echo
\echo '== If there are only nulls, we recommend you remove this column with: \\i 1b-rm-pat-study-email-col.sql'
\echo
\echo '== If there were non-nulls you want to keep,'
\echo '== Or there was an error:   ORA-00904: "EMAIL_ADDR": invalid identifier'
\echo '== continue with:  \\i 2a-chk-pk-for-user-pref.sql'
\echo

