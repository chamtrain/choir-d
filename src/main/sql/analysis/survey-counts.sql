-- Find the number of unique patients that have completed at least one assessment
-- and the total number of assessments.
with real_patients as (
        select patient_id from patient where last_name not like '%Test-Patient'
     ), completed_tokens as (
        select to_char(token) as token, survey_site_id from activity where activity_type='Completed'
        union
        select survey_token, survey_site_id from survey_token where is_complete='Y'
     )
select display_name as "Site/Clinic",
       completed_assessments as "Completed Assessments",
       unique_patients as "Unique Patients"
from (
        select t.survey_site_id,
               count(distinct t.token) as completed_assessments,
               count(distinct p.patient_id) as unique_patients
          from real_patients p inner join survey_registration r on p.patient_id=r.patient_id
                               inner join completed_tokens t on to_char(r.token)=t.token
          group by t.survey_site_id
) cnts join survey_site ss on cnts.survey_site_id=ss.survey_site_id
;
