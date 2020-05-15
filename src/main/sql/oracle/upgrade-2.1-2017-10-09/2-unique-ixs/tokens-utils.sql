@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT tokens-utils
prompt -- There are tokens in 4 tables:
prompt --   SURVEY_REGISTRATION - token can't be null, survey_site_id+token is unique
prompt --   PATIENT_STUDY       - token can't be null, survey_site_id+token is unique
prompt --   SURVEY_TOKEN        - token can't be null, survey_site_id+token is unique
prompt --   ACTIVITY            - token may be null
prompt
prompt -- In checking the tokens on our Stanford system that has been up a bit over 5 years and
prompt -- has about 150,000 tokens, I used a bunch of queries.  They're here FYI...
prompt -- 

set pagesize 99
set linesize 200
column SURVEY_REG_ID format 99999999999999999999
column SITE          format 9999 
column PATIENT_ID    format a10
column SURVEY_TYPE   format a20
column TOKEN         format a20
column as_reg_id     format 99999999999999999999
column sreg_id       format 99999999999999999999
column SURVEY_NAME   format a30
column SURVEY_ORDER  format 999
column SURVEY_DT     format a28
column DT_CREATED    format a28
column MIN_CRDT      format a28
column MAX_CRDT      format a28
column DT_CHANGED    format a28
column min_dt_created format a28
column token_id      format 99999999999999999999
column sub_tok       format 99999999999999999999


prompt == find the survey_registration rows that have duplicate site+token, but no entry in survey_token.

SELECT Survey_reg_id FROM survey_registration
 WHERE token in 

prompt -- Give a command like the following for each row you want to delete:
prompt --    DELETE FROM survey_registration WHERE survey_reg_id = 1234;
prompt -- Then run this script again till it lists no more rows:  @ 6b-fix-site-token-dups
prompt


column maxid format 999999999999999999
column minid format 999999999999999999
column len   format 99
column num   format 99999999
column uniq_num   format 99999999
select length(survey_token) len, count(*) num, min(survey_token_id) minid, max(survey_token_id) maxid
from survey_token where length(survey_token) > 10 group by length(survey_token) order by length(survey_token);

select 'for whole table' extent, min(survey_token_id) minid, max(survey_token_id) maxid
from survey_token;



prompt == This gives the rows that use the duplicate values in the TMP_SR_DUPS table

SELECT tbl_nam, nr rows_wi_dup_sr_tokens
FROM
(SELECT 'SurveyToken' tbl_nam,  count(st.survey_token) nr
  FROM survey_token st, tmp_sr_dups tmp where survey_site_id=tmp.site and st.survey_token=tmp.token
UNION
 SELECT 'SurRegistrtn' tbl_nam,  count(sr.token) nr
 FROM survey_registration sr,        tmp_sr_dups tmp where sr.survey_site_id=tmp.site and sr.token=tmp.token
UNION
 SELECT 'PatientStudy' tbl_nam,  count(sr.token) nr
 FROM patient_study sr,              tmp_sr_dups tmp where sr.survey_site_id=tmp.site and sr.token=tmp.token
UNION
 SELECT 'Activity'     tbl_nam,  count(sr.token) nr
 FROM Activity sr,                   tmp_sr_dups tmp where sr.survey_site_id=tmp.site and sr.token=tmp.token
) x;



prompt == This tells the lengths of the tokens in all 5 tables - why are some long?

SELECT tbl_nam, len, num, uniq_num FROM
(SELECT 'SurveyToken' tbl_nam,   length(survey_token) len, count(*) num, count(distinct(survey_token)) uniq_num
  FROM survey_token              GROUP BY length(survey_token)
UNION
 SELECT 'SurRegistrtn' tbl_nam,  length(token), count(*) num, count(distinct(token)) uniq_num
 FROM survey_registration        GROUP BY length(token)
UNION
 SELECT 'PatientStudy' tbl_nam,  length(token), count(*) num, count(distinct(token)) uniq_num
 FROM patient_study              GROUP BY length(token)
UNION
 SELECT 'Activity'     tbl_nam,  length(token), count(*) num, count(distinct(token)) uniq_num
 FROM Activity                   GROUP BY length(token)
) x;


