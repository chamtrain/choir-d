-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 7'
\echo '-- ======== Fix misleading tokens in the ACTIVITY table'

\echo
\echo '-- We will change any numeric tokens that are not in the SURVEY_REGISTRATION table'
\echo '--     to a dash followed by the activity_type'

UPDATE activity a SET token = concat('-',replace(activity_type,' ',''))
WHERE a.token IS NOT NULL
  AND a.token NOT LIKE '-%'
  AND a.assessment_reg_id is null
  AND NOT EXISTS (SELECT * FROM survey_registration sr 
                   WHERE a.token=sr.token AND a.survey_site_id=sr.survey_site_id AND a.patient_id=sr.patient_id);

\echo
\echo '-- After making the bad tokens non-numeric, here are the activity types'
\echo '--   And whether their assessment_reg_id is null or set'
\echo '--   And whether the token is null, set, or (new) starting with a minus sign:'
\echo

SELECT num, ass_nul, tok_nul, activity_type, to_char(max_dt, ' YYYY-MM') max_dt FROM
(SELECT activity_type, count(*) num, 
       CASE WHEN assessment_reg_id is null THEN '-null' ELSE 'SET++' END as ass_nul, 
       CASE WHEN token             is null THEN '-null' 
            WHEN token           like '-%' THEN '-----' ELSE 'SET++' END as tok_nul, 
       max(activity_dt) as max_dt
  FROM activity
 GROUP BY activity_type, ass_nul, tok_nul
) x WHERE num > 0 ORDER BY activity_type, ass_nul, tok_nul;

\echo
\echo '-- And here is the same output, but just for 2017 activities:'
\echo

SELECT num, ass_nul, tok_nul, activity_type, to_char(max_dt, ' YYYY-MM') max_dt FROM
(SELECT activity_type, count(*) num, 
       CASE WHEN assessment_reg_id is null THEN '-null' ELSE 'SET++' END as ass_nul, 
       CASE WHEN token             is null THEN '-null' 
            WHEN token           like '-%' THEN '-----' ELSE 'SET++' END as tok_nul, 
       max(activity_dt) as max_dt
  FROM activity WHERE to_char(activity_dt,'YY')='17'
 GROUP BY activity_type, ass_nul, tok_nul
) x WHERE num > 0 ORDER BY activity_type, ass_nul, tok_nul;

\echo '- We will also add a constraint that at least one of these is not null: TOKEN or ASSESSMENT_REG_ID'

ALTER TABLE activity ADD CONSTRAINT activity_token_or_asreg_set CHECK (token IS NOT NULL OR assessment_reg_id IS NOT NULL);

\echo
\echo '-- That should take care of tokens.  Next:  \\i 8-sr-chk-pat-tok.sql'
\echo

