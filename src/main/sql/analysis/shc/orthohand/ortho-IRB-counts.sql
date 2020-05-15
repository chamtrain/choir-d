--
-- Summary counts for the Ortho Hand Surgery CHOIR site's yearly IRB protocol renewal
--
set heading off
set echo off
set feedback off
--
-- Get the number of patients enrolled
select count(*) || ' Patients enrolled to date.' as rptval from patient_attribute pa where SURVEY_SITE_ID=10 and DATA_NAME='orthoHandConsent' and data_value = 'Y'
                                                                                           and exists (select * from patient_attribute pa2 where pa.patient_id = pa2.patient_id and pa.survey_site_id = pa2.survey_site_id and pa2.data_name = 'participatesInSurveys'
                                                                                                                                                 and data_value = 'y');
--
-- Get the counts by gender value
select 'Gender breakdown' from dual union select ' ' from dual;
with
    enrolled as (
          select patient_id from patient_attribute pa where survey_site_id=10 and data_name='orthoHandConsent' and data_value = 'Y'
                                                            and exists (select * from patient_attribute pa2 where pa.patient_id = pa2.patient_id and pa.survey_site_id = pa2.survey_site_id
                                                                                                                  and pa2.data_name = 'participatesInSurveys' and data_value = 'y')),
    gender as (
    select  data_value as val, count(*) as cnt from patient_attribute pa, enrolled e where data_name = 'gender' and pa.patient_id = e.patient_id and survey_site_id = 10
    group by data_value
    union   select 'Unknown' as val, count(*) as cnt from enrolled e where not exists
    (select data_value from patient_attribute pa where data_name = 'gender' and pa.patient_id = e.patient_id and survey_site_id = 10))
select lpad(to_char(sum(cnt)),8,' ') || '  ' || val from gender
group by val order by val;
select    'Ethnicity breakdown' from dual union select ' ' from dual;
--
-- Get the counts by ethnicity value
with
    enrolled as (
          select patient_id from patient_attribute pa where survey_site_id=10 and data_name='orthoHandConsent' and data_value = 'Y'
                                                            and exists (select * from patient_attribute pa2 where pa.patient_id = pa2.patient_id and pa.survey_site_id = pa2.survey_site_id
                                                                                                                  and pa2.data_name = 'participatesInSurveys' and data_value = 'y')),
    ethnicity as (
    select  data_value as val, count(*) as cnt from patient_attribute pa, enrolled e where data_name = 'ethnicity' and pa.patient_id = e.patient_id and survey_site_id = 10
    group by data_value
    union   select 'Unknown' as val, count(*) as cnt from enrolled e where not exists
    (select data_value from patient_attribute pa where data_name = 'ethnicity' and pa.patient_id = e.patient_id and survey_site_id = 10))
select  lpad(to_char(sum(cnt)),8,' ') || '  ' || val from ethnicity
group by val order by val;
--
-- Find patients that are not participating that at some point consented
with participation as (
  select patient_id, data_value, dt_created dt, 'survey' from patient_attribute_history where survey_site_id = 10 and data_name = 'orthoHandConsent' union
  select patient_id, 'Y' as data_value, activity_dt dt, 'Consented' from activity  where survey_site_id = 10  and activity_type = 'Consented' union
  select patient_id, 'N' as data_value, activity_dt dt, 'Declined' from activity where survey_site_id = 10  and activity_type = 'Declined' union
  select patient_id, data_value, nvl(dt_changed,dt_created) dt, 'Attribute' from patient_attribute where survey_site_id = 10 and data_name = 'orthoHandConsent'),
    consents as (
          select patient_id , dt from participation where data_value = 'Y' ),
    declines as (
          select patient_id, dt from participation where data_value = 'N' ),
    patient_list as (
          select distinct d.patient_id from consents c, declines d where c.patient_id = d.patient_id ),
    last_actions as (
          select p.patient_id, (select max(dt) from declines d where d.patient_id = p.patient_id) last_decline,
                               (select max(dt) from consents c where c.patient_id = p.patient_id) last_consent from patient_list p )
select count(*) || ' Patients who had consented are no longer participating' from last_actions where last_decline > last_consent order by 1;
