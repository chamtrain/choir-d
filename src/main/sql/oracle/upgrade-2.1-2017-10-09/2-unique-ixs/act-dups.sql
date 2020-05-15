@column-formats
set pagesize 50
set linesize 200
column TOKEN format a20
column types format a20
column ass_exists format 9999999999
column ass_null   format 99999999

prompt 
prompt 
prompt 
prompt == This checks for token+site pairs which are used by more than one patient
prompt 
prompt == We will use a temporary table: tmp_act_dups
drop table tmp_act_dups;


CREATE TABLE tmp_act_dups AS
(SELECT survey_site_id, token, num_pats, min_act_types, max_act_types, ass_null, ass_exists
FROM (
  SELECT survey_site_id, token, count(*) num_pats, 
         min(num_act_types) min_act_types, max(num_act_types) max_act_types,
	 sum(ass_exists) ass_exists, sum(ass_null) ass_null
    FROM (
      SELECT survey_site_id, token, count(distinct(activity_type)) num_act_types,
        sum(DECODE(assessment_reg_id, null, 0, 1)) ass_exists, sum(DECODE(assessment_reg_id, null, 1, 0)) ass_null
        FROM activity WHERE token IS NOT NULL
       GROUP BY token, survey_site_id, patient_id)
  GROUP BY survey_site_id, token)
WHERE num_pats > 1);

select * from tmp_act_dups;

SELECT count(*) num_rows, count(distinct(token)) num_tokens, 
       sum(ass_equals) ass_equals, sum(ass_differ) ass_differ, sum(act_ass_nul) act_ass_nul FROM (
SELECT sr.token, DECODE(sr.assessment_reg_id, act.assessment_reg_id, 1, null, 0, 0) ass_equals,
                 DECODE(sr.assessment_reg_id, act.assessment_reg_id, 0, null, 0, 1) ass_differ,
                 DECODE(act.assessment_reg_id, null, 1, 0) act_ass_nul
  FROM survey_registration sr, activity act, tmp_act_dups tmp
 WHERE sr.token=tmp.token AND act.token=tmp.token AND act.assessment_reg_id is not null);



SELECT COUNT(*) FROM activity a
WHERE token is not null 
  AND assessment_reg_id is null
  AND NOT EXISTS (SELECT * FROM survey_registration sr WHERE a.token=sr.token AND a.patient_id=sr.patient_id);



column sr_assreg_id format 999999999999999999
column act_assreg_id format 999999999999999999
column act_pat_id    format a12
column sr_pat_id     format a12

SELECT sr.token, sr.assessment_reg_id sr_assreg_id, act.assessment_reg_id act_assreg_id,
       sr.patient_id sr_pat_id, act.patient_id act_pat_id
  FROM survey_registration sr, activity act, tmp_act_dups tmp
 WHERE sr.token=tmp.token AND act.token=tmp.token AND act.assessment_reg_id is not null
       AND sr.assessment_reg_id != act.assessment_reg_id;

--       , listagg(concat(substr(Patient_id,length(patient_id),1),DECODE(activity_type,'Chart Viewed','CV','Declined','De','Registration Deleted','RD',
--                    'Chart Printed','CP','Sent Response','SR','Validated','Va','Completed','Cm',
--                    'Registered','Re','Survey Type Changed','Ch','Consented','Cn','TC')),',') within group (Order by Patient_id) types

prompt == Some activities occur with both null and non-null tokens...

COLUMN max_dt format a8
COLUMN tok_nul format a7
COLUMN ass_nul format a7
break on activity_type;


SELECT num, ass_nul, tok_nul, activity_type, to_char(max_dt, ' YYYY-MM') max_dt from (
select activity_type, count(*) num, 'NUL--' ass_nul, 'NUL--' tok_nul,  max(activity_dt) max_dt from activity 
 WHERE assessment_reg_id is null AND token is null 
 GROUP BY activity_type
union
SELECT activity_type, count(*) num, 'NUL--' ass_nul,  '--SET' tok_nul, max(activity_dt) max_dt from activity 
 WHERE assessment_reg_id is null AND token is not null
 GROUP BY activity_type
union
select activity_type, count(*) num, '--SET' ass_nul, 'NUL--' tok_nul,  max(activity_dt) max_dt from activity 
 WHERE assessment_reg_id is not null AND token is null
 GROUP BY activity_type
union
select activity_type, count(*) num, '--SET' ass_nul, '--SET' tok_nul, max(activity_dt) max_dt from activity 
 WHERE assessment_reg_id is not null AND token is not null
 GROUP BY activity_type
) ORDER BY activity_type;


column activity_dt format a11

select both_null,  asreg_set_toknull, token_set_asregnull, both_set, to_char(activity_dt,'  YYYY-MM') activity_dt
FROM (
select count(*) both_null, 0 asreg_set_toknull, 0 token_set_asregnull, 0 both_set, max(ACTIVITY_DT) activity_dt
  from activity where token is null and assessment_reg_id is null
UNION
select 0 both_null, count(*) asreg_set_toknull, 0 token_set_asregnull, 0 both_set, max(ACTIVITY_DT) activity_dt
  from activity where token is null and assessment_reg_id is not null
UNION
select 0 both_null, 0 asreg_set_toknull, count(*) token_set_asregnull, 0 both_set, max(ACTIVITY_DT) activity_dt
  from activity where token is not null and assessment_reg_id is null
UNION
select 0 both_null, 0 asreg_set_toknull, 0 token_set_asregnull, count(*) both_set, max(ACTIVITY_DT) activity_dt
  from activity where token is not null and assessment_reg_id is not null);

