@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 3
prompt -- ======== Remove the earlier duplicate PATIENT_ATTRIBUTEs
prompt
prompt -- If there are duplicates, it's because the identical value was made twice at almost the same instant.
prompt --   If they both lack change dates, we can keep either one.
prompt --   If one or both have change dates, we'll keep the latest one.

prompt -- FYI: First we update the null DT_CHANGED fields to be dates a bit smaller than the DT_CREATED fields.
prompt --   This ensures none of them are null, but they are smaller than any non-null changed date, if one exists
prompt --   And we give them slightly different dates, in case they are all null.
prompt --   And we'll keep the one with the latest change date.
prompt --   And then we'll change back to null any change dates less than the DT_CREATED fields.


prompt
prompt -- Step 1:  We update the null DT_CHANGED fields to be a bit less than DT_CREATED

-- If run a second time, this will update zero rows, so Okay
UPDATE
 (SELECT pa.dt_changed, tmp.max_date, pa.patient_attribute_id id 
    FROM tmp_dup_patient_attribute tmp, patient_attribute pa
   WHERE pa.survey_site_id = tmp.survey_site_id
     AND      pa.patient_id = tmp.patient_id 
     AND       pa.data_name = tmp.data_name
     AND  pa.dt_changed IS NULL) t 
 SET t.dt_changed = t.max_date -(1 + MOD(t.id, 32))
;

-- If you want to view some of the table...
-- SELECT * FROM tmp_dup_patient_attribute tmp FETCH FIRST 10 ROWS ONLY;



prompt -- Step 2: Now we put the max(DT_CHANGED) of each set of duplicates into the temp table

-- If run a second time, this will update the tmp table redundantly, which is fine.
UPDATE tmp_dup_patient_attribute tmp
   SET max_chgd = 
     (SELECT max(DT_CHANGED)
       FROM patient_attribute pa
      WHERE  pa.survey_site_id = tmp.survey_site_id
	AND      pa.patient_id = tmp.patient_id 
        AND       pa.data_name = tmp.data_name);



prompt -- Step 3:  TODO:  Insert them into PATIENT_ATTRIBUTE_HISTORY!

prompt "-- Step 3:  Count the number of patient_attribute_histories,"
prompt "--   then insert all the rows to delete into the history table""
prompt "--   and count the number of histories afterwards."
prompt

SELECT count(*) count_pat_attr_histories FROM patient_attribute_history;

prompt "-- here's the insert"

INSERT INTO patient_attribute_history 
   (patient_attribute_history_id, patient_attribute_id,    survey_site_id,    patient_id,    data_name, 
       data_value,    data_type,    meta_version,    dt_created,  dt_changed, user_principal_id) 
SELECT patient_seq.nextval,       patient_attribute_id, pa.survey_site_id, pa.patient_id, pa.data_name, 
    pa.data_value, pa.data_type, pa.meta_version, pa.dt_created, current_timestamp, 1 
 FROM patient_attribute pa, tmp_dup_patient_attribute tmp
 WHERE  pa.survey_site_id = tmp.survey_site_id
   AND      pa.patient_id = tmp.patient_id 
   AND       pa.data_name = tmp.data_name
   AND pa.DT_CHANGED < tmp.MAX_CHGD;

SELECT count(*) count_pat_attr_histories FROM patient_attribute_history;



prompt -- Step 4:  Now we delete all but the max DT_CHANGED row, of the duplicate rows

-- If run a second time, this will find zero rows to delete, so that's okay
MERGE INTO patient_attribute pa
USING tmp_dup_patient_attribute tmp
ON (
       pa.survey_site_id = tmp.survey_site_id
   AND     pa.patient_id = tmp.patient_id 
   AND      pa.data_name = tmp.data_name
   AND     pa.DT_CHANGED < tmp.MAX_CHGD
) WHEN MATCHED THEN UPDATE SET pa.data_value = pa.data_value  -- must have a SET clause...
  DELETE WHERE  pa.DT_CHANGED < tmp.MAX_CHGD;

-- Here's a simpler, less efficient form of the Delete query, FYI
--   DELETE FROM patient_attribute pa 
--    WHERE EXISTS 
--      (SELECT * FROM tmp_dup_patient_attribute tmp
--        WHERE pa.survey_site_id = tmp.survey_site_id
--         AND      pa.patient_id = tmp.patient_id 
--         AND       pa.data_name = tmp.data_name
--         AND pa.DT_CHANGED < tmp.MAX_CHGD);


prompt -- and, just to check, there should be the same number of rows in the tmp
prompt --   table as there are patient_attributes that match it

SELECT (SELECT count(*) FROM tmp_dup_patient_attribute) total_in_tmp, 
       (SELECT count(*) FROM patient_attribute pa, tmp_dup_patient_attribute tmp
         WHERE  pa.survey_site_id = tmp.survey_site_id
	   AND      pa.patient_id = tmp.patient_id 
	   AND       pa.data_name = tmp.data_name) count_attrs_matching_tmp
  FROM DUAL;

prompt -- and, one more check- how many duplicates?

SELECT count(*) FROM (
  SELECT p.survey_site_id, p.patient_id, p.data_name, count(*) row_count
    FROM patient_attribute p
  GROUP BY p.survey_site_id, p.patient_id, p.data_name
) x WHERE row_count > 1;

prompt
prompt -- If that was zero, all is well- make the index unique: @ 4-pa-fix-unique-index
prompt
prompt -- If not- is the system live? Maybe a new one was made?
prompt --    We can't make the unique index till all duplicates are gone...
prompt --    I suggest you start over with a new analysis table:  @ 2-pa-analyze-make-tmp
prompt

