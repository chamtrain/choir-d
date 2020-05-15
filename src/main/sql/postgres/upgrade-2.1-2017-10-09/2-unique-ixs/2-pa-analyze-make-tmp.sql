-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 2'
\echo '-- ======== Analyze the duplicate PATIENT_ATTRIBUTEs with a temporary table'
\echo
\echo '-- (If you ran this already, we will drop the temporary table using some PL/SQL)'

DROP TABLE IF EXISTS tmp_dup_patient_attribute;

\echo '-- we will make a table with all the site/patient/attribute_name trios that have duplicates,'

CREATE TABLE tmp_dup_patient_attribute AS (
SELECT * FROM (
  SELECT p.survey_site_id, p.patient_id, p.data_name
       , count(distinct(p.data_value)) num_values
       , coalesce(count(dt_changed),0) num_chgd
       , count(*) row_count
       , max(DT_CHANGED) max_chgd
       , (max(DT_CHANGED) - min(DT_CREATED)) whole_time_span
       , (max(DT_CREATED) - min(DT_CREATED)) delta_created
       , max(greatest(COALESCE(DT_CHANGED, DT_CREATED), DT_CREATED)) max_date
    FROM patient_attribute p
  GROUP BY p.survey_site_id, p.patient_id, p.data_name
) x WHERE row_count > 1);

\echo '-- Add an index so joins run faster'

CREATE UNIQUE INDEX tmp_dup_patient_at_ix 
    ON tmp_dup_patient_attribute ( survey_site_id, patient_id, data_name );

\echo '-- This analysis table is:  TMP_DUP_PATIENT_ATTRIBUTE'
\echo
\echo '-- We expect ONLY pairs, but this query will show if there are triples or quads, and the number of each'

SELECT count(*) num_of_each_kind, row_count "2=pairs,3=triples,4=..."
 FROM tmp_dup_patient_attribute
 GROUP BY row_count;

\echo '-- FYI: This tell the number of different types, '
\echo '--   and if some were updated (and thus have DT_CHANGED dates), and any multiple values'

SELECT count(*) number_like_this, 
   CASE WHEN num_values=1 THEN 'Both have same value' 
        WHEN num_values=2 THEN 'Rows have 2 diff values' ELSE concat('huh?: ',num_values) END num_diff_values
  ,CASE WHEN num_chgd=0 THEN 'None were updated'
        WHEN num_chgd=1 THEN 'One was updated' 
        WHEN num_chgd=2 THEN 'Both were updated!' ELSE concat('huh?: ',num_chgd) END num_wi_chgd_dates
 FROM tmp_dup_patient_attribute
 GROUP BY num_values, num_chgd
 ORDER BY num_values, num_chgd;

\echo '-- These next, if there are any, are unexpected.'
\echo '--   both have the same values, but both have DT_CHANGED dates!'
\echo '--   Either both were modified, or one had a value that was changed, then changed back...'
\echo
SELECT survey_site_id site, 
       patient_id, data_name, num_values num_vals, num_chgd, row_count,
       max_chgd, whole_time_span, delta_created, max_date         
  FROM tmp_dup_patient_attribute WHERE num_values <= num_chgd;

\echo '-- If there were any above, '
\echo '-- FYI, these are the patient_attribute rows along with their history IDs, so you can check:'
\echo
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

\echo
\echo '-- Also FYI, these are the first 10 rows of the temporary table'
\echo
SELECT survey_site_id site, 
       patient_id, data_name, num_values num_vals, num_chgd, row_count,
       max_chgd, whole_time_span, delta_created, max_date         
  FROM tmp_dup_patient_attribute LIMIT(10);

\echo
\echo ' -- FYI: if the above query has results, there are duplicate rows in patient_attribute to clean up'
\echo ' -- and the following query will show you the first 10 pairs of duplicate rows, just FYI'
\echo
SELECT p.patient_attribute_id attr_id,
       p.survey_site_id site, p.patient_id patient, p.data_name, p.data_value, p.data_type, 
       p.dt_changed, p.dt_created, p.meta_version
  FROM patient_attribute p, tmp_dup_patient_attribute tmp
    WHERE p.survey_site_id = tmp.survey_site_id
      AND     p.patient_id = tmp.patient_id 
      AND      p.data_name = tmp.data_name
  ORDER BY p.patient_id, p.data_name, p.survey_site_id, p.dt_changed DESC, p.patient_attribute_id DESC
  limit(20);

\echo
\echo ' -- BAbove you see the first up-to-10 pairs of duplicates, FYI.  Below, some counts'
\echo
select
  (select count(*) from patient                  ) total_num_patients,
  (select count(*) from patient_attribute        ) total_num_patient_attrs,
  (select count(*) from patient_attribute_history) total_num_attr_histories,
  (SELECT count(*) FROM tmp_dup_patient_attribute) num_dup_trios,
  (SELECT sum(row_count) FROM tmp_dup_patient_attribute) num_dup_rows;

\echo ' -- The next script will remove earlier duplicates:  \\i 3-pa-del-younger-dups.sql'
\echo ' -- Note:  It does all the hard stuff, and the deletion.  (if there are no duplicates, it does nothing.)'
\echo
