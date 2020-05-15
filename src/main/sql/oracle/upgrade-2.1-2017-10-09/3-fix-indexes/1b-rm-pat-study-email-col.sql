@column-formats

-- turn off echoing before/after variable substitution
prompt 
prompt 
prompt 
prompt 
prompt 
prompt ============= 1b.
prompt ============= Remove the unused EMAIL_ADDR column from PATIENT_STUDY ================

column TABLE_NAME format a13
column COLUMN_NAME format a21

prompt == Before removing it:

SELECT table_name, column_name FROM user_tab_columns
 WHERE table_name='PATIENT_STUDY' AND column_name='EMAIL_ADDR';

prompt == And the number of columns in your PATIENT_STUDY table:

SELECT count(*) number_of_columns FROM user_tab_columns 
 WHERE table_name='PATIENT_STUDY';

prompt == Removing the column:

ALTER TABLE PATIENT_STUDY drop column EMAIL_ADDR;

prompt == After removing it:

SELECT table_name, column_name FROM user_tab_columns 
 WHERE table_name='PATIENT_STUDY' AND column_name='EMAIL_ADDR';

prompt == And the number of columns in your PATIENT_STUDY table is now:

SELECT count(*) number_of_columns FROM user_tab_columns 
 WHERE table_name='PATIENT_STUDY';

prompt == Continue with: @ 2a-chk-pk-for-user-pref
prompt
