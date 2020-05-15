-- Real, completed surveys
select count(*) from survey_token where is_complete='Y' and survey_site_id=1 
   and survey_token in (select token from survey_registration where patient_id not in (select patient_id from patient where last_name = 'Test-Patient'));

-- Average time and standard deviation for real, completed, INITIAL surveys
select trunc(avg(time_seconds)/60) || ':' || mod(round(avg(time_seconds)),60) avg_initial_time, round(stddev(time_seconds)) from (
select round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress where survey_token_id in (
select survey_token_id from survey_token where is_complete='Y' and survey_site_id=1 
   and survey_token in (select token from survey_registration where survey_type like 'Initial%' and patient_id not in (select patient_id from patient where last_name = 'Test-Patient'))
) group by survey_token_id);
-- 22:12 sd 381

-- Average time and standard deviation for real, completed, FOLLOWUP surveys
select trunc(avg(time_seconds)/60) || ':' || mod(round(avg(time_seconds)),60) avg_followup_time, round(stddev(time_seconds)) from (
select round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress where survey_token_id in (
select survey_token_id from survey_token where is_complete='Y' and survey_site_id=1 
   and survey_token in (select token from survey_registration where survey_type like 'FollowUp%' and patient_id not in (select patient_id from patient where last_name = 'Test-Patient'))
) group by survey_token_id);
-- 8:51 sd 213

select distinct survey_type from survey_registration;

-- Average time and standard deviation for real, completed, surveys of a specific type
select trunc(avg(time_seconds)/60) || ':' || mod(round(avg(time_seconds)),60) avg_time, 
       trunc(stddev(time_seconds)/60) || ':' || mod(round(stddev(time_seconds)),60) standard_deviation from (
select round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress where survey_token_id in (
select survey_token_id from survey_token where is_complete='Y' and survey_site_id=1 
   and survey_token in (select token from survey_registration where survey_type = 'FollowUp' and patient_id not in (select patient_id from patient where last_name = 'Test-Patient'))
) group by survey_token_id);

-- Average time, number of questions asked, and std deviations for StanfordCat - PROMIS questionnaires
with timings as (
  select section_id, round(avg(num_questions), 1) avg_questions, round(stddev(num_questions), 2) qsd, trunc(avg(time_seconds)/60) || ':' || mod(round(avg(time_seconds)),60) avg_time, round(stddev(time_seconds)) std_dev from (
    select section_id, to_number(max(question_step_number))-to_number(min(question_step_number)) num_questions, round(sum(extract(second from (answer_time-question_time)))) time_seconds from survey_progress
    where section_id in ( select study_code from study where survey_system_id = (select survey_system_id from survey_system where survey_system_name = 'StanfordCat')) and survey_token_id in (
      select survey_token_id from survey_token where is_complete='Y' and survey_site_id=9
      and survey_token in (select token from survey_registration where  patient_id not in (select patient_id from patient where last_name = 'Test-Patient'))
    ) group by survey_token_id, section_id
    ),
   study where section_id = study_code group by title, section_id order by 1)
select nvl(title, study_description) Questionnaire, /*section_id,*/ avg_time as "Avg Time", std_dev as "Time SD", avg_questions "Avg #Quest", qsd "Quest SD"
from study s, timings t where s.study_code = t.section_id;