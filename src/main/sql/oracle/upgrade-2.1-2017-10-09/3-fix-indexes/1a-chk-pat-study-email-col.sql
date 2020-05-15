@column-formats

-- turn off echoing before/after variable substitution
prompt 
prompt 
prompt 
prompt 
prompt ============= 1a.
prompt ============= chk PATIENT_STUDY unused EMAIL_ADDR column ================
prompt
prompt == The EMAIL_ADDR column should not be in table PATIENT_STUDY
prompt == Lets look if your table contains it.
prompt ==   If it says 'no rows selected', you do not have the column

column TABLE_NAME format a13
column COLUMN_NAME format a21

SELECT table_name, column_name FROM user_tab_columns 
 WHERE table_name='PATIENT_STUDY' AND column_name='EMAIL_ADDR';

prompt == If the column was not listed by the query above, 
prompt ==   then this next one will have an error, which you can ignore
prompt ==   else the next query will count PATIENT_STUDY rows wi null and non-null email_addr values:

SELECT count(case when email_addr is null then 1 end) rows_wi_null_email_addr,
       count(email_addr)                              rows_wi_nonnull_email_addr
FROM patient_study;

prompt == If not all of our PATIENT_STUDY rows have null email_addrs,
prompt ==   we do not know how they got there- perhaps someone, or your code added them?
prompt
prompt == If there are only nulls, we recommend you remove this column with: @ 1b-rm-pat-study-email-col
prompt
prompt == If there were non-nulls you want to keep,
prompt == Or there was an error:   ORA-00904: "EMAIL_ADDR": invalid identifier
prompt == continue with:  @ 2a-chk-pk-for-user-pref
prompt
