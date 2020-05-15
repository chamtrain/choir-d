-- Look at patients seen on each day and categorize them based on whether,
-- during the prior 14 days, the started and/or completed an assessment. Also
-- track decline status for the patients as of that day.
with
    daily_visits as (select distinct trunc(survey_dt) as appt_day, patient_id from survey_registration where registration_type='a' and visit_type!='CONF'),
    survey_activity as (
          select r.patient_id, trunc(s.last_active) last_active_day, t.is_complete
          from survey_token t join survey_registration r on (t.survey_token=to_char(r.token) and r.survey_site_id=1)
            join survey_session s on (t.survey_token_id=s.survey_token_id and t.last_session_number=s.session_number and s.survey_site_id=1)
          where t.survey_site_id=1
  ),
    daily_visits2 as (
          select appt_day, patient_id,
            (select enroll_status from (
              select first_value(activity_type) over (order by activity_dt desc) as enroll_status
              from activity
              where activity_type in ('Declined', 'Consented') and patient_id=v.patient_id and activity_dt <= v.appt_day+1
            ) where rownum=1) as enroll_status,
            (select count(*) from survey_activity where patient_id=v.patient_id and last_active_day between v.appt_day-14 and v.appt_day and is_complete='N') as nbr_partial_14d,
            (select count(*) from survey_activity where patient_id=v.patient_id and last_active_day between v.appt_day and v.appt_day+1 and is_complete='N') as nbr_partial_day_of,
            (select count(*) from survey_activity where patient_id=v.patient_id and last_active_day between v.appt_day-14 and v.appt_day and is_complete='Y') as nbr_complete_14d,
            (select count(*) from survey_activity where patient_id=v.patient_id and last_active_day between v.appt_day and v.appt_day+1 and is_complete='Y') as nbr_complete_day_of
          from daily_visits v
  )
select to_char(appt_day, 'DD-MON-YYYY') as appt_day, nbr_patients, nbr_patients-nbr_decline as nbr_no_decline,
       nbr_complete+nbr_partial as nbr_partial_or_complete, nbr_complete from (
  select appt_day, count(patient_id) as nbr_patients,
                   sum(decode(enroll_status,'Declined',1,0)) as nbr_decline,
                   sum(decode(nbr_partial_14d+nbr_partial_day_of,0,0,1)) as nbr_partial,
                   sum(decode(nbr_complete_14d+nbr_complete_day_of,0,0,1)) as nbr_complete
  from daily_visits2
  where patient_id not in (select patient_id from patient_test_only) and appt_day < trunc(sysdate) and appt_day > to_date('29-OCT-2013')
  group by appt_day
  order by appt_day asc
);