prompt == This outer-joins the SurveyToken.survey_token with each of the other 3 tables

BREAK ON tbl SKIP 1;

SELECT tbl, num,
       DECODE((survey_tok_token*2+tbl_token), 0, 'Table value was null',
              1, 'No match for tables token', 2, 'Not in table', 'In both') Description
  FROM
((SELECT 'SurRegistrtn' tbl, count(*)num,  survey_tok_token,  tbl_token FROM (
    SELECT (DECODE(st.survey_token, null, 0, 1)) survey_tok_token,
           (DECODE(sr.token,        null, 0, 1)) tbl_token
    FROM survey_token ST FULL OUTER JOIN survey_registration SR ON ST.survey_token = SR.token AND ST.survey_site_id=SR.survey_site_id)
  GROUP BY survey_tok_token,  tbl_token)
UNION
(SELECT 'PatientStudy' tbl, count(*)num,  survey_tok_token,  tbl_token FROM (
    SELECT (DECODE(st.survey_token, null, 0, 1)) survey_tok_token,
           (DECODE(sr.token,        null, 0, 1)) tbl_token
    FROM survey_token ST FULL OUTER JOIN patient_study SR ON ST.survey_token = SR.token AND ST.survey_site_id=SR.survey_site_id)
  GROUP BY survey_tok_token,  tbl_token)
UNION
(SELECT 'Activity' tbl, count(*)num,  survey_tok_token,  tbl_token FROM (
    SELECT (DECODE(st.survey_token, null, 0, 1)) survey_tok_token,
           (DECODE(sr.token,        null, 0, 1)) tbl_token
    FROM survey_token ST FULL OUTER JOIN activity SR ON ST.survey_token = SR.token AND ST.survey_site_id=SR.survey_site_id)
  GROUP BY survey_tok_token,  tbl_token)
) x
 ORDER BY tbl, survey_tok_token, tbl_token;



prompt == This outer-joins the SurveyRegistration.token with each of the other 4 tables

SELECT tbl, num,
       DECODE((survey_tok_token*2+tbl_token), 0, 'Table value was null',
              1, 'No match for tables token', 2, 'Not in table', 'In both') Description
  FROM
((SELECT 'SurveyToken' tbl, count(*)num,  survey_tok_token,  tbl_token FROM (
    SELECT (DECODE(st.token, null, 0, 1)) survey_tok_token,
           (DECODE(sr.survey_token,        null, 0, 1)) tbl_token
    FROM survey_registration ST FULL OUTER JOIN survey_token SR ON ST.token = SR.survey_token AND ST.survey_site_id=SR.survey_site_id)
  GROUP BY survey_tok_token,  tbl_token)
UNION
(SELECT 'PatientStudy' tbl, count(*)num,  survey_tok_token,  tbl_token FROM (
    SELECT (DECODE(st.token, null, 0, 1)) survey_tok_token,
           (DECODE(sr.token, null, 0, 1)) tbl_token
    FROM survey_registration ST FULL OUTER JOIN patient_study SR ON ST.token = SR.token AND ST.survey_site_id=SR.survey_site_id)
  GROUP BY survey_tok_token,  tbl_token)
UNION
(SELECT 'Activity' tbl, count(*)num,  survey_tok_token,  tbl_token FROM (
    SELECT (DECODE(st.token, null, 0, 1)) survey_tok_token,
           (DECODE(sr.token, null, 0, 1)) tbl_token
    FROM survey_registration ST FULL OUTER JOIN activity SR ON ST.token = SR.token AND ST.survey_site_id=SR.survey_site_id)
  GROUP BY survey_tok_token,  tbl_token)
) x
 ORDER BY tbl, survey_tok_token, tbl_token;


