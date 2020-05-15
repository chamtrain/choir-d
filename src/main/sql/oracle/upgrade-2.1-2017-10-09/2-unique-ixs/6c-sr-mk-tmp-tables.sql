@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 6c
prompt -- ======== Make tmp tables to fix site/token duplicates in SURVEY_REGISTRATION ========
prompt

prompt -- This script creates a small table with your duplicate survey_registration site/token rows
prompt -- and then shows you the multiple rows from one set of duplicates.
prompt -- 

set pagesize 99
set linesize 200
column SURVEY_REG_ID format 99999999999999999999
column SITE          format 9999 
column PATIENT_ID    format a10
column SURVEY_TYPE   format a24
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
column min_n         format 99999999999999999999
column max_n         format 99999999999999999999
define SITE=Choir

SET VERIFY OFF

prompt == First, we will drop 3 temporary tables if they exist, then make them

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE tmp_sr_dups';
   EXECUTE IMMEDIATE 'DROP TABLE tmp_sr_rows';
   EXECUTE IMMEDIATE 'DROP TABLE tmp_sub_tokens';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

prompt == Create a small table of the duplicate site and token pairs, plus FYI their dates

CREATE TABLE tmp_sr_dups AS
(SELECT site, token, num_dups, min_crdt, max_crdt FROM
   (SELECT count(*) num_dups, survey_site_id site, token, 
           min(dt_created) min_crdt, max(dt_created) max_crdt FROM survey_registration 
     GROUP BY survey_site_id, token
   ) WHERE num_dups > 1);

prompt == Here they are:

SELECT * FROM tmp_sr_dups;

prompt == And FYI, here is more info about their rows:

column survey_dt format a9
column dt_created format a10
column dt_changed format a10
column survey_name format a20
column survey_reg_id format 999999999999999999

SELECT a.SURVEY_SITE_ID SITE, a.token, a.SURVEY_REG_ID, a.PATIENT_ID,
       to_Char(a.SURVEY_DT,'YY-MON-DD') survey_dt, a.SURVEY_TYPE, 
       to_char(a.DT_CREATED,'YY-MON-DD') DT_CREATED, to_char(a.DT_CHANGED,'YY-MON-DD') DT_CHANGED,
       a.ASSESSMENT_REG_ID as_reg_id, a.SURVEY_NAME, a.SURVEY_ORDER
FROM survey_registration a, tmp_sr_dups b
WHERE  a.survey_site_id = b.site AND a.token = b.token ORDER BY SITE, TOKEN, a.SURVEY_REG_ID;

prompt -- Make a small table tmp_sr_rows with the rows of SURVEY_REGISTRATION that are dups.
prompt --   We add PATIENT_IDS and TOKEN_IDS for the rows where the SITE,TOKEN,PATIENT_ID are in patient_study

define SUBTOK0=10000

CREATE TABLE tmp_sr_rows AS (
  SELECT  rownum rn, t.site, t.token, 
          (SELECT survey_token_id FROM survey_token st
            WHERE t.token=st.survey_token AND t.site=st.survey_site_id AND
                  EXISTS (SELECT * FROM patient_study p 
                          WHERE sr.survey_reg_id=p.survey_reg_id AND sr.patient_id=p.patient_id)) token_id,
			            sr.survey_reg_id sreg_id, sr.patient_id, &SUBTOK0 sub_tok
    FROM  tmp_sr_dups t JOIN survey_registration sr
           ON t.site=sr.survey_site_id AND t.token=sr.token
);

prompt -- If none of a set of duplicates has a token_id, we need to change all but one of them, so we will
prompt --   keep the one with the smallest patient_id by setting its token_id to 1 (not null) in our tmp table

UPDATE tmp_sr_rows SET token_id = 1 
 WHERE rn IN 
    (SELECT rn FROM tmp_sr_rows tmp WHERE
       rn NOT IN (SELECT rn
                    FROM tmp_sr_rows t, (SELECT site, token FROM tmp_sr_rows t WHERE t.token_id IS NOT NULL) q
                   WHERE t.site=q.site AND t.token=q.token)
       AND tmp.patient_id != (SELECT min(patient_id) FROM tmp_sr_rows t
                              WHERE tmp.site=t.site AND tmp.token=t.token));


prompt -- Now we make a small table of new, substitute tokens at 10,000

CREATE TABLE tmp_sub_tokens AS
  (select &SUBTOK0 ordr, &SUBTOK0 + rownum n from (SELECT 1 FROM dual CONNECT BY LEVEL < 1000));

prompt -- We delete any that happen to already be used by SURVEY_REGISTRATION

DELETE FROM tmp_sub_tokens WHERE EXISTS (SELECT * FROM survey_registration WHERE token=n);

prompt -- And we number the rows of these substitute tokens table

UPDATE tmp_sub_tokens SET ordr = rownum;

-- prompt -- FYI: Here's the min, max and number of rows in the table
-- SELECT min(n) min_n, max(n) max_n, count(*) num_rows FROM tmp_sub_tokens;

prompt -- Now we number the rows of tmp_sr_rows that need their token changed (TOKEN_ID is null)

UPDATE tmp_sr_rows SET rn = rownum WHERE TOKEN_ID IS NULL;

-- UPDATE tmp_sr_rows SET 
--   rn = rownum + (SELECT MAX(rn) FROM tmp_sr_rows WHERE TOKEN_ID IS NULL)
--  WHERE TOKEN_ID IS NOT NULL;

prompt -- Put the first substitute tokens into the rows of tmp_sr_rows that have a NULL token_id

UPDATE tmp_sr_rows SET sub_tok = (SELECT n FROM tmp_sub_tokens WHERE ordr=rn) 
WHERE token_id IS NULL;

prompt -- And here are the rows to update (the ones with NULL token_id)

select * from tmp_sr_rows ORDER BY TOKEN_ID, RN;

prompt -- If this looks good, fix the duplicate tokens with null TOKEN_IDs
prompt --    in the next script: @ 6d-sr-fix-site-token-dups
prompt
SET VERIFY ON
