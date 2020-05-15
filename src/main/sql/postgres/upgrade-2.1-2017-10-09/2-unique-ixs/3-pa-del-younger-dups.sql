-- PostGres script
\set IGNOREEOF 3
\echo 
\echo 
\echo 
\echo 
\echo 
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 3'
\echo '-- ======== Of the duplicate PATIENT_ATTRIBUTEs, remove the earlier one'
\echo
\echo '-- If there are duplicates, it is because the identical value was made twice at almost the same instant.'
\echo '--   If they both lack change dates, we can keep either one.'
\echo '--   If one or both have change dates, we will keep the latest one.'
\echo
\echo '-- FYI: First we update the null DT_CHANGED fields to be dates a bit smaller than the DT_CREATED fields.'
\echo '--   This ensures none of them are null, but they are smaller than any non-null changed date, if one exists'
\echo '--   And we give them slightly different dates, in case they are all null.'
\echo '--   And we will keep the one with the latest change date.'
\echo '--   And then we will change back to null any change dates less than the DT_CREATED fields.'
\echo
\echo '-- Step 1:  We update the null PATIENT_ATTRIBUTE . DT_CHANGED fields to be a bit less than DT_CREATED'
\echo

UPDATE patient_attribute pa
   SET dt_changed = tmp.max_date - make_interval(hours := (1 + (pa.patient_attribute_id::int % 32)))
  FROM tmp_dup_patient_attribute tmp
 WHERE  pa.survey_site_id = tmp.survey_site_id
   AND      pa.patient_id = tmp.patient_id 
   AND       pa.data_name = tmp.data_name
   AND  pa.dt_changed IS NULL;

-- If you want to view some of the table...
-- SELECT * FROM tmp_dup_patient_attribute tmp FETCH FIRST 10 ROWS ONLY;

\echo '-- Step 2: Now we put the max(DT_CHANGED) of each set of duplicates into the temp table'
\echo

UPDATE tmp_dup_patient_attribute tmp
   SET max_chgd = pa.max_chgd
  FROM (
     SELECT p.survey_site_id, p.patient_id, p.data_name, max(DT_CHANGED) max_chgd
       FROM patient_attribute p
      GROUP BY p.survey_site_id, p.patient_id, p.data_name
      ) pa
 WHERE  pa.survey_site_id = tmp.survey_site_id
   AND      pa.patient_id = tmp.patient_id 
   AND       pa.data_name = tmp.data_name
   AND  tmp.max_chgd IS NULL;



\echo '-- Step 3:  Count the number of patient_attribute_histories,'
\echo '--   then insert all the rows to delete into the history table'
\echo '--   and count the number of histories afterwards.'
\echo

SELECT count(*) number_of_histories FROM patient_attribute_history;

\echo '-- here is the insert'
INSERT INTO patient_attribute_history 
   (patient_attribute_history_id, patient_attribute_id,    survey_site_id,    patient_id,    data_name, 
    data_value, data_type, meta_version, dt_created, dt_changed, user_principal_id) 
SELECT nextval('patient_seq'), pa.patient_attribute_id, pa.survey_site_id, pa.patient_id, pa.data_name, 
    data_value, data_type, meta_version, dt_created, current_timestamp, 1 
 FROM patient_attribute pa, tmp_dup_patient_attribute tmp
 WHERE  pa.survey_site_id = tmp.survey_site_id
   AND      pa.patient_id = tmp.patient_id 
   AND       pa.data_name = tmp.data_name
   AND pa.DT_CHANGED < tmp.MAX_CHGD;

SELECT count(*) number_of_histories FROM patient_attribute_history;



\echo '-- Step 4:  Now we delete all but the max DT_CHANGED row, of the duplicate rows'
\echo

DELETE FROM patient_attribute pa 
 USING tmp_dup_patient_attribute tmp
 WHERE  pa.survey_site_id = tmp.survey_site_id
   AND      pa.patient_id = tmp.patient_id 
   AND       pa.data_name = tmp.data_name
   AND pa.DT_CHANGED < tmp.MAX_CHGD;



\echo '-- Step 5: To check, there should be the same number of rows in the tmp'
\echo '--   table as there are patient_attributes that match it'
\echo

SELECT (SELECT count(*) FROM tmp_dup_patient_attribute) total_in_tmp, 
       (SELECT count(*) FROM patient_attribute pa, tmp_dup_patient_attribute tmp
         WHERE  pa.survey_site_id = tmp.survey_site_id
	   AND      pa.patient_id = tmp.patient_id 
	   AND       pa.data_name = tmp.data_name) countMatchingTmp;

\echo '-- If total_in_tmp == countMatchingTmp, that is great'
\echo



\echo '-- Step 6: one more check- how many duplicates?'
\echo

SELECT count(*) FROM (
  SELECT p.survey_site_id, p.patient_id, p.data_name, count(*) row_count
    FROM patient_attribute p
  GROUP BY p.survey_site_id, p.patient_id, p.data_name
) x WHERE row_count > 1;

\echo '-- if that was zero, all is well...'
\echo
\echo '-- now we can make the index unique:  \\i 4-pa-fix-unique-index.sql'
\echo
