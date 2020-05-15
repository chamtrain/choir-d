@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 2
prompt -- ======== Analyze the duplicate PATIENT_ATTRIBUTEs with a temporary table
prompt
prompt -- (If you ran this already, we'll drop the temporary table using some PL/SQL)

-- DROP TABLE IF EXISTS tmp_dup_patient_attribute;
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE tmp_dup_patient_attribute';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
         RAISE;
      END IF;
END;
/

prompt -- we'll make a table with all the site/patient/attribute_name trios that have duplicates,

CREATE TABLE tmp_dup_patient_attribute AS (
SELECT * FROM (
  SELECT p.survey_site_id, p.patient_id, p.data_name
       , count(distinct(p.data_value)) num_values
       , coalesce(count(dt_changed),0) num_chgd
       , count(*) row_count
       , max(DT_CHANGED) max_chgd
       , (max(DT_CHANGED) - min(DT_CREATED)) whole_time_span
       , (max(DT_CREATED) - min(DT_CREATED)) delta_created
       , max(greatest(NVL(DT_CHANGED,current_timestamp - 20000), DT_CREATED)) max_date
    FROM patient_attribute p
  GROUP BY p.survey_site_id, p.patient_id, p.data_name
) x WHERE row_count > 1);

prompt -- Add an index so joins run faster

CREATE UNIQUE INDEX tmp_dup_patient_at_ix 
    ON tmp_dup_patient_attribute ( survey_site_id, patient_id, data_name );

prompt -- This analysis table is:  TMP_DUP_PATIENT_ATTRIBUTE
prompt
prompt -- We expect ONLY pairs, but this query will show if there are triples or quads, and the number of each

SELECT count(*) num_of_each_kind, row_count "2=pairs,3=triples,4=..."
 FROM tmp_dup_patient_attribute
 GROUP BY row_count;

prompt -- FYI: This tell the number of different types, 
prompt --   and if some were updated (and thus have DT_CHANGED dates), and any multiple values

SELECT count(*) number_like_this, 
   CASE WHEN num_values=1 THEN 'Both have same value' 
        WHEN num_values=2 THEN 'Rows have 2 diff values' ELSE concat('huh?: ',num_values) END num_diff_values
  ,CASE WHEN num_chgd=0 THEN 'None were updated'
        WHEN num_chgd=1 THEN 'One was updated' 
        WHEN num_chgd=2 THEN 'Both were updated!' ELSE concat('huh?: ',num_chgd) END num_wi_chgd_dates
 FROM tmp_dup_patient_attribute
 GROUP BY num_values, num_chgd
 ORDER BY num_values, num_chgd;

prompt -- These next, if there are any, are unexpected.
prompt --   both have the same values, but both have DT_CHANGED dates!
prompt --   Either both were modified, or one had a value that was changed, then changed back...
SELECT survey_site_id site, 
       patient_id, data_name, num_values num_vals, num_chgd, row_count,
       max_chgd, whole_time_span, delta_created, max_date         
  FROM tmp_dup_patient_attribute WHERE num_values <= num_chgd;

prompt -- If there were any above, 
prompt -- FYI, these are the patient_attribute rows along with their history IDs, so you can check:

SELECT ph.survey_site_id site, ph.patient_id, ph.data_name, 
       ph.patient_attribute_id attr_id,
       ph.patient_attribute_history_id hist_id, 
       ph.data_value, ph.data_type, 
       ph.dt_changed, ph.dt_created
  FROM patient_attribute p, tmp_dup_patient_attribute tmp, patient_attribute_history ph
    WHERE p.survey_site_id = tmp.survey_site_id
      AND     p.patient_id = tmp.patient_id 
      AND      p.data_name = tmp.data_name
      AND tmp.num_values <= tmp.num_chgd
      AND p.patient_attribute_id = ph.patient_attribute_id
  ORDER BY p.patient_id, p.data_name, p.survey_site_id, ph.patient_attribute_history_id, ph.patient_attribute_id;

prompt -- Also FYI, these are the first 10 rows of the temporary table

SELECT survey_site_id site, 
       patient_id, data_name, num_values num_vals, num_chgd, row_count,
       max_chgd, whole_time_span, delta_created, max_date         
  FROM tmp_dup_patient_attribute 
       FETCH FIRST 10 ROWS ONLY;

prompt  -- FYI: if the above query has results, there are duplicate rows in patient_attribute to clean up
prompt  -- and the following query will show you the first 10 pairs of duplicate rows, just FYI

SELECT p.patient_attribute_id attr_id,
       p.survey_site_id site, p.patient_id patient, p.data_name, p.data_value, p.data_type, 
       p.dt_changed, p.dt_created, p.meta_version
  FROM patient_attribute p, tmp_dup_patient_attribute tmp
    WHERE p.survey_site_id = tmp.survey_site_id
      AND     p.patient_id = tmp.patient_id 
      AND      p.data_name = tmp.data_name
  ORDER BY p.patient_id, p.data_name, p.survey_site_id, p.dt_changed DESC, p.patient_attribute_id DESC
  FETCH FIRST 20 ROWS ONLY;

prompt  -- BAbove you see the first up-to-10 pairs of duplicates, FYI.  Below, some counts

select
  (select count(*) from patient                  ) total_num_patients,
  (select count(*) from patient_attribute        ) total_num_patient_attrs,
  (select count(*) from patient_attribute_history) total_num_attr_histories,
  (SELECT count(*) FROM tmp_dup_patient_attribute) num_dup_trios,
  (SELECT sum(row_count) FROM tmp_dup_patient_attribute) num_dup_rows
FROM DUAL;

prompt  -- The next script will remove earlier duplicates:  @ 3-pa-del-younger-dups
prompt  -- Note:  It does all the hard stuff, and the deletion.  (if there are no duplicates, it does nothing.)
prompt
