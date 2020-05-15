@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 7
prompt -- ======== Fix misleading tokens in the ACTIVITY table

SET VERIFY OFF
define TBL=activity

column latest format a5

prompt -- We will change any tokens to -activity_type if they are not in the SURVEY_REGISTRATION table 
prompt --    This might take a minute...

UPDATE &TBL a SET token = concat('-',replace(activity_type,' ',''))
WHERE a.token is not null
  AND a.token NOT LIKE '-%'
  AND a.assessment_reg_id is null
  AND NOT EXISTS (SELECT * FROM survey_registration sr WHERE a.token=sr.token AND a.survey_site_id=sr.survey_site_id AND a.patient_id=sr.patient_id);

prompt -- After making the bad tokens non-numeric, here are the activity types
prompt --   And whether they the assessment_reg_id is null or set
prompt --   And whether the token is null, set, or newly start with a minus sign:

Break on activity_type 

SELECT num, ass_nul, tok_nul, activity_type, to_char(max_dt, ' YYYY-MM') max_dt FROM (
SELECT activity_type, count(*) num, '-null' ass_nul, '-----' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is null AND token LIKE '-%'
 GROUP BY activity_type
union
SELECT activity_type, count(*) num, '-null' ass_nul, '-null' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is null AND token is null
 GROUP BY activity_type
union
SELECT activity_type, count(*) num, '-null' ass_nul,  'SET++' tok_nul, max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is null AND token NOT LIKE '-%'
 GROUP BY activity_type
union
select activity_type, count(*) num, 'SET++' ass_nul, '-----' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is not null AND  token LIKE '-%'
 GROUP BY activity_type
union
select activity_type, count(*) num, 'SET++' ass_nul, '-null' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is not null AND  token is null
 GROUP BY activity_type
union
select activity_type, count(*) num, 'SET++' ass_nul, 'SET++' tok_nul, max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is not null AND token NOT LIKE '-%'
 GROUP BY activity_type
) WHERE num > 0 ORDER BY activity_type, ass_nul, tok_nul;


prompt -- And here is the same output, but just for 2017 activities:

SELECT num, ass_nul, tok_nul, activity_type, to_char(max_dt, ' YYYY-MM') max_dt FROM (
SELECT activity_type, count(*) num, '-null' ass_nul, '-----' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is null AND token LIKE '-%' AND to_char(activity_dt,'YY')='17'
 GROUP BY activity_type
union
SELECT activity_type, count(*) num, '-null' ass_nul, '-null' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is null AND token is null  AND to_char(activity_dt,'YY')='17'
 GROUP BY activity_type
union
SELECT activity_type, count(*) num, '-null' ass_nul,  'SET++' tok_nul, max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is null AND token NOT LIKE '-%' AND to_char(activity_dt,'YY')='17'
 GROUP BY activity_type
union
select activity_type, count(*) num, 'SET++' ass_nul, '-----' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is not null AND  token LIKE '-%' AND to_char(activity_dt,'YY')='17'
 GROUP BY activity_type
union
select activity_type, count(*) num, 'SET++' ass_nul, '-null' tok_nul,  max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is not null AND  token is null AND to_char(activity_dt,'YY')='17'
 GROUP BY activity_type
union
select activity_type, count(*) num, 'SET++' ass_nul, 'SET++' tok_nul, max(activity_dt) max_dt FROM &TBL
 WHERE assessment_reg_id is not null AND token NOT LIKE '-%' AND to_char(activity_dt,'YY')='17'
 GROUP BY activity_type
) WHERE num > 0 ORDER BY activity_type, ass_nul, tok_nul;

prompt - We will also add a constraint that at least one of these is not null: TOKEN or ASSESSMENT_REG_ID

ALTER TABLE &TBL ADD CONSTRAINT activity_token_or_asreg_set CHECK (token IS NOT NULL OR assessment_reg_id IS NOT NULL);


SET VERIFY ON
UNDEFINE TBL

prompt -- That should take care of tokens.  Next:  @ 8-sr-chk-pat-tok
prompt

